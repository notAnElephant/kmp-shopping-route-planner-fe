package org.example.srp_fe

import ShoppingListScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import org.example.srp_fe.screens.shopmapdrawer.ShopMapDrawerScreen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import io.ktor.util.PlatformUtils
import org.example.ApiRepository
import org.example.srp_fe.navigation.Screen
import org.example.srp_fe.screens.camera.CameraSetupScreen
import org.example.srp_fe.screens.physicallist.PhysicalListScreen
import org.example.srp_fe.screens.profile.ProfileScreen
import org.example.srp_fe.utils.isMobile
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(apiRepository : ApiRepository) {
    MaterialTheme {
//        //run in default coroutine scope
//        CoroutineScope(Dispatchers.Default).launch {
//            api.mapsIdGet(1).body().let {
//                println("API response:")
//                println(it)
//            }
//        }

        //TODO maybe don't hardcode the web api key here
        GoogleAuthProvider.create(credentials = GoogleAuthCredentials(serverId = "AIzaSyAug1ohAVBvOavKiBX7yTc8UWqOXHkl8tw"))
        MainScreen(apiRepository)
    }
}

@Composable
fun MainScreen(apiRepository: ApiRepository) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.MapDrawer.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.MapDrawer.route) { ShopMapDrawerScreen(apiRepository, navController) }
            composable(Screen.ShoppingList.route) { ShoppingListScreen(apiRepository, navController) }
            composable(Screen.Profile.route) { ProfileScreen(apiRepository, navController) }
            if (isMobile()) //TODO currently its actually "isandroid"
//                composable(Screen.PhysicalList.route) { PhysicalListScreen(apiRepository, navController) }
                composable(Screen.PhysicalList.route) { CameraSetupScreen() }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(Screen.MapDrawer, Screen.ShoppingList, Screen.Profile, Screen.PhysicalList)

    BottomNavigation {
        items.forEach { screen ->
            BottomNavigationItem(
                icon = { /* Add icon here */ },
                label = { Text(screen.route) },
                selected = false, // You can check currentRoute if needed
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

