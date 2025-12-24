// app/src/main/java/com/example/unitrack/ui/screens/HomeScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
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
import com.example.unitrack.ui.viewmodels.SemesterViewModel
import com.example.unitrack.ui.viewmodels.SemesterViewModelFactory
//import kotlinx.coroutines.flow.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userId: Int) {
    val context = LocalContext.current
    val repository = remember {
        GpaRepository(
            AppDatabase.getDatabase(context).userDao(),
            AppDatabase.getDatabase(context).semesterDao(),
            AppDatabase.getDatabase(context).subjectDao(),
            AppDatabase.getDatabase(context).assignmentDao() // ADD THIS LINE
        )
    }
    val viewModel: SemesterViewModel = viewModel(
        factory = SemesterViewModelFactory(repository)
    )
    val semesters by viewModel.getSemestersByUser(userId).collectAsState(emptyList())

    // Calculate overall stats
    val averageGPA = remember(semesters) {
        if (semesters.isNotEmpty()) {
            val sum = semesters.sumOf { it.gpa }
            String.format("%.2f", sum / semesters.size).toDouble()
        } else {
            0.0
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("UniTrack Dashboard") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("semester/$userId")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Semester")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to UniTrack",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Semesters",
                    value = semesters.size.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Avg GPA",
                    value = averageGPA.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Actions - Assignment Dashboard Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate("assignment_dashboard/$userId")
                },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Assignment,
                        contentDescription = "Assignments",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Assignment Dashboard",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Track all your pending assignments",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Actions - Semester Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController.navigate("semester/$userId")
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Semesters",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "View Semesters",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Manage your academic semesters",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        navController.navigate("user_selection")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = "Switch User", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Switch User")
                }

                if (semesters.isNotEmpty()) {
                    val latestSemester = semesters.maxByOrNull { it.createdAt }
                    latestSemester?.let {
                        Button(
                            onClick = {
                                navController.navigate("subjects/${it.id}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.School, contentDescription = "Latest Semester", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Continue with Latest Semester")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Instructions
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
                        text = "How to use:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    InstructionItem(number = 1, text = "Add a semester")
                    InstructionItem(number = 2, text = "Add subjects to the semester")
                    InstructionItem(number = 3, text = "Add assignments to subjects")
                    InstructionItem(number = 4, text = "Track deadlines with countdown timer")
                    InstructionItem(number = 5, text = "View GPA automatically calculated")
                }
            }

            // Quick Tips
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Quick Tip",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "Use the Assignment Dashboard to track all pending assignments across all your subjects in one place.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun InstructionItem(number: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Badge(
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall
        )
    }
}