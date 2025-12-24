// app/src/main/java/com/example/unitrack/data/models/Subject.kt
package com.example.unitrack.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subjects",
    foreignKeys = [
        ForeignKey(
            entity = Semester::class,
            parentColumns = ["id"],
            childColumns = ["semesterId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["semesterId"])]
)
data class Subject(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val semesterId: Int,
    val name: String,
    val creditValue: Int,
    val grade: String, // A+, A, A-, B+, B, B-, C+, C, C-, D+, D, E, F
    val isCalculated: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun gradeToPoints(): Double {
        return when (grade.uppercase()) {
            "A+" -> 4.0
            "A" -> 4.0
            "A-" -> 3.7
            "B+" -> 3.3
            "B" -> 3.0
            "B-" -> 2.7
            "C+" -> 2.3
            "C" -> 2.0
            "C-" -> 1.7
            "D+" -> 1.3
            "D" -> 1.0
            "E" -> 0.0
            "F" -> 0.0
            else -> 0.0
        }
    }
}