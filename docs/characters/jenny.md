# Jenny — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.jenny.name = Jenny` (lang key `entity.jenny.name`) |
| Voice actress | @Lizzywaffler (mcmod.info credits) |
| Entity class(es) | NPC: `JennyEntity` (extends `AbstractGirlNpcEntity`, implements `IEllie`, `IBeddableSexGirl`). Player form: `JennyPlayerEntity` (extends `AbstractPlayerGirlEntity`) |
| Model / Renderer | NPC: `JennyNpcModel` (geckolib `GirlModel<BaseGirlEntity>`) + `JennyRenderer` (extends `GirlRenderer`); vanilla placeholder `JennyModel` (`IVanillaModel`, single 2x6x2 cube) used on the vanilla-model render path. Player form: `PlayerJennyRenderer` (extends `GirlPlayerRenderer`), hand model = `SlimeModel` with `textures/entity/jenny/hand_nude.png` (nude) / `hand.png` |
| NpcType enum | `JENNY(JennyEntity.class, 177013, JennyPlayerEntity.class, 12388645)` — entity id `177013` (NpcType.npcID), player-form id `12388645` (NpcType.playerID). Editor id from `Reference.EDITOR_ID_COUNTER` |
| Player form | `JennyPlayerEntity` — the horny-potion transformation; scenes blowjob/paizuri/doggy, no payment gate |
| Obtaining | Spawn egg only (no natural spawn). `SexModEntities.registerSpawnEntity("jenny", JennyEntity.class, 177013, 3286592, 12655237)` — egg colors 3286592/12655237, 50-block tracking. Also spawnable via the `Girl wand` (`item.npc_editor_wand`) |

## Animations (complete list)

Source: `assets/sexmod/animations/jenny/jenny.animation.json` (38 animations, verified against the JSON — count matches the context list). Names are `animation.jenny.<name>`; loop column is the JSON `loop` flag (HOLD = hold_on_last_frame).

