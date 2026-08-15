package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT camera snap during girl scenes — forces the
 * client's view (third-person mode, yaw, pitch) to match the scene's camera
 * setup. Sent as part of scene entry/exit so all observers see the scene from
 * the intended angle.
 * <p>
 * <b>Handler.</b> CLIENT-side; schedules the actual work on the client thread.
 * Sets {@code thirdPersonView} and writes yaw/pitch (current and previous) into
 * the local {@link EntityPlayerSP} so interpolation does not fight the snap.
 */
public class SetPlayerCamPacket implements IMessage {
   boolean isValid = false;
   float camX;
   float camY;
   int camMode;

   public SetPlayerCamPacket() {
   }

   public SetPlayerCamPacket(float camX, float camY, int camMode) {
      this.camX = camX;
      this.camY = camY;
      this.camMode = camMode;
   }

   public void fromBytes(ByteBuf buf) {
      this.camX = buf.readFloat();
      this.camY = buf.readFloat();
      this.camMode = buf.readInt();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeFloat(this.camX);
      buf.writeFloat(this.camY);
      buf.writeInt(this.camMode);
   }

   public static class Handler implements IMessageHandler<SetPlayerCamPacket, IMessage> {
      public IMessage onMessage(SetPlayerCamPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.CLIENT) {
            System.out.println(Thread.currentThread().getName());
            Minecraft minecraft = Minecraft.getMinecraft();
            minecraft.addScheduledTask(() -> {
               minecraft.gameSettings.thirdPersonView = packet.camMode;
               EntityPlayerSP player = minecraft.player;
               player.rotationYaw = packet.camY;
               player.prevRotationYaw = packet.camY;
               player.prevRotationYawHead = packet.camY;
               player.rotationYawHead = packet.camY;
               player.renderYawOffset = packet.camY;
               player.rotationPitch = packet.camX;
               player.prevRotationPitch = packet.camX;
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerCam :(");
            return null;
         }
      }

   }
}
