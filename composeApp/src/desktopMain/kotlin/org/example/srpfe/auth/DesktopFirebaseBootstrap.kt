package org.example.srpfe.auth

import android.app.Application
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.app
import dev.gitlive.firebase.initialize
import java.util.prefs.Preferences

object DesktopFirebaseBootstrap {
    private val appContext = Application()
    private val preferenceStore = Preferences.userRoot().node("/org/example/srpfe/firebase")
    private var initialized = false

    fun initialize() {
        synchronized(this) {
            if (initialized) {
                return
            }

            FirebasePlatform.initializeFirebasePlatform(
                object : FirebasePlatform() {
                    override fun store(key: String, value: String) {
                        preferenceStore.put(key, value)
                    }

                    override fun retrieve(key: String): String? = preferenceStore.get(key, null)

                    override fun clear(key: String) {
                        preferenceStore.remove(key)
                    }

                    override fun log(msg: String) {
                        println("Firebase: $msg")
                    }
                },
            )

            runCatching { Firebase.app }
                .getOrElse {
                    Firebase.initialize(
                        context = appContext,
                        options =
                            FirebaseOptions(
                                applicationId = AuthConfig.FIREBASE_APPLICATION_ID,
                                apiKey = AuthConfig.FIREBASE_API_KEY,
                                projectId = AuthConfig.FIREBASE_PROJECT_ID,
                                storageBucket = AuthConfig.FIREBASE_STORAGE_BUCKET,
                                gcmSenderId = AuthConfig.FIREBASE_GCM_SENDER_ID,
                            ),
                    )
                }

            initialized = true
        }
    }
}
