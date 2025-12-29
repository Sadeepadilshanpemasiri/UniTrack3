// app/src/main/java/com/example/unitrack/data/database/AssignmentDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.Assignment
import kotlinx.coroutines.flow.Flow
import java.util.*

//@Dao
//interface AssignmentDao {
//    @Insert
//    suspend fun insert(assignment: Assignment): Long
//
//    @Update
//    suspend fun update(assignment: Assignment)
//
//    @Delete
//    suspend fun delete(assignment: Assignment)
//
//    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId ORDER BY dueDate ASC")
//    fun getAssignmentsBySubject(subjectId: Int): Flow<List<Assignment>>
//
//    @Query("SELECT * FROM assignments WHERE id = :assignmentId")
//    suspend fun getAssignmentById(assignmentId: Int): Assignment?
//
//    @Query("SELECT * FROM assignments WHERE status = :status ORDER BY dueDate ASC")
//    fun getAssignmentsByStatus(status: String): Flow<List<Assignment>>
//
//    @Query("SELECT * FROM assignments WHERE dueDate BETWEEN :startDate AND :endDate ORDER BY priority DESC, dueDate ASC")
//    fun getAssignmentsBetweenDates(startDate: Long, endDate: Long): Flow<List<Assignment>>
//
//    @Query("SELECT * FROM assignments WHERE status != 'completed' ORDER BY priority DESC, dueDate ASC")
//    fun getAllPendingAssignments(): Flow<List<Assignment>>
//
//    @Query("SELECT * FROM assignments WHERE subjectId IN (SELECT id FROM subjects WHERE semesterId = :semesterId) ORDER BY dueDate ASC")
//    fun getAssignmentsBySemester(semesterId: Int): Flow<List<Assignment>>
//
//    @Query("DELETE FROM assignments WHERE subjectId = :subjectId")
//    suspend fun deleteAssignmentsBySubject(subjectId: Int)
//
//    @Query("SELECT COUNT(*) FROM assignments WHERE status = 'completed' AND subjectId = :subjectId")
//    suspend fun getCompletedCountBySubject(subjectId: Int): Int
//
//    @Query("SELECT COUNT(*) FROM assignments WHERE status != 'completed' AND subjectId = :subjectId")
//    suspend fun getPendingCountBySubject(subjectId: Int): Int
//
//    @Query("SELECT AVG(obtainedMarks) FROM assignments WHERE status = 'completed' AND subjectId = :subjectId AND obtainedMarks IS NOT NULL")
//    suspend fun getAverageMarksBySubject(subjectId: Int): Float?
//
//    @Query("UPDATE assignments SET status = 'overdue' WHERE dueDate < :currentTime AND status = 'pending'")
//    suspend fun markOverdueAssignments(currentTime: Long)
//
//    // In AssignmentDao.kt, add this method inside the interface:
//    @Query("""
//    SELECT a.* FROM assignments a
//    JOIN subjects s ON a.subjectId = s.id
//    JOIN semesters sem ON s.semesterId = sem.id
//    WHERE sem.userId = :userId
//    ORDER BY a.dueDate ASC
//""")
//    fun getAllAssignmentsByUser(userId: Int): Flow<List<Assignment>>
//}


// app/src/main/java/com/example/unitrack/data/database/AssignmentDao.kt


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