package org.example.srp_fe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.example.srp_fe.repository.DefaultApiRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App(DefaultApiRepository())
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App(DefaultApiRepository())
}