// app/src/main/java/com/example/unitrack/ui/screens/DatabaseTestScreen.kt
package com.example.unitrack.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.models.Lecture
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers

@Composable
fun DatabaseTestScreen() {
    val context = LocalContext.current
    var message by remember { mutableStateOf("Testing database...") }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            try {
                // Test database creation
                val database = AppDatabase.getDatabase(context)
                message = "✅ Database created successfully!"

                // Test inserting a lecture
                val lecture = Lecture(
                    title = "Test Lecture",
                    userId = 1,
                    subjectId = 1,
                    dayOfWeek = 1,
                    startTime = "09:00",
                    endTime = "10:30",
                    room = "Test Room",
                    lecturer = "Test Lecturer"
                )

                val id = database.lectureDao().insert(lecture)
                message = "✅ Database working! Inserted lecture ID: $id"

            } catch (e: Exception) {
                message = "❌ Database error: ${e.message}"
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Database Test") }
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
            if (isLoading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}