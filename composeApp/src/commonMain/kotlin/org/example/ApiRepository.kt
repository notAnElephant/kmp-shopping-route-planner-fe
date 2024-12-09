package org.example

import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlanning
import org.openapitools.client.models.Store
import org.openapitools.client.models.WallBlock

interface ApiRepository {
	suspend fun getDepartmentsByMapId(mapId: Int): List<Department>

	suspend fun deleteDepartmentById(departmentId: Int): String

	suspend fun postDepartment(department: Department): String

	suspend fun getMapById(id: Int): Map?

	suspend fun deleteMapById(id: Int): String
	suspend fun postMap(map: Map): String

	suspend fun getStoreById(id: Int): Store?
	suspend fun deleteStoreById(id: Int): String
	suspend fun postStore(store: Store): String

	suspend fun getWallBlocksByMapId(mapId: Int): List<WallBlock>
	suspend fun postWallBlock(wallBlock: WallBlock): String

	suspend fun deleteWallBlockById(wallBlockId: Int): String

	suspend fun calculateRoute(routePlanning: RoutePlanning): List<String>  //TODO list<pair<int, int>> (theoretically)
}