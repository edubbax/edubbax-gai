package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BloomLevel
import com.example.model.DifficultyLevel
import com.example.model.Lesson
import com.example.model.QuestionType
import com.example.model.Quiz
import com.example.ui.components.AiSparkleBadge
import com.example.ui.components.SparkleIcon
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.ChipSelected
import com.example.ui.theme.ChipUnselected
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TerracottaWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GeneratorState
import com.example.ui.viewmodel.GeneratorViewModel

@Composable
fun AiGeneratorScreen(
    viewModel: GeneratorViewModel,
    onSaveToLibrary: (Quiz) -> Unit = {},
    onLaunchLive: (Quiz) -> Unit = {},
    onReviewBatches: (Lesson) -> Unit = {}
) {
    val state by viewModel.generatorState.collectAsState()
    val progressStep by viewModel.aiProgressStep.collectAsState()
    val progressValue by viewModel.aiProgressValue.collectAsState()
    val generatedLesson by viewModel.generatedLesson.collectAsState()
    val generatedQuiz by viewModel.generatedQuiz.collectAsState()

    val topic by viewModel.topic.collectAsState()
    val subject by viewModel.subject.collectAsState()
    val gradeLevel by viewModel.gradeLevel.collectAsState()
    val durationMinutes by viewModel.durationMinutes.collectAsState()
    val numBatches by viewModel.numBatches.collectAsState()
    val format by viewModel.questionFormat.collectAsState()
    val difficulty by viewModel.difficulty.collectAsState()
    val selectedBloomLevel by viewModel.selectedBloomLevel.collectAsState()
    val notes by viewModel.additionalNotes.collectAsState()

    when (state) {
        GeneratorState.INPUT_CONFIG -> {
            GeneratorConfigView(
                topic = topic,
                onTopicChange = { viewModel.topic.value = it },
                subject = subject,
                onSubjectChange = { viewModel.subject.value = it },
                gradeLevel = gradeLevel,
                onGradeLevelChange = { viewModel.gradeLevel.value = it },
                durationMinutes = durationMinutes,
                onDurationChange = { viewModel.durationMinutes.value = it },
                numBatches = numBatches,
                onBatchesChange = { viewModel.numBatches.value = it },
                difficulty = difficulty,
                onDifficultyChange = { viewModel.difficulty.value = it },
                selectedFormat = format,
                onFormatChange = { viewModel.questionFormat.value = it },
                selectedBloom = selectedBloomLevel,
                onBloomChange = { viewModel.selectedBloomLevel.value = it },
                notes = notes,
                onNotesChange = { viewModel.additionalNotes.value = it },
                onGenerateClick = {
                    viewModel.startGeneration { lesson ->
                        onReviewBatches(lesson)
                    }
                }
            )
        }

        GeneratorState.GENERATING_AI -> {
            GeneratingLoadingView(
                stepDescription = progressStep,
                progressValue = progressValue
            )
        }

        GeneratorState.PREVIEW_RESULT -> {
            generatedLesson?.let { lesson ->
                LessonPreviewResultView(
                    lesson = lesson,
                    onOpenReview = { onReviewBatches(lesson) },
                    onReGenerate = { viewModel.resetToConfig() }
                )
            }
        }
    }
}

