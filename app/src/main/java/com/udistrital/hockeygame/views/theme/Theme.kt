package com.udistrital.hockeygame.views.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CasinoColorScheme = darkColorScheme(
    primary = CasinoGold,
    onPrimary = CasinoDarkFelt,
    primaryContainer = CasinoGoldDark,
    onPrimaryContainer = CasinoGoldLight,
    secondary = CasinoEmeraldLight,
    onSecondary = CasinoDarkFelt,
    background = CasinoDarkFelt,
    onBackground = CasinoTextWhite,
    surface = CasinoFeltGreen,
    onSurface = CasinoTextWhite,
    surfaceVariant = CasinoEmerald,
    onSurfaceVariant = CasinoTextMuted,
    error = CasinoRed,
    onError = CasinoTextWhite
)

@Composable
fun HockeyGameTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CasinoColorScheme,
        typography = Typography,
        content = content
    )
}
