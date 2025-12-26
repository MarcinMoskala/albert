package com.marcinmoskala.albert

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.marcinmoskala.albert.di.appModule
import com.marcinmoskala.albert.di.platformModule
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(appModule, platformModule)
    }
    ComposeViewport {
        App()
    }
}