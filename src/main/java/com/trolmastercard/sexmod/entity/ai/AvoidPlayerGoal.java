package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;







import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class AvoidPlayerGoal extends EntityAIAvoidEntity<EntityPlayer> {
   final ManglelieEntity a;
   final float b;

   public AvoidPlayerGoal(ManglelieEntity var1, float var2, double var3, double var5) {
      super(var1, EntityPlayer.class, var2, var3, var5);
      this.a = var1;
      this.b = var2;
   }

   boolean a_clash220() {
      if (this.a.v_clash412() != null) {
         return true;
      }

      BlockPos var1 = this.a.getPosition();
      BlockPos var2 = new BlockPos(this.b, this.b, this.b);

      for (GalathEntity var5 : this.a.world.getEntitiesWithinAABB(GalathEntity.class, new AxisAlignedBB(var1.add(var2), var1.subtract(var2)))) {
         if (!var5.world.isRemote && !var5.isDead && var5.k_clash637()) {
            return true;
         }
      }

      return false;
   }

   public boolean shouldExecute() {
      return this.a_clash220() ? false : super.shouldExecute();
   }

   public boolean shouldContinueExecuting() {
      return this.a_clash220() ? false : super.shouldContinueExecuting();
   }

   public void startExecuting() {
      this.a.getDataManager().set(ManglelieEntity.ar, true);
      super.startExecuting();
   }

   public void resetTask() {
      this.a.getDataManager().set(ManglelieEntity.ar, false);
      super.resetTask();
   }

}
