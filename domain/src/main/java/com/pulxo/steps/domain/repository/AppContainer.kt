package com.pulxo.steps.domain.repository

/**
 * Interface for dependency injection container.
 */
interface AppContainer {
    val stepRepository: StepRepository
    val sensorDataSource: SensorDataSource
}

/**
 * Interface for application classes that provide an AppContainer.
 */
interface AppContainerProvider {
    val container: AppContainer
}
