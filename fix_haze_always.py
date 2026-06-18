import sys

path1 = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target = 'modifier = Modifier.let { if (LocalIsLiquidGlass.current) it.haze(state = hazeState) else it },'
replacement = 'modifier = Modifier.haze(state = hazeState),'
content1 = content1.replace(target, replacement)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Always applying haze to Scaffold.")
