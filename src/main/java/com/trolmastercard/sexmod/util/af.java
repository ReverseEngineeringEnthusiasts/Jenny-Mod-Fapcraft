package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.processor.IBone;

public class af {
   public static Vec3d[][] a(
      BaseGirlEntity var0, float var1, String var2, String var3, String var4, float var5, float var6, float var7, float var8, String var9
   ) {
      Vec3d[] var10 = b(var0, var1, var2, var3, var4, var5, var6, var7, var8, var9);
      return a(var10);
   }

   public static Vec3d[][] a(BaseGirlEntity var0, float var1, String var2, String var3, f7 var4, f7 var5) {
      Vec3d[] var6 = b(var0, var1, var2, var3, var4, var5);
      return b(var6);
   }

   static Vec3d[] b(BaseGirlEntity var0, float var1, String var2, String var3, f7 var4, f7 var5) {
      Vec3d var6 = var0.getCachedBoneOffset(var2);
      Vec3d var7 = var0.getCachedBoneOffset(var3);
      Vec3d[] var8 = new Vec3d[8];
      if (var4.a == 0.0F && var5.a == 0.0F) {
         var8[0] = new Vec3d(0.0, var4.c, var4.b);
         var8[1] = new Vec3d(0.0, -var4.c, var4.b);
         var8[2] = new Vec3d(0.0, -var4.c, -var4.b);
         var8[3] = new Vec3d(0.0, var4.c, -var4.b);
         var8[4] = new Vec3d(0.0, var5.c, var5.b);
         var8[5] = new Vec3d(0.0, -var5.c, var5.b);
         var8[6] = new Vec3d(0.0, -var5.c, -var5.b);
         var8[7] = new Vec3d(0.0, var5.c, -var5.b);
      } else {
         var8[0] = new Vec3d(var4.a, var4.c, 0.0);
         var8[1] = new Vec3d(-var4.a, var4.c, 0.0);
         var8[2] = new Vec3d(-var4.a, -var4.c, 0.0);
         var8[3] = new Vec3d(var4.a, -var4.c, 0.0);
         var8[4] = new Vec3d(var5.a, var5.c, 0.0);
         var8[5] = new Vec3d(-var5.a, var5.c, 0.0);
         var8[6] = new Vec3d(-var5.a, -var5.c, 0.0);
         var8[7] = new Vec3d(var5.a, -var5.c, 0.0);
      }

      for (int var9 = 0; var9 < var8.length; var9++) {
         var8[var9] = ck.rotateByYaw(var8[var9], var1);
      }

      for (int var10 = 0; var10 < 4; var10++) {
         var8[var10] = var8[var10].add(var6);
      }

      for (int var11 = 4; var11 < 8; var11++) {
         var8[var11] = var8[var11].add(var7);
      }

      return var8;
   }

   static Vec3d[][] b(Vec3d[] var0) {
      Vec3d[][] var1 = new Vec3d[6][4];
      var1[0][0] = var0[0];
      var1[0][1] = var0[1];
      var1[0][2] = var0[2];
      var1[0][3] = var0[3];
      var1[1][0] = var0[4];
      var1[1][1] = var0[5];
      var1[1][2] = var0[6];
      var1[1][3] = var0[7];
      var1[2][0] = var0[1];
      var1[2][1] = var0[2];
      var1[2][2] = var0[6];
      var1[2][3] = var0[5];
      var1[3][0] = var0[3];
      var1[3][1] = var0[7];
      var1[3][2] = var0[4];
      var1[3][3] = var0[0];
      var1[4][0] = var0[1];
      var1[4][1] = var0[0];
      var1[4][2] = var0[4];
      var1[4][3] = var0[5];
      var1[5][0] = var0[2];
      var1[5][1] = var0[3];
      var1[5][2] = var0[7];
      var1[5][3] = var0[6];
      return var1;
   }

