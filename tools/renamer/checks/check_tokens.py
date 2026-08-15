import sys

def check(f):
    src = open(f).read()
    i = 0
    n = len(src)
    depth = {'{': 0, '(': 0, '[': 0}
    pairs = {'}': '{', ')': '(', ']': '['}
    line = 1
    problems = []
    in_block = False
    in_line = False
    in_str = None
    while i < n:
        c = src[i]
        if c == '\n':
            line += 1
        if in_line:
            if c == '\n':
                in_line = False
            i += 1
            continue
        if in_block:
            if c == '*' and i+1 < n and src[i+1] == '/':
                in_block = False
                i += 2
                continue
            i += 1
            continue
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == in_str:
                in_str = None
            i += 1
            continue
        if c == '/' and i+1 < n and src[i+1] == '*':
            in_block = True
            i += 2
            continue
        if c == '/' and i+1 < n and src[i+1] == '/':
            in_line = True
            i += 2
            continue
        if c in ('"', "'"):
            in_str = c
            i += 1
            continue
        if c in '{([`':
            # backtick: JS template - skip
            pass
        if c in '}])':
            pass
        i += 1
    # second pass: proper counting
    i = 0
    in_block = in_line = False
    in_str = None
    stack = []
    while i < n:
        c = src[i]
        if in_line:
            if c == '\n':
                in_line = False
            i += 1
            continue
        if in_block:
            if c == '*' and i+1 < n and src[i+1] == '/':
                in_block = False
                i += 2
                continue
            i += 1
            continue
        if in_str:
            if c == '\\':
                i += 2
                continue
            if c == in_str:
                in_str = None
            i += 1
            continue
        if c == '/' and i+1 < n and src[i+1] == '*':
            in_block = True
            i += 2
            continue
        if c == '/' and i+1 < n and src[i+1] == '/':
            in_line = True
            i += 2
            continue
        if c in ('"', "'"):
            in_str = c
            i += 1
            continue
        if c in '{([':
            stack.append((c, line))
        elif c in '}])':
            if not stack or pairs[c] != stack[-1][0]:
                problems.append(f"line {line}: unmatched {c}")
            else:
                stack.pop()
        i += 1
    if stack:
        for ch, ln in stack:
            problems.append(f"line {ln}: unclosed {ch}")
    return problems

for f in sys.argv[1:]:
    probs = check(f)
    print(f, "->", "OK" if not probs else probs)
