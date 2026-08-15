# Goblin — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.goblin.name=Goblin` (en_us.lang). Entity `goblin` (`NpcType.GOBLIN.npcID` = 4567275, registered with **spawn egg colors 39424/19456**); player form `player_goblin` (`playerID` = 6584344). Command `/locatenearestgoblinlair` |
| Voice actress | Not credited in mcmod.info (credits list Jenny, Ellie, Bia, Luna, Kobold only) |
| Entity class(es) | `GoblinEntity extends AbstractNpcOnlyEntity implements IGoblin` (NPC); `GoblinPlayerEntity extends AbstractKoboldPlayerEntity implements IGoblin` (player form); `IGoblin` = carry/throw contract |
| Model / Renderer | `GoblinNpcModel extends GirlModel<BaseGirlEntity>` / `GoblinRenderer extends GirlRendererBase<GoblinEntity>`; player form `PlayerGoblinRenderer extends AbstractPlayerKoblinGoboldRenderer`; `GoblinFirstPersonRenderer` (client, first-person + hand/body hiding); `GoblinModel` = vanilla 2×6×2 placeholder (`IVanillaModel`) |
| NpcType enum | `GOBLIN(GoblinEntity.class, 4567275, GoblinPlayerEntity.class, 6584344, true)` — `hasSpecifics=true` |
| Player form | Horny-potion transformation that can itself be picked up / thrown by other players (owner binding, fake-player rendering on the carrier's head), plus nelson / paizuri / breeding scenes |
| Obtaining | Worldgen goblin lair (`GirlHouseGenerator("goblin")` + throne spawn, see `ConfigWorldGenHandler`); spawn egg; right-click any non-queen goblin to pick up (tame). Queens are found sitting on their throne in the lair |

## Animations (complete list)

Source: `assets/sexmod/animations/goblin/goblin.animation.json` (56). All action-animation names below are as in the JSON; several actions resolve to shared names with a suffix (e.g. `catch_` + `1person`/`3person` chosen at runtime from camera mode, `breeding_slow_1` + `l`/`r`).

| Animation name | Purpose / when used (code trigger) |
|---|---|
| `animation.goblin.null` | Empty pose: eyes controller while any action runs; movement controller while an action runs; Action.NULL |
| `animation.goblin.blink` | Eyes controller when action == NULL (autoBlink) |
| `animation.goblin.idle` | Movement controller idle |
| `animation.goblin.walk` | Movement controller walk (moved > 0, ground; both branches play `walk`) |
| `animation.goblin.running` | Action RUN on the ground (steal/gold run, speed 0.825) |
| `animation.goblin.fly` / `fly2` | Airborne fall (movement controller); RUN in air (player form toggles fly/fly2) |
| `animation.goblin.backwards_walk` | Player form walking backwards (speed 1.5) |
| `animation.goblin.sit` | Action SIT — queen sitting on her throne (also `ak` riding flag in player form) |
| `animation.goblin.pick_up_1person` / `pick_up_3person` | Action PICK_UP — picked-up pose; `1person` when the local player is the owner in first person, `3person` otherwise (`camMode` string, same for all `%sperson` names) |
| `animation.goblin.shoulder_idle` | Action SHOULDER_IDLE — perched on the owner's shoulder (noClip, glued to owner pos every tick) |
| `animation.goblin.throw_1person` / `throw_3person` | Action START_THROWING — wind-up before launch; server returns the stolen item (`L_clash281`) and rebinds the interaction player to the owner |
| `animation.goblin.thrown` | Action THROWN — ballistic pose in flight/landed; `fall()` is suppressed while THROWN/START_THROWING |
| `animation.goblin.stand_up` | Action STAND_UP (non-loop) — after landing (30 grounded throw-ticks) or 37 ticks of standing; → NULL |
| `animation.goblin.await_pick_up` | Actions AWAIT_PICK_UP and VANISH — breeding reward guards kneeling on the throne platform; VANISH also fades `ar` 0.05/tick then removes the entity |
| `animation.goblin.catch_1person` / `catch_3person` | Action CATCH — thief caught by the player: dialogue keyframes `catchEh` (1.917s), `catchAkward` (2.917s), `catchWell` (5.042s), `catchRather` (6.25s), `catchDone` (8.5s) → CATCH_BJ when `GIRL_HAND_STATES == "bj"` |
| `animation.goblin.catch_1personBj` / `catch_3personBj` | Action CATCH_BJ — blowjob attempt: `catchMe` (0.042s), `catchBjDone` (1.417s) → CATCH_BJ_IDLE |
| `animation.goblin.catch_1personBj_idle` / `catch_3personBj_idle` | Action CATCH_BJ_IDLE — holding the player, opens the menu `["use her", "take ur stuff back"]` |
| `animation.goblin.paizuri_start` | Action PAIZURI_START: `paizruiCam` (player pitch 70), `paizuriChoice` ("good choice!~"), `paizuriBoth` ("...for both of us!"), `touch`, `paizruiUse` ("now use me like a fuck toy!~"), `paizuri_startDone` (4.917s) → PAIZURI_IDLE |
| `animation.goblin.paizuri_idle` | Action PAIZURI_IDLE loop |
| `animation.goblin.paizuri_slow` / `paizuri_slow2` | Action PAIZURI_SLOW loop variants (suffix toggled by `aP` on `paizuriSwitch`): `smallPound` (×2, +0.02 horny), `paizuriSwitch` |
| `animation.goblin.paizuri_fast` | Action PAIZURI_FAST (sneak): `pound` (+0.04), `paizuriFastReady` (jump → FAST_CONTINUES), `paizuriFastDone` → SLOW |
| `animation.goblin.paizuri_fast_countinues` | Action PAIZURI_FAST_CONTINUES (jump on ready): `pound`, `paizuriFastContinuesReady` (jump resets offset), `paizuriFastDone` → SLOW |
| `animation.goblin.paizuri_cum` | Action PAIZURI_CUM: `pound`, `cumSound` (smallinserts @3), `blackScreen`, `paizuriCumDone` → NULL → server `D_clash278()` teardown (reset player, drop stolen item, untamed → despawn) |
| `animation.goblin.jump_1` / `jump_2` / `jump_3` | Actions JUMP_0 / JUMP_1 / JUMP_2 — queen + two guards leaping at the breeding target; `jumpCam` (0.042s, owner cam), `blackScreen` (0.333s) |
| `animation.goblin.breeding_intro_1` | Action BREEDING_INTRO_0 (queen): `breedingHmm`, `breedingFound`, `breedingEnough`, `breedingCam2`, `pound`, `breedingIntroDone` (12.2s) → BREEDING_SLOW_0 |
| `animation.goblin.breeding_intro_2` | Action BREEDING_INTRO_1 (guard 1): `breeding_intro_2Done` (12.167s) → BREEDING_1 |
| `animation.goblin.breeding_intro_3` | Action BREEDING_INTRO_2 (guard 2): `breeding_intro_3Done` (12.0s) → BREEDING_SLOW_2 |
| `animation.goblin.breeding_slow_1l` / `breeding_slow_1r` | Action BREEDING_SLOW_0 (queen, left/right pose by `aD`): `pound`, `breeding_slow1Done` (jump → FAST_0) |
| `animation.goblin.breeding_fast_1s` / `breeding_fast_1c` | Action BREEDING_FAST_0 (queen, `ay` pose): `pound`, `breeding_fast1Ready` (jump → `ay=true` + offset reset), `breeding_fast1Done` → SLOW_0 |
| `animation.goblin.breeding_cum_1` | Action BREEDING_CUM_0 (queen): `breeding_cumCam`, `pound`, `cum` ×2, `breeding_fast1Ready`/`breeding_fast1Done`, `blackScreen` (3.708s); server sets pregnant `aV` + `av` + `throwCooldown=0` |
| `animation.goblin.breeding_2` | Action BREEDING_1 (guard 1 mid scene) |
| `animation.goblin.breeding_cum_2` | Action BREEDING_CUM_1 (guard 1 cum) |
| `animation.goblin.breeding_slow_3` | Action BREEDING_SLOW_2 (guard 2): `touch` ×2, `breeding_3_wiggle` (random offset reset) |
| `animation.goblin.breeding_fast_3` | Action BREEDING_FAST_2 (guard 2, jump): `breeding_fast_3Done` → SLOW_2 unless jumping |
| `animation.goblin.breeding_cum_3` | Action BREEDING_CUM_2 (guard 2 cum) |
| `animation.goblin.nelson_intro` | Action NELSON_INTRO: `pound`, `neslon_introDone` (1.917s) → NELSON_SLOW |
| `animation.goblin.nelson_slow` / `nelson_slow2` | Action NELSON_SLOW loop variants by `aF` (random toggle on `nelson_slowDone`): `pound`, `nelson_slowDone` |
| `animation.goblin.nelson_fasts` / `nelson_fastc` | Action NELSON_FAST variants by `nelsonAltPose` (jump on `neslon_fastSwitch` sets it; `neslon_fastBackSwitch` resets offset): `pound`, `nelsonFastDone` → SLOW |
| `animation.goblin.nelson_cum` | Action NELSON_CUM: `pound`, `cum`, `blackScreen`, `nelson_cumDone` → reset + NULL; server toggles pregnancy `aV` on/off |
| `animation.goblin.attack0/1/2` | Player-form melee (Action.ATTACK, `nextAttack`): `attackSound`, `attackDone` |
| `animation.goblin.bowcharge` | Player-form bow draw (Action.BOW) |

## Scenes & Actions

**Pick up / shoulder ride**. Right-click a non-queen, non-RUN goblin: if you already carry one → status "you are already carrying a Goblin". Else `processInteract` sets owner UUID, `Action.PICK_UP`, `aQ = 45`, un-anchors, `aC=true` (tamed), clears path. `handlePickUpState` glues her to the owner's position every tick (falls back to NULL when the owner vanishes or > 10 blocks) and at countdown 0 → `SHOULDER_IDLE` (noClip). While SHOULDER_IDLE she snaps to the owner each tick; **G-key** (`ClientProxy.keyBindings[0]`, keycode 34 "Interact with your goblin") opens `GalathScreen` when a shouldered goblin is owned by the local player (ignored while a GalathScreen is open).

**Catch & throw**. A gold-thief goblin runs at you (Action.RUN, 0.825 speed, `J_clash267` flee pathing away from the nearest player). Right-click while RUN: > 3.5 blocks → "get a bit closer..."; else → `Action.CATCH` + `GIRL_HAND_STATES="bj"` + owner bind. CATCH plays the 4-line dialogue; `catchDone` → `CATCH_BJ`; `catchBjDone` → `CATCH_BJ_IDLE` + menu `["use her", "take ur stuff back"]`. `doAction`: "take ur stuff back" → `START_THROWING` (server returns the stolen item to the owner's inventory via `L_clash281`); "use her" → `setThrowTarget(uuid)` (15-tick `aY` timer → `PAIZURI_START`). Throw flight (`updateThrowProgress`): progress 0 → armed on `START_THROWING`; tick 15 → launched from the owner's eye with owner pitch/yaw (`getGoblinThrowPos/Height/Distance`, velocity (0,0,1.5) rotated, yaw = owner yawHead, distance = `rotationPitch`); tick 39 → `THROWN`, owner + interaction unbound; landed 30 ticks → `STAND_UP` → NULL. Untamed thrown goblins fade (`ar` 0.05/tick) and despawn once grounded/in water.

**Paizuri** (menu "use her" / player-form owner command `paizuri`): PAIZURI_START (server `handlePlayerInteract`: player repositioned y−0.5, pitch 70, noClip) → `paizuri_startDone` → PAIZURI_IDLE → sneak → PAIZURI_SLOW (`slow`/`slow2`) ↔ PAIZURI_FAST (`fast`) / jump → PAIZURI_FAST_CONTINUES (`fast_countinues`) → jump+full meter → PAIZURI_CUM → `paizuriCumDone` → NULL + `D_clash278()` (ResetGirlPacket, drop stolen item, un-anchor, untamed → teleport home + remove).

**Nelson** (player-form owner command `anal`): NELSON_INTRO (server `handlePlayerLook`: player pulled to z−1, noClip; client forces thirdPersonView 2) → `neslon_introDone` → NELSON_SLOW (`slow`/`slow2`, random variant toggle) ↔ NELSON_FAST (`fasts`/`fastc` via jump) → NELSON_CUM (pregnancy flag `aV`/`aA` toggled) → `nelson_cumDone` → reset + NULL.

**Breeding (queen)**. Queen sits SIT on her throne (`al` pos, `ac` rotation). `E_clash260` scans the 11×6×11 throne-zone AABB (offset per throne rotation: `aB/af/ao/au/aM/THROW_OFFSET_U/THROW_OFFSET_W`) for a grounded player; not pregnant → bind player, `JUMP_0` (queen) + `JUMP_1`/`JUMP_2` (guards), `SetPlayerMovementPacket(false)` lock. `handleJumpThrow` (26 ticks into JUMP_0) throws queen + guards onto the player (position offsets per rotation) → BREEDING_INTRO_0/1/2. Queen: intro → BREEDING_SLOW_0 (`slow_1l/r`) ↔ BREEDING_FAST_0 (`fast_1s/c`) → BREEDING_CUM_0 (pregnant `aV=true`, `av` = impregnation tick, `throwCooldown=0`). Guard 1: intro → BREEDING_1 → BREEDING_CUM_1. Guard 2: intro → BREEDING_SLOW_2 (`slow_3`) ↔ BREEDING_FAST_2 (`fast_3`) → BREEDING_CUM_2. Pregnancy lasts 8400 ticks (`handleHeldThrow` clears `aV`); while pregnant the queen refuses breeding with "The Queen is still pregnant - so no breeding for you uwu" (1200-tick cooldown `ai`). `handleThrowCooldown` (100 ticks after cum) releases both guards as AWAIT_PICK_UP rewards at fixed throne-relative positions, restores player physics + yaw 180/pitch 30, chat "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~". `handleHoldCooldown` (205 ticks) repositions the player for the cum shot. After breeding ends, if any guard is being carried, queen says "Farewell my knight. You are welcome once I am breedable again." and unbound guards VANISH.

## Dialogue (all lang lines)

No `goblin.dialogue.*` lang keys and **no `girls.goblin.*` sound events** — all dialogue is hardcoded chat (`sendChatMessage`) + `MISC_PLOB` plop sounds:

| Text (hardcoded) | When spoken (code) |
|---|---|
| "get a bit closer..." | processInteract on a RUN goblin > 3.5 blocks (status message) |
| "you are already carrying a Goblin" | pickup while owning another goblin (status message) |
| "ehh.." | `catchEh` keyframe (CATCH) |
| "awkward.." | `catchAkward` keyframe |
| "well..." | `catchWell` keyframe |
| "would you rather have this stupid... thing?" | `catchRather` keyframe |
| "...or use me?~" | `catchMe` keyframe (CATCH_BJ) |
| "good choice!~" | `paizuriChoice` keyframe |
| "...for both of us!" | `paizuriBoth` keyframe |
| "now use me like a fuck toy!~" | `paizruiUse` keyframe |
| "hmm..." | `breedingHmm` keyframe |
| "guess we found a worthy breeding partner!" | `breedingFound` keyframe |
| "Eh.. go pin him down, before he runs off!" | `breedingEnough` keyframe |
| "The Queen is still pregnant - so no breeding for you uwu" | pregnant queen scan attempt (status message, 1200-tick cooldown) |
| "Thanks to you, my clan is soon going to get a few new members! In return I will bear of one of my guards to serve as your personal Onahole. Choose wisely~" | `handleThrowCooldown` release (queen chat) |
| "Farewell my knight. You are welcome once I am breedable again." | `handleHeldParticles` when a guard is carried after breeding |
| "<Goblin> I got your <item> hehe~" | gold steal (`B_clash269`, player chat, item display name) |

## Sounds

- **No `girls.goblin.*` entries exist in sounds.json** and no `GIRLS_GOBLIN_*` arrays in SoundHandler — the goblin is fully silent apart from shared MISC events: `MISC_PLOB` (all dialogue beats), `MISC_POUNDING` (pound/smallPound @0.25), `MISC_SMALLINSERTS` (@3.0), `MISC_TOUCH` (@3.0), plus vanilla `SoundEvents.ENTITY_PLAYER_ATTACK_STRONG` (attackSound). Throne/guard sounds: none.
- Player form additionally uses `SoundEvents` for attacks only.

## Model & Appearance

- `GoblinNpcModel.getModelLocations()`: `[geo/goblin/goblin.geo.json, geo/goblin/armored.geo.json]` (nude/armored); texture `textures/entity/goblin/goblin.png`; animation `animations/goblin/goblin.animation.json`.
- Per-frame pass (`setLivingAnimations`): `preggy` bone hidden unless `aV`; breeding body offset +1.5 Y in first person for BREEDING_SLOW_2/FAST_2/CUM_2; AWAIT_PICK_UP/VANISH → head look-at the player (`updateBoneLook`); throw leg swing from owner limb motion; owner-first-person body/head hiding. `canRender` returns false when the interaction/owner player uses the **slim** skin model (`"default".equals(player.getSkinType())`) — slim players never see the goblin body.
- `GoblinRenderer`: static pose inputs `strafeRotation`/`forwardRotation` smoothed from `movementInput` + player yaw/pitch deltas (`MOVEMENT_DIR_VECTOR = (10, -20, -10)`), `lastPlayerYaw/Pitch` bookkeeping; `renderEntityInFirstPerson`; `getBoneColor` (body tint from `CURRENT_ACTION` skin color etc.); `getEyeColor(modelCodeParts[8])` → `EyeColor`.
- `GoblinFirstPersonRenderer`: while the local player owns a goblin girl in first person — draws her via RenderWorldLast, cancels `RenderHandEvent` (PICK_UP/START_THROWING) and `RenderPlayerEvent.Pre`, and force-renders her at the throw start with the **yaw sentinel `-420.69`** for the owner (must not be "cleaned up").
- `GoblinModel`: vanilla 2×6×2 cube placeholder (IVanillaModel). `GoblinPlayerEntity.getHandModel` → `AllieModel` + `textures/entity/kobold/hand.png`; hand tint from `SkinColor.values()[parts[7]].getColor()`.
- Color enums: `HairColor` (8: PURPLE/ORANGE/BLACK/BLUE/BROWN/PINK/RED/GREEN), `SkinColor` (5: LIGHT_GREEN/MEDIUM_GREEN/DARK_GREEN/LIGHT_YELLOW/LIGHT_BLUE; default `GoblinEntity.ax = DARK_GREEN`), `EyeColor` (6: RED/VIOLET/YELLOW/BROWN/TURKEY/BLUE; fallback RED). DNA indices: 6 = hair, 7 = skin (also model-part index), 8 = eyes, 9 = part index.

## AI & Behavior

- Goals (`initEntityAI`): `EntityAISwimming` (0), `DoorInteractAiGoal` (3), `WatchClosestGirlGoal(2.0F)` (5). Everything else is per-tick custom logic in `updateAITasks`: `handleGravity` (despawn guard: untamed, unqueen, queen-keyed, NULL → remove), `handlePickUpState`, `handleThrowState` (queen re-sits on throne when unbound), `B_clash269` (steal cycle), `J_clash267` (RUN flee pathing — tries points 20..0 blocks away, 0.825 speed), `E_clash260` (breeding scan), `handleJumpThrow`, `handleHoldCooldown`, `handleThrowCooldown`, `handleHeldParticles`, `handleThrownLand`, `handleStandUp`, `handleHeldState` (tamed guard: face the nearest player or wander a guard post within `√800`).
- `canBeCollidedWith`: false for THROWN, owner-bound, or any non-NULL action (RUN/AWAIT_PICK_UP use the super impl).
- Damage/fall immunity: `onLivingAttack` cancels all non-void damage while owned; `fall()` suppressed in THROWN/START_THROWING; `setFire` ignored while owned.
- Dimension handling: `c.onPlayerChangedDimension` — server clones the owned goblin (model + part codes copied) into the new dimension as SHOULDER_IDLE; client tick despawns goblins whose owner is in another dimension.
- Queen AI: steal cycle every 32000 ticks (`aO`, starts at 31520 → first steal ~480 ticks in) while sitting, unowned, unpregnant; nearest grounded, non-airborne player within 100 blocks with a gold item → spawns a RUN goblin beside the player carrying a copy of the stack (`a0` key), removes the item from the player's inventory, announces the theft; the thief runs home; when caught (CATCH) the stolen stack can be returned ("take ur stuff back") or kept as the paizuri reward ("use her"); dead goblins drop the stolen item on the server.
- Guards: `I_clash261` returns the queen's two guard entities, respawning missing ones (same girl id + model part) — guard UUIDs persisted in NBT (`guard0..N`).

## Unique mechanics

- **Queen system**: `aX` (not a data key — plain field, NBT `isQueen`), throne position `al`/rotation `ac`, guard list `ab`, pregnancy `aV` (key 126) + impregnation tick `av`, pregnancy duration 8400 ticks, guard reward system, gold-steal economy, `/locatenearestgoblinlair` locator (rejects Nether/End: "goblin lairs don't exist in the Nether/End"; "No nearby goblin lair found uwu").
- **Stolen gold**: `ag` whitelist = golden hoe/horse armor/ingot/apple/axe/shovel/pickaxe/sword/carrot/helmet/boots/chestplate/leggings + gold nugget + gold block + gold ore; stolen stack rides in data key `a0` (124), dropped on death, returned on "take ur stuff back".
- **Pick up / throw / catch**: 45-tick pickup glue → shoulder ride (noClip, invulnerable, hand/body render suppressed); 15/39-tick throw flight with owner-aim velocity; catch chain RUN → CATCH → CATCH_BJ → CATCH_BJ_IDLE with the bj choice.
- **G-key**: "Interact with your goblin" (keycode 34 = G) opens `GalathScreen` on a shouldered goblin.
- Player form shares all of the above through `IGoblin` (owner keys, throw progress, held distance, previous-action diff) — the *player entity itself* is thrown (fake-player rendering).

## Player form (if any)

`GoblinPlayerEntity` (horny potion):
- Keys: `ax` (122) = carrying player UUID, `aA` (126) = pregnancy flag (NELSON_CUM); DNA via `AbstractKoboldPlayerEntity` 119/120/121; scale 0.9; eye height 0.75; hand = AllieModel + kobold hand texture, tint from SkinColor.
- Carry: sneak+click a transformed goblin player (`a.onEntityInteract`, only when the clicker is *not* transformed) → `handlePlayerThrow` → PICK_UP + 45-tick hold, carrier physics locked (`SetPlayerMovementPacket(false)`); while carried: `isAnchored() == true` always, `isPlayerGirl() == false`, `E_clash458()` true, `shouldRenderModel()` false for the owner, model glued 2 blocks above the carrier every tick (server + client tick/render-tick), first-person hand hidden (`onRenderHand`), fake `EntityPlayer` copies rendered via RenderWorldLast (clear/kill pair each frame). Throw: same 15/39 flight, but it is the **owner's player entity** that is launched; `handleOwnerThrow` re-adds the player to the world if missing.
- Scenes: owner commands `anal` → NELSON_INTRO, `paizuri` → PAIZURI_START (teleport + broadcast + strip); menu `["anal", "paizuri"]`; identical sound listener to the NPC (catch dialogue included); `handleLocalAction` forces thirdPersonView 2 for nelson actions; `getCumAction` covers NELSON → NELSON_CUM, PAIZURI → PAIZURI_CUM, BREEDING → BREEDING_CUM_*.
- `onOwnerInteract` (carrier clicks) → `ResetGirlPacket.Handler.resetGirl`, un-anchor, NULL, unbind owner.
- Movement anims: `sit` when riding, `running` (1.2), `walk` (2.0), `backwards_walk` (1.5), `fly`/`fly2` toggle; movement controller transition 2 ticks.

## Data keys / NBT

- Own keys: `OWNER_UUID` (122), `aK` (123, queen's girl id — binds guards to their queen), `a0` (124, `DataSerializers.ITEM_STACK` stolen item), `aC` (125, tamed), `aV` (126, pregnant). Player form: `ax` (122), `aA` (126) — **id 126 is used for pregnancy in both**.
- Inherited: `CURRENT_ACTION` (119) = skin color name, `ACTION_TARGET_POS` (120) = eye color BlockPos, `APPEARANCE_DNA` (121) = model code with trailing segment 9 = model-part index; BaseGirlEntity 99–110 as in allie.md.
- NBT (`writeEntityToNBT`): `bodyColor`, `eyeColorX/Y/Z`, `model`, `girlID`, `queen`, `isQueen`, `isTamed`, `robTicks`, and (queen only) `preggo`, `throneRot`, `thronePosX/Y/Z`, `impregnationTick`, `guard0..N`. Old-model migration: parts[3]/parts[4] > 7 → DNA rebuilt + "updated an old Goblin" log.
- Player form NBT: nothing goblin-specific beyond the shared DNA keys.

## Pitfalls & quirks (deobfuscated code notes)

- `canRender` (GoblinNpcModel) **refuses to render the goblin for slim-skin owners/interaction players** — the goblin body is invisible to them.
- The `-420.69` yaw sentinel in `GoblinFirstPersonRenderer.onRenderWorldLastPlayer` forces the owner's goblin render pass during START_THROWING; the javadoc warns not to "clean it up".
- `ag` (gold steal whitelist) contains `Items.GOLD_INGOT` **twice** (harmless duplicate in the HashSet).
- `aO` (steal timer) initializes to 31520 — the first steal fires only ~480 ticks after the queen starts sitting (32000-tick cycle).
- `handleGravity` despawns untamed goblins whose `aK` queen key is non-empty while in Action.NULL — a queen-less goblin left idle vanishes.
- `H_clash275`/`F_clash274` both fade `ar` (0.05/tick) — one for untamed THROWN landings, one for VANISH; both remove the entity at 0.
- `processInteract` returns true immediately on the client (`world.isRemote`) — the whole pickup/catch state machine runs on the server; the client only sees the synced action changes.
- The catch `bj` hand-state is written server-side (`GIRL_HAND_STATES = "bj"` in processInteract) — unusual vs. other girls where the client writes `animationFollowUp`.
- `getCumAction(BREEDING_SLOW_0/FAST_0)` iterates `this.ab` calling `goblin.getCumAction(action)` (no-op — guards have their own actions) and returns BREEDING_CUM_0 only for the queen.
- `breeding_slow1Done` random-toggles `aD` (left/right pose); `nelson_slowDone` random-toggles `aF`; `paizuriSwitch` toggles `aP` with 50% chance — the loop variant counters are intentionally random.
- `handleThrowCooldown` positions the reward guards at `aT`/`ap` offsets and the player at `as` (all throne-rotation-relative) with yaw+180, pitch 30 — the "two kneeling guards, choose your Onahole" tableau.
- The queen's breeding AABB (`ah = 11,6,11`) is placed using `al - 0.5 - basePos` per throne rotation; `worldgen/ConfigWorldGenHandler` mirrors exactly these offsets (`GoblinEntity.aB/ao/aM/THROW_OFFSET_U`) when building the lair so the throne lines up.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
