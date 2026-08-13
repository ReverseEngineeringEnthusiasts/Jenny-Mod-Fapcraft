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
   protected EntityLiving c;
   protected BlockPos b = BlockPos.ORIGIN;
   protected BlockDoor d;
   boolean e;
   float f;
   float a;
   int g = 10;

   public DoorInteractAiGoal(EntityLiving var1) {
      this.c = var1;
      if (!(var1.getNavigator() instanceof PathNavigateGround)) {
         throw new IllegalArgumentException("Unsupported mob type for DoorInteractGoal");
      }
   }

   public boolean shouldExecute() {
      boolean var1 = true;

      for (int var2 = -3; var2 < 5; var2++) {
         for (int var3 = -3; var3 < 5; var3++) {
            IBlockState var4 = this.c.world.getBlockState(this.c.getPosition().add(var2, 0, var3));
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

      PathNavigateGround var6 = (PathNavigateGround)this.c.getNavigator();
      Path var7 = var6.getPath();
      if (var7 != null && !var7.isFinished() && var6.getEnterDoors()) {
         for (int var8 = 0; var8 < Math.min(var7.getCurrentPathIndex() + 2, var7.getCurrentPathLength()); var8++) {
            PathPoint var5 = var7.getPathPointFromIndex(var8);
            this.b = new BlockPos(var5.x, var5.y + 1, var5.z);
            if (this.c.getDistanceSq(this.b.getX(), this.c.posY, this.b.getZ()) <= 2.25) {
               this.d = this.a_clash800(this.b);
               if (this.d != null) {
                  return true;
               }
            }
         }

         this.b = new BlockPos(this.c).up();
         this.d = this.a_clash800(this.b);
         return this.d != null;
      } else {
         return false;
      }
   }

   public boolean shouldContinueExecuting() {
      return this.g >= 0;
   }

   public void startExecuting() {
      this.e = false;
      this.f = (float)(this.b.getX() + 0.5F - this.c.posX);
      this.a = (float)(this.b.getZ() + 0.5F - this.c.posZ);
      this.d.toggleDoor(this.c.world, this.b, true);
   }

   public void updateTask() {
      float var1 = (float)(this.b.getX() + 0.5F - this.c.posX);
      float var2 = (float)(this.b.getZ() + 0.5F - this.c.posZ);
      float var3 = this.f * var1 + this.a * var2;
      if (var3 < 0.0F && --this.g <= 0) {
         this.d.toggleDoor(this.c.world, this.b, false);
         this.e = true;
      }
   }

   public void resetTask() {
      this.g = 10;
   }

   private BlockDoor a_clash800(BlockPos var1) {
      IBlockState var2 = this.c.world.getBlockState(var1);
      Block var3 = var2.getBlock();
      return var3 instanceof BlockDoor && var2.getMaterial() == Material.WOOD ? (BlockDoor)var3 : null;
   }

}
