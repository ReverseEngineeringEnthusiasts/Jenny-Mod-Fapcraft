# Allie — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.allie.name=Allie` (en_us.lang). Entity id `allie` (egg entity, `NpcType.ALLIE.npcID` = 5614613); player form `player_allie` (`playerID` = 64867483) |
| Voice actress | Not credited in mcmod.info (credits only list Jenny @Lizzywaffler, Ellie @EndymionVA, Bia @MissMoonified, Luna @MacStarVA, Kobold @FlirtyFawn696) |
| Entity class(es) | `AllieEntity extends BaseGirlEntity` (NPC); `AlliePlayerEntity extends AbstractPlayerGirlEntity` (player form) |
| Model / Renderer | `AllieNpcModel extends GirlModel<BaseGirlEntity>` (geckolib) / `AllieRenderer extends GirlRenderer`; player form `PlayerAllieRenderer extends GirlPlayerRenderer`; lamp model `AlliesLampModel` + `AlliesLampRenderer` (TESR); `AllieModel` = vanilla placeholder cube (`IVanillaModel`, hand/vanilla path) |
| NpcType enum | `ALLIE(AllieEntity.class, 5614613, AlliePlayerEntity.class, 64867483)` |
| Player form | Horny-potion transformation; deepthroat + reverse-cowgirl scenes, no summon/wish flow; gets flight while transformed |
| Obtaining | `AlliesLampItem.ALLIES_LAMP` (`item.allies_lamp.name=Allies Lamp`), injected into loot: abandoned mineshaft, desert pyramid, simple dungeon, woodland mansion (`pool3`, fallback `pool2`, weight 5). Right-click lamp → 95-tick rub → Allie summoned (3 wishes per lamp stack) |

## Animations (complete list)

Source: `assets/sexmod/animations/allie/allie.animation.json` (29) + `animations/allie/lamp.animation.json` (2). Allie is a summoned girl: there are no walk/run/attack AI loops in her NPC form — the action controller drives everything; `eyes` and `movement` controllers only play `null`/`tail`/`blink`.

