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

/**
 * Base class for NPC-only girl renderers (goblins etc.): extends
 * {@link GirlRenderer} with per-bone coloring from the girl's model code and
 * the custom-bone render pipeline (held items, trade overlay, model-code bone
 * processing).
 * <p>
 * <b>Coloring.</b> {@link #getCachedBoneColor} resolves each bone's tint via
 * {@link #getBoneColor} and caches it keyed by (bone name hash + entity
 * UUID); {@link #clearBoneColors()} resets the cache (e.g. after a model-code
 * change). {@link #tintBoneColor} is the per-frame hook for lighting.
 * <p>
 * <b>Custom-bone pass.</b> {@link #renderCustomBones} recurses the whole
 * skeleton and, per bone: renders the held item on {@code weapon}, the trade
 * overlay on {@code itemRenderer} while the action is {@code PAYMENT}, applies
 * {@link #onBoneProcessing} hooks, then pushes the bone transform and renders
 * cubes (tinted) and children. Skipped entirely in the {@link SexWorldClient}
 * preload world.
 * <p>
 * CLIENT-side render thread only.
 */
public abstract class GirlRendererBase<G extends AbstractNpcOnlyEntity> extends GirlRenderer<G> {
   protected static final Vec3i defaultColor = new Vec3i(255, 255, 255);
   static HashMap<Integer, Vec3i> s = new HashMap<>();

   public GirlRendererBase(RenderManager renderManager, AnimatedGeoModel model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }

   public static void clearBoneColors() {
      s.clear();
   }

   /**
    * Bone tint lookup with per-entity caching (bone name hash + entity UUID);
    * recomputed only when absent. Call after any model-code change must go
    * through {@link #clearBoneColors()}.
    */
   protected Vec3i getCachedBoneColor(GeoBone bone) {
      String boneName = bone.getName();
      int cacheKey = boneName.hashCode() + this.renderEntity.getPersistentID().hashCode();
      Vec3i color = s.get(cacheKey);
      if (color != null) {
         return color;
      }

      color = this.getBoneColor(boneName);
      s.put(cacheKey, color);
      return color;
   }

   protected abstract Vec3i getBoneColor(String boneName);

   /**
    * Renders the girl's held item on the {@code weapon} bone: flushes pending
    * vertices, applies the bone transform, scales by
    * {@link #getDefaultScale()}, rotates by {@link #getItemRenderOffset()},
    * then renders the third-person item and re-binds the entity texture.
    */
   @Override
   protected void renderHeldItem(BufferBuilder buffer, GeoBone bone) {
      ItemStack heldStack = this.resolveHeldItemStack(null);
      float scale = this.getDefaultScale();
      Vec3d rotation = this.getItemRenderOffset(heldStack);
      if (heldStack != null) {
         GlStateManager.pushMatrix();
         Tessellator.getInstance().draw();
         com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
         GL11.glEnable(2896);
         GlStateManager.scale(scale, scale, scale);
         GlStateManager.rotate((float)rotation.x, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate((float)rotation.y, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate((float)rotation.z, 0.0F, 0.0F, 1.0F);
         Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, heldStack, TransformType.THIRD_PERSON_RIGHT_HAND);
         this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         GL11.glDisable(2896);
         GlStateManager.popMatrix();
      }
   }

   protected float getDefaultScale() {
      return 1.0F;
   }

   protected Vec3d getItemRenderOffset(ItemStack stack) {
      return new Vec3d(-90.0, 0.0, 0.0);
   }

   /**
    * Selects one child of a bone by index: sorts children by pivot Y (stable
    * part order), un-hides the chosen one and hides the rest.
    *
    * @return the chosen child bone
    */
   protected static GeoBone getChildBone(GeoBone bone, int index) {
      List children = bone.childBones;
      GeoBone chosen = null;
      children.sort(Comparator.comparingDouble(GeoBone::getPivotY));

      for (int i = 0; i < children.size(); i++) {
         GeoBone child = (GeoBone)children.get(i);
         if (index == i) {
            chosen = child;
            chosen.setHidden(false);
         } else {
            child.setHidden(true);
         }
      }

      return chosen;
   }

   protected Vec3i tintBoneColor(Vec3i color) {
      return color;
   }

   /**
    * Full custom-bone render pass (see class javadoc). Recurses the skeleton,
    * per bone: held item / trade overlay hooks, {@link #onBoneProcessing},
    * bone transform push, cube + child rendering. Skipped for entities living
    * in the {@link SexWorldClient} preload world.
    */
   @Override
   public void renderCustomBones(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a, double overlayAlpha) {
      if (!(this.renderEntity.world instanceof SexWorldClient)) {
         String boneName = bone.getName();
         if (boneName.equals("weapon")) {
            this.renderHeldItem(buffer, bone);
         }

         if (boneName.equals("itemRenderer") && this.renderEntity.getCurrentAction() == Action.PAYMENT) {
            this.renderTradeOverlay(buffer, bone);
         }

         this.onBoneProcessing(buffer, bone.getName(), bone);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(bone);
         MATRIX_STACK.moveToPivot(bone);
         MATRIX_STACK.rotate(bone);
         MATRIX_STACK.scale(bone);
         MATRIX_STACK.moveBackFromPivot(bone);
         if (!bone.isHidden) {
            for (GeoCube cube : bone.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = bone;
               this.renderCubeGeometry(buffer, cube, bone, r, g, b, a, overlayAlpha);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }

            for (GeoBone childBone : bone.childBones) {
               this.renderCustomBones(buffer, childBone, r, g, b, a, overlayAlpha);
            }
         }

         MATRIX_STACK.pop();
      }
   }

   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float a) {
      this.renderCustomBones(buffer, bone, r, g, b, a, 0.0);
   }

   /**
    * Emits a cube's quads with per-vertex tint from the bone color (cached,
    * then light-tinted) and transformed normals (mirrored on zero-size faces).
    * {@code textureVOffset} shifts the texture V coordinate (used by custom-part texture
    * variants).
    */
   public void renderCubeGeometry(BufferBuilder buffer, GeoCube cube, GeoBone bone, float r, float g, float b, float a, double textureVOffset) {
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

            Vec3i boneColor = this.getCachedBoneColor(bone);
            boneColor = this.tintBoneColor(boneColor);
            Vec3d worldColor = BodyParts.getBoneWorldPosition(
               this, bone, new Vec3d(boneColor.getX() / 255.0F, boneColor.getY() / 255.0F, boneColor.getZ() / 255.0F), normal
            );

            for (GeoVertex vertex : quad.vertices) {
               Vector4f matrixPos = new Vector4f(vertex.position.getX(), vertex.position.getY(), vertex.position.getZ(), 1.0F);
               MATRIX_STACK.getModelMatrix().transform(matrixPos);
               buffer.pos(matrixPos.getX(), matrixPos.getY(), matrixPos.getZ())
                  .tex(vertex.textureU + textureVOffset, vertex.textureV)
                  .color((float)worldColor.x, (float)worldColor.y, (float)worldColor.z, a)
                  .normal(normal.getX(), normal.getY(), normal.getZ())
                  .endVertex();
            }
         }
      }
   }

}
