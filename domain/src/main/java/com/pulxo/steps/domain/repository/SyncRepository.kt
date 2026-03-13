package com.pulxo.steps.domain.repository

import com.pulxo.steps.domain.model.DailyStats

interface SyncRepository {
    suspend fun syncDailyStats(userId: String, stats: DailyStats): Result<Unit>
    suspend fun fetchRemoteStats(userId: String): Result<List<DailyStats>>
}
