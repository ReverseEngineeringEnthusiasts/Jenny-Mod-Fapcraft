package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;







import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class di extends GirlPlayerRenderer {
   float z = 0.0F;

   public di(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179109_b(0.0F, -1.0F, 0.0F);
      GlStateManager.func_179152_a(0.65F, 0.65F, 0.65F);
   }

   @Override
   protected ItemStack a_clash341(@Nullable ItemStack var1) {
      switch (this.j.y_clash492()) {
         case FISHING_IDLE:
         case FISHING_START:
            ItemStack var2 = ((LunaEntity)this.j).ao;
            this.j.func_184611_a(EnumHand.MAIN_HAND, var2);
            return var2;
         default:
            return var1;
      }
   }

   boolean b_clash370() {
      return (Boolean)this.j.func_184212_Q().func_187225_a(BaseGirlEntity.G);
   }

   @Override
   protected void a(String var1, GeoBone var2) {
      if (!Minecraft.func_71410_x().func_147113_T()) {
         switch (var1) {
            case "head":
               this.z = var2.getRotationX();
               break;
            case "backHair":
               if (!this.b_clash370() && this.z > 0.0F) {
                  double var5 = this.z / gc.c_clash744(45.0F);
                  float var7 = (float)RotationHelper.b(0.0, 0.75, var5);
                  var2.setPositionZ(var7);
                  var2.setPositionY(var7);
                  var2.setRotationX(-this.z);
               }
               break;
            case "frontHairL":
            case "frontHairR":
               if (!this.b_clash370()) {
                  var2.setRotationX(-this.z);
               }
         }
      }
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      super.a(var1, var2);
      switch (var2.func_77973_b().func_77661_b(var2)) {
         default:
            GlStateManager.func_179114_b(var1 ? 60.0F : 150.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179137_b(0.0, 0.08, -0.05);
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
      GlStateManager.func_179114_b(var1 ? 60.0F : 150.0F, 1.0F, 0.0F, 0.0F);
      if (var1) {
         GlStateManager.func_179137_b(0.12, 0.0, 0.0);
      }
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (!var1 && var2) {
         GlStateManager.func_179114_b(120.0F, 0.0F, 1.0F, 0.0F);
      } else if (!var1 && !var2) {
         GlStateManager.func_179137_b(0.0, 0.3, -0.15);
         GlStateManager.func_179114_b(-45.0F, 1.0F, 0.0F, 0.0F);
      } else if (var1 && !var2) {
         GlStateManager.func_179137_b(-0.025, -0.05, 0.0);
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
