package org.example.srp_fe.screens.shopmapdrawer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.example.ApiRepository
import org.example.srp_fe.mapping.toModel
import org.example.srp_fe.model.DepartmentModel
import org.lighthousegames.logging.logging
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlan
import org.openapitools.client.models.RoutePlanning
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock

data class UiState(
	val store: Store? = null,
	val tills: List<Till> = emptyList(),
	val map: Map? = null,
	val concreteDepartments: List<DepartmentModel> = emptyList(),
	val wallBlocks: List<WallBlock> = emptyList(),
	val route: RoutePlan? = null,
	val errorState: String? = null,
)

class ShopMapDrawerViewModel(private val apiRepository: ApiRepository) : ViewModel() {
	companion object {
		val log = logging()
	}



	private val _uiState = MutableStateFlow(UiState())
	val uiState: StateFlow<UiState> = _uiState

	//TODO width and height are hardcoded for now
	val width = 100.0
	val height = 200.0

	init {
		viewModelScope.launch {
			val store = createStore()
			val map = createMap(
				Map(
					width = width,
					height = height,
					storeId = store.id ?: throw IllegalStateException("Store is not created yet"),
					entranceX = width / 2,
					entranceY = 1.0,
					exitX = width / 2,
					exitY = height,
				)
			)
			createTill(width / 2, height)
		}
	}

	private suspend fun createStore(): Store {
		return apiRepository.createStore(
			store = Store(
				name = "Store 10",
				location = "Szondi utca"
			)
		).also {
			_uiState.value = _uiState.value.copy(store = it)
			log.i { "created store: ${_uiState.value.store}" }
		}
	}

	private suspend fun createMap(map: Map): Map {
		return apiRepository.createMap(map).also {
			_uiState.value = _uiState.value.copy(map = it)
			log.i { "created map: ${_uiState.value.map}" }
		}
	}

	private suspend fun createTill(startX: Double, startY: Double) {
		val tillHeight = 10.0
		val tillWidth = 20.0
		apiRepository.createTill(
			Till(
				mapId = _uiState.value.map?.id
					?: throw IllegalStateException("Map is not created yet"),
				width = tillWidth,
				height = tillHeight,
				startX = startX,
				startY = startY - tillHeight
			)
		).also {
			_uiState.value = _uiState.value.copy(tills = _uiState.value.tills + it)
			log.i { "created till: $it" }
		}
	}
	fun dismissError() {
		_uiState.value = _uiState.value.copy(errorState = null)
	}

	fun calculateRoute(selectedDepartments: List<DepartmentModel>) {
		try {
			viewModelScope.launch {
				apiRepository.calculateRoute(
					RoutePlanning(
						mapId = _uiState.value.map?.id
							?: throw IllegalStateException("Map is not created yet"),
						departmentIds = selectedDepartments.map {
							it.id
								?: throw IllegalStateException("org.example.srp_fe.model.Department is not created yet")
						},
					)
				).let {
					_uiState.value = _uiState.value.copy(route = it)
					log.i { "route: $it" }
					log.i { "route length: ${it.route.count()}" }
				}
			}
		} catch (e: IOException) {
			_uiState.value = _uiState.value.copy(errorState = "Network error: ${e.cause?.message}")
		} catch (e: Exception) {
			_uiState.value = _uiState.value.copy(errorState = "Unexpected error: ${e.cause?.message}")
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
		try {
			viewModelScope.launch {
				apiRepository.createDepartment(
					Department(
						name = name,
						mapId = _uiState.value.map?.id
							?: throw IllegalStateException("Map is not created yet"),
						width = width.toDouble(),
						height = height.toDouble(),
						startX = startX.toDouble(),
						startY = startY.toDouble()
					)
				).let {
					_uiState.value = _uiState.value.copy(
						concreteDepartments = _uiState.value.concreteDepartments + it.toModel(
							color,
							true
						)
					)
					log.i { "created department: $it" }
				}
			}
		} catch (e: IOException) {
			_uiState.value = _uiState.value.copy(errorState = "Network error: ${e.cause?.message}")
		} catch (e: Exception) {
			_uiState.value = _uiState.value.copy(errorState = "Unexpected rror: ${e.cause?.message}")
		}
	}

	fun createWallBlock(
		width: Int,
		height: Int,
		startX: Int,
		startY: Int,
	) {
		try {
			viewModelScope.launch {
				apiRepository.createWallBlock(
					WallBlock(
						mapId = _uiState.value.map?.id
							?: throw IllegalStateException("Map is not created yet"),
						width = width.toDouble(),
						height = height.toDouble(),
						startX = startX.toDouble(),
						startY = startY.toDouble()
					)
				).let {
					_uiState.value = _uiState.value.copy(
						wallBlocks = _uiState.value.wallBlocks + it
					)
					log.i { "created wall block: $it" }
				}
			}
		} catch (e: IOException) {
			_uiState.value = _uiState.value.copy(errorState = "Network error: ${e.cause?.message}")
		} catch (e: Exception) {
			_uiState.value = _uiState.value.copy(errorState = "Unexpected rror: ${e.cause?.message}")
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

		// Calculate backend coordinates
		val newX = (x * widthRatio).toInt() + 1
		val newY = ((canvasSize.height - 1 - y) * heightRatio).toInt()  // Invert the y-axis
		val newSize = Size(
			(size.width * widthRatio).toFloat(),
			(size.height * heightRatio).toFloat()
		)

		// Return the new size and coordinates as a tuple
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

		// Calculate canvas coordinates
		val newX = (x * widthRatio).toFloat()
		val newY = (canvasSize.height - y * heightRatio).toFloat() - 1 // Invert the y-axis
		val newSize = Size(
			(size.width * widthRatio).toFloat(),
			(size.height * heightRatio).toFloat()
		)

		// Return the new size and coordinates as a tuple
		log.i { "old coordinates: x: $x, y: $y, size: $size" }
		log.i { "calculated coordinates: newX: $newX, newY: $newY, newSize: $newSize" }
		return Triple(newSize, newX, newY)
	}
}