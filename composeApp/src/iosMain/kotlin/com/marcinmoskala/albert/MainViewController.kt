package com.marcinmoskala.albert

import androidx.compose.ui.window.ComposeUIViewController
import com.marcinmoskala.albert.di.appModule
import com.marcinmoskala.albert.di.platformModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController {
    startKoin {
        modules(appModule, platformModule)
    }
    App()
}