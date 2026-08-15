package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * <b>Role.</b> Two-way animation-controller reset for a girl.
 * <p>
 * CLIENT->SERVER: the interacting player leaves a scene / disconnects; the
 * SERVER-side handler resets the girl's action tick counters and forwards the
 * packet to every *other* player within 100 blocks so their client-side
 * animation controller is reset too (the girl's client animation must restart
 * identically for all observers).
 * <p>
 * CLIENT-side handler: calls
 * {@link BaseGirlEntity#resetAnimationControllerTicks()} on the local client
 * entity to restart her animation cycle.
 */
public class ResetControllerPacket implements IMessage {
   public static final int controllerIndex = 100;
   boolean isValid;
   UUID girlUUID;
   UUID playerUUID;

   public ResetControllerPacket() {
      this.isValid = false;
   }

   public ResetControllerPacket(UUID girlUUID) {
      this.girlUUID = girlUUID;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<ResetControllerPacket, IMessage> {
      public IMessage onMessage(ResetControllerPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @ResetController :(");
            return null;
         }

         if (ctx.side.isServer()) {
            BaseGirlEntity serverGirl = BaseGirlEntity.getServerGirlEntity(packet.girlUUID);
            if (serverGirl == null) {
               return null;
            }

            UUID playerUuid = ctx.getServerHandler().player.getPersistentID();
            serverGirl.getCurrentAction().ticksPlaying = new int[]{0, 0};

            for (EntityPlayerMP otherPlayer : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
               if (!playerUuid.equals(otherPlayer.getPersistentID()) && otherPlayer.getDistance(serverGirl) < 100.0F) {
                  PacketHandler.networkWrapper.sendTo(new ResetControllerPacket(packet.girlUUID), otherPlayer);
               }
            }

            return null;
         } else {
            BaseGirlEntity clientGirl = BaseGirlEntity.getClientGirlEntity(packet.girlUUID);
            if (clientGirl != null) {
               clientGirl.resetAnimationControllerTicks();
            }

            return null;
         }
      }

   }
}
