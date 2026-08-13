package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ForcePlayerGirlUpdatePacket implements IMessage {
   boolean d = false;
   UUID c;
   int b;
   fp a;

   public ForcePlayerGirlUpdatePacket() {
   }

   public ForcePlayerGirlUpdatePacket(UUID var1, int var2, fp var3) {
      this.c = var1;
      this.b = var2;
      this.a = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = var1.readInt();
      this.a = fp.valueOf(ByteBufUtils.readUTF8String(var1));
      this.d = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      var1.writeInt(this.b);
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
   }

   public static class Handler implements IMessageHandler<ForcePlayerGirlUpdatePacket, IMessage> {
      public IMessage onMessage(ForcePlayerGirlUpdatePacket var1, MessageContext var2) {
         if (var1.d && var2.side.equals(Side.CLIENT)) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.c);
            if (var3 == null) {
               return null;
            }

            var3.getDataManager().set(BaseGirlEntity.J, var1.a.toString());
            var3.getDataManager().set(BaseGirlEntity.D, var1.b);
            return null;
         } else {
            System.out.println("received an invalid message @ForcePlayerGirlUpdate :(");
            return null;
         }
      }

   }
}
