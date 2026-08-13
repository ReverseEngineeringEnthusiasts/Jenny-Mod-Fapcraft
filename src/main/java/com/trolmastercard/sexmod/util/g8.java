package com.trolmastercard.sexmod.util;


public class g8 {
   public double b;
   public double a;

   public g8(double var1, double var3) {
      this.b = var1;
      this.a = var3;
   }

   public g8 a(g8 var1) {
      return new g8(this.b - var1.b, this.a - var1.a);
   }
}
