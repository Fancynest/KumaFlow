import sys
import glob

files = glob.glob('app/src/main/java/com/bearbones/kumaflow/ui/screens/*.kt')
for path in files:
    with open(path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Change stickyHeader background
    content = content.replace('.background(AppBg())', '.background(if (androidx.compose.foundation.isSystemInDarkTheme()) androidx.compose.ui.graphics.Color.Black.copy(alpha=0.2f) else androidx.compose.ui.graphics.Color.White.copy(alpha=0.4f))')

    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

print("Updated screens")
