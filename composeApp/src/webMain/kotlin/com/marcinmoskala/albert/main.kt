package com.marcinmoskala.albert

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.marcinmoskala.albert.di.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin()
    ComposeViewport {
        App()
    }
}