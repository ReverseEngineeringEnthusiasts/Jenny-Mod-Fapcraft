package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.LunaEntity;
import com.trolmastercard.sexmod.entity.SexEntity;
import com.trolmastercard.sexmod.util.RotationHelper;







import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SexEntityRenderer extends Render<SexEntity> {
   static final double b = 0.1896224320030116;
   static final double d = -0.5;
   static final double c = 0.08742380916962415;
   private static final ResourceLocation a = new ResourceLocation("textures/particle/particles.png");

   public SexEntityRenderer(RenderManager var1) {
      super(var1);
   }

   public void a(SexEntity var1, double var2, double var4, double var6, float var8, float var9) {
      LunaEntity var10 = var1.g_clash776();
      if (var10 != null && !this.field_188301_f && var10.Z != 1.0F) {
         var10.av = var1;
         ItemStack var11 = (ItemStack)var10.func_184212_Q().func_187225_a(LunaEntity.ag);
         if (!var11.func_77973_b().equals(Items.field_190931_a)) {
            float var12 = Minecraft.func_175610_ah();
            if (var12 == 0.0F) {
               var12 = 0.1F;
            }

            var10.Z += 60.0F / var12 * 0.01666F * 2.0F;
            var10.Z = Math.min(1.0F, var10.Z);
            EntityPlayerSP var13 = Minecraft.func_71410_x().field_71439_g;
            Vec3d var14 = RotationHelper.a(new Vec3d(var13.field_70142_S, var13.field_70137_T, var13.field_70136_U), var13.func_174791_d(), var9);
            Vec3d var15 = new Vec3d(var2, var4, var6);
            Vec3d var16 = RotationHelper.a(
               new Vec3d(var10.field_70142_S, var10.field_70137_T + 0.875, var10.field_70136_U), var10.func_174791_d().func_72441_c(0.0, 0.875, 0.0), var9
            );
            var16 = var16.func_178788_d(var14);
            var15 = RotationHelper.a(var15, var16, var10.Z);
            var2 = var15.field_72450_a;
            var4 = var15.field_72448_b;
            var6 = var15.field_72449_c;
         } else {
            var10.Z = 0.0F;
         }

         GlStateManager.func_179094_E();
         GlStateManager.func_179109_b((float)var2, (float)var4, (float)var6);
         GlStateManager.func_179091_B();
         GlStateManager.func_179152_a(0.5F, 0.5F, 0.5F);
         this.func_180548_c(var1);
         Tessellator var45 = Tessellator.func_178181_a();
         BufferBuilder var46 = var45.func_178180_c();
         GlStateManager.func_179114_b(180.0F - this.field_76990_c.field_78735_i, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b((this.field_76990_c.field_78733_k.field_74320_O == 2 ? -1 : 1) * -this.field_76990_c.field_78732_j, 1.0F, 0.0F, 0.0F);
         if (this.field_188301_f) {
            GlStateManager.func_179142_g();
            GlStateManager.func_187431_e(this.func_188298_c(var1));
         }

         if (!var11.func_77973_b().equals(Items.field_190931_a)) {
            GlStateManager.func_179152_a(2.0F, 2.0F, 2.0F);
            GlStateManager.func_179109_b(0.0F, -0.2F, 0.0F);
            Minecraft.func_71410_x().func_175597_ag().func_178099_a(var10, var11, TransformType.THIRD_PERSON_RIGHT_HAND);
            GlStateManager.func_179109_b(0.0F, 0.2F, 0.0F);
            GlStateManager.func_179152_a(0.5F, 0.5F, 0.5F);
         }

         this.func_180548_c(var1);
         var46.func_181668_a(7, DefaultVertexFormats.field_181710_j);
         var46.func_181662_b(-0.5, -0.5, 0.0).func_187315_a(0.0625, 0.1875).func_181663_c(0.0F, 1.0F, 0.0F).func_181675_d();
         var46.func_181662_b(0.5, -0.5, 0.0).func_187315_a(0.125, 0.1875).func_181663_c(0.0F, 1.0F, 0.0F).func_181675_d();
         var46.func_181662_b(0.5, 0.5, 0.0).func_187315_a(0.125, 0.125).func_181663_c(0.0F, 1.0F, 0.0F).func_181675_d();
         var46.func_181662_b(-0.5, 0.5, 0.0).func_187315_a(0.0625, 0.125).func_181663_c(0.0F, 1.0F, 0.0F).func_181675_d();
         var45.func_78381_a();
         if (this.field_188301_f) {
            GlStateManager.func_187417_n();
            GlStateManager.func_179119_h();
         }

         GlStateManager.func_179101_C();
         GlStateManager.func_179121_F();
         int var47 = var10.func_184591_cq() == EnumHandSide.RIGHT ? 1 : -1;
         ItemStack var49 = var10.func_184614_ca();
         if (!(var49.func_77973_b() instanceof ItemFishingRod)) {
            var47 = -var47;
         }

         var10.field_70177_z = var10.I_clash415();
         var10.field_70761_aq = var10.I_clash415();
         var10.field_70165_t = var10.o_clash501().field_72450_a;
         var10.field_70163_u = var10.o_clash501().field_72448_b;
         var10.field_70161_v = var10.o_clash501().field_72449_c;
         var10.field_70169_q = var10.o_clash501().field_72450_a;
         var10.field_70167_r = var10.o_clash501().field_72448_b;
         var10.field_70166_s = var10.o_clash501().field_72449_c;
         float var51 = (var10.field_70760_ar + (var10.field_70761_aq - var10.field_70760_ar) * var9) * (float) (Math.PI / 180.0);
         double var17 = MathHelper.func_76126_a(var51);
         double var19 = MathHelper.func_76134_b(var51);
         double var21 = var47 * 0.35;
         double var23 = var10.field_70169_q + (var10.field_70165_t - var10.field_70169_q) * var9 - var19 * var21 - var17 * 0.8;
         double var25 = var10.field_70167_r + var10.func_70047_e() + (var10.field_70163_u - var10.field_70167_r) * var9 - 0.45;
         double var27 = var10.field_70166_s + (var10.field_70161_v - var10.field_70166_s) * var9 - var17 * var21 + var19 * 0.8;
         double var29 = var10.func_70093_af() ? -0.1875 : 0.0;
         double var31 = var1.field_70169_q
            + (var1.field_70165_t - var1.field_70169_q) * var9
            - Math.sin((var10.I_clash415() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            - Math.sin(var10.I_clash415().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double var33 = var1.field_70167_r + (var1.field_70163_u - var1.field_70167_r) * var9 + 0.25 + -0.5;
         double var35 = var1.field_70166_s
            + (var1.field_70161_v - var1.field_70166_s) * var9
            + Math.cos((var10.I_clash415() + 90.0F) * (Math.PI / 180.0)) * 0.1896224320030116
            + Math.cos(var10.I_clash415().floatValue() * (Math.PI / 180.0)) * 0.08742380916962415;
         double var37 = (float)(var23 - var31);
         double var39 = (float)(var25 - var33) + var29;
         double var41 = (float)(var27 - var35);
         GlStateManager.func_179090_x();
         GlStateManager.func_179140_f();
         if (var11.func_77973_b().equals(Items.field_190931_a)) {
            var46.func_181668_a(3, DefaultVertexFormats.field_181706_f);

            for (int var43 = 0; var43 <= 16; var43++) {
               float var44 = var43 / 16.0F;
               var46.func_181662_b(var2 + var37 * var44, var4 + var39 * (var44 * var44 + var44) * 0.5 + 0.25, var6 + var41 * var44)
                  .func_181669_b(0, 0, 0, 255)
                  .func_181675_d();
            }

            var45.func_78381_a();
         }

         GlStateManager.func_179145_e();
         GlStateManager.func_179098_w();
         super.func_76986_a(var1, var2, var4, var6, var8, var9);
      }
   }

   @Nullable
   protected ResourceLocation func_110775_a(SexEntity var0) {
      return a;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
