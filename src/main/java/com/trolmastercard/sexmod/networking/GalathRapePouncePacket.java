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

public class GalathRapePouncePacket implements IMessage {
   boolean a = false;
   boolean b;

   public GalathRapePouncePacket() {
   }

   public GalathRapePouncePacket(boolean var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = var1.readBoolean();
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeBoolean(this.b);
   }

   public static class Handler implements IMessageHandler<GalathRapePouncePacket, IMessage> {
      public IMessage onMessage(GalathRapePouncePacket var1, MessageContext var2) {
         if (var1.a && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               BaseGirlEntity var2x = BaseGirlEntity.getActiveSceneInfo(var2.getServerHandler().player.getPersistentID());
               if (var2x instanceof GalathEntity) {
                  ((GalathEntity)var2x).c_clash694(var1.b);
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @GalathRapePounce :(");
            return null;
         }
      }

   }
}
