package com.marcinmoskala.albert.presentation.common

interface ErrorHandler {
    fun handleError(error: Throwable)
}

class ErrorHandlerImpl(
    private val snackbarController: SnackbarController
) : ErrorHandler {
    override fun handleError(error: Throwable) {
        error.printStackTrace()
        snackbarController.showError(error)
    }
}
