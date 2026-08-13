package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;

public class GirlAiBase extends EntityAIBase {
   private final EntityVillager targetVillager;
   private EntityVillager homeVillager;
   private final World world;
   private int tickInterval;

   public GirlAiBase(EntityVillager var1) {
      this.targetVillager = var1;
      this.world = var1.world;
      this.setMutexBits(3);
   }

   public boolean shouldExecute() {
      if (this.tickInterval != 0) {
         return false;
      }

      Entity var1 = this.world.findNearestEntityWithinAABB(EntityVillager.class, this.targetVillager.getEntityBoundingBox().grow(8.0, 3.0, 8.0), this.targetVillager);
      if (var1 == null) {
         return false;
      }

      this.homeVillager = (EntityVillager)var1;
      return true;
   }

   public void startExecuting() {
      this.tickInterval = 300;
      this.targetVillager.setMating(true);
   }

   public void resetTask() {
   }

   public boolean shouldContinueExecuting() {
      return true;
   }

   public void updateTask() {
      this.tickInterval--;
      this.targetVillager.getLookHelper().setLookPositionWithEntity(this.homeVillager, 10.0F, 30.0F);
      if (this.targetVillager.getDistanceSq(this.homeVillager) > 2.25) {
         this.targetVillager.getNavigator().tryMoveToEntityLiving(this.homeVillager, 0.25);
      }

      if (this.tickInterval <= 0) {
         this.a_clash349();
         this.targetVillager.tasks.removeTask(this);
      }

      if (this.targetVillager.getRNG().nextInt(35) == 0) {
         this.world.setEntityState(this.targetVillager, (byte)12);
      }
   }

   private void a_clash349() {
      EntityVillager var1 = this.targetVillager.createChild(this.homeVillager);
      this.homeVillager.setGrowingAge(6000);
      this.targetVillager.setGrowingAge(6000);
      this.homeVillager.setIsWillingToMate(false);
      this.targetVillager.setIsWillingToMate(false);
      BabyEntitySpawnEvent var2 = new BabyEntitySpawnEvent(this.targetVillager, this.homeVillager, var1);
      if (!MinecraftForge.EVENT_BUS.post(var2) && var2.getChild() != null) {
         EntityAgeable var3 = var2.getChild();
         var3.setGrowingAge(-24000);
         var3.setLocationAndAngles(this.targetVillager.posX, this.targetVillager.posY, this.targetVillager.posZ, 0.0F, 0.0F);
         this.world.spawnEntity(var3);
         this.world.setEntityState(var3, (byte)12);
      }
   }

}
