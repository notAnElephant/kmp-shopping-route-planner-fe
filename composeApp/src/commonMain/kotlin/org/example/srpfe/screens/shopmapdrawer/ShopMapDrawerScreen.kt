@file:Suppress("t")

package org.example.srpfe.screens.shopmapdrawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.core.rememberMenuState
import compose.icons.FeatherIcons
import compose.icons.feathericons.Briefcase
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import compose.icons.feathericons.Delete
import compose.icons.feathericons.Layout
import compose.icons.feathericons.Move
import compose.icons.feathericons.Plus
import org.example.srpfe.model.DepartmentType
import org.example.srpfe.screens.shopmapdrawer.ShopMapDrawerViewModel.Companion.log
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.random.Random

private val wallColor = Color.Black
private val tillColor = Color(0xFF6D4C41)
private val entranceColor = Color(0xFF1E88E5)
private val exitColor = Color(0xFF757575)
private val selectionColor = Color(0xFFFFC107)

enum class FunctionType(
    val label: String,
) {
    DEPARTMENT("Department"),
    WALL("Wall"),
    DELETE("Delete"),
    MOVE("Move"),
}

@Composable
fun ShopMapDrawerScreen(
    storeId: Int,
    onBack: () -> Unit,
) {
    val viewModel =
        koinViewModel<ShopMapDrawerViewModel>(
            parameters = { parametersOf(storeId) },
        )
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(16.dp),
    ) {
        val uiState by viewModel.uiState.collectAsState()

        var selectedFunction by remember { mutableStateOf(FunctionType.WALL) }
        var selectedDepartmentType by remember { mutableStateOf<DepartmentType?>(null) }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Back")
                }

                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    uiState.map == null -> {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text(
                                    text = "No map exists for ${uiState.store?.name ?: "this store"} yet.",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                                Button(
                                    onClick = viewModel::createMapForStore,
                                    enabled = !uiState.isCreatingMap,
                                ) {
                                    Text(if (uiState.isCreatingMap) "Creating map..." else "Create map")
                                }
                            }
                        }
                    }

                    else -> {
                        Column(
                            modifier = Modifier.weight(1f),
                        ) {
                            FunctionSelector(
                                selectedFunctionType = selectedFunction,
                                onFunctionSelected = { selectedFunction = it },
                            )
                            DepartmentControls(
                                selectedDepartmentType = selectedDepartmentType,
                                onDepartmentTypeSelected = { selectedDepartmentType = it },
                            )
                            ShopMapCanvas(
                                selectedFunctionType = selectedFunction,
                                viewModel = viewModel,
                                uiState = uiState,
                                selectedDepartmentType = selectedDepartmentType,
                            )
                        }
                        Button(
                            onClick = { viewModel.calculateRoute(uiState.concreteDepartments) },
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                        ) {
                            Text("Calculate Route")
                        }
                    }
                }
            }
            ErrorSnackbar(
                errorMessage = uiState.errorState,
            ) {
                viewModel.dismissError()
            }
        }
    }
}

@Composable
fun ErrorSnackbar(
    errorMessage: String?,
    onDismiss: () -> Unit,
) {
    if (errorMessage != null) {
        Snackbar(
            action = {
                Button(onClick = onDismiss) {
                    Text("Elvetés")
                }
            },
        ) {
            Text(text = errorMessage)
        }
    }
}

@Composable
fun FunctionSelector(
    selectedFunctionType: FunctionType,
    onFunctionSelected: (FunctionType) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FunctionType.entries.forEach { functionType ->
            FunctionButton(
                functionType = functionType,
                isSelected = selectedFunctionType == functionType,
                onClick = { onFunctionSelected(functionType) },
            )
        }
    }
}

@Composable
fun FunctionButton(
    functionType: FunctionType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector =
                    when (functionType) {
                        FunctionType.DEPARTMENT -> FeatherIcons.Briefcase
                        FunctionType.WALL -> FeatherIcons.Layout
                        FunctionType.DELETE -> FeatherIcons.Delete
                        FunctionType.MOVE -> FeatherIcons.Move
                    },
                contentDescription = functionType.label,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = functionType.label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.Green else Color.Black,
            )
        }
    }
}

