package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the Allie NPC.
 */
public class AllieRenderer extends GirlRenderer {
   public AllieRenderer(RenderManager renderManager, AnimatedGeoModel geoModel, double scaleFactor) {
      super(renderManager, geoModel, scaleFactor);
   }

   @Override
   public void renderModel(GeoModel model, BaseGirlEntity entity, float partialTicks, float x, float y, float z, float scale) {
      AllieEntity allie = (AllieEntity)entity;
      if (entity.getCurrentAction() != Action.NULL || entity.isLocallyRegistered()) {
         allie.LAMP_SCALE = allie.LAMP_SCALE == 1.0F ? allie.LAMP_SCALE : allie.LAMP_SCALE - 0.01F;
         scale = allie.LAMP_SCALE;
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.translate(0.0F, scale == 1.0F ? 0.0F : 3.0F - scale * 3.0F, 0.0F);
         super.renderModel(model, entity, partialTicks, x, y, z, scale);
      }
   }

   @Override
   protected void renderNameTag(double x, double y, double z) {
      if (this.renderEntity.getCurrentAction() != Action.NULL) {
         if (!this.renderEntity.isLocallyRegistered()) {
            if (!this.renderEntity.getCurrentAction().hideNameTag) {
               if (mc.getRenderManager().renderViewEntity != null) {
                  this.renderLivingLabel(this.renderEntity, this.renderEntity.getEffectiveDisplayName(), x, y + this.renderEntity.getScaleFactor(), z, 300);
               }
            }
         }
      }
   }

}
