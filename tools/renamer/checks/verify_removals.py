#!/usr/bin/env python3
import re

removed = {}
for line in open("/tmp/apply.log"):
    line = line.strip()
    if not line.startswith("[apply]"):
        continue
    rest = line[len("[apply] "):]
    path, _, imp = rest.rpartition(":")
    removed.setdefault(path, []).append(imp.strip())

bad = []
total = 0
for path, imps in sorted(removed.items()):
    with open(path, encoding="utf-8", errors="replace") as f:
        text = f.read()
    for imp in imps:
        total += 1
        name = imp.split(".")[-1]
        for i, ln in enumerate(text.split("\n"), 1):
            if re.search(r"(?<![A-Za-z0-9_$])" + re.escape(name) + r"(?![A-Za-z0-9_$])", ln):
                bad.append((path, i, name, ln.strip()[:100]))

for b in bad:
    print(b)
print(f"\n{total} imports removed total; raw-text occurrences of removed names: {len(bad)}")
