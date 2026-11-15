package com.trolmastercard.sexmod.network.galath;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.common.entity.galath.DragonChargeEntity;
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
public class DragonBreathParticlesPacket implements IMessage {
   Vec3d energyPos;
   boolean isValid;
   boolean isLeftSide = false;

   public DragonBreathParticlesPacket() {
   }

   public DragonBreathParticlesPacket(Vec3d energyPos, boolean isValid) {
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

   public static class Handler implements IMessageHandler<DragonBreathParticlesPacket, IMessage> {
      public IMessage onMessage(DragonBreathParticlesPacket packet, MessageContext ctx) {
         if (packet.isLeftSide && ctx.side.equals(Side.CLIENT)) {
            if (packet.isValid) {
               DragonChargeEntity.spawnDragonBreath(packet.energyPos);
            } else {
               DragonChargeEntity.spawnDragonBreathRandom(packet.energyPos);
            }

            return null;
         } else {
            Main.LOGGER.warn("received an invalid message @SpawnEnergyBallParticles :(");
            return null;
         }
      }

   }
}
