package com.marcinmoskala.albert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.marcinmoskala.albert.presentation.ui.app.MainScreen
import com.marcinmoskala.albert.presentation.ui.learning.LearningScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Main,
        modifier = modifier
    ) {
        composable<AppDestination.Main> {
            MainScreen()
        }

        composable<AppDestination.Learning> { backStackEntry ->
            val route = backStackEntry.toRoute<AppDestination.Learning>()
            LearningScreen(
                courseId = route.courseId,
                lessonId = route.lessonId
            )
        }
    }
}
