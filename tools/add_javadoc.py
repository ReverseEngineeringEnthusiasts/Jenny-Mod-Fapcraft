#!/usr/bin/env python3
"""add_javadoc.py — insert javadoc above matching method signatures.

JSON spec: [{"sig": "...", "doc": "/** ... */"}]

The doc is inserted directly above the signature line. Signatures must match
the exact text in the file (indentation included). Prints MISS for any sig
not found.

Usage:
  python3 add_javadoc.py <java-file> <spec.json>
"""
import json, sys

p = sys.argv[1]
spec = json.load(open(sys.argv[2], encoding="utf-8"))
t = open(p, encoding="utf-8").read()

count = 0
for e in spec:
    sig, doc = e["sig"], e["doc"]
    if sig in t:
        t = t.replace(sig, doc + "\n" + sig, 1)
        count += 1
    else:
        print("MISS:", sig[:70])
open(p, "w", encoding="utf-8").write(t)
print("inserted", count, "javadocs")
