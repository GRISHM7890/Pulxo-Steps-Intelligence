package com.pulxo.steps.domain.repository

import kotlinx.coroutines.flow.Flow

interface SensorDataSource {
    /**
     * Emits the absolute hardware step count provided by the OS.
     */
    val stepCountFlow: Flow<Int>
    
    /**
     * Emits true if vehicular movement is currently detected.
     */
    val isVehicleMovementDetected: Flow<Boolean>

    fun startListening()
    fun stopListening()
}
