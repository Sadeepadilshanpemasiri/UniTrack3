// app/src/main/java/com/example/unitrack/data/database/LectureDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.Lecture
import kotlinx.coroutines.flow.Flow

@Dao
interface LectureDao {
    @Insert
    suspend fun insert(lecture: Lecture): Long

    @Update
    suspend fun update(lecture: Lecture)

    @Delete
    suspend fun delete(lecture: Lecture)

    @Query("SELECT * FROM lectures WHERE subjectId = :subjectId ORDER BY dayOfWeek, startTime")
    fun getLecturesBySubject(subjectId: Int): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE userId = :userId ORDER BY dayOfWeek, startTime")
    fun getLecturesByUser(userId: Int): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE dayOfWeek = :dayOfWeek AND userId = :userId ORDER BY startTime")
    fun getLecturesByDay(userId: Int, dayOfWeek: Int): Flow<List<Lecture>>

    @Query("SELECT * FROM lectures WHERE notificationEnabled = 1 AND userId = :userId")
    fun getLecturesWithNotificationsForUser(userId: Int): Flow<List<Lecture>>

    // In LectureDao.kt
    @Query("SELECT * FROM lectures WHERE userId = :userId ORDER BY dayOfWeek, startTime")
    fun getAllLecturesForUser(userId: Int): Flow<List<Lecture>>
}