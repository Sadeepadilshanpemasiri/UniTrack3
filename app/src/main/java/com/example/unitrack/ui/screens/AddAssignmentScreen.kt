// app/src/main/java/com/example/unitrack/ui/screens/AddAssignmentScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.repositories.GpaRepository
import com.example.unitrack.ui.viewmodels.AssignmentViewModel
import com.example.unitrack.ui.viewmodels.AssignmentViewModelFactory
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAssignmentScreen(navController: NavController, subjectId: Int) {
    val context = LocalContext.current
    val repository = remember {
        GpaRepository(
            AppDatabase.getDatabase(context).userDao(),
            AppDatabase.getDatabase(context).semesterDao(),
            AppDatabase.getDatabase(context).subjectDao(),
            AppDatabase.getDatabase(context).assignmentDao()
        )
    }
    val viewModel: AssignmentViewModel = viewModel(
        factory = AssignmentViewModelFactory(repository)
    )
    val coroutineScope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(2) } // Medium by default
    var estimatedHours by remember { mutableStateOf("2") }
    var totalMarks by remember { mutableStateOf("100") }

    // Date picker states - use mutableStateOf for Calendar
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedTime by remember { mutableStateOf(Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 7) // Default to 1 week from now
    }) }

    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val priorities = listOf(
        Pair(1, "Low"),
        Pair(2, "Medium"),
        Pair(3, "High"),
        Pair(4, "Critical")
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Assignment") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateInput(title, estimatedHours, totalMarks)) {
                                // Combine date and time
                                val dueCalendar = Calendar.getInstance().apply {
                                    time = selectedDate.time
                                    set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                                    set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                                    set(Calendar.SECOND, 0)
                                }

                                coroutineScope.launch {
                                    viewModel.addAssignment(
                                        subjectId = subjectId,
                                        title = title,
                                        description = description,
                                        dueDate = dueCalendar.timeInMillis,
                                        priority = selectedPriority,
                                        estimatedTimeHours = estimatedHours.toInt(),
                                        totalMarks = totalMarks.toFloat()
                                    )
                                    navController.navigateUp()
                                }
                            } else {
                                errorMessage = "Please fill all required fields correctly"
                                showError = true
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
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Assignment Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && title.isBlank()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5
            )

            Text("Priority:", style = MaterialTheme.typography.labelMedium)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(priorities) { (value, label) ->
                    val priorityColor = when (value) {
                        1 -> Color(0xFF4CAF50) // Green
                        2 -> Color(0xFFFBC02D) // Yellow
                        3 -> Color(0xFFFF9800) // Orange
                        4 -> Color(0xFFF44336) // Red
                        else -> Color(0xFF4CAF50)
                    }

                    FilterChip(
                        selected = selectedPriority == value,
                        onClick = { selectedPriority = value },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = priorityColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Date Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showDatePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Due Date & Time",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = formatDateTime(selectedDate, selectedTime),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = "Select Date"
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = estimatedHours,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                            estimatedHours = it
                        }
                    },
                    label = { Text("Estimated Hours *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = showError && (estimatedHours.isBlank() || estimatedHours.toIntOrNull() == null)
                )

                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = {
                        if (it.all { char -> char.isDigit() || char == '.' } || it.isEmpty()) {
                            totalMarks = it
                        }
                    },
                    label = { Text("Total Marks") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            // Quick date buttons - FIXED VERSION
            Text("Quick Set:", style = MaterialTheme.typography.labelMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("Tomorrow", 1),
                    Pair("3 Days", 3),
                    Pair("1 Week", 7),
                    Pair("2 Weeks", 14)
                ).forEach { (label, days) ->
                    Button(
                        onClick = {
                            // Create a new Calendar instance for the date
                            val newDate = Calendar.getInstance()
                            newDate.add(Calendar.DAY_OF_YEAR, days)
                            selectedDate = newDate

                            // Update the existing selectedTime Calendar object
                            selectedTime.set(Calendar.HOUR_OF_DAY, 17)
                            selectedTime.set(Calendar.MINUTE, 0)

                            // Trigger recomposition by updating the state
                            selectedTime = Calendar.getInstance().apply {
                                timeInMillis = selectedTime.timeInMillis
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }

            Button(
                onClick = {
                    if (validateInput(title, estimatedHours, totalMarks)) {
                        val dueCalendar = Calendar.getInstance().apply {
                            time = selectedDate.time
                            set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
                            set(Calendar.SECOND, 0)
                        }

                        coroutineScope.launch {
                            viewModel.addAssignment(
                                subjectId = subjectId,
                                title = title,
                                description = description,
                                dueDate = dueCalendar.timeInMillis,
                                priority = selectedPriority,
                                estimatedTimeHours = estimatedHours.toInt(),
                                totalMarks = totalMarks.toFloat()
                            )
                            navController.navigateUp()
                        }
                    } else {
                        errorMessage = "Please fill all required fields correctly"
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Save Assignment")
            }

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Preview card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Assignment Preview",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (title.isNotBlank()) {
                        Text(
                            text = "Title: $title",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (description.isNotBlank()) {
                        Text(
                            text = "Description: ${description.take(50)}${if (description.length > 50) "..." else ""}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Text(
                        text = "Priority: ${priorities.find { it.first == selectedPriority }?.second ?: "Medium"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Due: ${formatDateTime(selectedDate, selectedTime)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Estimated: ${estimatedHours.toIntOrNull() ?: 2} hours",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Due Date & Time") },
            text = {
                Column {
                    // Date picker
                    Text("Select Date:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Note: For a real date picker, you'd use DatePicker from accompanist
                    // This is a simplified version
                    OutlinedTextField(
                        value = formatDateOnly(selectedDate),
                        onValueChange = { /* In real app, this would open a date picker */ },
                        label = { Text("Date") },
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Pick Date"
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time picker
                    Text("Select Time:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = String.format("%02d", selectedTime.get(Calendar.HOUR_OF_DAY)),
                            onValueChange = {
                                val hour = it.toIntOrNull() ?: 0
                                if (hour in 0..23) {
                                    // Create a new Calendar instance with updated time
                                    val updatedTime = Calendar.getInstance().apply {
                                        timeInMillis = selectedTime.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, hour)
                                    }
                                    selectedTime = updatedTime
                                }
                            },
                            label = { Text("Hour") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = String.format("%02d", selectedTime.get(Calendar.MINUTE)),
                            onValueChange = {
                                val minute = it.toIntOrNull() ?: 0
                                if (minute in 0..59) {
                                    // Create a new Calendar instance with updated time
                                    val updatedTime = Calendar.getInstance().apply {
                                        timeInMillis = selectedTime.timeInMillis
                                        set(Calendar.MINUTE, minute)
                                    }
                                    selectedTime = updatedTime
                                }
                            },
                            label = { Text("Minute") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Quick time buttons
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Common Times:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("9 AM", "12 PM", "3 PM", "5 PM", "11 PM").forEach { time ->
                            Button(
                                onClick = {
                                    val hour = when (time) {
                                        "9 AM" -> 9
                                        "12 PM" -> 12
                                        "3 PM" -> 15
                                        "5 PM" -> 17
                                        "11 PM" -> 23
                                        else -> 17
                                    }
                                    // Create a new Calendar instance with updated time
                                    val updatedTime = Calendar.getInstance().apply {
                                        timeInMillis = selectedTime.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, 0)
                                    }
                                    selectedTime = updatedTime
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(time)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDatePicker = false }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePicker = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun validateInput(title: String, hours: String, marks: String): Boolean {
    return title.isNotBlank() &&
            hours.isNotBlank() &&
            hours.toIntOrNull() != null &&
            hours.toInt() > 0 &&
            (marks.isEmpty() || marks.toFloatOrNull() != null)
}

private fun formatDateTime(date: Calendar, time: Calendar): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val combined = Calendar.getInstance().apply {
        setTime(date.time)  // Use setTime() instead of time = date.time
        set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, time.get(Calendar.MINUTE))
    }
    return sdf.format(combined.time)
}

private fun formatDateOnly(date: Calendar): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(date.time)
}