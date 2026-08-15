# The Action/Scene System — every action, every mechanic

All girls (NPCs and player forms) share one scene state machine: the `Action` enum
(170 constants in `entity/Action.java`), stored in the `CUR_ACTION` data-manager
key and animated through GeckoLib animation controllers
(`GirlAnimationController`).

## The 170 actions

Each constant is declared with up to 7 fields: `length` (ticks before the
sound-listener/counter transition), `lengthIsTransition` (whether `length` is a
GeckoLib transition vs. a hard tick counter), `lengthIsFollowUp`,
`isAnchored`/`isTouching` flags, and optional pitch/yaw/lock parameters. The
full declaration list (verbatim from `Action.java`, lines 65+):

```
NULL  STARTBLOWJOB  SUCKBLOWJOB  SUCKBLOWJOB_BLINK  CUMBLOWJOB  THRUSTBLOWJOB  PAYMENT  STARTDOGGY
WAITDOGGY  DOGGYSTART  DOGGYSLOW  DOGGYFAST  DOGGYCUM  STRIP  DASH  HUG
HUGIDLE  HUGSELECTED  UNDRESS  DRESS  SITDOWN  SITDOWNIDLE  COWGIRLSTART  COWGIRLSLOW
COWGIRLFAST  COWGIRLCUM  ATTACK  BOW  RIDE  SIT  THROW_PEARL  DOWNED
PAIZURI_START  PAIZURI_IDLE  PAIZURI_SLOW  PAIZURI_FAST  PAIZURI_FAST_CONTINUES  PAIZURI_CUM  MISSIONARY_START  MISSIONARY_SLOW
MISSIONARY_FAST  MISSIONARY_CUM  TALK_HORNY  TALK_IDLE  TALK_RESPONSE  ANAL_PREPARE  ANAL_WAIT  ANAL_START
ANAL_SLOW  ANAL_FAST  ANAL_CUM  KOBOLD_ANAL_START  KOBOLD_ANAL_SLOW  KOBOLD_ANAL_FAST  KOBOLD_ANAL_CUM  SUMMON
SUMMON_WAIT  HEAD_PAT  ALLIE_PREPARE_FIRST_TIME  DEEPTHROAT_START  DEEPTHROAT_SLOW  DEEPTHROAT_FAST  DEEPTHROAT_CUM  ALLIE_PREPARE_NORMAL
SUMMON_NORMAL  SUMMON_SAND  SUMMON_NORMAL_WAIT  RICH_FIRST_TIME  RICH_NORMAL  CITIZEN_START  CITIZEN_SLOW  CITIZEN_FAST
CITIZEN_CUM  FISHING_START  FISHING_IDLE  FISHING_EAT  FISHING_THROW_AWAY  TOUCH_BOOBS_INTRO  TOUCH_BOOBS_SLOW  TOUCH_BOOBS_FAST
TOUCH_BOOBS_CUM  WAIT_CAT  COWGIRL_SITTING_INTRO  COWGIRL_SITTING_SLOW  COWGIRL_SITTING_FAST  COWGIRL_SITTING_CUM  MINE  SLEEP
MATING_PRESS_START  MATING_PRESS_SOFT  MATING_PRESS_HARD  MATING_PRESS_CUM  SHOULDER_IDLE  PICK_UP  RUN  CATCH
CATCH_BJ  CATCH_BJ_IDLE  START_THROWING  THROWN  JUMP_0  JUMP_1  JUMP_2  BREEDING_INTRO_0
BREEDING_INTRO_1  BREEDING_INTRO_2  BREEDING_SLOW_0  BREEDING_1  BREEDING_SLOW_2  BREEDING_FAST_0  BREEDING_FAST_2  BREEDING_CUM_0
BREEDING_CUM_1  BREEDING_CUM_2  AWAIT_PICK_UP  VANISH  STAND_UP  NELSON_INTRO  NELSON_SLOW  NELSON_FAST
NELSON_CUM  CARRY_SLOW  CARRY_FAST  CARRY_CUM  CARRY_INTRO  PRONE_DOGGY_INTRO  PRONE_DOGGY_SOFT  PRONE_DOGGY_HARD
PRONE_DOGGY_INSERT  PRONE_DOGGY_CUM  REVERSE_COWGIRL_SLOW  REVERSE_COWGIRL_FAST_START  REVERSE_COWGIRL_FAST_CONTINUES  REVERSE_COWGIRL_CUM  REVERSE_COWGIRL_START  WAVE_IDLE
WAVE  FLY  SUMMON_SKELETON  ATTACK_SWORD  KNOCK_OUT_FLY  KNOCK_OUT_GROUND  KNOCK_OUT_STAND_UP  RAPE_PREPARE
RAPE_CHARGE  RAPE_ON_GOING  RAPE_INTRO  RAPE_CUM_IDLE  RAPE_CUM  CORRUPT_SLOW  CORRUPT_FAST  CORRUPT_CUM
CORRUPT_INTRO  CONTROLLED_FLIGHT  BOOST  GALATH_SUMMON  GALATH_DE_SUMMON  GIVE_COIN  MASTERBATE  HUG_MANG
RIDE_MOMMY_HEAD  THREESOME_SLOW  THREESOME_FAST  THREESOME_CUM  PUSSY_LICKING  MASTERBATE_SITTING  MASTERBATE_SITTING_CUM  MORNING_BLOWJOB_SLOW
MORNING_BLOWJOB_FAST  MORNING_BLOWJOB_CUM
```

