package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import java.util.HashSet;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Bia (horny potion).
 */
public class PlayerBiaRenderer extends GirlPlayerRenderer {
   public PlayerBiaRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0, -1.0, -0.05);
      GlStateManager.scale(0.65F, 0.65F, 0.65F);
   }

   @Override
   protected void applyBowRotation(boolean var1) {
      super.applyBowRotation(var1);
      if (var1) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (!var1 && !var2) {
         GlStateManager.translate(0.0, -0.1, 0.05);
         GlStateManager.rotate(40.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
      } else if (var1 && !var2) {
         GlStateManager.translate(-0.025, -0.1, 0.0);
      }
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      return new HashSet<String>() {
         {
            this.add("boobs");
            this.add("booty");
            this.add("vagina");
            this.add("fuckhole");
            this.add("leaf7");
            this.add("leaf8");
         }
      };
   }

}
