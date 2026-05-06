import sys

def check_braces(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    depth = 0
    lines = content.split('\n')
    for i, line in enumerate(lines):
        for char in line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
        if depth == 0 and line.strip() == "}":
            print(f"Top-level brace closed at line {i+1}")
        if line.startswith("@Composable"):
            print(f"Composable at line {i+1} starts with depth {depth}")

check_braces(r"C:\Users\HCnets\Desktop\AI\app\src\main\java\com\poxiao\app\calculator\ScientificCalculator.kt")