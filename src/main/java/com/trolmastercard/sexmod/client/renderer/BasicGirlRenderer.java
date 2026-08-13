package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.BasicGirlEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;







import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class BasicGirlRenderer extends Render<BasicGirlEntity> {

   @Override
   protected ResourceLocation func_110775_a(BasicGirlEntity var1) {
      return this.g;
   }

   static final ResourceLocation g = new ResourceLocation("sexmod", "textures/entity/pyrocinical/standing.png");
   static final ResourceLocation f = new ResourceLocation("sexmod", "textures/entity/pyrocinical/praising.png");
   static final ResourceLocation a = new ResourceLocation("sexmod", "textures/entity/pyrocinical/walking1.png");
   static final ResourceLocation b = new ResourceLocation("sexmod", "textures/entity/pyrocinical/walking2.png");
   static final String e = "textures/entity/pyrocinical/fat/";
   static final int j = 30;
   static final float c = 1.4F;
   static final float h = 0.75F;
   Minecraft d;
   ResourceLocation k = null;
   long i = 0L;

   public BasicGirlRenderer(RenderManager var1) {
      super(var1);
      this.d = Minecraft.func_71410_x();
   }

   @Nullable
   protected ResourceLocation a(BasicGirlEntity var1) {
      return null;
   }

   public void a(BasicGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      GL11.glDisable(2896);
      GlStateManager.func_179141_d();
      GlStateManager.func_179147_l();
      GlStateManager.func_187401_a(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0F, 240.0F);
      EntityPlayerSP var10 = this.d.field_71439_g;
      Vec3d var11 = RotationHelper.a(new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U), var1.func_174791_d(), var9);
      Vec3d var12 = RotationHelper.a(new Vec3d(var10.field_70142_S, var10.field_70137_T, var10.field_70136_U), var10.func_174791_d(), var9);
      Vec3d var13 = var11.func_178788_d(var12);
      ResourceLocation var14 = this.a(var1, Math.abs(var13.field_72450_a) + Math.abs(var13.field_72448_b) + Math.abs(var13.field_72449_c));
      this.d.field_71446_o.func_110577_a(var14);
      GlStateManager.func_179094_E();
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, this.b(var1, var9));
      GlStateManager.func_179137_b(var13.field_72450_a, var13.field_72448_b + this.a_clash808(var14), var13.field_72449_c);
      GlStateManager.func_179114_b(180.0F - this.field_76990_c.field_78735_i, 0.0F, 1.0F, 0.0F);
      float var15 = 1.4F + this.a(var1, var9);
      GlStateManager.func_179152_a(var15, var15, var15);
      Tessellator var16 = Tessellator.func_178181_a();
      BufferBuilder var17 = var16.func_178180_c();
      var17.func_181668_a(7, DefaultVertexFormats.field_181707_g);
      var17.func_181662_b(-1.0, 0.0, 0.0).func_187315_a(0.0, 1.0).func_181675_d();
      var17.func_181662_b(1.0, 0.0, 0.0).func_187315_a(1.0, 1.0).func_181675_d();
      var17.func_181662_b(1.0, 2.0, 0.0).func_187315_a(1.0, 0.0).func_181675_d();
      var17.func_181662_b(-1.0, 2.0, 0.0).func_187315_a(0.0, 0.0).func_181675_d();
      var16.func_78381_a();
      GlStateManager.func_179121_F();
      GL11.glEnable(2896);
      GlStateManager.func_179118_c();
      OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
      long var18 = System.currentTimeMillis();
      if (this.k != f && var14 == f && var18 > this.i + 60000L) {
         this.d.field_71439_g.func_184185_a(SoundHandler.MISC_PYRO[0], 1.0F, 1.0F);
         this.i = var18;
      }

      this.k = var14;
   }

   ResourceLocation a(BasicGirlEntity var1, double var2) {
      if (var1.a != -1) {
         return new ResourceLocation("sexmod", String.format("%s%s.png", "textures/entity/pyrocinical/fat/", this.b(var1)));
      } else if (var2 < 3.0) {
         return f;
      } else {
         Vec3d var4 = new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U).func_178788_d(var1.func_174791_d());
         if (Math.abs(var4.field_72450_a) + Math.abs(var4.field_72448_b) + Math.abs(var4.field_72449_c) == 0.0) {
            return g;
         } else {
            return Math.sin(this.d.field_71439_g.field_70173_aa * 0.75F) > 0.0 ? a : b;
         }
      }
   }

   double a_clash808(ResourceLocation var1) {
      return !a.equals(var1) && !b.equals(var1) ? 0.0 : Math.sin(this.d.field_71439_g.field_70173_aa * 0.75F) * 0.1F;
   }

   int b(BasicGirlEntity var1) {
      return var1.a == -1 ? 0 : (int)ThreadNames.b(this.d.field_71439_g.field_70173_aa - var1.a, 1.0F, 30.0F);
   }

   float a(BasicGirlEntity var1, float var2) {
      if (var1.a == -1) {
         return 0.0F;
      }

      int var3 = this.b(var1);
      return var3 == 30 ? 1.0F : (var3 + var2) / 30.0F;
   }

   float b(BasicGirlEntity var1, float var2) {
      if (var1.a == -1) {
         return 1.0F;
      }

      if (this.d.field_71439_g.field_70173_aa - var1.a > 120) {
         return 0.0F;
      }

      float var4 = ThreadNames.b(this.d.field_71439_g.field_70173_aa - var1.a, 90.0F, 120.0F) - 90.0F;
      float var5 = (var4 + var2) / 30.0F;
      return 1.0F - var5;
   }

}
