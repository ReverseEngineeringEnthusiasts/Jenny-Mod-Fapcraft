# Worldgen & Configuration

## Girl houses

`GirlHouseGenerator` generates the girls' houses in the overworld — the structures where NPC
girls spawn and call home (`homePos`). Generation is routed through `ConfigWorldGenHandler`
(a `WorldSavedData` + `IWorldGenerator`; saved under `sexmod:generation`), per configured
generator (`IWorldGen` interface).

### Biome rules (from `ConfigWorldGenHandler` constructor, verbatim)

| Girl | Biomes | Structure size (blocks) | Max height diff | Flatten ground |
|---|---|---|---|---|
| Ellie | REDWOOD_TAIGA, COLD_TAIGA, TAIGA, ROOFED_FOREST | 30×27×26 | 9 | yes |
| Jenny | PLAINS, FOREST | 9×4×9 | 1 | yes |
| Bia | MUTATED_BIRCH_FOREST, BIRCH_FOREST | 11×9×15 | 2 | yes |
| Luna | OCEAN, DEEP_OCEAN | 3×7×10 | 0 | no |

(Note: the Ellie rule appears **twice** in the constructor — duplicated in the
original source, harmless since generated positions are deduped by the
156-block same-girl / 62-block any-girl minimum distance check.)

- Structure placement requires: matching biome, flatness within `maxHeightDiff`,
  and no other girl's house within 62 blocks (156 for the same girl). Flattening
  fills below the structure with grass/dirt.
- Houses are loaded from `assets/sexmod/structures/*.nbt` (alex, bia, ellie,
  goblin, jenny, luna, ssa) via the vanilla `Template`/`TemplateManager` API.
- **Goblin lairs** spawn via a separate chunk scan: up to 4 candidate air spots
  around a 12×3×12 box, rotated to face the found wall, spawning a
  `GoblinEntity` (`forceSpawn=true`).
- **Kobold tribes** spawn randomly: 0.4% per chunk (`random.nextDouble() > 0.004`
  rejects), non-liquid surface, via `KoboldManager.spawnKoboldAt`.
- Nothing generates in `WorldType.FLAT` worlds.

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
