package org.example.srpfe

import androidx.compose.ui.window.ComposeUIViewController
import org.example.srpfe.repository.DefaultApiRepository

fun MainViewController() = ComposeUIViewController { App(DefaultApiRepository()) }
