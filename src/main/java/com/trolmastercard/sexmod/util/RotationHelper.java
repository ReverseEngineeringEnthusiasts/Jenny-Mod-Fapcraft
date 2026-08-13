package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;







import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class RotationHelper {
   public static Vec3d a(Vec3d var0, Vec3d var1, int var2) {
      if (var2 == 0) {
         return var1;
      }

      Vec3d var3 = var1.func_178788_d(var0);
      return var0.func_72441_c(var3.field_72450_a / var2, var3.field_72448_b / var2, var3.field_72449_c / var2);
   }

   public static double b(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   public static float a(float var0, float var1, double var2) {
      float var4 = var1 - var0;

      while (var4 < -Math.PI) {
         var4 = (float)(var4 + (Math.PI * 2));
      }

      while (var4 >= Math.PI) {
         var4 = (float)(var4 - (Math.PI * 2));
      }

      return (float)(var0 + var4 * var2);
   }

   public static float b(float var0, float var1, double var2) {
      double var4 = Math.toRadians(var0);
      double var6 = Math.toRadians(var1);
      return (float)Math.toDegrees(a((float)var4, (float)var6, var2));
   }

   public static Vec3d a(Vec3d var0, Vec3d var1, double var2) {
      Vec3d var4 = var1.func_178788_d(var0);
      return var0.func_178787_e(new Vec3d(var4.field_72450_a * var2, var4.field_72448_b * var2, var4.field_72449_c * var2));
   }

   public static f7 a(f7 var0, f7 var1, double var2) {
      f7 var4 = var1.b(var0);
      return var0.a(var4.a_clash407((float)var2));
   }

   public static Vec3i a(Vec3i var0, Vec3i var1, double var2) {
      Vec3d var4 = new Vec3d(
         var1.func_177958_n() - var0.func_177958_n(), var1.func_177956_o() - var0.func_177956_o(), var1.func_177952_p() - var0.func_177952_p()
      );
      return new Vec3i(
         var0.func_177958_n() + var4.field_72450_a * var2, var0.func_177956_o() + var4.field_72448_b * var2, var0.func_177952_p() + var4.field_72449_c * var2
      );
   }

   public static UnknownScreen a(UnknownScreen var0, UnknownScreen var1, double var2) {
      UnknownScreen var4 = new UnknownScreen(var1.a - var0.a, var1.d - var0.d, var1.c - var0.c, var1.b - var0.b);
      return new UnknownScreen((int)(var0.a + var4.a * var2), (int)(var0.d + var4.d * var2), (int)(var0.c + var4.c * var2), (int)(var0.b + var4.b * var2));
   }

   public static double e(double var0) {
      return 1.0 - Math.pow(1.0 - var0, 4.0);
   }

   public static double g(double var0) {
      return 1.0 - Math.pow(1.0 - var0, 3.0);
   }

   public static double c_clash26(double var0) {
      double var2 = 1.70158;
      double var4 = 2.70158;
      return 1.0 + var4 * Math.pow(var0 - 1.0, 3.0) + var2 * Math.pow(var0 - 1.0, 2.0);
   }

   public static double d(double var0) {
      double var2 = 1.70158;
      double var4 = 2.70158;
      return var4 * var0 * var0 * var0 - var2 * var0 * var0;
   }

   public static double b(double var0) {
      return Math.sin(var0 * Math.PI / 2.0);
   }

   public static double a_clash27(double var0) {
      return var0 * var0 * var0;
   }

   public static double h(double var0) {
      return -(Math.cos(Math.PI * var0) - 1.0) / 2.0;
   }

   public static double f(double var0) {
      return 1.0 - Math.cos(Math.PI * var0 / 2.0);
   }

   public static double a_clash28(double var0, double var2, double var4) {
      double var6 = (1.0 - Math.cos(var4 * Math.PI)) / 2.0;
      return var0 * (1.0 - var6) + var2 * var6;
   }

}
