// app/src/main/java/com/example/unitrack/ui/screens/AssignmentDashboardScreen.kt
package com.example.unitrack.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.repositories.GpaRepository
import com.example.unitrack.ui.components.CountdownTimer
import com.example.unitrack.ui.viewmodels.AssignmentViewModel
import com.example.unitrack.ui.viewmodels.AssignmentViewModelFactory
import com.example.unitrack.ui.viewmodels.SemesterViewModel
import com.example.unitrack.ui.viewmodels.SemesterViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentDashboardScreen(navController: NavController, userId: Int) {
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

    val assignmentViewModel: AssignmentViewModel = viewModel(
        factory = AssignmentViewModelFactory(repository)
    )

    val semesterViewModel: SemesterViewModel = viewModel(
        factory = SemesterViewModelFactory(repository)
    )

    val allPendingAssignments by assignmentViewModel.getAssignmentsByUser(userId).collectAsState(emptyList())
    val semesters by semesterViewModel.getSemestersByUser(userId).collectAsState(emptyList())

    val coroutineScope = rememberCoroutineScope()

    var todayAssignments by remember { mutableStateOf<List<com.example.unitrack.data.models.Assignment>>(emptyList()) }
    var upcomingAssignments by remember { mutableStateOf<List<com.example.unitrack.data.models.Assignment>>(emptyList()) }
    var overdueAssignments by remember { mutableStateOf<List<com.example.unitrack.data.models.Assignment>>(emptyList()) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") }

    // Calculate assignments based on filter
    val filteredAssignments = remember(allPendingAssignments, selectedFilter) {
        when (selectedFilter) {
            "overdue" -> allPendingAssignments.filter { it.isOverdue() }
            "today" -> allPendingAssignments.filter { it.isDueToday() }
            "week" -> allPendingAssignments.filter {
                !it.isOverdue() && it.daysRemaining() in 0..7
            }
            "month" -> allPendingAssignments.filter {
                !it.isOverdue() && it.daysRemaining() in 0..30
            }
            else -> allPendingAssignments // "all"
        }
    }

    LaunchedEffect(allPendingAssignments) {
        coroutineScope.launch {
            todayAssignments = assignmentViewModel.getTodayAssignments()
            upcomingAssignments = assignmentViewModel.getAssignmentsDueSoon(7)
            overdueAssignments = allPendingAssignments.filter { it.isOverdue() }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Text("Assignment Dashboard")
                        Text(
                            text = "${filteredAssignments.size} assignments",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Show filter icon with badge if filter is active
                    BadgedBox(
                        badge = {
                            if (selectedFilter != "all") {
                                Badge {
                                    Text(selectedFilter.take(1).uppercase())
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showFilterDialog = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter")
                        }
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
            // Summary Stats Cards - Updated to use filtered assignments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filteredOverdue = filteredAssignments.count { it.isOverdue() }
                val filteredToday = filteredAssignments.count { it.isDueToday() && !it.isOverdue() }
                val filteredWeek = filteredAssignments.count {
                    !it.isOverdue() && !it.isDueToday() && it.daysRemaining() in 0..7
                }

                SummaryCard(
                    title = "Overdue",
                    count = filteredOverdue,
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Warning,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "Today",
                    count = filteredToday,
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Today,
                    modifier = Modifier.weight(1f)
                )

                SummaryCard(
                    title = "This Week",
                    count = filteredWeek,
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.CalendarViewWeek,
                    modifier = Modifier.weight(1f)
                )
            }

            // Quick Stats - Updated to use filtered assignments
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatItem(
                        value = filteredAssignments.size.toString(),
                        label = "Total",
                        icon = Icons.Default.Assignment
                    )

                    StatItem(
                        value = calculateAvgTimeRemaining(filteredAssignments),
                        label = "Avg Time Left",
                        icon = Icons.Default.Schedule
                    )

                    StatItem(
                        value = calculateCompletionRate(filteredAssignments),
                        label = "On Track",
                        icon = Icons.Default.TrendingUp
                    )
                }
            }

            if (filteredAssignments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FilterAlt,
                            contentDescription = "No filtered assignments",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = when (selectedFilter) {
                                "overdue" -> "No overdue assignments"
                                "today" -> "No assignments due today"
                                "week" -> "No assignments due this week"
                                "month" -> "No assignments due this month"
                                else -> "No pending assignments"
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Text(
                            text = when (selectedFilter) {
                                "overdue" -> "Great! You have no overdue assignments."
                                "today" -> "No assignments are due today."
                                "week" -> "No assignments due within the next 7 days."
                                "month" -> "No assignments due within the next 30 days."
                                else -> "Great job! You're all caught up."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        if (selectedFilter != "all") {
                            Button(
                                onClick = {
                                    selectedFilter = "all"
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Show All Assignments")
                            }
                        } else {
                            Button(
                                onClick = {
                                    if (semesters.isNotEmpty()) {
                                        val latestSemester = semesters.maxByOrNull { it.createdAt }
                                        latestSemester?.let {
                                            navController.navigate("subjects/${it.id}")
                                        }
                                    }
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text("Add New Assignment")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Show filtered assignments in appropriate sections
                    val overdue = filteredAssignments.filter { it.isOverdue() }
                    val today = filteredAssignments.filter { it.isDueToday() && !it.isOverdue() }
                    val upcoming = filteredAssignments.filter {
                        !it.isOverdue() && !it.isDueToday() && it.daysRemaining() in 0..7
                    }
                    val future = filteredAssignments.filter {
                        !it.isOverdue() && !it.isDueToday() && it.daysRemaining() > 7
                    }

                    // Overdue Section
                    if (overdue.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "⚠️ OVERDUE ASSIGNMENTS",
                                count = overdue.size,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        items(overdue) { assignment ->
                            DashboardAssignmentCard(
                                assignment = assignment,
                                onClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                }
                            )
                        }
                    }

                    // Today's Assignments
                    if (today.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📅 TODAY'S ASSIGNMENTS",
                                count = today.size,
                                color = Color(0xFFFF9800)
                            )
                        }
                        items(today) { assignment ->
                            DashboardAssignmentCard(
                                assignment = assignment,
                                onClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                }
                            )
                        }
                    }

                    // Upcoming Assignments (Next 7 days)
                    if (upcoming.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "📋 UPCOMING (NEXT 7 DAYS)",
                                count = upcoming.size,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(upcoming) { assignment ->
                            DashboardAssignmentCard(
                                assignment = assignment,
                                onClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                }
                            )
                        }
                    }

                    // Future Assignments
                    if (future.isNotEmpty()) {
                        item {
                            SectionHeader(
                                title = "🔮 FUTURE ASSIGNMENTS",
                                count = future.size,
                                color = Color(0xFF2196F3)
                            )
                        }
                        items(future) { assignment ->
                            DashboardAssignmentCard(
                                assignment = assignment,
                                onClick = {
                                    navController.navigate("assignment_detail/${assignment.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Filter Dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = "Filter",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Filter Assignments")
                }
            },
            text = {
                Column {
                    listOf(
                        Pair("all", "All Assignments"),
                        Pair("overdue", "Overdue Only"),
                        Pair("today", "Today Only"),
                        Pair("week", "This Week"),
                        Pair("month", "This Month")
                    ).forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedFilter = value },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFilter == value,
                                onClick = { selectedFilter = value }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFilterDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showFilterDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SummaryCard(
    title: String,
    count: Int,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StatItem(
    value: String,
    label: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SectionHeader(title: String, count: Int, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.weight(1f)
        )
        Badge(
            containerColor = color.copy(alpha = 0.2f),
            contentColor = color
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DashboardAssignmentCard(
    assignment: com.example.unitrack.data.models.Assignment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (assignment.isOverdue()) MaterialTheme.colorScheme.errorContainer
            else if (assignment.isDueToday()) Color(0xFFFFF3E0)
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

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Badge(
                            containerColor = Color(android.graphics.Color.parseColor(assignment.getPriorityColor()))
                        ) {
                            Text(
                                text = assignment.getPriorityText(),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }

                        if (assignment.isOverdue()) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error
                            ) {
                                Text(
                                    text = "OVERDUE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        } else if (assignment.isDueToday()) {
                            Badge(
                                containerColor = Color(0xFFFF9800)
                            ) {
                                Text(
                                    text = "TODAY",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Time indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            color = when {
                                assignment.daysRemaining() < 0 -> MaterialTheme.colorScheme.error
                                assignment.daysRemaining() == 0L -> Color(0xFFFF9800)
                                assignment.daysRemaining() < 3 -> Color(0xFFFFC107)
                                else -> MaterialTheme.colorScheme.primary
                            }.copy(alpha = 0.1f),
                            shape = MaterialTheme.shapes.medium
                        )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Math.abs(assignment.daysRemaining()).toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                assignment.daysRemaining() < 0 -> MaterialTheme.colorScheme.error
                                assignment.daysRemaining() == 0L -> Color(0xFFFF9800)
                                assignment.daysRemaining() < 3 -> Color(0xFFFFC107)
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Text(
                            text = if (assignment.daysRemaining() < 0) "days late" else "days left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress and time
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
                        text = "⏱️ ${assignment.estimatedTimeHours}h • ${assignment.getTimeRemainingText()}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Quick action button
                if (assignment.isOverdue()) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Overdue",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Mini countdown for today/tomorrow assignments
            if (assignment.isDueToday() || assignment.isDueTomorrow()) {
                Spacer(modifier = Modifier.height(8.dp))
                CountdownTimer(
                    dueDate = assignment.dueDate,
                    modifier = Modifier.fillMaxWidth(),
                    showDays = false
                )
            }
        }
    }
}

// Helper functions - Updated to accept List parameter
private fun calculateAvgTimeRemaining(assignments: List<com.example.unitrack.data.models.Assignment>): String {
    if (assignments.isEmpty()) return "0d"

    val notOverdueAssignments = assignments.filterNot { it.isOverdue() }
    if (notOverdueAssignments.isEmpty()) return "0d"

    val avgDays = notOverdueAssignments
        .map { it.daysRemaining() }
        .average()

    return if (avgDays.isNaN()) "0d" else "%.1fd".format(avgDays)
}

private fun calculateCompletionRate(assignments: List<com.example.unitrack.data.models.Assignment>): String {
    if (assignments.isEmpty()) return "100%"

    val notOverdue = assignments.count { !it.isOverdue() }
    val percentage = (notOverdue.toFloat() / assignments.size * 100)

    // Format to 1 decimal place with % sign
    return "%.1f%%".format(percentage)
}