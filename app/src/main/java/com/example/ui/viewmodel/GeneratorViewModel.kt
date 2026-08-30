package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.GeminiGeneratorService
import com.example.model.BatchStatus
import com.example.model.BloomLevel
import com.example.model.DifficultyLevel
import com.example.model.Lesson
import com.example.model.LessonEntity
import com.example.model.LessonStatus
import com.example.model.QuestionEntity
import com.example.model.QuestionType
import com.example.model.Quiz
import com.example.model.QuizBatch
import com.example.model.QuizBatchEntity
import com.example.model.QuizOption
import com.example.model.QuizQuestion
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class GeneratorState {
    INPUT_CONFIG,
    GENERATING_AI,
    PREVIEW_RESULT
}

class GeneratorViewModel(
    private val geminiService: GeminiGeneratorService = GeminiGeneratorService(),
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {
    val topic = MutableStateFlow("Nyutonun Hərəkət Qanunları və Dinamika")
    val subject = MutableStateFlow("Fizika")
    val gradeLevel = MutableStateFlow("9-cu sinif")
    val questionFormat = MutableStateFlow(QuestionType.MULTIPLE_CHOICE)
    val durationMinutes = MutableStateFlow(60) // 45, 60, 90 min
    val numBatches = MutableStateFlow(3)       // 1, 2, 3 batches
    val difficulty = MutableStateFlow(DifficultyLevel.MEDIUM)
    val selectedBloomLevel = MutableStateFlow(BloomLevel.APPLY)
    val additionalNotes = MutableStateFlow("Dərsin gedişatında formativ müdaxilə üçün konseptual və qrafik suallar.")

    private val _generatorState = MutableStateFlow(GeneratorState.INPUT_CONFIG)
    val generatorState: StateFlow<GeneratorState> = _generatorState.asStateFlow()

    private val _aiProgressStep = MutableStateFlow("")
    val aiProgressStep: StateFlow<String> = _aiProgressStep.asStateFlow()

    private val _aiProgressValue = MutableStateFlow(0f)
    val aiProgressValue: StateFlow<Float> = _aiProgressValue.asStateFlow()

    private val _generatedLesson = MutableStateFlow<Lesson?>(null)
    val generatedLesson: StateFlow<Lesson?> = _generatedLesson.asStateFlow()

    private val _generatedQuiz = MutableStateFlow<Quiz?>(null)
    val generatedQuiz: StateFlow<Quiz?> = _generatedQuiz.asStateFlow()

    // Active batch index for review screen
    val selectedBatchIndex = MutableStateFlow(0)

    fun calculateBatchOffsets(): List<Int> {
        val total = durationMinutes.value
        val count = numBatches.value
        val interval = total / count
        return (1..count).map { it * interval }
    }

    fun startGeneration(onComplete: (Lesson) -> Unit = {}) {
        viewModelScope.launch {
            _generatorState.value = GeneratorState.GENERATING_AI

            _aiProgressStep.value = "Gemini AI modeli ilə əlaqə qurulur..."
            _aiProgressValue.value = 0.2f
            delay(400)

            _aiProgressStep.value = "Kurikulum və fənn standartları təhlil edilir..."
            _aiProgressValue.value = 0.45f

            val result = geminiService.generateBatchedLesson(
                topic = topic.value.ifBlank { "Yeni Mövzu" },
                subject = subject.value.ifBlank { "Fizika" },
                gradeLevel = gradeLevel.value,
                durationMinutes = durationMinutes.value,
                numBatches = numBatches.value,
                questionFormat = questionFormat.value.label,
                bloomLevel = selectedBloomLevel.value.name,
                additionalNotes = additionalNotes.value
            )

            _aiProgressStep.value = "Pedaqoji distraktorlar və Bloom səviyyələri formalaşdırılır..."
            _aiProgressValue.value = 0.8f
            delay(400)

            _aiProgressStep.value = "Dərs və vaxt batch-ləri tamamlanır..."
            _aiProgressValue.value = 0.95f
            delay(300)

            val rawData = result.getOrNull()
            val finalLesson = if (rawData != null) {
                mapGeminiResponseToLesson(rawData)
            } else {
                createFallbackLesson()
            }

            _generatedLesson.value = finalLesson
            _generatedQuiz.value = Quiz(
                id = finalLesson.id,
                title = finalLesson.title,
                subject = finalLesson.subject,
                gradeLevel = finalLesson.gradeLevel,
                description = finalLesson.topicDescription,
                difficulty = difficulty.value,
                questions = finalLesson.batches.flatMap { it.questions },
                createdAt = "Bugün",
                tags = listOf(finalLesson.subject, "AI Batch Dərs", "${finalLesson.durationMinutes} dəq")
            )

            _generatorState.value = GeneratorState.PREVIEW_RESULT
            onComplete(finalLesson)
        }
    }

    /**
     * Saves the reviewed lesson, batches, and questions directly into Firestore
     */
    fun saveLessonToFirestore(onSuccess: (Lesson) -> Unit, onError: (String) -> Unit = {}) {
        val lesson = _generatedLesson.value ?: return
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "teacher_demo"

        viewModelScope.launch {
            try {
                val lessonEntity = LessonEntity(
                    id = lesson.id,
                    teacherId = currentUserId,
                    subject = lesson.subject,
                    gradeLevel = lesson.gradeLevel,
                    topicDescription = lesson.topicDescription,
                    questionFormat = lesson.questionFormat.name,
                    durationMinutes = lesson.durationMinutes,
                    numQuizzes = lesson.numQuizzes,
                    status = "scheduled",
                    startedAt = null,
                    uniqueLinkSlug = lesson.uniqueLinkSlug,
                    pinCode = lesson.pinCode
                )

                val batchesPayload = lesson.batches.map { batch ->
                    val batchEntity = QuizBatchEntity(
                        id = batch.id,
                        lessonId = lesson.id,
                        sequenceNumber = batch.sequenceNumber,
                        scheduledOffsetMinutes = batch.scheduledOffsetMinutes,
                        status = "pending",
                        activatedAt = null
                    )

                    val questionsEntities = batch.questions.map { q ->
                        QuestionEntity(
                            id = q.id,
                            quizBatchId = batch.id,
                            questionText = q.text,
                            options = q.options.map { it.text },
                            correctAnswer = q.options.firstOrNull { it.isCorrect }?.text ?: q.options.firstOrNull()?.text ?: "",
                            topicTag = q.topicTag,
                            explanation = q.explanation,
                            points = q.points,
                            timeLimitSeconds = q.timeLimitSeconds
                        )
                    }
                    Pair(batchEntity, questionsEntities)
                }

                firestoreService.createLesson(lessonEntity, batchesPayload)
                onSuccess(lesson)
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Firestore xətası")
            }
        }
    }

    fun resetToConfig() {
        _generatorState.value = GeneratorState.INPUT_CONFIG
        _generatedLesson.value = null
        _generatedQuiz.value = null
        selectedBatchIndex.value = 0
    }

    fun updateBatchQuestionText(batchIdx: Int, qIdx: Int, newText: String) {
        val current = _generatedLesson.value ?: return
        val currentBatches = current.batches.toMutableList()
        if (batchIdx in currentBatches.indices) {
            val batch = currentBatches[batchIdx]
            val questions = batch.questions.toMutableList()
            if (qIdx in questions.indices) {
                questions[qIdx] = questions[qIdx].copy(text = newText)
                currentBatches[batchIdx] = batch.copy(questions = questions)
                _generatedLesson.value = current.copy(batches = currentBatches)
            }
        }
    }

    fun deleteBatchQuestion(batchIdx: Int, qIdx: Int) {
        val current = _generatedLesson.value ?: return
        val currentBatches = current.batches.toMutableList()
        if (batchIdx in currentBatches.indices) {
            val batch = currentBatches[batchIdx]
            val questions = batch.questions.toMutableList()
            if (qIdx in questions.indices) {
                questions.removeAt(qIdx)
                currentBatches[batchIdx] = batch.copy(questions = questions)
                _generatedLesson.value = current.copy(batches = currentBatches)
            }
        }
    }

    private fun mapGeminiResponseToLesson(res: com.example.data.GeneratedLessonResponse): Lesson {
        val randomPin = "${(100..999).random()} ${(100..999).random()}"
        val slug = "edubbax.live/${res.subject.take(3).lowercase()}-${(10..99).random()}"
        val lessonId = "les_" + UUID.randomUUID().toString().take(8)

        val batches = res.batches.mapIndexed { idx, b ->
            val batchId = "batch_${lessonId}_${idx + 1}"
            val questions = b.questions.mapIndexed { qIdx, q ->
                val correctText = q.correct_answer.trim()
                val options = q.options.mapIndexed { optIdx, optText ->
                    QuizOption(
                        id = "opt_${optIdx + 1}",
                        text = optText,
                        isCorrect = optText.trim() == correctText || optText.startsWith(correctText) || correctText.startsWith(optText.take(2))
                    )
                }
                QuizQuestion(
                    id = "q_${batchId}_${qIdx + 1}",
                    text = q.question_text,
                    type = questionFormat.value,
                    topicTag = q.topic_tag.ifBlank { res.subject },
                    bloomLevel = try { BloomLevel.valueOf(q.bloom_level) } catch (e: Exception) { selectedBloomLevel.value },
                    points = 10,
                    timeLimitSeconds = 30,
                    explanation = q.explanation,
                    options = options
                )
            }

            QuizBatch(
                id = batchId,
                sequenceNumber = b.batch_number,
                scheduledOffsetMinutes = b.scheduled_offset_minutes,
                title = b.batch_title,
                status = BatchStatus.PENDING,
                questions = questions
            )
        }

        return Lesson(
            id = lessonId,
            title = res.lesson_title,
            subject = res.subject,
            gradeLevel = res.grade_level,
            topicDescription = res.topic_description,
            questionFormat = questionFormat.value,
            durationMinutes = durationMinutes.value,
            numQuizzes = res.batches.size,
            status = LessonStatus.SCHEDULED,
            uniqueLinkSlug = slug,
            pinCode = randomPin,
            batches = batches,
            meaningfulInterventionsCount = 0,
            totalStudents = 0,
            averageScore = 0,
            createdAt = "Bugün"
        )
    }

    private fun createFallbackLesson(): Lesson {
        val slugPrefix = when (subject.value.lowercase()) {
            "fizika" -> "fiz"
            "riyaziyyat" -> "riy"
            "kimya" -> "kim"
            "biologiya" -> "bio"
            else -> "edu"
        }
        val randomSlug = "$slugPrefix-${(10..99).random()}"
        val randomPin = "${(100..999).random()} ${(100..999).random()}"
        val lessonId = "les_" + UUID.randomUUID().toString().take(8)

        val offsets = calculateBatchOffsets()
        val batches = (1..numBatches.value).map { idx ->
            val offsetMin = offsets.getOrElse(idx - 1) { idx * (durationMinutes.value / numBatches.value) }
            val batchId = "batch_${lessonId}_$idx"
            val questions = listOf(
                QuizQuestion(
                    id = "q_${batchId}_1",
                    text = "${topic.value} mövzusu üzrə əsas qanun və ya qayda necə ifadə olunur?",
                    type = questionFormat.value,
                    topicTag = "${subject.value} Konsepti",
                    bloomLevel = selectedBloomLevel.value,
                    points = 10,
                    timeLimitSeconds = 30,
                    explanation = "${topic.value} mövzusunun əsas prinsiplərinə əsaslanır.",
                    options = listOf(
                        QuizOption("o1", "A) İlkin bərabərlik", false),
                        QuizOption("o2", "B) Doğru Fundamental Qayda", true),
                        QuizOption("o3", "C) Əks mütənasiblik", false),
                        QuizOption("o4", "D) Sabit vəziyyət", false)
                    )
                )
            )

            QuizBatch(
                id = batchId,
                sequenceNumber = idx,
                scheduledOffsetMinutes = offsetMin,
                title = "Batch $idx ($offsetMin. dəqiqə): Formativ Qiymətləndirmə",
                status = BatchStatus.PENDING,
                questions = questions
            )
        }

        return Lesson(
            id = lessonId,
            title = topic.value,
            subject = subject.value,
            gradeLevel = gradeLevel.value,
            topicDescription = additionalNotes.value,
            questionFormat = questionFormat.value,
            durationMinutes = durationMinutes.value,
            numQuizzes = numBatches.value,
            status = LessonStatus.SCHEDULED,
            uniqueLinkSlug = "edubbax.live/$randomSlug",
            pinCode = randomPin,
            batches = batches,
            meaningfulInterventionsCount = 0,
            totalStudents = 0,
            averageScore = 0,
            createdAt = "Bugün"
        )
    }
}
