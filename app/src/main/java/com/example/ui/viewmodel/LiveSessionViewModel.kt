package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FirestoreService
import com.example.data.SampleData
import com.example.model.BatchStatus
import com.example.model.FormativeInterventionEvent
import com.example.model.Lesson
import com.example.model.LiveQuestionStat
import com.example.model.LiveSession
import com.example.model.LiveSessionStatus
import com.example.model.LiveStudent
import com.example.model.Quiz
import com.example.model.ResponseEntity
import com.example.model.StudentEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LiveSessionViewModel(
    private val firestoreService: FirestoreService = FirestoreService()
) : ViewModel() {

    private val _activeLesson = MutableStateFlow<Lesson>(SampleData.sampleLessons.first())
    val activeLesson: StateFlow<Lesson> = _activeLesson.asStateFlow()

    private val _session = MutableStateFlow(SampleData.initialLiveSession)
    val session: StateFlow<LiveSession> = _session.asStateFlow()

    // Lesson timeline minute (e.g. 0 min to 60 min)
    private val _lessonCurrentMinute = MutableStateFlow(38)
    val lessonCurrentMinute: StateFlow<Int> = _lessonCurrentMinute.asStateFlow()

    private val _activeBatchIndex = MutableStateFlow(1) // 0: Batch 1, 1: Batch 2, 2: Batch 3
    val activeBatchIndex: StateFlow<Int> = _activeBatchIndex.asStateFlow()

    // Formative Intervention active popup
    private val _activeIntervention = MutableStateFlow<FormativeInterventionEvent?>(SampleData.sampleInterventions.firstOrNull())
    val activeIntervention: StateFlow<FormativeInterventionEvent?> = _activeIntervention.asStateFlow()

    private val _meaningfulInterventionsCount = MutableStateFlow(4)
    val meaningfulInterventionsCount: StateFlow<Int> = _meaningfulInterventionsCount.asStateFlow()

    // Student interactive view state (for previewing student perspective)
    private val _studentSelectedOptionId = MutableStateFlow<String?>(null)
    val studentSelectedOptionId: StateFlow<String?> = _studentSelectedOptionId.asStateFlow()

    private val _studentHasSubmitted = MutableStateFlow(false)
    val studentHasSubmitted: StateFlow<Boolean> = _studentHasSubmitted.asStateFlow()

    private var currentStudentId: String = ""
    private var currentStudentName: String = ""

    private var timerJob: Job? = null
    private var lessonClockJob: Job? = null
    private var firestoreSyncJob: Job? = null

    init {
        startLessonClock()
    }

    /**
     * Dərsin cari vaxt axını (hər 15 saniyədən bir dərs vaxtı 1 dəqiqə irəliləyir)
     * Və təyin olunmuş vaxt batch-inə çatdıqda avtomatik bildiriş/aktivləşmə təmin edilir
     */
    private fun startLessonClock() {
        lessonClockJob?.cancel()
        lessonClockJob = viewModelScope.launch {
            while (true) {
                delay(15000)
                if (_lessonCurrentMinute.value < _activeLesson.value.durationMinutes) {
                    val nextMin = _lessonCurrentMinute.value + 1
                    _lessonCurrentMinute.value = nextMin

                    // Vaxt batch yoxlanışı (Avtomatik tetiklənmə)
                    val currentLesson = _activeLesson.value
                    val currentIdx = _activeBatchIndex.value
                    val nextBatchIdx = currentIdx + 1
                    if (nextBatchIdx < currentLesson.batches.size) {
                        val nextBatch = currentLesson.batches[nextBatchIdx]
                        if (nextMin >= nextBatch.scheduledOffsetMinutes && nextBatch.status == BatchStatus.PENDING) {
                            // Avtomatik növbəti batch vaxtı çatdı!
                            activateBatchNow(nextBatchIdx)
                        }
                    }
                }
            }
        }
    }

    fun initializeWithLesson(lesson: Lesson) {
        _activeLesson.value = lesson
        _lessonCurrentMinute.value = 5
        _activeBatchIndex.value = 0
        _activeIntervention.value = null
        _studentHasSubmitted.value = false
        _studentSelectedOptionId.value = null

        val pin = lesson.pinCode.ifBlank { "839 214" }
        _session.value = SampleData.initialLiveSession.copy(
            id = "ls_" + lesson.id,
            pinCode = pin,
            quizTitle = lesson.title,
            subject = "${lesson.subject} • ${lesson.gradeLevel}",
            currentQuestionIndex = 0,
            totalQuestions = lesson.batches.firstOrNull()?.questions?.size ?: 3,
            status = LiveSessionStatus.WAITING_ROOM,
            connectedStudents = SampleData.initialLiveStudents
        )

        // Firestore real-time sync for students
        listenToFirestoreStudents(lesson.id)
    }

    fun initializeWithQuiz(quiz: Quiz) {
        val pin = "${(100..999).random()} ${(100..999).random()}"
        _session.value = SampleData.initialLiveSession.copy(
            id = "ls_" + System.currentTimeMillis().toString().takeLast(6),
            pinCode = pin,
            quizTitle = quiz.title,
            subject = "${quiz.subject} • ${quiz.gradeLevel}",
            currentQuestionIndex = 0,
            totalQuestions = quiz.questions.size.coerceAtLeast(1),
            status = LiveSessionStatus.WAITING_ROOM,
            connectedStudents = SampleData.initialLiveStudents
        )
    }

    private fun listenToFirestoreStudents(lessonId: String) {
        firestoreSyncJob?.cancel()
        firestoreSyncJob = viewModelScope.launch {
            firestoreService.observeStudentsForLesson(lessonId).collect { firestoreStudents ->
                if (firestoreStudents.isNotEmpty()) {
                    val currentList = _session.value.connectedStudents.toMutableList()
                    firestoreStudents.forEach { fsStudent ->
                        if (currentList.none { it.name.equals(fsStudent.name, ignoreCase = true) }) {
                            currentList.add(
                                LiveStudent(
                                    id = fsStudent.id,
                                    name = fsStudent.name,
                                    score = 0,
                                    streak = 0,
                                    isAnswered = false,
                                    isCorrect = false
                                )
                            )
                        }
                    }
                    _session.value = _session.value.copy(connectedStudents = currentList)
                }
            }
        }
    }

    // Manual Trigger: "İndi Göndər" (Activate Next Batch Now)
    fun activateBatchNow(batchIdx: Int) {
        val lesson = _activeLesson.value
        val batches = lesson.batches.mapIndexed { idx, b ->
            when {
                idx < batchIdx -> b.copy(status = BatchStatus.COMPLETED)
                idx == batchIdx -> b.copy(status = BatchStatus.ACTIVE)
                else -> b.copy(status = BatchStatus.PENDING)
            }
        }
        _activeLesson.value = lesson.copy(batches = batches)
        _activeBatchIndex.value = batchIdx
        _studentHasSubmitted.value = false
        _studentSelectedOptionId.value = null

        // Update status in Firestore
        val activeBatch = batches.getOrNull(batchIdx)
        if (activeBatch != null) {
            viewModelScope.launch {
                try {
                    firestoreService.updateBatchStatus(activeBatch.id, "active", System.currentTimeMillis())
                } catch (e: Exception) {
                    // Log or handle
                }
            }
        }

        startQuestion()
    }

    // Manual Trigger: "Təxirə Sal" (+5 min Snooze)
    fun snoozeCurrentBatch(batchIdx: Int, addMinutes: Int = 5) {
        val lesson = _activeLesson.value
        val batches = lesson.batches.mapIndexed { idx, b ->
            if (idx == batchIdx) {
                b.copy(scheduledOffsetMinutes = b.scheduledOffsetMinutes + addMinutes)
            } else b
        }
        _activeLesson.value = lesson.copy(batches = batches)
    }

    fun startQuestion() {
        val current = _session.value
        val timeLimit = 30

        val resetStudents = current.connectedStudents.map {
            it.copy(isAnswered = false, isCorrect = false)
        }

        _session.value = current.copy(
            status = LiveSessionStatus.QUESTION_ACTIVE,
            currentTimerSeconds = timeLimit,
            maxTimerSeconds = timeLimit,
            connectedStudents = resetStudents
        )

        startTimerAndStudentSimulation(timeLimit)
    }

    private fun startTimerAndStudentSimulation(totalSeconds: Int) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            for (remaining in totalSeconds downTo 0) {
                _session.value = _session.value.copy(currentTimerSeconds = remaining)

                if (remaining > 0 && remaining % 2 == 0) {
                    simulateStudentAnswers()
                }

                if (remaining == 0) {
                    endQuestionTime()
                    break
                }
                delay(1000)
            }
        }
    }

    private fun simulateStudentAnswers() {
        val current = _session.value
        val unanswered = current.connectedStudents.filter { !it.isAnswered }
        if (unanswered.isNotEmpty()) {
            val toAnswer = unanswered.shuffled().take(2)
            val updated = current.connectedStudents.map { student ->
                if (toAnswer.any { it.id == student.id }) {
                    val isCorrect = Math.random() > 0.4
                    val speedBonus = (1000..1500).random()
                    val addedScore = if (isCorrect) 700 + speedBonus else 0
                    student.copy(
                        isAnswered = true,
                        isCorrect = isCorrect,
                        score = student.score + addedScore,
                        streak = if (isCorrect) student.streak + 1 else 0
                    )
                } else {
                    student
                }
            }
            _session.value = current.copy(connectedStudents = updated)
        }
    }

    fun endQuestionTime() {
        timerJob?.cancel()
        val current = _session.value
        val answeredStudents = current.connectedStudents.map {
            if (!it.isAnswered) it.copy(isAnswered = true, isCorrect = false) else it
        }

        val correctCount = answeredStudents.count { it.isCorrect }
        val incorrectCount = (answeredStudents.size - correctCount).coerceAtLeast(0)
        val total = answeredStudents.size.coerceAtLeast(1)
        val accuracy = (correctCount * 100) / total

        val stats = listOf(
            LiveQuestionStat(optionIndex = 0, count = (incorrectCount * 0.4).toInt(), percentage = 0.20f, isCorrect = false),
            LiveQuestionStat(optionIndex = 1, count = correctCount, percentage = correctCount.toFloat() / total, isCorrect = true),
            LiveQuestionStat(optionIndex = 2, count = (incorrectCount * 0.4).toInt(), percentage = 0.20f, isCorrect = false),
            LiveQuestionStat(optionIndex = 3, count = (incorrectCount * 0.2).toInt(), percentage = 0.10f, isCorrect = false)
        )

        _session.value = current.copy(
            status = LiveSessionStatus.QUESTION_RESULTS,
            connectedStudents = answeredStudents,
            questionStats = stats
        )

        // If accuracy is < 65%, trigger formative intervention alert!
        if (accuracy < 65) {
            val batch = _activeLesson.value.batches.getOrNull(_activeBatchIndex.value)
            val currentQ = batch?.questions?.getOrNull(_session.value.currentQuestionIndex)
            val tag = currentQ?.topicTag ?: "Dinamika və Nyuton Qanunları"
            val qText = currentQ?.text ?: "Formativ qiymətləndirmə sualı üzrə cavablar"

            _activeIntervention.value = FormativeInterventionEvent(
                id = "int_" + System.currentTimeMillis(),
                batchId = "b_${_activeBatchIndex.value + 1}",
                topicTag = tag,
                questionText = qText,
                errorRatePercent = 100 - accuracy,
                commonMisconception = "Şagirdlərin ${(100 - accuracy)}%-i bu konseptdə yanlış varyantı seçib.",
                suggestedPrompt = "Lövhədə $tag mövzusunun əsas düstur və təriflərini vizual nümunə ilə izah edin.",
                isResolved = false
            )
        }
    }

    // Formative Intervention action: "İzah etdim / Müdaxilə edildi"
    fun resolveCurrentIntervention(notes: String = "Dərs daxilində təkrar izah edildi.") {
        _activeIntervention.value = _activeIntervention.value?.copy(
            isResolved = true,
            resolvedNote = notes
        )
        _meaningfulInterventionsCount.value += 1
    }

    fun dismissIntervention() {
        _activeIntervention.value = null
    }

    fun showLeaderboard() {
        _session.value = _session.value.copy(
            status = LiveSessionStatus.LEADERBOARD
        )
    }

    fun nextQuestion() {
        val current = _session.value
        val nextIndex = current.currentQuestionIndex + 1
        if (nextIndex < current.totalQuestions) {
            _session.value = current.copy(
                currentQuestionIndex = nextIndex,
                status = LiveSessionStatus.WAITING_ROOM
            )
            startQuestion()
        } else {
            // Mark current batch as completed
            val lesson = _activeLesson.value
            val currentIdx = _activeBatchIndex.value
            val batches = lesson.batches.mapIndexed { idx, b ->
                if (idx == currentIdx) b.copy(status = BatchStatus.COMPLETED) else b
            }
            _activeLesson.value = lesson.copy(batches = batches)

            val currentBatch = batches.getOrNull(currentIdx)
            if (currentBatch != null) {
                viewModelScope.launch {
                    try {
                        firestoreService.updateBatchStatus(currentBatch.id, "completed", System.currentTimeMillis())
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            }

            _session.value = current.copy(
                status = LiveSessionStatus.SESSION_FINISHED
            )
        }
    }

    // Student interaction with real Firestore response submission
    fun setStudentIdentity(id: String, name: String) {
        currentStudentId = id
        currentStudentName = name
    }

    fun studentSelectOption(optionId: String) {
        _studentSelectedOptionId.value = optionId
    }

    fun studentSubmitAnswer() {
        _studentHasSubmitted.value = true
        val batch = _activeLesson.value.batches.getOrNull(_activeBatchIndex.value)
        val question = batch?.questions?.getOrNull(_session.value.currentQuestionIndex)
        val selectedOptId = _studentSelectedOptionId.value

        if (question != null && selectedOptId != null) {
            val selectedOption = question.options.firstOrNull { it.id == selectedOptId }
            val isCorrect = selectedOption?.isCorrect == true

            viewModelScope.launch {
                try {
                    firestoreService.submitResponse(
                        ResponseEntity(
                            id = "",
                            questionId = question.id,
                            studentId = currentStudentId.ifBlank { "std_demo" },
                            answerGiven = selectedOption?.text ?: "",
                            isCorrect = isCorrect,
                            answeredAt = System.currentTimeMillis()
                        )
                    )
                } catch (e: Exception) {
                    // Log or handle
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        lessonClockJob?.cancel()
        firestoreSyncJob?.cancel()
    }
}
