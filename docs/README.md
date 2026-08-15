# Fapcraft 1.12.2 — Technical Documentation

The exhaustive, source-grounded reference for the Fapcraft mod (modid `sexmod`,
"Fapcraft"), reconstructed from the deobfuscated + renamed source in this
repository. Every claim here is verifiable against the code and assets in
`src/main/java` and `src/main/resources`.

## Documentation layout

```
docs/
├── README.md                  ← you are here: index, quick overview, metadata
├── characters/                ← one file per character: every animation, scene,
│   ├── jenny.md                 feature, dialogue line, sound, model detail
│   ├── ellie.md
│   ├── bia.md
│   ├── luna.md
│   ├── slime.md
│   ├── bee.md
│   ├── allie.md
│   ├── kobold.md
│   ├── goblin.md
│   ├── galath.md
│   └── manglelie.md
└── systems/                   ← cross-cutting mechanics, not tied to one girl
    ├── actions-scenes.md      ← the Action state machine, scene flow, controls
    ├── items.md               ← every item, block, potion, loot table
    ├── tribe-system.md        ← kobold tribes, dragon staff, tasks
    ├── gui.md                 ← every screen, container, HUD, keybind
    ├── custom-models.md       ← model codes, wardrobe, server-pushed models
    ├── networking.md          ← the sexmodchannel packet protocol
    ├── commands.md            ← every command, exact syntax, permission levels
    ├── worldgen-config.md     ← girl houses, natural spawns, config/sexmod.json
    ├── translations-sounds.md ← 16 languages, voice actresses, sound layout
    └── dev-features.md        ← hidden/dev features, dead code, code quirks
```

## Quick overview

- **11 characters**: Jenny, Ellie, Bia, Luna, Slime girl, Bee, Allie, Kobold,
  Goblin, Galath, Manglelie. Each character exists as an **NPC** (spawns in the
  world or is summoned) and most also as a **player form** (drink the horny
  potion to transform).
- **Horny potion** (`Potion of horny`) transforms the player into a girl; it is
  also the "payment" for Jenny's services.
- **Key items**: the **girl wand** opens the model-customization editor; the
  **dragon staff** manages kobold tribes; the **Allies lamp** summons the genie
  Allie; the **Galath coin** binds a defeated Galath; **kobold egg / tribe egg**
  create kobolds; the **Luna rod** is Luna's fishing rod.
- Everything is controlled through **right-click interaction menus** (one GUI
  per girl), with scenes driven by a shared action state machine (170 actions
  in the `Action` enum) over the `sexmodchannel` network channel.
- **Custom model system**: servers can ship their own GeckoLib models
  (`.cfg` + `.png` + `.geo.json`) that clients download after whitelisting the
  server.
- **Adult content gate**: on startup an 18+ warning window
  ("I'm at least 18 years old!" / "I'm below the age of 18!") — declining
  deletes the content and closes the game.

## Mod metadata

- Modid `sexmod`, name **Fapcraft**, version 1.1.0
- Author: Trolmastercard (credits in `mcmod.info`)
- Requires Forge 1.12.2; depends on `after:geckolib` (GeckoLib 3, bundled/shaded)
- Network channel: `sexmodchannel` (SimpleNetworkWrapper)

## Reading order

New to the codebase? Read in this order:

1. `characters/jenny.md` — the "main" character; introduces the common girl
   hierarchy, scene flow and interaction patterns used by everyone.
2. `systems/actions-scenes.md` — the shared scene state machine.
3. `characters/galath.md` — the boss arc (fight → corrupt → bind → ride).
4. `systems/tribe-system.md` — the kobold tribe management system.
5. `systems/networking.md` — the wire protocol, if you touch packets.
6. Everything else as needed.
