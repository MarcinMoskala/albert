package com.marcinmoskala.albert.presentation.navigation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeNavigator : Navigator {
    private val _navigationCommands = MutableSharedFlow<NavigationCommand>()
    override val navigationCommands: Flow<NavigationCommand> = _navigationCommands

    val destinations = mutableListOf<AppDestination>()
    var backPressedCount = 0
        private set

    override fun navigateTo(destination: AppDestination) {
        destinations.add(destination)
    }

    override fun navigateBack() {
        backPressedCount++
    }

    fun clear() {
        destinations.clear()
        backPressedCount = 0
    }
}
