package com.trolmastercard.sexmod.util;


import javax.vecmath.Vector3f;
import net.minecraft.util.math.Vec3d;

public class VectorMath {
   public static Vec3d a_clash304(Vec3d var0, double var1) {
      return new Vec3d(var0.x * var1, var0.y * var1, var0.z * var1);
   }

   public static double a(Vector3f var0, Vec3d var1) {
      return var0.x * var1.x + var0.y * var1.y + var0.z * var1.z;
   }

   public static double a_clash305(Vec3d var0, Vec3d var1) {
      return var0.x * var1.x + var0.y * var1.y + var0.z * var1.z;
   }

   public static Vec3d b(Vec3d var0, Vec3d var1) {
      return new Vec3d(
         var0.y * var1.z - var0.z * var1.y,
         var0.z * var1.x - var0.x * var1.z,
         var0.x * var1.y - var0.y * var1.x
      );
   }

   public static Vec3d a(double var0, double var2, double var4, float var6) {
      return rotateByYaw(new Vec3d(var0, var2, var4), var6);
   }

   public static Vec3d rotateByYaw(Vec3d var0, float var1) {
      return a(var0, 0.0F, var1);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2) {
      Vec3d var3 = new Vec3d(
         var0.x,
         var0.y * Math.cos(var1 * (Math.PI / 180.0)) - var0.z * Math.sin(var1 * (Math.PI / 180.0)),
         var0.y * Math.sin(var1 * (Math.PI / 180.0)) + var0.z * Math.cos(var1 * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((var2 + 90.0F) * (Math.PI / 180.0)) * var3.x - Math.sin(var2 * (Math.PI / 180.0)) * var3.z,
         var3.y,
         Math.cos((var2 + 90.0F) * (Math.PI / 180.0)) * var3.x + Math.cos(var2 * (Math.PI / 180.0)) * var3.z
      );
   }

   public static Vec3d a_clash307(double var0, double var2, double var4, float var6, float var7) {
      return a(new Vec3d(var0, var2, var4), var6, var7);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2, float var3) {
      var1 = TrigMath.wrapDegrees(var1);
      var2 = TrigMath.wrapDegrees(var2);
      var3 = TrigMath.wrapDegrees(var3);
      double var4 = (float)Math.sin(var1);
      double var6 = (float)Math.cos(var1);
      double var8 = (float)Math.sin(var2);
      double var10 = (float)Math.cos(var2);
      double var12 = (float)Math.sin(var3);
      double var14 = (float)Math.cos(var3);
      double var16 = var0.y * var6 - var0.z * var4;
      double var18 = var0.y * var4 + var0.z * var6;
      var0 = new Vec3d(var0.x, var16, var18);
      double var20 = var0.x * var10 + var0.z * var8;
      var18 = -var0.x * var8 + var0.z * var10;
      var0 = new Vec3d(var20, var0.y, var18);
      var20 = var0.x * var14 - var0.y * var12;
      var16 = var0.x * var12 + var0.y * var14;
      return new Vec3d(var20, var16, var0.z);
   }

   public static Vec3d c_clash308(Vec3d var0) {
      return new Vec3d(-var0.x, var0.y, -var0.z);
   }

   public static Vec3d a_clash309(Vec3d var0) {
      return new Vec3d(-var0.x, -var0.y, var0.z);
   }

   public static Vec3d b_clash310(Vec3d var0) {
      return new Vec3d(var0.x, -var0.y, -var0.z);
   }

   static double a_clash311(double var0, double var2, double var4) {
      return (var4 - var0) / (var2 - var0);
   }

   public static double a(Vec3d var0, Vec3d var1, Vec3d var2) {
      return a_clash311(var0.x, var1.x, var2.x);
   }
}
