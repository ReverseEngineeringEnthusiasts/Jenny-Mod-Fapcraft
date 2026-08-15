# Hidden / Developer Features

Everything in the mod that is hidden, dev-only, dormant, or easy to miss.

## DebugMode (`util/DebugMode.java`)

Developer-only debug tooling — **active only in a deobfuscated (dev) environment**
(`isDeobfuscated()`); obfuscated builds never touch it.

- Registers client chat commands: `set N value`, `get N`, `time`, `girls`, `kobs`,
  `setcumtime`, `resetcolor` (see [commands.md](commands.md)).
- Prints **tribe-task diagnostics when a kobold is hurt**.
- The shared dev-float array `b` is a tuning surface for renderer constants
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
