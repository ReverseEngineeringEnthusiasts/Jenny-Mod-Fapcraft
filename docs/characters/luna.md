# Luna — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.luna.name = Luna` (+ `item.luna_rod.name = Luna's rod`) |
| Voice actress | @MacStarVA (mcmod.info credits: "for being Lunas voice actress and being super cute and awesome :))") |
| Entity class(es) | `LunaEntity` (NPC, extends `AbstractGirlNpcEntity`), `LunaPlayerEntity` (player form, extends `AbstractPlayerGirlEntity`); fishing bobber = `SexEntity` ("luna hook") |
| Model / Renderer | `CatNpcModel` (geckolib; both outfit slots → `geo/cat/cat.geo.json`) + `LunaRenderer` (NPC, scaleFactor -0.4) / `PlayerLunaRenderer` (player form); hand placeholder `LunaModel` (IVanillaModel, 2×6×2 cube), hand texture `textures/entity/cat/hand.png` |
| NpcType enum | `LUNA(LunaEntity.class, 6816463, LunaPlayerEntity.class, 81234824)` |
| Player form | `sexmod:player_luna` (playerID 81234824) — spawned via horny-potion transformation |
| Obtaining | Spawn egg (`sexmod:luna`, egg colors 7881787/7940422 — registered via `registerSpawnEntity`, no natural biome spawn); player form via horny potion. Internal codename: **"cat"** (animations under `animations/cat/`, geo under `geo/cat/`, texture `textures/entity/cat/cat.png`) |

## Animations (complete list)

Source: `animations/cat/cat.animation.json` (format 1.8.0), 34 animations. Controller layout: 3 controllers (`action`, `movement`, `eyes`). Trigger mapping from `LunaEntity.animationPredicate` / `LunaPlayerEntity.animationPredicate` and the `registerControllers` sound listeners.

