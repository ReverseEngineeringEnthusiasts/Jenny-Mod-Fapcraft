package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BeeEntity;
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
 * <b>Role.</b> CLIENT->SERVER request to open the bee-girl's chest GUI. Sent when
 * the player clicks "open chest" on a horny {@link BeeEntity}.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread. Looks up the girl by
 * UUID in {@link BaseGirlEntity#girlList}; only if she is a {@link BeeEntity} with
 * the {@code HORNY_FLAG} data-manager flag set, opens GUI id 1 at her position for
 * the requesting player (the bee's inventory screen). If the flag is not set the
 * request is silently dropped — the horny flag gates access to the chest.
 */
public class BeeOpenChestPacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   UUID playerUUID;

   public BeeOpenChestPacket() {
   }

   public BeeOpenChestPacket(UUID girlUUID, UUID playerUUID) {
      this.girlUUID = girlUUID;
      this.playerUUID = playerUUID;
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

   public static class Handler implements IMessageHandler<BeeOpenChestPacket, IMessage> {
      public IMessage onMessage(BeeOpenChestPacket packet, MessageContext ctx) {
         if (!packet.isValid) {
            System.out.println("received an invalid message @BeeOpenChest :(");
            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                        if (!girl.world.isRemote && girl instanceof BeeEntity) {
                           BeeEntity bee = (BeeEntity)girl;
                           if ((Boolean)bee.getDataManager().get(BeeEntity.HORNY_FLAG)) {
                              EntityPlayerMP player = (EntityPlayerMP)bee.world.getPlayerEntityByUUID(packet.playerUUID);
                              if (player != null) {
                                 player.openGui(
                                    Main.instance,
                                    1,
                                    girl.world,
                                    girl.getPosition().getX(),
                                    girl.getPosition().getY(),
                                    girl.getPosition().getZ()
                                 );
                                 return;
                              }
                           }
                        }
                     }
                  }
               );
            return null;
         }
      }

   }
}
