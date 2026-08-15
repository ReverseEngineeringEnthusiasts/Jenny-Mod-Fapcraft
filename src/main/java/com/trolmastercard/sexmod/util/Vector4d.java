package com.trolmastercard.sexmod.util;
/**
 * 4D double vector value type (used e.g. for bed target + yaw).
 */
public class Vector4d {
   public double x;
   public double y;
   public double z;
   public double w;

   public Vector4d(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }
}
