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

   public SpawnEnergyBallParticlesPacket2(Vec3d energyPos, boolean isValid) {
      this.energyPos = energyPos;
      this.isValid = isValid;
   }

   public void fromBytes(ByteBuf buf) {
      this.energyPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
      this.isValid = buf.readBoolean();
      this.isLeftSide = true;
   }

   public void toBytes(ByteBuf buf) {
      buf.writeDouble(this.energyPos.x);
      buf.writeDouble(this.energyPos.y);
      buf.writeDouble(this.energyPos.z);
      buf.writeBoolean(this.isValid);
   }

   public static class Handler implements IMessageHandler<SpawnEnergyBallParticlesPacket2, IMessage> {
      public IMessage onMessage(SpawnEnergyBallParticlesPacket2 packet, MessageContext ctx) {
         if (packet.isLeftSide && ctx.side.equals(Side.CLIENT)) {
            if (packet.isValid) {
               DragonEntity.spawnDragonBreath(packet.energyPos);
            } else {
               DragonEntity.spawnDragonBreathRandom(packet.energyPos);
            }

            return null;
         } else {
            System.out.println("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
