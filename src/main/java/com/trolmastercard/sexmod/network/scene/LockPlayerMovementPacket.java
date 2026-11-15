package com.trolmastercard.sexmod.network.scene;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT movement lock while a girl scene is active. The
 * server freezes the player (velocity zeroed, input locked via
 * {@link HandlePlayerMovement#setMovementLock}) so the player cannot walk away
 * mid-scene; on scene end the server sends {@code isSprinting=false} to unlock.
 * <p>
 * <b>Handler.</b> CLIENT-side. Locks/unlocks movement and zeroes the local
 * player's velocity; on lock also hides the horny meter HUD
 * ({@link HornyMeterHud#hideHornyMeter()}).
 */
public class LockPlayerMovementPacket implements IMessage {
   boolean isValid;
   boolean isSprinting;

   public LockPlayerMovementPacket(boolean isSprinting) {
      this.isSprinting = isSprinting;
      this.isValid = true;
   }

   public LockPlayerMovementPacket() {
      this.isValid = false;
   }

   public void fromBytes(ByteBuf buf) {
      this.isSprinting = buf.readBoolean();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeBoolean(this.isSprinting);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<LockPlayerMovementPacket, IMessage> {
      public IMessage onMessage(LockPlayerMovementPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.CLIENT) {
            PlayerMovementController.setMovementLock(packet.isSprinting);

            try {
               Minecraft.getMinecraft().player.setVelocity(0.0, 0.0, 0.0);
            } catch (Exception exception) {
               // ignored: the client player can be gone by the time the packet lands
            }

            if (packet.isSprinting) {
               HornyMeterHud.hideHornyMeter();
            }

            return null;
         } else {
            Main.LOGGER.warn("received an invalid message @SetPlayerMovement :(");
            return null;
         }
      }

   }
}
