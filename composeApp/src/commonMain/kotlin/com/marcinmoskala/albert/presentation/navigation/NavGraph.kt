package com.marcinmoskala.albert.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.marcinmoskala.albert.presentation.ui.app.MainScreen
import com.marcinmoskala.albert.presentation.ui.learning.LearningScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = AppDestination.Main
    ) {
        composable<AppDestination.Main> {
            MainScreen(
                onCourseClick = { courseId ->
                    navController.navigate(
                        AppDestination.Learning(
                            courseId = courseId,
                            lessonId = null
                        )
                    )
                },
                onLessonClick = { courseId, lessonId ->
                    navController.navigate(
                        AppDestination.Learning(
                            courseId = courseId,
                            lessonId = lessonId
                        )
                    )
                },
                onReviewAllClick = {
                    navController.navigate(
                        AppDestination.Learning(
                            courseId = null,
                            lessonId = null
                        )
                    )
                }
            )
        }

        composable<AppDestination.Learning> { backStackEntry ->
            val route = backStackEntry.toRoute<AppDestination.Learning>()
            LearningScreen(
                courseId = route.courseId,
                lessonId = route.lessonId,
                onBack = { navController.navigateUp() }
            )
        }
    }
}
