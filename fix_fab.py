import sys

path = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target = '''                FloatingActionButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); transactionToEdit = null; showBottomSheet = true },
                    containerColor = AppPrimary(), contentColor = Color.White, shape = CircleShape, modifier = Modifier.size(70.dp)
                ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp)) }'''

replacement = '''                FloatingActionButton(
                    onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); transactionToEdit = null; showBottomSheet = true },
                    containerColor = if (LocalIsLiquidGlass.current) Color.Transparent else AppPrimary(),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = if (LocalIsLiquidGlass.current) 0.dp else 6.dp),
                    contentColor = if (LocalIsLiquidGlass.current) AppPrimary() else Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(70.dp).let { if (LocalIsLiquidGlass.current) it.hazeChild(state = LocalHazeState.current, shape = CircleShape).border(1.dp, Color.White.copy(0.3f), CircleShape) else it }
                ) { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(40.dp)) }'''

content = content.replace(target, replacement)

target_nav = '''@Composable
fun CustomBottomNav(selectedItem: Int, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = AppSurfaceVariant(), tonalElevation = 8.dp) {'''

replacement_nav = '''@Composable
fun CustomBottomNav(selectedItem: Int, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback, onSelect: (Int) -> Unit) {
    NavigationBar(containerColor = if (LocalIsLiquidGlass.current) Color.Transparent else AppSurfaceVariant(), tonalElevation = if (LocalIsLiquidGlass.current) 0.dp else 8.dp, modifier = Modifier.let { if (LocalIsLiquidGlass.current) it.hazeChild(state = LocalHazeState.current) else it }) {'''

content = content.replace(target_nav, replacement_nav)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done FAB and Nav.")
