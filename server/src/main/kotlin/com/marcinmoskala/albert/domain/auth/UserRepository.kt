package com.marcinmoskala.albert.domain.auth

import com.marcinmoskala.model.UserApi

interface UserRepository {
    suspend fun findOrCreateUser(userId: String, email: String, displayName: String): UserApi
    suspend fun findUserById(userId: String): UserApi?
}
