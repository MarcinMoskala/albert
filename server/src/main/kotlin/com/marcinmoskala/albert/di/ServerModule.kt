package com.marcinmoskala.albert.di

import com.marcinmoskala.albert.data.auth.InMemoryUserRepository
import com.marcinmoskala.albert.data.auth.SimpleFirebaseAuthVerifier
import com.marcinmoskala.albert.data.yaml.CourseYamlRepository
import com.marcinmoskala.albert.domain.auth.AuthService
import com.marcinmoskala.albert.domain.auth.FirebaseAuthVerifier
import com.marcinmoskala.albert.domain.auth.JwtService
import com.marcinmoskala.albert.domain.auth.UserRepository
import com.marcinmoskala.albert.domain.course.CourseRepository
import com.marcinmoskala.albert.domain.course.CourseService
import org.koin.dsl.module

val serverModule = module {
    single<CourseRepository> { CourseYamlRepository() }
    single { CourseService(get()) }

    // Auth
    single<UserRepository> { InMemoryUserRepository() }
    single<FirebaseAuthVerifier> { SimpleFirebaseAuthVerifier() }
    single { JwtService() }
    single { AuthService(get(), get(), get()) }
}