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
import androidx.compose.ui.graphics.vector.ImageVector  // ADD THIS IMPORT
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
            AppDatabase.getDatabase(context).assignmentDao()
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
                        IconButton(
                            onClick = { showEditDialog = true },
                            enabled = assignment!!.status != "completed"
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
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
            if (assignment != null && assignment!!.status != "completed") {
                ExtendedFloatingActionButton(
                    onClick = { showCompleteDialog = true },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Complete") },
                    text = { Text("Mark as Complete") }
                )
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
                // Countdown Timer
                CountdownTimer(
                    dueDate = assignment.dueDate,
                    modifier = Modifier.fillMaxWidth()
                )

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

                        DetailRow(
                            icon = Icons.Default.Schedule,
                            title = "Time Remaining",
                            value = assignment.getTimeRemainingText()
                        )

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
                            DetailRow(
                                icon = Icons.Default.Score,
                                title = "Obtained Marks",
                                value = "${assignment.obtainedMarks}/${assignment.totalMarks} (${String.format("%.1f", assignment.calculatePercentage())}%)"
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

                // Progress Visualization (if completed)
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
                                text = "Performance",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            CircularProgressIndicator(
                                progress = assignment.calculatePercentage() / 100,
                                modifier = Modifier.size(120.dp),
                                strokeWidth = 12.dp,
                                color = when {
                                    assignment.calculatePercentage() >= 80 -> Color(0xFF4CAF50)
                                    assignment.calculatePercentage() >= 60 -> Color(0xFFFBC02D)
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${String.format("%.1f", assignment.calculatePercentage())}%",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "${String.format("%.1f", assignment.obtainedMarks)}/${assignment.totalMarks}",
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = when {
                                    assignment.calculatePercentage() >= 90 -> "Excellent! 🎉"
                                    assignment.calculatePercentage() >= 80 -> "Great job! 👍"
                                    assignment.calculatePercentage() >= 70 -> "Good work! 👌"
                                    assignment.calculatePercentage() >= 60 -> "Satisfactory"
                                    else -> "Needs improvement"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
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
            text = { Text("Are you sure you want to delete '${assignment?.title}'?") },
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
                            navController.navigateUp()
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
fun DetailRow(icon: ImageVector, title: String, value: String) {  // CHANGED FROM Icons.Filled to ImageVector
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
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}