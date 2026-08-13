package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.fg;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SendGirlToSexPacket implements IMessage {
   boolean a;
   UUID b;

   public SendGirlToSexPacket() {
      this.a = false;
   }

   public SendGirlToSexPacket(UUID var1) {
      this.b = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.b.toString());
   }

   public static class Handler implements IMessageHandler<SendGirlToSexPacket, IMessage> {
      public IMessage onMessage(SendGirlToSexPacket var1, MessageContext var2) {
         if (!var1.a) {
            System.out.println("received an invalid message @SendGirlToSex :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().addScheduledTask(() -> {
               for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.b)) {
                  if (!var3.world.isRemote && var3 instanceof fg) {
                     ((fg)var3).a_clash292();
                  }
               }
            });
            return null;
         }
      }

   }
}
