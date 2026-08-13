package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class StartStandingSexAnimationPacket implements IMessage {
   boolean c;
   UUID a;
   UUID b;
   String d;

   public StartStandingSexAnimationPacket() {
   }

   public StartStandingSexAnimationPacket(UUID var1, UUID var2, String var3) {
      this.a = var1;
      this.b = var2;
      this.d = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.d = ByteBufUtils.readUTF8String(var1);
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
      ByteBufUtils.writeUTF8String(var1, this.d);
   }

   public static class Handler implements IMessageHandler<StartStandingSexAnimationPacket, IMessage> {
      public IMessage onMessage(StartStandingSexAnimationPacket var1, MessageContext var2) {
         if (var1.c && var2.side == Side.SERVER) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               AbstractPlayerGirlEntity var1x = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.a);
               if (var1x != null) {
                  if (!FMLCommonHandler.instance().getMinecraftServerInstance().func_71262_S()) {
                     try {
                        for (BaseGirlEntity var3 : BaseGirlEntity.getGirlEntityList()) {
                           if (var3 instanceof AbstractPlayerGirlEntity) {
                              var1x = (AbstractPlayerGirlEntity)var3;
                              if (!var1x.field_70170_p.field_72995_K && var1x.getOwnerUserUUID().equals(var1.a)) {
                                 break;
                              }
                           }
                        }
                     } catch (Exception var4) {
                     }
                  }

                  var1x.b(var1.d, var1.b);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @StartStandingSexAnimation :(");
            return null;
         }
      }

   }
}
