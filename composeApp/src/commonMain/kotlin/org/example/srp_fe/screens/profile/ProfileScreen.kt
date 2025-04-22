package org.example.srp_fe.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import com.mmk.kmpauth.firebase.google.GoogleButtonUiContainerFirebase
import dev.gitlive.firebase.auth.FirebaseUser
import org.example.ApiRepository

@Composable
fun ProfileScreen(apiRepository: ApiRepository, navController: NavHostController) {
	var email by remember { mutableStateOf("") }
	var password by remember { mutableStateOf("") }
	var errorMessage by remember { mutableStateOf<String?>(null) }

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

		//TODO sign-in won't work on desktop per se, but can be solved, see: https://github.com/mirzemehdi/KMPAuth/issues/15#issuecomment-2424153435

		val onFirebaseResult = { result: Result<FirebaseUser?> ->
			if (result.isSuccess) {
				val firebaseUser = result.getOrNull()
				errorMessage = null
				println("Firebase User: ${firebaseUser?.displayName}")
			} else {
				errorMessage = "Sign-in failed: ${result.exceptionOrNull()?.message}"
				println("Error Result: ${result.exceptionOrNull()?.message}")
			}
		}
		GoogleButtonUiContainerFirebase(onResult = onFirebaseResult, linkAccount = false) {
			GoogleSignInButton(modifier = Modifier.fillMaxWidth().height(44.dp), fontSize = 19.sp) { this.onClick() }
		}

		errorMessage?.let {
			Spacer(modifier = Modifier.height(8.dp))
			Text(text = it, color = MaterialTheme.colors.error)
		}
	}
}