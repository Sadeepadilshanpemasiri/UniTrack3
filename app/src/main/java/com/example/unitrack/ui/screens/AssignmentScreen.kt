// app/src/main/java/com/example/unitrack/ui/screens/AssignmentScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
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
// In AssignmentScreen.kt, add this import at the top:
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(navController: NavController, subjectId: Int) {
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
    val assignments by viewModel.getAssignmentsBySubject(subjectId).collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var assignmentToDelete by remember { mutableStateOf<com.example.unitrack.data.models.Assignment?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Assignments") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("add_assignment/$subjectId")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Assignment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Stats Card
            val stats by produceState(initialValue = GpaRepository.AssignmentStats(0, 0, 0, 0f, 0f)) {
                coroutineScope.launch {
                    value = repository.getAssignmentStats(subjectId)
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        title = "Total",
                        value = stats.totalAssignments.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    StatItem(
                        title = "Completed",
                        value = stats.completed.toString(),
                        color = Color(0xFF4CAF50)
                    )
                    StatItem(
                        title = "Pending",
                        value = stats.pending.toString(),
                        color = Color(0xFFF44336)
                    )
                    StatItem(
                        title = "Avg Marks",
                        value = String.format("%.1f", stats.averageMarks),
                        color = Color(0xFF2196F3)
                    )
                }
            }

            if (assignments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "No assignments",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No assignments yet",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Add your first assignment to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Overdue assignments first
                    val overdue = assignments.filter { it.isOverdue() }
                    if (overdue.isNotEmpty()) {
                        item {
                            Text(
                                text = "⚠️ OVERDUE (${overdue.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(overdue) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                onCardClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                },
                                onDeleteClick = {
                                    assignmentToDelete = assignment
                                    showDeleteDialog = true
                                },
                                onCompleteClick = {
                                    coroutineScope.launch {
                                        viewModel.completeAssignment(assignment.id, null)
                                    }
                                }
                            )
                        }
                    }

                    // Pending assignments
                    val pending = assignments.filter { it.status == "pending" && !it.isOverdue() }
                    if (pending.isNotEmpty()) {
                        item {
                            Text(
                                text = "PENDING (${pending.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(pending) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                onCardClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                },
                                onDeleteClick = {
                                    assignmentToDelete = assignment
                                    showDeleteDialog = true
                                },
                                onCompleteClick = {
                                    coroutineScope.launch {
                                        viewModel.completeAssignment(assignment.id, null)
                                    }
                                }
                            )
                        }
                    }

                    // Completed assignments
                    val completed = assignments.filter { it.status == "completed" }
                    if (completed.isNotEmpty()) {
                        item {
                            Text(
                                text = "COMPLETED (${completed.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF4CAF50),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(completed) { assignment ->
                            AssignmentCard(
                                assignment = assignment,
                                onCardClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                },
                                onDeleteClick = {
                                    assignmentToDelete = assignment
                                    showDeleteDialog = true
                                },
                                onCompleteClick = null // Already completed
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && assignmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Assignment") },
            text = {
                Text("Are you sure you want to delete '${assignmentToDelete?.title}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            assignmentToDelete?.let { viewModel.deleteAssignment(it) }
                            assignmentToDelete = null
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        assignmentToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AssignmentCard(
    assignment: com.example.unitrack.data.models.Assignment,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onCompleteClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onCardClick,
        colors = CardDefaults.cardColors(
            containerColor = if (assignment.isOverdue()) MaterialTheme.colorScheme.errorContainer
            else if (assignment.status == "completed") Color(0xFFE8F5E9)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1
                    )

                    if (assignment.description.isNotBlank()) {
                        Text(
                            text = assignment.description,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Priority Badge
                        Badge(
                            containerColor = Color(android.graphics.Color.parseColor(assignment.getPriorityColor()))
                        ) {
                            Text(
                                text = assignment.getPriorityText(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }

                        // Status Badge
                        Badge(
                            containerColor = when (assignment.status) {
                                "completed" -> Color(0xFF4CAF50)
                                "overdue" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.primary
                            }
                        ) {
                            Text(
                                text = assignment.status.replace("_", " ").uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }

                // Action Buttons
                Row {
                    onCompleteClick?.let {
                        IconButton(
                            onClick = it,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Mark as Complete",
                                tint = Color(0xFF4CAF50)
                            )
                        }
                    }

                    if (assignment.isOverdue()) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Overdue",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(24.dp)
                                .align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "📅 ${assignment.getDueDateFormatted()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "⏱️ ${assignment.estimatedTimeHours} hours",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Text(
                    text = assignment.getTimeRemainingText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (assignment.isOverdue()) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }

            if (assignment.status == "completed" && assignment.obtainedMarks != null) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = assignment.calculatePercentage() / 100,
                    modifier = Modifier.fillMaxWidth(),
                    color = when {
                        assignment.calculatePercentage() >= 80 -> Color(0xFF4CAF50)
                        assignment.calculatePercentage() >= 60 -> Color(0xFFFBC02D)
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Text(
                    text = "Marks: ${String.format("%.1f", assignment.obtainedMarks)}/${assignment.totalMarks} (${String.format("%.1f", assignment.calculatePercentage())}%)",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}