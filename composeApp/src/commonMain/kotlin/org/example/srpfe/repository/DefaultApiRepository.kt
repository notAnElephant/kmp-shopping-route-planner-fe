package org.example.srpfe.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.example.ApiRepository
import org.example.srpfe.auth.AuthSession
import org.example.srpfe.utils.backendBaseUrl
import org.openapitools.client.apis.DefaultApi
import org.openapitools.client.infrastructure.Base64ByteArray
import org.openapitools.client.models.AppUserResponse
import org.openapitools.client.models.CreateDepartmentRequest
import org.openapitools.client.models.CreateMapRequest
import org.openapitools.client.models.CreateShoppingListItemRequest
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.CreateStoreRequest
import org.openapitools.client.models.CreateTillRequest
import org.openapitools.client.models.CreateWallBlockRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.DepartmentResponse
import org.openapitools.client.models.Map
import org.openapitools.client.models.MapResponse
import org.openapitools.client.models.PlaceDetailsResponse
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.SalesResponse
import org.openapitools.client.models.ShoppingList
import org.openapitools.client.models.Store
import org.openapitools.client.models.StoreDetailsResponse
import org.openapitools.client.models.StoreResponse
import org.openapitools.client.models.Till
import org.openapitools.client.models.TillResponse
import org.openapitools.client.models.UpdateDepartmentRequest
import org.openapitools.client.models.UpdateMapRequest
import org.openapitools.client.models.UpdateStoreRequest
import org.openapitools.client.models.UpdateTillRequest
import org.openapitools.client.models.UpdateWallBlockRequest
import org.openapitools.client.models.WallBlock
import org.openapitools.client.models.WallBlockResponse
import org.openapitools.client.infrastructure.HttpResponse as OpenApiHttpResponse