@Composable
fun DepartmentControls(
    selectedDepartmentType: DepartmentType?,
    onDepartmentTypeSelected: (DepartmentType) -> Unit,
) {
    var departmentName by remember { mutableStateOf(TextFieldValue("")) }
    val departmentTypes = remember { mutableStateListOf<DepartmentType>() }

    fun addDepartmentType(name: String) {
        val newColor =
            Color(
                red = Random.nextInt(256) / 255f,
                green = Random.nextInt(256) / 255f,
                blue = Random.nextInt(256) / 255f,
            )
        departmentTypes.add(DepartmentType(name, newColor))
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        rememberMenuState(expanded = true)

        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextField(
                    value = departmentName,
                    onValueChange = { departmentName = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = {
                    if (departmentName.text.isNotEmpty()) {
                        addDepartmentType(departmentName.text)
                        departmentName = TextFieldValue("")
                        onDepartmentTypeSelected(departmentTypes.last())
                    }
                }) {
                    Icon(FeatherIcons.Plus, contentDescription = "Add Department")
                }
            }
            DepartmentTypeDropdown(
                departmentTypes = departmentTypes,
                selectedDepartmentType = selectedDepartmentType,
                onDepartmentTypeSelected = onDepartmentTypeSelected,
            )
        }
    }
}

@Composable
fun DepartmentTypeDropdown(
    departmentTypes: List<DepartmentType>,
    selectedDepartmentType: DepartmentType?,
    onDepartmentTypeSelected: (DepartmentType) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val arrowIcon = if (expanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .clickable { expanded = !expanded },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (selectedDepartmentType != null) {
                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .background(selectedDepartmentType.color, RoundedCornerShape(4.dp)),
                )
            }
            Text(
                text = selectedDepartmentType?.name ?: "Departments",
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
            Icon(
                imageVector = arrowIcon,
                contentDescription = "Dropdown Arrow",
                modifier = Modifier.size(24.dp),
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (departmentTypes.isNotEmpty()) {
                departmentTypes.forEach { department ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier =
                                        Modifier
                                            .size(20.dp)
                                            .background(department.color, RoundedCornerShape(4.dp)),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = department.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        onClick = {
                            onDepartmentTypeSelected(department)
                            expanded = false
                        },
                    )
                }
            } else {
                DropdownMenuItem(text = { Text("No departments available") }, onClick = { })
            }
        }
    }
}

