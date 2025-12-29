// app/src/main/java/com/example/unitrack/ui/screens/SubjectScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.unitrack.ui.viewmodels.SubjectViewModel
import com.example.unitrack.ui.viewmodels.SubjectViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreen(navController: NavController, semesterId: Int) {
    val context = LocalContext.current
    val repository = remember { RepositoryFactory.getRepository(context) }
    val viewModel: SubjectViewModel = viewModel(
        factory = SubjectViewModelFactory(repository)
    )
    val subjects by viewModel.getSubjectsBySemester(semesterId).collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var subjectToDelete by remember { mutableStateOf<com.example.unitrack.data.models.Subject?>(null) }

    // Calculate current GPA
    val (currentGPA, totalCredits) = remember(subjects) {
        val calculatedSubjects = subjects.filter { it.isCalculated }
        var totalPoints = 0.0
        var totalCredits = 0

        calculatedSubjects.forEach { subject ->
            totalPoints += subject.gradeToPoints() * subject.creditValue
            totalCredits += subject.creditValue
        }

        val gpa = if (totalCredits > 0) {
            String.format("%.2f", totalPoints / totalCredits).toDouble()
        } else 0.0

        Pair(gpa, subjects.sumOf { it.creditValue })
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Subjects")
                        Text(
                            text = "GPA: $currentGPA | Total Credits: $totalCredits",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
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
                    navController.navigate("add_subject/$semesterId")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GPA Summary Card
            item {
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Semester GPA",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = currentGPA.toString(),
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Subjects",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = subjects.size.toString(),
                                style = MaterialTheme.typography.displayMedium
                            )
                        }
                    }
                }
            }

            if (subjects.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No subjects added yet",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Tap + button to add your first subject",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(subjects) { subject ->
                    SubjectCard(
                        subject = subject,
                        onDeleteClick = {
                            subjectToDelete = subject
                            showDeleteDialog = true
                        },
                        onEditClick = {
                            // TODO: Implement edit functionality
                        },
                        navController = navController
                    )
                }
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Subject") },
            text = {
                Text("Are you sure you want to delete '${subjectToDelete?.name}'?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            subjectToDelete?.let { viewModel.deleteSubject(it) }
                            subjectToDelete = null
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        subjectToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SubjectCard(
    subject: com.example.unitrack.data.models.Subject,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavController
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Grade: ${subject.grade} | Credits: ${subject.creditValue}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }

                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Grade Points: ${subject.gradeToPoints()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "Contribution: ${subject.gradeToPoints() * subject.creditValue}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = if (subject.isCalculated) "In GPA" else "Excluded",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (subject.isCalculated) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.error
                )
            }

            // Assignments button
            Button(
                onClick = {
                    navController.navigate("assignments/${subject.id}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Icon(
                    Icons.Default.Assignment,
                    contentDescription = "Assignments",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Assignments")
            }
        }
    }
}