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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.model.Lesson
import com.example.model.LessonStatus
import com.example.model.NorthStarInterventionMetric
import com.example.model.Quiz
import com.example.model.TeacherProfile
import com.example.ui.components.AiSparkleBadge
import com.example.ui.components.SparkleIcon
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.CardDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentDark
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.GoldGlow
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TerracottaWarning
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AppDestination

@Composable
fun DashboardScreen(
    profile: TeacherProfile,
    lessons: List<Lesson>,
    northStarMetric: NorthStarInterventionMetric,
    onNavigate: (AppDestination) -> Unit,
    onOpenLessonLobby: (Lesson) -> Unit,
    onStartLiveLesson: (Lesson) -> Unit,
    onReviewLesson: (Lesson) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("Hamısı") }
    val filters = listOf("Hamısı", "Canlı", "Planlaşdırılan", "Tamamlanmış")

    val filteredLessons = when (selectedFilter) {
        "Canlı" -> lessons.filter { it.status == LessonStatus.LIVE }
        "Planlaşdırılan" -> lessons.filter { it.status == LessonStatus.SCHEDULED }
        "Tamamlanmış" -> lessons.filter { it.status == LessonStatus.COMPLETED }
        else -> lessons
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Welcome & Date
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Xoş gəldiniz, ${profile.fullName.split(" ").firstOrNull() ?: "Müəllim"}",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.school} • Dərs Planı",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                AiSparkleBadge(
                    text = "Gemini Pro",
                    accentColor = GoldAccent
                )
            }
        }

        // NORTH STAR METRIC CARD: Meaningful Learning Interventions
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                CardDark,
                                Color(0xFF231D10)
                            )
                        )
                    )
                    .border(1.5.dp, GoldAccent.copy(alpha = 0.7f), RoundedCornerShape(18.dp))
                    .padding(18.dp)
                    .testTag("north_star_metric_card")
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                SparkleIcon(color = GoldAccentLight, size = 20.dp)
                            }
                            Column {
                                Text(
                                    text = "NORTH STAR METRİKA",
                                    color = GoldAccentLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Mənalı Öyrənmə Müdaxilələri",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.18f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+${northStarMetric.thisWeekInterventions} bu həftə",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${northStarMetric.totalInterventions}",
                                color = GoldAccent,
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Black,
                                lineHeight = 36.sp
                            )
                            Text(
                                text = "Dərs daxili izah edilmiş zəif mövzu",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        // Secondary Stats inside Metric
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${northStarMetric.topicMasteryGainPercent}%",
                                    color = EmeraldSuccess,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Anlama artımı",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${northStarMetric.resolvedMisconceptionsCount}",
                                    color = TextPrimary,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Həll olunmuş yanılgı",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Action: "Yeni Dərs / Quiz Yarat" CTA
        item {
            Button(
                onClick = { onNavigate(AppDestination.AI_GENERATOR) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("create_new_lesson_cta"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = BackgroundDark
                )
            ) {
                SparkleIcon(color = BackgroundDark, size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yeni Dərs & Quiz Tərtib Et",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Section Title & Filter Chips
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dərslər və Quiz Batch-ləri",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${lessons.size} dərs",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        val isSelected = selectedFilter == filter
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) GoldAccent else CardDark)
                                .border(1.dp, if (isSelected) GoldAccent else SurfaceBorder, RoundedCornerShape(20.dp))
                                .clickable { selectedFilter = filter }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("filter_$filter")
                        ) {
                            Text(
                                text = filter,
                                color = if (isSelected) BackgroundDark else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Lesson Items
        items(filteredLessons) { lesson ->
            LessonDashboardCard(
                lesson = lesson,
                onStartLive = { onStartLiveLesson(lesson) },
                onShareLobby = { onOpenLessonLobby(lesson) },
                onReview = { onReviewLesson(lesson) }
            )
        }

        // Bottom space
        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

@Composable
fun LessonDashboardCard(
    lesson: Lesson,
    onStartLive: () -> Unit,
    onShareLobby: () -> Unit,
    onReview: () -> Unit
) {
    val isLive = lesson.status == LessonStatus.LIVE

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(
                width = if (isLive) 1.5.dp else 1.dp,
                color = if (isLive) GoldAccent else SurfaceBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
            .testTag("lesson_card_${lesson.id}")
    ) {
        Column {
            // Header Row: Subject Pill, Grade, Status Badge
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
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = lesson.subject,
                            color = GoldAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = lesson.gradeLevel,
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(SurfaceElevated)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "${lesson.durationMinutes} dəq",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Status Badge
                val (statusBg, statusFg, statusText) = when (lesson.status) {
                    LessonStatus.LIVE -> Triple(GoldAccent, BackgroundDark, "● CANLI DƏRS")
                    LessonStatus.SCHEDULED -> Triple(SurfaceElevated, TextSecondary, "Planlaşdırılıb")
                    LessonStatus.COMPLETED -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, "Tamamlandı")
                    LessonStatus.DRAFT -> Triple(SurfaceElevated, TextMuted, "Qaralama")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusFg,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lesson Title
            Text(
                text = lesson.title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )

            if (lesson.topicDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.topicDescription,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline Batches preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SparkleIcon(color = GoldAccentLight, size = 12.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${lesson.numQuizzes} Quiz Batch:",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    val intervals = (1..lesson.numQuizzes).map { "${it * (lesson.durationMinutes / lesson.numQuizzes)}m" }
                    Text(
                        text = intervals.joinToString(" → "),
                        color = GoldAccentLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "PIN: ${lesson.pinCode}",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Lobby / Share Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onShareLobby() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("lobby_btn_${lesson.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Paylaş",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Link & PIN",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Review & Edit Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onReview() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("review_btn_${lesson.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Suallara Bax",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Launch / Continue Live Button
                Button(
                    onClick = onStartLive,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldAccent,
                        contentColor = BackgroundDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .testTag("launch_live_btn_${lesson.id}")
                ) {
                    Icon(
                        imageVector = if (isLive) Icons.Default.LiveTv else Icons.Default.PlayArrow,
                        contentDescription = "Başlat",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLive) "Dərsə Qayıt" else "Dərsi Başlat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
