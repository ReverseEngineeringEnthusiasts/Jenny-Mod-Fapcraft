package com.trolmastercard.sexmod.util;


public class Point2D {
   public static final Point2D ZERO = new Point2D(0, 0);
   public int x;
   public int y;

   public Point2D(int var1, int var2) {
      this.x = var1;
      this.y = var2;
   }

   public float a_clash298(int var1, int var2) {
      float var3 = var1 - this.x;
      float var4 = var2 - this.y;
      return (float)Math.sqrt(var3 * var3 + var4 * var4);
   }

   @Override
   public String toString() {
      return String.format("(%s, %s)", this.x, this.y);
   }
}
