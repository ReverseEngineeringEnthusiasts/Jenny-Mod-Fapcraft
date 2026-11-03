# Guide books in the reference modpacks

Research note for the first-join guide book (issue: "add a BOOK to their hotbar"). Question: do
the modpacks we were pointed at ship documentation as an inventory item, and what pattern do they
use?

## Method

For each Modrinth project, the latest `.mrpack` was downloaded and opened read-only:

- `modrinth.index.json` gives the pack's declared mod files.
- `overrides/` gives the extra jars and configs the pack author ships on top.
- Every jar was scanned for `assets/*/patchouli_books/`, `*book*`/`*guide*` assets and book lang
  keys.

Pack versions read: Mustard Virus 2.2.0, Winter Zombies 1.0.1, AHZNB Modpack 1.2.9, Space Craft
(Galacticraft) 10.2. All four are 1.12.2 Forge packs.

## Findings

| Pack | Mods (index + overrides) | Book / guide mods | What actually ships |
|------|--------------------------|-------------------|---------------------|
| Mustard Virus 2.2.0 | 11 + 36 | none | Quark, Biomes O' Plenty, Tough As Nails, JEI. No book item, no in-game manual. |
| Winter Zombies 1.0.1 | 10 + 22 | none | Serene Seasons, Tough As Nails, JEI. No book item, no in-game manual. |
| AHZNB Modpack 1.2.9 | 16 + 12 | Patchouli 1.0-23.6 | Patchouli is installed, but the pack ships no `patchouli_books/` content and no "Inventory Button Book". The Naruto jars contain no book assets. `config/anbubingobook.cfg` is a Naruto/Sharingan config file that kept the name; there is no bingo-book item behind it. |
| Space Craft (Galacticraft) 10.2 | 68 + 38 | Patchouli via Galactic Research | **Galactic Research 1.1.3** ships one Patchouli book, `galacticresearch:galacticresearch_guide`. It is a shapeless recipe (book + `galacticraftcore:basic_item` data 13), `show_progress:false`, in the Galacticraft creative tab. It is not handed to the player. |

## Takeaway for Fapcraft

None of the four packs puts a documentation item in the player's inventory. The only actual guide
is a crafted Patchouli book, and Patchouli is a hard dependency we do not need for one small book.

For the first-join guide we therefore use a vanilla `minecraft:written_book`:

- no item registration, texture, model, lang key or new dependency;
- the pages are plain text, so they can be edited without touching assets;
- it is granted once per save by `GuideBook.giveOnce` and recorded in `GuideBookData` (a per-save
  `WorldSavedData`), instead of every login.

The same book also covers the manual custom-model install path (`sexmod/custom_models/`), which is
what the "models stay on downloading forever" report needs as a fallback.
