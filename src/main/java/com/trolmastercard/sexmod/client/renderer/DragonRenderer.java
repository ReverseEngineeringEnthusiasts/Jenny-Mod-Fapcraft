package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.entity.DragonEntity;
import com.trolmastercard.sexmod.util.RotationHelper;







import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class DragonRenderer extends Render<DragonEntity> {
   public static DragonRenderer a;
   static final UnknownScreen e = new UnknownScreen(0, 255, 251, 255);
   static final UnknownScreen b = new UnknownScreen(255, 0, 236, 255);
   static final UnknownScreen d = new UnknownScreen(255, 255, 255, 0);
   Minecraft c = Minecraft.func_71410_x();

   public DragonRenderer(RenderManager var1) {
      super(var1);
      a = this;
   }

   @Nullable
   protected ResourceLocation func_110775_a(DragonEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/galath/energy_ball.png");
   }

   @Override
   public void func_76986_a(DragonEntity var1, double var2, double var4, double var6, float var8, float var9) {
      GL11.glDisable(2896);
      GlStateManager.func_179141_d();
      GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 0.5F);
      OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, 240.0F, 240.0F);
      EntityPlayerSP var10 = this.c.field_71439_g;
      Vec3d var11 = RotationHelper.a(new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U), var1.func_174791_d(), var9);
      Vec3d var12 = RotationHelper.a(new Vec3d(var10.field_70142_S, var10.field_70137_T, var10.field_70136_U), var10.func_174791_d(), var9);
      Vec3d var13 = var11.func_178788_d(var12);
      GlStateManager.func_179094_E();
      GlStateManager.func_179137_b(var13.field_72450_a, var13.field_72448_b, var13.field_72449_c);
      GlStateManager.func_179114_b(180.0F - this.field_76990_c.field_78735_i, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b((this.field_76990_c.field_78733_k.field_74320_O == 2 ? -1 : 1) * -this.field_76990_c.field_78732_j, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179139_a(var1.g, var1.g, var1.g);
      Tessellator var14 = Tessellator.func_178181_a();
      BufferBuilder var15 = var14.func_178180_c();
      this.c.field_71446_o.func_110577_a(this.func_110775_a(var1));
      UnknownScreen var16;
      UnknownScreen var17;
      if (var1.g == 1.0) {
         float var18 = (float)this.c.field_71441_e.func_82737_E() + this.c.func_184121_ak();
         double var19 = 0.5 * Math.sin(var18 * 0.5) + 0.5;
         var16 = RotationHelper.a(e, b, var19);
         var17 = RotationHelper.a(b, e, var19);
      } else {
         var16 = RotationHelper.a(d, e, var1.g);
         var17 = RotationHelper.a(d, e, var1.g);
      }

      var15.func_181668_a(7, DefaultVertexFormats.field_181709_i);
      this.a(var15, var16, 0.0F);
      var14.func_78381_a();
      var15.func_181668_a(7, DefaultVertexFormats.field_181709_i);
      GlStateManager.func_179152_a(0.75F, 0.75F, 0.75F);
      GlStateManager.func_179109_b(0.0F, 0.075F, 0.0F);
      this.a(var15, var17, 0.001F);
      var14.func_78381_a();
      GlStateManager.func_179121_F();
      GlStateManager.func_179118_c();
      GL11.glEnable(2896);
      OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
   }

   void a(BufferBuilder var1, UnknownScreen var2, float var3) {
      var1.func_181662_b(-0.25, 0.0, var3).func_187315_a(0.0, 0.0).func_181669_b(var2.a, var2.d, var2.c, var2.b).func_181675_d();
      var1.func_181662_b(0.25, 0.0, var3).func_187315_a(1.0, 0.0).func_181669_b(var2.a, var2.d, var2.c, var2.b).func_181675_d();
      var1.func_181662_b(0.25, 0.5, var3).func_187315_a(1.0, 1.0).func_181669_b(var2.a, var2.d, var2.c, var2.b).func_181675_d();
      var1.func_181662_b(-0.25, 0.5, var3).func_187315_a(0.0, 1.0).func_181669_b(var2.a, var2.d, var2.c, var2.b).func_181675_d();
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