| Animation name | Loop | Purpose / when used (code cite) |
|---|---|---|
| `fhappy` | LOOP (6.0s) | Eyes controller, played when `getCurrentAction() == Action.NULL && action.autoBlink` (`JennyEntity.animationPredicate` case `"eyes"`; same in `JennyPlayerEntity`) |
| `null` | LOOP, no bones | Eyes controller when action is not NULL; movement controller when any non-NULL action runs; action controller `NULL` case. Empty animation = "pose reset" |
| `idle` | LOOP (8.0s) | Movement controller when action NULL, not riding, and |Δpos| == 0 (`animationPredicate` case `"movement"`) |
| `walk` | LOOP (1.2s) | Movement controller when `getWalkType() == WALK` |
| `fastwalk` | LOOP (0.8s) | Movement controller when `getWalkType() == FAST_WALK` |
| `run` | LOOP (0.72s) | Movement controller when `getWalkType() == RUN` |
| `backwards_walk` | LOOP (0.68s) | Player form only: movement controller when moving backwards (`ao.y < -0.1F`, speed 1.2) — `JennyPlayerEntity` |
| `fly` / `fly2` | LOOP (0.84s) | Player form only: `!af` branch of movement controller; `fly2` toggled every re-play while the current movement animation name contains "fly" (`ap` flag toggling) |
| `sit` | LOOP (4.12s) | Action `SIT`; also `isRiding()` branch of the NPC movement controller |
| `ride` | LOOP (4.12s) | Action `RIDE` |
| `attack0` / `attack1` / `attack2` | one-shot (0.36/0.52/0.52s) | Action `ATTACK`, selected by `nextAttack` counter (0→1→2→0). Sound keyframes `attackSound` (plays `ENTITY_PLAYER_ATTACK_STRONG`) and `attackDone` (back to NULL, increments counter) |
| `bowcharge` | LOOP (93.6s) | Action `BOW` (charged bow hold, non-loop) |
| `throwpearl` | LOOP (4.58s) | Action `THROW_PEARL`; keyframe `pearl` sends `SendCompanionHomePacket` (sends girl home) |
| `downed` | LOOP (5.125s) | Action `DOWNED` (0-HP combat state, 1 HP kept, revived by healing — see `GirlFollowGoal` inner class `a`) |
| `strip` | one-shot (5.04s) | Action `STRIP` (both dress-up and strip; keyframes `stripMSG1` "Hihi~" + giggle, `becomeNude` toggles outfit, `stripDone` → `U()`) |
| `payment` | one-shot (9.32s) | Action `PAYMENT` (emerald-demand scene, see Scenes). Keyframes `paymentMSG1` ("Huh?"), `paymentMSG2` (player line per scene), `paymentMSG3` ("Hehe~"), `paymentMSG4` (plob), `paymentDone` → `U()` |
| `blowjobintro` | HOLD (49.28s) | Action `STARTBLOWJOB`. Keyframes `bjiMSG1..12` (dialogue + sounds + horny meter), `bjiDone` → `SUCKBLOWJOB` |
| `blowjobsuck` | LOOP (6.04s) | Action `SUCKBLOWJOB` |
| `blowjobthrust` | LOOP (1.76s) | Action `THRUSTBLOWJOB` (sneak/fast). Keyframes `bjtMSG1` (+0.04 meter), `bjtDone` → `SUCKBLOWJOB`, `bjtReady` jump-reset |
| `blowjobcum` | LOOP (11.8s) | Action `CUMBLOWJOB` (jump). Keyframes `bjcMSG1..7`, `bjcBlackScreen` → `BeeScreen.enableInteraction()`, `bjcDone` → `resetCameraAndPhysics()` (horny meter reset) |
| `paizuri_start` | LOOP (8.67s) | Action `PAIZURI_START` (boobjob intro, non-loop in practice). Keyframes `paizuriStartMSG1`, `paizuri_startStep` (block step sound), `paizuri_startDone` → `PAIZURI_SLOW` |
| `paizuri_slow` | LOOP (3.375s) | Action `PAIZURI_SLOW` |
| `paizuri_fast` | LOOP (1.1s) | Action `PAIZURI_FAST` (sneak). Keyframes `paizuriFastMSG1` (+0.04), `paizuri_fastDone` → `PAIZURI_SLOW` |
| `paizuri_cum` | LOOP (11.48s) | Action `PAIZURI_CUM` (jump). Keyframes `paizuri_cumStart` (camera), `paizuri_cumDone` → reset |
| `doggygoonbed` | one-shot (6.52s) | Action `STARTDOGGY` (getting on the bed). Keyframes `doggyGoOnBedMSG1..4`, `doggyGoOnBedDone` → sends `SetPlayerForGirlPacket` + `WAITDOGGY` |
| `doggywait` | LOOP (6.44s) | Action `WAITDOGGY` (waiting for the player to walk up) |
| `doggystart` | LOOP (9.92s) | Action `DOGGYSTART` (player snapped in via `af` snap-in in `updateAITasks`). Keyframes `doggystartMSG1..5`, `doggystartDone` → `DOGGYSLOW` |
| `doggyslow` | LOOP (2.32s) | Action `DOGGYSLOW`. Keyframes `doggyslowMSG1` (+0.00666 meter, random MMM/MOAN/heavybreathing), `doggyslowMSG2` |
| `doggyfast_soft` / `doggyfast_hard` | LOOP (1.08s) | Action `DOGGYFAST`, suffix chosen by `aa` flag (`doggyfast_` + (aa ? "hard" : "soft")); `aa` set true by `doggyfastReady` while jumping, cleared on `doggyslowMSG1`/`doggyfastDone`. `doggyfastMSG1` (+0.02 meter, alternating moan/ahh via `ag` counter) |
| `doggycum` | LOOP (6.68s) | Action `DOGGYCUM`. Keyframes `doggycumMSG1..5` (cuminflation, pounding, heavybreathing 4..7), `doggyCumDone` → reset |
| `wave` | one-shot (3.54s) | Action `WAVE` — **dead**: no code path sets `Action.WAVE` (Action.WAVE has auto followUp `WAVE_IDLE` after 71 ticks) |
| `wave_idle` | LOOP (5.21s) | Action `WAVE_IDLE` — dead alongside `WAVE` |
| `fortnite` | one-shot (10.76s) | **Orphan**: not referenced by any code or sound listener — dead asset left in the JSON |
| `animation.ellie.missionary_slow` | LOOP, len unset | **Shared/stray**: declared inside *jenny's* animation JSON with no length; Ellie's own JSON also defines `animation.ellie.missionary_slow`. Not referenced by Jenny code — leftover from Ellie scene dev |

