#!/usr/bin/env python3
"""rename_fields.py — class-scoped field rename, method-call-safe.

Renames a field: declaration + references. Distinguishes FIELD refs from
METHOD calls: a reference `this.X` is a field only when the char after X is
NOT '(' (method call) — it must be one of . = ; [ , ) ] space < > + - * / etc.

Usage:
  python3 rename_fields.py <java-file> <oldName> <newName> <inherited-from-class?>
    - 4th arg optional: if given (e.g. AbstractGirlNpcEntity), also updates
      `this.X` refs in ALL files that extend that class, skipping files that
      declare their own X (shadowing). Static ClassName.X refs are always
      updated tree-wide.

  # renames S->nextAttack in AbstractGirlNpcEntity and propagates to subclasses
  python3 rename_fields.py entity/AbstractGirlNpcEntity.java S nextAttack AbstractGirlNpcEntity
"""
import os, re, sys

def find_decl(text, old):
    return re.search(
        r"^(\s*(?:(?:public|protected|private|static|final|@\w+)\s+)*)"
        r"([A-Za-z0-9_<>.]+)\s+"
        + re.escape(old) + r"(\s*[=;])", text, re.M)

def rename_field_refs(text, old, new):
    """this.X / bare X field refs (NOT method calls this.X())."""
    # this.X followed by non-( -> field
    text = re.sub(r"\bthis\." + re.escape(old) + r"(?!\s*\()", "this." + new, text)
    # bare X (not after dot, not a decl, not method call) followed by field-ish
    text = re.sub(r"(?<![.\w])" + re.escape(old) + r"(?=[\s]*[=;,.)\]\[<>+\-*/%&|^!?:])", new, text)
    return text

def main():
    path, old, new = sys.argv[1], sys.argv[2], sys.argv[3]
    inherit_from = sys.argv[4] if len(sys.argv) > 4 else None
    t = open(path, encoding="utf-8").read()
    orig = t
    # 1. declaration
    m = find_decl(t, old)
    if not m:
        print(f"ERROR: declaration of {old} not found in {path}")
        sys.exit(1)
    t = t[:m.start()] + m.group(1) + m.group(2) + " " + new + m.group(3) + t[m.end():]
    # 2. refs in declaring file
    t = rename_field_refs(t, old, new)
    open(path, "w", encoding="utf-8").write(t)
    print(f"renamed {old} -> {new} in {os.path.basename(path)}")
    # 3. static ClassName.X refs tree-wide
    cls = os.path.basename(path).replace(".java", "")
    n = 0
    for dp, _, fs in os.walk("src/main/java"):
        for fn in fs:
            if not fn.endswith(".java"):
                continue
            p = os.path.join(dp, fn)
            if os.path.abspath(p) == os.path.abspath(path):
                continue
            tt = open(p, encoding="utf-8").read()
            tt2 = re.sub(r"\b" + re.escape(cls) + r"\." + re.escape(old) + r"\b",
                         cls + "." + new, tt)
            if tt2 != tt:
                open(p, "w", encoding="utf-8").write(tt2)
                n += 1
    if n:
        print(f"  static {cls}.{old} refs updated in {n} files")
    # 4. inherited this.X in subclasses (skip files declaring their own X)
    if inherit_from:
        sub_pat = re.compile(r"\bextends\s+" + re.escape(inherit_from) + r"\b|\bextends\s+\w+.*" + re.escape(inherit_from))
        # simple: any entity file that (transitively) extends inherit_from
        n2 = 0
        for dp, _, fs in os.walk("src/main/java"):
            for fn in fs:
                if not fn.endswith(".java"):
                    continue
                p = os.path.join(dp, fn)
                if os.path.abspath(p) == os.path.abspath(path):
                    continue
                tt = open(p, encoding="utf-8").read()
                if not re.search(r"\bthis\." + re.escape(old) + r"\b", tt):
                    continue
                if find_decl(tt, old):
                    print(f"  SKIP {fn} (declares its own {old})")
                    continue
                tt2 = rename_field_refs(tt, old, new)
                if tt2 != tt:
                    open(p, "w", encoding="utf-8").write(tt2)
                    n2 += 1
        if n2:
            print(f"  inherited this.{old} refs updated in {n2} files")

if __name__ == "__main__":
    main()
