import re

def check_balance():
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
    balance = 0
    for i, line in enumerate(lines):
        balance += line.count('{')
        balance -= line.count('}')
        if balance < 0:
            print(f'Negative balance at line {i+1}: {line.strip()}')
            return
    print(f'Final balance: {balance}')

if __name__ == "__main__":
    check_balance()
