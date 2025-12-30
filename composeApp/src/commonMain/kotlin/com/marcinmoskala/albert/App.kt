package com.marcinmoskala.albert

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.marcinmoskala.albert.presentation.navigation.AppDestination
import com.marcinmoskala.albert.presentation.common.SnackbarController
import com.marcinmoskala.albert.presentation.navigation.AppNavHost
import com.marcinmoskala.albert.presentation.navigation.BrowserNavigator
import com.marcinmoskala.albert.presentation.navigation.Navigator
import com.marcinmoskala.albert.presentation.navigation.rememberBrowserNavigator
import com.marcinmoskala.albert.presentation.navigation.handleNavigationCommands
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    AppTheme {
        val navController = rememberNavController()
        val browserNavigator: BrowserNavigator? = rememberBrowserNavigator(navController)
        val startDestination = browserNavigator?.initialDestination ?: AppDestination.Main
        val snackbarHostState = remember { SnackbarHostState() }
        val snackbarController: SnackbarController = koinInject()
        val navigator: Navigator = koinInject()

        LaunchedEffect(Unit) {
            snackbarController.messages.collect { message ->
                snackbarHostState.showSnackbar(message)
            }
        }

        LaunchedEffect(browserNavigator) {
            navigator.handleNavigationCommands(navController, browserNavigator)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { paddingValues ->
            AppNavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}