# Kobold — Full Character Reference

| Field | Value |
|---|---|
| Registry name | No `entity.kobold.name` lang key — display name comes from the `KOBOLD_NAME` data key (random `KoboldNames` entry per spawn, renamable with a vanilla name tag). Entity `kobold` (`NpcType.KOBOLD.npcID` = 5648456); player form `player_kobold` (`playerID` = 62484851). Items: `item.kobold_egg_item.name=Kobold egg`, `item.tribe_egg.name=Tribe egg`, `item.dragon_staff.name=Dragon Staff` |
| Voice actress | @FlirtyFawn696 (mcmod.info credits: "being the kobolds voice actress") |
| Entity class(es) | `KoboldEntity extends AbstractNpcOnlyEntity implements IEllie, IInventory, IKobold` (NPC); `KoboldPlayerEntity extends AbstractKoboldPlayerEntity implements IKobold` (player form); `AbstractKoboldPlayerEntity extends AbstractPlayerGirlEntity`; egg: `KoboldEggEntity extends EntityLivingBase`; pearl: `KoboldEggProjectileEntity extends EntityEnderPearl` |
| Model / Renderer | `KoboldNpcModel extends GirlModel<BaseGirlEntity>` (geckolib, DNA-driven parts) / `KoboldRenderer extends GirlRendererBase<KoboldEntity>`; player form `PlayerKoboldRenderer extends AbstractPlayerKoblinGoboldRenderer`; `KoboldModel` = vanilla 2×6×2 placeholder (`IVanillaModel`); egg `KoboldEggModel` + `KoboldEggRenderer` + `KoboldEggItemModel`/`KoboldEggItemRenderer` |
| NpcType enum | `KOBOLD(KoboldEntity.class, 5648456, KoboldPlayerEntity.class, 62484851, true)` — `hasSpecifics=true` |
| Player form | Horny-potion transformation: blowjob / anal / mating-press scenes, full appearance customization (size, body color, eye color), `IKobold` ceiling checks |
| Obtaining | Tribes spawn from the **Tribe egg** (`TribeEggItem` → `KoboldManager.spawnKoboldAt`, 4 kobolds, one leader with size 0.25) and from worldgen (`ConfigWorldGenHandler`). New members hatch from **Kobold eggs** (mating-press reward) after 12000 ticks |

## Animations (complete list)

Source: `assets/sexmod/animations/kobold/kobold.animation.json` (36) + `animations/kobold/egg.animation.json` (6, `animation.model.*`) + `animations/kobold/staff.animation.json` (1, `animation.model.null`).

