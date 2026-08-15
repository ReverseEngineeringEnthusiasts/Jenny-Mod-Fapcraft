# Bia — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.bia.name = Bia` (lang key `entity.bia.name`) |
| Voice actress | @MissMoonified (mcmod.info credits) |
| Entity class(es) | NPC: `BiaEntity` (extends `AbstractGirlNpcEntity`, implements `IEllie`, `IBeddableSexGirl`). Player form: `BiaPlayerEntity` (extends `AbstractPlayerGirlEntity`) |
| Model / Renderer | NPC: `BiaNpcModel` (geckolib `GirlModel<BaseGirlEntity>`) + `BiaRenderer` (extends `GirlRenderer`, **overrides `getBlacklistedBones`**). Vanilla placeholder `BiaModel` (single 2x6x2 cube). Player form: `PlayerBiaRenderer` (extends `GirlPlayerRenderer`, same blacklist), hand model = `JennyModel` (reused placeholder!) with `textures/entity/bia/hand.png` |
| NpcType enum | `BIA(BiaEntity.class, 230053, BiaPlayerEntity.class, 65456415)` — NPC id `230053`, player-form id `65456415` |
| Player form | `BiaPlayerEntity` — horny-potion transformation; scenes anal + prone doggy (bed) + headpat |
| Obtaining | Spawn egg only (no natural spawn). `SexModEntities.registerSpawnEntity("bia", BiaEntity.class, 230053, 7488816, 7254603)` — egg colors 7488816/7254603, 50-block tracking. Also via `Girl wand` |

## Animations (complete list)

Source: `assets/sexmod/animations/bia/bia.animation.json` (41 animations — count matches the context list).

