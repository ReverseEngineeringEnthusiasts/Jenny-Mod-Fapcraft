# Items, Blocks, Potions, Loot

## Items

All registered through `ItemRegistrationHandler`. Registry names in `en_us.lang` shown where set.

### Girl wand (`item.npc_editor_wand.name` = Girl wand)

The customization tool (also called "NPC editor wand"):

- **Right-click a girl** → opens the **ClothingScreen** (model-code editor, see [07-custom-models.md](07-custom-models.md)).
- **Left-click/attack a girl** → copies her model-code + part-ids to the **clipboard** as `code$parts`.
- **Damage value** while held switches the held model between normal/active display.
- The `?` button in its GUI opens the tutorial video (girl_wand.mp4, now archived at web.archive.org); the folder button opens the `sexmod/custom_models/singleplayer` folder.

### Dragon Staff (`item.dragon_staff.name` = Dragon Staff)

The **tribe-command tool** (see [04-tribe-system.md](04-tribe-system.md)):

- Right-click opens the **StructureCommandScreen** (direction pad): mark/unmark chests & beds, toggle tribe follow mode, toggle staff rendering mode, fell a tree / mine a tunnel, cancel tasks.
- Right-clicking a bed/chest while holding it is blocked (tribe blocks only managed through the UI).
- **Rendering**: GeckoLib staff model + spinning end-crystal on the `staff` bone, bob animation, screen-space sway, orbiting colored-wool-particle ring (particles = tribe markers: bed/chest/mine positions). Dual mode: particles in view direction vs bone chain.
- Drawn markers: `StructureMarkerRenderer` — blue = bed, green = chest, red = mine target, drawn only while the staff is held.
- Never places anything on right-click.

### Tribe egg (`item.tribe_egg.name` = Tribe egg)

Right-click spawns a **whole new kobold tribe** (4 kobolds, random color) at the targeted block via `KoboldManager.spawnKoboldAt`. Consumed unless creative.

### Kobold egg (`item.kobold_egg_item.name` = Kobold egg)

Placing it spawns a **KoboldEggEntity** colored by the item's metadata (wool color). A `tribeID` NBT tag (written by SendEggPacket) binds the hatched kobold to that tribe. Handed out by the dragon-staff UI ("give me a tribe egg").

### Allies Lamp (`item.allies_lamp.name` = Allies Lamp)

Summons the genie girl **Allie** (see [01-entities.md](01-entities.md)):

- Right-click starts a **95-tick rub animation** (client shows the rub + ramping particles, server spawns the Allie at tick 95, 2 blocks in front of the player).
- **3 wishes per stack** (NBT `sexmodUses`); third wish = "Make me rich!" (diamonds/emeralds/gold).
- Lamp is **injected into dungeon/mineshaft loot**.
- Renders with the **holder's own Minecraft skin** blended over the lamp texture.
- Can't be used while transformed into a player-girl, or while an Allie already holds the same lamp.

### Galath coin (`item.galath_coin.name` = Galath coin)

Binds a defeated succubus to her victor and summons/dismisses her:

- Right-click: **4-second summon window** with energy particles streaming to the summon point; the server then spawns the Galath and grants ownership (persisted in `GirlSavedData`).
- Right-click the **owned Galath** → de-summon animation.
- Held coin renders with a red tint, a "pentagram" bone, and animation states from persistent NBT timestamps (`sexmod:galath_coin_activation_time` / `_deactivation_time`): spin-up (lightmap 240→120, bright→dark) alternating with deactivation fade over 2s windows; idle coins bob and stay bright red.

### Luna rod (`item.luna_rod.name` = Luna's rod)

Luna's fishing rod:

- A fishing rod with a `cast` model override driven by her data manager.
- `castFishingRod` is invoked by CatActivateFishingPacket server-side: retrieves the existing `SexEntity` bobber or spawns a new one aimed at her fishing target; **fishing-speed/luck enchantments** from the rod apply.

### Winchester (`sexmod:winchester`, no lang name)

A placeholder GeckoLib-animated item (SummonItemModel/SummonItemRenderer) with **no gameplay logic yet**. Its renderer does a fake "depth shading" pass (cube tint by normal direction, GL lighting disabled).

### Secret map (`item.item_map_secret.name` = hehe)

An in-hand map rendered for transformed player-girls — see `InHandMapRenderer` (renders the map + the girl's hand model in first person). The item name in the lang file is literally "hehe".

### Non-spreading fire (`tile.fire_no_spread.name` = non-spreading fire)

**SexFireBlock** registered as `sexmod:fire` — inert fire whose updateTick is deliberately empty (never spreads or burns). Used by the Galath/dragon effects (dragon breath, energy balls, fire effects).

## Potion

### Horny potion (`potion.effect.horny_potion` = Potion of horny)

The mod's transformation potion:

- Drinking it **transforms the player into a girl** (spawns the `XxxPlayerEntity` form of the chosen type, hides the vanilla player; revert = drink again / right-click player form picker — see `GirlScreenBase`).
- Available as potion, splash potion, lingering potion and **tipped arrow**.
- Girls check the effect for the **payment gate** (Jenny's scenes, Luna's menu) — with the effect active, scenes are free.
- Drives pregnancy for Slime girl.

## Loot tables

Registered for **Jenny, Ellie, Slime, Bia** (`loot_tables/bia.json`, `loot_tables/ellie.json` in assets; the others via LootTableHandler). Also: Allies lamp injected into dungeon/mineshaft chests; Luna's fishing rolls vanilla `GAMEPLAY_FISHING` loot.

## Sounds (summary)

~719 sound files under `assets/sexmod/sounds/`:
- Per-girl voice folders: allie, bia, ellie, galath, jenny, kobold, luna (voice actresses in mcmod.info credits).
- Misc scene/effect sounds: bedrustle, beew, belljingle, clap, cuminflation, eat, fart, flap, inserts, jump, … (all registered by SoundHandler via reflection over its arrays; field names are load-bearing for the sound paths).
