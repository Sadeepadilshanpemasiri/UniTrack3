// app/src/main/java/com/example/unitrack/ui/YourActualApp.kt
package com.example.unitrack.ui

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.unitrack.ui.screens.TimetableScreen
import com.example.unitrack.ui.screens.AddLectureScreen

@Composable
fun YourActualApp(navController: androidx.navigation.NavHostController) {
    // Start with just TimetableScreen first
    NavHost(
        navController = navController,
        startDestination = "actual_timetable/1"
    ) {
        // Start with ONLY timetable
        composable("actual_timetable/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")?.toIntOrNull() ?: 1
            TimetableScreen(
                navController = navController,
                userId = userId
            )
        }

        composable("actual_add_lecture/{dayOfWeek}") { backStackEntry ->
            val dayOfWeek = backStackEntry.arguments?.getString("dayOfWeek")?.toIntOrNull() ?: 1
            AddLectureScreen(
                navController = navController,
                dayOfWeek = dayOfWeek
            )
        }
    }
}