package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.model.api.IVanillaModel;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;







import java.io.PrintWriter;
import java.io.StringWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class cn {
   Minecraft f;
   float g = 2.0F;
   boolean c = false;
   private static final ResourceLocation e = new ResourceLocation("textures/map/map_background.png");
   IVanillaModel d;
   ResourceLocation h;
   Vec3i b;
   float a = 0.0F;

   @SubscribeEvent
   public void a(RenderSpecificHandEvent var1) {
      AbstractPlayerGirlEntity.C_clash585();
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.d_clash567(Minecraft.func_71410_x().field_71439_g.getPersistentID());
      if (var2 != null) {
         int var3 = var2.ah_clash493();
         this.d = var2.a_clash228(var3);
         this.h = new ResourceLocation("sexmod", var2.c_clash229(var3));
         this.b = var2.b_clash357(var3);
         if (this.d == null) {
            System.out.println("HAND IS NULL uwu did you forget to assign this girl a hand owo?");
         } else {
            this.f = Minecraft.func_71410_x();
            float var4 = 0.0F;
            float var5 = 0.0F;

            try {
               ItemRenderer var6 = this.f.func_175597_ag();
               if (ad.a_clash64()) {
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "prevEquippedProgressMainHand");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "equippedProgressMainHand");
               } else {
                  var4 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "field_187470_g");
                  var5 = (Float)ObfuscationReflectionHelper.getPrivateValue(ItemRenderer.class, var6, "field_187469_f");
               }

               this.g = 2.0F - (var4 + (var5 - var4) * var1.getPartialTicks());
            } catch (Exception var9) {
               System.out.println("couldnt do the reflection thingy");
               StringWriter var7 = new StringWriter();
               var9.printStackTrace(new PrintWriter(var7));
               Minecraft.func_71410_x().field_71439_g.func_71165_d(var7.toString());
            }

            EntityPlayerSP var10 = this.f.field_71439_g;
            float var11 = var10.func_70678_g(var1.getPartialTicks());
            ItemStack var8 = this.f.field_71439_g.func_184614_ca();
            GlStateManager.func_179124_c(this.b.func_177958_n() / 255.0F, this.b.func_177956_o() / 255.0F, this.b.func_177952_p() / 255.0F);
            if (var1.getHand() == EnumHand.MAIN_HAND) {
               if (var8.func_190926_b() || var8.func_77973_b() instanceof ItemMap) {
                  var1.setCanceled(true);
                  this.a(var8, var1.getPartialTicks(), var10, this.g, var11);
                  this.c = true;
               } else if (var5 < var4) {
                  if (this.c) {
                     var1.setCanceled(true);
                     this.a(var8, var1.getPartialTicks(), var10, this.g, var11);
                  }
               } else {
                  this.c = false;
               }
            } else if (this.f.field_71439_g.func_184592_cb().func_77973_b() instanceof ItemMap) {
               var1.setCanceled(true);
               this.a(EnumHandSide.LEFT, this.g - 1.0F, var11, this.f.field_71439_g.func_184592_cb());
            }

            GlStateManager.func_179117_G();
         }
      }
   }

   void a(ItemStack var1, float var2, AbstractClientPlayer var3, float var4, float var5) {
      if (var1.func_77973_b() instanceof ItemMap) {
         if (var3.func_184592_cb().func_190926_b()) {
            this.a(var1, var3, var5, var2);
         } else {
            this.a(EnumHandSide.RIGHT, var4 - 1.0F, var5, var1);
         }
      } else {
         this.a_clash297(var5, var2);
      }
   }

   void a(EnumHandSide var1, float var2, float var3, ItemStack var4) {
      float var5 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.func_179109_b(var5 * 0.125F, -0.125F, 0.0F);
      if (!this.f.field_71439_g.func_82150_aj()) {
         GlStateManager.func_179094_E();
         GlStateManager.func_179114_b(var5 * 10.0F, 0.0F, 0.0F, 1.0F);
         this.a(var2, var3, var1);
         GlStateManager.func_179109_b(-0.5F, -1.1F, 0.0F);
         if (var1 == EnumHandSide.RIGHT) {
            GlStateManager.func_179109_b(0.48F, 0.15F, 0.0F);
         } else {
            GlStateManager.func_179109_b(0.44F, 1.3F, 1.0F);
         }

         Minecraft.func_71410_x().func_110434_K().func_110577_a(this.h);
         this.d.a_clash17().func_78785_a(0.175F);
         GlStateManager.func_179121_F();
      }

      GlStateManager.func_179094_E();
      GlStateManager.func_179109_b(var5 * 0.51F, -0.08F + var2 * -1.2F, -0.75F);
      float var6 = MathHelper.func_76129_c(var3);
      float var7 = MathHelper.func_76126_a(var6 * (float) Math.PI);
      float var8 = -0.5F * var7;
      float var9 = 0.4F * MathHelper.func_76126_a(var6 * (float) (Math.PI * 2));
      float var10 = -0.3F * MathHelper.func_76126_a(var3 * (float) Math.PI);
      GlStateManager.func_179109_b(var5 * var8, var9 - 0.3F * var7, var10);
      GlStateManager.func_179114_b(var7 * -45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179114_b(var5 * var7 * -30.0F, 0.0F, 1.0F, 0.0F);
      this.a_clash295(var4);
      GlStateManager.func_179121_F();
   }

   void a(ItemStack var1, AbstractClientPlayer var2, float var3, float var4) {
      float var5 = var2.field_70127_C + (var2.field_70125_A - var2.field_70127_C) * var4;
      float var6 = MathHelper.func_76129_c(var3);
      float var7 = -0.2F * MathHelper.func_76126_a(var3 * (float) Math.PI);
      float var8 = -0.4F * MathHelper.func_76126_a(var6 * (float) Math.PI);
      GlStateManager.func_179109_b(0.0F, -var7 / 2.0F, var8);
      float var9 = this.a_clash296(var5);
      GlStateManager.func_179109_b(0.0F, 0.04F + (this.g - 1.0F) * -1.2F + var9 * -0.5F, -0.72F);
      GlStateManager.func_179114_b(var9 * -85.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179129_p();
      GlStateManager.func_179094_E();
      GlStateManager.func_179114_b(90.0F, 0.0F, 1.0F, 0.0F);
      this.a(EnumHandSide.RIGHT);
      this.a(EnumHandSide.LEFT);
      GlStateManager.func_179121_F();
      GlStateManager.func_179089_o();
      float var10 = MathHelper.func_76126_a(var6 * (float) Math.PI);
      GlStateManager.func_179114_b(var10 * 20.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179152_a(2.0F, 2.0F, 2.0F);
      this.a_clash295(var1);
      GlStateManager.func_179145_e();
   }

   void a_clash295(ItemStack var1) {
      GlStateManager.func_179117_G();
      GlStateManager.func_179114_b(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179152_a(0.38F, 0.38F, 0.38F);
      GlStateManager.func_179140_f();
      this.f.func_110434_K().func_110577_a(e);
      Tessellator var2 = Tessellator.func_178181_a();
      BufferBuilder var3 = var2.func_178180_c();
      GlStateManager.func_179109_b(-0.5F, -0.5F, 0.0F);
      GlStateManager.func_179152_a(0.0078125F, 0.0078125F, 0.0078125F);
      var3.func_181668_a(7, DefaultVertexFormats.field_181707_g);
      var3.func_181662_b(-7.0, 135.0, 0.0).func_187315_a(0.0, 1.0).func_181675_d();
      var3.func_181662_b(135.0, 135.0, 0.0).func_187315_a(1.0, 1.0).func_181675_d();
      var3.func_181662_b(135.0, -7.0, 0.0).func_187315_a(1.0, 0.0).func_181675_d();
      var3.func_181662_b(-7.0, -7.0, 0.0).func_187315_a(0.0, 0.0).func_181675_d();
      var2.func_78381_a();
      MapData var4 = ((ItemMap)var1.func_77973_b()).func_77873_a(var1, this.f.field_71441_e);
      if (var4 != null) {
         this.f.field_71460_t.func_147701_i().func_148250_a(var4, false);
      }

      GlStateManager.func_179124_c(this.b.func_177958_n() / 255.0F, this.b.func_177956_o() / 255.0F, this.b.func_177952_p() / 255.0F);
   }

   private void a(EnumHandSide var1) {
      GlStateManager.func_179094_E();
      float var2 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.func_179114_b(92.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179114_b(var2 * -41.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179109_b(var2 * 0.3F, -1.1F, 0.45F);
      if (var1 == EnumHandSide.RIGHT) {
         GlStateManager.func_179109_b(0.63F, 0.36F, 0.0F);
      } else {
         GlStateManager.func_179109_b(1.6F, 0.35F, 0.0F);
      }

      Minecraft.func_71410_x().func_110434_K().func_110577_a(this.h);
      this.d.a_clash17().func_78785_a(0.175F);
      GlStateManager.func_179121_F();
   }

   private float a_clash296(float var1) {
      float var2 = 1.0F - var1 / 45.0F + 0.1F;
      var2 = MathHelper.func_76131_a(var2, 0.0F, 1.0F);
      return -MathHelper.func_76134_b(var2 * (float) Math.PI) * 0.5F + 0.5F;
   }

   void a_clash297(float var1, float var2) {
      GlStateManager.func_179129_p();
      GlStateManager.func_179094_E();
      this.a(this.g, var1, EnumHandSide.RIGHT);
      Minecraft.func_71410_x().func_110434_K().func_110577_a(this.h);
      this.d.a_clash17().func_78785_a(0.175F);
      GlStateManager.func_179084_k();
      GlStateManager.func_179089_o();
      GlStateManager.func_179121_F();
   }

   private void a(float var1, float var2, EnumHandSide var3) {
      boolean var4 = var3 != EnumHandSide.LEFT;
      float var5 = var4 ? 1.0F : -1.0F;
      float var6 = MathHelper.func_76129_c(var2);
      float var7 = -0.3F * MathHelper.func_76126_a(var6 * (float) Math.PI);
      float var8 = 0.4F * MathHelper.func_76126_a(var6 * (float) (Math.PI * 2));
      float var9 = -0.4F * MathHelper.func_76126_a(var2 * (float) Math.PI);
      GlStateManager.func_179109_b(var5 * (var7 + 0.64000005F), var8 + -0.6F + var1 * -0.6F, var9 + -0.71999997F);
      GlStateManager.func_179114_b(var5 * 45.0F, 0.0F, 1.0F, 0.0F);
      float var10 = MathHelper.func_76126_a(var2 * var2 * (float) Math.PI);
      float var11 = MathHelper.func_76126_a(var6 * (float) Math.PI);
      GlStateManager.func_179114_b(var5 * var11 * 70.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179114_b(var5 * var10 * -20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179109_b(var5 * -1.0F, 3.6F, 3.5F);
      GlStateManager.func_179114_b(var5 * 120.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.func_179114_b(200.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.func_179114_b(var5 * -135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.func_179109_b(var5 * 5.6F, 0.0F, 0.0F);
      GlStateManager.func_179109_b(0.5F, 1.1F, 0.0F);
   }

   private static Exception a(Exception var0) {
      return var0;
   }
}
