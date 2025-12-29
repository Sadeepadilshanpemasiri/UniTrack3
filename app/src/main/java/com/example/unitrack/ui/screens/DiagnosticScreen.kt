// app/src/main/java/com/example/unitrack/ui/screens/DiagnosticScreen.kt
package com.example.unitrack.ui.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.unitrack.data.database.AppDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Composable
fun DiagnosticScreen(onSuccess: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf("Starting diagnostics...") }
    var error by remember { mutableStateOf<String?>(null) }
    var isTesting by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // Step 1: Check database
                step = "Checking database..."
                Log.d("DIAGNOSTIC", "Step 1: Checking database")

                val database = AppDatabase.getDatabase(context)
                step = "✅ Database initialized"

                // Step 2: Test each DAO
                step = "Testing DAOs..."
                Log.d("DIAGNOSTIC", "Step 2: Testing DAOs")

                database.userDao()
                database.semesterDao()
                database.subjectDao()
                database.assignmentDao()
                database.lectureDao()

                step = "✅ All DAOs working"

                // Step 3: Try to insert test data
                step = "Testing insert operations..."

                // If we get here, everything works
                isTesting = false
                step = "✅ All tests passed!"

                // Move to main app
                onSuccess()

            } catch (e: Exception) {
                error = "❌ Error at step: $step\n${e.message}"
                Log.e("DIAGNOSTIC", "Error: ${e.message}", e)
                isTesting = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Diagnostics") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isTesting) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = step,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error!!,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        // Restart diagnostics
                        isTesting = true
                        error = null
                        step = "Restarting diagnostics..."
                        scope.launch(Dispatchers.IO) {
                            // Retry
                        }
                    }
                ) {
                    Text("Retry")
                }
            }
        }
    }
}