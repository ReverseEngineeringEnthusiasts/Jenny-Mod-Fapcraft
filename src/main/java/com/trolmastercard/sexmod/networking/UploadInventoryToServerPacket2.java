package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "close the girl's inventory" — removes the girl
 * entity from the server world when the player closes the chest GUI while it
 * contains the girl's items (the girl is only "materialized" while her chest is
 * open).
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Removes every
 * entity matching the UUID via {@code world.removeEntity} — the client mirrors
 * the despawn via the vanilla destroy packet.
 */
public class UploadInventoryToServerPacket2 implements IMessage {
   boolean isValid;
   UUID girlUUID;

   public UploadInventoryToServerPacket2() {
   }

   public UploadInventoryToServerPacket2(UUID girlUUID) {
      this.girlUUID = girlUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<UploadInventoryToServerPacket2, IMessage> {
      public IMessage onMessage(UploadInventoryToServerPacket2 packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                  if (!girl.world.isRemote) {
                     girl.world.removeEntity(girl);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UploadInventoryToServer :(");
            return null;
         }
      }

   }
}
