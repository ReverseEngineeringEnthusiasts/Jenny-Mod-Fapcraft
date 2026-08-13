package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class dl extends GirlPlayerRenderer {
   public dl(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179109_b(0.0F, -1.5F, 0.0F);
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      super.a(var1, var2);
      switch (var2.func_77973_b().func_77661_b(var2)) {
         default:
            GlStateManager.func_179114_b(var1 ? 90.0F : 180.0F, 1.0F, 0.0F, 0.0F);
            if (var1) {
               GlStateManager.func_179137_b(0.0, 0.23900000452995301, -0.1F);
            } else {
               GlStateManager.func_179137_b(0.0, 0.1, -0.07);
            }
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
      GlStateManager.func_179114_b(var1 ? 90.0F : 180.0F, 1.0F, 0.0F, 0.0F);
      if (var1) {
         GlStateManager.func_179137_b(0.2, -0.2, 0.0);
      }
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      if (var1) {
         GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
         if (var2) {
            GlStateManager.func_179114_b(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.func_179114_b(90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.func_179114_b(-20.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179109_b(0.4F, 0.0F, 0.228F);
         }
      } else {
         GlStateManager.func_179109_b(0.0F, 0.282F, 0.141F);
         if (var2) {
            GlStateManager.func_179137_b(0.165, -0.45F, 0.0);
            GlStateManager.func_179114_b(-90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179114_b(-90.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.func_179114_b(-27.0F, 0.0F, 1.0F, 0.0F);
         } else {
            GlStateManager.func_179137_b(0.0, 0.0, -0.05);
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
