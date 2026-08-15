# Fapcraft 1.12.2 Remap — Code Audit Report

Date: 2026-08-15 · Method: 3 parallel read-only audits (entity, client+networking, util+misc) + spot-verification of high-impact findings against the original obfuscated jar (`Fapcraft.1.12.2.v1.1.jar`).

**Nothing was modified by this audit** except the two items listed under *Already fixed*.

---

## 1. Executive summary

The codebase is in good shape after the deobfuscation and the two debugging campaigns (scene system works end to end). The audit found:

- **2 genuine deobf bugs already fixed** (one crash, one wrong-client-branch family) — see §2.
- **~40 bug-like findings** — most are *robustness* issues (missing null checks on player disconnects) that exist in the original jar too, a handful are crash/behavior risks that need jar verification before touching (§3).
- **~35 jar-faithful artifacts** — decompiler quirks that match the released jar bytecode. **Do not "fix" these** (§4).
- **Per-tick/per-frame waste** concentrated in the kobold tribe AI, `getGirlEntityList` world scans, and a few render paths (§5).
- **Concurrency hazards** — daemon threads mutating entities, plus the known ride-guard band-aid (§6).
- **Cosmetic/dead code** — unused imports, dead constants, debug prints (§7).

---

## 2. Already fixed (deobf regressions, jar-verified)

| Item | File | What was wrong | Fix |
|---|---|---|---|
| `null instanceof ClientProxy` (8 sites) | `ServerWhitelistManager`, `DownloadServerModelPacket`, `BaseGirlEntity`, `GoblinEntity`, `UnknownPacket` | Deobf nulled the original `Main.proxy instanceof ClientProxy` side check → always-false → client always took the server branch (whitelist key, model-download wire format, client/server girl pick, bone-color clear all wrong-side) | Restored `Main.proxy instanceof ClientProxy` (verified in original `br`/`cu`/`em` bytecode) |
| `GalathFlightData` flight-target comparator | `entity/GalathFlightData.java:145` | Lambda params were clobbered into the entity var `((Entry) galath)` → `ClassCastException` + no sort whenever the weighted flight-target path runs | Restored descending sort by weight using the lambda params (jar-verified against original `h8`) |

Both are in commit `43e27ed` / deployed.

---

## 3. Bug-like findings (need attention)

Severity: **CRIT** = crash/hang, **HIGH** = broken behavior, **MED** = robustness/edge case. "Jar?" = whether the original bytecode shows the same pattern (V = verified same in jar → treat as upstream behavior, not a deobf regression).

### 3.1 CRIT — crash / hang

| # | File:line | Issue | Jar? |
|---|---|---|---|
| C1 | `util/InHandMapRenderer.java:139` | 5-arg `renderPlayerMap` self-calls the **same overload** with swapped args → infinite recursion/StackOverflow when a transformed player holds a map with empty offhand. A 4-arg overload exists at `:181`. | unverified |
| C2 | `util/RotationHelper.java:39-43` | `lerpAngleDegrees(float,float,double)` calls **itself** with radians (should call the double `lerpAngle` at `:105`) → StackOverflow if ever called. Currently zero callers. | unverified |
| C3 | `util/KoboldTask.java:132-134` | `findConnectedBlocks`: `while (!isAboveMineable(groundPos)) { groundPos = startPos.down(); }` — `groundPos` never advances → **infinite loop / server hang** for a floating log. Reachable from `FallTreePacket` (server thread). | unverified |
| C4 | `entity/GalathFlightData.java` (sort) | Fixed, see §2. | V |
| C5 | `client/gui/ClothingScreen.java:292-299` | `onBoneTypeToggle`: `partIndex` can go negative / exceed `matches.size()` → `IndexOutOfBoundsException` on click (multi-custom-bone case). | unverified |
| C6 | `util/ServerWhitelistManager.java:700-702` | `ModelData` ctor: `girlsStr.replace(...)` NPE when the `which_girls` property is missing (sibling props are null-checked) → custom-model load crash on malformed cfg. | unverified |
| C7 | `item/KoboldEggItem.java:81` | `UUID.fromString(tag.getString("tribeID"))` throws on empty tag → event-handler crash. | unverified |

### 3.2 HIGH — broken behavior

