#!/usr/bin/env python3
import glob, re, sys

def brace_balance(src):
    depth = 0
    in_str = in_char = False
    i, n = 0, len(src)
    while i < n:
        c = src[i]
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == '"':
                in_str = False
        elif in_char:
            if c == '\\':
                i += 2
                continue
            if c == "'":
                in_char = False
        else:
            if c == '"':
                in_str = True
            elif c == "'":
                in_char = True
            elif c == '/' and i + 1 < n and src[i + 1] == '/':
                j = src.find('\n', i)
                i = n if j == -1 else j
                continue
            elif c == '/' and i + 1 < n and src[i + 1] == '*':
                j = src.find('*/', i + 2)
                i = n if j == -1 else j + 2
                continue
            elif c == '{':
                depth += 1
            elif c == '}':
                depth -= 1
        i += 1
    return depth

VAR = re.compile(r'\bvar\d+[a-z_]?\d*\b')
bad = []
for p in sorted(glob.glob(sys.argv[1] + '/*.java')):
    src = open(p, encoding='utf-8').read()
    b = brace_balance(src)
    if b != 0:
        bad.append(f'{p}: brace depth {b}')
    # varN inside string literals?
    for m in re.finditer(r'"(?:\\.|[^"\\])*"', src):
        if VAR.search(m.group(0)):
            bad.append(f'{p}: varN inside string literal at {m.start()}')
print('\n'.join(bad) if bad else 'ALL OK')
