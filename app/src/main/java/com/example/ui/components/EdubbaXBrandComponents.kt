package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DifficultyLevel
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardDark
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldAccentLight
import com.example.ui.theme.SparkleAccent
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TerracottaWarning
import com.example.ui.theme.TextPrimary

/**
 * Custom 4-pointed sparkle star icon as requested in the specification.
 */
@Composable
fun SparkleIcon(
    modifier: Modifier = Modifier,
    color: Color = GoldAccent,
    size: Dp = 18.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val path = Path().apply {
            moveTo(cx, 0f)
            // Curve to right point
            quadraticTo(cx, cy, w, cy)
            // Curve to bottom point
            quadraticTo(cx, cy, cx, h)
            // Curve to left point
            quadraticTo(cx, cy, 0f, cy)
            // Curve back to top point
            quadraticTo(cx, cy, cx, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

/**
 * EdubbaX Official Brand Logo:
 * - "edubba" in serif styling with gold color (#C9A227)
 * - "X" in bold sans-serif with white color (#FFFFFF)
 * - 4-pointed sparkle icon positioned atop the "X"
 */
@Composable
fun EdubbaXLogo(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
    sparkleSize: Dp = 14.dp,
    showSubtitle: Boolean = false,
    subtitleText: String = "AI Müəllim Platforması"
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "edubba" in Serif Gold
        Text(
            text = "edubba",
            color = GoldAccent,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
            letterSpacing = (-0.5).sp
        )

        // "X" in Bold Sans White with 4-pointed sparkle
        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Text(
                text = "X",
                color = TextPrimary,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = (fontSize.value * 1.12f).sp,
                modifier = Modifier.padding(end = 4.dp)
            )
            SparkleIcon(
                color = GoldAccentLight,
                size = sparkleSize,
                modifier = Modifier
                    .offset(x = 6.dp, y = (-4).dp)
            )
        }
    }
}

/**
 * Reusable AI Feature Badge with 4-pointed Sparkle icon.
 */
@Composable
fun AiSparkleBadge(
    text: String = "Gemini AI",
    modifier: Modifier = Modifier,
    accentColor: Color = GoldAccent
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            SparkleIcon(
                color = accentColor,
                size = 12.dp
            )
            Text(
                text = text,
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DifficultyBadge(
    difficulty: DifficultyLevel,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderCol) = when (difficulty) {
        DifficultyLevel.EASY -> Triple(EmeraldSuccess.copy(alpha = 0.15f), EmeraldSuccess, EmeraldSuccess.copy(alpha = 0.4f))
        DifficultyLevel.MEDIUM -> Triple(GoldAccent.copy(alpha = 0.15f), GoldAccentLight, GoldAccent.copy(alpha = 0.4f))
        DifficultyLevel.HARD -> Triple(AmberWarning.copy(alpha = 0.15f), AmberWarning, AmberWarning.copy(alpha = 0.4f))
        DifficultyLevel.EXPERT -> Triple(TerracottaWarning.copy(alpha = 0.15f), TerracottaWarning, TerracottaWarning.copy(alpha = 0.4f))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderCol, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = difficulty.label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
