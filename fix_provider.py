import sys

path = 'app/src/main/java/com/bearbones/kumaflow/ui/screens/LockScreen.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

target1 = 'import com.bearbones.kumaflow.LocalIsAmoled'
replacement1 = 'import com.bearbones.kumaflow.LocalIsAmoled\nimport com.bearbones.kumaflow.LocalIsLiquidGlass'
content = content.replace(target1, replacement1)

target2 = '''            CompositionLocalProvider(
                LocalIsDark provides isDark,
                LocalIsAmoled provides isAmoled
            ) {'''
replacement2 = '''            CompositionLocalProvider(
                LocalIsDark provides isDark,
                LocalIsAmoled provides isAmoled,
                LocalIsLiquidGlass provides (userProfile?.isLiquidGlass == true)
            ) {'''
content = content.replace(target2, replacement2)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done fixing provider.")