Scene actions use camera clamps from the `Action` enum: paizuri `minGirlPitch=-90, maxGirlPitch=-56`, all others default `(-90, 30)`; no `flipGirlYaw`/`useBoyCam` on any Jenny action.

## Scenes & Actions

Entry flow (shared): client `doAction` writes `animationFollowUp` → `GIRL_HAND_STATES` via `ChangeDataParameterPacket` and sends `KoboldStatePacket` → server `setDismounted()` sets `ab` → `updateAITasks()` lerps her to `TARGET_POS` for 40 ticks (`ac` counter, `RotationHelper.lerpVec3d` **INT** variant) → anchors (`IS_ANCHORED`) → `U()` dispatches on `GIRL_HAND_STATES`.

Interaction menu (`openInteractionMenu`): options `[action.names.blowjob, action.names.boobjob, action.names.doggy, strip|dressup]` (strip shown when `OUTFIT_INDEX == 1`, else dressup). Without the horny potion effect, the menu is opened with payment rewards (3 emerald, 2 ender pearl, 2 diamond, 1 gold ingot when dressed); with the potion, no rewards.

1. **Strip / Dress up** — `strip` (or `dressup`) → `U()` case `"strip"` → `resetGirlState()` + `Action.STRIP`; `becomeNude` keyframe toggles `currentModel` (outfit) client-side; `stripDone` → `U()` (hand state now empty → no-op) + `resetCameraAndPhysics()`. `dressup` sets `Action.STRIP` directly (reverse strip).
2. **Payment gate** — without the horny potion effect (`yFlag`, server-set in `onUpdate()` from `isPotionActive(HornyPotion.HORNY_POTION)`), the first scene request plays `Action.PAYMENT` instead: the `payment` animation runs and `paymentMSG2` broadcasts the player's demand line depending on the pending hand state — `showBobsandveganapls` (strip), `giveblowjob`, `givesex` (doggy), `givebooba` (boobjob), default `"sex pls"`. `paymentDone` → `U()` starts the real scene. With the potion active the scene starts directly.
3. **Blowjob** — `STARTBLOWJOB` (blowjobintro) → `bjiMSG1..12` dialogue/sound keyframes (lines blowjobtext1..8, sounds MMM/lightbreathing/aftersessionmoan/belljingle/hmph/giggle/lipsound/bjmoan, horny meter +0.02 per keyframe, camera reposition at `bjiMSG10` (-0.65,-0.8,-0.25, 60°, -3°) only if local player) → `bjiDone` → `SUCKBLOWJOB` (blowjobsuck, meter visible) → sneak = fast (`getNextAction` → `THRUSTBLOWJOB`, +0.04/loop) → jump = cum (`getCumAction` → `CUMBLOWJOB`, camera pitch 70) → `bjcBlackScreen` → `bjcDone` → `resetCameraAndPhysics()` (full `ResetGirlPacket`).
4. **Boobjob / Paizuri** — `U()` case `"boobjob"`: requires `OUTFIT_INDEX == 0`, otherwise runs `Action.STRIP` first and returns. → `PAIZURI_START` → `paizuri_startDone` → `PAIZURI_SLOW` → sneak → `PAIZURI_FAST` → jump → `PAIZURI_CUM` → reset. `boobjob_camera` keyframe repositions the nearest player (-0.7,-0.6,0.2, 60°, -3°) once (`ae` one-shot) and binds them as interaction player.
5. **Doggy (bed)** — `U()` case `"doggy"` → `SendGirlToSexPacket` → `goToSexBed()`: nearest bed via `findNearestBed` (spiral scan, radius 22, ±3 height); if none → `GIRLS_JENNY_HMPH[2]` + `jenny.dialogue.nobedinsight`; picks the best of 4 bed-side offsets (yaws 0/180/-90/90) with free AIR; if all blocked → `jenny.dialogue.bedobscured`. Walks at 0.35 speed (`zFlag` walk state, re-path at ticks 60/120, timeout 200) → anchor + noClip + noGravity → `STARTDOGGY` (doggygoonbed) → `doggyGoOnBedDone` → `WAITDOGGY` → **snap-in** (`af` in `updateAITasks`): when the nearest player is within 0.5 blocks the player is bound (`INTERACTION_PARTNER_UUID`), teleported in front, movement locked (`SetPlayerMovementPacket(false)`) → `DOGGYSTART` (doggystart) → `doggystartDone` → `DOGGYSLOW` → sneak → `DOGGYFAST` (soft/hard by jump) → jump → `DOGGYCUM` → reset.
6. **Combat/follow actions** (AI, not scenes): `ATTACK` (attack0-2), `BOW` (bowcharge), `RIDE`, `SIT`, `THROW_PEARL` (pearl keyframe → send home), `DOWNED`.
7. **Interact while busy** — `processInteract` returns false → chat `jenny.dialogue.busy` ("I am busy at the moment~").

