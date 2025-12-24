package com.marcinmoskala.albert

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.marcinmoskala.albert.presentation.ui.app.MainScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        MainScreen()
    }
}