package org.example.srpfe.navigation

sealed class Screen(
    val route: String,
    val label: String,
) {
    data object Nearby : Screen("nearby", "Nearby")

    data object ShoppingList : Screen("shoppinglist", "Lists")

    data object Sales : Screen("sales", "Sales")

    data object MapDrawer : Screen("mapdrawer", "Map")

    data object Profile : Screen("profile", "Profile")

    data object PhysicalList : Screen("physicallist", "Scan")
}
