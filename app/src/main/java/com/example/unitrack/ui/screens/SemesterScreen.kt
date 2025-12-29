// REPLACE THE ENTIRE SemesterScreen.kt file with this:

// app/src/main/java/com/example/unitrack/ui/screens/SemesterScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.repositories.GpaRepository
import com.example.unitrack.ui.viewmodels.SemesterViewModel
import com.example.unitrack.ui.viewmodels.SemesterViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SemesterScreen(navController: NavController, userId: Int) {
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
    val viewModel: SemesterViewModel = viewModel(
        factory = SemesterViewModelFactory(repository)
    )
    val semesters by viewModel.getSemestersByUser(userId).collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var year by remember { mutableStateOf("1") }
    var semesterNumber by remember { mutableStateOf("1") }
    var semesterName by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Semesters") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Semester")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (semesters.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No semesters yet",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = "Add your first semester to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Add Semester")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Group by year
                    val groupedByYear = semesters.groupBy { it.year }

                    groupedByYear.keys.sorted().forEach { year ->
                        item {
                            Text(
                                text = "Year $year",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        val yearSemesters = groupedByYear[year]?.sortedBy { it.semesterNumber } ?: emptyList()
                        items(yearSemesters) { semester ->
                            SemesterCard(
                                semester = semester,
                                onClick = {
                                    navController.navigate("subjects/${semester.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Semester") },
            text = {
                Column {
                    OutlinedTextField(
                        value = year,
                        onValueChange = {
                            // Allow only 1-4
                            if (it.isEmpty() || (it.toIntOrNull() in 1..4)) {
                                year = it
                            }
                        },
                        label = { Text("Year (1-4)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = year.isNotBlank() && year.toIntOrNull() !in 1..4
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = semesterNumber,
                        onValueChange = {
                            // Allow only 1 or 2
                            if (it.isEmpty() || it == "1" || it == "2") {
                                semesterNumber = it
                            }
                        },
                        label = { Text("Semester (1 or 2)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = semesterNumber.isNotBlank() && semesterNumber !in listOf("1", "2")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = semesterName,
                        onValueChange = { semesterName = it },
                        label = { Text("Semester Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (errorMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Clear previous error
                        errorMessage = ""

                        // Validate inputs
                        val yearInt = year.toIntOrNull()
                        val semesterInt = semesterNumber.toIntOrNull()

                        if (yearInt == null || yearInt !in 1..4) {
                            errorMessage = "Year must be between 1 and 4"
                            return@Button
                        }

                        if (semesterInt == null || semesterInt !in 1..2) {
                            errorMessage = "Semester must be 1 or 2"
                            return@Button
                        }

                        coroutineScope.launch {
                            // Check if semester already exists
                            val existing = repository.getSemesterByDetails(userId, yearInt, semesterInt)
                            if (existing != null) {
                                errorMessage = "Year $yearInt Semester $semesterInt already exists"
                                return@launch
                            }

                            // Create semester
                            val name = if (semesterName.isBlank()) {
                                "Year $yearInt Semester $semesterInt"
                            } else {
                                semesterName
                            }

                            viewModel.addSemester(
                                userId = userId,
                                year = yearInt,
                                semesterNumber = semesterInt,
                                name = name
                            )

                            // Reset and close
                            showAddDialog = false
                            year = "1"
                            semesterNumber = "1"
                            semesterName = ""
                            errorMessage = ""
                        }
                    },
                    enabled = year.isNotBlank() && semesterNumber.isNotBlank()
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        year = "1"
                        semesterNumber = "1"
                        semesterName = ""
                        errorMessage = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SemesterCard(
    semester: com.example.unitrack.data.models.Semester,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = semester.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Year ${semester.year} - Semester ${semester.semesterNumber}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = String.format("GPA: %.2f", semester.gpa),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}