class DefaultApiRepository
    @OptIn(ExperimentalSerializationApi::class)
    constructor(
        private val authSession: AuthSession,
        private val profileClient: HttpClient =
            HttpClient {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            explicitNulls = true
                        },
                    )
                }
            },
        private val api: DefaultApi =
            DefaultApi(
                jsonSerializer =
                    Json {
                        encodeDefaults = true
                        explicitNulls = true
                    },
                baseUrl = backendBaseUrl(),
            ),
    ) : ApiRepository {
        private suspend fun requireBackendIdToken(): String = authSession.requireBackendIdToken()

        private suspend fun authedHeaderValue(): String = "Bearer ${requireBackendIdToken()}"

        override suspend fun getDepartmentsByMap(mapId: Int): List<Department> =
            withContext(Dispatchers.IO) {
                api
                    .departmentsMapIdGet(mapId)
                    .requireSuccessBody(this@DefaultApiRepository::getDepartmentsByMap.name)
                    .map(DepartmentResponse::toDepartment)
            }

        override suspend fun deleteDepartment(departmentId: Int): String =
            withContext(Dispatchers.IO) {
                api
                    .departmentsIdDelete(departmentId)
                    .requireSuccessBody(this@DefaultApiRepository::deleteDepartment.name)
            }

        override suspend fun updateDepartment(
            id: String,
            department: Department,
        ): Department =
            withContext(Dispatchers.IO) {
                api
                    .departmentsPut(
                        department.toUpdateDepartmentRequest(id),
                    ).requireSuccessBody<DepartmentResponse>(this@DefaultApiRepository::updateDepartment.name)
                    .toDepartment()
            }

        override suspend fun createDepartment(department: Department): Department =
            withContext(Dispatchers.IO) {
                api
                    .departmentsPost(
                        department.toCreateDepartmentRequest(),
                    ).requireSuccessBody<DepartmentResponse>(this@DefaultApiRepository::createDepartment.name)
                    .toDepartment()
            }

        override suspend fun getMap(id: Int): Map =
            withContext(Dispatchers.IO) {
                api
                    .mapsIdGet(id)
                    .requireSuccessBody<MapResponse>(this@DefaultApiRepository::getMap.name)
                    .toMap()
            }

        override suspend fun updateMap(
            id: String,
            map: Map,
        ): Map =
            withContext(Dispatchers.IO) {
                api
                    .mapsPut(map.toUpdateMapRequest(id))
                    .requireSuccessBody<MapResponse>(this@DefaultApiRepository::updateMap.name)
                    .toMap()
            }

        override suspend fun deleteMap(id: Int): String =
            withContext(Dispatchers.IO) {
                api.mapsIdDelete(id).requireSuccessBody(this@DefaultApiRepository::deleteMap.name)
            }

        override suspend fun createMap(map: Map): Map =
            withContext(Dispatchers.IO) {
                api
                    .mapsPost(map.toCreateMapRequest())
                    .requireSuccessBody<MapResponse>(this@DefaultApiRepository::createMap.name)
                    .toMap()
            }

        override suspend fun getStore(id: Int): Store =
            withContext(Dispatchers.IO) {
                api
                    .storeIdGet(id)
                    .requireSuccessBody<StoreResponse>(this@DefaultApiRepository::getStore.name)
                    .toStore()
            }

        override suspend fun getStores(): List<Store> =
            withContext(Dispatchers.IO) {
                api
                    .storeGet()
                    .requireSuccessBody<List<StoreResponse>>(this@DefaultApiRepository::getStores.name)
                    .map(StoreResponse::toStore)
            }

        override suspend fun deleteStore(id: Int): String =
            withContext(Dispatchers.IO) {
                api.storeIdDelete(id).requireSuccessBody(this@DefaultApiRepository::deleteStore.name)
            }

        override suspend fun createStore(store: Store): Store =
            withContext(Dispatchers.IO) {
                api
                    .storePost(store.toCreateStoreRequest())
                    .requireSuccessBody<StoreResponse>(this@DefaultApiRepository::createStore.name)
                    .toStore()
            }

        override suspend fun updateStore(
            id: Int,
            store: Store,
        ): Store =
            withContext(Dispatchers.IO) {
                api
                    .storePut(store.toUpdateStoreRequest(id))
                    .requireSuccessBody<StoreResponse>(this@DefaultApiRepository::updateStore.name)
                    .toStore()
            }

        override suspend fun getStorePlaceDetails(id: Int): PlaceDetailsResponse =
            withContext(Dispatchers.IO) {
                api
                    .storeIdPlaceDetailsGet(id)
                    .requireSuccessBody(this@DefaultApiRepository::getStorePlaceDetails.name)
            }

        override suspend fun getStoreComponentDetails(id: Int): StoreDetailsResponse =
            withContext(Dispatchers.IO) {
                api
                    .storeIdComponentDetailsGet(id)
                    .requireSuccessBody(this@DefaultApiRepository::getStoreComponentDetails.name)
            }

        override suspend fun getSales(store: String): SalesResponse =
            withContext(Dispatchers.IO) {
                val response =
                    profileClient.get(backendBaseUrl()) {
                        url {
                            appendPathSegments("sales", store)
                        }
                    }

                response.requireSuccessBody(this@DefaultApiRepository::getSales.name)
            }

        override suspend fun updateTill(
            id: String,
            till: Till,
        ): Till =
            withContext(Dispatchers.IO) {
                api
                    .tillsPut(till.toUpdateTillRequest(id))
                    .requireSuccessBody<TillResponse>(this@DefaultApiRepository::updateTill.name)
                    .toTill()
            }

        override suspend fun createTill(till: Till): Till =
            withContext(Dispatchers.IO) {
                api
                    .tillsPost(till.toCreateTillRequest())
                    .requireSuccessBody<TillResponse>(this@DefaultApiRepository::createTill.name)
                    .toTill()
            }

        override suspend fun deleteTill(tillId: Int): String =
            withContext(Dispatchers.IO) {
                api.tillsIdDelete(tillId).requireSuccessBody(this@DefaultApiRepository::deleteTill.name)
            }

        override suspend fun getTills(tillId: Int): List<Till> =
            withContext(Dispatchers.IO) {
                api
                    .tillsMapIdGet(tillId)
                    .requireSuccessBody<List<TillResponse>>(this@DefaultApiRepository::getTills.name)
                    .map(TillResponse::toTill)
            }

        override suspend fun updateWallBlock(
            id: String,
            wallBlock: WallBlock,
        ): WallBlock =
            withContext(Dispatchers.IO) {
                api
                    .wallBlocksPut(
                        wallBlock.toUpdateWallBlockRequest(id),
                    ).requireSuccessBody<WallBlockResponse>(this@DefaultApiRepository::updateWallBlock.name)
                    .toWallBlock()
            }

        override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> =
            withContext(Dispatchers.IO) {
                api
                    .wallBlocksMapIdGet(
                        mapId,
                    ).requireSuccessBody<List<WallBlockResponse>>(this@DefaultApiRepository::getWallBlocksByMap.name)
                    .map(WallBlockResponse::toWallBlock)
            }

        override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock =
            withContext(Dispatchers.IO) {
                api
                    .wallBlocksPost(
                        wallBlock.toCreateWallBlockRequest(),
                    ).requireSuccessBody<WallBlockResponse>(this@DefaultApiRepository::createWallBlock.name)
                    .toWallBlock()
            }

        override suspend fun deleteWallBlock(wallBlockId: Int): String =
            withContext(Dispatchers.IO) {
                api
                    .wallBlocksIdDelete(wallBlockId)
                    .requireSuccessBody(this@DefaultApiRepository::deleteWallBlock.name)
            }

        override suspend fun googleOcr(image: List<Base64ByteArray>): List<CreateShoppingListItemRequest> =
            withContext(Dispatchers.IO) {
                api
                    .ocrShoppingListPost(image)
                    .requireSuccessBody(this@DefaultApiRepository::googleOcr.name)
            }

        override suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse =
            withContext(Dispatchers.IO) {
                api
                    .calculateRoutePost(routePlanning)
                    .requireSuccessBody(this@DefaultApiRepository::calculateRoute.name)
            }

        override suspend fun getCurrentUser(): AppUserResponse =
            withContext(Dispatchers.IO) {
                val response =
                    profileClient
                        .get("${backendBaseUrl()}/me") {
                            header(HttpHeaders.Authorization, authedHeaderValue())
                        }

                response.requireSuccessBody(this@DefaultApiRepository::getCurrentUser.name)
            }

        override suspend fun getShoppingLists(): List<ShoppingList> =
            withContext(Dispatchers.IO) {
                profileClient
                    .get("${backendBaseUrl()}/shopping-lists") {
                        header(HttpHeaders.Authorization, authedHeaderValue())
                    }.requireSuccessBody(this@DefaultApiRepository::getShoppingLists.name)
            }

        override suspend fun getShoppingList(id: Int): ShoppingList =
            withContext(Dispatchers.IO) {
                profileClient
                    .get("${backendBaseUrl()}/shopping-lists/$id") {
                        header(HttpHeaders.Authorization, authedHeaderValue())
                    }.requireSuccessBody(this@DefaultApiRepository::getShoppingList.name)
            }

        override suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingList =
            withContext(Dispatchers.IO) {
                profileClient
                    .post("${backendBaseUrl()}/shopping-lists") {
                        header(HttpHeaders.Authorization, authedHeaderValue())
                        setBody(request)
                    }.requireSuccessBody(this@DefaultApiRepository::createShoppingList.name)
            }

        override suspend fun updateShoppingList(
            id: Int,
            request: CreateShoppingListRequest,
        ): ShoppingList =
            withContext(Dispatchers.IO) {
                deleteShoppingList(id)
                createShoppingList(request)
            }

        override suspend fun deleteShoppingList(id: Int) {
            withContext(Dispatchers.IO) {
                profileClient
                    .delete("${backendBaseUrl()}/shopping-lists/$id") {
                        header(HttpHeaders.Authorization, authedHeaderValue())
                    }.requireSuccess(this@DefaultApiRepository::deleteShoppingList.name)
            }
        }
    }

