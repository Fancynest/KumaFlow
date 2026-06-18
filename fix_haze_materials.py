import sys

path = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add imports
imports = '''import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.HazeMaterials
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi'''

content = content.replace('import dev.chrisbanes.haze.HazeState\nimport dev.chrisbanes.haze.haze\nimport dev.chrisbanes.haze.hazeChild', imports)

# 2. Update glassCard
target_glassCard = '''        @Composable
        fun Modifier.glassCard(radius: Dp, fallbackColor: Color, useHaze: Boolean = false): Modifier {
            val glassColor = if (LocalIsDark.current) {
                if (fallbackColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.2f) else fallbackColor.copy(alpha = 0.3f)
            } else {
                if (fallbackColor.luminance() < 0.5f) Color.White.copy(alpha = 0.3f) else fallbackColor.copy(alpha = 0.3f)
            }
            return this
                .clip(RoundedCornerShape(radius))
                .let { if (useHaze) it.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius)) else it }
                .background(glassColor)
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(radius))
        }'''

replacement_glassCard = '''        @OptIn(ExperimentalHazeMaterialsApi::class)
        @Composable
        fun Modifier.glassCard(radius: Dp, fallbackColor: Color, useHaze: Boolean = false): Modifier {
            val glassColor = if (LocalIsDark.current) {
                if (fallbackColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.2f) else fallbackColor.copy(alpha = 0.3f)
            } else {
                if (fallbackColor.luminance() < 0.5f) Color.White.copy(alpha = 0.3f) else fallbackColor.copy(alpha = 0.3f)
            }
            return this
                .clip(RoundedCornerShape(radius))
                .let { 
                    if (useHaze) {
                        it.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius), style = HazeMaterials.regular())
                    } else {
                        it.background(glassColor)
                    }
                }
                .border(1.dp, Color.White.copy(0.1f), RoundedCornerShape(radius))
        }'''

content = content.replace(target_glassCard, replacement_glassCard)

# 3. Update FAB
target_fab = '''                modifier = Modifier
                    .hazeChild(state = LocalHazeState.current, shape = CircleShape)
                    .background(Color.Transparent)'''

replacement_fab = '''                modifier = Modifier
                    .hazeChild(state = LocalHazeState.current, shape = CircleShape, style = HazeMaterials.regular())'''

content = content.replace(target_fab, replacement_fab)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated MainActivity.kt with HazeMaterials.")
