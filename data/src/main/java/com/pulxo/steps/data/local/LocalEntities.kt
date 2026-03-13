package com.pulxo.steps.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_stats")
data class DailyStatsEntity(
    @PrimaryKey val dateEpochDays: Long,
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val caloriesBurned: Float = 0f,
    val activeTimeMinutes: Int = 0
)

@Entity(tableName = "step_history")
data class StepRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val stepsDelta: Int
)
