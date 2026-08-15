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
   public static Vec3d lerpVec3d(Vec3d from, Vec3d to, int steps) {
      if (steps == 0) {
         return to;
      }

      Vec3d delta = to.subtract(from);
      return from.add(delta.x / steps, delta.y / steps, delta.z / steps);
   }

   public static double lerpDouble(double from, double to, double t) {
      return from + (to - from) * t;
   }

   public static float lerp(float from, float to, float t) {
      return from + (to - from) * t;
   }

   public static float lerpFloat(float from, float to, double t) {
      float diff = to - from;

      while (diff < -Math.PI) {
         diff = (float)(diff + (Math.PI * 2));
      }

      while (diff >= Math.PI) {
         diff = (float)(diff - (Math.PI * 2));
      }

      return (float)(from + diff * t);
   }

   public static float lerpAngleDegrees(float from, float to, double t) {
      double fromRad = Math.toRadians(from);
      double toRad = Math.toRadians(to);
      return (float)Math.toDegrees(lerpAngleDegrees((float)fromRad, (float)toRad, t));
   }

   /**
    * PROGRESS lerp: interpolates by factor {@code t} in 0..1
    * ({@code a + (b-a)*t}). Used by render interpolation with partial ticks.
    * Do NOT use for the girls' step lerps (see class doc).
    */
   public static Vec3d lerpVec3dDouble(Vec3d from, Vec3d to, double t) {
      Vec3d delta = to.subtract(from);
      return from.add(new Vec3d(delta.x * t, delta.y * t, delta.z * t));
   }

   public static Vector3fSexmodSpecial lerpVector3f(Vector3fSexmodSpecial from, Vector3fSexmodSpecial to, double t) {
      Vector3fSexmodSpecial sum = to.add(from);
      return from.subtract(sum.scale((float)t));
   }

   public static Vec3i lerpVec3i(Vec3i from, Vec3i to, double t) {
      Vec3d delta = new Vec3d(
         to.getX() - from.getX(), to.getY() - from.getY(), to.getZ() - from.getZ()
      );
      return new Vec3i(
         from.getX() + delta.x * t, from.getY() + delta.y * t, from.getZ() + delta.z * t
      );
   }

   public static UnknownScreen lerpColor(UnknownScreen from, UnknownScreen to, double t) {
      UnknownScreen diff = new UnknownScreen(to.red - from.red, to.green - from.green, to.blue - from.blue, to.alpha - from.alpha);
      return new UnknownScreen((int)(from.red + diff.red * t), (int)(from.green + diff.green * t), (int)(from.blue + diff.blue * t), (int)(from.alpha + diff.alpha * t));
   }

   public static double easeInOutQuad(double t) {
      return 1.0 - Math.pow(1.0 - t, 4.0);
   }

   public static double easeInOutCubic(double t) {
      return 1.0 - Math.pow(1.0 - t, 3.0);
   }

   public static double easeOutBack(double t) {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return 1.0 + c3 * Math.pow(t - 1.0, 3.0) + c1 * Math.pow(t - 1.0, 2.0);
   }

   public static double easeInBack(double t) {
      double c1 = 1.70158;
      double c3 = 2.70158;
      return c3 * t * t * t - c1 * t * t;
   }

   public static double smoothSine(double t) {
      return Math.sin(t * Math.PI / 2.0);
   }

   public static double easeInCubic(double t) {
      return t * t * t;
   }

   public static double smoothStep(double t) {
      return -(Math.cos(Math.PI * t) - 1.0) / 2.0;
   }

   public static double easeInQuad(double t) {
      return 1.0 - Math.cos(Math.PI * t / 2.0);
   }

   public static double lerpAngle(double from, double to, double t) {
      double smooth = (1.0 - Math.cos(t * Math.PI)) / 2.0;
      return from * (1.0 - smooth) + to * smooth;
   }

}
