package com.marcinmoskala.albert.presentation.ui.login

import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.SnackbarController
import com.marcinmoskala.albert.presentation.common.viewmodels.BaseViewModel
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.navigation.Navigator
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val navigator: Navigator,
    private val snackbarController: SnackbarController,
    errorHandler: ErrorHandler
) : BaseViewModel(errorHandler) {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Loading)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "1:215669576873:web:f0ecd6d22356ffde6145b8"))
            _uiState.value = LoginUiState.ReadyToLogin
        }
    }

    fun onFirebaseResult(idToken: Result<FirebaseUser?>) {
        viewModelScope.launch {
            idToken.getOrNull()
                ?.getIdToken(false)
                ?.let { user -> login(user) }
                ?: snackbarController.showMessage("Failed to get token")
        }
    }

    private fun login(idToken: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            val result = userRepository.login(idToken)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = LoginUiState.Success(user.email)
                    snackbarController.showMessage("Logged in as ${user.email}")
                    navigator.navigateTo(AppDestination.Main)
                },
                onFailure = { error ->
                    error.printStackTrace()
                    _uiState.value = LoginUiState.Error(
                        error.message ?: "Failed to login"
                    )
                }
            )
        }
    }

    fun reset() {
        _uiState.value = LoginUiState.ReadyToLogin
    }

    fun onBackClick() {
        navigator.navigateBack()
    }
}

sealed class LoginUiState {
    data object ReadyToLogin : LoginUiState()
    data object Loading : LoginUiState()
    data class Error(val message: String) : LoginUiState()
    data class Success(val email: String) : LoginUiState()
}
