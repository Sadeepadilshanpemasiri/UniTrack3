// app/src/main/java/com/example/unitrack/ui/screens/AddLectureScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var isLoading by remember { mutableStateOf(false) }

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
                                isLoading = true
                                val lecture = Lecture(
                                    title = title,
                                    userId = 1,
                                    subjectId = subjectId ?: 0,
                                    dayOfWeek = selectedDay,
                                    startTime = formatTime(startTime),
                                    endTime = formatTime(endTime),
                                    room = room,
                                    lecturer = lecturer,
                                    notificationEnabled = notificationEnabled,
                                    notificationMinutesBefore = if (notificationEnabled) notificationMinutes else 0
                                )

                                scope.launch {
                                    try {
                                        val success = viewModel.addLecture(lecture)
                                        if (success) {
                                            navController.navigateUp()
                                        } else {
                                            errorMessage = "Failed to save lecture"
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error: ${e.message}"
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            } else {
                                errorMessage = "Please fill all required fields"
                            }
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            if (errorMessage != null) {
                item {
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
            }

            item {
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
            }

            // Day selector
            item {
                Text("Day:", style = MaterialTheme.typography.labelMedium)
            }

            item {
                DaySelector(
                    daysOfWeek = daysOfWeek,
                    selectedDay = selectedDay,
                    onDaySelected = { selectedDay = it }
                )
            }

            // Time inputs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {
                            if (isValidTimeInput(it)) {
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
                            if (isValidTimeInput(it)) {
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
            }

            item {
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
            }

            item {
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
            }

            // Notification settings
            item {
                NotificationSettings(
                    notificationEnabled = notificationEnabled,
                    notificationMinutes = notificationMinutes,
                    onNotificationEnabledChanged = { notificationEnabled = it },
                    onNotificationMinutesChanged = { notificationMinutes = it }
                )
            }

            // Save button
            item {
                Button(
                    onClick = {
                        if (validateInput(title, startTime, endTime)) {
                            isLoading = true
                            val lecture = Lecture(
                                title = title,
                                userId = 1,
                                subjectId = subjectId ?: 0,
                                dayOfWeek = selectedDay,
                                startTime = formatTime(startTime),
                                endTime = formatTime(endTime),
                                room = room,
                                lecturer = lecturer,
                                notificationEnabled = notificationEnabled,
                                notificationMinutesBefore = if (notificationEnabled) notificationMinutes else 0
                            )

                            scope.launch {
                                try {
                                    val success = viewModel.addLecture(lecture)
                                    if (success) {
                                        navController.navigateUp()
                                    } else {
                                        errorMessage = "Failed to save lecture"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Error: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Please fill all required fields correctly"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = title.isNotBlank() && isValidTime(startTime) && isValidTime(endTime) && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saving...")
                    } else {
                        Text("Save Lecture")
                    }
                }
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DaySelector(
    daysOfWeek: List<String>,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(daysOfWeek) { day ->
            val dayIndex = daysOfWeek.indexOf(day) + 1
            FilterChip(
                selected = dayIndex == selectedDay,
                onClick = { onDaySelected(dayIndex) },
                label = { Text(day) }
            )
        }
    }
}

@Composable
fun NotificationSettings(
    notificationEnabled: Boolean,
    notificationMinutes: Int,
    onNotificationEnabledChanged: (Boolean) -> Unit,
    onNotificationMinutesChanged: (Int) -> Unit
) {
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
                    onCheckedChange = onNotificationEnabledChanged
                )
            }

            if (notificationEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Text("Remind me before:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                NotificationTimeSelector(
                    selectedMinutes = notificationMinutes,
                    onMinutesSelected = onNotificationMinutesChanged
                )
            }
        }
    }
}

@Composable
fun NotificationTimeSelector(
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit
) {
    val timeOptions = listOf(5, 10, 15, 30, 60)

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(timeOptions) { minutes ->
            FilterChip(
                selected = selectedMinutes == minutes,
                onClick = { onMinutesSelected(minutes) },
                label = { Text("${minutes}m") }
            )
        }
    }
}

private fun validateInput(title: String, startTime: String, endTime: String): Boolean {
    return title.isNotBlank() && isValidTime(startTime) && isValidTime(endTime)
}

private fun isValidTime(time: String): Boolean {
    return time.matches(Regex("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
}

private fun isValidTimeInput(input: String): Boolean {
    // Allow partial input while typing
    return input.matches(Regex("^([0-9]?[0-9]?)?:?([0-9]?[0-9]?)?$"))
}

private fun formatTime(time: String): String {
    // Ensure time is in HH:MM format
    return if (time.matches(Regex("^\\d{1,2}:\\d{2}$"))) {
        val parts = time.split(":")
        val hour = parts[0].padStart(2, '0')
        val minute = parts[1].padStart(2, '0')
        "$hour:$minute"
    } else {
        time
    }
}