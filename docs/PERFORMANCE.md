# Performance & maintainability review

Working notes from an audit of `performance` (branch cut from `master` @ `cd3085c`).
Everything here is source-grounded: file:line points at the code as it stands.

## Status

Applied on this branch (all merged, `mvn -o test` green, `./build.sh` produces the jar):

| § | What landed |
|---|-------------|
| 1.1 / 1.2 | dirty-flagged player-girl table (no rebuild on render events), `containsKey` lookup |
| 1.3 / 1.4 | `GirlFollowGoal` nearest-mob scan (comparator bug fixed), AABB without `BlockPos` |
| 2.1 | bounded LRU skin cache, textures deleted on eviction, invalidation + session clear |
| 3 | `StructureMarkerRenderer` early-out + state snapshot/restore; `try/finally` matrix/state in the renderers |
| 4.2 | identifiers renamed: `PLAYER_GIRL_BY_OWNER`, `SKIN_TEXTURE_CACHE`, `OWNER_UUID`, `usesInvertedCamera()`, `shouldRenderWhenOwnerInvisible()`, … |
| 4.4 / 4.5 | `pom.xml` is the single version source; first unit tests (`UpdateChecker`, version drift) |

Deferred on purpose: §2.2 (dev-only code kept), §3 method-spanning matrix pushes
(`doRenderEntity`, outer `renderCustomBones` — needs a reviewed reindent), §4.1 class
splits, §4.3 logging pass, and everything under §5.

## TL;DR — do these first

| # | Where | Why it matters |
|---|-------|----------------|
| 1 | `client/GirlCameraHelper.java:53`, `util/InHandMapRenderer.java:72` | Rebuilds the player-girl lookup table **every frame**, twice per frame in first person |
| 2 | `entity/AbstractPlayerGirlEntity.java:310` | `hasPlayerGirlWithUUID` rebuilds the table, then linear-scans a map that is already keyed by UUID |
| 3 | `entity/ai/GirlFollowGoal.java:244` | Allocates + sorts the mob list every tick, and the comparator is a no-op (`mobB` on both sides) |
| 4 | `client/renderer/GirlRenderer.java:133` | `static HashMap<UUID, ResourceLocation>` skin cache: unbounded, never invalidated, one GPU texture per player seen |
| 5 | `util/StructureMarkerRenderer.java:205` | Enables lighting unconditionally on exit, for every player, every frame |

---

## 1. Per-frame work

### 1.1 The player-girl table is rebuilt on render events

`AbstractPlayerGirlEntity.rebuildPlayerGirlTableFromWorld()` (`entity/AbstractPlayerGirlEntity.java:608`):

```java
ArrayList toRemove = new ArrayList();
for (AbstractPlayerGirlEntity playerGirl : playerGirlList) { ... PLAYER_GIRL_BY_OWNER.put(...); toRemove.add(...); }
for (AbstractPlayerGirlEntity playerGirl : toRemove) playerGirlList.remove(playerGirl);   // O(n^2)
rebuildPlayerGirlTableInternal();
```

Called from:

- `client/GirlCameraHelper.java:53` — `CameraSetup.Pre`, i.e. **every frame**
- `util/InHandMapRenderer.java:72` — `RenderSpecificHandEvent`, **every frame the hand renders**
- `entity/AbstractPlayerGirlEntity.java:310` (inside `hasPlayerGirlWithUUID`), `:441`
- `util/PlayerIds.java:76` — join path (fine)

Per frame that is: one `ArrayList` allocation, a full list walk, `n` `ArrayList.remove` calls
(O(n²)), then a second rebuild pass. On a server/world with several player-girls this is pure
garbage on the render thread.

**Fix:** keep a `dirty` flag set on spawn/death/login/logout and rebuild at most once per tick
(`ServerTickEvent`/`ClientTickEvent`), or incrementally maintain `PLAYER_GIRL_BY_OWNER` in the entity's
`setDead`/`onAddedToWorld`. Render paths then just read `PLAYER_GIRL_BY_OWNER`.

### 1.2 `hasPlayerGirlWithUUID` is a rebuild plus a linear scan

`entity/AbstractPlayerGirlEntity.java:309`:

```java
rebuildPlayerGirlTableFromWorld();
for (Entry entry : PLAYER_GIRL_BY_OWNER.entrySet()) if (uuid.equals(entry.getKey())) return true;
```

`PLAYER_GIRL_BY_OWNER` is `HashMap<UUID, …>` — `PLAYER_GIRL_BY_OWNER.containsKey(uuid)` is the whole method. `isOwnerPlayer()` calls
this on the hot path too. Removing the rebuild (1.1) makes this a single hash lookup.

### 1.3 `GirlFollowGoal` mob search

