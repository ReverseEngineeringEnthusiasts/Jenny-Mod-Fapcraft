package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteractSpecific;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

/**
 * Handles name-tag naming of girls on right-click (sets CUSTOM_NAME).
 */
public class NameTagInteractHandler {
   @SubscribeEvent
   public void onEntityInteractSpecific(EntityInteractSpecific event) {
      Entity target = event.getTarget();
      if (target instanceof BaseGirlEntity) {
         EntityPlayer player = event.getEntityPlayer();
         ItemStack stack;
         if (player.getHeldItemMainhand().getItem() == Items.NAME_TAG) {
            stack = player.getHeldItemMainhand();
         } else {
            if (player.getHeldItemOffhand().getItem() != Items.NAME_TAG) {
               return;
            }

            stack = player.getHeldItemOffhand();
         }

         String displayName = stack.getDisplayName();
         if (!"".equals(displayName)) {
            ((BaseGirlEntity)target).setCustomNameOverride(displayName);
            if (!player.capabilities.isCreativeMode) {
               stack.shrink(1);
            }

            event.setCanceled(true);
            event.setResult(Result.DENY);
         }
      }
   }

}
