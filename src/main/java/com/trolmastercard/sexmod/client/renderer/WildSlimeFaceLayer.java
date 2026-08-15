package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.WildSlimeEntity;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

/**
 * Vanilla face layer for the wild slime: renders the classic {@link ModelSlime}
 * face quad over the slime body model, inheriting the main model's pose
 * attributes. Hidden while the slime is invisible.
 * <p>
 * CLIENT-side render thread only.
 */
public class WildSlimeFaceLayer implements LayerRenderer<WildSlimeEntity> {
   private final WildSlimeRenderer slimeRenderer;
   private final ModelBase modelBase = new ModelSlime(0);

   public WildSlimeFaceLayer(WildSlimeRenderer var1) {
      this.slimeRenderer = var1;
   }

   /**
    * Renders the slime face with alpha blending over the body, using the
    * renderer's main model attributes for pose consistency. No-op for
    * invisible slimes.
    */
   public void doRenderLayer(WildSlimeEntity var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!var1.isInvisible()) {
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableNormalize();
         GlStateManager.enableBlend();
         GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         this.modelBase.setModelAttributes(this.slimeRenderer.getMainModel());
         this.modelBase.render(var1, var2, var3, var5, var6, var7, var8);
         GlStateManager.disableBlend();
         GlStateManager.disableNormalize();
      }
   }

   public boolean shouldCombineTextures() {
      return true;
   }

}
