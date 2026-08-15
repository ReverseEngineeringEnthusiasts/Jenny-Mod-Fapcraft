# Entities — every character and creature

All entity code lives in `com.trolmastercard.sexmod.entity` (plus `entity/ai`, `entity/api`).
Every girl type is registered in the `NpcType` enum with an NPC class and (most) a player-form class;
entity ids are persisted — never renumbered.

## The girl hierarchy

```
BaseGirlEntity                       shared girl state + scene lifecycle (data keys 99-110)
├─ AbstractNpcOnlyEntity             NPC-only girls: DNA/appearance params (keys 119-121)
│  └─ GoblinEntity                   (plus KoboldEntity below uses its own scheme)
├─ AbstractGirlNpcEntity             common NPC-girl behavior: 7-slot inventory, weapon/bow/armor
│  ├─ JennyEntity / BiaEntity / LunaEntity / EllieEntity / SlimeEntity
│  └─ BeeEntity / GoblinEntity / AllieEntity / ManglelieEntity / GalathEntity
├─ KoboldEntity                      tribe kobold (oral/anal/mating + tribe AI)
└─ AbstractPlayerGirlEntity          horny-potion player forms
   └─ Jenny/Bia/Luna/Ellie/Slime/Bee/Allie/Kobold/Goblin/Galath PlayerEntity
```

- **Data keys 99–110** (CUR_ACTION, IS_ANCHORED, TARGET_POS, MASTER, INTERACTION_PARTNER_UUID, OUTFIT_INDEX, GIRL_HAND_STATES, homePos, YAW_ROTATION, …) are shared, explicit IDs — do not renumber.
- NPC girls register in a global `girlList` (also mirrored in `GirlRegistry` by UUID) and are cleaned up on world close / death.

---

## Jenny (`entity.jenny.name` = Jenny)

The blond sword/bow girl. The "main" character.

- **Obtaining**: spawns in her house (worldgen), or is summoned by her house structure. NPC only via her home; player form via horny potion.
- **Combat**: melee with her sword, charged bow shots; follows the master, rides his mount, attacks hostiles and anyone who attacked her or her master (see `GirlFollowGoal`); enters a downed state at 0 HP.
- **Equipment**: 7-slot inventory — weapon, bow, helmet, chestplate, legs, boots (slot ids 0-5 in `GirlInventorySlot`; sword/tools/bow/armor enforced per slot).
- **Scenes** (NPC + player form): **Blowjob** (suck/thrust/cum), **Boobjob**, **Doggy** (bed: start/wait/slow/fast/cum), **Paizuri** (start/slow/fast/cum).
- **Payment gate**: Jenny refuses scene actions unless the player has the **horny potion effect** active (the potion IS the currency; the menu shows the "payment" state and she goes into `PAYMENT` action otherwise).
- **Scene entry flow**: right-click → interaction menu → pick action → she dismounts, walks ~40 ticks to her target position, anchors, then plays the chosen scene.
- **Dialogue**: "I am busy at the moment~", "Okay! I am right behind you %s~", "Hihi~", "show Bobs and vegana pls", "Give me the sucky sucky and these are yours" etc.
- **Loot table** registered (`loot_tables/`), drops on death.
- Can be named with a **name tag** (right-click sets CUSTOM_NAME).
- Goes home via ender-pearl-style teleport (`SendCompanionHomePacket` flow).

## Ellie (`entity.ellie.name` = Ellie)

The tall "mommy" girl (voice: @EndymionVA).

- **Obtaining**: her house; player form via horny potion.
- **Scenes**: **Cowgirl** (bed), **Missionary** (bed), and the carried **"Face fuck"** (carry intro/slow/fast/cum).
- **Sit-down flow**: Ellie has a couch/bed sit-down state machine (sitdown/sitdownidle) — she waits seated until the player comes within 1 block, then locks both in.
- **Hug**: hug/hugselected/hugidle dialogue phase before scenes.
- **Unique model**: her GeckoLib model has a custom sitting pose — while sitting idle her head tracks the nearest player within 15 blocks.
- **Dialogue**: "Mommy is horny", "Come to mommy~", "follow me, darling~", "Do you like what you see honey?~", "goooood.. ehh.. boy... hehe~".
- Player form: same scene set + sit-down interaction flow.
- **Loot table** registered.

