package com.trolmastercard.sexmod.util;
/**
 * <b>Role.</b> Simple immutable int point with distance helper. Used by the
 * {@code StructureCommandScreen} grid math.
 */
public class Point2D {
   public static final Point2D ZERO = new Point2D(0, 0);
   public int x;
   public int y;

   public Point2D(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public float distanceTo(int x, int y) {
      float dx = x - this.x;
      float dy = y - this.y;
      return (float)Math.sqrt(dx * dx + dy * dy);
   }

   @Override
   public String toString() {
      return String.format("(%s, %s)", this.x, this.y);
   }
}
