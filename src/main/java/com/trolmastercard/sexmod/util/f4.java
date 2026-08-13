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
         if (var3.getHeldItemMainhand().getItem() == Items.NAME_TAG) {
            var4 = var3.getHeldItemMainhand();
         } else {
            if (var3.getHeldItemOffhand().getItem() != Items.NAME_TAG) {
               return;
            }

            var4 = var3.getHeldItemOffhand();
         }

         String var5 = var4.getDisplayName();
         if (!"".equals(var5)) {
            ((BaseGirlEntity)var2).g_clash538(var5);
            if (!var3.capabilities.isCreativeMode) {
               var4.shrink(1);
            }

            var1.setCanceled(true);
            var1.setResult(Result.DENY);
         }
      }
   }

}