| Animation name | Length (s) | Purpose / when used (code trigger) |
|---|---|---|
| `animation.allie.null` | ∞ (no keyframes) | Idle/empty pose; `eyes` controller when action != NULL, `action` on Action.NULL |
| `animation.allie.blink` | 4.75 | Eye blink (eyes controller, `closedL/openL` bones) — plays when action has `autoBlink` |
| `animation.allie.tail` | 1.04 | Tail sway, movement controller; speed scaled by 4×Δpos (player form) |
| `animation.allie.summon` | 12.2 | First summon intro (SUMMON): spinning body rise + 8 dialogue keyframes (`summonMSG1..8`, `summonDone`); ends → `Action.SUMMON_WAIT` |
| `animation.allie.summon_wait` | 2.4 | SUMMON_WAIT loop — floating idle before the menu opens |
| `animation.allie.summon_normal` | 6.24 | Repeat summon (SUMMON_NORMAL, lamp without first-use tag): shorter intro, `summon_normalMSG1..5` + `summon_normalDone` |
| `animation.allie.summon_normal_wait` | 1.64 | SUMMON_NORMAL_WAIT bobbing loop; on done the interaction menu re-opens |
| `animation.allie.summon_sand` | 12.2 | SUMMON_SAND — sand-phobia panic: `summonMSG1`, `disappear` (2.0s), `summon_sandMSG1`, `scream`, `summon_sandMSG2`; despawns |
| `animation.allie.deepthroat_prepare` | 3.44 | ALLIE_PREPARE_FIRST_TIME (lamp first use): `deepthroat_prepareMSG1` (hihi), `MSG2` (boys), `blackscreen`, `deepthroat_prepareDone` |
| `animation.allie.deepthroat_normal_prepare` | 1.64 | ALLIE_PREPARE_NORMAL (later uses): `deepthroat_normal_prepareMSG1` (alright), `blackscreen`, `deepthroat_prepareDone` |
| `animation.allie.deepthroat_start` | 3.2 | DEEPTHROAT_START entry: `deepthroat_slowMSG1` (1.28s), `deepthroat_startDone` (3.16s) → DEEPTHROAT_SLOW |
| `animation.allie.deepthroat_slow` | 2.96 | DEEPTHROAT_SLOW loop: `deepthroat_slowMSG1` (lipsound/bjmoan, +0.02 horny) |
| `animation.allie.deepthroat_fast` | 1.2 | DEEPTHROAT_FAST loop (sneak to enter): `deepthroat_fastMSG1` (+0.04 horny), `deepthroat_fastDone` → back to SLOW unless jumping |
| `animation.allie.deepthroat_cum` | 2.28 | DEEPTHROAT_CUM: `deepthroat_cumMSG1` (moan+lipsound+cuminflation @1.5), `blackscreen`, `deepthroat_cumDone` → `resetCameraAndPhysics()` + inventory upload |
| `animation.allie.reverse_cowgirl_start` | 4.4167 | REVERSE_COWGIRL_START (server repositions player via `handleAllieOwner`); keyframes `giggle`, `pounding`, `moan`, `mmm`, `slide`, `openSexUi`; auto-followUp 88 ticks → SLOW |
| `animation.allie.reverse_cowgirl_slow1/2/3` | 2.375 each | REVERSE_COWGIRL_SLOW loop variants, picked by `stateIndex` (rerolled on `cowgirlSlowDone`); `pounding`, `slowMoan`, `slide` |
| `animation.allie.reverse_cowgirl_fasts` | 1.6667 | REVERSE_COWGIRL_FAST_START (sneak); `pounding`, `fastMoan`, `fastSwitch` |
| `animation.allie.reverse_cowgirl_fastc1/2/3` | 2.1667 each | REVERSE_COWGIRL_FAST_CONTINUES variants by `stateCount` (jump on `fastSwitch` rerolls); `slide`, `pounding`, `fastMoan`, `fastSwitch` |
| `animation.allie.reverse_cowgirl_cum` | 2.4167 | REVERSE_COWGIRL_CUM: `slide`, `pounding`, `cum` (inserts @6), `moan`, `aftermoan`, `blackscreen`, `cowgirl_cumDone` → reset + upload |
| `animation.allie.rich` | 5.0 | RICH_FIRST_TIME wish (lamp first use): `rich_MSG1` (wishgranted + MakeRichWishPacket), `disappear` (0.84s) |
| `animation.allie.rich_normal` | 5.28 | RICH_NORMAL wish (later uses): `rich_MSG1`, `disappear` |
| `animation.allie.attack0/1/2` | 0.36/0.32/0.32 | Player-form melee swings (Action.ATTACK, `nextAttack` 0..2); `attackSound` + `attackDone` |
| `animation.allie.bowcharge` | 1.6 | Player-form bow draw (Action.BOW) |
| `animation.lamp.rub` | — | Lamp item first-person rub (HOLD_ON_LAST_FRAME) while `sexmodAllieInUse` is set (AlliesLampItem.animationPredicate) |
| `animation.lamp.null` | — | Lamp idle (unused unless the controller falls through) |

## Scenes & Actions

Action enum entries used (with their pitch/hideNameTag config): `SUMMON`, `SUMMON_WAIT`, `SUMMON_NORMAL`, `SUMMON_NORMAL_WAIT`, `SUMMON_SAND`, `ALLIE_PREPARE_FIRST_TIME` (40/-40 pitch, no player), `ALLIE_PREPARE_NORMAL`, `DEEPTHROAT_START/SLOW/FAST/CUM` (all `useBoyCam=true, 40/-40`), `REVERSE_COWGIRL_START/SLOW/FAST_START/FAST_CONTINUES/CUM` (all `useBoyCam=true, 30/-90, flipGirlYaw=true`), `RICH_FIRST_TIME`, `RICH_NORMAL`.

**Summon flow** (NPC only). Rub the lamp 95 ticks → server spawns `AllieEntity` 2 blocks in front of the player (facing player+180°, noGravity, noClip, anchored). `hasLampItem()` (lamp NBT `sexmodUses` == 1 → first use) picks `Action.SUMMON` vs `SUMMON_NORMAL`. If the block below her is `Blocks.SAND` → `Action.SUMMON_SAND` instead (she panics, screams, despawns). `summonDone`/`summon_normalDone` → `SUMMON_WAIT`/`SUMMON_NORMAL_WAIT`, then the interaction menu opens (`openInteractionMenu` → `openInventoryGui(player, allie, ["action.names.makemerichallie", "action.names.deepthroat", "Reverse cowgirl"], false)`). No bed requirement; she hovers at the summon point.

