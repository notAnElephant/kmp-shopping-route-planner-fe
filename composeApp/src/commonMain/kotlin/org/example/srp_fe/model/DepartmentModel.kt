package org.example.srp_fe.model

import androidx.compose.ui.graphics.Color

data class DepartmentModel (

    val color: Color,
    val isSelected: Boolean,

    val mapId: Int,
    val name: String,
    val width: Double,

    val height: Double,

    val startX: Double,

    val startY: Double,

    val id: Int? = null

)