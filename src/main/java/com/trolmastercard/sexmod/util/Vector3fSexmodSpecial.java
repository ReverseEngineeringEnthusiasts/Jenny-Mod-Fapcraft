package com.trolmastercard.sexmod.util;


public class Vector3fSexmodSpecial {
   public static final Vector3fSexmodSpecial d = new Vector3fSexmodSpecial(0.0F, 0.0F, 0.0F);
   public float a;
   public float c;
   public float b;

   public Vector3fSexmodSpecial(float var1, float var2, float var3) {
      this.a = var1;
      this.c = var2;
      this.b = var3;
   }

   public Vector3fSexmodSpecial b(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.a - var1.a, this.c - var1.c, this.b - var1.b);
   }

   public Vector3fSexmodSpecial a(Vector3fSexmodSpecial var1) {
      return new Vector3fSexmodSpecial(this.a + var1.a, this.c + var1.c, this.b + var1.b);
   }

   public Vector3fSexmodSpecial a_clash407(float var1) {
      return new Vector3fSexmodSpecial(this.a * var1, this.c * var1, this.b * var1);
   }
}
