package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SetNewHomePacket implements IMessage {
   boolean b;
   UUID c;
   Vec3d a;

   public SetNewHomePacket() {
   }

   public SetNewHomePacket(UUID var1, Vec3d var2) {
      this.c = var1;
      this.a = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.a = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      var1.writeDouble(this.a.field_72450_a);
      var1.writeDouble(this.a.field_72448_b);
      var1.writeDouble(this.a.field_72449_c);
   }

   public static class Handler implements IMessageHandler<SetNewHomePacket, IMessage> {
      public IMessage onMessage(SetNewHomePacket var1, MessageContext var2) {
         if (!var1.b) {
            System.out.println("received an invalid message @SetNewHome :(");
            return null;
         } else {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               ArrayList var1x = BaseGirlEntity.g_clash524(var1.c);
               if (!var1x.isEmpty()) {
                  for (BaseGirlEntity var3 : (java.util.Collection<BaseGirlEntity>) (var1x) ) {
                     var3.l = new Vec3d(var1.a.field_72450_a, Math.floor(var1.a.field_72448_b), var1.a.field_72449_c);
                  }
               }
            });
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
