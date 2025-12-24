package com.marcinmoskala.albert

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.marcinmoskala.albert.di.startKoin

fun main() {
    startKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Albert",
        ) {
            App()
        }
    }
}