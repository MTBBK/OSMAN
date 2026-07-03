import os
import re
import glob

themes_dir = "/home/mtbbk/Temel/OSMAN/Themes"
css_files = glob.glob(os.path.join(themes_dir, "*/*.css"))

for css_file in css_files:
    with open(css_file, "r") as f:
        content = f.read()

    if "--theme-bg:" in content:
        continue # Already injected
    
    # 1. Parse body background and color
    body_match = re.search(r'body\s*{([^}]+)}', content)
    bg = "var(--bg-earth, #121212)"
    text = "var(--paper-text, #e0e0e0)"
    if body_match:
        body_content = body_match.group(1)
        bg_match = re.search(r'background-color:\s*([^;]+);', body_content)
        if bg_match: bg = bg_match.group(1).strip()
        
        color_match = re.search(r'(?<![-])color:\s*([^;]+);', body_content)
        if color_match: text = color_match.group(1).strip()

    # 2. Parse accent (from 'a {' or '.site-title {')
    accent = "var(--dull-gold, #bb86fc)"
    a_match = re.search(r'[^a-zA-Z0-9-]a\s*{([^}]+)}', content)
    if a_match:
        a_color_match = re.search(r'(?<![-])color:\s*([^;]+);', a_match.group(1))
        if a_color_match: accent = a_color_match.group(1).strip()

    # 3. Parse border and alt-bg (from .post-card or header)
    border = "var(--stone-border, #333)"
    alt_bg = "var(--clay-dark, #1e1e1e)"
    card_match = re.search(r'\.post-card\s*{([^}]+)}', content)
    if card_match:
        card_content = card_match.group(1)
        card_bg = re.search(r'background-color:\s*([^;]+);', card_content)
        if not card_bg: card_bg = re.search(r'background:\s*([^;]+);', card_content)
        if card_bg: alt_bg = card_bg.group(1).strip()
        
        card_border = re.search(r'border(?:-bottom)?:\s*[^#a-zA-Z-]*([#a-zA-Zvar()-]+);', card_content)
        if card_border: border = card_border.group(1).strip()

    # Special adjustments for specific themes
    if "Hacker" in css_file:
        alt_bg = "#0d1a0d"
        border = "#1a3a1a"
        accent = "#00cc33"
    elif "Neon" in css_file:
        alt_bg = "var(--carbon-weave)"
        border = "#333"
        accent = "var(--nos-cyan)"
    elif "Arabalar" in css_file:
        alt_bg = "var(--asphalt-card)"
        border = "var(--mcqueen-red)"
        accent = "var(--mcqueen-yellow)"
    elif "OldSchool" in css_file:
        alt_bg = "var(--bg-dark)"
        border = "var(--border)"
        accent = "var(--accent)"

    injection = f"""
/* Injected Global Theme Variables for Templates */
:root {{
    --theme-bg: {bg};
    --theme-text: {text};
    --theme-accent: {accent};
    --theme-border: {border};
    --theme-alt-bg: {alt_bg};
}}
"""
    
    # Prepend or insert into existing :root if we want, but it's safer to just prepend a new :root block at the top.
    # CSS allows multiple :root blocks.
    
    # We will put it right after the first comment block or at the very top.
    if content.startswith("/*"):
        end_comment = content.find("*/")
        if end_comment != -1:
            new_content = content[:end_comment+2] + "\n" + injection + content[end_comment+2:]
        else:
            new_content = injection + content
    else:
        new_content = injection + content
        
    with open(css_file, "w") as f:
        f.write(new_content)
    
    print(f"Updated {css_file} with --theme-bg: {bg}")

print("Done.")
