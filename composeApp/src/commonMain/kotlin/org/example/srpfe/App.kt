package org.example.srpfe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.example.ApiRepository
import org.example.srpfe.auth.AuthConfig
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.auth.FirebaseAuthSessionBridge
import org.example.srpfe.navigation.Screen
import org.example.srpfe.repository.DefaultApiRepository
import org.example.srpfe.screens.camera.CameraSetupScreen
import org.example.srpfe.screens.nearby.NearbyShopsScreen
import org.example.srpfe.screens.physicallist.PlatformPhysicalListScreen
import org.example.srpfe.screens.profile.ProfileScreen
import org.example.srpfe.screens.shopmapdrawer.ShopMapDrawerScreen
import org.example.srpfe.screens.shoppinglist.ShoppingListScreen
import org.example.srpfe.utils.isMobile
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val authSession = remember { AuthSession() }
    val apiRepository = remember(authSession) { DefaultApiRepository(authSession) }

    LaunchedEffect(authSession) {
        authSession.syncFromPlatformAuth()
        FirebaseAuthSessionBridge.idTokenChanges().collect { user ->
            authSession.syncFromFirebaseUser(user)
        }
    }

    MaterialTheme {
        if (AuthConfig.GOOGLE_SERVER_CLIENT_ID.isNotBlank()) {
            GoogleAuthProvider.create(
                credentials = GoogleAuthCredentials(serverId = AuthConfig.GOOGLE_SERVER_CLIENT_ID),
            )
        }

        MainScreen(apiRepository, authSession)
    }
}

@Composable
fun MainScreen(
    apiRepository: ApiRepository,
    authSession: AuthSession,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.PhysicalList.route, // TODO not this in prod, obv.
            modifier = Modifier.padding(paddingValues),
        ) {
            if (isMobile()) {
                composable(Screen.Nearby.route) { NearbyShopsScreen(apiRepository, navController) }
            }
            composable(Screen.MapDrawer.route) { ShopMapDrawerScreen(apiRepository, navController) }
            composable(Screen.ShoppingList.route) { ShoppingListScreen(apiRepository, navController, authSession) }
            composable(Screen.Profile.route) { ProfileScreen(apiRepository, navController, authSession) }
            composable(Screen.PhysicalList.route) {
                if (isMobile()) {
                    CameraSetupScreen(apiRepository, navController)
                } else {
                    PlatformPhysicalListScreen(apiRepository, navController)
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items =
        buildList {
            if (isMobile()) {
                add(Screen.Nearby)
            }
            add(Screen.MapDrawer)
            add(Screen.ShoppingList)
            add(Screen.Profile)
            add(Screen.PhysicalList)
        }

    NavigationBar {
        items.forEach { screen ->
            NavigationBarItem(
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
                },
            )
        }
    }
}
