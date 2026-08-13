package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;







import java.util.HashSet;
import javax.vecmath.Vector3f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class d5 extends GirlPlayerRenderer {
   Vector3f A = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f D = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f F = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f E = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f z = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f B = new Vector3f(0.0F, 0.0F, 0.0F);
   Vector3f C = new Vector3f(0.0F, 0.0F, 0.0F);

   public d5(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179109_b(0.0F, -1.25F, 0.0F);
      GlStateManager.func_179152_a(0.8F, 0.8F, 0.8F);
   }

   @Override
   protected void a(String var1, GeoBone var2) {
      if ("slime".equals(var1)) {
         this.F = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
         this.A = new Vector3f(var2.getScaleX(), var2.getScaleY(), var2.getScaleZ());
         this.D = new Vector3f(var2.getPositionX(), var2.getPositionY(), var2.getPositionZ());
      }

      if ("upperBody".equals(var1)) {
         this.B = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("torso".equals(var1)) {
         this.E = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("head".equals(var1)) {
         this.C = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("boobs".equals(var1)) {
         this.z = new Vector3f(var2.getRotationX(), var2.getRotationY(), var2.getRotationZ());
      }

      if ("figure".equals(var1)) {
         var2.setRotationX(this.F.x);
         var2.setRotationY(this.F.y);
         var2.setRotationZ(this.F.z);
         var2.setScaleX(this.A.x);
         var2.setScaleY(this.A.y);
         var2.setScaleZ(this.A.z);
         var2.setPositionX(this.D.x);
         var2.setPositionY(this.D.y);
         var2.setPositionZ(this.D.z);
      }

      if ("dress".equals(var1)) {
         var2.setRotationX(this.B.x);
         var2.setRotationY(this.B.y);
         var2.setRotationZ(this.B.z);
      }

      if ("hat".equals(var1)) {
         var2.setRotationX(this.C.x);
         var2.setRotationY(this.C.y);
         var2.setRotationZ(this.C.z);
      }

      if ("boobsSlime".equals(var1)) {
         var2.setRotationX(this.z.x);
         var2.setRotationY(this.z.y);
         var2.setRotationZ(this.z.z);
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
      super.a_clash146(var1);
      if (var1) {
         GlStateManager.func_179109_b(0.15F, 0.0F, 0.0F);
      } else {
         GlStateManager.func_179137_b(-0.02, 0.0, 0.0);
         GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   public HashSet<String> a() {
      HashSet var1 = super.a();
      var1.add("figure");
      return var1;
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (var1 && !var2) {
         GlStateManager.func_179137_b(-0.025, -0.025, 0.0);
      } else if (!var1 && var2) {
         GlStateManager.func_179114_b(120.0F, 0.0F, 1.0F, 0.0F);
      } else {
         if (!var1 && !var2) {
            GlStateManager.func_179137_b(0.0, 0.4, -0.1);
            GlStateManager.func_179114_b(-30.0F, 1.0F, 0.0F, 0.0F);
         }
      }
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      super.a(var1, var2);
      switch (var2.func_77973_b().func_77661_b(var2)) {
         default:
            GlStateManager.func_179114_b(var1 ? 30.0F : 135.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179137_b(0.0, 0.05, -0.05);
         case BLOCK:
         case BOW:
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