`entity/ai/GirlFollowGoal.java:236` — every tick a girl has a follow goal:

- allocates an `AxisAlignedBB`
- runs `getEntitiesWithinAABB(EntityMob.class, …)`
- `mobs.sort((a,b) -> { double distA = mobB.getDistance(girl); double distB = mobB.getDistance(girl); … })`

Both sides read `mobB`, so the comparator always returns 0: **the sort does nothing** and the
"nearest" mob is whichever the world returned first. Two bugs for the price of one.

**Fix:** drop the sort entirely and track the minimum distance in the existing loop:

```java
EntityMob best = null; double bestSq = Double.MAX_VALUE;
for (EntityMob mob : mobs) { double d = mob.getDistanceSq(girl); if (d < bestSq) { bestSq = d; best = mob; } }
```

`getDistanceSq` avoids a `sqrt` per candidate.

### 1.4 Allocation in `updateAITasks`

`entity/AbstractGirlNpcEntity.java:131` builds two `BlockPos` objects and an `AxisAlignedBB`
inside a `%80` branch — minor, but `new AxisAlignedBB(x0,y0,z0,x1,y1,z1)` has no `BlockPos`
round-trip and no `BlockPos.getAllInBox` semantics to lose.

---

## 2. Memory & GPU lifetimes

### 2.1 Skin cache never shrinks or invalidates

`client/renderer/GirlRenderer.java:133`:

```java
protected static HashMap<UUID, ResourceLocation> l = new HashMap<>();
```

`getTintedSkinTexture` (`:182`) registers `new DynamicTexture(skinImage)` per UUID (`:203`) and
returns the location. Consequences:

- one GPU texture (+ CPU copy) per player UUID ever rendered, for the life of the process
- changing a skin never refreshes (cache hit forever)
- the fallback path does `ImageIO.read(...)` from the render thread (`:197`)

**Fix:** bounded LRU (say 64 entries) + `TextureManager.deleteTexture` on eviction; invalidate on
`ClientPlayerNetworkEvent`/skin update; load the fallback once in `RenderHandler.register()`.

### 2.2 `buildModelCode(String)` is dead code that does file I/O

`client/renderer/GirlRenderer.java:392` opens a `FileReader` and reads a file line by line. Nothing
calls it (`grep buildModelCode` only finds the declaration and the unrelated `buildModelCodeDNA`
family). Delete it or move it to a dev tool.

---

## 3. GL state discipline

The dark-GUI bug in 1.1.0 was a state leak; the same pattern is still present:

`util/StructureMarkerRenderer.java:205`:

```java
GlStateManager.enableColorMaterial();
GlStateManager.disableLighting();
ItemStack stack = mc.player.getHeldItem(MAIN_HAND);
if (stack.getItem() != DRAGON_STAFF) stack = …OFF_HAND;
if (stack.getItem() == DRAGON_STAFF) renderMarkers();
GlStateManager.enableLighting();
GlStateManager.enableDepth();
GlStateManager.enableAlpha();
GlStateManager.enableLighting();   // twice
```

- The early-out happens **after** two state changes, so every player pays them every frame.
- Lighting is force-enabled on exit regardless of whether it was on before.

**Fix:** read the held item first, return early; otherwise `pushAttrib`-style save the three flags
and restore exactly what was set.

Same review applies to the renderers that push/pop matrices without a `try/finally`
(`GirlRenderer`, `GalathRenderer`, `ManglelieRenderer`): an exception between push and pop leaks
the matrix stack and the color/lighting state for the rest of the frame.

---

## 4. Development ergonomics

### 4.1 File size

314 files, **64,219 lines**. 35 classes over 500 lines:

| lines | file |
|------:|------|
| 3193 | `entity/GalathEntity.java` |
| 3190 | `entity/KoboldEntity.java` |
| 2159 | `entity/GoblinEntity.java` |
| 1810 | `entity/BaseGirlEntity.java` |
| 1384 | `client/renderer/GirlRenderer.java` |
| 1280 | `entity/GoblinPlayerEntity.java` |
| 1157 | `util/KoboldManager.java` |

These are entities + AI + animation state + persistence + networking in one class. Splitting one
per branch (AI goals already exist as separate classes — keep going) pays for itself the first
time someone has to debug a scene.

### 4.2 Decompiler names are still in the source

The player-girl state (`AbstractPlayerGirlEntity`), the skin cache (`GirlRenderer`) and the
dev-float array (`util/DebugMode`) have been renamed to domain terms. What remains:
`*_clashNNN` methods on the NPC entities (`GoblinEntity`, `GalathEntity`, `KoboldEntity`,
`EllieEntity`, `LunaEntity`, `ManglelieEntity`) and their per-class `aX`/`aB`/`aM` offsets.
These are the single biggest obstacle to reading the code. Rename opportunistically:
touch a file → rename its fields to domain terms → keep behaviour identical.

