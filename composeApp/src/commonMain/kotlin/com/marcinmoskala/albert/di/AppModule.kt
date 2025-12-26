package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.CourseRepositoryImpl
import com.marcinmoskala.albert.data.UserProgressRepositoryImpl
import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.domain.model.MultipleAnswerStep
import com.marcinmoskala.albert.domain.model.ExactTextStep
import com.marcinmoskala.albert.domain.model.TextStep
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.usecase.SubmitStepAnswerUseCase
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.ErrorHandlerImpl
import com.marcinmoskala.albert.presentation.common.SnackbarController
import com.marcinmoskala.albert.presentation.navigation.Navigator
import com.marcinmoskala.albert.presentation.navigation.NavigatorImpl
import com.marcinmoskala.albert.presentation.ui.app.MainViewModel
import com.marcinmoskala.albert.presentation.ui.learning.LearningViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.SingleAnswerStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.MultipleAnswerStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.ExactTextStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.TextStepViewModel
import com.marcinmoskala.client.CourseClient
import com.marcinmoskala.client.buildDefaultHttpClient
import com.marcinmoskala.database.UserProgressLocalClient
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { buildDefaultHttpClient() }
    single { CourseClient(get()) }
    single<CourseRepository> { CourseRepositoryImpl(get()) }

    // Background scope for repository
    single<CoroutineScope>(createdAtStart = true) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, e -> get<ErrorHandler>().handleError(e) })
    }

    // User Progress Repository - requires UserProgressLocalClient to be provided by platform
    single<UserProgressRepository> { UserProgressRepositoryImpl(get(), get()) }

    // Use cases
    single { SubmitStepAnswerUseCase(get()) }

    // Navigation and UI controllers
    single<Navigator> { NavigatorImpl() }
    single { SnackbarController() }
    single<ErrorHandler> { ErrorHandlerImpl(get()) }

    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { (courseId: String?, lessonId: String?) ->
        LearningViewModel(get(), get(), get(), get(), courseId, lessonId, get())
    }

    // Step view models
    viewModel { (step: SingleAnswerStep, onAnswerSubmitted: (Boolean) -> Unit) ->
        SingleAnswerStepViewModel(step, onAnswerSubmitted, get())
    }
    viewModel { (step: MultipleAnswerStep, onAnswerSubmitted: (Boolean) -> Unit) ->
        MultipleAnswerStepViewModel(step, onAnswerSubmitted, get())
    }
    viewModel { (step: ExactTextStep, onAnswerSubmitted: (Boolean) -> Unit) ->
        ExactTextStepViewModel(step, onAnswerSubmitted, get())
    }
    viewModel { (step: TextStep, onAnswerSubmitted: (Boolean) -> Unit) ->
        TextStepViewModel(step, onAnswerSubmitted, get())
    }
}