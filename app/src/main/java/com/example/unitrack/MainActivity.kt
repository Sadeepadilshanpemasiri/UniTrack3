// app/src/main/java/com/example/unitrack/MainActivity.kt
package com.example.unitrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
        setContent {
            UniTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "user_selection"
    ) {
        composable("user_selection") {
            UserSelectionScreen(navController)
        }
        composable("home/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            userId?.let {
                HomeScreen(navController = navController, userId = it)
            }
        }
        composable("semester/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            userId?.let {
                SemesterScreen(navController = navController, userId = it)
            }
        }
        composable("subjects/{semesterId}") { backStackEntry ->
            val semesterId = backStackEntry.arguments?.getString("semesterId")?.toIntOrNull()
            semesterId?.let {
                SubjectScreen(navController = navController, semesterId = it)
            }
        }
        composable("add_subject/{semesterId}") { backStackEntry ->
            val semesterId = backStackEntry.arguments?.getString("semesterId")?.toIntOrNull()
            semesterId?.let {
                AddSubjectScreen(navController = navController, semesterId = it)
            }
        }

        // ========== ASSIGNMENT ROUTES ==========
        composable("assignments/{subjectId}") { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")?.toIntOrNull()
            subjectId?.let {
                AssignmentScreen(navController = navController, subjectId = it)
            }
        }

        composable("add_assignment/{subjectId}") { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")?.toIntOrNull()
            subjectId?.let {
                AddAssignmentScreen(navController = navController, subjectId = it)
            }
        }

        composable("assignment_detail/{assignmentId}") { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId")?.toIntOrNull()
            assignmentId?.let {
                AssignmentDetailScreen(navController = navController, assignmentId = it)
            }
        }

        composable("assignment_dashboard/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull()
            userId?.let {
                AssignmentDashboardScreen(navController = navController, userId = it)
            }
        }
    }
}