package com.pulxo.steps.domain.repository

import com.pulxo.steps.domain.model.PulxoUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<PulxoUser?>
    suspend fun signInWithGoogle(idToken: String): Result<PulxoUser>
    suspend fun signInWithEmail(email: String, password: String): Result<PulxoUser>
    suspend fun signUpWithEmail(email: String, password: String): Result<PulxoUser>
    suspend fun signOut()
}
