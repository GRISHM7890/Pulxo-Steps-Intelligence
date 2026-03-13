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

    override fun startListening() {
        stepCounterSensor?.let {
            // SENSOR_DELAY_NORMAL is sufficient for step tracking and saves battery
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        accelSensor?.let {
            // SENSOR_DELAY_UI for accelerometer
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
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
                detectVehicleMovement(event)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for step counter
    }

    private fun detectVehicleMovement(event: SensorEvent) {
        // Intelligent filtering logic to avoid false steps from vehicle movement.
        // Extremely basic heuristic: sustained high acceleration without rhythmic step-like patterns is likely a vehicle.
        // For production, a more sophisticated Fast Fourier Transform (FFT) or ML model would be needed.
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        
        val acceleration = sqrt((x * x + y * y + z * z).toDouble()) - SensorManager.GRAVITY_EARTH
        
        // Simple mock detection for illustration:
        // if sustained acceleration is found, set true.
        if (acceleration > 5f) {
             _isVehicleMovementDetected.value = true
        } else if (acceleration < 1f) {
             _isVehicleMovementDetected.value = false
        }
    }
}
