package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LiveSessionStatus
import com.example.ui.components.AiSparkleBadge
import com.example.ui.components.EdubbaXLogo
import com.example.ui.components.SparkleIcon
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LiveSessionViewModel

@Composable
fun StudentInteractiveScreen(
    viewModel: LiveSessionViewModel,
    studentName: String = "Ayan Quliyeva",
    onExit: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val lesson by viewModel.activeLesson.collectAsState()
    val activeBatchIdx by viewModel.activeBatchIndex.collectAsState()
    val selectedOptionId by viewModel.studentSelectedOptionId.collectAsState()
    val hasSubmitted by viewModel.studentHasSubmitted.collectAsState()

    val batch = lesson.batches.getOrNull(activeBatchIdx) ?: lesson.batches.firstOrNull()
    val currentQuestion = batch?.questions?.getOrNull(session.currentQuestionIndex) ?: batch?.questions?.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp)
            .testTag("student_interactive_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onExit) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Çıxış", tint = TextPrimary)
                }
                Column {
                    Text(
                        text = studentName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lesson.subject} • PIN: ${session.pinCode}",
                        color = GoldAccentLight,
                        fontSize = 11.sp
                    )
                }
            }

            AiSparkleBadge(text = "Şagird")
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (session.status == LiveSessionStatus.WAITING_ROOM) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Müəllim sualları başladır...",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dərs: ${lesson.title}",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            // Active Question View for Student
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Batch ${batch?.sequenceNumber ?: 1} • Sual ${session.currentQuestionIndex + 1}",
                            color = GoldAccentLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${session.currentTimerSeconds} saniyə",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentQuestion?.text ?: "Sual mətni...",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Options List
            Text(
                text = "Cavabınızı seçin:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currentQuestion?.options?.forEachIndexed { idx, opt ->
                    val isSelected = selectedOptionId == opt.id
                    val isLocked = hasSubmitted

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) GoldAccent.copy(alpha = 0.2f) else CardDark
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) GoldAccent else SurfaceBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable(enabled = !isLocked) {
                                viewModel.studentSelectOption(opt.id)
                            }
                            .padding(14.dp)
                            .testTag("student_option_$idx")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) GoldAccent else SurfaceElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = listOf("A", "B", "C", "D").getOrElse(idx) { "$idx" },
                                    color = if (isSelected) BackgroundDark else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = opt.text,
                                color = if (isSelected) GoldAccentLight else TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seçildi",
                                    tint = GoldAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Submit Button
            Button(
                onClick = { viewModel.studentSubmitAnswer() },
                enabled = selectedOptionId != null && !hasSubmitted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("student_submit_answer_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = BackgroundDark,
                    disabledContainerColor = SurfaceElevated,
                    disabledContentColor = TextMuted
                )
            ) {
                if (hasSubmitted) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cavab Göndərildi ✓", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Cavabı Təsdiq Et", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
