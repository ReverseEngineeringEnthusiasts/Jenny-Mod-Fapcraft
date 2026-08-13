package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.WildSlimeEntity;







import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;

public class WildSlimeFaceLayer implements LayerRenderer<WildSlimeEntity> {
   private final WildSlimeRenderer b;
   private final ModelBase a = new ModelSlime(0);

   public WildSlimeFaceLayer(WildSlimeRenderer var1) {
      this.b = var1;
   }

   public void func_177141_a(WildSlimeEntity var1, float var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      if (!var1.func_82150_aj()) {
         GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.func_179108_z();
         GlStateManager.func_179147_l();
         GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
         this.a.func_178686_a(this.b.func_177087_b());
         this.a.func_78088_a(var1, var2, var3, var5, var6, var7, var8);
         GlStateManager.func_179084_k();
         GlStateManager.func_179133_A();
      }
   }

   public boolean func_177142_b() {
      return true;
   }

}
