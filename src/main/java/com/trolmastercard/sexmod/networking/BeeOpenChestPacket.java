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

   public BeeOpenChestPacket(UUID var1, UUID var2) {
      this.girlUUID = var1;
      this.playerUUID = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.playerUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.playerUUID.toString());
   }

   public static class Handler implements IMessageHandler<BeeOpenChestPacket, IMessage> {
      public IMessage onMessage(BeeOpenChestPacket var1, MessageContext var2) {
         if (!var1.isValid) {
            System.out.println("received an invalid message @BeeOpenChest :(");
            return null;
         } else {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                        if (!var3.world.isRemote && var3 instanceof BeeEntity) {
                           BeeEntity var4 = (BeeEntity)var3;
                           if ((Boolean)var4.getDataManager().get(BeeEntity.HORNY_FLAG)) {
                              EntityPlayerMP var5 = (EntityPlayerMP)var4.world.getPlayerEntityByUUID(var1.playerUUID);
                              if (var5 != null) {
                                 var5.openGui(
                                    Main.instance,
                                    1,
                                    var3.world,
                                    var3.getPosition().getX(),
                                    var3.getPosition().getY(),
                                    var3.getPosition().getZ()
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
