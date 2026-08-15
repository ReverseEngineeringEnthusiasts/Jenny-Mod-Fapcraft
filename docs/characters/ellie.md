# Ellie — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.ellie.name = Ellie` (lang key `entity.ellie.name`) |
| Voice actress | @EndymionVA (mcmod.info credits) |
| Entity class(es) | NPC: `EllieEntity` (extends `AbstractGirlNpcEntity`, implements `IEllie`). Player form: `ElliePlayerEntity` (extends `AbstractPlayerGirlEntity`) |
| Model / Renderer | NPC: `EllieNpcModel` (geckolib `GirlModel<BaseGirlEntity>` with custom sitting head-tracking) + `EllieRenderer` (extends `GirlRenderer`); vanilla placeholder `EllieModel` (`IVanillaModel`, rotated two-cube + empty part). Player form: `PlayerEllieRenderer` (extends `GirlPlayerRenderer`), hand model = `EllieModel` with `textures/entity/ellie/hand_nude.png` (nude) / `hand.png` |
| NpcType enum | `ELLIE(EllieEntity.class, 228922, ElliePlayerEntity.class, 46348348)` — NPC id `228922`, player-form id `46348348` |
| Player form | `ElliePlayerEntity` — horny-potion transformation; scenes carry ("Face fuck"), cowgirl, missionary + sit-down flow |
| Obtaining | Spawn egg only (no natural spawn). `SexModEntities.registerSpawnEntity("ellie", EllieEntity.class, 228922, 1447446, 9961472)` — egg colors 1447446/9961472, 50-block tracking. Also via `Girl wand` |

## Animations (complete list)

Source: `assets/sexmod/animations/ellie/ellie.animation.json` (42 animations — count matches the context list).

