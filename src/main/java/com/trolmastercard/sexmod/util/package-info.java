/**
 * Utilities and managers shared across the mod.
 * <p>
 * <b>Scene-critical:</b>
 * <ul>
 *   <li>{@code RotationHelper} — math helpers. <b>Two lerp families:</b>
 *       {@code lerpVec3d(int)} = STEP (divide, used by the girls' 40-tick
 *       dismount lerps — do not reroute), {@code lerpVec3dDouble} = PROGRESS
 *       (multiply, render interpolation).</li>
 *   <li>{@code HandlePlayerMovement} — client input lock + scene controls.</li>
 *   <li>{@code InHandMapRenderer} — in-hand map rendering for transformed
 *       players (SRG reflection names!).</li>
 *   <li>{@code SceneDebug} — per-subsystem debug logging flags.</li>
 * </ul>
 * <b>State/managers:</b> {@code GirlRegistry} (girl UUID registry),
 * {@code GirlSavedData} (galath/manglelie ownership WorldSavedData),
 * {@code KoboldManager}/{@code KoboldTask} (the tribe system),
 * {@code ServerWhitelistManager} (custom-model whitelist),
 * {@code PlayerIds} (player-girl transformation tracking),
 * {@code SoundHandler} (sound arrays), {@code ThreadNames} (general utils
 * despite the name).
 */
package com.trolmastercard.sexmod.util;