## Dialogue (all lang lines)

`en_us.lang` (also translated in 14 other lang files). "Player line" = broadcast with `<player> ` prefix via `broadcastChatAround(..., true)`; girl lines via `sendGirlChatMessage`/`sendChatMessage`.

| Lang key | Text | When spoken (code cite) |
|---|---|---|
| `jenny.dialogue.busy` | I am busy at the moment~ | `processInteract` when `openInteractionMenu` fails (NPC) |
| `jenny.dialogue.followme` | Okay! I am right behind you %s~ | **Lang-only** — not referenced in current Java (follow-me default GUI action; the `%s` is never formatted) |
| `jenny.dialogue.bye` | Oh.. well.. cya~ | **Lang-only** — unused in code |
| `jenny.dialogue.bye2` | Alright, I'm going home. Cya~ | **Lang-only** — unused in code |
| `jenny.dialogue.nobedinsight` | no bed in sight... | `goToSexBed()` when no bed found (also reused by Bia's bed search) |
| `jenny.dialogue.bedobscured` | bed is obscured... | `goToSexBed()` when all 4 bed sides blocked |
| `jenny.dialogue.hihi` | Hihi~ | `stripMSG1` keyframe + giggle |
| `jenny.dialogue.hehe` | Hehe~ | `paymentMSG3` keyframe + giggle |
| `jenny.dialogue.huh` | Huh? | `paymentMSG1` keyframe + `GIRLS_JENNY_HUH[1]` |
| `jenny.dialogue.showBobsandveganapls` | show Bobs and vegana pls | `paymentMSG2` — player line while strip is pending |
| `jenny.dialogue.giveblowjob` | Give me the sucky sucky and these are yours | `paymentMSG2` — player line while blowjob is pending |
| `jenny.dialogue.givesex` | Give me the sex pls :) | `paymentMSG2` — player line while doggy is pending |
| `jenny.dialogue.givebooba` | gib booba OwO | `paymentMSG2` — player line while boobjob is pending |
| `jenny.dialogue.blowjobtext1` | What are you... | `bjiMSG1` + `GIRLS_JENNY_MMM[8]`, camera flip 180°, meter reset |
| `jenny.dialogue.blowjobtext2` | eh... boys... | `bjiMSG2` + `GIRLS_JENNY_LIGHTBREATHING[8]` |
| `jenny.dialogue.blowjobtext3` | OHOhh...! | `bjiMSG3` + `GIRLS_JENNY_AFTERSESSIONMOAN[0]` |
| `jenny.dialogue.blowjobtext4` | Was this really necessary?! | `bjiMSG5` + `GIRLS_JENNY_HMPH[1]` @0.5, meter reset |
| `jenny.dialogue.blowjobtext5` | Oh~ | `bjiMSG6` + lightbreathing[8] |
| `jenny.dialogue.blowjobtext6` | You like it?~ | `bjiMSG7` + `GIRLS_JENNY_GIGGLE[4]` |
| `jenny.dialogue.blowjobtext7` | Yee | `bjiMSG8` — player line + `MISC_PLOB[0]` @0.5 |
| `jenny.dialogue.blowjobtext8` | Hihihi~ | `bjiMSG9` + `GIRLS_JENNY_GIGGLE[2]` |
| `jenny.dialogue.doggytext1` | what are you waiting for?~ | `doggyGoOnBedMSG2` + lightbreathing[9] |
| `jenny.dialogue.doggytext2` | this ass ain't gonna fuck itself... | `doggyGoOnBedMSG3` + `GIRLS_JENNY_GIGGLE[0]` |

Hardcoded (non-lang) lines: `onArriveHome` → "Alright, this is my new Home~" + `GIRLS_JENNY_HAPPYOH[1]`; `paymentMSG2` default → "sex pls"; player-form hardcodes the payment lines instead of the lang keys.

