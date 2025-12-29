// app/src/main/java/com/example/unitrack/ui/screens/AddLectureScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unitrack.data.RepositoryFactory
import com.example.unitrack.data.models.Lecture
import com.example.unitrack.ui.viewmodels.LectureViewModel
import com.example.unitrack.ui.viewmodels.LectureViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLectureScreen(
    navController: NavController,
    dayOfWeek: Int? = null,
    subjectId: Int? = null
) {
    val context = LocalContext.current
    val repository = remember { RepositoryFactory.getRepository(context) }
    val viewModel: LectureViewModel = viewModel(
        factory = LectureViewModelFactory(repository)
    )
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var room by remember { mutableStateOf("") }
    var lecturer by remember { mutableStateOf("") }
    var notificationEnabled by remember { mutableStateOf(true) }
    var notificationMinutes by remember { mutableStateOf(15) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    var selectedDay by remember {
        mutableStateOf(dayOfWeek ?: 1)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Lecture") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateInput(title, startTime, endTime)) {
                                val lecture = Lecture(
                                    title = title,
                                    userId = 1,
                                    subjectId = subjectId ?: 0,
                                    dayOfWeek = selectedDay,
                                    startTime = startTime,
                                    endTime = endTime,
                                    room = room,
                                    lecturer = lecturer,
                                    notificationEnabled = notificationEnabled,
                                    notificationMinutesBefore = if (notificationEnabled) notificationMinutes else 0
                                )

                                scope.launch {
                                    val success = viewModel.addLecture(lecture)
                                    if (success) {
                                        navController.navigateUp()
                                    } else {
                                        errorMessage = "Failed to save lecture"
                                    }
                                }
                            } else {
                                errorMessage = "Please fill all required fields"
                            }
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Lecture Title *") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.School, contentDescription = "Title")
                },
                singleLine = true,
                isError = title.isBlank()
            )

            // Day selector
            Text("Day:", style = MaterialTheme.typography.labelMedium)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { day ->
                    FilterChip(
                        selected = daysOfWeek.indexOf(day) + 1 == selectedDay,
                        onClick = { selectedDay = daysOfWeek.indexOf(day) + 1 },
                        label = { Text(day) }
                    )
                }
            }

            // Time inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = startTime,
                    onValueChange = {
                        if (it.matches(Regex("^([0-9]?[0-9]):?([0-5][0-9])?$"))) {
                            startTime = it
                        }
                    },
                    label = { Text("Start Time *") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("HH:MM") },
                    singleLine = true,
                    isError = !isValidTime(startTime)
                )

                OutlinedTextField(
                    value = endTime,
                    onValueChange = {
                        if (it.matches(Regex("^([0-9]?[0-9]):?([0-5][0-9])?$"))) {
                            endTime = it
                        }
                    },
                    label = { Text("End Time *") },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("HH:MM") },
                    singleLine = true,
                    isError = !isValidTime(endTime)
                )
            }

            OutlinedTextField(
                value = room,
                onValueChange = { room = it },
                label = { Text("Room/Building") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Room")
                },
                singleLine = true
            )

            OutlinedTextField(
                value = lecturer,
                onValueChange = { lecturer = it },
                label = { Text("Lecturer/Instructor") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "Lecturer")
                },
                singleLine = true
            )

            // Notification settings
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = { notificationEnabled = it }
                        )
                    }

                    if (notificationEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Remind me before:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 30, 60).forEach { minutes ->
                                FilterChip(
                                    selected = notificationMinutes == minutes,
                                    onClick = { notificationMinutes = minutes },
                                    label = { Text("${minutes}m") }
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = {
                    if (validateInput(title, startTime, endTime)) {
                        val lecture = Lecture(
                            title = title,
                            userId = 1,
                            subjectId = subjectId ?: 0,
                            dayOfWeek = selectedDay,
                            startTime = startTime,
                            endTime = endTime,
                            room = room,
                            lecturer = lecturer,
                            notificationEnabled = notificationEnabled,
                            notificationMinutesBefore = if (notificationEnabled) notificationMinutes else 0
                        )

                        scope.launch {
                            val success = viewModel.addLecture(lecture)
                            if (success) {
                                navController.navigateUp()
                            } else {
                                errorMessage = "Failed to save lecture"
                            }
                        }
                    } else {
                        errorMessage = "Please fill all required fields correctly"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                enabled = title.isNotBlank() && isValidTime(startTime) && isValidTime(endTime)
            ) {
                Text("Save Lecture")
            }
        }
    }
}

private fun validateInput(title: String, startTime: String, endTime: String): Boolean {
    return title.isNotBlank() && isValidTime(startTime) && isValidTime(endTime)
}

private fun isValidTime(time: String): Boolean {
    return time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
}