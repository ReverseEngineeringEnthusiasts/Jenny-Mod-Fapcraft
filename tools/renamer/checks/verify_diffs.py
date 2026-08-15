#!/usr/bin/env python3
import os
import re

EDIT = "<REPO>/src/main/java"
BAK = "/tmp/fapcraft_backup/java"

removed_lines = {}
for line in open("/tmp/apply.log"):
    if not line.startswith("[apply]"):
        continue
    rest = line.strip()[len("[apply] "):]
    path, _, imp = rest.rpartition(":")
    removed_lines.setdefault(path, []).append(int(imp.split(" ")[0]))

diff_lines = removed_lines  # alias: file -> original 1-based line numbers removed

bad = []
n_files = 0
for path, linenos in sorted(diff_lines.items()):
    rel = os.path.relpath(path, EDIT)
    bak = os.path.join(BAK, rel)
    with open(path, encoding="utf-8", errors="replace") as f:
        new = f.read().split("\n")
    with open(bak, encoding="utf-8", errors="replace") as f:
        old = f.read().split("\n")
    n_files += 1
    drop = set(linenos)
    rebuilt = [l for i, l in enumerate(old, 1) if i not in drop]
    if rebuilt != new:
        bad.append(rel)
        print(f"MISMATCH {rel}: rebuilt from backup != current")
    for i in linenos:
        line = old[i - 1]
        if not re.match(r"^\s*import\s", line):
            bad.append((rel, i, "NON-IMPORT LINE DELETED: " + line))
            print(f"BAD {rel}:{i} non-import line removed: {line!r}")

print(f"\n{n_files} edited files checked; mismatches: {len(bad)}")
