package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.AllieEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;







import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class d8 extends GirlRenderer {
   public d8(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   public void a(GeoModel var1, BaseGirlEntity var2, float var3, float var4, float var5, float var6, float var7) {
      AllieEntity var8 = (AllieEntity)var2;
      if (var2.y_clash492() != fp.NULL || var2.h_clash508()) {
         var8.U = var8.U == 1.0F ? var8.U : var8.U - 0.01F;
         var7 = var8.U;
         GlStateManager.func_179152_a(var7, var7, var7);
         GlStateManager.func_179109_b(0.0F, var7 == 1.0F ? 0.0F : 3.0F - var7 * 3.0F, 0.0F);
         super.a(var1, var2, var3, var4, var5, var6, var7);
      }
   }

   @Override
   protected void a_clash199(double var1, double var3, double var5) {
      if (this.j.y_clash492() != fp.NULL) {
         if (!this.j.h_clash508()) {
            if (!this.j.y_clash492().hideNameTag) {
               if (i.func_175598_ae().field_78734_h != null) {
                  this.func_147906_a(this.j, this.j.ab_clash540(), var1, var3 + this.j.i_clash226(), var5, 300);
               }
            }
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
