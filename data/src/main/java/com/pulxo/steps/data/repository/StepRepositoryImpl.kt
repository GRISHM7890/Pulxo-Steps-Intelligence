package com.pulxo.steps.data.repository

import com.pulxo.steps.data.local.DailyStatsEntity
import com.pulxo.steps.data.local.StepDao
import com.pulxo.steps.data.local.StepRecordEntity
import com.pulxo.steps.domain.model.DailyStats
import com.pulxo.steps.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StepRepositoryImpl(
    private val stepDao: StepDao
) : StepRepository {

    override fun getTodayStepsFlow(): Flow<Int> {
        val todayEpoch = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        return stepDao.getStatsForDayFlow(todayEpoch).map { it?.steps ?: 0 }
    }

    override suspend fun getStatsForDateRange(
        startEpochDay: Long,
        endEpochDay: Long
    ): List<DailyStats> {
        return stepDao.getStatsForDateRange(startEpochDay, endEpochDay).map { entity ->
            DailyStats(
                dateEpochDays = entity.dateEpochDays,
                steps = entity.steps,
                distanceMeters = entity.distanceMeters,
                caloriesBurned = entity.caloriesBurned,
                activeTimeMinutes = entity.activeTimeMinutes
            )
        }
    }

    override suspend fun getStatsForDate(epochDay: Long): DailyStats? {
        return stepDao.getStatsForDay(epochDay)?.let { entity ->
            DailyStats(
                dateEpochDays = entity.dateEpochDays,
                steps = entity.steps,
                distanceMeters = entity.distanceMeters,
                caloriesBurned = entity.caloriesBurned,
                activeTimeMinutes = entity.activeTimeMinutes
            )
        }
    }

    override suspend fun addSteps(steps: Int, timestamp: Long) {
        if (steps <= 0) return
        
        val epochDay = timestamp / (1000 * 60 * 60 * 24)
        
        // Save the raw record
        stepDao.insertStepRecord(
            StepRecordEntity(timestamp = timestamp, stepsDelta = steps)
        )
        
        // Update daily aggregated stats
        val currentStats = stepDao.getStatsForDay(epochDay)
        if (currentStats != null) {
            stepDao.insertOrUpdateDailyStats(
                currentStats.copy(steps = currentStats.steps + steps)
            )
        } else {
            stepDao.insertOrUpdateDailyStats(
                DailyStatsEntity(dateEpochDays = epochDay, steps = steps)
            )
        }
    }
}
