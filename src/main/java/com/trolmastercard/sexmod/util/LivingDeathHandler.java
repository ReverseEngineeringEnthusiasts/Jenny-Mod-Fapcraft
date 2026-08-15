package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Forge death handler: cleans dead girls out of the global girl list
 * ({@code BaseGirlEntity.GLOBAL_GIRL_CACHE} / getGirlEntityList removals)
 * and handles girl-specific death bookkeeping.
 */
public class LivingDeathHandler {
   @SubscribeEvent(priority = EventPriority.LOW)
   public void onLivingDeath(LivingDeathEvent event) {
      if (event.getEntity() instanceof BaseGirlEntity) {
         BaseGirlEntity girl = (BaseGirlEntity)event.getEntity();
         BaseGirlEntity.getGirlEntityList().remove(girl);
      } else if (event.getEntity() instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.getEntity();
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByOwner(player.getPersistentID());
         if (playerGirl != null) {
            ResetGirlPacket.Handler.resetGirl(playerGirl);
         }
      }
   }
}
