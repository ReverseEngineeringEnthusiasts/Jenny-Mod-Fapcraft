#!/usr/bin/env python3
"""oracle_methods2.py — map a_clashNNN tokens to clean names via raw-decompile bridge.

The raw decompile (jennymodre) and the clean project (jennymodre-clean) are
decompilations of the SAME jar. A method's (a_clash name, signature) is
identical in both raw and clean — only the method NAME differs (clean renamed
it). So:

1. Index clean project: for each method, (a_clash_token_if_present, ret, params)
   -> clean method name. Methods that were renamed lost their a_clash token, so
   also index by (ret, params, body-string-fingerprint).
2. For each a_clash method in OUR source, look up clean methods with the same
   (ret, params) and report their clean names.

Usage: python3 oracle_methods2.py <our-src-root> <clean-src-root>
"""
import os, re, sys
from collections import defaultdict

OUR = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
CLEAN = sys.argv[2] if len(sys.argv) > 2 else "/tmp/opencode/jennymodre-clean/src/main/java"

METHOD_RE = re.compile(
    r"(?m)^\s*(?:(?:public|protected|private|static|final|abstract|synchronized|native|@\w+)\s+)*"
    r"([A-Za-z0-9_<>\[\],\s.]+?)\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)\s*\{?"
)

def parse_file(path):
    t = open(path, encoding="utf-8", errors="replace").read()
    t = re.sub(r'"(?:\\.|[^"\\])*"', '""', t)
    t = re.sub(r"//[^\n]*", "", t)
    t = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
    cls = re.search(r"(?:class|interface|enum)\s+([A-Za-z0-9_]+)", t)
    cls_name = cls.group(1) if cls else os.path.basename(path).replace(".java", "")
    methods = []
    for m in METHOD_RE.finditer(t):
        ret, name, params = m.group(1).strip(), m.group(2), m.group(3)
        if name in ("if", "for", "while", "switch", "return", "catch", "new", "synchronized"):
            continue
        if not ret or ret in ("import", "package"):
            continue
        ptypes = tuple(p.strip().split()[-1].replace("[]", "").replace("...", "").split(".")[-1]
                       for p in params.split(",") if p.strip())
        methods.append((name, ret.split(".")[-1].replace("[]", ""), ptypes))
    return cls_name, methods

def obf(n):
    return n.startswith("a_clash") or (len(n) <= 2 and n in "abcdefghijklmnopqrstuvwxyz")

def main():
    clean_by_sig = defaultdict(list)  # (ret, params) -> [names]
    clean_tokens = {}  # a_clash token -> clean name (from clean files that kept tokens)
    for dp, _d, fs in os.walk(CLEAN):
        for fn in fs:
            if fn.endswith(".java"):
                cls, methods = parse_file(os.path.join(dp, fn))
                for name, ret, params in methods:
                    clean_by_sig[(ret, params)].append(name)
                    if name.startswith("a_clash"):
                        clean_tokens[name] = name  # still obfuscated in clean too

    found = []
    for dp, _d, fs in os.walk(OUR):
        for fn in fs:
            if fn.endswith(".java"):
                cls, methods = parse_file(os.path.join(dp, fn))
                for name, ret, params in methods:
                    if not obf(name):
                        continue
                    cands = clean_by_sig.get((ret, params), [])
                    good = [c for c in cands if not obf(c)]
                    if good:
                        found.append((fn, name, ret, params, good[0], len(good)))
    found.sort(key=lambda x: x[2])
    print(f"matched {len(found)} of our obfuscated methods by (ret, params)")
    for fn, name, ret, params, cname, n in found:
        print(f"{fn}:{name} -> {cname}  ({ret}({','.join(params)})) matches={n}")

if __name__ == "__main__":
    main()
