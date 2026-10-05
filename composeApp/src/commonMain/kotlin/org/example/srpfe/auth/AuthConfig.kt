package org.example.srpfe.auth

import org.example.srpfe.utils.RuntimeConfig
import org.example.srpfe.utils.configuredPort

object AuthConfig {
    const val GOOGLE_SERVER_CLIENT_ID = "1003232389500-j79nn9nbpeuieenc2um6iq32h3hgu81v.apps.googleusercontent.com"
    val GOOGLE_DESKTOP_CALLBACK_PORT: Int
        get() = configuredPort(RuntimeConfig.OAUTH_CALLBACK_PORT_ENV, RuntimeConfig.DEFAULT_OAUTH_CALLBACK_PORT)

    val GOOGLE_DESKTOP_REDIRECT_URI: String
        get() = "http://localhost:$GOOGLE_DESKTOP_CALLBACK_PORT/callback"

    const val FIREBASE_API_KEY = "AIzaSyA6CQ3i7tWFtODWC1txNzTTCDeE38ajQ_o"
    const val FIREBASE_APPLICATION_ID = "1:1003232389500:android:9104a1ccfdd21ae9635cf9"
    const val FIREBASE_PROJECT_ID = "shopmap-1afca"
    const val FIREBASE_STORAGE_BUCKET = "shopmap-1afca.firebasestorage.app"
    const val FIREBASE_GCM_SENDER_ID = "1003232389500"
}
