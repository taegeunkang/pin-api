package com.pin.pinapi.core.notification.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream


@Configuration
class FirebaseConfig {
    @Value("\${firebase.key.path}")
    private lateinit var FIREBASE_KEY_PATH: String

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val serviceAccount = FileInputStream(FIREBASE_KEY_PATH)

        val options = FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
            .build()

        val firebaseApp = FirebaseApp.initializeApp(options)

        return FirebaseMessaging.getInstance(firebaseApp)
    }
}