| Animation name | Purpose / when used | Trigger in code |
|---|---|---|
| `null` | Empty/neutral pose; all controllers when a scene action is active (overrides everything) | `animationPredicate` — any non-NULL action, per controller |
| `idle` | Standing idle (NPC: loop; emits `idleDone` which 10% of the time sets `ad` → switches to `idle2`) | movement controller, action NULL, no movement |
| `idle2` | Second idle variant (NPC only; `ad` flag toggled by `idleDone`/`idle2Done`) | movement controller, `"animation.cat.idle" + (ad ? "2" : "")` |
| `blink` | Eyes controller blink while action == NULL | eyes controller (`getCurrentAction() == NULL`) |
| `walk` | Walking (NPC: when `yFlag` (dist to path end) < 3.0) | movement controller, onGround + small delta |
| `run` | Running (NPC: `yFlag >= 3.0`; player: jump/`aj` flag → speed 1.5) | movement controller |
| `fastwalk` | Fast walk (player form only, speed 2.0, `ao.y >= -0.1`) | `LunaPlayerEntity` movement controller |
| `backwards_walk` | Backwards walk (player form only, `ao.y < -0.1`) | `LunaPlayerEntity` movement controller |
| `fly` | Airborne pose (NPC when not onGround; player form when `!af`, toggling with `fly2` via `aq`) | movement controller |
| `fly2` | Alternate airborne pose (player form only, toggled by `aq`) | `LunaPlayerEntity` movement controller |
| `sit` | Sitting (RIDE / SIT actions; also riding) | action controller, cases RIDE/SIT |
| `head_pat` | Head-pat scene (loop) | action controller, case HEAD_PAT; emits `headpatMSG1..3`, `giggle`, `resetGirl` |
| `attack0` / `attack1` / `attack2` | Melee swing (cycles via `nextAttack`; emits `attackSound` → vanilla `ENTITY_PLAYER_ATTACK_STRONG`, `attackDone` → NULL + advance) | action controller, case ATTACK |
| `bowcharge` | Bow draw | action controller, case BOW |
| `throwpearl` | "Pearl" throw → sends the girl home (`SendCompanionHomePacket` on `pearl` sound) | action controller, case THROW_PEARL |
| `downed` | Downed/knocked-out pose | action controller, case DOWNED |
| `start_fishing` | Cast animation (loop=false; emits `rod_breath`, `rod_shoot` → `CatActivateFishingPacket`, `start_fishingDone` → FISHING_IDLE if local player nearby) | action controller, case FISHING_START |
| `idle_fishing` | Waiting with rod out (loop) | action controller, case FISHING_IDLE |
| `eat_fishing` | Eating the catch (loop=false; emits `happyOh`, `renderItem`, `cutenya3`, `eat` (3× eats, shrinks `aa` scale), `burp`, `eatingDone` → `CatEatingDonePacket` + NULL) | action controller, case FISHING_EAT |
| `throw_away` | Throwing the junk catch away (loop=false; emits `happyOh`, `renderItem`, `huh`, `hmph`, `rod_breath`, `throw_away` → `CatThrowAwayItemPacket`, `eatingDone`) | action controller, case FISHING_THROW_AWAY |
| `payment` | Payment gate animation (loop=false; emits `paymentMSG1..4` chat + sounds, `eatPay` (3× eats), `burp`, `paymentDone` → re-invokes `U()`) | action controller, case PAYMENT |
| `touch_boobs_intro` | Touch-boobs intro (loop=false; emits `hehe`, `singing`, `jump`, `touch`, `touch_boobsMSG1`, `horninya`, `breath`, `touch_boobs_introDone` → TOUCH_BOOBS_SLOW + show horny meter) | action controller, case TOUCH_BOOBS_INTRO |
| `touch_boobs_slow` | Touch-boobs slow phase (loop; emits `addCumSlow` (+0.02 meter), `breath`, `touch_boobs_slowDone` → toggles `ae` → variant `slow1`) | action controller, case TOUCH_BOOBS_SLOW (`+ (ae ? "1" : "")`) |
| `touch_boobs_slow1` | Alternate slow-phase variant (randomly picked via `ae` flag on `touch_boobs_slowDone`; emits `addCumFast` (+0.04), `moan`) | action controller, `"animation.cat.touch_boobs_slow" + (ae ? "1" : "")` |
| `touch_boobs_fast` | Touch-boobs fast phase (loop; emits `addCumFast`, `moanOrNya`, `fastDone` → back to SLOW unless jump held) | action controller, case TOUCH_BOOBS_FAST |
| `touch_boobs_cum` | Touch-boobs cum (loop=false; emits `touch_boobs_cumMSG1` (HORNINYA[3]), `touch_boobs_cumMSG3` (HORNINYA[1] + `MISC_CUMINFLATION`), `breath`, `blackScreen` (`BeeScreen.enableInteraction`), `touch_boobs_cumDone` → reset meter + `resetCameraAndPhysics`) | action controller, case TOUCH_BOOBS_CUM |
| `wait` | Cowgirl scene entry wait — Luna standing at the bed (loop=false; emits `call_playerMSG1` "come here - big guy hehe~" + giggle) | action controller, case WAIT_CAT |
| `sitting_intro` | Cowgirl sitting intro (loop=false; emits `jump`, `pounding`, `horninya2` (HORNINYA[1]+cuminflation), `giggle`, `moan`, `sitting_introDone` → COWGIRL_SITTING_SLOW + meter) | action controller, case COWGIRL_SITTING_INTRO |
| `sitting_slow` | Cowgirl sitting slow (loop; emits `pounding`, `sitting_slowMSG1` (random horninya/moan/lightbreathing +0.02)) | action controller, case COWGIRL_SITTING_SLOW |
| `sitting_fast` | Cowgirl sitting fast (loop; emits `sitting_fastTp` (repositions local player), `pounding`, `sitting_fastMSG1` (+0.04), `sitting_fastDone` → back to SLOW unless jump) | action controller, case COWGIRL_SITTING_FAST |
| `sitting_cum` | Cowgirl sitting cum (loop=false; emits `sitting_fastTp`, `pounding`, `sitting_cumMSG1`, `breath`, `blackScreen`, `touch_boobs_cumDone` → reset) | action controller, case COWGIRL_SITTING_CUM |
| `vid` | **Unused by code** — exists in the JSON (animates `blush` bone) but no Java reference anywhere; likely a leftover/disabled feature | none |
| `null` (eyes) | Eyes controller while a scene action is active | eyes controller |

