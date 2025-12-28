package com.marcinmoskala.albert.data.auth

import com.marcinmoskala.albert.domain.auth.UserRepository
import com.marcinmoskala.model.UserApi

class InMemoryUserRepository : UserRepository {
    private val users = mutableMapOf<String, UserApi>()

    override suspend fun findOrCreateUser(
        userId: String,
        email: String,
        displayName: String
    ): UserApi {
        return users.getOrPut(userId) {
            UserApi(
                userId = userId,
                email = email,
                displayName = displayName
            )
        }
    }

    override suspend fun findUserById(userId: String): UserApi? {
        return users[userId]
    }
}