@Composable
fun ShopMapCanvas(
    selectedFunctionType: FunctionType,
    viewModel: ShopMapDrawerViewModel,
    uiState: UiState,
    selectedDepartmentType: DepartmentType? = null,
) {
    val scale by remember { mutableStateOf(1f) }
    var createStartPoint by remember { mutableStateOf<Offset?>(null) }
    var selectedItemRef by remember { mutableStateOf<EditableItemRef?>(null) }
    var draftRect by remember { mutableStateOf<CanvasRect?>(null) }
    var interaction by remember { mutableStateOf(EditorInteraction.NONE) }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .border(1.dp, Color.Black)
                .pointerInput(
                    selectedFunctionType,
                    selectedDepartmentType,
                    uiState.concreteDepartments,
                    uiState.wallBlocks,
                    uiState.tills,
                    uiState.map,
                ) {
                    when (selectedFunctionType) {
                        FunctionType.DEPARTMENT,
                        FunctionType.WALL,
                        FunctionType.DELETE,
                        -> detectTapGestures(
                            onTap = { offset ->
                                val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                                val items = buildEditableItems(viewModel, uiState, canvasSize)
                                when (selectedFunctionType) {
                                    FunctionType.DEPARTMENT,
                                    FunctionType.WALL,
                                    -> {
                                        if (createStartPoint == null) {
                                            createStartPoint = offset
                                            selectedItemRef = null
                                            draftRect = null
                                        } else {
                                            val start = createStartPoint ?: return@detectTapGestures
                                            val rect = createCanvasRect(start, offset)
                                            val backendCoordinates =
                                                viewModel.convertToBackendCoordinates(
                                                    canvasSize = canvasSize,
                                                    size = rect.size,
                                                    x = rect.left,
                                                    y = rect.bottom,
                                                )

                                            when (selectedFunctionType) {
                                                FunctionType.WALL -> {
                                                    log.i { "Drawing wall: $start, $offset" }
                                                    viewModel.createWallBlock(
                                                        width = backendCoordinates.first.width.toInt(),
                                                        height = backendCoordinates.first.height.toInt(),
                                                        startX = backendCoordinates.second,
                                                        startY = backendCoordinates.third,
                                                    )
                                                }

                                                FunctionType.DEPARTMENT -> {
                                                    val departmentType = selectedDepartmentType ?: return@detectTapGestures
                                                    log.i { "Drawing department: $start, $offset" }
                                                    viewModel.createDepartment(
                                                        name = departmentType.name,
                                                        color = departmentType.color,
                                                        width = backendCoordinates.first.width.toInt(),
                                                        height = backendCoordinates.first.height.toInt(),
                                                        startX = backendCoordinates.second,
                                                        startY = backendCoordinates.third,
                                                    )
                                                }

                                                else -> Unit
                                            }

                                            createStartPoint = null
                                        }
                                    }

                                    FunctionType.DELETE -> {
                                        val hitItem = hitTestItem(items, offset)
                                        if (hitItem == null) {
                                            selectedItemRef = null
                                            draftRect = null
                                            return@detectTapGestures
                                        }

                                        val isSameSelection = selectedItemRef == hitItem.ref
                                        selectedItemRef = hitItem.ref
                                        draftRect = null
                                        interaction = EditorInteraction.NONE

                                        if (isSameSelection && hitItem.canDelete) {
                                            performDelete(hitItem.ref, viewModel)
                                            selectedItemRef = null
                                        }
                                    }

                                    else -> Unit
                                }
                            },
                        )

                        FunctionType.MOVE -> awaitEachGesture {
                            val canvasSize = Size(size.width.toFloat(), size.height.toFloat())
                            val bounds = canvasSize
                            val items = buildEditableItems(viewModel, uiState, canvasSize)
                            val down = awaitAnyDown()
                            val selectedItem = items.firstOrNull { it.ref == selectedItemRef }
                            val selectedRect = selectedItem?.rect
                            val hitHandle =
                                if (selectedItem?.canResize == true && selectedRect != null) {
                                    hitTestHandle(selectedRect, down.position)
                                } else {
                                    null
                                }

                            var activeItem: EditableCanvasItem? = null
                            var activeRect: CanvasRect? = null

                            if (hitHandle != null && selectedItem != null) {
                                activeItem = selectedItem
                                activeRect = selectedRect
                                draftRect = selectedRect
                                interaction = interactionForHandle(hitHandle)
                            } else {
                                val hitItem = hitTestItem(items, down.position)
                                if (hitItem == null) {
                                    selectedItemRef = null
                                    draftRect = null
                                    interaction = EditorInteraction.NONE
                                    return@awaitEachGesture
                                }

                                selectedItemRef = hitItem.ref
                                activeItem = hitItem
                                activeRect = hitItem.rect
                                draftRect = hitItem.rect
                                interaction = EditorInteraction.MOVING
                            }

                            var moved = false
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = trackedChange(event.changes, down.id) ?: break
                                if (!change.pressed) {
                                    break
                                }

                                val delta = change.positionChange()
                                if (delta != Offset.Zero && activeRect != null) {
                                    moved = true
                                    activeRect =
                                        when (interaction) {
                                            EditorInteraction.MOVING ->
                                                moveRect(
                                                    rect = activeRect,
                                                    delta = delta,
                                                    bounds = bounds,
                                                )

                                            else ->
                                                resizeRect(
                                                    rect = activeRect,
                                                    handle = handleForInteraction(interaction) ?: ResizeHandle.TOP,
                                                    delta = delta,
                                                    bounds = bounds,
                                                    minSize =
                                                        minCanvasSize(
                                                            canvasSize = canvasSize,
                                                            mapWidth = viewModel.width,
                                                            mapHeight = viewModel.height,
                                                        ),
                                                )
                                        }
                                    draftRect = activeRect
                                }
                                change.consume()
                            }

                            if (moved && activeItem != null && activeRect != null) {
                                commitRectChange(
                                    ref = activeItem.ref,
                                    rect = activeRect,
                                    canvasSize = canvasSize,
                                    uiState = uiState,
                                    viewModel = viewModel,
                                )
                            }

                            draftRect = null
                            interaction = EditorInteraction.NONE
                        }
                    }
                },
    ) {
        scale(scale) {
            val canvasSize = Size(size.width, size.height)
            val items = buildEditableItems(viewModel, uiState, canvasSize)

            items.forEach { item ->
                val rect =
                    if (item.ref == selectedItemRef && draftRect != null) {
                        draftRect ?: item.rect
                    } else {
                        item.rect
                    }
                drawRect(
                    color = item.color,
                    topLeft = rect.topLeft,
                    size = rect.size,
                )

                if (item.label.isNotEmpty() && rect.size.width > 20f && rect.size.height > 20f) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = item.label,
                        topLeft = rect.topLeft + Offset(5f, rect.size.height / 2f),
                    )
                }

                if (item.ref == selectedItemRef) {
                    drawRect(
                        color = selectionColor,
                        topLeft = rect.topLeft,
                        size = rect.size,
                        style = Stroke(width = 3f),
                    )

                    if (item.canResize) {
                        ResizeHandle.entries.forEach { handle ->
                            drawCircle(
                                color = selectionColor,
                                radius = HANDLE_RADIUS,
                                center = handleCenter(rect, handle),
                            )
                        }
                    }
                }
            }

            uiState.route?.route?.forEachIndexed { index, _ ->
                val coords =
                    viewModel.convertToCanvasCoordinates(
                        canvasSize = canvasSize,
                        size = Size(20f, 10f),
                        x = 0,
                        y = 0,
                    )

                val totalPoints = uiState.route?.route?.size ?: 1
                val colorFraction = index.toFloat() / totalPoints
                val greenShade = interpolateColor(Color(0xFFB2FF59), Color(0xFF1B5E20), colorFraction)

                drawCircle(
                    color = greenShade,
                    center = Offset(coords.second, coords.third),
                    radius = 5f,
                )
            }
        }
    }
}

