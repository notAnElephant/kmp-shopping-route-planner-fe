import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import org.example.ApiRepository
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ShoppingListScreen(apiRepository: ApiRepository, navController: NavHostController) {
    val items = listOf(
        ShoppingItem("Milk", "Dairy"),
        ShoppingItem("Apples", "Fruits"),
        ShoppingItem("Chicken", "Meat"),
        ShoppingItem("Bread", "Bakery"),
    )

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(items) { item ->
            ShoppingListItem(item)
        }
    }
}

@Preview
@Composable
fun ShoppingListItem(item: ShoppingItem = ShoppingItem("Milk", "Dairy")) {
    var checked by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.name, fontSize = 18.sp)
            Text(text = item.category, fontSize = 14.sp)
        }
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

data class ShoppingItem(val name: String, val category: String)
