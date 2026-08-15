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

   public TeleportPlayerPacket(String var1, Vec3d var2) {
      this.girlId = var1;
      this.position = var2;
      this.yaw = 0.0F;
      this.pitch = 0.0F;
      this.isValid = true;
   }

   public TeleportPlayerPacket(String var1, Vec3d var2, float var3, float var4) {
      this.girlId = var1;
      this.position = var2;
      this.yaw = var3;
      this.pitch = var4;
      this.isValid = true;
   }

   public TeleportPlayerPacket(String var1, double var2, double var4, double var6, float var8, float var9) {
      this.girlId = var1;
      this.position = new Vec3d(var2, var4, var6);
      this.yaw = var8;
      this.pitch = var9;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlId = ByteBufUtils.readUTF8String(var1);
      this.position = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.yaw = var1.readFloat();
      this.pitch = var1.readFloat();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlId);
      var1.writeDouble(this.position.x);
      var1.writeDouble(this.position.y);
      var1.writeDouble(this.position.z);
      var1.writeFloat(this.yaw);
      var1.writeFloat(this.pitch);
      this.isValid = true;
   }

   public static class Handler implements IMessageHandler<TeleportPlayerPacket, IMessage> {
      public IMessage onMessage(TeleportPlayerPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     try {
                        System.out.println("teleporting player " + var1.girlId + " to " + var1.position);
                        EntityPlayerMP var1x = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(UUID.fromString(var1.girlId));
                        var1.yaw = MathHelper.wrapDegrees(var1.yaw);
                        var1.pitch = MathHelper.wrapDegrees(var1.pitch);
                        var1x.setLocationAndAngles(var1.position.x, var1.position.y, var1.position.z, var1.yaw, var1.pitch);
                        var1x.setRotationYawHead(var1.yaw);
                        var1x.motionX = 0.0;
                        var1x.motionY = 0.0;
                        var1x.motionZ = 0.0;
                        var1x.connection
                           .setPlayerLocation(var1.position.x, var1.position.y, var1.position.z, var1.yaw, var1.pitch, EnumSet.noneOf(EnumFlags.class));
                     } catch (Exception var2x) {
                        System.out.println("couldn't find player with UUID: " + var1.girlId);
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
