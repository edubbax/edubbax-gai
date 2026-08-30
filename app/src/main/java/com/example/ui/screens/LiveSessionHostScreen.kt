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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BatchStatus
import com.example.model.FormativeInterventionEvent
import com.example.model.Lesson
import com.example.model.LiveSession
import com.example.model.LiveSessionStatus
import com.example.model.Quiz
import com.example.model.QuizBatch
import com.example.model.QuizQuestion
import com.example.ui.components.AiSparkleBadge
import com.example.ui.components.SparkleIcon
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EmeraldLight
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
import com.example.ui.viewmodel.LiveSessionViewModel

@Composable
fun LiveSessionHostScreen(
    viewModel: LiveSessionViewModel,
    onFinishSession: () -> Unit
) {
    val session by viewModel.session.collectAsState()
    val lesson by viewModel.activeLesson.collectAsState()
    val activeBatchIdx by viewModel.activeBatchIndex.collectAsState()
    val currentMinute by viewModel.lessonCurrentMinute.collectAsState()
    val activeIntervention by viewModel.activeIntervention.collectAsState()
    val interventionsCount by viewModel.meaningfulInterventionsCount.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .testTag("live_session_host_screen")
    ) {
        // Top Room Header
        LiveRoomHeader(
            pinCode = session.pinCode,
            lessonTitle = lesson.title,
            subject = "${lesson.subject} • ${lesson.gradeLevel}",
            currentMinute = currentMinute,
            totalMinutes = lesson.durationMinutes,
            studentCount = session.connectedStudents.size,
            onClose = onFinishSession
        )

        // HORIZONTAL BATCH TIMELINE & MANUAL CONTROLS
        BatchTimelineSection(
            lesson = lesson,
            activeBatchIdx = activeBatchIdx,
            currentMinute = currentMinute,
            onActivateBatchNow = { idx -> viewModel.activateBatchNow(idx) },
            onSnoozeBatch = { idx -> viewModel.snoozeCurrentBatch(idx, 5) }
        )

        // FORMATIVE INTERVENTION BANNER (If active and not yet resolved)
        if (activeIntervention != null && !activeIntervention!!.isResolved) {
            FormativeInterventionAlertBanner(
                intervention = activeIntervention!!,
                onResolve = { viewModel.resolveCurrentIntervention() },
                onDismiss = { viewModel.dismissIntervention() }
            )
        }

        // Active State View
        when (session.status) {
            LiveSessionStatus.WAITING_ROOM -> {
                LiveWaitingRoomView(
                    session = session,
                    lesson = lesson,
                    onStart = { viewModel.startQuestion() }
                )
            }

            LiveSessionStatus.QUESTION_ACTIVE -> {
                LiveQuestionActiveView(
                    session = session,
                    lesson = lesson,
                    activeBatchIdx = activeBatchIdx,
                    onEndEarly = { viewModel.endQuestionTime() }
                )
            }

            LiveSessionStatus.QUESTION_RESULTS -> {
                LiveQuestionResultsView(
                    session = session,
                    lesson = lesson,
                    activeBatchIdx = activeBatchIdx,
                    onShowLeaderboard = { viewModel.showLeaderboard() }
                )
            }

            LiveSessionStatus.LEADERBOARD -> {
                LiveLeaderboardView(
                    session = session,
                    onNext = { viewModel.nextQuestion() }
                )
            }

            LiveSessionStatus.SESSION_FINISHED -> {
                LiveFinalSummaryView(
                    session = session,
                    lesson = lesson,
                    interventionsCount = interventionsCount,
                    onExit = onFinishSession
                )
            }
        }
    }
}

