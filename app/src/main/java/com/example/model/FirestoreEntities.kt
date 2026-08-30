package com.example.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore Schema Entities matching the exact specifications:
 *
 * teachers: id, email, name, created_at
 * lessons: id, teacher_id, subject, grade_level, topic_description, question_format, duration_minutes, num_quizzes, status, started_at, unique_link_slug
 * quiz_batches: id, lesson_id, sequence_number, scheduled_offset_minutes, status, activated_at
 * questions: id, quiz_batch_id, question_text, options, correct_answer, topic_tag
 * students: id, lesson_id, name, joined_at
 * responses: id, question_id, student_id, answer_given, is_correct, answered_at
 */

data class TeacherEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("email") @set:PropertyName("email") var email: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("created_at") @set:PropertyName("created_at") var createdAt: Long = System.currentTimeMillis()
)

data class LessonEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("teacher_id") @set:PropertyName("teacher_id") var teacherId: String = "",
    @get:PropertyName("subject") @set:PropertyName("subject") var subject: String = "",
    @get:PropertyName("grade_level") @set:PropertyName("grade_level") var gradeLevel: String = "",
    @get:PropertyName("topic_description") @set:PropertyName("topic_description") var topicDescription: String = "",
    @get:PropertyName("question_format") @set:PropertyName("question_format") var questionFormat: String = "multiple_choice",
    @get:PropertyName("duration_minutes") @set:PropertyName("duration_minutes") var durationMinutes: Int = 60,
    @get:PropertyName("num_quizzes") @set:PropertyName("num_quizzes") var numQuizzes: Int = 3,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "scheduled", // draft, scheduled, live, completed
    @get:PropertyName("started_at") @set:PropertyName("started_at") var startedAt: Long? = null,
    @get:PropertyName("unique_link_slug") @set:PropertyName("unique_link_slug") var uniqueLinkSlug: String = "",
    @get:PropertyName("pin_code") @set:PropertyName("pin_code") var pinCode: String = ""
)

data class QuizBatchEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("lesson_id") @set:PropertyName("lesson_id") var lessonId: String = "",
    @get:PropertyName("sequence_number") @set:PropertyName("sequence_number") var sequenceNumber: Int = 1,
    @get:PropertyName("scheduled_offset_minutes") @set:PropertyName("scheduled_offset_minutes") var scheduledOffsetMinutes: Int = 20,
    @get:PropertyName("status") @set:PropertyName("status") var status: String = "pending", // pending, active, completed
    @get:PropertyName("activated_at") @set:PropertyName("activated_at") var activatedAt: Long? = null
)

data class QuestionEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("quiz_batch_id") @set:PropertyName("quiz_batch_id") var quizBatchId: String = "",
    @get:PropertyName("question_text") @set:PropertyName("question_text") var questionText: String = "",
    @get:PropertyName("options") @set:PropertyName("options") var options: List<String> = emptyList(),
    @get:PropertyName("correct_answer") @set:PropertyName("correct_answer") var correctAnswer: String = "",
    @get:PropertyName("topic_tag") @set:PropertyName("topic_tag") var topicTag: String = "",
    @get:PropertyName("explanation") @set:PropertyName("explanation") var explanation: String = "",
    @get:PropertyName("points") @set:PropertyName("points") var points: Int = 10,
    @get:PropertyName("time_limit_seconds") @set:PropertyName("time_limit_seconds") var timeLimitSeconds: Int = 30
)

data class StudentEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("lesson_id") @set:PropertyName("lesson_id") var lessonId: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("joined_at") @set:PropertyName("joined_at") var joinedAt: Long = System.currentTimeMillis()
)

data class ResponseEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("question_id") @set:PropertyName("question_id") var questionId: String = "",
    @get:PropertyName("student_id") @set:PropertyName("student_id") var studentId: String = "",
    @get:PropertyName("answer_given") @set:PropertyName("answer_given") var answerGiven: String = "",
    @get:PropertyName("is_correct") @set:PropertyName("is_correct") var isCorrect: Boolean = false,
    @get:PropertyName("answered_at") @set:PropertyName("answered_at") var answeredAt: Long = System.currentTimeMillis()
)