Each action carries a **transitionTick** (GeckoLib controller transition length for the animation
switch) and chains through sound keyframes (each phase advances on animation sound events, e.g.
`bjiDone` → SUCKBLOWJOB, `bjcDone` → doggy, `xxx_cumDone` → scene end).

## Interaction menu

Right-click a girl → `PlayerActionPacket` → the girl's interaction screen
(`GirlInventoryScreen`), listing (lang keys `action.names.*`):

```
Missionary | Cowgirl | Blowjob | Boobjob | Doggy | Strip | Dress up | Set new home |
Go home | Follow me | Stop following me | Equipment | Make me rich! | Deepthroat! |
Talk | Head pat | Anal | sex | touch boobs
```

- **Dynamic action buttons** may carry an **item cost** (e.g. fish for Luna, potion for Jenny):
  non-creative players must own a matching stack or the action is refused (chat line + sad sound).
- "Set new home" → SetNewHomePacket (respawn point, y floored). "Go home" → ender-pearl
  teleport home (SendCompanionHomePacket). "Follow me"/"Stop following me" → follow-mode toggle.
- "Equipment" → the girl's equipment chest GUI (weapon/bow/armor, or Luna's rod variant).

## Scene entry flow (NPC girls)

1. Client picks an action in the menu → sets `animationFollowUp` (GIRL_HAND_STATES) via
   `ChangeDataParameterPacket` + sends `KoboldStatePacket` (despite the name, the shared
   "start a scene" packet; note: its `tribeId` arg is the **girl** UUID, `girlId` is the **player** UUID).
2. Server: `setDismounted()`, then the girl **walks ~40 ticks** to TARGET_POS (step-lerp —
   `RotationHelper.lerpVec3d` int family, do not swap to the double variant or girls get flung),
   gets **anchored** (IS_ANCHORED), and `U()` dispatches on the hand-state.
3. Beddable girls (Jenny, Bia, Luna) instead walk to the **nearest bed** (`SendGirlToSexPacket` →
   `IBeddableSexGirl.goToSexBed()`), anchor at the bed, wait (ANAL_WAIT / WAIT_CAT) until the
   player is within 1 block, then lock both players in.

## Scene-end flow

