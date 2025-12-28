package com.marcinmoskala.albert

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.marcinmoskala.albert.initialize.initializeApp
import com.marcinmoskala.albert.di.platformModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeApp { platformModule }
    ComposeViewport {
        App()
    }
}