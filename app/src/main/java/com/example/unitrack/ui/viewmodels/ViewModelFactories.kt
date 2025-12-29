// app/src/main/java/com/example/unitrack/ui/viewmodels/ViewModelFactories.kt
package com.example.unitrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.unitrack.data.repositories.GpaRepository
import kotlin.reflect.KClass

/**
 * Base sealed class for ViewModel factories to handle creation errors
 */
sealed class ViewModelCreationError(message: String) : IllegalStateException(message) {
    class UnknownViewModelClass(className: String) :
        ViewModelCreationError("Unknown ViewModel class: $className")
    class MissingDependencies(dependency: String) :
        ViewModelCreationError("Missing required dependency: $dependency")
}

/**
 * Generic ViewModelFactory that can create any ViewModel with a repository dependency
 */
class GenericViewModelFactory(
    private val repository: GpaRepository,
    private val viewModelClass: KClass<out ViewModel>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (!modelClass.isAssignableFrom(viewModelClass.java)) {
            throw ViewModelCreationError.UnknownViewModelClass(modelClass.name)
        }

        return when (modelClass) {
            UserViewModel::class.java -> UserViewModel(repository) as T
            SemesterViewModel::class.java -> SemesterViewModel(repository) as T
            SubjectViewModel::class.java -> SubjectViewModel(repository) as T
            AssignmentViewModel::class.java -> AssignmentViewModel(repository) as T
            LectureViewModel::class.java -> LectureViewModel(repository) as T
            else -> throw ViewModelCreationError.UnknownViewModelClass(modelClass.name)
        }
    }
}

/**
 * Individual factories for each ViewModel
 */
class UserViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            return UserViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SemesterViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SemesterViewModel::class.java)) {
            return SemesterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class SubjectViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubjectViewModel::class.java)) {
            return SubjectViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class AssignmentViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AssignmentViewModel::class.java)) {
            return AssignmentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class LectureViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LectureViewModel::class.java)) {
            return LectureViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * AppViewModelFactory that can create all ViewModels in the app
 */
class AppViewModelFactory(private val repository: GpaRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> {
                UserViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SemesterViewModel::class.java) -> {
                SemesterViewModel(repository) as T
            }
            modelClass.isAssignableFrom(SubjectViewModel::class.java) -> {
                SubjectViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AssignmentViewModel::class.java) -> {
                AssignmentViewModel(repository) as T
            }
            modelClass.isAssignableFrom(LectureViewModel::class.java) -> {
                LectureViewModel(repository) as T
            }
            else -> {
                throw ViewModelCreationError.UnknownViewModelClass(modelClass.name)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return create(modelClass)
    }
}