package com.trolmastercard.sexmod.client.model;


import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class GoblinModel extends ModelBase {
   private final ModelRenderer a;
   private final ModelRenderer d;
   private final ModelRenderer e;
   private final ModelRenderer c;
   private final ModelRenderer b;

   public GoblinModel() {
      this.textureWidth = 64;
      this.textureHeight = 32;
      this.a = new ModelRenderer(this);
      this.a.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.a.cubeList.add(new ModelBox(this.a, 0, 16, -3.0F, 17.0F, -3.0F, 6, 6, 6, 0.0F, true));
      this.d = new ModelRenderer(this);
      this.d.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.d.cubeList.add(new ModelBox(this.d, 32, 0, 1.3F, 18.0F, -3.5F, 2, 2, 2, 0.0F, true));
      this.e = new ModelRenderer(this);
      this.e.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.e.cubeList.add(new ModelBox(this.e, 32, 4, -3.3F, 18.0F, -3.5F, 2, 2, 2, 0.0F, true));
      this.c = new ModelRenderer(this);
      this.c.setRotationPoint(0.0F, 0.0F, 0.0F);
      this.c.cubeList.add(new ModelBox(this.c, 32, 8, -1.0F, 21.0F, -3.5F, 1, 1, 1, 0.0F, true));
      this.b = new ModelRenderer(this);
      this.b.setRotationPoint(-0.5F, 0.0F, 0.1F);
      ModelRenderer var1 = new ModelRenderer(this);
      var1.setRotationPoint(2.0F, 20.7406F, 4.0504F);
      this.b.addChild(var1);
      this.a(var1, 1.0908F, 0.0F, 0.0F);
      var1.cubeList.add(new ModelBox(var1, 10, 11, -2.5F, 0.0F, 0.0F, 2, 2, 1, 0.0F, false));
      ModelRenderer var2 = new ModelRenderer(this);
      var2.setRotationPoint(2.0F, 19.9214F, 3.4768F);
      this.b.addChild(var2);
      this.a(var2, 0.6109F, 0.0F, 0.0F);
      var2.cubeList.add(new ModelBox(var2, 10, 11, -3.0F, 0.0F, 0.0F, 3, 1, 1, 0.0F, false));
      ModelRenderer var3 = new ModelRenderer(this);
      var3.setRotationPoint(2.0F, 19.0074F, 3.0643F);
      this.b.addChild(var3);
      this.a(var3, 0.3491F, 0.0F, 0.0F);
      var3.cubeList.add(new ModelBox(var3, 10, 11, -4.0F, 0.0F, 0.075F, 5, 1, 1, 0.0F, false));
      ModelRenderer var4 = new ModelRenderer(this);
      var4.setRotationPoint(0.0F, 17.925F, 3.5F);
      this.b.addChild(var4);
      this.a(var4, 0.1309F, 0.0F, 0.0F);
      var4.cubeList.add(new ModelBox(var4, 10, 11, -3.0F, -1.0F, -0.5F, 7, 2, 1, 0.0F, false));
   }

   public void render(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.a.render(var7);
      this.d.render(var7);
      this.e.render(var7);
      this.c.render(var7);
      this.b.render(var7);
   }

   public void a(ModelRenderer var1, float var2, float var3, float var4) {
      var1.rotateAngleX = var2;
      var1.rotateAngleY = var3;
      var1.rotateAngleZ = var4;
   }
}
