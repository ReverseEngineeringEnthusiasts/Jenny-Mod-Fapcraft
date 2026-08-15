# Slime girl — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.slime.name = Slime girl` |
| Voice actress | **None** — no `girls.slime.*` sound events exist; she uses vanilla slime sounds (`ENTITY_SLIME_*`, `BLOCK_SLIME_HIT`) + shared misc SFX. No credit in mcmod.info |
| Entity class(es) | `SlimeEntity` (NPC, extends `BaseGirlEntity` directly — no AbstractGirlNpcEntity), `SlimePlayerEntity` (player form, extends `AbstractPlayerGirlEntity`), `WildSlimeEntity` (vanilla `EntityLiving` mob, "friendly slime") |
| Model / Renderer | `SlimeNpcModel` (geckolib; geo `slime/nude`(0) / `slime/armored`(1) / `slime/dressed`(2)) + `SlimeRenderer` (NPC, scaleFactor −0.2, blacklists `figure` bone) / `PlayerSlimeRenderer` (player form, translate −1.25, scale 0.8, bone-pose remaps). `WildSlimeRenderer` = vanilla `RenderLiving` with the mod's **`GoblinModel`** as body + `WildSlimeFaceLayer` (vanilla `ModelSlime` face quad) |
| NpcType enum | `SLIME(SlimeEntity.class, 168597, SlimePlayerEntity.class, 54816432)` |
| Player form | `sexmod:player_slime` (playerID 54816432) — horny-potion transformation |
| Obtaining | Natural spawn in `SWAMPLAND` / `MUTATED_SWAMPLAND` (weight 10, 1 at a time); spawn egg (`sexmod:slime`, colors 13167780/8244330); `sexmod:friendly_slime` egg (5548484) spawns the wild slime mob; player form via horny potion |

## Animations (complete list)

Source: `animations/slime/slime.animation.json` (format 1.8.0), 28 animations. Controller layout: NPC registers only **action + eyes** (no movement controller); the jump cycle runs through the action controller via `SlimeEntityState` (IDLE → JUMP_START → JUMP_AIR → JUMP_END). Player form registers action + eyes + movement.

