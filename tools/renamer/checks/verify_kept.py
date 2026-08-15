#!/usr/bin/env python3
import os
import re

ROOT = "<REPO>/src/main/java"
IMPORT_RE = re.compile(r"^\s*import\s+(?:static\s+)?([A-Za-z0-9_.$]+);\s*$")
WILDCARD = re.compile(r"^\s*import\s+(?:static\s+)?[A-Za-z0-9_.$]+\.\*\s*;?\s*$")

suspects = []
for dirpath, _dirs, files in os.walk(ROOT):
    for fn in sorted(files):
        if not fn.endswith(".java"):
            continue
        path = os.path.join(dirpath, fn)
        with open(path, encoding="utf-8", errors="replace") as f:
            lines = f.read().split("\n")
        imports = []
        for i, ln in enumerate(lines):
            m = IMPORT_RE.match(ln)
            if m:
                imports.append((i, m.group(1)))
        if not imports:
            continue
        for i, p in imports:
            name = p.split(".")[-1]
            used = False
            for j, ln in enumerate(lines):
                if j == i or IMPORT_RE.match(ln) or WILDCARD.match(ln):
                    continue
                if re.search(r"(?<![A-Za-z0-9_$])" + re.escape(name) + r"(?![A-Za-z0-9_$])", ln):
                    used = True
                    break
            if not used:
                suspects.append((path, i + 1, p))

for s in suspects:
    print(s)
print(f"\n{len(suspects)} remaining imports with no raw-text occurrence of their simple name")
