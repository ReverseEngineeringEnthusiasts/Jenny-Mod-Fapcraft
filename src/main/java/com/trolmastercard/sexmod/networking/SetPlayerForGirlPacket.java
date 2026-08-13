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
   boolean a;
   UUID c;
   UUID b;

   public SetPlayerForGirlPacket() {
      this.a = false;
   }

   public SetPlayerForGirlPacket(UUID var1, UUID var2) {
      this.c = var1;
      this.b = var2;
      this.a = true;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
   }

   public static class Handler implements IMessageHandler<SetPlayerForGirlPacket, IMessage> {
      public IMessage onMessage(SetPlayerForGirlPacket var1, MessageContext var2) {
         if (var1.a && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.c)) {
                  PlayerList var4 = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();

                  try {
                     var4.getPlayerByUUID(var1.b).getName();
                  } catch (NullPointerException var8) {
                     System.out.println("couldn't find player with UUID: " + var1.b);
                     System.out.println("could only find players with thsese UUID's:");

                     for (EntityPlayerMP var7 : var4.getPlayers()) {
                        System.out.println(var7.getName() + " " + var7.getUniqueID());
                     }
                     continue;
                  }

                  if (var3 instanceof JennyEntity) {
                     ((JennyEntity)var3).af = true;
                  }

                  var3.setInteractionPlayerUUID(var1.b);
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
