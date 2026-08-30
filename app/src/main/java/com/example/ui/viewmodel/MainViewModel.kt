package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.QuizRepository
import com.example.data.SampleData
import com.example.model.FormativeInterventionEvent
import com.example.model.Lesson
import com.example.model.NorthStarInterventionMetric
import com.example.model.Quiz
import com.example.model.TeacherProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppDestination(val title: String, val icon: String) {
    DASHBOARD("Dərslər", "dashboard"),
    AI_GENERATOR("AI Tərtib", "auto_awesome"),
    REVIEW_BATCHES("Nəzərdən Keçir", "rate_review"),
    LESSON_LOBBY("Dərs Linki", "qr_code"),
    LIVE_SESSION("Canlı Dərs", "podium"),
    STUDENT_MODE("Şagird Görünüşü", "school"),
    LIBRARY("Bank", "library"),
    ANALYTICS("Analiz", "analytics"),
    PROFILE("Profil", "person"),
    AUTH("Giriş", "login")
}

class MainViewModel(
    private val repository: QuizRepository = QuizRepository()
) : ViewModel() {

    private val _currentDestination = MutableStateFlow(AppDestination.DASHBOARD)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedQuizId = MutableStateFlow<String?>(null)
    val selectedQuizId: StateFlow<String?> = _selectedQuizId.asStateFlow()

    private val _selectedLessonId = MutableStateFlow<String?>(null)
    val selectedLessonId: StateFlow<String?> = _selectedLessonId.asStateFlow()

    private val _profile = MutableStateFlow(SampleData.teacherProfile)
    val profile: StateFlow<TeacherProfile> = _profile.asStateFlow()

    val quizzes: StateFlow<List<Quiz>> = repository.quizzes
    val lessons: StateFlow<List<Lesson>> = repository.lessons
    val interventions: StateFlow<List<FormativeInterventionEvent>> = repository.interventions
    val northStarMetric: StateFlow<NorthStarInterventionMetric> = repository.northStarMetric

    // Active lesson being edited / viewed
    val activeEditingLesson = MutableStateFlow<Lesson?>(null)

    fun navigateTo(destination: AppDestination) {
        _currentDestination.value = destination
    }

    fun openQuizDetail(quizId: String) {
        _selectedQuizId.value = quizId
    }

    fun closeQuizDetail() {
        _selectedQuizId.value = null
    }

    fun openLesson(lessonId: String) {
        _selectedLessonId.value = lessonId
    }

    fun closeLesson() {
        _selectedLessonId.value = null
    }

    fun toggleFavorite(quizId: String) {
        repository.toggleFavorite(quizId)
    }

    fun deleteQuiz(quizId: String) {
        repository.deleteQuiz(quizId)
    }

    fun saveNewQuiz(quiz: Quiz) {
        repository.addQuiz(quiz)
    }

    fun saveNewLesson(lesson: Lesson) {
        repository.addLesson(lesson)
    }

    fun updateLesson(lesson: Lesson) {
        repository.updateLesson(lesson)
    }

    fun resolveIntervention(id: String, note: String) {
        repository.resolveIntervention(id, note)
    }
}
