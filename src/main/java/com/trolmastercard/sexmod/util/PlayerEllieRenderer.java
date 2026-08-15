package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Ellie (horny potion).
 */
public class PlayerEllieRenderer extends GirlPlayerRenderer {
   public PlayerEllieRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.5F, 0.0F);
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      switch (var2.getItem().getItemUseAction(var2)) {
         default:
            GlStateManager.rotate(var1 ? 90.0F : 180.0F, 1.0F, 0.0F, 0.0F);
            if (var1) {
               GlStateManager.translate(0.0, 0.23900000452995301, -0.1F);
            } else {
               GlStateManager.translate(0.0, 0.1, -0.07);
            }
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void applyBowRotation(boolean var1) {
      GlStateManager.rotate(var1 ? 90.0F : 180.0F, 1.0F, 0.0F, 0.0F);
      if (var1) {
         GlStateManager.translate(0.2, -0.2, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      if (var1) {
         GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.4F, 0.0F, 0.228F);
         }
      } else {
         GlStateManager.translate(0.0F, 0.282F, 0.141F);
         if (var2) {
            GlStateManager.translate(0.165, -0.45F, 0.0);
            GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(-27.0F, 0.0F, 1.0F, 0.0F);
         } else {
            GlStateManager.translate(0.0, 0.0, -0.05);
         }
      }
   }

}
