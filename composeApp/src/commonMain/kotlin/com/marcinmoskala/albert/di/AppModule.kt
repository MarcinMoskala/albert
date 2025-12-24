package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.network.CourseRepositoryImpl
import com.marcinmoskala.albert.domain.repository.CourseRepository
import com.marcinmoskala.albert.presentation.ui.app.MainViewModel
import com.marcinmoskala.client.CourseClient
import com.marcinmoskala.client.buildDefaultHttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { buildDefaultHttpClient() }
    single { CourseClient(get()) }
    single<CourseRepository> { CourseRepositoryImpl(get()) }
    viewModel { MainViewModel(get()) }
}