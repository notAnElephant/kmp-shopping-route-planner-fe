package org.example.srpfe.screens.shopmapdrawer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.max

internal const val MAP_ANCHOR_WIDTH = 20f
internal const val MAP_ANCHOR_HEIGHT = 10f
internal const val HANDLE_RADIUS = 6f
internal const val HANDLE_HIT_RADIUS = 16f
internal const val MIN_ITEM_SIZE_BACKEND = 4f

internal enum class EditableItemKind {
    DEPARTMENT,
    WALL,
    TILL,
    ENTRANCE,
    EXIT,
}

internal data class EditableItemRef(
    val kind: EditableItemKind,
    val id: Int? = null,
)

internal enum class ResizeHandle {
    TOP,
    RIGHT,
    BOTTOM,
    LEFT,
}

internal enum class EditorInteraction {
    NONE,
    MOVING,
    RESIZING_TOP,
    RESIZING_RIGHT,
    RESIZING_BOTTOM,
    RESIZING_LEFT,
}

internal data class CanvasRect(
    val topLeft: Offset,
    val size: Size,
) {
    val left: Float = topLeft.x
    val top: Float = topLeft.y
    val right: Float = topLeft.x + size.width
    val bottom: Float = topLeft.y + size.height
    val center: Offset = Offset(left + size.width / 2f, top + size.height / 2f)

    fun contains(point: Offset): Boolean = point.x in left..right && point.y in top..bottom
}

internal data class EditableCanvasItem(
    val ref: EditableItemRef,
    val rect: CanvasRect,
    val color: Color,
    val label: String = "",
    val canResize: Boolean,
    val canDelete: Boolean,
)

internal fun handleForInteraction(interaction: EditorInteraction): ResizeHandle? =
    when (interaction) {
        EditorInteraction.RESIZING_TOP -> ResizeHandle.TOP
        EditorInteraction.RESIZING_RIGHT -> ResizeHandle.RIGHT
        EditorInteraction.RESIZING_BOTTOM -> ResizeHandle.BOTTOM
        EditorInteraction.RESIZING_LEFT -> ResizeHandle.LEFT
        else -> null
    }

internal fun interactionForHandle(handle: ResizeHandle): EditorInteraction =
    when (handle) {
        ResizeHandle.TOP -> EditorInteraction.RESIZING_TOP
        ResizeHandle.RIGHT -> EditorInteraction.RESIZING_RIGHT
        ResizeHandle.BOTTOM -> EditorInteraction.RESIZING_BOTTOM
        ResizeHandle.LEFT -> EditorInteraction.RESIZING_LEFT
    }

internal fun handleCenter(
    rect: CanvasRect,
    handle: ResizeHandle,
): Offset =
    when (handle) {
        ResizeHandle.TOP -> Offset(rect.center.x, rect.top)
        ResizeHandle.RIGHT -> Offset(rect.right, rect.center.y)
        ResizeHandle.BOTTOM -> Offset(rect.center.x, rect.bottom)
        ResizeHandle.LEFT -> Offset(rect.left, rect.center.y)
    }

internal fun hitTestHandle(
    rect: CanvasRect,
    point: Offset,
): ResizeHandle? =
    ResizeHandle.entries.firstOrNull { handle ->
        val center = handleCenter(rect, handle)
        val dx = center.x - point.x
        val dy = center.y - point.y
        dx * dx + dy * dy <= HANDLE_HIT_RADIUS * HANDLE_HIT_RADIUS
    }

internal fun minCanvasSize(
    canvasSize: Size,
    mapWidth: Double,
    mapHeight: Double,
): Size =
    Size(
        width = max(1f, canvasSize.width * (MIN_ITEM_SIZE_BACKEND / mapWidth.toFloat())),
        height = max(1f, canvasSize.height * (MIN_ITEM_SIZE_BACKEND / mapHeight.toFloat())),
    )

internal fun moveRect(
    rect: CanvasRect,
    delta: Offset,
    bounds: Size,
): CanvasRect {
    val newLeft = (rect.left + delta.x).coerceIn(0f, bounds.width - rect.size.width)
    val newTop = (rect.top + delta.y).coerceIn(0f, bounds.height - rect.size.height)
    return CanvasRect(Offset(newLeft, newTop), rect.size)
}

internal fun resizeRect(
    rect: CanvasRect,
    handle: ResizeHandle,
    delta: Offset,
    bounds: Size,
    minSize: Size,
): CanvasRect {
    var left = rect.left
    var top = rect.top
    var right = rect.right
    var bottom = rect.bottom

    when (handle) {
        ResizeHandle.TOP -> {
            top = (top + delta.y).coerceIn(0f, bottom - minSize.height)
        }
        ResizeHandle.RIGHT -> {
            right = (right + delta.x).coerceIn(left + minSize.width, bounds.width)
        }
        ResizeHandle.BOTTOM -> {
            bottom = (bottom + delta.y).coerceIn(top + minSize.height, bounds.height)
        }
        ResizeHandle.LEFT -> {
            left = (left + delta.x).coerceIn(0f, right - minSize.width)
        }
    }

    return CanvasRect(
        topLeft = Offset(left, top),
        size = Size(right - left, bottom - top),
    )
}