| Animation name | Loop | Purpose / when used (code cite) |
|---|---|---|
| `fhappy` | LOOP (6.0s) | Eyes controller when `action == NULL && autoBlink` (BiaEntity + BiaPlayerEntity, case `"eyes"`) |
| `null` | one-shot (2.0s, no bones) | Eyes/movement/action NULL reset pose |
| `idle` | LOOP (6.4s) | Movement controller: NULL, standing still (`tongueSmile2` mouth bone) |
| `walk` | LOOP (1.6s) | Movement controller, `WALK` |
| `fastwalk` | LOOP (0.68s) | Movement controller, `FAST_WALK` |
| `run` | LOOP (0.72s) | Movement controller, `RUN` |
| `backwards_walk` | LOOP (0.68s) | Player form: walking backwards (`ao.y < -0.1`) |
| `fly` / `fly2` | LOOP (0.84s) | Player form: flying; `fly2` toggle while movement anim name contains "fly" (`ap` flag). Note bia `fly` includes `cheekR` too |
| `sit` | LOOP (4.12s) | Action `SIT`; riding branch of movement controller |
| `ride` | LOOP (4.12s) | Action `RIDE` |
| `sitdown` | LOOP (5.52s) | Action `SITDOWN` (doggy bed intro; `spike21` + pony-tail bones). Keyframe `sitdownMSG1` ("come here big boy~" + breath), `sitdownDone` → `SITDOWNIDLE` |
| `sitdownidle` | LOOP (4.04s) | Action `SITDOWNIDLE` — the prone-doggy wait state on the bed; one of the two countdown states |
| `strip` | one-shot (5.04s) | Action `STRIP`; `stripMSG1` ("Hihi~" + giggle), `becomeNude` outfit toggle, `stripDone` → `resetCameraAndPhysics()` + `U()` |
| `attack0` / `attack1` / `attack2` | one-shot (0.36/0.52/0.52s) | Action `ATTACK`, `nextAttack` cycle; `attackDone` → NULL (no `attackSound` listener in Bia) |
| `bowcharge` | LOOP (161.2s) | Action `BOW` |
| `throwpearl` | LOOP (4.58s) | Action `THROW_PEARL`; `pearl` keyframe → `SendCompanionHomePacket` |
| `downed` | LOOP (5.125s) | Action `DOWNED` |
| `talk_horny` | LOOP (9.24s) | Player form's `TALK_HORNY` (NPC uses `talk_horny2`). Keyframes `talk_hornyMSG1..4` (heya/horny/so/fun) + `talk_hornyDone` → `TALK_IDLE` + anal/doggy menu |
| `talk_horny2` | one-shot (2.33s) | NPC's `TALK_HORNY` (name mismatch: the NPC plays `talk_horny2` for TALK_HORNY, the player form plays `talk_horny`) |
| `talk_idle` | LOOP (4.92s) | Player form's `TALK_IDLE` |
| `talk_idle2` | one-shot (6.5s) | NPC's `TALK_IDLE` (plays `talk_idle2`; includes `brush` bone) |
| `talk_response` | LOOP (3.4s) | Action `TALK_RESPONSE` (both forms). Keyframes `talk_responseMSG1..3` (huh/iuhm/yes), `talk_responseDone` → strip-or-`U()` (player form resets girl state first) |
| `headpat` | one-shot (7.72s) | Action `HEAD_PAT`. Keyframes `headpatMSG1..4` (headpats/hmm/huh2/thankyou), `headpatDone` → `resetCameraAndPhysics()` |
| `anal_prepare` | LOOP (5.52s) | Action `ANAL_PREPARE` (climb onto the bed). Keyframes `anal_prepareMSG1` (plob), `anal_prepareMSG2` (bedrustle), `anal_prepareDone` → `ANAL_WAIT` |
| `anal_wait` | LOOP (4.6s) | Action `ANAL_WAIT` — first countdown state (player must come within 1 block) |
| `anal_start` | LOOP (2.84s) | Action `ANAL_START` (penetration). Keyframes `anal_startMSG1` (MMM[3] + POUNDING[34]), `anal_startMSG2`, `anal_startDone` → `ANAL_SLOW` |
| `anal_slow` | LOOP (1.48s) | Action `ANAL_SLOW`. Keyframes `anal_slowMSG1` (+0.02, pounding + ahh) |
| `anal_fast` | LOOP (0.92s) | Action `ANAL_FAST`. `anal_fastMSG1` (+0.02), `anal_fastDone` → `ANAL_SLOW` (jump keeps fast) |
| `anal_cum` | LOOP (3.76s) | Action `ANAL_CUM`. Keyframes `anal_cumMSG2` (ahh), `anal_cumBlackScreen` → `BeeScreen.enableInteraction()`, `anal_cumDone` → reset |
| `prone_doggy_intro` | LOOP (1.875s) | Action `PRONE_DOGGY_INTRO` — player positioned behind her (pitch 60) |
| `prone_doggy_insert` | LOOP (2.125s) | Action `PRONE_DOGGY_INSERT` (auto followUp 42 ticks → `PRONE_DOGGY_SOFT`) |
| `prone_doggy_soft` | LOOP (1.46s) | Action `PRONE_DOGGY_SOFT` (uses `boyCam`). Keyframes `slide` (+0.005), `pound`, `doggyMoan` (+0.04), `doggySwitch` (jump → `PRONE_DOGGY_HARD`), `doggyReset` (jump → controller reset), `cum` (inserts), `orgasm1/2` (MMM[6]/MMM[7]) |
| `prone_doggy_hard1` / `hard2` / `hard3` | one-shot (2.04s each) | Action `PRONE_DOGGY_HARD` — variant suffix `ah` (NPC) / `aq` (player) re-rolled on `resetAnimationControllerTicks` (1..3, never the previous); auto followUp 34 ticks → `PRONE_DOGGY_SOFT` |
| `prone_doggy_cum` | one-shot (2.875s) | Action `PRONE_DOGGY_CUM`. `doggy_cumDone` → reset |
| `wave` | one-shot (3.79s) | Action `WAVE` — **dead**: no code path sets `Action.WAVE` |
| `wave_idle` | LOOP (5.21s) | Action `WAVE_IDLE` — dead alongside `WAVE` |

