package com.trolmastercard.sexmod.util;


import net.minecraft.util.math.Vec3i;

public enum g5 {
   PURPLE(103, 39, 123),
   ORANGE(251, 153, 56),
   BLACK(30, 33, 38),
   BLUE(88, 83, 186),
   BROWN(63, 35, 34),
   PINK(247, 102, 109),
   RED(241, 69, 49),
   GREEN(75, 143, 106);

   private final Vec3i b;

   g5(int var3, int var4, int var5) {
      this.b = new Vec3i(var3, var4, var5);
   }

   public Vec3i a_clash448() {
      return this.b;
   }

   public static int a(g5 var0) {
      int var1 = 0;

      for (g5 var5 : values()) {
         if (var0 == var5) {
            return var1;
         }

         var1++;
      }

      return var1;
   }

}
