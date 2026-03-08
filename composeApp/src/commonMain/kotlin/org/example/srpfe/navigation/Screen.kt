package org.example.srpfe.navigation

sealed class Screen(val route: String) {
    data object Nearby : Screen("nearby")
    data object ShoppingList : Screen("shoppinglist")
    data object MapDrawer : Screen("mapdrawer")
    data object Profile : Screen("profile")
    data object PhysicalList : Screen("physicallist")
}
