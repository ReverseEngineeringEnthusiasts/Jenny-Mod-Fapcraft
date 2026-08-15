package com.trolmastercard.sexmod.util;

import javax.vecmath.Vector3f;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> Vector math helpers for the renderers and scene code: scaling,
 * dot/cross products, yaw/pitch and Euler rotations, mirroring and linear-factor
 * interpolation.
 * <p>
 * <b>Pitfall.</b> {@link #rotateByYawPitch} uses the Minecraft convention
 * (yaw + 90 shift, x/z plane) — feeding it raw degrees from a different
 * convention rotates meshes sideways. {@link #getLinearFactor} divides by
 * {@code (b-a)}; a zero denominator yields NaN — callers must ensure distinct
 * points.
 */
public class VectorMath {
   public static Vec3d scale(Vec3d vector, double factor) {
      return new Vec3d(vector.x * factor, vector.y * factor, vector.z * factor);
   }

   public static double dotProduct(Vector3f a, Vec3d b) {
      return a.x * b.x + a.y * b.y + a.z * b.z;
   }

   public static double dotProduct(Vec3d a, Vec3d b) {
      return a.x * b.x + a.y * b.y + a.z * b.z;
   }

   public static Vec3d crossProduct(Vec3d a, Vec3d b) {
      return new Vec3d(
         a.y * b.z - a.z * b.y,
         a.z * b.x - a.x * b.z,
         a.x * b.y - a.y * b.x
      );
   }

   public static Vec3d rotateByYaw(double x, double y, double z, float yaw) {
      return rotateByYaw(new Vec3d(x, y, z), yaw);
   }

   public static Vec3d rotateByYaw(Vec3d vector, float yaw) {
      return rotateByYawPitch(vector, 0.0F, yaw);
   }

   public static Vec3d rotateByYawPitch(Vec3d vector, float yaw, float pitch) {
      Vec3d rotated = new Vec3d(
         vector.x,
         vector.y * Math.cos(yaw * (Math.PI / 180.0)) - vector.z * Math.sin(yaw * (Math.PI / 180.0)),
         vector.y * Math.sin(yaw * (Math.PI / 180.0)) + vector.z * Math.cos(yaw * (Math.PI / 180.0))
      );
      return new Vec3d(
         -Math.sin((pitch + 90.0F) * (Math.PI / 180.0)) * rotated.x - Math.sin(pitch * (Math.PI / 180.0)) * rotated.z,
         rotated.y,
         Math.cos((pitch + 90.0F) * (Math.PI / 180.0)) * rotated.x + Math.cos(pitch * (Math.PI / 180.0)) * rotated.z
      );
   }

   public static Vec3d rotate(double x, double y, double z, float yaw, float pitch) {
      return rotateByYawPitch(new Vec3d(x, y, z), yaw, pitch);
   }

   public static Vec3d rotateByEuler(Vec3d vector, float yaw, float pitch, float roll) {
      yaw = TrigMath.wrapDegrees(yaw);
      pitch = TrigMath.wrapDegrees(pitch);
      roll = TrigMath.wrapDegrees(roll);
      double sinYaw = (float)Math.sin(yaw);
      double cosYaw = (float)Math.cos(yaw);
      double sinPitch = (float)Math.sin(pitch);
      double cosPitch = (float)Math.cos(pitch);
      double sinRoll = (float)Math.sin(roll);
      double cosRoll = (float)Math.cos(roll);
      double newY = vector.y * cosYaw - vector.z * sinYaw;
      double newZ = vector.y * sinYaw + vector.z * cosYaw;
      vector = new Vec3d(vector.x, newY, newZ);
      double newX = vector.x * cosPitch + vector.z * sinPitch;
      newZ = -vector.x * sinPitch + vector.z * cosPitch;
      vector = new Vec3d(newX, vector.y, newZ);
      newX = vector.x * cosRoll - vector.y * sinRoll;
      newY = vector.x * sinRoll + vector.y * cosRoll;
      return new Vec3d(newX, newY, vector.z);
   }

   public static Vec3d MirrorXZ(Vec3d vector) {
      return new Vec3d(-vector.x, vector.y, -vector.z);
   }

   public static Vec3d MirrorXY(Vec3d vector) {
      return new Vec3d(-vector.x, -vector.y, vector.z);
   }

   public static Vec3d MirrorYZ(Vec3d vector) {
      return new Vec3d(vector.x, -vector.y, -vector.z);
   }

   static double getLinearFactor(double start, double end, double value) {
      return (value - start) / (end - start);
   }

   public static double getLinearFactor(Vec3d start, Vec3d end, Vec3d value) {
      return getLinearFactor(start.x, end.x, value.x);
   }
}
