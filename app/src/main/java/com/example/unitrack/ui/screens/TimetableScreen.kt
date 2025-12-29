// app/src/main/java/com/example/unitrack/ui/screens/TimetableScreen.kt
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
fun TimetableScreen(navController: NavController, userId: Int = 1) {
    // Days of week
    val daysOfWeek = listOf(
        "Monday", "Tuesday", "Wednesday", "Thursday",
        "Friday", "Saturday", "Sunday"
    )

    // Sample lectures for each day
    val lecturesByDay = remember {
        mapOf(
            1 to listOf( // Monday
                LectureData("Mathematics", "09:00", "10:30", "Room 101", "Dr. Smith", true),
                LectureData("Physics", "11:00", "12:30", "Lab 202", "Prof. Johnson", true),
            ),
            2 to listOf( // Tuesday
                LectureData("Computer Science", "10:00", "11:30", "Room 303", "Dr. Williams", false),
            ),
            3 to listOf( // Wednesday
                LectureData("Chemistry", "14:00", "15:30", "Lab 105", "Dr. Brown", true),
            ),
            4 to listOf(), // Thursday - No lectures
            5 to listOf( // Friday
                LectureData("Biology", "13:00", "14:30", "Lab 107", "Dr. Davis", true),
            ),
            6 to listOf(), // Saturday - No lectures
            7 to listOf(), // Sunday - No lectures
        )
    }

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
                    IconButton(
                        onClick = { navController.navigate("add_lecture/1") }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Lecture")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add_lecture/1") }
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
                        val totalLectures = lecturesByDay.values.sumOf { it.size }
                        Text(
                            text = "$totalLectures lectures scheduled",
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

            // Day Cards
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                daysOfWeek.forEachIndexed { index, day ->
                    item {
                        DayScheduleCard(
                            dayName = day,
                            dayNumber = index + 1,
                            lectures = lecturesByDay[index + 1] ?: emptyList(),
                            navController = navController
                        )
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
    lectures: List<LectureData>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
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
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Badge(
                    containerColor = if (lectures.isNotEmpty())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (lectures.isNotEmpty())
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Text(lectures.size.toString())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lectures.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No lectures scheduled",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { navController.navigate("add_lecture/$dayNumber") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Lecture")
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    lectures.forEach { lecture ->
                        LectureCard(lecture = lecture)
                    }

                    Button(
                        onClick = { navController.navigate("add_lecture/$dayNumber") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add More")
                    }
                }
            }
        }
    }
}

@Composable
fun LectureCard(lecture: LectureData) {
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
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                if (lecture.hasNotification) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Notification",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Badge(
                    containerColor = Color(0xFF2196F3).copy(alpha = 0.2f),
                    contentColor = Color(0xFF2196F3)
                ) {
                    Text("${lecture.startTime} - ${lecture.endTime}")
                }

                if (lecture.room.isNotBlank()) {
                    Badge(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        contentColor = Color(0xFF4CAF50)
                    ) {
                        Text(lecture.room)
                    }
                }
            }

            if (lecture.lecturer.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Lecturer: ${lecture.lecturer}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Simple data class
data class LectureData(
    val title: String,
    val startTime: String,
    val endTime: String,
    val room: String,
    val lecturer: String,
    val hasNotification: Boolean
)