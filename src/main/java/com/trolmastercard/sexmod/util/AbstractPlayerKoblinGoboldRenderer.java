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

/**
 * Base geckolib renderer for player-form kobolds/goblins.
 */
public abstract class AbstractPlayerKoblinGoboldRenderer extends GirlPlayerRenderer {
   protected static final Vec3i tintColor = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> A = new HashMap<>();

   public AbstractPlayerKoblinGoboldRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   public static void clearRenderCache() {
      A.clear();
   }

   protected Vec3i getBoneColor(GeoBone bone) {
      String boneName = bone.getName();
      int cacheKey = boneName.hashCode() + this.playerGirl.getPersistentID().hashCode();
      Vec3i color = A.get(cacheKey);
      if (color != null) {
         return color;
      }

      color = this.resolveBoneColor(boneName);
      A.put(cacheKey, color);
      return color;
   }

   protected abstract Vec3i resolveBoneColor(String boneName);

   protected void hideBone(GeoBone bone, int index) {
      List childBones = bone.childBones;

      for (int i = 0; i < childBones.size(); i++) {
         GeoBone childBone = (GeoBone)childBones.get(i);
         if (index == i) {
            GeoBone hiddenBone = childBone;
            hiddenBone.setHidden(false);
            return;
         }
      }
   }

   protected float getDefaultScale() {
      return 1.0F;
   }

   protected Vec3d getItemRenderOffset(ItemStack stack) {
      return new Vec3d(-90.0, 0.0, 0.0);
   }

   protected GeoBone getChildBone(GeoBone bone, int index) {
      List childBones = bone.childBones;
      GeoBone result = null;
      childBones.sort(Comparator.comparingDouble(GeoBone::getPivotY));

      for (int i = 0; i < childBones.size(); i++) {
         GeoBone childBone = (GeoBone)childBones.get(i);
         if (index == i) {
            result = childBone;
            result.setHidden(false);
         } else {
            childBone.setHidden(true);
         }
      }

      return result;
   }

