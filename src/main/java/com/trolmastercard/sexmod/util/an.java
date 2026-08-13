package com.trolmastercard.sexmod.util;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class an {
   public static final float a = 9.81F;
   public static final float g = 0.05F;
   public static final float b = 0.05F;
   public static final float c = 0.03F;
   World h;
   Vec3d d;
   Vec3d f;
   Vec3d e;

   public an(World var1, Vec3d var2, Vec3d var3) {
      this.h = var1;
      this.f = var2;
      this.d = var2;
      this.e = var3;
   }

   public void a_clash41() {
      if (Vec3d.field_186680_a.equals(this.e)) {
         this.d = this.f;
      } else {
         this.e = new Vec3d(this.e.field_72450_a * 0.95F, (this.e.field_72448_b - 0.49050003F) * 0.95F, this.e.field_72449_c * 0.95F);
         this.d = this.f;
         this.f = new Vec3d(
            this.f.field_72450_a + this.e.field_72450_a * 0.05F,
            this.f.field_72448_b + this.e.field_72448_b * 0.05F,
            this.f.field_72449_c + this.e.field_72449_c * 0.05F
         );
         BlockPos var1 = new BlockPos(this.d);
         BlockPos var2 = null;

         for (BlockPos var4 : a(new BlockPos(this.d), new BlockPos(this.f))) {
            if (this.h.func_180495_p(var4).func_177230_c() != Blocks.field_150350_a) {
               var2 = var4;
               break;
            }

            var1 = var4;
         }

         if (var2 != null) {
            int var23 = var2.func_177958_n();
            int var24 = var1.func_177958_n();
            if (var23 - var24 != 0) {
               double var25 = Math.max(var23, var24);
               double var27 = (this.d.field_72448_b - this.f.field_72448_b) / (this.d.field_72450_a - this.f.field_72450_a);
               double var29 = this.f.field_72448_b - var27 * this.f.field_72450_a;
               double var31 = var27 * var25 + var29;
               double var33 = (this.d.field_72449_c - this.f.field_72449_c) / (this.d.field_72450_a - this.f.field_72450_a);
               double var35 = this.f.field_72449_c - var33 * this.f.field_72450_a;
               double var37 = var33 * var25 + var35;
               this.f = new Vec3d(var25 + 0.03F * (var23 > var24 ? -1 : 1), var31, var37);
               this.e = new Vec3d(0.0, 0.0, 0.0);
            } else {
               int var5 = var2.func_177956_o();
               int var6 = var1.func_177956_o();
               if (var5 - var6 != 0) {
                  double var26 = Math.max(var5, var6);
                  double var28 = (this.d.field_72450_a - this.f.field_72450_a) / (this.d.field_72448_b - this.f.field_72448_b);
                  double var30 = this.f.field_72450_a - var28 * this.f.field_72448_b;
                  double var32 = var28 * var26 + var30;
                  double var34 = (this.d.field_72449_c - this.f.field_72449_c) / (this.d.field_72448_b - this.f.field_72448_b);
                  double var36 = this.f.field_72449_c - var34 * this.f.field_72448_b;
                  double var38 = var34 * var26 + var36;
                  this.f = new Vec3d(var32, var26 + 0.03F * (var5 > var6 ? -1 : 1), var38);
                  this.e = new Vec3d(0.0, 0.0, 0.0);
               } else {
                  int var7 = var2.func_177952_p();
                  int var8 = var1.func_177952_p();
                  if (var7 - var8 != 0) {
                     double var9 = Math.max(var7, var8);
                     double var11 = (this.d.field_72448_b - this.f.field_72448_b) / (this.d.field_72449_c - this.f.field_72449_c);
                     double var13 = this.f.field_72448_b - var11 * this.f.field_72449_c;
                     double var15 = var11 * var9 + var13;
                     double var17 = (this.d.field_72450_a - this.f.field_72450_a) / (this.d.field_72449_c - this.f.field_72449_c);
                     double var19 = this.f.field_72450_a - var17 * this.f.field_72449_c;
                     double var21 = var17 * var9 + var19;
                     this.f = new Vec3d(var21, var15, var9 + 0.03F * (var7 > var8 ? -1 : 1));
                     this.e = new Vec3d(0.0, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   static List<BlockPos> a(BlockPos var0, BlockPos var1) {
      ArrayList var2 = new ArrayList();
      var2.add(var0);
      int var3 = var0.func_177958_n();
      int var4 = var0.func_177956_o();
      int var5 = var0.func_177952_p();
      int var6 = var1.func_177958_n();
      int var7 = var1.func_177956_o();
      int var8 = var1.func_177952_p();
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
