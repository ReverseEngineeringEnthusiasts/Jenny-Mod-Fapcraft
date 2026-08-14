# Fapcraft 1.12.2 Remap -- engineering log and findings

**Strictly adult oriented (18+) project.** This is a reverse engineering project with a main focus on Java obfuscation.

I do not claim ownership over this mod's source code or any affiliation with the original developers of this mod.

All original audiovisual assets have been excluded to comply with legalities and Github TOS.

---

# The project

Full deobfuscation of the ZKM-obfuscated Fapcraft 1.12.2 v1.1 jar (modid `sexmod`, package `com.trolmastercard.sexmod`), rebuilt as a clean Maven project, then reobfuscated back to SRG names so it actually runs inside Forge 1.12.2.

Two projects:

| project | path | what it is |
|---|---|---|
| base (decompile) | `../Fapcraft-1.12.2/` | byte-faithful deobfuscation, flat package, Maven build |
| remap (this repo) | `./` | same code restructured into role packages (`entity/`, `client/model/`, `client/renderer/`, `networking/`, `item/`, `util/`, `proxy/`, `command/`, `worldgen/`) + every obfuscated name replaced |

The end result: 299 source files, ~55k lines, every single-letter method, field, and class renamed to something meaningful, and a jar that builds green and runs in game.

---

# The obfuscation

ZKM (Zelix KlassMaster). Several layers, ordered by severity:

- exception pass-and-return wrapping
    ```
    try {
        // code ...
    } catch (RuntimeException runtimeException) {
        throw a(new RuntimeException(runtimeException));
    }

    public static RuntimeException a(RuntimeException runtimeException) {
        return runtimeException;
    }
    ```
- exception table mashing
    ```
    try {
        try {
            try {
                // ... real code
            } catch (RuntimeException runtimeException) {
                throw a(new RuntimeException(runtimeException));
            }
            // code ... or fallthrough
        } catch (RuntimeException runtimeException) {
            throw a(new RuntimeException(runtimeException));
        }
        // code ... or fallthrough
    } catch (RuntimeException runtimeException) {
        throw a(new RuntimeException(runtimeException));
    }
    ```
- dangling catch blocks
    ```
    int a = 5 + z;
    // some code ...
    catch { // dangling catch without prior try
        throw a.b(new RuntimeException());
    }
    ```
- impossible control flow (goto spaghetti)
- local reuse (one `Object z` slot reused as Integer, HashMap, String)
- duplicate identifiers (class `a` containing field `a`, method `a()`, inner class `a`)
- generic stripping (`class A<B>` -> `class A` across classes, methods, params)
- clashing signatures (`int a()`, `float a()`, `a a()` in one class)
- synthetic fill-all wrappers and inner class helpers

On top of that, everything is SRG-mapped (NMS converted to SRG mappings), so there are two layers of name soup: the mod's own ZKM names AND the SRG names of every Minecraft reference.

---

# The pipeline

## 1. Isolate and decompile

Isolate TMC-specific code (shadowed Geckolib classes stay untouched and get passed as classpath so the decompiler can resolve inheritance). Run CFR with anti-obf flags against the SRG-mapped jar.

## 2. The cleaning pre-processor

A pre-deobfuscator written with the ObjectWeb ASM library that strips obfuscation artifacts from bytecode BEFORE decompiling: removes exception wrapping patterns, fixes clashing names, normalizes the garbage. This is what stops the decompiler from vomiting garbage.

## 3. SRG to MCP mapping

Map the SRG names back to readable MCP names so the code becomes human-parseable.

## 4. Manual editing

The long haul. Every remaining `a_clashNNN` token gets a semantic name, verified against:
- the original jar bytecode (`javap -c -p`)
- a clean 2021 non-obfuscated source reference
- a 1.21.1 port of the mod (rechenz port)

---

# Our own SRG tool: MCRepack

The build compiles against MCP-named jars, but Forge 1.12.2 at runtime expects SRG names. ForgeGradle normally does this with `reobf`; we have plain Maven, so we wrote our own.

`tools/MCRepack.java` is a custom ASM-based reobfuscator:

