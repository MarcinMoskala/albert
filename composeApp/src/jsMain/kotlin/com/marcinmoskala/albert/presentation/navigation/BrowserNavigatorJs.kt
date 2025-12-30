package com.marcinmoskala.albert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import kotlinx.browser.window
import org.w3c.dom.events.EventListener

private class BrowserNavigatorImpl(
    private val navController: NavHostController
) : BrowserNavigator {

    private var suppressNextPush = false
    private val popStateListener = EventListener { _ ->
        val destination = browserPathToDestination(window.location.pathname + window.location.search) ?: AppDestination.Main
        suppressNextPush = true
        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
        suppressNextPush = false
    }

    override val initialDestination: AppDestination? =
        browserPathToDestination(window.location.pathname + window.location.search)

    fun attach() {
        window.addEventListener("popstate", popStateListener)
    }

    fun detach() {
        window.removeEventListener("popstate", popStateListener)
    }

    override fun handleNavigate(destination: AppDestination) {
        if (suppressNextPush) {
            suppressNextPush = false
            return
        }
        val path = destination.toBrowserPath()
        val current = window.location.pathname + window.location.search
        if (current != path) {
            window.history.pushState(data = null, title = "", url = path)
        }
    }

    override fun handleNavigateBack(): Boolean {
        suppressNextPush = true
        window.history.back()
        return true
    }
}

@Composable
actual fun rememberBrowserNavigator(navController: NavHostController): BrowserNavigator? {
    val navigator = remember(navController) {
        BrowserNavigatorImpl(navController)
    }

    DisposableEffect(navController) {
        navigator.attach()
        onDispose { navigator.detach() }
    }

    return navigator
}