package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class RotationHelper {
   public static Vec3d lerpVec3d(Vec3d var0, Vec3d var1, int var2) {
      if (var2 == 0) {
         return var1;
      }

      Vec3d var3 = var1.subtract(var0);
      return var0.add(var3.x / var2, var3.y / var2, var3.z / var2);
   }

   public static double lerpDouble(double var0, double var2, double var4) {
      return var0 + (var2 - var0) * var4;
   }

   public static float lerp(float var0, float var1, float var2) {
      return var0 + (var1 - var0) * var2;
   }

   public static float lerpFloat(float var0, float var1, double var2) {
      float var4 = var1 - var0;

      while (var4 < -Math.PI) {
         var4 = (float)(var4 + (Math.PI * 2));
      }

      while (var4 >= Math.PI) {
         var4 = (float)(var4 - (Math.PI * 2));
      }

      return (float)(var0 + var4 * var2);
   }

   public static float lerpAngleDegrees(float var0, float var1, double var2) {
      double var4 = Math.toRadians(var0);
      double var6 = Math.toRadians(var1);
      return (float)Math.toDegrees(lerpAngleDegrees((float)var4, (float)var6, var2));
   }

   public static Vec3d lerpVec3dDouble(Vec3d var0, Vec3d var1, double var2) {
      Vec3d var4 = var1.subtract(var0);
      return var0.add(new Vec3d(var4.x * var2, var4.y * var2, var4.z * var2));
   }

   public static Vector3fSexmodSpecial lerpVector3f(Vector3fSexmodSpecial var0, Vector3fSexmodSpecial var1, double var2) {
      Vector3fSexmodSpecial var4 = var1.b(var0);
      return var0.a(var4.scale((float)var2));
   }

   public static Vec3i a(Vec3i var0, Vec3i var1, double var2) {
      Vec3d var4 = new Vec3d(
         var1.getX() - var0.getX(), var1.getY() - var0.getY(), var1.getZ() - var0.getZ()
      );
      return new Vec3i(
         var0.getX() + var4.x * var2, var0.getY() + var4.y * var2, var0.getZ() + var4.z * var2
      );
   }

   public static UnknownScreen lerpColor(UnknownScreen var0, UnknownScreen var1, double var2) {
      UnknownScreen var4 = new UnknownScreen(var1.red - var0.red, var1.green - var0.green, var1.blue - var0.blue, var1.alpha - var0.alpha);
      return new UnknownScreen((int)(var0.red + var4.red * var2), (int)(var0.green + var4.green * var2), (int)(var0.blue + var4.blue * var2), (int)(var0.alpha + var4.alpha * var2));
   }

   public static double easeInOutQuad(double var0) {
      return 1.0 - Math.pow(1.0 - var0, 4.0);
   }

   public static double easeInOutCubic(double var0) {
      return 1.0 - Math.pow(1.0 - var0, 3.0);
   }

   public static double easeOutBack(double var0) {
      double var2 = 1.70158;
      double var4 = 2.70158;
      return 1.0 + var4 * Math.pow(var0 - 1.0, 3.0) + var2 * Math.pow(var0 - 1.0, 2.0);
   }

   public static double easeInBack(double var0) {
      double var2 = 1.70158;
      double var4 = 2.70158;
      return var4 * var0 * var0 * var0 - var2 * var0 * var0;
   }

   public static double smoothSine(double var0) {
      return Math.sin(var0 * Math.PI / 2.0);
   }

   public static double easeInCubic(double var0) {
      return var0 * var0 * var0;
   }

   public static double smoothStep(double var0) {
      return -(Math.cos(Math.PI * var0) - 1.0) / 2.0;
   }

   public static double easeInQuad(double var0) {
      return 1.0 - Math.cos(Math.PI * var0 / 2.0);
   }

   public static double lerpAngle(double var0, double var2, double var4) {
      double var6 = (1.0 - Math.cos(var4 * Math.PI)) / 2.0;
      return var0 * (1.0 - var6) + var2 * var6;
   }

}
