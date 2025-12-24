// app/src/main/java/com/example/unitrack/data/database/SemesterDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.Semester
import kotlinx.coroutines.flow.Flow

@Dao
interface SemesterDao {
    @Insert
    suspend fun insert(semester: Semester): Long

    @Update
    suspend fun update(semester: Semester)

    @Delete
    suspend fun delete(semester: Semester)

    @Query("SELECT * FROM semesters WHERE userId = :userId ORDER BY year, semesterNumber")
    fun getSemestersByUser(userId: Int): Flow<List<Semester>>

    @Query("SELECT * FROM semesters WHERE id = :semesterId")
    suspend fun getSemesterById(semesterId: Int): Semester?

    @Query("SELECT * FROM semesters WHERE userId = :userId AND year = :year AND semesterNumber = :semesterNumber")
    suspend fun getSemester(userId: Int, year: Int, semesterNumber: Int): Semester?

    // Add this method for deleting semesters by user
    @Query("DELETE FROM semesters WHERE userId = :userId")
    suspend fun deleteSemestersByUser(userId: Int)
}