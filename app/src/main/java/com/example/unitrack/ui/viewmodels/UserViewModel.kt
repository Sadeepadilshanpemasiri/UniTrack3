// app/src/main/java/com/example/unitrack/ui/viewmodels/UserViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.User
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UserViewModel(private val repository: GpaRepository) : ViewModel() {
    val allUsers: Flow<List<User>> = repository.getAllUsers()

    fun getUserById(userId: Int): Flow<User?> {
        return repository.getUserById(userId)
    }

    fun addUser(name: String, studentId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = User(name = name, studentId = studentId)
            repository.addUser(user)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUser(user)
        }
    }

    fun deleteUserById(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteUserById(userId)
        }
    }
}