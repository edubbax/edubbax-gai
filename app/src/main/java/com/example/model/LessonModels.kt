package com.example.model

enum class LessonStatus(val label: String) {
    DRAFT("Qaralama"),
    SCHEDULED("Planlaşdırılmış"),
    LIVE("Canlı Dərs"),
    COMPLETED("Tamamlanmış")
}

enum class BatchStatus(val label: String) {
    PENDING("Gözləyir"),
    ACTIVE("Aktiv"),
    COMPLETED("Tamamlandı")
}

enum class SubjectCategory(val displayName: String, val iconName: String) {
    PHYSICS("Fizika", "science"),
    MATH("Riyaziyyat", "functions"),
    CHEMISTRY("Kimya", "biotech"),
    BIOLOGY("Biologiya", "eco"),
    COMPUTER_SCIENCE("İnformatika", "terminal"),
    HISTORY("Tarix", "history_edu"),
    ENGLISH("İngilis dili", "language")
}

data class QuizBatch(
    val id: String,
    val sequenceNumber: Int,              // 1, 2, or 3
    val scheduledOffsetMinutes: Int,      // e.g. 20, 40, 60 min
    val title: String,
    val status: BatchStatus = BatchStatus.PENDING,
    val questions: List<QuizQuestion> = emptyList(),
    val averageAccuracy: Int = 0,
    val totalSubmissions: Int = 0
)

data class Lesson(
    val id: String,
    val teacherId: String = "teacher_1",
    val title: String,
    val subject: String,
    val gradeLevel: String,
    val topicDescription: String = "",
    val questionFormat: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val durationMinutes: Int = 60,         // 45, 60, 90 min
    val numQuizzes: Int = 3,              // 1, 2, 3 batches
    val status: LessonStatus = LessonStatus.SCHEDULED,
    val uniqueLinkSlug: String = "edubbax.live/fiz-92",
    val pinCode: String = "839 214",
    val batches: List<QuizBatch> = emptyList(),
    val meaningfulInterventionsCount: Int = 0,
    val totalStudents: Int = 24,
    val averageScore: Int = 82,
    val createdAt: String = "Bugün"
)

data class FormativeInterventionEvent(
    val id: String,
    val batchId: String,
    val topicTag: String,
    val questionText: String,
    val errorRatePercent: Int,
    val commonMisconception: String,
    val suggestedPrompt: String,
    val isResolved: Boolean = false,
    val resolvedNote: String = ""
)

data class NorthStarInterventionMetric(
    val totalInterventions: Int = 42,
    val thisWeekInterventions: Int = 8,
    val topicMasteryGainPercent: Int = 34,
    val resolvedMisconceptionsCount: Int = 38
)

data class ParentReportSummary(
    val studentName: String,
    val lessonTitle: String,
    val date: String,
    val participationRate: String,
    val understandingScore: String,
    val strongTopics: List<String>,
    val reviewTopics: List<String>,
    val teacherAiNote: String
)
