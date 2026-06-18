import sys

# 1. Update glassCard definition to default useHaze = false
path1 = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target1 = '''fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color,
    useHaze: Boolean = true'''
replacement1 = '''fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color,
    useHaze: Boolean = false'''
content1 = content1.replace(target1, replacement1)

# 2. Update CustomBottomNav to useHaze = true
target2 = '.glassCard(24.dp, AppSurface())'
replacement2 = '.glassCard(24.dp, AppSurface(), useHaze = true)'
content1 = content1.replace(target2, replacement2)

# 3. Update Scaffold to NOT have haze, and Box to HAVE haze, and Box to NOT have paddingValues
target3 = '''        Scaffold(
            modifier = Modifier.haze(state = hazeState),'''
replacement3 = '''        Scaffold('''
content1 = content1.replace(target3, replacement3)

target4 = '''        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {'''
replacement4 = '''        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().haze(state = hazeState)) {'''
content1 = content1.replace(target4, replacement4)

# 4. Pass paddingValues to screens
# Wait, let's just pass the padding bottom to the screens!
# HorizontalPager doesn't have contentPadding, so we must pass it to the screens.
# HomeScreen( ..., paddingValues = paddingValues )
target5 = 'selectedYear = selectedYear,'
replacement5 = 'selectedYear = selectedYear,\n                        paddingValues = paddingValues,'
content1 = content1.replace(target5, replacement5)

target6 = 'isPrideThemeActive = isPrideThemeActive'
replacement6 = 'isPrideThemeActive = isPrideThemeActive,\n                        paddingValues = paddingValues'
content1 = content1.replace(target6, replacement6)

# ReportScreen
target7 = 'onDateChanged = { selectedMonth = it.first; selectedYear = it.second }'
replacement7 = 'onDateChanged = { selectedMonth = it.first; selectedYear = it.second },\n                        paddingValues = paddingValues'
content1 = content1.replace(target7, replacement7)

# SettingsScreen
target8 = 'onForceUpdate = { forceUpdateTrigger++ }'
replacement8 = 'onForceUpdate = { forceUpdateTrigger++ },\n                        paddingValues = paddingValues'
content1 = content1.replace(target8, replacement8)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Updated MainActivity.kt")
