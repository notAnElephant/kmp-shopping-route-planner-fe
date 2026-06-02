package org.example.srpfe.screens.shopmapdrawer

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.ApiRepository
import org.example.srpfe.mapping.toModel
import org.example.srpfe.model.DepartmentModel
import org.lighthousegames.logging.logging
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningProductRequest
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock

data class UiState(
    val store: Store? = null,
    val tills: List<Till> = emptyList(),
    val map: Map? = null,
    val concreteDepartments: List<DepartmentModel> = emptyList(),
    val wallBlocks: List<WallBlock> = emptyList(),
    val route: RoutePlanResponse? = null,
    val isLoading: Boolean = false,
    val isCreatingMap: Boolean = false,
    val errorState: String? = null,
)

class ShopMapDrawerViewModel(
    private val apiRepository: ApiRepository,
    private val storeId: Int,
) : ViewModel() {
    companion object {
        val log = logging()
    }

    private val _uiState = MutableStateFlow(UiState(isLoading = true))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // TODO width and height are hardcoded for now
    val width = 100.0
    val height = 200.0

    init {
        refreshStoreComponents()
    }

    fun refreshStoreComponents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorState = null)
            runCatching {
                apiRepository.getStoreComponentDetails(storeId)
            }.onSuccess { details ->
                _uiState.value =
                    _uiState.value.copy(
                        store = details.store,
                        map = details.map,
                        tills = details.tills,
                        wallBlocks = details.wallBlocks,
                        concreteDepartments =
                            details.departments.map { department ->
                                department.toModel(
                                    color = colorForDepartmentName(department.name),
                                    isSelected = false,
                                )
                            },
                        isLoading = false,
                    )
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isLoading = false,
                        errorState = error.message ?: "Could not load store map details.",
                    )
            }
        }
    }

    fun createMapForStore() {
        viewModelScope.launch {
            val store = _uiState.value.store ?: run {
                _uiState.value = _uiState.value.copy(errorState = "Store details are not loaded yet.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isCreatingMap = true, errorState = null)
            runCatching {
                val map =
                    apiRepository.createMap(
                        Map(
                            width = width,
                            height = height,
                            storeId = store.id ?: storeId,
                            entranceX = width / 2,
                            entranceY = 1.0,
                            exitX = width / 2,
                            exitY = height,
                        ),
                    )
                createDefaultTill(map.id ?: error("Map was created without an id."))
            }.onSuccess {
                _uiState.value = _uiState.value.copy(isCreatingMap = false)
                refreshStoreComponents()
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        isCreatingMap = false,
                        errorState = error.message ?: "Could not create map.",
                    )
            }
        }
    }

    private suspend fun createDefaultTill(mapId: Int) {
        val tillHeight = 10.0
        val tillWidth = 20.0
        apiRepository.createTill(
            Till(
                mapId = mapId,
                width = tillWidth,
                height = tillHeight,
                startX = width / 2,
                startY = height - tillHeight,
            ),
        )
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(errorState = null)
    }

    fun calculateRoute(selectedDepartments: List<DepartmentModel>) {
        viewModelScope.launch {
            val map = _uiState.value.map ?: run {
                _uiState.value = _uiState.value.copy(errorState = "Create a map before calculating a route.")
                return@launch
            }

            runCatching {
                apiRepository.calculateRoute(
                    RoutePlanningRequest(
                        mapId = map.id ?: error("Map is not created yet"),
                        products =
                            selectedDepartments.mapIndexed { index, department ->
                                RoutePlanningProductRequest(
                                    articleNo = index + 1,
                                    departmentId = department.id ?: error("Department is not created yet"),
                                    position = department.id.toString(),
                                )
                            },
                    ),
                )
            }.onSuccess { route ->
                _uiState.value = _uiState.value.copy(route = route)
                log.i { "route: $route" }
                log.i { "route length: ${route.route.count()}" }
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        errorState = error.message ?: "Could not calculate route.",
                    )
            }
        }
    }

    fun createDepartment(
        name: String,
        color: Color,
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
    ) {
        viewModelScope.launch {
            val mapId = _uiState.value.map?.id ?: run {
                _uiState.value = _uiState.value.copy(errorState = "Create a map before adding departments.")
                return@launch
            }

            runCatching {
                apiRepository.createDepartment(
                    Department(
                        name = name,
                        mapId = mapId,
                        width = width.toDouble(),
                        height = height.toDouble(),
                        startX = startX.toDouble(),
                        startY = startY.toDouble(),
                    ),
                )
            }.onSuccess { department ->
                _uiState.value =
                    _uiState.value.copy(
                        concreteDepartments =
                            _uiState.value.concreteDepartments +
                                department.toModel(
                                    color = color,
                                    isSelected = true,
                                ),
                    )
                log.i { "created department: $department" }
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        errorState = error.message ?: "Could not create department.",
                    )
            }
        }
    }

    fun createWallBlock(
        width: Int,
        height: Int,
        startX: Int,
        startY: Int,
    ) {
        viewModelScope.launch {
            val mapId = _uiState.value.map?.id ?: run {
                _uiState.value = _uiState.value.copy(errorState = "Create a map before adding walls.")
                return@launch
            }

            runCatching {
                apiRepository.createWallBlock(
                    WallBlock(
                        mapId = mapId,
                        width = width.toDouble(),
                        height = height.toDouble(),
                        startX = startX.toDouble(),
                        startY = startY.toDouble(),
                    ),
                )
            }.onSuccess { wallBlock ->
                _uiState.value =
                    _uiState.value.copy(
                        wallBlocks = _uiState.value.wallBlocks + wallBlock,
                    )
                log.i { "created wall block: $wallBlock" }
            }.onFailure { error ->
                _uiState.value =
                    _uiState.value.copy(
                        errorState = error.message ?: "Could not create wall block.",
                    )
            }
        }
    }

    fun convertToBackendCoordinates(
        canvasSize: Size,
        size: Size,
        x: Float,
        y: Float,
    ): Triple<Size, Int, Int> {
        val widthRatio = width / canvasSize.width
        val heightRatio = height / canvasSize.height

        val newX = (x * widthRatio).toInt() + 1
        val newY = ((canvasSize.height - 1 - y) * heightRatio).toInt()
        val newSize =
            Size(
                (size.width * widthRatio).toFloat(),
                (size.height * heightRatio).toFloat(),
            )

        log.i { "old coordinates: x: $x, y: $y, size: $size" }
        log.i { "calculated coordinates: newX: $newX, newY: $newY, newSize: $newSize" }
        return Triple(newSize, newX, newY)
    }

    fun convertToCanvasCoordinates(
        canvasSize: Size,
        size: Size,
        x: Int,
        y: Int,
    ): Triple<Size, Float, Float> {
        val widthRatio = canvasSize.width / width
        val heightRatio = canvasSize.height / height

        val newX = (x * widthRatio).toFloat()
        val newY = (canvasSize.height - y * heightRatio).toFloat() - 1
        val newSize =
            Size(
                (size.width * widthRatio).toFloat(),
                (size.height * heightRatio).toFloat(),
            )

        log.i { "old coordinates: x: $x, y: $y, size: $size" }
        log.i { "calculated coordinates: newX: $newX, newY: $newY, newSize: $newSize" }
        return Triple(newSize, newX, newY)
    }

    private fun colorForDepartmentName(name: String): Color {
        val hash = name.hashCode()
        val red = ((hash shr 16) and 0x7F) + 64
        val green = ((hash shr 8) and 0x7F) + 64
        val blue = (hash and 0x7F) + 64
        return Color(red / 255f, green / 255f, blue / 255f)
    }
}
