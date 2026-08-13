package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;







import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class ef {
   public static void a(BufferBuilder var0, Tessellator var1, Minecraft var2, ef.b var3) {
      Vec3d[] var4 = new Vec3d[]{
         new Vec3d(-var3.f, -var3.a, 0.0), new Vec3d(-var3.f, var3.a, 0.0), new Vec3d(var3.f, var3.a, 0.0), new Vec3d(var3.f, -var3.a, 0.0)
      };
      Vec3d var5 = new Vec3d(0.0, 0.0, -var3.g);
      Vec3d var6 = ck.a_clash304(var5.normalize(), var3.e);
      Vec3d[] var7 = new Vec3d[4];
      System.arraycopy(var4, 0, var7, 0, 4);
      ArrayList var8 = new ArrayList();
      float var9 = var2.player.ticksExisted + var2.getRenderPartialTicks();

      for (int var10 = 0; var10 <= var3.c; var10++) {
         Vec3d[] var11 = new Vec3d[4];
         float var12 = 1.0F - (float)var10 / var3.c;

         for (int var13 = 0; var13 < 4; var13++) {
            Vec3d var14 = var4[var13];
            var11[var13] = new Vec3d(var14.x * var12, var14.y, var14.z).add(var6);
         }

         var8.add(var11);
         var5 = ck.a(var5, var3.i.a(var10, var9), var3.b.a(var10, var9), var3.d.a(var10, var9));
         var6 = var6.add(var5);
      }

      var0.begin(7, DefaultVertexFormats.POSITION_COLOR);
      a(var0, var7, (Vec3d[])var8.get(0), var3.h);

      for (int var15 = 0; var15 < var3.c - 1; var15++) {
         Vec3d[] var16 = (Vec3d[])var8.get(var15);
         Vec3d[] var17 = (Vec3d[])var8.get(var15 + 1);
         a(var0, var16, var17, var3.h);
      }

      var1.draw();
   }

   static float a(float var0, float var1, float var2, int var3, float var4) {
      return (float)(Math.sin(var0 * var1 + var2 * var3) * var4);
   }

   static void a(BufferBuilder var0, Vec3d[] var1, Vec3d[] var2, UnknownScreen var3) {
      var0.pos(var1[1].x, var1[1].y, var1[1].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[2].x, var1[2].y, var1[2].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[0].x, var1[0].y, var1[0].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[1].x, var1[1].y, var1[1].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[0].x, var2[0].y, var2[0].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[2].x, var1[2].y, var1[2].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[3].x, var1[3].y, var1[3].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[3].x, var2[3].y, var2[3].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[0].x, var1[0].y, var1[0].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var1[3].x, var1[3].y, var1[3].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[3].x, var2[3].y, var2[3].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
      var0.pos(var2[0].x, var2[0].y, var2[0].z).color(var3.a, var3.d, var3.c, var3.b).endVertex();
   }

   @FunctionalInterface
   public interface a {
      float a(int var1, float var2);
   }

   public static class b {
      public UnknownScreen h;
      public float e;
      public int c;
      public float g;
      public ef.a i;
      public ef.a b;
      public ef.a d;
      public float f;
      public float a;

      public b(UnknownScreen var1, float var2, int var3, float var4, ef.a var5, ef.a var6, ef.a var7, float var8, float var9) {
         this.h = var1;
         this.e = var2;
         this.c = var3;
         this.g = var4;
         this.i = var5;
         this.b = var6;
         this.d = var7;
         this.f = var8;
         this.a = var9;
      }

      public ef.b a_clash906() {
         return new ef.b(this.h, this.e, this.c, this.g, this.i, this.b, this.d, this.f, this.a);
      }
   }
}