## Bia (`entity.bia.name` = Bia)

The catgirl (voice: @MissMoonified).

- **Obtaining**: her house; player form via horny potion.
- **Scenes**: **Talk** (TALK_HORNY dialogue), **Headpat** (HEAD_PAT), **Strip** (STRIP), and bed scenes **Anal** (prepare/wait/start/slow/fast/cum) and **Prone doggy** (sitdown/sitdownidle/intro/insert/soft/hard/cum).
- **Bed walk**: Bia (like Jenny/Luna) is `IBeddableSexGirl` — she walks to the nearest bed before the scene starts.
- **Player form special**: in ANAL_WAIT/SITDOWNIDLE with a player within 1 block, a countdown locks both players in.
- **Model**: cat ears/leaf attachments, nude/dressed variants; renders with `BiaRenderer`.
- **Dialogue**: "Heya lets Go!", "Me is going home nya~", "I am Hornyyyyy~", "Ooh headpats!", "Tanku hehe".
- **Loot table** registered.
- Fixed-UUID special player (see PlayerIds).

## Luna (`entity.luna.name` = Luna)

The fishing catgirl (voice: @MacStarVA).

- **Obtaining**: her house; player form via horny potion.
- **Unique systems**:
  - **Fishing**: give her a `Luna rod` and pick "go fishing" — she casts a custom bobber entity (`SexEntity`, the "luna hook") that does a full vanilla fishing-fight simulation: flies, hooks entities/ground, bobs, rolls loot from `GAMEPLAY_FISHING`, and hands the catch to Luna. She then **eats** the fish (advance with "eat/reel in" tick) or **throws it away** (CatThrowAwayItemPacket).
  - **Equipment**: 7-slot inventory **with an extra fishing-rod slot** (only girl with one).
  - **Heart particles**: during her scenes small heart/particle markers (`SexEntity` renders) glide to her hand.
- **Scenes**: **Touch boobs** (intro/slow/fast/cum, preceded by a fish PAYMENT), **Sitting cowgirl** (intro/slow/fast/cum; she waits in WAIT_CAT with a 25-tick approach countdown), **Headpat**.
- **Dialogue / UI**: interaction menu with the action list; her fishing progress shown on the rod's cast model override.
- Player form: touch-boobs, cowgirl-sitting, headpat.

## Slime girl (`entity.slime.name` = Slime girl)

A jump-happy slime girl with a **pregnancy lifecycle**:

- Hops around (procedural jump animation; no AI tasks).
- Gets "impregnated" by the horny potion; grows; then births a **WildSlimeEntity** (a vanilla-slime-looking mob using the goblin body model) that matures over 8400 ticks back into a new Slime girl.
- Data keys: TICKS_UNTIL_BIRTH (0..2400 progression: 1 = undress, 2 = pregnant start, 4+ = scene-ready), TARGET_YAW (hop direction), HORNY_LEVEL (countdown to birthing; -1 = not pregnant).
- **Scenes**: Blowjob and Doggy (same action set as Jenny).
- Player form: same scenes, no pregnancy. Fall-damage immunity while transformed.
- Wild slimes: vanilla slime AI, split into two on death, tracked in `ALL_SLIMES` for egg-laying checks.

## Bee (`entity.bee.name` = Bee)

A flying girl with a **taming + chest** mechanic:

- Wild bees get horny over time (hornyTimer, 4800 ticks) and **approach the nearest player** to start their citizen scene by themselves.
- **Taming/chest**: once tamed (HORNY_FLAG), she carries a **27-slot chest inventory** — right-click opens the bee dialogue; when horny you can open her chest GUI (BeeOpenChestPacket).
- **Scenes**: citizen sex (start/slow/fast/cum).
- **Follow**: simplified follow goal (walks, hops terrain via air-jump factor).
- Player form: citizen scene + flight granted while transformed.
- Dialogue: "The bee looks sad", "The bee looks exited", "The bee likes its new home".
- Model: bee geo (nude/armored), chest-bone visibility toggled by animation name.