| Animation name | Loop | Purpose / when used (code cite) |
|---|---|---|
| `eyes` | LOOP (4.16s) | Eyes controller when `action == NULL && autoBlink` (`EllieEntity` + `ElliePlayerEntity` case `"eyes"`) |
| `null` | LOOP, no bones | Eyes controller when action ≠ NULL; movement/action `NULL` reset pose |
| `idle` | LOOP (6.4s) | Movement controller: action NULL, standing still, **not** bed-blocked |
| `crouchidle` | LOOP (6.7s) | Movement controller: action NULL, standing, `isBedBlocked()` (block 2 above her) — NPC; player form uses it when `hasNoOwner()` (block above owner's head) |
| `walk` | LOOP (1.5s) | Movement controller, `WALK` |
| `fastwalk` | LOOP (1.0s) | Movement controller, `FAST_WALK` |
| `crouchwalk` | LOOP (1.0s) | Movement controller, `isBedBlocked()` while moving (NPC); player form: `hasNoOwner()` while moving (run/fastwalk/backwards all map to it) |
| `run` | LOOP (0.84s) | Movement controller, `RUN` (pony-tail bones animated) |
| `backwards_walk` | LOOP (0.68s) | Player form: moving backwards (`ao.y < -0.1`) |
| `fly` / `fly2` | LOOP (0.84s) | Player form: `!af` flying branch, `fly2` toggled while the movement animation name contains "fly" (`ar` flag) |
| `sit` | LOOP (4.12s) | Action `SIT`; player form: riding (`ak`) |
| `ride` | LOOP (4.12s) | Action `RIDE` |
| `sitdown` | LOOP (5.52s) | Action `SITDOWN` — sitting down on the bed (non-loop in practice; `spike21` bone animated). Keyframe `sitdownMSG1` ("come to mommy" line) and `sitdownDone` → `SITDOWNIDLE` (NPC also triggers the scene menu via `handleSitIdle`) |
| `sitdownidle` | LOOP (7.04s) | Action `SITDOWNIDLE` — sitting idle on the bed; head-tracks the nearest player (EllieNpcModel.setLivingAnimations) |
| `dash` | LOOP (1.48s) | Action `DASH` — horny-potion approach dash (16 ticks). Keyframes `dashMSG1` (face nearest player), `dashReady`, `dashDone` → `HUG` (player form only; NPC advances via `handleSitUpFinish` timer instead) |
| `hug` | one-shot (7.6s) | Action `HUG` — hug dialogue intro (150 ticks). Keyframes `hugMSG1` (player teleport yaw-80, player form), `hugMSG2` ("Hmm..."), `hugMSG3` ("Hey!"), `hugMSG4` (mommyhorny), `hugMSG5` (whattodo), `hugDone` → reopens the scene menu (`canJoinPlayer(player, true)`) |
| `hugidle` | LOOP (3.64s) | Action `HUGIDLE` — hugging idle (after `hugDone`, yFlag timer 150 in NPC) |
| `hugselected` | one-shot (4.04s) | Action `HUGSELECTED` — after picking cowgirl/missionary in the hug menu. Keyframes `hugselectedMSG1` ("Mhm.. I know~"), `hugselectedMSG2` ("follow me, darling~" + movement lock), `hugselectedDone` (player form: repositions via targetPos packet, sends `SendGirlToSexPacket`, resets) |
| `strip` | one-shot (5.04s) | Action `STRIP`; `becomeNude` toggles outfit, `stripDone` → NULL + `resetCameraAndPhysics()` + `U()` |
| `cowgirlstart` | LOOP (15.76s) | Action `COWGIRLSTART` (non-loop). Keyframes `cowgirlStartMSG0` (giggle), `cowgirlStartMSG1` ("like" line + meter reset), `cowgirlStartMSG2` (ahh+pounding, +0.02), `cowgirlStartDone` → `COWGIRLSLOW` |
| `cowgirlslow2` | LOOP (3.2s) | Action `COWGIRLSLOW` (name note: "cowgirlslow2", no `cowgirlslow1` exists) |
| `cowgirlfast` | LOOP (3.4s) | Action `COWGIRLFAST`. Keyframes `cowgirlfastMSG1` (+0.04, `aj` one-shot suppress), `cowgirlfastReady` (jump keeps fast / else → SLOW, player form: 1-in-4 cache clear), `cowgirlfastdomMSG1` (+0.2), `cowgirlfastDone` → `COWGIRLSLOW` (unless jumping) |
| `cowgirlcum` | LOOP (15.36s) | Action `COWGIRLCUM`. Keyframes `cowgirlcumMSG1..6` (ahh/pounding, MOAN[5] @3.0, goodboy line at MSG5, black screen at MSG6 → `BeeScreen.enableInteraction()`), `cowgirlcumDone` → reset |
| `missionary_start` | LOOP (3.25s) | Action `MISSIONARY_START`. Keyframe `missionary_startDone` → `MISSIONARY_SLOW` + meter |
| `missionary_slow` | LOOP (2.92s) | Action `MISSIONARY_SLOW`. Keyframe `missionary_slowMSG1` (+0.02, moan/ahh pick) |
| `missionary_fast` | LOOP (1.875s) | Action `MISSIONARY_FAST`. Keyframes `missionary_fastMSG1` (+0.05), `missionary_fastDone` → SLOW (NPC: unless jumping; player form: jump → stays FAST) |
| `missionary_cum` | one-shot (14.44s) | Action `MISSIONARY_CUM`. Keyframes `missionary_cumMSG1` (ahh), `missionary_cumMSG2` (goodboy line), `missionary_cumDone` → reset. Shared `bedRustle`/`bedRustle1` keyframes (pounding + bedrustle) |
| `carry_intro` | LOOP (9.67s) | Action `CARRY_INTRO` — "Face fuck" intro, 110-tick timer (`ak`). Keyframes `carry_introMSG1` ("I'm hungry.."), `carry_introMSG2` ("heh~"). Uses `boyCam`+`girlCam` camera bones |
| `carry_slow1` / `carry_slow2` / `carry_slow3` / `carry_slow4` | LOOP (3.17s each; slow4 = 1.58s) | Action `CARRY_SLOW` — variant suffix `aa` (NPC) / `ap` (player) re-rolled on `carry_slowDone` (1..4, never the previous value). `lipsound` (+0.02), `pound` (+0.04), `cum` keyframes |
| `carry_fast` | LOOP (0.75s) | Action `CARRY_FAST`. `carry_fastDone` → `CARRY_SLOW` unless jumping |
| `carry_cum` | LOOP (3.17s) | Action `CARRY_CUM`. `carry_cumDone` → reset |
| `attack0` / `attack1` / `attack2` | one-shot (0.36/0.52/0.52s) | Action `ATTACK`, `nextAttack` cycle; `attackSound` (ENTITY_PLAYER_ATTACK_STRONG), `attackDone` → NULL |
| `bowcharge` | LOOP (127.32s) | Action `BOW` (longest bow charge in the mod) |
| `throwpearl` | LOOP (4.58s) | Action `THROW_PEARL`; `pearl` keyframe → `SendCompanionHomePacket` |
| `downed` | LOOP (5.125s) | Action `DOWNED` |
| `new` | LOOP, no length | **Orphan**: not referenced by any code (bones body/upperBody/bone/head/girlCam) |

Camera metadata (Action enum): `SITDOWN` (max 60, min -90, flip), `SITDOWNIDLE` (60, -60, flip), `COWGIRL*` (60, -60, no flip), `MISSIONARY*` (30, -90, **flip true**), `CARRY_*` (`useBoyCam=true`, `hideNameTag=true`), `HUG*`/`DASH` default cam.

## Scenes & Actions

NPC interaction gate (`processInteract`): refuses interaction while a scene is active (`getActiveSceneInfo(player)` or bound interaction player). `canJoinPlayer(player, dressUp)`:
- `dressUp` (after hug): menu `[action.names.cowgirl, action.names.missionary]`
- nude (`OUTFIT_INDEX == 0`): menu `[action.names.dressup]`
- dressed: menu `["Face fuck"]`

1. **Sit-down flow (background)**: `updateAITasks` runs a sit-state machine — `setFirstSit` (one-shot physics cleanup `ac`), `handleHornyPotion` (dash route), `handleSitUpTimer` (`ai` 79-tick sit-up delay or `ah` one-shot → un-anchor, walk to random bed pose via `getRandomSitPose()` → `SITDOWN` with `al`=109 → `SITDOWNIDLE`), `handleSitDownTimer` (`al` → `SITDOWNIDLE`), `handleHugTimer` (`yFlag` 150 → `HUGIDLE`), `handleSitTransition` (in `SITDOWNIDLE`, player within 1.5 blocks → `af`=20 + bind player), `handleStandTimer` (`af` countdown → `U()`), `handleSitTimer` (`ak` 110 → reposition player for carry).
2. **Carry / "Face fuck"** — menu → `triggerActionSync(true, true, uuid)` → `setDismounted()`: faces player (yaw-180), `CARRY_INTRO` anchored. `ak` timer (110 ticks) repositions the interacting player at target + (0, 2.5625 - eyeHeight, -0.3125) rotated by yaw+180 → `CARRY_SLOW` (variant 1-4) → sneak → `CARRY_FAST` → jump → `CARRY_CUM` → `carry_cumDone` → reset. Name tag hidden (`shouldRenderNameTag` false during CARRY_INTRO).
3. **Cowgirl (bed)** — hug menu → `doAction` "action.names.cowgirl": `HUGSELECTED` + `animationFollowUp "cowgirl"` → hug dialogue → server `U()` reads `GIRL_HAND_STATES == "cowgirl"`: strip (`OUTFIT_INDEX=0`), `COWGIRLSTART`, player locked (noGravity+noClip, positioned at target + (0, 1-eyeHeight, -1.8125) rotated yaw+180, movement locked) → `cowgirlStartDone` → `COWGIRLSLOW` → sneak → `COWGIRLFAST` (jump holds fast) → jump-with-full-meter → `COWGIRLCUM` → `cowgirlcumMSG6` black screen → `cowgirlcumDone` → reset.
4. **Missionary (bed)** — same flow with `"Missionary"`: `MISSIONARY_START`, player at target + (0,0,0.1) rotated by her yaw, `player.rotationYaw = girl yaw` → `missionary_startDone` → `MISSIONARY_SLOW` → sneak → `MISSIONARY_FAST` → jump → `MISSIONARY_CUM` → reset.
5. **Horny-potion route** — `handleHornyPotion()`: removes the potion, binds nearest player (10 blocks), yaw atan2 to player, `DASH` + `zFlag`=16, removes wander/watch goals → `handleSitUpFinish()` (`zFlag` 16-tick countdown → player locked, girl anchored 0.5 behind the player → `HUG` + `yFlag`=150) → `handleHugTimer` → `HUGIDLE` → `handleSitIdle()`: player within 1.5 blocks → `BeeScreen.enableInteraction()`; `handleSitTransition` → `U()`.
6. **Strip / dress up** — `doAction` `strip`/`dressup` → `STRIP` + `animationFollowUp ""`; `becomeNude` toggles outfit; `stripDone` → NULL + `resetCameraAndPhysics()` + `U()`.
7. **Home** — `goHome()` sends "stay safe darling~" + `GIRLS_ELLIE_SIGH[1]` @6.0. `onArriveHome` → "Okay, I will be residing here then.." + `GIRLS_ELLIE_HUH[0]` @6.0.
8. **Combat/AI actions** — `ATTACK`, `BOW`, `RIDE`, `SIT`, `THROW_PEARL`, `DOWNED` (same as Jenny).

## Dialogue (all lang lines)

| Lang key | Text | When spoken (code cite) |
|---|---|---|
| `ellie.dialogue.busy` | I'm busy right now honey~ | **Lang-only** — not referenced in current Java (Ellie's `processInteract` returns silently instead) |
| `ellie.dialogue.followme` | I'll protect you darling~ | **Lang-only** — unused in code |
| `ellie.dialogue.stopfollowme` | stay safe darling~ | **Lang-only** — unused in code (hardcoded "stay safe darling~" used in `goHome()` instead) |
| `ellie.dialogue.gohome` | Okay, mommy is going home... stay safe darling~ | **Lang-only** — unused in code |
| `ellie.dialogue.mommyhorny` | Mommy is horny | `hugMSG4` keyframe + `GIRLS_ELLIE_MOMMYHORNY` @0.5 (NPC; player form uses GIGGLE[0] @3.0 instead) |
| `ellie.dialogue.whattodo` | so... what am I gonna do with you now, darling~ ? | `hugMSG5` keyframe + `GIRLS_ELLIE_HUH[1]` @6.0 |
| `ellie.dialogue.iknow` | Mhm.. I know~ | `hugselectedMSG1` + `GIRLS_ELLIE_HMPH[3]` @6.0 (player form: `GIRLS_ELLIE_MMM[0]` @3.0) |
| `ellie.dialogue.followmedarling` | follow me, darling~ | `hugselectedMSG2` + `GIRLS_ELLIE_GIGGLE[3]` @6.0 + movement lock |
| `ellie.dialogue.cometomommy` | Come to mommy~ | `sitdownMSG1` + `GIRLS_ELLIE_COMETOMOMMY` @0.5 (only if local player nearby) |
| `ellie.dialogue.like` | Do you like what you see honey?~ | `cowgirlStartMSG1` + meter reset |
| `ellie.dialogue.goodboy` | goooood.. ehh.. boy... hehe~ | `cowgirlcumMSG5` / `missionary_cumMSG2` + `GIRLS_ELLIE_GOODBOY` @0.5 (player form: GIGGLE[4] @3.0) |

