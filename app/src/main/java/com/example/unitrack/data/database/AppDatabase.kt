// app/src/main/java/com/example/unitrack/data/database/AppDatabase.kt
package com.example.unitrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.unitrack.data.models.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Semester::class,
        Subject::class,
        Assignment::class,
        Lecture::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun semesterDao(): SemesterDao
    abstract fun subjectDao(): SubjectDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun lectureDao(): LectureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unitrack_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context)) // Add callback for async initialization
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Initialize database on background thread
            CoroutineScope(Dispatchers.IO).launch {
                // Pre-populate with sample data if needed
                prePopulateDatabase(context)
            }
        }
    }
}

// Optional: Pre-populate with sample data
private suspend fun prePopulateDatabase(context: Context) {
    val database = AppDatabase.getDatabase(context)
    // Add a default user if no users exist
    val userCount = database.userDao().getAllUsers()
    // Just get count, don't collect flow here
}