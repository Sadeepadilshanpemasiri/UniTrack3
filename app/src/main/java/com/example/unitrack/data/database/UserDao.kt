// app/src/main/java/com/example/unitrack/data/database/UserDao.kt
package com.example.unitrack.data.database

import androidx.room.*
import com.example.unitrack.data.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM users ORDER BY name")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): Flow<User?>

    // Add this method - delete by user ID
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Int)

    // Remove this line - it doesn't belong in UserDao
    // suspend fun deleteSubjectsByUser(userId: Int)
}