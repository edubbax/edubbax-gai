package com.example.data

import android.util.Log
import com.example.model.LessonEntity
import com.example.model.QuestionEntity
import com.example.model.QuizBatchEntity
import com.example.model.ResponseEntity
import com.example.model.StudentEntity
import com.example.model.TeacherEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreService {
    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    companion object {
        private const val TAG = "FirestoreService"
        const val COLLECTION_TEACHERS = "teachers"
        const val COLLECTION_LESSONS = "lessons"
        const val COLLECTION_QUIZ_BATCHES = "quiz_batches"
        const val COLLECTION_QUESTIONS = "questions"
        const val COLLECTION_STUDENTS = "students"
        const val COLLECTION_RESPONSES = "responses"
    }

    // ----------------------------------------------------
    // 1. TEACHERS COLLECTION
    // ----------------------------------------------------
    suspend fun saveTeacher(teacher: TeacherEntity) {
        try {
            firestore.collection(COLLECTION_TEACHERS)
                .document(teacher.id)
                .set(teacher)
                .await()
            Log.d(TAG, "Teacher saved: ${teacher.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving teacher", e)
            throw e
        }
    }

    suspend fun getTeacher(teacherId: String): TeacherEntity? {
        return try {
            val snapshot = firestore.collection(COLLECTION_TEACHERS)
                .document(teacherId)
                .get()
                .await()
            snapshot.toObject(TeacherEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching teacher: $teacherId", e)
            null
        }
    }

    // ----------------------------------------------------
    // 2. LESSONS COLLECTION
    // ----------------------------------------------------
    suspend fun createLesson(
        lesson: LessonEntity,
        batches: List<Pair<QuizBatchEntity, List<QuestionEntity>>>
    ): String {
        return try {
            val lessonDoc = if (lesson.id.isNotBlank()) {
                firestore.collection(COLLECTION_LESSONS).document(lesson.id)
            } else {
                firestore.collection(COLLECTION_LESSONS).document()
            }
            val finalLessonId = lessonDoc.id
            val finalLesson = lesson.copy(id = finalLessonId)

            val batchWrite = firestore.batch()
            batchWrite.set(lessonDoc, finalLesson)

            // Save batches and questions in the batch
            for ((batch, questions) in batches) {
                val batchDoc = if (batch.id.isNotBlank()) {
                    firestore.collection(COLLECTION_QUIZ_BATCHES).document(batch.id)
                } else {
                    firestore.collection(COLLECTION_QUIZ_BATCHES).document()
                }
                val finalBatchId = batchDoc.id
                val finalBatch = batch.copy(id = finalBatchId, lessonId = finalLessonId)
                batchWrite.set(batchDoc, finalBatch)

                for (question in questions) {
                    val questionDoc = if (question.id.isNotBlank()) {
                        firestore.collection(COLLECTION_QUESTIONS).document(question.id)
                    } else {
                        firestore.collection(COLLECTION_QUESTIONS).document()
                    }
                    val finalQuestion = question.copy(id = questionDoc.id, quizBatchId = finalBatchId)
                    batchWrite.set(questionDoc, finalQuestion)
                }
            }

            batchWrite.commit().await()
            Log.d(TAG, "Created lesson with batches: $finalLessonId")
            finalLessonId
        } catch (e: Exception) {
            Log.e(TAG, "Error creating lesson", e)
            throw e
        }
    }

    fun observeLessonsForTeacher(teacherId: String): Flow<List<LessonEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_LESSONS)
            .whereEqualTo("teacher_id", teacherId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to lessons", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(LessonEntity::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getLessonByPin(pin: String): LessonEntity? {
        val cleanPin = pin.replace(" ", "").trim()
        return try {
            val query = firestore.collection(COLLECTION_LESSONS)
                .whereEqualTo("pin_code", cleanPin)
                .limit(1)
                .get()
                .await()
            query.documents.firstOrNull()?.toObject(LessonEntity::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error finding lesson by pin: $pin", e)
            null
        }
    }

    suspend fun updateLessonStatus(lessonId: String, status: String, startedAt: Long? = null) {
        try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (startedAt != null) {
                updates["started_at"] = startedAt
            }
            firestore.collection(COLLECTION_LESSONS)
                .document(lessonId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating lesson status", e)
            throw e
        }
    }

    fun observeLesson(lessonId: String): Flow<LessonEntity?> = callbackFlow {
        val listener = firestore.collection(COLLECTION_LESSONS)
            .document(lessonId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error observing lesson: $lessonId", error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(LessonEntity::class.java))
            }
        awaitClose { listener.remove() }
    }

    // ----------------------------------------------------
    // 3. QUIZ BATCHES COLLECTION
    // ----------------------------------------------------
    fun observeBatchesForLesson(lessonId: String): Flow<List<QuizBatchEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_QUIZ_BATCHES)
            .whereEqualTo("lesson_id", lessonId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to batches for lesson: $lessonId", error)
                    return@addSnapshotListener
                }
                val batches = snapshot?.documents?.mapNotNull { it.toObject(QuizBatchEntity::class.java) }
                    ?.sortedBy { it.sequenceNumber } ?: emptyList()
                trySend(batches)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateBatchStatus(batchId: String, status: String, activatedAt: Long? = null) {
        try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (activatedAt != null) {
                updates["activated_at"] = activatedAt
            }
            firestore.collection(COLLECTION_QUIZ_BATCHES)
                .document(batchId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating batch status: $batchId", e)
            throw e
        }
    }

    // ----------------------------------------------------
    // 4. QUESTIONS COLLECTION
    // ----------------------------------------------------
    fun observeQuestionsForBatch(batchId: String): Flow<List<QuestionEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_QUESTIONS)
            .whereEqualTo("quiz_batch_id", batchId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to questions for batch: $batchId", error)
                    return@addSnapshotListener
                }
                val questions = snapshot?.documents?.mapNotNull { it.toObject(QuestionEntity::class.java) } ?: emptyList()
                trySend(questions)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getQuestionsForBatches(batchIds: List<String>): List<QuestionEntity> {
        if (batchIds.isEmpty()) return emptyList()
        val allQuestions = mutableListOf<QuestionEntity>()
        for (batchId in batchIds) {
            val snap = firestore.collection(COLLECTION_QUESTIONS)
                .whereEqualTo("quiz_batch_id", batchId)
                .get()
                .await()
            allQuestions.addAll(snap.documents.mapNotNull { it.toObject(QuestionEntity::class.java) })
        }
        return allQuestions
    }

    // ----------------------------------------------------
    // 5. STUDENTS COLLECTION
    // ----------------------------------------------------
    suspend fun registerStudent(student: StudentEntity): String {
        val doc = if (student.id.isNotBlank()) {
            firestore.collection(COLLECTION_STUDENTS).document(student.id)
        } else {
            firestore.collection(COLLECTION_STUDENTS).document()
        }
        val finalStudent = student.copy(id = doc.id)
        doc.set(finalStudent).await()
        return doc.id
    }

    fun observeStudentsForLesson(lessonId: String): Flow<List<StudentEntity>> = callbackFlow {
        val listener = firestore.collection(COLLECTION_STUDENTS)
            .whereEqualTo("lesson_id", lessonId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to students for lesson: $lessonId", error)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { it.toObject(StudentEntity::class.java) }
                    ?.sortedBy { it.joinedAt } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // ----------------------------------------------------
    // 6. RESPONSES COLLECTION
    // ----------------------------------------------------
    suspend fun submitResponse(response: ResponseEntity): String {
        val doc = if (response.id.isNotBlank()) {
            firestore.collection(COLLECTION_RESPONSES).document(response.id)
        } else {
            firestore.collection(COLLECTION_RESPONSES).document()
        }
        val finalResponse = response.copy(id = doc.id)
        doc.set(finalResponse).await()
        return doc.id
    }

    fun observeResponsesForQuestions(questionIds: List<String>): Flow<List<ResponseEntity>> = callbackFlow {
        if (questionIds.isEmpty()) {
            trySend(emptyList())
            awaitClose { }
            return@callbackFlow
        }

        // Firestore whereIn supports up to 30 items
        val chunks = questionIds.chunked(10)
        val listeners = mutableListOf<ListenerRegistration>()
        val resultsMap = mutableMapOf<Int, List<ResponseEntity>>()

        chunks.forEachIndexed { index, chunk ->
            val listener = firestore.collection(COLLECTION_RESPONSES)
                .whereIn("question_id", chunk)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing responses chunk $index", error)
                        return@addSnapshotListener
                    }
                    val responses = snapshot?.documents?.mapNotNull { it.toObject(ResponseEntity::class.java) } ?: emptyList()
                    resultsMap[index] = responses
                    trySend(resultsMap.values.flatten())
                }
            listeners.add(listener)
        }

        awaitClose {
            listeners.forEach { it.remove() }
        }
    }
}
