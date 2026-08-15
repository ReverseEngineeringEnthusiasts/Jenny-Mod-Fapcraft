# Manglelie — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.manglelie.name = Manglelie` (lang/en_us.lang) |
| Voice actress | None — **no voice lines at all** (no `girls.manglelie.*` entries in sounds.json, no `sounds/girls/manglelie/` folder, no dialogue lang keys; only `entity.manglelie.name`) |
| Entity class(es) | `ManglelieEntity` (NPC only, entity id **618151**, `registerSpawnEntity("manglelie", ..., 16382457, 8485574)`). No player-form class. |
| Model / Renderer | `ManglelieNpcModel` (geo: `manglelie.geo.json` ×2 + `geo/galath/galath_con_mang.geo.json`; anim `manglelie.animation.json`); `ManglelieRenderer`. |
| NpcType enum | `MANGLELIE(ManglelieEntity.class, 618151)` — **isNpcOnly = true**, `playerClass = null`, `playerID = 0` |
| Player form | None (NPC-only girl) |
| Obtaining | Not directly obtainable. Wild Mangles spawn naturally near hives (`getCanSpawnHere`: must NOT be within 700 blocks of a `BeeWorldData.hivePositions`/`flowerPositions` position, then registers itself in `flowerPositions`) or via spawn egg. Bound state: adopted by a wild Galath (hug → `RIDE_MOMMY_HEAD`), or **spawned on demand** by a tamed Galath via `canStartPussyLicking()` (when the owner is already manglelie-owned: at coin summon, at morning-blowjob wake, or via the failsafe in `handleManglelieOwned`). |

## Animations (complete list)

File `animations/manglelie/manglelie.animation.json` — **20 animations** (verified). All cited from `ManglelieEntity.animationPredicate` / `handleActionAnimationOverrides` / sound listener.

| Animation name | Purpose / when used |
|---|---|
| `animation.manglelie.idle` | movement controller, action NULL + not corrupting, standing still |
| `animation.manglelie.walk` | movement controller, action NULL + not corrupting, moving, `ar` (scared) false |
| `animation.manglelie.scared_run` | movement controller, action NULL + not corrupting, moving, `ar` true (set by `AvoidPlayerGoal`) |
| `animation.manglelie.running` | action controller, `RUN` (running to mommy) |
| `animation.manglelie.sit_on_galath` | action controller, `RIDE_MOMMY_HEAD` (riding Galath's head) |
| `animation.manglelie.sit_on_galath2` | In JSON, **no code reference** (legacy variant) |
| `animation.manglelie.angry_face` | eyes controller, only while a corrupt target entity exists (`getCorruptEntity() != null`) |
| `animation.manglelie.null` | In JSON, **no code reference** (legacy; unlike GalathPlayerEntity, Mang never plays a `null` anim) |
| `animation.manglelie.bed_slow` | In JSON, **no code reference** (legacy; the morning blowjob uses `shared.bed_slow` from Galath) |
| `animation.shared.bed_slow` | Present in Mang's file too (used by Galath's morning blowjob rig) |
| `animation.shared.double_holding_slow` | `THREESOME_SLOW`, `playRandomizedAnimation(...,4,0.33F)` |
| `animation.shared.double_holding_slow1/2/3` | Random variants of the above (suffix picked by `pickRandomVariant`) |
| `animation.shared.double_holding_soft` | THREESOME_SLOW→FAST transition (override `threesomeSlowStarted`) |
| `animation.shared.double_holding_hard` | `THREESOME_FAST`, `playRandomizedAnimation(...,3,0.33F)` |
| `animation.shared.double_holding_hard1/2` | Random variants of the above |
| `animation.shared.double_holding_back` | THREESOME_FAST→SLOW restart (override) and `THREESOME_SLOW` after cum (`threesomeCumDone`) |
| `animation.shared.double_holding_cum` | `THREESOME_CUM` (override transition and action state) |

## Scenes & Actions

