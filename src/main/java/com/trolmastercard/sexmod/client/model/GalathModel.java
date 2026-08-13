package com.trolmastercard.sexmod.client.model;


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

public class GalathModel extends ModelBase {
   private final ModelRenderer b;
   private final ModelRenderer a = new ModelRenderer(this, "glass");

   public GalathModel() {
      this.a.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
      this.b = new ModelRenderer(this, "cube");
      this.b.setTextureOffset(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      GlStateManager.pushMatrix();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      GlStateManager.rotate(var3, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      this.a.render(var7);
      GlStateManager.scale(0.875F, 0.875F, 0.875F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.rotate(var3, 0.0F, 1.0F, 0.0F);
      this.a.render(var7);
      GlStateManager.scale(0.875F, 0.875F, 0.875F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.rotate(var3, 0.0F, 1.0F, 0.0F);
      this.b.render(var7);
      GlStateManager.popMatrix();
   }
}
