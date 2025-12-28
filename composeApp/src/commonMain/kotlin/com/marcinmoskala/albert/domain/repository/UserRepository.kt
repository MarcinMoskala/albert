package com.marcinmoskala.albert.domain.repository

import com.marcinmoskala.model.UserApi
import kotlinx.coroutines.flow.StateFlow

interface UserRepository {
    val currentUser: StateFlow<UserApi?>
    val isLoggedIn: StateFlow<Boolean>

    suspend fun login(idToken: String): Result<UserApi>
    suspend fun logout()
}
