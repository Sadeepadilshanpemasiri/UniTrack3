// app/src/main/java/com/example/unitrack/data/RepositoryFactory.kt
package com.example.unitrack.data

import android.content.Context
import com.example.unitrack.data.database.AppDatabase
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RepositoryFactory {
    private var repository: GpaRepository? = null

    fun getRepository(context: Context): GpaRepository {
        return repository ?: synchronized(this) {
            // Initialize database on background thread
            val database = AppDatabase.getDatabase(context.applicationContext)
            GpaRepository(
                database.userDao(),
                database.semesterDao(),
                database.subjectDao(),
                database.assignmentDao(),
                database.lectureDao()
            ).also { repository = it }
        }
    }
}