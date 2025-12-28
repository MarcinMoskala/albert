package com.marcinmoskala.albert.initialize

import com.marcinmoskala.albert.di.appModule
import com.mmk.kmpauth.core.KMPAuth
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.apps
import dev.gitlive.firebase.initialize
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initializeApp(platformModuleProvider: () -> Module) {
    initializeAuth()
    startKoin {
        modules(appModule, platformModuleProvider())
    }
}

private fun initializeAuth() {
    KMPAuth.setLogger {
        println("KMPAuthLog: $it")
    }
    if (Firebase.apps().isEmpty()) {
        Firebase.initialize(
            options = FirebaseOptions(
                applicationId = "1:215669576873:web:f0ecd6d22356ffde6145b8",
                apiKey = "AIzaSyBkphXUhpVoQe3DZX96ZwsYs04zgdq-T1k",
                projectId = "albert-8091e",
                storageBucket = "albert-8091e.firebasestorage.app",
                gcmSenderId = "215669576873",
                authDomain = "albert-8091e.firebaseapp.com",
            )
        )
    }
    GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "215669576873-1h0aoa2dub3h9tjol66p44tlven697as.apps.googleusercontent.com"))
}