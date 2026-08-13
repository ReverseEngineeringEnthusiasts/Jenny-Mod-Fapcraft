package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.GoblinModel;
import com.trolmastercard.sexmod.entity.WildSlimeEntity;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class WildSlimeRenderer extends RenderLiving<WildSlimeEntity> {
   private static final ResourceLocation a = new ResourceLocation("textures/entity/slime/slime.png");

   public WildSlimeRenderer(RenderManager var1) {
      super(var1, new GoblinModel(), 0.25F);
      this.func_177094_a(new WildSlimeFaceLayer(this));
   }

   @Override
   public void func_76986_a(WildSlimeEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.field_76989_e = 0.25F * var1.getSquishFactor();
      super.func_76986_a(var1, var2, var4, var6, var8, var9);
   }

   @Override
   protected void func_77041_b(WildSlimeEntity var1, float var2) {
      GlStateManager.func_179152_a(0.999F, 0.999F, 0.999F);
      float var4 = var1.getSquishFactor();
      float var5 = (var1.h + (var1.e - var1.h) * var2) / (var4 * 0.5F + 1.0F);
      float var6 = 1.0F / (var5 + 1.0F);
      GlStateManager.func_179152_a(var6 * var4, 1.0F / var6 * var4, var6 * var4);
   }

   protected ResourceLocation func_110775_a(WildSlimeEntity var0) {
      return a;
   }
}
