package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

public class ak {
   public static Vec3d b(Entity var0, EntityPlayer var1, float var2) {
      Vec3d var3 = RotationHelper.a(
         new Vec3d(var0.lastTickPosX, var0.lastTickPosY + var1.getEyeHeight(), var0.lastTickPosZ),
         var0.getPositionVector().add(0.0, var1.getEyeHeight(), 0.0),
         var2
      );
      Vec3d var4 = RotationHelper.a(new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ), var1.getPositionVector(), var2);
      return var3.subtract(var4);
   }

   public static Vec3d a(Entity var0, EntityPlayer var1, float var2) {
      Vec3d var3 = a_clash52(var0, var2);
      if (var1 == null) {
         return var3;
      }

      Vec3d var4 = a_clash52(var1, var2);
      return var3.subtract(var4);
   }

   public static Vec3d a_clash52(Entity var0, float var1) {
      if (!(var0 instanceof BaseGirlEntity)) {
         return b(var0, var1);
      }

      BaseGirlEntity var2 = (BaseGirlEntity)var0;
      return !var2.isAnchored() ? b(var0, var1) : var2.getTargetPosition();
   }

   static Vec3d b(Entity var0, float var1) {
      return RotationHelper.a(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector(), var1);
   }

   public static void a_clash53() {
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
   }

}
