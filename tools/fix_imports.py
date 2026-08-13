#!/usr/bin/env python3
"""fix_imports.py — clean CFR decompiler import-spacing artifacts.

CFR emits huge runs of blank lines between imports and around the class
declaration (e.g. GalathActionListener.java). This normalizes the file header:

  - collapse 3+ consecutive blank lines to a single blank line ANYWHERE
    (CFR also pads inside method bodies occasionally; one blank line is the
    standard)
  - collapse 2+ blank lines after `package ...;` to one
  - collapse 2+ blank lines before the first `import` to none
  - ensure exactly one blank line between the last import and the class decl
  - strips trailing whitespace on every line
  - ensures file ends with exactly one newline

It does NOT reorder imports or touch code logic. Verify with git diff that
only whitespace changed: `git diff -w` should be empty.

Usage:
  python3 fix_imports.py <file-or-dir> [--check]
"""
import os, re, sys

def fix(text):
    lines = text.split("\n")
    out = []
    prev_blank = False
    for ln in lines:
        ln = ln.rstrip()
        if ln.strip() == "":
            if prev_blank:
                continue  # collapse runs of blank lines
            prev_blank = True
            out.append("")
        else:
            prev_blank = False
            out.append(ln)
    text = "\n".join(out)
    # header normalization
    text = re.sub(r"(package [^;]+;)\n+", r"\1\n", text)
    # exactly one blank line after package, before first import
    text = re.sub(r"(package [^;]+;)\n(?!\n)(?=import )", r"\1\n\n", text)
    # imports: exactly one blank line between consecutive import groups? keep simple:
    # collapse blank lines between imports
    text = re.sub(r"(import [^;]+;)\n\n+(?=import )", r"\1\n", text)
    # exactly one blank line before class/interface/enum declaration (not between annotations)
    text = re.sub(r"(import [^;]+;)\n+(?=(?:@|public |final |abstract |class |interface |enum ))", r"\1\n\n", text)
    # remove blank lines between annotations and the declaration they annotate
    text = re.sub(r"(@[^\n]+)\n\n+(?=@|(?:public |final |abstract |class |interface |enum ))",
                  r"\1\n", text)
    text = text.rstrip("\n") + "\n"
    return text

def process(path, check_only=False):
    t = open(path, encoding="utf-8", errors="replace").read()
    fixed = fix(t)
    if t == fixed:
        return 0
    if check_only:
        print("WOULD FIX:", path)
        return 1
    open(path, "w", encoding="utf-8").write(fixed)
    print("fixed:", path)
    return 1

def main():
    target = sys.argv[1]
    check_only = "--check" in sys.argv
    if os.path.isdir(target):
        n = 0
        for dp, _dirs, fs in os.walk(target):
            for fn in fs:
                if fn.endswith(".java"):
                    n += process(os.path.join(dp, fn), check_only)
        print(f"{'would fix' if check_only else 'fixed'} {n} files")
    else:
        process(target, check_only)

if __name__ == "__main__":
    main()
