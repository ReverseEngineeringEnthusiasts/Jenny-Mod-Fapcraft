#!/usr/bin/env python3
"""find_single_letter_fields.py — inventory single-letter field declarations.

Usage: python3 find_single_letter_fields.py [src-root] [--min-len N] [--json]
Prints per-file field list: TYPE NAME <- declaration line.
--min-len controls max name length (default 1, use 2 for aa/ab style).
"""
import json, os, re, sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
MINLEN = 1
JSON = False
for a in sys.argv[2:]:
    if a == "--json":
        JSON = True
    elif a.startswith("--min-len"):
        MINLEN = int(a.split("=")[1])

def find_fields(path):
    t = open(path, encoding="utf-8").read()
    out = []
    for ln in t.split("\n"):
        s = ln.strip()
        if not s or s.startswith(("//", "/*", "*", "import", "package")):
            continue
        s = re.sub(r"@\w+(\([^)]*\))?\s*", "", s)
        m = re.search(r"^(.*?)\s*[=;]", s)
        if not m:
            continue
        decl = m.group(1).strip()
        if "(" in decl or " " not in decl:
            continue
        parts = decl.split()
        name = parts[-1]
        typ = parts[-2]
        if len(name) <= MINLEN and name.isalpha():
            out.append({"type": typ, "name": name, "decl": decl})
    return out

def main():
    results = {}
    for dp, _, fs in os.walk(ROOT):
        for fn in fs:
            if fn.endswith(".java"):
                p = os.path.join(dp, fn)
                fields = find_fields(p)
                if fields:
                    results[os.path.relpath(p, ROOT)] = fields
    if JSON:
        print(json.dumps(results, indent=1))
        return
    for f in sorted(results, key=lambda k: -len(results[k])):
        print(f"{len(results[f]):3d}  {f}")
        for fd in results[f]:
            print(f"      {fd['type']} {fd['name']}   <- {fd['decl'][:70]}")

if __name__ == "__main__":
    main()
