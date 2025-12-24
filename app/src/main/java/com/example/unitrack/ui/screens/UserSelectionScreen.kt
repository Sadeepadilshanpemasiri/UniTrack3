// app/src/main/java/com/example/unitrack/ui/screens/UserSelectionScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import com.example.unitrack.ui.viewmodels.UserViewModel
import com.example.unitrack.ui.viewmodels.UserViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val repository = remember {
        GpaRepository(
            AppDatabase.getDatabase(context).userDao(),
            AppDatabase.getDatabase(context).semesterDao(),
            AppDatabase.getDatabase(context).subjectDao(),
            AppDatabase.getDatabase(context).assignmentDao()
        )
    }
    val viewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(repository)
    )
    // FIXED: Use collectAsState() instead of observeAsState()
    val users by viewModel.allUsers.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<com.example.unitrack.data.models.User?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    var newStudentId by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Select User") }
            )
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
                text = "UniTrack",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Users (${users.size})",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("Add User")
                        }
                    }

                    if (users.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found. Add your first user!",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(users, key = { it.id }) { user ->
                                UserCard(
                                    user = user,
                                    onSelect = {
                                        navController.navigate("home/${user.id}")
                                    },
                                    onDelete = {
                                        userToDelete = user
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }

                    if (users.isNotEmpty()) {
                        Text(
                            text = "Click delete icon (🗑️) to remove user",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }

    // Add User Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New User") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newStudentId,
                        onValueChange = { newStudentId = it },
                        label = { Text("Student ID *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUserName.isNotBlank() && newStudentId.isNotBlank()) {
                            viewModel.addUser(newUserName, newStudentId)
                            newUserName = ""
                            newStudentId = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && userToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete User") },
            text = {
                Column {
                    Text("Are you sure you want to delete this user?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("User: ${userToDelete?.name}", style = MaterialTheme.typography.bodyMedium)
                    Text("ID: ${userToDelete?.studentId}", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "⚠️ Warning: This will also delete all semesters and subjects for this user!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            userToDelete?.let { viewModel.deleteUser(it) }
                            userToDelete = null
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
                        userToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UserCard(
    user: com.example.unitrack.data.models.User,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSelect
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Student ID: ${user.studentId}",
                    style = MaterialTheme.typography.bodyMedium
                )
                // Fixed date formatting
                Text(
                    text = "Created: ${formatDate(user.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete User",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// Helper function for date formatting
private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}