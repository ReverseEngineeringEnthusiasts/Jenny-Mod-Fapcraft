package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class GirlAiBase extends EntityAIBase {
   private final EntityVillager c;
   private EntityVillager d;
   private final World a;
   private int b;

   public GirlAiBase(EntityVillager var1) {
      this.c = var1;
      this.a = var1.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.b != 0) {
         return false;
      }

      Entity var1 = this.a.findNearestEntityWithinAABB(EntityVillager.class, this.c.getEntityBoundingBox().grow(8.0, 3.0, 8.0), this.c);
      if (var1 == null) {
         return false;
      }

      this.d = (EntityVillager)var1;
      return true;
   }

   public void startExecuting() {
      this.b = 300;
      this.c.setMating(true);
   }

   public void resetTask() {
   }

   public boolean shouldContinueExecuting() {
      return true;
   }

   public void updateTask() {
      this.b--;
      this.c.getLookHelper().setLookPositionWithEntity(this.d, 10.0F, 30.0F);
      if (this.c.getDistanceSq(this.d) > 2.25) {
         this.c.getNavigator().tryMoveToEntityLiving(this.d, 0.25);
      }

      if (this.b <= 0) {
         this.a_clash349();
         this.c.tasks.removeTask(this);
      }

      if (this.c.getRNG().nextInt(35) == 0) {
         this.a.setEntityState(this.c, (byte)12);
      }
   }

   private void a_clash349() {
      EntityVillager var1 = this.c.createChild(this.d);
      this.d.setGrowingAge(6000);
      this.c.setGrowingAge(6000);
      this.d.setIsWillingToMate(false);
      this.c.setIsWillingToMate(false);
      BabyEntitySpawnEvent var2 = new BabyEntitySpawnEvent(this.c, this.d, var1);
      if (!MinecraftForge.EVENT_BUS.post(var2) && var2.getChild() != null) {
         EntityAgeable var3 = var2.getChild();
         var3.setGrowingAge(-24000);
         var3.setLocationAndAngles(this.c.posX, this.c.posY, this.c.posZ, 0.0F, 0.0F);
         this.a.spawnEntity(var3);
         this.a.setEntityState(var3, (byte)12);
      }
   }

}
