package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEggProjectileEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.an;







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

public class SendCompanionHomePacket implements IMessage {
   boolean b;
   UUID a;

   public SendCompanionHomePacket() {
   }

   public SendCompanionHomePacket(UUID var1) {
      this.a = var1;
   }

   public void fromBytes(ByteBuf var1) {
      this.a = UUID.fromString(ByteBufUtils.readUTF8String(var1));
      this.b = true;
   }

   public void toBytes(ByteBuf var1) {
      ByteBufUtils.writeUTF8String(var1, this.a.toString());
   }

   public static class Handler implements IMessageHandler<SendCompanionHomePacket, IMessage> {
      public IMessage onMessage(SendCompanionHomePacket var1, MessageContext var2) {
         if (var1.b && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .addScheduledTask(
                  () -> {
                     for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.a)) {
                        if (!var3.world.isRemote) {
                           if (var3.getCurrentAction() != fp.THROW_PEARL) {
                              var3.setCurrentAction(fp.THROW_PEARL);
                              var3.setYawRotation(
                                 (float)Math.atan2(var3.posZ - var3.homePos.z, var3.posX - var3.homePos.x)
                                       * (float) (180.0 / Math.PI)
                                    + 90.0F
                              );
                              var3.setTargetPosition(var3.getPositionVector());
                              var3.getDataManager().set(BaseGirlEntity.IS_ANCHORED, true);
                              var3.activeEnderPearl = null;
                           } else if (var3.activeEnderPearl == null) {
                              float var6 = (float)var3.getPositionVector().distanceTo(var3.homePos);
                              var3.activeEnderPearl = new KoboldEggProjectileEntity(var3.world, var3);
                              var3.activeEnderPearl
                                 .shoot(
                                    var3.homePos.x - var3.posX,
                                    var3.homePos.y - var3.posY,
                                    var3.homePos.z - var3.posZ,
                                    Math.min(4.0F, var6 * 0.1F),
                                    0.0F
                                 );
                              var3.world.spawnEntity(var3.activeEnderPearl);
                           } else {
                              WorldServer var4 = (WorldServer)var3.world;

                              for (int var5 = 0; var5 < 32; var5++) {
                                 var4.spawnParticle(
                                    EnumParticleTypes.PORTAL,
                                    false,
                                    var3.posX,
                                    var3.posY + Reference.f.nextDouble() * 2.0,
                                    var3.posZ,
                                    32,
                                    0.2,
                                    0.2,
                                    0.2,
                                    Reference.f.nextGaussian(),
                                    new int[0]
                                 );
                              }

                              var3.setPosition(var3.homePos.x, var3.homePos.y, var3.homePos.z);
                              var3.activeEnderPearl = null;
                              var3.setCurrentAction(fp.NULL);
                              var3.getDataManager().set(BaseGirlEntity.IS_ANCHORED, false);
                              var3.goHome();
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
