# Hidden / Developer Features

Everything in the mod that is hidden, dev-only, dormant, or easy to miss.

## DebugMode (`util/DebugMode.java`)

Developer-only debug tooling — **active only in a deobfuscated (dev) environment**
(`isDeobfuscated()`); obfuscated builds never touch it.

- Registers client chat commands: `set N value`, `get N`, `time`, `girls`, `kobs`,
  `setcumtime`, `resetcolor` (see [commands.md](commands.md)).
- Prints **tribe-task diagnostics when a kobold is hurt**.
- The shared dev-float array `DEV_FLOATS` is a tuning surface for renderer constants
  (values set via `set`/`get`).

## DebugWindow / DebugWindow2 (`util/`)

Two developer debug overlay windows — **obfuscated-era leftovers, largely dormant**.
Kept jar-faithful; not user-facing.

## SceneDebug (`util/SceneDebug.java`)

Special-build scene debug logging. Every subsystem has its own flag
(e.g. `SceneDebug.CLOTHING` for the clothing screen); `ENABLED=false` silences
everything for release builds. Messages are tagged by subsystem
(e.g. "ClothingScreen: opening folder …").

## DeprecatedCheckForUpdates (`util/`)

Legacy update checker — **no longer used**, kept for reference only.

## The futa command (`/futa`)

Hidden toggle for Galath's futa feature: persists to `sexmod/futa`, spawns dragon-breath
particles at her `cockParticles` bone when enabled (see [commands.md](commands.md)).

## Unused / placeholder items

- **Winchester** (`sexmod:winchester`) — registered, animated, **no gameplay logic yet**.
- **Secret map** (`item.item_map_secret` name = "hehe") — a **dead lang key only**:
  never registered, no model, no code reference. The in-hand map feature is
  `InHandMapRenderer` on vanilla maps (see [items.md](items.md)).

## Dead code kept jar-faithful

- `GirlAiBase` — leftover vanilla villager-breeding AI, **not attached to any girl**.
  Do not wire it up.
- `UnknownPacket` — wire-compat placeholder.
- `DebugWindow`/`DebugWindow2` — dormant overlays.

## Obfuscation-era quirks (documented pitfalls)

