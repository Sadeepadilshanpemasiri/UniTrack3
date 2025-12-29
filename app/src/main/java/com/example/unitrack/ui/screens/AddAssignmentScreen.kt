// app/src/main/java/com/example/unitrack/ui/screens/AddAssignmentScreen.kt
package com.example.unitrack.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.text.SimpleDateFormat
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
            AppDatabase.getDatabase(context).assignmentDao(),
            AppDatabase.getDatabase(context).lectureDao()
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
    var totalMarks by remember { mutableStateOf("") }

    // Use a single Calendar instance for due date/time
    var dueCalendar by remember {
        mutableStateOf(Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 7) // Default: 1 week from now
            set(Calendar.HOUR_OF_DAY, 17) // 5 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        })
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var savedAssignmentTitle by remember { mutableStateOf("") }

    // Manual input fields for date - synchronized with dueCalendar
    var dayInput by remember {
        mutableStateOf(dueCalendar.get(Calendar.DAY_OF_MONTH).toString())
    }
    var monthInput by remember {
        mutableStateOf((dueCalendar.get(Calendar.MONTH) + 1).toString())
    }
    var yearInput by remember {
        mutableStateOf(dueCalendar.get(Calendar.YEAR).toString())
    }
    var hourInput by remember {
        mutableStateOf(String.format("%02d", dueCalendar.get(Calendar.HOUR_OF_DAY)))
    }
    var minuteInput by remember {
        mutableStateOf(String.format("%02d", dueCalendar.get(Calendar.MINUTE)))
    }

    val priorities = listOf(
        Pair(1, "Low"),
        Pair(2, "Medium"),
        Pair(3, "High"),
        Pair(4, "Critical")
    )

    // Update calendar when manual inputs change
    LaunchedEffect(dayInput, monthInput, yearInput, hourInput, minuteInput) {
        val day = dayInput.toIntOrNull() ?: 1
        val month = monthInput.toIntOrNull() ?: 1
        val year = yearInput.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val hour = hourInput.toIntOrNull() ?: 17
        val minute = minuteInput.toIntOrNull() ?: 0

        if (day in 1..31 && month in 1..12 && year >= 2023 && year <= 2030 &&
            hour in 0..23 && minute in 0..59) {

            dueCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month - 1) // Month is 0-based
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
            }
        }
    }

    // Update manual inputs when quick buttons are pressed
    fun updateInputsFromCalendar() {
        dayInput = dueCalendar.get(Calendar.DAY_OF_MONTH).toString()
        monthInput = (dueCalendar.get(Calendar.MONTH) + 1).toString()
        yearInput = dueCalendar.get(Calendar.YEAR).toString()
        hourInput = String.format("%02d", dueCalendar.get(Calendar.HOUR_OF_DAY))
        minuteInput = String.format("%02d", dueCalendar.get(Calendar.MINUTE))
    }

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
                                coroutineScope.launch {
                                    try {
                                        viewModel.addAssignment(
                                            subjectId = subjectId,
                                            title = title,
                                            description = description,
                                            dueDate = dueCalendar.timeInMillis,
                                            priority = selectedPriority,
                                            estimatedTimeHours = estimatedHours.toInt(),
                                            totalMarks = totalMarks.toFloatOrNull() ?: 0f
                                        )
                                        savedAssignmentTitle = title
                                        showSuccessDialog = true
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to save assignment: ${e.message}"
                                        showError = true
                                    }
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

            // Date Selection Card
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
                            text = formatDateTime(dueCalendar.time),
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
                    label = { Text("Total Marks *") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = showError && (totalMarks.isBlank() || totalMarks.toFloatOrNull() == null)
                )
            }

            // Quick date buttons
            Text("Quick Set Due Date:", style = MaterialTheme.typography.labelMedium)

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
                            dueCalendar = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, days)
                                set(Calendar.HOUR_OF_DAY, 17) // 5 PM
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            updateInputsFromCalendar()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label)
                    }
                }
            }

            // Quick time buttons
            Text("Quick Set Time:", style = MaterialTheme.typography.labelMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Pair("9 AM", 9),
                    Pair("12 PM", 12),
                    Pair("3 PM", 15),
                    Pair("5 PM", 17),
                    Pair("11 PM", 23)
                ).forEach { (label, hour) ->
                    Button(
                        onClick = {
                            dueCalendar = Calendar.getInstance().apply {
                                timeInMillis = dueCalendar.timeInMillis
                                set(Calendar.HOUR_OF_DAY, hour)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            updateInputsFromCalendar()
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
                        coroutineScope.launch {
                            try {
                                viewModel.addAssignment(
                                    subjectId = subjectId,
                                    title = title,
                                    description = description,
                                    dueDate = dueCalendar.timeInMillis,
                                    priority = selectedPriority,
                                    estimatedTimeHours = estimatedHours.toInt(),
                                    totalMarks = totalMarks.toFloatOrNull() ?: 0f
                                )
                                savedAssignmentTitle = title
                                showSuccessDialog = true
                            } catch (e: Exception) {
                                errorMessage = "Failed to save assignment: ${e.message}"
                                showError = true
                            }
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
                        text = "Due: ${formatDateTime(dueCalendar.time)}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Estimated: ${estimatedHours.toIntOrNull() ?: 2} hours",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (totalMarks.isNotBlank()) {
                        Text(
                            text = "Total Marks: ${totalMarks.toFloatOrNull() ?: 0f}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
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
                    // Date Selection
                    Text("Date (DD/MM/YYYY):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Day
                        OutlinedTextField(
                            value = dayInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    dayInput = it
                                }
                            },
                            label = { Text("DD") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("DD") },
                            supportingText = {
                                if (dayInput.isNotBlank()) {
                                    val day = dayInput.toIntOrNull()
                                    if (day == null || day !in 1..31) {
                                        Text("Must be 1-31")
                                    }
                                }
                            },
                            isError = dayInput.isNotBlank() &&
                                    (dayInput.toIntOrNull() == null || dayInput.toInt() !in 1..31)
                        )

                        // Month
                        OutlinedTextField(
                            value = monthInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    monthInput = it
                                }
                            },
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("MM") },
                            supportingText = {
                                if (monthInput.isNotBlank()) {
                                    val month = monthInput.toIntOrNull()
                                    if (month == null || month !in 1..12) {
                                        Text("Must be 1-12")
                                    }
                                }
                            },
                            isError = monthInput.isNotBlank() &&
                                    (monthInput.toIntOrNull() == null || monthInput.toInt() !in 1..12)
                        )

                        // Year
                        OutlinedTextField(
                            value = yearInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 4) {
                                    yearInput = it
                                }
                            },
                            label = { Text("YYYY") },
                            modifier = Modifier.weight(1.5f),
                            placeholder = { Text("YYYY") },
                            supportingText = {
                                if (yearInput.isNotBlank()) {
                                    val year = yearInput.toIntOrNull()
                                    if (year == null || year < 2023 || year > 2030) {
                                        Text("2023-2030")
                                    }
                                }
                            },
                            isError = yearInput.isNotBlank() &&
                                    (yearInput.toIntOrNull() == null ||
                                            yearInput.toInt() !in 2023..2030)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Selection
                    Text("Time (24-hour format):", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hour
                        OutlinedTextField(
                            value = hourInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    hourInput = it
                                }
                            },
                            label = { Text("HH") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("HH") },
                            supportingText = {
                                if (hourInput.isNotBlank()) {
                                    val hour = hourInput.toIntOrNull()
                                    if (hour == null || hour !in 0..23) {
                                        Text("Must be 0-23")
                                    }
                                }
                            },
                            isError = hourInput.isNotBlank() &&
                                    (hourInput.toIntOrNull() == null || hourInput.toInt() !in 0..23)
                        )

                        // Minute
                        OutlinedTextField(
                            value = minuteInput,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 2) {
                                    minuteInput = it
                                }
                            },
                            label = { Text("MM") },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("MM") },
                            supportingText = {
                                if (minuteInput.isNotBlank()) {
                                    val minute = minuteInput.toIntOrNull()
                                    if (minute == null || minute !in 0..59) {
                                        Text("Must be 0-59")
                                    }
                                }
                            },
                            isError = minuteInput.isNotBlank() &&
                                    (minuteInput.toIntOrNull() == null || minuteInput.toInt() !in 0..59)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Current date display
                    Text(
                        text = "Selected: ${formatDateTime(dueCalendar.time)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Quick date selection
                    Text("Quick Selection:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        listOf(
                            "Today" to 0,
                            "Tomorrow" to 1,
                            "3 Days" to 3,
                            "1 Week" to 7,
                            "2 Weeks" to 14
                        ).forEach { (label, days) ->
                            TextButton(
                                onClick = {
                                    dueCalendar = Calendar.getInstance().apply {
                                        add(Calendar.DAY_OF_YEAR, days)
                                        set(Calendar.HOUR_OF_DAY, 17)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    updateInputsFromCalendar()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(label)
                            }
                        }
                    }

                    // Quick time selection
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Quick Time:", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "9:00" to 9,
                            "12:00" to 12,
                            "15:00" to 15,
                            "17:00" to 17,
                            "23:00" to 23
                        ).forEach { (time, hour) ->
                            Button(
                                onClick = {
                                    dueCalendar = Calendar.getInstance().apply {
                                        timeInMillis = dueCalendar.timeInMillis
                                        set(Calendar.HOUR_OF_DAY, hour)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                    }
                                    updateInputsFromCalendar()
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
                    onClick = {
                        showDatePicker = false
                    }
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

    // Success Dialog - FIXED VERSION
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                navController.navigateUp()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Done,  // Changed from CheckCircle to Done
                        contentDescription = "Success",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Assignment Added!")
                }
            },
            text = {
                Column {
                    Text("'$savedAssignmentTitle' has been saved successfully.",
                        style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Due: ${formatDateTime(dueCalendar.time)}",
                        style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Column {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            // Reset form for new assignment
                            title = ""
                            description = ""
                            selectedPriority = 2
                            estimatedHours = "2"
                            totalMarks = ""
                            dueCalendar = Calendar.getInstance().apply {
                                add(Calendar.DAY_OF_YEAR, 7)
                                set(Calendar.HOUR_OF_DAY, 17)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                            }
                            updateInputsFromCalendar()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Text("Add Another Assignment")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            showSuccessDialog = false
                            navController.navigateUp()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        )
    }
}

// Helper functions
private fun validateInput(title: String, hours: String, marks: String): Boolean {
    return title.isNotBlank() &&
            hours.isNotBlank() &&
            hours.toIntOrNull() != null &&
            hours.toInt() > 0 &&
            (marks.isEmpty() || marks.toFloatOrNull() != null)
}

private fun formatDateTime(date: Date): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}

