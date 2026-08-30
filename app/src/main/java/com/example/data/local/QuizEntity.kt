package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val subject: String,
    val gradeLevel: String,
    val description: String,
    val difficulty: String,
    val questionsJson: String,
    val totalPlays: Int,
    val averageScore: Int,
    val createdAt: String,
    val isDraft: Boolean,
    val isFavorite: Boolean,
    val tagsCsv: String
)
