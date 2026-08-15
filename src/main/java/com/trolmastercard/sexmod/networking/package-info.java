/**
 * The Forge SimpleNetworkWrapper packet layer — every packet the mod sends
 * over the {@code "sexmodchannel"}.
 * <p>
 * <b>The scene-critical packets</b> (see their class docs for full flows):
 * <ul>
 *   <li>{@code ChangeDataParameterPacket} — client-&gt;server data-manager
 *       bridge; how client scene mutations reach the server.</li>
 *   <li>{@code KoboldStatePacket} — scene-entry trigger
 *       (dismount/position/start).</li>
 *   <li>{@code ResetGirlPacket} — scene end; resetPose=false = FULL reset,
 *       true = player-only.</li>
 *   <li>{@code SendGirlToSexPacket} — walk a beddable girl to her bed.</li>
 *   <li>{@code TeleportPlayerPacket} / {@code SetPlayerMovementPacket} —
 *       scene camera positioning and movement-lock restore.</li>
 * </ul>
 * <b>Pitfall:</b> packet IDs are assigned sequentially in
 * {@code PacketHandler.register()} — the order IS the wire protocol shared
 * with the SRG-reobfuscated build. Never reorder registrations.
 * SERVER-side handlers must schedule on the main thread via
 * {@code FMLCommonHandler.instance().getMinecraftServerInstance()
 * .addScheduledTask(...)}.
 */
package com.trolmastercard.sexmod.networking;
