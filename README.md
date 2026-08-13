# Fapcraft 1.12.2 — Remapped Reconstruction (packages + classes)

The deobfuscated reconstruction from `../Fapcraft-1.12.2`, **restructured into role-based
packages** and with all **obfuscated class names replaced by meaningful names**. Maven
buildable, zero warnings, zero errors.

## Why this project exists

The parent `Fapcraft-1.12.2/` project is byte-identical to the official Fapcraft v1.1,
deobfuscated and buildable — but every class sat in one flat package
(`com.trolmastercard.sexmod`) and ~40 classes still carried ZKM short names (`a4`,
`bs`, `gj`, `x`, `y`, …). This project fixes that:

- **folders** — classes moved into role packages mirroring the official source layout
  (`entity/`, `client/gui/`, `client/model/`, `client/renderer/`, `item/`,
  `networking/`, `command/`, `worldgen/`, `proxy/`, `util/`, …);
- **class names** — every remaining obfuscated class got a meaningful name;
- **registration helpers** — static init/register methods renamed to `register`.

## Package layout

| package | contents |
|---|---|
| `com.trolmastercard.sexmod` | `Main` + single-letter helpers kept at root |
| `.entity` | all entities + `NpcType`, `BodyParts`, `BoneType`, `EyeAndKoboldColor`, `AnimationState` |
| `.entity.ai` | AI goals (follow, goto, attack, door-interact) |
| `.entity.api` | NPC marker interfaces (`IGoblin`, `IGalath`, `IEllie`, `IKobold`) |
| `.client.model` | GeckoLib models + vanilla `ModelBase` models |
| `.client.model.api` | `IGirlModelInfo`, `IVanillaModel` |
| `.client.renderer` | entity + item renderers, layers |
| `.client.gui` | screens, containers, HUDs, `GuiHandler` |
| `.client.particle` | `DragonBreathParticle` |
| `.client` | `SexWorldClient`, `SexNetworkManager`, `ClientNetHandlerOverride`, camera/shader helpers |
| `.item` | all items |
| `.networking` | `PacketHandler` + all 48 packets |
| `.command` | all commands |
| `.worldgen` | `GirlHouseGenerator`, `ConfigWorldGenHandler`, `IWorldGen` |
| `.block` / `.potion` | `SexFireBlock`, `HornyPotion` |
| `.proxy` | `CommonProxy`, `ClientProxy` |
| `.util` | managers, handlers, saved data, helpers |
| `.api` | loose interfaces/enums |

## Class renames applied

| old | new | old | new |
|---|---|---|---|
| `a4` | `WildSlimeFaceLayer` | `bs` | `KoboldTask` |
| `bi` | `SexModEntities` (entity registry) | `bn` | `ForgeEventHandler` |
| `ez` | `DragonBreathParticle` | `f5` | `ClientNetHandlerOverride` |
| `gj` | `SexWorldClient` | `g6` | `UnknownPacket` |
| `h8` | `GalathFlightData` | `y` | `SkinFetcher` |
| `x` | `SexNetworkManager` | `l` | `PositionData` |
| `d` | `MobPredicates` | `p` | `MatrixHelper` |
| `u` | `GalathActionListener` | — | — |

The NPC/entity/item/packet class names came from the original reconstruction
(`NpcType` enum ground truth + `@ActionName` strings). See
`../Fapcraft-1.12.2/pipeline/README.md` for the full reconstruction pathway.

## Member names — status

- Class-level remap: **done**.
- `values()` / `valueOf()`: restored (javac synthesizes them for enums).
- `IMessageHandler.onMessage`, `func_110775_a` (getEntityTexture), GeckoLib
  `getModelLocation`/`getTextureLocation`/`getAnimationFileLocation`, bone getters:
  restored during the original reconstruction.
- Static registration helpers (`a_clashN` → `register`): done.
- The mod's own ZKM-renamed members that kept their bytecode names (≈5500
  `a_clashNNN` methods) still carry the ZKM-original names. **Semantic member
  remapping is the remaining frontier** — each needs the per-method analysis that
  produced the class names (reference: the 2021 non-obfuscated source and 1.21.1
  ports in `../Fapcraft-1.12.2/pipeline/reference/`).

## Build

```bash
mvn clean package
# -> target/fapcraft-1.12.2-1.1.0.jar   (zero warnings, zero errors)
```

Identical classpath setup to the parent project (`lib/`, `lib-repo/`); see
`../Fapcraft-1.12.2/lib/README.md`.
