// app/src/main/java/com/example/unitrack/ui/viewmodels/LectureViewModel.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.unitrack.data.models.Lecture
import com.example.unitrack.data.repositories.GpaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LectureViewModel(private val repository: GpaRepository) : ViewModel() {
    private val _lectures = MutableStateFlow<List<Lecture>>(emptyList())
    val lectures: StateFlow<List<Lecture>> = _lectures.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadLectures(userId = 1)
    }

    fun loadLectures(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                repository.getLecturesByUser(userId).collect { lecturesList ->
                    _lectures.value = lecturesList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load lectures: ${e.message}"
                _lectures.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun addLecture(lecture: Lecture): Boolean {
        return try {
            repository.insertLecture(lecture)
            loadLectures(lecture.userId)
            true
        } catch (e: Exception) {
            _errorMessage.value = "Failed to add lecture: ${e.message}"
            false
        }
    }

    fun refresh(userId: Int) {
        loadLectures(userId)
    }
}