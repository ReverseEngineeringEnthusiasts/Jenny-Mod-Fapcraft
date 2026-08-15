package com.trolmastercard.sexmod.util;

import net.minecraft.util.math.Vec3d;

/**
 * Trigonometry helpers (wrapDegrees etc.).
 */
public class TrigMath {
   public static double calculatePitchAngle(Vec3d from, Vec3d to) {
      double dx = to.x - from.x;
      double dy = to.y - from.y;
      double dz = to.z - from.z;
      return Math.atan2(dz, Math.sqrt(dx * dx + dy * dy));
   }

   public static float NormalizeAngle(float angle) {
      angle %= 360.0F;
      if (angle < 0.0F) {
         angle += 360.0F;
      }

      return angle;
   }

   public static float normalizedAngle360(float angle) {
      float result;
      return (result = angle % 360.0F) >= 0.0F ? result : result + 360.0F;
   }

   public static double normalizedAngle360(double angle) {
      double result;
      return (result = angle % 360.0) >= 0.0 ? result : result + 360.0;
   }

   public static float wrapDegrees(float angle) {
      return (float)((Math.PI * 2) / (360.0 / angle));
   }

   public static float toRadians(double degrees) {
      return (float)((Math.PI * 2) / (360.0 / degrees));
   }

   public static float toDegrees(float radians) {
      return (float)((180.0 / Math.PI) * radians);
   }

   public static double sinDegrees(double degrees) {
      return (180.0 / Math.PI) * degrees;
   }

}
