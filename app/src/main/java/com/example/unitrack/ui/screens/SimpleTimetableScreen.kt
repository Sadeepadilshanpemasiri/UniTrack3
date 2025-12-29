// app/src/main/java/com/example/unitrack/ui/screens/SimpleTimetableScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTimetableScreen(navController: NavController, userId: Int = 1) {
    // Use your actual Lecture model
    val lectures = remember {
        listOf(
            com.example.unitrack.data.models.Lecture(
                title = "Mathematics",
                userId = 1,
                subjectId = 1,
                dayOfWeek = 1, // Monday
                startTime = "09:00",
                endTime = "10:30",
                room = "Room 101",
                lecturer = "Dr. Smith",
                notificationEnabled = true
            ),
            com.example.unitrack.data.models.Lecture(
                title = "Physics",
                userId = 1,
                subjectId = 2,
                dayOfWeek = 2, // Tuesday
                startTime = "11:00",
                endTime = "12:30",
                room = "Lab 202",
                lecturer = "Prof. Johnson",
                notificationEnabled = true
            ),
            com.example.unitrack.data.models.Lecture(
                title = "Computer Science",
                userId = 1,
                subjectId = 3,
                dayOfWeek = 3, // Wednesday
                startTime = "14:00",
                endTime = "15:30",
                room = "Room 303",
                lecturer = "Dr. Williams",
                notificationEnabled = false
            )
        )
    }

    val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Weekly Timetable") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("add_lecture")
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Lecture")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_lecture") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Lecture")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Card
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
                            text = "This Week",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lectures.size} lectures scheduled",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "Schedule",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (lectures.isEmpty()) {
                // Show empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No lectures scheduled",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("add_lecture") }
                    ) {
                        Text("Add Your First Lecture")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        item {
                            DayScheduleCard(
                                dayName = day,
                                dayNumber = index + 1,
                                lectures = lectures.filter { it.dayOfWeek == index + 1 },
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DayScheduleCard(
    dayName: String,
    dayNumber: Int,
    lectures: List<com.example.unitrack.data.models.Lecture>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    text = dayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${lectures.size} lecture${if (lectures.size != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lectures.isEmpty()) {
                Text(
                    text = "No lectures scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lectures.forEach { lecture ->
                        LectureCard(lecture = lecture)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("add_lecture/$dayNumber")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Lecture")
            }
        }
    }
}

@Composable
fun LectureCard(lecture: com.example.unitrack.data.models.Lecture) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = lecture.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                if (lecture.notificationEnabled) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notification enabled",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${lecture.startTime} - ${lecture.endTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (lecture.room.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${lecture.room}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            if (lecture.lecturer.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "👨‍🏫 ${lecture.lecturer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}