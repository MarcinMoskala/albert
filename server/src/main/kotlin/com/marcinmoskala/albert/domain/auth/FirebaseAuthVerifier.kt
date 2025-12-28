package com.marcinmoskala.albert.domain.auth

interface FirebaseAuthVerifier {
    suspend fun verifyIdToken(idToken: String): VerifiedUser
}
