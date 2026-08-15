package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Vanilla model for the Ellie NPC's vanilla render path: a rotated two-cube
 * placeholder plus an empty part (structure only, no geometry).
 */
public class EllieModel extends ModelBase implements IVanillaModel {
   private final ModelRenderer rootPart;
   private final ModelRenderer childPart;
   private final ModelRenderer emptyPart;

   public EllieModel() {
      this.textureWidth = 16;
      this.textureHeight = 16;
      this.rootPart = new ModelRenderer(this);
      this.rootPart.setRotationPoint(-5.0F, 1.5708F, 0.0F);
      this.childPart = new ModelRenderer(this);
      this.childPart.setRotationPoint(-1.0F, -3.0F, 1.0F);
      this.rootPart.addChild(this.childPart);
      this.setRotation(this.childPart, 0.0F, 1.5708F, 0.0F);
      this.childPart.cubeList.add(new ModelBox(this.childPart, 0, 0, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F, false));
      this.emptyPart = new ModelRenderer(this);
      this.emptyPart.setRotationPoint(0.0F, 0.0F, 0.0F);
   }

   public void render(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
      this.rootPart.render(scale);
      this.emptyPart.render(scale);
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