Camera metadata (Action enum): `TALK_*`/`HEAD_PAT`/`ANAL_*` default cam (min -90, max 30); `SITDOWN` (60, -90, flip), `SITDOWNIDLE` (60, -60, flip); `PRONE_DOGGY_*` and `CARRY_*`-style `useBoyCam=true, hideNameTag=true` (prone doggy is the boy-camera scene).

## Scenes & Actions

Entry flow: client `doAction` → `animationFollowUp` (→ `GIRL_HAND_STATES`) + `triggerAnalAction` (`KoboldStatePacket`) → server `setDismounted()` sets `yFlag` → `updateAITasks()` lerps her to `TARGET_POS` 40 ticks (`ag`) → anchors → `U()` dispatches on hand state: `talkHorny` → `TALK_HORNY`, `Headpat` → `HEAD_PAT`, `anal`/`doggy` → `SendGirlToSexPacket` → `goToSexBed()`.

Interaction menu (`openInteractionMenu`): `[strip|dressup, action.names.talk, action.names.headpat]` (strip when `OUTFIT_INDEX == 1`, else dressup). `processInteract` also handles name tags (`Items.NAME_TAG` → vanilla renaming) and ignores interaction during `SITDOWNIDLE`.

1. **Talk** — `action.names.talk` → binds the local player (`INTERACTION_PARTNER_UUID` + `playerSheHasSexWith` packet), `animationFollowUp "talkHorny"` → after lerp, `U()` → `TALK_HORNY` (NPC plays `talk_horny2`): `talk_hornyMSG1..4` lines (Heyaaa~/I am Hornyyyyy~/So.../Are we gonna have some fun?~) → `talk_hornyDone` → `TALK_IDLE` (NPC `talk_idle2`) + `openBiaInventory` menu **`[action.names.anal, doggy]`**.
2. **Scene choice → response** — `anal` or `doggy` → `animationFollowUp "anal"/"doggy"` + `TALK_RESPONSE` (`aa` = true, suppress camera reset): `talk_responseMSG1..3` (Huh?!.../I... uhm.../yes~) → `talk_responseDone` → `U()` → `SendGirlToSexPacket` → `goToSexBed()`.
3. **Anal (bed)** — `goToSexBed()`: `GIRL_HAND_STATES == "anal"` → `findNearestBedVector()` (nearest bed via spiral scan, 4 side offsets, nearest free side); else `getBedVector()` (validates AIR + `WorldUtils.canPlaceStructure` on the opposite side, up to 50 attempts). No bed → `GIRLS_BIA_BREATH[2]` + **`jenny.dialogue.nobedinsight`**; all sides blocked → `jenny.dialogue.bedobscured`. Walks at 0.35 (`af`/`zFlag`, re-path at 60/120, timeout 200) → on arrival: if hand state "anal" → `ANAL_PREPARE` + `setOutfitIndex(0)`, else → `SITDOWN`. `anal_prepareDone` → `ANAL_WAIT`. **Countdown** (`handleAnalState`, both sides every tick): player within 1.0 block → first contact (`ac == -1`): client `BeeScreen.enableInteraction()` + movement unlock, server binds interaction player, **`ac = 22`** (jar-faithful: original static `j = 22`; a deobf regression to -1 permanently stalled the scene) → countdown 22→0 → player locked (noClip+noGravity), `ANAL_START` (server; player positioned at target + rotateByYaw(-0.3,-1,-0.5, yaw), horny meter shown client-side) → `anal_startDone` → `ANAL_SLOW` → sneak → `ANAL_FAST` (jump keeps) → jump-with-full-meter → `ANAL_CUM` → `anal_cumBlackScreen` → `anal_cumDone` → `resetCameraAndPhysics()`. `resetLocalPlayerClientState()` re-arms `ac = -1`.
4. **Prone doggy (bed)** — hand state "doggy" → `SITDOWN` (sitdown anim) → `sitdownDone` → `SITDOWNIDLE` → same 22-tick countdown → on expiry: player rotated to her yaw + pitch 60; server: `setOutfitIndex(0)`, `PRONE_DOGGY_INTRO`, target shifted +1.0 z (rotateByYaw), player at target + (0, 1.1875 - eyeHeight, 0.5), anchored → `getNextAction` (sneak) → `PRONE_DOGGY_INSERT` (42 ticks auto) → `PRONE_DOGGY_SOFT` → jump (`doggySwitch`) → `PRONE_DOGGY_HARD` (hard1-3 variants) → jump-with-full-meter → `PRONE_DOGGY_CUM` → `doggy_cumDone` → reset. During `PRONE_DOGGY_INTRO` the horny meter shows when the Bee screen isn't visible.
5. **Headpat** — `action.names.headpat` → `animationFollowUp "Headpat"` → `U()` → `HEAD_PAT` (headpat anim): `headpatMSG1..4` lines (Ooh headpats!/Hmmm.... :D/huh...?/Tanku hehe) → `headpatDone` → `resetCameraAndPhysics()`.
6. **Strip / dress up** — `strip`/`dressup` → `STRIP` (strip anim) → `stripMSG1` ("Hihi~") → `stripDone` → `resetCameraAndPhysics()` + `U()` (hand state "" → no-op).
7. **Death drop** — `onDeath` (server): drops `ItemStack(Blocks.WOOL, random 0..3, meta 12)` (brown wool).
8. **Combat/AI actions** — `ATTACK`, `BOW`, `RIDE`, `SIT`, `THROW_PEARL`, `DOWNED`, plus dead `WAVE`/`WAVE_IDLE`.

