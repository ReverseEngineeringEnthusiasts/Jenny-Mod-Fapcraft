#!/usr/bin/env python3
"""rename_methods.py — project-wide obfuscated-token -> meaningful-name renames.

Token map given as JSON: {"a_clash123": "meaningfulName", ...}. Applies with
word boundaries to every .java file under the root. Prints per-file change
count. NOT javadoc-aware, does not touch signatures (name-only, so signature
lines get renamed too, which is what you want for method renames).

Usage:
  python3 rename_methods.py <src-root> <map.json>
"""
import json, os, re, sys

root = sys.argv[1]
mapping = json.load(open(sys.argv[2], encoding="utf-8"))

# report tokens that never appear anywhere (check BEFORE writing)
appeared = set()
for dp, _dirs, fs in os.walk(root):
    for fn in fs:
        if fn.endswith(".java"):
            t = open(os.path.join(dp, fn), encoding="utf-8").read()
            for old in mapping:
                if re.search(r"\b%s\b" % re.escape(old), t):
                    appeared.add(old)
for old in mapping:
    if old not in appeared:
        print("WARN: never found:", old)

count_files = 0
for dp, _dirs, fs in os.walk(root):
    for fn in fs:
        if not fn.endswith(".java"):
            continue
        p = os.path.join(dp, fn)
        t = open(p, encoding="utf-8").read()
        orig = t
        for old, new in mapping.items():
            t = re.sub(r"\b%s\b" % re.escape(old), new, t)
        if t != orig:
            open(p, "w", encoding="utf-8").write(t)
            count_files += 1
print(f"updated {count_files} files")