Sound-keyframe markers per animation (from `sound_effects` in the JSON): `head_pat` → headpatMSG1..3/giggle/resetGirl; `start_fishing` → rod_breath/rod_shoot/start_fishingDone; `throw_away` → happyOh/renderItem/huh/hmph/rod_breath/throw_away/eatingDone; `eat_fishing` → happyOh/renderItem/cutenya3/eat/burp/eatingDone; `touch_boobs_slow` → addCumSlow/breath/touch_boobs_slowDone; `touch_boobs_slow1` → addCumFast/moan/touch_boobs_slowDone; `wait` → call_playerMSG1; `payment` → paymentMSG1..4/eatPay/burp/paymentDone; `touch_boobs_intro` → hehe/singing/jump/touch/touch_boobsMSG1/horninya/breath/touch_boobs_introDone; `sitting_intro` → jump/pounding/horninya2/giggle/moan/sitting_introDone; `sitting_slow` → pounding/sitting_slowMSG1; `touch_boobs_cum` → touch_boobs_cumMSG1/cumMSG3/breath/blackScreen/cumDone; `touch_boobs_fast` → addCumFast/moanOrNya/fastDone; `idle` → idleDone; `idle2` → idle2Done; `attack0/1/2` → attackSound/attackDone; `sitting_fast` → sitting_fastTp/pounding/sitting_fastMSG1/sitting_fastDone; `sitting_cum` → sitting_fastTp/pounding/sitting_cumMSG1/breath/blackScreen/touch_boobs_cumDone; `throwpearl` → pearl.

## Scenes & Actions

Interaction menu (`LunaEntity.openInteractionMenu`, via `GirlInventoryScreen`): options `["action.names.sex", "action.names.touchboobs", "action.names.headpat"]` with costs `[3× raw fish (Items.FISH meta 0), 2× cooked fish (meta 1), null]` — non-creative players must own the stack or get "<Luna> you cannot afford that..." (played with `GIRLS_JENNY_SADOH[1]`; client-side, `GirlInventoryScreen.actionPerformed`). Busy refusal: `bia.dialogue.busy` ("I am busy at the moment~").

Client `doAction` (all three) calls `triggerActionSync(true, true, uuid)` and sets `animationFollowUp` (`GIRL_HAND_STATES`) via `ChangeDataParameterPacket`; server `U()` dispatches on that state:

- **Touch boobs** (`touch_boobs`): PAYMENT → (fish payment) → TOUCH_BOOBS_INTRO → SLOW ↔ FAST (sneak-to-fast via `getNextAction`, jump keeps fast; cum via `getCumAction` when meter full) → CUM → reset. `OUTFIT_INDEX` forced to 0 (nude). Horny meter shown at `touch_boobs_introDone`, reset at `touch_boobs_cumDone`. `setCurrentAction` forbids re-entering SLOW/FAST while CUM plays.
- **Sex (cowgirl sitting)**: PAYMENT → `SendGirlToSexPacket` + `ResetGirlPacket` (jar-faithful order) → `goToSexBed()`: searches nearest bed (`getNearestBed`), picks the closest free side among 4 yaw/offset candidates (0/180/-90/90), walks there at 0.2 speed (`ay`/`ak` lerp, 60 & 120 tick re-path), then anchors (IS_ANCHORED, noClip, noGravity) → WAIT_CAT. No bed → chat "Heh.. there is no bed nearby.. but I already ate the fish so nya~ hehe" (broadcast packet) or "Heh.. the bed is obscured.. but I already ate the fish so nya~ hehe" + GIRLS_LUNA_GIGGLE. In WAIT_CAT, `handleNearbyPlayer` waits for the closest player ≤1.25 blocks; at tick 25 (server) locks the player in (positioned `positionPlayerRelative(0, -0.075, -0.7109375)`), yaw = her yaw+180, `OUTFIT_INDEX=0`, → COWGIRL_SITTING_INTRO. Client mirrors: tick 0 unlock (`BeeScreen.enableInteraction` + `HandlePlayerMovement.setMovementLock(false)`), tick 25 `thirdPersonView = 2`. Then SLOW ↔ FAST (jump keeps fast; `sitting_fastTp`/`sitting_fastDone` reposition the player to head/back offsets), CUM → reset.
- **Head pat** (`headpat`): no payment; HEAD_PAT directly; chat "huh?~" (HUH sound) / MMM / "nya~" (HORNINYA[0]).