| Animation name | Purpose / when used (code trigger) |
|---|---|
| `animation.kobold.null` | Empty pose: eyes controller when action != NULL; movement controller while any action runs; Action.NULL |
| `animation.kobold.blink` | Eyes controller when action == NULL (autoBlink) |
| `animation.kobold.idle` | Movement controller: standing idle (unarmed) |
| `animation.kobold.idle_armed` | Movement controller idle while `aC` (tribe combat flag) is set |
| `animation.kobold.walk` | Movement controller walk (moved ≤ 0.2, speed `1.0 + shrink*2`) |
| `animation.kobold.run` | Movement controller run (moved > 0.2, onGround) |
| `animation.kobold.run_armed` | Movement controller run while `aC` (combat) |
| `animation.kobold.crouch_idle` | Movement controller idle when `isBlockedByCeiling()` |
| `animation.kobold.crouch_walk` | Movement controller walk when `isBlockedByCeiling()` |
| `animation.kobold.fly` / `fly2` | Movement controller air-fall (NPC: `fly`; player form toggles `fly`/`fly2` every tick while airborne) |
| `animation.kobold.backwards_walk` | Player form walking backwards (`ao.y < -0.1`, speed 1.75) |
| `animation.kobold.sit` | Action RIDE / SIT (also player form `ak` riding flag) |
| `animation.kobold.sleep` | Action SLEEP (assigned bed) |
| `animation.kobold.attack` | Action ATTACK (tribe combat, 84-tick full cycle in `updateAITasks`, damage 5.0 within 2 blocks at tick 32; `haa` sound at 1.25s) |
| `animation.kobold.fall_tree` | Action MINE — woodcutting swing (loop; `haa` at 0.875s) |
| `animation.kobold.paymentBackpack` | Action PAYMENT — the payment scene before blowjob/anal for non-master customers (`paymentMSG1`, `yep`, `plob`, `giggle`, `blackScreen`, `paymentDone`; backpack bone forced visible) |
| `animation.kobold.blowjobStart` | Action STARTBLOWJOB (non-loop): teleports player in (`blowjobStartMSG1` at 0.083s → pos (0, 0.625-eyes, -1) yaw+180; `blowjobStartMSG2` at 4.167s → pos (0.5, 0.5-eyes, -0.6875) yaw+180-40), `blowjobStartbreath`, `lipsound`, `touch`, `bjmoan`, `blowjobStartDone` (11.25s) → SUCKBLOWJOB_BLINK |
| `animation.kobold.blowjobSlowL` / `blowjobSlowR` | Action SUCKBLOWJOB_BLINK loops, side = `WildSlimeFaceLayer` ? R : L; `lipsound` ×2, `switch` (2.458s) → random `aT` → ...Switch variant |
| `animation.kobold.blowjobSlowLSwitch` / `blowjobSlowRSwitch` | Same loop with switch pose (`lipsound`, `touch`, `endSwitch` → flip side) |
| `animation.kobold.blowjobFast` | Action THRUSTBLOWJOB (sneak from slow): `lipsound`, `bjmoan`, `blowjobFastDone` → back to SUCKBLOWJOB_BLINK unless jumping |
| `animation.kobold.blowjobCum` | Action CUMBLOWJOB: `lipsound`, `cumLoud` (smallinserts @3), `bjmoan`, `cumQuiet`, `blackScreen`, `blowjobCumDone` → reset + hide horny meter |
| `animation.kobold.analStart` | Action KOBOLD_ANAL_START (non-loop): `analStartCam` (player reposition), `giggle`, `pounding`, `moan`, `analStartDone` → KOBOLD_ANAL_SLOW |
| `animation.kobold.analSoft` | Action KOBOLD_ANAL_SLOW loop: `analSoft` (+0.02 horny), `pounding`, `breath`, `analFastRapid` (jump → FAST or reset offset) |
| `animation.kobold.analHard` | Action KOBOLD_ANAL_FAST loop: `analHard` (+0.04), `pounding`, `analHardMSG1` (moan, every 4th), `analFastRapid`, `analDone` → back to SLOW |
| `animation.kobold.analCum` | Action KOBOLD_ANAL_CUM: `pounding`, `cum` (smallinserts @3), `orgasm`, `analFastRapid`, `analDone`, `blackScreen`, `breath`, `analCumDone` → reset |
| `animation.kobold.mating_press_start` | Action MATING_PRESS_START (non-loop): `matingCam` (player reposition yaw+180 pitch 10), `breath`, `pounding`, `orgasm`, `mating_press_startDone` → MATING_PRESS_SOFT |
| `animation.kobold.mating_press_soft` | Action MATING_PRESS_SOFT loop: `pounding`, `breath`, `mating_press_softReady` (+0.04 horny; jump → HARD) |
| `animation.kobold.mating_press_hard` | Action MATING_PRESS_HARD loop: `pounding`, `moanMating` (every 3rd), `mating_press_hardReady` (jump resets offset), `mating_press_hardDone` → SOFT |
| `animation.kobold.mating_press_cum` | Action MATING_PRESS_CUM: `mating_cum_cam` (player reposition pitch 70), `pounding`, `orgasm`, `cumLoud`, **`renderEgg` (3.708s → `isRenderEgg=true` + plob)**, `cumMsg` ("I.. hope I am satisfying you sir" + sad), `blackScreen`, `mating_press_cumDone` → reset |
| `animation.model.null` / `slow` / `medium` / `fast` / `veryfast` / `hatch` | Egg wiggle escalation: no anim < 50% age, `slow` > 50%, `medium` > 75%, `fast` > 85%, `veryfast` > 98%, `hatch` in the final 20 ticks (`KoboldEggEntity.animationPredicate`, progress = EGG_TYPE/12000) |
| `animation.model.null` (staff) | Dragon Staff idle (1 anim in staff.animation.json) |

## Scenes & Actions

Action enum entries: `PAYMENT`, `STARTBLOWJOB`, `SUCKBLOWJOB_BLINK`, `THRUSTBLOWJOB`, `CUMBLOWJOB`, `KOBOLD_ANAL_START/SLOW/FAST/CUM` (pitch range 4.0/-80, `useBoyCam=true`), `MATING_PRESS_START/SOFT/HARD/CUM` (pitch -50/-90, `useBoyCam=false`), `MINE`, `SLEEP`, `ATTACK`, `RIDE`/`SIT`, `THROW_PEARL` (kobold pearl).