## Allie (`entity.allie.name` = Allie)

The **summoned lamp genie girl** (from the Allies Lamp item) — the "3 wishes" character:

- **Summon**: rubbing the lamp for 95 ticks (right-click; rub animation + ramping particles) summons her 2 blocks in front of the player. She hovers (gravity/clip disabled) during the summon intro, then opens her interaction menu.
- **Wishes**: 3 wishes per lamp stack (tracked in NBT `sexmodUses`): 1st = summon, 2nd = (scene), 3rd = **Make me rich!** (spawns 1-2 diamonds, emeralds, gold ingots — MakeRichWishPacket).
- **Phobia**: Allie refuses to be summoned onto **sand** (SUMMON_SAND branch: "Allie has a phobia about sand. Summon her somewhere, where there is none of that").
- **Scenes**: Deepthroat and Reverse cowgirl.
- Despawns when idle (Action.NULL).
- **AllieWorldData** persists tamed Allie state per world.
- Player form: deepthroat + reverse cowgirl, no summon/wish flow, flight while transformed.
- **Lamp rendering**: the lamp item model is textured with the *holder's own Minecraft skin* (skin face pixel-blended onto the lamp).
- Dialogue: "HIIYAAYA!", "Congratulations mortal~", "you got yourself... 3 wishes!", "2 wishes left!", "one last wish", "Here ya go! *thanos snaps*", "OOOH HECK NO!!1".

## Kobold (`entity.kobold.name` = Kobold)

The tribe creature — see [04-tribe-system.md](04-tribe-system.md) for the full tribe system. Quick facts:

