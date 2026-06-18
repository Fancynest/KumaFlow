import sys

# 1. Update build.gradle.kts
path1 = 'app/build.gradle.kts'
with open(path1, 'r', encoding='utf-8') as f:
    content1 = f.read()

target1 = 'implementation("dev.chrisbanes.haze:haze:1.1.1")'
replacement1 = '''implementation("dev.chrisbanes.haze:haze:1.1.1")
    implementation("dev.chrisbanes.haze:haze-materials:1.1.1")
    implementation("dev.chrisbanes.haze:haze-blur:1.1.1")'''
content1 = content1.replace(target1, replacement1)

with open(path1, 'w', encoding='utf-8') as f:
    f.write(content1)

print("Updated build.gradle.kts")