**Interaction / payment gate** (`processInteract` + `openInteractionMenu`):
- Name-tag rename (master only, consumes tag, sets `KOBOLD_NAME`).
- Master with **Dragon Staff** in hand: right-click opens GUI id 1 (tribe management); without master, staff click opens `TribeNameScreen`.
- Menu: master → `["anal", "oral", "mating"]`; horny-potion (non-master) → `["anal", "oral"]`; otherwise → `["anal", "oral"]` with **payment display** `[3× gold ingot, 1× iron pickaxe]` (GirlInventoryScreen requirement items; payment is a cosmetic gate — `doAction` routes through `Action.PAYMENT` first: `U()` only starts the scene once `PAYMENT` has played, i.e. the client "pays" by clicking through).
- `doAction`: `oral` → GIRL_HAND_STATES = STARTBLOWJOB; `anal` → KOBOLD_ANAL_START; `mating` → MATING_PRESS_START; then `triggerActionSync(true, uuid)` (KoboldStatePacket, movement unlocked after).
- Scene entry: 40-tick lerp to TARGET_POS (`isSitting()`), then `U()` picks the scene from `GIRL_HAND_STATES` (skipping PAYMENT when master/horny-potion).

**Blowjob**: PAYMENT → STARTBLOWJOB (`blowjobStart` teleports player twice) → `blowjobStartDone` → SUCKBLOWJOB_BLINK (L/R + Switch variants, side flips on `endSwitch`, random switch on `switch`) → sneak → THRUSTBLOWJOB (`blowjobFast`, jump holds fast) → jump+full meter → CUMBLOWJOB → `blowjobCumDone` → `resetCameraAndPhysics()` + hide horny meter.

**Anal**: KOBOLD_ANAL_START (`analStartCam` repositions player) → `analStartDone` → KOBOLD_ANAL_SLOW (`analSoft`) ↔ sneak → KOBOLD_ANAL_FAST (`analHard`; `analFastRapid` on jump: FAST→reset offset, SLOW→FAST; `analDone` returns to SLOW) → jump+full meter → KOBOLD_ANAL_CUM (`analCum`) → `analCumDone` → reset.

**Mating press**: MATING_PRESS_START (`matingCam` repositions player to yaw+180, pitch 10) → `mating_press_startDone` → MATING_PRESS_SOFT (`mating_press_soft`, jump → HARD) → MATING_PRESS_HARD (`mating_press_hard`, jump resets offset, `mating_press_hardDone` → SOFT) → jump+full meter → MATING_PRESS_CUM → `renderEgg` shows the egg bone + plob → `cumMsg` ("I.. hope I am satisfying you sir" + sad sound) → `mating_press_cumDone` → reset; **server arms `actionCooldown=0`**; after 132 ticks, if still in MATING_PRESS_CUM, the player receives `KoboldEggItem` (metadata = tribe wool color, NBT `tribeID` + `tribeColor`).

**Payment scene**: PAYMENT (`paymentBackpack`) → `paymentMSG1` ("I'd like to use ur services owo" + plob), `interested`, `yep`, `plob`, `giggle`, `blackScreen`, `paymentDone` → `U()` re-dispatches the stored scene.

## Dialogue (all lang lines)

Kobold has **no `kobold.dialogue.*` lang keys**. All lines are hardcoded `sendChatMessage` / `sendGirlChatMessage` / `sendChatMessageToPlayer` calls in `KoboldEntity` (+ egg/hatch messages):

