package com.example.data

import android.util.Log
import com.example.model.TeacherEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService(
    private val firestoreService: FirestoreService = FirestoreService()
) {
    private val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthService"
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInTeacher(email: String, pass: String): Result<TeacherEntity> {
        return try {
            val cleanEmail = email.trim()
            val cleanPass = pass.trim()
            
            val authResult = try {
                auth.signInWithEmailAndPassword(cleanEmail, cleanPass).await()
            } catch (e: Exception) {
                // If user doesn't exist yet, automatically create teacher account for seamless testing
                auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            }

            val user = authResult.user ?: throw IllegalStateException("Firebase User tapılmadı")
            
            // Check if teacher profile exists in Firestore
            var teacher = firestoreService.getTeacher(user.uid)
            if (teacher == null) {
                val displayName = user.displayName?.ifBlank { null } 
                    ?: cleanEmail.substringBefore("@").replace(".", " ").capitalizeWords()
                teacher = TeacherEntity(
                    id = user.uid,
                    email = cleanEmail,
                    name = displayName,
                    createdAt = System.currentTimeMillis()
                )
                firestoreService.saveTeacher(teacher)
            }
            
            Result.success(teacher)
        } catch (e: Exception) {
            Log.e(TAG, "signInTeacher failed", e)
            Result.failure(e)
        }
    }

    suspend fun registerTeacher(email: String, pass: String, name: String): Result<TeacherEntity> {
        return try {
            val cleanEmail = email.trim()
            val cleanPass = pass.trim()
            val authResult = auth.createUserWithEmailAndPassword(cleanEmail, cleanPass).await()
            val user = authResult.user ?: throw IllegalStateException("User creation failed")

            val teacher = TeacherEntity(
                id = user.uid,
                email = cleanEmail,
                name = name.ifBlank { cleanEmail.substringBefore("@") },
                createdAt = System.currentTimeMillis()
            )
            firestoreService.saveTeacher(teacher)
            Result.success(teacher)
        } catch (e: Exception) {
            Log.e(TAG, "registerTeacher failed", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
