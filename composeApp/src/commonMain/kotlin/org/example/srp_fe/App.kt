package org.example.srp_fe

import org.example.srp_fe.screens.shopmapdrawer.ShopMapDrawerScreen
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.openapitools.client.apis.DefaultApi

@Composable
@Preview
fun App() {
    MaterialTheme {
        val api = DefaultApi()

//        //run in default coroutine scope
//        CoroutineScope(Dispatchers.Default).launch {
//            api.mapsIdGet(1).body().let {
//                println("API response:")
//                println(it)
//            }
//        }

        ShopMapDrawerScreen()
    }
}