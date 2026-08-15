package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEggProjectileEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.TrailSegment;
import io.netty.buffer.ByteBuf;
import java.util.UUID;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * <b>Role.</b> CLIENT->SERVER "send my girl home" command from the interaction
 * menu. Sends the girl home via an ender-pearl-style teleport.
 * <p>
 * <b>Handler.</b> SERVER-side, scheduled on the main thread — a 3-phase state
 * machine on the girl:
 * <ol>
 * <li>{@code Action != THROW_PEARL}: start phase — set
 *     {@code THROW_PEARL}, face her {@code homePos}, anchor her.</li>
 * <li>no active pearl yet: spawn the {@link KoboldEggProjectileEntity} aimed at
 *     {@code homePos}.</li>
 * <li>pearl exists: burst PORTAL particles, snap her to {@code homePos},
 *     un-anchor, {@code Action.NULL} and {@code goHome()}.</li>
 * </ol>
 * Re-sending the packet advances the machine; the girl must be a bed-scene
 * capable girl ({@link IBeddableSexGirl} style flow) for a clean home return.
 */
public class SendCompanionHomePacket implements IMessage {
   boolean isValid;
   UUID girlUUID;

   public SendCompanionHomePacket() {
   }

   public SendCompanionHomePacket(UUID girlUUID) {
      this.girlUUID = girlUUID;
   }

   public void fromBytes(ByteBuf buf) {
      this.girlUUID = UUID.fromString(ByteBufUtils.readUTF8String(buf));
      this.isValid = true;
   }

   public void toBytes(ByteBuf buf) {
      ByteBufUtils.writeUTF8String(buf, this.girlUUID.toString());
   }

   public static class Handler implements IMessageHandler<SendCompanionHomePacket, IMessage> {
      public IMessage onMessage(SendCompanionHomePacket packet, MessageContext ctx) {
         if (packet.isValid && ctx.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity girl : BaseGirlEntity.girlList(packet.girlUUID)) {
                        if (!girl.world.isRemote) {
                           if (girl.getCurrentAction() != Action.THROW_PEARL) {
                              girl.setCurrentAction(Action.THROW_PEARL);
                              girl.setYawRotation(
                                 (float)Math.atan2(girl.posZ - girl.homePos.z, girl.posX - girl.homePos.x)
                                       * (float) (180.0 / Math.PI)
                                    + 90.0F
                              );
                              girl.setTargetPosition(girl.getPositionVector());
                              girl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                              girl.activeEnderPearl = null;
                           } else if (girl.activeEnderPearl == null) {
                              float distance = (float)girl.getPositionVector().distanceTo(girl.homePos);
                              girl.activeEnderPearl = new KoboldEggProjectileEntity(girl.world, girl);
                              girl.activeEnderPearl
                                 .shoot(
                                    girl.homePos.x - girl.posX,
                                    girl.homePos.y - girl.posY,
                                    girl.homePos.z - girl.posZ,
                                    Math.min(4.0F, distance * 0.1F),
                                    0.0F
                                 );
                              girl.world.spawnEntity(girl.activeEnderPearl);
                           } else {
                              WorldServer worldServer = (WorldServer)girl.world;

                              for (int i = 0; i < 32; i++) {
                                 worldServer.spawnParticle(
                                    EnumParticleTypes.PORTAL,
                                    false,
                                    girl.posX,
                                    girl.posY + Reference.RANDOM.nextDouble() * 2.0,
                                    girl.posZ,
                                    32,
                                    0.2,
                                    0.2,
                                    0.2,
                                    Reference.RANDOM.nextGaussian(),
                                    new int[0]
                                 );
                              }

                              girl.setPosition(girl.homePos.x, girl.homePos.y, girl.homePos.z);
                              girl.activeEnderPearl = null;
                              girl.setCurrentAction(Action.NULL);
                              girl.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
                              girl.goHome();
                           }
                        }
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @SendCompanionHome :(");
            return null;
         }
      }

   }
}
