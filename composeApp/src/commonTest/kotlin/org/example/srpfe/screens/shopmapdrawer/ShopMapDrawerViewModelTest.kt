package org.example.srpfe.screens.shopmapdrawer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.example.ApiRepository
import org.example.srpfe.mapping.toModel
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
import org.openapitools.client.models.StoreDetailsResponse
import org.openapitools.client.models.StorePlaceDetailsResponse
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ShopMapDrawerViewModelTest {
    @Test
    fun geometryHelpersClampMoveResizeAndHandleHitTesting() {
        val rect = CanvasRect(Offset(20f, 20f), Size(30f, 40f))
        val moved = moveRect(rect, Offset(100f, -50f), Size(80f, 70f))
        assertEquals(50f, moved.left)
        assertEquals(0f, moved.top)

        val minSize = Size(10f, 12f)
        val resizedTop = resizeRect(rect, ResizeHandle.TOP, Offset(0f, 35f), Size(100f, 100f), minSize)
        val resizedRight = resizeRect(rect, ResizeHandle.RIGHT, Offset(80f, 0f), Size(60f, 100f), minSize)
        val resizedBottom = resizeRect(rect, ResizeHandle.BOTTOM, Offset(0f, 80f), Size(100f, 70f), minSize)
        val resizedLeft = resizeRect(rect, ResizeHandle.LEFT, Offset(25f, 0f), Size(100f, 100f), minSize)

        assertEquals(48f, resizedTop.top)
        assertEquals(12f, resizedTop.size.height)
        assertEquals(60f, resizedRight.right)
        assertEquals(40f, resizedRight.size.width)
        assertEquals(70f, resizedBottom.bottom)
        assertEquals(50f, resizedBottom.size.height)
        assertEquals(40f, resizedLeft.left)
        assertEquals(10f, resizedLeft.size.width)

        assertEquals(ResizeHandle.TOP, hitTestHandle(rect, handleCenter(rect, ResizeHandle.TOP)))
    }

    @Test
    fun updateAndDeleteOperationsMutateLoadedUiState() {
        runBlocking {
            val repository = FakeShopMapRepository()
            val viewModel = ShopMapDrawerViewModel(repository, 1)
            seedUiState(viewModel, repository.initialUiState())

            viewModel.updateDepartmentRect(
                departmentId = 11,
                name = "Produce",
                width = 20,
                height = 25,
                startX = 7,
                startY = 30,
            )
            viewModel.updateWallBlockRect(
                wallBlockId = 21,
                width = 8,
                height = 30,
                startX = 1,
                startY = 60,
            )
            viewModel.updateTillRect(
                tillId = 31,
                width = 14,
                height = 9,
                startX = 50,
                startY = 15,
            )
            repeat(20) { yield() }

            val updatedState = viewModel.uiState.value
            assertEquals(20.0, updatedState.concreteDepartments.single { it.id == 11 }.width)
            assertEquals(8.0, updatedState.wallBlocks.single { it.id == 21 }.width)
            assertEquals(14.0, updatedState.tills.single { it.id == 31 }.width)

            viewModel.deleteDepartment(11)
            viewModel.deleteWallBlock(21)
            viewModel.deleteTill(31)
            repeat(20) { yield() }

            assertNull(viewModel.uiState.value.concreteDepartments.firstOrNull { it.id == 11 })
            assertNull(viewModel.uiState.value.wallBlocks.firstOrNull { it.id == 21 })
            assertNull(viewModel.uiState.value.tills.firstOrNull { it.id == 31 })
        }
    }

    @Test
    fun movingAnchorsAndFailedUpdatesRollbackState() {
        runBlocking {
            val repository = FakeShopMapRepository()
            val viewModel = ShopMapDrawerViewModel(repository, 1)
            seedUiState(viewModel, repository.initialUiState())

            viewModel.updateEntrancePosition(startX = 14, startY = 5)
            viewModel.updateExitPosition(startX = 80, startY = 180)
            repeat(20) { yield() }

            val movedMap = assertNotNull(viewModel.uiState.value.map)
            assertEquals(14.0, movedMap.entranceX)
            assertEquals(5.0, movedMap.entranceY)
            assertEquals(80.0, movedMap.exitX)
            assertEquals(180.0, movedMap.exitY)

            repository.failDepartmentUpdate = true
            viewModel.updateDepartmentRect(
                departmentId = 11,
                name = "Produce",
                width = 99,
                height = 99,
                startX = 99,
                startY = 99,
            )
            repeat(20) { yield() }

            val rolledBackDepartment = viewModel.uiState.value.concreteDepartments.single { it.id == 11 }
            assertEquals(12.0, rolledBackDepartment.width)
            assertEquals(10.0, rolledBackDepartment.startX)
            assertNotNull(viewModel.uiState.value.errorState)
        }
    }

    private fun seedUiState(
        viewModel: ShopMapDrawerViewModel,
        state: UiState,
    ) {
        @Suppress("UNCHECKED_CAST")
        val stateFlow =
            viewModel
                .javaClass
                .getDeclaredField("_uiState")
                .apply { isAccessible = true }
                .get(viewModel) as MutableStateFlow<UiState>
        stateFlow.value = state
    }
}

