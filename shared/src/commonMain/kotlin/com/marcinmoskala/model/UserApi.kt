package com.marcinmoskala.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val idToken: String,
)

@Serializable
enum class AuthProvider {
    GOOGLE,
    GITHUB
}

@Serializable
data class LoginResponse(
    val userId: String,
    val email: String,
    val displayName: String,
    val token: String
)

@Serializable
data class UserApi(
    val userId: String,
    val email: String,
    val displayName: String
)