| # | File:line | Issue | Jar? |
|---|---|---|---|
| H1 | `util/KoboldManager.java:293-331` | `getTribeBeds` returns `tribeChests` and vice versa — bed gate uses chest count (`MinePacket:73`, `FallTreePacket:68`); kobolds "sleep" at chests; `isTribeChestOpen:1957` casts beds to `TileEntityChest` → NPE. **Verify against jar before touching** (the javadoc pass documented the crossing as original). | unverified |
| H2 | `util/KoboldManager.java:1119-1129` vs `1016-1070` | Serialization crossed the same way: `writeToNBT` writes chests under `"bed"`, beds under `"chest"`; `readFromNBT` merges both into `tribeBeds` → state flips after reload. | unverified |
| H3 | `util/KoboldManager.java:638-645` | `setTribeHomeForMember` looks up `c.get(memberUuid)` — wrong key, always null → NBT-restore path dead ("not found uwu" spam). `Tribe.setTribeHomePos` has zero callers; `savePosition` *removes* entries and runs on every leadership change → saved positions permanently empty. | unverified |
| H4 | `util/KoboldManager.java:603-636` | `getTribeSavedPositions`: zero-volume AABB (`pos.subtract(-3,-3,-3)` == `pos.add(3,3,3)`), `staleMemberIds` unused, `tribe.k = savedPositions` self-assignment. | unverified |
| H5 | `util/KoboldManager.java:793-815` | `setLeaderKobold` **removes** the kobold from `members`; leader re-derivation picks the *slowest* member; leader absent from `members` breaks counts; dead kobolds never pruned. | unverified |
| H6 | `util/KoboldManager.java:79,149-181` | Static kobold→bed map entries never removed on death → dead kobold's bed stays "assigned" (blocks sleep, other kobolds). | unverified |
| H7 | `util/GirlSavedData.java:162-169` | `shouldDespawn`: `time - lastCumTime > 0L` — the `CUM_TIMEOUT=60` constant is never used → wake-up scene fires unconditionally after any cum. | unverified |
| H8 | `entity/GalathEntity.java:2160-2166` + `GalathFlightData:176,329` | Flight progress keys disconnected: `setSwordAttackProgress` writes key 116, finish checks read key 112; the 112-writer has no callers → wild Galath may never leave the first flight action. Original jar *does* define both keys; whether the original wired them the same needs bytecode tracing. | partial |
| H9 | `entity/KoboldEntity.java:268` | `createKoboldWithSpeed` mutates **static** `af` (read by `applyEntityAttributes` for MAX_HEALTH) → last-created kobold's value races all others' health. | unverified |
| H10 | `entity/KoboldEntity.java:951-956` | `handleMasterPresence`: `hasWorker → addWorker` + forced `setCurrentAction(NULL)`/un-anchor — while a master is present and tribe alerted. Gated, so scenes still work; verify intended cadence. | unverified |
| H11 | `item/GalathCoinItem.java:162-163,242-278` | `writeCooldownNBT` twice per tick on the same key → deactivation expiry re-summons a second Galath + ownership grant ~4s after dismiss. | unverified |
| H12 | `client/renderer/GirlModel.java:110-114` | Outfit-index guard `>` instead of `>=` → `ArrayIndexOutOfBoundsException` at `OUTFIT_INDEX == modelLocations.length`. | unverified |
| H13 | `networking/SendBlocksPacket.java:149-155` | Bed-removal branch calls `removeTribeChest` instead of `removeTribeBed`. | unverified |
| H14 | `util/ServerWhitelistManager.java:87-89` | `isModelDisabled(name)` returns true when the model **is registered** (inverted). Callers compensate inconsistently (`SexSceneRenderer:131` skips rendering registered parts; `UnknownPacket:79` requests downloads of unregistered ones). Verify against jar. | unverified |
| H15 | `client/gui/BeeScreen.java` + `HornyMeterHud` | `BeeScreen.isVisible` never reset on disconnect (stuck black overlay ~1.5s into next world); `HornyMeterHud` slide/smoothed statics never reset by `resetHornyMeter` (meter skips slide-in); `BeeScreen`'s one-way `hideHornyMeter` permanently hides the meter until a future `showHornyMeter`. | unverified |
| H16 | `networking/DownloadServerModelPacket.java:122,252-257` | Static `packetCounter` never reset on disconnect → stale counter corrupts the next batch's completion check. | unverified |
| H17 | `client/renderer/GirlRenderer.java:255-279` | `if (thirdPersonView != 0) return true;` makes the entire third-person camera-repositioning block dead. | unverified |
| H18 | `client/renderer/GirlPlayerRenderer.java:177-191` | Offhand-bow item swap runs **per bone** in the recursion (outside the arm check) → toggles back/forth, wrong-hand items during offhand-bow use. | unverified |

