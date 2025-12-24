// app/src/main/java/com/example/unitrack/data/models/Semester.kt
package com.example.unitrack.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "semesters",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Semester(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val year: Int, // 1, 2, 3, or 4
    val semesterNumber: Int, // 1 or 2
    val name: String,
    val gpa: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)