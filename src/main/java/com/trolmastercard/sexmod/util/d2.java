package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class d2 extends GirlPlayerRenderer {
   public d2(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      GlStateManager.func_179114_b(var1 ? 290.0F : 90.0F, 1.0F, 0.0F, 0.0F);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179109_b(0.0F, -0.6F, 0.0F);
      GlStateManager.func_179152_a(0.4F, 0.4F, 0.4F);
   }

   @Override
   protected void a_clash146(boolean var1) {
      super.a_clash146(var1);
      if (var1) {
         GlStateManager.func_179137_b(0.1, 0.0, 0.0);
      }
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      if (var1) {
         GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179109_b(0.0F, -0.14F, -0.17F);
         if (var2) {
            GlStateManager.func_179114_b(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.func_179137_b(0.067, 0.0, 0.0);
         }
      } else if (var2) {
         GlStateManager.func_179114_b(-90.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179114_b(-90.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.func_179109_b(0.0F, 0.165F, 0.0F);
      }
   }

}
