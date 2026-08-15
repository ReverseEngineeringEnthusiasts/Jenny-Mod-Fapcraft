package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Vanilla placeholder model (a single 2x6x2 cube) used as the
 * {@link IVanillaModel} shape for the bee NPC's vanilla-model render path.
 */
public class BeeModel extends ModelBase implements IVanillaModel {
   private final ModelRenderer rootPart;

   public BeeModel() {
      this.textureWidth = 16;
      this.textureHeight = 16;
      this.rootPart = new ModelRenderer(this);
      this.rootPart.setRotationPoint(-5.0F, 2.5F, 0.0F);
      this.rootPart.cubeList.add(new ModelBox(this.rootPart, 0, 0, -2.0F, -6.0F, 0.0F, 2, 6, 2, 0.0F, false));
   }

   public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.rootPart.render(scale);
   }

   public void setRotation(ModelRenderer modelRenderer, float x, float y, float z) {
      modelRenderer.rotateAngleX = x;
      modelRenderer.rotateAngleY = y;
      modelRenderer.rotateAngleZ = z;
   }

   @Override
   public ModelRenderer getModel() {
      return this.rootPart;
   }
}