## Sounds

Registration: `SoundHandler` reflection over `SoundEvent[]` fields → event key `sexmod:<field lowercased with _ → .>.<category><index>` (e.g. `GIRLS_JENNY_AFTERSESSIONMOAN` → `girls.jenny.aftersessionmoan.aftersessionmoan0`), all `stream: true`, category `entity`. Audio files under `assets/sexmod/sounds/girls/jenny/`.

| SoundHandler array (size) | sounds.json folder | Events |
|---|---|---|
| `GIRLS_JENNY_AFTERSESSIONMOAN` (5) | `aftersessionmoan/` | 5 (`aftersessionmoan0..4`) |
| `GIRLS_JENNY_AHH` (10) | `ahh/` | 10 |
| `GIRLS_JENNY_BJMOAN` (13) | `bjmoan/` | 13 |
| `GIRLS_JENNY_GIGGLE` (5) | `giggle/` | 5 |
| `GIRLS_JENNY_HAPPYOH` (3) | `happyoh/` | 3 |
| `GIRLS_JENNY_HEAVYBREATHING` (8) | `heavybreathing/` | 8 |
| `GIRLS_JENNY_HMPH` (5) | `hmph/` | 4 files (hmph0..3) — **array index 4 registered but no sound file** |
| `GIRLS_JENNY_HUH` (2) | `huh/` | 2 |
| `GIRLS_JENNY_LIGHTBREATHING` (12) | `lightbreathing/` | 12 |
| `GIRLS_JENNY_LIPSOUND` (10) | `lipsound/` | 10 |
| `GIRLS_JENNY_MMM` (9) | `mmm/` | 9 |
| `GIRLS_JENNY_MOAN` (8) | `moan/` | 8 |
| `GIRLS_JENNY_SADOH` (2) | `sadoh/` | 2 |
| `GIRLS_JENNY_SIGH` (2) | `sigh/` | 2 |
| — (unused) | `aftersessionmoan/bjmoan/` | 13 orphan events (`girls.jenny.aftersessionmoan.bjmoan.bjmoan0..12`) — never registered by the reflection loop (no matching array) |

Misc scene sounds referenced from Jenny code (shared `MISC_*` arrays): `MISC_PLOB` (1), `MISC_BELLJINGLE` (1), `MISC_BEDRUSTLE` (2), `MISC_SLAP` (2), `MISC_TOUCH` (2), `MISC_POUNDING` (35), `MISC_SMALLINSERTS` (5), `MISC_CUMINFLATION` (1), plus `SoundEvents.ENTITY_PLAYER_ATTACK_STRONG` (attackSound keyframe) and the block's step sound (`paizuri_startStep`).

## Model & Appearance

- **Geo variants** (outfit index: 0 = nude, 1 = dressed; `OUTFIT_INDEX` data key, default 1): `geo/jenny/jennynude.geo.json` (243 bones) and `geo/jenny/jennydressed.geo.json` (294 bones). Top-level bones: `body`, `items`, `steve`. Model selection `GirlModel.getSexWorldTexture` (falls back to nude for out-of-range indices).
- **Texture**: `textures/entity/jenny/jenny.png` (single texture for both outfits).
- **Animations file**: `animations/jenny/jenny.animation.json`.
- **Outfit bone groups** (`JennyNpcModel`): HeadArmor `[armorHelmet]`; TopArmor `[armorShoulderR, armorShoulderL, armorChest, armorBoobs]`; Top `[boobsFlesh, upperBodyL, upperBodyR]`; BottomArmor `[armorBootyR, armorBootyL, armorPantsLowL, armorPantsLowR, armorPantsLowR (dup), armorPantsUpR, armorPantsUpL, armorHip]`; Bottom `[fleshL, fleshR, vagina, curvesL, curvesR, kneeL, kneeR]`; ShoesArmor `[armorShoesL, armorShoesR]`. Armor bones hidden when the matching NPC armor slot is empty; nude group hidden when worn.
- **Steve/Alex arms**: `animateGirl` toggles `rightArmSteve/rightLowerArmSteve/leftArmSteve/leftLowerArmSteve` vs `rightArmAlex/...` from the interaction player's skin type; the whole `steve` body bone is hidden while the current action has no player (`action.hasPlayer`).
- **Camera bones**: `boyCam`, `girlCam` (CAMERA_PLACEMENTS) — world positions published each frame by the renderer.
- **Renderer specifics**: `JennyRenderer` does not override `getBlacklistedBones` (empty default from `IGirlRenderer` — all bones drawn). Name tag offset uses `getScaleFactor()` (-0.2F). Renderer handles weapon bone (held item, bow-pull, sword-stab poses via `slashSwordRot=140`, `stabSwordRot=50`, `holdBowRot=140`, `swordOffsetStab=(0,-0.03,-0.2)`), payment trade overlay while `PAYMENT`, opaque `ballL/ballR/cock`, first-person `Head2` hidden, armor material tinting.

