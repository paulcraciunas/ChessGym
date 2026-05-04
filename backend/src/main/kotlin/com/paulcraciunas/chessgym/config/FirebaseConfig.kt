package com.paulcraciunas.chessgym.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.ktor.server.application.*

object FirebaseConfig {
    fun initialize(environment: ApplicationEnvironment): Firestore {
        if (FirebaseApp.getApps().isEmpty()) {
            val projectId = environment.config
                .property("chessgym.firebase.projectId")
                .getString()

            val isEmulator = System.getenv("FIRESTORE_EMULATOR_HOST") != null

            val optionsBuilder = FirebaseOptions.builder()
                .setProjectId(projectId)

            if (!isEmulator) {
                optionsBuilder.setCredentials(GoogleCredentials.getApplicationDefault())
            }

            FirebaseApp.initializeApp(optionsBuilder.build())
        }

        return FirestoreClient.getFirestore()
    }
}
