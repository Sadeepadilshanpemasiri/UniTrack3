// app/src/main/java/com/example/unitrack/data/database/SubjectDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.Subject
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert
    suspend fun insert(subject: Subject): Long

    @Update
    suspend fun update(subject: Subject)

    @Delete
    suspend fun delete(subject: Subject)

    @Query("SELECT * FROM subjects WHERE semesterId = :semesterId ORDER BY name")
    fun getSubjectsBySemester(semesterId: Int): Flow<List<Subject>>

    @Query("DELETE FROM subjects WHERE semesterId = :semesterId")
    suspend fun deleteSubjectsBySemester(semesterId: Int)
}