package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;







import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class EllieModel extends ModelBase implements IVanillaModel {
   private final ModelRenderer a;
   private final ModelRenderer b;
   private final ModelRenderer c;

   public EllieModel() {
      this.textureWidth = 16;
      this.textureHeight = 16;
      this.a = new ModelRenderer(this);
      this.a.setRotationPoint(-5.0F, 1.5708F, 0.0F);
      this.b = new ModelRenderer(this);
      this.b.setRotationPoint(-1.0F, -3.0F, 1.0F);
      this.a.addChild(this.b);
      this.a(this.b, 0.0F, 1.5708F, 0.0F);
      this.b.cubeList.add(new ModelBox(this.b, 0, 0, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F, false));
      this.c = new ModelRenderer(this);
      this.c.setRotationPoint(0.0F, 0.0F, 0.0F);
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.a.render(var7);
      this.c.render(var7);
   }

   public void a(ModelRenderer var1, float var2, float var3, float var4) {
      var1.rotateAngleX = var2;
      var1.rotateAngleY = var3;
      var1.rotateAngleZ = var4;
   }

   @Override
   public ModelRenderer getModel() {
      return this.a;
   }
}
