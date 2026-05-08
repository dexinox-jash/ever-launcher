package com.everlauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.everlauncher.domain.model.FontChoice
import com.everlauncher.domain.model.FontSize
import com.everlauncher.domain.model.ThemePreference

// Light color scheme
private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary,
    primary = LightAccent,
    onPrimary = LightBackground,
    outline = LightBorder,
    error = LightDestructive,
)

// Dark color scheme
private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary,
    primary = DarkAccent,
    onPrimary = DarkBackground,
    outline = DarkBorder,
    error = DarkDestructive,
)

// AMOLED color scheme
private val AmoledColorScheme = darkColorScheme(
    background = AmoledBackground,
    onBackground = AmoledTextPrimary,
    surface = AmoledSurface,
    onSurface = AmoledTextPrimary,
    onSurfaceVariant = AmoledTextSecondary,
    primary = AmoledAccent,
    onPrimary = AmoledBackground,
    outline = AmoledBorder,
    error = AmoledDestructive,
)

val LocalThemePreference = staticCompositionLocalOf { ThemePreference.SYSTEM }

/** Scales a base [TextUnit] by the user's font size preference. */
private fun TextUnit.scaled(fontSize: FontSize): TextUnit = when (fontSize) {
    FontSize.SMALL  -> (value * 0.9f).sp
    FontSize.MEDIUM -> this
    FontSize.LARGE  -> (value * 1.15f).sp
}

/** Builds a full Typography derived from [EverTypography] with the given user preferences applied. */
fun buildTypography(fontChoice: FontChoice, fontSize: FontSize): Typography {
    val bodyFamily = when (fontChoice) {
        FontChoice.SYSTEM    -> FontFamily.Default
        FontChoice.SERIF     -> FontFamily.Serif
        FontChoice.MONOSPACE -> FontFamily.Monospace
        FontChoice.ROUNDED   -> FontFamily.Default   // no built-in Rounded; stays Default
    }
    val base = EverTypography
    return base.copy(
        displayLarge   = base.displayLarge.copy(fontFamily = FontFamily.Serif,  fontSize = base.displayLarge.fontSize.scaled(fontSize)),
        headlineMedium = base.headlineMedium.copy(fontFamily = FontFamily.Serif, fontSize = base.headlineMedium.fontSize.scaled(fontSize)),
        bodyLarge      = base.bodyLarge.copy(fontFamily = bodyFamily,  fontSize = base.bodyLarge.fontSize.scaled(fontSize)),
        bodyMedium     = base.bodyMedium.copy(fontFamily = bodyFamily, fontSize = base.bodyMedium.fontSize.scaled(fontSize)),
        bodySmall      = base.bodySmall.copy(fontFamily = bodyFamily,  fontSize = base.bodySmall.fontSize.scaled(fontSize)),
        labelSmall     = base.labelSmall.copy(fontSize = base.labelSmall.fontSize.scaled(fontSize)),
        titleLarge     = base.titleLarge.copy(fontFamily = bodyFamily, fontSize = base.titleLarge.fontSize.scaled(fontSize)),
        titleMedium    = base.titleMedium.copy(fontFamily = bodyFamily, fontSize = base.titleMedium.fontSize.scaled(fontSize)),
    )
}

@Composable
fun EverLauncherTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    fontChoice: FontChoice = FontChoice.SYSTEM,
    fontSize: FontSize = FontSize.MEDIUM,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themePreference) {
        ThemePreference.LIGHT  -> LightColorScheme
        ThemePreference.DARK   -> DarkColorScheme
        ThemePreference.AMOLED -> AmoledColorScheme
        ThemePreference.SYSTEM -> if (isSystemInDarkTheme()) DarkColorScheme else LightColorScheme
    }

    CompositionLocalProvider(LocalThemePreference provides themePreference) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = buildTypography(fontChoice, fontSize),
            content = content
        )
    }
}