@Composable
private fun GeneratorConfigView(
    topic: String,
    onTopicChange: (String) -> Unit,
    subject: String,
    onSubjectChange: (String) -> Unit,
    gradeLevel: String,
    onGradeLevelChange: (String) -> Unit,
    durationMinutes: Int,
    onDurationChange: (Int) -> Unit,
    numBatches: Int,
    onBatchesChange: (Int) -> Unit,
    difficulty: DifficultyLevel,
    onDifficultyChange: (DifficultyLevel) -> Unit,
    selectedFormat: QuestionType,
    onFormatChange: (QuestionType) -> Unit,
    selectedBloom: BloomLevel,
    onBloomChange: (BloomLevel) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onGenerateClick: () -> Unit
) {
    val subjects = listOf("Fizika", "Riyaziyyat", "Kimya", "Biologiya", "Tarix", "İnformatika", "Azərbaycan dili", "Coğrafiya")
    val gradeLevels = listOf("5-ci sinif", "6-cı sinif", "7-ci sinif", "8-ci sinif", "9-cu sinif", "10-cu sinif", "11-ci sinif", "Universitet")
    val durations = listOf(45, 60, 90)
    val batchOptions = listOf(1, 2, 3)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp)
            .testTag("ai_generator_screen"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AI Dərs və Batch Tərtibatı",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dərs vaxt xətti üzrə avtomatlaşdırılmış quiz generasiyası",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                AiSparkleBadge(text = "Gemini 2.5")
            }
        }

        // Lesson Topic & Prompt Input
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DƏRSİN MÖVZUSU VƏ YA STANDART",
                            color = GoldAccentLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        SparkleIcon(color = GoldAccent, size = 14.dp)
                    }

                    OutlinedTextField(
                        value = topic,
                        onValueChange = onTopicChange,
                        placeholder = { Text("Məs: Nyutonun I və II qanunları, sürtünmə qüvvəsi", color = TextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("topic_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        // Subject & Grade Selector
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "FƏNN VƏ SİNİF SƏVİYYƏSİ",
                        color = GoldAccentLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Subjects horizontal chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(subjects) { subj ->
                            val isSelected = subj == subject
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSubjectChange(subj) },
                                label = { Text(subj, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChipSelected,
                                    selectedLabelColor = GoldAccentLight,
                                    containerColor = ChipUnselected,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) GoldAccent else SurfaceBorder
                                )
                            )
                        }
                    }

                    // Grade levels chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(gradeLevels) { grade ->
                            val isSelected = grade == gradeLevel
                            FilterChip(
                                selected = isSelected,
                                onClick = { onGradeLevelChange(grade) },
                                label = { Text(grade, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChipSelected,
                                    selectedLabelColor = GoldAccentLight,
                                    containerColor = ChipUnselected,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) GoldAccent else SurfaceBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // DURATION & BATCHES TIMELINE CONFIGURATION
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DƏRS MÜDDƏTİ VƏ BATCH VAXT XƏTTİ",
                            color = GoldAccentLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Duration Selection (45m, 60m, 90m)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        durations.forEach { dur ->
                            val isSelected = dur == durationMinutes
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ChipSelected else SurfaceElevated)
                                    .border(1.dp, if (isSelected) GoldAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { onDurationChange(dur) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$dur dəqiqə",
                                    color = if (isSelected) GoldAccentLight else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }

                    Text(
                        text = "Batch sayı (Quiz sayı):",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )

                    // Batches Count (1, 2, 3)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        batchOptions.forEach { count ->
                            val isSelected = count == numBatches
                            val interval = durationMinutes / count
                            val timelineDesc = (1..count).map { "${it * interval}m" }.joinToString(", ")

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ChipSelected else SurfaceElevated)
                                    .border(1.dp, if (isSelected) GoldAccent else SurfaceBorder, RoundedCornerShape(10.dp))
                                    .clickable { onBatchesChange(count) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$count Batch",
                                        color = if (isSelected) GoldAccentLight else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "@ $timelineDesc",
                                        color = if (isSelected) GoldAccentLight.copy(alpha = 0.8f) else TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bloom Level & Difficulty
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "BLOOM TAKSONOMİYASI VƏ İDRAK DƏRƏCƏSİ",
                        color = GoldAccentLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(BloomLevel.values().toList()) { bloom ->
                            val isSelected = bloom == selectedBloom
                            FilterChip(
                                selected = isSelected,
                                onClick = { onBloomChange(bloom) },
                                label = { Text("L${bloom.levelNumber}. ${bloom.title}", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ChipSelected,
                                    selectedLabelColor = GoldAccentLight,
                                    containerColor = ChipUnselected,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) GoldAccent else SurfaceBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // Additional Pedagogical Notes
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "PEDAQOJİ İSTİQAMƏT VƏ DİSTRAKTOR TƏLƏBİ",
                        color = GoldAccentLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        placeholder = { Text("Xüsusi vurğulanmalı qaydalar və ya çaşdırıcı variantlar...", color = TextMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceElevated,
                            unfocusedContainerColor = SurfaceElevated
                        ),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 2
                    )
                }
            }
        }

        // Generate Button
        item {
            Button(
                onClick = onGenerateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_quiz_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = BackgroundDark
                )
            ) {
                SparkleIcon(color = BackgroundDark, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AI ilə Dərs və Batch-ləri Tərtib Et",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GeneratingLoadingView(
    stepDescription: String,
    progressValue: Float
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
            .testTag("ai_loading_view"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(GoldAccent.copy(alpha = 0.15f))
                    .border(2.dp, GoldAccent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                SparkleIcon(color = GoldAccentLight, size = 38.dp)
            }

            Text(
                text = "Süni İntellekt Dərsi Tərtib Edir",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stepDescription.ifBlank { "Pedaqoji suallar və distraktorlar sintez olunur..." },
                color = GoldAccentLight,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldAccent,
                trackColor = SurfaceElevated
            )
        }
    }
}

@Composable
private fun LessonPreviewResultView(
    lesson: Lesson,
    onOpenReview: () -> Unit,
    onReGenerate: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = EmeraldSuccess,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Dərs və Batch-lər Hazırdır!",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${lesson.title} (${lesson.batches.size} Batch, ${lesson.durationMinutes} dəqiqə)",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onOpenReview,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("open_batch_review_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BackgroundDark)
        ) {
            Text("Sualları Nəzərdən Keçir və Redaktə Et", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onReGenerate) {
            Text("Parametrləri Dəyiş və Yenidən Yarat", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
