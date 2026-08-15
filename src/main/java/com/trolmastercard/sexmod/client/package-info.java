/**
 * Client-side layer: rendering, GUIs, the scene camera and the input hooks.
 * <p>
 * <b>Rendering.</b> {@code GirlRenderer}/{@code GirlRendererBase} render the
 * NPC girls (geckolib animated models); {@code GirlPlayerRenderer} +
 * {@code GirlCameraHelper} handle the horny-potion player-girls, including
 * the first-person camera attached to the model's {@code girlCam} bone and
 * the 180&deg; roll when anchored. {@code SexWorldClient} is a client-only
 * flat world used ONLY for model preloading — entity code checks
 * {@code instanceof SexWorldClient} to skip preview logic.
 * <p>
 * <b>GUI.</b> {@code GirlInventoryScreen} is the interaction menu
 * (calls {@code girl.doAction(action, playerUUID)}); {@code BeeScreen} is
 * the shared interaction overlay; {@code HornyMeterHud} is the cum meter;
 * {@code ClothingScreen} is the girl-wand customization GUI.
 * <p>
 * <b>Input.</b> {@code HandlePlayerMovement} maps sneak/jump to scene
 * controls; {@code SexSceneKeyHandler} is the R-Shift "leave scene" key
 * (progress-to-ending with a full-exit fallback).
 */
package com.trolmastercard.sexmod.client;
