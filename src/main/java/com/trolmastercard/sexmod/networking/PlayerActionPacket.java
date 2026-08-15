package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER request to open a girl's interaction GUI (id 0).
 * Sent when the player interacts with a girl (e.g. Jenny).
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Finds the girl by
 * UUID in the global girl list and opens GUI id 0 at her position for the
 * requesting player. The client-side {@code GuiHandler} maps id 0 to the girl
 * interaction screen.
 */
public class PlayerActionPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   UUID playerUUID;

   public PlayerActionPacket() {
   }

   public PlayerActionPacket(UUID girlUUID, UUID playerUUID) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
      this.isValid = true;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<PlayerActionPacket, IMessage> {
      public IMessage onMessage(PlayerActionPacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                        if (!girl.world.isRemote && girl.getGirlId().equals(packet.girlUUID)) {
                           ((EntityPlayerMP)girl.world.getPlayerEntityByUUID(packet.playerUUID))
                              .openGui(
                                 Main.instance,
                                 0,
                                 girl.world,
                                 girl.getPosition().getX(),
                                 girl.getPosition().getY(),
                                 girl.getPosition().getZ()
                              );
                        }
                     }
                  }
               );
            return null;
         } else {
            return null;
         }
      }

   }
}
