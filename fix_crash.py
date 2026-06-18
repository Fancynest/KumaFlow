import sys

# 1. Update MainActivity.kt
path1 = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

# Update glassCard definition
old_glass_def = '''fun Modifier.glassCard(
    radius: Dp,
    bgColor: Color
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val glassColor = if (LocalIsDark.current) {
            if (bgColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.2f) else bgColor.copy(alpha = 0.3f)
        } else {
            if (bgColor.luminance() < 0.5f) Color.White.copy(alpha = 0.2f) else bgColor.copy(alpha = 0.3f)
        }
        this.clip(RoundedCornerShape(radius))
            .hazeChild(state = LocalHazeState.current)
            .background(glassColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(radius))
    } else {
        this.clip(RoundedCornerShape(radius)).background(bgColor)
    }
}'''

new_glass_def = '''fun Modifier.glassCard(
    radius: Dp,
    bgColor: Color,
    useHaze: Boolean = true
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val glassColor = if (LocalIsDark.current) {
            if (bgColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.2f) else bgColor.copy(alpha = 0.3f)
        } else {
            if (bgColor.luminance() < 0.5f) Color.White.copy(alpha = 0.2f) else bgColor.copy(alpha = 0.3f)
        }
        this.clip(RoundedCornerShape(radius))
            .let { if (useHaze) it.hazeChild(state = LocalHazeState.current) else it }
            .background(glassColor)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(radius))
    } else {
        this.clip(RoundedCornerShape(radius)).background(bgColor)
    }
}'''
content1 = content1.replace(old_glass_def, new_glass_def)

# Update TransactionBottomSheet usages
content1 = content1.replace('.glassCard(16.dp, AppSurfaceVariant())', '.glassCard(16.dp, AppSurfaceVariant(), useHaze = false)')
content1 = content1.replace('modifier = Modifier.height(150.dp).glassCard(8.dp, AppSurfaceVariant())', 'modifier = Modifier.height(150.dp).glassCard(8.dp, AppSurfaceVariant(), useHaze = false)')
content1 = content1.replace('.glassCard(20.dp, AppSurfaceVariant())', '.glassCard(20.dp, AppSurfaceVariant(), useHaze = false)')

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)


# 2. Update SettingsScreen.kt
path2 = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/SettingsScreen.kt'
with open(path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

# Update AlertDialog usages
old_setting1 = '''                                .height(40.dp)
                                .glassCard(12.dp, AppSurfaceVariant())'''
new_setting1 = '''                                .height(40.dp)
                                .glassCard(12.dp, AppSurfaceVariant(), useHaze = false)'''
content2 = content2.replace(old_setting1, new_setting1)

old_setting2 = 'modifier = Modifier.height(120.dp).glassCard(8.dp, AppSurfaceVariant())'
new_setting2 = 'modifier = Modifier.height(120.dp).glassCard(8.dp, AppSurfaceVariant(), useHaze = false)'
content2 = content2.replace(old_setting2, new_setting2)

with open(path2, 'w', encoding='utf-8') as f:
    f.write(content2)

print("Fixes applied successfully.")
