package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> SERVER->CLIENT particle burst around a girl (used by scenes and
 * item effects, e.g. heart/heal particles).
 * <p>
 * <b>Handler.</b> CLIENT-side. For the client-side entity matching the UUID,
 * spawns {@code count} particles of the named type around her via
 * {@link BaseGirlEntity#spawnParticlesAround}.
 */
public class SpawnParticlePacket implements IMessage {
   boolean isValid = false;
   UUID girlUUID;
   String particleType;
   int count;

   public SpawnParticlePacket() {
   }

   public SpawnParticlePacket(UUID girlUUID, String particleType) {
      this.girlUUID = girlUUID;
      this.particleType = particleType;
      this.count = 1;
   }

   public SpawnParticlePacket(UUID girlUUID, String particleType, int count) {
      this.girlUUID = girlUUID;
      this.particleType = particleType;
      this.count = count;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.particleType = ByteBufUtils.readUTF8String(buf);
      this.count = buf.readInt();
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(buf, this.particleType);
      buf.writeInt(this.count);
   }

   public static class Handler implements IMessageHandler<SpawnParticlePacket, IMessage> {
      public IMessage onMessage(SpawnParticlePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side.equals(Side.CLIENT)) {
            for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
               if (girl.world.isRemote) {
                  for (int i = 0; i < packet.count; i++) {
                     BaseGirlEntity.spawnParticlesAround(EnumParticleTypes.getByName(packet.particleType), girl);
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

   }
}
