package com.trolmastercard.sexmod.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;

/**
 * Simple vanilla "glass crystal" model: three nested transparent cubes with
 * scale/rotation offsets. Used by {@code DragonStaffRenderer} as the spinning
 * end-crystal on the dragon staff. Renders with the caller-bound texture
 * (e.g. the ender-crystal texture); the rotation parameter spins the crystal.
 */
public class GalathModel extends ModelBase {
   private final ModelRenderer wingModel;
   private final ModelRenderer bodyModel = new ModelRenderer(this, "glass");

   public GalathModel() {
      this.bodyModel.setTextureOffset(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
      this.wingModel = new ModelRenderer(this, "cube");
      this.wingModel.setTextureOffset(32, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8);
   }

   public void render(Entity entity, float limbSwing, float spin, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      GlStateManager.pushMatrix();
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      this.bodyModel.render(scale);
      GlStateManager.scale(0.875F, 0.875F, 0.875F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
      this.bodyModel.render(scale);
      GlStateManager.scale(0.875F, 0.875F, 0.875F);
      GlStateManager.rotate(60.0F, 0.7071F, 0.0F, 0.7071F);
      GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
      this.wingModel.render(scale);
      GlStateManager.popMatrix();
   }
}
