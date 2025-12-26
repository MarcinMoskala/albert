package com.marcinmoskala.albert.presentation.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class SnackbarController {
    private val _messages = Channel<String>(Channel.UNLIMITED)
    val messages = _messages.receiveAsFlow()

    fun showMessage(message: String) {
        _messages.trySend(message)
    }

    fun showError(error: Throwable) {
        val message = error.message ?: "An unknown error occurred"
        showMessage(message)
    }
}
