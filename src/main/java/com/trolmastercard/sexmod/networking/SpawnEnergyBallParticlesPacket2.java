package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT dragon breath-effect trigger for
 * {@link DragonEntity} — spawns a targeted or randomized dragon-breath particle
 * burst at a world position.
 * <p>
 * <b>Handler.</b> CLIENT-side. {@code isValid=true} ->
 * {@link DragonEntity#spawnDragonBreath(Vec3d)}, otherwise
 * {@link DragonEntity#spawnDragonBreathRandom(Vec3d)}.
 */
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
               DragonEntity.spawnDragonBreath(var1.energyPos);
            } else {
               DragonEntity.spawnDragonBreathRandom(var1.energyPos);
            }

            return null;
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