## Dialogue (all lang lines)

| Lang key | Text | When spoken (code cite) |
|---|---|---|
| `bia.dialogue.busy` | I am busy at the moment~ | `processInteract` when `openInteractionMenu` fails (NPC) |
| `bia.dialogue.letsgo` | Heya lets Go! | **Lang-only** — unused in code (follow-me default GUI action) |
| `bia.dialogue.stopfollowme` | Hey, don't just leave me like that hehe~ | **Lang-only** — unused in code |
| `bia.dialogue.gohome` | Me is going home nya~ | **Lang-only** — unused in code |
| `bia.dialogue.hihi` | Hihi~ | `stripMSG1` + random giggle (NPC and player form) |
| `bia.dialogue.heya` | Heyaaa~ | `talk_hornyMSG1` + `GIRLS_BIA_HEY` (NPC) / `GIRLS_BIA_HEY[3]` (player) |
| `bia.dialogue.horny` | I am Hornyyyyy~ | `talk_hornyMSG2` + `GIRLS_BIA_GIGGLE[2]` |
| `bia.dialogue.so` | So... | `talk_hornyMSG3` + `GIRLS_BIA_BREATH[0]` |
| `bia.dialogue.fun` | Are we gonna have some fun?~ | `talk_hornyMSG4` + `GIRLS_BIA_HUH[0]` (NPC); player form hardcodes "Are we gonna have some fun nyaa?" |
| `bia.dialogue.huh` | Huh?!... | `talk_responseMSG1` + `GIRLS_BIA_HUH[2]` |
| `bia.dialogue.iuhm` | I... uhm... | `talk_responseMSG2` + `GIRLS_BIA_BREATH[1]` |
| `bia.dialogue.yes` | yes~ | `talk_responseMSG3` + `GIRLS_BIA_GIGGLE[0]` |
| `bia.dialogue.headpats` | Ooh headpats! | `headpatMSG1` + `GIRLS_BIA_BREATH[0]` |
| `bia.dialogue.hmm` | Hmmm.... :D | `headpatMSG2` + `GIRLS_BIA_MMM[0]` |
| `bia.dialogue.huh2` | huh...? | `headpatMSG3` + `GIRLS_BIA_HUH[0]` |
| `bia.dialogue.thankyou` | Tanku hehe | `headpatMSG4` + `GIRLS_BIA_GIGGLE[1]` |