| Text (hardcoded) | When spoken (code) |
|---|---|
| "I'd like to use ur services owo" | `paymentMSG1` keyframe (payment scene, to interaction player) |
| "Hey master!" | `handleSleepState`: master within 2 blocks, idle, not alerted — throttled by static `aV` (300 ticks), + `GIRLS_KOBOLD_HEYMASTER` random |
| "okay resting time owo" | Tribe state → REST (night, `getTribeStateForTime` time ≥ 12000) |
| "Time to work bitches!" | Tribe member arrives at home (within 5 blocks) during ACTIVE → tasks start |
| "Lets go somewhere else" | ACTIVE: all tribe tasks finished |
| "Someone, go fall this tree!" | `findConnectedLogs` finds an unclaimed tree near home |
| "Ima fall this tree owo" | FALL_TREE task assigned |
| "Ima go mine uwu" | MINE task assigned (+ `syncTribeBlocks` teleport to target) |
| "<name> It's impossible to mine here..." | MINE task with no reachable targets (to master) |
| "<name>s inventory is full and there are either no chests..." | `canStoreInventory` failed, 300-tick reminder (status message, tribe color) |
| "Your kobolds cannot fall this tree because it starts underground" | `mineBlockAt`: no exposed neighbor below the log |
| "Your Tribe is under Attack!" | `LivingHurtEvent` on a tribe member (status message, red) |
| "%s%s%s has perished %suwu" | `onDeath` (master chat) |
| "ur %stribe %shas been %seradicated %suwu" | `KoboldManager.setTribeLeader` when the last member dies |
| "I.. hope I am satisfying you sir" | `cumMsg` keyframe (mating press cum) + `GIRLS_KOBOLD_SAD` |
| "%s%s %shas become a %snew tribe member%s!" | Egg hatch announcement to master (+ arrow-hit + firework-twinkle sounds) |
| (17 "insult" strings, incl. navy-seals copypasta, "suck my iron cock you worthless piece of shit!", "Ligma titties!", ...) | Defined in field `an[]` — dead data; no usage found inside KoboldEntity |

## Sounds

Sound event prefixes `girls.kobold.<folder>.<fileN>` (sounds.json) ↔ `SoundHandler.GIRLS_KOBOLD_*`:

| Folder | Files | Array | Used by |
|---|---|---|---|
| `bjmoan` | 0–9 (10) | GIRLS_KOBOLD_BJMOAN[10] | `bjmoan` keyframe |
| `giggle` | 0–3 (4) | GIRLS_KOBOLD_GIGGLE[4] | `giggle` keyframe |
| `haa` | 0–6 (7) | GIRLS_KOBOLD_HAA[7] | `haa` keyframe (attack / fall_tree, @0.7) |
| `heymaster` | 0–5 (6) | GIRLS_KOBOLD_HEYMASTER[6] | "Hey master!" greeting |
| `interested` | 0–2 (3) | GIRLS_KOBOLD_INTERESTED[3] | `interested` keyframe (payment) |
| `lightbreathing` | 0–10 (11 in json; code array = 12) | GIRLS_KOBOLD_LIGHTBREATHING[12] | `breath` (@0.5), `blowjobStartbreath` |
| `master` | 0–5 (6) | GIRLS_KOBOLD_MASTER[6] | `processInteract` greeting for master |
| `moan` | 0–10 (11) | GIRLS_KOBOLD_MOAN[10] | `moan`/`moanMating`/`analHardMSG1` (rate-limited) |
| `orgasm` | 0–3 (4) | GIRLS_KOBOLD_ORGASM[4] | `orgasm` keyframe (cum scenes) |
| `sad` | 0–2 (3) | GIRLS_KOBOLD_SAD[3] | `cumMsg` |
| `yep` | 0–6 (7) | GIRLS_KOBOLD_YEP[7] | `yep` keyframe (payment) |

Reused from other girls: `GIRLS_ALLIE_LIPSOUND`/`GIRLS_JENNY_LIPSOUND` (50/50) for `lipsound`. MISC events: `MISC_PLOB`, `MISC_POUNDING`, `MISC_SMALLINSERTS` (@3.0), `MISC_TOUCH`. **Size pitch**: `playSoundAtVolume` scales pitch `lerp(0.9, 1.1, (0.25-aE)/0.25)` — smaller kobolds squeak higher. Folder layout `sounds/girls/kobold/<folder>/`.

## Model & Appearance

