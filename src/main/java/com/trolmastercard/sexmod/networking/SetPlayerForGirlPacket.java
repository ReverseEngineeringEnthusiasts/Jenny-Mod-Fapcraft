package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.JennyEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SetPlayerForGirlPacket implements IMessage {
   boolean isValid;
   UUID girlUUID;
   UUID playerUUID;

   public SetPlayerForGirlPacket() {
      this.isValid = false;
   }

   public SetPlayerForGirlPacket(UUID var1, UUID var2) {
      this.girlUUID = var1;
      this.playerUUID = var2;
      this.isValid = true;
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

   public static class Handler implements IMessageHandler<SetPlayerForGirlPacket, IMessage> {
      public IMessage onMessage(SetPlayerForGirlPacket var1, MessageContext var2) {
         if (var1.isValid && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.girlUUID)) {
                  PlayerList var4 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

                  try {
                     var4.getPlayerByUUID(var1.playerUUID).getName();
                  } catch (NullPointerException var8) {
                     System.out.println("couldn't find player with UUID: " + var1.playerUUID);
                     System.out.println("could only find players with thsese UUID's:");

                     for (EntityPlayerMP var7 : var4.getPlayers()) {
                        System.out.println(var7.getName() + " " + var7.getUniqueID());
                     }
                     continue;
                  }

                  if (var3 instanceof JennyEntity) {
                     ((JennyEntity)var3).af = true;
                  }

                  var3.setInteractionPlayerUUID(var1.playerUUID);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @SetPlayerForGirl :(");
            return null;
         }
      }

   }
}
