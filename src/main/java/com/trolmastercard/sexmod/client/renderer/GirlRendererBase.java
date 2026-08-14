package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.Action;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoQuad;
import software.bernie.geckolib3.geo.render.built.GeoVertex;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public abstract class GirlRendererBase<G extends AbstractNpcOnlyEntity> extends GirlRenderer<G> {
   protected static final Vec3i defaultColor = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> s = new HashMap<>();

   public GirlRendererBase(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   public static void clearBoneColors() {
      s.clear();
   }

   protected Vec3i getCachedBoneColor(GeoBone var1) {
      String var2 = var1.getName();
      int var3 = var2.hashCode() + this.renderEntity.getPersistentID().hashCode();
      Vec3i var4 = s.get(var3);
      if (var4 != null) {
         return var4;
      }

      var4 = this.getBoneColor(var2);
      s.put(var3, var4);
      return var4;
   }

   protected abstract Vec3i getBoneColor(String var1);

   protected static void b(GeoBone var0, int var1) {
      List var2 = var0.childBones;

      for (int var4 = 0; var4 < var2.size(); var4++) {
         GeoBone var5 = (GeoBone)var2.get(var4);
         if (var1 == var4) {
            GeoBone var3 = var5;
            var3.setHidden(false);
            return;
         }
      }
   }

   @Override
   protected void renderHeldItem(BufferBuilder var1, GeoBone var2) {
      ItemStack var3 = this.resolveHeldItemStack(null);
      float var4 = this.getDefaultScale();
      Vec3d var5 = this.getItemRenderOffset(var3);
      if (var3 != null) {
         GlStateManager.pushMatrix();
         Tessellator.getInstance().draw();
         com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, var2);
         GL11.glEnable(2896);
         GlStateManager.scale(var4, var4, var4);
         GlStateManager.rotate((float)var5.x, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate((float)var5.y, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)var5.z, 0.0F, 0.0F, 1.0F);
         Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, var3, TransformType.THIRD_PERSON_RIGHT_HAND);
         this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         GL11.glDisable(2896);
         GlStateManager.popMatrix();
      }
   }

   protected float getDefaultScale() {
      return 1.0F;
   }

   protected Vec3d getItemRenderOffset(ItemStack var1) {
      return new Vec3d(-90.0, 0.0, 0.0);
   }

   protected static GeoBone a(GeoBone var0, int var1) {
      List var2 = var0.childBones;
      GeoBone var3 = null;
      var2.sort(Comparator.comparingDouble(GeoBone::getPivotY));

      for (int var4 = 0; var4 < var2.size(); var4++) {
         GeoBone var5 = (GeoBone)var2.get(var4);
         if (var1 == var4) {
            var3 = var5;
            var3.setHidden(false);
         } else {
            var5.setHidden(true);
         }
      }

      return var3;
   }

   protected Vec3i tintBoneColor(Vec3i var1) {
      return var1;
   }

   @Override
   public void renderCustomBones(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6, double var7) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String var9 = var2.getName();
         if (var9.equals("weapon")) {
            this.renderHeldItem(var1, var2);
         }

         if (var9.equals("itemRenderer") && this.renderEntity.getCurrentAction() == Action.PAYMENT) {
            this.renderTradeOverlay(var1, var2);
         }

         this.onBoneProcessing(var1, var2.getName(), var2);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!var2.isHidden) {
            for (GeoCube var11 : var2.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = var2;
               this.renderCubeGeometry(var1, var11, var2, var3, var4, var5, var6, var7);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }

            for (GeoBone var13 : var2.childBones) {
               this.renderCustomBones(var1, var13, var3, var4, var5, var6, var7);
            }
         }

         MATRIX_STACK.pop();
      }
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      this.renderCustomBones(var1, var2, var3, var4, var5, var6, 0.0);
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

            Vec3i var15 = this.getCachedBoneColor(var3);
            var15 = this.tintBoneColor(var15);
            Vec3d var16 = BodyParts.getBoneWorldPosition(
               this, var3, new Vec3d(var15.getX() / 255.0F, var15.getY() / 255.0F, var15.getZ() / 255.0F), var14
            );

            for (GeoVertex var20 : var13.vertices) {
               Vector4f var21 = new Vector4f(var20.position.getX(), var20.position.getY(), var20.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(var21);
               var1.pos(var21.getX(), var21.getY(), var21.getZ())
                  .tex(var20.textureU + var8, var20.textureV)
                  .color((float)var16.x, (float)var16.y, (float)var16.z, var7)
                  .normal(var14.getX(), var14.getY(), var14.getZ())
                  .endVertex();
            }
         }
      }
   }

}
