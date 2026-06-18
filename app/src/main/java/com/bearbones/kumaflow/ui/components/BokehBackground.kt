package com.bearbones.kumaflow.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.bearbones.kumaflow.LocalIsDark

@Composable
fun BokehBackground() {
    val isDark = LocalIsDark.current
    
    // Aesthetic dynamic colors for the bokeh mesh gradient
    val color1 = if (isDark) Color(0xFF673AB7).copy(alpha = 0.5f) else Color(0xFFFFB74D).copy(alpha = 0.4f)
    val color2 = if (isDark) Color(0xFF00695C).copy(alpha = 0.5f) else Color(0xFF4DD0E1).copy(alpha = 0.4f)
    val color3 = if (isDark) Color(0xFFC62828).copy(alpha = 0.4f) else Color(0xFFF06292).copy(alpha = 0.4f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color1, Color.Transparent),
                center = Offset(width * 0.2f, height * 0.1f),
                radius = width * 0.9f
            ),
            radius = width * 0.9f,
            center = Offset(width * 0.2f, height * 0.1f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color2, Color.Transparent),
                center = Offset(width * 0.8f, height * 0.6f),
                radius = width * 1.0f
            ),
            radius = width * 1.0f,
            center = Offset(width * 0.8f, height * 0.6f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color3, Color.Transparent),
                center = Offset(width * 0.3f, height * 0.9f),
                radius = width * 0.8f
            ),
            radius = width * 0.8f,
            center = Offset(width * 0.3f, height * 0.9f)
        )
    }
}
