#!/usr/bin/env python3
"""Unified CLI for the scope-aware varN renamers (merged vartool.py,
rename_varN.py, renamer_lib.py, batch.py, apply_renames.py).

Modes:
  report <file>                       token/scope overview of one file
  check  <file>                       list varN tokens still in code/comments/strings
  apply  <file> <spec.json>           scopes spec   (vartool format)
  rename <file> <mapping.json>        methods spec  (rename_varN format, brace-validated)
  batch  --root DIR SPEC.json...      run apply over every scopes entry in the spec
  apply-maps --root DIR [--maps DIR]  run rename over every per-file map in maps/

Examples:
  python3 renamer.py report src/main/java/com/trolmastercard/sexmod/Foo.java
  python3 renamer.py check  src/main/java/com/trolmastercard/sexmod/Foo.java
  python3 renamer.py apply  src/main/java/com/trolmastercard/sexmod/Foo.java /tmp/one_file_spec.json
  python3 renamer.py rename src/main/java/com/trolmastercard/sexmod/Foo.java /tmp/one_file_map.json
  python3 renamer.py batch  --root src/main/java /tmp/one_file_spec.json
  python3 renamer.py apply-maps --root src/main/java --maps /tmp/maps

Spec formats:
  {"scopes": [{"sig": "<regex>", "renames": {"var1": "name", ...}}]}   (apply/batch)
  {"methods": {"<signature or name>": {"var1": "name", ...}},          (rename)
   "lambdas": {...}, "init": {...}, "global": {...}}
  {"files": [{"path": ..., "scopes": [...]}]}                          (batch; planning
   or legacy [["rel/path", {"scopes": [...]}], ...]                      specs without
                                                                         scopes are skipped)
"""
import argparse, glob, json, os, sys

CORE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "core")
sys.path.insert(0, CORE)

EX = {
    "report":     'renamer.py report src/main/java/com/trolmastercard/sexmod/Foo.java',
    "check":      'renamer.py check  src/main/java/com/trolmastercard/sexmod/Foo.java',
    "apply":      'renamer.py apply  src/main/java/com/trolmastercard/sexmod/Foo.java specs/spec8.json',
    "rename":     'renamer.py rename src/main/java/com/trolmastercard/sexmod/Foo.java maps/Foo.json',
    "batch":      'renamer.py batch  --root src/main/java specs/spec8.json',
    "apply-maps": 'renamer.py apply-maps --root src/main/java --maps maps',
}

def _load(path):
    with open(path, encoding="utf-8") as f:
        return f.read()

def _write(path, text):
    with open(path, "w", encoding="utf-8") as f:
        f.write(text)

# ---------------- report / check / apply (vartool engine) ----------------

def mode_report(args):
    import vartool
    vartool.report(_load(args.file))

def mode_check(args):
    import vartool
    vartool.print_check(_load(args.file))

def mode_apply(args):
    import vartool
    src = _load(args.file)
    spec = json.load(open(args.spec))
    newsrc, total, errors = vartool.apply(src, spec)
    _write(args.file, newsrc)
    print(f"renamed {total} tokens")
    for e in errors:
        print("ERROR:", e)

# ---------------- rename (rename_varN engine, brace-validated) ----------------

def mode_rename(args):
    import rename_varN
    src = _load(args.file)
    mappings = json.load(open(args.mapping))
    toks = rename_varN.lex(src)
    scopes, errors = rename_varN.scope_scan(toks)
    before = rename_varN.brace_balance(src)
    out, renamed = rename_varN.apply(src, toks, scopes, mappings, errors)
    after = rename_varN.brace_balance(out)
    print(f'{args.file}: scopes={len(scopes)} renamed={renamed} braces {before}->{after}')
    for err in errors:
        print(f'  ERROR: {err}')
    if errors:
        sys.exit(2)
    if before != after:
        print('  BRACE MISMATCH - aborting write')
        sys.exit(3)
    _write(args.file, out)

# ---------------- batch / apply-maps (generalized drivers) ----------------

