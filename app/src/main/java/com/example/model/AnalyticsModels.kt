package com.example.model

data class BloomStat(
    val level: BloomLevel,
    val percentage: Float,
    val questionCount: Int,
    val avgScore: Int
)

data class TopicMastery(
    val topicName: String,
    val masteryPercent: Int,
    val totalAttempts: Int,
    val trendDelta: Int
)

data class DifficultQuestion(
    val questionText: String,
    val quizTitle: String,
    val errorRate: Int,
    val commonWrongAnswer: String
)

data class TeacherAnalytics(
    val totalQuizzesCreated: Int = 24,
    val totalStudentsEngaged: Int = 348,
    val activeLiveSessionsCount: Int = 18,
    val overallAverageScore: Int = 78,
    val bloomStats: List<BloomStat> = emptyList(),
    val topicMasteries: List<TopicMastery> = emptyList(),
    val difficultQuestions: List<DifficultQuestion> = emptyList()
)

data class TeacherProfile(
    val fullName: String = "Dr. Samir Məmmədov",
    val title: String = "Baş Müəllim & Təhsil Tədqiqatçısı",
    val school: String = "Bakı Avropa Liseyi / Fizika-Riyaziyyat",
    val email: String = "samir.m@edubbax.edu.az",
    val planType: String = "EdubbaX Pro AI Plan",
    val aiCreditsRemaining: Int = 450,
    val aiCreditsTotal: Int = 500,
    val quizzesCount: Int = 28,
    val studentsCount: Int = 342,
    val averageEngagement: String = "94.2%"
)
