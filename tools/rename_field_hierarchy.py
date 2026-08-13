#!/usr/bin/env python3
"""rename_field_hierarchy.py — rename one field across a class hierarchy.

Usage:
  python3 rename_field_hierarchy.py <entity-dir> <baseClass> <oldName> <newName>

Renames field <oldName> declared in <baseClass>:
  1. declaration + this.X refs inside <baseClass>.java
  2. this.X refs in all files that TRANSITIVELY extend <baseClass>
     (skips files that declare their own X)
  3. bare static X refs in those same subclass files (entityDataManager.set(X,...))
  4. varN.X object-field refs in ALL files (renderers/models/etc.)
  5. <BaseClass>.X static refs in ALL files

Method-call-safe: this.X( / varN.X( are never touched.
"""
import os, re, sys

def find_decl(text, old):
    return re.search(
        r"^(\s*(?:(?:public|protected|private|static|final|@\w+)\s+)*)"
        r"([A-Za-z0-9_<>.]+)\s+"
        + re.escape(old) + r"(\s*[=;])", text, re.M)

def field_refs(text, old, new):
    """this.X (not method call), bare X before , ) = ; etc, varN.X (not call)."""
    text = re.sub(r"\bthis\." + re.escape(old) + r"(?!\s*\()", "this." + new, text)
    text = re.sub(r"(?<![.\w])" + re.escape(old) + r"(?=\s*[=;,.)\]\[<>+\-*/%&|^!?:])", new, text)
    text = re.sub(r"\b(var\d+)\.(?!" + old + r"\()" + old + r"\b", r"\1." + new, text)
    return text

def main():
    entity_dir, base, old, new = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
    base_path = os.path.join(entity_dir, base + ".java")

    # 1. base class file
    t = open(base_path, encoding="utf-8").read()
    m = find_decl(t, old)
    if not m:
        print(f"ERROR: no declaration of {old} in {base}.java")
        sys.exit(1)
    t = t[:m.start()] + m.group(1) + m.group(2) + " " + new + m.group(3) + t[m.end():]
    t = field_refs(t, old, new)
    open(base_path, "w", encoding="utf-8").write(t)
    print(f"[base] {base}.java: {old} -> {new}")

    # 2. transitive subclasses
    extends = {}
    for fn in os.listdir(entity_dir):
        if fn.endswith(".java"):
            tt = open(os.path.join(entity_dir, fn)).read()
            mm = re.search(r"extends\s+(\w+)", tt)
            if mm:
                extends[fn] = mm.group(1)
    subs = set()
    frontier = {base + ".java"}
    while frontier:
        nxt = set()
        for fn, parent in extends.items():
            if parent + ".java" in frontier and fn not in subs:
                subs.add(fn)
                nxt.add(fn)
        frontier = nxt
    for fn in sorted(subs):
        p = os.path.join(entity_dir, fn)
        tt = open(p, encoding="utf-8").read()
        if find_decl(tt, old):
            print(f"  [skip] {fn} declares its own {old}")
            continue
        tt2 = field_refs(tt, old, new)
        if tt2 != tt:
            open(p, "w", encoding="utf-8").write(tt2)
            print(f"  [sub]  {fn}: this./bare {old} -> {new}")

    # 3. varN.X refs + BaseClass.X refs in ALL files
    n = 0
    for dp, _, fs in os.walk("src/main/java"):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dp, fn)
            if os.path.abspath(p) == os.path.abspath(base_path):
                continue
            tt = open(p, encoding="utf-8").read()
            tt2 = field_refs(tt, old, new)
            tt2 = re.sub(r"\b" + re.escape(base) + r"\." + re.escape(old) + r"\b",
                         base + "." + new, tt2)
            if tt2 != tt:
                open(p, "w", encoding="utf-8").write(tt2)
                n += 1
    print(f"[tree] varN.X / {base}.X refs updated in {n} files")

if __name__ == "__main__":
    main()
