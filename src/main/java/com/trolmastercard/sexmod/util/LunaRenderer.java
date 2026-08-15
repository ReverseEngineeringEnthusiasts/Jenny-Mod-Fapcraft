package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

/**
 * Renderer for the Luna NPC.
 */
public class LunaRenderer extends GirlRenderer {
   float rotR;

   public LunaRenderer(RenderManager renderManager, AnimatedGeoModel geoModel, double scaleFactor) {
      super(renderManager, geoModel, scaleFactor);
   }

   @Override
   protected ItemStack resolveHeldItemStack(@Nullable ItemStack stack) {
      switch (this.renderEntity.getCurrentAction()) {
         case FISHING_IDLE:
         case FISHING_START:
            ItemStack lunaStack = ((LunaEntity)this.renderEntity).ao;
            ItemStack cachedStack = (ItemStack)this.renderEntity.getDataManager().get(LunaEntity.az);
            if (cachedStack.equals(ItemStack.EMPTY)) {
               return lunaStack;
            }

            Map enchantments = EnchantmentHelper.getEnchantments(cachedStack);
            EnchantmentHelper.setEnchantments(enchantments, lunaStack);
            this.renderEntity.setHeldItem(EnumHand.MAIN_HAND, lunaStack);
            return lunaStack;
         default:
            return stack;
      }
   }

   boolean isAnchored() {
      return (Boolean)this.renderEntity.getDataManager().get(BaseGirlEntity.IS_ANCHORED);
   }

   @Override
   protected void onBoneProcessing(BufferBuilder buffer, String boneName, GeoBone bone) {
      if (!Minecraft.getMinecraft().isGamePaused()) {
         switch (boneName) {
            case "head":
               this.rotR = bone.getRotationX();
               break;
            case "backHair":
               if (!this.isAnchored()) {
                  double t = this.rotR / TrigMath.wrapDegrees(45.0F);
                  float pos = (float)RotationHelper.lerpDouble(0.0, 0.75, t);
                  bone.setPositionZ(pos);
                  bone.setPositionY(pos);
                  bone.setRotationX(-this.rotR);
               }
               break;
            case "sideHairR":
            case "sideHairL":
               if (this.isAnchored()) {
                  break;
               }

               double t2 = this.rotR / TrigMath.wrapDegrees(45.0F);
               float pos2 = (float)RotationHelper.lerpDouble(0.0, 1.3F, t2);
               bone.setPositionZ(-pos2);
               bone.setPositionY(pos2);
            case "frontHairL":
            case "frontHairR":
               if (!this.isAnchored()) {
                  bone.setRotationX(-this.rotR);
               }
               break;
            case "offhand":
               LunaEntity luna = (LunaEntity)this.renderEntity;
               ItemStack itemStack = (ItemStack)this.renderEntity.getDataManager().get(LunaEntity.ag);
               if (!itemStack.equals(ItemStack.EMPTY) && luna.zFlag == 1.0F) {
                  GlStateManager.pushMatrix();
                  Tessellator.getInstance().draw();
                  com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(IGeoRenderer.MATRIX_STACK, bone);
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                  GlStateManager.scale(luna.aa, luna.aa, luna.aa);
                  Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, itemStack, TransformType.THIRD_PERSON_RIGHT_HAND);
                  GirlRenderer.tempBuffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
                  this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
                  GlStateManager.popMatrix();
               }
         }
      }
   }

}
