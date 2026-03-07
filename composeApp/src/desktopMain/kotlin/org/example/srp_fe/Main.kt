package org.example.srpfe

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.example.srpfe.repository.DefaultApiRepository

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ShoppingRoutePlanner-FE",
        ) {
            App(DefaultApiRepository())
        }
    }
