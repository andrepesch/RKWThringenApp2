package com.example.rkwthringenapp.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Ein helles Farbschema, das die RKW CI-Farben verwendet
private val RkwLightColorScheme = lightColorScheme(
    primary = RKW_Orange,               // Haupt-Akzentfarbe für Buttons etc.
    background = RKW_White,             // Hintergrund der App
    surface = RKW_White,                // Oberfläche von Karten etc.
    onPrimary = RKW_White,              // Text auf primärfarbigen Flächen (z.B. Buttons)
    onBackground = RKW_Dark_Gray,       // Standard-Textfarbe
    onSurface = RKW_Dark_Gray,          // Standard-Textfarbe
    outline = RKW_Light_Gray,           // Rahmen von Eingabefeldern
    error = RKW_Error_Red               // Farbe für Fehlertexte
)

@Composable
fun RKWThüringenAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // darkTheme aktuell nicht unterstützt
    content: @Composable () -> Unit
) {
    val colorScheme = RkwLightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}