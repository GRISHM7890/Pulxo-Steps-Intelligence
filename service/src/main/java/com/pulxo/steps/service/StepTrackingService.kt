package com.pulxo.steps.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.pulxo.steps.domain.repository.SensorDataSource
import com.pulxo.steps.domain.repository.StepRepository
import com.pulxo.steps.domain.repository.AppContainerProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StepTrackingService : Service() {

    // In a real app, inject these via Hilt
    private lateinit var sensorDataSource: SensorDataSource
    private lateinit var stepRepository: StepRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private var previousStepCount: Int = -1

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Starting step tracker..."))
        
        val container = (application as AppContainerProvider).container
        sensorDataSource = container.sensorDataSource
        stepRepository = container.stepRepository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopSelf()
            return START_NOT_STICKY
        }
        
        startTracking()
        return START_STICKY // Restart service if system kills it
    }

    private fun startTracking() {
        // Stop any existing tracking
        sensorDataSource.stopListening()
        
        // Start listening to hardware sensor
        sensorDataSource.startListening()

        sensorDataSource.stepCountFlow
            .onEach { totalSteps ->
                if (previousStepCount == -1) {
                    previousStepCount = totalSteps
                } else {
                    val delta = totalSteps - previousStepCount
                    if (delta > 0) {
                        // Persist immediately. In production, batch this to save battery!
                        stepRepository.addSteps(delta, System.currentTimeMillis())
                        previousStepCount = totalSteps
                        
                        // Update notification
                        updateNotification("Steps today: $totalSteps")
                    }
                }
            }
            .launchIn(serviceScope)
    }

    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification(text))
    }

    private fun createNotification(content: String): Notification {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Pulxo Step Intelligence")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_directions) // Placeholder icon
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Step Tracking Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the step tracker running in the background"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorDataSource.stopListening()
        serviceJob.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val CHANNEL_ID = "step_tracker_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    }
}
