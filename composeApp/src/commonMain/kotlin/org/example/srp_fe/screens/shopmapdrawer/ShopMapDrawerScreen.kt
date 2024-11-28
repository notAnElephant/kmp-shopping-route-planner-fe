package org.example.srp_fe.screens.shopmapdrawer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.composables.core.rememberMenuState
import org.example.ApiRepository
import kotlin.random.Random

data class Rectangle(val topLeft: Offset, val size: Size, val color: Color)

data class Department(val name: String, val color: Color)

enum class FunctionType(val label: String) {
	DEPARTMENT("Department"), WALL("Wall"), DELETE("Delete"), MOVE("Move")
}


@Composable
fun ShopMapDrawerScreen(apiRepository: ApiRepository) {
	Column(
		modifier = Modifier.fillMaxSize().padding(16.dp)
	) {
		var selectedFunction by remember { mutableStateOf(FunctionType.WALL) }
		var selectedDepartment by remember { mutableStateOf<Department?>(null) }

		FunctionSelector(
			selectedFunctionType = selectedFunction,
			onFunctionSelected = { selectedFunction = it })
		DepartmentControls(
			selectedDepartment = selectedDepartment,
			onDepartmentSelected = { selectedDepartment = it })
		ShopMapCanvas(
			selectedFunctionType = selectedFunction,
			selectedDepartment = selectedDepartment
		)

		var text by remember { mutableStateOf("No data") }
		Button(onClick = {
			LaunchedEffect(Unit) {
				val departments = apiRepository.getDepartmentsByMapId(1)
				text = departments.joinToString { it.name }
			}
		}) {
			Text("Backend call")
		}
		Text("backend's answer: ${text}")
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
		verticalAlignment = Alignment.CenterVertically
	) {
		FunctionType.entries.forEach { functionType ->
			FunctionButton(functionType = functionType,
				isSelected = selectedFunctionType == functionType,
				onClick = { onFunctionSelected(functionType) })
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
				imageVector = when (functionType) {
					FunctionType.DEPARTMENT -> Icons.Default.Business
					FunctionType.WALL -> Icons.Default.Wallpaper
					FunctionType.DELETE -> Icons.Default.Delete
					FunctionType.MOVE -> Icons.Default.ZoomOutMap
				}, contentDescription = functionType.label, modifier = Modifier.size(24.dp)
			)
			Text(
				text = functionType.label,
				style = MaterialTheme.typography.caption,
				color = if (isSelected) Color.Green else Color.Black
			)
		}
	}
}

@Composable
fun DepartmentControls(
	selectedDepartment: Department?,
	onDepartmentSelected: (Department) -> Unit,
) {
	var departmentName by remember { mutableStateOf(TextFieldValue("")) }
	val departments = remember { mutableStateListOf<Department>() }

	// Function to add a new department
	fun addDepartment(name: String) {
		val newColor = Color(
			red = Random.nextInt(256) / 255f,
			green = Random.nextInt(256) / 255f,
			blue = Random.nextInt(256) / 255f
		)
		departments.add(Department(name, newColor))
	}

	//  input field and add button
	Column(modifier = Modifier.fillMaxWidth()) {
		rememberMenuState(expanded = true)

		Column(Modifier.fillMaxWidth()) {
			Row(
				modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				TextField(
					value = departmentName,
					onValueChange = { departmentName = it },
					label = { Text("Name") },
					singleLine = true,
					modifier = Modifier.weight(1f)
				)
				IconButton(onClick = {
					if (departmentName.text.isNotEmpty()) {
						addDepartment(departmentName.text)
						departmentName = TextFieldValue("")  // Reset input
						onDepartmentSelected(departments.last())

					}
				}) {
					Icon(Icons.Default.Add, contentDescription = "Add Department")
				}
			}
			DepartmentDropdown(
				departments = departments,
				selectedDepartment = selectedDepartment,
				onDepartmentSelected = onDepartmentSelected
			)
		}
	}
}

//TODO app mas platformokon valo futtatasahoz
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html#next-step
@Composable
fun DepartmentDropdown(
	departments: List<Department>,
	selectedDepartment: Department?,
	onDepartmentSelected: (Department) -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }

	// Toggle the menu state
	val arrowIcon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown

	// TODO disable this somehow when not the Department is the selected function
	// Use a custom Menu button with color and selected department label
	Box(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
				.border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp)).background(Color.White)
				.padding(horizontal = 14.dp, vertical = 10.dp).clickable { expanded = !expanded },
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			if (selectedDepartment != null) {
				Box(
					modifier = Modifier.size(20.dp)
						.background(selectedDepartment.color, RoundedCornerShape(4.dp))
				)
			}
			Text(
				text = selectedDepartment?.name ?: "Departments",
				style = MaterialTheme.typography.body1,
				overflow = TextOverflow.Ellipsis,
				maxLines = 1
			)
			// Arrow icon (up or down based on dropdown state)
			Icon(
				imageVector = arrowIcon,
				contentDescription = "Dropdown Arrow",
				modifier = Modifier.size(24.dp)
			)
		}

		// Show the dropdown content
		DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
			if (departments.isNotEmpty()) {
				departments.forEach { department ->
					DropdownMenuItem(onClick = {
						onDepartmentSelected(department)
						expanded = false
					}) {
						Row(
							modifier = Modifier.fillMaxWidth()
								.padding(vertical = 8.dp, horizontal = 4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							// Department color box
							Box(
								modifier = Modifier.size(20.dp)
									.background(department.color, RoundedCornerShape(4.dp))
							)
							Spacer(modifier = Modifier.width(8.dp))
							Text(
								text = department.name, style = MaterialTheme.typography.body2
							)
						}
					}
				}
			} else {
				DropdownMenuItem(onClick = { }) {
					Text("No departments available")
				}
			}
		}
	}
}

@Composable
fun ShopMapCanvas(
	selectedFunctionType: FunctionType,
	selectedDepartment: Department? = null,
) {
	val scale by remember { mutableStateOf(1f) }
	val rectangles = remember { mutableStateListOf<Rectangle>() }

	var startPoint: Offset? by remember { mutableStateOf(null) }
	var currentPoint: Offset? by remember { mutableStateOf(null) }

	// Canvas for drawing
	Canvas(
		modifier = Modifier.fillMaxSize().background(Color.LightGray)
			.pointerInput(selectedDepartment, selectedFunctionType) {
				detectTapGestures(onPress = { offset ->
					if (startPoint == null) {
						// First click: Set top-left corner
						startPoint = offset
					} else {
						// Second click: Set bottom-right corner
						currentPoint = offset
						if (startPoint != null && currentPoint != null) {
							val size1 = Size(
								width = currentPoint!!.x - startPoint!!.x,
								height = currentPoint!!.y - startPoint!!.y
							)
							val departmentColor = when (selectedFunctionType) {
								FunctionType.WALL -> Color.Black
								FunctionType.DEPARTMENT -> selectedDepartment?.color ?: Color.White
								else -> Color.Transparent //TODO just shouldn't let the user draw
							}
							rectangles.add(Rectangle(startPoint!!, size1, departmentColor))
							startPoint = null
							currentPoint = null
						}
					}
				})
			}) {
		scale(scale) {
			rectangles.forEach { rect ->
				drawRect(
					color = rect.color,  // Use the department color
					topLeft = rect.topLeft, size = rect.size
				)
			}
		}
	}
}
