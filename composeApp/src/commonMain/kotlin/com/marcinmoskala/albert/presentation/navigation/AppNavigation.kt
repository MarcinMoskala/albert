package com.marcinmoskala.albert.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination {
    @Serializable
    data object Main : AppDestination

    @Serializable
    data class Learning(
        val courseId: String? = null,
        val lessonId: String? = null
    ) : AppDestination

    @Serializable
    data object Login : AppDestination
}
