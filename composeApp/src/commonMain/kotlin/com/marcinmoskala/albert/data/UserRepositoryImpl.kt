package com.marcinmoskala.albert.data

import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.client.AuthClient
import com.marcinmoskala.model.LoginRequest
import com.marcinmoskala.model.UserApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepositoryImpl(
    private val authClient: AuthClient
) : UserRepository {

    private val _currentUser = MutableStateFlow<UserApi?>(null)
    override val currentUser: StateFlow<UserApi?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    override suspend fun login(idToken: String): Result<UserApi> {
        return try {
            val loginRequest = LoginRequest(idToken = idToken)
            val response = authClient.login(loginRequest)
            val user = UserApi(
                userId = response.userId,
                email = response.email,
                displayName = response.displayName
            )
            _currentUser.value = user
            _isLoggedIn.value = true
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
    }
}