Hardcoded lines: `hugMSG2` "Hmm..." (+ HMPH[3]), `hugMSG3` "Hey!" (+ HUH[1]), `carry_introMSG1` "I'm hungry.." (+ random HMPH @6.0), `carry_introMSG2` "heh~" (+ GIGGLE[3] @6.0), `handleSitUpTimer` "no bed in sight..." (+ GIRLS_ELLIE_SIGH[0] NEUTRAL @6.0).

## Sounds

Registration identical to Jenny (reflection, `stream: true`). Files under `assets/sexmod/sounds/girls/ellie/`:

| SoundHandler array (size) | sounds.json folder | Events |
|---|---|---|
| `GIRLS_ELLIE_AFTERSESSIONMOAN` (5) | `aftersessionmoan/` | 5 |
| `GIRLS_ELLIE_AHH` (10) | `ahh/` | 10 |
| `GIRLS_ELLIE_BJMOAN` (13) | `bjmoan/` | 13 (registered; not referenced by Ellie scene code — only Jenny's blowjob uses bjmoan) |
| `GIRLS_ELLIE_GIGGLE` (5) | `giggle/` | 5 |
| `GIRLS_ELLIE_HAPPYOH` (3) | `happyoh/` | 3 |
| `GIRLS_ELLIE_HEAVYBREATHING` (8) | `heavybreathing/` | **9 files (heavybreathing0..8)** — array registers 8; `heavybreathing8` orphaned |
| `GIRLS_ELLIE_HMPH` (4) | `hmph/` | 4 |
| `GIRLS_ELLIE_HUH` (2) | `huh/` | 2 |
| `GIRLS_ELLIE_LIGHTBREATHING` (8) | `lightbreathing/` | 8 |
| `GIRLS_ELLIE_LIPSOUND` (10) | `lipsound/` | 10 |
| `GIRLS_ELLIE_MMM` (9) | `mmm/` | 9 |
| `GIRLS_ELLIE_MOAN` (9) | `moan/` | 9 |
| `GIRLS_ELLIE_SADOH` (2) | `sadoh/` | 2 |
| `GIRLS_ELLIE_SIGH` (2) | `sigh/` | 2 |
| `GIRLS_ELLIE_COMETOMOMMY` (2) | `cometomommy/` | 2 |
| `GIRLS_ELLIE_GOODBOY` (2) | `goodboy/` | 2 |
| `GIRLS_ELLIE_MOMMYHORNY` (2) | `mommyhorny/` | 2 |

Misc shared arrays used by Ellie scenes: `MISC_POUNDING` (35), `MISC_BEDRUSTLE` (2), `MISC_INSERTS` (5), `GIRLS_ALLIE_LIPSOUND` (the carry `lipsound` keyframe plays **Allie's** lipsound array — cross-girl reuse), `SoundEvents.ENTITY_PLAYER_ATTACK_STRONG`.

## Model & Appearance

- **Geo variants**: `geo/ellie/nude.geo.json` (194 bones, top-level `body`, `steve`, `bone`) and `geo/ellie/dressed.geo.json` (425 bones, top-level `body`, `items`, `steve`). Note the nude model lacks the `items` root — dressed-only items bone.
- **Texture**: `textures/entity/ellie/ellie.png`. **Animations**: `animations/ellie/ellie.animation.json`.
- **Outfit bone groups** (`EllieNpcModel`): HeadArmor `[armorHelmet]`; **Attachments `[headband]`** (Ellie is the only girl of the three with a head attachment); TopArmor `[armorShoulderR, armorShoulderL, armorChest, armorBoobs]`; Top `[boobsFlesh, upperBodyL, upperBodyR]`; BottomArmor (same 8 as Jenny incl. duplicated `armorPantsLowR`); Bottom `[fleshL, fleshR, vagina, hotpants, slip, curvesL, curvesR, kneeL, kneeR]` (hotpants + slip = underwear group); ShoesArmor `[armorShoesL, armorShoesR]`.
- **Sitting head-tracking** (`EllieNpcModel.setLivingAnimations`): NPC only (not player-girls), `SITDOWNIDLE` only; head yaw aims at the nearest player within 15 blocks with per-facing clamps (`headYawOffsets`: yaw 0 → [0,-1.2,1.2]; -90 → [2,-71.56,-68]; 90 → [-2,68,70.5]); the 180° (back-facing) case uses an atan2 arc with ±1.5..3.14 clamps and a +3.0 offset trick; pitch from player height difference (±0.75). Clamped values are zeroed (head snaps back).
- **Vanilla placeholder** `EllieModel`: root at (-5, 1.5708, 0) with a child rotated 90° and an empty `emptyPart` (structure-only).
- **Renderer**: no blacklist override (all bones drawn). Name-tag offset `getScaleFactor()` = 0.4F. Weapon pose constants: `slashSwordRot=-85`, `stabSwordRot=-175`, `holdBowRot=-85`, `swordOffsetStab=(-0.1, 0.05, 0)` (left-handed-looking offsets vs Jenny).
- **Eye height**: `isBedBlocked() ? 1.53F : 1.9F` (crouch under a block lowers the eyes; player form mirrors via `hasNoOwner()`). Entity size from `BaseGirlEntity` defaults.

## AI & Behavior

- **Goals**: identical base set to Jenny — swim(0), `GirlFollowGoal`(1), tempt(2, 0.4, emerald/diamond/gold/pearl), `DoorInteractAiGoal`(3), `WatchClosestGirlGoal`(5, 3.0), wander(5, 0.35). `reinitTasks()` re-adds wander/watch (via super) and resets `yFlag` to -1.
- **Attributes**: 20 HP, 0.5 speed, 30 follow range; regen every 80 ticks (1 HP unmastered / 4 HP mastered, heart particles; 1 HP with mobs near).
- **Combat**: `GirlFollowGoal` identical to Jenny (melee/bow/downed). `LootTableHandler.ELLIE_TABLE` = `sexmod:ellie`.
- **Bed logic**: `getRandomSitPose()` — spiral bed scan (`findNearestBed`), 4 side offsets validated AIR + adjacent BED block, nearest wins; returns `{pos, yaw}` or null ("no bed in sight..." + SIGH). Sit-up transition walks her at 0.35, anchors at the pose → `SITDOWN` (109 ticks) → `SITDOWNIDLE`.
- **Horny-potion dash**: locks onto the nearest player, removes wander/watch AI, anchors, `DASH` for 16 ticks, then hug chain — unique to Ellie among the three.
- **Unique NPC quirk**: `alignPlayerToGirl` is overridden to a no-op (the hug/carry positioning uses `handleSitTimer` instead).

## Unique mechanics

- **Hug dialogue gate**: every bed scene (cowgirl/missionary) passes through the hug (HUG → HUGIDLE → HUGSELECTED) with the 5-line dialogue; the scene choice travels in `GIRL_HAND_STATES` ("cowgirl"/"Missionary") from client to server via the `animationFollowUp` packet.
- **Sit state machine**: five timers (`ak` 110 carry, `yFlag` 150 hug, `al` 109 sitdown, `ai` 79 sit-up, `zFlag` 16 dash, `af` 20 stand) — all reset in `resetSitState()`; `reinitTasks()` resets `yFlag`.
- **Carry variant re-roll**: `carry_slowDone` re-rolls `aa` (1..4) excluding the current value; `PRONE_DOGGY`-style variant logic shared with Bia.
- **Re-entry guards**: refuses MISSIONARY/COWGIRL/CARRY slow/fast while the corresponding cum plays; `HUGSELECTED` set server-side arms `ai`=79.
- **Horny meter**: shown during `CARRY_SLOW` (`showHornyMeter`), via `openSexUi` keyframe, hidden at cowgirlcumMSG4, reset at cum ends.
- **Bed-blocked crouching**: `isBedBlocked()` (block 2 blocks above her) selects crouchwalk/crouchidle and lowers eye height — lets her walk under low ceilings.

## Player form (ElliePlayerEntity)

Differences from the NPC:

- **Scale**: `getScaleFactor()` 2.05 (name tag); renderer `preRenderCallback` translates (0,-1.5,0), no scale. Eye height 1.53/1.9 by `hasNoOwner()`.
- **Menu**: `[Face fuck]` (`openInteractionMenu`); after sitdown/hug, `openEllieInventory` shows `[cowgirl, missionary]`. `handleInteraction()` → `SITDOWN`.
- **Owner command**: `Face fuck` → `teleportPlayerToGirl(uuid)` + `CARRY_INTRO` + `sendActionPacket`.
- **Scene chooser**: `doAction` stores "Cowgirl"/"Missionary" in `GIRL_HAND_STATES`; unknown actions → `SexPromptPacket` (with `ab` first-interaction flag). `updateAITasks` waits in `SITDOWNIDLE` for the choice + player within 1 block, then locks **both** players (owner + actor: flying, noClip, noGravity, movement lock) and starts `MISSIONARY_START` (player at her pos, yaw = her yaw, pitch 60) or `COWGIRLSTART` (player 1.8 blocks in front of her yaw, yaw+180, pitch -30).
- **Hug flow is client-timer driven**: `dashDone` → `HUG`; `hugDone` → `HUGIDLE` + scene menu; `hugselectedDone` → targetPos packet + `SendGirlToSexPacket` + NULL + `resetCameraAndPhysics()` (NPC instead routes through `handleStandTimer`/`U()`).
- **Movement**: crouch variants when `hasNoOwner()` (block above the owner's head), fly/fly2 toggle (`ar`), animation speeds 1.5/2.0/1.5.
- **`setCurrentAction`**: guards only MISSIONARY/COWGIRL cum re-entry (no CARRY guard; CARRY_INTRO timer is server-side only in the NPC).
- **Hand rendering**: `EllieModel` + `hand_nude.png`/`hand.png`; shield/bow item transforms heavily customized in `PlayerEllieRenderer` (`applyBowRotation` 90/180° pitch, `applyShieldBlockingTransform` full re-pose).
- **Data key `ai`** (118, OPTIONAL_UNIQUE_ID) = owner UUID; `ab` first-interaction flag; display name = owner's player name, else "anonymous horny girl".

## Data keys / NBT

Same shared key block as Jenny (BaseGirlEntity 99-110, AbstractGirlNpcEntity 111-117 — see jenny.md table). Ellie adds nothing beyond those; player form uses `ai` (118). NBT: `homeX/Y/Z`, `girlID` (with duped-id deletion), `sexmod:customname`, `sexmod:customModel`, `inventory` (7 slots, sword+bow defaults); player form adds `owner`. Loot table `sexmod:ellie`.

## Pitfalls & quirks (deobfuscated code notes)

- **Sit-state timer fields are obfuscated counters**: `yFlag` (hugidle 150), `al` (sitdown 109), `ai` (sit-up 79), `zFlag` (dash 16), `af` (stand 20), `ak` (carry 110) — all must be reset in `resetSitState()`; forgetting one leaves Ellie stuck in a sit/hug state.
- **`U()` must only run after the hug dialogue** — `GIRL_HAND_STATES` carries the scene choice from the client packet; `handleStandTimer` counts down `af` and calls `U()` (server side).
- **`setDismounted()` falls back to `resetSitState()`** when no player is bound (uuid null or player entity missing).
- **No-op `alignPlayerToGirl`** — do not "restore" it; the carry path repositions the player in `handleSitTimer`.
- **`animation.ellie.new`** is an orphan asset; **`BJMOAN` (13 events) is registered but never referenced** by Ellie scene code.
- **Cross-girl sound reuse**: the carry `lipsound` keyframe plays `GIRLS_ALLIE_LIPSOUND`.
- **`isBedBlocked()` differs client/server**: returns false when `isLocallyRegistered()` (client preview entities never crouch).
- **`showHornyMeter` only triggers on `CARRY_SLOW`** — the cowgirl/missionary meters come from `cowgirlStartMSG1`/`openSexUi`/`missionary_startDone`.
- **Hug-selected camera**: NPC `hugselectedMSG2` sets `HandlePlayerMovement.setMovementLock(true)` — the player is locked while the server walks her to the bed.
- **Bed search loops**: `getRandomSitPose` increments `attempts` per outer loop (`findNearestBed(this.getPosition(), ++attempts)`) — unbounded retry until a bed with a free side is found or the spiral scan exhausts (returns null).
- **Player-form `missionary_fastDone` differs from NPC**: player form keeps FAST while jumping (`setCurrentAction(MISSIONARY_FAST)`), NPC returns to SLOW unless jumping.

## Gaps

- Exact per-frame keyframe timings inside the animation JSON were not enumerated; keyframe names are taken from the code sound listeners.
- `ellie.dialogue.busy/followme/stopfollowme/gohome` have no code reference in this tree (unused lang entries).
- The `cowgirlslow2` name (no `cowgirlslow1`/`cowgirlslow` variant) is taken verbatim from the JSON and entity code.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
