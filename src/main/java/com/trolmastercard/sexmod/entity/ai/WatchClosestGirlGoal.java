package com.trolmastercard.sexmod.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIWatchClosest2;

/**
 * <b>Role.</b> Vanilla "watch closest player" goal with a software kill
 * switch: {@link #isWatching} is set false by the follow goals while the girl
 * is actively following/attacking, so she stops staring at the player.
 */
public class WatchClosestGirlGoal extends EntityAIWatchClosest2 {
   public boolean isWatching = true;

   public WatchClosestGirlGoal(EntityLiving entity, Class<? extends Entity> watchedClass, float radius, float maxDist) {
      super(entity, watchedClass, radius, maxDist);
   }

   public void updateTask() {
      if (this.isWatching) {
         super.updateTask();
      }
   }

}
