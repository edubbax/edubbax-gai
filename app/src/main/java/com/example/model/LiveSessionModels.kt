package com.example.model

enum class LiveSessionStatus {
    WAITING_ROOM,
    QUESTION_ACTIVE,
    QUESTION_RESULTS,
    LEADERBOARD,
    SESSION_FINISHED
}

data class LiveStudent(
    val id: String,
    val name: String,
    val avatarRes: Int? = null,
    val score: Int = 0,
    val streak: Int = 0,
    val isAnswered: Boolean = false,
    val isCorrect: Boolean = false,
    val responseTimeMs: Long = 0L,
    val isOnline: Boolean = true
)

data class LiveQuestionStat(
    val optionIndex: Int,
    val count: Int,
    val percentage: Float,
    val isCorrect: Boolean
)

data class LiveSession(
    val id: String,
    val pinCode: String,
    val quizTitle: String,
    val subject: String,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 5,
    val status: LiveSessionStatus = LiveSessionStatus.WAITING_ROOM,
    val connectedStudents: List<LiveStudent> = emptyList(),
    val currentTimerSeconds: Int = 30,
    val maxTimerSeconds: Int = 30,
    val questionStats: List<LiveQuestionStat> = emptyList()
)
