# Bee — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.bee.name = Bee` |
| Voice actress | **None** — no `girls.bee.*` sound events exist; bee scenes use only shared misc SFX (`misc.pounding`, `misc.cuminflation`). No credit in mcmod.info (`misc.beew` wing sounds belong to Galath, not the bee) |
| Entity class(es) | `BeeEntityBase` (abstract, extends `BaseGirlEntity`, implements `IInventory` — 27-slot chest), `BeeEntity` (NPC, extends `BeeEntityBase`), `BeePlayerEntity` (player form, extends `AbstractPlayerGirlEntity`) |
| Model / Renderer | `BeeNpcModel` (geckolib; geo `bee/bee`(0) / `bee/armored`(1)) + `BeeRenderer` (NPC, scaleFactor −0.4) / `PlayerBeeRenderer` (player form, translate −0.6, scale 0.4); hand placeholder `BeeModel` (IVanillaModel 2×6×2 cube), hand texture `textures/entity/bee/hand.png` |
| NpcType enum | `BEE(BeeEntity.class, 4663354, BeePlayerEntity.class, 48648638)` |
| Player form | `sexmod:player_bee` (playerID 48648638) — horny-potion transformation; grants the owner flight |
| Obtaining | Natural spawn in `FOREST` / `FOREST_HILLS` (weight 5, 1 at a time); spawn egg (`sexmod:bee`, colors 16701032/4400155); player form via horny potion |

## Animations (complete list)

Source: `animations/bee/bee.animation.json` (format 1.8.0), 12 animations. Controller layout: NPC registers action + movement + eyes (but the predicate only handles `movement` and `action` — the eyes controller is registered yet never animated); player form registers action + movement only.

| Animation name | Purpose / when used | Trigger in code |
|---|---|---|
| `null` | Neutral pose (any active scene action) | movement + action controllers when `getCurrentAction() != NULL` |
| `idle` | Hovering idle (no chest) | `BeeEntity` movement controller, action NULL: `"animation.bee." + (HORNY_FLAG ? "idle_has_chest" : "idle")` |
| `idle_has_chest` | Hovering idle with the chest (tamed) | same expression when `HORNY_FLAG` set |
| `sex_start` | Citizen scene intro (loop=false; emits `resetCumPercentage` (reset meter), `sex_startMSG1` (pounding +0.02), `sex_startDone` → CITIZEN_SLOW + show meter) | action controller, case CITIZEN_START |
| `sex_slow` | Citizen slow phase (loop; emits `sex_startMSG1` — pounding +0.02 per loop) | action controller, case CITIZEN_SLOW |
| `sex_fast` | Citizen fast phase (loop; emits `sex_fastMSG1` (pounding +0.04), `sex_fastReady` (jump resets animation offset), `sex_fastDone` → back to SLOW unless jump) | action controller, case CITIZEN_FAST |
| `sex_cum` | Citizen cum (loop=false; emits `sex_cumMSG1` (cuminflation + pounding), `blackscreen` (`BeeScreen.enableInteraction`), `sex_cumDone` → reset meter + `resetCameraAndPhysics`) | action controller, case CITIZEN_CUM |
| `throw_pearl` | "Pearl" throw → sends the girl home (`SendCompanionHomePacket` on `pearl`, gated on action == THROW_PEARL) | action controller, case THROW_PEARL |
| `attack0` / `attack1` / `attack2` | Melee swings (player form only; emit `attackSound`/`attackDone`; `attackSound` has no listener case) | `BeePlayerEntity` action controller, case ATTACK |
| `bowcharge` | Bow draw (player form only) | `BeePlayerEntity` action controller, case BOW |
| `ride` | **Gap**: `BeePlayerEntity` case RIDE requests `animation.bee.ride` — not in the JSON | player-form action controller (dangling reference) |

