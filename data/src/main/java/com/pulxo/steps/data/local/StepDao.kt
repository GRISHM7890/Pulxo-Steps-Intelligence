package com.pulxo.steps.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM daily_stats WHERE dateEpochDays = :epochDay")
    fun getStatsForDayFlow(epochDay: Long): Flow<DailyStatsEntity?>

    @Query("SELECT * FROM daily_stats WHERE dateEpochDays = :epochDay")
    suspend fun getStatsForDay(epochDay: Long): DailyStatsEntity?

    @Query("SELECT * FROM daily_stats WHERE dateEpochDays BETWEEN :startEpoch AND :endEpoch")
    suspend fun getStatsForDateRange(startEpoch: Long, endEpoch: Long): List<DailyStatsEntity>

    @Query("SELECT * FROM daily_stats WHERE dateEpochDays BETWEEN :startEpoch AND :endEpoch ORDER BY dateEpochDays DESC")
    fun getStatsForDateRangeFlow(startEpoch: Long, endEpoch: Long): Flow<List<DailyStatsEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDailyStats(stats: DailyStatsEntity)

    @Insert
    suspend fun insertStepRecord(record: StepRecordEntity)
}