@Composable
private fun LiveRoomHeader(
    pinCode: String,
    lessonTitle: String,
    subject: String,
    currentMinute: Int,
    totalMinutes: Int,
    studentCount: Int,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark)
            .border(1.dp, SurfaceBorder)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "PIN: ",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = pinCode,
                        color = GoldAccentLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "$currentMinute / $totalMinutes dəq",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Text(
                    text = lessonTitle,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Groups,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$studentCount",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(SurfaceElevated)
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Bağla",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchTimelineSection(
    lesson: Lesson,
    activeBatchIdx: Int,
    currentMinute: Int,
    onActivateBatchNow: (Int) -> Unit,
    onSnoozeBatch: (Int) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceElevated.copy(alpha = 0.4f))
            .border(1.dp, SurfaceBorder)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SparkleIcon(color = GoldAccentLight, size = 13.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Dərs Vaxt Xətti və Quiz Batch-ləri",
                        color = GoldAccentLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Quick manual controls
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
                            .clickable { onSnoozeBatch(activeBatchIdx) }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                            .testTag("snooze_batch_btn")
                    ) {
                        Text(
                            text = "+5 dəq Təxirə sal",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent)
                            .clickable { onActivateBatchNow(activeBatchIdx) }
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("activate_batch_now_btn")
                    ) {
                        Text(
                            text = "İndi Göndər",
                            color = BackgroundDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Batches Horizontal Display
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(lesson.batches) { idx, batch ->
                    val isActive = idx == activeBatchIdx
                    val isCompleted = batch.status == BatchStatus.COMPLETED || idx < activeBatchIdx

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isActive) CardDark else SurfaceElevated)
                            .border(
                                width = if (isActive) 1.5.dp else 1.dp,
                                color = if (isActive) GoldAccent else (if (isCompleted) EmeraldSuccess else SurfaceBorder),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onActivateBatchNow(idx) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("timeline_batch_$idx")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(13.dp)
                                )
                            } else if (isActive) {
                                SparkleIcon(color = GoldAccentLight, size = 13.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Default.HourglassTop,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(5.dp))

                            Column {
                                Text(
                                    text = "Batch ${batch.sequenceNumber}",
                                    color = if (isActive) GoldAccentLight else (if (isCompleted) EmeraldLight else TextPrimary),
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "${batch.scheduledOffsetMinutes}. dəqiqə",
                                    color = TextMuted,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FormativeInterventionAlertBanner(
    intervention: FormativeInterventionEvent,
    onResolve: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2B1C16),
                        Color(0xFF1E1E1E)
                    )
                )
            )
            .border(1.5.dp, TerracottaWarning, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .testTag("formative_intervention_banner")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TerracottaWarning,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "FORMATİV MÜDAXİLƏ ZƏRURƏTİ",
                        color = TerracottaWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(TerracottaWarning.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "%${intervention.errorRatePercent} Səhv nisbəti",
                        color = TerracottaWarning,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "⚠️ Zəif Anlaşılan Mövzu: ${intervention.topicTag}",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Yanılgı: ${intervention.commonMisconception}",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SparkleIcon(color = GoldAccentLight, size = 12.dp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Tövsiyə: ${intervention.suggestedPrompt}",
                    color = GoldAccentLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clickable { onDismiss() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Bağla",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onResolve,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldSuccess,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("resolve_intervention_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "İzah etdim / Müdaxilə edildi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveWaitingRoomView(
    session: LiveSession,
    lesson: Lesson,
    onStart: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("waiting_room_view"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Şagirdlər üçün qoşulma linki",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = lesson.uniqueLinkSlug,
                        color = GoldAccent,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "PIN: ${session.pinCode}",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = BackgroundDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("start_live_question_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Batch 1 Suallarını Göndər", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Qoşulan Şagirdlər (${session.connectedStudents.size})",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(session.connectedStudents) { student ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = student.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "Hazırdır", color = EmeraldSuccess, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveQuestionActiveView(
    session: LiveSession,
    lesson: Lesson,
    activeBatchIdx: Int,
    onEndEarly: () -> Unit
) {
    val batch = lesson.batches.getOrNull(activeBatchIdx) ?: lesson.batches.firstOrNull()
    val currentQuestion = batch?.questions?.getOrNull(session.currentQuestionIndex) ?: batch?.questions?.firstOrNull()
    val answeredCount = session.connectedStudents.count { it.isAnswered }
    val totalStudents = session.connectedStudents.size.coerceAtLeast(1)
    val timerProgress = session.currentTimerSeconds.toFloat() / session.maxTimerSeconds.coerceAtLeast(1)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("question_active_view"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            // Header with question number and countdown timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Batch ${batch?.sequenceNumber ?: 1} • Sual ${session.currentQuestionIndex + 1} / ${batch?.questions?.size ?: 3}",
                        color = GoldAccentLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { timerProgress },
                        modifier = Modifier.size(50.dp),
                        color = if (session.currentTimerSeconds <= 5) TerracottaWarning else GoldAccent,
                        trackColor = SurfaceElevated,
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "${session.currentTimerSeconds}",
                        color = if (session.currentTimerSeconds <= 5) TerracottaWarning else TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Column {
                    if (currentQuestion != null && currentQuestion.topicTag.isNotBlank()) {
                        Text(
                            text = "#${currentQuestion.topicTag} • ${currentQuestion.bloomLevel.title}",
                            color = GoldAccentLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(
                        text = currentQuestion?.text ?: "Sual mətni yüklənir...",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Answered Counter & End Early Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Cavablandıranlar: $answeredCount / $totalStudents", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onEndEarly,
                    colors = ButtonDefaults.buttonColors(containerColor = TerracottaWarning.copy(alpha = 0.2f), contentColor = TerracottaWarning),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Vaxtı Bitir", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "Variantlar", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
        }

        itemsIndexed(currentQuestion?.options ?: emptyList()) { index, option ->
            val optionLetter = listOf("A", "B", "C", "D").getOrElse(index) { "${index + 1}" }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(SurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = optionLetter, color = GoldAccentLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = option.text, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun LiveQuestionResultsView(
    session: LiveSession,
    lesson: Lesson,
    activeBatchIdx: Int,
    onShowLeaderboard: () -> Unit
) {
    val batch = lesson.batches.getOrNull(activeBatchIdx) ?: lesson.batches.firstOrNull()
    val currentQuestion = batch?.questions?.getOrNull(session.currentQuestionIndex) ?: batch?.questions?.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("question_results_view"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Nəticələrin Təhlili", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Real-vaxt cavab paylanması", color = TextSecondary, fontSize = 11.sp)
                }

                Button(
                    onClick = onShowLeaderboard,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("show_leaderboard_btn")
                ) {
                    Icon(imageVector = Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Liderlər", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = currentQuestion?.text ?: "", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
        }

        itemsIndexed(currentQuestion?.options ?: emptyList()) { index, option ->
            val stat = session.questionStats.getOrNull(index)
            val count = stat?.count ?: (if (option.isCorrect) 8 else 2)
            val percent = stat?.percentage ?: (if (option.isCorrect) 0.57f else 0.14f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, if (option.isCorrect) EmeraldSuccess else SurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${listOf("A", "B", "C", "D").getOrElse(index) { "$index" }}. ${option.text}",
                            color = if (option.isCorrect) EmeraldSuccess else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (option.isCorrect) FontWeight.Bold else FontWeight.Normal
                        )
                        Text(
                            text = "$count tələbə (${(percent * 100).toInt()}%)",
                            color = if (option.isCorrect) EmeraldLight else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SurfaceElevated)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(percent)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (option.isCorrect) EmeraldSuccess else TerracottaWarning.copy(alpha = 0.6f))
                        )
                    }
                }
            }
        }

        if (!currentQuestion?.explanation.isNullOrBlank()) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SurfaceElevated)
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(imageVector = Icons.Outlined.Lightbulb, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = currentQuestion?.explanation ?: "", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveLeaderboardView(
    session: LiveSession,
    onNext: () -> Unit
) {
    val sorted = session.connectedStudents.sortedByDescending { it.score }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("leaderboard_view"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Liderlər Cədvəli", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BackgroundDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp).testTag("next_question_btn")
                ) {
                    Text("Növbəti Sual / Batch", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        itemsIndexed(sorted) { idx, student ->
            val rank = idx + 1
            val rankColor = when (rank) {
                1 -> GoldAccent
                2 -> Color(0xFFC0C0C0)
                3 -> Color(0xFFCD7F32)
                else -> TextMuted
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CardDark)
                    .border(1.dp, if (rank <= 3) rankColor.copy(alpha = 0.4f) else SurfaceBorder, RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$rank", color = rankColor, fontSize = 13.sp, fontWeight = FontWeight.Black, modifier = Modifier.width(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = student.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(text = "${student.score} xal", color = GoldAccentLight, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LiveFinalSummaryView(
    session: LiveSession,
    lesson: Lesson,
    interventionsCount: Int,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("final_podium_view"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(52.dp))
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "Dərs və Bütün Batch-lər Tamamlandı!", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = lesson.title, color = TextSecondary, fontSize = 13.sp)

        Spacer(modifier = Modifier.height(20.dp))

        // Interventions North Star recap card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CardDark)
                .border(1.5.dp, GoldAccent, RoundedCornerShape(14.dp))
                .padding(16.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DƏRS ÜZRƏ FORMATİV MÜDAXİLƏ NƏTİCƏSİ", color = GoldAccentLight, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "$interventionsCount Zəif Mövzu İzah Edildi və Mənimsənildi", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Valideyn və məktəb hesabatı avtomatik hazırlandı.", color = TextMuted, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onExit,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = BackgroundDark)
        ) {
            Text("İdarə Panelinə Qayıt", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}
