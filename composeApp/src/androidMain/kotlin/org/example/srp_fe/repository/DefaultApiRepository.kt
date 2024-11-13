package org.example.srp_fe.repository

import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.Store
import org.openapitools.client.models.WallBlock
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultApiRepository(private val api: DefaultApi = DefaultApi()) {

	suspend fun getDepartmentsByMapId(mapId: Int): List<Department> = withContext(Dispatchers.IO) {
		val response = api.departmentsMapIdGet(mapId)
		response.body() ?: emptyList()
	}

	suspend fun deleteDepartmentById(departmentId: Int): String = withContext(Dispatchers.IO) {
		val response = api.departmentsDepartmentIdDelete(departmentId)
		response.body() ?: "Error"
	}

	suspend fun postDepartment(department: Department): String = withContext(Dispatchers.IO) {
		val response = api.departmentsPost(department)
		response.body() ?: "Error"
	}

	suspend fun getMapById(id: Int): Map? = withContext(Dispatchers.IO) {
		val response = api.mapsIdGet(id)
		response.body()
	}

	suspend fun deleteMapById(id: Int): String = withContext(Dispatchers.IO) {
		val response = api.mapsIdDelete(id)
		response.body() ?: "Error"
	}

	suspend fun postMap(map: Map): String = withContext(Dispatchers.IO) {
		val response = api.mapsPost(map)
		response.body() ?: "Error"
	}

	suspend fun getStoreById(id: Int): Store? = withContext(Dispatchers.IO) {
		val response = api.storeIdGet(id)
		response.body()
	}

	suspend fun deleteStoreById(id: Int): String = withContext(Dispatchers.IO) {
		val response = api.storeIdDelete(id)
		response.body() ?: "Error"
	}

	suspend fun postStore(store: Store): String = withContext(Dispatchers.IO) {
		val response = api.storePost(store)
		response.body() ?: "Error"
	}

	suspend fun getWallBlocksByMapId(mapId: Int): List<WallBlock> = withContext(Dispatchers.IO) {
		val response = api.wallBlocksMapIdGet(mapId)
		response.body() ?: emptyList()
	}

	suspend fun postWallBlock(wallBlock: WallBlock): String = withContext(Dispatchers.IO) {
		val response = api.wallBlocksPost(wallBlock)
		response.body() ?: "Error"
	}

	suspend fun deleteWallBlockById(wallBlockId: Int): String = withContext(Dispatchers.IO) {
		val response = api.wallBlocksWallBlockIdDelete(wallBlockId)
		response.body() ?: "Error"
	}
}