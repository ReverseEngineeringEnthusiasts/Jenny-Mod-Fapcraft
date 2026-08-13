package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SpawnEnergyBallParticlesPacket2 implements IMessage {
   Vec3d a;
   boolean c;
   boolean b = false;

   public SpawnEnergyBallParticlesPacket2() {
   }

   public SpawnEnergyBallParticlesPacket2(Vec3d var1, boolean var2) {
      this.a = var1;
      this.c = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.c = var1.readBoolean();
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.a.field_72450_a);
      var1.writeDouble(this.a.field_72448_b);
      var1.writeDouble(this.a.field_72449_c);
      var1.writeBoolean(this.c);
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket2, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket2 var1, MessageContext var2) {
         if (var1.b && var2.side.equals(Side.CLIENT)) {
            if (var1.c) {
               DragonEntity.a_clash118(var1.a);
            } else {
               DragonEntity.c_clash119(var1.a);
            }

            return null;
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
