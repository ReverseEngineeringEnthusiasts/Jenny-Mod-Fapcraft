# Translations, Sounds & Credits

## Translations (16 languages)

`assets/sexmod/lang/`:

| File | Language | Translator (per mcmod.info) |
|---|---|---|
| `en_us.lang` (+ `en_id.lang`) | English | — |
| `pl_pl.lang` | Polish | @Wiktor82178716 |
| `tr_tr.lang` | Turkish | @MasterAdam1234 |
| `zh_cn.lang` | Chinese | @yimamiantang |
| `ru_ru.lang` | Russian | @PeiMochy |
| `es_es.lang` | Spanish | @eddyslejandro |
| `hu_hu.lang` | Hungarian | Lobasz |
| `id_id.lang` (`en_id`) | Indonesian | @AsiangalM |
| `bg_bg.lang` | Bulgarian | @fursainty |
| `nl_nl` → `ne_be`? | Dutch | @Bongos25270749 |
| `fi_fi.lang` | Finnish | @ImLegitment |
| `he_il.lang` | Hebrew | @Atlantic281 |
| `it_it.lang` | Italian | @RkTzXq1 |
| `fr_fr.lang` | French | @MarcLewisNSFW |
| `de_de.lang` | German | — |

## Voice actresses (from mcmod.info credits)

| Character | Voice |
|---|---|
| Jenny | @Lizzywaffler |
| Ellie | @EndymionVA |
| Bia | @MissMoonified |
| Luna | @MacStarVA ("being super cute and awesome :))") |
| Kobolds | @FlirtyFawn696 |

## Sounds (~719 files in `assets/sexmod/sounds/`)

- **Per-girl folders**: allie, bia, ellie, galath, jenny, kobold, luna (+ more) — dialogue/sex
  voice lines, registered by `SoundHandler` as arrays per field.
- **Misc scene/effect sounds**: bedrustle, beew, belljingle, clap, cuminflation, eat, fart,
  flap, inserts, jump, … (the folder contents were normalized/renamed by the
  `normalizse.py` / `renameSounds.py` / `writeSoundFile.py` helper scripts in the sound folder).
- `SoundHandler.randomSound` picks a random variant per array without repeating the last one;
  the registry-name derivation (lowercased field name, `_` → `.`) is load-bearing.
- One credited external sound: "Magical Whoosh.m4a" by JalynCatbtg (Freesound, CC BY 4.0),
  used edited in the mod. The Anime Blush PNG comes from pngall.com (CC BY-NC 4.0), edited.

## Animation/geo assets

- Per-character GeckoLib animations: allie (incl. lamp), bee, bia, cat, ellie, galath
  (incl. coin), goblin, jenny, kobold (incl. egg + staff), manglelie, slime, plus the generic
  `model.animation.json` and the `cross` geo (custom parts).
- Geo models per character with **dressed/nude variants** (jenny: jennydressed/jennynude;
  bia: biadressed/bianude; ellie: dressed/nude; slime: dressed/nude/armored) and
  **armored variants** for allie, bee, goblin, kobold, slime; galath has the combined
  `galath_con_mang` pose geo; a `cat` geo for the (cat) NPC; `koboldegg` / `staff` /
  `galath_coin` / `lamp` item geos.

## Other credits

- @OdysseyEllie — built the characters' homes (the worldgen houses).
- KellyLoveyness (DarkPleasures) — lag fixes.
- u/Shurifera — pixel-art characters.
- @MoriRoseMC — animation teaching.
- 16 translators (see table).
