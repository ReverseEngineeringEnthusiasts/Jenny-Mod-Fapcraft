package com.trolmastercard.sexmod.util;
/**
 * 2D float vector value type.
 */
public class Vector2f {
   public static final Vector2f ZERO = new Vector2f(0.0F, 0.0F);
   public float x;
   public float y;

   public Vector2f(float x, float y) {
      this.x = x;
      this.y = y;
   }
}
