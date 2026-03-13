package com.pulxo.steps

import android.app.Application
import com.pulxo.steps.domain.repository.AppContainer
import com.pulxo.steps.domain.repository.AppContainerProvider

// In a real app we would annotate with @HiltAndroidApp
class PulxoStepsApp : Application(), AppContainerProvider {
    
    // Manual dependency injection container
    override lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