**Wish flow** (Make me rich!). Menu choice `action.names.makemerichallie` → `doAction` → `RICH_FIRST_TIME` or `RICH_NORMAL` (first vs later use). Keyframe `rich_MSG1` (0.04/0.52s) → chat `allie.dialogue.wishgranted` ("Here ya go! *thanos snaps*") + `MISC_PLOB` + client sends `MakeRichWishPacket` (server spawns 1–2 × diamond, emerald, gold ingot `EntityItem`s at her position). `disappear` (0.84/0.92s) sets `LAMP_SCALE = 0.99` → the renderer shrinks her 0.01/tick; at ≤ 0 the client uploads inventory (`UploadInventoryToServerPacket2`), locks movement, sets `LAMP_SCALE = -69` (done). She is then removed on the next `updateAITasks` (action == NULL).

**Deepthroat flow**. Menu `action.names.deepthroat` → client writes `GIRL_HAND_STATES = "deepthroat"` via `animationFollowUp` + `setCurrentAction(ALLIE_PREPARE_FIRST_TIME|ALLIE_PREPARE_NORMAL)`. `deepthroat_prepareDone` (sound listener, client) reads `GIRL_HAND_STATES`: `"reverse_cowgirl"` → `REVERSE_COWGIRL_START` (pitch 30); else `DEEPTHROAT_START` + `KoboldStatePacket(girlId, playerUUID, false, true)` locks the player in + camera yaw+180, player repositioned (0,0,1.35, pitch 30), horny meter reset. `deepthroat_startDone` → SLOW; `deepthroat_slowMSG1` → lipsound (67%) or bjmoan (33%) +0.02 horny; sneak (`getNextAction`) → FAST; `deepthroat_fastDone` returns to SLOW unless jumping; `deepthroat_fastMSG1` +0.04 horny. Jump with full meter (`getCumAction`) → DEEPTHROAT_CUM; `deepthroat_cumMSG1` (moan + lipsound + cuminflation), `blackscreen` (BeeScreen interaction), `deepthroat_cumDone` → `resetCameraAndPhysics()` + inventory upload.

**Reverse cowgirl flow**. Menu `"Reverse cowgirl"` → `GIRL_HAND_STATES = "reverse_cowgirl"` + `ALLIE_PREPARE_*`; `deepthroat_prepareDone` dispatches `REVERSE_COWGIRL_START` (server also runs `handleAllieOwner()` on entry → snaps the player to her target pos). 88-tick server followUp → SLOW. SLOW plays `reverse_cowgirl_slow{stateIndex}` (1..3); `cowgirlSlowDone` rerolls `stateIndex` ≠ current. Sneak → `REVERSE_COWGIRL_FAST_START` (`fasts`); `fastSwitch` (jump) → `REVERSE_COWGIRL_FAST_CONTINUES` (`fastc{stateCount}`) or rerolls `stateCount` + `resetAnimationControllerOffset()`. Jump+full meter → REVERSE_COWGIRL_CUM → `cowgirl_cumDone` → reset + upload. `setCurrentAction` refuses re-entry into SLOW/FAST while CUM plays.

## Dialogue (all lang lines)

All lines are `allie.dialogue.*` (en_us.lang); spoken via `sendChatMessage` in the AllieEntity sound listener (keys `summonMSG1..8`, `summon_normalMSG1..5`, `deepthroat_prepareMSG1/2`, `deepthroat_normal_prepareMSG1`, `rich_MSG1`, `summon_sandMSG1/2`):

