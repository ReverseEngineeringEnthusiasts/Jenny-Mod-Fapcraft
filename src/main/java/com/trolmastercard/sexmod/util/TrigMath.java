package com.trolmastercard.sexmod.util;

import net.minecraft.util.math.Vec3d;

/**
 * Trigonometry helpers (wrapDegrees etc.).
 */
public class TrigMath {
   public static double calculatePitchAngle(Vec3d var0, Vec3d var1) {
      double var2 = var1.x - var0.x;
      double var4 = var1.y - var0.y;
      double var6 = var1.z - var0.z;
      return Math.atan2(var6, Math.sqrt(var2 * var2 + var4 * var4));
   }

   public static float NormalizeAngle(float var0) {
      var0 %= 360.0F;
      if (var0 < 0.0F) {
         var0 += 360.0F;
      }

      return var0;
   }

   public static float normalizedAngle360(float var0) {
      float var1;
      return (var1 = var0 % 360.0F) >= 0.0F ? var1 : var1 + 360.0F;
   }

   public static double normalizedAngle360(double var0) {
      double var2;
      return (var2 = var0 % 360.0) >= 0.0 ? var2 : var2 + 360.0;
   }

   public static float wrapDegrees(float var0) {
      return (float)((Math.PI * 2) / (360.0 / var0));
   }

   public static float toRadians(double var0) {
      return (float)((Math.PI * 2) / (360.0 / var0));
   }

   public static float toDegrees(float var0) {
      return (float)((180.0 / Math.PI) * var0);
   }

   public static double sinDegrees(double var0) {
      return (180.0 / Math.PI) * var0;
   }

}
