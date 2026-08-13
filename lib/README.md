# lib/ — compile-time classpath jars

These jars are the hand-assembled Minecraft 1.12.2 + Forge classpath that the decompiled
source is compiled against. They are installed into `../lib-repo/` (a local Maven repo)
and resolved by `pom.xml`.

| jar | origin / construction |
|---|---|
| `mc-1.12.2-srg.jar` | vanilla `1.12.2` client.jar, remapped `obf → srg` with MCPConfig `joined.tsrg`; then: Forge default-AT fields made public (`Minecraft.field_71446_o`, `EntityLiving.field_70715_bh`, `field_70714_bg`); Forge-patched class hierarchy (`Item`, `Block`, `Potion`, `SoundEvent`, `PotionType` extend `IForgeRegistryEntry.Impl<Self>`); `Entity` ICapabilityProvider methods + `isAddedToWorld`; `Item.setTileEntityItemStackRenderer`; `LootEntryItem` 6-arg ctor; `SharedMonsterAttributes.SWIM_SPEED`; plus MCP-name stubs for the mod's mixed SRG/MCP references (`getPersistentID`, `getEntityData`, `getDefaultEyeHeight`, `ActiveRenderInfo.getCameraPosition`(static), `ModelBakery.registerItemVariants`(static), `OpenGlHelper.lastBrightnessX/Y`(static)). |
| `forge-1.12.2-srg.jar` | `forge-1.12.2-14.23.5.2860-universal.jar`, remapped `obf → srg` with the same `joined.tsrg`; `IForgeRegistryEntry` gained a correct `setRegistryName(String,String)` default method. |
| `geckolib3-1.12.2.jar` | `software/bernie/geckolib3/**` extracted from the mod jar. |
| `geckolib3-shadowed-libs.jar` | `software/bernie/shadowed/**` (relocated Jackson) extracted from the mod jar. |
| `apache-commons-lang3-1.12.2.jar` | `org/apache/commons/lang3/**` extracted from the mod jar. |
| `authlib`, `realms`, `patchy`, `lwjgl`, `lwjgl_util`, `launchwrapper` | from `libraries.minecraft.net`. |
| others | Maven Central (`log4j`, `vecmath`, `jsr305`, `netty-all`, `guava`, `gson`, `fastutil`, `commons-io`, `commons-codec`, `jna`). |

Regenerating `mc-1.12.2-srg.jar` from scratch is scripted in the tool sources
(`pipeline/tools/src/SrgRemap.java`, `ApplyAT.java`, `ForgePatch*.java`,
`McpOverlay.java`, `MorePatches.java`, `LootPatch.java`, `MakeStatic.java`,
`QuickPatches.java`). The order is:

```
SrgRemap   (obf→srg)          → mc-1.12.2-srg.jar
ApplyAT    (Forge default AT) → mc-1.12.2-srg-at.jar
ForgePatch (Impl as iface)    → mc-1.12.2-patched.jar      (superseded by ForgePatch3)
ForgePatch3(Impl as extends)  → mc-1.12.2-mcp5.jar
LootPatch  (6-arg LootEntryItem) → mc-1.12.2-mcp6.jar
QuickPatches (protected→public, static stubs) → mc-1.12.2-quick.jar
McpOverlay (MCP-name stubs)   → mc-1.12.2-mcp.jar
MorePatches (SWIM_SPEED, Potion/SoundEvent) → mc-1.12.2-mcp3.jar
```

The `ForgePatch2` 2-arg `setRegistryName` default method corrupted `IForgeRegistryEntry`
(bad maxStack); it was replaced by `FixDefault.java` with `COMPUTE_MAXS`.
