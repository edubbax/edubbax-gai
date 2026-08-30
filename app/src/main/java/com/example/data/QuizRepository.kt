package com.example.data

import com.example.model.BatchStatus
import com.example.model.DifficultyLevel
import com.example.model.FormativeInterventionEvent
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.NorthStarInterventionMetric
import com.example.model.Quiz
import com.example.model.QuizBatch
import com.example.model.QuizQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QuizRepository {
    private val _quizzes = MutableStateFlow<List<Quiz>>(SampleData.sampleQuizzes)
    val quizzes: StateFlow<List<Quiz>> = _quizzes.asStateFlow()

    private val _lessons = MutableStateFlow<List<Lesson>>(SampleData.sampleLessons)
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _interventions = MutableStateFlow<List<FormativeInterventionEvent>>(SampleData.sampleInterventions)
    val interventions: StateFlow<List<FormativeInterventionEvent>> = _interventions.asStateFlow()

    private val _northStarMetric = MutableStateFlow<NorthStarInterventionMetric>(SampleData.northStarMetric)
    val northStarMetric: StateFlow<NorthStarInterventionMetric> = _northStarMetric.asStateFlow()

    fun getQuizById(id: String): Quiz? {
        return _quizzes.value.find { it.id == id }
    }

    fun addQuiz(quiz: Quiz) {
        _quizzes.value = listOf(quiz) + _quizzes.value
    }

    fun updateQuiz(updated: Quiz) {
        _quizzes.value = _quizzes.value.map { if (it.id == updated.id) updated else it }
    }

    fun deleteQuiz(id: String) {
        _quizzes.value = _quizzes.value.filter { it.id != id }
    }

    fun toggleFavorite(id: String) {
        _quizzes.value = _quizzes.value.map {
            if (it.id == id) it.copy(isFavorite = !it.isFavorite) else it
        }
    }

    // Lesson management
    fun addLesson(lesson: Lesson) {
        _lessons.value = listOf(lesson) + _lessons.value
    }

    fun updateLesson(updated: Lesson) {
        _lessons.value = _lessons.value.map { if (it.id == updated.id) updated else it }
    }

    fun resolveIntervention(interventionId: String, note: String) {
        _interventions.value = _interventions.value.map {
            if (it.id == interventionId) {
                it.copy(isResolved = true, resolvedNote = note)
            } else it
        }
        // Increment North Star metric
        _northStarMetric.value = _northStarMetric.value.copy(
            totalInterventions = _northStarMetric.value.totalInterventions + 1,
            thisWeekInterventions = _northStarMetric.value.thisWeekInterventions + 1,
            resolvedMisconceptionsCount = _northStarMetric.value.resolvedMisconceptionsCount + 1
        )
    }
}