## AI & Behavior

- **Goals** (`BaseGirlEntity.initEntityAI` + `AbstractGirlNpcEntity.initEntityAI`): task 0 `EntityAISwimming`, task 1 `GirlFollowGoal` (companion AI), task 2 `EntityAITempt` (0.4 speed, TEMPTATION_ITEMS = emerald, diamond, gold ingot, ender pearl), task 3 `DoorInteractAiGoal` (breaks doors, `PathNavigateGround.setBreakDoors(true)`), task 5 `WatchClosestGirlGoal` (players, 3.0 range, 1.0 rate), task 5 `EntityAIWanderAvoidWater` (0.35). `reinitTasks()` restores wander+watch after scenes.
- **Attributes**: maxHealth 20, movementSpeed 0.5, followRange 30. `canDespawn()` false.
- **Regen**: every 80 ticks when hurt — no master: +1 HP; with master: +4 HP (or +1 with heart particles if hostile mobs within 7 blocks).
- **Combat** (`GirlFollowGoal`): state machine ride → target → attacker → master's target → nearby mobs → follow/idle; `ATTACK_MODE` data key 0 idle/1 melee/2 bow; melee damage from held weapon attribute modifiers, bow = charged `BOW` action with arrow; downed mechanic: lethal damage downs (1 HP, `Action.DOWNED`), full heal revives, death drops inventory.
- **Follow/home**: `followme` binds master UUID (`MASTER` key), `stopfollowme`/`gohome` clear it + `SendCompanionHomePacket`, `setnewhome` stores `homePos` (NBT `homeX/Y/Z`) + `onArriveHome` chat/sound; equipment GUI (`PlayerActionPacket`). Ender-pearl "send home" via `THROW_PEARL` keyframe `pearl`.
- **Bed logic**: `goToSexBed()` — spiral bed scan (radius 22, ±3 Y), 4 side offsets with yaw 0/180/-90/90, walks at 0.35 (re-path at 60/120, 200-tick timeout), then anchor+noClip+noGravity for the scene.
- **Scene anchoring**: while `IS_ANCHORED`, `updateAITasks()` pins her to `TARGET_POS`/`YAW_ROTATION` every tick; `GirlModel.setLivingAnimations` additionally pins client-side at render time.

## Unique mechanics

- **Payment gate**: first scene request without the horny potion effect runs `Action.PAYMENT` — the player must pay (emeralds/pearls/diamonds/gold in the menu rewards) before the scene starts. `yFlag` (data id 118, BOOLEAN) mirrors the potion effect server→client every tick; potion bypasses payment.
- **Horny potion**: `HornyPotion.HORNY_POTION` effect — as an NPC it disables the payment gate; drinking it transforms the player into `JennyPlayerEntity`.
- **Dismount lerp**: 40-tick lerp to `TARGET_POS` using `RotationHelper.lerpVec3d(pos, target, 40 - ac)` — **INT (step) variant required**; the double variant flings the girl ~40 blocks (documented pitfall in class javadoc).
- **Doggy snap-in**: `af` flag waits for the player within 0.5 blocks, then binds + repositions them to start `DOGGYSTART` — this is what starts the standing doggy scene.
- **Horny meter**: `HornyMeterHud` shown at scene start (`sexUiOn`), incremented by MSG keyframes, reset at intro/cum, hidden at `bjcMSG2`, reset at scene end.
- **Re-entry guards** (`setCurrentAction`): refuses `DOGGYSLOW/DOGGYFAST` while `DOGGYCUM` plays, `THRUSTBLOWJOB/SUCKBLOWJOB` while `CUMBLOWJOB` plays, `PAIZURI_SLOW/FAST` while `PAIZURI_CUM` plays. Also repositions the player (+0.2 z, yaw+180) when leaving `STARTBLOWJOB`/`PAIZURI_START`.

