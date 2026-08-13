package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;







import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class AllieModel extends ModelBase implements IVanillaModel {
   private final ModelRenderer a;

   public AllieModel() {
      this.field_78090_t = 16;
      this.field_78089_u = 16;
      this.a = new ModelRenderer(this);
      this.a.func_78793_a(-5.0F, 2.5F, 0.0F);
      this.a.field_78804_l.add(new ModelBox(this.a, 0, 0, -2.0F, -6.0F, 0.0F, 2, 6, 2, 0.0F, false));
   }

   public void func_78088_a(Entity var1, float var2, float var3, float var4, float var5, float var6, float var7) {
      this.a.func_78785_a(var7);
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
