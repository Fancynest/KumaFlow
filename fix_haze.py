import sys

path = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Add imports
content = content.replace(
    'import androidx.compose.ui.draw.blur',
    'import dev.chrisbanes.haze.*\nimport androidx.compose.ui.draw.blur'
)

# Add LocalHazeState
content = content.replace(
    'val LocalIsLiquidGlass = compositionLocalOf { false }',
    'val LocalIsLiquidGlass = compositionLocalOf { false }\nval LocalHazeState = compositionLocalOf { HazeState() }'
)

# Add HazeState to MainScreen
target1 = '''    val totalTxCount = transactionListWithSplits.size

    Scaffold(
        containerColor = AppBg(),'''

replacement1 = '''    val totalTxCount = transactionListWithSplits.size

    val hazeState = remember { HazeState() }
    CompositionLocalProvider(LocalHazeState provides hazeState) {
        Scaffold(
            modifier = Modifier.let { if (LocalIsLiquidGlass.current) it.haze(state = hazeState) else it },
            containerColor = AppBg(),'''

content = content.replace(target1, replacement1)

# Close CompositionLocalProvider at the end of MainScreen
target2 = '''        if (showBackupReminder) {
            AlertDialog(
                onDismissRequest = { showBackupReminder = false },
                title = { Text(AppStr.backupReminderTitle, fontWeight = FontWeight.Black) },
                text = { Text(AppStr.backupReminderMsg) },
                confirmButton = {
                    Button(
                        onClick = { showBackupReminder = false; backupAppToJSON(context, userProfile, transactionListWithSplits) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary())
                    ) { Text(AppStr.backupNow, color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showBackupReminder = false }) { Text(AppStr.later, color = AppText()) } },
                shape = RoundedCornerShape(28.dp), containerColor = AppSurface(), titleContentColor = AppText(), textContentColor = AppText()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)'''

replacement2 = '''        if (showBackupReminder) {
            AlertDialog(
                onDismissRequest = { showBackupReminder = false },
                title = { Text(AppStr.backupReminderTitle, fontWeight = FontWeight.Black) },
                text = { Text(AppStr.backupReminderMsg) },
                confirmButton = {
                    Button(
                        onClick = { showBackupReminder = false; backupAppToJSON(context, userProfile, transactionListWithSplits) },
                        colors = ButtonDefaults.buttonColors(containerColor = AppPrimary())
                    ) { Text(AppStr.backupNow, color = Color.White) }
                },
                dismissButton = { TextButton(onClick = { showBackupReminder = false }) { Text(AppStr.later, color = AppText()) } },
                shape = RoundedCornerShape(28.dp), containerColor = AppSurface(), titleContentColor = AppText(), textContentColor = AppText()
            )
        }
    }
    }
}

@OptIn(ExperimentalFoundationApi::class)'''

content = content.replace(target2, replacement2)

# Now apply hazeChild to glassCard
target3 = '''@Composable
fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        this.glassmorphic(radius)
    } else {
        this.clip(RoundedCornerShape(radius)).background(fallbackColor)
    }
}

@Composable
fun Modifier.glassmorphic(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    borderAlpha: Float = 0.2f
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val glassColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = 0.05f)
        } else {
            Color.White.copy(alpha = 0.4f)
        }
        val borderColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = borderAlpha)
        } else {
            Color.White.copy(alpha = 0.5f)
        }
        
        this
            .clip(RoundedCornerShape(radius))
            .background(glassColor)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(radius)
            )
    } else {
        this
    }
}'''

replacement3 = '''@Composable
fun Modifier.glassCard(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    fallbackColor: Color,
    borderAlpha: Float = 0.2f
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val borderColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = borderAlpha)
        } else {
            Color.White.copy(alpha = 0.5f)
        }
        this.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(radius)
            )
    } else {
        this.clip(RoundedCornerShape(radius)).background(fallbackColor)
    }
}

@Composable
fun Modifier.glassmorphic(
    radius: androidx.compose.ui.unit.Dp = 16.dp,
    borderAlpha: Float = 0.2f
): Modifier {
    return if (LocalIsLiquidGlass.current) {
        val borderColor = if (LocalIsDark.current) {
            Color.White.copy(alpha = borderAlpha)
        } else {
            Color.White.copy(alpha = 0.5f)
        }
        this.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius))
            .border(
                width = 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(radius)
            )
    } else {
        this
    }
}'''

content = content.replace(target3, replacement3)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done replacing.")