Hardcoded lines: `onArriveHome` "I am living here now nya~" + random `GIRLS_BIA_BREATH`; `sitdownMSG1` "come here big boy~" + random breath. Bed-failure lines reuse **Jenny's** lang keys (`jenny.dialogue.nobedinsight` / `jenny.dialogue.bedobscured`).

## Sounds

Registration identical to Jenny/Ellie (reflection over `SoundEvent[]` fields, `stream: true`). Files under `assets/sexmod/sounds/girls/bia/` — smallest set of the three:

| SoundHandler array (size) | sounds.json folder | Events |
|---|---|---|
| `GIRLS_BIA_AHH` (8) | `ahh/` | 8 |
| `GIRLS_BIA_BJMOAN` (5) | `bjmoan/` | 5 (registered; not referenced by Bia scene code — no blowjob scene) |
| `GIRLS_BIA_BREATH` (4) | `breath/` | 4 |
| `GIRLS_BIA_GIGGLE` (3) | `giggle/` | 3 |
| `GIRLS_BIA_HEY` (4) | `hey/` | 4 |
| `GIRLS_BIA_HUH` (3) | `huh/` | 3 |
| `GIRLS_BIA_MMM` (8) | `mmm/` | 8 |

Misc shared arrays used by Bia scenes: `MISC_PLOB` (1), `MISC_BEDRUSTLE` (2), `MISC_POUNDING` (35; index 34 used explicitly in `anal_startMSG1`), `MISC_SLIDE` (7), `MISC_INSERTS` (5). No `moan`/`heavybreathing`/`lipsound` folders exist for Bia — scene sounds pick from ahh/mmm/breath only.

## Model & Appearance

