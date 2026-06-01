package org.example.srpfe

import kotlinx.coroutines.runBlocking
import org.example.ApiRepository
import org.example.srpfe.screens.shoppinglist.ShoppingListViewModel
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.AppUserResponse
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.ShoppingList
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SanityTest {
    @Test
    fun shoppingListViewModelSurfacesRepositoryErrorsFromKoin() =
        runBlocking {
            val koinApp =
                koinApplication {
                    modules(
                        module {
                            single<ApiRepository> { FailingShoppingListRepository() }
                            factory { ShoppingListViewModel(get()) }
                        },
                    )
                }

            val viewModel = koinApp.koin.get<ShoppingListViewModel>()

            viewModel.loadShoppingLists()

            assertEquals("boom", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isLoading)
        }
}

private class FailingShoppingListRepository : ApiRepository {
    override suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse = notImplemented()

    override suspend fun deleteDepartment(departmentId: Int): String = notImplemented()

    override suspend fun updateDepartment(
        id: String,
        department: Department,
    ): Department = notImplemented()

    override suspend fun getDepartmentsByMap(mapId: Int): List<Department> = notImplemented()

    override suspend fun createDepartment(department: Department): Department = notImplemented()

    override suspend fun deleteMap(id: Int): String = notImplemented()

    override suspend fun getMap(id: Int): Map = notImplemented()

    override suspend fun updateMap(
        id: String,
        map: Map,
    ): Map = notImplemented()

    override suspend fun createMap(map: Map): Map = notImplemented()

    override suspend fun deleteStore(id: Int): String = notImplemented()

    override suspend fun getStore(id: Int): Store = notImplemented()

    override suspend fun createStore(store: Store): Store = notImplemented()

    override suspend fun updateTill(
        id: String,
        till: Till,
    ): Till = notImplemented()

    override suspend fun createTill(till: Till): Till = notImplemented()

    override suspend fun deleteTill(tillId: Int): String = notImplemented()

    override suspend fun getTills(tillId: Int): List<Till> = notImplemented()

    override suspend fun updateWallBlock(
        id: String,
        wallBlock: WallBlock,
    ): WallBlock = notImplemented()

    override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> = notImplemented()

    override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock = notImplemented()

    override suspend fun deleteWallBlock(wallBlockId: Int): String = notImplemented()

    override suspend fun googleOcr(image: List<Base64ByteArray>): List<CreateShoppingListItemRequest> = notImplemented()

    override suspend fun getCurrentUser(): AppUserResponse = notImplemented()

    override suspend fun getShoppingLists(): List<ShoppingList> = error("boom")

    override suspend fun getShoppingList(id: Int): ShoppingList = notImplemented()

    override suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingList = notImplemented()

    override suspend fun updateShoppingList(
        id: Int,
        request: CreateShoppingListRequest,
    ): ShoppingList = notImplemented()

    override suspend fun deleteShoppingList(id: Int) {
        notImplemented<Unit>()
    }

    private fun <T> notImplemented(): T = error("Not needed in this test")
}
