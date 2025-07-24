package com.example.rkwthringenapp.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Ein durchdachtes "Professional Dark" Farbschema
private val RkwProfessionalDarkColorScheme = darkColorScheme(
    primary = App_Accent_Orange,
    onPrimary = App_Text_White,
    background = App_Background_Dark,
    onBackground = App_Text_White,
    surface = App_Surface_Gray, // Haupt-Fläche für Inhalte
    onSurface = App_Text_White,
    surfaceContainerLowest = App_Background_Dark,
    outline = App_Text_Gray,
    error = App_Error_Red,
    secondaryContainer = App_Status_Entwurf_Bar, // Farbe für Entwurf-Header
    tertiaryContainer = App_Status_Gesendet_Bar  // Farbe für Gesendet-Header
)

@Composable
fun RKWThüringenAppTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = RkwProfessionalDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}