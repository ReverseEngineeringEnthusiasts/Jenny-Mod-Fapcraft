# Rendering pipeline

How a girl becomes triangles: model selection, the bone conventions, who binds which texture,
and the state rules that keep the frame from falling apart. Written after the 2026 audit, which
found four separate render-thread crashes in this layer.

## Call chain

```
RenderManager.renderEntity
└─ GirlRenderer.doRender                (entity type dispatch, shadow offset)
   └─ GirlRenderer.doRenderEntity       (translate, model lookup, matrix push/pop)
      └─ GirlRenderer.renderModel       (animation tick -> setLivingAnimations -> bone pass)
         └─ <Subclass>Renderer.renderModelBuffer   (per-model split: body / steve / body2)
            └─ GirlRenderer.renderRecursively      (per-bone: layers, items, colours, children)
```

`renderRecursively` is the only place that walks the bone tree; everything above it decides
*which* bones are drawn and *with which texture*.

## Model selection

`GirlModel.getModelLocation(entity)` → `getSexWorldTexture(entity)` returns
`modelLocations[OUTFIT_INDEX]` (name is historic; it is the model picker, not a texture).

Each model declares an array, usually two entries (outfit 0 = nude, 1 = dressed):

| model | index 0/1 | index 2 |
|---|---|---|
| `GalathNpcModel` | `galath.geo.json` | **`galath_con_mang.geo.json`** |
| `ManglelieNpcModel` | `manglelie.geo.json` | **`galath_con_mang.geo.json`** |
| `AllieNpcModel`, `SlimeNpcModel` | own geo | own third variant |
| everyone else | own geo | — |

`OUTFIT_INDEX` out of range falls back to 0 (the guard is `>=`, the original jar used `>` and
crashed at `index == length`).

**The combined model** `geo/galath/galath_con_mang.geo.json` is 532 bones, root `rotationTool`,
with children `body`, `coin`, `steve`, `body2`. It is the only model that has **`body2`** — the
second girl in a two-girl scene. Neither `galath.geo.json` (253 bones) nor `manglelie.geo.json`
(304 bones) has it.

Nothing in the mod (or the v1.0 jar) ever sets `OUTFIT_INDEX = 2`, so index 2 is reachable only
through the `ManglelieNpcModel.getModelLocation` override added for the threesome (see below).

## Bone conventions

| bone | meaning | texture bound for it |
|---|---|---|
| `body` | the girl's own body | her entity texture |
| `steve` | the **interaction player's** body, posed by the scene animations | `getSkinTexture(girl)` — the player's skin |
| `body2` | the second girl (Manglelie) in the combined model | `ManglelieNpcModel.MANGLELIE_TEXTURE` |
| `coin` / `pentagram` | Galath's coin and its pentagram child | girl texture |
| `rotationTool` | root of the Galath/Manglelie models | — |
| `head`, `upperBody` | pose anchors read/written by the models | — |

`GirlModel` toggles `steve`'s visibility per frame from `Action.hasPlayer` — during a scene the
player's body is part of the girl's rig, so the model shows it and the real player is normally
out of frame (camera lock) or invisible.

## The threesome (Galath + Manglelie + player)

1. The corrupt scene ends with the Galath and Manglelie in `THREESOME_SLOW` / `THREESOME_FAST` /
   `THREESOME_CUM` (both entities).
2. The **Manglelie** plays `animation.shared.double_holding_slow|hard|soft|back|cum`
   (`ManglelieEntity.handleActionAnimationOverrides`). Those clips animate `body` **and**
   `body2` **and** `steve` — all three participants.
3. `ManglelieNpcModel.getModelLocation` returns the combined model while she is in a
   `THREESOME_*` action, so `body2` exists and the clips have something to move.
4. `ManglelieRenderer.renderModelBuffer` then draws only `body2` (her, with her texture) and
   `steve` (the player, with the **player skin**). The Galath keeps her own model and renders
   herself normally; her `body2` pass never fires because `galath.geo.json` has no such bone.

Before the fix, the animations ran against the normal Manglelie model: `body2` did not exist, so
the pose looked frozen and the `steve` pass rendered the player with the Manglelie's skin sheet
(garbled UVs).

## Null-safe bone access (`ModelBones`)

`AnimationProcessor.getBone(name)` returns `null` for a bone the model does not have. Four
crashes came from assuming otherwise:

- `GalathNpcModel` hands its own processor to `ManglelieNpcModel.animateModel` while hugging;
  that pass looks up Manglelie-only bones (`cheekRBelowSkirt`, `skirt`, `sideRNoSkirt`, …).
- `ManglelieRenderer`'s two-bone pass on a model without `body2`.
- `GalathRenderer` drawing `body`/`steve` without checking the lookup succeeded.
- `GalathCoinRenderer` rendering `pentagram` and indexing `topLevelBones.get(0)` blind.

Rule: **every** bone lookup goes through `client/model/ModelBones` (`get` / `setHidden`) or is
explicitly null-checked. A missing bone is a no-op, never a render-thread exception. The two
`_clash`-era methods that still touch bones directly do their own null checks.

## Texture binds

`TextureManager.bindTexture` is global state; the last bind wins until the next one. Bind before
each pass, never assume:

```java
bindTexture(girlTexture);   renderRecursively(body);
bindTexture(skinTexture);   renderRecursively(steve);     // player skin, not the girl's
bindTexture(MANGLELIE_TEXTURE); renderRecursively(body2);
```

The skin texture comes from `GirlRenderer.getSkinTexture(girl)` → the bounded LRU
`SkinTextureCache` (64 entries, eviction deletes the GL texture, cleared on session change).
Misses return the shared steve fallback first and refresh off-thread.

## GL state rules

The 1.1.0 "dark GUI" was a state leak: the mod toggled `GL_LIGHTING` with raw `GL11.glEnable`
behind `GlStateManager`'s back, the cache went stale, and vanilla's `disableLighting()` became a
no-op. Everything drawn afterwards (the whole GUI) was lit, i.e. ~4 % brightness.

Rules now enforced in this package:

- **Never** call `glEnable`/`glDisable` for lighting, blend, alpha, depth or cull — use
  `GlStateManager` so the cache stays correct. Raw GL is only acceptable for *queries*
  (`glIsEnabled`, `glGetFloatv`) and for matrix operations that have no GlStateManager wrapper.
- Every `enableX()` has a matching restore on every exit path, including exceptions
  (`try/finally`).
- Method-spanning `pushMatrix()` gets a `finally { popMatrix(); }`. The per-bone recursion
  pushes its own frame, so an exception deep in the tree unwinds one level per recursion instead
  of stranding the stack.
- Snapshot colours before you overwrite them and restore the snapshot; don't leave a tint for
  the next pass to inherit.

## Per-frame costs worth knowing

- `getTargetPosition()` / `getInteractionPlayerUUID()` decode strings from the data manager
  (`split("\\|")` + three `parseDouble` + `UUID.fromString`). They are memoised on the entity and
  re-decoded only when the raw value changes — call them freely, but do not add a second decoder.
- `RenderOcclusion.canRender` traces up to 8 voxel rays with a DDA that allocates per step. It
  early-outs on `thirdPersonView` before doing any vector work; keep it that way.
- `SexSceneRenderer` re-parses custom parts per frame; the parsed set is cached on the girl and
  invalidated with `CUSTOM_MODEL_KEY`.
- `StructureMarkerRenderer` keeps a `ConcurrentHashMap` key set (netty writes, render reads) and
  a per-position colour memo.
