#!/usr/bin/env python3
"""rename_class.py — context-aware Java class rename across a source tree.

Renames a class/interface/enum: the file, the declaration, import lines, and
TYPE usages only. Deliberately does NOT rename:
  - fields/variables named like the class (e.g. `float ba = 40.0F`)
  - method calls named like the class (e.g. `void ao()` / `this.ao()`)
  - member accesses after a dot (`this.ao`, `var.b2`)
  - tokens inside comments or string literals

Usage:
  python3 rename_class.py <src-root> <package-dir-relpath> <oldName> <newName>
Example:
  python3 rename_class.py src/main/java com/trolmastercard/sexmod/api ao IGalathFinish

Prints every skipped/ambiguous occurrence for manual review. Re-running after
the declaration file was already renamed only fixes remaining references
(degraded mode, for recovering a partially-applied rename).
"""
import os, re, sys

def find_file(root, pkg_rel, old):
    d = os.path.join(root, *pkg_rel.split("/"))
    for f in os.listdir(d):
        if f == old + ".java":
            return os.path.join(d, f)
    return None

def mask_comments_strings(t):
    """Replace comment bodies and string literals with spaces of equal length,
    so regex matches never land inside them."""
    out = list(t)
    i = 0
    n = len(t)
    in_str = False
    while i < n:
        c = t[i]
        if in_str:
            if c == "\\":
                i += 2
                continue
            if c == '"':
                in_str = False
            out[i] = " "
            i += 1
            continue
        if c == '"':
            in_str = True
            out[i] = " "
            i += 1
            continue
        if c == "/" and i + 1 < n and t[i + 1] == "/":
            j = i
            while j < n and t[j] != "\n":
                out[j] = " "
                j += 1
            i = j
            continue
        if c == "/" and i + 1 < n and t[i + 1] == "*":
            j = i
            while j + 1 < n and not (t[j] == "*" and t[j + 1] == "/"):
                out[j] = " "
                j += 1
            if j + 1 < n:
                out[j] = " "
                out[j + 1] = " "
                j += 2
            i = j
            continue
        i += 1
    return "".join(out)

def is_type_use(text, start, end):
    """True if the token at [start,end) is a TYPE use (should be renamed).
    False for declared names (`float ba = 40.0F`), expression refs
    (`x = ao;`), member accesses (`this.ao`), method calls (`ao()`).

    Strategy: look ahead first, disambiguate casts/instanceof by look-behind.
    """
    # look ahead (skip whitespace) — needed to disambiguate `=` behind:
    # `x = ao;` (var ref) vs `== fp.BOW` (static type access)
    i = end
    while i < len(text) and text[i] in " \t":
        i += 1
    after = text[i] if i < len(text) else "\n"
    # followed by '.' -> static type access (`fp.valueOf`, `== fp.BOW`,
    # `? fp.NULL :`). Member accesses (`this.ao.x`) were already skipped by the
    # pre-dot rule in rename_in_file.
    if after == ".":
        return True
    # look behind (skip whitespace)
    j = start - 1
    while j >= 0 and text[j] in " \t":
        j -= 1
    behind = text[j] if j >= 0 else "\n"
    # expression / member-access context -> definitely not a type
    if behind in "=:[!&|+*/%^?~":
        return False
    if behind == ".":
        return False
    if behind == ")":
        return False
    if behind == ">":
        return False  # after generic close: `Foo<Bar> name` -> name
    if behind.isalpha() or behind == "_":
        kw = re.search(r"\w+$", text[max(0, j - 12):j + 1])
        if kw:
            if kw.group(0) in ("return", "case"):
                return False
            if kw.group(0) in ("instanceof", "new"):
                return True  # `instanceof fg` / `new fg(` -> type
    # followed by identifier -> declaration `ao x` (TYPE)
    if after.isalpha() or after == "_" or after == "@":
        return True
    # followed by '[' -> array type `ao[] f` (TYPE) unless `int x[]` (name)
    if after == "[":
        j = i + 1
        while j < len(text) and text[j] in " \t":
            j += 1
        if not (j < len(text) and text[j] == "]"):
            return False
        if behind in "new":
            return True
        if behind.isalpha() or behind == "_":
            return False  # `int x[]` -> x is the name
        return True
    # followed by '>' -> generic close `List<ao>` (TYPE)
    if after == ">":
        return True
    # followed by ',' or ';' -> type only in generic/implements/throws lists
    # (`Map<ar, b2>`, `implements IEllie, fg`). Method args `foo(ar, x)` are
    # expressions -> NOT types.
    if after in ",;":
        return behind in "<,"
    # followed by ')' -> either a cast `(ao)var3` (TYPE) or a method-arg
    # field ref `get(b8))` (NAME). Disambiguate by what follows the ')':
    # `)`/`,`/`;`/newline/EOF after it -> method-arg context -> NAME.
    # expression start after it -> cast -> TYPE.
    if after == ")":
        k = i + 1
        while k < len(text) and text[k] in " \t":
            k += 1
        nxt = text[k] if k < len(text) else "\n"
        if nxt in "),;\n":
            return False
        return True
    # followed by '=' -> declared name `float ba =` (NAME)
    if after == "=":
        return False
    return True

