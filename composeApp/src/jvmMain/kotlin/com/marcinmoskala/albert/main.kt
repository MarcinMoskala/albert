package com.marcinmoskala.albert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.marcinmoskala.albert.di.appModule
import com.marcinmoskala.albert.di.platformModule
import com.marcinmoskala.albert.initialize.initializeApp
import org.koin.core.context.startKoin

fun main() {
    initializeApp { platformModule }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Albert",
        ) {
            App()
        }
    }
}