- **Scenes**: oral (blowjob), anal, mating-press; THROW_PEARL (ender-pearl teleport home, limited to 5 blocks of home).
- **Appearance**: size, eye color, body color from the `EyeAndKoboldColor` palette (default PURPLE), custom DNA string; model applies horn variants (up/down), boob/eye scales, freckles, backpack/tailpack, crown/egg visibility.
- **Reproduction**: KoboldEggItem (placed) → KoboldEggEntity sits 12000 ticks with escalating wiggle animations → hatches into a new kobold joined to the tribe. Eggs come in wool colors.
- **AI**: tasks (mining, woodcutting), combat, breeding, follow modes; hostile mobs can hunt tribe members (NearestAttackableGirlGoal).
- Player form: blowjob/anal/mating-press + full appearance customization; can also be picked up/thrown like a goblin? (no — that's goblin; kobold player gets size scaling in [0, 0.25]).

## Goblin (`entity.goblin.name` = Goblin)

The tamed shoulder-rider/throwable goblin:

- **Pick up / throw / catch**: a tamed goblin can be picked up (PICK_UP → SHOULDER_IDLE, rides the owner's shoulder with first-person camera bob), **thrown at players** (START_THROWING → THROWN → STAND_UP), and **caught**. Controls via the goblin interact key (G, key 34) + the direction-pad GalathScreen.
- **Queen system**: a goblin can be a **queen** (SIT on a throne, guards, breeding scenes, **stolen gold** — she can steal an item stack from a player, data key 124).
- **Scenes**: paizuri, nelson, breeding (with pregnancy flag).
- **Combat**: thrown at enemies damages them; a goblin caught mid-throw lands in your hands.
- **Appearance**: DNA model code: hair color (index 6), skin color (index 7), eye color (index 8), body variants; color palette shared with kobolds.
- Player form: can itself be picked up and thrown like the NPC goblin (the *owning player* is the entity thrown); nelson/paizuri scenes; breeding scenes when acting as queen.
- **First-person rendering**: GoblinFirstPersonRenderer draws the goblin in first person with pick-up/throw animations replacing the vanilla hand.

## Galath (`entity.galath.name` = Galath)

The flying succubus **boss** girl — see [05-galath.md](05-galath.md) for the full write-up:

- Wild Galaths spawn near wither skeleton/blaze hives; fly with a full flight AI state machine (CHANGE_POSITION, SUMMON_SKELETON, sword attacks, knock-out, rape pounce).
- Defeat → knockout → **corrupt** → she grants a **Galath coin** and becomes your bound succubus (cowgirl/anal/threesome/morning-blowjob scenes).
- Player form: rape + corrupt scenes, flight, dash boost, escape minigame.

## Manglelie ("Mang")

The small imp girl, Galath's "daughter" sidekick (NPC-only, no player form):

- Wild Mangles wander near hives; a Galath **adopts** one (rides her head — RIDE_MOMMY_HEAD).
- **Corruption**: while her Galath corrupts a mob, Mang holds it in a magical beam for ~60 ticks and converts it into a **threesome scene** (shared animations with the Galath).
- Avoids players (AvoidPlayerGoal) — but never runs away while her "mommy" has a master.
- Model: corruption arm/head animation, ride-mommy pose blend, threesome pose (body follows Galath's published rotation/scale), skirt/cheek visibility.
- Ownership tracked in GirlSavedData (manglelie-owned players set).

## Other entities

| Entity | Class | Notes |
|---|---|---|
| **Pyrocinical girl** | `BasicGirlEntity` | Ambient filler mob, spawns rarely in the **Nether**, wanders/follows players, despawns 60s after being hit. Billboard renderer with 2-frame walk, praise/practice pose, and a 30-tick "fat" morph animation on sound. Never actually takes damage. |
| **Wild slime** | `WildSlimeEntity` | Vanilla-style slime (GoblinModel body), squish factor, splits on death, matures into Slime girl after 8400 ticks. |
| **Kobold egg (placed)** | `KoboldEggEntity` | Hatchable egg entity, 12000-tick incubation, wiggle animations, tribe binding. |
| **Kobold egg (projectile)** | `KoboldEggProjectileEntity` | Ender-pearl for girls' teleport-home (5-block home limit). |
| **Luna hook** | `SexEntity` | Luna's fishing bobber: full vanilla fishing-fight sim, hooks entities, rolls GAMEPLAY_FISHING loot. |
| **Energy ball** | `DragonEntity` | Galath's charge projectile: flies, spawns dragon-breath particles, converts into a wither skeleton guard on block hit, or explodes on Galath contact (knockout); player attacks reflect it. |
| **Custom model anchor** | `SexSceneEntity` | Invisible marker entity that positions custom model parts in the world/scene; also the wardrobe preview mode. |
| **SexScenePart** | `SexEntityPart` | Multi-part hitbox for Galath's two energy-ball hitboxes, active only while charging. |

## AI goals (entity/ai)

- `GirlFollowGoal` — the companion combat-follow state machine: ride → target → attacker → master's target → nearby mobs → follow/idle; melee or charged bow; downed at 0 HP; teleport-fallback navigation; master's sprint affects follow distance.
- `GirlGotoGoal` — bee's simple follow (no combat), air-jump for terrain.
- `WatchClosestGirlGoal` — "watch player" with a software kill-switch while following/attacking.
- `AvoidPlayerGoal` — Manglelie flee (with mommy-with-master exception), sets "scared" flag.
- `DoorInteractAiGoal` — girls open/close wooden doors on their path (ground navigators only).
- `NearestAttackableGirlGoal` — hostile mobs hunt tamed kobolds (darkness + radius chance gate).
- `GirlAiBase` — leftover vanilla villager-breeding AI, **dead code** (not attached anywhere).

## Combat/armor/damage

- `GirlCombatProtection`: girls in a scene and player-girls are invulnerable to normal damage (void damage always applies); the owner standing within 1 block of his girl is protected too (except void/succubus damage).
- `DamageCalculation`: full custom armor formula for NPC girls (vanilla armor/toughness table, Protection/Thorns/Fire/Blast/Feather-Falling/Projectile enchantments, Thorns reflected).
- `LivingDeathHandler`: cleans dead girls from the global lists.
