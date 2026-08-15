package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Bee (horny potion).
 */
public class PlayerBeeRenderer extends GirlPlayerRenderer {
   public PlayerBeeRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      GlStateManager.rotate(isMainHand ? 290.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -0.6F, 0.0F);
      GlStateManager.scale(0.4F, 0.4F, 0.4F);
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      super.applyBowRotation(isMainHand);
      if (isMainHand) {
         GlStateManager.translate(0.1, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      if (isBlocking) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.translate(0.0F, -0.14F, -0.17F);
         if (isMainHand) {
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.067, 0.0, 0.0);
         }
      } else if (isMainHand) {
         GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0F, 0.165F, 0.0F);
      }
   }

}
