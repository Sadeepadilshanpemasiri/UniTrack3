// app/src/main/java/com/example/unitrack/data/repositories/GpaRepository.kt
package com.example.unitrack.data.repositories

import com.example.unitrack.data.database.*
import com.example.unitrack.data.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class GpaRepository(
    private val userDao: UserDao,
    private val semesterDao: SemesterDao,
    private val subjectDao: SubjectDao,
    private val assignmentDao: AssignmentDao,
    private val lectureDao: LectureDao
) {
    // ========== LECTURE OPERATIONS ==========
    suspend fun insertLecture(lecture: Lecture): Long = lectureDao.insert(lecture)
    suspend fun updateLecture(lecture: Lecture) = lectureDao.update(lecture)
    suspend fun deleteLecture(lecture: Lecture) = lectureDao.delete(lecture)

    fun getLecturesBySubject(subjectId: Int): Flow<List<Lecture>> =
        lectureDao.getLecturesBySubject(subjectId)

    fun getLecturesByUser(userId: Int): Flow<List<Lecture>> =
        lectureDao.getLecturesByUser(userId)

    fun getLecturesByDay(userId: Int, dayOfWeek: Int): Flow<List<Lecture>> =
        lectureDao.getLecturesByDay(userId, dayOfWeek)

    fun getLecturesWithNotificationsForUser(userId: Int): Flow<List<Lecture>> =
        lectureDao.getLecturesWithNotificationsForUser(userId)

    // Get today's lectures for a user
    suspend fun getTodayLectures(userId: Int): List<Lecture> {
        val calendar = Calendar.getInstance()
        val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> 7
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 1
        }
        return getLecturesByDay(userId, dayOfWeek).first()
    }

    // Existing User operations...
    suspend fun addUser(user: User): Long = userDao.insert(user)
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    fun getUserById(userId: Int): Flow<User?> = userDao.getUserById(userId)

    // Existing Semester operations...
    suspend fun addSemester(semester: Semester): Long = semesterDao.insert(semester)
    fun getSemestersByUser(userId: Int): Flow<List<Semester>> = semesterDao.getSemestersByUser(userId)
    suspend fun updateSemester(semester: Semester) = semesterDao.update(semester)
    suspend fun getSemesterById(semesterId: Int): Semester? = semesterDao.getSemesterById(semesterId)
    suspend fun getSemesterByDetails(userId: Int, year: Int, semesterNumber: Int): Semester? =
        semesterDao.getSemester(userId, year, semesterNumber)

    // Existing User deletion methods...
    suspend fun deleteUser(user: User) {
        // First delete user's lectures
        deleteLecturesByUser(user.id)
        // Then delete user's subjects (cascade)
        deleteSubjectsByUser(user.id)
        // Then delete user's semesters
        deleteSemestersByUser(user.id)
        // Finally delete the user
        userDao.delete(user)
    }

    suspend fun deleteUserById(userId: Int) {
        // Delete all related data first
        deleteLecturesByUser(userId)
        deleteSubjectsByUser(userId)
        deleteSemestersByUser(userId)
        userDao.deleteUserById(userId)
    }

    private suspend fun deleteLecturesByUser(userId: Int) {
        // Get all lectures for this user and delete them
        val lectures = getLecturesByUser(userId).first()
        lectures.forEach { lecture ->
            lectureDao.delete(lecture)
        }
    }

    private suspend fun deleteSubjectsByUser(userId: Int) {
        // Get all semesters for this user
        val semesters = semesterDao.getSemestersByUser(userId).first()
        // Delete subjects for each semester
        semesters.forEach { semester ->
            subjectDao.deleteSubjectsBySemester(semester.id)
        }
    }

    private suspend fun deleteSemestersByUser(userId: Int) {
        semesterDao.deleteSemestersByUser(userId)
    }

    // Existing Subject operations...
    suspend fun addSubject(subject: Subject): Long = subjectDao.insert(subject)
    fun getSubjectsBySemester(semesterId: Int): Flow<List<Subject>> = subjectDao.getSubjectsBySemester(semesterId)
    suspend fun updateSubject(subject: Subject) = subjectDao.update(subject)
    suspend fun deleteSubject(subject: Subject) = subjectDao.delete(subject)

    // Get subjects by user
    fun getSubjectsByUser(userId: Int): Flow<List<Subject>> = subjectDao.getSubjectsByUser(userId)

    // Existing GPA Calculation...
    suspend fun calculateSemesterGPA(semesterId: Int): Double {
        val subjects = getSubjectsBySemester(semesterId).first()
        val calculatedSubjects = subjects.filter { it.isCalculated }

        if (calculatedSubjects.isEmpty()) return 0.0

        var totalPoints = 0.0
        var totalCredits = 0

        calculatedSubjects.forEach { subject ->
            totalPoints += subject.gradeToPoints() * subject.creditValue
            totalCredits += subject.creditValue
        }

        return if (totalCredits > 0) {
            val gpa = totalPoints / totalCredits
            String.format("%.2f", gpa).toDouble()
        } else 0.0
    }

    suspend fun calculateOverallGPA(userId: Int): Double {
        val semesters = getSemestersByUser(userId).first()
        var totalPoints = 0.0
        var totalCredits = 0

        semesters.forEach { semester ->
            val subjects = getSubjectsBySemester(semester.id).first()
            val calculatedSubjects = subjects.filter { it.isCalculated }

            calculatedSubjects.forEach { subject ->
                totalPoints += subject.gradeToPoints() * subject.creditValue
                totalCredits += subject.creditValue
            }
        }

        return if (totalCredits > 0) {
            val gpa = totalPoints / totalCredits
            String.format("%.2f", gpa).toDouble()
        } else 0.0
    }

    // Existing Assignment operations...
    suspend fun addAssignment(assignment: Assignment): Long = assignmentDao.insert(assignment)
    suspend fun updateAssignment(assignment: Assignment) = assignmentDao.update(assignment)
    suspend fun deleteAssignment(assignment: Assignment) = assignmentDao.delete(assignment)
    fun getAssignmentsBySubject(subjectId: Int): Flow<List<Assignment>> = assignmentDao.getAssignmentsBySubject(subjectId)
    fun getAllPendingAssignments(): Flow<List<Assignment>> = assignmentDao.getAllPendingAssignments()
    fun getAssignmentsBySemester(semesterId: Int): Flow<List<Assignment>> = assignmentDao.getAssignmentsBySemester(semesterId)
    fun getAssignmentsByUser(userId: Int): Flow<List<Assignment>> = assignmentDao.getAllAssignmentsByUser(userId)
    suspend fun getAssignmentById(assignmentId: Int): Assignment? = assignmentDao.getAssignmentById(assignmentId)

    fun getAllLecturesForUser(userId: Int): Flow<List<Lecture>> =
        lectureDao.getAllLecturesForUser(userId)

    suspend fun getAssignmentsDueSoon(days: Int = 7): List<Assignment> {
        val now = System.currentTimeMillis()
        val future = now + (days * 24 * 60 * 60 * 1000L)
        return assignmentDao.getAssignmentsBetweenDates(now, future).first()
    }

    suspend fun getTodayAssignments(): List<Assignment> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000)
        return assignmentDao.getAssignmentsBetweenDates(startOfDay, endOfDay).first()
    }

    suspend fun completeAssignment(assignmentId: Int, marks: Float? = null) {
        val assignment = assignmentDao.getAssignmentById(assignmentId)
        assignment?.let {
            val updated = it.copy(
                status = "completed",
                completionDate = System.currentTimeMillis(),
                obtainedMarks = marks
            )
            assignmentDao.update(updated)
        }
    }

    suspend fun getAssignmentStats(subjectId: Int): AssignmentStats {
        val completed = assignmentDao.getCompletedCountBySubject(subjectId)
        val pending = assignmentDao.getPendingCountBySubject(subjectId)
        val averageMarks = assignmentDao.getAverageMarksBySubject(subjectId)

        return AssignmentStats(
            totalAssignments = completed + pending,
            completed = completed,
            pending = pending,
            averageMarks = averageMarks ?: 0f,
            completionRate = if (completed + pending > 0) {
                (completed.toFloat() / (completed + pending)) * 100
            } else 0f
        )
    }

    suspend fun getUserAssignmentStats(userId: Int): AssignmentStats {
        val allAssignments = getAssignmentsByUser(userId).first()
        val completed = allAssignments.count { it.status == "completed" }
        val pending = allAssignments.count { it.status != "completed" }
        val averageMarks = allAssignments
            .filter { it.obtainedMarks != null }
            .mapNotNull { it.obtainedMarks }
            .average()
            .toFloat()

        return AssignmentStats(
            totalAssignments = allAssignments.size,
            completed = completed,
            pending = pending,
            averageMarks = if (averageMarks.isNaN()) 0f else averageMarks,
            completionRate = if (allAssignments.isNotEmpty()) {
                (completed.toFloat() / allAssignments.size) * 100
            } else 0f
        )
    }

    suspend fun markOverdueAssignments() {
        assignmentDao.markOverdueAssignments(System.currentTimeMillis())
    }

    suspend fun updateAssignmentTotalMarks(assignmentId: Int, newTotalMarks: Float) {
        val assignment = assignmentDao.getAssignmentById(assignmentId)
        assignment?.let {
            val updated = it.copy(totalMarks = newTotalMarks)
            assignmentDao.update(updated)
        }
    }

    data class AssignmentStats(
        val totalAssignments: Int,
        val completed: Int,
        val pending: Int,
        val averageMarks: Float,
        val completionRate: Float
    )
}