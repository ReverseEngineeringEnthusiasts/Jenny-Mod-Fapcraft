# Javadoc Style Guide — Fapcraft 1.12.2 remap

## Purpose
This codebase is a deobfuscated Minecraft mod. The javadoc must make the
architecture *obvious* so future edits never break the scene system again.

## Rules

1. **Class-level javadoc on every class** you touch: what the class does, who
   uses it, and — critically — **what it must NOT break** (stateful flows,
   data-manager keys, packet ordering, obfuscation pitfalls).

2. **Method-level javadoc on non-trivial methods**: what it does, side
   effects, which side it runs on (CLIENT/SERVER/BOTH), and any ordering
   constraints. Skip trivial getters/setters/one-liners.

3. **Formatting** (javadoc allows HTML — use it sparingly):
   - `{@code ...}` for code tokens
   - `<p>` between paragraphs
   - `{@link Class#method}` for references
   - `@param`, `@return`, `@throws`, `@see` where useful
   - Mark sides explicitly: "CLIENT-side", "SERVER-side", "runs on both sides"

4. **CRITICAL — document these pitfalls wherever relevant** (from the actual
   debugging history):
   - `RotationHelper.lerpVec3d(Vec3d, Vec3d, int)` = STEP lerp (divides: one
     step of the remaining distance). `lerpVec3dDouble(...)` = PROGRESS lerp
     (multiplies by t in 0..1). The girls' 40-tick dismount lerp MUST use the
     INT version; using the double version flings the girl 40x and she
     vanishes (chunk unloads, destroy packet, setDead client-side). NEVER
     change these call sites.
   - The dismount/scene-entry flow: `KoboldStatePacket.sendState` ->
     `setDismounted()` (sets a per-girl flag, e.g. `ab`/`ac`/`a2`/`yFlag`) ->
     updateAITasks lerps the girl to `TARGET_POS` for ~40 ticks ->
     `IS_ANCHORED=true` -> `U()` reads `GIRL_HAND_STATES`
     (the `animationFollowUp` client packet) and picks the scene action.
   - `BiaEntity.handleAnalState` countdown: first contact sets `ac = 22`
     (jar-verified static field), then counts down; the scene action starts
     when it hits 0. This was once deobf'd to `-1` which permanently stalled
     Bia's anal/doggy scenes.
   - Obfuscated-runtime reflection needs SRG names (e.g.
     `field_187470_g` = `prevEquippedProgressMainHand`) — MCP names throw
     `NoSuchFieldException` at runtime.
   - `ResetGirlPacket`: single-arg ctor = `resetPose=false` = FULL reset
     (player physics + girl release). Two-arg `(uuid, true)` = player-only.
     The natural scene end uses the single-arg form via
     `resetCameraAndPhysics` -> `resetLocalPlayerClientState`.
   - `SceneDebug` has per-subsystem boolean flags; keep new debug logging
     behind a flag.

5. **Never** document obfuscated single-letter field names as if meaningful —
   either rename-mention the deobf name or describe the semantic role.
   Do NOT rename code. Javadoc only.

6. Keep line width ~130 chars. Use 3-space indentation in the javadoc
   content to match the file style.

## Structure for class-level javadoc
```
/**
 * <b>Role.</b> One paragraph on what this class is and where it sits in the
 * scene system.
 * <p>
 * <b>Scene flow.</b> If it participates in scenes, describe its exact role:
 * entry, progression, ending.
 * <p>
 * <b>State.</b> Key fields/data-manager entries and their invariants.
 * <p>
 * <b>Pitfalls.</b> Anything a future editor must not break.
 */
```

## Example
```java
/**
 * Bia NPC — the catgirl with anal/doggy bed scenes.
 * <p>
 * Scene entry: {@code doAction} sets {@code animationFollowUp} via
 * {@link ChangeDataParameterPacket}, then {@link KoboldStatePacket} triggers
 * {@code setDismounted()} ({@code yFlag}). {@code updateAITasks} lerps her to
 * {@code TARGET_POS} (40 ticks, {@link RotationHelper#lerpVec3d} INT version —
 * do not switch to lerpVec3dDouble, it flings her), anchors her, and calls
 * {@code U()} which dispatches on {@code GIRL_HAND_STATES}.
 * <p>
 * <b>Bed scenes</b> ({@code anal}/{@code doggy}) walk her to a bed
 * ({@link #goToSexBed()}), then {@code handleAnalState} runs a jar-verified
 * 22-tick countdown ({@code ac}) before the scene action starts. Talk/headpat
 * scenes play in place.
 * <p>
 * <b>Pitfall:</b> {@code handleAnalState} must keep {@code ac = 22} on first
 * contact — a deobf regression to {@code -1} permanently stalled every Bia
 * bed scene.
 */
```
