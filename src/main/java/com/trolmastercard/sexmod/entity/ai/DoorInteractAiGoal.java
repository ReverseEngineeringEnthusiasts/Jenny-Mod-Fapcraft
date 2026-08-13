package com.trolmastercard.sexmod.entity.ai;


import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public class DoorInteractAiGoal extends EntityAIBase {
   protected EntityLiving entity;
   protected BlockPos doorPosition = BlockPos.ORIGIN;
   protected BlockDoor doorBlock;
   boolean hasOpenedDoor;
   float approachX;
   float approachZ;
   int ticksRemaining = 10;

   public DoorInteractAiGoal(EntityLiving var1) {
      this.entity = var1;
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean shouldExecute() {
      boolean var1 = true;

      for (int var2 = -3; var2 < 5; var2++) {
         for (int var3 = -3; var3 < 5; var3++) {
            IBlockState var4 = this.entity.world.getBlockState(this.entity.getPosition().add(var2, 0, var3));
            if (var4.getBlock() instanceof BlockDoor && var4.getMaterial() == Material.WOOD) {
               var1 = false;
               break;
            }
         }

         if (!var1) {
            break;
         }
      }

      if (var1) {
         return false;
      }

      PathNavigateGround var6 = (PathNavigateGround)this.entity.getNavigator();
      Path var7 = var6.getPath();
      if (var7 != null && !var7.isFinished() && var6.getEnterDoors()) {
         for (int var8 = 0; var8 < Math.min(var7.getCurrentPathIndex() + 2, var7.getCurrentPathLength()); var8++) {
            PathPoint var5 = var7.getPathPointFromIndex(var8);
            this.doorPosition = new BlockPos(var5.x, var5.y + 1, var5.z);
            if (this.entity.getDistanceSq(this.doorPosition.getX(), this.entity.posY, this.doorPosition.getZ()) <= 2.25) {
               this.doorBlock = this.findDoorBlock(this.doorPosition);
               if (this.doorBlock != null) {
                  return true;
               }
            }
         }

         this.doorPosition = new BlockPos(this.entity).up();
         this.doorBlock = this.findDoorBlock(this.doorPosition);
         return this.doorBlock != null;
      } else {
         return false;
      }
   }

   public boolean shouldContinueExecuting() {
      return this.ticksRemaining >= 0;
   }

   public void startExecuting() {
      this.hasOpenedDoor = false;
      this.approachX = (float)(this.doorPosition.getX() + 0.5F - this.entity.posX);
      this.approachZ = (float)(this.doorPosition.getZ() + 0.5F - this.entity.posZ);
      this.doorBlock.toggleDoor(this.entity.world, this.doorPosition, true);
   }

   public void updateTask() {
      float var1 = (float)(this.doorPosition.getX() + 0.5F - this.entity.posX);
      float var2 = (float)(this.doorPosition.getZ() + 0.5F - this.entity.posZ);
      float var3 = this.approachX * var1 + this.approachZ * var2;
      if (var3 < 0.0F && --this.ticksRemaining <= 0) {
         this.doorBlock.toggleDoor(this.entity.world, this.doorPosition, false);
         this.hasOpenedDoor = true;
      }
   }

   public void resetTask() {
      this.ticksRemaining = 10;
   }

   private BlockDoor findDoorBlock(BlockPos var1) {
      IBlockState var2 = this.entity.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      return var3 instanceof BlockDoor && var2.getMaterial() == Material.WOOD ? (BlockDoor)var3 : null;
   }

}