- `xxx_cumDone` sound keyframe → `resetCameraAndPhysics` → single-arg `ResetGirlPacket`
  (resetPose=false = **full scene-end reset**: restore player physics + release the girl:
  re-add AI tasks, un-anchor, clear interaction partner, restore gravity/noClip, teleport to air).
- The two-arg ctor `ResetGirlPacket(uuid, true)` is the **player-only** reset. (Inverted flag —
  jar-verified.)
- `ResetControllerPacket` resets the girl's animation controller for all observers within 100 blocks.

## In-scene controls

While a scene is active the player's input is intercepted (`HandlePlayerMovement`):

| Key | Effect |
|---|---|
| **Shift (sneak)** | `triggerFastSexAction` — advance the action chain one step (`getNextAction`) |
| **Space (jump)** | `resetPlayerGirlCamera` (player-girls) — camera reset |
| **Escape-direction keys** | escape actions in scenes (`EscapeDirectionKey`) |
| **R-Shift** | "Leave sex scene" keybind — **progresses the scene to its natural ending**: jumps to the cum action (`triggerCumAction`), plays it, then walks the chain to release the player cleanly |
| **Jump + full horny meter** | `triggerCumAction` (fast cum) |

## Horny meter ("the balls")

`HornyMeterHud` — slide-in cum meter during scenes:

- Fills as the player performs actions: `addToHornyMeter` per sound keyframe (0.02 slow / 0.04 fast).
- When full (`>= 1.0`) it expands to show the ending input is available (jump → triggerCumAction).
- Hidden during scene transitions, on movement lock, and for non-scene states.
- Static/global — one meter, one scene at a time.

## Camera system

- **NPC scenes**: `PositionData` (boyCam) — the vanilla player render is cancelled, the first-person
  hand hidden, and the client player is repositioned to the girl's `boyCam` bone every render tick,
  so scenes are viewed first-person from the girl's perspective. `SetPlayerCamPacket` snaps
  third-person view/yaw/pitch for all observers at scene entry/exit.
- **Player-girl scenes**: `GirlCameraHelper` attaches the camera to the model's `girlCam` bone
  (player position overridden at render-tick start/end, body rendered first-person); when the girl
  is **anchored the camera rolls 180°** (`onCameraSetup`).
- Camera-bone world positions are published by the renderers from the static
  `CAMERA_PLACEMENTS` list (`boyCam`, `girlCam`) — keep in sync with the geo models.

## Movement lock

`SetPlayerMovementPacket` freezes the player mid-scene (velocity zeroed, input locked
via `setMovementLock`); on scene end `isSprinting=false` unlocks. Locking also hides the horny meter.

## Sex prompts (player ⇄ player)

- **Gender swap / standing sex**: `SexPromptPacket` + `GenderSwapScreen` — when a player-girl's
  owner requests a standing sex action with another player, the recipient gets a chat prompt
  ("asked you for a…") with accept/decline; answering via chat (`accept`/`decline` words) resolves
  it. Declines auto-timeout after 1 minute ("The request is declined automatically after 1 minute").
- **Player-girl ⇄ player-girl**: interaction menu between player-girls (lesbo prompt, `PlayerGirlEvents`).

## Scene extras

- **Particles**: heart/heal bursts (SpawnParticlePacket), dragon breath (SpawnEnergyBallParticlesPacket2),
  energy tendrils (DynamicTrailRenderer), wavy ribbons (RibbonRenderer), cum trails (CummyEntity,
  registered from animation sound listeners — creampie/masterbate/threesome keyframes; cleared on scene end).
- **Transition overlay**: `BeeScreen` — a ~69-tick animated black-screen wipe used between scenes
  (hides the horny meter while animating).
- **Sound**: `SoundHandler.randomSound` picks a random voice line per array without repeating
  the previous one; `SendChatMessagePacket` broadcasts girl lines to players within 40 blocks.
- **Anchored rendering**: anchored girls render at `getTargetPosition()` relative to the local
  player with pinned yaw; `EntityLookVectorHelper` makes anchored girls look at TARGET_POS.
