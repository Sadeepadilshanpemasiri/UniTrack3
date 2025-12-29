// app/src/main/java/com/example/unitrack/ui/screens/HomeScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userId: Int = 1) {
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
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Welcome Card
            WelcomeCard()

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Stats Row
            QuickStatsRow()

            Spacer(modifier = Modifier.height(8.dp))

            // Main Actions Grid
            Text(
                text = "Quick Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 4.dp)
            )

            MainActionsGrid(navController = navController, userId = userId)

            Spacer(modifier = Modifier.height(8.dp))

            // Upcoming Section
            UpcomingSection(navController = navController, userId = userId)
        }
    }
}

@Composable
fun WelcomeCard() {
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
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Welcome back, Student! 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ready to manage your academic journey?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Profile icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
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
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today's Lectures
        StatCard(
            title = "Today",
            value = "3",
            unit = "Lectures",
            icon = Icons.Default.Schedule,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        // Due Assignments
        StatCard(
            title = "Due",
            value = "2",
            unit = "Assignments",
            icon = Icons.Default.Assignment,
            color = Color(0xFFFF9800),
            modifier = Modifier.weight(1f)
        )

        // GPA
        StatCard(
            title = "GPA",
            value = "3.75",
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
fun UpcomingSection(navController: NavController, userId: Int) {
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

                TextButton(
                    onClick = { navController.navigate("timetable/$userId") }
                ) {
                    Text("View All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sample upcoming items
            UpcomingItem(
                time = "09:00 AM",
                title = "Mathematics Lecture",
                location = "Room 101",
                type = "lecture"
            )

            UpcomingItem(
                time = "02:00 PM",
                title = "Physics Assignment",
                location = "Due Today",
                type = "assignment"
            )

            UpcomingItem(
                time = "04:30 PM",
                title = "Computer Lab",
                location = "Lab Building",
                type = "lab"
            )
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
                contentAlignment = Alignment.Center
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