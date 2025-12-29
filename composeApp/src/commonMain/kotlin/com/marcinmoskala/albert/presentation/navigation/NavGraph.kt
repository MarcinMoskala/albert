package com.marcinmoskala.albert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.marcinmoskala.albert.presentation.ui.app.MainScreen
import com.marcinmoskala.albert.presentation.ui.app.ResetProgressDialog
import com.marcinmoskala.albert.presentation.ui.learning.LearningScreen
import com.marcinmoskala.albert.presentation.ui.login.LoginScreen

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

        composable<AppDestination.Login> {
            LoginScreen()
        }

        dialog<AppDestination.ResetProgressDialog> {
            ResetProgressDialog()
        }
    }
}
