package com.trolmastercard.sexmod.util;


public class Vector2d {
   public double y;
   public double x;

   public Vector2d(double var1, double var3) {
      this.y = var1;
      this.x = var3;
   }

   public Vector2d a(Vector2d var1) {
      return new Vector2d(this.y - var1.y, this.x - var1.x);
   }
}
