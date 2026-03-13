package com.pulxo.steps.domain.repository

import com.pulxo.steps.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

interface StepRepository {
    /**
     * Emits real-time step counts for the current day.
     */
    fun getTodayStepsFlow(): Flow<Int>

    /**
     * Get statistics for a specific range of days.
     */
    suspend fun getStatsForDateRange(startEpochDay: Long, endEpochDay: Long): List<DailyStats>

    /**
     * Get stats for a specific day.
     */
    suspend fun getStatsForDate(epochDay: Long): DailyStats?
    
    /**
     * Saves raw delta steps (e.g., +10 steps) to the local database, 
     * updating the current day's total.
     */
    suspend fun addSteps(steps: Int, timestamp: Long)
}
