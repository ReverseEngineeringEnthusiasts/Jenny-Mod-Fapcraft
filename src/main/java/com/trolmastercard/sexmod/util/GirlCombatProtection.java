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
   public void onLivingAttack(LivingAttackEvent var1) {
      if (var1.getSource() != DamageSource.OUT_OF_WORLD) {
         if (var1.getEntity() instanceof BaseGirlEntity) {
            BaseGirlEntity var2 = (BaseGirlEntity)var1.getEntity();
            if (var2 instanceof AbstractPlayerGirlEntity) {
               var1.setCanceled(true);
            } else {
               var1.setCanceled(var2.getInteractionPlayerUUID() != null);
            }
         }
      }
   }

   @SubscribeEvent
   public void onPlayerDamage(LivingAttackEvent var1) {
      DamageSource var2 = var1.getSource();
      if (var2 != DamageSource.OUT_OF_WORLD && !(var2 instanceof SuccubusDamageSource)) {
         if (var1.getEntity() instanceof EntityPlayer) {
            EntityPlayer var3 = (EntityPlayer)var1.getEntity();
            BaseGirlEntity var4 = BaseGirlEntity.getGirlByUUID(var3.getPersistentID());
            if (var4 != null) {
               if (var4.getDistance(var3) < 1.0F) {
                  var1.setCanceled(true);
               }
            }
         }
      }
   }

}
