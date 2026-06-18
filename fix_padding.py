import sys

# 1. Update HomeScreen.kt
path1 = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/HomeScreen.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target1 = '''    selectedYear: Int,
    onMonthChange: (Int, Int) -> Unit,'''
replacement1 = '''    selectedYear: Int,
    paddingValues: PaddingValues,
    onMonthChange: (Int, Int) -> Unit,'''
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
    onMonthChange: (Int, Int) -> Unit,'''
replacement3 = '''    selectedYear: Int,
    paddingValues: PaddingValues,
    onMonthChange: (Int, Int) -> Unit,'''
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

target5 = '''    selectedYear: Int,
    onForceUpdate: () -> Unit
) {'''
replacement5 = '''    selectedYear: Int,
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

# 4. Import PaddingValues in HomeScreen, ReportScreen, SettingsScreen if not present
paths = [path1, path2, path3]
for p in paths:
    with open(p, 'r', encoding='utf-8') as f:
        c = f.read()
    if 'import androidx.compose.foundation.layout.PaddingValues' not in c:
        c = c.replace('import androidx.compose.foundation.layout.*', 'import androidx.compose.foundation.layout.*\nimport androidx.compose.foundation.layout.PaddingValues')
    with open(p, 'w', encoding='utf-8') as f:
        f.write(c)

print("PaddingValues fixed.")
