package com.marcinmoskala.albert.domain.auth

import com.marcinmoskala.model.AuthProvider
import com.marcinmoskala.model.LoginRequest
import com.marcinmoskala.model.LoginResponse
import com.marcinmoskala.model.UserApi

class AuthService(
    private val userRepository: UserRepository,
    private val firebaseAuthVerifier: FirebaseAuthVerifier,
    private val jwtService: JwtService
) {
    suspend fun login(request: LoginRequest): LoginResponse {
        val verifiedUser = firebaseAuthVerifier.verifyIdToken(request.idToken)

        val user = userRepository.findOrCreateUser(
            userId = verifiedUser.userId,
            email = verifiedUser.email,
            displayName = verifiedUser.displayName
        )

        val token = jwtService.generateToken(user.userId, user.email)

        return LoginResponse(
            userId = user.userId,
            email = user.email,
            displayName = user.displayName,
            token = token
        )
    }
}

data class VerifiedUser(
    val userId: String,
    val email: String,
    val displayName: String
)
