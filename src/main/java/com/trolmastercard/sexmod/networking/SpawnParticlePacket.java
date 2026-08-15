package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.TrailSegment;
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

   public SpawnParticlePacket(UUID var1, String var2) {
      this.girlUUID = var1;
      this.particleType = var2;
      this.count = 1;
   }

   public SpawnParticlePacket(UUID var1, String var2, int var3) {
      this.girlUUID = var1;
      this.particleType = var2;
      this.count = var3;
   }

   public void fromBytes(ByteBuf var1) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.particleType = ByteBufUtils.readUTF8String(var1);
      this.count = var1.readInt();
      this.isValid = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.girlUUID.toString());
      ByteBufUtils.writeUTF8String(var1, this.particleType);
      var1.writeInt(this.count);
   }

   public static class Handler implements IMessageHandler<SpawnParticlePacket, IMessage> {
      public IMessage onMessage(SpawnParticlePacket var1, MessageContext var2) {
         if (var1.isValid && var2.side.equals(Side.CLIENT)) {
            for (BaseGirlEntity var5 : BaseGirlEntity.girlList(var1.girlUUID)) {
               if (var5.world.isRemote) {
                  for (int var6 = 0; var6 < var1.count; var6++) {
                     BaseGirlEntity.spawnParticlesAround(EnumParticleTypes.getByName(var1.particleType), var5);
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
