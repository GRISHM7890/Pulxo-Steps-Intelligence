package com.pulxo.steps.domain.model

data class DailyStats(
    val dateEpochDays: Long, // Epoch day
    val steps: Int,
    val distanceMeters: Float,
    val caloriesBurned: Float,
    val activeTimeMinutes: Int
)

data class StepRecord(
    val timestamp: Long,
    val steps: Int
)

data class PulxoUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)