| Lang key | Text | When spoken |
|---|---|---|
| `allie.dialogue.summon1` | HIIYAAYA! | First summon, 0.04s + scawy0 |
| `allie.dialogue.summon2` | Congratulations mortal~ | 1.72s + giggle |
| `allie.dialogue.summon3` | By finding and rubbing my lamp... | 3.68s |
| `allie.dialogue.summon4` | you got yourself... | 4.48s + lightbreathing2 |
| `allie.dialogue.summon5` | 3 wishes! | 5.44s + hmph4 |
| `allie.dialogue.summon6` | ... get the reference? :D | 7.56s + giggle |
| `allie.dialogue.summon7` | So..! | (defined; keyframe list shows summonMSG8 at 9.68s — summon7 is reachable in the JSON but unused by the listener) |
| `allie.dialogue.summon8` | tell me your first wish, mortal~ | 9.68s + huh (random) → opens menu |
| `allie.dialogue.sup` | sup mortal? | Repeat summon 1.48s + giggle |
| `allie.dialogue.youhave` | you have... | 2.72s |
| `allie.dialogue.2wishes` | 2 wishes left! | 3.52s when lamp `sexmodUses` == 2 |
| `allie.dialogue.1wish` | one last wish | 3.52s otherwise |
| `allie.dialogue.tellme` | Tell me your wish, mortal! | 5.08s + huh |
| `allie.dialogue.hihi` | Hihihi | deepthroat_prepareMSG1 (first-time) + giggle |
| `allie.dialogue.boys` | Boys... | deepthroat_prepareMSG2 + sigh0 |
| `allie.dialogue.alright` | alright! | deepthroat_normal_prepareMSG1 + giggle |
| `allie.dialogue.wishgranted` | Here ya go! *thanos snaps* | rich_MSG1 + plob |
| `allie.dialogue.nooo` | OOOH HECK NO!!1 | summon_sandMSG1 + scawy2 |
| `allie.dialogue.phobia` | Allie has a phobia about sand. Summon her somewhere, where there is none of that | summon_sandMSG2 broadcast around |
| — (hardcoded) | So... | summon_normalMSG4 |
| — (hardcoded) | I'd like to use ur services owo | not Allie — kobold line (see kobold.md) |

## Sounds

Sound event prefixes `girls.allie.<folder>.<fileN>` in sounds.json (all mapped in `SoundHandler.GIRLS_ALLIE_*`):

| Folder | Files | SoundHandler array |
|---|---|---|
| `aftersessionmoan` | 0–3 (4) | GIRLS_ALLIE_AFTERSESSIONMOAN[4] — aftermoan keyframe |
| `ahh` | 0–9 (10) | GIRLS_ALLIE_AHH[10] — slowMoan 50% |
| `bjmoan` | 0–13 (14) | GIRLS_ALLIE_BJMOAN[14] — deepthroat fast/slow |
| `giggle` | 0–4 (5) | GIRLS_ALLIE_GIGGLE[5] — giggle, summon 2/6 |
| `happyoh` | 0–2 (3) | GIRLS_ALLIE_HAPPYOH[3] (defined; unused in Allie code paths) |
| `heavybreathing` | 0–7 (8) | GIRLS_ALLIE_HEAVYBREATHING[8] (defined) |
| `hmph` | 0–4 (5) | GIRLS_ALLIE_HMPH[5] — summon5, summon_normalMSG3 |
| `huh` | 0–1 (2) | GIRLS_ALLIE_HUH[2] — summon8, summon_normalMSG5 |
| `lightbreathing` | 0–10 (11) | GIRLS_ALLIE_LIGHTBREATHING[11] — summon4 |
| `lipsound` | 0–13 (14) | GIRLS_ALLIE_LIPSOUND[14] — deepthroat slow/cum |
| `mmm` | 0–9 (10) | GIRLS_ALLIE_MMM[10] — mmm keyframe |
| `moan` | 0–7 (8) | GIRLS_ALLIE_MOAN[8] — moan/fastMoan/cum |
| `sadoh` | 0–1 (2) | GIRLS_ALLIE_SADOH[2] (defined) |
| `scawy` | 0–2 (3) | GIRLS_ALLIE_SCAWY[3] — summon1, sand phobia |
| `sigh` | 0–1 (2) | GIRLS_ALLIE_SIGH[2] — deepthroat_prepareMSG2 |

Shared MISC events she uses: `MISC_PLOB` (wish), `MISC_POUNDING` (35), `MISC_SLIDE` (7, indices 0,1,4,6), `MISC_INSERTS` (5, @6.0), `MISC_CUMINFLATION` (@1.5), `MISC_SCREAM` (sand). Sounds live under `sounds/girls/allie/<folder>/<file>.ogg`.

## Model & Appearance

