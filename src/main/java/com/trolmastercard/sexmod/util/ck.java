package com.trolmastercard.sexmod.util;


import javax.vecmath.Vector3f;
import net.minecraft.util.math.Vec3d;

public class ck {
   public static Vec3d a_clash304(Vec3d var0, double var1) {
      return new Vec3d(var0.field_72450_a * var1, var0.field_72448_b * var1, var0.field_72449_c * var1);
   }

   public static double a(Vector3f var0, Vec3d var1) {
      return var0.x * var1.field_72450_a + var0.y * var1.field_72448_b + var0.z * var1.field_72449_c;
   }

   public static double a_clash305(Vec3d var0, Vec3d var1) {
      return var0.field_72450_a * var1.field_72450_a + var0.field_72448_b * var1.field_72448_b + var0.field_72449_c * var1.field_72449_c;
   }

   public static Vec3d b(Vec3d var0, Vec3d var1) {
      return new Vec3d(
         var0.field_72448_b * var1.field_72449_c - var0.field_72449_c * var1.field_72448_b,
         var0.field_72449_c * var1.field_72450_a - var0.field_72450_a * var1.field_72449_c,
         var0.field_72450_a * var1.field_72448_b - var0.field_72448_b * var1.field_72450_a
      );
   }

   public static Vec3d a(double var0, double var2, double var4, float var6) {
      return a_clash306(new Vec3d(var0, var2, var4), var6);
   }

   public static Vec3d a_clash306(Vec3d var0, float var1) {
      return a(var0, 0.0F, var1);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2) {
      Vec3d var3 = new Vec3d(
         var0.field_72450_a,
         var0.field_72448_b * Math.cos(var1 * (Math.PI / 180.0)) - var0.field_72449_c * Math.sin(var1 * (Math.PI / 180.0)),
         var0.field_72448_b * Math.sin(var1 * (Math.PI / 180.0)) + var0.field_72449_c * Math.cos(var1 * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((var2 + 90.0F) * (Math.PI / 180.0)) * var3.field_72450_a - Math.sin(var2 * (Math.PI / 180.0)) * var3.field_72449_c,
         var3.field_72448_b,
         Math.cos((var2 + 90.0F) * (Math.PI / 180.0)) * var3.field_72450_a + Math.cos(var2 * (Math.PI / 180.0)) * var3.field_72449_c
      );
   }

   public static Vec3d a_clash307(double var0, double var2, double var4, float var6, float var7) {
      return a(new Vec3d(var0, var2, var4), var6, var7);
   }

   public static Vec3d a(Vec3d var0, float var1, float var2, float var3) {
      var1 = gc.c_clash744(var1);
      var2 = gc.c_clash744(var2);
      var3 = gc.c_clash744(var3);
      double var4 = (float)Math.sin(var1);
      double var6 = (float)Math.cos(var1);
      double var8 = (float)Math.sin(var2);
      double var10 = (float)Math.cos(var2);
      double var12 = (float)Math.sin(var3);
      double var14 = (float)Math.cos(var3);
      double var16 = var0.field_72448_b * var6 - var0.field_72449_c * var4;
      double var18 = var0.field_72448_b * var4 + var0.field_72449_c * var6;
      var0 = new Vec3d(var0.field_72450_a, var16, var18);
      double var20 = var0.field_72450_a * var10 + var0.field_72449_c * var8;
      var18 = -var0.field_72450_a * var8 + var0.field_72449_c * var10;
      var0 = new Vec3d(var20, var0.field_72448_b, var18);
      var20 = var0.field_72450_a * var14 - var0.field_72448_b * var12;
      var16 = var0.field_72450_a * var12 + var0.field_72448_b * var14;
      return new Vec3d(var20, var16, var0.field_72449_c);
   }

   public static Vec3d c_clash308(Vec3d var0) {
      return new Vec3d(-var0.field_72450_a, var0.field_72448_b, -var0.field_72449_c);
   }

   public static Vec3d a_clash309(Vec3d var0) {
      return new Vec3d(-var0.field_72450_a, -var0.field_72448_b, var0.field_72449_c);
   }

   public static Vec3d b_clash310(Vec3d var0) {
      return new Vec3d(var0.field_72450_a, -var0.field_72448_b, -var0.field_72449_c);
   }

   static double a_clash311(double var0, double var2, double var4) {
      return (var4 - var0) / (var2 - var0);
   }

   public static double a(Vec3d var0, Vec3d var1, Vec3d var2) {
      return a_clash311(var0.field_72450_a, var1.field_72450_a, var2.field_72450_a);
   }
}