- `KoboldNpcModel.getModelLocations()`: `[geo/kobold/kobold.geo.json, geo/kobold/armored.geo.json]` (nude / armored); texture `textures/entity/kobold/kobold.png`; animation file `animations/kobold/kobold.animation.json`.
- **DNA-driven parts** (`setLivingAnimations`, model code from `APPEARANCE_DNA`, dash-separated; getModelPartByIndex texture coords 140–227×0–40):
  - `[0]` horn-up variant → `hornUL<n>/hornUR<n>` bones (n = variant);
  - `[1]` horn-down variant → `hornDL<n>/hornDR<n>`;
  - `[2]` boob scale (0.75–1.35) → `boobL`, `boobR`, `armorBoobs`;
  - `[3]` eye scale (1.0–1.2) + eye spacing (positionX shift) → `eyeL`, `eyeR`;
  - `[4]` freckle-arm variant (1/2) → `frecklesAR1/2`, `frecklesAL1/2`;
  - `[5]` freckle-head variant (1/2) → `frecklesHR1/2`, `frecklesHL1/2`;
  - `[6]` backpack/tailpack pose (0–3: backpack only / both / tailpack only / none) → `backpack`, `tailpack` (forced visible during PAYMENT);
  - `[7]` mouth/eye-alpha gene: value 1 → `mouth` alpha −0.078125 (PlayerKoboldRenderer.calculateBoneArmorColor);
  - size gene feeds `GeckoLibCache.parser.setValue("size", shrink)` (model-wide scale).
- Special bones: `crown` (visible only for tribe leader, key `aZ`), `egg` (visible only while `isRenderEgg`), `tounge` (typo in code — shown during STARTBLOWJOB/SUCKBLOWJOB_BLINK/THRUSTBLOWJOB/CUMBLOWJOB, hidden otherwise).
- Scene body positioning while the action controller transitions (`handleSwingAnimation`, interpolated from `0.25 - aE`): blowjob → body Z `11.43 - 7*t`; anal → body X `1.78 - 1.5*t`, Y `13.07 - 11*t`, Z `2.05 - 8*t`; mating-press → raised.
- `KoboldModel`: vanilla 2×6×2 cube placeholder (IVanillaModel, NPC vanilla path + player-form hand model with `textures/entity/kobold/hand.png`).
- Egg: `KoboldEggModel` + `KoboldEggRenderer` (geo `geo/kobold/koboldegg.geo.json`, anim `animations/kobold/egg.animation.json`), `KoboldEggItemModel`/`KoboldEggItemRenderer` (TESR), `geo/kobold/staff.geo.json` for the Dragon Staff.

## AI & Behavior

- Goals (`initEntityAI`): `EntityAISwimming` (0), `EntityAITempt(0.4, TEMPTATION_ITEMS = emerald/diamond/gold ingot/ender pearl)` (2), `DoorInteractAiGoal` (3), `WatchClosestGirlGoal(3.0F)` (5). Most movement is custom per-tick navigation (wander `handleNearbyPlayer` with `getWanderRange = √800`, home/task paths at speed 0.35, combat 0.7).
- Attributes: MAX_HEALTH = **static `af`** (default 69.0; `createKoboldWithSpeed` sets `af = 10.0 - throwDelay*25.0`, throwDelay ∈ [0, 0.25] → 3.75–10 — a global shared across all kobolds), MOVEMENT_SPEED 0.5, FOLLOW_RANGE 30.0. Jump 0.45.
- Heal: out of combat (not `aC`), every 100 ticks +2 HP + HEART particle packet.
- **Ceiling checks** (`IKobold.isBlockedByCeiling`): block above head not passable → crouch animations; `LivingHurtEvent` cancels `DamageSource.IN_WALL` damage and teleports the kobold up 1 block.
- **Tribe combat**: `handleTribeCombat` — the leader scans a 30-block AABB for kobolds of other/no tribes (visible, not both master-owned), nearest target within 30 blocks; non-leaders just hold (`aC=true`); leader paths at 0.7 and at ≤1.5 blocks starts Action.ATTACK (`aP=84`); attack ticks: 22 → `onTickEmpty` (no-op), 32 → 5.0 damage to targets within 2 blocks, 84 → NULL. Being attacked by a player (non-creative, non-master) or mob → `addCombatant` + "Your Tribe is under Attack!". `NearestAttackableGirlGoal` added to zombies/skeletons/spiders on join.
- **Tribe day/night**: `TribeState` REST (worldTime ≥ 12000) / ACTIVE; REST → sleep in assigned beds (`handleBedRequest`: occupied/unoccupied checks, `assignBed` head-half detection; players are blocked from sleeping in assigned beds via `PlayerSleepInBedEvent → SleepResult.OTHER_PROBLEM`), or claim a home (netherrack + `SexFireBlock` campfire) when none exists; ACTIVE → teleport home, work tasks.
- Follow: master presence (`handleMasterPresence`) — alerted tribe follows the master at `getKickDistance` (0.7 sprint / 0.35 walk + 0.3 per 5 blocks, ×60 in water), teleport-dismount past 15 blocks.
- Interaction heads-up: `handleKoboldOwner` pitches the head toward the interacting player's eye level.
- Static `KoboldEntity.c`: death drops inventory; hurt → tribe alert; world unload → `teleportToHome`; `IN_WALL` cancel; client every 20 ticks sends `GetTribeUiValuesPacket` (tribe HUD).
- Egg: 12000-tick hatch (`spawnHatchExplosion`, 30 explosion particles), hatch → kobold joined to tribe, tribe name copied, master notified; damage kills the egg (`attackEntityFrom` → `setDead`); `canTrample = false`.
- Kobold pearl (`KoboldEggProjectileEntity`): ender-pearl subclass; teleports the thrower (a `BaseGirlEntity`) only within 5 blocks of `homePos`; end-gateway support; `a.onEnderTeleport` clears `activeEnderPearl`, resets action/anchor, `goHome()`.

