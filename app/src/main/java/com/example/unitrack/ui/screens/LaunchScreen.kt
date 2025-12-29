// app/src/main/java/com/example/unitrack/ui/screens/LaunchScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.unitrack.data.RepositoryFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LaunchScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }

    // Initialize repository in background
    LaunchedEffect(Unit) {
        scope.launch {
            // Initialize repository (database)
            RepositoryFactory.getRepository(context)

            // Small delay to show splash
            delay(1000)

            isLoading = false

            // Navigate to user selection
            navController.navigate("users") {
                popUpTo("launch") { inclusive = true }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "UniTrack",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Initializing...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}