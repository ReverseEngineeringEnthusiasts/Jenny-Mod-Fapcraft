# Translations, Sounds & Credits

## Translations (15 language files)

`assets/sexmod/lang/` (15 files; the vanilla locale list only loads the ones
the client's locale maps to):

| File | Language | Translator (per mcmod.info) |
|---|---|---|
| `en_us.lang` | English | — |
| `en_id.lang` | Indonesian | @AsiangalM |
| `pl_pl.lang` | Polish | @Wiktor82178716 |
| `tr_tr.lang` | Turkish | @MasterAdam1234 |
| `zh_cn.lang` | Chinese | @yimamiantang |
| `ru_ru.lang` | Russian | @PeiMochy |
| `es_es.lang` | Spanish | @eddyslejandro |
| `hu_hu.lang` | Hungarian | Lobasz |
| `bg_bg.lang` | Bulgarian | @fursainty |
| `ne_be.lang` | Dutch (locale `nl_nl`) | @Bongos25270749 |
| `fi_fi.lang` | Finnish | @ImLegitment |
| `he_il.lang` | Hebrew | @Atlantic281 |
| `it_it.lang` | Italian | @RkTzXq1 |
| `fr_fr.lang` | French | @MarcLewisNSFW |
| `de_de.lang` | German | — |

> Note: there is **no** `nl_nl.lang` — the Dutch translation ships as
> `ne_be.lang` (Nepali/Belarusian locale code), which is what the original
> mod published. `en_us.lang` is the canonical key source; all other files are
> partial translations.

## Voice actresses (from mcmod.info credits)

| Character | Voice |
|---|---|
| Jenny | @Lizzywaffler |
| Ellie | @EndymionVA |
| Bia | @MissMoonified |
| Luna | @MacStarVA ("being super cute and awesome :))") |
| Kobolds | @FlirtyFawn696 |

## Sounds (719 events, 748 files)

`sounds.json` registers **719 sound events** pointing at **748 audio files**
under `assets/sexmod/sounds/`:

| Group | Events | Notes |
|---|---|---|
| `girls.jenny` | 106 | voice + sex lines |
| `girls.luna` | 161 | largest set (fishing + scenes) |
| `girls.allie` | 101 | |
| `girls.ellie` | 97 | |
| `girls.kobold` | 72 | |
| `girls.galath` | 62 | |
| `girls.bia` | 35 | |
| `misc` | 85 | bedrustle, beew, belljingle, clap, cuminflation, eat, fart, flap, inserts, jump, … |

- **Per-girl folders**: `sounds/girls/<name>/<category>/<name><n>.ogg`
  (e.g. `girls/galath/moan/moan0.ogg`), registered by `SoundHandler` as arrays
  per field; the registry name derivation (lowercased field name, `_` → `.`)
  is load-bearing.
- `SoundHandler.randomSound` picks a random variant per array without repeating
  the last one.
- One credited external sound: "Magical Whoosh.m4a" by JalynCatbtg (Freesound,
  CC BY 4.0), used edited in the mod. The Anime Blush PNG comes from pngall.com
  (CC BY-NC 4.0), edited.

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