- scans the SRG minecraft jar for every declared `func_XXX` / `field_XXX` with its descriptor
- loads the methods.csv / fields.csv mapping tables (func_XXX -> MCP name)
- builds a reverse map (MCP name + descriptor -> SRG name)
- rewrites the compiled mod jar, renaming every Minecraft reference back to SRG

The hard part was the edge cases. Each one became a commit:

- inherited MC fields written through a mod subclass owner (Item.maxStackSize -> field_77777_bU) were skipped by the naive owner guard -> NoSuchFieldError at runtime
- duplicate SRG names in the CSV for one MCP name (getMinecraft had three: func_71410_x, func_193295_e, func_192989_b) -> wrong one picked -> NoSuchMethodError
- calls on subclass owners (FolderResourcePack.getResourceDomains) needed the full superclass chain walked
- JDK classes must never be remapped (BufferedReader.close() was being turned into func_76708_c, the SRG name of RegionFile.close) -> java.* owner guard
- shadowed third-party libs (jackson under software/bernie/shadowed) must never be remapped -> strict owner allowlist
- forge classes (net/minecraftforge) keep their own MCP method names at runtime, but inherit vanilla MC methods that ARE SRG-renamed -> hierarchy-aware resolution with a BFS over superclasses AND implemented interfaces (ICommand.getName -> func_71517_b, GeckoLibCache.onResourceManagerReload -> func_110549_a)
- mod-declared fields that share an MCP name+desc with an MC field (TrailSegment.world) must keep their names -> walk the full chain, only remap on a hit

The tool also scans extra jars (forge srg jar, MCP jar) purely for their class hierarchy so owner resolution can walk from mod subclasses up to their MC superclasses.

Current state: 8878 method entries, 7846 field entries in the map, and the built jar passes a full sweep for stale or MCP-leaked names.

## TinyGen

`tools/TinyGen.java` generates the tiny mapping format used when building the MCP-named compile jars.

## The mapping tables

`tools/mappings/methods.csv` and `fields.csv` are the full MCP SRG mapping tables (searge name, MCP name, side, description). These came from the stable_39 mappings. `tools/regenerate-mcp-jar.sh` rebuilds the MCP-named compile jars.

---

# The tooling

A whole toolbox grew out of this project:

| tool | what it does |
|---|---|
| `MCRepack.java` | the SRG reobfuscator (see above) |
| `TinyGen.java` | tiny-format mapping generator |
| `rename_class.py` | context-aware class renames (casts, instanceof, static access, arrays, generics) |
| `rename_methods.py` | method rename helper |
| `rename_fields.py` | scoped field rename helper |
| `rename_field_hierarchy.py` | inherited-field renames with subclass propagation |
| `rename_params.py` | brace-matched parameter renames |
| `find_single_letter_fields.py` | the survey tool that counted every obfuscated field |
| `apply_field_renames.py` | batch field rename application |
| `apply-renames.sh` | rename batch wrapper |
| `fix_imports.py` | CFR header normalization |
| `match_oracle.py` / `vote_match.py` / `oracle_methods.py` | oracle fingerprinting: match obfuscated methods to the clean reference source |
| `add_javadoc.py` | selective javadoc helper (used sparingly, see notes) |
| `build.sh` / `build.bat` | universal build scripts that find Java and Maven on any system (JAVA_HOME, PATH, SDKMAN, system dirs), build, and copy the jar to dist/ |
| `push.sh` / `push.bat` | commit + push helpers |

---

# The rename campaign

The numbers:

- 683 single-letter method declarations -> 0
- 787 a_clash method tokens -> 0
- 1007 single-letter fields -> 0
- every single-letter class renamed (KoboldManager.Tribe, RibbonRenderer.WaveFunction, CustomModelList.ModelListEntry, ServerWhitelistManager.ModelData, ...)
- ~30 classes renamed from ZKM short names to meaningful names

The rename strategy was compiler-driven and paranoid:

1. rename the declaration only
2. `mvn -q clean compile`
3. let javac report every broken call site
4. fix each one precisely
5. iterate to zero errors
6. commit

Blanket find-and-replace on call sites was banned after it caused more damage than it fixed. Every batch was committed separately so anything wrong could be reverted cleanly.

