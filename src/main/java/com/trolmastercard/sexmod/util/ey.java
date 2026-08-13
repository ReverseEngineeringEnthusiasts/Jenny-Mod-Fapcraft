package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ey {
   static final int a = 3;

   @SubscribeEvent
   public void a(BreakEvent var1) {
      Block var2 = var1.getState().func_177230_c();
      if (var2 == Blocks.field_150324_C) {
         BlockPos var3 = var1.getPos();
         AxisAlignedBB var4 = new AxisAlignedBB(
            var3.func_177958_n() - 3,
            var3.func_177956_o() - 3,
            var3.func_177952_p() - 3,
            var3.func_177958_n() + 3,
            var3.func_177956_o() + 3,
            var3.func_177952_p() + 3
         );
         List var5 = var1.getWorld().func_72872_a(BaseGirlEntity.class, var4);
         boolean var6 = false;

         for (BaseGirlEntity var8 : (java.util.Collection<BaseGirlEntity>) (var5) ) {
            if (!var8.field_70128_L && (Boolean)var8.func_184212_Q().func_187225_a(BaseGirlEntity.G)) {
               var6 = true;
               break;
            }
         }

         if (var6) {
            var1.getPlayer()
               .func_146105_b(new TextComponentString("this bed is currently used by a girl.. pls don't disturb okay? ... you are kinda mean rn"), true);
            var1.setCanceled(true);
         }
      }
   }

   @SubscribeEvent
   @SideOnly(Side.CLIENT)
   public void a(PlayerSPPushOutOfBlocksEvent var1) {
      if (BaseGirlEntity.d_clash532(var1.getEntityPlayer()) != null) {
         var1.setCanceled(true);
      }
   }

}