## Player form (JennyPlayerEntity)

Differences from the NPC:

- **Scale**: renderer `preRenderCallback` translates (0, -1.25, 0) and scales 0.8; `getScaleFactor()` 1.75 (name tag offset); eye height 1.64. NPC `getScaleFactor()` is -0.2.
- **Menu**: `[action.names.blowjob, action.names.boobjob]` only — no doggy/strip in the GUI; doggy is started via `handleInteraction()` (`STARTDOGGY` + strip + camera yaw) when the owner walks up.
- **Owner commands** (`handleOwnerCommand`): `boobjob` → strip + `PAIZURI_START` + `sendActionPacket` + `teleportPlayerToGirl`; `blowjob` → `STARTBLOWJOB` + packet (outfit index passed) + teleport.
- **Doggy bed phase**: `WAITDOGGY` waits for the **nearest non-owner** player within 1 block (owner gets DARK_PURPLE "sowy no lesbo action yet uwu"); both players are set flying, movement locked, camera at (0,0,0.4, 0°, 60°), then `DOGGYSTART`.
- **Movement animations**: flying (`fly`/`fly2` toggle via `ap`), run 1.2×, fastwalk 1.5×, `backwards_walk` when walking backward; `sit` when `ak` (riding) — the NPC's movement controller instead uses `sit` for `isRiding()`.
- **Hardcoded dialogue**: player form uses hardcoded strings for payment lines ("show Bobs and vegana pls", "Give me the sucky sucky and these are yours", "gib boba OwO", "sex pls") instead of the lang keys; stripMSG1 "Hihi~"; blowjob lines via lang keys in `JennyPlayerEntity` sound listener (identical keyframe names).
- **Camera differences**: `bjiMSG10` uses (-0.4,-0.8,-0.2, 60°, -3°); `boobjob_camera` sets `cameraYaw = 180` and repositions only when local player + `as` one-shot; paizuri reposition (-0.7,-0.6,-0.2).
- **Hand rendering**: `getHandModel` returns `SlimeModel` (vanilla placeholder) with `hand_nude.png` (index 0) / `hand.png`.
- **Shared machinery**: same `getNextAction`/`getCumAction`/`setCurrentAction` guards as the NPC; `A_clash381()` false; scene end identical (`bjcDone`/`paizuri_cumDone`/`doggyCumDone` → `resetCameraAndPhysics()`); PAYMENT cases in the sound listener are vestigial (`U()` is empty in player girls).
- **Player-girl rules** (from `AbstractPlayerGirlEntity`): size (0.01,0.01), `canBeCollidedWith` false, `canBePushed` false, always noClip+noGravity, glued to the owner's position when not anchored, owner's swing mirrors into `ATTACK` action, armor mirrored from owner (`syncArmor`), `resetLocalPlayerClientState` sends the single-arg full `ResetGirlPacket`, strip sequence `D_clash581` toggles outfit at tick 65 and ends at 100.

## Data keys / NBT

Shared data-manager keys (explicit serializer ids, **never renumber**):

| Id | Key | Type | Meaning |
|---|---|---|---|
| 99 | `CUSTOM_NAME` | STRING | custom display name ("" = default) |
| 100 | `CUSTOM_MODEL_KEY` | STRING | whitelisted custom-model code (`sexmod:CustomModel...` on players) |
| 101 | `WALK_SPEED` | STRING | `WALK`/`FAST_WALK`/`RUN` (BaseGirlEntityState) |
| 102 | `INTERACTION_PARTNER_UUID` | STRING | scene-bound player UUID, `"null"` = unset |
| 103 | `GIRL_HAND_STATES` | STRING | scene-entry hand state (`animationFollowUp` on the wire) |
| 104 | `CUR_ACTION` | STRING | current `Action` name |
| 105 | `OUTFIT_INDEX` | VARINT | 0 nude / 1 dressed |
| 106 | `GIRL_ID` | STRING | persistent girl UUID (minted on first access) |
| 107 | `YAW_ROTATION` | FLOAT | anchored yaw |
| 108 | `TARGET_POS` | STRING | `x\|y\|z` anchor position |
| 109 | `IS_ANCHORED` | BOOLEAN | anchor lock |
| 110 | `MASTER` | STRING | owner player UUID (follow mode) |
| 111 | `ATTACK_MODE` | VARINT | 0 idle / 1 melee / 2 bow (GirlFollowGoal) |
| 112-115 | `BOOTS_SLOT`..`HELMET_SLOT` | ITEM_STACK | armor mirror |
| 116 | `BOW` | ITEM_STACK | bow slot (inventory slot 1) |
| 117 | `WEAPON` | ITEM_STACK | weapon slot (inventory slot 0) |
| 118 | `yFlag` (JennyEntity) | BOOLEAN | horny-potion active. **Note**: id 118 is dual-purpose — `AbstractPlayerGirlEntity.ai` (owner UUID, OPTIONAL_UNIQUE_ID) uses the same id on the same key class. No collision in practice because NPCs and player-girls are distinct classes that never both register on one instance; do not register both on a single class |

