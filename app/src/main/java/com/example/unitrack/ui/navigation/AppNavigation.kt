// app/src/main/java/com/example/unitrack/ui/navigation/AppNavigation.kt
package com.example.unitrack.ui.navigation

//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavHostController
//import androidx.navigation.compose.NavHost
//import androidx.navigation.compose.composable
//import androidx.navigation.navArgument
//import com.example.unitrack.data.database.AppDatabase
//import com.example.unitrack.data.repositories.GpaRepository
//import com.example.unitrack.ui.screens.*
//import com.example.unitrack.ui.viewmodels.LectureViewModel
//
//@Composable
//fun AppNavigation(navController: NavHostController) {
//    val context = LocalContext.current
//
//    // Create ViewModel with repository
//    val lectureViewModel: LectureViewModel = viewModel(
//        factory = LectureViewModelFactory(
//            GpaRepository(
//                AppDatabase.getDatabase(context).userDao(),
//                AppDatabase.getDatabase(context).semesterDao(),
//                AppDatabase.getDatabase(context).subjectDao(),
//                AppDatabase.getDatabase(context).assignmentDao(),
//                AppDatabase.getDatabase(context).lectureDao()
//            )
//        )
//    )
//
//    NavHost(
//        navController = navController,
//        startDestination = "timetable"  // Use string directly
//    ) {
//        composable("timetable") {
//            TimetableScreen(
//                navController = navController,
//                userId = 1
//            )
//        }
//
//        composable(
//            route = "add_lecture/{dayOfWeek}?subjectId={subjectId}",
//            arguments = listOf(
//                navArgument("dayOfWeek") {
//                    type = androidx.navigation.NavType.IntType
//                    defaultValue = 1
//                },
//                navArgument("subjectId") {
//                    type = androidx.navigation.NavType.IntType
//                    nullable = true
//                }
//            )
//        ) { backStackEntry ->
//            val dayOfWeek = backStackEntry.arguments?.getInt("dayOfWeek") ?: 1
//            val subjectId = backStackEntry.arguments?.getInt("subjectId")
//
//            AddLectureScreen(
//                navController = navController,
//                dayOfWeek = dayOfWeek,
//                subjectId = subjectId
//            )
//        }
//    }
//}
//
//// ViewModel Factory
//class LectureViewModelFactory(
//    private val repository: GpaRepository
//) : androidx.lifecycle.ViewModelProvider.Factory {
//    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(LectureViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return LectureViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
//
//// OPTIONAL: You can keep this but fix it, or remove it and use strings directly
//object Screen {
//    const val Timetable = "timetable"
//    const val AddLecture = "add_lecture"
//}