- `Vector3fSexmodSpecial.add` **subtracts** and `subtract` **adds** (deobf swap) — callers rely on it.
- `BodyParts.offsetBonePosition` doesn't offset; it progress-lerps toward the skin-color vector.
- `ResetGirlPacket`'s resetPose flag is inverted relative to intuition (jar-verified).
- `RotationHelper.lerpVec3d(int)` is STEP lerp (girls' 40-tick dismount walks) vs
  `lerpVec3dDouble` PROGRESS lerp (render interpolation) — swapping them flings girls 40× the distance.
- Player-kobold eye iris color is literally the `ACTION_TARGET_POS` block position (a debug
  artifact that doubles as an RGB vector).
- `KoboldStatePacket`'s constructor args are named confusingly (tribeId = girl UUID, girlId = player UUID).
- Sound field names are load-bearing (registry path = lowercase field name).

## Reconstruction bugs found by the audit (fixed)

These were carried over from the v1.0 decompile (or introduced while porting) and
are kept here so the next reader does not re-introduce them. Each was verified
against the v1.0 jar before being called a regression or an upstream bug.

- **Inverted `AxisAlignedBB`s.** `AvoidPlayerGoal` and `ManglelieEntity.handleCorruptStart`
  built their scan box as `(pos + offset, pos - offset)` — min > max, so
  `getEntitiesWithinAABB` always returned an empty list. The goal could never see a
  Galath, and the Manglelie corrupt scene stalled before its second step. Upstream,
  not a port regression. `KoboldBlockLookup.scanSavedPositions` had the same class of
  bug via `pos.subtract(new Vec3i(-3,-3,-3))` (a zero-size box).
- **Galath flight counters.** `CHANGE_POSITION` advanced `SWORD_ATTACK_PROGRESS` while
  its completion predicate read `ATTACK_PROGRESS`, so the counter stayed 0: `noClip`
  never turned off and `ATTACK_SWORD` cycled its damage ticks forever.
- **Galath target selection** was inverted — a tamed galath hunted players and a wild
  one hunted mobs, which made the wild RAPE action unreachable.
- **`Action.ticksPlaying` lives on the enum constant**, so two girls in the same action
  advanced one counter twice per tick and any girl leaving rewound the others. The
  entity now keeps its own counter (`BaseGirlEntity.actionTicks`) and writes the enum
  field only as a legacy mirror for the coin/boost scenes.
- **Manglelie pose pass got the wrong bones.** `GalathNpcModel` hands its own
  `AnimationProcessor` to `ManglelieNpcModel.animateModel` while hugging, and that pass
  toggles Manglelie-only bones (`cheekRBelowSkirt`, `skirt`, …). `getBone` returned
  null and the render thread crashed. All bone lookups now go through
  `client/model/ModelBones`, which degrades a missing bone to a no-op.
- **Escape-minigame HUD** restarted its slide every time the looping rape animation
  re-fired `enableRapeUI`, and only ever hid via the `removeUI` keyframe, so it could
  bounce up and down and outlive its scene. It now tracks the owning Galath and hides
  when that scene ends.
- **Kobold work assignment** called `addWorker` where `removeWorker` was meant, and
  `setTribeHomeForMember` passed the member UUID where the tribe id was expected
  (saved member positions were never restored on load).
- **`RemoveItemsPacket`** trusted the client (item type only, no count clamp, player
  UUID from the packet).
- **Container GUIs** registered themselves in a static list and never removed the
  entry, so closing a girl's inventory re-uploaded stale snapshots.
- **Three more render-thread bone crashes** (`GalathRenderer` drawing `body`/`steve`
  unguarded, `GalathCoinRenderer` rendering `pentagram` and indexing `topLevelBones[0]`
  blind, `ManglelieRenderer`'s two-bone pass on a model without `body2`). All bone
  lookups are null-safe now — see `rendering.md`.
- **`NearestAttackableGirlGoal`'s 3-arg constructor** shifted its arguments: the caller's
  `nearbyOnly` landed in the daylight flag and `onlyNearby` was hard-wired false, so the
  kobold spider was night-only instead of nearby-only.
- **`ThreadNames.weightedRandomIndex`** weighted candidates by their index, so candidate 0
  could never be returned and the worst candidate dominated — exactly backwards for
  `GalathFlightData`, which sorts best-first before calling it.
- **`GirlFollowGoal`** entered RIDE when `startRiding` failed (any saddled horse passed the
  condition), welding an invisible noClip girl to the master; it also incremented
  `bowTimer` twice per tick, halving the draw time.
- **`AbstractPlayerGirlEntity`** called `Optional.get()` unguarded on save and
  `UUID.fromString("")` on load, so a girl with no owner or any older save crashed the
  world save/load.
- **`BasicGirlEntity`** removed itself from the world on a daemon thread
  (`ThreadNames.createDaemonThread(6250, …)`), which is not thread-safe and re-armed on
  every hit; it is a tick-counted removal now.
- **`SkinFetcher`** leaked its `BufferedReader` and both fallback streams (no
  try-with-resources), and `AlliesLampRenderer` calls it from inside `render()`.
- **`StructureMarkerRenderer`** iterated a plain `HashSet` mutated from netty (the CME
  catch was a band-aid) and re-queried `getBlockState` per marker per frame.
- **`CummyEntity.onRenderWorldLast`** did texture binds and GL toggles every frame even
  with no trails, and restored them inside `if (mc.player != null)`.
- **Preview girls** created by the GUI screens were registered in `GirlRegistry` and never
  unregistered.
- **The threesome rendered frozen / with a garbled player.** The shared
  `animation.shared.double_holding_*` clips are played by the **Manglelie** and animate
  `body2`, a bone that exists only in `geo/galath/galath_con_mang.geo.json` (532 bones;
  root `rotationTool` with `body`/`coin`/`steve`/`body2`). Both `GalathNpcModel` and
  `ManglelieNpcModel` expose that file as outfit index 2, but nothing ever sets
  `OUTFIT_INDEX` to 2 — not in this tree, not in the v1.0 jar — so the clips ran against
  the normal model and the pose never moved. `ManglelieNpcModel.getModelLocation` now
  returns the combined model while she is in a `THREESOME_*` action, and
  `ManglelieRenderer` binds `getSkinTexture(...)` before its `steve` pass: it had been
  binding the girl's own texture, which wrapped the Manglelie's UVs over the player's
  body.
- **`ManglelieRenderer.renderModelBuffer`** assumed the Manglelie geo model always
  carries the threesome bones (`body2`, `steve`) as direct children of the top bone.
  A reskinned/custom model without them left both lookups null and
  `renderRecursively(null, …)` NPE'd on `bone.getName()`. Upstream code does the same;
  it now falls back to the normal render pass when either bone is missing.

Remaining, deliberately un-fixed: ~126 unreferenced tuning constants, and three
constants in `GalathEntity` literally named after classes (`KoboldTask = 25`,
`ForgeEventHandler = 1.5F`, `SexModEntities = 0.55F`) — almost certainly a
deobfuscation-script collision; nothing reads them and renaming would be guesswork.

## How to audit this codebase (method that worked)

The v1.0 decompile is checked in next to this repo
(`../Fapcraft-1.12.2`) and is SRG-named (`func_12345_a`, `field_…`). It is the reference for
telling a **port regression** from an **upstream bug**: if the reference has the same code, the
bug shipped in the original jar; if not, the reconstruction introduced it. Both kinds were fixed,
but only the first kind is our fault.

Recipes that paid off:

- **Inverted scan boxes:** grep `new AxisAlignedBB(` and read every two-argument call. `add`/
  `subtract` in the wrong order produces min > max; `subtract(new Vec3i(-3,-3,-3))` produces a
  zero-size box.
- **Bone lookups:** grep `getBone("…").` — any call that discards null crashes when a reskin or a
  different model variant lacks the bone. Static bone access belongs in `ModelBones`.
- **Model/animation cross-check:** `python3 -c` over `assets/sexmod/geo/*.geo.json`
  (`minecraft:geometry[0].bones`) and `animations/*/*.animation.json` (`animations[*].bones`).
  A clip that keys a bone no model has is either dead content or a model nobody selects — that is
  how the `body2`/combined-model gap was found.
- **Decompiler artifacts:** a structural parse (brace-match class bodies, blank comments and
  strings) beats regex — a regex pass once called `KoboldEntity.aY` dead while three qualified
  `Kobold.…` references existed. Rename only where a use site, NBT key or accessor proves the
  meaning; leave the rest and say so.
- **Runtime proof:** build, swap the jar into the Prism instance's `mods/`, launch, then read
  `logs/latest.log` for `Crash report`, `NoSuchMethodError`, `NoClassDefFoundError` and
  `[sexmod-scene]` markers. A boot test that reaches `Forge Mod Loader has successfully loaded`
  and a world load catches linkage/structure breakage; it does not catch scene logic — that
  needs a real playthrough.
- **JVM-level questions** (is this GL state set? who draws this?) are fastest with a tiny
  `-javaagent`/coremod that logs `glIsEnabled`/stack traces at the suspect method, then removed.

## The porn warning window

On game start (before world load), the mod shows the adult-content gate:
"Adult content warning!" with "I'm at least 18 years old!" / "I'm below the age of 18!"
and a "don't ask again" toggle. Declining **removes the content from your system and closes
the game** (per the lang text: "the pornographic content will be removed of your System,
the Game will be closed").

## Preload world

`SexWorldClient` is a client-only throwaway flat "plains" world created at startup purely to
preload every NPC's GeckoLib model/animation (so first contact has no load hitch). It is neutered
(no weather, no block updates, solid ground at y≤63, zero creatures, full sun, mining forbidden)
and **never used for gameplay**. Entity code checks `instanceof SexWorldClient` to skip preview logic.

## Threading

- `ThreadNames` names daemon threads after the side they run on (via `ClientServerCheck`).
- `SkinFetcher` downloads the player's Mojang skin synchronously — never call from the render
  thread; falls back to bundled `steve.png`, then null.

## Login/backup behaviour

`PlayerIds` restores transformed players to vanilla state on login, releases half-open scenes on
logout, and re-registers the fixed-UUID players (Bia, Ellie).
