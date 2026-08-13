package com.trolmastercard.sexmod.util;


import net.minecraft.util.math.Vec3i;

public enum eh {
   RED(255, 0, 0),
   VIOLET(132, 30, 156),
   YELLOW(243, 247, 0),
   BROWN(105, 60, 9),
   TURKEY(0, 206, 217),
   BLUE(0, 0, 255);

   private final Vec3i b;

   eh(int var3, int var4, int var5) {
      this.b = new Vec3i(var3, var4, var5);
   }

   public Vec3i a_clash565() {
      return this.b;
   }

   public static eh a_clash566(Vec3i var0) {
      for (eh var4 : values()) {
         if (var0.equals(var4.a_clash565())) {
            return var4;
         }
      }

      return RED;
   }

   public static int a(eh var0) {
      int var1 = 0;

      for (eh var5 : values()) {
         if (var0 == var5) {
            return var1;
         }

         var1++;
      }

      return var1;
   }

}
