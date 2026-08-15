# GUIs — every screen, button by button

All screens live in `client/gui/`; routing via `GuiHandler` (server) + `GuiOpenHandler` (client).

## GUI ids

| id | server container | client screen |
|---|---|---|
| 0 | girl equipment: `GirlInventoryContainer2` (Luna, 7 slots incl. rod) or `ChestContainer` (others, 6 slots) | `GirlInventoryContainerGui` (Luna) / `GirlInventoryContainerGui2` (others) |
| 1 | `GirlInventoryContainer` (27-slot girl chest) | `ChestContainerGui` |

Entities are located by matching block position against the packet's x/y/z; the GUI closes by
matching the container instance in the static container lists.

## GirlInventoryScreen (the interaction menu)

Right-click a girl → action list (`action.names.*`):
Follow me / Stop following me / Go home / Set new home / Equipment + the girl's dynamic
action buttons (with item costs). Buttons send `girl.doAction(actionName, playerUUID)`.
Non-creative players without the required stack get a chat refusal + sad sound.

## ClothingScreen (the girl wand editor)

See [07-custom-models.md](07-custom-models.md) for the full system. Controls:

- **Bone toggles** with `</>` and `+/-` for the target girl's custom model parts (one row per
  `BoneType` via `CustomModelList`, each row showing a live preview via a temporary SexSceneEntity).
- **Preview rotation** of the model.
- **? button** → opens the tutorial video (girl_wand.mp4 — archived link; guarded Desktop.browse).
- **Folder button** → opens `sexmod/custom_models/singleplayer` (guarded Desktop.open, mkdirs first).

## GirlScreenBase (player-girl picker)

Opened from GirlDataPacket after right-clicking a girl with the wand while transformed:

- Cycles previews of every non-NPC girl type + the player's own model (reflection-built into the
  client world, static spin animation), sends the choice via UpdatePlayerModelPacket
  (this is the horny-potion transformation picker too).

## Chest GUIs

- **ChestContainerGui** (27-slot girl chest + player inventory, vanilla chest texture, dynamic height,
  girl's name as title). On close: 63-slot snapshot → UploadInventoryToServerPacket.
- **GirlInventoryContainerGui** (Luna's 7-slot equipment, `girlinventory.png` texture). On close:
  43-slot snapshot (player 0-35 + Luna 7).
- **GirlInventoryContainerGui2** (generic girl's 6-slot equipment). On close: 42-slot snapshot.
- **GirlInventorySlot** enforces per-slot item types (swords/tools, bows, armor by body part,
  rod for Luna).
- The girl entity is only "materialized" while her chest is open — closing sends
  UploadInventoryToServerPacket2, which removes her from the server world.

## BeeDialogueScreen

Right-click a bee: "follow me / stop following", "go home", "set home here",
and — when she's horny — "open chest" (BeeOpenChestPacket, gated by the horny flag).
All actions go through packets (ChangeDataParameterPacket master toggle,
SendCompanionHomePacket, SetNewHomePacket, BeeOpenChestPacket).

## StructureCommandScreen (dragon staff)

Direction-pad command screen (see [04-tribe-system.md](04-tribe-system.md)):
bottom-left mark/unmark bed/chest, top-left tribe follow mode, bottom-right staff rendering mode,
top-right fell log / mine block / cancel task. Shows the tribe overview
(GetTribeUiValuesPacket: alerted state, saved positions, member positions + wool colors).
Point2D/Rectangle used for grid math.

## GalathScreen (direction pad)

Control screen for Galath (and goblin throw/pickup): hold the mouse in a quadrant, release/Escape
commits — left = start throwing, top = throw the goblin at the player, bottom = pick the goblin up.
For non-goblin targets only the left action is meaningful.

## GenderSwapScreen (consent prompt)

Standing-sex consent flow (see [03-actions-scenes.md](03-actions-scenes.md)): purple chat prompt
with accept/decline, 1200-tick countdown; "accept"/"decline" typed in chat resolve it.
SexPromptPacket routes the prompt to the other participant.

## TribeNameScreen

Claim-tribe name input (max 15 chars, "set" button → ClaimTribePacket; empty field sends nothing).

## HUDs (not screens but part of the GUI layer)

- **HornyMeterHud** — the cum meter (see [03-actions-scenes.md](03-actions-scenes.md)).
- **GalathFlightHud** — 3 boost-charge pips (3s cooldown, 5s regen per charge), fades in/out.
- **EscapeMinigameHud** — WASD prompt minigame during the rape pounce (35-tick prompt cycle).
- **BeeScreen** — 69-tick black transition wipe between scenes (hides the horny meter).

## Keys (ClientProxy keybindings, category "Sex mod")

| Key | Name | Purpose |
|---|---|---|
| **G** (34) | Interact with your goblin | goblin interact (pick up/throw/… — opens GalathScreen) |
| **L** (76) | open character customisation menu | ClothingScreen while transformed |
| **R-Shift** (54) | Leave sex scene | progress scene to natural ending (see [03-actions-scenes.md](03-actions-scenes.md)) |

Other input: **shift = fast sex action / scene advance**, **space = camera reset / cum trigger
with full meter** (HandlePlayerMovement), **escape-direction keys** for scene escapes.
