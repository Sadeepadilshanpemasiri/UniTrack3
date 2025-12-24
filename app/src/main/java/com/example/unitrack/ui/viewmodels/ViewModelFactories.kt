// app/src/main/java/com/example/unitrack/ui/viewmodels/ViewModelFactories.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.unitrack.data.repositories.GpaRepository

class UserViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SemesterViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SemesterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SemesterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SubjectViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AssignmentViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignmentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}