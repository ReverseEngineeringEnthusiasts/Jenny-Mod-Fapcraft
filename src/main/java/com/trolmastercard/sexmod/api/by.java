package com.trolmastercard.sexmod.api;


import net.minecraft.util.math.Vec3i;

public enum by {
   LIGHT_GREEN(213, 239, 150),
   MEDIUM_GREEN(189, 165, 91),
   DARK_GREEN(160, 183, 135),
   LIGHT_YELLOW(234, 176, 102),
   LIGHT_BLUE(187, 203, 252);

   private final Vec3i a;

   by(int var3, int var4, int var5) {
      this.a = new Vec3i(var3, var4, var5);
   }

   public Vec3i a_clash189() {
      return this.a;
   }

   public static int a(by var0) {
      int var1 = 0;

      for (by var5 : values()) {
         if (var0 == var5) {
            return var1;
         }

         var1++;
      }

      return var1;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
