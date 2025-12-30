package com.marcinmoskala.albert.presentation.navigation

import androidx.navigation.NavHostController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface NavigationCommand {
    data class NavigateTo(val destination: AppDestination) : NavigationCommand
    data object NavigateBack : NavigationCommand
}

interface Navigator {
    val navigationCommands: Flow<NavigationCommand>
    fun navigateTo(destination: AppDestination)
    fun navigateBack()
}

class NavigatorImpl : Navigator {
    private val _navigationCommands = Channel<NavigationCommand>(Channel.UNLIMITED)
    override val navigationCommands = _navigationCommands.receiveAsFlow()

    override fun navigateTo(destination: AppDestination) {
        _navigationCommands.trySend(NavigationCommand.NavigateTo(destination))
    }

    override fun navigateBack() {
        _navigationCommands.trySend(NavigationCommand.NavigateBack)
    }
}

suspend fun Navigator.handleNavigationCommands(
    navController: NavHostController,
    browserNavigator: BrowserNavigator? = null
) {
    navigationCommands.collect { command ->
        when (command) {
            is NavigationCommand.NavigateTo -> {
                navController.navigate(command.destination)
                browserNavigator?.handleNavigate(command.destination)
            }

            is NavigationCommand.NavigateBack -> {
                val handledByBrowser = browserNavigator?.handleNavigateBack() == true
                if (!handledByBrowser) {
                    navController.navigateUp()
                }
            }
        }
    }
}
