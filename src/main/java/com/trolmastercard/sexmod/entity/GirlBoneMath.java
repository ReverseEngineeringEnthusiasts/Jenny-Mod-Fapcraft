package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.util.RotationHelper;
import java.util.ArrayList;
import java.util.Collections;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.util.MatrixStack;

/**
 * Client-side bone geometry for a girl: the world-transform matrix of a named
 * bone, the bone offset cache and the girlCam bone height.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}. The entity keeps its
 * methods as delegates, and the overridable hooks
 * ({@code applyAdditionalMatrixTransformations}, {@code transformCameraPivotY},
 * {@code getCachedBoneOffset}) are called back through the girl instance so
 * subclass overrides still dispatch.
 */
final class GirlBoneMath {
   private GirlBoneMath() {
   }

   /**
    * CLIENT: computes the world-transform MatrixStack for a named model bone
    * (walks the bone parent chain, applies the girl's yaw when anchored).
    * @return empty stack if the bone is unknown
    */
   static MatrixStack getBoneMatrixStack(BaseGirlEntity girl, String boneName, boolean usePivots) {
      if (girl.cachedAnimationProcessor == null) {
         girl.cachedAnimationProcessor = girl.getAnimationProcessor();
      }

      IBone bone = girl.cachedAnimationProcessor.getBone(boneName);
      if (bone == null) {
         if (!GirlModel.CAMERA_PLACEMENTS.contains(boneName)) {
            Main.LOGGER.log(Level.WARN, String.format("The bone '%s' does not exist on %s. Bone model matrix couldn't be calculated", boneName, girl.getDisplayNameText()));
            girl.boneTrackingList.remove(boneName);
         }

         return new MatrixStack();
      } else {
         GeoBone currentBone = (GeoBone)bone;
         ArrayList chain = new ArrayList();
         GeoBone bone6 = currentBone;

         while (bone6.parent != null) {
            GeoBone parent = bone6.parent;
            chain.add(parent);
            bone6 = parent;
         }

         Collections.reverse(chain);
         MatrixStack matrixStack = new MatrixStack();
         if (girl.isAnchored()) {
            matrixStack.rotateY((float)(-Math.toRadians(girl.getYawRotation().floatValue())));
         } else if (usePivots) {
            matrixStack.rotateY(
               (float)(-Math.toRadians(RotationHelper.lerp(girl.prevRenderYawOffset, girl.renderYawOffset, Minecraft.getMinecraft().getRenderPartialTicks())))
            );
         }

         for (GeoBone chainBone : (java.util.Collection<GeoBone>) (chain) ) {
            matrixStack.translate(chainBone);
            matrixStack.moveToPivot(chainBone);
            matrixStack.rotate(chainBone);
            matrixStack.scale(chainBone);
            matrixStack.moveBackFromPivot(chainBone);
         }

         matrixStack.translate(currentBone);
         matrixStack.moveToPivot(currentBone);
         matrixStack.rotate(currentBone);
         matrixStack.scale(currentBone);
         return girl.applyAdditionalMatrixTransformations(matrixStack);
      }
   }

   /** @return the cached world offset of a bone, or ZERO (tracking the bone) */
   static Vec3d getCachedBoneOffset(BaseGirlEntity girl, String boneName) {
      Vec3d offset = girl.boneOffsetCache.get(boneName);
      if (offset != null) {
         return offset;
      }

      if (!girl.boneTrackingList.contains(boneName)) {
         girl.boneTrackingList.add(boneName);
      }

      return Vec3d.ZERO;
   }

   /** @return the girl's position plus the cached offset of the named bone */
   static Vec3d getBoneWorldPosition(BaseGirlEntity girl, String boneName) {
      return girl.getCachedBoneOffset(boneName).add(girl.getPositionVector());
   }

   static void setBoneWorldPosition(BaseGirlEntity girl, String boneName, Vec3d pos) {
      girl.boneOffsetCache.put(boneName, pos);
   }

   /** CLIENT: height of the girlCam bone in world units (for camera placement). */
   static float getCameraBoneHeight(BaseGirlEntity girl) {
      AnimationProcessor processor = girl.getAnimationProcessor();
      IBone bone = processor.getBone("girlCam");
      if (bone == null) {
         return 0.0F;
      }

      float pivotY = bone.getPivotY();
      pivotY = girl.transformCameraPivotY(pivotY);
      return pivotY / 16.0F;
   }
}
