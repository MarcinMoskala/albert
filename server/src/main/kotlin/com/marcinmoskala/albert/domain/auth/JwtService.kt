package com.marcinmoskala.albert.domain.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class JwtService(
    secret: String = System.getenv("JWT_SECRET")
        ?: "default-secret-change-in-production",
    private val issuer: String = "albert-server",
    private val audience: String = "albert-app",
    private val validityInMs: Long = 3_600_000 * 24 * 7 // 7 days
) {
    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(userId: String, email: String): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(userId)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + validityInMs))
            .withIssuedAt(Date())
            .sign(algorithm)
    }

    fun verifyToken(token: String): JwtPayload? {
        return try {
            val decodedJWT = verifier.verify(token)
            JwtPayload(
                userId = decodedJWT.getClaim("userId").asString(),
                email = decodedJWT.getClaim("email").asString(),
                expiresAt = decodedJWT.expiresAt
            )
        } catch (e: Exception) {
            null
        }
    }
}

data class JwtPayload(
    val userId: String,
    val email: String,
    val expiresAt: Date
)
