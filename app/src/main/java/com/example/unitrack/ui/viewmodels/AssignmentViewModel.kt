// app/src/main/java/com/example/unitrack/ui/viewmodels/AssignmentViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.Assignment
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AssignmentViewModel(private val repository: GpaRepository) : ViewModel() {
    fun getAssignmentsBySubject(subjectId: Int): Flow<List<Assignment>> {
        return repository.getAssignmentsBySubject(subjectId)
    }

    fun getAllPendingAssignments(): Flow<List<Assignment>> {
        return repository.getAllPendingAssignments()
    }

    fun getAssignmentsBySemester(semesterId: Int): Flow<List<Assignment>> {
        return repository.getAssignmentsBySemester(semesterId)
    }

    suspend fun getAssignmentById(assignmentId: Int): Assignment? {
        return repository.getAssignmentById(assignmentId)
    }

    fun addAssignment(
        subjectId: Int,
        title: String,
        description: String,
        dueDate: Long,
        priority: Int,
        estimatedTimeHours: Int,
        totalMarks: Float = 100f
    ) {
        viewModelScope.launch {
            val assignment = Assignment(
                subjectId = subjectId,
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                estimatedTimeHours = estimatedTimeHours,
                totalMarks = totalMarks
            )
            repository.addAssignment(assignment)
        }
    }

    fun updateAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.updateAssignment(assignment)
        }
    }

    fun deleteAssignment(assignment: Assignment) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun completeAssignment(assignmentId: Int, marks: Float? = null) {
        viewModelScope.launch {
            repository.completeAssignment(assignmentId, marks)
        }
    }

    suspend fun getAssignmentStats(subjectId: Int): GpaRepository.AssignmentStats {
        return repository.getAssignmentStats(subjectId)
    }

    suspend fun getTodayAssignments(): List<Assignment> {
        return repository.getTodayAssignments()
    }

    suspend fun getAssignmentsDueSoon(days: Int = 7): List<Assignment> {
        return repository.getAssignmentsDueSoon(days)
    }
}