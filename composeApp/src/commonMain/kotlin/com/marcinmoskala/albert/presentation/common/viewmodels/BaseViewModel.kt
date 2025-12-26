package com.marcinmoskala.albert.presentation.common.viewmodels

import androidx.lifecycle.ViewModel
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job

abstract class BaseViewModel(
    private val errorHandler: ErrorHandler
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        errorHandler.handleError(throwable)
    }

    protected val viewModelScope: CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate + coroutineExceptionHandler)

    override fun onCleared() {
        viewModelScope.coroutineContext.job.cancelChildren()
    }
}