### 3.3 MED — robustness (player disconnect / malformed input)

These all exist in the original decompile too (CFR preserves the derefs) — **fixing = adding null guards, behavior-preserving**:

| # | File:line | Issue |
|---|---|---|
| M1 | `KoboldEntity:624-625`, `BiaEntity:186`, `JennyEntity:205`, `LunaEntity:300` | Dismount-lerp end derefs the interaction player without null check → server NPE if the player logs out mid-lerp. |
| M2 | `SlimePlayerEntity:142-143`, `JennyPlayerEntity:156`, `ElliePlayerEntity:197`, `AbstractPlayerGirlEntity:342,677` | `getPlayerEntityByUUID(getOwnerUserUUID())` / `Optional.get()` on the owner key without null check. |
| M3 | `JennyEntity:162,904` | Second `getClosestPlayerToEntity` call re-run and deref'd without null check. |
| M4 | `AbstractGirlNpcEntity:140` | `((WorldServer)world).spawnParticle` — ClassCastException if the branch ever runs client-side. |
| M5 | `GalathEntity:1166` | `PathUtils.getPathEnd(this.aq)` with `aq` null on first ticks (NPE unless `getPathEnd` guards). |
| M6 | `GalathEntity:1116-1122` | `canStartPussyLicking()` spawns a new Manglelie **per tick** when the partner lookup fails — spawn-until-it-works risk. |
| M7 | `networking/UploadInventoryToServerPacket:84-105` | `packet.d[i]` no bounds check → AIOOBE on short payload (server thread). |
| M8 | `networking/SendChatMessagePacket:87` | `girlList(uuid).get(0)` with no empty check. |
| M9 | `networking/UploadModelStringPacket:81-89` | `getServerGirlEntity` can be null → NPE in the scheduled task. |
| M10 | `networking/RemoveItemsPacket:52` | `getPlayerByUUID(...).inventory` no null check. |
| M11 | `networking/StartStandingSexAnimationPacket:60-72` | Empty CME catch leaves `playerGirl` = last scanned girl → wrong-girl fallback. |
| M12 | `util/PlayerIds.java:152-153` | `sendTo(..., null)` NPE in the logout handler. |
| M13 | `client/gui/ClothingScreen.java:91-97` | `previewGirl` stays null if the NpcType ctor throws → `refreshCustomParts()` NPE. |

---

## 4. Jar-faithful artifacts — do not touch

Verified against the original jar (V) or structurally identical to the decompile (S):

