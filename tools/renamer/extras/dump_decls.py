#!/usr/bin/env python3
import re, sys, glob, os
files = [os.path.basename(x) for x in glob.glob(sys.argv[1] + '/*.java')]
for f in files:
    path = os.path.join(sys.argv[1], f)
    src = open(path).read()
    lines = src.split('\n')
    decls = []
    for i, ln in enumerate(lines, 1):
        # declarations: varN = ...;  varN;   for (Type varN : ...)   catch (Type varN)
        if re.search(r'\bvar\d+[a-z_]?\d*\s*[=;]', ln) or re.search(r'\bfor\s*\([^)]*\bvar\d+[a-z_]?\d*\b', ln) or re.search(r'\bcatch\s*\([^)]*\bvar\d+[a-z_]?\d*\b', ln) or re.search(r'^\s*[A-Za-z_<][^;{]*\bvar\d+[a-z_]?\d*\s*[),]', ln):
            decls.append(f"{i}: {ln.strip()}")
    print(f"== {f}")
    for d in decls:
        print('  ' + d)
