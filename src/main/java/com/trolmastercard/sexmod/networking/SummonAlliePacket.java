package com.trolmastercard.sexmod.networking;

import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.an;







import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class SummonAlliePacket implements IMessage {
   boolean a = false;

   public void fromBytes(ByteBuf var1) {
      this.a = true;
   }

   public void toBytes(ByteBuf var1) {
   }

   public static class Handler implements IMessageHandler<SummonAlliePacket, IMessage> {
      public IMessage onMessage(SummonAlliePacket var1, MessageContext var2) {
         if (var1.a && var2.side == Side.SERVER) {
            FMLCommonHandler.instance()
               .getMinecraftServerInstance()
               .func_152344_a(
                  () -> {
                     EntityPlayerMP var1x = var2.getServerHandler().field_147369_b;
                     Vec3d var2x = var1x.func_174791_d()
                        .func_72441_c(-Math.sin(var1x.field_70759_as * (Math.PI / 180.0)) * 2.0, 0.0, Math.cos(var1x.field_70759_as * (Math.PI / 180.0)) * 2.0);
                     AllieEntity var3 = new AllieEntity(var1x.field_70170_p, var1x.func_184614_ca());
                     var3.e_clash499(var1x.getPersistentID());
                     var3.func_70080_a(var2x.field_72450_a, var2x.field_72448_b, var2x.field_72449_c, var1x.field_70759_as + 180.0F, var1x.field_70125_A);
                     var3.c_clash502(var3.func_174791_d());
                     var3.b_clash431(var1x.field_70759_as + 180.0F);
                     var3.func_189654_d(true);
                     var3.field_70145_X = true;
                     var1x.field_70170_p.func_72838_d(var3);
                     BlockPos var4 = var3.func_180425_c().func_177982_a(0, -1, 0);
                     if (var3.field_70170_p.func_180495_p(var4).func_177230_c().equals(Blocks.field_150354_m)) {
                        var3.b(fp.SUMMON_SAND);
                     } else {
                        var3.b(var3.f_clash697() ? fp.SUMMON : fp.SUMMON_NORMAL);
                     }
                  }
               );
            return null;
         } else {
            System.out.println("received an invalid message @SummonAllie :(");
            return null;
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
