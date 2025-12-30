package com.marcinmoskala.albert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

interface BrowserNavigator {
    val initialDestination: AppDestination?

    /**
     * Called whenever app-driven navigation happens to push the new URL into the browser history.
     */
    fun handleNavigate(destination: AppDestination)

    /**
     * Called when the app requests a back navigation.
     * @return true if browser navigation was handled, false to allow default navController back stack.
     */
    fun handleNavigateBack(): Boolean
}

@Composable
expect fun rememberBrowserNavigator(navController: NavHostController): BrowserNavigator?