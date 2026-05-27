package org.example.srpfe.repository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
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
import org.openapitools.client.models.CreateShoppingListRequest
import org.openapitools.client.models.Department
import org.openapitools.client.models.Map
import org.openapitools.client.models.RoutePlanResponse
import org.openapitools.client.models.RoutePlanningRequest
import org.openapitools.client.models.ShopList
import org.openapitools.client.models.ShoppingListResponse
import org.openapitools.client.models.Store
import org.openapitools.client.models.Till
import org.openapitools.client.models.UpdateShoppingListRequest
import org.openapitools.client.models.WallBlock

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
        private val shoppingLists = linkedMapOf<String, ShoppingListResponse>()
        private var nextShoppingListId = 1

        private suspend fun <T> withBearerAuth(block: suspend () -> T): T {
            authSession.requireBackendIdToken()
            return block()
        }

        override suspend fun getDepartmentsByMap(mapId: Int): List<Department> =
            withContext(Dispatchers.IO) {
                api.departmentsMapIdGet(mapId).body()
            }

        override suspend fun deleteDepartment(departmentId: Int): String =
            withContext(Dispatchers.IO) {
                api.departmentsDepartmentIdDelete(departmentId).body()
            }

        override suspend fun updateDepartment(
            id: String,
            department: Department,
        ): Department =
            withContext(Dispatchers.IO) {
                api.departmentsIdPut(id, department).body()
            }

        override suspend fun createDepartment(department: Department): Department =
            withContext(Dispatchers.IO) {
                api.departmentsPost(department).body()
            }

        override suspend fun getMap(id: Int): Map =
            withContext(Dispatchers.IO) {
                api.mapsIdGet(id).body()
            }

        override suspend fun updateMap(
            id: String,
            map: Map,
        ): Map =
            withContext(Dispatchers.IO) {
                api.mapsIdPut(id, map).body()
            }

        override suspend fun deleteMap(id: Int): String =
            withContext(Dispatchers.IO) {
                api.mapsIdDelete(id).body()
            }

        override suspend fun createMap(map: Map): Map =
            withContext(Dispatchers.IO) {
                api.mapsPost(map).body()
            }

        override suspend fun getStore(id: Int): Store =
            withContext(Dispatchers.IO) {
                api.storeIdGet(id).body()
            }

        override suspend fun deleteStore(id: Int): String =
            withContext(Dispatchers.IO) {
                api.storeIdDelete(id).body()
            }

        override suspend fun createStore(store: Store): Store =
            withContext(Dispatchers.IO) {
                api.storePost(store).body()
            }

        override suspend fun updateTill(
            id: String,
            till: Till,
        ): Till =
            withContext(Dispatchers.IO) {
                api.tillsIdPut(id, till).body()
            }

        override suspend fun createTill(till: Till): Till =
            withContext(Dispatchers.IO) {
                api.tillsPost(till).body()
            }

        override suspend fun deleteTill(tillId: Int): String =
            withContext(Dispatchers.IO) {
                api.tillsTillIdDelete(tillId).body()
            }

        override suspend fun getTills(tillId: Int): List<Till> =
            withContext(Dispatchers.IO) {
                api.tillsTillIdGet(tillId).body()
            }

        override suspend fun updateWallBlock(
            id: String,
            wallBlock: WallBlock,
        ): WallBlock =
            withContext(Dispatchers.IO) {
                api.wallBlocksIdPut(id, wallBlock).body()
            }

        override suspend fun getWallBlocksByMap(mapId: Int): List<WallBlock> =
            withContext(Dispatchers.IO) {
                api.wallBlocksMapIdGet(mapId).body()
            }

        override suspend fun createWallBlock(wallBlock: WallBlock): WallBlock =
            withContext(Dispatchers.IO) {
                api.wallBlocksPost(wallBlock).body()
            }

        override suspend fun deleteWallBlock(wallBlockId: Int): String =
            withContext(Dispatchers.IO) {
                api.wallBlocksWallBlockIdDelete(wallBlockId).body()
            }

        override suspend fun googleOcr(image: List<Base64ByteArray>): ShopList =
            withContext(Dispatchers.IO) {
                api.googleocrPost(image).body()
            }

        override suspend fun calculateRoute(routePlanning: RoutePlanningRequest): RoutePlanResponse =
            withContext(Dispatchers.IO) {
                api.calculateRoutePost(routePlanning).body()
            }

        override suspend fun getCurrentUser(): AppUserResponse =
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    val token = authSession.requireBackendIdToken()
                    profileClient.get("${backendBaseUrl()}/me") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                    }.body()
                }
            }

        override suspend fun getShoppingLists(): List<ShoppingListResponse> =
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    shoppingLists.values.toList()
                }
            }

        override suspend fun getShoppingList(id: String): ShoppingListResponse =
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    shoppingLists[id] ?: error("Shopping list $id was not found.")
                }
            }

        override suspend fun createShoppingList(request: CreateShoppingListRequest): ShoppingListResponse =
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    val shoppingList =
                        ShoppingListResponse(
                            id = nextShoppingListId.toString(),
                            name = request.name,
                            items = request.items,
                        )
                    nextShoppingListId += 1
                    shoppingLists[shoppingList.id] = shoppingList
                    shoppingList
                }
            }

        override suspend fun updateShoppingList(
            id: String,
            request: UpdateShoppingListRequest,
        ): ShoppingListResponse =
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    val existing = shoppingLists[id] ?: error("Shopping list $id was not found.")
                    ShoppingListResponse(
                        id = existing.id,
                        name = request.name,
                        items = request.items,
                    ).also { shoppingLists[id] = it }
                }
            }

        override suspend fun deleteShoppingList(id: String) {
            withContext(Dispatchers.IO) {
                withBearerAuth {
                    check(shoppingLists.remove(id) != null) { "Shopping list $id was not found." }
                }
            }
        }
    }
