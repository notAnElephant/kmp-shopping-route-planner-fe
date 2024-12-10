package org.example

import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlan
import org.openapitools.client.models.RoutePlanning
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.WallBlock

interface ApiRepository {
	suspend fun calculateRoute(routePlanning: RoutePlanning): RoutePlan
	suspend fun deleteDepartment(departmentId: Int): String
	suspend fun updateDepartment(id: String, department: Department): Department
	suspend fun getDepartmentsByMap(mapId: Int): List<Department>
	suspend fun createDepartment(department: Department): Department
	suspend fun deleteMap(id: Int): String
	suspend fun getMap(id: Int): Map
	suspend fun updateMap(id: String, map: Map): Map
	suspend fun createMap(map: Map): Map
	suspend fun deleteStore(id: Int): String
	suspend fun getStore(id: Int): Store
	suspend fun createStore(store: Store): Store
	suspend fun updateTill(id: String, till: Till): Till
	suspend fun createTill(till: Till): Till
	suspend fun deleteTill(tillId: Int): String
	suspend fun getTills(tillId: Int): List<Till>
	suspend fun updateWallBlock(id: String, wallBlock: WallBlock): WallBlock
	suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock>
	suspend fun createWallBlock(wallBlock: WallBlock): WallBlock
	suspend fun deleteWallBlock(wallBlockId: Int): String
}
