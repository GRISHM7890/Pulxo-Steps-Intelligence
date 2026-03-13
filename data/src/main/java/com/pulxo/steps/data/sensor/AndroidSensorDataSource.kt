package com.pulxo.steps.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.pulxo.steps.domain.repository.SensorDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.sqrt

class AndroidSensorDataSource(
    context: Context
) : SensorDataSource, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepCounterSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private val accelSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _stepCountFlow = MutableStateFlow(0)
    override val stepCountFlow: StateFlow<Int> = _stepCountFlow.asStateFlow()

    private val _isVehicleMovementDetected = MutableStateFlow(false)
    override val isVehicleMovementDetected: StateFlow<Boolean> = _isVehicleMovementDetected.asStateFlow()

    // --- Step Detector Engine State ---
    private var isUsingFallback = false
    private var fallbackStepCount = 0

    private var lastAccels = FloatArray(3)
    private var gravity = 9.81f
    
    // Adaptive threshold vars
    private var currentThreshold = 2.0f 
    private val MIN_THRESHOLD = 1.0f
    
    // Cadence validation
    private var lastStepTime = 0L
    private val MIN_TIME_BETWEEN_STEPS_MS = 250L
    private val MAX_TIME_BETWEEN_STEPS_MS = 2000L
    
    // False positive prevention
    private var consecutiveSteps = 0
    private val STEPS_TO_START_COUNTING = 4

    override fun startListening() {
        if (stepCounterSensor != null) {
            isUsingFallback = false
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL)
            
            // Also listen to accel for vehicle detection if hardware counter is present
            accelSensor?.let {
                sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            }
        } else if (accelSensor != null) {
            isUsingFallback = true
            // Fast delay needed for accurate waveform analysis
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_STEP_COUNTER -> {
                val currentSteps = event.values[0].toInt()
                _stepCountFlow.value = currentSteps
            }
            Sensor.TYPE_ACCELEROMETER -> {
                if (isUsingFallback) {
                    processAccelerometerStep(event)
                } else {
                    detectVehicleMovement(event)
                }
            }
        }
    }

    /**
     * High-Accuracy Accelerometer Fallback Engine
     * Uses Low-Pass Filter, Adaptive Thresholds, and Cadence Validation
     */
    private fun processAccelerometerStep(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        // Sensor timestamp is in nanoseconds, convert to MS for cadence math
        val timestampMs = event.timestamp / 1_000_000L

        // 1. Low pass filter (EMA)
        val alpha = 0.8f
        lastAccels[0] = alpha * lastAccels[0] + (1 - alpha) * x
        lastAccels[1] = alpha * lastAccels[1] + (1 - alpha) * y
        lastAccels[2] = alpha * lastAccels[2] + (1 - alpha) * z
        
        // 2. Magnitude calculation (avoiding Math.pow for performance)
        val magnitude = sqrt((lastAccels[0] * lastAccels[0]) + (lastAccels[1] * lastAccels[1]) + (lastAccels[2] * lastAccels[2]))
        
        // Dynamic Gravity Baseline
        gravity = (0.9f * gravity) + (0.1f * magnitude)
        val linearAcceleration = magnitude - gravity
        
        // 3. Peak Detection & Adaptive Threshold
        if (linearAcceleration > currentThreshold) {
            val timeSinceLastStep = timestampMs - lastStepTime
            
            // 4. Cadence validation
            if (timeSinceLastStep in MIN_TIME_BETWEEN_STEPS_MS..MAX_TIME_BETWEEN_STEPS_MS) {
                consecutiveSteps++
                
                if (consecutiveSteps >= STEPS_TO_START_COUNTING) {
                    // If we just hit the trigger count, emit the buffered steps
                    val stepsToAdd = if (consecutiveSteps == STEPS_TO_START_COUNTING) STEPS_TO_START_COUNTING else 1
                    fallbackStepCount += stepsToAdd
                    _stepCountFlow.value = fallbackStepCount
                }
                lastStepTime = timestampMs
            } else if (timeSinceLastStep > MAX_TIME_BETWEEN_STEPS_MS) {
                // Break in rhythm (e.g. car stop, desk pickup)
                consecutiveSteps = 1
                lastStepTime = timestampMs
            }
        }
        
        // Adapt threshold based on recent acceleration intensity
        currentThreshold = max(MIN_THRESHOLD, (currentThreshold * 0.9f) + (linearAcceleration * 0.1f))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    private fun detectVehicleMovement(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
        
        if (acceleration > 5f) {
             _isVehicleMovementDetected.value = true
        } else if (acceleration < 1f) {
             _isVehicleMovementDetected.value = false
        }
    }
}
