#!/usr/bin/env python3
"""Vote-based oracle matcher: for each obfuscated file, tally which clean file
shares the most distinctive string constants. Prints ranked candidates.

Usage: python3 vote_match.py <our-file> [clean-root]
"""
import os, re, sys, collections

CLEAN = sys.argv[2] if len(sys.argv) > 2 else "/tmp/opencode/jennymodre-clean/src/main/java/com/trolmastercard/sexmod"
STR_RE = re.compile(r'"((?:[^"\\]|\\.){4,80})"')

def strings_of(path):
    try:
        t = open(path, encoding="utf-8", errors="replace").read()
    except Exception:
        return set()
    out = set()
    for s in STR_RE.findall(t):
        s2 = s.replace("\\n", "\n").replace("\\t", "\t").replace('\\"', '"')
        if re.fullmatch(r'[\d\s\W_]+', s2):
            continue
        if s2.startswith(("textures/", "models/", "sounds/", "items/")):
            continue
        if s2 in ("null", "true", "false", "sexmod"):
            continue
        out.add(s2)
    return out

# preload clean files -> strings
clean_files = {}
for dirpath, _, files in os.walk(CLEAN):
    for f in files:
        if f.endswith(".java"):
            full = os.path.join(dirpath, f)
            clean_files[os.path.relpath(full, CLEAN)] = strings_of(full)

def match_one(our_path):
    ours = strings_of(our_path)
    if not ours:
        return None
    votes = collections.Counter()
    for crel, cstrs in clean_files.items():
        n = len(ours & cstrs)
        if n > 0:
            votes[crel] += n
    return votes

def main():
    target = sys.argv[1]
    if os.path.isdir(target):
        files = []
        for dirpath, _, fs in os.walk(target):
            for f in fs:
                if f.endswith(".java"):
                    files.append(os.path.join(dirpath, f))
    else:
        files = [target]
    for path in sorted(files):
        votes = match_one(path)
        if not votes:
            print(f"{os.path.basename(path):20s} NO STRINGS")
            continue
        top = votes.most_common(3)
        tot = sum(v for _, v in top)
        name = os.path.relpath(path, os.path.dirname(target)) if os.path.isdir(target) else os.path.basename(path)
        line = f"{name:40s} -> {top[0][0]:50s} votes={top[0][1]}"
        if len(top) > 1 and top[1][1] >= top[0][1] * 0.7:
            line += f"  (also: {top[1][0]}={top[1][1]})"
        print(line)

if __name__ == "__main__":
    main()
