package com.marcinmoskala.albert.data

import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.client.AuthClient
import com.marcinmoskala.model.LoginRequest
import com.marcinmoskala.model.UserApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepositoryImpl(
    private val authClient: AuthClient,
    private val settings: Settings
) : UserRepository {

    private val _currentUser = MutableStateFlow<UserApi?>(null)
    override val currentUser: StateFlow<UserApi?> = _currentUser.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    override val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    init {
        restoreUserFromStorage()
    }

    override suspend fun login(idToken: String): Result<UserApi> {
        return try {
            val loginRequest = LoginRequest(idToken = idToken)
            val response = authClient.login(loginRequest)
            val user = UserApi(
                userId = response.userId,
                email = response.email,
                displayName = response.displayName
            )
            persistUser(user, response.token)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        settings.remove(KEY_USER_ID)
        settings.remove(KEY_USER_EMAIL)
        settings.remove(KEY_USER_DISPLAY_NAME)
        settings.remove(KEY_AUTH_TOKEN)
        _currentUser.value = null
        _isLoggedIn.value = false
    }

    private fun persistUser(user: UserApi, token: String) {
        settings[KEY_USER_ID] = user.userId
        settings[KEY_USER_EMAIL] = user.email
        settings[KEY_USER_DISPLAY_NAME] = user.displayName
        settings[KEY_AUTH_TOKEN] = token
        _currentUser.value = user
        _isLoggedIn.value = true
    }

    private fun restoreUserFromStorage() {
        if (!settings.contains(KEY_USER_ID) || !settings.contains(KEY_USER_EMAIL) ||
            !settings.contains(KEY_USER_DISPLAY_NAME) || !settings.contains(KEY_AUTH_TOKEN)
        ) return

        val userId: String = settings.get(KEY_USER_ID, "")
        val email: String = settings.get(KEY_USER_EMAIL, "")
        val displayName: String = settings.get(KEY_USER_DISPLAY_NAME, "")
        val token: String = settings.get(KEY_AUTH_TOKEN, "")

        if (userId.isNotBlank() && email.isNotBlank() && displayName.isNotBlank() && token.isNotBlank()) {
            _currentUser.value = UserApi(
                userId = userId,
                email = email,
                displayName = displayName
            )
            _isLoggedIn.value = true
        }
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_DISPLAY_NAME = "user_display_name"
        const val KEY_AUTH_TOKEN = "auth_token"
    }
}
