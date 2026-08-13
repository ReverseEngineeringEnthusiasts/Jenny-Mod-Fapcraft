package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class ak {
   public static Vec3d b(Entity var0, EntityPlayer var1, float var2) {
      Vec3d var3 = RotationHelper.a(
         new Vec3d(var0.field_70142_S, var0.field_70137_T + var1.func_70047_e(), var0.field_70136_U),
         var0.func_174791_d().func_72441_c(0.0, var1.func_70047_e(), 0.0),
         var2
      );
      Vec3d var4 = RotationHelper.a(new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U), var1.func_174791_d(), var2);
      return var3.func_178788_d(var4);
   }

   public static Vec3d a(Entity var0, EntityPlayer var1, float var2) {
      Vec3d var3 = a_clash52(var0, var2);
      if (var1 == null) {
         return var3;
      }

      Vec3d var4 = a_clash52(var1, var2);
      return var3.func_178788_d(var4);
   }

   public static Vec3d a_clash52(Entity var0, float var1) {
      if (!(var0 instanceof BaseGirlEntity)) {
         return b(var0, var1);
      }

      BaseGirlEntity var2 = (BaseGirlEntity)var0;
      return !var2.isAnchored() ? b(var0, var1) : var2.getTargetPosition();
   }

   static Vec3d b(Entity var0, float var1) {
      return RotationHelper.a(new Vec3d(var0.field_70142_S, var0.field_70137_T, var0.field_70136_U), var0.func_174791_d(), var1);
   }

   public static void a_clash53() {
      OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0F, 240.0F);
   }

}
