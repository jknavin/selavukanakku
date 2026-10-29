package com.selavukanakku.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
class GlassPalette(
    val dark: Boolean,
    val bgTop: Color,
    val bgBottom: Color,
    val blobA: Color,
    val blobB: Color,
    val blobC: Color,
    val glass: Color,
    val glassStrong: Color,
    val border: Color,
    val outline: Color,
    val text: Color,
    val textDim: Color,
    val income: Color,
    val expense: Color,
    val accent: Color,
    val accent2: Color,
    val dialog: Color,
    val bar: Color
)

val DarkPalette = GlassPalette(
    dark = true,
    bgTop = Color(0xFF0B1020),
    bgBottom = Color(0xFF1A1245),
    blobA = Color(0xFF7C3AED),
    blobB = Color(0xFF06B6D4),
    blobC = Color(0xFFEC4899),
    glass = Color.White.copy(alpha = 0.09f),
    glassStrong = Color.White.copy(alpha = 0.16f),
    border = Color.White.copy(alpha = 0.24f),
    outline = Color.White.copy(alpha = 0.30f),
    text = Color(0xFFF8FAFC),
    textDim = Color(0xFFF8FAFC).copy(alpha = 0.68f),
    income = Color(0xFF34D399),
    expense = Color(0xFFFB7185),
    accent = Color(0xFF818CF8),
    accent2 = Color(0xFFC084FC),
    dialog = Color(0xFF191636),
    bar = Color(0xFF1B1745).copy(alpha = 0.92f)
)

val LightPalette = GlassPalette(
    dark = false,
    bgTop = Color(0xFFE8EDFF),
    bgBottom = Color(0xFFFDEBF5),
    blobA = Color(0xFF818CF8),
    blobB = Color(0xFF22D3EE),
    blobC = Color(0xFFF472B6),
    glass = Color.White.copy(alpha = 0.55f),
    glassStrong = Color.White.copy(alpha = 0.78f),
    border = Color.White.copy(alpha = 0.95f),
    outline = Color(0xFFCBD5E1),
    text = Color(0xFF0F172A),
    textDim = Color(0xFF475569),
    income = Color(0xFF059669),
    expense = Color(0xFFE11D48),
    accent = Color(0xFF4F46E5),
    accent2 = Color(0xFF9333EA),
    dialog = Color(0xFFFFFFFF),
    bar = Color.White.copy(alpha = 0.92f)
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

@Composable
fun SelavuTheme(dark: Boolean, content: @Composable () -> Unit) {
    val p = if (dark) DarkPalette else LightPalette
    val scheme = if (dark) {
        darkColorScheme(
            primary = p.accent,
            onPrimary = Color.White,
            surface = p.dialog,
            onSurface = p.text,
            surfaceVariant = p.dialog,
            onSurfaceVariant = p.textDim,
            background = p.bgTop,
            onBackground = p.text,
            secondaryContainer = p.accent.copy(alpha = 0.35f),
            onSecondaryContainer = p.text
        )
    } else {
        lightColorScheme(
            primary = p.accent,
            onPrimary = Color.White,
            surface = p.dialog,
            onSurface = p.text,
            surfaceVariant = Color(0xFFF1F5F9),
            onSurfaceVariant = p.textDim,
            background = p.bgTop,
            onBackground = p.text,
            secondaryContainer = p.accent.copy(alpha = 0.18f),
            onSecondaryContainer = p.text
        )
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

/** Soft gradient with colour blobs behind the glass cards. */
@Composable
fun GlassBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(p.bgTop, p.bgBottom)))
            .drawBehind {
                val w = size.width
                val h = size.height
                fun blob(color: Color, cx: Float, cy: Float, r: Float) {
                    val center = Offset(cx, cy)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.55f), Color.Transparent),
                            center = center,
                            radius = r
                        ),
                        radius = r,
                        center = center
                    )
                }
                blob(p.blobA, w * 0.10f, h * 0.10f, w * 0.85f)
                blob(p.blobB, w * 0.95f, h * 0.45f, w * 0.75f)
                blob(p.blobC, w * 0.20f, h * 0.88f, w * 0.80f)
            },
        content = content
    )
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    corner: Dp = 26.dp,
    padding: Dp = 18.dp,
    strong: Boolean = false,
    fill: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(corner)
    var m = modifier
        .clip(shape)
        .background(fill ?: if (strong) p.glassStrong else p.glass)
        .border(
            1.dp,
            Brush.linearGradient(listOf(p.border, p.border.copy(alpha = p.border.alpha * 0.22f))),
            shape
        )
    if (onClick != null) m = m.clickable(onClick = onClick)
    Column(modifier = m.padding(padding), content = content)
}

@Composable
fun GlassCircleButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    val p = LocalPalette.current
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(p.glassStrong)
            .border(BorderStroke(1.dp, p.border), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = { content() }
    )
}

@Composable
fun GradientButton(
    text: String,
    icon: ImageVector,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(colors))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            text,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun Pill(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val p = LocalPalette.current
    val shape = RoundedCornerShape(50)
    val base = modifier.clip(shape)
    val styled = if (selected) {
        base.background(Brush.horizontalGradient(listOf(p.accent, p.accent2)))
    } else {
        base.background(p.glass).border(1.dp, p.outline.copy(alpha = 0.6f), shape)
    }
    Box(
        modifier = styled.clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else p.text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    val p = LocalPalette.current
    Text(
        text,
        modifier = modifier.padding(top = 4.dp, start = 4.dp),
        color = p.text,
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun fieldColors(): TextFieldColors {
    val p = LocalPalette.current
    return OutlinedTextFieldDefaults.colors(
        focusedTextColor = p.text,
        unfocusedTextColor = p.text,
        focusedBorderColor = p.accent,
        unfocusedBorderColor = p.outline,
        errorBorderColor = p.expense,
        focusedLabelColor = p.accent,
        unfocusedLabelColor = p.textDim,
        cursorColor = p.accent,
        focusedPrefixColor = p.text,
        unfocusedPrefixColor = p.text
    )
}
