package com.pulxo.steps.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pulxo.steps.domain.model.PulxoUser
import com.pulxo.steps.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : AuthRepository {

    override val currentUser: Flow<PulxoUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser?.let {
                PulxoUser(
                    uid = it.uid,
                    email = it.email,
                    displayName = it.displayName,
                    photoUrl = it.photoUrl?.toString()
                )
            }
            trySend(user)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<PulxoUser> = suspendCancellableCoroutine { continuation ->
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user?.let {
                    PulxoUser(
                        uid = it.uid,
                        email = it.email,
                        displayName = it.displayName,
                        photoUrl = it.photoUrl?.toString()
                    )
                }
                if (user != null) {
                    continuation.resume(Result.success(user))
                } else {
                    continuation.resume(Result.failure(Exception("User is null")))
                }
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
            }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<PulxoUser> = suspendCancellableCoroutine { continuation ->
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user?.let {
                    PulxoUser(
                        uid = it.uid,
                        email = it.email,
                        displayName = it.displayName,
                        photoUrl = it.photoUrl?.toString()
                    )
                }
                if (user != null) {
                    continuation.resume(Result.success(user))
                } else {
                    continuation.resume(Result.failure(Exception("User is null")))
                }
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
            }
    }

    override suspend fun signUpWithEmail(email: String, password: String): Result<PulxoUser> = suspendCancellableCoroutine { continuation ->
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user?.let {
                    PulxoUser(
                        uid = it.uid,
                        email = it.email,
                        displayName = it.displayName,
                        photoUrl = it.photoUrl?.toString()
                    )
                }
                if (user != null) {
                    continuation.resume(Result.success(user))
                } else {
                    continuation.resume(Result.failure(Exception("User is null")))
                }
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
            }
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
