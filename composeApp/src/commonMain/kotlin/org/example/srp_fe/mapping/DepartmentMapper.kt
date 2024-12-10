package org.example.srp_fe.mapping

import androidx.compose.ui.graphics.Color
import org.example.srp_fe.model.DepartmentModel
import org.openapitools.client.models.Department

fun Department.toModel(color: Color = Color.Gray, isSelected: Boolean = false): DepartmentModel {
	return DepartmentModel(
		color = color,
		isSelected = isSelected,
		mapId = this.mapId,
		name = this.name,
		width = this.width,
		height = this.height,
		startX = this.startX,
		startY = this.startY,
		id = this.id
	)
}

fun DepartmentModel.toDepartment(): Department {
	return Department(
		mapId = this.mapId,
		name = this.name,
		width = this.width,
		height = this.height,
		startX = this.startX,
		startY = this.startY,
		id = this.id
	)
}