| Animation name | Purpose / when used | Trigger in code |
|---|---|---|
| `null` | Neutral pose (eyes controller and idle-state fallback) | eyes controller when action==NULL (else `fhappy`); action controller default |
| `idle` | Idle blob wobble | `SlimeEntityState.IDLE` (action controller, action NULL); player-form movement controller idle |
| `jumpstart` | Squash-down before the hop (emits `SVS…` scale keyframes, `jumpStart` → `ENTITY_SLIME_JUMP`, `jumpStartDone` → JUMP_AIR) | `SlimeEntityState.JUMP_START` (client sets at `jumpTicks == 90`) |
| `jumpair` | Mid-air stretch | `SlimeEntityState.JUMP_AIR` |
| `jumpend` | Landing squish (emits `SVS…`, `jumpEndSound` → `ENTITY_SLIME_SQUISH`, `jumpEndDone` → IDLE) | `SlimeEntityState.JUMP_END` (client on land) |
| `undress` | Strip her dress (loop=false; emits `undress` → server: currentModel 0 + NULL; player form: OUTFIT 0 + reset) | action controller, case UNDRESS |
| `dress` | Put dress back on (loop=false; emits `dress` → OUTFIT_INDEX 1 + NULL + `resetCameraAndPhysics`) | action controller, case DRESS |
| `strip` | Strip scene (loop=false; emits `startStrip`, `stripMSG1`, `becomeNude` → OUTFIT 0, `stripDone`) | action controller, case STRIP |
| `blowjobsuck` | Blowjob suck phase (loop; emits `sexUiOn` (show meter), `bjiMSG12`/`bjiMSG11` — squish sounds +0.02) | action controller, case SUCKBLOWJOB |
| `blowjobthrust` | Blowjob thrust phase (loop; emits `bjtMSG1` (hit+death sounds +0.04), `sexUiOn`, `bjtReady` (jump resets animation offset), `bjtDone` → SUCKBLOWJOB) | action controller, case THRUSTBLOWJOB |
| `blowjobcum` | Blowjob cum (loop=false; emits `bjcMSG1..7` — jump sounds, hide meter, `bjcBlackScreen`, `bjcDone` → reset + pregnancy 2400) | action controller, case CUMBLOWJOB |
| `blowjobintro` | **Gap**: `SlimeEntity.animationPredicate` requests `animation.slime.blowjobintro` for STARTBLOWJOB, but **no such animation exists in the JSON** (Jenny has `animation.jenny.blowjobintro`, slime doesn't) — geckolib finds nothing; scene still advances via sound keyframes of the *next* animation | action controller, case STARTBLOWJOB (dangling reference) |
| `doggygoonbed` | "Go on bed" crawl (loop=false; emits `doggyGoOnBedMSG1` (squish + cameraYaw lock), `doggyGoOnBedDone` → WAITDOGGY) | action controller, case STARTDOGGY |
| `doggywait` | Waiting on the bed (loop; emits `sexUiOn`) | action controller, case WAITDOGGY |
| `doggystart` | Doggy start/insert (loop=false; emits `doggystartMSG1..5` — touch/squish/smallinserts/pounding, `doggystartDone` → DOGGYSLOW + meter) | action controller, case DOGGYSTART |
| `doggyslow` | Doggy slow (loop; emits `doggyslowMSG1` (pounding + random slime sound +0.02), `sexUiOn`, `doggyslowMSG2` (BLOCK_SLIME_HIT)) | action controller, case DOGGYSLOW |
| `doggyfast` | Doggy fast (loop; emits `sexUiOn`, `doggyfastMSG1` (pounding + alternating slime sounds +0.04), `doggyfastReady` (jump resets offset), `doggyfastDone` → DOGGYSLOW) | action controller, case DOGGYFAST |
| `doggycum` | Doggy cum (loop=false; emits `doggycumMSG1..5` (cuminflation+pounding+death), `bjcBlackScreen`, `doggyCumDone` → reset + pregnancy 2400) | action controller, case DOGGYCUM |
| `sit` | Sitting (player form: RIDE/SIT actions) | `SlimePlayerEntity` movement/action controller |
| `fly` / `fly2` | Airborne poses (player form, `!af` toggling via `ap`) | `SlimePlayerEntity` movement controller |
| `walk` | Walking (player form) | `SlimePlayerEntity` movement controller |
| `backwards_walk` | Backwards walk (player form) | `SlimePlayerEntity` movement controller |
| `run` | Running (player form, `aj` flag) | `SlimePlayerEntity` movement controller |
| `ride` | **Gap**: `SlimePlayerEntity` case RIDE requests `animation.slime.ride` — not in the JSON | player-form action controller (dangling reference) |
| `attack0` / `attack1` / `attack2` | Melee swings (player form; emit `attackSound`/`attackDone` — note `attackSound` has no case in the slime sound listener) | `SlimePlayerEntity` action controller, case ATTACK |
| `bowcharge` | Bow draw (player form) | `SlimePlayerEntity` action controller, case BOW |
| `fhappy` | Happy face (eyes controller while a scene action with `autoBlink` runs) | eyes controller, `action != NULL && action.autoBlink` |

Sound-keyframe markers per animation (from `sound_effects`): `jumpstart` → jumpStart/jumpStartDone; `jumpend` → jumpEndSound/jumpEndDone; `doggyslow` → doggyslowMSG1/sexUiOn; `doggyfast` → sexUiOn/doggyfastMSG1/doggyfastReady/doggyfastDone; `doggycum` → doggycumMSG1..5/bjcBlackScreen/doggyCumDone; `doggystart` → doggystartMSG1..5/doggystartDone; `undress` → undress; `strip` → startStrip/stripMSG1/becomeNude/stripDone; `dress` → dress; `blowjobcum` → bjcMSG1..7/bjcBlackScreen/bjcDone; `blowjobsuck` → sexUiOn/bjiMSG12/bjiMSG11; `blowjobthrust` → bjtMSG1/sexUiOn/bjtReady/bjtDone; `doggygoonbed` → doggyGoOnBedMSG1/doggyGoOnBedDone; `attack0/1/2` → attackSound/attackDone.

## Scenes & Actions

The Slime NPC has **no interaction menu and no `processInteract` override** — scenes are entirely driven by the pregnancy lifecycle (`checkInteractionTrigger`, server AI tick, only when `TICKS_UNTIL_BIRTH >= 2`):

- **Blowjob** (start via `SUCKBLOWJOB` directly — the `STARTBLOWJOB`/`blowjobintro` gap is only hit on the client animation path): player within 1.0 block, on ground, not already in a scene → both get anchored (IS_ANCHORED, noGravity, noClip; player gets `SetPlayerMovementPacket(false)`), player placed `0.65` blocks in front (`VectorMath.rotateByYaw(0,0,0.65)`) facing her yaw → SUCKBLOWJOB. Progression: SUCK ↔ THRUST (`getNextAction`; sneak to thrust, jump keeps thrust via `bjtReady`/`doggyfastReady` controller-offset reset), cum via `getCumAction` → CUMBLOWJOB → `bjcDone` → `resetCameraAndPhysics()` + `changeDataParameterFromClient("pregnant", "2400")`.
- **Doggy** (requires bed visual — `doggygoonbed` crawl, but **no bed search**; no `goToSexBed`): if progress ≥ 4 and on ground and idle, she self-starts STARTDOGGY (anchored); else a nearby player triggers it the same way as blowjob. `doggyGoOnBedDone` → WAITDOGGY (waits for the locked player), then DOGGYSTART → SLOW ↔ FAST → CUM → `doggyCumDone` → reset + pregnancy 2400.
- **Undress/dress**: dosing with the horny potion (or jump progress reaching 1) triggers UNDRESS; `dress` re-equips (OUTFIT 1).
- **`setCurrentAction` guard**: re-entering THRUST/SUCK while `CUMBLOWJOB` plays, or DOGGYFAST/SLOW while `DOGGYCUM` plays, is blocked.

No payment gate, no horny meter show at start (meter appears via `sexUiOn` keyframe; hidden at `bjcMSG2`).

## Dialogue (all lang lines)

**None.** No `slime.dialogue.*` keys in `en_us.lang` and no hardcoded chat lines in `SlimeEntity`/`SlimePlayerEntity`. The slime is non-verbal (sound effects only). Name tag is suppressed (`shouldRenderNameTag()` returns false).

## Sounds

No `girls.slime.*` events in `sounds.json` and no folder under `sounds/girls/`. All SFX are vanilla slime sounds + shared misc events (from the sound listener):

| Event | Used for |
|---|---|
| `SoundEvents.ENTITY_SLIME_JUMP` | jumpstart, bjiMSG12 (20%), bjtMSG1, bjcMSG1/2, doggyslowMSG1 (random), doggyfastMSG1 (alternating) |
| `SoundEvents.ENTITY_SLIME_SQUISH` | jumpEndSound, doggygoonbed, doggystartMSG3, doggyfastMSG1 (alternating), bjiMSG11/12 |
| `SoundEvents.BLOCK_SLIME_HIT` | bjtMSG1, doggyslowMSG1/2, doggyfastMSG1 (alternating) |
| `SoundEvents.ENTITY_SLIME_DEATH` | bjtMSG1, doggycumMSG1 |
| `MISC_TOUCH[0/1]` | doggystartMSG1/2 |
| `MISC_SMALLINSERTS` (random) | doggystartMSG4 |
| `MISC_POUNDING` (random) | doggystartMSG5, doggyslowMSG1, doggyfastMSG1, doggycumMSG1 |
| `MISC_CUMINFLATION[0]` | doggycumMSG1 (volume 4.0) |
| `MISC_PLOB[0]` | `handleHornyLevel` when `HORNY_LEVEL == 0` (birth moment) |

## Model & Appearance

- Geckolib `SlimeNpcModel`: `getModelLocations()` = `geo/slime/nude.geo.json` (index 0, 251 bones), `geo/slime/armored.geo.json` (index 1, 296 bones), `geo/slime/dressed.geo.json` (index 2, 254 bones); all declare identifier `geometry.slime`. Texture: `textures/entity/slime/slime.png`.
- **Outfit quirk**: the code uses OUTFIT_INDEX **1** as the dressed state (`reinitTasks` sets 1; `dress` sound sets 1; `becomeNude`/pregnancy set 0) — i.e. the "armored" geo file is the dressed variant in practice; index 2 (`dressed.geo.json`) is never selected by slime code.
- **Bed-slime bones**: `bedSlime` + `bedSlimeLayer` bones (present in nude/armored geos) are hidden unless the current action is one of `{STARTDOGGY, DOGGYCUM, DOGGYSLOW, DOGGYFAST, DOGGYSTART, WAITDOGGY}` (`SlimeNpcModel.setLivingAnimations`).
- Hat binding: for plain NPCs (not `AbstractPlayerGirlEntity`), the `hat` bone is pose-bound to the `head` bone (rotation+position sum, `applyBoneName`).
- Bone groups: Top = `boobsFlesh`, `upperBodyL/R`, `cloth`; Bottom = `fleshL/R`, `vagina`, `curvesL/R`, `kneeL/R`; Attachments = `bigblob`; armor arrays as usual; also `slime`, `figure`, `dress`, `boobsSlime` bones exist in the geos.
- `SlimeRenderer` blacklists the `figure` bone from rendering (both NPC and player forms).
- `PlayerSlimeRenderer.onBoneRenderStart` copies poses: `figure` ← `slime` (rotation/scale/position), `dress` ← `upperBody`, `hat` ← `head`, `boobsSlime` ← `boobs` — the geo's redundant wrapper bones mirror their sources.
- Wild slime renders with `GoblinModel` (a 4-segment-tail body model — see its class doc) + vanilla slime texture `textures/entity/slime/slime.png`, vanilla squish scaling (`preRenderCallback`, squish formula with 0.999 base scale), shadow 0.25 × size, plus `WildSlimeFaceLayer` (vanilla `ModelSlime(0)` face quad, alpha-blended, `shouldCombineTextures() = true`, skipped when invisible).
- Scale factor 1.6 (both NPC and player form); `SlimeEntity` uses the inherited `BaseGirlEntity` eye height (no override); `SlimePlayerEntity` overrides to 1.64; `WildSlimeEntity` = 0.625 × height.

## AI & Behavior

- **NPC**: `initEntityAI()` is empty and `reinitTasks()` only resets `TICKS_UNTIL_BIRTH=0` + `OUTFIT_INDEX=1`. No wander/follow goals at all. Locomotion = procedural hopping (`handleJumpState`): client cycles jump animation states (JUMP_START at `jumpTicks==90`, JUMP_END on landing) and locks yaw to `TARGET_YAW`; server hops every ~50 ticks (`stopMovement`: zero motion, `jump()`, rotate to target, launch 0.7 blocks/tick), increments `TICKS_UNTIL_BIRTH` at tick 50 (triggers UNDRESS at progress 1), picks `TARGET_YAW` via `getBirthProgress()` — random angle while not pregnant, aimed at the nearest free player (within 30 blocks, not in a scene) once pregnant. `isJumping` re-arms on landing with 10% chance while not pregnant. `fall()` is a no-op (no fall damage).
- **Horny-potion trigger** (`updateAITasks`): if potion active + idle + `HORNY_LEVEL == -1` → `TICKS_UNTIL_BIRTH = 2`, UNDRESS if dressed, potion removed. This is the "impregnation" step.
- **Pregnancy** (`onUpdate`): HEART particles every 10 ticks while `TICKS_UNTIL_BIRTH >= 2`. `HORNY_LEVEL` countdown (server, per AI tick) spawns SPELL_WITCH particles; at `< 0` a `WildSlimeEntity` spawns in place and the flag clears.
- **Wild slime** (`WildSlimeEntity`): vanilla slime AI (SlimeJumpAI priority 5 always-executes → moveHelper speed 1.0; SlimeWanderAI priority 1 only in water/lava → speed 1.2; SlimeMoveHelper with yaw-limit 90°, squish-delay jumps; SlimeFloatAI exists but is **never added** to the task list), `canDespawn() = false`, splits on death (`setDead`: size>1 → 2–3 children of size/2, vanilla behavior), drops `SLIME_BALL` only at size 1, loot table vanilla `ENTITIES_SLIME` at size 1. **Maturation**: ages every tick; client spawns VILLAGER_HAPPY particles after 5880 ticks (every 10) and CLOUD after 7980; server converts to a fresh `SlimeEntity` (same position/rotation, plays `ENTITY_EXPERIENCE_ORB_PICKUP`) at **8400 ticks**.
- **Static tracking quirk**: `ALL_SLIMES` list + `findSlimesNear(Vec3d)` exist but nothing ever adds/removes entities from the list — dead code (grep confirms only declaration + iteration).
- Loot table: `LootTableHandler.SLIME_TABLE` = `sexmod:slime`.

## Unique mechanics

- **Pregnancy → wild-slime birth lifecycle**: horny potion → progress 2 → hops raise progress to 4+ (scene-ready) → blowjob/doggy cum sets progress = 2400 (growth) → birth countdown (`HORNY_LEVEL`, decremented per AI tick) → `WildSlimeEntity` spawned at her position → 8400 ticks later it matures into a new Slime girl. The "egg" is the wild slime itself — there is no egg item/block in the slime code path (kobolds have the actual egg). Note the odd start: birth countdown starts at −1+1 → first decrement toggles to 0 → `MISC_PLOB` plays → next tick spawns (HORNY_LEVEL decrements to <0).
- **No bed requirement**: the doggy scene plays on an invisible bed (the `doggygoonbed` animation + `bedSlime` bones); she never searches for a real bed.
- **Fall immunity**: `fall()` no-ops on both NPC and wild slime.

## Player form (if any)

`SlimePlayerEntity` — same blowjob + doggy scenes, no pregnancy lifecycle:
- `canBeInteracted() = false`, `A_clash381() = false` (no clash/interact), eye height 1.64, scale factor 1.6.
- Hand model: **`KoboldModel`** (not SlimeModel!) with hand texture `textures/entity/slime/hand.png` (quirk).
- Only owner command: `action.names.blowjob` → `sendActionPacket(0, SUCKBLOWJOB)` + `teleportPlayerToGirl`. Menu offers just `["action.names.blowjob"]`.
- Doggy entry: `updateAITasks` waits in WAITDOGGY for a player within 1.0 block → movement lock + noClip + noGravity + flying for both players (`SetPlayerMovementPacket`, `capabilities.isFlying = true`) → `positionPlayerRelative(0, 0, 0.4, 0, 60)` → DOGGYSTART. `doggyGoOnBedDone` additionally sends `SetPlayerForGirlPacket` to bind the local player.
- No fall-damage immunity code in the player form itself (the NPC/wild forms no-op `fall`); the player form gets `noGravity` during doggy instead.
- Movement controller: walk/run/backwards_walk/fly/fly2/idle/sit (NPC has no movement controller at all).
- Slight meter difference vs NPC: `doggyslowMSG1` adds 0.00666 (NPC 0.02), `doggyfastMSG1` adds 0.02 (NPC 0.04).

## Data keys / NBT

Own keys (explicit ids):
- 113 `TICKS_UNTIL_BIRTH` (VarInt) — pregnancy progress (0..2400; 1 = undress, 2 = pregnant, 4+ = scene-ready, 2400 = birth growth).
- 112 `TARGET_YAW` (Float) — hop direction (random, or aimed at nearest player when pregnant).
- 111 `HORNY_LEVEL` (VarInt) — birth countdown (−1 = not pregnant; decrements per AI tick; <0 spawns wild slime).

`WildSlimeEntity` keys: 110 `SIZE` (VarInt, base 1; hitbox 0.51×size, max HP size², speed 0.2+0.1·size), 111 `AGE_IN_TICKS` (VarInt).

NBT quirks:
- `SlimeEntity.writeEntityToNBT` writes `hornyLevel` = TICKS_UNTIL_BIRTH and `ticksUntilBirth` = HORNY_LEVEL — **deliberately swapped keys** (jar behavior, documented in code; keep as-is). `readEntityFromNBT` swaps them back and forces `noClip=false`, `setNoGravity(false)`; also sets OUTFIT 0 when `TICKS_UNTIL_BIRTH != 0` (pregnant = nude).
- `WildSlimeEntity` NBT: `Size` (stored −1), `wasOnGround`, `ageInTicks`; `registerFixes` via `EntityLiving.registerFixesMob`.

## Pitfalls & quirks (deobfuscated code notes)

- `animation.slime.blowjobintro` and `animation.slime.ride` are **dangling references** — requested by the animation predicates, absent from `slime.animation.json`.
- `SlimeEntity.animationPredicate` returns `null` (not `PlayState.CONTINUE`) in the `SexWorldClient` preload world — deliberate to skip the preload pass.
- The NPC never registers a movement controller (`registerControllers` adds only action + eyes) — all locomotion anims ride the action controller via the jump state machine.
- NBT horny-level/ticks-until-birth swap will corrupt pregnancy state across saves if "fixed".
- `getBirthProgress()` returns `getRandomAngle()` when `HORNY_LEVEL != -1` — i.e. **during the birth countdown she hops randomly, not toward the player** (the aim-at-player only applies while pregnant-but-not-birthing).
- Wild slime split-on-death means killing a pregnant slime's offspring yields more wild slimes (vanilla behavior retained).
- `reinitTasks` resets her to dressed (OUTFIT 1) and clears pregnancy — called on master/state re-init.
- `shouldRenderNameTag() = false` — the slime NPC never shows its name.
- The player-form hand model is the **kobold** placeholder (`new KoboldModel()`), a copy-paste artifact.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
