// app/src/main/java/com/example/unitrack/data/database/AssignmentDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.Assignment
import kotlinx.coroutines.flow.Flow
import java.util.*


@Dao
interface AssignmentDao {
    @Insert
    suspend fun insert(assignment: Assignment): Long

    @Update
    suspend fun update(assignment: Assignment)

    @Delete
    suspend fun delete(assignment: Assignment)

    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId ORDER BY dueDate")
    fun getAssignmentsBySubject(subjectId: Int): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE subjectId IN " +
            "(SELECT id FROM subjects WHERE semesterId = :semesterId) " +
            "ORDER BY dueDate")
    fun getAssignmentsBySemester(semesterId: Int): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE subjectId IN " +
            "(SELECT id FROM subjects WHERE semesterId IN " +
            "(SELECT id FROM semesters WHERE userId = :userId)) " +
            "ORDER BY dueDate")
    fun getAllAssignmentsByUser(userId: Int): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE status != 'completed' ORDER BY dueDate")
    fun getAllPendingAssignments(): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :start AND :end ORDER BY dueDate")
    fun getAssignmentsBetweenDates(start: Long, end: Long): Flow<List<Assignment>>

    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
    suspend fun getAssignmentById(assignmentId: Int): Assignment?

    @Query("SELECT COUNT(*) FROM assignments WHERE subjectId = :subjectId AND status = 'completed'")
    suspend fun getCompletedCountBySubject(subjectId: Int): Int

    @Query("SELECT COUNT(*) FROM assignments WHERE subjectId = :subjectId AND status != 'completed'")
    suspend fun getPendingCountBySubject(subjectId: Int): Int

    @Query("SELECT AVG(obtainedMarks) FROM assignments WHERE subjectId = :subjectId AND obtainedMarks IS NOT NULL")
    suspend fun getAverageMarksBySubject(subjectId: Int): Float?

    @Query("UPDATE assignments SET status = 'overdue' WHERE dueDate < :currentTime AND status != 'completed'")
    suspend fun markOverdueAssignments(currentTime: Long)
}