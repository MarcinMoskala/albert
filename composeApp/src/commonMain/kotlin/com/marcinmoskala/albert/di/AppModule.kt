package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.CourseRepositoryImpl
import com.marcinmoskala.albert.data.UserProgressRepositoryImpl
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.presentation.ui.app.MainViewModel
import com.marcinmoskala.albert.presentation.ui.learning.LearningViewModel
import com.marcinmoskala.albert.domain.model.SingleAnswerStep
import com.marcinmoskala.albert.domain.model.MultipleAnswerStep
import com.marcinmoskala.albert.domain.model.ExactTextStep
import com.marcinmoskala.albert.domain.model.TextStep
import com.marcinmoskala.albert.presentation.ui.learning.components.SingleAnswerStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.MultipleAnswerStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.ExactTextStepViewModel
import com.marcinmoskala.albert.presentation.ui.learning.components.TextStepViewModel
import com.marcinmoskala.client.CourseClient
import com.marcinmoskala.client.buildDefaultHttpClient
import com.marcinmoskala.database.UserProgressLocalClient
import com.marcinmoskala.model.course.ExactTextStepApi
import com.marcinmoskala.model.course.MultipleAnswerStepApi
import com.marcinmoskala.model.course.SingleAnswerStepApi
import com.marcinmoskala.model.course.TextStepApi
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
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    // User Progress Repository - requires UserProgressLocalClient to be provided by platform
    single<UserProgressRepository> { UserProgressRepositoryImpl(get(), get()) }

    viewModel { MainViewModel(get()) }
    viewModel { (courseId: String?, lessonId: String?) ->
        LearningViewModel(get(), courseId, lessonId)
    }

    // Step view models
    viewModel { (step: SingleAnswerStep, courseId: String, lessonId: String, onStepCompleted: () -> Unit) ->
        SingleAnswerStepViewModel(step, courseId, lessonId, onStepCompleted, get())
    }
    viewModel { (step: MultipleAnswerStep, courseId: String, lessonId: String, onStepCompleted: () -> Unit) ->
        MultipleAnswerStepViewModel(step, courseId, lessonId, onStepCompleted, get())
    }
    viewModel { (step: ExactTextStep, courseId: String, lessonId: String, onStepCompleted: () -> Unit) ->
        ExactTextStepViewModel(step, courseId, lessonId, onStepCompleted, get())
    }
    viewModel { (step: TextStep, courseId: String, lessonId: String, onStepCompleted: () -> Unit) ->
        TextStepViewModel(step, courseId, lessonId, onStepCompleted, get())
    }
}