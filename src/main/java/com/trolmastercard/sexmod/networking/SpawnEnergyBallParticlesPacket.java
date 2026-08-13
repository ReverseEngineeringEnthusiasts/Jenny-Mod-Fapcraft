package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SpawnEnergyBallParticlesPacket implements IMessage {
   boolean c = false;
   UUID a;
   UUID b;

   public SpawnEnergyBallParticlesPacket() {
   }

   public SpawnEnergyBallParticlesPacket(UUID var1, UUID var2) {
      this.a = var1;
      this.b = var2;
   }

   public void fromBytes(ByteBuf var1) {
      try {
         this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      } catch (Exception var3) {
         this.a = null;
      }

      try {
         this.b = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      } catch (Exception var2) {
         this.b = null;
      }

      this.c = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a == null ? "trol was here" : this.a.toString());
      ByteBufUtils.writeUTF8String(var1, this.b == null ? "trol was here" : this.b.toString());
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket var1, MessageContext var2) {
         if (var1.c && var2.side.equals(Side.CLIENT)) {
            BaseGirlEntity var3 = BaseGirlEntity.b_clash522(var1.a);
            if (!(var3 instanceof GalathEntity)) {
               System.out.println("doesnt exit");
               return null;
            } else {
               GalathCoinItem.a(var1.b, (GalathEntity)var3);
               return null;
            }
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