Actions used: `RIDE_MOMMY_HEAD` (adoption), `THREESOME_SLOW/FAST/CUM`, `RUN`, `NULL`, plus shared scene actions driven by her Galath.

- **Adoption (RIDE_MOMMY_HEAD)**: wild Mang (`handleCorruptInit`) seeks an unbound, on-ground Galath within 15 blocks and RUNs to her (0.65 speed, yaw to target). On contact (`handlePartnerLook` — only for the `galathPartnerUUID` loaded from NBT) or when the Galath's `at()` reaches her (≤3.65 blocks): Galath sets HUG_MANG, both partner UUIDs are bound (`ad`/`WildSlimeFaceLayer`), Mang → `RIDE_MOMMY_HEAD`, `GirlSavedData.markAsManglelieOwned`. While corrupting with no threesome action, `handleThreesomeState` keeps her anchored at Galath's position (yaw 0, `setTargetPosition(galath.pos)`); a dead mommy → `"A dead mommy has been saved onto a mang. Deleting her and creating a new one"` (LOGGER.warn) + remove.
- **Corruption beam → threesome conversion** (tamed): while `ap` (corrupting) and Galath is in the corrupt state, `handleCorruptStart` picks a valid `EntityMob` within 15 blocks of Galath (`isGalathBlocked`: alive, same dimension, daylight-visible, ≤15 blocks horizontal, in front of Galath's aim via `getAimYaw`). `setCorruptEntity` records the id + start world time. `handleCorruptTimer`: at 28 ticks after start, fires an `EntityTippedArrow` from Galath's position +3.5 Y at the target (velocity 4.0, `ENTITY_ARROW_SHOOT` from Galath) and sets `corrupting=true`. `handleCorruptTick`: at 60 ticks total, releases (`corrupting=false`, `setCorruptEntity(-1)`). While bound she is `noGravity` + `noClip` + `canBeCollidedWith()==false` (`updateCorruptGravity`). The player then right-clicks Galath → `threesome` menu option → Mang `THREESOME_SLOW` + Galath `PUSSY_LICKING` (see Galath doc).
- **Threesome flow** (`handleActionAnimationOverrides`): THREESOME_SLOW start → set THREESOME_FAST + `double_holding_soft` + Galath `ak()` (MASTERBATE_SITTING); THREESOME_FAST first start → `double_holding_hard`; THREESOME_FAST end (not started) → `threesomeCumDone=true`, back to THREESOME_SLOW + `double_holding_back` + Galath `startPussyLicking()`; THREESOME_SLOW after cum → `double_holding_slow`; `threesomeSlowStarted && THREESOME_FAST` → THREESOME_CUM + `double_holding_cum` + Galath `MASTERBATE_SITTING_CUM`; THREESOME_CUM end → reset both girls (`resetCameraAndPhysics`), `CummyEntity.spawnCummyTrails` on both, clear state (`an=2`).
- **Movement**: walk/idle/scared_run via movement controller only when action NULL and not corrupting; `RUN` otherwise. `AvoidPlayerGoal` (task 1, range 20.0, speeds 1.0/1.2) flees players — but never while bound to a corrupting Galath or when any master-owned Galath is within `distance` blocks (`shouldAvoid`); sets `ar` (scared) while executing → `scared_run` anim.
- **Despawn rules**: `aa` (despawned flag) → removed; non-wild Mang whose mommy is gone (`handleCorruptFinish`) → removed with console print `"removed non-wild mang for lack of mommy"`; mommy disowned her for another Mang (`handleGalathPartner`) → removed with `"removed non-wild mang cuz her mommy disowned her and got another mang"`.
- **Render-scene coupling**: while corrupting (not threesome), her render position/aim is overridden by `renderCustomModelTransform`/`getBoneWorldPosManglelie` → `renderGalathInteract` + `getLookVector(galath)` (Galath's aim vector + `mangPos` bone offset) — she renders at Galath's location, not her own. `getYawRotation()` adds 180° during threesome actions. Name tag hidden while corrupting (`shouldRenderNameTag`).

## Dialogue (all lang lines)

**None.** No `manglelie.*` lang keys, no chat messages, no sound events. The only console output is debug prints:
- `"removed non-wild mang for lack of mommy"` (System.out, `handleCorruptFinish`)
- `"removed non-wild mang cuz her mommy disowned her and got another mang"` (System.out, `handleGalathPartner`)
- `"A dead mommy has been saved onto a mang. Deleting her and creating a new one"` (LOGGER.warn, `handleThreesomeState`)

## Sounds

**None of her own.** No `girls.manglelie.*` entries in `sounds.json`, no `sounds/girls/manglelie/` directory on disk. She only triggers:
- `SoundEvents.ENTITY_ARROW_SHOOT` (played from Galath's position on the corrupt arrow)
- `MISC_POUNDING` (threesome `pound` cue, +0.02 horny meter)
- `MISC_INSERTS` at volume 6.0 (`doubleSemen0` cue, together with pounding)
- `MISC_SMALLINSERTS`/`MISC_CLAP` etc. via her Galath partner's listener.

## Model & Appearance

- 3 geo slots: `geo/manglelie/manglelie.geo.json` ×2 + `geo/galath/galath_con_mang.geo.json`. **The geo identifier is `geometry.galath`** (shared base rig) — the Mang model is a re-skinned Galath rig. Texture `textures/entity/manglelie/manglelie.png`.
- Notable bones (verified from geo): `steve`/`Torso2`/`Head2`/`boyCam`/`blocks` (player-skin slot), `cock`/`bone123`/`bone99`/`ballL`/`ballR`, `cockStage0/1/2` (corruption stage bones, visibility `setHidden(i > an)`), full Galath-style `body`/`rotationTool`/`upperBody`/`hip`, skirt segment bones `skirt_<i>_<j>` (0..39 strips × 3 verts), `Rskirt`/`Lskirt`/`Fskirt`/`Bskirt` groups, `cheekR`/`cheekL` + `cheekRBelowSkirt`/`cheekLBelowSkirt`, `sideR`/`sideRSkirt`/`sideRNoSkirt`/`sideL`/`sideLSkirt`/`sideLNoSkirt`, `boobs2`/`booty2`/`vagina2`/`fuckhole2` (Mang-specific custom parts, blacklisted from the normal pass), `semenEmitter`/`semenDir` (cum trails), `armR`/`armL`/`lowerArmR`/`lowerArmL`/`elbowR`/`elbowL` (corruption arm animation), `weapon`/`offhand` (corruption bow).
- Renderer blacklist (`ManglelieRenderer.BLACKLISTED_BONES`): `boobs2, booty2, vagina2, fuckhole2` + `BodyParts.CUSTOM_PART_BONES`. Galath's renderer additionally blacklists these (shared static set).
- Custom render passes: 40-strip corruption skirt mesh (`renderManglelieMesh`, dark/light purple strips `CORRUPTION_COLOR_DARK/LIGHT` from cached `skirt_<i>_<j>` offsets — drawn only in the "look" pose), corruption body ribbon (`renderManglelieRibbonMesh` from `clothBoobLconStart/End`, `clothBoobRconStart/End`, `clothBoobMidconStart/End` — `hasParentBone` special-cases `clothBoob*` to always render), held **bow** on `weapon`/`offhand` (`renderEquippedItem`; charge pose via `setItemUseCount(11*(1-eased)+71980)` while `getCorruptProgress()<1.0`, else clears hand state; bow rendering chosen by `isLookingAtGalathEntity`), first-person POV wing mesh (`renderMangleliePov`, counter-rotated ribbon `renderManglelieRibbon`), threesome two-bone pass (`body2` normal + `steve` with render-scale).
- Skirt-follow logic (`applyBoneTransform`): strips 17..35 follow `cheekL/cheekR` rotation (positionY += rot×0.01), strips 1..11 suffix "1" copy `legR/legL` rotationX (rotationX + positionY 0.03×rot); `isSecondary` uses `cheek2`/`leg2` variants (Galath's con_mang rig). No-op when driver rotation is negative or game paused.
- Look-pose cloth swap (`setCheekHidden`/`setSkirtHidden`): skirt hidden while `isGalathLooking` (the ribbon mesh replaces it); below-skirt cheeks (`cheek*BelowSkirt`, `side*NoSkirt`) swapped against `side*Skirt` variants.
- `getScaleFactor()` returns **0.0F** (unused/oddity — render uses the partner-relative path instead).

## AI & Behavior

- `initEntityAI`: base girl tasks + `AvoidPlayerGoal` at priority 1 (20.0 range, 1.0 far / 1.2 near speed). No combat, no tempt, no doors.
- `updateAITasks` dispatch order: despawn check → `loadModelCode` → `handleThreesomeState` → super → `handlePartnerLook` → `handleCorruptInit` → `handleCorruptEntity` → `handleCorruptTick` → `handleCorruptStart` → `updateCorruptGravity` → `handleCorruptTimer` → `handleGalathPartner` → `handleCorruptFinish`.
- **Damage forwarding**: `attackEntityFrom` (non-void) forwards 100% to her Galath partner (`galath.attackEntityFrom`), returns false; void damage kills normally.
- **Arrow immunity for girls**: `ManglelieEntity.b.handleArrowHit` cancels any projectile impact on a `BaseGirlEntity` when the arrow's shooter is a Manglelie (her corrupt arrow must not hurt girls).
- `addPotionEffect` no-op; `canBeCollidedWith` false while bound (corrupting).
- `reinitTasks`: restores RIDE_MOMMY_HEAD + yaw 0 + dirty `YAW_ROTATION` while corrupting.
- Look helpers: `isLookingAtGalathEntity`/`isLookingAtGalathPoint`/`isThrowBlocked` (rotated delta x > 0.35 vs Galath's lerped head yaw) drive the bow hand and pose.
- `getCorruptTarget()` (client, particle tick): corrupt entity, else nearest player within 6 blocks — drives the head/arm aim (`af`/`rotationLerp` computed in `handleParticleTick`, every 7 player ticks).

## Unique mechanics

- **Adoption RIDE_MOMMY_HEAD**: mommy/daughter bond; Mang renders *on* Galath's head (`mangPos` bone) via the interaction render path; Galath HUG_MANG precedes it.
- **Corruption beam**: no beam entity — a `DragonEntity`-style arrow is shot from Galath's head (28-tick charge-up, `getCorruptProgress = elapsed/28` drives bow draw + arm swing), the mob is "held" by the corruption state (noClip/noGravity/uncollidable) while the look-pose arms track it, released at 60 ticks. The corruption arms blend between corrupt-pose and ride-pose on a frame-rate-independent cycle (`VELOCITY_0`/`aj` fields), head chases Galath at 7°/frame (60 fps) (`TICK_0`/`ai` fields).
- **Threesome conversion**: the corrupted mob's scene is a *threesome* (Galath + Mang), with Mang's body mirroring Galath's published body rotation/scale (`bw`/`bm`), pose variants via `cs0/cs1/cs2` sound cues (`an` = cock-stage counter 0..2), semen trails from `semenEmitter`/`semenDir`, and her own `THREESOME_CUM` handling resetting both girls.
- **Avoidance / scared run**: `AvoidPlayerGoal` sets data key `ar` → `scared_run` animation; suppressed near any master-owned Galath ("mommy protects her").
- **Name-tag & render gating**: `shouldRenderNameTag` false while corrupting; `doRenderManglelie` skips the standard render while looking at Galath / ride-ready / in the look pose / riding mommy (CONTROLLED_FLIGHT/BOOST) — those poses render through POV/interaction paths; shadow/fire pass also skipped while looking/corrupting.

## Player form (if any)

**None.** `NpcType.MANGLELIE` is `isNpcOnly=true`; there is no `MangleliePlayerEntity` and no `player_manglelie` registration.

## Data keys / NBT

Data-manager keys (explicit serializer ids — do not reorder; 99–110 from `BaseGirlEntity`, 111–115 own):

| Id | Field | Type | Meaning |
|---|---|---|---|
| 99–110 | (BaseGirlEntity keys) | — | shared girl keys (see Galath doc; incl. `MASTER`=110, `CUR_ACTION`=104, `GIRL_ID`=106, `YAW_ROTATION`=107, `TARGET_POS`=108, `IS_ANCHORED`=109) |
| 111 | `ad` | STRING | Galath partner ("mommy") UUID (`getCorruptPlayerUUID`) — empty = unbound |
| 112 | `ap` | BOOLEAN | corrupting flag (`isCorrupting`/`setCorrupting`) |
| 113 | `ab` | VARINT | corrupt target entity id (−1 none) |
| 114 | `al` | STRING | corrupt start world time (long as string; −1 none) |
| 115 | `ar` | BOOLEAN | scared flag (set by `AvoidPlayerGoal`) |

NBT (entity): `sexmod:mommy` (STRING, Galath UUID), `sexmod:iswild` (BOOL, `aq`), `sexmod:despawned` (BOOL). Note the transient `galathPartnerUUID` field: `readFromNBT` stores `sexmod:mommy` there and `handlePartnerLook()` performs the actual two-way binding + RIDE_MOMMY_HEAD on the next AI tick.

## Pitfalls & quirks (deobfuscated code notes)

- **Silent girl**: zero sound events, zero dialogue keys — all "speech" is the Galath partner's or debug stdout.
- **Geo identifier is `geometry.galath`** — Mang shares the Galath base rig; the "outfit" slots are `manglelie.geo.json` ×2 then the combined `galath_con_mang.geo.json`.
- **`getScaleFactor()` returns 0.0F** — likely a dead branch; her render position/aim is driven by the partner-relative interaction path instead.
- **Render is skipped in most states**: looking at mommy, ride-ready, look-pose, riding mommy, corrupting — the standard pipeline only draws her walking/idle/running alone.
- **Animations `sit_on_galath2`, `bed_slow`, `null` and `shared.double_holding_hard1` exist in the JSON but are unreferenced in code** (legacy/leftover).
- **`isGalathBlocked` null-handles `getAimYaw`** (which returns null outside FLY/SUMMON_SKELETON/RAPE_PREPARE) — falls back to `galath.rotationYawHead`.
- **Damage forwarding** means killing a Mang is impossible while her mommy lives (she never takes the hit).
- **`getCorruptProgress` is 28-tick based** even though the release is at 60 ticks — the arm swing (`swingProgress=(progress*28-28)/32`) and bow draw use the charge-up phase only.
- **`handlePartnerLook` only fires for NBT-loaded partners** — live adoption is done by the Galath side (`at()` → HUG_MANG → binds + RIDE_MOMMY_HEAD directly).
- **The `an` cock-stage counter is a *render* field** written by the sound cues `cs0/1/2` from the threesome animations; `animatePose` hides `cockStage<i>` for `i > an`.
- **`setCurrentAction` guards** the THREESOME_CUM loop (no re-entry from FAST/SLOW) and persists cum time to `GirlSavedData.saveCumTime` (server) on THREESOME_CUM.
- **`getYawRotation()` +180° flip** during threesome actions keeps her facing the player in the shared pose — read by render/model code, not a real rotation change.
- **AvoidPlayerGoal semantics are inverted from the name**: `shouldAvoid()` returns true when she should NOT flee (bound or protected by a master-Galath nearby); the vanilla `EntityAIAvoidEntity` is then suppressed.
- **The corrupt arrow's shooter is Mang** but the launch origin is Galath +3.5 Y — the `handleArrowHit` girl-immunity handler is what keeps it from harming the mommy herself.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
