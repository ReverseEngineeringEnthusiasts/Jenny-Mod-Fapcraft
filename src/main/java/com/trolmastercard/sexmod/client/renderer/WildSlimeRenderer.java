package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.GoblinModel;
import com.trolmastercard.sexmod.entity.WildSlimeEntity;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class WildSlimeRenderer extends RenderLiving<WildSlimeEntity> {
   private static final ResourceLocation slimeTexture = new ResourceLocation("textures/entity/slime/slime.png");

   public WildSlimeRenderer(RenderManager var1) {
      super(var1, new GoblinModel(), 0.25F);
      this.addLayer(new WildSlimeFaceLayer(this));
   }

   @Override
   public void doRender(WildSlimeEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.shadowSize = 0.25F * var1.getSquishFactor();
      super.doRender(var1, var2, var4, var6, var8, var9);
   }

   @Override
   protected void preRenderCallback(WildSlimeEntity var1, float var2) {
      GlStateManager.scale(0.999F, 0.999F, 0.999F);
      float var4 = var1.getSquishFactor();
      float var5 = (var1.prevSquishFactor + (var1.squishFactor - var1.prevSquishFactor) * var2) / (var4 * 0.5F + 1.0F);
      float var6 = 1.0F / (var5 + 1.0F);
      GlStateManager.scale(var6 * var4, 1.0F / var6 * var4, var6 * var4);
   }

   protected ResourceLocation getEntityTexture(WildSlimeEntity var0) {
      return slimeTexture;
   }
}
