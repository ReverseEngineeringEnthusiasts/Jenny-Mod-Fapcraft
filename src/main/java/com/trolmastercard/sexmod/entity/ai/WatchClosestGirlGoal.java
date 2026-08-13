package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIWatchClosest2;

public class WatchClosestGirlGoal extends EntityAIWatchClosest2 {
   public boolean a = true;

   public WatchClosestGirlGoal(EntityLiving var1, Class<? extends Entity> var2, float var3, float var4) {
      super(var1, var2, var3, var4);
   }

   public void func_75246_d() {
      if (this.a) {
         super.func_75246_d();
      }
   }

}