private fun trackedChange(
    changes: List<PointerInputChange>,
    pointerId: androidx.compose.ui.input.pointer.PointerId,
): PointerInputChange? = changes.firstOrNull { it.id == pointerId } ?: changes.firstOrNull()

private suspend fun AwaitPointerEventScope.awaitAnyDown(): PointerInputChange {
    while (true) {
        val event = awaitPointerEvent()
        event.changes.firstOrNull { it.pressed }?.let { return it }
    }
}

private fun performDelete(
    ref: EditableItemRef,
    viewModel: ShopMapDrawerViewModel,
) {
    when (ref.kind) {
        EditableItemKind.DEPARTMENT -> ref.id?.let(viewModel::deleteDepartment)
        EditableItemKind.WALL -> ref.id?.let(viewModel::deleteWallBlock)
        EditableItemKind.TILL -> ref.id?.let(viewModel::deleteTill)
        EditableItemKind.ENTRANCE,
        EditableItemKind.EXIT,
        -> Unit
    }
}

private fun commitRectChange(
    ref: EditableItemRef,
    rect: CanvasRect,
    canvasSize: Size,
    uiState: UiState,
    viewModel: ShopMapDrawerViewModel,
) {
    val backendCoordinates =
        viewModel.convertToBackendCoordinates(
            canvasSize = canvasSize,
            size = rect.size,
            x = rect.left,
            y = rect.bottom,
        )

    when (ref.kind) {
        EditableItemKind.DEPARTMENT -> {
            val department = uiState.concreteDepartments.firstOrNull { it.id == ref.id } ?: return
            ref.id?.let { departmentId ->
                viewModel.updateDepartmentRect(
                    departmentId = departmentId,
                    name = department.name,
                    width = backendCoordinates.first.width.toInt(),
                    height = backendCoordinates.first.height.toInt(),
                    startX = backendCoordinates.second,
                    startY = backendCoordinates.third,
                )
            }
        }

        EditableItemKind.WALL ->
            ref.id?.let { wallId ->
                viewModel.updateWallBlockRect(
                    wallBlockId = wallId,
                    width = backendCoordinates.first.width.toInt(),
                    height = backendCoordinates.first.height.toInt(),
                    startX = backendCoordinates.second,
                    startY = backendCoordinates.third,
                )
            }

        EditableItemKind.TILL ->
            ref.id?.let { tillId ->
                viewModel.updateTillRect(
                    tillId = tillId,
                    width = backendCoordinates.first.width.toInt(),
                    height = backendCoordinates.first.height.toInt(),
                    startX = backendCoordinates.second,
                    startY = backendCoordinates.third,
                )
            }

        EditableItemKind.ENTRANCE ->
            viewModel.updateEntrancePosition(
                startX = backendCoordinates.second,
                startY = backendCoordinates.third,
            )

        EditableItemKind.EXIT ->
            viewModel.updateExitPosition(
                startX = backendCoordinates.second,
                startY = backendCoordinates.third,
            )
    }
}

