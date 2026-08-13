package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class f4 {
   @SubscribeEvent
   public void a(EntityInteractSpecific var1) {
      Entity var2 = var1.getTarget();
      if (var2 instanceof BaseGirlEntity) {
         EntityPlayer var3 = var1.getEntityPlayer();
         ItemStack var4;
         if (var3.func_184614_ca().func_77973_b() == Items.field_151057_cb) {
            var4 = var3.func_184614_ca();
         } else {
            if (var3.func_184592_cb().func_77973_b() != Items.field_151057_cb) {
               return;
            }

            var4 = var3.func_184592_cb();
         }

         String var5 = var4.func_82833_r();
         if (!"".equals(var5)) {
            ((BaseGirlEntity)var2).g_clash538(var5);
            if (!var3.field_71075_bZ.field_75098_d) {
               var4.func_190918_g(1);
            }

            var1.setCanceled(true);
            var1.setResult(Result.DENY);
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
