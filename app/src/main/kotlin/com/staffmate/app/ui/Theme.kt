package com.staffmate.app.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Minimal, calm palette: deep teal primary, warm neutral surfaces.
// Chosen for legibility on factory-floor lighting conditions and a clean,
// professional feel appropriate for daily supervisor use.
private val Primary = Color(0xFF4FD1A5)
private val PrimaryContainer = Color(0xFF13342A)
private val Secondary = Color(0xFF8AB4C8)
private val Background = Color(0xFF101314)
private val Surface = Color(0xFF181C1D)
private val SurfaceVariant = Color(0xFF232829)
private val Error = Color(0xFFEF7B72)
private val OnSurfaceVariant = Color(0xFFA6B0B0)

private val StaffMateDarkColors = darkColorScheme(
    primary = Primary,
    onPrimary = Color(0xFF00382A),
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = Primary,
    secondary = Secondary,
    background = Background,
    onBackground = Color(0xFFE3E7E6),
    surface = Surface,
    onSurface = Color(0xFFE3E7E6),
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = Color(0xFF3A0906),
    outline = Color(0xFF3C4344)
)

private val StaffMateShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

private val StaffMateTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 23.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
)

@Composable
fun StaffMateTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = StaffMateDarkColors,
            typography = StaffMateTypography,
            shapes = StaffMateShapes,
            content = content
        )
    }
}

/** Semantic score colors, used wherever a score/trend value is displayed. */
object ScoreColors {
    val positive = Color(0xFF4FD1A5)
    val negative = Color(0xFFEF7B72)
    val neutral = Color(0xFFA6B0B0)
}
