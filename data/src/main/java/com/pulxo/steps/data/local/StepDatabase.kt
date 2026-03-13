package com.pulxo.steps.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DailyStatsEntity::class, StepRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class StepDatabase : RoomDatabase() {
    abstract val stepDao: StepDao
}
