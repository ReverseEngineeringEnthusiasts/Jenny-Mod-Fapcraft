package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Luna (horny potion).
 */
public class PlayerLunaRenderer extends GirlPlayerRenderer {
   float rotationZ = 0.0F;

   public PlayerLunaRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.0F, 0.0F);
      GlStateManager.scale(0.65F, 0.65F, 0.65F);
   }

   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack stack) {
      switch (this.renderEntity.getCurrentAction()) {
         case FISHING_IDLE:
         case FISHING_START:
            ItemStack lunaStack = ((LunaEntity)this.renderEntity).ao;
            this.renderEntity.setHeldItem(EnumHand.MAIN_HAND, lunaStack);
            return lunaStack;
         default:
            return stack;
      }
   }

   boolean isLunaAnchored() {
      return (Boolean)this.renderEntity.getDataManager().get(BaseGirlEntity.IS_ANCHORED);
   }

   @Override
   protected void onBoneRenderStart(String boneName, GeoBone bone) {
      if (!Minecraft.getMinecraft().isGamePaused()) {
         switch (boneName) {
            case "head":
               this.rotationZ = bone.getRotationX();
               break;
            case "backHair":
               if (!this.isLunaAnchored() && this.rotationZ > 0.0F) {
                  double t = this.rotationZ / TrigMath.wrapDegrees(45.0F);
                  float pos = (float)RotationHelper.lerpDouble(0.0, 0.75, t);
                  bone.setPositionZ(pos);
                  bone.setPositionY(pos);
                  bone.setRotationX(-this.rotationZ);
               }
               break;
            case "frontHairL":
            case "frontHairR":
               if (!this.isLunaAnchored()) {
                  bone.setRotationX(-this.rotationZ);
               }
         }
      }
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      super.applyItemPostRotation(isMainHand, stack);
      switch (stack.getItem().getItemUseAction(stack)) {
         default:
            GlStateManager.rotate(isMainHand ? 60.0F : 150.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, 0.08, -0.05);
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      GlStateManager.rotate(isMainHand ? 60.0F : 150.0F, 1.0F, 0.0F, 0.0F);
      if (isMainHand) {
         GlStateManager.translate(0.12, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (!isBlocking && isMainHand) {
         GlStateManager.rotate(120.0F, 0.0F, 1.0F, 0.0F);
      } else if (!isBlocking && !isMainHand) {
         GlStateManager.translate(0.0, 0.3, -0.15);
         GlStateManager.rotate(-45.0F, 1.0F, 0.0F, 0.0F);
      } else if (isBlocking && !isMainHand) {
         GlStateManager.translate(-0.025, -0.05, 0.0);
      }
   }

}
