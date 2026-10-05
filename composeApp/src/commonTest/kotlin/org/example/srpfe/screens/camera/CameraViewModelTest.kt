package org.example.srpfe.screens.camera

import kotlinx.coroutines.runBlocking
import org.example.ApiRepository
import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.AppUserResponse
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.SalesResponse
import org.openapitools.client.models.ShoppingList
import org.openapitools.client.models.Store
import org.openapitools.client.models.StoreDetailsResponse
import org.openapitools.client.models.StorePlaceDetailsResponse
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class CameraViewModelTest {
    @Test
    fun uploadedImageIsSentToOcrAndReturnedItemsAreExposed() = runBlocking {
        val image = byteArrayOf(1, 2, 3, 4)
        val expected = listOf(
            CreateShoppingListItemRequest(shoppingItemName = "Milk", attributes = "2%"),
            CreateShoppingListItemRequest(shoppingItemName = "Bread", attributes = "Whole grain"),
        )
        val repository = FakeOcrRepository(expected)
        val viewModel = CameraViewModel(repository)

        viewModel.processUploadedImageWithOcr(image)

        assertContentEquals(image, repository.receivedImage)
        assertEquals(expected, viewModel.ocrResult.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun ocrFailureIsExposedAndLoadingIsCleared() = runBlocking {
        val viewModel = CameraViewModel(FakeOcrRepository(failure = IllegalStateException("OCR unavailable")))

        viewModel.processUploadedImageWithOcr(byteArrayOf(1))

        assertEquals("OCR unavailable", viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }
}

private class FakeOcrRepository(
    private val result: List<CreateShoppingListItemRequest> = emptyList(),
    private val failure: Exception? = null,
) : ApiRepository {
    var receivedImage: ByteArray? = null

    override suspend fun googleOcr(image: List<Base64ByteArray>): List<CreateShoppingListItemRequest> {
        receivedImage = image.single().value
        failure?.let { throw it }
        return result
    }

    override suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse = TODO()
    override suspend fun deleteDepartment(departmentId: Int): String = TODO()
    override suspend fun updateDepartment(id: String, department: Department): Department = TODO()
    override suspend fun getDepartmentsByMap(mapId: Int): List<Department> = TODO()
    override suspend fun createDepartment(department: Department): Department = TODO()
    override suspend fun deleteMap(id: Int): String = TODO()
    override suspend fun getMap(id: Int): Map = TODO()
    override suspend fun updateMap(id: String, map: Map): Map = TODO()
    override suspend fun createMap(map: Map): Map = TODO()
    override suspend fun deleteStore(id: Int): String = TODO()
    override suspend fun getStores(): List<Store> = TODO()
    override suspend fun getStore(id: Int): Store = TODO()
    override suspend fun createStore(store: Store): Store = TODO()
    override suspend fun updateStore(id: Int, store: Store): Store = TODO()
    override suspend fun getStorePlaceDetails(id: Int): StorePlaceDetailsResponse = TODO()
    override suspend fun getStoreComponentDetails(id: Int): StoreDetailsResponse = TODO()
    override suspend fun getSales(store: String): SalesResponse = TODO()
    override suspend fun updateTill(id: String, till: Till): Till = TODO()
    override suspend fun createTill(till: Till): Till = TODO()
    override suspend fun deleteTill(tillId: Int): String = TODO()
    override suspend fun getTills(tillId: Int): List<Till> = TODO()
    override suspend fun updateWallBlock(id: String, wallBlock: WallBlock): WallBlock = TODO()
    override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> = TODO()
    override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock = TODO()
    override suspend fun deleteWallBlock(wallBlockId: Int): String = TODO()
    override suspend fun getCurrentUser(): AppUserResponse = TODO()
    override suspend fun getShoppingLists(): List<ShoppingList> = TODO()
    override suspend fun getShoppingList(id: Int): ShoppingList = TODO()
    override suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingList = TODO()
    override suspend fun updateShoppingList(id: Int, request: CreateShoppingListRequest): ShoppingList = TODO()
    override suspend fun deleteShoppingList(id: Int): Unit = TODO()
}