The work happened in two days of grinding. The git history originally had 171 near-identical commits. It got rewritten down to 29: the renamer commits merged into 7 massive per-day deobfuscation commits (classes and hierarchy, all fields, a_clash tokens, single-letter methods x3, final methods and inner classes), with the non-renamer work (bug fixes, tooling, docs, MCP build step, formatting) kept as its own commits.

---

# The crash saga

Every crash that got fixed, in rough order. Each one was a real root cause found by comparing our code against the original jar bytecode.

## Mapping layer (the MCRepack fixes)

- `NoSuchFieldError: maxStackSize` -- inherited MC field written through a mod subclass owner got skipped by the owner guard. Fixed in MCRepack.
- `NoSuchMethodError: func_193295_e` -- getMinecraft had multiple SRG names in the CSV; wrong one picked. Fixed with owner-aware resolution.
- `NoSuchMethodError: func_135055_a` (getResourceDomains) -- same duplicate-name problem, callsite on FolderResourcePack. Fixed with hierarchy-aware resolution.
- `NoSuchMethodError: BufferedReader.func_76708_c` -- JDK class method remapped because the map had RegionFile.close -> func_76708_c. Fixed with a java.* owner guard.
- 29 jackson classes with func_76708_c -- shadowed third-party lib remapped. Fixed with a strict owner allowlist.
- `NoSuchFieldError: field_73235_d` (Entity.world) -- wrong SRG picked for a field with multiple declaring classes. Fixed with per-declaring-class candidate tracking.
- mod fields (TrailSegment.world, GirlAiBase.homeVillager) mangled -- mod-declared fields sharing a name with MC fields got remapped. Fixed by walking the full chain and only remapping on a hit.
- `NoSuchMethodError: registerCommand` -- forge owner not in the allowlist. Added net/minecraftforge.
- `AbstractMethodError: LootTableLoadEvent.getName` / `CommandWhitelistServer.func_71517_b` -- forge classes keep their own MCP method names at runtime, but vanilla-inherited methods through forge owners are SRG. Fixed with hierarchy BFS including implemented interfaces.
- `AbstractMethodError` on ICommand.getName overrides -- the mod's own overrides of interface methods needed the interface walk. Fixed.

## Deobfuscation bugs (wrong bodies, wrong calls)