- **Geo variants**: `geo/bia/bianude.geo.json` (155 bones, top-level `items`, `body`, `pillow`, `steve` — the **`pillow` root is unique to Bia**, used by the bed scenes) and `geo/bia/biadressed.geo.json` (277 bones, top-level `body`, `items`, `steve`).
- **Texture**: `textures/entity/bia/bia.png`. **Animations**: `animations/bia/bia.animation.json`.
- **Outfit bone groups** (`BiaNpcModel`): HeadArmor `[armorHelmet]`; **Attachments `[leaf7, leaf8]`** (leaf decorations; also blacklisted); TopArmor `[armorChest, armorBoobs, armorShoulderR, armorShoulderL]`; Top `[bra, upperBodyR, upperBodyL]` (bra instead of boobsFlesh); BottomArmor (same 8 incl. duplicated `armorPantsLowR`); Bottom `[slip, fleshL, fleshR, vagina, curvesL, curvesR, kneeL, kneeR]`; ShoesArmor `[armorShoesL, armorShoesR]`.
- **Blacklisted bones** (`BiaRenderer`/`PlayerBiaRenderer.getBlacklistedBones`): `boobs`, `booty`, `vagina`, `fuckhole`, `leaf7`, `leaf8` — excluded from the custom-part/custom-bone pass (only Bia among the three overrides the blacklist; Jenny/Ellie use the empty default).
- **Animation-specific bones**: `pillow`, `zipper` (talk/anal), `brush` (talk_idle2/horny), `fuckhole` (anal/prone), `leaf7/leaf8` (attachments), `spike21` (sitdown — shared with Ellie's rig), `ballL/ballR`, `boyCam` (prone doggy).
- **Renderer**: name-tag offset `getScaleFactor()` -0.2F (same as Jenny). Weapon pose constants identical to Jenny (`slashSwordRot=140`, `stabSwordRot=50`, `holdBowRot=140`, `swordOffsetStab=(0,-0.03,-0.2)`), arm angles `getLeftArmAngle()=35`, `getRightArmAngle()=140`.
- **Entity size**: 0.49 × 1.65 (shorter than Jenny's 1.95).

## AI & Behavior

- **Goals**: same base set (swim 0, `GirlFollowGoal` 1, tempt 2 @0.4, door 3, watch 5 @3.0, wander 5 @0.35). `reinitTasks()` re-adds wander+watch (no extra resets — Bia has no persistent timers beyond `ac`).
- **Attributes**: 20 HP, 0.5 speed, 30 follow; regen every 80 ticks (1/4 HP, heart particles). `LootTableHandler.BIA_TABLE` = `sexmod:bia`.
- **Bed logic**: `getBedVector()` (structure-validated, ≤50 attempts) vs `findNearestBedVector()` (plain nearest) chosen by hand state; 4 side offsets + yaws [0,180,-90,90]; walks at 0.35 with 60/120 re-path and 200-tick timeout; arrival → noClip+noGravity+zeroed motion.
- **One-shot physics cleanup**: `ab` flag clears noGravity/noClip once on the first AI tick after spawn.
- **Combat**: `GirlFollowGoal` (melee/bow/downed) — same as Jenny/Ellie; `onDeath` drops brown wool.
- **No combat-heal bonus beyond the shared regen; `SceneDebug` logging** wired into scene entry (`SCENE_ENTRY` group) on `doAction`/`U()`/`handleAnalState`/`setDismounted` — Bia is the only one of the three with SceneDebug instrumentation.

## Unique mechanics

- **22-tick scene countdown** (`ac`): the bed-scene pickup — player must stand within 1 block of an ANAL_WAIT/SITDOWNIDLE Bia; first contact arms `ac = 22` (client: interaction prompt + movement unlock; server: bind player), then 22→0 ticks to the scene action. **Pitfall documented in the class javadoc**: a deobf regression set `ac = -1` which permanently stalled every Bia bed scene; the player twin `BiaPlayerEntity.handleBiaAnalState` (`ar = 22`) is the reference implementation.
- **Talk → choice gate**: the anal/doggy menu only opens after the talk dialogue (`talk_hornyDone`); the choice plays through `TALK_RESPONSE` before the bed walk.
- **Prone-doggy variant re-roll**: `resetAnimationControllerTicks` re-rolls `ah` (1..3) while `PRONE_DOGGY_HARD` plays; `Action.PRONE_DOGGY_HARD` auto-follows to `PRONE_DOGGY_SOFT` after 34 ticks, `PRONE_DOGGY_INSERT` after 42.
- **Cum re-entry guards** (`setCurrentAction`): refuses ANAL/PRONE_DOGGY slow/fast while the cum plays; entering cum clears `GIRL_HAND_STATES` to `""`.
- **Camera reset suppression**: `aa` flag set by anal/doggy choices makes `ac()` (scene-end hook) skip `resetCameraAndPhysics()` during the bed transition.
- **Headpat is a scene**: binds the player (teleport, movement lock) and runs the full 4-line dialogue before `headpatDone` releases.
- **Horny meter**: shown via `sexUiOn`, at countdown end, during PRONE_DOGGY_INTRO (when Bee screen hidden); incremented by `anal_slowMSG1`/`anal_fastMSG1` (+0.02), `slide` (+0.005), `doggyMoan` (+0.04); reset at `anal_prepareDone`, `anal_cumDone`, `doggy_cumDone`.

## Player form (BiaPlayerEntity)

Differences from the NPC:

- **Scale**: `getScaleFactor()` 1.5; renderer `preRenderCallback` translates (0,-1.0,-0.05) and scales 0.65 (smallest of the three). Eye height 1.5.
- **Menu**: `[action.names.headpat]` (`openInteractionMenu`); the bed menu `[anal, doggy]` opens via `H_clash570` after the talk dialogue. `handleInteraction()` is a no-op.
- **Scene start is local**: `handleActionRequest("anal")` → `ANAL_PREPARE` + strip; `"doggy"` → `SITDOWN` + strip (returns true so the base `doAction` doesn't forward). `handleOwnerCommand("action.names.headpat")` → teleport + `HEAD_PAT` + `sendActionPacket`.
- **Countdown field** named `ar` (not `ac`): -1 idle / 22 first contact; re-armed in `resetLocalPlayerClientState()`. `handleBiaAnalState` mirrors the NPC logic but the client only acts when the nearby player is the local one (`isOwnPlayer`); the prone-doggy branch also teleports the **owner** to the follow position.
- **Animation naming mismatches vs NPC**: player form plays `talk_horny`/`talk_idle` for TALK_HORNY/TALK_IDLE (NPC: `talk_horny2`/`talk_idle2`); `anal_wait` plays LOOP (NPC non-loop); `prone_doggy_hard` variant field `aq`.
- **`talk_responseDone`**: player form calls `resetGirlState()` first, then `STRIP` if still dressed, else `U()`.
- **`anal_cumDone`/`doggy_cumDone`**: player form resets the meter and calls `resetCameraAndPhysics()` unconditionally (NPC only when locally controlled).
- **Hand rendering**: `getHandModel` returns `JennyModel` (reused placeholder) with `textures/entity/bia/hand.png` (no nude hand texture).
- **Controller registration order differs**: player form adds `movement` → `eyes` → `action` (NPC: action first).
- **Movement**: run/fastwalk/backwards all at 1.2× speed; `sit` when riding (`ak`); fly/fly2 toggle `ap`.

## Data keys / NBT

Same shared key block as Jenny (BaseGirlEntity 99-110, AbstractGirlNpcEntity 111-117 — see jenny.md table). Bia adds no new static data keys; player form uses `ai` (118). NBT: `homeX/Y/Z`, `girlID` (duped-id deletion), `sexmod:customname`, `sexmod:customModel`, `inventory` (7 slots, sword+bow defaults); player form adds `owner`. Loot table `sexmod:bia`. Bed-search uses `WorldUtils.canPlaceStructure` (structure-validated placement).

## Pitfalls & quirks (deobfuscated code notes)

- **THE Bia bug**: `handleAnalState` must assign `ac = 22` on first contact — the jar's static field was `j = 22` on `BaseGirlEntity`; the deobf regression to `-1` permanently stalled every Bia bed scene at ANAL_WAIT/SITDOWNIDLE (documented in both `BiaEntity` and `BiaPlayerEntity` javadocs; `SceneDebug` logs the countdown at `ac % 5 == 0`).
- **`TARGET_POS.equals(null)` try/catch**: the dismount lerp deliberately calls `TARGET_POS.equals(null)` inside a try to trigger an NPE as a null-check (sets target to `getFrontOffsetVector()` on catch) — jar-faithful weirdness, do not "clean up".
- **Cross-girl lang reuse**: Bia's bed-failure lines use `jenny.dialogue.nobedinsight`/`jenny.dialogue.bedobscured`.
- **`implements IEllie`** on Bia (and Jenny) — the "Ellie" interface is a misnamed shared marker.
- **Blacklisted bones include `fuckhole`** — the penetration hole bone is excluded from custom-bone processing so it cannot be removed/hidden by custom parts.
- **`sitdownMSG1`** hardcodes "come here big boy~" — not in the lang file.
- **Talk animation name flip-flop**: NPC TALK_HORNY → `talk_horny2`, NPC TALK_IDLE → `talk_idle2`, but the player form uses the base names — a jar inconsistency.
- **`ANAL_START`/`ANAL_WAIT` loop flags differ between forms** (NPC anal_wait non-loop vs player LOOP) — cosmetic only.
- **`processInteract` returns early during `SITDOWNIDLE`** — the bed-wait state consumes interaction so the menu can't interrupt the countdown.
- **Wool drop uses meta 12** (brown) with `nextInt(4)` count (0..3) — always at least an empty-stack chance of 0.
- **`openInventoryGui` for Bia's bed menu uses `dressUp=false`** (scene options, no dress-up toggle).

## Gaps

- Exact per-frame keyframe timings inside the animation JSON were not enumerated; keyframe names are taken from the code sound listeners.
- `bia.dialogue.letsgo/stopfollowme/gohome` have no code reference in this tree (unused lang entries).
- The purpose of `GIRLS_BIA_BJMOAN` (5 events) is unknown from code — registered but never played by any Bia scene (likely leftover from a cut blowjob scene).

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
