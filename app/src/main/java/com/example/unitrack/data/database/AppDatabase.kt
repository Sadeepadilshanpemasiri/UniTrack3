// app/src/main/java/com/example/unitrack/data/database/AppDatabase.kt
package com.example.unitrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.unitrack.data.models.User
import com.example.unitrack.data.models.Semester
import com.example.unitrack.data.models.Subject
import com.example.unitrack.data.models.Assignment

@Database(
    entities = [User::class, Semester::class, Subject::class, Assignment::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun semesterDao(): SemesterDao
    abstract fun subjectDao(): SubjectDao
    abstract fun assignmentDao(): AssignmentDao

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
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}