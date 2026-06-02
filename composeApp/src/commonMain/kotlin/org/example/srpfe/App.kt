package org.example.srpfe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.savedstate.read
import com.mmk.kmpauth.google.GoogleAuthCredentials
import com.mmk.kmpauth.google.GoogleAuthProvider
import org.example.srpfe.auth.AuthConfig
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.auth.FirebaseAuthSessionBridge
import org.example.srpfe.navigation.Screen
import org.example.srpfe.screens.stores.CreateStoreScreen
import org.example.srpfe.screens.stores.StoreDetailsScreen
import org.example.srpfe.screens.stores.StoresScreen
import org.example.srpfe.screens.camera.CameraSetupScreen
import org.example.srpfe.screens.nearby.NearbyShopsScreen
import org.example.srpfe.screens.physicallist.PlatformPhysicalListScreen
import org.example.srpfe.screens.profile.ProfileScreen
import org.example.srpfe.screens.sales.SalesScreen
import org.example.srpfe.screens.shopmapdrawer.ShopMapDrawerScreen
import org.example.srpfe.screens.shoppinglist.ShoppingListScreen
import org.example.srpfe.utils.isMobile
import org.koin.compose.koinInject

@Composable
fun App() {
    val authSession = koinInject<AuthSession>()
    val apiRepository = koinInject<org.example.ApiRepository>()

    LaunchedEffect(authSession) {
        authSession.syncFromPlatformAuth()
        FirebaseAuthSessionBridge.idTokenChanges().collect { user ->
            authSession.syncFromFirebaseUser(user)
            if (user != null) {
                runCatching {
                    apiRepository.getCurrentUser()
                }
            }
        }
    }

    MaterialTheme {
        if (AuthConfig.GOOGLE_SERVER_CLIENT_ID.isNotBlank()) {
            GoogleAuthProvider.create(
                credentials = GoogleAuthCredentials(serverId = AuthConfig.GOOGLE_SERVER_CLIENT_ID),
            )
        }

        MainScreen()
    }
}

@Composable
fun MainScreen() {
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
                composable(Screen.Nearby.route) { NearbyShopsScreen() }
            }
            composable(Screen.Stores.route) {
                StoresScreen(
                    onCreateStore = { navController.navigate(Screen.CreateStore.route) },
                    onOpenStore = { storeId -> navController.navigate(Screen.StoreDetails.route(storeId)) },
                )
            }
            composable(Screen.CreateStore.route) {
                CreateStoreScreen(
                    onBack = { navController.popBackStack() },
                    onStoreCreated = { storeId ->
                        navController.navigate(Screen.StoreDetails.route(storeId)) {
                            popUpTo(Screen.Stores.route)
                        }
                    },
                )
            }
            composable(
                route = Screen.StoreDetails.route,
                arguments = listOf(navArgument(Screen.storeIdArg) { type = NavType.IntType }),
            ) { backStackEntry ->
                val storeId =
                    backStackEntry.arguments?.read { getIntOrNull(Screen.storeIdArg) }
                        ?: return@composable
                StoreDetailsScreen(
                    storeId = storeId,
                    onBack = { navController.popBackStack() },
                    onOpenMapEditor = { navController.navigate(Screen.MapDrawer.route(storeId)) },
                    onStoreDeleted = {
                        navController.popBackStack(Screen.Stores.route, false)
                    },
                )
            }
            composable(Screen.Sales.route) { SalesScreen() }
            composable(
                route = Screen.MapDrawer.route,
                arguments = listOf(navArgument(Screen.storeIdArg) { type = NavType.IntType }),
            ) { backStackEntry ->
                val storeId =
                    backStackEntry.arguments?.read { getIntOrNull(Screen.storeIdArg) }
                        ?: return@composable
                ShopMapDrawerScreen(
                    storeId = storeId,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Screen.ShoppingList.route) { ShoppingListScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }
            composable(Screen.PhysicalList.route) {
                if (isMobile()) {
                    CameraSetupScreen()
                } else {
                    PlatformPhysicalListScreen()
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items =
        buildList {
            if (isMobile()) {
                add(Screen.Nearby)
            }
            add(Screen.Stores)
            add(Screen.Sales)
            add(Screen.ShoppingList)
            add(Screen.Profile)
            add(Screen.PhysicalList)
        }

    NavigationBar {
        items.forEach { screen ->
            val selected =
                when (screen) {
                    Screen.Stores ->
                        currentRoute?.startsWith(Screen.Stores.route) == true ||
                            currentRoute?.startsWith("mapdrawer/") == true
                    else -> currentRoute == screen.route
                }
            NavigationBarItem(
                icon = { /* Add icon here */ },
                label = { Text(screen.label) },
                selected = selected,
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
