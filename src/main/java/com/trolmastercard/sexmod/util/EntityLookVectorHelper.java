package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> CLIENT-side look/aim vector helpers for girl renderers and item
 * aiming. Computes entity-to-player and aim direction vectors with partial-tick
 * interpolation; anchored girls aim at their {@code TARGET_POS} instead of their
 * yaw so scene animations look at the correct spot.
 * <p>
 * <b>Pitfall.</b> All lerps here use
 * {@link RotationHelper#lerpVec3dDouble} (PROGRESS lerp by partial tick) — the
 * correct choice for render interpolation.
 */
public class EntityLookVectorHelper {
   public static Vec3d getLookVectorTo(Entity entity, EntityPlayer player, float partialTicks) {
      Vec3d entityPos = RotationHelper.lerpVec3dDouble(
         new Vec3d(entity.lastTickPosX, entity.lastTickPosY + player.getEyeHeight(), entity.lastTickPosZ),
         entity.getPositionVector().add(0.0, player.getEyeHeight(), 0.0),
         partialTicks
      );
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(player.lastTickPosX, player.lastTickPosY, player.lastTickPosZ), player.getPositionVector(), partialTicks);
      return entityPos.subtract(playerPos);
   }

   public static Vec3d getAimVector(Entity entity, EntityPlayer player, float partialTicks) {
      Vec3d lookVec = getEntityLookVector(entity, partialTicks);
      if (player == null) {
         return lookVec;
      }

      Vec3d playerLookVec = getEntityLookVector(player, partialTicks);
      return lookVec.subtract(playerLookVec);
   }

   public static Vec3d getEntityLookVector(Entity entity, float partialTicks) {
      if (!(entity instanceof BaseGirlEntity)) {
         return getLookVectorYaw(entity, partialTicks);
      }

      BaseGirlEntity girl = (BaseGirlEntity)entity;
      return !girl.isAnchored() ? getLookVectorYaw(entity, partialTicks) : girl.getTargetPosition();
   }

   static Vec3d getLookVectorYaw(Entity entity, float partialTicks) {
      return RotationHelper.lerpVec3dDouble(new Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ), entity.getPositionVector(), partialTicks);
   }

   public static void setFullBrightLightmap() {
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
   }

}