Sound-keyframe markers per animation (from `sound_effects`): `sex_slow` → sex_startMSG1; `sex_fast` → sex_fastMSG1/sex_fastReady/sex_fastDone; `sex_start` → resetCumPercentage/sex_startMSG1/sex_startDone; `sex_cum` → sex_cumMSG1/blackscreen/sex_cumDone; `throw_pearl` → pearl; `attack0/1/2` → attackSound/attackDone.

## Scenes & Actions

Single scene: **citizen sex** (`CITIZEN_START → CITIZEN_SLOW ↔ CITIZEN_FAST → CITIZEN_CUM`).

- **Wild bee approach** (`handleBeeIdle`, server AI tick): while interaction-free and masterless, `hornyTimer` counts up (starts 3200). At ≥ 4800 she pursues the nearest player within 10 blocks (not in a scene, not the owner — `AbstractPlayerGirlEntity.isOwnerPlayer`): flies to them (`tryMoveToEntityLiving(player, 1.0)`); within **1.5 blocks** she resets the timer, anchors herself (IS_ANCHORED, yaw = player yaw −180, target = front offset), locks the player (`SetPlayerMovementPacket(false)`), teleports them 0.2 toward her (`getVectorTowardPlayer(0.2)`) and starts CITIZEN_START.
- **Tamed/owner flow**: player form / owner via `BeePlayerEntity.handleOwnerCommand` (any command) → `sendActionPacket(0, CITIZEN_START)` + strip (`setOutfitIndex(0)`) + `teleportPlayerToGirl` + player placed at `getVectorTowardPlayer(-0.2)` (0.2 *behind* her).
- Progression: slow ↔ fast (sneak via `getNextAction`; jump keeps fast via `sex_fastReady` controller-offset reset), cum via `getCumAction` → CITIZEN_CUM → `sex_cumDone` → reset. `setCurrentAction` blocks re-entering FAST/COWGIRLSLOW while CITIZEN_CUM plays (note the guard checks `COWGIRLSLOW`, not `CITIZEN_SLOW` — quirk).
- `U()` is an empty override (no payment gate — the bee is the only girl with no PAYMENT step), `doAction` is an empty override (no menu actions), `openInteractionMenu()` returns false (no GirlInventoryScreen; right-click uses the bee dialogue screen instead when tamed).
- No bed requirement anywhere.

## Dialogue (all lang lines)

Three lang keys, used by `BeeDialogueScreen` (sent as chat feedback when pressing follow/set-home buttons):

| Lang key | Text | When spoken |
|---|---|---|
| `bee.dialogue.sad` | The bee looks sad | pressing "Stop following me" (button 0 while following) |
| `bee.dialogue.exited` | The bee looks exited | pressing "Follow me" (button 0 while not following) |
| `bee.dialogue.home` | The bee likes its new home | pressing "Set new home here" (button 2) |

The NPC itself never chats; these are player-side feedback lines printed via `player.sendMessage(new TextComponentString(I18n.format(...)))`.

## Sounds

No `girls.bee.*` events and no `sounds/girls/bee/` folder. Scene SFX (sound listener):
- `MISC_POUNDING` (random) — `sex_startMSG1` / `sex_fastMSG1` / `sex_cumMSG1`.
- `MISC_CUMINFLATION` (random, volume 2.0) — `sex_cumMSG1`.
- `MISC_BEEW` / `MISC_FLAP` are **not** used by the bee (they belong to GalathEntity/GalathPlayerEntity/GalathCoinItem).

## Model & Appearance

