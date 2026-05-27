package org.openapitools.client.models

typealias RoutePlanResponse = RoutePlan
typealias RoutePlanningRequest = RoutePlanning

data class AppUserResponse(
    val firebaseUid: String? = null,
    val displayName: String? = null,
    val email: String? = null,
)

data class CreateShoppingListRequest(
    val name: String,
    val items: List<ShopItem>,
)

data class UpdateShoppingListRequest(
    val name: String,
    val items: List<ShopItem>,
)

data class ShoppingListResponse(
    val id: String,
    val name: String,
    val items: List<ShopItem>,
)