private suspend fun HttpResponse.requireSuccess(endpoint: String): HttpResponse {
    if (status.isSuccess()) {
        return this
    }

    val backendMessage = bodyAsText().ifBlank { "No response body" }
    error("$endpoint failed with ${status.value} ${status.description}: $backendMessage")
}

private suspend inline fun <reified T> HttpResponse.requireSuccessBody(endpoint: String): T = requireSuccess(endpoint).body()

private suspend fun <T : Any> OpenApiHttpResponse<T>.requireSuccess(endpoint: String): OpenApiHttpResponse<T> {
    if (success) {
        return this
    }

    val backendMessage = response.bodyAsText().ifBlank { "No response body" }
    error("$endpoint failed with ${response.status.value} ${response.status.description}: $backendMessage")
}

private suspend inline fun <reified T : Any> OpenApiHttpResponse<T>.requireSuccessBody(endpoint: String): T =
    requireSuccess(endpoint).body()

private fun DepartmentResponse.toDepartment() =
    Department(
        name = name,
        mapId = mapId,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
        id = id,
    )

private fun Department.toCreateDepartmentRequest() =
    CreateDepartmentRequest(
        mapId = mapId,
        name = name,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )

private fun Department.toUpdateDepartmentRequest(id: String) =
    UpdateDepartmentRequest(
        id = id.toInt(),
        mapId = mapId,
        name = name,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )

