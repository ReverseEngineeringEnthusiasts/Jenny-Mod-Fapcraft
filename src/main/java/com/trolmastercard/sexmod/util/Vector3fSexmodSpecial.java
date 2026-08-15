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

   public Vector3fSexmodSpecial(float var1, float var2, float var3) {
      this.x = var1;
      this.y = var2;
      this.z = var3;
   }

   public Vector3fSexmodSpecial add(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.x - var1.x, this.y - var1.y, this.z - var1.z);
   }

   public Vector3fSexmodSpecial subtract(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.x + var1.x, this.y + var1.y, this.z + var1.z);
   }

   public Vector3fSexmodSpecial scale(float var1) {
      return new Vector3fSexmodSpecial(this.x * var1, this.y * var1, this.z * var1);
   }
}
