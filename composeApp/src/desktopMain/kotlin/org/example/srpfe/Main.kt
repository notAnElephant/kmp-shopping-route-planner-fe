package org.example.srpfe

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.srpfe.auth.DesktopFirebaseBootstrap
import org.example.srpfe.di.initKoin

fun main() {
    DesktopFirebaseBootstrap.initialize()
    initKoin()

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ShoppingRoutePlanner-FE",
        ) {
            App()
        }
    }
}
