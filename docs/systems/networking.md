# Networking — the `sexmodchannel` protocol

All packets are SimpleNetworkWrapper messages on the channel **"sexmodchannel"**
(`PacketHandler`). **46 packet classes are registered via 53 `registerMessage`
calls** (bidirectional packets register twice, once per side). **Packet IDs are
assigned sequentially at registration time — the ID order IS the wire
protocol.** Never reorder/insert/remove registrations without a full
client+server redeploy; the channel name must never change.

## Scene-critical flow (the core loop)

1. `PlayerActionPacket` (C→S) — open the girl's interaction GUI (id 0).
2. `KoboldStatePacket` (C→S, misleading name) — scene-entry trigger: dismount, bind player,
   set TARGET_POS, start the scene. Args: `tribeId` = **girl UUID**, `girlId` = **player UUID**.
3. `ChangeDataParameterPacket` (C→S) — the data-manager bridge: every client-side scene mutation
   (currentAction, animationFollowUp, playerSheHasSexWith, …) writes the server girl's key.
   Guarded: ATTACK only allowed from NULL.
4. `SendGirlToSexPacket` (C→S) — beddable girls walk to their bed (`IBeddableSexGirl.goToSexBed`).
5. `SetPlayerMovementPacket` (S→C) — lock the player's input mid-scene (+ hide horny meter).
6. `SetPlayerCamPacket` (S→C) — snap third-person view/yaw/pitch for all observers.
7. `ResetGirlPacket` (C→S) — scene end. **Inverted flag**: single-arg ctor = resetPose false =
   FULL reset (player physics + release girl); two-arg with true = player-only reset.
8. `ResetControllerPacket` (C→S, then S→C to all within 100 blocks) — restart the girl's
   animation controller identically for all observers.

## Packet directory

| Packet | Direction | Purpose |
|---|---|---|
| BeeOpenChestPacket | C→S | Open the bee's chest GUI (gated by horny flag) |
| CancelTaskPacket | C→S | Cancel a tribe mining task, server un-highlights the blocks |
| CatActivateFishingPacket | C→S | Luna casts her fishing rod (spawns the SexEntity bobber) |
| CatEatingDonePacket | C→S | Advance Luna's fishing catch timer / reel-in |
| CatThrowAwayItemPacket | C→S | Luna drops whatever she is holding |
| ChangeDataParameterPacket | C→S | Data-manager bridge (see above) |
| ClaimTribePacket | C→S | Form/claim a kobold tribe with a name (chat announce, follow mode) |
| DownloadServerModelPacket | both | Custom-model sync: client sends wanted list, server streams files |
| FallTreePacket | C→S | Fell the marked log (tribe bed-requirement gated) |
| ForcePlayerGirlUpdatePacket | S→C | Forced appearance sync (outfit/action) after re-login etc. |
| GalathBackOffRapePacket | C→S | "Back off" input during the rape pounce |
| GalathRapePouncePacket | C→S | Pounce (yes/no) decision in the rape scene |
| GetTribeUiValuesPacket | both | Tribe overview (alerted, positions, wool colors) |
| GirlDataPacket | S→C | Girl-customization payload → opens GirlScreenBase |
| InformOfOwnershipPacket | S→C | Galath/Manglelie ownership status mirror |
| KoboldStatePacket | C→S | Scene-entry trigger (see above) |
| MakeRichWishPacket | C→S | Allie's 3rd wish: spawn diamonds/emeralds/gold at position |
| MinePacket | C→S | Mine a 30-block corridor (3-wide); bed-gated, bedrock-rejected |
| PlayerActionPacket | C→S | Open girl interaction GUI |
| RemoveItemsPacket | C→S | Remove a stack of an item type from a player's inventory |
| RequestRidingPacket | C→S | Mount the owner on his Galath (controlled flight; girl removed from chunk) |
| ResetControllerPacket | both | Animation-controller reset (see above) |
| ResetGirlPacket | C→S | Scene end (see above) |
| SendBlocksPacket | both | Tribe marker sync: S→C add/remove highlighted blocks; C→S staff clicks on marked beds/chests (double-block resolution) |
| SendChatMessagePacket | both | Girl voice lines broadcast to players within 40 blocks (channel field = dimension id) |
| SendCompanionHomePacket | C→S | Send girl home: 3-phase ender-pearl state machine |
| SendEggPacket | C→S | Give a tribe-colored kobold egg |
| SendGirlToSexPacket | C→S | Walk beddable girl to her bed |
| SetNewHomePacket | C→S | Set girl's respawn point (y floored) |
| SetPlayerCamPacket | S→C | Scene camera snap |
| SetPlayerForGirlPacket | C→S | Bind player as the girl's interaction partner |
| SetPlayerMovementPacket | S→C | Movement lock / unlock |
| SetTribeFollowModePacket | C→S | Toggle tribe follow/alerted mode |
| SexPromptPacket | both | Girl-girl sex-prompt routing (accept → female, decline → male) |
| SpawnEnergyBallParticlesPacket | S→C | Galath summon particle burst + ownership debug flag |
| SpawnEnergyBallParticlesPacket2 | S→C | Dragon-breath burst (targeted or random) |
| SpawnParticlePacket | S→C | Particle burst around a girl (hearts etc.) |
| StartStandingSexAnimationPacket | C→S | Owner command: standing sex between owner and a player |
| SummonAlliePacket | C→S | Summon the Allie 2 blocks in front of the player (sand-aware) |
| TeleportPlayerPacket | C→S | Hard teleport (setPlayerLocation; zeroes motion; used by scenes/admin) |
| UnknownPacket | both | Wire-compat placeholder, no behavior |
| UpdateEquipmentPacket | C→S | Girl equipment/held-item update from the inventory GUIs |
| UpdatePlayerModelPacket | C→S | Horny-potion transformation toggle (null = revert to player; spawns the player-girl 69 blocks above) |
| UpdateVelocityPacket | C→S | Velocity impulse on a girl (abilities/items) |
| UploadInventoryToServerPacket | C→S | Chest-GUI save: player slots 0-35 + girl slots (6/7/27 by type) |
| UploadInventoryToServerPacket2 | C→S | Close girl's chest: removes her from the server world |
| UploadModelStringPacket | C→S | Custom model code + part-ids upload (validated) |

## Server-side handling notes

- Almost every C→S handler is **scheduled on the main thread** (SimpleNetworkWrapper thread-safe wrappers).
- Girls are looked up in the global `girlList` / `GirlRegistry` by UUID; wrong-type or missing
  girls make packets **no-ops** (never crash the server).
- `ChangeDataParameterPacket` is the ONLY way client scene mutations reach the server — the data
  manager sync then mirrors state back to all clients.
