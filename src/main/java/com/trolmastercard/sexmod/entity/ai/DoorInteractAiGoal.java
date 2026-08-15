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

/**
 * <b>Role.</b> Door interaction for girl NPCs: scans for a wooden door within
 * 3 blocks ahead on the current path, opens it when close enough, walks
 * through and closes it behind. Requires a {@link PathNavigateGround}
 * navigator (doors are ground-only).
 * <p>
 * <b>Pitfalls.</b> The constructor throws for non-ground navigators — only
 * add this goal to ground-walking girls. The close happens when the entity
 * passes the door plane ({@code approachX * dx + approachZ * dz < 0}).
 */
public class DoorInteractAiGoal extends EntityAIBase {
   protected EntityLiving entity;
   protected BlockPos doorPosition = BlockPos.ORIGIN;
   protected BlockDoor doorBlock;
   boolean hasOpenedDoor;
   float approachX;
   float approachZ;
   int ticksRemaining = 10;

   public DoorInteractAiGoal(EntityLiving entity) {
      this.entity = entity;
      if (!(entity.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean shouldExecute() {
      boolean found = true;

      for (int x = -3; x < 5; x++) {
         for (int z = -3; z < 5; z++) {
            IBlockState state = this.entity.world.getBlockState(this.entity.getPosition().add(x, 0, z));
            if (state.getBlock() instanceof BlockDoor && state.getMaterial() == Material.WOOD) {
               found = false;
               break;
            }
         }

         if (!found) {
            break;
         }
      }

      if (found) {
         return false;
      }

      PathNavigateGround navigator = (PathNavigateGround)this.entity.getNavigator();
      Path path = navigator.getPath();
      if (path != null && !path.isFinished() && navigator.getEnterDoors()) {
         for (int i = 0; i < Math.min(path.getCurrentPathIndex() + 2, path.getCurrentPathLength()); i++) {
            PathPoint point = path.getPathPointFromIndex(i);
            this.doorPosition = new BlockPos(point.x, point.y + 1, point.z);
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
      float dx = (float)(this.doorPosition.getX() + 0.5F - this.entity.posX);
      float dz = (float)(this.doorPosition.getZ() + 0.5F - this.entity.posZ);
      float dot = this.approachX * dx + this.approachZ * dz;
      if (dot < 0.0F && --this.ticksRemaining <= 0) {
         this.doorBlock.toggleDoor(this.entity.world, this.doorPosition, false);
         this.hasOpenedDoor = true;
      }
   }

   public void resetTask() {
      this.ticksRemaining = 10;
   }

   private BlockDoor findDoorBlock(BlockPos pos) {
      IBlockState state = this.entity.world.getBlockState(pos);
      Block block = state.getBlock();
      return block instanceof BlockDoor && state.getMaterial() == Material.WOOD ? (BlockDoor)block : null;
   }

}