| File | Artifact | Status |
|---|---|---|
| `GalathEntity:1203` | `dist = abs(dx) + abs(dx)` (Z ignored) — original has the same | V |
| `AbstractKoboldPlayerEntity:79-83` | Client branch sets `ar = true` (per-tick `clearBoneColors`) — original does the same | V |
| `KoboldEntity:2449` | `onDeath` calls `setTribeLeader(tribeId, this)` (dying kobold as leader) — original calls the same | V |
| `BiaEntity:129` / `JennyEntity:196` | `TARGET_POS.equals(null)` / `Vec3d.equals(DataParameter)` — always-false dead guards; lerp survives via the `<= 40` cap | V |
| `BeeEntity:341-343` | `HORNY_FLAG && !HORNY_FLAG` always-false branch | V |
| `SlimeEntity:147-157` | NBT keys `hornyLevel`/`ticksUntilBirth` deliberately cross-written | V |
| `ResetGirlPacket` | `resetPose` boolean inverted (single-arg = FULL reset) — documented, jar-verified | V |
| `Vector3fSexmodSpecial` / `Vector2d` | `add` subtracts, `subtract` adds (callers compensate) | V |
| `TrigMath` | `wrapDegrees` is deg→rad, `sinDegrees` is rad→deg — call sites consistent | V |
| `KoboldManager:797-815` | `getLeaderKobold` picks the *slowest* member | V |
| `KoboldManager:273-281` | `isTribeMember` actually checks "is leader" — all call sites use it as leader check | V |
| `KoboldTask:81-85` | `setMiningTargets` *removes* — call sites want removal | V |
| `StructureMarkerRenderer` | `renderMarkers` adds, `setMarkers` removes (SendBlocksPacket pairing) | V |
| `ServerWhitelistManager:541-549` | `getModelScales` returns zOffsets; UnknownPacket uses as "changed" heuristic | V |
| `Main:148-150` | `setConfigs` parses JSON by char offset — load-bearing, do not touch | V |
| `SexSceneKeyHandler` | Duplicated progression block for girls vs player-girl | S |
| `GirlInventoryScreen:204-258` | Loop-var skip mutation, button id 0 never created — jar-faithful | S |
| `GalathCoinRenderer` / `AlliesLampRenderer` | Channel-shifted recursive render args — verify against jar before touching | S |
| 17× empty `catch (ConcurrentModificationException)` | All guard iteration over snapshot lists — vestigial | S |
| Every animation predicate | `action == NULL && action.autoBlink` redundant second call | S |
| `BeeEntity:149` | `hornyTimer = 6.9420184E7F` magic "disabled" sentinel | S |
| `KoboldEggEntity:51,188-190` | Static shared AnimationController (works, eggs animate one at a time) | S |
| `SexEntity:70,90` | Static `ownerLuna` set before spawn — ordering-dependent but functional | S |
| `GirlFollowGoal` / `GirlGotoGoal` | 60-tick FOLLOW/IDLE oscillators — odd cadence, functional | S |

---

## 5. Optimization candidates (per-tick / per-frame)

> The user's ground rule: **don't break anything**. These are candidates only — each needs a behavioral-replay check.

### 5.1 Hot loops (world tick)

- **`KoboldEntity.handleTribeCombat:1316`** — 60³ AABB `getEntitiesWithinAABB(KoboldEntity.class)` + per-candidate sight raycast, run up to twice per kobold per tick → O(tribe²) AABB queries/tick.
- **`KoboldEntity.handleTribeState:821-850`** — per kobold per tick: loops all players × all tribe members computing distances.
- **`KoboldEntity` per-tick data-manager writes** (`handleModelSync` + `updateAITasks`: `aC`, `aZ`, `ak`, `CURRENT_ACTION`) — unconditional writes every tick → sync churn with identical values.
- **`BaseGirlEntity.getGirlEntityList`** — builds a fresh world-wide entity list per call; `getServerGirlEntity`/`girlList` pay the full scan; several per-tick callers (incl. `GirlSavedData.onServerTick` per ownership entry → O(owners × entities) per tick).
- **`ManglelieEntity`** — `getGalathPartner()` full-world scan 5-6× per AI tick across its handlers.
- **`BeeEntity.doParticleStuff:226-327`** — up to 4 passes over `playerEntities` per tick for ~250 ticks, each sending `SPacketParticles`.
- **`GalathEntity:1244-1296`** — 15³ AABB adoption scan per AI tick while onGround/unbound.
- **`LunaEntity.findFishingSpot:589-607`** — re-allocates the 7-entry biome HashSet up to 50× per search.
- **`WildSlimeEntity:187` / `KoboldEggEntity:77-84`** — data-manager counter written every tick.

### 5.2 Render path (per frame)

- **`InHandMapRenderer:72,77`** — `rebuildPlayerGirlTableFromWorld()` per frame + `new ResourceLocation` per frame.
- **`GirlRendererBase:141-157`** — sorts the bone child list by pivotY **on every call**, inside the per-frame bone recursion.
- **`GirlRendererBase:68-79`** — `boneName.hashCode() + id.hashCode()` recomputed per quad per frame (hash-sum cache keys are collision-prone).
- **`GirlPlayerRenderer:280`** — `new GirlLayerRenderer(this)` per frame.
- **`GirlRenderer:358,390`** — `getSkinTexture` called twice per frame.
- **`GalathRenderer:541-550`** — dead per-frame lerped vectors + atan2 during FLY/ATTACK_SWORD; `renderBoneAction/Blowjob` build new `RibbonConfig` with 3 lambdas per frame.
- **`ManglelieRenderer:266-282`** — ~240 string allocations/frame (`"skirt_"+i+"_"+j`).
- **`BasicGirlRenderer:122-135`** — `String.format` + `new ResourceLocation` per frame (30 frames could be constants).
- **`DragonStaffRenderer:101-111,164`** — scans every player's inventory per frame + `new DragonStaffModel()` per frame for its texture.
- **`SexSceneRenderer:160-174`** — new `SexSceneEntity` per custom part per frame (intentional, but allocates).
- **`WorldUtils.getEntityLookVector:52-96`** — 27 block light probes + ~30 Vec3d allocations per call, called per frame per girl.
- **`DragonBreathParticle`/model `getModelLocation`** — `new ResourceLocation` per call in model classes (geckolib calls per frame).