- `AllieNpcModel.getModelLocations()`: `[geo/allie/allie.geo.json, geo/allie/armored.geo.json, geo/allie/allie.geo.json]` — **variant 0 = nude, 1 = armored, 2 = nude-2** (outfit index); texture `textures/entity/allie/allie.png`.
- Bone groups (GirlModel armor/top toggles): `HeadArmor = ["armorHelmet"]`; `TopArmor = ["armorShoulderR","armorShoulderL","armorChest","armorBoobs"]`; `Top = ["boobsFlesh","clothes","clothesR","clothesL"]`.
- Geo bones of note: `body, upperBody, torso, hipbend, head, customHead, girlCam, hair, heart, eyes` + eye rig (`eyeL/eyeR`, `browL/browR`, `closedL/closedR`, `openL/openR`, `irisL/irisR`, `whiteL/whiteR`, `eyeLiner`) + mouth rig (`mouth, wideSmugSmile2, cum, mouthThingDown, worryed, normalSmile, tongueSmile2, suprisedPikatchu, tongueSmile, blush`), `neck, choker, boobs, boobPreviewR, boobPreviwL, bra, midsection`, arms `armR/lowerArmR/customHandR/weapon/ellbow`, `clothes`, `hip, tailhang, tail, tail0..tail7` (portal particles spawn from `tail0..7` offsets every 10 ticks), `sideR/sideL, booty, cheekR/cheekL, vagina, frontAndInside, fuckhole`, `customShoeR/L`, plus the **embedded steve rig** for reverse cowgirl: `steve, Torso2, Head2, boyCam, cock, ballL/R, LeftArm/LeftLowerArm/leftArmAlex/leftArmSteve..., RightArm..., RightLeg/kneeR2/shinR2, LeftLeg/kneeR3/shinL2`.
- `AllieModel` — vanilla 2×6×2 cube placeholder (`IVanillaModel`) used for the NPC vanilla-model render path. `AlliePlayerEntity.getHandModel` returns **`BiaModel`** with `textures/entity/allie/hand.png`.
- `AlliesLampModel` + `AlliesLampRenderer`: lamp geo `geo/allie/lamp.geo.json`; TESR for the item.

## AI & Behavior

- NPC Allie has **no movement AI** (summoned, anchored, noGravity, noClip). `updateAITasks` is a lifecycle guard: removes the entity when `getCurrentAction() == Action.NULL` or when the interaction player is gone from the world.
- `onUpdate` (client): opens the interaction menu once (`isLampActive` → `openInteraction`), portal puff burst at spawn (`resetToDefaultState`, 300 particles), tail particles, and the `LAMP_SCALE` despawn sequence (upload inventory + `HandlePlayerMovement.setMovementLock(true)` when scale ≤ 0, sentinel `-69`).
- Combat/attacks only exist in the **player form** (Action.ATTACK with `attack0..2`, Action.BOW `bowcharge`); the NPC has no attack logic.
- `ac()` (client): sets `isLampActive = true` unless `stateFlag2` (set while a `doAction` menu choice is in flight) — the one-shot menu open.

## Unique mechanics

- **Allies Lamp + 95-tick rub** (`AlliesLampItem`): right-click (not sneaking; blocked for player-girls and while any Allie holds the *same* ItemStack) sets player NBT `sexmodAllieInUse=true`, `sexmodAllieInUseTicks=0`; `onUpdate` increments ticks, spawns CRIT_MAGIC particles from tick 50 with eased (easeInOutQuad) rising offset `player.eyeHeight*(1-progress)`, count `progress*150`, and at tick 95 spawns 150 particles, resets the flags, increments lamp NBT `sexmodUses`, spawns the Allie (see summon flow) — all while `HandlePlayerMovement` locks movement. Logout resets `sexmodAllieInUse`.
- **3 wishes**: lamp NBT `sexmodUses` 0→1→2→3; tooltip shows remaining ("2 wishes left" / "1 wish left" / "no wishes left"); `hasLampItem()` (uses == 1) selects first-time vs normal dialogue/animations; wish 3 = MakeRichWishPacket (diamonds/emeralds/gold).
- **Sand phobia**: summon over `Blocks.SAND` → SUMMON_SAND panic animation → `disappear` + scream + dialogue; she despawns without granting wishes.
- **Despawn-by-scale**: `LAMP_SCALE` renderer shrink (0.01/tick, translate `3 - scale*3`) with the `-69` completion sentinel — the same mechanism ends rich-wish and sand scenes.

## Player form (if any)

