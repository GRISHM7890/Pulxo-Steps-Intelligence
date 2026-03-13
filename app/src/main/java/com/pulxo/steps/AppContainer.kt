package com.pulxo.steps

import android.content.Context
import androidx.room.Room
import com.pulxo.steps.data.local.StepDatabase
import com.pulxo.steps.data.repository.StepRepositoryImpl
import com.pulxo.steps.data.sensor.AndroidSensorDataSource
import com.pulxo.steps.domain.repository.SensorDataSource
import com.pulxo.steps.domain.repository.StepRepository

import com.pulxo.steps.data.repository.FirebaseAuthRepository
import com.pulxo.steps.data.repository.FirebaseSyncRepository
import com.pulxo.steps.domain.repository.AuthRepository
import com.pulxo.steps.domain.repository.SyncRepository
import com.pulxo.steps.domain.repository.AppContainer
import com.pulxo.steps.domain.repository.AppContainerProvider

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val stepDatabase: StepDatabase by lazy {
        Room.databaseBuilder(
            context,
            StepDatabase::class.java,
            "steps_db"
        ).build()
    }

    override val stepRepository: StepRepository by lazy {
        StepRepositoryImpl(stepDatabase.stepDao)
    }

    override val sensorDataSource: SensorDataSource by lazy {
        AndroidSensorDataSource(context)
    }

    override val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }

    override val syncRepository: SyncRepository by lazy {
        FirebaseSyncRepository()
    }
}
