package org.example

import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.AppUserResponse
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.SalesResponse
import org.openapitools.client.models.ShoppingList
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock

interface ApiRepository {
    suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse

    suspend fun deleteDepartment(departmentId: Int): String

    suspend fun updateDepartment(
        id: String,
        department: Department,
    ): Department

    suspend fun getDepartmentsByMap(mapId: Int): List<Department>

    suspend fun createDepartment(department: Department): Department

    suspend fun deleteMap(id: Int): String

    suspend fun getMap(id: Int): Map

    suspend fun updateMap(
        id: String,
        map: Map,
    ): Map

    suspend fun createMap(map: Map): Map

    suspend fun deleteStore(id: Int): String

    suspend fun getStores(): List<Store>

    suspend fun getStore(id: Int): Store

    suspend fun createStore(store: Store): Store

    suspend fun getSales(store: String): SalesResponse

    suspend fun updateTill(
        id: String,
        till: Till,
    ): Till

    suspend fun createTill(till: Till): Till

    suspend fun deleteTill(tillId: Int): String

    suspend fun getTills(tillId: Int): List<Till>

    suspend fun updateWallBlock(
        id: String,
        wallBlock: WallBlock,
    ): WallBlock

    suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock>

    suspend fun createWallBlock(wallBlock: WallBlock): WallBlock

    suspend fun deleteWallBlock(wallBlockId: Int): String

    suspend fun googleOcr(image: List<Base64ByteArray>): List<CreateShoppingListItemRequest>

    suspend fun getCurrentUser(): AppUserResponse

    suspend fun getShoppingLists(): List<ShoppingList>

    suspend fun getShoppingList(id: Int): ShoppingList

    suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingList

    suspend fun updateShoppingList(
        id: Int,
        request: CreateShoppingListRequest,
    ): ShoppingList

    suspend fun deleteShoppingList(id: Int)
}
