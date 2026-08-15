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

   public ResetControllerPacket(UUID var1) {
      this.girlUUID = var1;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<ResetControllerPacket, IMessage> {
      public IMessage onMessage(ResetControllerPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @ResetController :(");
            return null;
         }

         if (var2.side.isServer()) {
            BaseGirlEntity var7 = BaseGirlEntity.getServerGirlEntity(var1.girlUUID);
            if (var7 == null) {
               return null;
            }

            UUID var4 = var2.getServerHandler().player.getPersistentID();
            var7.getCurrentAction().ticksPlaying = new int[]{0, 0};

            for (EntityPlayerMP var6 : FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers()) {
               if (!var4.equals(var6.getPersistentID()) && var6.getDistance(var7) < 100.0F) {
                  PacketHandler.networkWrapper.sendTo(new ResetControllerPacket(var1.girlUUID), var6);
               }
            }

            return null;
         } else {
            BaseGirlEntity var3 = BaseGirlEntity.getClientGirlEntity(var1.girlUUID);
            if (var3 != null) {
               var3.resetAnimationControllerTicks();
            }

            return null;
         }
      }

   }
}