Bed requirement: sex scene **requires a nearby bed**; touch-boobs and headpat do not.

## Dialogue (all lang lines)

Luna has **no lang-file dialogue keys** — all lines are hardcoded in `LunaEntity`/`LunaPlayerEntity`:

| Line | When spoken |
|---|---|
| "Love it here owo" + `GIRLS_LUNA_OWO` | `onArriveHome()` (set-new-home action) |
| I18n `bia.dialogue.busy` = "I am busy at the moment~" | right-click while busy (`processInteract` fallback) |
| "<Luna> Heh.. there is no bed nearby.. but I already ate the fish so nya~ hehe" | `goToSexBed` — no bed found (via `SendChatMessagePacket`) |
| "Heh.. the bed is obscured.. but I already ate the fish so nya~ hehe" | `goToSexBed` — bed sides all blocked |
| "Here, I know u like fish and yea.. these are for you" | `paymentMSG1` (to interaction player) + `MISC_PLOB[0]` |
| "huh~?" + `GIRLS_LUNA_HUH` | `paymentMSG2` |
| "nyyyaaaa~ :D" + `GIRLS_LUNA_CUTENYA[1/7/10/11]` (random) | `paymentMSG3` |
| "tankuuuu owowowo" + `GIRLS_LUNA_OWO` | `paymentMSG4` |
| "comon~ touch me hihi~" + `GIRLS_LUNA_GIGGLE` | `touch_boobsMSG1` |
| "come here - big guy hehe~" + `GIRLS_LUNA_GIGGLE` | `call_playerMSG1` (WAIT_CAT) |
| "hehe~" + `GIRLS_LUNA_GIGGLE` | `sitting_introMSG1` |
| "huh?~" + `GIRLS_LUNA_HUH` | `headpatMSG1` |
| (sound only) `GIRLS_LUNA_MMM` | `headpatMSG2` |
| "nya~" + `GIRLS_LUNA_HORNINYA[0]` | `headpatMSG3` |
| "<Luna> you cannot afford that..." | `GirlInventoryScreen` cost refusal (shared, uses `GIRLS_JENNY_SADOH[1]`) |

## Sounds

`sounds.json` events `girls.luna.<folder>.<name><n>` — 15 folders under `sounds/girls/luna/` (matches SoundHandler arrays):

| SoundEvent array | Size | Folder |
|---|---|---|
| `GIRLS_LUNA_AHH` | 18 | ahh |
| `GIRLS_LUNA_CUTENYA` | 12 | cutenya |
| `GIRLS_LUNA_GIGGLE` | 15 | giggle |
| `GIRLS_LUNA_HAPPYOH` | 8 | happyoh |
| `GIRLS_LUNA_HMPH` | 6 | hmph |
| `GIRLS_LUNA_HORNINYA` | 10 | horninya |
| `GIRLS_LUNA_HUH` | 5 | huh |
| `GIRLS_LUNA_LIGHTBREATHING` | 25 | lightbreathing |
| `GIRLS_LUNA_MMM` | 8 | mmm |
| `GIRLS_LUNA_MOAN` | 10 | moan |
| `GIRLS_LUNA_OUU` | 13 | ouu |
| `GIRLS_LUNA_OWO` | 8 | owo |
| `GIRLS_LUNA_SADOH` | 7 | sadoh |
| `GIRLS_LUNA_SIGH` | 8 | sigh |
| `GIRLS_LUNA_SINGING` | 8 | singing |

