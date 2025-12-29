// app/src/main/java/com/example/unitrack/ui/screens/HomeScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.unitrack.data.RepositoryFactory
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userId: Int = 1) {
    val context = LocalContext.current
    val repository = remember { RepositoryFactory.getRepository(context) }
    val coroutineScope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("Student") }
    var todayLecturesCount by remember { mutableStateOf(0) }
    var dueAssignmentsCount by remember { mutableStateOf(0) }
    var currentGPA by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var todayUpcoming by remember { mutableStateOf<List<com.example.unitrack.data.models.Lecture>>(emptyList()) }
    var todayAssignments by remember { mutableStateOf<List<com.example.unitrack.data.models.Assignment>>(emptyList()) }

    LaunchedEffect(userId) {
        coroutineScope.launch {
            try {
                // Get user name - FIXED: Properly collect the flow
                repository.getUserById(userId).collect { user ->
                    userName = user?.name ?: "Student"
                }

                // Get today's lectures
                val todayLectures = repository.getTodayLectures(userId)
                todayLecturesCount = todayLectures.size
                todayUpcoming = todayLectures

                // Get today's assignments
                val todayAssignmentsList = repository.getTodayAssignments()
                dueAssignmentsCount = todayAssignmentsList.size
                todayAssignments = todayAssignmentsList

                // Calculate current GPA
                currentGPA = repository.calculateOverallGPA(userId)

            } catch (e: Exception) {
                // Use default values if there's an error
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "UniTrack",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    // Add switch user button
                    IconButton(
                        onClick = {
                            navController.navigate("users") {
                                popUpTo("home/{userId}") { inclusive = false }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Switch User"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Card
            item {
                WelcomeCard(userName = userName)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Quick Stats Row
            item {
                QuickStatsRow(
                    todayLectures = todayLecturesCount,
                    dueAssignments = dueAssignmentsCount,
                    currentGPA = currentGPA,
                    isLoading = isLoading
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Main Actions Grid
            item {
                Text(
                    text = "Quick Access",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                MainActionsGrid(navController = navController, userId = userId)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Upcoming Today Section
            item {
                val allUpcoming = todayUpcoming.map { lecture ->
                    UpcomingItemData(
                        time = formatTimeFrom12Hour(lecture.startTime),
                        title = lecture.title,
                        location = lecture.room,
                        type = "lecture",
                        isAssignment = false
                    )
                } + todayAssignments.map { assignment ->
                    UpcomingItemData(
                        time = formatTimeFromTimestamp(assignment.dueDate),
                        title = assignment.title,
                        location = "Due Today",
                        type = "assignment",
                        isAssignment = true
                    )
                }

                // Sort by time
                val sortedUpcoming = allUpcoming.sortedBy { it.time }

                UpcomingSection(
                    upcomingItems = sortedUpcoming,
                    navController = navController,
                    userId = userId
                )
            }

            // Add some bottom padding
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun WelcomeCard(userName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Welcome back, $userName! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ready to manage your academic journey?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Profile icon - Centered properly
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = "Profile",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickStatsRow(
    todayLectures: Int,
    dueAssignments: Int,
    currentGPA: Double,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's Lectures
        StatCard(
            title = "Today",
            value = if (isLoading) "..." else todayLectures.toString(),
            unit = "Lectures",
            icon = Icons.Default.Schedule,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // Due Assignments
        StatCard(
            title = "Due",
            value = if (isLoading) "..." else dueAssignments.toString(),
            unit = "Assignments",
            icon = Icons.Default.Assignment,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )

        // GPA
        StatCard(
            title = "GPA",
            value = if (isLoading) "..." else String.format("%.2f", currentGPA),
            unit = "Current",
            icon = Icons.Default.TrendingUp,
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(16.dp)
                )
            }

            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun MainActionsGrid(navController: NavController, userId: Int) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Timetable Card
            ActionCard(
                title = "Timetable",
                icon = Icons.Default.Schedule,
                color = Color(0xFF2196F3),
                onClick = { navController.navigate("timetable/$userId") },
                modifier = Modifier.weight(1f)
            )

            // Assignments Card
            ActionCard(
                title = "Assignments",
                icon = Icons.Default.Assignment,
                color = Color(0xFFFF9800),
                onClick = { navController.navigate("assignment_dashboard/$userId") },
                modifier = Modifier.weight(1f)
            )
        }

        // Second row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Subjects Card
            ActionCard(
                title = "Subjects",
                icon = Icons.Default.School,
                color = Color(0xFF4CAF50),
                onClick = { navController.navigate("semester/$userId") },
                modifier = Modifier.weight(1f)
            )

            // Lectures Card
            ActionCard(
                title = "Lectures",
                icon = Icons.Default.Class,
                color = Color(0xFF9C27B0),
                onClick = { navController.navigate("timetable/$userId") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f),
            contentColor = color
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = "Go to $title",
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun UpcomingSection(
    upcomingItems: List<UpcomingItemData>,
    navController: NavController,
    userId: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Upcoming Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (upcomingItems.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            if (upcomingItems.any { it.isAssignment }) {
                                navController.navigate("assignment_dashboard/$userId")
                            } else {
                                navController.navigate("timetable/$userId")
                            }
                        }
                    ) {
                        Text("View All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (upcomingItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = "No upcoming items",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Nothing scheduled for today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "You're all caught up!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // Show only first 3 items
                val itemsToShow = upcomingItems.take(3)
                itemsToShow.forEach { item ->
                    UpcomingItem(
                        time = item.time,
                        title = item.title,
                        location = item.location,
                        type = item.type
                    )
                }

                if (upcomingItems.size > 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${upcomingItems.size - 3} more items...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(start = 68.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingItem(
    time: String,
    title: String,
    location: String,
    type: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time circle
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = time,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Details
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = location,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Type indicator
        Badge(
            containerColor = when (type) {
                "lecture" -> Color(0xFF2196F3)
                "assignment" -> Color(0xFFFF9800)
                "lab" -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }.copy(alpha = 0.2f),
            contentColor = when (type) {
                "lecture" -> Color(0xFF2196F3)
                "assignment" -> Color(0xFFFF9800)
                "lab" -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }
        ) {
            Text(
                text = when (type) {
                    "lecture" -> "Lecture"
                    "assignment" -> "Assignment"
                    "lab" -> "Lab"
                    else -> type
                },
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

// Helper data class for upcoming items
data class UpcomingItemData(
    val time: String,
    val title: String,
    val location: String,
    val type: String,
    val isAssignment: Boolean
)

// Helper function to format time from timestamp
private fun formatTimeFromTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    return formatTimeFrom24Hour(hour, minute)
}

// Helper function to format 24-hour time to 12-hour format
private fun formatTimeFrom24Hour(hour: Int, minute: Int): String {
    val hour12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val amPm = if (hour < 12) "AM" else "PM"
    val minuteStr = String.format("%02d", minute)

    return "$hour12:$minuteStr $amPm"
}

// Helper function to format 12-hour time string (HH:MM) to display format
private fun formatTimeFrom12Hour(timeString: String): String {
    return try {
        val parts = timeString.split(":")
        if (parts.size >= 2) {
            val hour = parts[0].toInt()
            val minute = parts[1].toInt()
            formatTimeFrom24Hour(hour, minute)
        } else {
            timeString
        }
    } catch (e: Exception) {
        timeString
    }
}