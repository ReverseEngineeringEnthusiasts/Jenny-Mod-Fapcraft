# Package refactor tooling

`move_packages.py` moves top-level Java classes to new packages, rewrites the
`package` declarations and fully-qualified references, and adds the imports
needed by the rest of the tree.

The mapping is JSON: `{ "com.example.old.Class": "com.example.new.package" }`.
Moves use `git mv`. The tool intentionally stops before changing access
modifiers: compile with Maven afterwards and make only the package-private
classes/members that the cross-package references actually need public.

```bash
python3 tools/refactor/move_packages.py tools/refactor/mappings/stage1.json
python3 tools/refactor/add_imports.py
mvn -q -DskipTests compile
```

`add_imports.py` adds imports for public project types that the moved classes
now reference from other packages (for example, `PacketHandler` after a packet
moves below `networking/`). It is idempotent and only touches imports.

`mappings/` keeps the staged mappings used for this tree so a package move can
be reproduced or reviewed.
