package com.marcinmoskala.albert.presentation.ui.app

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import com.marcinmoskala.albert.domain.repository.UserProgressRepository
import com.marcinmoskala.albert.domain.repository.UserRepository
import com.marcinmoskala.albert.presentation.common.ErrorHandler
import com.marcinmoskala.albert.presentation.common.SnackbarController
import com.marcinmoskala.albert.presentation.navigation.Navigator
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ResetProgressDialog() {
    val userRepository: UserRepository = koinInject()
    val userProgressRepository: UserProgressRepository = koinInject()
    val navigator: Navigator = koinInject()
    val snackbarController: SnackbarController = koinInject()
    val errorHandler: ErrorHandler = koinInject()
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = navigator::navigateBack,
        title = { Text("Reset progress?") },
        text = {
            Text("This will remove current user progress from this device. This action cannot be undone.")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        runCatching {
                            val activeUserId = userRepository.currentUser.value?.userId
                                ?: UserProgressRepository.ANONYMOUS_USER_ID
                            userProgressRepository.deleteAllForUser(activeUserId)
                            snackbarController.showMessage("Progress reset")
                            navigator.navigateBack()
                        }.onFailure { errorHandler.handleError(it) }
                    }
                }
            ) {
                Text("Reset")
            }
        },
        dismissButton = {
            TextButton(onClick = navigator::navigateBack) {
                Text("Cancel")
            }
        }
    )
}
