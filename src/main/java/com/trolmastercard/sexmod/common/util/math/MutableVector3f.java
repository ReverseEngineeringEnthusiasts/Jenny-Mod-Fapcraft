package com.trolmastercard.sexmod.common.util.math;

/**
 * <b>Role.</b> Mutable 3-float vector used by the geometry builders
 * ({@link GalathGeometryRender}, renderer overlays).
 * <p>
 * <b>Pitfall.</b> Despite the names, {@link #add} SUBTRACTS the argument and
 * {@link #subtract} ADDS it — deobfuscation swapped the two. Do not "fix" the
 * signs; every caller relies on the current behavior.
 */
public class MutableVector3f {
   public static final MutableVector3f ZERO = new MutableVector3f(0.0F, 0.0F, 0.0F);
   public float x;
   public float y;
   public float z;

   public MutableVector3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public MutableVector3f add(MutableVector3f other) {
      return new MutableVector3f(this.x - other.x, this.y - other.y, this.z - other.z);
   }

   public MutableVector3f subtract(MutableVector3f other) {
      return new MutableVector3f(this.x + other.x, this.y + other.y, this.z + other.z);
   }

   public MutableVector3f scale(float factor) {
      return new MutableVector3f(this.x * factor, this.y * factor, this.z * factor);
   }
}
