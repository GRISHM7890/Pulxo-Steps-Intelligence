package com.pulxo.steps.data.repository

import com.google.firebase.database.FirebaseDatabase
import com.pulxo.steps.domain.model.DailyStats
import com.pulxo.steps.domain.repository.SyncRepository
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseSyncRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
) : SyncRepository {

    override suspend fun syncDailyStats(userId: String, stats: DailyStats): Result<Unit> = suspendCancellableCoroutine { continuation ->
        val userRef = database.getReference("users").child(userId).child("daily_stats")
            .child(stats.dateEpochDays.toString())
        
        val statsMap = mapOf(
            "steps" to stats.steps,
            "distanceMeters" to stats.distanceMeters,
            "caloriesBurned" to stats.caloriesBurned,
            "activeTimeMinutes" to stats.activeTimeMinutes
        )

        userRef.setValue(statsMap)
            .addOnSuccessListener {
                continuation.resume(Result.success(Unit))
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
            }
    }

    override suspend fun fetchRemoteStats(userId: String): Result<List<DailyStats>> = suspendCancellableCoroutine { continuation ->
        database.getReference("users").child(userId).child("daily_stats")
            .get()
            .addOnSuccessListener { snapshot ->
                val statsList = mutableListOf<DailyStats>()
                snapshot.children.forEach { child ->
                    val dateEpochDays = child.key?.toLongOrNull() ?: return@forEach
                    val steps = (child.child("steps").value as? Long)?.toInt() ?: 0
                    val distanceMeters = (child.child("distanceMeters").value as? Double)?.toFloat() ?: 0f
                    val caloriesBurned = (child.child("caloriesBurned").value as? Double)?.toFloat() ?: 0f
                    val activeTimeMinutes = (child.child("activeTimeMinutes").value as? Long)?.toInt() ?: 0
                    
                    statsList.add(
                        DailyStats(
                            dateEpochDays = dateEpochDays,
                            steps = steps,
                            distanceMeters = distanceMeters,
                            caloriesBurned = caloriesBurned,
                            activeTimeMinutes = activeTimeMinutes
                        )
                    )
                }
                continuation.resume(Result.success(statsList))
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
            }
    }
}
