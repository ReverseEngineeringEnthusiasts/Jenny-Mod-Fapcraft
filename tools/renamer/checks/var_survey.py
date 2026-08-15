import os, re

ROOT = '<REPO>/src/main/java/com/trolmastercard/sexmod'
pat = re.compile(r'\bvar\d+[a-z_]?\d*\b')

from collections import Counter
counts = Counter()
per_dir = {}
for dirpath, dirs, files in os.walk(ROOT):
    for fn in sorted(files):
        if not fn.endswith('.java'):
            continue
        p = os.path.join(dirpath, fn)
        s = open(p, errors='replace').read()
        n = len(pat.findall(s))
        if n:
            rel = os.path.relpath(p, ROOT)
            d = os.path.dirname(rel)
            per_dir.setdefault(d, []).append((rel, n))

total = 0
for d in sorted(per_dir):
    n = sum(x[1] for x in per_dir[d])
    total += n
    print(f"{d or '(root)'}: {len(per_dir[d])} files, {n} tokens")
print("TOTAL:", total)
