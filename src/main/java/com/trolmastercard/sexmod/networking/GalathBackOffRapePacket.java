package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GalathBackOffRapePacket implements IMessage {
   boolean isValid = false;

   public void fromBytes(ByteBuf var1) {
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
   }

   public static class Handler implements IMessageHandler<GalathBackOffRapePacket, IMessage> {
      public IMessage onMessage(GalathBackOffRapePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var1x = BaseGirlEntity.getGirlByUUID(var2.getServerHandler().player.getPersistentID(), Boolean.valueOf(true));
               if (var1x instanceof GalathEntity) {
                  ((GalathEntity)var1x).w_clash641();
               }
            });
            return null;
         } else {
            System.out.println("received an invalid Message @GalathBackOffRape :(");
            return null;
         }
      }

   }
}