private fun buildEditableItems(
    viewModel: ShopMapDrawerViewModel,
    uiState: UiState,
    canvasSize: Size,
): List<EditableCanvasItem> {
    val walls =
        uiState.wallBlocks.mapNotNull { wallBlock ->
            val wallId = wallBlock.id ?: return@mapNotNull null
            EditableCanvasItem(
                ref = EditableItemRef(EditableItemKind.WALL, wallId),
                rect =
                    backendRectToCanvasRect(
                        viewModel = viewModel,
                        canvasSize = canvasSize,
                        width = wallBlock.width.toFloat(),
                        height = wallBlock.height.toFloat(),
                        startX = wallBlock.startX.toInt(),
                        startY = wallBlock.startY.toInt(),
                    ),
                color = wallColor,
                canResize = true,
                canDelete = true,
            )
        }

    val departments =
        uiState.concreteDepartments.mapNotNull { department ->
            val departmentId = department.id ?: return@mapNotNull null
            EditableCanvasItem(
                ref = EditableItemRef(EditableItemKind.DEPARTMENT, departmentId),
                rect =
                    backendRectToCanvasRect(
                        viewModel = viewModel,
                        canvasSize = canvasSize,
                        width = department.width.toFloat(),
                        height = department.height.toFloat(),
                        startX = department.startX.toInt(),
                        startY = department.startY.toInt(),
                    ),
                color = department.color,
                label = department.name,
                canResize = true,
                canDelete = true,
            )
        }

    val tills =
        uiState.tills.mapNotNull { till ->
            val tillId = till.id ?: return@mapNotNull null
            EditableCanvasItem(
                ref = EditableItemRef(EditableItemKind.TILL, tillId),
                rect =
                    backendRectToCanvasRect(
                        viewModel = viewModel,
                        canvasSize = canvasSize,
                        width = till.width.toFloat(),
                        height = till.height.toFloat(),
                        startX = till.startX.toInt(),
                        startY = till.startY.toInt(),
                    ),
                color = tillColor,
                label = "Till",
                canResize = true,
                canDelete = true,
            )
        }

    val exit =
        uiState.map?.let { map ->
            EditableCanvasItem(
                ref = EditableItemRef(EditableItemKind.EXIT),
                rect =
                    backendRectToCanvasRect(
                        viewModel = viewModel,
                        canvasSize = canvasSize,
                        width = MAP_ANCHOR_WIDTH,
                        height = MAP_ANCHOR_HEIGHT,
                        startX = map.exitX.toInt(),
                        startY = map.exitY.toInt(),
                    ),
                color = exitColor,
                label = "Exit",
                canResize = false,
                canDelete = false,
            )
        }

    val entrance =
        uiState.map?.let { map ->
            EditableCanvasItem(
                ref = EditableItemRef(EditableItemKind.ENTRANCE),
                rect =
                    backendRectToCanvasRect(
                        viewModel = viewModel,
                        canvasSize = canvasSize,
                        width = MAP_ANCHOR_WIDTH,
                        height = MAP_ANCHOR_HEIGHT,
                        startX = map.entranceX.toInt(),
                        startY = map.entranceY.toInt(),
                    ),
                color = entranceColor,
                label = "Entrance",
                canResize = false,
                canDelete = false,
            )
        }

    return buildList {
        addAll(walls)
        addAll(departments)
        addAll(tills)
        exit?.let(::add)
        entrance?.let(::add)
    }
}

private fun hitTestItem(
    items: List<EditableCanvasItem>,
    point: Offset,
): EditableCanvasItem? = items.asReversed().firstOrNull { item -> item.rect.contains(point) }

private fun createCanvasRect(
    start: Offset,
    end: Offset,
): CanvasRect {
    val left = minOf(start.x, end.x)
    val top = minOf(start.y, end.y)
    val right = maxOf(start.x, end.x)
    val bottom = maxOf(start.y, end.y)
    return CanvasRect(
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
    )
}

private fun backendRectToCanvasRect(
    viewModel: ShopMapDrawerViewModel,
    canvasSize: Size,
    width: Float,
    height: Float,
    startX: Int,
    startY: Int,
): CanvasRect {
    val canvasCoords =
        viewModel.convertToCanvasCoordinates(
            canvasSize = canvasSize,
            size = Size(width, height),
            x = startX,
            y = startY,
        )
    return CanvasRect(
        topLeft = Offset(canvasCoords.second, canvasCoords.third - canvasCoords.first.height),
        size = canvasCoords.first,
    )
}

fun interpolateColor(
    start: Color,
    end: Color,
    fraction: Float,
): Color {
    val r = start.red + (end.red - start.red) * fraction
    val g = start.green + (end.green - start.green) * fraction
    val b = start.blue + (end.blue - start.blue) * fraction
    val a = start.alpha + (end.alpha - start.alpha) * fraction
    return Color(r, g, b, a)
}
