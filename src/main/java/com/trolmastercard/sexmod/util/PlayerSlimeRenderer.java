package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import java.util.HashSet;
import javax.vecmath.Vector3f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class PlayerSlimeRenderer extends GirlPlayerRenderer {
   Vector3f scaleSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f positionSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot3 = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f currentRotation = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot2 = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f rotationSnapshot4 = new Vector3f(0.0F, 0.0F, 0.0F);

   public PlayerSlimeRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.25F, 0.0F);
      GlStateManager.scale(0.8F, 0.8F, 0.8F);
   }

   @Override
   protected void onBoneRenderStart(String var1, GeoBone var2) {
      if ("slime".equals(var1)) {
         this.rotationSnapshot = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
         this.scaleSnapshot = new Vector3f(var2.getScaleX(), var2.getScaleY(), var2.getScaleZ());
         this.positionSnapshot = new Vector3f(var2.getPositionX(), var2.getPositionY(), var2.getPositionZ());
      }

      if ("upperBody".equals(var1)) {
         this.rotationSnapshot2 = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("torso".equals(var1)) {
         this.rotationSnapshot3 = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("head".equals(var1)) {
         this.rotationSnapshot4 = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("boobs".equals(var1)) {
         this.currentRotation = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("figure".equals(var1)) {
         var2.setRotationX(this.rotationSnapshot.x);
         var2.setRotationY(this.rotationSnapshot.y);
         var2.setRotationZ(this.rotationSnapshot.z);
         var2.setScaleX(this.scaleSnapshot.x);
         var2.setScaleY(this.scaleSnapshot.y);
         var2.setScaleZ(this.scaleSnapshot.z);
         var2.setPositionX(this.positionSnapshot.x);
         var2.setPositionY(this.positionSnapshot.y);
         var2.setPositionZ(this.positionSnapshot.z);
      }

      if ("dress".equals(var1)) {
         var2.setRotationX(this.rotationSnapshot2.x);
         var2.setRotationY(this.rotationSnapshot2.y);
         var2.setRotationZ(this.rotationSnapshot2.z);
      }

      if ("hat".equals(var1)) {
         var2.setRotationX(this.rotationSnapshot4.x);
         var2.setRotationY(this.rotationSnapshot4.y);
         var2.setRotationZ(this.rotationSnapshot4.z);
      }

      if ("boobsSlime".equals(var1)) {
         var2.setRotationX(this.currentRotation.x);
         var2.setRotationY(this.currentRotation.y);
         var2.setRotationZ(this.currentRotation.z);
      }
   }

   @Override
   protected void applyBowRotation(boolean var1) {
      super.applyBowRotation(var1);
      if (var1) {
         GlStateManager.translate(0.15F, 0.0F, 0.0F);
      } else {
         GlStateManager.translate(-0.02, 0.0, 0.0);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      HashSet var1 = super.getBlacklistedBones();
      var1.add("figure");
      return var1;
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (var1 && !var2) {
         GlStateManager.translate(-0.025, -0.025, 0.0);
      } else if (!var1 && var2) {
         GlStateManager.rotate(120.0F, 0.0F, 1.0F, 0.0F);
      } else {
         if (!var1 && !var2) {
            GlStateManager.translate(0.0, 0.4, -0.1);
            GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
         }
      }
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      switch (var2.getItem().getItemUseAction(var2)) {
         default:
            GlStateManager.rotate(var1 ? 30.0F : 135.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, 0.05, -0.05);
         case BLOCK:
         case BOW:
      }
   }

}
