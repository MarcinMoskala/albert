package com.marcinmoskala.albert.presentation.common

class FakeErrorHandler : ErrorHandler {
    val errors = mutableListOf<Throwable>()

    override fun handleError(error: Throwable) {
        errors.add(error)
    }

    fun clear() {
        errors.clear()
    }
}
