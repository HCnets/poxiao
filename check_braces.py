import sys

def check_braces(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    depth = 0
    for i, line in enumerate(lines):
        for char in line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
        
        # A top-level function or class should start at depth 0
        if line.strip().startswith('@Composable') or line.strip().startswith('fun ') or line.strip().startswith('private fun ') or line.strip().startswith('class ') or line.strip().startswith('private class '):
            if depth != 0 and '}' not in line:
                print(f"Warning: Top-level declaration at line {i+1} but depth is {depth}: {line.strip()}")
                
    print(f"Final depth: {depth}")

if __name__ == '__main__':
    check_braces('app/src/main/java/com/poxiao/app/calculator/ScientificCalculator.kt')
