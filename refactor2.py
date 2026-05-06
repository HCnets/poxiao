import re

def refactor_colors(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    new_lines = []
    in_composable = False
    composable_start_idx = -1
    has_is_dark_mode = False
    open_braces = 0

    for i, line in enumerate(lines):
        if '@Composable' in line:
            in_composable = True
            has_is_dark_mode = False
            open_braces = 0
            composable_start_idx = len(new_lines)
            
        if in_composable:
            open_braces += line.count('{') - line.count('}')
            
            if 'val isDarkMode =' in line:
                has_is_dark_mode = True
                
            # Perform replacements
            original = line
            # Don't replace if it's already in an if (isDarkMode)
            if 'if (isDarkMode)' not in line:
                # Only replace colors inside composables!
                # PineInk
                line = re.sub(r'color\s*=\s*PineInk\b', 'color = if (isDarkMode) CloudWhite else PineInk', line)
                # ForestDeep
                match = re.search(r'color\s*=\s*ForestDeep\.copy\(alpha\s*=\s*([0-9.]+[fF]?)\)', line)
                if match:
                    alpha = match.group(1)
                    line = re.sub(r'color\s*=\s*ForestDeep\.copy\(alpha\s*=\s*[0-9.]+[fF]?\)', 
                                  f'color = if (isDarkMode) CloudWhite.copy(alpha = {alpha}) else ForestDeep.copy(alpha = {alpha})', line)
                
                # ProKeypad colors
                if 'containerColor = ForestGreen' in line:
                    line = line.replace('containerColor = ForestGreen', 'containerColor = if (isDarkMode) Color(0xFF66FFB2).copy(alpha = 0.2f) else ForestGreen')
                if 'contentColor = CloudWhite' in line:
                    line = line.replace('contentColor = CloudWhite', 'contentColor = if (isDarkMode) Color(0xFF66FFB2) else CloudWhite')
                if 'tint = ForestGreen' in line:
                    line = line.replace('tint = ForestGreen', 'tint = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen')
                if 'color = ForestGreen' in line and 'BorderStroke' not in line and 'if (' not in line:
                    line = line.replace('color = ForestGreen', 'color = if (isDarkMode) Color(0xFF66FFB2) else ForestGreen')
                
                # ProKeypad Haptic
                if 'onClick = onEqual' in line:
                    line = line.replace('onClick = onEqual', 'onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onEqual() }')
                
            if line != original and not has_is_dark_mode:
                # Inject isDarkMode at the beginning of the function
                for j in range(composable_start_idx, len(new_lines)):
                    if '{' in new_lines[j]:
                        indent = re.match(r'^\s*', new_lines[j]).group(0) + '    '
                        new_lines.insert(j + 1, f'{indent}val isDarkMode = com.poxiao.app.ui.LocalLiquidGlassStylePreset.current == com.poxiao.app.ui.LiquidGlassStylePreset.Hyper\n')
                        if 'haptic.' in line:
                            new_lines.insert(j + 2, f'{indent}val haptic = LocalHapticFeedback.current\n')
                        has_is_dark_mode = True
                        break
            
            new_lines.append(line)
            
            if open_braces <= 0 and '{' in ''.join(lines[i-5:i+1]):
                in_composable = False
        else:
            new_lines.append(line)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.writelines(new_lines)

if __name__ == '__main__':
    refactor_colors('app/src/main/java/com/poxiao/app/calculator/ScientificCalculator.kt')
