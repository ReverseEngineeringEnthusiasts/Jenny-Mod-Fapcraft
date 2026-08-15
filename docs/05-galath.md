# Galath & Manglelie — the succubus boss arc

Galath is the flying succubus boss girl; Manglelie is her imp "daughter" sidekick.
The full loop: **find → fight → knock out → corrupt → bind → own** (and ride).

## The wild Galath

- **Spawning**: wild Galaths spawn near **wither skeleton / blaze hives** in the Nether.
- **Flight AI** (`GalathFlightData` state machine, driven through IGalathStart/Update/Finish +
  GalathActionListener callbacks):
  - `CHANGE_POSITION` — pick a new flight position near the target (weighted by open air space), glide there.
  - `SUMMON_SKELETON` — charge two **energy balls** (DragonEntity) at her hands for `ad` ticks and fire
    them at the target; the charges explode into **wither skeleton guards** on block hit.
  - Sword attacks (ATTACK_SWORD), knock-out flight/ground states, and the **rape pounce**.
- **Multi-part hitbox**: two energy-ball hitboxes (SexEntityPart) exist exactly during the charge window.
- **The escape minigame**: when she pounces, the player must press the WASD key matching the blinking
  prompt (EscapeMinigameHud, prompt cycles every 35 ticks) to fill the bar; correct keys add,
  wrong keys subtract. Filling it then pressing an unrelated key sends GalathBackOffRapePacket
  (she backs off — the pounce is aborted); failure flashes the bar red and the scene continues.
- **Dragon charges**: DragonEntity projectiles — no-clip, dragon-breath particle trails; a player's
  attack reflects the charge back at her; contact with Galath flings her into the knockout state.

## Defeat → corrupt → bind

- **Knockout** (KNOCK_OUT_FLY/GROUND/STAND_UP): defeating her leaves her knocked out on the ground.
- **Corrupt**: interacting starts the corrupt scenes (CORRUPT_INTRO → SLOW → FAST → CUM).
- On completion she **grants a Galath coin** (GIVE_COIN) and becomes the player's **bound succubus**
  — ownership persisted in `GirlSavedData` ("sexmod:galath_owner_ship": player↔girl pairs,
  per-owner last-cum-dosage time, manglelie-owned player set).

## Owning & riding

- **Galath coin** (see [02-items.md](02-items.md)): summon (4s window, energy particles, server spawns
  her + grants ownership) and de-summon (right-click the owned Galath).
- **Riding**: RequestRidingPacket mounts the owner on her, switches her to `CONTROLLED_FLIGHT`
  and gives upward velocity. While ridden she is removed from her chunk (not ticked as a normal
  entity — required for the riding physics).
- **Flight HUD**: up to **3 boost charges** (GalathFlightHud): 3s cooldown between uses, charges
  regenerate one per 5s; each pip has an animated charge bar. BOOST action. Flight is also granted
  to the **player-form** Galath while transformed.
- **Bound scenes**: cowgirl, anal, threesome (with Manglelie), morning blowjob, masterbate,
  pussy-licking, GIVE_COIN. Rape variant cycling (RAPE_INTRO → RAPE_ON_GOING → RAPE_CUM).
- **Futa**: `/futa true|false` toggles her futa (see [10-dev-features.md](10-dev-features.md)).
- **Damage sources**: GalathDamageSource (her attacks), SuccubusDamageSource (drain attacks).
- **Knockout via her own charge**: if she's charging and a DragonEntity hits her, she's flung into
  knockout (setFlightVelocity).

## Manglelie ("Mang")

- **Adoption**: wild Mangles wander near hives; when a Galath finds one she adopts it and it rides
  her head (RIDE_MOMMY_HEAD / HUG_MANG actions).
- **Corruption beam**: while the owned Galath is in the corrupt state, Mang picks a nearby mob,
  holds it in a magical beam (arrow shot from the Galath at 28 ticks) and after ~60 ticks the mob
  converts into a **threesome scene** (THREESOME_SLOW/FAST/CUM — shared animations with Galath).
- **Personality AI**: avoids players (AvoidPlayerGoal) unless her "mommy" has a master; scared flag
  switches the movement animation to a run.
- **Models/poses**: corruption arm/head animation (frame-rate independent blend cycle), ride-mommy
  pose blend, threesome pose (body follows Galath's published rotation/scale), skirt/cheek
  visibility for the look pose; ManglelieRenderer adds the first-person POV wing mesh, a hand-built
  corruption skirt/ribbon mesh, held-bow rendering with corruption progress, and pose coupling to
  her Galath partner (aim yaw, ride/head actions, threesome mode).
- **Ownership**: tracked in GirlSavedData's manglelie set; InformOfOwnershipPacket mirrors ownership
  changes client-side.

## Persistence

`GirlSavedData` (world-saved "sexmod:galath_owner_ship"): bidirectional player↔girl UUID map,
last cum-dosage time per owner (drives despawn/cooldown), manglelie owners.
`GirlWorldData` ("sexmod:static_custom_model_manager"): UUID→model-code maps for Galath and
Manglelie custom outfits (two distinct maps — mixing garbles outfits on reload).
