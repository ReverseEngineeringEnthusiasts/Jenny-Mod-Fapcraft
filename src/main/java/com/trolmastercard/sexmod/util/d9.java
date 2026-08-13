package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.BodyParts;







import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public abstract class d9 extends GirlPlayerRenderer {
   protected static final Vec3i z = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> A = new HashMap<>();

   public d9(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   public static void e_clash190() {
      A.clear();
   }

   protected Vec3i a_clash191(GeoBone var1) {
      String var2 = var1.getName();
      int var3 = var2.hashCode() + this.j.getPersistentID().hashCode();
      Vec3i var4 = A.get(var3);
      if (var4 != null) {
         return var4;
      }

      var4 = this.a_clash192(var2);
      A.put(var3, var4);
      return var4;
   }

   protected abstract Vec3i a_clash192(String var1);

   protected void b(GeoBone var1, int var2) {
      List var3 = var1.childBones;

      for (int var5 = 0; var5 < var3.size(); var5++) {
         GeoBone var6 = (GeoBone)var3.get(var5);
         if (var2 == var5) {
            GeoBone var4 = var6;
            var4.setHidden(false);
            return;
         }
      }
   }

   protected float a_clash193() {
      return 1.0F;
   }

   protected Vec3d a_clash194(ItemStack var1) {
      return new Vec3d(-90.0, 0.0, 0.0);
   }

   protected GeoBone a(GeoBone var1, int var2) {
      List var3 = var1.childBones;
      GeoBone var4 = null;
      var3.sort(Comparator.comparingDouble(GeoBone::getPivotY));

      for (int var5 = 0; var5 < var3.size(); var5++) {
         GeoBone var6 = (GeoBone)var3.get(var5);
         if (var2 == var5) {
            var4 = var6;
            var4.setHidden(false);
         } else {
            var6.setHidden(true);
         }
      }

      return var4;
   }

   protected Vec3i a_clash195(Vec3i var1) {
      return var1;
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if (this.r) {
         if (var7.equals("upperBody")) {
            var2.setRotationX(var2.getRotationX() - 0.5F);
         }

         if (var7.equals("head")) {
            var2.setRotationX(var2.getRotationX() + 0.5F);
         }

         if (var7.equals("legL") || var7.equals("legR")) {
            var2.setPositionZ(var2.getPositionZ() + 1.0F);
         }
      }

      if (var7.equals("head")) {
         this.a(var1, var2, Color.ofRGB(var3, var4, var5));
      }

      this.a(var7, var2);
      this.a(var7, var2, this.w, var1);
      if (this.u && (this.s.func_77973_b() instanceof ItemBow || this.x.func_77973_b() instanceof ItemBow)) {
         if (var7.equals("armR")) {
            var2.setRotationX(var2.getRotationX() - this.j.field_70125_A / 50.0F);
         }

         if (var7.equals("armL")) {
            var2.setRotationY(var2.getRotationY() - this.j.field_70125_A / 50.0F);
         }

         if (this.x.func_77973_b() instanceof ItemBow) {
            ItemStack var8 = this.x;
            this.x = this.s;
            this.s = var8;
         }
      }

      if (this.u && this.s.func_77973_b() instanceof ItemShield) {
         if (var7.equals("armR")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         } else if (this.x.func_77973_b() instanceof ItemShield && var7.equals("armL")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         }
      }

      if (var7.equals("weapon") && !this.s.func_190926_b()) {
         this.a(var1, var2, false);
      }

      if (var7.equals("offhand") && !this.x.func_190926_b()) {
         this.a(var1, var2, true);
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(var2);
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.scale(var2);
      MATRIX_STACK.moveBackFromPivot(var2);
      if ("Head2".equals(var7) && !this.c_clash339()) {
         MATRIX_STACK.pop();
      } else if (("neck".equals(var7) || "head".equals(var7)) && !this.a_clash367()) {
         MATRIX_STACK.pop();
      } else {
         if (!var2.isHidden) {
            Vector4f var17 = this.a(var7, var3, var4, var5);
            var3 = var17.x;
            var4 = var17.y;
            var5 = var17.z;
            double var9 = var17.w;
            if (!this.p.contains(var7)) {
               for (GeoCube var12 : var2.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.func_179094_E();
                  this.q = var2;
                  this.a(var1, var12, var2, var3, var4, var5, var6, var9);
                  GlStateManager.func_179121_F();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone var19 : var2.childBones) {
               if (var9 == 0.0) {
                  this.renderRecursively(var1, var19, var3, var4, var5, var6);
               } else {
                  this.a(var1, var19, var3, var4, var5, var6, (double)var9);
               }
            }
         }

         try {
            MATRIX_STACK.pop();
         } catch (IllegalStateException var13) {
         }
      }
   }

   public void a(BufferBuilder var1, GeoCube var2, GeoBone var3, float var4, float var5, float var6, float var7, double var8) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var13 : var2.quads) {
         if (var13 != null) {
            Vector3f var14 = new Vector3f(var13.normal.func_177958_n(), var13.normal.func_177956_o(), var13.normal.func_177952_p());
            MATRIX_STACK.getNormalMatrix().transform(var14);
            if ((var2.size.y == 0.0F || var2.size.z == 0.0F) && var14.getX() < 0.0F) {
               var14.x *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.z == 0.0F) && var14.getY() < 0.0F) {
               var14.y *= -1.0F;
            }

            if ((var2.size.x == 0.0F || var2.size.y == 0.0F) && var14.getZ() < 0.0F) {
               var14.z *= -1.0F;
            }

            Vec3d var15;
            if (this.c_clash196(var3.getName())) {
               var15 = new Vec3d(var4, var5, var6);
            } else {
               Vec3i var16 = this.a_clash191(var3);
               var16 = this.a_clash195(var16);
               var15 = BodyParts.a(this, var3, new Vec3d(var16.func_177958_n() / 255.0F, var16.func_177956_o() / 255.0F, var16.func_177952_p() / 255.0F), var14);
            }

            for (GeoVertex var19 : var13.vertices) {
               Vector4f var20 = new Vector4f(var19.position.getX(), var19.position.getY(), var19.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var20);
               var1.func_181662_b(var20.getX(), var20.getY(), var20.getZ())
                  .func_187315_a(var19.textureU + var8, var19.textureV)
                  .func_181666_a((float)var15.field_72450_a, (float)var15.field_72448_b, (float)var15.field_72449_c, var7)
                  .func_181663_c(var14.getX(), var14.getY(), var14.getZ())
                  .func_181675_d();
            }
         }
      }
   }

   protected boolean c_clash196(String var1) {
      return var1.startsWith("armor");
   }

   private static IllegalStateException a(IllegalStateException var0) {
      return var0;
   }
}