- `BeeNpcModel`: `getModelLocations()` = `geo/bee/bee.geo.json` (index 0, 230 bones) and `geo/bee/armored.geo.json` (index 1, 263 bones). **Quirk: both geo files declare the identifier `geometry.Jenny`** (copy-paste artifact — geometry identifier does not match the character). Texture `textures/entity/bee/bee.png`; `BeeEntity` scale factor −0.1 (NPC; renderer passes −0.4), player 1.4.
- **Chest bone toggle**: `chest` bone visibility is driven by the *movement controller's current animation name* — visible only while the animation name contains "chest" (i.e. `idle_has_chest`), hidden otherwise (`BeeNpcModel.setLivingAnimations`).
- Head look (`handleAnimationEvent`, only for NULL/ATTACK/BOW actions): `neck` rotates at half `netHeadYaw`, `head` at full yaw with `headPitch` (rotationX = 1.0 + pitch·rad — the leading 1.0 rad is a constant offset quirk); `body` bone yaw zeroed (falls back to bone `dd` if `body` missing).
- Notable bones: `chest`, `wing`, `wing2`, `feeler`, `feeler2`, `band`, `brow`/`brow2`/`brow3`/`brow4` (Attachments array), `boobs`, `boobsFlesh`, `armorBoobs`, `armorChest`; standard armor/nude bone groups (Top = `boobsFlesh`, `upperBodyL/R`; Bottom = `sideL/R`, `fleshL/R`, `vagina`, `curvesL/R`, `kneeL/R`).
- Outfit states: OUTFIT_INDEX 0 = `bee.geo.json` (nude; forced at scene start), 1 = `armored.geo.json` (dressed; `reinitTasks` / `dress`).
- Hitbox 0.3 × 1.5 (`BeeEntity` constructor).

## AI & Behavior

- `initEntityAI`: `GirlGotoGoal` (priority 0 — follow/companion movement), `EntityAIPanic(1.25)` + `EntityAISwimming` (1), `WatchClosestGirlGoal(EntityPlayer, 3.0F, 1.0F)` (2), `EntityAIWanderAvoidWaterFlying(1.0)` (3). Navigator = `PathNavigateFlying` (no doors, can float, can enter doors). Move helper = `EntityFlyHelper`.
- Attributes: MAX_HEALTH 12, FLYING_SPEED 0.4, MOVEMENT_SPEED 0.2, FOLLOW_RANGE 16, plus KNOCKBACK_RESISTANCE / ARMOR / ARMOR_TOUGHNESS registered.
- **Flight ceiling** (`rayTraceFlower`): ray-traces straight down; if more than 3 blocks above ground and rising, `motionY = 0`. Fall dampening: while unbounded (`hornyTimer < 4800`), falling `motionY *= 0.4`; `fall()` is a no-op (no fall damage).
- **Horny potion handling** (`updateAITasks`): if potion active, unbounded and not in a scene → potion removed and `hornyTimer = 6.9420184E7` (~69.4M ticks — effectively "never horny again"; a float-format easter-egg value).
- `onUpdate`: fall-damp while `hornyTimer < 4800`.

## Unique mechanics

- **Taming / chest flag** (`HORNY_FLAG`, data key 112): after a citizen-cum scene, `doParticleStuff()` runs the post-cum particle cycle: `eggState` counts up from 1 (latched while `getCurrentAction() == CITIZEN_CUM`); HEART particles (<40) while already tamed; SPELL particles (<200); at exactly 200 a coin-flip `HORNY_FLAG = RNG.nextBoolean()`; then HEART (accepted) or VILLAGER_ANGRY (rejected) particles until 250, with SPELL particle spam throughout. So **~50% of cum sessions tame the bee**.
- **27-slot chest inventory**: `BeeEntityBase implements IInventory` delegating to an `ItemStackHandler(27)`; `getSizeInventory() = 27`, stack limit 64, all slots valid, `isEmpty()` always false, `markDirty()` no-op. Persisted as NBT tag `inventory` (serialized ItemStackHandler).
- **Chest access**: when tamed, right-click opens `BeeDialogueScreen` (client) — buttons "Follow me/Stop following me" (toggles `MASTER` data param via `ChangeDataParameterPacket`), "Go home" (`SendCompanionHomePacket`), "Set new home here" (`SetNewHomePacket` at her position), and a bee icon (only when HORNY_FLAG) that sends `BeeOpenChestPacket` → server opens **GUI id 1** (`GirlInventoryContainer` server / `ChestContainerGui` client, wrapping the bee `IInventory`). The horny flag gates access — `BeeOpenChestPacket.Handler` silently drops requests when unset.
- **Dead taming branch**: `BeeEntity.processInteract` starts with `if (HORNY_FLAG && !HORNY_FLAG && heldItem == chest) { set HORNY_FLAG true; shrink chest; }` — the condition is **always false** (jar-faithful dead code; giving a chest never tames — taming is the post-cum coin flip). Do not "fix" without testing the taming flow (documented in the class).
- **BeeWorldData**: `WorldSavedData` named `sexmod:galath_spawn_list` (copy-paste from Galath) storing static `hivePositions`/`flowerPositions` BlockPos lists; written with `sexmod:pos_amount`/`sexmod:x|y|z` keys, second list under the `mang` key suffix. No bee code reads these lists back (registration-only holder).

