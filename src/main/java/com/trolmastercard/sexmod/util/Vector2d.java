package com.trolmastercard.sexmod.util;


public class Vector2d {
   public double b;
   public double a;

   public Vector2d(double var1, double var3) {
      this.b = var1;
      this.a = var3;
   }

   public Vector2d a(Vector2d var1) {
      return new Vector2d(this.b - var1.b, this.a - var1.a);
   }
}
