#!/usr/bin/env python3
"""oracle_methods.py — map our a_clashNNN method names to the clean project's names.

For each method in our source that has an obfuscated name (a_clashNNN or
single-letter a/b/c...), find the method in jennymodre-clean with the SAME
signature (return type + param types + declaring class role) and report the
clean name.

Strategy: build a signature index from the clean project:
  (simple_class_name, method_name, param_type_simple_names) -> clean method name
Then for each obfuscated method in our project, look up candidates.

This is heuristic — output must be verified by the agent before applying.
Usage: python3 oracle_methods.py <our-src-root> <clean-src-root>
"""
import os, re, sys
from collections import defaultdict

OUR = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
CLEAN = sys.argv[2] if len(sys.argv) > 2 else "/tmp/opencode/jennymodre-clean/src/main/java"

SIG_RE = re.compile(
    r"(?m)^\s*(?:public|protected|private|static|final|abstract|synchronized|native|@\w+|\s)*"
    r"(?:([A-Za-z0-9_<>\[\],\s.]+?))\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)\s*(?:throws[^{]+)?\{?"
)

def strip_ann(s):
    return re.sub(r"@\w+(\([^)]*\))?\s*", "", s).strip()

def parse_file(path):
    t = open(path, encoding="utf-8", errors="replace").read()
    no_str = re.sub(r'"(?:\\.|[^"\\])*"', '""', t)
    no_str = re.sub(r"//[^\n]*", "", no_str)
    no_str = re.sub(r"/\*.*?\*/", "", no_str, flags=re.S)
    cls = re.search(r"(?:class|interface|enum)\s+([A-Za-z0-9_]+)", no_str)
    cls_name = cls.group(1) if cls else os.path.basename(path).replace(".java", "")
    methods = []
    for m in SIG_RE.finditer(no_str):
        ret = strip_ann(m.group(1))
        name = m.group(2)
        params = [p.strip().split()[-1] for p in m.group(3).split(",") if p.strip()]
        # skip obvious non-methods
        if name in ("if", "for", "while", "switch", "return", "catch", "new"):
            continue
        if not ret or ret in ("import", "package", "throws"):
            continue
        methods.append((name, ret, params))
    return cls_name, methods

def simple(t):
    return t.split(".")[-1].replace("[]", "").replace("...", "")

def main():
    clean_idx = defaultdict(list)  # (cls, params) -> [(clean_name, ret)]
    for dp, _d, fs in os.walk(CLEAN):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            cls, methods = parse_file(os.path.join(dp, fn))
            for name, ret, params in methods:
                key = (cls, tuple(simple(p) for p in params))
                clean_idx[key].append((name, simple(ret)))

    # our obfuscated methods
    for dp, _d, fs in os.walk(OUR):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dp, fn)
            cls, methods = parse_file(p)
            for name, ret, params in methods:
                if not (name.startswith("a_clash") or (len(name) <= 2 and name in "abcdefghijklmnopqrstuvwxyz")):
                    continue
                key = (cls, tuple(simple(x) for x in params))
                cands = clean_idx.get(key, [])
                if cands:
                    # prefer non-obfuscated clean names
                    good = [c for c in cands if not c[0].startswith(("a_clash", "var")) and not (len(c[0]) <= 2 and c[0] in "abcdefghijklmnopqrstuvwxyz")]
                    pick = good[0] if good else cands[0]
                    print(f"{fn}:{name} -> {pick[0]}  (ret {simple(ret)} -> {pick[1]})")

if __name__ == "__main__":
    main()
