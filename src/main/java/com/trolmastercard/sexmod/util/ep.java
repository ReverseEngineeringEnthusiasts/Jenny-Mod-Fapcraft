package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.api.b8;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;







import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

public class ep {
   static final int c = 30;
   static final int k = 6;
   static final int f = 6;
   static final float b = 0.15F;
   List<an> g = new ArrayList<>();
   final int a;
   final ar i;
   final b8 d;
   public final BaseGirlEntity e;
   final float j;
   final float h;

   public ep(int var1, ar var2, b8 var3, BaseGirlEntity var4, float var5, float var6) {
      this.a = var1;
      this.i = var2;
      this.d = var3;
      this.e = var4;
      this.j = var5;
      this.h = var6;
   }

   public void a(Minecraft var1, Tessellator var2, BufferBuilder var3, float var4) {
      if (this.g.size() < this.a) {
         for (int var5 = 0; var5 < 6; var5++) {
            Vec3d var6 = this.i.a_clash120(this.e);
            this.g
               .add(
                  new an(
                     var1.field_71441_e,
                     this.d.a_clash24(this.e),
                     new Vec3d(
                        var6.field_72450_a + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j,
                        var6.field_72448_b + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j,
                        var6.field_72449_c + (Reference.f.nextFloat() * 2.0F - 1.0F) * this.j
                     )
                  )
               );
         }
      }

      GlStateManager.func_179129_p();
      GlStateManager.func_179118_c();
      Vec3d var10 = RotationHelper.a(
         new Vec3d(var1.field_71439_g.field_70142_S, var1.field_71439_g.field_70137_T, var1.field_71439_g.field_70136_U),
         var1.field_71439_g.func_174791_d(),
         var4
      );
      var3.func_181668_a(9, DefaultVertexFormats.field_181706_f);
      this.b_clash450();
      Vec3d var11 = null;

      for (an var8 : this.g) {
         Vec3d var9 = RotationHelper.a(var8.d, var8.f, var4);
         if (var11 == null) {
            var11 = var9;
         }

         if (var11.func_72438_d(var9) > this.h) {
            var2.func_78381_a();
            var3.func_181668_a(9, DefaultVertexFormats.field_181706_f);
         }

         var3.func_181662_b(var9.field_72450_a - var10.field_72450_a, var9.field_72448_b - var10.field_72448_b, var9.field_72449_c - var10.field_72449_c)
            .func_181669_b(255, 255, 255, 255)
            .func_181675_d();
         var11 = var9;
      }

      var2.func_78381_a();
      GlStateManager.func_179089_o();
   }

   public void a_clash449() {
      for (an var2 : this.g) {
         var2.a_clash41();
      }
   }

   void b_clash450() {
      if (!this.g.isEmpty() && this.g.size() > 1) {
         for (int var1 = 1; var1 < this.g.size(); var1++) {
            an var2 = this.g.get(var1);
            Vec3d var3 = var2.f;

            int var4;
            for (var4 = var1 - 1; var4 >= 0 && var3.func_72438_d(this.g.get(var4).f) < var3.func_72438_d(this.g.get(var4 + 1).f); var4--) {
               this.g.set(var4 + 1, this.g.get(var4));
            }

            this.g.set(var4 + 1, var2);
         }
      }
   }

}
