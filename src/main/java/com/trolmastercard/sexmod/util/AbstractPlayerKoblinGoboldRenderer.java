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

public abstract class AbstractPlayerKoblinGoboldRenderer extends GirlPlayerRenderer {
   protected static final Vec3i tintColor = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> A = new HashMap<>();

   public AbstractPlayerKoblinGoboldRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   public static void clearRenderCache() {
      A.clear();
   }

   protected Vec3i getBoneColor(GeoBone var1) {
      String var2 = var1.getName();
      int var3 = var2.hashCode() + this.playerGirl.getPersistentID().hashCode();
      Vec3i var4 = A.get(var3);
      if (var4 != null) {
         return var4;
      }

      var4 = this.resolveBoneColor(var2);
      A.put(var3, var4);
      return var4;
   }

   protected abstract Vec3i resolveBoneColor(String var1);

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

   protected float getDefaultScale() {
      return 1.0F;
   }

   protected Vec3d getItemRenderOffset(ItemStack var1) {
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

   protected Vec3i passThroughColor(Vec3i var1) {
      return var1;
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if (this.isRendering) {
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
         this.renderOverlay(var1, var2, Color.ofRGB(var3, var4, var5));
      }

      this.onBoneRenderStart(var7, var2);
      this.onBoneRenderingLayer(var7, var2, this.playerGirl, var1);
      if (this.isUsingItem && (this.mainhandItem.getItem() instanceof ItemBow || this.offhandItem.getItem() instanceof ItemBow)) {
         if (var7.equals("armR")) {
            var2.setRotationX(var2.getRotationX() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (var7.equals("armL")) {
            var2.setRotationY(var2.getRotationY() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (this.offhandItem.getItem() instanceof ItemBow) {
            ItemStack var8 = this.offhandItem;
            this.offhandItem = this.mainhandItem;
            this.mainhandItem = var8;
         }
      }

      if (this.isUsingItem && this.mainhandItem.getItem() instanceof ItemShield) {
         if (var7.equals("armR")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         } else if (this.offhandItem.getItem() instanceof ItemShield && var7.equals("armL")) {
            var2.setRotationZ(0.0F);
            var2.setRotationX(0.5F);
         }
      }

      if (var7.equals("weapon") && !this.mainhandItem.isEmpty()) {
         this.renderEquippedItem(var1, var2, false);
      }

      if (var7.equals("offhand") && !this.offhandItem.isEmpty()) {
         this.renderEquippedItem(var1, var2, true);
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(var2);
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.scale(var2);
      MATRIX_STACK.moveBackFromPivot(var2);
      if ("Head2".equals(var7) && !this.shouldRenderHead2()) {
         MATRIX_STACK.pop();
      } else if (("neck".equals(var7) || "head".equals(var7)) && !this.shouldRenderFirstPersonHead()) {
         MATRIX_STACK.pop();
      } else {
         if (!var2.isHidden) {
            Vector4f var17 = this.calculateBoneArmorColor(var7, var3, var4, var5);
            var3 = var17.x;
            var4 = var17.y;
            var5 = var17.z;
            double var9 = var17.w;
            if (!this.activeCustomPartBones.contains(var7)) {
               for (GeoCube var12 : var2.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.pushMatrix();
                  this.currentRenderingBone = var2;
                  this.renderCubeGeometry(var1, var12, var2, var3, var4, var5, var6, var9);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone var19 : var2.childBones) {
               if (var9 == 0.0) {
                  this.renderRecursively(var1, var19, var3, var4, var5, var6);
               } else {
                  this.renderCustomBones(var1, var19, var3, var4, var5, var6, (double)var9);
               }
            }
         }

         try {
            MATRIX_STACK.pop();
         } catch (IllegalStateException var13) {
         }
      }
   }

   public void renderCubeGeometry(BufferBuilder var1, GeoCube var2, GeoBone var3, float var4, float var5, float var6, float var7, double var8) {
      MATRIX_STACK.moveToPivot(var2);
      MATRIX_STACK.rotate(var2);
      MATRIX_STACK.moveBackFromPivot(var2);

      for (GeoQuad var13 : var2.quads) {
         if (var13 != null) {
            Vector3f var14 = new Vector3f(var13.normal.getX(), var13.normal.getY(), var13.normal.getZ());
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
            if (this.isBoneBlacklisted(var3.getName())) {
               var15 = new Vec3d(var4, var5, var6);
            } else {
               Vec3i var16 = this.getBoneColor(var3);
               var16 = this.passThroughColor(var16);
               var15 = BodyParts.getBoneWorldPosition(this, var3, new Vec3d(var16.getX() / 255.0F, var16.getY() / 255.0F, var16.getZ() / 255.0F), var14);
            }

            for (GeoVertex var19 : var13.vertices) {
               Vector4f var20 = new Vector4f(var19.position.getX(), var19.position.getY(), var19.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var20);
               var1.pos(var20.getX(), var20.getY(), var20.getZ())
                  .tex(var19.textureU + var8, var19.textureV)
                  .color((float)var15.x, (float)var15.y, (float)var15.z, var7)
                  .normal(var14.getX(), var14.getY(), var14.getZ())
                  .endVertex();
            }
         }
      }
   }

   protected boolean isBoneBlacklisted(String var1) {
      return var1.startsWith("armor");
   }

}
