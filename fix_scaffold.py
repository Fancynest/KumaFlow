import sys

path = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# 1. Add import for BokehBackground
content = content.replace('import com.bearbones.kumaflow.ui.screens.SettingsScreen', 'import com.bearbones.kumaflow.ui.screens.SettingsScreen\nimport com.bearbones.kumaflow.ui.components.BokehBackground')

# 2. Add BokehBackground behind the Scaffold
target_scaffold = '''        CompositionLocalProvider(LocalHazeState provides hazeState) {
            Scaffold(
                containerColor = AppBg(),'''

replacement_scaffold = '''        CompositionLocalProvider(LocalHazeState provides hazeState) {
          Box(modifier = Modifier.fillMaxSize().background(AppBg())) {
            BokehBackground()
            Scaffold(
                containerColor = Color.Transparent,'''

content = content.replace(target_scaffold, replacement_scaffold)

# 3. Close the Box after Scaffold
target_close = '''                }
            }
        }
    }'''

replacement_close = '''                }
            }
          }
        }
    }'''
content = content.replace(target_close, replacement_close)

# 4. Remove Haze modifier from the Box that holds HorizontalPager because the user doesn't want frosted glass
target_box_haze = '''            Box(modifier = Modifier.fillMaxSize().haze(state = hazeState)) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->'''

replacement_box_haze = '''            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->'''

content = content.replace(target_box_haze, replacement_box_haze)


# 5. Modify glassCard to just be normal translucent cards
target_glassCard = '''        @OptIn(ExperimentalHazeMaterialsApi::class)
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

replacement_glassCard = '''        @Composable
        fun Modifier.glassCard(radius: Dp, fallbackColor: Color, useHaze: Boolean = false): Modifier {
            val glassColor = if (LocalIsDark.current) {
                if (fallbackColor.luminance() > 0.5f) Color.Black.copy(alpha = 0.4f) else fallbackColor.copy(alpha = 0.5f)
            } else {
                if (fallbackColor.luminance() < 0.5f) Color.White.copy(alpha = 0.4f) else fallbackColor.copy(alpha = 0.5f)
            }
            return this
                .clip(RoundedCornerShape(radius))
                .background(glassColor)
                .border(1.dp, Color.White.copy(0.2f), RoundedCornerShape(radius))
        }'''
content = content.replace(target_glassCard, replacement_glassCard)

# 6. FAB modification to remove haze
target_fab = '''                modifier = Modifier
                    .hazeChild(state = LocalHazeState.current, shape = CircleShape, style = HazeMaterials.regular())
            ) {'''

replacement_fab = '''                modifier = Modifier
                    .background(if (LocalIsDark.current) Color.Black.copy(alpha=0.4f) else Color.White.copy(alpha=0.4f), CircleShape)
                    .border(1.dp, Color.White.copy(0.2f), CircleShape)
            ) {'''
content = content.replace(target_fab, replacement_fab)

target_fab_color = '''                containerColor = AppBg(),'''
replacement_fab_color = '''                containerColor = Color.Transparent,'''
content = content.replace(target_fab_color, replacement_fab_color)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated MainActivity.kt")
