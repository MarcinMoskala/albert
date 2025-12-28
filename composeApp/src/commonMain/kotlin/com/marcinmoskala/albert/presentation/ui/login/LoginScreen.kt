package com.marcinmoskala.albert.presentation.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mmk.kmpauth.firebase.github.GithubButtonUiContainer
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import dev.gitlive.firebase.auth.FirebaseUser
import org.koin.compose.viewmodel.koinViewModel

// Platform-specific login buttons implemented per target
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                navigationIcon = {
                    IconButton(onClick = viewModel::onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (val state = uiState) {
                is LoginUiState.ReadyToLogin -> {
                    LoginContent(
                        onFirebaseAuthResult = viewModel::onFirebaseResult,
                    )
                }

                is LoginUiState.Loading -> {
                    CircularProgressIndicator()
                }

                is LoginUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = viewModel::reset
                    )
                }

                is LoginUiState.Success -> {
                    // Navigation should be handled by ViewModel
                }
            }
        }
    }
}

@Composable
private fun LoginContent(
    onFirebaseAuthResult: (Result<FirebaseUser?>) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Welcome to Albert",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Please sign in to sync your progress",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LoginButtons(
            onFirebaseAuthResult = onFirebaseAuthResult,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
    }
}

@Composable
fun LoginButtons(
    onFirebaseAuthResult: (Result<FirebaseUser?>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        GoogleButtonUiContainerFirebase(
            onResult = onFirebaseAuthResult,
            linkAccount = false,
            filterByAuthorizedAccounts = false,
        ) {
            GoogleSignInButton(modifier = Modifier.fillMaxWidth()) { this.onClick() }
        }
        GithubButtonUiContainer(
            onResult = onFirebaseAuthResult,
            linkAccount = false,
        ) {
            Button(onClick = { this.onClick() }) { Text("Github Sign-In") }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Login Failed",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
