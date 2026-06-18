import sys

path1 = 'app/src/main/java/com/bearbones/kumaflow/MainActivity.kt'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target = 'import androidx.compose.ui.graphics.Color'
replacement = 'import androidx.compose.ui.graphics.Color\nimport androidx.compose.ui.graphics.luminance'
content1 = content1.replace(target, replacement)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Fixed luminance import.")
