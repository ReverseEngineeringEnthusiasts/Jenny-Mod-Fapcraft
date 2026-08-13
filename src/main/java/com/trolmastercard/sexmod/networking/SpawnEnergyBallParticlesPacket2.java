package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.util.TrailSegment;







import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SpawnEnergyBallParticlesPacket2 implements IMessage {
   Vec3d energyPos;
   boolean isValid;
   boolean isLeftSide = false;

   public SpawnEnergyBallParticlesPacket2() {
   }

   public SpawnEnergyBallParticlesPacket2(Vec3d var1, boolean var2) {
      this.energyPos = var1;
      this.isValid = var2;
   }

   public void fromBytes(ByteBuf var1) {
      this.energyPos = new Vec3d(var1.readDouble(), var1.readDouble(), var1.readDouble());
      this.isValid = var1.readBoolean();
      this.isLeftSide = true;
   }

   public void toBytes(ByteBuf var1) {
      var1.writeDouble(this.energyPos.x);
      var1.writeDouble(this.energyPos.y);
      var1.writeDouble(this.energyPos.z);
      var1.writeBoolean(this.isValid);
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket2, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket2 var1, MessageContext var2) {
         if (var1.isLeftSide && var2.side.equals(Side.CLIENT)) {
            if (var1.isValid) {
               DragonEntity.a_clash118(var1.energyPos);
            } else {
               DragonEntity.c_clash119(var1.energyPos);
            }

            return null;
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