NBT (`writeEntityToNBT`/`readEntityFromNBT`): `homeX`/`homeY`/`homeZ` (double), `girlID` (string; duped-id detection removes the duplicate entity with a WARN log), `sexmod:customname` (if set), `sexmod:customModel` (if `supportsCustomModels`), `inventory` (serialized `ItemStackHandler`, 7 slots: 0 sword, 1 bow, 2-5 armor, 6 spare). Player form adds `owner` (owner UUID string). Client writes must go through `changeDataParameterFromClient` (packet) — direct client data-manager writes never reach the server.

## Pitfalls & quirks (deobfuscated code notes)

- **Lerp variant bug**: `updateAITasks` must keep `RotationHelper.lerpVec3d(pos, target, 40 - ac)` (int step) — the double variant flings the girl 40× her distance and she vanishes (class javadoc warning).
- **`onUpdate()` must never `setDead`**: the original jar has no removal there; a deobf-regression that deleted girls on benign tick exceptions made Jenny disappear permanently (documented in `BaseGirlEntity.onUpdate` javadoc).
- **`HAND_STATES` force-set to "1" every AI tick** in `AbstractGirlNpcEntity.updateAITasks` — combat hand-raise flag, do not "fix".
- **`OutfitIndex` bound check**: `getSexWorldTexture` uses `>=` (jar had `>`, OOB at index == length) and prints "Girl doesn't have an outfit Nr.X so im just making her nude lol".
- **`animation.ellie.missionary_slow`** is declared in jenny's JSON (no length) and in ellie's JSON — a stray shared asset; Jenny never plays it.
- **`fortnite`** and **`wave`/`wave_idle`** are unreferenced dead assets/actions (no code sets `Action.WAVE`).
- **Sound file gaps**: `GIRLS_JENNY_HMPH` array has 5 slots but sounds.json defines only 4 (`hmph[4]` = silent/missing); `aftersessionmoan/bjmoan/*` (13 files) are orphaned (no matching array field).
- **`setCurrentAction` camera nudge**: leaving `STARTBLOWJOB`/`PAIZURI_START` repositions the interaction player by (0,0,0.2) rotated by yaw+180.
- **Doggy hard variant**: `aa` flag set by `doggyfastReady` only while the local player jumps; `ag` counter alternates moan vs ahh on `doggyfastMSG1`; `doggyslowMSG1` resets `aa` and uses a weighted sound pick (1-in-4 → MMM/MOAN, else heavybreathing).
- **`positionPlayerRelative`** requires a bound interaction player and uses `cameraOriginPos` as the reference anchor — it must be cleared (`cameraOriginPos = null`) on every reset path or the next scene repositions from a stale point.
- **`triggerActionSync`** sends `KoboldStatePacket` client→server; the server-side handler calls `setDismounted()` — the scene entry funnel shared with Bia/Luna/Kobold.
- **Name-tag/scale oddity**: NPC `getScaleFactor()` returns -0.2F (negative — used only as a name-tag Y offset), while the player form returns 1.75F.

## Gaps

- Exact per-frame keyframe timings inside the animation JSON were not enumerated (only keyframe names used by the sound listener); the `animation.jenny.*` MSG keyframe ordering is taken from the code listener.
- `jenny.dialogue.followme/bye/bye2` have no code reference in this source tree (may be used by a chat command or legacy GUI not present in the read files).

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
