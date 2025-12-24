package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.yaml.CourseYamlRepository
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.albert.domain.course.CourseService
import org.koin.dsl.module

val serverModule = module {
    single<CourseRepository> { CourseYamlRepository() }
    single { CourseService(get()) }
}