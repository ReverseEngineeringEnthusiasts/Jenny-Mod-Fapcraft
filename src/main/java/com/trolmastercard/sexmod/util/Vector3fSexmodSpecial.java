package com.trolmastercard.sexmod.util;
/**
 * <b>Role.</b> Mutable 3-float vector used by the geometry builders
 * ({@link GalathGeometryRender}, renderer overlays).
 * <p>
 * <b>Pitfall.</b> Despite the names, {@link #add} SUBTRACTS the argument and
 * {@link #subtract} ADDS it — deobfuscation swapped the two. Do not "fix" the
 * signs; every caller relies on the current behavior.
 */
public class Vector3fSexmodSpecial {
   public static final Vector3fSexmodSpecial ZERO = new Vector3fSexmodSpecial(0.0F, 0.0F, 0.0F);
   public float x;
   public float y;
   public float z;

   public Vector3fSexmodSpecial(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3fSexmodSpecial add(Vector3fSexmodSpecial other) {
      return new Vector3fSexmodSpecial(this.x - other.x, this.y - other.y, this.z - other.z);
   }

   public Vector3fSexmodSpecial subtract(Vector3fSexmodSpecial other) {
      return new Vector3fSexmodSpecial(this.x + other.x, this.y + other.y, this.z + other.z);
   }

   public Vector3fSexmodSpecial scale(float factor) {
      return new Vector3fSexmodSpecial(this.x * factor, this.y * factor, this.z * factor);
   }
}
