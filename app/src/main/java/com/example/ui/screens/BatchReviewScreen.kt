package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Lesson
import com.example.model.QuizQuestion
import com.example.ui.components.AiSparkleBadge
import com.example.ui.components.SparkleIcon
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TerracottaWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.GeneratorViewModel

@Composable
fun BatchReviewScreen(
    generatorViewModel: GeneratorViewModel,
    onSaveAndOpenLobby: (Lesson) -> Unit,
    onBack: () -> Unit
) {
    val lesson by generatorViewModel.generatedLesson.collectAsState()
    val currentBatchIdx by generatorViewModel.selectedBatchIndex.collectAsState()

    var editingQuestionIdx by remember { mutableStateOf<Int?>(null) }
    var editingText by remember { mutableStateOf("") }

    if (lesson == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Text("Heç bir dərs seçilməyib", color = TextSecondary)
        }
        return
    }

    val activeLesson = lesson!!
    val batches = activeLesson.batches
    val currentBatch = batches.getOrNull(currentBatchIdx) ?: batches.firstOrNull()

    // Edit Question Dialog
    if (editingQuestionIdx != null) {
        AlertDialog(
            onDismissRequest = { editingQuestionIdx = null },
            title = { Text("Sualı Redaktə Et", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editingText,
                    onValueChange = { editingText = it },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = SurfaceBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingQuestionIdx?.let { qIdx ->
                            generatorViewModel.updateBatchQuestionText(currentBatchIdx, qIdx, editingText)
                        }
                        editingQuestionIdx = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BackgroundDark)
                ) {
                    Text("Yadda Saxla", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { editingQuestionIdx = null }) {
                    Text("Ləğv et", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Sualları Nəzərdən Keçir",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${activeLesson.title} • ${activeLesson.durationMinutes} dəq",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            AiSparkleBadge(text = "AI Tərtib")
        }

        // Horizontal Batch Selection Tabs
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(batches) { idx, batch ->
                val isSelected = idx == currentBatchIdx
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GoldAccent else CardDark)
                        .border(1.dp, if (isSelected) GoldAccent else SurfaceBorder, RoundedCornerShape(12.dp))
                        .clickable { generatorViewModel.selectedBatchIndex.value = idx }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("batch_tab_$idx")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SparkleIcon(
                            color = if (isSelected) BackgroundDark else GoldAccentLight,
                            size = 12.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Batch ${batch.sequenceNumber}",
                                color = if (isSelected) BackgroundDark else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${batch.scheduledOffsetMinutes}. dəqiqədə",
                                color = if (isSelected) BackgroundDark.copy(alpha = 0.8f) else TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Questions List for selected batch
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
        ) {
            item {
                Text(
                    text = currentBatch?.title ?: "Batch Sualları",
                    color = GoldAccentLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (currentBatch != null) {
                itemsIndexed(currentBatch.questions) { qIdx, question ->
                    ReviewQuestionCard(
                        question = question,
                        index = qIdx + 1,
                        onEdit = {
                            editingQuestionIdx = qIdx
                            editingText = question.text
                        },
                        onDelete = {
                            generatorViewModel.deleteBatchQuestion(currentBatchIdx, qIdx)
                        }
                    )
                }
            }
        }

        // Bottom Sticky Action Bar: Confirm & Open Lobby
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardDark)
                .border(1.dp, SurfaceBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Button(
                onClick = { onSaveAndOpenLobby(activeLesson) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("confirm_and_open_lobby_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = BackgroundDark
                )
            ) {
                Icon(
                    imageVector = Icons.Default.QrCode,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dərsi Təsdiqlə və Link / PIN Əldə Et",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ReviewQuestionCard(
    question: QuizQuestion,
    index: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("review_question_$index")
    ) {
        Column {
            // Badges row: Question number, Topic Tag, Bloom Level, Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(GoldAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$index",
                            color = BackgroundDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (question.topicTag.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(SurfaceElevated)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#${question.topicTag}",
                                color = GoldAccentLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = question.bloomLevel.title,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Redaktə", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = TerracottaWarning, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question Text
            Text(
                text = question.text,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                question.options.forEachIndexed { optIdx, opt ->
                    val isCorrect = opt.isCorrect
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCorrect) EmeraldSuccess.copy(alpha = 0.12f) else SurfaceElevated)
                            .border(1.dp, if (isCorrect) EmeraldSuccess.copy(alpha = 0.5f) else SurfaceBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + optIdx)}. ",
                            color = if (isCorrect) EmeraldSuccess else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = opt.text,
                            color = if (isCorrect) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Doğru",
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            if (question.explanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated.copy(alpha = 0.6f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = GoldAccentLight,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = question.explanation,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
