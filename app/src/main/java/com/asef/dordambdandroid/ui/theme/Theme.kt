package com.asef.dordambdandroid.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary                = IndigoPrimary,
    onPrimary              = IndigoOnPrimary,
    primaryContainer       = IndigoPrimaryContainer,
    onPrimaryContainer     = IndigoOnPrimaryContainer,
    secondary              = IndigoSecondary,
    onSecondary            = IndigoOnSecondary,
    secondaryContainer     = IndigoSecondaryContainer,
    onSecondaryContainer   = IndigoOnSecondaryContainer,
    tertiary               = IndigoTertiary,
    onTertiary             = IndigoOnTertiary,
    tertiaryContainer      = IndigoTertiaryContainer,
    onTertiaryContainer    = IndigoOnTertiaryContainer,
    error                  = IndigoError,
    onError                = IndigoOnError,
    errorContainer         = IndigoErrorContainer,
    onErrorContainer       = IndigoOnErrorContainer,
    background             = IndigoBackground,
    onBackground           = IndigoOnBackground,
    surface                = IndigoSurface,
    onSurface              = IndigoOnSurface,
    surfaceVariant         = IndigoSurfaceVariant,
    onSurfaceVariant       = IndigoOnSurfaceVariant,
    outline                = IndigoOutline,
    outlineVariant         = IndigoOutlineVariant,
    scrim                  = IndigoScrim,
    inverseSurface         = IndigoInverseSurface,
    inverseOnSurface       = IndigoInverseOnSurface,
    inversePrimary         = IndigoInversePrimary,
)

private val DarkColorScheme = darkColorScheme(
    primary                = IndigoPrimaryDark,
    onPrimary              = IndigoOnPrimaryDark,
    primaryContainer       = IndigoPrimaryContainerDark,
    onPrimaryContainer     = IndigoOnPrimaryContainerDark,
    secondary              = IndigoSecondaryDark,
    onSecondary            = IndigoOnSecondaryDark,
    secondaryContainer     = IndigoSecondaryContainerDark,
    onSecondaryContainer   = IndigoOnSecondaryContainerDark,
    tertiary               = IndigoTertiaryDark,
    onTertiary             = IndigoOnTertiaryDark,
    tertiaryContainer      = IndigoTertiaryContainerDark,
    onTertiaryContainer    = IndigoOnTertiaryContainerDark,
    error                  = IndigoErrorDark,
    onError                = IndigoOnErrorDark,
    errorContainer         = IndigoErrorContainerDark,
    onErrorContainer       = IndigoOnErrorContainerDark,
    background             = IndigoBackgroundDark,
    onBackground           = IndigoOnBackgroundDark,
    surface                = IndigoSurfaceDark,
    onSurface              = IndigoOnSurfaceDark,
    surfaceVariant         = IndigoSurfaceVariantDark,
    onSurfaceVariant       = IndigoOnSurfaceVariantDark,
    outline                = IndigoOutlineDark,
    outlineVariant         = IndigoOutlineVariantDark,
    scrim                  = IndigoScrimDark,
    inverseSurface         = IndigoInverseSurfaceDark,
    inverseOnSurface       = IndigoInverseOnSurfaceDark,
    inversePrimary         = IndigoInversePrimaryDark,
)

// ── Custom extended colors ──────────────────────────────────────────────────
@Immutable
data class ExtendedColors(
    val trendUp: Color = Color.Unspecified,
    val trendDown: Color = Color.Unspecified,
)

val LocalExtendedColors = staticCompositionLocalOf { ExtendedColors() }

// ── Custom extended typography ──────────────────────────────────────────────
@Immutable
data class ExtendedTypography(
    val price: TextStyle = TextStyle.Default,
)

val LocalExtendedTypography = staticCompositionLocalOf { ExtendedTypography() }

@Composable
fun DorDamBDAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val extendedColors = if (darkTheme) {
        ExtendedColors(trendUp = DarkTrendUp, trendDown = DarkTrendDown)
    } else {
        ExtendedColors(trendUp = LightTrendUp, trendDown = LightTrendDown)
    }

    val extendedTypography = ExtendedTypography(
        price = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.02.sp
        )
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalExtendedTypography provides extendedTypography,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}