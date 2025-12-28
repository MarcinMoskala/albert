package com.marcinmoskala.albert

import android.app.Application
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider

class AlbertApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize GoogleAuthProvider with Web Client ID from google-services.json
        GoogleAuthProvider.create(
            credentials = GoogleAuthCredentials(
                serverId = "215669576873-1h0aoa2dub3h9tjol66p44tlven697as.apps.googleusercontent.com"
            )
        )
    }
}
