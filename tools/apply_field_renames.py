#!/usr/bin/env python3
"""apply_field_renames.py — per-declaration field renames with class-scoped refs.

Mapping file format (JSON):
{
  "__inherited__": {"old": {"new": "BaseClass"}, ...},   // optional
  "rel/path/File.java": [
     {"old": "a", "new": "squishAmount", "line": 48},    // line optional: 1-based decl line; if absent, first unrenamed decl
     ...
  ]
}

For each entry:
  - locates the decl (by line if given, else first match), determines the
    enclosing class body (state-machine brace tracking, string/comment-safe),
    renames this.X / bare-X refs within that class body only (nested bodies
    carved out). this.X must be followed by a word boundary (so this.d does
    NOT match this.dataManager).
  - static fields: also updates ClassName.old tree-wide.

Usage: python3 apply_field_renames.py <mapping.json>
"""
import json, os, re, sys

SRC = "src/main/java"

def class_scopes(text):
    """Return (start, end, name) of each class/interface/enum body in ORIGINAL
    text coordinates (start = index just after '{', end = index of '}').
    Skips strings, chars, comments via a small state machine."""
    ranges = []
    i = 0
    n = len(text)
    state = "code"  # code | linec | blockc | str | chr
    while i < n:
        c = text[i]
        if state == "code":
            if c == '/' and i + 1 < n and text[i+1] == '/':
                state = "linec"; i += 2; continue
            if c == '/' and i + 1 < n and text[i+1] == '*':
                state = "blockc"; i += 2; continue
            if c == '"':
                state = "str"; i += 1; continue
            if c == "'":
                state = "chr"; i += 1; continue
            if c.isalpha() or c == '_':
                j = i
                while j < n and (text[j].isalnum() or text[j] == '_'):
                    j += 1
                word = text[i:j]
                if word in ("class", "interface", "enum"):
                    # scan forward (normal state) to the opening '{'
                    k = j
                    while k < n and text[k] != '{' and text[k] != ';':
                        k += 1
                    if k < n and text[k] == '{':
                        # find matching close brace with state machine
                        depth = 1
                        kk = k + 1
                        st = "code"
                        while kk < n and depth > 0:
                            ch = text[kk]
                            if st == "code":
                                if ch == '/' and kk+1 < n and text[kk+1] == '/':
                                    st = "linec"; kk += 2; continue
                                if ch == '/' and kk+1 < n and text[kk+1] == '*':
                                    st = "blockc"; kk += 2; continue
                                if ch == '"':
                                    st = "str"; kk += 1; continue
                                if ch == "'":
                                    st = "chr"; kk += 1; continue
                                if ch == '{':
                                    depth += 1
                                elif ch == '}':
                                    depth -= 1
                            elif st == "linec":
                                if ch == '\n':
                                    st = "code"
                            elif st == "blockc":
                                if ch == '*' and kk+1 < n and text[kk+1] == '/':
                                    st = "code"; kk += 1
                            elif st == "str":
                                if ch == '\\':
                                    kk += 1
                                elif ch == '"':
                                    st = "code"
                            elif st == "chr":
                                if ch == '\\':
                                    kk += 1
                                elif ch == "'":
                                    st = "code"
                            kk += 1
                        if depth == 0:
                            ranges.append((k + 1, kk - 1, word))
                    i = j
                    continue
                i = j
                continue
            i += 1
        elif state == "linec":
            if c == '\n':
                state = "code"
            i += 1
        elif state == "blockc":
            if c == '*' and i + 1 < n and text[i+1] == '/':
                state = "code"; i += 2
            else:
                i += 1
        elif state == "str":
            if c == '\\':
                i += 2
            else:
                i += 1
                if c == '"':
                    state = "code"
        elif state == "chr":
            if c == '\\':
                i += 2
            else:
                i += 1
                if c == "'":
                    state = "code"
    return ranges

def enclosing_class(scopes, pos):
    best = None
    for s, e, name in scopes:
        if s <= pos < e:
            if best is None or (s >= best[0] and e <= best[1]):
                best = (s, e, name)
    return best

