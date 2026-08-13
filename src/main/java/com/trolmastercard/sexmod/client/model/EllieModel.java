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
      this.field_78090_t = 16;
      this.field_78089_u = 16;
      this.a = new ModelRenderer(this);
      this.a.func_78793_a(-5.0F, 1.5708F, 0.0F);
      this.b = new ModelRenderer(this);
      this.b.func_78793_a(-1.0F, -3.0F, 1.0F);
      this.a.func_78792_a(this.b);
      this.a(this.b, 0.0F, 1.5708F, 0.0F);
      this.b.field_78804_l.add(new ModelBox(this.b, 0, 0, -1.0F, -3.0F, -1.0F, 2, 6, 2, 0.0F, false));
      this.c = new ModelRenderer(this);
      this.c.func_78793_a(0.0F, 0.0F, 0.0F);
   }

   public void func_78088_a(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.a.func_78785_a(var7);
      this.c.func_78785_a(var7);
   }

   public void a(ModelRenderer var1, float var2, float var3, float var4) {
      var1.field_78795_f = var2;
      var1.field_78796_g = var3;
      var1.field_78808_h = var4;
   }

   @Override
   public ModelRenderer getModel() {
      return this.a;
   }
}