- `StackOverflowError: GalathEntity.hasMaster` -- the method body was `return this.hasMaster()` (infinite recursion). The original was `k_clash637 -> J_clash526` (a delegation to the parent check). Fixed to `super.hasMaster()`.
- `AbstractPlayerGirlEntity.isAnchored` -- a bogus self-recursive override. The original `o_clash456` was a camera scene-active check, not the anchored getter. Split into `isSceneActive()` calling the real getter.
- `GoblinPlayerEntity.isAnchored` -- `return this.isAnchored() || ...` recursed. Fixed to `super.isAnchored() || ...`. These two corrupted AI, interaction, and camera state across all girls (stuck walking, can't interact, disappearing girls) because isAnchored is checked 25 times across 5 entity classes.
- `NullPointerException` on player login -- PlayerIds.onPlayerLoggedIn called a client-only camera reset on the server thread. The original called the server-safe table rebuild. Fixed.

## Gameplay bugs

- ResetGirlPacket boolean inverted -- the earlier deobf flipped the branch so `resetGirl()` ran when `resetPose==true`. The jar does the opposite: the FULL reset (player physics via resetGirls + girl release via resetGirl) runs on the SINGLE-ARG packet (`resetPose==false`), which is what the natural scene end sends (cumDone sound -> resetCameraAndPhysics -> resetLocalPlayerClientState -> single-arg packet). The two-arg TRUE packet is only the player-only reset used by strip/doggy transitions. With the branch inverted, every natural scene end left the girl anchored/noGravity/noClip/interacting forever: slime/goblin "stuck against each other, can't move, both defy gravity, can't hit her" after the scene ends, "reload puts me BACK into the sex scene" (the girl's interaction partner + anchored state were never cleared server-side), goblin "stands still, invulnerable, no gravity". Fixed by matching the jar bytecode: `if (!resetPose) resetGirl(girl)`.
- The invented hasPlayer reset branch -- `l_clash514` (the per-tick followUp transition) had an extra branch NOT in the jar that reset every hasPlayer action to NULL on the first server tick. Scenes flashed and died. Removed.
- The invented setDead catch in BaseGirlEntity.onUpdate -- NOT in the jar. The jar's onUpdate is just `super.onUpdate(); tickFollowUpTransitions();`. be258ed wrapped super.onUpdate in try/catch and setDead'd the girl on ANY tick exception; 6e489ab narrowed it to NPE+riding, but girls RIDE during follow-mode and scenes, so any benign scene-action NPE while riding still deleted the girl (Jenny/Bia "disappear + gone on reload"). Removed entirely; the ride guard stays as a dismount-only cleanup (never removes the girl).
- The R-Shift scene exit -- the original keybind (d99f7fb) sent a single-arg ResetGirlPacket per interacting girl, which (with the jar's true semantics) IS the full reset. 49e6f87 reworked it to PROGRESS the scene (triggerCumAction/triggerFastSexAction chain), which only helps a scene that is mid-action with a valid next/cum action; a girl stuck at PAYMENT or NULL goes nowhere. 6e489ab then added a "safety net" that sent ResetGirlPacket(playerUUID) -- the PLAYER's UUID, which girlList() never matches (girl IDs are random, not the player's persistent ID), so the safety net was a no-op. Restored the original single-arg-per-girl reset using the GIRL's UUID.
- Old-world corruption guard -- a bee saved with a corrupt ride chain by an earlier build crashed vanilla's tick every load. BaseGirlEntity.onUpdate now clears stale/dead rides before super (dismount-only; does NOT remove the entity).

## Build / environment bugs

- Forge 2859 lacks SWIM_SPEED (added in 2860) -- removed all references.
- Decompiler dropped Main.instance in registry and GUI handler registration -- restored.
- Item/block registry ClassCastException -- raw Register instead of Register<Item>/Register<Block>.
- Stream-closed IOException in Main.setConfigs -- decompiler couldn't verify finally blocks; fixed with try-with-resources.
- Dropped doRender bridges -- renderers dispatched to the geckolib base no-op instead of the custom render. Same bug class in 6 renderers.
- Dropped setLivingAnimations bridges -- girl models always rendered the steve subtree and armor bones because the per-frame hook was dead code.
- GirlRenderer.getEntityTexture returned the player's skin instead of the girl's own texture -- every girl rendered as a garbled steve.

---

# What runs now

The game loads, worlds load, all 6 mods come up, NPCs spawn and walk, interactions and scenes work, scenes end cleanly (the natural end releases the girl and the player: un-anchor, clear the interaction partner, restore gravity/noClip, re-add AI tasks), R-Shift leaves a scene with a full per-girl reset, and old worlds with corrupt rides get their stale rides cleared instead of crashing.

The remaining known rough edges:
- the mod's own assets (geo models, textures, animations) are not bundled; the user supplies them
- the MolangParser IndexOutOfBounds log during startup is caught and harmless
- `Skipping bad option: lastServer:` in the log is vanilla noise

---

# The lessons

- The compiler is the best deobfuscation tool. Rename the declaration, let javac find every call site, fix them one at a time. Never blanket-replace call sites by name.
- `this.X()` where it should be `super.X()` is the silent killer. It compiles, it looks right, and it either recurses forever or silently breaks the override chain. Scan for self-calls in overrides.
- Line numbers in crash reports lie after deobf. The method names in the stack are the truth; the line numbers map to a different source than you think.
- Forge classes keep their own MCP method names at runtime; vanilla MC classes are SRG. A reobfuscator has to know which is which.
- Deobfuscation creates bugs. Lots of them. Each one needs the original jar bytecode as ground truth.

---

# References

- The original obfuscated jar (Fapcraft 1.12.2 v1.1)
- https://github.com/palkaline/jenny-mod-re (independent reconstruction)
- The rechenz 1.21.1 port (clean naming reference)
- MCP stable_39 mappings (the SRG tables)
