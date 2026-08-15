package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * <b>Role.</b> The Manglelie player-avoidance goal — flees from nearby
 * players, but only while no Galath "mommy" with a master is within
 * {@code distance} blocks (a mommied Mang never runs away).
 * <p>
 * <b>Pitfalls.</b> Also sets the data-manager "scared" flag
 * ({@link ManglelieEntity#ar}, key 115) while executing so the movement
 * animation switches to the scared run.
 */
public class AvoidPlayerGoal extends EntityAIAvoidEntity<EntityPlayer> {
   final ManglelieEntity manglelieEntity;
   final float distance;

   public AvoidPlayerGoal(ManglelieEntity var1, float var2, double var3, double var5) {
      super(var1, EntityPlayer.class, var2, var3, var5);
      this.manglelieEntity = var1;
      this.distance = var2;
   }

   /**
    * True while the Mang is bound to a corrupting Galath (never flees then)
    * or no unbound master-Galath is near enough to protect her.
    */
   boolean shouldAvoid() {
      if (this.manglelieEntity.getCorruptPlayerUUID() != null) {
         return true;
      }

      BlockPos var1 = this.manglelieEntity.getPosition();
      BlockPos var2 = new BlockPos(this.distance, this.distance, this.distance);

      for (GalathEntity var5 : this.manglelieEntity.world.getEntitiesWithinAABB(GalathEntity.class, new AxisAlignedBB(var1.add(var2), var1.subtract(var2)))) {
         if (!var5.world.isRemote && !var5.isDead && var5.hasMaster()) {
            return true;
         }
      }

      return false;
   }

   public boolean shouldExecute() {
      return this.shouldAvoid() ? false : super.shouldExecute();
   }

   public boolean shouldContinueExecuting() {
      return this.shouldAvoid() ? false : super.shouldContinueExecuting();
   }

   public void startExecuting() {
      this.manglelieEntity.getDataManager().set(ManglelieEntity.ar, true);
      super.startExecuting();
   }

   public void resetTask() {
      this.manglelieEntity.getDataManager().set(ManglelieEntity.ar, false);
      super.resetTask();
   }

}
