package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.GoblinModel;
import com.trolmastercard.sexmod.entity.WildSlimeEntity;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

/**
 * Renderer for the wild slime entity: a vanilla {@link RenderLiving} using the
 * mod's {@link GoblinModel} as the body shape with the vanilla slime texture,
 * squish/scaling driven by the entity's squish factor, plus the
 * {@link WildSlimeFaceLayer} face.
 * <p>
 * CLIENT-side render thread only.
 */
public class WildSlimeRenderer extends RenderLiving<WildSlimeEntity> {
   private static final ResourceLocation slimeTexture = new ResourceLocation("textures/entity/slime/slime.png");

   public WildSlimeRenderer(RenderManager renderManager) {
      super(renderManager, new GoblinModel(), 0.25F);
      this.addLayer(new WildSlimeFaceLayer(this));
   }

   @Override
   public void doRender(WildSlimeEntity slime, double x, double y, double z, float entityYaw, float partialTicks) {
      this.shadowSize = 0.25F * slime.getSquishFactor();
      super.doRender(slime, x, y, z, entityYaw, partialTicks);
   }

   /**
    * Scales the model by the slime's squish formula (interpolated squish
    * factor, vanilla slime deformation) with a near-1.0 base scale.
    */
   @Override
   protected void preRenderCallback(WildSlimeEntity slime, float partialTicks) {
      GlStateManager.scale(0.999F, 0.999F, 0.999F);
      float squishFactor = slime.getSquishFactor();
      float squishScale = (slime.prevSquishFactor + (slime.squishFactor - slime.prevSquishFactor) * partialTicks) / (squishFactor * 0.5F + 1.0F);
      float inverseScale = 1.0F / (squishScale + 1.0F);
      GlStateManager.scale(inverseScale * squishFactor, 1.0F / inverseScale * squishFactor, inverseScale * squishFactor);
   }

   protected ResourceLocation getEntityTexture(WildSlimeEntity slime) {
      return slimeTexture;
   }
}
