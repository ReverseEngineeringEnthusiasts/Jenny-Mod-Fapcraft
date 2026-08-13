package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;







import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class KoboldModel extends ModelBase implements IVanillaModel {
   private final ModelRenderer rootPart;

   public KoboldModel() {
      this.textureWidth = 16;
      this.textureHeight = 16;
      this.rootPart = new ModelRenderer(this);
      this.rootPart.setRotationPoint(-5.0F, 2.5F, 0.0F);
      this.rootPart.cubeList.add(new ModelBox(this.rootPart, 0, 0, -2.0F, -6.0F, 0.0F, 2, 6, 2, 0.0F, false));
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.rootPart.render(var7);
   }

   public void a(ModelRenderer var1, float var2, float var3, float var4) {
      var1.rotateAngleX = var2;
      var1.rotateAngleY = var3;
      var1.rotateAngleZ = var4;
   }

   @Override
   public ModelRenderer getModel() {
      return this.rootPart;
   }
}
