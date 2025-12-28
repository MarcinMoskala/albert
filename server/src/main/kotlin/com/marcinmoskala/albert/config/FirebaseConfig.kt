package com.marcinmoskala.albert.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import java.io.ByteArrayInputStream

object FirebaseConfig {

    fun initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            // Check both environment variables and system properties (for IntelliJ run configs)
            val projectId = System.getenv("FIREBASE_PROJECT_ID")
                ?: System.getProperty("FIREBASE_PROJECT_ID")
                ?: "albert-8091e"
            val serviceAccountJson = System.getenv("FIREBASE_SERVICE_ACCOUNT_JSON")
                ?: System.getProperty("FIREBASE_SERVICE_ACCOUNT_JSON")

            val options = buildFirebaseOptions(projectId, serviceAccountJson)
                ?: run {
                    println(
                        "WARNING: Firebase Admin SDK not initialized (no credentials available). " +
                                "Set FIREBASE_SERVICE_ACCOUNT_JSON or GOOGLE_APPLICATION_CREDENTIALS to enable Firebase features."
                    )
                    return
                }

            FirebaseApp.initializeApp(options)
            println("✅ Firebase Admin SDK initialized for project: $projectId")
        }
    }

    private fun buildFirebaseOptions(
        projectId: String,
        serviceAccountJson: String?
    ): FirebaseOptions? {
        if (serviceAccountJson != null) {
            return try {
                val serviceAccount = ByteArrayInputStream(serviceAccountJson.toByteArray())
                FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build()
            } catch (e: Exception) {
                println("ERROR: Failed to initialize Firebase with FIREBASE_SERVICE_ACCOUNT_JSON: ${e.message}")
                println("Falling back to Application Default Credentials...")
                buildOptionsWithApplicationDefaultCredentials(projectId)
            }
        }

        println("WARNING: FIREBASE_SERVICE_ACCOUNT_JSON not set. Trying Application Default Credentials...")
        return buildOptionsWithApplicationDefaultCredentials(projectId)
    }

    private fun buildOptionsWithApplicationDefaultCredentials(projectId: String): FirebaseOptions? {
        return try {
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId(projectId)
                .build()
        } catch (e: Exception) {
            println("WARNING: Application Default Credentials not available.")
            println("To enable Firebase features, set FIREBASE_SERVICE_ACCOUNT_JSON in local.properties")
            null
        }
    }
}
