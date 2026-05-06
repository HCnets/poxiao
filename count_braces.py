import re

def count_braces():
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

    print("Total {:", content.count('{'))
    print("Total }:", content.count('}'))

if __name__ == "__main__":
    count_braces()
