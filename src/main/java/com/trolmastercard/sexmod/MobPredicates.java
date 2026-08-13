package com.trolmastercard.sexmod;


import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;

public class MobPredicates {
   public static boolean a_clash801(Entity var0) {
      if (var0 instanceof EntityCreeper) {
         return false;
      } else if (var0 instanceof EntityPigZombie) {
         return false;
      } else {
         return var0 instanceof EntityGuardian ? false : !(var0 instanceof EntityEnderman);
      }
   }

   public static boolean a(World var0, Vec3d var1, Entity var2) {
      RayTraceResult var3 = var0.func_147447_a(var1, var2.func_174791_d().func_72441_c(0.0, var2.func_70047_e(), 0.0), true, true, false);
      return var3 == null ? true : var3.field_72313_a != Type.BLOCK;
   }

}
