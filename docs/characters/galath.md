# Galath — Full Character Reference

| Field | Value |
|---|---|
| Registry name | `entity.galath.name = Galath` (lang/en_us.lang) |
| Voice actress | None credited in mcmod.info (credits list only Jenny `@Lizzywaffler`, Ellie `@EndymionVA`, Bia `@MissMoonified`, Luna `@MacStarVA`, Kobold `@FlirtyFawn696`) |
| Entity class(es) | `GalathEntity` (NPC, entity id **314351**, `registerSpawnEntity("galath", ..., 16711680, 16711680)` red egg); `GalathPlayerEntity` (player form, entity id **652535516**) |
| Model / Renderer | `GalathNpcModel` (geo: `galath.geo.json` ×2 + `galath_con_mang.geo.json`; anim `galath.animation.json`); `GalathRenderer` (NPC). Player form: `PlayerGalathRenderer` (extends `GirlPlayerRenderer`). Coin item: `GalathCoinRenderer`/`GalathCoinModel` + `galath_coin.geo.json` + `galath_coin.animation.json`. **`GalathModel.java` is unrelated** — it is the vanilla "glass crystal" model used by the dragon-staff renderer. |
| NpcType enum | `GALATH(GalathEntity.class, 314351, GalathPlayerEntity.class, 652535516)` — not NpcOnly |
| Player form | `GalathPlayerEntity` (owner transforms/rides as Galath; interaction menu `cowgirl` / `mating press` / `ride`, scale factor 2.3) |
| Obtaining | Wild Galaths spawn by replacing wither-skeleton/blaze natural spawns within 1000 blocks of a bee hive (`GalathEntity.a.canSpawn`, `isNearHive`) or via spawn egg. Taming: defeat → paralysis (knockout) → right-click to corrupt → `GIVE_COIN` grants a `GalathCoinItem` bound to her soul. Coin right-click summons her (4 s window); right-click on her with the coin de-summons her. |

## Animations (complete list)

File `animations/galath/galath.animation.json` — **57 animations** (verified). Names are cited with the code that triggers them (all in `GalathEntity.animationPredicate` / `GalathPlayerEntity.animationPredicate` / sound listener unless noted).