Also used: `MISC_EAT` (eat/eatPay keyframes), `MISC_PLOB[0]`, `MISC_TOUCH`, `MISC_JUMP[0]`, `MISC_POUNDING`, `MISC_CUMINFLATION[0]`, vanilla `ENTITY_PLAYER_ATTACK_STRONG` (attackSound), `ENTITY_PLAYER_BURP` (burp), `ENTITY_BOBBER_THROW/RETRIEVE/SPLASH` (rod). Hurt sound = `GIRLS_LUNA_OUU`; death sound = 5% chance `GIRLS_ALLIE_SCAWY[2]`, else `GIRLS_LUNA_OUU[12]`.

## Model & Appearance

- Geo: single `geo/cat/cat.geo.json` (identifier `geometry.cat`, format 1.12.0, ~350 bones); `CatNpcModel.getModelLocations()` returns the **same file for both outfit slots** — outfit switching only toggles bone visibility groups, no separate nude/dressed geo.
- Bone groups (`CatNpcModel`): Top = `boobsFlesh`, `cloth`; Bottom = `fleshL`, `fleshR`, `vagina`, `curvesL`, `curvesR`, `kneeL`, `kneeR`, `cloth`; armor arrays (`armorHelmet`, `armorShoulderR/L`, `armorChest`, `armorBoobs`, `armorBootyR/L`, `armorPantsLowR/L`, `armorPantsUpR/L`, `armorHip`, `armorShoesR/L`).
- Notable bones: `head`, `backHair`, `sideHairR/L`, `frontHairR/L`, `offhand`, `weapon`, `girlCam` (camera placement), `heart` (animated during the cowgirl sitting scene — the "heart particles" visual), `question` (animated in `throw_away`), `blush` (head-pat / touch-boobs / sitting scenes), `cum`, `cock`/`shaft`/`ballL`/`ballR` (male prop for scenes), `eyes`/`eyeR`/`eyeL` with open/closed/lash variants, `mouth` variants (wideSmugSmile2, tongueSmile2/3, suprisedPikatchu), ears `earL`/`earR` with rings, `flower`/`leaf` hair ornaments, `choker`.
- Renderer hair dynamics (`LunaRenderer.onBoneProcessing`, NPC): unanchored, `backHair`/`sideHairR/L`/`frontHairR/L` counter-rotate against head pitch (`rotR` captured from `head` bone); `offhand` bone renders the held catch item when `zFlag == 1` (scaled by `aa` = eat-shrink factor). `PlayerLunaRenderer` mirrors hair counter-rotation (`rotationZ`) and has custom item/bow/shield transforms.
- Held rod: during FISHING_START/FISHING_IDLE, `resolveHeldItemStack` swaps in `LunaEntity.ao` (the `LunaRodItem.LUNA_ROD` stack, enchantments copied from data key `az`).
- Textures: `textures/entity/cat/cat.png` (632K), `hand.png` (hand model), `robot.png` (unused by these classes — likely cat-robot alt).

## AI & Behavior

