package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class UpdateVelocityPacket implements IMessage {
   boolean c = false;
   Vec3d b;
   UUID a;

   public UpdateVelocityPacket(Vec3d var1, UUID var2) {
      this.b = var1;
      this.a = var2;
   }

   public UpdateVelocityPacket() {
   }

   public void fromBytes(ByteBuf var1) {
      this.b = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.b.field_72450_a);
      var1.writeDouble(this.b.field_72448_b);
      var1.writeDouble(this.b.field_72449_c);
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
   }

   public static class Handler implements IMessageHandler<UpdateVelocityPacket, IMessage> {
      public IMessage onMessage(UpdateVelocityPacket var1, MessageContext var2) {
         if (var1.c && var2.side.equals(Side.SERVER)) {
            FMLCommonHandler.instance().getMinecraftServerInstance().func_152344_a(() -> {
               BaseGirlEntity var2x = BaseGirlEntity.a_clash523(var1.a);
               if (var2x instanceof GalathEntity) {
                  GalathEntity var3 = (GalathEntity)var2x;
                  if (var2.getServerHandler().field_147369_b.equals(var3.ab_clash671())) {
                     var3.d(var1.b);
                  }
               }
            });
            return null;
         } else {
            System.out.println("received an invalid message @UpdateVelocity :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
