# Worldgen & Configuration

## Girl houses

`GirlHouseGenerator` generates the girls' houses in the overworld — the structures where NPC
girls spawn and call home (`homePos`). Generation is routed through `ConfigWorldGenHandler`
(the Forge world-gen event handler) per configured generator (`IWorldGen` interface).

## Natural spawns (`SexModEntities`)

| Entity | Where it spawns |
|---|---|
| Slimes | swamps |
| Bees | forests |
| Pyrocinical girl (BasicGirlEntity) | Nether (rare) |
| Manglelie | Nether (near hives) |
| Galath | near wither skeleton / blaze hives (Nether) |

Girl NPCs themselves spawn from their houses (worldgen), not as random surface spawns.
The Allies lamp is injected into **dungeon/mineshaft loot**.

## `config/sexmod.json` (created on first launch)

```json
{"shouldGenBuildings": true, "shouldLoadOtherSkins": false, "allowFlying": true}
```

| Key | Effect |
|---|---|
| `shouldGenBuildings` | Girl-house generation on/off (`ConfigWorldGenHandler.GENERATION_ENABLED`) |
| `shouldLoadOtherSkins` | Skin loading/caching (`GirlModel.enableModelCache`) — loads other players' skins for the girls' textures |
| `allowFlying` | Flying allowed for transformed player-girls (`AbstractPlayerGirlEntity.ag`) |

The file is rewritten (deleted + recreated with defaults) if it lacks `shouldGenBuildings`;
values are read by character offset (`'t'` check) — the exact layout is load-bearing.

## Other persisted files (runtime, not in the jar)

| Path | Content |
|---|---|
| `config/sexmod.json` | the config above |
| `sexmod/futa` | Galath futa flag |
| `sexmod/custom_models/whitelisted_servers.txt` | whitelisted server IPs |
| `sexmod/custom_models/singleplayer` | local custom-model folder (girl wand button) |
| `sexmod/custom_models/<server>/` | downloaded server models (`.cfg` + `.png` + `.geo.json`) |
| `sexmod_custom_models/<name>/` | server-side model registry (dedicated servers) |
| world-saved data | `sexmod:galath_owner_ship` (Galath/Manglelie ownership), `tribes` (KoboldManager), `sexmod:static_custom_model_manager` (Galath/Manglelie model codes) |
| player NBT | `sexmod:CustomModel<type>`, `sexmod:GirlSpecific<type>`, `sexmod:galath_coin_activation_time`/`_deactivation_time`, `sexmodAllieInUse`, `sexmodUses` (lamp wishes) |
