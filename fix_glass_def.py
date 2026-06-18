import sys

path1 = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

old_glass_def = '''fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        this.glassmorphic(radius)
    } else {
        this.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius)).background(fallbackColor)
    }
}'''

new_glass_def = '''fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color,
    useHaze: Boolean = true
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val glassColor = if (LocalIsDark.current) {
            if (fallbackColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.2f) else fallbackColor.copy(alpha = 0.3f)
        } else {
            if (fallbackColor.luminance() < 0.5f) Color.White.copy(alpha = 0.2f) else fallbackColor.copy(alpha = 0.3f)
        }
        this.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius))
            .let { if (useHaze) it.hazeChild(state = LocalHazeState.current) else it }
            .background(glassColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), androidx.compose.foundation.shape.RoundedCornerShape(radius))
    } else {
        this.clip(androidx.compose.foundation.shape.RoundedCornerShape(radius)).background(fallbackColor)
    }
}'''

content1 = content1.replace(old_glass_def, new_glass_def)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Fixed glassCard definition.")
