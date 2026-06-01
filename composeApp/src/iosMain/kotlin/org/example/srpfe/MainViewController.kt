package org.example.srpfe

import androidx.compose.ui.window.ComposeUIViewController
import org.example.srpfe.di.initKoin

fun MainViewController() =
    run {
        initKoin()
        ComposeUIViewController { App() }
    }
