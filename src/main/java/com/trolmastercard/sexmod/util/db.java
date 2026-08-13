package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class db extends GirlPlayerRenderer {
   public db(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179109_b(0.0F, -1.25F, 0.0F);
      GlStateManager.func_179152_a(0.8F, 0.8F, 0.8F);
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (!var1 && !var2) {
         GlStateManager.func_179137_b(0.0, -0.1, 0.05);
         GlStateManager.func_179114_b(40.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179114_b(0.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(0.0F, 0.0F, 0.0F, 1.0F);
      } else if (var1 && !var2) {
         GlStateManager.func_179137_b(-0.025, -0.1, 0.0);
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
      super.a_clash146(var1);
      if (var1) {
         GlStateManager.func_179137_b(0.15, 0.0, 0.0);
      }
   }

}