## Unique mechanics

- **Tribe system** (`KoboldManager`): `Tribe` = {masterPlayerUUID, leaderKobold, members, tribeColor (`EyeAndKoboldColor`), state, tribeHome, tasks, combatants, tribeBeds, tribeChests, savedPositions `k` (member girl-id → pos), followModeEnabled}. Leader = the *fastest* member (`getLeaderKobold` compares `aE`, smaller = faster). Member color forced to tribe color unless `editedColorManually` (`aA`). Tribe UUID stored in data key `aL` (129). Persisted by `TribeWorldSavedData` under world data key **"tribes"** with string-indexed NBT (`tribeId<i>`, `tribeColor<i>`, `tribeMaster<i>`, `<uuid>member<i>pos|id`, `<uuid>bed<i>`, `<uuid>chest<i>`, `<uuid><i>taskKind|facing|pos|block<j>`); `readNBTValue` **empties keys as it reads** (single-pass consumption).
- **Eggs**: `KoboldEggItem` (metadata = wool color → `EyeAndKoboldColor.getColorByWoolId`; NBT `tribeID`); placed on right-click block → `KoboldEggEntity` (data keys `EGG_COLOR` 115, `EGG_TYPE` 116); hatches into a tribe member. Mating-press cum grants one to the player after a 132-tick cooldown (`handleActionCooldown`).
- **DNA codes**: `APPEARANCE_DNA` (121) — dash-separated zero-padded numbers; kobold layout `[8,3,gene,gene,2,2,1,1]` + size/color/eye in keys 122/119/120; goblin layout `[3,2,2,8,8,5,<hair>,<skin>,<eye>,<partIdx>]`. Persisted wardrobe: player NBT `"sexmod:GirlSpecific" + NpcType`.
- **Mining/woodcutting tasks** (`KoboldTask`): TaskType FALL_TREE (1 worker) / MINE (3 workers); `findConnectedBlocks` flood-fills logs (horizontal + 1 up), registers the task; `executeMiningTask` picks the front row/column per facing; liquids → cobblestone scaffolding; air columns re-targeted; `mineBlockAt` needs an exposed neighbor (else "starts underground" status); block break → item into 27-slot inventory (drop as EntityItem if full), **sapling replanting** (`getBlockItem` maps log id/meta 17:1-3, 162:0-1 → SAPLING 1-5, default oak); `startMiningTask`/`fall_tree` anim chops the trunk top-down spawning `EntityFallingBlock`s (fallTime=1) for the upper logs; completed/removed tasks re-send `SendBlocksPacket` markers to the master; workers teleport with portal particles (`syncTribeBlocks`) when out of reach; chest deposit (`isTribeChestOpen` → insert, chest-locked sound).
- **Beds**: kobolds claim unoccupied, unassigned beds (assigned = `KoboldManager.a` map kobold → [foot, head]); sleeping players blocked.
- **Home**: ACTIVE tribe without a home claims a random spot 50–100 blocks away (standable), or builds a netherrack+fire camp if it is already a member near its home.
- **Global max-health hack**: `KoboldEntity.af` is `static` — every kobold in the world shares one MAX_HEALTH value.

## Player form (if any)

