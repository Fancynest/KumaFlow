import sys

path = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Fix glassCard
target1 = 'this.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius))'
replacement1 = 'this.clip(RoundedCornerShape(radius)).hazeChild(state = LocalHazeState.current)'
content = content.replace(target1, replacement1)

# Fix glassmorphic
target2 = 'this.hazeChild(state = LocalHazeState.current, shape = RoundedCornerShape(radius))'
content = content.replace(target2, replacement1)

# Fix FAB
target3 = 'it.hazeChild(state = LocalHazeState.current, shape = CircleShape)'
replacement3 = 'it.clip(CircleShape).hazeChild(state = LocalHazeState.current)'
content = content.replace(target3, replacement3)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)

print("Done fixing haze shape.")
