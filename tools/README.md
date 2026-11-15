# tools/

| Directory | Contents |
|---|---|
| `build/` | `MCRepack` SRG reobfuscator, MCP mapping CSVs, and `regenerate-mcp-jar.sh` (release build) |
| `refactor/` | Package moves, import repair, access widening, class renames, layout checks |
| `analysis/` | Oracle matching/voting scripts and `TinyGen` |
| `sim/` | Standalone simulation harnesses |
| `renamer/` | Historical varN renaming campaign; see its own README |

`tools/DESIGN.md` is the layout and access policy. Read it before adding a
package, moving a class or widening visibility.

The Maven build uses `build/` for the release reobfuscation step; `refactor/`
and `analysis/` are developer tools and are not part of the mod runtime.

## Refactor sequence

```bash
# rename misleading classes first
python3 tools/refactor/rename_class.py src/main/java <package/path> Old New

# move classes; mapping is { "old.fqn": "new.package" }
python3 tools/refactor/move_packages.py tools/refactor/mappings/layout.json

# make only cross-package package-private types public
python3 tools/refactor/widen_cross_package.py

# add missing imports, then remove imports that only appeared in Javadoc
python3 tools/refactor/add_imports.py
python3 tools/refactor/prune_imports.py
mvn -q -DskipTests compile

# enforce the design language (--strict-side makes legacy client imports fail)
python3 tools/refactor/check_layout.py
```
