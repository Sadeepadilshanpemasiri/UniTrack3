// app/src/main/java/com/example/unitrack/ui/viewmodels/SemesterViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.Semester
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SemesterViewModel(private val repository: GpaRepository) : ViewModel() {
    fun getSemestersByUser(userId: Int): Flow<List<Semester>> {
        return repository.getSemestersByUser(userId)
    }

    fun addSemester(userId: Int, year: Int, semesterNumber: Int, name: String) {
        viewModelScope.launch {
            // Check if semester already exists
            val existing = repository.getSemesterByDetails(userId, year, semesterNumber)
            if (existing == null) {
                val semester = Semester(
                    userId = userId,
                    year = year,
                    semesterNumber = semesterNumber,
                    name = name
                )
                repository.addSemester(semester)
            } else {
                // Update existing semester name
                val updated = existing.copy(name = name)
                repository.updateSemester(updated)
            }
        }
    }
}