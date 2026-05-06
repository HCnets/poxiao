import sys

def trace_braces(filepath, start_line, end_line):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    depth = 0
    for i, line in enumerate(lines):
        prev_depth = depth
        for char in line:
            if char == '{':
                depth += 1
            elif char == '}':
                depth -= 1
        
        if start_line <= i+1 <= end_line:
            try:
                print(f"{i+1:4d} | {prev_depth}->{depth} | {line.rstrip()}")
            except UnicodeEncodeError:
                # Fallback for console encoding issues
                print(f"{i+1:4d} | {prev_depth}->{depth} | {line.rstrip().encode('ascii', 'replace').decode('ascii')}")

if __name__ == '__main__':
    trace_braces(sys.argv[1], int(sys.argv[2]), int(sys.argv[3]))
