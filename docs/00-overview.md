# Fapcraft 1.12.2 — Complete Feature Documentation

This is the exhaustive feature reference for the mod (modid `sexmod`, "Fapcraft"),
reconstructed from the deobfuscated + renamed source in this repository.
It documents **every** feature, item, entity, GUI, animation, interaction,
network packet and dev/hidden feature — nothing is omitted.

## Index

| File | Covers |
|---|---|
| [01-entities.md](01-entities.md) | All 10 characters (NPCs + player forms), wild mobs, the girl hierarchy |
| [02-items.md](02-items.md) | Every item, the fire block, the horny potion, loot tables |
| [03-actions-scenes.md](03-actions-scenes.md) | The full action/sex-scene state machine, controls, camera, horny meter |
| [04-tribe-system.md](04-tribe-system.md) | Kobolds, tribes, the dragon staff, mining/woodcutting tasks |
| [05-galath.md](05-galath.md) | The Galath boss, her flight AI, corruption, the coin, Manglelie |
| [06-gui.md](06-gui.md) | Every GUI screen, button by button || [07-custom-models.md](07-custom-models.md) | The girl wand editor, model codes, custom parts, server model downloads |
| [08-commands.md](08-commands.md) | All commands, exact syntax |
| [09-networking.md](09-networking.md) | The full `sexmodchannel` packet protocol |
| [10-dev-features.md](10-dev-features.md) | Hidden/dev features: DebugMode, DebugWindow, SceneDebug, futa, etc. |
| [11-worldgen-config.md](11-worldgen-config.md) | Girl houses, spawns, `config/sexmod.json` |
| [12-translations-sounds.md](12-translations-sounds.md) | 16 languages, voice actresses, 700+ sound files |

## Quick overview

- **10 characters**: Jenny, Ellie, Bia, Luna, Slime girl, Bee, Allie, Kobold, Goblin, Galath (+ Manglelie, the imp sidekick).
- Each character exists as an **NPC** (spawns in the world / summoned) and most also as a **player form** (drink the horny potion to transform).
- **Horny potion** (`Potion of horny`) transforms the player into a girl; it is also the "payment" for Jenny's services.
- **The girl wand** opens the model-customization editor; **dragon staff** manages kobold tribes; **Allies lamp** summons the genie Allie; **Galath coin** binds a defeated Galath; **kobold egg / tribe egg** create kobolds; **Luna rod** is Luna's fishing rod.
- Everything is controlled through **right-click interaction menus** (one GUI per girl), with scenes driven by a shared action state machine (98 actions) over the `sexmodchannel`.
- **Custom model system**: servers can ship their own GeckoLib models (`.cfg` + `.png` + `.geo.json`) that clients download after whitelisting the server.
- **Adult content gate**: on startup an 18+ warning window ("I'm at least 18 years old!" / "I'm below the age of 18!") — declining deletes the content and closes the game.

## Mod metadata

- Modid `sexmod`, name **Fapcraft**, version 1.1.0
- Author: Trolmastercard (with the credits in `mcmod.info`)
- Mod URL: twitter.com/trolmastercard
- Requires Forge 1.12.2; depends on `after:geckolib` (GeckoLib 3, bundled/shaded)
- Channel: `sexmodchannel` (SimpleNetworkWrapper)