def rename_in_file(path, old, new, verbose=False):
    t = open(path, encoding="utf-8").read()
    # mask comments/strings so tokens inside them are never touched
    masked = mask_comments_strings(t)
    out = []
    skipped = []
    pat = re.compile(r"\b" + re.escape(old) + r"\b")
    for m in pat.finditer(masked):
        start, end = m.start(), m.end()
        pre = masked[start - 1] if start > 0 else ""
        post = masked[end] if end < len(masked) else ""
        # 1) import lines: always rename
        line_start = t.rfind("\n", 0, start) + 1
        line = t[line_start:end + 40]
        if line.startswith("import ") and (".%s;" % old in line or "import %s;" % line == old):
            out.append((start, end, new))
            continue
        # 2) member access after dot: skip
        if pre == ".":
            skipped.append((start, end, "member access after dot"))
            continue
        # 3) `old(`:
        #    - preceded by `new ` -> constructor call -> RENAME
        #    - otherwise a method call on an object named `old` -> SKIP
        if post == "(":
            # find previous non-space token on this line
            j = start - 1
            while j >= 0 and t[j] in " \t":
                j -= 1
            # walk back over `new` keyword
            if j >= 0 and re.match(r"new$", t[max(0, j - 2):j + 1]):
                out.append((start, end, new))
            else:
                skipped.append((start, end, "method call"))
            continue
        # 4) declared NAME / expression ref (`float ba =`, `x = ao;`): skip
        if not is_type_use(t, start, end):
            skipped.append((start, end, "name/expr ref"))
            continue
        # 5) everything else in type position: rename
        out.append((start, end, new))
    for start, end, rep in reversed(out):
        t = t[:start] + rep + t[end:]
    open(path, "w", encoding="utf-8").write(t)
    if verbose or skipped:
        print(f"  {os.path.basename(path)}: renamed {len(out)}, skipped {len(skipped)}")
        for s, e, why in skipped[:10]:
            print(f"    SKIP {t[max(0,s-30):e+10]!r}  ({why})")
    return len(out), skipped

def main():
    root, pkg_rel, old, new = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
    file_path = find_file(root, pkg_rel, old)
    new_path = None
    if not file_path:
        print("WARN: declaration file not found:", os.path.join(root, pkg_rel, old + ".java"),
              "- continuing with reference renames only")
    else:
        # 1. declaration inside the file
        t = open(file_path, encoding="utf-8").read()
        t2 = re.sub(r"\b(class|interface|enum)\s+" + re.escape(old) + r"\b", r"\1 " + new, t)
        if t2 == t:
            print("WARN: no declaration found in", file_path, "- continuing with reference renames only")
        else:
            # rename constructor declarations: `old(` preceded by decl context (start of
            # line, access modifier, `{`, `}`, `;`) — but NOT call sites (`= old(`, `(old(`)
            t2 = re.sub(r"(?m)(^|[{}\n;]\s*(?:public|protected|private)?\s*)\b" + re.escape(old) + r"\s*\(",
                        r"\1" + new + "(", t2)
            open(file_path, "w", encoding="utf-8").write(t2)
            # 2. rename file
            new_path = os.path.join(os.path.dirname(file_path), new + ".java")
            os.rename(file_path, new_path)
            print(f"declaration + file: {os.path.basename(file_path)} -> {os.path.basename(new_path)}")
            # 2b. type uses of the old name inside the declaration file itself
            # (e.g. `fp[] f`, `fp followUp`, ctor params typed fp). The class declaration
            # line itself no longer contains the old name (renamed in step 1), so this is safe.
            n, sk = rename_in_file(new_path, old, new)
            print(f"  self-file type-use renames: {n}, skipped {len(sk)}")
    # 3. all references tree-wide
    total = 0
    fp_abs = os.path.abspath(file_path) if file_path else None
    np_abs = os.path.abspath(new_path) if new_path else None
    for dp, _dirs, fs in os.walk(root):
        for fn in fs:
            if fn.endswith(".java"):
                p = os.path.join(dp, fn)
                p_abs = os.path.abspath(p)
                if p_abs == fp_abs or p_abs == np_abs:
                    continue
                n, sk = rename_in_file(p, old, new)
                total += n
    print(f"total type-use renames across tree: {total}")

if __name__ == "__main__":
    main()
