// app/src/main/java/com/example/unitrack/ui/screens/AddSubjectScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
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
import com.example.unitrack.ui.viewmodels.SubjectViewModel
import com.example.unitrack.ui.viewmodels.SubjectViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubjectScreen(navController: NavController, semesterId: Int) {
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
    val viewModel: SubjectViewModel = viewModel(
        factory = SubjectViewModelFactory(repository)
    )
    val coroutineScope = rememberCoroutineScope()

    var subjectName by remember { mutableStateOf("") }
    var creditValue by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("A+") }
    var isCalculated by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    val grades = listOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "E", "F")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Subject") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (validateInput(subjectName, creditValue)) {
                                coroutineScope.launch {
                                    viewModel.addSubject(
                                        semesterId = semesterId,
                                        name = subjectName,
                                        creditValue = creditValue.toInt(),
                                        grade = selectedGrade,
                                        isCalculated = isCalculated
                                    )
                                    navController.navigateUp()
                                }
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
                value = subjectName,
                onValueChange = {
                    subjectName = it
                    if (showError) showError = false
                },
                label = { Text("Subject Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && subjectName.isBlank()
            )

            // In AddSubjectScreen.kt, update the creditValue TextField:

            OutlinedTextField(
                value = creditValue,
                onValueChange = {
                    // Only allow digits and limit to 1-30 credits
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        val value = it.toIntOrNull() ?: 0
                        if (value <= 30) { // Limit to reasonable credit value
                            creditValue = it
                            if (showError) showError = false
                        }
                    }
                },
                label = { Text("Credit Value (1-30) *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = showError && (creditValue.isBlank() || creditValue.toIntOrNull() == null || creditValue.toInt() !in 1..30),
                supportingText = {
                    if (creditValue.isNotBlank()) {
                        val value = creditValue.toIntOrNull() ?: 0
                        if (value > 30) {
                            Text("Max 30 credits allowed")
                        }
                    }
                }
            )

            Text("Select Grade:", style = MaterialTheme.typography.labelMedium)

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(grades) { grade ->
                    FilterChip(
                        selected = selectedGrade == grade,
                        onClick = { selectedGrade = grade },
                        label = { Text(grade) }
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(
                    checked = isCalculated,
                    onCheckedChange = { isCalculated = it }
                )
                Text(
                    text = if (isCalculated) "Include in GPA calculation"
                    else "Exclude from GPA calculation",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Save Button at bottom
            Button(
                onClick = {
                    if (validateInput(subjectName, creditValue)) {
                        coroutineScope.launch {
                            viewModel.addSubject(
                                semesterId = semesterId,
                                name = subjectName,
                                creditValue = creditValue.toInt(),
                                grade = selectedGrade,
                                isCalculated = isCalculated
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
                Text("Save Subject")
            }

            if (showError) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // GPA Calculation Preview
            val credits = creditValue.toIntOrNull() ?: 0
            val gradePoints = when (selectedGrade) {
                "A+" -> 4.0
                "A" -> 4.0
                "A-" -> 3.7
                "B+" -> 3.3
                "B" -> 3.0
                "B-" -> 2.7
                "C+" -> 2.3
                "C" -> 2.0
                "C-" -> 1.7
                "D+" -> 1.3
                "D" -> 1.0
                "E" -> 0.0
                "F" -> 0.0
                else -> 0.0
            }
            val contribution = credits * gradePoints

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
                        text = "GPA Contribution Preview",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Grade Points: $gradePoints",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Credits: $credits",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Contribution: $contribution points",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!isCalculated) {
                        Text(
                            text = "Note: Will be excluded from GPA calculation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

private fun validateInput(name: String, credits: String): Boolean {
    return name.isNotBlank() &&
            credits.isNotBlank() &&
            credits.toIntOrNull() != null &&
            credits.toInt() > 0
}