# tools/renamer/ — deobf/rename campaign tooling (backup, 2026-08-15)

Backup of the scripts used to rename ~24k decompiler `varN` tokens to
readable names across the Fapcraft 1.12.2 codebase. Everything is
sanitized: no absolute paths, no usernames, no temp-dir references.

## Layout

| path              | what                                                     |
|-------------------|----------------------------------------------------------|
| `renamer.py`      | **merged CLI** — one tool, six modes (see below)         |
| `core/`           | the two renamer engines (libraries imported by renamer.py)|
| `checks/`         | QA: brace/token/javadoc/verify scripts                   |
| `fixes/`          | one-off targeted source patches (fix_*.py)               |
| `extras/`         | investigation one-offs (dump*, region*, add_flags*, ...) |

## Engines (`core/`)

- `vartool.py` — scope-based renamer; spec = `{"scopes": [{"sig": "<regex>", "renames": {"var1": "name"}}]}`.
  `apply` aborts on brace mismatch (validates before writing).
- `rename_varN.py` — mapping = `{"methods": {"<signature or name>": {"var1": "name"}}, "lambdas": {...}, "init": {...}, "global": {...}}`

## renamer.py modes

```
report <file>                      token/scope overview of one file
check  <file>                      list varN tokens still in code/comments/strings
apply  <file> <spec.json>          scopes spec   (vartool format)
rename <file> <mapping.json>       methods spec  (rename_varN format, brace-validated)
batch  --root DIR SPEC.json...     run apply over every scopes entry in the spec(s)
apply-maps --root DIR [--maps M]   run rename over every per-file map in a maps dir
```

Examples:

```bash
python3 tools/renamer/renamer.py report src/main/java/com/trolmastercard/sexmod/Foo.java
python3 tools/renamer/renamer.py check  src/main/java/com/trolmastercard/sexmod/Foo.java
python3 tools/renamer/renamer.py apply  src/main/java/com/trolmastercard/sexmod/Foo.java /tmp/one_file_spec.json
python3 tools/renamer/renamer.py rename src/main/java/com/trolmastercard/sexmod/Foo.java /tmp/one_file_map.json
python3 tools/renamer/renamer.py batch  --root src/main/java /tmp/one_file_spec.json
python3 tools/renamer/renamer.py apply-maps --root src/main/java --maps /tmp/maps
```

Spec formats accepted by `batch`: `{"files": [{"path", "scopes"}]}`,
legacy `[["rel/path", {"scopes"}]...]` pairs, and planning-only
`{"files": [{"path", "methods"}]}` (skipped with a notice).

## QA after a rename pass

```bash
python3 tools/renamer/checks/check_braces.py
python3 tools/renamer/checks/finalcheck.py
```

`fixes/` and `extras/` are historical: they hardcode campaign file/class
names and were single-use. Keep them as-is for reference; do not "fix" them.