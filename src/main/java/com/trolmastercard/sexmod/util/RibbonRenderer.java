package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;







import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class RibbonRenderer {
   public static void a(BufferBuilder var0, Tessellator var1, Minecraft var2, RibbonRenderer.b var3) {
      Vec3d[] var4 = new Vec3d[]{
         new Vec3d(-var3.thickness, -var3.advance, 0.0), new Vec3d(-var3.thickness, var3.advance, 0.0), new Vec3d(var3.thickness, var3.advance, 0.0), new Vec3d(var3.thickness, -var3.advance, 0.0)
      };
      Vec3d var5 = new Vec3d(0.0, 0.0, -var3.length);
      Vec3d var6 = VectorMath.scale(var5.normalize(), var3.width);
      Vec3d[] var7 = new Vec3d[4];
      System.arraycopy(var4, 0, var7, 0, 4);
      ArrayList var8 = new ArrayList();
      float var9 = var2.player.ticksExisted + var2.getRenderPartialTicks();

      for (int var10 = 0; var10 <= var3.segmentCount; var10++) {
         Vec3d[] var11 = new Vec3d[4];
         float var12 = 1.0F - (float)var10 / var3.segmentCount;

         for (int var13 = 0; var13 < 4; var13++) {
            Vec3d var14 = var4[var13];
            var11[var13] = new Vec3d(var14.x * var12, var14.y, var14.z).add(var6);
         }

         var8.add(var11);
         var5 = VectorMath.a(var5, var3.xWaveFn.a(var10, var9), var3.yWaveFn.a(var10, var9), var3.zWaveFn.a(var10, var9));
         var6 = var6.add(var5);
      }

      var0.begin(7, DefaultVertexFormats.POSITION_COLOR);
      a(var0, var7, (Vec3d[])var8.get(0), var3.color);

      for (int var15 = 0; var15 < var3.segmentCount - 1; var15++) {
         Vec3d[] var16 = (Vec3d[])var8.get(var15);
         Vec3d[] var17 = (Vec3d[])var8.get(var15 + 1);
         a(var0, var16, var17, var3.color);
      }

      var1.draw();
   }

   static float a(float var0, float var1, float var2, int var3, float var4) {
      return (float)(Math.sin(var0 * var1 + var2 * var3) * var4);
   }

   static void a(BufferBuilder var0, Vec3d[] var1, Vec3d[] var2, UnknownScreen var3) {
      var0.pos(var1[1].x, var1[1].y, var1[1].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[2].x, var1[2].y, var1[2].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[0].x, var1[0].y, var1[0].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[1].x, var1[1].y, var1[1].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[0].x, var2[0].y, var2[0].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[2].x, var1[2].y, var1[2].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[3].x, var1[3].y, var1[3].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[3].x, var2[3].y, var2[3].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[0].x, var1[0].y, var1[0].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var1[3].x, var1[3].y, var1[3].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[3].x, var2[3].y, var2[3].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
      var0.pos(var2[0].x, var2[0].y, var2[0].z).color(var3.red, var3.green, var3.blue, var3.alpha).endVertex();
   }

   @FunctionalInterface
   public interface a {
      float a(int var1, float var2);
   }

   public static class b {
      public UnknownScreen color;
      public float width;
      public int segmentCount;
      public float length;
      public RibbonRenderer.a xWaveFn;
      public RibbonRenderer.a yWaveFn;
      public RibbonRenderer.a zWaveFn;
      public float thickness;
      public float advance;

      public b(UnknownScreen var1, float var2, int var3, float var4, RibbonRenderer.a var5, RibbonRenderer.a var6, RibbonRenderer.a var7, float var8, float var9) {
         this.color = var1;
         this.width = var2;
         this.segmentCount = var3;
         this.length = var4;
         this.xWaveFn = var5;
         this.yWaveFn = var6;
         this.zWaveFn = var7;
         this.thickness = var8;
         this.advance = var9;
      }

      public RibbonRenderer.b copy() {
         return new RibbonRenderer.b(this.color, this.width, this.segmentCount, this.length, this.xWaveFn, this.yWaveFn, this.zWaveFn, this.thickness, this.advance);
      }
   }
}
