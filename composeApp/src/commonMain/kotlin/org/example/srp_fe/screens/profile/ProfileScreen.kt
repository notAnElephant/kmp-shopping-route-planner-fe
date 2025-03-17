package org.example.srp_fe.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
//import com.google.firebase.auth.FirebaseAuth
import org.example.ApiRepository

@Composable
fun ProfileScreen(apiRepository: ApiRepository, navController: NavHostController) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf<String?>(null) }

//	val auth = FirebaseAuth.getInstance()

	Column(
		modifier = Modifier.fillMaxSize().padding(16.dp),
		verticalArrangement = Arrangement.Center
	) {
		TextField(
			value = email,
			onValueChange = { email = it },
			label = { Text("Email") },
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(modifier = Modifier.height(8.dp))
		TextField(
			value = password,
			onValueChange = { password = it },
			label = { Text("Password") },
			modifier = Modifier.fillMaxWidth(),
			visualTransformation = PasswordVisualTransformation()
		)
		Spacer(modifier = Modifier.height(16.dp))
		Button(
			onClick = {
//				auth.signInWithEmailAndPassword(email, password)
//					.addOnCompleteListener { task ->
//						if (task.isSuccessful) {
//							// Navigate to the profile screen or show a success message
//						} else {
//							errorMessage = task.exception?.message
//						}
//					}
			},
			modifier = Modifier.fillMaxWidth()
		) {
			Text("Login")
		}
		errorMessage?.let {
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = it, color = MaterialTheme.colors.error)
		}
	}
}