- Tasks (`reinitTasks`): `EntityAIWanderAvoidWater(0.35)` + `WatchClosestGirlGoal(EntityPlayer.class, 3.0F, 1.0F)`, both priority 5. Movement speed 1.0 without master, 0.5 with master (`updateAITasks`).
- Creepers flee Luna: static inner class `LunaEntity.a` subscribes `EntityJoinWorldEvent` and adds `EntityAIAvoidEntity(creeper, LunaEntity.class, 6.0F, 1.0, 1.2)` to every spawned creeper.
- Combat: iron axe in inventory slot 0 (constructor), sword rotations `slashSwordRot=230`, `stabSwordRot=150`, `holdBowRot=320`; ATTACK/BOW/THROW_PEARL actions standard.
- Idle fishing loop (`handleFishingIdle`): every 1200 ticks (`aj`), if masterless + no interaction + not going-to-bed (`ar`), finds a water spot (`findFishingSpot`, up to 50 attempts, water biomes only — RIVER/OCEAN/DEEP_OCEAN/BEACH/STONE_BEACH/SWAMPLAND/MUTATED_SWAMPLAND — preferring depth ≥6, skipping already-used spots in `an`), walks there (0.35, path-abort if water in the way), becomes silent, removes wander/watch goals, anchors and casts (FISHING_START → `rod_shoot` → server `CatActivateFishingPacket` → `LunaRodItem.castFishingRod`). When the hook's `lureTimer` hits 15 (on a re-cast), she re-casts (`al` schedules hook removal +20 ticks) and starts FISHING_EAT (ItemFood catch) or FISHING_THROW_AWAY (junk). Catch handling via `CatEatingDonePacket` / `CatThrowAwayItemPacket` / `addCaughtItem`.
- No natural biome spawn — egg only.

## Unique mechanics

