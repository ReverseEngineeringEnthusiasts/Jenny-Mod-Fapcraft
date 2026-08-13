package com.trolmastercard.sexmod.util;


public class f7 {
   public static final f7 d = new f7(0.0F, 0.0F, 0.0F);
   public float a;
   public float c;
   public float b;

   public f7(float var1, float var2, float var3) {
      this.a = var1;
      this.c = var2;
      this.b = var3;
   }

   public f7 b(f7 var1) {
      return new f7(this.a - var1.a, this.c - var1.c, this.b - var1.b);
   }

   public f7 a(f7 var1) {
      return new f7(this.a + var1.a, this.c + var1.c, this.b + var1.b);
   }

   public f7 a_clash407(float var1) {
      return new f7(this.a * var1, this.c * var1, this.b * var1);
   }
}