`AlliePlayerEntity` (horny potion):
- Scale `1.9 + aq` (aq fed by the renderer's smoothed bob), eye height 1.63; `canBeInteracted() = false`.
- Menu: `["action.names.deepthroat", "Reverse cowgirl"]`; `handleOwnerCommand` maps them to `DEEPTHROAT_START` / `REVERSE_COWGIRL_START` + `sendActionPacket` + `teleportPlayerToGirl`.
- Same sound-listener scene flow as the NPC (deepthroat_prepareDone → KoboldStatePacket lock-in + camera reposition; cowgirlSlowDone rerolls `ar`; fastSwitch rerolls `av`; cumDone → `resetCameraAndPhysics()`), plus `attackDone` cycling `nextAttack` (0→1→2).
- `F_clash231()` marks ALLIE_PREPARE_*, DEEPTHROAT_START/SLOW/FAST/CUM for **first-person rendering** (the girl replaces the player's body).
- Flight while transformed: `B_clash233()`/`onTickClient()` → `handleOwnerUUID(...)` (also `updateAITasks` re-binds when the owner appears).
- Animation predicate additionally maps `ATTACK → animation.allie.attack{nextAttack}`, `BOW → bowcharge`, `NULL → null`; movement controller speed = `4*Δpos` clamped to 4.
- No summon/wish/sand mechanics; no `LAMP_ITEM` key.

## Data keys / NBT

Data-manager (explicit serializer ids, shared hierarchy):
- `AllieEntity.LAMP_ITEM` = **111** (`DataSerializers.ITEM_STACK`) — the lamp stack that summoned her; `hasLampItem()` reads its NBT `sexmodUses`.
- Inherited `BaseGirlEntity` keys 99–110: `CUSTOM_NAME` (99), `CUSTOM_MODEL_KEY` (100), `WALK_SPEED` (101), `INTERACTION_PARTNER_UUID` (102, "null" when unset), `GIRL_HAND_STATES` (103, holds the scene choice `"deepthroat"`/`"reverse_cowgirl"` written by the client via `animationFollowUp`), `CUR_ACTION` (104), `OUTFIT_INDEX` (105, 0=nude/1=dressed), `GIRL_ID` (106), `YAW_ROTATION` (107), `TARGET_POS` (108), `IS_ANCHORED` (109), `MASTER` (110).
- Item/player NBT: `sexmodUses` (wishes used), `sexmodAllieInUse`, `sexmodAllieInUseTicks`, `sexmodAllieID` (all on the player's entity data / lamp stack).
- `AllieWorldData` (`sexmod:customstaticgirlnames` WorldSavedData) — custom NPC-name registry keyed by player UUID → NpcType → name (shared with Galath naming; not used by AllieEntity itself).

## Pitfalls & quirks (deobfuscated code notes)

- `updateAITasks()` **despawns Allie whenever the action returns to `Action.NULL`** — any scene teardown must happen before that (the class javadoc calls this out explicitly) or she vanishes mid-flow.
- `LAMP_SCALE` doubles as despawn state machine: `1.0` = normal, `(0,1)` = shrinking, `0` = upload+lock (once), `-69` = done.
- `setCurrentAction` guards: refuses `DEEPTHROAT_FAST/SLOW` while `DEEPTHROAT_CUM` plays and refuses `REVERSE_COWGIRL_SLOW/FAST_START/FAST_CONTINUES` while `REVERSE_COWGIRL_CUM` plays (both NPC and player form).
- `summonMSG7` (`allie.dialogue.summon7` "So..!") is **not handled** in the sound listener — the JSON keyframe exists but the listener has no case for it (jumps straight to `summonMSG8` at 9.68s).
- `AllieEntity.openInteractionMenu` re-sets `stateFlag2 = false` (re-arming the `ac()` menu trigger), while `doAction` sets `stateFlag2 = true` to suppress it.
- `reverse_cowgirl_slow1/2/3` and `fastc1/2/3` variants are selected by `stateIndex`/`stateCount` — the cowgirl "Slow" loop cannot be entered twice with the same variant consecutively (do-while reroll).
- `handleAllieOwner()` snaps the player to `getTargetPosition()` on REVERSE_COWGIRL_START (server only).
- Player form reuses `animation.bia.blink` for the eyes controller (not `allie.blink`) when idle.
- The summon dialogue "3 wishes" count reads lamp NBT `sexmodUses` — the *summon* repeat flow (`summon_normal`) is the one that tells you how many wishes remain.

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
