// app/src/main/java/com/example/unitrack/MainActivity.kt
package com.example.unitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unitrack.ui.screens.*
import com.example.unitrack.ui.theme.UniTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UniTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UniTrackApp()
                }
            }
        }
    }
}

@Composable
fun UniTrackApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "launch"  // Start with launch screen
    ) {
        // Launch/Splash Screen
        composable("launch") {
            LaunchScreen(navController = navController)
        }

        // User Selection
        composable("users") {
            UserSelectionScreen(navController = navController)
        }

        // Main Dashboard
        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            HomeScreen(navController = navController, userId = userId)
        }

        // Semester Management
        composable("semester/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            SemesterScreen(navController = navController, userId = userId)
        }

        // Subject Management
        composable("subjects/{semesterId}") { backStackEntry ->
            val semesterId = backStackEntry.arguments?.getString("semesterId")?.toIntOrNull() ?: 1
            SubjectScreen(navController = navController, semesterId = semesterId)
        }

        // Add Subject
        composable("add_subject/{semesterId}") { backStackEntry ->
            val semesterId = backStackEntry.arguments?.getString("semesterId")?.toIntOrNull() ?: 1
            AddSubjectScreen(navController = navController, semesterId = semesterId)
        }

        // Add Assignment (corrected from AssignmentScreen)
        composable("add_assignment/{subjectId}") { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")?.toIntOrNull() ?: 1
            AddAssignmentScreen(navController = navController, subjectId = subjectId)
        }

        // Assignment Detail
        composable("assignment_detail/{assignmentId}") { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId")?.toIntOrNull() ?: 1
            AssignmentDetailScreen(navController = navController, assignmentId = assignmentId)
        }

        // Assignment Dashboard
        composable("assignment_dashboard/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            AssignmentDashboardScreen(navController = navController, userId = userId)
        }

        // Timetable
        composable("timetable/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            TimetableScreen(navController = navController, userId = userId)
        }

        // Add Lecture
        composable("add_lecture/{dayOfWeek}") { backStackEntry ->
            val dayOfWeek = backStackEntry.arguments?.getString("dayOfWeek")?.toIntOrNull() ?: 1
            AddLectureScreen(navController = navController, dayOfWeek = dayOfWeek)
        }
    }
}