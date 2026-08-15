package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import java.util.HashSet;
import javax.vecmath.Vector3f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Slime (horny potion).
 */
public class PlayerSlimeRenderer extends GirlPlayerRenderer {
   Vector3f scaleSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f positionSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot3 = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f currentRotation = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot2 = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot4 = new Vector3f(0.0F, 0.0F, 0.0F);

   public PlayerSlimeRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.25F, 0.0F);
      GlStateManager.scale(0.8F, 0.8F, 0.8F);
   }

   @Override
   protected void onBoneRenderStart(String boneName, GeoBone bone) {
      if ("slime".equals(boneName)) {
         this.rotationSnapshot = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
         this.scaleSnapshot = new Vector3f(bone.getScaleX(), bone.getScaleY(), bone.getScaleZ());
         this.positionSnapshot = new Vector3f(bone.getPositionX(), bone.getPositionY(), bone.getPositionZ());
      }

      if ("upperBody".equals(boneName)) {
         this.rotationSnapshot2 = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
      }

      if ("torso".equals(boneName)) {
         this.rotationSnapshot3 = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
      }

      if ("head".equals(boneName)) {
         this.rotationSnapshot4 = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
      }

      if ("boobs".equals(boneName)) {
         this.currentRotation = new Vector3f(bone.getRotationX(), bone.getRotationY(), bone.getRotationZ());
      }

      if ("figure".equals(boneName)) {
         bone.setRotationX(this.rotationSnapshot.x);
         bone.setRotationY(this.rotationSnapshot.y);
         bone.setRotationZ(this.rotationSnapshot.z);
         bone.setScaleX(this.scaleSnapshot.x);
         bone.setScaleY(this.scaleSnapshot.y);
         bone.setScaleZ(this.scaleSnapshot.z);
         bone.setPositionX(this.positionSnapshot.x);
         bone.setPositionY(this.positionSnapshot.y);
         bone.setPositionZ(this.positionSnapshot.z);
      }

      if ("dress".equals(boneName)) {
         bone.setRotationX(this.rotationSnapshot2.x);
         bone.setRotationY(this.rotationSnapshot2.y);
         bone.setRotationZ(this.rotationSnapshot2.z);
      }

      if ("hat".equals(boneName)) {
         bone.setRotationX(this.rotationSnapshot4.x);
         bone.setRotationY(this.rotationSnapshot4.y);
         bone.setRotationZ(this.rotationSnapshot4.z);
      }

      if ("boobsSlime".equals(boneName)) {
         bone.setRotationX(this.currentRotation.x);
         bone.setRotationY(this.currentRotation.y);
         bone.setRotationZ(this.currentRotation.z);
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      super.applyBowRotation(isMainHand);
      if (isMainHand) {
         GlStateManager.translate(0.15F, 0.0F, 0.0F);
      } else {
         GlStateManager.translate(-0.02, 0.0, 0.0);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      HashSet blacklisted = super.getBlacklistedBones();
      blacklisted.add("figure");
      return blacklisted;
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (isBlocking && !isMainHand) {
         GlStateManager.translate(-0.025, -0.025, 0.0);
      } else if (!isBlocking && isMainHand) {
         GlStateManager.rotate(120.0F, 0.0F, 1.0F, 0.0F);
      } else {
         if (!isBlocking && !isMainHand) {
            GlStateManager.translate(0.0, 0.4, -0.1);
            GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
         }
      }
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      super.applyItemPostRotation(isMainHand, stack);
      switch (stack.getItem().getItemUseAction(stack)) {
         default:
            GlStateManager.rotate(isMainHand ? 30.0F : 135.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, 0.05, -0.05);
         case BLOCK:
         case BOW:
      }
   }

}
