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

public class PlayerKoboldRenderer extends AbstractPlayerKoblinGoboldRenderer {
   public PlayerKoboldRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected Vec3i resolveBoneColor(String var1) {
      EntityDataManager var2 = this.renderEntity.getDataManager();
      EyeAndKoboldColor var3 = EyeAndKoboldColor.valueOf((String)var2.get(KoboldEntity.CURRENT_ACTION));
      BlockPos var4 = (BlockPos)var2.get(KoboldEntity.ACTION_TARGET_POS);
      if (KoboldRenderer.hideBones.contains(var1)) {
         return var3.getMainColor();
      } else if (KoboldRenderer.showBones.contains(var1)) {
         return var3.getSecondaryColor();
      } else {
         return (Vec3i)(!"irisR".equals(var1) && !"irisL".equals(var1) ? tintColor : var4);
      }
   }

   @Override
   protected Vector4f calculateBoneArmorColor(String var1, float var2, float var3, float var4) {
      if ("mouth".equals(var1)) {
         String[] var5 = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
         int var6 = Integer.parseInt(var5[7]);
         if (var6 == 1) {
            return new Vector4f(var2, var3, var4, -0.078125F);
         }
      }

      return super.calculateBoneArmorColor(var1, var2, var3, var4);
   }

   @Override
   protected void d_clash331() {
      float var1 = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      GlStateManager.scale(1.0F - var1, 1.0F - var1, 1.0F - var1);
   }

   @Override
   protected void b_clash332() {
      float var1 = 0.25F - (Float)this.renderEntity.getDataManager().get(KoboldPlayerEntity.aA);
      double var2 = 1.0 / (1.0 - var1);
      GlStateManager.scale(var2, var2, var2);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0, -0.8F, 0.05);
      GlStateManager.scale(0.5, 0.5, 0.5);
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      if (var2.getItem().getItemUseAction(var2) == EnumAction.BOW) {
         if (!var1) {
            GlStateManager.rotate(170.0F, 1.0F, 0.0F, 0.0F);
         }

         if (var1) {
            GlStateManager.translate(0.1F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.rotate(var1 ? 80.0F : 180.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (var1) {
         if (var2) {
            GlStateManager.translate(0.06, 0.0, -0.13);
            GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, -0.3F, -0.13);
         }
      } else if (var2) {
         GlStateManager.rotate(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(0.0, -0.35, 0.0);
      } else {
         GlStateManager.translate(0.0, -0.1, -0.083F);
      }
   }

}