### 4.3 Logging

- 144 `System.out.print` / `System.err.print`
- 32 `printStackTrace()`
- dozens of empty `catch` blocks

Route through one helper (log4j is already on the classpath via Forge), and at minimum keep the
exception. `System.out` in a render path is a measurable cost when it fires per frame.

### 4.4 Version is duplicated six ways

`pom.xml`, `util/Reference.MOD_VERSION`, `Main` `@Mod(version=…)`, `ServerWhitelistManager.localBuild()`,
`UpdateChecker` User-Agent, `src/main/resources/mcmod.info`. Bumping 1.1.0 → 1.1.1 meant editing all
six by hand (and missing one is silent).

**Fix:** `maven-resources-plugin` filtering for `mcmod.info` (`${project.version}`) plus a tiny
generated `BuildInfo.java`, and make `Reference.MOD_VERSION` read it.

### 4.5 No tests, but CI already builds

`src/test` does not exist. The pure logic is easy to test and would have caught the comparator bug:

- `UpdateChecker.parseVersion` / `compare` (the `v7` vs `v1.1.1` formats are both supported — pin that)
- `ServerWhitelistManager` parsing
- packet encode/decode round-trips
- `ThreadNames` / `RotationHelper` helpers

`build.sh` runs Maven, so `src/test/java` costs nothing in CI.

### 4.6 Sentinel values instead of flags

`client/GirlCameraHelper.java:49` — `if (event.getPartialRenderTick() != 1.2345679F)` uses
`CAMERA_SCALE` as an "is this our camera" boolean. A `boolean cameraActive` field (or a field on the
girl) is clearer and can't collide with a real partial-tick value.

### 4.7 Event registration is scattered

62 `EVENT_BUS.register(...)` sites, 139 `@SubscribeEvent` methods. One `ClientEvents.registerAll()`
/ `ServerEvents.registerAll()` list makes it possible to see, in one screen, everything the mod
hooks — which is also how you find per-frame handlers that shouldn't be per-frame.

### 4.8 Dead / deprecated leftovers

- `util/DeprecatedCheckForUpdates` (registered in `util/ForgeEventHandler.java:105`) — confirm it's
  still needed next to `reskin/UpdateChecker`.
- `GirlRenderer.buildModelCode` (§2.2).
- `Reference.EDITOR_ID_COUNTER` / `BUTTON_ID_COUNTER` — mutable statics used for GUI ids; fine for
  one screen, a source of collisions across screens.

---

## 5. Making the mod better (beyond performance)

1. **Render distance for NPCs.** GeckoLib models are expensive; skip `doRender` past N blocks
   (configurable), and skip animation ticks for entities outside that radius.
2. **Config surface.** Spawn rates, max girls per world, render distance, skin cache size, update
   checker on/off. Right now `sexmod.json` has three keys.
3. **Animation/model cache.** DNA strings (`buildModelCodeDNA`) are rebuilt per spawn; cache by
   `(model, parts)` key — the strings are deterministic.
4. **Packet coalescing.** 62 files touch the network; per-tick sends (e.g. `SendBlocksPacket` in
   `PlayerIds`) can be batched into one world-sync packet per second.
5. **i18n.** 15 lang files; verify the newer GUI strings (reskin browser, sliders) are covered.
6. **Skin refresh keybind/menu entry** instead of "restart to update skin" (see §2.1).

---

## 6. How to measure before/after

- **JFR** (already on the JVM): add `-XX:StartFlightRecording=filename=fc.jfr,duration=120s` to the
  instance's JVM args, then look at allocation hot spots — `rebuildPlayerGirlTableFromWorld`,
  `GirlFollowGoal`, `getTintedSkinTexture` should dominate.
- **Spark** (`/spark profiler start`) if it is installed in the pack — same data, friendlier.
- **F3 pie chart** for frame time split; the GeckoLib model renderer is usually the top slice in a
  crowded scene.
- Baselines worth capturing: main menu, singleplayer idle with 1 girl, 6 girls + 20 kobolds, and a
  scene (worst case for animation + texture binds).

## 7. Suggested order

1. §1.1 + §1.2 (table rebuild + `containsKey`) — smallest diff, biggest per-frame win.
2. §1.3 (comparator bug + min-scan) — correctness and cost together.
3. §3 lighting/state restore — prevents the class of bug fixed in 1.1.1.
4. §2.1 skin cache — memory, and fixes stale skins.
5. §4.4 version single-source, then §4.5 first tests.
6. §4.1/§4.2 refactors per file, opportunistically.