def rename_in_range(text, s, e, old, new):
    sub = text[s:e]
    nested = [(ns, ne) for ns, ne, nn in class_scopes(sub) if (ns, ne) != (0, len(sub))]
    keep = []
    prev = 0
    for ns, ne in sorted(nested):
        keep.append((prev, ns))
        prev = ne
    keep.append((prev, len(sub)))
    out = []
    for ks, ke in keep:
        chunk = sub[ks:ke]
        chunk = re.sub(r"\bthis\." + re.escape(old) + r"(?![\w(])", "this." + new, chunk)
        chunk = re.sub(r"(?<![.\w])" + re.escape(old) + r"(?=[\s]*[=;,.)\]\[<>+\-*/%&|^!?:])", new, chunk)
        out.append(chunk)
    return text[:s] + "".join(out) + text[e:]

DECL_RE = re.compile(
    r"^(?:\s*)(?P<mods>(?:(?:public|protected|private|static|final|transient|volatile|@\w+(?:\([^)]*\))?)\s+)*)"
    r"(?P<type>[A-Za-z_][\w.<>\[\], ]*?)\s+(?P<name>[A-Za-z_]\w*)\s*(?P<rest>[=;])", re.M)

def line_to_offset(text, line):
    off = 0
    for _ in range(line - 1):
        off = text.find("\n", off) + 1
    return off

def find_decl(text, old, line=None):
    if line is not None:
        off = line_to_offset(text, line)
        line_end = text.find("\n", off)
        seg = text[off:line_end if line_end != -1 else len(text)]
        m = DECL_RE.search(seg)
        if m and m.group("name") == old:
            return m, off
        # fall through to first-match
    for m in DECL_RE.finditer(text):
        if m.group("name") == old:
            return m, m.start()
    return None, None

def is_static_decl(mods):
    return bool(re.search(r"\bstatic\b", mods))

def main():
    mapping = json.load(open(sys.argv[1]))
    inherited = mapping.pop("__inherited__", {})
    for rel, entries in sorted(mapping.items()):
        path = os.path.join(SRC, rel)
        if not os.path.exists(path):
            print(f"MISSING {path}")
            continue
        text = open(path, encoding="utf-8").read()
        scopes = class_scopes(text)
        cls = os.path.basename(path).replace(".java", "")
        entries = sorted(entries, key=lambda e: e.get("line", 10**9))
        for ent in entries:
            old, new = ent["old"], ent["new"]
            m, off = find_decl(text, old, ent.get("line"))
            if not m:
                print(f"  !! decl of {old} (line {ent.get('line')}) not found in {rel}")
                continue
            rng = enclosing_class(scopes, off)
            if not rng:
                print(f"  !! no class scope for {old} in {rel}")
                continue
            text = rename_in_range(text, rng[0], rng[1], old, new)
            if is_static_decl(m.group("mods")):
                n = 0
                for dp, _, fs in os.walk(SRC):
                    for fn in fs:
                        if not fn.endswith(".java"):
                            continue
                        p = os.path.join(dp, fn)
                        if os.path.abspath(p) == os.path.abspath(path):
                            continue
                        tt = open(p, encoding="utf-8").read()
                        tt2 = re.sub(r"\b" + re.escape(cls) + r"\." + re.escape(old) + r"\b", cls + "." + new, tt)
                        if tt2 != tt:
                            open(p, "w", encoding="utf-8").write(tt2)
                            n += 1
                if n:
                    print(f"  static {cls}.{old} refs updated in {n} files")
            print(f"  {rel}: L{ent.get('line')} {old} -> {new}")
        open(path, "w", encoding="utf-8").write(text)

    for old, spec in inherited.items():
        for new, base in spec.items():
            n = 0
            base_pat = re.compile(r"\bextends\s+" + re.escape(base) + r"\b")
            for dp, _, fs in os.walk(SRC):
                for fn in fs:
                    if not fn.endswith(".java"):
                        continue
                    p = os.path.join(dp, fn)
                    tt = open(p, encoding="utf-8").read()
                    if not base_pat.search(tt):
                        continue
                    if find_decl(tt, old)[0]:
                        print(f"  SKIP {fn} (declares own {old})")
                        continue
                    tt2 = re.sub(r"\bthis\." + re.escape(old) + r"(?![\w(])", "this." + new, tt)
                    tt2 = re.sub(r"(?<![.\w])" + re.escape(old) + r"(?=[\s]*[=;,.)\]\[<>+\-*/%&|^!?:])", new, tt2)
                    if tt2 != tt:
                        open(p, "w", encoding="utf-8").write(tt2)
                        n += 1
            print(f"  inherited {base}.{old} -> {new} in {n} files")

if __name__ == "__main__":
    main()
