#!/usr/bin/env python3
"""rename_params.py — rename method parameters + locals inside method bodies.

Signature-anchored: for each method signature that appears in the target file,
the region from the signature's '{' to its matching '}' (brace-depth matched)
has token renames applied. Overlap-guarded: a later method whose signature
starts inside an already-processed region is skipped.

JSON spec:
[
  {"sig": "public void setTargetPosition(Vec3d var1) {", "rename": {"var1": "pos", "var2": "formatted"}},
  ...
]

The sig is matched by substring; include the opening '{' in it so the brace
search starts at the right place (see prompt.txt pitfall: matching at the
wrong '{' mangled method bodies).

Usage:
  python3 rename_params.py <java-file> <spec.json>
"""
import json, re, sys

p = sys.argv[1]
spec = json.load(open(sys.argv[2], encoding="utf-8"))
t = open(p, encoding="utf-8").read()
orig_t = t

def method_body_end(text, sig_start):
    open_idx = text.index("{", sig_start)
    depth = 0
    for i in range(open_idx, len(text)):
        if text[i] == "{":
            depth += 1
        elif text[i] == "}":
            depth -= 1
            if depth == 0:
                return i
    return len(text) - 1

occ = []
for entry in spec:
    sig = entry["sig"]
    idx = 0
    while True:
        i = t.find(sig, idx)
        if i == -1:
            break
        occ.append((i, sig, entry["rename"]))
        idx = i + 1

occ.sort()
segs = []
covered_until = -1
prev_start = None
for start, sig, mapping in occ:
    if start <= covered_until or start == prev_start:
        continue
    prev_start = start
    end = method_body_end(t, start + len(sig))
    covered_until = end
    sig_text = t[start:start + len(sig)]
    body = t[start + len(sig):end]
    for old, new in mapping.items():
        sig_text = re.sub(r"\b%s\b" % re.escape(old), new, sig_text)
        body = re.sub(r"\b%s\b" % re.escape(old), new, body)
    segs.append((start, start + len(sig), end, sig_text, body))

for start, sig_end, end, sig_text, body in reversed(segs):
    t = t[:start] + sig_text + body + t[end:]
open(p, "w", encoding="utf-8").write(t)
print("processed", len(segs), "methods")
for e in spec:
    if e["sig"] not in orig_t:
        print("WARN: sig not found:", e["sig"][:60])
