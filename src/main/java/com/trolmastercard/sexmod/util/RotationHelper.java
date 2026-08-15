package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

/**
 * Math/lerp helpers shared across the mod.
 * <p>
 * <b>CRITICAL — two different lerp families:</b>
 * <ul>
 *   <li>{@link #lerpVec3d(Vec3d, Vec3d, int)} — <b>STEP lerp</b>: takes one
 *       step of the remaining distance ({@code a + (b-a)/t}). The girls'
 *       40-tick dismount/walk lerps call this with {@code t = 40 - counter}
 *       (int). Do NOT switch those call sites to the double variant — the
 *       deobfuscation once did and every girl got flung 40&times; the
 *       distance on the first tick, their chunk unloaded, and they vanished
 *       (destroy packet -> client setDead, gone on reload).</li>
 *   <li>{@link #lerpVec3dDouble(Vec3d, Vec3d, double)} — <b>PROGRESS lerp</b>:
 *       interpolates by a factor in 0..1 ({@code a + (b-a)*t}). Used by
 *       render code with partial ticks — that is correct there.</li>
 * </ul>
 * Both variants existed in the original jar with distinct descriptors
 * ({@code (Vec3d, Vec3d, int)} vs {@code (Vec3d, Vec3d, double)}); the int
 * arguments auto-widen, which is how the wrong-variant bug slipped in.
 */
public class RotationHelper {
   /**
    * STEP lerp: one step of the remaining distance ({@code a + (b-a)/t}).
    * Returns {@code b} unchanged when {@code t == 0}.
    * <p>
    * Used by the girls' 40-tick dismount/walk lerps with {@code t = 40 - counter}.
    * <b>Do not reroute those call sites to
    * {@link #lerpVec3dDouble(Vec3d, Vec3d, double)}</b> — int arguments
    * auto-widen and the multiply version flings the girl 40x (see class doc).
    */
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

   /**
    * PROGRESS lerp: interpolates by factor {@code t} in 0..1
    * ({@code a + (b-a)*t}). Used by render interpolation with partial ticks.
    * Do NOT use for the girls' step lerps (see class doc).
    */
   public static Vec3d lerpVec3dDouble(Vec3d var0, Vec3d var1, double var2) {
      Vec3d var4 = var1.subtract(var0);
      return var0.add(new Vec3d(var4.x * var2, var4.y * var2, var4.z * var2));
   }

   public static Vector3fSexmodSpecial lerpVector3f(Vector3fSexmodSpecial var0, Vector3fSexmodSpecial var1, double var2) {
      Vector3fSexmodSpecial var4 = var1.add(var0);
      return var0.subtract(var4.scale((float)var2));
   }

   public static Vec3i lerpVec3i(Vec3i var0, Vec3i var1, double var2) {
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
