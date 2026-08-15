# The Kobold Tribe System

The kobolds are a full tribe-management system: claim tribes, assign tasks (mining,
woodcutting), beds/chests, follow modes, and kobold reproduction.

## Getting kobolds

- **Tribe egg** — right-click spawns a whole new tribe (4 kobolds, random color) at the block you aim at.
- **Kobold egg** — place it; it incubates (12000 ticks, escalating wiggle animations) and hatches
  into a kobold **joined to the tribe** whose UUID is on the egg (`tribeID` NBT).
- Kobold eggs handed out from the dragon-staff UI are dyed in the tribe color.

## Tribe state (`KoboldManager`)

All tribe state lives in `KoboldManager`, persisted to a world-saved-data file ("tribes"):

- Tribes keyed by UUID; each holds: **members, leader, color, state (REST/ACTIVE), tasks,
  combatants, beds, chests, saved positions, follow-mode flag**.
- **Claiming**: the dragon-staff UI shows unclaimed tribes; entering a name (TribeNameScreen,
  max 15 chars) sends ClaimTribePacket — every masterless kobold of the tribe becomes yours,
  the tribe formation is announced in chat (chat color = leader's `EyeAndKoboldColor`),
  follow mode + leader assignment enabled.
- **Follow/alerted mode**: toggled from the staff UI (SetTribeFollowModePacket) — kobolds follow
  the master vs. idle; `TribeState` REST/ACTIVE gates working/fighting.

## The dragon staff (tribe command tool)

Right-click the staff → **StructureCommandScreen** — a direction pad. Hold the mouse in a
quadrant and release (or press Escape to cancel):

| Quadrant | Command | Packet |
|---|---|---|
| Bottom-left | Mark/unmark targeted **chest/bed** (tribe storage/respawn) | SendBlocksPacket |
| Top-left | Toggle **tribe follow mode** | SetTribeFollowModePacket |
| Bottom-right | Toggle the staff's **rendering mode** (particle layout) | (local) |
| Top-right | Fell the targeted **log** / mine the targeted **breakable block** | FallTreePacket / MinePacket / CancelTaskPacket |

- Right-clicking beds/chests with the staff is blocked — tribe blocks are managed only through the UI.
- "Give me a tribe egg" button → SendEggPacket (egg in your tribe color).
- The UI also shows the tribe overview (GetTribeUiValuesPacket: alerted state, saved positions,
  member positions with wool-color markers).
- `StructureMarkerRenderer` draws colored world markers while the staff is held:
  **blue = bed, green = chest, red = mine target**. `CancelTaskPacket` erases a highlighted
  mining target (server replies to un-highlight the blocks).

## Tasks (`KoboldTask`)

Work orders created by the packets and executed by kobold AI; persisted with the tribe:

- **MINE** — up to **3 workers**; digs a 30-block corridor (3-wide) through the selected block
  and facing. Rejected if any target block is unbreakable (bedrock).
- **FALL_TREE** — **1 worker**; walks the log down to ground, computes all connected log blocks,
  fells the tree.
- **Capacity gate**: both tasks require the tribe's **bed count ≥ 2 × tribe
  members** before the tribe works — `bedCount = floor(tribeBeds.size() / 2)`
  must be ≥ `memberCount` (FallTreePacket/MinePacket both enforce this and
  report `N/M Beds` in chat). Workers themselves are capped per task type
  (FALL_TREE: 1, MINE: 3).
- Workers are released when the task finishes or is cancelled (`releaseWorkers` restores physics
  and anchor flags; kobolds in an interaction are skipped).

## Kobold AI & behavior

- **Combat**: hostile mobs can hunt tribe members (NearestAttackableGirlGoal, tamed kobolds only);
  kobolds fight back as a tribe (combatants list).
- **Breeding**: kobold scenes (oral/anal/mating-press) + the egg reproduction path.
- **Teleport-home**: THROW_PEARL — ender-pearl thrown home, limited to 5 blocks of home
  (KoboldEggProjectileEntity; end-gateway supported).
- **Ceiling check**: `IKobold.isBlockedByCeiling` gates pathing/head space.
- **Names**: `KoboldNames` generates tribe member names.
- **Debug**: `DebugMode` prints tribe-task diagnostics when a kobold is hurt (dev only).

## Kobold appearance customization

- Body/eye colors from the `EyeAndKoboldColor` palette (packed as Vec3i in data keys); default PURPLE.
- Model-code DNA string: horn variants (up/down), boob/eye scales, freckle variants,
  backpack/tailpack pose, crown/egg visibility.
- Player-form kobold: size scalar [0, 0.25] (part-id 0), body color (part-id 1), eye color (part-id 2).

## Scenes

Oral (blowjob), anal (KOBOLD_ANAL_*), mating-press (MATING_PRESS_*) — with tongue-bone rendering
only for blowjob actions and transition-time interpolation for body offsets.
