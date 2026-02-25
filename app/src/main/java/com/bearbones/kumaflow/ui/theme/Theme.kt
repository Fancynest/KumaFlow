package com.bearbones.kumaflow.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 1. Skema Warna Dark Mode
private val DarkColorScheme = darkColorScheme(
    primary = BearRust,
    background = PitchBlack,
    surface = CardDark, // Buat background kotak-kotak menu
    onPrimary = Color.White, // Teks di atas warna utama
    onBackground = CreamyText, // Teks biasa di Dark Mode
    onSurface = CreamyText
)

// 2. Skema Warna Light Mode
private val LightColorScheme = lightColorScheme(
    primary = DeepGrizzly,
    background = CreamyBelly,
    surface = CreamyBelly,
    onPrimary = Color.White,
    onBackground = DeepGrizzly,
    onSurface = DeepGrizzly
)

@Composable
fun KumaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // WAJIB FALSE: Biar warna HP Oppo Reno 7 temen-temen lo gak nimpa warna desain asli lo!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Ini buat ngubah warna status bar (jam & baterai di atas layar) biar nyatu sama background
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