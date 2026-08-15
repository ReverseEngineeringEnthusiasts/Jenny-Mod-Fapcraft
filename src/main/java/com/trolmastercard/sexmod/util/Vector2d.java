package com.trolmastercard.sexmod.util;
/**
 * 2D double vector value type.
 */
public class Vector2d {
   public double y;
   public double x;

   public Vector2d(double y, double x) {
      this.y = y;
      this.x = x;
   }

   public Vector2d add(Vector2d other) {
      return new Vector2d(this.y - other.y, this.x - other.x);
   }
}
