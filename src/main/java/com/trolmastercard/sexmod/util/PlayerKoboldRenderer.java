package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.KoboldRenderer;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.KoboldPlayerEntity;
import javax.vecmath.Vector4f;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * <b>Role.</b> Renderer for {@link KoboldPlayerEntity} (a player transformed
 * into a kobold). Subclasses {@link AbstractPlayerKoblinGoboldRenderer} and
 * overrides the bone coloring, eye scaling, item poses and shield-blocking
 * transforms for the kobold body.
 * <p>
 * <b>State.</b> Colors come from {@link EyeAndKoboldColor#valueOf(CURRENT_ACTION)}
 * with {@link KoboldRenderer#hideBones/showBones} toggles; the iris color is
 * the {@code ACTION_TARGET_POS} block position (a debug artifact — the target
 * position doubles as an RGB vector for the eyes).
 */
public class PlayerKoboldRenderer extends AbstractPlayerKoblinGoboldRenderer {
   public PlayerKoboldRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected Vec3i resolveBoneColor(String boneName) {
      EntityDataManager dataManager = this.renderEntity.getDataManager();
      EyeAndKoboldColor color = EyeAndKoboldColor.valueOf((String)dataManager.get(KoboldEntity.CURRENT_ACTION));
      BlockPos irisPos = (BlockPos)dataManager.get(KoboldEntity.ACTION_TARGET_POS);
      if (KoboldRenderer.hideBones.contains(boneName)) {
         return color.getMainColor();
      } else if (KoboldRenderer.showBones.contains(boneName)) {
         return color.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(boneName) && !"irisL".equals(boneName) ? tintColor : irisPos);
      }
   }

   @Override
   protected Vector4f calculateBoneArmorColor(String boneName, float red, float green, float blue) {
      if ("mouth".equals(boneName)) {
         String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
         int eyeIndex = Integer.parseInt(modelCodeParts[7]);
         if (eyeIndex == 1) {
            return new Vector4f(red, green, blue, -0.078125F);
         }
      }

      return super.calculateBoneArmorColor(boneName, red, green, blue);
   }

   @Override
   protected void renderLeftEye() {
      float scale = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      GlStateManager.scale(1.0F - scale, 1.0F - scale, 1.0F - scale);
   }

   @Override
   protected void renderRightEye() {
      float scale = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      double inverseScale = 1.0 / (1.0 - scale);
      GlStateManager.scale(inverseScale, inverseScale, inverseScale);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0, -0.8F, 0.05);
      GlStateManager.scale(0.5, 0.5, 0.5);
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      super.applyItemPostRotation(isMainHand, stack);
      if (stack.getItem().getItemUseAction(stack) == EnumAction.BOW) {
         if (!isMainHand) {
            GlStateManager.rotate(170.0F, 1.0F, 0.0F, 0.0F);
         }

         if (isMainHand) {
            GlStateManager.translate(0.1F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.rotate(isMainHand ? 80.0F : 180.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (isBlocking) {
         if (isMainHand) {
            GlStateManager.translate(0.06, 0.0, -0.13);
            GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, -0.3F, -0.13);
         }
      } else if (isMainHand) {
         GlStateManager.rotate(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0, -0.35, 0.0);
      } else {
         GlStateManager.translate(0.0, -0.1, -0.083F);
      }
   }

}