### 5.3 GUI (screens)

- `CustomModelList.drawScreen` — full entry rebuild + `boneTypes.indexOf` sort per frame.
- `GirlScreenBase`/`BeeDialogueScreen`/`GirlInventoryScreen` — button lists cleared/rebuild with `new GuiButton` + `new ScaledResolution` per frame.
- `StructureCommandScreen.drawScreen:270` — `getTargetMaterial()` block-walk runs per frame (can descend dozens of blocks).
- `ClothingScreen.currentModelYaw` — static, never reset on `onGuiClosed` (next preview continues old rotation).

---

## 6. Concurrency

- **`GLOBAL_GIRL_CACHE`** (`BaseGirlEntity:170`) — declared, never read or written anywhere. Dead field.
- **`playerGirlList`** (static `ArrayList`, `AbstractPlayerGirlEntity:100`) — same-thread but walked N times/tick (`rebuildPlayerGirlTableFromWorld` from every player-girl's `updateAITasks`) → O(n²) at scale; sibling map `al` is a `Hashtable` — inconsistent thread-awareness.
- **Daemon threads mutating entities** while the main thread ticks them (pre-existing races):
  - `BasicGirlEntity:121` — `world.removeEntity(this)` from a 6250 ms daemon thread.
  - `GalathEntity:383-394` (8000 ms), `:2074-2116` (three 1200 ms scene threads doing `setCurrentAction`/`setAnchored`/`setInteractionPlayerUUID` + packets), `:3007` (900 ms), `:3198-3201` (500 ms, player teleport + packet).
- **Netty-thread entity/data-manager writes** (should be `addScheduledTask`): `ForcePlayerGirlUpdatePacket:54-68`, `RequestRidingPacket:44-56`, `SendChatMessagePacket:81`, `DownloadServerModelPacket:126,190-229` (disk I/O).
- **Known band-aid**: `BaseGirlEntity.onUpdate` ride-guard `catch (Throwable) → dismountRidingEntity()` — documented risk, do not touch without a plan.

---

## 7. Cosmetic / dead code (no behavior impact)

### Unused imports (systematic, ~40 files)
`TrailSegment` (networking), `DebugMode`/`GalathGeometryRender`/`GirlCombatProtection`/`GoblinFirstPersonRenderer` (girl entities).

### Dead fields/constants
`GirlSavedData.lastSaveTime` · `KoboldTask.maxWorkers` + `getMaxWorkers` · `SkinFetcher.maxCacheSize` · `GirlWorldData.DATA_KEY/SAVE_KEY` · `BeeWorldData.DATA_KEY/SAVE_KEY` · `AllieWorldData.DATA_KEY` · `GirlBedInteraction.bedIndex` · `DebugMode.debugFlag` · `GalathScreen` SIZE/OFFSET constants · `StructureCommandScreen` constants · `TribeNameScreen` constants · `ManglelieRenderer` SCALE/OFFSET constants · `GalathRenderer` STAR_UV/RIBBON constants · `GoblinRenderer.RENDER_SCALE_A` · `GalathFlightHud` PIP/UI constants · `SexEntityRenderer` PARTICLE_OFFSET constants · `DragonStaffRenderer.Vector2f[] l` · `GirlRenderer` LINE_SCALE/CACHE_K/B/D/`globalModelMatrix` · `KoboldRenderer.renderOffset` · `GuiHandler` configFile/dataFile/isInitialized + empty `onGuiOpen` · `GirlModel.enableModelCache` · `ShaderHelper.outlineFramebuffer` · `ResetControllerPacket.controllerIndex/playerUUID` · `DragonBreathParticle.motionX/motionY` · `GirlRendererBase.setBoneHidden` (never called) · `BasicGirlRenderer.getGirlTexture` (returns null, never called) · `AlliePlayerEntity.aq` (write-only→never written).

### Dead methods / classes
`UpdateEquipmentPacket` (never registered, never constructed) · `GalathCoinItem.readCooldownNBT/handleCoinUse/summonGalathFor` · `DeprecatedCheckForUpdates.onClientTick` (empty) + byte-array URL fields · `Main.registerReplacedRenderers` (duplicate GeckoLib init) · `PlayerGirlEvents.onGetCollisionBoxes` (empty handler, per-collision-query dispatch) · `KoboldManager.triggerFastSexAction` (no-op) · `Tribe.tribeUUID` (written, never read) · `Reference.CLIENT_PROXY/COMMON_PROXY` (wrong package, would ClassNotFound if referenced) · `wrapException` identity helpers (`ServerWhitelistManager`, `CommandWhitelistServer`, `GirlModelBase`).

### Debug leftovers
`SetPlayerCamPacket:53` thread-name println · `TeleportPlayerPacket:90` "teleporting player" println · `ShaderHelper:54` "succ registered the outline shader :)" · `InHandMapRenderer:80` "HAND IS NULL uwu..." per frame · `DownloadServerModelPacket:152` " doesnt exist lol" · `WorldUtils:174` "bed is fucked up..." · `KoboldManager` ~30 "tribe of UUID ... not found uwu" prints · `SceneDebug.ENABLED = true` (flip to false for release).

### Misc style
- Raw types throughout (`ArrayList`, `HashMap`, `HashSet` unparameterized).
- `System.out.println` instead of `Main.LOGGER` (~50 sites).
- Tautological double compare: `PlayerIds:143` `equals(persistentID) || equals(uniqueID)` (same value).
- `GirlSavedData`/`GirlWorldData`/`BeeWorldData`/`AllieWorldData`/`ConfigWorldGenHandler` ctor `(String dataId)` ignores the parameter.
- `DebugMode:218` catches `NullPointerException` for `Long.parseLong` (should be `NumberFormatException`).
- `SlimePlayerEntity:141,144` — `player.noClip = true;` twice in a row.
- `GirlFollowGoal:29` — `hasOpenedDoor` assigned, never read.
- `LunaPlayerEntity:547,560` — `getTargetPosition().y - 0.0` dead arithmetic.
- `GalathEntity:2264` pointless `getWorld` override; `JennyEntity:152` null `getHurtSound`; `SexSceneEntity:126` empty `onDeath` override.
- `GalathEntity:703/720` — near-identical twin scene-enders (28 vs 30 ticks), mergeable.
- `ConfigWorldGenHandler:41` — `double KOBOBLIN_SPAWN_CHANCE = 0.004F` (float literal).
- `AbstractPlayerKoblinGoboldRenderer:42` — static color cache keyed by `boneName.hashCode() + playerId.hashCode()` (collision-prone).
- `Potion/HornyPotion:32,46` — registry name `"sexmod:horny potion"` contains a space.
- `BeeWorldData` reuses the `"sexmod:galath_spawn_list"` data key (copy-paste).
- Static world-state never cleared on world close: `BeeWorldData`/`AllieWorldData`/`GirlWorldData` maps, `GirlSavedData.b` (cum-time map), `ServerWhitelistManager.isGlobalRenderingDisabled`, `DownloadServerModelPacket.packetCounter`, `BeeScreen.isVisible`, `GalathFlightHud` charges.

---

## 8. Recommended next steps

1. **Fix the CRIT bucket** (C1–C7) — two are one-line corrections verified against the jar; the rest are null/bounds guards that cannot change behavior on the happy path.
2. **Verify the kobold tribe accessors** (H1–H6) against the original jar bytecode before touching — the javadoc pass documented the bed/chest crossing as original, but the serialization merge (H2) and dead saved-positions (H3) look like real lossage either way.
3. **Re-run the scene tests** after any of H-class fixes — kobold scenes, bed scenes, and the tribe UI are the sensitive paths.
4. **Per-tick waste** (§5.1) is safe to optimize incrementally: hoist `getGirlEntityList` results per tick, gate the kobold data-manager writes on value change, dedupe the double `handleTribeCombat` call.
5. **Do not touch §4** without a bytecode-verified plan — every item there matches the shipped jar.
