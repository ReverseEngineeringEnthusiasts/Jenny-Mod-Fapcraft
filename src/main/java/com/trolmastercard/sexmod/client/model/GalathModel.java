package com.trolmastercard.sexmod.client.model;


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class GalathModel extends ModelBase {
   private final ModelRenderer b;
   private final ModelRenderer a = new ModelRenderer(this, "glass");

   public GalathModel() {
      this.a.func_78784_a(0, 0).func_78789_a(-4.0F, -4.0F, -4.0F, 8, 8, 8);
      this.b = new ModelRenderer(this, "cube");
      this.b.func_78784_a(32, 0).func_78789_a(-4.0F, -4.0F, -4.0F, 8, 8, 8);
   }

   public void func_78088_a(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.func_179094_E();
      GlStateManager.func_179152_a(2.0F, 2.0F, 2.0F);
      GlStateManager.func_179114_b(var3, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(60.0F, 0.7071F, 0.0F, 0.7071F);
      this.a.func_78785_a(var7);
      GlStateManager.func_179152_a(0.875F, 0.875F, 0.875F);
      GlStateManager.func_179114_b(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.func_179114_b(var3, 0.0F, 1.0F, 0.0F);
      this.a.func_78785_a(var7);
      GlStateManager.func_179152_a(0.875F, 0.875F, 0.875F);
      GlStateManager.func_179114_b(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.func_179114_b(var3, 0.0F, 1.0F, 0.0F);
      this.b.func_78785_a(var7);
      GlStateManager.func_179121_F();
   }
}