| Animation name | Purpose / when used |
|---|---|
| `animation.galath.idle` | NPC idle: movement controller, on ground, no movement, action NULL (`animationPredicate`); also `isLocallyRegistered()` override |
| `animation.galath.walk` | movement controller, action NULL, on ground, moving, `bT` (sprint flag) false |
| `animation.galath.run` | movement controller, action NULL, moving, `bT` true |
| `animation.galath.controlled_flight` | movement controller while airborne at NULL action; action `CONTROLLED_FLIGHT` (riding); `GalathPlayerEntity` movement default when not `af` |
| `animation.galath.blink` | eyes controller, action.autoBlink && action != GALATH_DE_SUMMON; also player-form eyes at NULL |
| `animation.galath.null` | **Player form only** — eyes/movement fallback pose when not blinking or action != NULL (NPC never plays it) |
| `animation.galath.sit` | Player form only: `SIT`/`RIDE` action; movement controller when `ak` |
| `animation.galath.crouchidle` | Player form movement, idle, `hasNoGalathOwner()` (block above owner's head) |
| `animation.galath.crouchwalk` | Player form movement, walking/running/backwards when `hasNoGalathOwner()` |
| `animation.galath.backwards_walk` | Player form movement, moving backwards (`ao.y < -0.1`) |
| `animation.galath.bowcharge` | Player form only: `BOW` action |
| `animation.galath.strip` | Player form only: `STRIP` action |
| `animation.galath.attack0` / `attack1` / `attack2` | Player form only: `ATTACK` action, cycled by `nextAttack` on `attackDone` sound cue |
| `animation.galath.attack` | NPC `ATTACK_SWORD` (sword lunge flight action) |
| `animation.galath.idle_flying` | NPC `FLY` action (CHANGE_POSITION gliding) |
| `animation.galath.summon_skeleton` / `summon_skeletonMirrored` | NPC `SUMMON_SKELETON`, suffix chosen by data key `ay` (mirror side) |
| `animation.galath.knockout_air` | NPC `KNOCK_OUT_FLY`, controller speed 1.5 |
| `animation.galath.knocked_out` | NPC `KNOCK_OUT_GROUND` |
| `animation.galath.knocked_out_stand_up` | NPC `KNOCK_OUT_STAND_UP` |
| `animation.galath.boost` | NPC `BOOST` (ride boost impulse) |
| `animation.galath.summon` | NPC `GALATH_SUMMON` (spawn), loop=false |
| `animation.galath.desummon` / `desummon_standing` | NPC `GALATH_DE_SUMMON`; `_standing` suffix when onGround |
| `animation.galath.running` | NPC `RUN` (running to Manglelie / follow), controller speed 0.7 |
| `animation.galath.give_coin` | NPC `GIVE_COIN` (coin grant scene, 140 ticks, followUp NULL) |
| `animation.galath.hug_mang` | NPC `HUG_MANG` (adoption hug, 239 ticks, followUp NULL) |
| `animation.galath.rape_prepare` | NPC `RAPE_PREPARE` (hover before pounce) |
| `animation.galath.rape_charge` | NPC `RAPE_CHARGE` (pounce dive) |
| `animation.galath.rape_intro` | `RAPE_INTRO` (46 ticks, followUp RAPE_ON_GOING) |
| `animation.galath.rape0` / `rape1` / `rape2` | `RAPE_ON_GOING`, variant `b1` re-rolled on `rape_switch` sound cue |
| `animation.galath.rape_cum` | `RAPE_CUM` (34 ticks, followUp RAPE_CUM_IDLE) |
| `animation.galath.rape_cum_idle` | `RAPE_CUM_IDLE` |
| `animation.galath.corrupt_intro` | `CORRUPT_INTRO` (29 ticks, followUp CORRUPT_SLOW); `isCustomType()` true while `corruptIntroActive` |
| `animation.galath.corrupt_slow` | `CORRUPT_SLOW` (anal scene slow phase; also player form) |
| `animation.galath.corrupt_soft` / `corrupt_hard` | `CORRUPT_FAST`, chosen by `aT` (player jump toggles `corrupt_hard` on `corrupt_hard` sound cue) |
| `animation.galath.corrupt_cum` | `CORRUPT_CUM` (30 ticks server → GIVE_COIN; 30 ticks → NULL + player reset) |
| `animation.galath.masterbate` | `MASTERBATE` (wild summoning variant intro) |
| `animation.galath.masterbating_sitting` | `MASTERBATE_SITTING` (threesome mirror; override via `handleActionAnimationOverrides` when `bx`) |
| `animation.galath.masterbating_sitting_cum` | `MASTERBATE_SITTING_CUM` (set on Mang's THREESOME_FAST→CUM transition) |
| `animation.galath.pussy_licking` | `PUSSY_LICKING` (threesome mirror) |
| `animation.galath.pussy_licking_forward` | `PUSSY_LICKING` when `a5` (fresh start) |
| `animation.galath.pussy_licking_back` | `MASTERBATE_SITTING` when `bx` (fresh start) |
| `animation.shared.bed_slow` | `MORNING_BLOWJOB_SLOW` (morning blowjob; via override `bt` uses bed_back) |
| `animation.shared.bed_soft` | MORNING_BLOWJOB slow→fast transition (override when `aD`) |
| `animation.shared.bed_fast` + `bed_fast1/2/3` | `MORNING_BLOWJOB_FAST`, `playRandomizedAnimation(...,4,0.75F)` |
| `animation.shared.bed_cum` | `MORNING_BLOWJOB_CUM` |
| `animation.shared.bed_back` | MORNING_BLOWJOB_SLOW restart (override `bt`) |
| `animation.galath.double_holding_hard1` | Present in JSON but **no code reference** (leftover of the shared threesome rig) |

File `animations/galath/galath_coin.animation.json` — **2 animations**:

| Animation name | Purpose / when used |
|---|---|
| `animation.galath_coin.summon` | Coin item controller, PLAY_ONCE, while `activation_time` or `deactivation_time` != 0 (`GalathCoinItem.animationPredicate`) |
| `animation.galath_coin.new` | Unreferenced in code (legacy) |

## Scenes & Actions

Action enum values used by Galath (Action.java): `FLY, SUMMON_SKELETON, ATTACK_SWORD, KNOCK_OUT_FLY, KNOCK_OUT_GROUND, KNOCK_OUT_STAND_UP, RAPE_PREPARE, RAPE_CHARGE, RAPE_INTRO(46→RAPE_ON_GOING), RAPE_ON_GOING, RAPE_CUM_IDLE, RAPE_CUM(34→RAPE_CUM_IDLE), CORRUPT_SLOW, CORRUPT_FAST, CORRUPT_CUM, CORRUPT_INTRO(29→CORRUPT_SLOW), CONTROLLED_FLIGHT, BOOST(43→CONTROLLED_FLIGHT), GALATH_SUMMON(15→NULL), GALATH_DE_SUMMON, GIVE_COIN(140→NULL), MASTERBATE, HUG_MANG(239→NULL), RIDE_MOMMY_HEAD, THREESOME_SLOW/FAST/CUM, PUSSY_LICKING, MASTERBATE_SITTING, MASTERBATE_SITTING_CUM, MORNING_BLOWJOB_SLOW/FAST/CUM`.

- **Wild fight** (no master): flight state machine (see AI) with CHANGE_POSITION → FLY, SUMMON_SKELETON (energy balls), ATTACK_SWORD (5 dmg at sword-progress 36 and 40, `GalathDamageSource`), RAPE (pounce).
- **Rape pounce → escape minigame** (wild): RAPE_PREPARE (48 ticks hover) → RAPE_CHARGE (parabolic eased pounce on `bO` progress; on contact with a grounded player: skeletons removed, player anchored, movement locked, entity-look packet, `RAPE_INTRO`). Player gets `EscapeMinigameHud`: WASD prompt cycles every 35 ticks, correct key +0.08 / wrong −0.04 progress, decay 0.006/tick; full bar + unrelated key → `GalathBackOffRapePacket` → `handleRapeState()` → `bZ=CHANGE_POSITION`, action FLY, player released (`SetPlayerMovementPacket(true)`), dialog[0] sound. Player "pounce" input is `GalathRapePouncePacket` → `handleRapeAction(pounce)`: 1 HP/tick (non-creative, floors at 1 HP) while RAPE_ON_GOING/RAPE_INTRO, heals Galath 1.5/pulse if pounce=true. RAPE_ON_GOING variants cycle via `rape_switch`; wild player at ≤1 HP → RAPE_CUM. RAPE_CUM tick 28 (server) → unanchor, NULL, player on solid ground. `Z_clash676`: wild girls, tick 15 of RAPE_CUM → player takes 2.1474836E9 dmg (`SuccubusDamageSource`) — lethal.
- **Knockout / corrupt / bind loop**: `setFlightVelocity` (dragon charge hit) → `bP` paralyzed, KNOCK_OUT_FLY (random or away-from-source impulse, "aaa" sound) → KNOCK_OUT_GROUND on ground (in liquids: 3×3 obsidian platform + lava-extinguish sound; 50 ticks unless HIDE_EFFECTS_FLAG) → KNOCK_OUT_STAND_UP (tick 39: noGravity + up impulse 0.6, shockwave knockback 1.0 velocity + 0.5 `GalathDamageSource` damage to everything within 2 blocks; tick 58: `bP=false`, reset). Tracking players get `"Galath is paralyzed! Now it's time to corrupt her"` + `"(Walk to her and right click her)"` + `SpawnEnergyBallParticlesPacket2`. `processGirlInteract` (paralyzed + KNOCK_OUT_GROUND, server): `CORRUPT_INTRO`, anchored, player movement locked; client locks camera (pitch 19). CORRUPT_INTRO → (29) CORRUPT_SLOW → jump (`corruptSwitch`) → CORRUPT_FAST (soft/hard via `corrupt_hard`) → `getCumAction` → CORRUPT_CUM → 30 ticks → GIVE_COIN → (140) → NULL → `ap()` binds master (see Unique mechanics).
- **Tamed interaction menu** (`processMasterInteract`, master only, blocked during HUG_MANG/RUN/GALATH_SUMMON/GALATH_DE_SUMMON/MASTERBATE and while holding the coin): airborne → `ride`; ground w/o Manglelie → `cowgirl`, `anal`, `ride`; with Manglelie partner → `cowgirl`, `anal`, `threesome`, `ride`. `doAction` (client, 1200 ms daemon thread):
  - `ride` → `GalathFlightHud.showHud()` + `RequestRidingPacket` (server: `player.startRiding(girl,true)`, CONTROLLED_FLIGHT, motionY 0.25, **girl removed from her chunk** — no reverse packet, dismount restores her).
  - `anal` → CORRUPT_SLOW anchored (yaw 0).
  - `cowgirl` → RAPE_INTRO anchored (player yaw + 180).
  - `threesome` → Manglelie `THREESOME_SLOW` + Galath `PUSSY_LICKING`, both anchored, third-person view forced; Mang overrides drive Galath `MASTERBATE_SITTING(_CUM)` / `startPussyLicking()` mirrors.
- **Riding + boost**: BOOST action from jump key (`onKeyInput`, `GalathFlightHud.canUseCharge()`); server `aw()`: at tick 13 impulse `al=6.0` along player look, decay ×0.94; 43 ticks → CONTROLLED_FLIGHT. Yaw follows rider (`aB()`).
- **Morning blowjob** (`onWake`, server, `GirlSavedData.shouldDespawn`): spawns tamed Galath (constructor `spawnStructure=true` → GALATH_SUMMON action skip; actually set to MORNING_BLOWJOB_SLOW directly) beside the bed head; requires the right side of the bed (top+half) to be clear (`spawnStructure`/`isValidFlightBlock`), else a chat message explains the requirement; camera set via `SetPlayerCamPacket`. Jump toggles MORNING_BLOWJOB_FAST (`HandlePlayerMovement.isJumping`), which → MORNING_BLOWJOB_CUM → `aE()` resets both girls via `ResetGirlPacket`. `canStartPussyLicking()` spawns a fresh Manglelie partner when the owner already owns one (hug → RIDE_MOMMY_HEAD).
- **MASTERBATE** (wild spawn 10% chance constructor variant, or forced): molang-driven masturbate pose tracking the master (`updateIdlePose`), futa cum trails if `CommandFuta.ENABLED`.
- **HUG_MANG**: Galath runs to a nearby unbound Manglelie (≤3.65 blocks), anchors, sets partner UUIDs both ways, Mang → RIDE_MOMMY_HEAD, `GirlSavedData.markAsManglelieOwned`.

## Dialogue (all lang lines)

No `galath.*` keys exist in `lang/en_us.lang`. All spoken/chat text is hard-coded:

| Lang key | Text | When spoken (code) |
|---|---|---|
| — (none) | `Good timing boy~` | Client sound listener `goodTiming` + `GIRLS_GALATH_DIALOG[4]` |
| — | `Galath is paralyzed! Now it's time to corrupt her` (yellow) | `sendTrackingMessage` on wild death |
| — | `(Walk to her and right click her)` (gray) | `sendTrackingMessage` on wild death |
| — | `[sneak] + [right click] if you want to edit Manglelie instead` (gray status) | `asGirl()` when partner exists and not sneaking |
| — | `Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin` | `ap()` coin grant |
| — | `For Galath and Manglelie to *wake you up with a blowjob*, you have to provide enough space to the *right side* of your bed. This includes the *top and bottom half* of the bed.` | `onWake` when bed space check fails |
| — | `mommy thinks she got no daughter but she actually does have one. Failsafe called. Hopefully its fixed` (LOGGER.warn) | `handleManglelieOwned` |

Spoken dialogue audio: `GIRLS_GALATH_DIALOG[0..5]` (sounds.json `girls.galath.dialog.dialog0..5`).

## Sounds

All events registered as `girls.galath.<folder>.<folder><n>` → `sounds/girls/galath/<folder>/<folder><n>.ogg` (stream:true, category entity). `SoundHandler` arrays + file counts (verified on disk):

| SoundHandler array | Count | Folder |
|---|---|---|
| `GIRLS_GALATH_AAA` | 2 | `aaa/aaa0..1` |
| `GIRLS_GALATH_AHH` | 8 | `ahh/ahh0..7` |
| `GIRLS_GALATH_BREATHING` | 7 | `breathing/breathing0..6` |
| `GIRLS_GALATH_DIALOG` | 6 | `dialog/dialog0..5` |
| `GIRLS_GALATH_GIGGLE` | 4 | `giggle/giggle0..3` |
| `GIRLS_GALATH_HMPH` | 3 | `hmph/hmph0..2` |
| `GIRLS_GALATH_HUH` | 3 | `huh/huh0..2` |
| `GIRLS_GALATH_LIGHTCHARGE` | 5 | `lightcharge/lightcharge0..4` |
| `GIRLS_GALATH_MOAN` | 8 | `moan/moan0..7` |
| `GIRLS_GALATH_ORGASM` | 5 | `orgasm/orgasm0..4` |
| `GIRLS_GALATH_STRONGCHARGE` | 4 | `strongcharge/strongcharge0..3` |
| `GIRLS_GALATH_UUH` | 7 | `uuh/uuh0..6` |

Also used: `MISC_POUNDING` (35), `MISC_SMALLINSERTS` (5), `MISC_INSERTS` (5), `MISC_FLAP`, `MISC_CLAP`, `MISC_BEEW[1/2]` (energy), `MISC_WEOWEO[0/1/2]` (coin summon/desummon/tp), `MISC_SHATTER[0]`, `GIRLS_ALLIE_LIPSOUND` (lick), `SoundEvents.ENTITY_ARROW_SHOOT`, `SoundEvents.BLOCK_LAVA_EXTINGUISH`. `playHurtSound` throttles `GIRLS_GALATH_UUH` to 1/s. Sound cues in the animation JSON (`sound` field) drive the client sound listener (`switchmoan` alternates breathing/moan+ahh, `pound`/`cum`/`orgasm`/`flap`/`clap`/`energysound`/`energy2`/`tpSound`/`boostSound`, `lightcharge`/`strongcharge` at the aim point).

## Model & Appearance

- 3 geo "outfit" slots (`getModelLocations`): `geo/galath/galath.geo.json` (dressed), `geo/galath/galath.geo.json` (same file again), `geo/galath/galath_con_mang.geo.json` (combined Galath+Manglelie rig). Single texture `textures/entity/galath/galath.png`; `GALATH_TEXTURE` shared by wing-mesh rendering.
- Notable bones (verified from geo): `rotationTool` (flight/charge pose driver), `body`, `upperBody`, `hip`, `wings` with `wingR/wingL` + 14 `wingRV0..13`/`wingLV0..13` joint strips (line-strip mesh, `WING_VERTICES_COUNT=14`), `boobs`/`nippleR`/`nippleL`/`braBoobR`/`braBoobL`/`slip` (nudity toggles), futa bones `futaCock`/`futaBallLL`/`futaBallLR`/`futaCockTip`/`futaCockTipDirHelp`/`cockParticles` (hidden unless `CommandFuta.ENABLED`), `weapon`/`weaponStart`/`weaponEnd` (sword + breath particles), `energyBallR`/`energyBallL` (dragon-charge anchors), `coin`, `stars` (wing ring), `mangPos` (Manglelie head anchor), `tongue`/`mangTongue` (ribbon effects), `head3`, `irisL`/`irisR`, `irsisFaceR2`/`irsisFaceR3` (morning-blowjob sway), `creampiePos`, `slipR`/`slipL`/`turnable` (particle anchors), `body2` (Manglelie mesh slot in con_mang geo), `steve` (player skin slot). Armor slot: `HeadArmor() = {"armorHelmet"}`.
- Renderer blacklist (`GalathRenderer.BLACKLISTED_BONES`): `static, turnable, slip, boobs, booty, vagina, fuckhole, futaBallLR, futaBallLL, coin, pentagram` + `BodyParts.CUSTOM_PART_BONES` + `ManglelieRenderer.BLACKLISTED_BONES` (`boobs2, booty2, vagina2, fuckhole2`).
- Custom render passes: wing line-strips, hair-strand ribbons (`hairStrandStart/Mid/End R/L`), star ring at `stars` bone, sword item on `weapon` when `ap` (iron sword, scaled 1.5/1/2), tongue/mangTongue ribbons, GIVE_COIN coin glow (lightmap 120→240 over ticks 105..125, coin color lerp dark→bright), dash POV (`renderDashPov` draws Galath geometry + wings in first person), Manglelie POV while hugging.
- Dash trajectory (`getDashPosition`): progress 24..32 lerp from `B_clash642()` to 3 blocks behind target's eyes; 32..54 hold at 1.5 blocks behind; `az()==-1` disables.
- Wings visibility: `areWingsAnimated()` false during CORRUPT_SLOW/FAST/CUM/COWGIRLCUM; `isWingsAnimated()` = `bb` flag (set during rape/corrupt/masterbate actions and `setNude` cue). Nudity swap: wings animated → nipples visible, bra+slip hidden; else hidden nipples, bra visible.
- `getScaleFactor()`: 0.5 without Manglelie partner, 1.35 with one. `getRenderScaleFactor()`: 1.0 in third person; 0.5 first-person during CORRUPT_*/MASTERBATE/RAPE_PREPARE/CORRUPT_INTRO(active).
- `shouldRender`: skipped entirely when wild and has a flight target (flies invisibly until anchored).

## AI & Behavior

- `initEntityAI`: `EntityAISwimming` (0), `EntityAITempt` (2, speed 0.4, emerald/diamond/gold-ingot/ender-pearl), `DoorInteractAiGoal` (3), `WatchClosestGirlGoal` (5, watch while `isFlyingIdle`).
- Attributes: MAX_HEALTH 110, FLYING_SPEED 0.6, MOVEMENT_SPEED 0.6, FOLLOW_RANGE 50; ignores fire/drown/cactus/fall/fly-into-wall damage.
- **Flight state machine** (`GalathFlightData`, see `GalathFlightData.java`): enum with start/update/finish/stop callbacks; `initFlightData()` picks a random executable action each time the current one finishes (`D_clash685` → `executeUpdate` → true → re-init); `applyAttackCoolDown` actions collapse to CHANGE_POSITION; `onlyDoThisOnPlayers` never picked for mob targets.
  - `CHANGE_POSITION`: 20×20×20 candidate scan around target (air + ray-trace clear + weighted by open-air count, weighted-random pick), glide via `motionX/Z 0.6`, attack progress counter; finished at progress > 23; noClip while progress == 0.
  - `SUMMON_SKELETON`: sets both energy-ball flags + random mirror `ay`, strongcharge sound; at `ad==30` fires up to two `DragonEntity` charges (0.4 velocity + 0.3 random spread) from head/back anchors (`bz`/`bC` offsets, mirrored); finished at `ad>=45`; executable only while `bI.size() < 2`.
  - `ATTACK_SWORD`: anchor + yaw at target; progress 24..32 lerp-dash toward eye+3-blocks-forward, 32..54 hold behind target with 5 dmg at 36 and 40 (`hurtTime`/`hurtResistantTime` zeroed first); 54 → FLY + knockback 0.6.
  - `RAPE`: RAPE_PREPARE 48 ticks → RAPE_CHARGE eased parabolic pounce (progress stored in `bO`, body yaw = progress×180); on contact with grounded non-girl player → rape scene; also removes all `bI` skeletons.
- Target acquisition (`I_clash687`): wild → nearest non-owner/non-creative/non-spectator player in 20-block box; tamed → valid daylight mob in 7-block box (`MobPredicates.isValidTarget`/`isDaylight`); on acquire: dialog[1] sound + CHANGE_POSITION.
- Skeleton guards (`bI`): wither skeletons with stone swords spawned by dragon charges near the target (<15 blocks) — otherwise the charge explodes (2.0). Guards removed when >15 blocks from target (particles + remove), when Galath is paralyzed, or on rape contact.
- **Knockout**: dragon-charge explosion (`DragonEntity.tickChargeState`, 1.0 explosion) → `setFlightVelocity` → paralysis flow (see Scenes). Death is intercepted (`onLivingDeath`): wild → paralysis broadcast, health 1.0, event cancelled; tamed → `GalathCoinItem.deSummonGalath` (GALATH_DE_SUMMON, anchored) + manglelie partner update, `bU` once-guard.
- Tamed follow: owner within 60 blocks (`GirlSavedData.isOwnerNearby`) → follow path (speed 0.5/0.55 sprint + distance bonus, water ×60); if owner too far / blocked → `handlePlayerRide` teleport-hops (up to 20 attempts). Owner join mid-ride → CONTROLLED_FLIGHT restore (`handlePlayerJoin`).
- Respawn: player respawn mid-scene → girl reset via `ResetGirlPacket`, target cleared, flight data stopped.

## Unique mechanics

- **Flight AI state machine** — see AI section (`GalathFlightData` + `bZ` field, `initFlightData` gate).
- **Energy-ball DragonEntity charges** — `SUMMON_SKELETON` spawns `DragonEntity` projectiles (no-clip, fixed direction, DRAGON_BREATH particles; player hit turns them into "charging" projectiles aimed back at the attacker; arrows destroy them and remove the arrow). Block impact near target → wither-skeleton guard; else 2.0 explosion. Charged dragons explode on Galaths (1.0 explosion) → paralysis. The growing hand-dragons during charge-up are rendered via `onRenderWorldLast` (scale `(ad-9)/21`, at `energyBallR/L` bone offsets).
- **Rape pounce + escape minigame** — `EscapeMinigameHud` (see Scenes). `enableRapeUI`/`removeUI` sound cues toggle it.
- **Knockout/corrupt/bind loop** — see Scenes; boss bar (`aO`, red, PROGRESS) only for wild girls (`an()`), percent synced to health.
- **Riding + boost charges** — `GalathFlightHud`: 3 pips, 3 s use cooldown (`canUseCharge`), 1 charge/5 s regen, 500 ms fades, wall-clock based; `BOOST` impulse 6.0 decay 0.94.
- **Coin summon/desummon** — `GalathCoinItem`: 4000 ms summon window, particles from coin to summon point (`isSummonWindow` 1000..3000 ms), server spawn + `grantOwnership` + `handleCoinClick` (client `GirlSavedData.debugEnabled=true`); de-summon: right-click owned Galath with coin → `deactivation_time` → 1000..3000 ms `isCooldownElapsed` particle stream to the coin → server `deSummonGalath`. Dimension change removes the girl (`onPlayerChangedDimension`). `GIVE_COIN` client at tick 95 summons the coin visual for the local player (`GalathCoinItem.summonForPlayer`) with hand-to-hand breath particles (ticks 25..38).
- **GirlSavedData ownership** — `WorldSavedData "sexmod:galath_owner_ship"`: bidirectional player↔girl map, per-owner last-cum-dosage time (`saveCumTime` on CORRUPT_CUM/RAPE_CUM/MORNING_BLOWJOB_CUM/THREESOME_CUM), manglelie-owned player set; server tick drops ownership when the girl is gone (InformOfOwnershipPacket); `CUM_TIMEOUT=60s` gates the morning-blowjob despawn (`shouldDespawn`).
- **Futa toggle** — `CommandFuta.ENABLED` (default true, config file `sexmod/futa`): futa bones visible, `masterbateCumming`/`creampieGalath` cum trails from `futaCockTip`/`futaCockTipDirHelp`.
- **Manglelie partnership** — adoption (HUG_MANG → RIDE_MOMMY_HEAD), threesome scene mirroring, morning-blowjob duo (`canStartPussyLicking` failsafe spawns a new Mang).
- **`asGirl()` targeting** — with a Manglelie partner, sneaking+right-click targets Mang instead (status hint).

## Player form (if any)

`GalathPlayerEntity extends AbstractPlayerGirlEntity implements IGalath`:
- Interaction menu `cowgirl` / `mating press` / `ride` (`openInteractionMenu`); `canBeInteracted` false (menu only via owner commands).
- `handleOwnerCommand`: `cowgirl` → RAPE_INTRO; `mating press` → CORRUPT_SLOW + `handleGalathPlayerOwner` reposition (rotated offset 0.5/0.5-eye/0.4). Both teleport the player in.
- Animations differ: uses `sit`, `crouchidle`, `crouchwalk`, `bowcharge`, `strip`, `attack0/1/2`, `backwards_walk`, and the `null` fallback; movement `crouch*` when `hasNoGalathOwner()` (block above owner's head); speed 2.0 walk / 1.5 run-backwards, 1.5 sprint.
- Eyes controller: `blink` at NULL+autoBlink else `null` anim (NPC never uses `null`).
- `getScaleFactor()` 2.3; `getFlightData()` returns zeros; hand model `CatModel` + `textures/entity/galath/hand.png`; no HUG_MANG (`isHuggingManglelie` false); scene guards refuse re-entry into cum loops (incl. RAPE_CUM→RAPE_CUM_IDLE); `handleCumState` keeps wings animated during rape/corrupt; `handlePlayerAction` hides horny meter during RAPE_INTRO; creampie trails (futa + `creampiePos`).
- Sound listener subset: `attackDone` (attack cycle), `rapeIntroDone`, `rape_switch`, `poundRape`, `corruptSwitch`/`corrupt_hard`/`corrupt_hard_end` (jump-driven), `reset` → `resetCameraAndPhysics`, `setCamCorrupt`/`enableBoyCam` (`aq` flag), `blackScreen` variants, `flapControlled` (UpdateVelocityPacket flight input).

## Data keys / NBT

Data-manager keys (explicit serializer ids — do not reorder; ids 99–110 from `BaseGirlEntity`, 111–122 own):

| Id | Field | Type | Meaning |
|---|---|---|---|
| 99 | `CUSTOM_NAME` | STRING | custom name override (also persisted via AllieWorldData npc name) |
| 100 | `CUSTOM_MODEL_KEY` | STRING | custom model code |
| 101 | `WALK_SPEED` | STRING | walk speed |
| 102 | `INTERACTION_PARTNER_UUID` | STRING | scene player |
| 103 | `GIRL_HAND_STATES` | STRING | hand state pack |
| 104 | `CUR_ACTION` | STRING | current Action name |
| 105 | `OUTFIT_INDEX` | VARINT | geo outfit slot |
| 106 | `GIRL_ID` | STRING | girl UUID |
| 107 | `YAW_ROTATION` | FLOAT | yaw |
| 108 | `TARGET_POS` | STRING | target position |
| 109 | `IS_ANCHORED` | BOOLEAN | anchored |
| 110 | `MASTER` | STRING | master player UUID |
| 111 | `bq` | VARINT | flight target entity id (−1 none) |
| 112 | `aP` | VARINT | flight/attack progress counter |
| 113 | `bN` | BOOLEAN | left energy-ball alive |
| 114 | `b7` | BOOLEAN | right energy-ball alive |
| 115 | `ay` | BOOLEAN | mirror side (summon_skeletonMirrored) |
| 116 | `bH` | VARINT | sword attack progress (az()) |
| 117 | `b8` | STRING | flight target pos `"x\|y\|z"` (`B_clash642`) |
| 118 | `bP` | BOOLEAN | paralyzed flag |
| 119 | `bO` | FLOAT | rape-charge pounce progress |
| 120 | `HIDE_EFFECTS_FLAG` | BOOLEAN | knock-out state (hides wing effects) |
| 121 | `WildSlimeFaceLayer` | STRING | Manglelie partner UUID (`aF()`) |
| 122 | `bT` | BOOLEAN | sprint flag (run anim) |

NBT (entity): `sexmod:master` (STRING), `sexmod:despawned` (BOOL, set when `bA`). NBT (player, coin): `sexmod:galath_coin_activation_time`, `sexmod:galath_coin_deactivation_time` (long ms), `sexmod:galath_coin_de_summoning_animation_time` (bool). GirlSavedData NBT: `sexmod:ownershipdata` {`amount`, `master<i>` UUID, `galath<i>` UUID, `lastcumdosage<i>` long}, `sexmod:mangownershipdata` {`mang<i>` UUID}.

## Pitfalls & quirks (deobfuscated code notes)

- **`GalathModel.java` is not Galath** — it is a nested glass-crystal cube model for the dragon-staff renderer; the real model is `GalathNpcModel`.
- **`GalathScreen.java` is not Galath's screen** — it is the generic direction-pad screen for goblin throw/pickup (`START_THROWING`); Galath's interaction menu is `openInventoryGui` + `BeeScreen`.
- **`getScaleFactor()` is partner-dependent**: 0.5 without / 1.35 with a Manglelie partner (`aF()==null` check).
- **`getAimYaw` mutates `renderYawOffset`** on the entity and returns null outside FLY/SUMMON_SKELETON/RAPE_PREPARE — callers (`ManglelieEntity.isGalathBlocked`, `ManglelieNpcModel`) must null-check.
- **`shouldRender` skips wild girls with a flight target** — she is invisible mid-fight until anchored.
- **Rape-cum instakill**: `Z_clash676` deals `2.1474836E9` damage at tick 15 of RAPE_CUM for wild girls (intended lethal end).
- **`setCurrentAction` guard**: transitions out of GALATH_DE_SUMMON blocked; cum loops guarded (CORRUPT_CUM→FAST/SLOW, RAPE_CUM→RAPE_ON_GOING, MORNING_BLOWJOB_CUM→SLOW/FAST); cum time persisted server-side only.
- **`resetAnimationControllerTicks`** skips tick-offset reset during GALATH_DE_SUMMON (keeps the de-summon anim frozen mid-play).
- **Energy-ball hitboxes** (`b2`, `energyBallHitboxRight`, `SexEntityPart` 0.75³) active exactly while `ad ∈ [9, 30]`; `attackEntityFromPart` sets the per-side alive flag false (kills one charge).
- **Morning blowjob bed check**: uses the block *south/east/north/west of the bed head* (facing-dependent), top and bottom halves; weird orientations log `"Weird bed orientation..."`.
- **`isManglelieOwned(UUID)` parameter mismatch**: the param is named `girlUuid` but callers pass the *owner's* UUID (the set stores owner UUIDs) — works only because of consistent misuse.
- **NoClip toggling** during CHANGE_POSITION (`noClip = progress==0`, forced true inside blocks).
- **Skeleton cleanup** runs every AI tick (`aA` particles, `aG` dead removal) — ConcurrentModificationException is caught silently in several loops.
- **Coin NBT timestamps are the single source of truth** for the summon/desummon state machine on both sides; windows 4000/1000/3000 ms.
- **`GalathFlightHud` is wall-clock**, not tick-based, and static — only one rider UI state globally.
- **Boss bar** (`aO`, `BossInfoServer` RED/PROGRESS) is created in the field initializer with the display name, visible only for wild girls.
- **`E_clash646` gravity**: `setNoGravity(getRidingPlayer() != null)`; tamed fall is damped `motionY *= 0.4` unless MASTERBATE.
- Player-form `getCumAction` also maps RAPE_ON_GOING→RAPE_CUM and CORRUPT_FAST/SLOW→CORRUPT_CUM (shared scene logic with the NPC).

## Related documentation

- [README](../README.md) — index of all docs
- [systems/actions-scenes.md](../systems/actions-scenes.md) — the shared Action state machine
- [systems/gui.md](../systems/gui.md) — screens, containers, HUDs, keybinds
- [systems/networking.md](../systems/networking.md) — the sexmodchannel packet protocol
- [systems/custom-models.md](../systems/custom-models.md) — model codes and the wardrobe
- [systems/items.md](../systems/items.md) — items, potions, loot tables
