package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Jenny (horny potion).
 */
public class PlayerJennyRenderer extends GirlPlayerRenderer {
   public PlayerJennyRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.25F, 0.0F);
      GlStateManager.scale(0.8F, 0.8F, 0.8F);
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (!isBlocking && !isMainHand) {
         GlStateManager.translate(0.0, -0.1, 0.05);
         GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
      } else if (isBlocking && !isMainHand) {
         GlStateManager.translate(-0.025, -0.1, 0.0);
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      super.applyBowRotation(isMainHand);
      if (isMainHand) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      }
   }

}
