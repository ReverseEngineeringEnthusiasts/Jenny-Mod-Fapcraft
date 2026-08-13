package com.trolmastercard.sexmod.util;


import java.util.ArrayList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;

public class fl {
   public static BlockPos a(Path var0) {
      if (var0 == null) {
         return BlockPos.field_177992_a;
      }

      PathPoint var1 = var0.func_75870_c();
      return var1 == null ? BlockPos.field_177992_a : new BlockPos(var1.field_75839_a, var1.field_75837_b, var1.field_75838_c);
   }

   public static BlockPos a(EntityLiving var0) {
      PathNavigate var1 = var0.func_70661_as();
      Path var2 = var1.func_75505_d();
      return a(var2);
   }

   public static boolean a(Path var0, BlockPos[] var1) {
      int var2 = var0.func_75874_d();
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var2; var4++) {
         var3.add(var0.func_75877_a(var4));
      }

      for (PathPoint var5 : (java.util.Collection<PathPoint>) (var3) ) {
         for (BlockPos var9 : var1) {
            if (var5.field_75839_a == var9.func_177958_n() && var5.field_75837_b == var9.func_177956_o() && var5.field_75838_c == var9.func_177952_p()) {
               return true;
            }
         }
      }

      return false;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