private class FakeShopMapRepository : ApiRepository {
    private val store = Store(id = 1, name = "Store", location = "Budapest")
    private var map =
        Map(
            id = 2,
            storeId = 1,
            width = 100.0,
            height = 200.0,
            entranceX = 50.0,
            entranceY = 1.0,
            exitX = 50.0,
            exitY = 190.0,
        )
    private val departments =
        mutableListOf(
            Department(
                id = 11,
                mapId = 2,
                name = "Produce",
                width = 12.0,
                height = 14.0,
                startX = 10.0,
                startY = 20.0,
            ),
        )
    private val wallBlocks =
        mutableListOf(
            WallBlock(
                id = 21,
                mapId = 2,
                width = 6.0,
                height = 18.0,
                startX = 2.0,
                startY = 40.0,
            ),
        )
    private val tills =
        mutableListOf(
            Till(
                id = 31,
                mapId = 2,
                width = 10.0,
                height = 8.0,
                startX = 40.0,
                startY = 12.0,
            ),
        )

    var failDepartmentUpdate = false

    fun initialUiState(): UiState =
        UiState(
            store = store,
            tills = tills.toList(),
            map = map,
            concreteDepartments = departments.map { it.toModel(color = Color.Red) },
            wallBlocks = wallBlocks.toList(),
            isLoading = false,
        )

    override suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse = notImplemented()

    override suspend fun deleteDepartment(departmentId: Int): String {
        departments.removeAll { it.id == departmentId }
        return "deleted"
    }

    override suspend fun updateDepartment(
        id: String,
        department: Department,
    ): Department {
        if (failDepartmentUpdate) {
            failDepartmentUpdate = false
            error("department update failed")
        }
        val departmentId = id.toInt()
        val index = departments.indexOfFirst { it.id == departmentId }
        val updated = department.copy(id = departmentId)
        departments[index] = updated
        return updated
    }

    override suspend fun getDepartmentsByMap(mapId: Int): List<Department> = departments.toList()

    override suspend fun createDepartment(department: Department): Department = notImplemented()

    override suspend fun deleteMap(id: Int): String = notImplemented()

    override suspend fun getMap(id: Int): Map = map

    override suspend fun updateMap(
        id: String,
        map: Map,
    ): Map {
        this.map = map.copy(id = id.toInt())
        return this.map
    }

    override suspend fun createMap(map: Map): Map = notImplemented()

    override suspend fun deleteStore(id: Int): String = notImplemented()

    override suspend fun getStores(): List<Store> = listOf(store)

    override suspend fun getStore(id: Int): Store = store

    override suspend fun createStore(store: Store): Store = notImplemented()

    override suspend fun updateStore(
        id: Int,
        store: Store,
    ): Store = notImplemented()

    override suspend fun getStorePlaceDetails(id: Int): StorePlaceDetailsResponse = notImplemented()

    override suspend fun getStoreComponentDetails(id: Int): StoreDetailsResponse =
        StoreDetailsResponse(
            store = store,
            departments = departments.toList(),
            wallBlocks = wallBlocks.toList(),
            tills = tills.toList(),
            map = map,
        )

    override suspend fun getSales(store: String): SalesResponse = notImplemented()

    override suspend fun updateTill(
        id: String,
        till: Till,
    ): Till {
        val tillId = id.toInt()
        val index = tills.indexOfFirst { it.id == tillId }
        val updated = till.copy(id = tillId)
        tills[index] = updated
        return updated
    }

    override suspend fun createTill(till: Till): Till = notImplemented()

    override suspend fun deleteTill(tillId: Int): String {
        tills.removeAll { it.id == tillId }
        return "deleted"
    }

    override suspend fun getTills(tillId: Int): List<Till> = tills.toList()

    override suspend fun updateWallBlock(
        id: String,
        wallBlock: WallBlock,
    ): WallBlock {
        val wallBlockId = id.toInt()
        val index = wallBlocks.indexOfFirst { it.id == wallBlockId }
        val updated = wallBlock.copy(id = wallBlockId)
        wallBlocks[index] = updated
        return updated
    }

    override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> = wallBlocks.toList()

    override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock = notImplemented()

    override suspend fun deleteWallBlock(wallBlockId: Int): String {
        wallBlocks.removeAll { it.id == wallBlockId }
        return "deleted"
    }

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
