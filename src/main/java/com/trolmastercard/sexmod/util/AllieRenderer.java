package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class AllieRenderer extends GirlRenderer {
   public AllieRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   public void renderModel(GeoModel var1, BaseGirlEntity var2, float var3, float var4, float var5, float var6, float var7) {
      AllieEntity var8 = (AllieEntity)var2;
      if (var2.getCurrentAction() != Action.NULL || var2.isLocallyRegistered()) {
         var8.LAMP_SCALE = var8.LAMP_SCALE == 1.0F ? var8.LAMP_SCALE : var8.LAMP_SCALE - 0.01F;
         var7 = var8.LAMP_SCALE;
         GlStateManager.scale(var7, var7, var7);
         GlStateManager.translate(0.0F, var7 == 1.0F ? 0.0F : 3.0F - var7 * 3.0F, 0.0F);
         super.renderModel(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   @Override
   protected void renderNameTag(double var1, double var3, double var5) {
      if (this.renderEntity.getCurrentAction() != Action.NULL) {
         if (!this.renderEntity.isLocallyRegistered()) {
            if (!this.renderEntity.getCurrentAction().hideNameTag) {
               if (mc.getRenderManager().renderViewEntity != null) {
                  this.renderLivingLabel(this.renderEntity, this.renderEntity.getEffectiveDisplayName(), var1, var3 + this.renderEntity.getScaleFactor(), var5, 300);
               }
            }
         }
      }
   }

}
