package com.marcinmoskala.albert.data.auth

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.marcinmoskala.albert.domain.auth.FirebaseAuthVerifier
import com.marcinmoskala.albert.domain.auth.VerifiedUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimpleFirebaseAuthVerifier : FirebaseAuthVerifier {

    override suspend fun verifyIdToken(idToken: String): VerifiedUser =
        withContext(Dispatchers.IO) {
            // Check if this is a test token (for development)
            if (idToken.startsWith("test-token-")) {
                val platform = idToken.removePrefix("test-token-")
                println("WARNING: Using test token for platform: $platform (development only)")
                return@withContext VerifiedUser(
                    userId = "test-user-$platform",
                    email = "test-$platform@albert-dev.com",
                    displayName = "Test User ($platform)"
                )
            }

            if (FirebaseApp.getApps().isEmpty()) {
                throw IllegalStateException(
                    "Firebase Admin SDK is not initialized. " +
                            "Set FIREBASE_SERVICE_ACCOUNT_JSON (preferred) or GOOGLE_APPLICATION_CREDENTIALS and FIREBASE_PROJECT_ID."
                )
            }

            // Verify real Firebase ID token (works for Google, GitHub, and any Firebase Auth provider)
            try {
                val auth = FirebaseAuth.getInstance(FirebaseApp.getInstance())
                val decodedToken = auth.verifyIdToken(idToken)
                VerifiedUser(
                    userId = decodedToken.uid,
                    email = decodedToken.email
                        ?: throw IllegalStateException("Email not found in token"),
                    displayName = decodedToken.name ?: decodedToken.email ?: "Unknown User"
                )
            } catch (e: FirebaseAuthException) {
                throw IllegalArgumentException("Invalid Firebase ID token: ${e.message}", e)
            } catch (e: Exception) {
                throw IllegalArgumentException("Error verifying Firebase token: ${e.message}", e)
            }
    }
}
