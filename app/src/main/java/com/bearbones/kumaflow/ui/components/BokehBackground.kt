package com.bearbones.kumaflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.bearbones.kumaflow.LocalIsDark
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BokehBackground(
    isPaused: Boolean = false,
    scrollOffsetProvider: () -> Float = { 0f }
) {
    val isDark = LocalIsDark.current
    
    // Aesthetic dynamic colors for the bokeh mesh gradient
    val color1 = if (isDark) Color(0xFF673AB7).copy(alpha = 0.5f) else Color(0xFFFFB74D).copy(alpha = 0.4f)
    val color2 = if (isDark) Color(0xFF00695C).copy(alpha = 0.5f) else Color(0xFF4DD0E1).copy(alpha = 0.4f)
    val color3 = if (isDark) Color(0xFFC62828).copy(alpha = 0.4f) else Color(0xFFF06292).copy(alpha = 0.4f)

    // Manual animation loop that pauses when `isPaused` is true.
    // This saves GPU and battery when the background is obscured.
    var timeMillis by remember { mutableLongStateOf(0L) }
    
    LaunchedEffect(isPaused) {
        if (!isPaused) {
            var lastTime = androidx.compose.runtime.withFrameNanos { it }
            while (isActive) {
                val currentTime = androidx.compose.runtime.withFrameNanos { it }
                timeMillis += (currentTime - lastTime) / 1000000L
                lastTime = currentTime
            }
        }
    }

    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        // Read animation time in the draw phase
        val t = timeMillis / 10000f // Slow, ambient movement

        // Read scroll offset directly in the draw phase to avoid recomposition (60 FPS Parallax)
        val parallaxOffset = scrollOffsetProvider() * 0.15f

        val width = size.width
        val height = size.height

        // Calculate dynamic centers based on time (Orbiting/Breathing) and parallax
        val cx1 = width * 0.2f + cos(t) * (width * 0.1f)
        val cy1 = height * 0.1f + sin(t) * (height * 0.1f) - parallaxOffset

        val cx2 = width * 0.8f + cos(t + 2f) * (width * 0.15f)
        val cy2 = height * 0.6f + sin(t + 2f) * (height * 0.15f) - (parallaxOffset * 0.8f) // Slightly slower parallax

        val cx3 = width * 0.3f + sin(t + 4f) * (width * 0.1f)
        val cy3 = height * 0.9f + cos(t + 4f) * (height * 0.1f) - (parallaxOffset * 1.2f)

        // Draw animated and parallax-shifted circles
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(cx1, cy1),
                radius = width * 0.9f
            ),
            radius = width * 0.9f,
            center = Offset(cx1, cy1)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(cx2, cy2),
                radius = width * 1.0f
            ),
            radius = width * 1.0f,
            center = Offset(cx2, cy2)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(cx3, cy3),
                radius = width * 0.8f
            ),
            radius = width * 0.8f,
            center = Offset(cx3, cy3)
        )
    }
}
