#!/usr/bin/env python3
"""oracle_methods3.py — body-fingerprint oracle matcher.

For each obfuscated method (a_clashNNN / single-letter) in OUR source, find the
clean-project method with the best-matching body fingerprint:
  - same return type + param types (hard filter)
  - overlap of string literals (weight 3)
  - overlap of called method names (weight 1)
  - overlap of referenced type names (weight 1)
Reports ranked candidates per method. Output must be verified before applying.

Usage: python3 oracle_methods3.py <our-root> <clean-root> [min-score]
"""
import os, re, sys
from collections import defaultdict

OUR = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
CLEAN = sys.argv[2] if len(sys.argv) > 2 else "/tmp/opencode/jennymodre-clean/src/main/java"
MINSCORE = float(sys.argv[3]) if len(sys.argv) > 3 else 2.0

STR_RE = re.compile(r'"((?:[^"\\]|\\.){3,60})"')
METHOD_RE = re.compile(
    r"(?m)^\s*(?:(?:public|protected|private|static|final|abstract|synchronized|native|@\w+)\s+)*"
    r"([A-Za-z0-9_<>\[\],\s.]+?)\s+([a-zA-Z_][a-zA-Z0-9_]*)\s*\(([^)]*)\)\s*\{"
)
CALL_RE = re.compile(r"\b([a-z][a-zA-Z0-9_]*)\s*\(")

def norm(t):
    t = re.sub(r'"(?:\\.|[^"\\])*"', '""', t)
    t = re.sub(r"//[^\n]*", "", t)
    t = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
    return t

def body_of(t, brace_start):
    depth = 0
    for i in range(brace_start, len(t)):
        if t[i] == "{":
            depth += 1
        elif t[i] == "}":
            depth -= 1
            if depth == 0:
                return t[brace_start + 1:i]
    return t[brace_start:]

def extract(path):
    raw = open(path, encoding="utf-8", errors="replace").read()
    t = norm(raw)
    cls = re.search(r"(?:class|interface|enum)\s+([A-Za-z0-9_]+)", t)
    cls_name = cls.group(1) if cls else os.path.basename(path).replace(".java", "")
    out = []
    for m in METHOD_RE.finditer(t):
        ret, name, params = m.group(1).strip(), m.group(2), m.group(3)
        if name in ("if", "for", "while", "switch", "return", "catch", "new", "synchronized", "assert"):
            continue
        if not ret or ret in ("import", "package"):
            continue
        ptypes = tuple(p.strip().split()[-1].replace("[]", "").replace("...", "").split(".")[-1]
                       for p in params.split(",") if p.strip())
        br = t.find("{", m.end() - 1)
        body = body_of(t, br) if br != -1 else ""
        # body fingerprint on the ORIGINAL raw (strings preserved)
        rawbody = ""
        if br != -1:
            rb = body_of(raw, raw.find("{", m.end() - 1))
            rawbody = rb
        strings = set(STR_RE.findall(rawbody))
        calls = set(CALL_RE.findall(body)) - set(("if", "for", "while", "switch", "return", "new"))
        types = set(re.findall(r"\b[A-Z][A-Za-z0-9_]*\b", body)) - set(("String", "Integer", "Float", "Double", "Boolean", "Long", "Character", "Byte", "Short", "Object", "Math", "System", "Arrays", "Collections", "List", "Map", "Set", "ArrayList", "HashMap", "HashSet", "Optional", "Iterator", "Iterable", "Comparator", "StringBuilder"))
        out.append((name, ret.split(".")[-1].replace("[]", ""), ptypes, strings, calls, types))
    return cls_name, out

def obf(n):
    return n.startswith("a_clash") or (len(n) <= 2 and n in "abcdefghijklmnopqrstuvwxyz")

def main():
    clean_idx = defaultdict(list)
    for dp, _d, fs in os.walk(CLEAN):
        for fn in fs:
            if fn.endswith(".java"):
                cls, methods = extract(os.path.join(dp, fn))
                for name, ret, params, strings, calls, types in methods:
                    clean_idx[(ret, params)].append((name, strings, calls, types, os.path.basename(fn)))

    for dp, _d, fs in os.walk(OUR):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            cls, methods = extract(os.path.join(dp, fn))
            for name, ret, params, strings, calls, types in methods:
                if not obf(name):
                    continue
                cands = clean_idx.get((ret, params), [])
                scored = []
                for cname, cstr, ccall, ctyp, cfile in cands:
                    if obf(cname):
                        continue
                    sc = 0.0
                    shared_s = strings & cstr
                    sc += sum(3.0 for _ in shared_s)
                    shared_c = calls & ccall
                    sc += sum(1.0 for _ in shared_c)
                    shared_t = types & ctyp
                    sc += sum(0.5 for _ in shared_t)
                    if sc >= MINSCORE:
                        scored.append((sc, cname, cfile, len(shared_s), len(shared_c), len(shared_t)))
                scored.sort(reverse=True)
                if scored:
                    best = scored[0]
                    print(f"{fn}:{name} -> {best[1]}  score={best[0]:.1f} str={best[3]} call={best[4]} typ={best[5]} ({best[2]})")

if __name__ == "__main__":
    main()
