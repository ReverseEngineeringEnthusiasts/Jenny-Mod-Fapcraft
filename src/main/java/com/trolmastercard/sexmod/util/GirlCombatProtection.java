package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> Combat protection for girls: girls in an active scene (interaction
 * partner set) and player-girls cannot be hurt by normal sources; a player
 * standing within 1 block of the girl he owns is likewise invulnerable (except
 * void/succubus damage). Prevents scene-breaking damage and griefing during
 * animations.
 * <p>
 * <b>Invariants.</b> {@code DamageSource.OUT_OF_WORLD} is never cancelled (girls
 * must still die to the void). Player-girls are unconditionally protected;
 * NPC girls only while an interaction partner is bound.
 */
public class GirlCombatProtection {
   @SubscribeEvent
   public void onLivingAttack(LivingAttackEvent event) {
      if (event.getSource() != DamageSource.OUT_OF_WORLD) {
         if (event.getEntity() instanceof BaseGirlEntity) {
            BaseGirlEntity girl = (BaseGirlEntity)event.getEntity();
            if (girl instanceof AbstractPlayerGirlEntity) {
               event.setCanceled(true);
            } else {
               event.setCanceled(girl.getInteractionPlayerUUID() != null);
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerDamage(LivingAttackEvent event) {
      DamageSource source = event.getSource();
      if (source != DamageSource.OUT_OF_WORLD && !(source instanceof SuccubusDamageSource)) {
         if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.getEntity();
            BaseGirlEntity girl = BaseGirlEntity.getGirlByUUID(player.getPersistentID());
            if (girl != null) {
               if (girl.getDistance(player) < 1.0F) {
                  event.setCanceled(true);
               }
            }
         }
      }
   }

}
