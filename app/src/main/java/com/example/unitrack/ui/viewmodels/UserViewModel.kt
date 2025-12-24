// app/src/main/java/com/example/unitrack/ui/viewmodels/UserViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.User
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: GpaRepository) : ViewModel() {
    val allUsers: Flow<List<User>> = repository.getAllUsers()

    fun addUser(name: String, studentId: String) {
        viewModelScope.launch {
            val user = User(name = name, studentId = studentId)
            repository.addUser(user)
        }
    }

    // Add this method to delete user
    fun deleteUser(user: User) {
        viewModelScope.launch {
            repository.deleteUser(user)
        }
    }

    // Alternative: Delete by ID
    fun deleteUserById(userId: Int) {
        viewModelScope.launch {
            repository.deleteUserById(userId)
        }
    }
}