// app/src/main/java/com/example/unitrack/data/models/User.kt
package com.example.unitrack.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val studentId: String,
    val university: String = "My University",
    val createdAt: Long = System.currentTimeMillis()
)