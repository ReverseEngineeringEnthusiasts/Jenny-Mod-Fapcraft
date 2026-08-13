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
               .func_152344_a(
                  () -> {
                     for (BaseGirlEntity var3 : BaseGirlEntity.girlList(var1.a)) {
                        if (!var3.field_70170_p.field_72995_K) {
                           if (var3.getCurrentAction() != fp.THROW_PEARL) {
                              var3.b(fp.THROW_PEARL);
                              var3.setYawRotation(
                                 (float)Math.atan2(var3.field_70161_v - var3.l.field_72449_c, var3.field_70165_t - var3.l.field_72450_a)
                                       * (float) (180.0 / Math.PI)
                                    + 90.0F
                              );
                              var3.setTargetPosition(var3.func_174791_d());
                              var3.func_184212_Q().func_187227_b(BaseGirlEntity.G, true);
                              var3.q = null;
                           } else if (var3.q == null) {
                              float var6 = (float)var3.func_174791_d().func_72438_d(var3.l);
                              var3.q = new KoboldEggProjectileEntity(var3.field_70170_p, var3);
                              var3.q
                                 .func_70186_c(
                                    var3.l.field_72450_a - var3.field_70165_t,
                                    var3.l.field_72448_b - var3.field_70163_u,
                                    var3.l.field_72449_c - var3.field_70161_v,
                                    Math.min(4.0F, var6 * 0.1F),
                                    0.0F
                                 );
                              var3.field_70170_p.func_72838_d(var3.q);
                           } else {
                              WorldServer var4 = (WorldServer)var3.field_70170_p;

                              for (int var5 = 0; var5 < 32; var5++) {
                                 var4.func_180505_a(
                                    EnumParticleTypes.PORTAL,
                                    false,
                                    var3.field_70165_t,
                                    var3.field_70163_u + Reference.f.nextDouble() * 2.0,
                                    var3.field_70161_v,
                                    32,
                                    0.2,
                                    0.2,
                                    0.2,
                                    Reference.f.nextGaussian(),
                                    new int[0]
                                 );
                              }

                              var3.func_70107_b(var3.l.field_72450_a, var3.l.field_72448_b, var3.l.field_72449_c);
                              var3.q = null;
                              var3.b(fp.NULL);
                              var3.func_184212_Q().func_187227_b(BaseGirlEntity.G, false);
                              var3.x_clash475();
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
