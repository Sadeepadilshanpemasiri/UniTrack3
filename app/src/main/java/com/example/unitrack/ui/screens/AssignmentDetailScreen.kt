// app/src/main/java/com/example/unitrack/ui/screens/AssignmentDetailScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.repositories.GpaRepository
import com.example.unitrack.ui.components.CountdownTimer
import com.example.unitrack.ui.viewmodels.AssignmentViewModel
import com.example.unitrack.ui.viewmodels.AssignmentViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDetailScreen(navController: NavController, assignmentId: Int) {
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

    var assignment by remember { mutableStateOf<com.example.unitrack.data.models.Assignment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCompleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var obtainedMarks by remember { mutableStateOf("") }

    // States for editing marks
    var showMarksDialog by remember { mutableStateOf(false) }
    var editMarks by remember { mutableStateOf("") }

    LaunchedEffect(assignmentId) {
        assignment = viewModel.getAssignmentById(assignmentId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    if (assignment != null) {
                        Text(assignment!!.title)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (assignment != null) {
                        // Edit Marks button
                        IconButton(
                            onClick = {
                                assignment?.let {
                                    editMarks = it.obtainedMarks?.toString() ?: ""
                                    showMarksDialog = true
                                }
                            },
                            enabled = assignment!!.status == "completed"
                        ) {
                            Icon(Icons.Default.Score, contentDescription = "Edit Marks")
                        }

                        IconButton(
                            onClick = { showEditDialog = true },
                            enabled = assignment!!.status != "completed"
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Assignment")
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (assignment != null) {
                if (assignment!!.status != "completed") {
                    ExtendedFloatingActionButton(
                        onClick = { showCompleteDialog = true },
                        icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Complete") },
                        text = { Text("Mark as Complete") }
                    )
                } else if (assignment!!.status == "completed" && assignment!!.obtainedMarks == null) {
                    // FAB to add marks after completion
                    ExtendedFloatingActionButton(
                        onClick = {
                            assignment?.let {
                                editMarks = ""
                                showMarksDialog = true
                            }
                        },
                        icon = { Icon(Icons.Default.Grade, contentDescription = "Add Marks") },
                        text = { Text("Add Marks") }
                    )
                }
            }
        }
    ) { paddingValues ->
        assignment?.let { assignment ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Countdown Timer (only if not completed)
                if (assignment.status != "completed") {
                    CountdownTimer(
                        dueDate = assignment.dueDate,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Completed status badge
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Completed",
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "COMPLETED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }
                }

                // Status and Priority Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatusBadge(
                            status = assignment.status.replace("_", " ").uppercase(),
                            color = when (assignment.status) {
                                "completed" -> Color(0xFF4CAF50)
                                "overdue" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )

                        PriorityBadge(
                            priority = assignment.getPriorityText(),
                            color = Color(android.graphics.Color.parseColor(assignment.getPriorityColor()))
                        )

                        TimeBadge(
                            time = "${assignment.estimatedTimeHours}h",
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Assignment Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        DetailRow(
                            icon = Icons.Default.CalendarToday,
                            title = "Due Date",
                            value = assignment.getDueDateFormatted()
                        )

                        if (assignment.status != "completed") {
                            DetailRow(
                                icon = Icons.Default.Schedule,
                                title = "Time Remaining",
                                value = assignment.getTimeRemainingText()
                            )
                        }

                        DetailRow(
                            icon = Icons.Default.Timer,
                            title = "Estimated Time",
                            value = "${assignment.estimatedTimeHours} hours"
                        )

                        if (assignment.totalMarks > 0) {
                            DetailRow(
                                icon = Icons.Default.Grade,
                                title = "Total Marks",
                                value = assignment.totalMarks.toString()
                            )
                        }

                        if (assignment.createdAt > 0) {
                            DetailRow(
                                icon = Icons.Default.DateRange,
                                title = "Created",
                                value = formatDate(assignment.createdAt)
                            )
                        }

                        if (assignment.completionDate != null) {
                            DetailRow(
                                icon = Icons.Default.CheckCircle,
                                title = "Completed",
                                value = formatDate(assignment.completionDate!!)
                            )
                        }

                        if (assignment.obtainedMarks != null) {
                            val percentage = assignment.calculatePercentage()
                            DetailRow(
                                icon = Icons.Default.Score,
                                title = "Obtained Marks",
                                value = "${String.format("%.1f", assignment.obtainedMarks)}/${assignment.totalMarks} (${String.format("%.1f", percentage)}%)",
                                valueColor = when {
                                    percentage >= 80 -> Color(0xFF4CAF50)
                                    percentage >= 60 -> Color(0xFFFBC02D)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )
                        } else if (assignment.status == "completed") {
                            DetailRow(
                                icon = Icons.Default.Info,
                                title = "Marks Status",
                                value = "Not entered yet",
                                valueColor = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Description Card
                if (assignment.description.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = assignment.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Notes Card
                if (assignment.notes.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Notes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = assignment.notes,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Performance Visualization (if completed with marks)
                if (assignment.status == "completed" && assignment.obtainedMarks != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Performance Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            val percentage = assignment.calculatePercentage()
                            val gradeInfo = getGradeAndFeedback(percentage)

                            CircularProgressIndicator(
                                progress = percentage / 100,
                                modifier = Modifier.size(120.dp),
                                strokeWidth = 12.dp,
                                color = gradeInfo.color
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${String.format("%.1f", percentage)}%",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = gradeInfo.color
                            )

                            Text(
                                text = "${String.format("%.1f", assignment.obtainedMarks)}/${assignment.totalMarks}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grade Display
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = gradeInfo.color.copy(alpha = 0.1f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = gradeInfo.grade,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = gradeInfo.color
                                    )
                                    Text(
                                        text = gradeInfo.feedback,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // Additional Statistics
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "📊 Performance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when {
                                            percentage >= 90 -> "Excellent"
                                            percentage >= 80 -> "Very Good"
                                            percentage >= 70 -> "Good"
                                            percentage >= 60 -> "Satisfactory"
                                            percentage >= 50 -> "Pass"
                                            else -> "Needs Improvement"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "🎯 Target",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = when {
                                            percentage >= assignment.totalMarks * 0.8 -> "Above Target"
                                            percentage >= assignment.totalMarks * 0.6 -> "On Target"
                                            else -> "Below Target"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Button to add/edit marks if completed but no marks
                if (assignment.status == "completed" && assignment.obtainedMarks == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF3E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "📝 Marks Not Entered",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "You can add your marks now or later",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Total Marks: ${assignment.totalMarks}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Button(
                                onClick = {
                                    editMarks = ""
                                    showMarksDialog = true
                                },
                                modifier = Modifier.padding(top = 12.dp)
                            ) {
                                Icon(Icons.Default.Grade, contentDescription = "Add Marks")
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Marks Now")
                            }
                        }
                    }
                }
            }
        } ?: run {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Assignment") },
            text = {
                Column {
                    Text("Are you sure you want to delete '${assignment?.title}'?")
                    if (assignment?.status == "completed") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ This assignment has been completed and graded.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            assignment?.let { viewModel.deleteAssignment(it) }
                            navController.navigateUp()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Complete Assignment Dialog
    if (showCompleteDialog && assignment != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Assignment") },
            text = {
                Column {
                    Text("Are you sure you want to mark '${assignment!!.title}' as complete?")

                    Spacer(modifier = Modifier.height(16.dp))

                    if (assignment!!.totalMarks > 0) {
                        Text("Enter obtained marks (optional):", style = MaterialTheme.typography.labelMedium)
                        OutlinedTextField(
                            value = obtainedMarks,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() || char == '.' } || it.isEmpty()) {
                                    obtainedMarks = it
                                }
                            },
                            label = { Text("Marks") },
                            modifier = Modifier.fillMaxWidth(),
                            suffix = { Text("/${assignment!!.totalMarks}") }
                        )

                        // Show percentage preview if marks entered
                        if (obtainedMarks.isNotBlank()) {
                            val marks = obtainedMarks.toFloatOrNull()
                            marks?.let {
                                if (assignment!!.totalMarks > 0) {
                                    val percentage = (it / assignment!!.totalMarks) * 100
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Percentage: ${String.format("%.1f", percentage)}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            percentage >= 80 -> Color(0xFF4CAF50)
                                            percentage >= 60 -> Color(0xFFFBC02D)
                                            else -> MaterialTheme.colorScheme.error
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You can add marks later if not available now",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val marks = if (obtainedMarks.isNotBlank()) {
                                obtainedMarks.toFloatOrNull()
                            } else null

                            viewModel.completeAssignment(assignmentId, marks)
                            assignment = viewModel.getAssignmentById(assignmentId)
                            showCompleteDialog = false
                            obtainedMarks = ""
                        }
                    }
                ) {
                    Text("Mark Complete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showCompleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Marks Dialog
    if (showMarksDialog && assignment != null) {
        AlertDialog(
            onDismissRequest = { showMarksDialog = false },
            title = {
                Text(
                    if (assignment!!.obtainedMarks == null) "Add Assignment Marks"
                    else "Edit Assignment Marks"
                )
            },
            text = {
                Column {
                    Text("Assignment: ${assignment!!.title}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Total Marks: ${assignment!!.totalMarks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = editMarks,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() || char == '.' } || it.isEmpty()) {
                                editMarks = it
                            }
                        },
                        label = { Text("Obtained Marks") },
                        modifier = Modifier.fillMaxWidth(),
                        suffix = { Text("/${assignment!!.totalMarks}") },
                        supportingText = {
                            if (editMarks.isNotBlank()) {
                                val marks = editMarks.toFloatOrNull()
                                marks?.let {
                                    if (it > assignment!!.totalMarks) {
                                        Text("Marks cannot exceed total marks")
                                    }
                                }
                            }
                        },
                        isError = editMarks.isNotBlank() &&
                                (editMarks.toFloatOrNull() ?: 0f) > assignment!!.totalMarks
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Leave blank to remove marks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Show current percentage and grade if marks are entered
                    if (editMarks.isNotBlank()) {
                        val marks = editMarks.toFloatOrNull()
                        marks?.let {
                            if (assignment!!.totalMarks > 0 && it <= assignment!!.totalMarks) {
                                val percentage = (it / assignment!!.totalMarks) * 100
                                val gradeInfo = getGradeAndFeedback(percentage)

                                Spacer(modifier = Modifier.height(12.dp))
                                Column {
                                    Text(
                                        text = "Percentage: ${String.format("%.1f", percentage)}%",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = gradeInfo.color
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Grade: ${gradeInfo.grade}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = gradeInfo.color
                                        )
                                        Text(
                                            text = gradeInfo.feedback,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val marks = editMarks.toFloatOrNull()
                        if (marks != null && marks > assignment!!.totalMarks) {
                            return@Button
                        }

                        coroutineScope.launch {
                            // Update assignment with marks
                            assignment?.let {
                                val updated = it.copy(obtainedMarks = marks)
                                viewModel.updateAssignment(updated)
                                assignment = updated
                            }
                            showMarksDialog = false
                            editMarks = ""
                        }
                    },
                    enabled = editMarks.isEmpty() ||
                            (editMarks.toFloatOrNull() ?: 0f) <= assignment!!.totalMarks
                ) {
                    Text(if (assignment!!.obtainedMarks == null) "Add Marks" else "Update Marks")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMarksDialog = false
                        editMarks = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatusBadge(status: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Badge(
            containerColor = color,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Text(
            text = "Status",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PriorityBadge(priority: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Badge(
            containerColor = color,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = priority,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun TimeBadge(time: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Badge(
            containerColor = color,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Text(
            text = "Est. Time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DetailRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = valueColor
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// Helper data class for grade information
data class GradeInfo(
    val grade: String,
    val feedback: String,
    val color: Color
)

// Helper function to get grade from percentage
// Replace the getGradeAndFeedback function with this:

// Helper function to get grade from percentage
// Option 2: Use hardcoded color for error
private fun getGradeAndFeedback(percentage: Float): GradeInfo {
    return when {
        percentage >= 90 -> GradeInfo(
            grade = "A+",
            feedback = "Excellent! 🎉",
            color = Color(0xFF4CAF50)
        )
        percentage >= 85 -> GradeInfo(
            grade = "A",
            feedback = "Very Good! 👍",
            color = Color(0xFF4CAF50)
        )
        percentage >= 80 -> GradeInfo(
            grade = "A-",
            feedback = "Good job! 👌",
            color = Color(0xFF4CAF50)
        )
        percentage >= 75 -> GradeInfo(
            grade = "B+",
            feedback = "Good work!",
            color = Color(0xFFFBC02D)
        )
        percentage >= 70 -> GradeInfo(
            grade = "B",
            feedback = "Satisfactory",
            color = Color(0xFFFBC02D)
        )
        percentage >= 65 -> GradeInfo(
            grade = "B-",
            feedback = "Satisfactory",
            color = Color(0xFFFBC02D)
        )
        percentage >= 60 -> GradeInfo(
            grade = "C+",
            feedback = "Pass",
            color = Color(0xFFFBC02D)
        )
        percentage >= 55 -> GradeInfo(
            grade = "C",
            feedback = "Pass",
            color = Color(0xFFFBC02D)
        )
        percentage >= 50 -> GradeInfo(
            grade = "C-",
            feedback = "Pass",
            color = Color(0xFFFBC02D)
        )
        percentage >= 45 -> GradeInfo(
            grade = "D+",
            feedback = "Marginal Pass",
            color = Color(0xFFFF9800)
        )
        percentage >= 40 -> GradeInfo(
            grade = "D",
            feedback = "Marginal Pass",
            color = Color(0xFFFF9800)
        )
        else -> GradeInfo(
            grade = "F",
            feedback = "Needs Improvement",
            color = Color(0xFFF44336) // Hardcoded red color instead of MaterialTheme.colorScheme.error
        )
    }
}