## Player form (if any)

`BeePlayerEntity` — same citizen scene, no chest/taming:
- **Owner flight**: `B_clash233()` / `onTickClient()` call `handleOwnerUUID(true/false)` — while the transformation is active the owner gets the bee's flight capability.
- Scale 1.4, eye height 1.3, hand model `BeeModel` (placeholder), hand texture `textures/entity/bee/hand.png`.
- Menu: `["action.names.sex"]` only; `canBeInteracted() = false`; `reinitTasks` sets OUTFIT_INDEX 1 (dressed).
- `animationPredicate` handles NULL/ATTACK/BOW/RIDE/THROW_PEARL + citizen actions; registers only action + movement controllers (no eyes).
- Same sound listener as the NPC minus the `pearl` gating difference (identical), no `eggState`/particle cycle.

## Data keys / NBT

- **`HORNY_FLAG` is declared twice**: `BeeEntityBase.HORNY_FLAG` (Boolean, key **111**, registered in `entityInit`) and `BeeEntity.HORNY_FLAG` (Boolean, key **112**, also registered) — the subclass key wins at runtime for BeeEntity; base key 111 is effectively shadowed. Never "deduplicate" without auditing every register/set call site (class doc).
- Inherited BaseGirlEntity keys 99–110 (see Luna doc): incl. `MASTER` (110, follow toggle), `IS_ANCHORED` (109), `OUTFIT_INDEX` (105), `CUR_ACTION` (104).
- NBT (`BeeEntity`): `isTamed` = HORNY_FLAG, `hasChest` = HORNY_FLAG (both keys map the same flag; `readFromNBT` applies `isTamed` if present, then unconditionally overwrites with `hasChest`), plus `inventory` = serialized ItemStackHandler.

## Pitfalls & quirks (deobfuscated code notes)

- Both bee geos declare `geometry.Jenny` as their identifier — mismatch with `BeeNpcModel`/`geometry.bee` naming expectations.
- `animation.bee.ride` referenced by the player form but absent from the JSON (dangling reference; slime has the same bug).
- The `sex_cumDone` reset path for the NPC additionally runs `doParticleStuff` (tame roll) via the `eggState` latch — the cum animation itself only emits `sex_cumMSG1`/`blackscreen`/`sex_cumDone`.
- `setCurrentAction` cum-guard compares against `Action.COWGIRLSLOW` (a different enum constant) instead of `CITIZEN_SLOW` — the guard only effectively blocks CITIZEN_FAST re-entry.
- The "horny potion resets the timer" value `6.9420184E7` is a hardcoded float — never triggers the wild approach again on that bee.
- `hornyTimer` starts at 3200 (not 0): freshly spawned wild bees need 1600 ticks (~80s) to reach the 4800 approach threshold, then pursue and engage when within 1.5 blocks.
- `BeeWorldData` is a copy-paste of Galath's spawn-list storage (`sexmod:galath_spawn_list`) — its hive/flower lists are written but never consumed by bee code.
- `EggState` naming: the post-cum particle counter is called `eggState` though no egg exists in the bee code path.
- `readFromNBT`'s unconditional `hasChest` write means a bee saved with only `isTamed` set will still load with the flag from `hasChest` (both must agree in the save).
- The eyes controller is registered for the NPC but never driven (no `"eyes"` case in `animationPredicate`) — eyes stay in the last movement/action pose.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
