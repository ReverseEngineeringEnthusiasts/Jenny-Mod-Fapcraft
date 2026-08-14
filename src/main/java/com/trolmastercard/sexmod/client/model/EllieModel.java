package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

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

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.rootPart.render(var7);
      this.emptyPart.render(var7);
   }

   public void setRotation(ModelRenderer var1, float var2, float var3, float var4) {
      var1.rotateAngleX = var2;
      var1.rotateAngleY = var3;
      var1.rotateAngleZ = var4;
   }

   @Override
   public ModelRenderer getModel() {
      return this.rootPart;
   }
}
