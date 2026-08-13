package com.trolmastercard.sexmod.util;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TrailSegment {
   public static final float GRAVITY = 9.81F;
   public static final float TIME_DELTA = 0.05F;
   public static final float AIR_RESISTANCE = 0.05F;
   public static final float COLLISION_OFFSET = 0.03F;
   World world;
   Vec3d offset;
   Vec3d velocity;
   Vec3d position;

   public TrailSegment(World var1, Vec3d var2, Vec3d var3) {
      this.world = var1;
      this.velocity = var2;
      this.offset = var2;
      this.position = var3;
   }

   public void onUpdate() {
      if (Vec3d.ZERO.equals(this.position)) {
         this.offset = this.velocity;
      } else {
         this.position = new Vec3d(this.position.x * 0.95F, (this.position.y - 0.49050003F) * 0.95F, this.position.z * 0.95F);
         this.offset = this.velocity;
         this.velocity = new Vec3d(
            this.velocity.x + this.position.x * 0.05F,
            this.velocity.y + this.position.y * 0.05F,
            this.velocity.z + this.position.z * 0.05F
         );
         BlockPos var1 = new BlockPos(this.offset);
         BlockPos var2 = null;

         for (BlockPos var4 : a(new BlockPos(this.offset), new BlockPos(this.velocity))) {
            if (this.world.getBlockState(var4).getBlock() != Blocks.AIR) {
               var2 = var4;
               break;
            }

            var1 = var4;
         }

         if (var2 != null) {
            int var23 = var2.getX();
            int var24 = var1.getX();
            if (var23 - var24 != 0) {
               double var25 = Math.max(var23, var24);
               double var27 = (this.offset.y - this.velocity.y) / (this.offset.x - this.velocity.x);
               double var29 = this.velocity.y - var27 * this.velocity.x;
               double var31 = var27 * var25 + var29;
               double var33 = (this.offset.z - this.velocity.z) / (this.offset.x - this.velocity.x);
               double var35 = this.velocity.z - var33 * this.velocity.x;
               double var37 = var33 * var25 + var35;
               this.velocity = new Vec3d(var25 + 0.03F * (var23 > var24 ? -1 : 1), var31, var37);
               this.position = new Vec3d(0.0, 0.0, 0.0);
            } else {
               int var5 = var2.getY();
               int var6 = var1.getY();
               if (var5 - var6 != 0) {
                  double var26 = Math.max(var5, var6);
                  double var28 = (this.offset.x - this.velocity.x) / (this.offset.y - this.velocity.y);
                  double var30 = this.velocity.x - var28 * this.velocity.y;
                  double var32 = var28 * var26 + var30;
                  double var34 = (this.offset.z - this.velocity.z) / (this.offset.y - this.velocity.y);
                  double var36 = this.velocity.z - var34 * this.velocity.y;
                  double var38 = var34 * var26 + var36;
                  this.velocity = new Vec3d(var32, var26 + 0.03F * (var5 > var6 ? -1 : 1), var38);
                  this.position = new Vec3d(0.0, 0.0, 0.0);
               } else {
                  int var7 = var2.getZ();
                  int var8 = var1.getZ();
                  if (var7 - var8 != 0) {
                     double var9 = Math.max(var7, var8);
                     double var11 = (this.offset.y - this.velocity.y) / (this.offset.z - this.velocity.z);
                     double var13 = this.velocity.y - var11 * this.velocity.z;
                     double var15 = var11 * var9 + var13;
                     double var17 = (this.offset.x - this.velocity.x) / (this.offset.z - this.velocity.z);
                     double var19 = this.velocity.x - var17 * this.velocity.z;
                     double var21 = var17 * var9 + var19;
                     this.velocity = new Vec3d(var21, var15, var9 + 0.03F * (var7 > var8 ? -1 : 1));
                     this.position = new Vec3d(0.0, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   static List<BlockPos> a(BlockPos var0, BlockPos var1) {
      ArrayList var2 = new ArrayList();
      var2.add(var0);
      int var3 = var0.getX();
      int var4 = var0.getY();
      int var5 = var0.getZ();
      int var6 = var1.getX();
      int var7 = var1.getY();
      int var8 = var1.getZ();
      int var9 = Math.abs(var6 - var3);
      int var10 = Math.abs(var7 - var4);
      int var11 = Math.abs(var8 - var5);
      int var12 = var3 < var6 ? 1 : -1;
      int var13 = var4 < var7 ? 1 : -1;
      int var14 = var5 < var8 ? 1 : -1;
      int var15 = Math.max(var9, Math.max(var10, var11));
      int var16 = var3;
      int var17 = var4;
      int var18 = var5;
      int var19 = var15 / 2;
      int var20 = var15 / 2;
      int var21 = var15 / 2;

      for (int var22 = 0; var22 < var15; var22++) {
         var2.add(new BlockPos(var16, var17, var18));
         var19 -= var9;
         var20 -= var10;
         var21 -= var11;
         if (var19 < 0) {
            var16 += var12;
            var19 += var15;
         } else if (var20 < 0) {
            var17 += var13;
            var20 += var15;
         } else if (var21 < 0) {
            var18 += var14;
            var21 += var15;
         }
      }

      var2.add(var1);
      return var2;
   }

}
