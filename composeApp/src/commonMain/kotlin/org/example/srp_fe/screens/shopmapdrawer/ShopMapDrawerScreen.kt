@file:Suppress("t")

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
import androidx.compose.material.Snackbar
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.composables.core.rememberMenuState
import org.example.ApiRepository
import org.example.srp_fe.model.DepartmentType
import org.example.srp_fe.screens.shopmapdrawer.ShopMapDrawerViewModel.Companion.log
import kotlin.random.Random

data class Rectangle(val topLeft: Offset, val size: Size, val color: Color, val name: String = "")

enum class FunctionType(val label: String) {
	DEPARTMENT("Department"), WALL("Wall"), DELETE("Delete"), MOVE("Move")
}

@Composable
fun ShopMapDrawerScreen(apiRepository: ApiRepository, navController: NavHostController) {
	val viewModel by remember { mutableStateOf(ShopMapDrawerViewModel(apiRepository)) }
	Column(
		modifier = Modifier.fillMaxSize().padding(16.dp)
	) {
		val uiState by viewModel.uiState.collectAsState()

		var selectedFunction by remember { mutableStateOf(FunctionType.WALL) }
		var selectedDepartmentType by remember { mutableStateOf<DepartmentType?>(null) }
		Box(modifier = Modifier.fillMaxSize()) {
			Column(
				modifier = Modifier.fillMaxSize(),
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Column(
					modifier = Modifier.weight(1f) // Canvas and controls take remaining space
				) {
					FunctionSelector(
						selectedFunctionType = selectedFunction,
						onFunctionSelected = { selectedFunction = it }
					)
					DepartmentControls(
						selectedDepartmentType = selectedDepartmentType,
						onDepartmentTypeSelected = { selectedDepartmentType = it }
					)
					ShopMapCanvas(
						selectedFunctionType = selectedFunction,
						viewModel = viewModel,
						uiState = uiState,
						selectedDepartmentType = selectedDepartmentType
					)
				}
				Button(
					onClick = { viewModel.calculateRoute(uiState.concreteDepartments) },
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				) {
					Text("Calculate Route")
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
fun ErrorSnackbar(errorMessage: String?, onDismiss: () -> Unit) {
	if (errorMessage != null) {
		Snackbar(
			action = {
				Button(onClick = onDismiss) {
					Text("Elvetés")
				}
			}
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
	selectedDepartmentType: DepartmentType?,
	onDepartmentTypeSelected: (DepartmentType) -> Unit
) {
	var departmentName by remember { mutableStateOf(TextFieldValue("")) }
	val departmentTypes = remember { mutableStateListOf<DepartmentType>() }

	// Function to add a new departmenttype
	fun addDepartmentType(name: String) {
		val newColor = Color(
			red = Random.nextInt(256) / 255f,
			green = Random.nextInt(256) / 255f,
			blue = Random.nextInt(256) / 255f
		)
		departmentTypes.add(DepartmentType(name, newColor))
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
						addDepartmentType(departmentName.text)
						departmentName = TextFieldValue("")  // Reset input
						onDepartmentTypeSelected(departmentTypes.last())

					}
				}) {
					Icon(Icons.Default.Add, contentDescription = "Add Department")
				}
			}
			DepartmentTypeDropdown(
				departmentTypes = departmentTypes,
				selectedDepartmentType = selectedDepartmentType,
				onDepartmentTypeSelected = onDepartmentTypeSelected
			)
		}
	}
}

//TODO app mas platformokon valo futtatasahoz
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html#next-step
@Composable
fun DepartmentTypeDropdown(
	departmentTypes: List<DepartmentType>,
	selectedDepartmentType: DepartmentType?,
	onDepartmentTypeSelected: (DepartmentType) -> Unit,
) {
	var expanded by remember { mutableStateOf(false) }

	// Toggle the menu state
	val arrowIcon = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown

	// TODO disable this somehow when not the org.example.srp_fe.model.Department is the selected function
	// Use a custom Menu button with color and selected department label
	Box(modifier = Modifier.fillMaxWidth()) {
		Row(
			modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
				.border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(6.dp)).background(Color.White)
				.padding(horizontal = 14.dp, vertical = 10.dp).clickable { expanded = !expanded },
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			if (selectedDepartmentType != null) {
				Box(
					modifier = Modifier.size(20.dp)
						.background(selectedDepartmentType.color, RoundedCornerShape(4.dp))
				)
			}
			Text(
				text = selectedDepartmentType?.name ?: "Departments",
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

			//TODO checkbox to select/deselect departmenttype
		}

		// Show the dropdown content
		DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
			if (departmentTypes.isNotEmpty()) {
				departmentTypes.forEach { department ->
					DropdownMenuItem(onClick = {
						onDepartmentTypeSelected(department)
						expanded = false
					}) {
						Row(
							modifier = Modifier.fillMaxWidth()
								.padding(vertical = 8.dp, horizontal = 4.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							// org.example.srp_fe.model.Department color box
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
	viewModel: ShopMapDrawerViewModel,
	uiState: UiState,
	selectedDepartmentType: DepartmentType? = null,
) {
	val scale by remember { mutableStateOf(1f) }
	val rectangles = remember { mutableStateListOf<Rectangle>() }

	var startPoint: Offset? by remember { mutableStateOf(null) }
	var currentPoint: Offset? by remember { mutableStateOf(null) }


	val textMeasurer = rememberTextMeasurer()

	// Canvas for drawing
	Canvas(
		modifier = Modifier.fillMaxSize().background(Color.LightGray).border(1.dp, Color.Black)
			.pointerInput(selectedDepartmentType, selectedFunctionType) {
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
							val canvasSize = Size(size.width.toFloat(), size.height.toFloat())

							when(selectedFunctionType){
								FunctionType.WALL -> {
									val color = Color.Black
									rectangles.add(Rectangle(startPoint!!, size1, color, ""))
									log.i{"Drawing wall: $startPoint, $currentPoint"}


									//newSize, newX, newY)
									val backendCoordinates: Triple<Size, Int, Int> =
										viewModel.convertToBackendCoordinates(canvasSize, size1,
											startPoint!!.x,
											startPoint!!.y + size1.height)

									//TODO a wallblockokról nem tároljuk el a színt külön, de valszeg minek is
									viewModel.createWallBlock(
										width = backendCoordinates.first.width.toInt(),
										height = backendCoordinates.first.height.toInt(),
										startX = backendCoordinates.second,
										startY = backendCoordinates.third,
									)
								}
								FunctionType.DEPARTMENT -> {
									val color = selectedDepartmentType?.color ?: Color.White
									rectangles.add(Rectangle(startPoint!!, size1, color, selectedDepartmentType?.name ?: ""))
									log.i{"Drawing department: $startPoint, $currentPoint"}

									//newSize, newX, newY)
									val backendCoordinates: Triple<Size, Int, Int> =
										viewModel.convertToBackendCoordinates(canvasSize, size1,
											startPoint!!.x,
											startPoint!!.y + size1.height)

									viewModel.createDepartment(
										name = selectedDepartmentType?.name ?: "",
										color = color,
										width = backendCoordinates.first.width.toInt(),
										height = backendCoordinates.first.height.toInt(),
										startX = backendCoordinates.second,
										startY = backendCoordinates.third,
									)
								}
								else -> return@detectTapGestures
							}

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
				// Check if the rectangle is not too small (adjust thresholds as needed)
				if (rect.size.width > 20 && rect.size.height > 20 && rect.name.isNotEmpty()) {
					var topleft = rect.topLeft + Offset(5.0F, rect.size.height / 2)
					var color = when(rect.color){ //TODO not always white color
//						Color.Black -> Color.White
						else -> Color.White
					}
					drawText(
						topLeft = topleft,
						textMeasurer = textMeasurer,
						text = rect.name,
					)
				}
			}

			val coords = viewModel.convertToCanvasCoordinates(
				canvasSize = Size(size.width, size.height),
				size = Size(20f, 10f),
				x = uiState.map?.exitX?.toInt()?: 0,
				y = uiState.map?.exitY?.toInt() ?: 0
			)
			drawRect(
				color = Color.Gray,
				topLeft = Offset(coords.second, coords.third),
				size = Size(coords.first.width, coords.first.height)
			)

			val coords2 = viewModel.convertToCanvasCoordinates(
				canvasSize = Size(size.width, size.height),
				size = Size(20f, 10f),
				x = uiState.map?.entranceX?.toInt()?: 0,
				y = (uiState.map?.entranceY?.toInt() ?: 0)
			)
			drawRect(
				color = Color.Blue,
				topLeft = Offset(coords2.second, coords2.third - 75),
				size = Size(coords2.first.width, coords2.first.height)
			)

			uiState.route?.route?.forEachIndexed { index, route ->
				val coords = viewModel.convertToCanvasCoordinates(
					canvasSize = Size(size.width, size.height),
					size = Size(20f, 10f),
					x = route.first,
					y = route.second
				)

				// Calculate color transition from light green to dark green
				val totalPoints = uiState.route.route.size
				val colorFraction = index.toFloat() / totalPoints
				val greenShade = interpolateColor(Color(0xFFB2FF59), Color(0xFF1B5E20), colorFraction)

				drawCircle(
					color = greenShade,
					center = Offset(coords.second, coords.third),
					radius = 5f
				)
			}



		}
	}
}
fun interpolateColor(start: Color, end: Color, fraction: Float): Color {
	val r = start.red + (end.red - start.red) * fraction
	val g = start.green + (end.green - start.green) * fraction
	val b = start.blue + (end.blue - start.blue) * fraction
	val a = start.alpha + (end.alpha - start.alpha) * fraction
	return Color(r, g, b, a)
}

