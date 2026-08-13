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
         return BlockPos.ORIGIN;
      }

      PathPoint var1 = var0.getFinalPathPoint();
      return var1 == null ? BlockPos.ORIGIN : new BlockPos(var1.x, var1.y, var1.z);
   }

   public static BlockPos a(EntityLiving var0) {
      PathNavigate var1 = var0.getNavigator();
      Path var2 = var1.getPath();
      return a(var2);
   }

   public static boolean a(Path var0, BlockPos[] var1) {
      int var2 = var0.getCurrentPathLength();
      ArrayList var3 = new ArrayList();

      for (int var4 = 0; var4 < var2; var4++) {
         var3.add(var0.getPathPointFromIndex(var4));
      }

      for (PathPoint var5 : (java.util.Collection<PathPoint>) (var3) ) {
         for (BlockPos var9 : var1) {
            if (var5.x == var9.getX() && var5.y == var9.getY() && var5.z == var9.getZ()) {
               return true;
            }
         }
      }

      return false;
   }

}
