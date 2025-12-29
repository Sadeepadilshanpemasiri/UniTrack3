// app/src/main/java/com/example/unitrack/ui/viewmodels/SubjectViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.Subject
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SubjectViewModel(private val repository: GpaRepository) : ViewModel() {
    fun getSubjectsBySemester(semesterId: Int): Flow<List<Subject>> {
        return repository.getSubjectsBySemester(semesterId)
    }

    suspend fun addSubject(
        semesterId: Int,
        name: String,
        creditValue: Int,
        grade: String,
        isCalculated: Boolean
    ) {
        val subject = Subject(
            semesterId = semesterId,
            name = name,
            creditValue = creditValue,
            grade = grade,
            isCalculated = isCalculated
        )
        repository.addSubject(subject)

        // Update semester GPA
        updateSemesterGPA(semesterId)
    }

    suspend fun deleteSubject(subject: Subject) {
        repository.deleteSubject(subject)

        // Update semester GPA
        updateSemesterGPA(subject.semesterId)
    }

    private suspend fun updateSemesterGPA(semesterId: Int) {
        val gpa = repository.calculateSemesterGPA(semesterId)
        val semester = repository.getSemesterById(semesterId)
        semester?.let {
            val updatedSemester = it.copy(gpa = gpa)
            repository.updateSemester(updatedSemester)
        }
    }
}