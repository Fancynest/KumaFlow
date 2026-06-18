import sys

# 1. Update HomeScreen.kt
path1 = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/HomeScreen.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target1 = '''    selectedYear: Int,
    isPrivacyMode: Boolean,'''
replacement1 = '''    selectedYear: Int,
    paddingValues: PaddingValues,
    isPrivacyMode: Boolean,'''
content1 = content1.replace(target1, replacement1)

target2 = '''        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 24.dp)
        ) {'''
replacement2 = '''        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(top = 24.dp),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp)
        ) {'''
content1 = content1.replace(target2, replacement2)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

# 2. Update ReportScreen.kt
path2 = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/ReportScreen.kt'
with open(path2, 'r', encoding='utf-8') as f:
    content2 = f.read()

target3 = '''    selectedYear: Int,
    onDateChanged: (Pair<Int, Int>) -> Unit
) {'''
replacement3 = '''    selectedYear: Int,
    paddingValues: PaddingValues,
    onDateChanged: (Pair<Int, Int>) -> Unit
) {'''
content2 = content2.replace(target3, replacement3)

target4 = '''        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)
        ) {'''
replacement4 = '''        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp)
        ) {'''
content2 = content2.replace(target4, replacement4)

with open(path2, 'w', encoding='utf-8') as f:
    f.write(content2)

# 3. Update SettingsScreen.kt
path3 = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/SettingsScreen.kt'
with open(path3, 'r', encoding='utf-8') as f:
    content3 = f.read()

target5 = '''    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    onForceUpdate: () -> Unit
) {'''
replacement5 = '''    haptic: androidx.compose.ui.hapticfeedback.HapticFeedback,
    paddingValues: PaddingValues,
    onForceUpdate: () -> Unit
) {'''
content3 = content3.replace(target5, replacement5)

target6 = '''    Box(modifier = Modifier.fillMaxSize().background(AppBg())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {'''
replacement6 = '''    Box(modifier = Modifier.fillMaxSize().background(AppBg())) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding() + 24.dp)
        ) {'''
content3 = content3.replace(target6, replacement6)

with open(path3, 'w', encoding='utf-8') as f:
    f.write(content3)

print("Updated screens.")
