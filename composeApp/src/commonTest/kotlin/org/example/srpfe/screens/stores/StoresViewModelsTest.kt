package org.example.srpfe.screens.stores

import kotlinx.coroutines.runBlocking
import org.example.ApiRepository
import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.AppUserResponse
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.PlaceDetailsResponse
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.SalesResponse
import org.openapitools.client.models.ShoppingList
import org.openapitools.client.models.Store
import org.openapitools.client.models.StoreDetailsResponse
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class StoresViewModelsTest {
    @Test
    fun storesViewModelLoadsSortedStoresAndDeletesStore() =
        runBlocking {
            val repository =
                FakeStoreRepository(
                    stores =
                        mutableListOf(
                            Store(name = "Bravo", id = 2, location = "B"),
                            Store(name = "Alpha", id = 1, location = "A"),
                        ),
                )
            val viewModel = StoresViewModel(repository)

            viewModel.loadStores()

            assertEquals(listOf("Alpha", "Bravo"), viewModel.uiState.value.stores.map(Store::name))

            viewModel.deleteStore(1)

            assertEquals(listOf("Bravo"), viewModel.uiState.value.stores.map(Store::name))
            assertNull(viewModel.uiState.value.deletingStoreId)
        }

    @Test
    fun createStoreViewModelRejectsBlankName() =
        runBlocking {
            val viewModel = CreateStoreViewModel(FakeStoreRepository())

            viewModel.updateDraftLocation("Budapest")
            val result = viewModel.createStore()

            assertNull(result)
            assertEquals("Store name is required.", viewModel.uiState.value.errorMessage)
            assertFalse(viewModel.uiState.value.isSaving)
        }

    @Test
    fun storeDetailsViewModelLoadsUpdatesAndLoadsPlaceDetails() =
        runBlocking {
            val repository =
                FakeStoreRepository(
                    stores = mutableListOf(Store(name = "Original", id = 10, location = "Old place")),
                )
            val viewModel = StoreDetailsViewModel(repository, 10)

            viewModel.loadStore()
            assertEquals("Original", viewModel.uiState.value.draftName)
            assertEquals("Old place", viewModel.uiState.value.draftLocation)

            viewModel.updateDraftName("Updated")
            viewModel.updateDraftLocation("New place")
            assertTrue(viewModel.saveStore())
            assertEquals("Updated", repository.stores.single().name)
            assertEquals("New place", repository.stores.single().location)

            viewModel.loadPlaceDetails()
            assertEquals("google-place-10", viewModel.uiState.value.placeDetails?.id)
        }
}

private class FakeStoreRepository(
    val stores: MutableList<Store> = mutableListOf(),
) : ApiRepository {
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

    override suspend fun deleteStore(id: Int): String {
        stores.removeAll { it.id == id }
        return "deleted"
    }

    override suspend fun getStores(): List<Store> = stores.toList()

    override suspend fun getStore(id: Int): Store = stores.first { it.id == id }

    override suspend fun createStore(store: Store): Store {
        val created = store.copy(id = (stores.maxOfOrNull { it.id ?: 0 } ?: 0) + 1)
        stores += created
        return created
    }

    override suspend fun updateStore(
        id: Int,
        store: Store,
    ): Store {
        val index = stores.indexOfFirst { it.id == id }
        val updated = store.copy(id = id)
        stores[index] = updated
        return updated
    }

    override suspend fun getStorePlaceDetails(id: Int): PlaceDetailsResponse =
        PlaceDetailsResponse(
            id = "google-place-$id",
            internationalPhoneNumber = "+36 1 555 000$id",
        )

    override suspend fun getStoreComponentDetails(id: Int): StoreDetailsResponse =
        StoreDetailsResponse(
            store = getStore(id),
            departments = emptyList(),
            wallBlocks = emptyList(),
            tills = emptyList(),
            map = null,
        )

    override suspend fun getSales(store: String): SalesResponse = notImplemented()

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

    override suspend fun getShoppingLists(): List<ShoppingList> = notImplemented()

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
