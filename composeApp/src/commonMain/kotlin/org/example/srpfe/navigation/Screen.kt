package org.example.srpfe.navigation

sealed class Screen(
    val route: String,
    val label: String,
) {
    companion object {
        const val storeIdArg = "storeId"
    }

    data object Nearby : Screen("nearby", "Nearby")

    data object Stores : Screen("stores", "Stores")

    data object ShoppingList : Screen("shoppinglist", "Lists")

    data object Sales : Screen("sales", "Sales")

    data object CreateStore : Screen("stores/create", "Add Store")

    data object StoreDetails : Screen("stores/{$storeIdArg}", "Store Details") {
        fun route(storeId: Int): String = "stores/$storeId"
    }

    data object MapDrawer : Screen("mapdrawer/{$storeIdArg}", "Map") {
        fun route(storeId: Int): String = "mapdrawer/$storeId"
    }

    data object Profile : Screen("profile", "Profile")

    data object PhysicalList : Screen("physicallist", "Scan")
}
