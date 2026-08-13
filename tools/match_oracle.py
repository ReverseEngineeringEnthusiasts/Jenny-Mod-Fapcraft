#!/usr/bin/env python3
"""Fingerprint-match obfuscated classes against the jennymodre-clean oracle.

Scoring signals (strongest first):
  1. shared string constants (exact, len>=3, non-numeric)
  2. shared referenced class simple names (types, casts, new, instanceof)
  3. method count delta, field count delta (penalty)
  4. shared superclass/interfaces (heavier weight)
  5. shared method name patterns (already-renamed methods like func_ -> MCP names)

Usage: python3 match_oracle.py <our-src-root> <clean-src-root> [class-prefix-filter]
"""
import os, re, sys, collections

OUR = sys.argv[1] if len(sys.argv) > 1 else "src/main/java"
CLEAN = sys.argv[2] if len(sys.argv) > 2 else "/tmp/opencode/jennymodre-clean/src/main/java"
FILTER = sys.argv[3] if len(sys.argv) > 3 else ""

STR_RE = re.compile(r'"([^"\n]{3,60})"')
TYPE_RE = re.compile(r'\b([A-Z][A-Za-z0-9_]*)\b')
METHOD_RE = re.compile(r'\b(?:public|protected|private|static|final|abstract|synchronized|native|\s)*(?:[A-Za-z0-9_<>\[\],\s]+?)\s+([a-z][A-Za-z0-9_]*)\s*\(')

def parse_file(path):
    try:
        t = open(path, encoding="utf-8", errors="replace").read()
    except Exception:
        return None
    # strip comments/strings for signature-ish analysis
    no_str = re.sub(r'"(?:\\.|[^"\\])*"', '""', t)
    no_str = re.sub(r"//[^\n]*", "", no_str)
    no_str = re.sub(r"/\*.*?\*/", "", no_str, flags=re.S)
    strings = [s for s in STR_RE.findall(t) if not re.fullmatch(r'[\d\s\W_]+', s)]
    types = collections.Counter(TYPE_RE.findall(no_str))
    # drop java/lang/primitive noise
    for k in list(types):
        if k in ("String","int","void","boolean","float","double","long","char","byte","short","new","return","if","else","for","while","switch","case","class","public","private","protected","static","final","this","super","null","true","false","import","package","throws","extends","implements","break","continue","try","catch","finally","throw","instanceof","abstract","enum","interface","synchronized","volatile","transient","native","assert","default","List","Map","Set","ArrayList","HashMap","HashSet","Optional","Integer","Float","Double","Boolean","Long","Character","Byte","Short","Object","Math","System","Arrays","Collections","Iterator","Iterable","Comparator","StringBuilder","StringBuffer"):
            del types[k]
    methods = set()
    for m in METHOD_RE.finditer(no_str):
        name = m.group(1)
        if name.startswith(("a_clash","func_","field_")) or (len(name) <= 2 and name in "abcdefghijklmnopqrstuvwxyz"):
            continue
        if name in ("if","for","while","switch","return","new","catch","throw","synchronized"):
            continue
        methods.add(name)
    sup = re.search(r'(?:extends|implements)\s+([A-Za-z0-9_.,\s]+)\s*\{', no_str)
    sup_names = set()
    if sup:
        for s in sup.group(1).replace(",", " ").split():
            s = s.strip()
            if s and s[0].isupper():
                sup_names.add(s)
    return {"strings": collections.Counter(strings), "types": types, "methods": methods,
            "sups": sup_names, "lines": t.count("\n")}

def load_all(root):
    out = {}
    for dirpath, _, files in os.walk(root):
        for f in files:
            if f.endswith(".java"):
                full = os.path.join(dirpath, f)
                rel = os.path.relpath(full, root)
                if FILTER and FILTER not in rel:
                    continue
                d = parse_file(full)
                if d:
                    out[rel] = d
    return out

def score(a, b):
    s = 0.0
    # strings: exact shared, weight by rarity
    shared = set(a["strings"]) & set(b["strings"])
    s += sum(min(a["strings"][x], b["strings"][x]) * 3.0 for x in shared)
    # types
    shared_t = set(a["types"]) & set(b["types"])
    s += sum(min(a["types"][x], b["types"][x]) for x in shared_t) * 1.0
    # sups
    shared_s = a["sups"] & b["sups"]
    s += len(shared_s) * 8.0
    # methods
    shared_m = a["methods"] & b["methods"]
    s += len(shared_m) * 1.5
    # size penalty
    s -= abs(a["lines"] - b["lines"]) * 0.05
    return s, len(shared), len(shared_t), len(shared_s), len(shared_m)

def main():
    our = load_all(OUR)
    clean = load_all(CLEAN)
    print(f"our files: {len(our)}, clean files: {len(clean)}")
    results = []
    for rel, a in sorted(our.items()):
        if any(x in rel for x in ("MCRepack",)):
            continue
        best = []
        for crel, b in clean.items():
            if b["lines"] < 5:
                continue
            sc, ns, nt, nsups, nm = score(a, b)
            if ns >= 1 or nsups >= 1 or nt >= 3:
                best.append((sc, crel, ns, nt, nsups, nm))
        best.sort(reverse=True)
        results.append((rel, best[:3]))
    for rel, best in results:
        if not best:
            print(f"NO MATCH  {rel}")
        else:
            top = best[0]
            print(f"{rel:60s} -> {top[1]:55s} score={top[0]:7.1f} str={top[2]} typ={top[3]} sup={top[4]} met={top[5]}")

if __name__ == "__main__":
    main()
