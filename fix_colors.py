import re

file_path = r'c:\Users\HCnets\Desktop\AI\app\src\main\java\com\poxiao\app\calculator\ScientificCalculator.kt'

with open(file_path, 'r', encoding='utf-8') as f:
    lines = f.readlines()

new_lines = []
in_composable = False
composable_start_idx = -1
has_is_dark_mode = False
needs_is_dark_mode = False
open_braces = 0

def process_line(line):
    # Ignore lines that already have `if (isDarkMode)` to avoid double-wrapping
    if 'if (isDarkMode)' in line:
        return line, False
    
    modified = False
    
    # Replace PineInk
    if re.search(r'color\s*=\s*PineInk\b', line):
        line = re.sub(r'color\s*=\s*PineInk\b', 'color = if (isDarkMode) CloudWhite else PineInk', line)
        modified = True
        
    # Replace ForestDeep.copy(alpha = X)
    match = re.search(r'color\s*=\s*ForestDeep\.copy\(alpha\s*=\s*([0-9.]+[fF]?)\)', line)
    if match:
        alpha = match.group(1)
        line = re.sub(r'color\s*=\s*ForestDeep\.copy\(alpha\s*=\s*[0-9.]+[fF]?\)', 
                      f'color = if (isDarkMode) CloudWhite.copy(alpha = {alpha}) else ForestDeep.copy(alpha = {alpha})', line)
        modified = True
        
    return line, modified

i = 0
while i < len(lines):
    line = lines[i]
    
    if '@Composable' in line:
        in_composable = True
        has_is_dark_mode = False
        needs_is_dark_mode = False
        open_braces = 0
        composable_start_idx = len(new_lines)
    
    if in_composable:
        open_braces += line.count('{') - line.count('}')
        
        if 'val isDarkMode =' in line:
            has_is_dark_mode = True
            
        processed_line, modified = process_line(line)
        if modified:
            needs_is_dark_mode = True
        
        new_lines.append(processed_line)
        
        # Inject isDarkMode if needed right after the first open brace of the function
        if needs_is_dark_mode and not has_is_dark_mode:
            # Look back to find the first '{' of this composable function
            for j in range(composable_start_idx, len(new_lines)):
                if '{' in new_lines[j]:
                    # Insert after this line
                    indent = re.match(r'^\s*', new_lines[j]).group(0) + '    '
                    new_lines.insert(j + 1, f'{indent}val isDarkMode = com.poxiao.app.ui.LocalLiquidGlassStylePreset.current == com.poxiao.app.ui.LiquidGlassStylePreset.Hyper\n')
                    has_is_dark_mode = True
                    break
        
        if open_braces <= 0 and '{' in ''.join(lines[i-5:i+1]): # Rough check for end of function
            in_composable = False
            
    else:
        new_lines.append(line)
        
    i += 1

with open(file_path, 'w', encoding='utf-8') as f:
    f.writelines(new_lines)

print("Done replacing colors.")
