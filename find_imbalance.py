import sys

def find_imbalance(filepath):
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
                
        if depth != 0 and i > 1000 and prev_depth == 0:
            print(f"Depth became 1 at line {i+1}: {line.rstrip()}")
            # Print a few lines context
            for j in range(max(0, i-5), min(len(lines), i+10)):
                print(f"{j+1:4d}: {lines[j].rstrip()}")
            break

if __name__ == '__main__':
    find_imbalance(sys.argv[1])
