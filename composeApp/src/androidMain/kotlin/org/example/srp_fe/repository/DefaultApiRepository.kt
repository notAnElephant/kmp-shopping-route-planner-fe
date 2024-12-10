package org.example.srp_fe.repository

import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.Store
import org.openapitools.client.models.WallBlock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.example.ApiRepository
import org.openapitools.client.models.RoutePlan
import org.openapitools.client.models.RoutePlanning
import org.openapitools.client.models.Till

class DefaultApiRepository @OptIn(ExperimentalSerializationApi::class) constructor(private val api: DefaultApi = DefaultApi(
	jsonSerializer = Json {
		encodeDefaults = true
		explicitNulls = true
	},
	baseUrl = "http://10.0.2.2:8080"
)) : ApiRepository {

	override suspend fun getDepartmentsByMap(mapId: Int): List<Department> = withContext(Dispatchers.IO) {
		val response = api.departmentsMapIdGet(mapId)
		response.body()
	}

	override suspend fun deleteDepartment(departmentId: Int): String = withContext(Dispatchers.IO) {
		val response = api.departmentsDepartmentIdDelete(departmentId)
		response.body()
	}

	override suspend fun updateDepartment(id: String, department: Department): Department = withContext(Dispatchers.IO) {
		val response = api.departmentsIdPut(id, department)
		response.body()
	}

	override suspend fun createDepartment(department: Department): Department = withContext(Dispatchers.IO) {
		val response = api.departmentsPost(department)
		response.body()
	}

	override suspend fun getMap(id: Int): Map = withContext(Dispatchers.IO) {
		val response = api.mapsIdGet(id)
		response.body()
	}

	override suspend fun updateMap(id: String, map: Map): Map = withContext(Dispatchers.IO) {
		val response = api.mapsIdPut(id, map)
		response.body()
	}

	override suspend fun deleteMap(id: Int): String = withContext(Dispatchers.IO) {
		val response = api.mapsIdDelete(id)
		response.body()
	}

	override suspend fun createMap(map: Map): Map = withContext(Dispatchers.IO) {
		val response = api.mapsPost(map)
		response.body()
	}

	override suspend fun getStore(id: Int): Store = withContext(Dispatchers.IO) {
		val response = api.storeIdGet(id)
		response.body()
	}

	override suspend fun deleteStore(id: Int): String = withContext(Dispatchers.IO) {
		val response = api.storeIdDelete(id)
		response.body()
	}

	override suspend fun createStore(store: Store): Store = withContext(Dispatchers.IO) {
		val response = api.storePost(store)
		response.body()
	}

	override suspend fun updateTill(id: String, till: Till): Till = withContext(Dispatchers.IO) {
		val response = api.tillsIdPut(id, till)
		response.body()
	}

	override suspend fun createTill(till: Till): Till = withContext(Dispatchers.IO) {
		val response = api.tillsPost(till)
		response.body()
	}

	override suspend fun deleteTill(tillId: Int): String = withContext(Dispatchers.IO) {
		val response = api.tillsTillIdDelete(tillId)
		response.body()
	}

	override suspend fun getTills(tillId: Int): List<Till> = withContext(Dispatchers.IO) {
		val response = api.tillsTillIdGet(tillId)
		response.body()
	}

	override suspend fun updateWallBlock(id: String, wallBlock: WallBlock): WallBlock = withContext(Dispatchers.IO) {
		val response = api.wallBlocksIdPut(id, wallBlock)
		response.body()
	}

	override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> = withContext(Dispatchers.IO) {
		val response = api.wallBlocksMapIdGet(mapId)
		response.body()
	}

	override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock = withContext(Dispatchers.IO) {
		val response = api.wallBlocksPost(wallBlock)
		response.body()
	}

	override suspend fun deleteWallBlock(wallBlockId: Int): String = withContext(Dispatchers.IO) {
		val response = api.wallBlocksWallBlockIdDelete(wallBlockId)
		response.body()
	}

	override suspend fun calculateRoute(routePlanning: RoutePlanning): RoutePlan = withContext(Dispatchers.IO) {
		val response = api.calculateRoutePost(routePlanning)
		response.body()
	}
}