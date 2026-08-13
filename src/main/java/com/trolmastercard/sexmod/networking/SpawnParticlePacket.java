package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SpawnParticlePacket implements IMessage {
   boolean d = false;
   UUID c;
   String b;
   int a;

   public SpawnParticlePacket() {
   }

   public SpawnParticlePacket(UUID var1, String var2) {
      this.c = var1;
      this.b = var2;
      this.a = 1;
   }

   public SpawnParticlePacket(UUID var1, String var2, int var3) {
      this.c = var1;
      this.b = var2;
      this.a = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.c = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = ByteBufUtils.readUTF8String(var1);
      this.a = var1.readInt();
      this.d = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.c.toString());
      ByteBufUtils.writeUTF8String(var1, this.b);
      var1.writeInt(this.a);
   }

   public static class Handler implements IMessageHandler<SpawnParticlePacket, IMessage> {
      public IMessage onMessage(SpawnParticlePacket var1, MessageContext var2) {
         if (var1.d && var2.side.equals(Side.CLIENT)) {
            for (BaseGirlEntity var5 : BaseGirlEntity.g_clash524(var1.c)) {
               if (var5.field_70170_p.field_72995_K) {
                  for (int var6 = 0; var6 < var1.a; var6++) {
                     BaseGirlEntity.a(EnumParticleTypes.func_186831_a(var1.b), var5);
                  }
                  break;
               }
            }

            return null;
         } else {
            System.out.println("received an invalid message @SpawnParticle :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
