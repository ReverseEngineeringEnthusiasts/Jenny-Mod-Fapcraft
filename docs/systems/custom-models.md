# The Custom Model System

The mod has a full custom-model pipeline: per-girl model codes, custom parts (dildos, outfits,
scene props), client-side editors, and **server-pushed GeckoLib models** for multiplayer.

## Model codes ("DNA" strings)

- Every girl's appearance can be packed into a **model-code string** + optional `$`-separated
  **part-id list**. Format `code$parts` (also what the wand copies to the clipboard).
- NPC-only girls store it in the `APPEARANCE_DNA` data key (id 121); player-form kobolds/goblins
  share keys 119-121; transformed player-girls persist it on the player's NBT
  (`sexmod:CustomModel<type>` and `sexmod:GirlSpecific<type>`).
- The goblin/kobold DNA encodes: size, **hair color (index 6)**, **skin color (index 7)**,
  **eye color (index 8)** (HairColor / SkinColor / EyeColor enums), body variants.
- Validation (`UploadModelStringPacket.isValidModelCode`): part-id lists are checked per girl type.

## The girl wand editor (ClothingScreen)

- Open: right-click a girl with the **girl wand**, or press **L** while transformed.
- Edit: cycle custom parts with `</>` and `+/-` per `BoneType`; rotate the preview; each row
  renders a live model preview (temporary SexSceneEntity).
- `BoneType` classifies bones: girl-specific (head, feet, hands) vs fully custom (CUSTOM_BONE);
  the enum order is load-bearing (persisted part indexes) and its button ids are allocated at
  class-load time.
- `UploadModelStringPacket` saves the code server-side (entity for NPCs, player NBT for player-girls).
- Custom parts are rendered by `SexSceneRenderer` via `SexSceneEntity` anchors: a part's root bone
  is replaced by the girl's bone matrix (leg/arm names mapped to vanilla bone names), so parts
  follow the girl's animation. `IGirlRenderer` blacklists bones whose custom-part ancestor is hidden.
- `BodyParts` computes world offsets for the "custom" bones (boobs/booty/vagina/fuckhole).

## Custom model parts (the wardrobe)

- Parts live in `sexmod/custom_models/` (client) and `sexmod_custom_models/` (server).
- `CustomModelList` builds the editor list from `ClothingScreen.m` (static custom-part registry)
  and `ServerWhitelistManager.getModelParts`.
- Item-model parts use the built-in `cross` geo/texture; other parts use per-part gecko models
  with textures resolved per-part.
- `SexSceneModel` resolves model+texture per-part from the server whitelist.

## Server-pushed models (whitelist system, `ServerWhitelistManager`)

- **Client**: `sexmod/custom_models/whitelisted_servers.txt` lists trusted server IPs
  (`/whitelistserver [confirm]` — two-step consent is a security gate: "only whitelist servers you trust").
- Models are loaded from `sexmod/custom_models/<server>/` — each model is a
  **`.cfg` + `.png` + `.geo.json` trio**, registered into GeckoLib at runtime (`registerModel`).
- **Server**: `sexmod_custom_models/<name>/` holds the model files; `CommonProxy` loads the
  registry on dedicated servers so models can be served.
- **Download flow** (DownloadServerModelPacket): the client sends the list of model names it still
  wants; the server streams one packet per file (tagged with a total-count modelIndex); the client
  writes them into `sexmod/custom_models/<server>/<name>/` with progress output.
- **Lighting modes** (`LightingType` enum: `DEFAULT`, `SEXMOD`, `NONE`) — per-model
  lighting override (SEXMOD = look-vector lighting) stored in the whitelist registry.
- **Rendering gate**: `isGlobalRenderingDisabled()` — the current server IP must be whitelisted
  for its models to render; `/reloadcustommodels` (op 2) reloads the registry and pushes fresh
  model scales to every player via UnknownPacket.
- **GirlWorldData** persists Galath/Manglelie model codes across world reloads.

## Selective model downloads (`/models`)

The old no-models prompt downloaded the whole `main.zip` (~560 MB, ~452 MB of packs). It now opens
`ModelDownloadScreen`:

- `ArchiveIndex` fetches the archive's GitHub tree once (one API call), turns every `.zip`/`.rar`/
  `.7z` blob into a pack (name, girl, size, raw URL) and caches the list in
  `sexmod/custom_models/archive_index.json` for 24 h. `Refresh` forces a new fetch.
- The screen filters by pack name, girl or path, ticks individual packs, or **Download all**.
  Downloads go to `sexmod/custom_models/<group>/...` (the folder is created first; the screen
  prints the path and has an **Open folder** button).
- `ModelDownloadManager` runs one daemon worker, one pack at a time, 64 KB buffer, checks free
  disk space before each file, and writes `<pack>.part` until complete. **Pause** closes the
  stream and keeps the part file; **Resume** reopens it with a `Range` request, and a server that
  ignores the range restarts that file. **Cancel** removes the unfinished part files and keeps
  finished packs.
- Closing the screen does not stop a batch; `/models` reopens it. Once a batch finishes, the
  manager calls `ReskinManager.rescan()`, so the wardrobe sees the new packs immediately.

## Combined models (two characters in one rig)

A "combined model" is a single geo file containing more than one body, driven by one animation
file. The mod ships one: `geo/galath/galath_con_mang.geo.json` — 532 bones, root `rotationTool`
with `body` (Galath), `coin`, `steve` (the interaction player) and `body2` (Manglelie). Both
`GalathNpcModel` and `ManglelieNpcModel` expose it as **outfit index 2**, and
`animation.shared.double_holding_*` in both animation files key all three bodies.

Who draws what during `THREESOME_SLOW/FAST/CUM`:

- **Manglelie** — `getModelLocation` returns the combined model for those actions; her
  `renderModelBuffer` draws `body2` (her, with her texture) then `steve` (the player, with
  `getSkinTexture`).
- **Galath** — keeps her normal model and renders normally; her renderer's `body2` pass stays
  dormant because `galath.geo.json` has no such bone.

The custom-model archive's "Combined Models" folder follows the same idea as a resource pack: it
replaces `assets/sexmod/geo/<girl>/*.geo.json` and the matching `*.animation.json` with a rig that
carries a second body and clips that move it. Note `steve (Jenny & Zoey).zip` in the archive is
corrupt (multi-part zip concatenation, case-mismatched entry names, bad CRCs) — 7-Zip refuses it
outright, so it needs re-downloading or re-zipping on a case-preserving filesystem.

## The model cache

`GirlModel.enableModelCache` (config `shouldLoadOtherSkins`) controls texture caching;
`GirlAnimationProcessor` caches bones by name for O(1) per-frame access (hair-follow,
skirt-follow); the cache is cleared with the renderer list on model reloads.