`KoboldPlayerEntity` (horny potion, `IKobold`):
- Size key `aA` (122, 0..0.25; part-id 0 = `id/100*0.25`), body color `as` (119), eye color `au` (120 BlockPos), DNA `at` (121); `getCustomPartIdList` = [101, colors, colors, 8, 3, 101, 101, 3, 3, 4, 2]. Render scale `1.4 - (0.25-aA)`; model matrix `scale(1-shrink)` (`applyAdditionalMatrixTransformations`) + camera pivot `y * (1+shrink)` (`transformCameraPivotY`).
- Hand: `AllieModel` + `textures/entity/kobold/hand.png`, hand tint = body color main RGB (`getHandColor`).
- Scenes: `handleOwnerCommand` `anal` → KOBOLD_ANAL_START, `oral` → STARTBLOWJOB, `mating` → MATING_PRESS_START (all teleport + broadcast + strip). Menu `["anal","oral","mating"]`. Identical sound-listener transitions to the NPC.
- Movement anims: `fly`/`fly2` toggle (`aB`), `run` (1.2 speed), `walk` (2.0), `backwards_walk` (1.75), `sit` when riding (`ak`), `idle`; movement controller transition 3 ticks.
- `isBlockedByCeiling` same head-space check; collision box 0.3×0.9.
- No tribe AI, no inventory, no eggs, no payment.

## Data keys / NBT

Kobold-specific data-manager keys (explicit ids):
- `aE` (122) = size scalar float 0..0.25 (throw delay; leader 0.25); `KOBOLD_NAME` (123); `aC` (124) = tribe-combat flag; `aZ` (125) = tribe leader (crown); `aU` (126) = tribe name (empty for non-queen); `ak` (127) = tribe alerted flag; `at` (128) = mining-task flag (MINE action set); `aL` (129) = `Optional<UUID>` tribe id.
- Inherited NPC/player twins: `CURRENT_ACTION` (119) = **body color name** (`EyeAndKoboldColor`, repurposed), `ACTION_TARGET_POS` (120) = **eye color** packed as BlockPos(RGB), `APPEARANCE_DNA` (121) = DNA string; BaseGirlEntity 99–110 as in allie.md.
- NBT (`writeEntityToNBT`): `body_size`, `eyeColorX/Y/Z`, `model`, `name`, `master`, `inventory` (27-slot ItemStackHandler), `bodyColor`, `editedColorManually`, `tribeId` (UUID), `isLeader`, `tribeName`.
- Egg NBT: `tribeID`, `egg_color`, `eggAge`. Egg item NBT: `tribeID`, `tribeColor`.

## Pitfalls & quirks (deobfuscated code notes)

- **`KoboldManager.isTribeMember` returns `leaderKobold.getEntityId() == kobold.getEntityId()`** — only the current leader entity counts as "member"; the name is misleading.
- `getTribeMemberCount` returns `Tribe.getTribeId()` = **distinct girl-UUID count** (members + saved positions), not the member-list size — it gates the bed-count logic.
- The deobf has explicit jar-verified notes: bed/chest getters were once crossed (`getTribeBeds` returning chests and vice versa) and `writeToNBT` bed/chest keys were crossed — fixed to match the original jar ("bed" keys = `addTribeBed` set, "chest" keys = `addTribeChest` set).
- `findConnectedBlocks` originally had `startPos.down()` in the ground-walk loop (never advances → infinite loop on floating logs); the deobf uses `groundPos = groundPos.down()` per the jar-faithful intent comment.
- `KoboldEntity.af` (max health) is **static and shared world-wide** — spawning any kobold changes every kobold's max health.
- The 17-string `an[]` insult list (navy-seals copypasta etc.) is dead data — no in-class references.
- `mating_press_startDone` case falls through into `mating_press_hardDone` (missing `break` in the switch — intentional in jar: `mating_press_startDone` shows the meter, then `mating_press_hardDone` sets SOFT).
- `getScaleFactor() = 0.2 - (0.25 - aE)` — the NPC is tiny (0.2-ish) while the player form is `1.4 - shrink`.
- `isTribeMember`-style naming aside, the tribe leader is recomputed on death (`onDeath` sets itself as leader → next `getLeaderKobold()` picks the fastest survivor).
- Movement controller transition is 10 ticks for the NPC, 3 for the player form (registerControllers).
- `KoboldPlayerEntity.getCustomPartIdList` part 0 is 101 (size slider max); part ids 1/2 are the color enum lengths — the wardrobe screen maps indices 0/1/2 to size/body/eye.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
