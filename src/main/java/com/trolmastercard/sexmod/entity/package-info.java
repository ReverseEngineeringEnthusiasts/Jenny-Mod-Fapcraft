/**
 * The entity layer: every girl (NPC + player-form) and the shared scene
 * state machine.
 * <p>
 * <b>Hierarchy.</b>
 * <pre>
 * BaseGirlEntity                     -- shared girl state + scene lifecycle (see its class doc)
 *  +- AbstractNpcOnlyEntity          -- NPC girls: DNA/appearance params (keys 119-121)
 *  |   +- AbstractGirlNpcEntity      -- common NPC girl behavior
 *  |   |   +- JennyEntity / BiaEntity / LunaEntity / EllieEntity / SlimeEntity
 *  |   |   +- BeeEntity / GoblinEntity / AllieEntity / ManglelieEntity / GalathEntity
 *  |   +- KoboldEntity               -- tribe kobold (oral/anal/mating + tribe AI)
 *  +- AbstractPlayerGirlEntity       -- horny-potion player forms (XxxPlayerEntity)
 * </pre>
 * <b>Scene system</b> (the mod's core). Entry: client {@code doAction} sets
 * {@code GIRL_HAND_STATES} via {@code ChangeDataParameterPacket} and sends
 * {@code KoboldStatePacket} -&gt; server {@code setDismounted()} -&gt; 40-tick
 * lerp to {@code TARGET_POS} ({@code RotationHelper.lerpVec3d} INT variant!)
 * -&gt; anchored -&gt; {@code U()} starts the {@link Action}. Progression via
 * animation sound keyframes; ending via the cum action -&gt;
 * {@code resetCameraAndPhysics} -&gt; {@code ResetGirlPacket}.
 * <p>
 * <b>Pitfall history</b> (all fixed, all jar-verified): lerp int/double swap
 * flung girls 40x (vanishing), Bia's {@code ac = 22} countdown was dropped
 * (stalled bed scenes), an invented {@code setDead} catch deleted girls on
 * benign tick exceptions, and the ResetGirlPacket boolean was inverted.
 * Read the per-class "Pitfalls" sections before touching anything here.
 */
package com.trolmastercard.sexmod.entity;
