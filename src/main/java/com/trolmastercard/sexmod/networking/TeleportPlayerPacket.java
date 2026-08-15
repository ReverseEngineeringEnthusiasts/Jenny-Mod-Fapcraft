package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook.EnumFlags;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER hard teleport of a player (by UUID) to a position
 * with optional yaw/pitch. Used by scene transitions and admin-style commands;
 * the client is reset via the connection's {@code setPlayerLocation} so vanilla
 * interpolation cannot fight the teleport.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Wraps yaw/pitch,
 * sets position/angles + head rotation, zeroes motion and confirms the move to
 * the player's connection. Unknown UUID -> logs the available players and
 * returns.
 */
public class TeleportPlayerPacket implements IMessage {
   boolean isValid;
   String girlId;
   Vec3d position;
   float yaw;
   float pitch;

   public TeleportPlayerPacket() {
      this.isValid = false;
   }

   public TeleportPlayerPacket(String girlId, Vec3d position) {
      this.girlId = girlId;
      this.position = position;
      this.yaw = 0.0F;
      this.pitch = 0.0F;
      this.isValid = true;
   }

   public TeleportPlayerPacket(String girlId, Vec3d position, float yaw, float pitch) {
      this.girlId = girlId;
      this.position = position;
      this.yaw = yaw;
      this.pitch = pitch;
      this.isValid = true;
   }

   public TeleportPlayerPacket(String girlId, double x, double y, double z, float yaw, float pitch) {
      this.girlId = girlId;
      this.position = new Vec3d(x, y, z);
      this.yaw = yaw;
      this.pitch = pitch;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlId = ByteBufUtils.readUTF8String(buf);
      this.position = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.yaw = buf.readFloat();
      this.pitch = buf.readFloat();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlId);
      buf.writeDouble(this.position.x);
      buf.writeDouble(this.position.y);
      buf.writeDouble(this.position.z);
      buf.writeFloat(this.yaw);
      buf.writeFloat(this.pitch);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<TeleportPlayerPacket, IMessage> {
      public IMessage onMessage(TeleportPlayerPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     try {
                        System.out.println("teleporting player " + packet.girlId + " to " + packet.position);
                        EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(packet.girlId));
                        packet.yaw = MathHelper.wrapDegrees(packet.yaw);
                        packet.pitch = MathHelper.wrapDegrees(packet.pitch);
                        player.setLocationAndAngles(packet.position.x, packet.position.y, packet.position.z, packet.yaw, packet.pitch);
                        player.setRotationYawHead(packet.yaw);
                        player.motionX = 0.0;
                        player.motionY = 0.0;
                        player.motionZ = 0.0;
                        player.connection
                           .setPlayerLocation(packet.position.x, packet.position.y, packet.position.z, packet.yaw, packet.pitch, EnumSet.noneOf(EnumFlags.class));
                     } catch (Exception exception) {
                        System.out.println("couldn't find player with UUID: " + packet.girlId);
                        System.out.println("could only find the following players:");
                        System.out.println(FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getFormattedListOfPlayers(true));
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @TeleportPlayer :(");
            return null;
         }
      }

   }
}
