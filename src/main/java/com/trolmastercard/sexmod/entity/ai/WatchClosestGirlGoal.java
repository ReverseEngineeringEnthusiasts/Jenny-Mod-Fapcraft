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

   public WatchClosestGirlGoal(EntityLiving var1, Class<? extends Entity> var2, float var3, float var4) {
      super(var1, var2, var3, var4);
   }

   public void updateTask() {
      if (this.isWatching) {
         super.updateTask();
      }
   }

}