   static Vec3d[] b(BaseGirlEntity var0, float var1, String var2, String var3, String var4, float var5, float var6, float var7, float var8, String var9) {
      IBone var10 = var0.getAnimationProcessor().getBone(var9);
      if (var10 == null) {
         Vec3d[] var18 = new Vec3d[12];
         Arrays.fill(var18, Vec3d.ZERO);
         return var18;
      }

      float var11 = gc.d_clash746(var10.getRotationY());
      float var12 = gc.d_clash746(var10.getRotationZ());
      Vec3d var13 = var0.getCachedBoneOffset(var2);
      Vec3d var14 = var0.getCachedBoneOffset(var3);
      Vec3d var15 = var0.getCachedBoneOffset(var4);
      Vec3d[] var16 = new Vec3d[]{
         new Vec3d(var5, 0.0, -var6),
         new Vec3d(-var5, 0.0, -var6),
         new Vec3d(-var5, 0.0, var6),
         new Vec3d(var5, 0.0, var6),
         new Vec3d(var5, var6, 0.0),
         new Vec3d(-var5, var6, 0.0),
         new Vec3d(-var5, -var6, 0.0),
         new Vec3d(var5, -var6, 0.0),
         new Vec3d(var7, 0.0, -var8),
         new Vec3d(-var7, 0.0, -var8),
         new Vec3d(-var7, 0.0, var8),
         new Vec3d(var7, 0.0, var8)
      };

      for (int var17 = 0; var17 < var16.length; var17++) {
         var16[var17] = ck.rotateByYaw(var16[var17], var1);
      }

      for (int var19 = 0; var19 < 4; var19++) {
         var16[var19] = ck.a(var16[var19], 0.0F, var11, var12);
      }

      for (int var20 = 0; var20 < 4; var20++) {
         var16[var20] = var16[var20].add(var13);
      }

      for (int var21 = 4; var21 < 8; var21++) {
         var16[var21] = var16[var21].add(var14);
      }

      for (int var22 = 8; var22 < 12; var22++) {
         var16[var22] = var16[var22].add(var15);
      }

      return var16;
   }

   static Vec3d[][] a(Vec3d[] var0) {
      Vec3d[][] var1 = new Vec3d[10][4];
      var1[0][0] = var0[0];
      var1[0][1] = var0[1];
      var1[0][2] = var0[5];
      var1[0][3] = var0[4];
      var1[1][0] = var0[1];
      var1[1][1] = var0[2];
      var1[1][2] = var0[6];
      var1[1][3] = var0[5];
      var1[2][0] = var0[3];
      var1[2][1] = var0[2];
      var1[2][2] = var0[6];
      var1[2][3] = var0[7];
      var1[3][0] = var0[0];
      var1[3][1] = var0[4];
      var1[3][2] = var0[7];
      var1[3][3] = var0[3];
      var1[4][0] = var0[0];
      var1[4][1] = var0[1];
      var1[4][2] = var0[2];
      var1[4][3] = var0[3];
      var1[5][0] = var0[4];
      var1[5][1] = var0[5];
      var1[5][2] = var0[9];
      var1[5][3] = var0[8];
      var1[6][0] = var0[9];
      var1[6][1] = var0[10];
      var1[6][2] = var0[6];
      var1[6][3] = var0[5];
      var1[7][0] = var0[10];
      var1[7][1] = var0[11];
      var1[7][2] = var0[7];
      var1[7][3] = var0[6];
      var1[8][0] = var0[4];
      var1[8][1] = var0[7];
      var1[8][2] = var0[11];
      var1[8][3] = var0[8];
      var1[9][0] = var0[8];
      var1[9][1] = var0[9];
      var1[9][2] = var0[10];
      var1[9][3] = var0[11];
      return var1;
   }

   public static void a(BufferBuilder var0, Vec3d[][] var1, UnknownScreen var2) {
      for (Vec3d[] var6 : var1) {
         for (Vec3d var10 : var6) {
            var0.pos(var10.x, var10.y, var10.z)
               .tex(0.0, 0.0)
               .color(var2.a, var2.d, var2.c, var2.b)
               .endVertex();
         }
      }
   }

   public static void a(Minecraft var0, BaseGirlEntity var1, float var2) {
      EntityPlayerSP var3 = var0.player;
      if (var3 != null) {
         GlStateManager.translate(0.0, 0.01, 0.0);
         Entity var4 = ((GirlRenderer)var0.getRenderManager().getEntityRenderObject(var1)).c_clash336(var1);
         Vec3d var5 = var1.isAnchored()
            ? var1.getTargetPosition()
            : RotationHelper.a(new Vec3d(var4.lastTickPosX, var4.lastTickPosY, var4.lastTickPosZ), var4.getPositionVector(), var2);
         Vec3d var6 = RotationHelper.a(new Vec3d(var3.lastTickPosX, var3.lastTickPosY, var3.lastTickPosZ), var3.getPositionVector(), var2);
         Vec3d var7 = var5.subtract(var6);
         var7 = var1.a_clash432(var7, var2);
         GlStateManager.translate(var7.x, var7.y, var7.z);
      }
   }

}
