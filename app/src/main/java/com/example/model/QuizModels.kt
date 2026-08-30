package com.example.model

enum class DifficultyLevel(val label: String) {
    EASY("Asan"),
    MEDIUM("Orta"),
    HARD("Çətin"),
    EXPERT("Ekspert")
}

enum class QuestionType(val label: String) {
    MULTIPLE_CHOICE("Çoxseçimli"),
    TRUE_FALSE("Doğru / Yanlış"),
    OPEN_ENDED("Açıq sual"),
    FILL_BLANK("Boşluğu doldur")
}

enum class BloomLevel(val title: String, val levelNumber: Int) {
    REMEMBER("Xatırlama", 1),
    UNDERSTAND("Anlama", 2),
    APPLY("Tətbiq", 3),
    ANALYZE("Təhlil", 4),
    EVALUATE("Dəyərləndirmə", 5),
    CREATE("Yaratma", 6)
}

enum class QuizFormat(val label: String, val iconName: String) {
    MCQ("Çoxseçimli Test", "mcq"),
    OPEN_ENDED("Açıq tipli", "text"),
    MIXED("Qarışıq format", "mixed"),
    TIMED_BLITZ("Sürətli Blitz", "timer")
}

data class QuizOption(
    val id: String,
    val text: String,
    val isCorrect: Boolean
)

data class QuizQuestion(
    val id: String,
    val text: String,
    val type: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<QuizOption> = emptyList(),
    val explanation: String = "",
    val topicTag: String = "Əsas anlayışlar",
    val bloomLevel: BloomLevel = BloomLevel.APPLY,
    val points: Int = 10,
    val timeLimitSeconds: Int = 30
)

data class Quiz(
    val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: String,
    val description: String = "",
    val difficulty: DifficultyLevel = DifficultyLevel.MEDIUM,
    val questions: List<QuizQuestion> = emptyList(),
    val totalPlays: Int = 0,
    val averageScore: Int = 0,
    val createdAt: String = "Bugün",
    val isDraft: Boolean = false,
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList()
)