   protected Vec3i passThroughColor(Vec3i color) {
      return color;
   }

   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float red, float green, float blue, float partialTicks) {
      String boneName = bone.getName();
      if (this.isRendering) {
         if (boneName.equals("upperBody")) {
            bone.setRotationX(bone.getRotationX() - 0.5F);
         }

         if (boneName.equals("head")) {
            bone.setRotationX(bone.getRotationX() + 0.5F);
         }

         if (boneName.equals("legL") || boneName.equals("legR")) {
            bone.setPositionZ(bone.getPositionZ() + 1.0F);
         }
      }

      if (boneName.equals("head")) {
         this.renderOverlay(buffer, bone, Color.ofRGB(red, green, blue));
      }

      this.onBoneRenderStart(boneName, bone);
      this.onBoneRenderingLayer(boneName, bone, this.playerGirl, buffer);
      if (this.isUsingItem && (this.mainhandItem.getItem() instanceof ItemBow || this.offhandItem.getItem() instanceof ItemBow)) {
         if (boneName.equals("armR")) {
            bone.setRotationX(bone.getRotationX() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (boneName.equals("armL")) {
            bone.setRotationY(bone.getRotationY() - this.playerGirl.rotationPitch / 50.0F);
         }

         if (this.offhandItem.getItem() instanceof ItemBow) {
            ItemStack offhandItem = this.offhandItem;
            this.offhandItem = this.mainhandItem;
            this.mainhandItem = offhandItem;
         }
      }

      if (this.isUsingItem && this.mainhandItem.getItem() instanceof ItemShield) {
         if (boneName.equals("armR")) {
            bone.setRotationZ(0.0F);
            bone.setRotationX(0.5F);
         } else if (this.offhandItem.getItem() instanceof ItemShield && boneName.equals("armL")) {
            bone.setRotationZ(0.0F);
            bone.setRotationX(0.5F);
         }
      }

      if (boneName.equals("weapon") && !this.mainhandItem.isEmpty()) {
         this.renderEquippedItem(buffer, bone, false);
      }

      if (boneName.equals("offhand") && !this.offhandItem.isEmpty()) {
         this.renderEquippedItem(buffer, bone, true);
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(bone);
      MATRIX_STACK.moveToPivot(bone);
      MATRIX_STACK.rotate(bone);
      MATRIX_STACK.scale(bone);
      MATRIX_STACK.moveBackFromPivot(bone);
      if ("Head2".equals(boneName) && !this.shouldRenderHead2()) {
         MATRIX_STACK.pop();
      } else if (("neck".equals(boneName) || "head".equals(boneName)) && !this.shouldRenderFirstPersonHead()) {
         MATRIX_STACK.pop();
      } else {
         if (!bone.isHidden) {
            Vector4f armorColor = this.calculateBoneArmorColor(boneName, red, green, blue);
            red = armorColor.x;
            green = armorColor.y;
            blue = armorColor.z;
            double alpha = armorColor.w;
            if (!this.activeCustomPartBones.contains(boneName)) {
               for (GeoCube cube : bone.childCubes) {
                  MATRIX_STACK.push();
                  GlStateManager.pushMatrix();
                  this.currentRenderingBone = bone;
                  this.renderCubeGeometry(buffer, cube, bone, red, green, blue, partialTicks, alpha);
                  GlStateManager.popMatrix();
                  MATRIX_STACK.pop();
               }
            }

            for (GeoBone childBone : bone.childBones) {
               if (alpha == 0.0) {
                  this.renderRecursively(buffer, childBone, red, green, blue, partialTicks);
               } else {
                  this.renderCustomBones(buffer, childBone, red, green, blue, partialTicks, (double)alpha);
               }
            }
         }

         try {
            MATRIX_STACK.pop();
         } catch (IllegalStateException exception) {
         }
      }
   }

   public void renderCubeGeometry(BufferBuilder buffer, GeoCube cube, GeoBone bone, float red, float green, float blue, float partialTicks, double alpha) {
      MATRIX_STACK.moveToPivot(cube);
      MATRIX_STACK.rotate(cube);
      MATRIX_STACK.moveBackFromPivot(cube);

      for (GeoQuad quad : cube.quads) {
         if (quad != null) {
            Vector3f normal = new Vector3f(quad.normal.getX(), quad.normal.getY(), quad.normal.getZ());
            MATRIX_STACK.getNormalMatrix().transform(normal);
            if ((cube.size.y == 0.0F || cube.size.z == 0.0F) && normal.getX() < 0.0F) {
               normal.x *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.z == 0.0F) && normal.getY() < 0.0F) {
               normal.y *= -1.0F;
            }

            if ((cube.size.x == 0.0F || cube.size.y == 0.0F) && normal.getZ() < 0.0F) {
               normal.z *= -1.0F;
            }

            Vec3d colorVec;
            if (this.isBoneBlacklisted(bone.getName())) {
               colorVec = new Vec3d(red, green, blue);
            } else {
               Vec3i boneColor = this.getBoneColor(bone);
               boneColor = this.passThroughColor(boneColor);
               colorVec = BodyParts.getBoneWorldPosition(this, bone, new Vec3d(boneColor.getX() / 255.0F, boneColor.getY() / 255.0F, boneColor.getZ() / 255.0F), normal);
            }

            for (GeoVertex vertex : quad.vertices) {
               Vector4f transformedVertex = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(transformedVertex);
               buffer.pos(transformedVertex.getX(), transformedVertex.getY(), transformedVertex.getZ())
                  .tex(vertex.textureU + alpha, vertex.textureV)
                  .color((float)colorVec.x, (float)colorVec.y, (float)colorVec.z, partialTicks)
                  .normal(normal.getX(), normal.getY(), normal.getZ())
                  .endVertex();
            }
         }
      }
   }

   protected boolean isBoneBlacklisted(String boneName) {
      return boneName.startsWith("armor");
   }

}
