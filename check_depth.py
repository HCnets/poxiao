import re

def check_depth():
    with open('app/src/main/java/com/poxiao/app/calculator/ScientificCalculator.kt', 'r', encoding='utf-8') as f:
        content = f.read()

    # Remove single line comments
    content = re.sub(r'//.*', '', content)
    # Remove multi-line comments
    content = re.sub(r'/\*.*?\*/', '', content, flags=re.DOTALL)
    
    # Remove strings
    content = re.sub(r'"""[\s\S]*?"""', '""', content)
    content = re.sub(r'"([^"\\]|\\.)*"', '""', content)
    content = re.sub(r"'([^'\\]|\\.)*'", "''", content)

    lines = content.split('\n')
    depth = 0
    for i, line in enumerate(lines):
        if line.startswith('@Composable') or line.startswith('private fun '):
            if depth > 0:
                print(f'Line {i+1} starts function but depth is {depth}')
        
        depth += line.count('{')
        depth -= line.count('}')

if __name__ == "__main__":
    check_depth()
