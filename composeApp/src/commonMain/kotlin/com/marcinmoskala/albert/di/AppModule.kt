package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.CourseRepositoryImpl
import com.marcinmoskala.albert.data.UserProgressRepositoryImpl
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.presentation.ui.app.MainViewModel
import com.marcinmoskala.albert.presentation.ui.learning.LearningViewModel
import com.marcinmoskala.client.CourseClient
import com.marcinmoskala.client.buildDefaultHttpClient
import com.marcinmoskala.database.UserProgressLocalClient
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
}