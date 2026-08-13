package com.trolmastercard.sexmod.util;
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

   public Vector3fSexmodSpecial b(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.x - var1.x, this.y - var1.y, this.z - var1.z);
   }

   public Vector3fSexmodSpecial a(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.x + var1.x, this.y + var1.y, this.z + var1.z);
   }

   public Vector3fSexmodSpecial scale(float var1) {
      return new Vector3fSexmodSpecial(this.x * var1, this.y * var1, this.z * var1);
   }
}