- **Fishing sim**: `SexEntity` ("luna hook", registered `sexmod:luna_hook`, egg 4768742, data keys 110 `OWNER_UUID` / 111 `CAUGHT_ENTITY_ID`) — vanilla bobber physics: flies from the rod (`positionLunaAbove` aims toward `LunaEntity.ai` fishing spot, angle -22.5°+45°·dist/7), ray-traces catch (`checkCatch`, hookable = `canBeCollidedWith()` or EntityItem; own Luna hookable only after 5 water-bob ticks), hooks entities (rides them, `handleCatch` yanks them toward Luna), bobs in water with lure/bob/catch-delay chain (`spawnLootBlocks`: catchDelay 100..600 − fishingLevel·100; rain +25% / no-skylight −1 loot), rolls `LootTableList.GAMEPLAY_FISHING` into Luna's held stack (`lureTimer = 9999`), despawns after 1200 hooked ticks. `canCatch()` always returns false (dead method — catch runs from `checkCatch` in FLYING). NBT no-op (never persists). `getCatchResult()`: entity-item → 3, entity → 5, lure-caught → 1, hooked-ground → 2, else 0; damages the rod (`stack.damageItem(catchResult, luna)`).
- **Rod slot + item**: inventory slot 6 = fishing rod (`az` data key syncs it, `syncHeldItem` copies enchantments onto the held `LunaRodItem`); `af` (119) = rod-cast state driving the item model's `cast` property override (`LunaRodItem` custom `IItemPropertyGetter`, returns 1.0 only when entity is a LunaEntity with `af` set); rod damage 64, uses vanilla `fishing_rod` model. Her equipment GUI (id 0) is Luna-specific: `GirlInventoryContainer2`/`GirlInventoryContainerGui` add a dedicated **ROD slot** (slot 6) beyond weapon/bow/armor (other girls get plain `ChestContainer`).
- **Heart particles**: the `heart` geo bone is animated during the cowgirl sitting scene (`sitting_intro/slow/fast/cum`); `blush` bone during touch-boobs/head-pat. (No `EnumParticleTypes.HEART` code in Luna — that is the Slime's mechanic.)
- Scale/eat state: `aa` (0.333 per eat, reset 1.0 on eatingDone/throw_away) scales the rendered held catch item; `zFlag` gates item render (`renderItem` sound keyframe).
- `getJumpUpwardsMotion()`: 1.0 in water, 0.5 otherwise (higher hop out of water).

## Player form (if any)

`LunaPlayerEntity` — same 3 scenes (touch-boobs, cowgirl sitting, headpat) with identical action machine. Differences:
- Scale factor 1.6 (renderer `PlayerLunaRenderer`: translate −1.0, scale 0.65), eye height 1.34.
- Hand model `LunaModel` (placeholder cube), hand texture `textures/entity/cat/hand.png`.
- Movement controller additionally maps `fastwalk`/`backwards_walk`/`fly`/`fly2` (NPC uses walk/run/fly); movement controller transition length 10 ticks (both forms).
- No fishing idle at all — no rod, no `az/ag/af/yFlag` data keys, no `ao` rod stack, no `goToSexBed` bed search.
- Scene entry: menu offers `["action.names.touchboobs", "action.names.headpat"]` (no cost); owner commands (`handleOwnerCommand`) `touchboobs` → strip + TOUCH_BOOBS_INTRO + `teleportPlayerToGirl`; `headpat` → HEAD_PAT. Cowgirl scene starts from `handleInteraction()` → WAIT_CAT, countdown `ar` (same 25-tick, 1.25-block logic as the NPC, `handleLunaOwner`).
- `openInteractionMenu` uses `openInventoryGui(..., false)` (no dress-up preview). `canBeInteracted() = true`.
- Sends `SetPlayerForGirlPacket`-style flow via shared `sendActionPacket`.

## Data keys / NBT

Own keys (explicit `createKey(...)` ids — never renumber, shared hierarchy):
- 121 `yFlag` (Float) — distance from her position to current path's final point (walk vs run switch).
- 120 `az` (ItemStack) — synced fishing rod (inventory slot 6).
- 119 `af` (Boolean) — rod-cast flag (`av != null && ag == EMPTY`), drives the rod item's `cast` override.
- 118 `ag` (ItemStack) — the caught item in her hand (eaten / thrown away / `dropHeldItem`).
Inherited (BaseGirlEntity, 99–110): `CUSTOM_NAME` 99, `CUSTOM_MODEL_KEY` 100, `WALK_SPEED` 101, `INTERACTION_PARTNER_UUID` 102, `GIRL_HAND_STATES` 103, `CUR_ACTION` 104, `OUTFIT_INDEX` 105, `GIRL_ID` 106, `YAW_ROTATION` 107, `TARGET_POS` 108, `IS_ANCHORED` 109, `MASTER` 110. `SexEntity` keys: 110 `OWNER_UUID` (Optional\<UUID\>), 111 `CAUGHT_ENTITY_ID` (entity id + 1).
NBT: `readEntityFromNBT` forces `setNoGravity(false)` on load (documented as an **invented band-aid** in the code comment — float-on-reload symptom fix, not jar behavior).

## Pitfalls & quirks (deobfuscated code notes)

- **Codename "cat"**: all Luna assets are `cat`-prefixed (animations/cat, geo/cat, textures/entity/cat, `CatNpcModel`, "geometry.cat"); `getDisplayNameText()` is "Luna".
- `animation.cat.vid` exists in the JSON but is referenced nowhere in Java — dead asset.
- The dismount/bed lerps must use `RotationHelper.lerpVec3d(pos, target, 40 - counter)` (INT step variant) — the double variant flings her and she vanishes (code comment).
- `readEntityFromNBT` no-gravity reset is a non-jar fix; keep only while the reload symptom persists.
- `setCurrentAction` guards: while `COWGIRL_SITTING_CUM` or `TOUCH_BOOBS_CUM` play, SLOW/FAST re-entry is blocked.
- The `eyes` controller plays `blink` only when action == NULL; any scene action forces `null` on all controllers (including eyes).
- "bjiMSG"-style `positionPlayerRelative` offsets appear as `(0, -0.075, -0.7109375)` (head) and `(0, -0.160625, -0.9925)` (back) for the sitting scene's camera-anchored player repositioning.
- `GirlInventoryScreen` cost check compares `getMetadata()` exactly (raw vs cooked fish are different metas), removes the paid stack via `RemoveItemsPacket`.
- Interaction entry uses `triggerActionSync(true, true, uuid)` + `animationFollowUp` (`GIRL_HAND_STATES`) + `HandlePlayerMovement.setMovementLock(false)` for every scene; `blackScreen`/`resetGirl` sound keyframes call `BeeScreen.enableInteraction()` (shared transition overlay) and `resetCameraAndPhysics()`.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
