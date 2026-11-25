package com.skillexchange.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF34D399), // Vibrant emerald green for dark mode
    onPrimary = Color(0xFF064E3B),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondary = Color(0xFFFBBF24),
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = Color(0xFFFEF3C7),
    background = Color(0xFF0F172A), // Deep dark slate background
    onBackground = Color(0xFFF8FAFC), // Crisp white text
    surface = Color(0xFF1E293B), // Dark slate surface cards
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF334155),
    outlineVariant = Color(0xFF1E293B)
)

@Composable
fun SkillExchangeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