def _entries(spec, root):
    if isinstance(spec, list):  # legacy batchN pairs format
        for rel, inner in spec:
            yield os.path.join(root, rel), inner
        return
    for f in spec.get("files", []):
        path = f["path"]
        if not os.path.isabs(path):
            path = os.path.join(root, path)
        yield path, f

def mode_batch(args):
    import vartool
    total, failed = 0, 0
    for spec_path in args.specs:
        spec = json.load(open(spec_path))
        for path, inner in _entries(spec, args.root):
            if "scopes" not in inner:
                print(f"SKIP (planning format, no scopes): {path}")
                continue
            src = _load(path)
            newsrc, renamed, errors = vartool.apply(src, inner)
            _write(path, newsrc)
            total += 1
            print(f"{os.path.relpath(path, args.root)}: renamed {renamed} tokens")
            for e in errors:
                failed += 1
                print(f"  ERROR: {e}")
    print(f"TOTAL: {total} files, {failed} errors")
    sys.exit(1 if failed else 0)

def mode_apply_maps(args):
    import rename_varN
    failed = 0
    for mapfile in sorted(glob.glob(os.path.join(args.maps, "*.json"))):
        base = os.path.splitext(os.path.basename(mapfile))[0] + ".java"
        hits = [os.path.join(dp, base) for dp, _, names in os.walk(args.root) if base in names]
        if not hits:
            print(f"SKIP (no {base} under root): {mapfile}")
            continue
        for java in hits:
            src = _load(java)
            mappings = json.load(open(mapfile))
            toks = rename_varN.lex(src)
            scopes, errors = rename_varN.scope_scan(toks)
            before = rename_varN.brace_balance(src)
            out, renamed = rename_varN.apply(src, toks, scopes, mappings, errors)
            after = rename_varN.brace_balance(out)
            print(f"{os.path.relpath(java, args.root)}: renamed {renamed} tokens")
            for e in errors:
                failed += 1
                print(f"  ERROR: {e}")
            if errors or before != after:
                print("  NOT WRITTEN (errors or brace mismatch)")
                continue
            _write(java, out)
    print(f"FAILED: {failed}")
    sys.exit(1 if failed else 0)

# ---------------- CLI ----------------

def main():
    ap = argparse.ArgumentParser(
        prog="renamer.py",
        description="Scope-aware varN renamer for decompiled Java (merged campaign tooling).",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="Examples:\n" + "\n".join("  " + v for v in EX.values()))
    sub = ap.add_subparsers(dest="mode", required=True)

    for name, args_, desc in [
        ("report",     ["file"], "token/scope overview of one file"),
        ("check",      ["file"], "list varN tokens still present (code/comments/strings)"),
        ("apply",      ["file", "spec"], "apply a scopes spec to one file (vartool format)"),
        ("rename",     ["file", "mapping"], "apply a methods mapping to one file (rename_varN format)"),
    ]:
        p = sub.add_parser(name, help=desc, description=desc, epilog="Example:\n  " + EX[name],
                           formatter_class=argparse.RawDescriptionHelpFormatter)
        for a in args_:
            p.add_argument(a)
        p.set_defaults(func={"report": mode_report, "check": mode_check,
                             "apply": mode_apply, "rename": mode_rename}[name])

    p = sub.add_parser("batch", help="run apply over every scopes entry in one or more specs",
                       epilog="Example:\n  " + EX["batch"],
                       formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--root", required=True, help="source root; relative spec paths are joined to it")
    p.add_argument("specs", nargs="+")
    p.set_defaults(func=mode_batch)

    p = sub.add_parser("apply-maps", help="run rename over every per-file map in a maps dir",
                       epilog="Example:\n  " + EX["apply-maps"],
                       formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("--root", required=True, help="source root to search for the java files")
    p.add_argument("--maps", default=os.path.join(os.path.dirname(os.path.abspath(__file__)), "maps"))
    p.set_defaults(func=mode_apply_maps)

    args = ap.parse_args()
    args.func(args)

if __name__ == "__main__":
    main()