private fun MapResponse.toMap() =
    Map(
        width = width,
        height = height,
        entranceX = entranceX,
        entranceY = entranceY,
        exitX = exitX,
        exitY = exitY,
        storeId = storeId,
        id = id,
    )

private fun Map.toCreateMapRequest() =
    CreateMapRequest(
        width = width,
        height = height,
        entranceX = entranceX,
        entranceY = entranceY,
        exitX = exitX,
        exitY = exitY,
        storeId = storeId,
    )

private fun Map.toUpdateMapRequest(id: String) =
    UpdateMapRequest(
        id = id.toInt(),
        width = width,
        height = height,
        entranceX = entranceX,
        entranceY = entranceY,
        exitX = exitX,
        exitY = exitY,
        storeId = storeId,
    )

private fun StoreResponse.toStore() =
    Store(
        name = name,
        id = id,
        location = location ?: "",
    )

private fun Store.toCreateStoreRequest() =
    CreateStoreRequest(
        name = name,
        location = location,
    )

private fun Store.toUpdateStoreRequest(id: Int) =
    UpdateStoreRequest(
        id = id,
        name = name,
        location = location,
    )

private fun TillResponse.toTill() =
    Till(
        mapId = mapId,
        width = width,
        height = height,
        startX = startX,
        startY = startY,
        id = id,
    )

private fun Till.toCreateTillRequest() =
    CreateTillRequest(
        mapId = mapId,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )

private fun Till.toUpdateTillRequest(id: String) =
    UpdateTillRequest(
        id = id.toInt(),
        mapId = mapId,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )

private fun WallBlockResponse.toWallBlock() =
    WallBlock(
        mapId = mapId,
        width = width,
        height = height,
        startX = startX,
        startY = startY,
        id = id,
    )

private fun WallBlock.toCreateWallBlockRequest() =
    CreateWallBlockRequest(
        mapId = mapId,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )

private fun WallBlock.toUpdateWallBlockRequest(id: String) =
    UpdateWallBlockRequest(
        id = id.toInt(),
        mapId = mapId,
        startX = startX,
        startY = startY,
        width = width,
        height = height,
    )
