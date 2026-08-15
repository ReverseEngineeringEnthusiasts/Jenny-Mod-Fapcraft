package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.renderer.GalathRenderer;
import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.api.IGalath;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * <b>Role.</b> Renderer for the player-controlled Galath (owner's own galath
 * when transformed / summoned). Extends {@link GirlPlayerRenderer} with galath
 * specifics: a bone blacklist so armor/clothing overlays don't double-draw,
 * per-armor overlay colors (gold/iron/leather dye), the dash point-of-view
 * overlay, and a two-pass body/steve-bone render with separate textures.
 * <p>
 * <b>Pitfalls.</b> {@link #getBlacklistedBones()} mutates the shared
 * {@code GalathRenderer.BLACKLISTED_BONES} set — repeated calls must stay
 * idempotent. {@link #doRenderEntity} skips the dash POV only for the owner in
 * first person; anchored girls always render it.
 */
public class PlayerGalathRenderer extends GirlPlayerRenderer {
   static final HashSet<String> blacklistBones = new HashSet<>(
      Arrays.asList(
         "kneeL",
         "kneeR",
         "shinL",
         "shinR",
         "armorHelmet",
         "sockL",
         "sockR",
         "braBoobL",
         "braBoobR",
         "armorNippleR",
         "armorNippleL",
         "slip",
         "turnable",
         "static"
      )
   );

   public PlayerGalathRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Nullable
   @Override
   protected Vector3fSexmodSpecial getAdditionalOverlayColor(BaseGirlEntity girl) {
      if (girl.world instanceof SexWorldClient) {
         return null;
      } else {
         return ((IGalath)girl).isWingsAnimated() ? null : GalathRenderer.ZERO_OFFSET;
      }
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      GalathRenderer.BLACKLISTED_BONES.addAll(BodyParts.CUSTOM_PART_BONES);
      return GalathRenderer.BLACKLISTED_BONES;
   }

   @Override
   protected void drawOverlayLines(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial offset, float alpha) {
      renderGirlTint(tessellator, buffer, girl, offset, alpha);
   }

   @Override
   public void doRenderEntity(BaseGirlEntity entity, double x, double y, double z, float yaw, float partialTicks) {
      super.doRenderEntity(entity, x, y, z, yaw, partialTicks);
      if (mc.gameSettings.thirdPersonView != 0 || !mc.player.getPersistentID().equals(((AbstractPlayerGirlEntity)entity).getOwnerUserUUID()) || entity.isAnchored()) {
         GalathRenderer.renderDashPov(entity, partialTicks);
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      super.applyBowRotation(isMainHand);
      if (isMainHand) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (isBlocking) {
         GlStateManager.translate(0.0, -0.05, -0.05);
         GlStateManager.rotate(15.0F, 1.0F, 0.0F, 0.0F);
         if (isMainHand) {
            GlStateManager.translate(0.3, 0.2, 0.0);
            GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);
         }
      } else {
         GlStateManager.translate(0.0, 0.0, 0.1);
         GlStateManager.rotate(30.0F, 1.0F, 0.0F, 0.0F);
         if (isMainHand) {
            GlStateManager.rotate(-29.0F, 1.0F, 0.0F, 0.0F);
         }
      }
   }

   @Override
   protected Vector4f calculateBoneArmorColor(String boneName, float red, float green, float blue) {
      if (!blacklistBones.contains(boneName)) {
         return this.createOverlayColor(red, green, blue);
      }

      if ("armorHelmet".equals(boneName)) {
         return super.calculateBoneArmorColor(boneName, red, green, blue);
      }

      ItemStack stack = ItemStack.EMPTY;
      switch (boneName) {
         case "braBoobL":
         case "braBoobR":
         case "armorNippleR":
         case "armorNippleL":
            stack = (ItemStack)this.renderEntity.getDataManager().get(AbstractGirlNpcEntity.CHEST_SLOT);
            break;
         case "turnable":
         case "static":
         case "slip":
            stack = (ItemStack)this.renderEntity.getDataManager().get(AbstractGirlNpcEntity.LEGS_SLOT);
            break;
         case "shinL":
         case "shinR":
         case "sockL":
         case "sockR":
         case "kneeL":
         case "kneeR":
            stack = (ItemStack)this.renderEntity.getDataManager().get(AbstractGirlNpcEntity.BOOTS_SLOT);
      }

      if (!(stack.getItem() instanceof ItemArmor)) {
         return this.createOverlayColor(red, green, blue);
      }

      ItemArmor armor = (ItemArmor)stack.getItem();
      switch (armor.getArmorMaterial()) {
         case GOLD:
            return new Vector4f(red, green, blue, -0.15625F);
         case IRON:
         case CHAIN:
            return new Vector4f(red, green, blue, -0.125F);
         case LEATHER:
            int colorInt = armor.getColor(stack);
            float colorRed = (colorInt >> 16 & 0xFF) / 255.0F;
            float colorGreen = (colorInt >> 8 & 0xFF) / 255.0F;
            float colorBlue = (colorInt & 0xFF) / 255.0F;
            red *= colorRed;
            green *= colorGreen;
            blue *= colorBlue;
            return new Vector4f(red, green, blue, -0.09375F);
         default:
            return new Vector4f(red, green, blue, -0.1875F);
      }
   }

   @Override
   protected void renderModelBuffer(GeoModel model, BufferBuilder buffer, BaseGirlEntity entity, float partialTicks, float x, float y, float z, float scale) {
      GeoBone rootBone = model.topLevelBones.get(0);
      GeoBone bodyBone = null;
      GeoBone headBone = null;

      for (GeoBone childBone : rootBone.childBones) {
         switch (childBone.getName()) {
            case "steve":
               headBone = childBone;
               break;
            case "body":
               bodyBone = childBone;
         }
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(rootBone);
      MATRIX_STACK.moveToPivot(rootBone);
      MATRIX_STACK.rotate(rootBone);
      MATRIX_STACK.scale(rootBone);
      MATRIX_STACK.moveBackFromPivot(rootBone);
      this.renderRecursively(buffer, bodyBone, partialTicks, x, y, z);
      Tessellator.getInstance().draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.renderEntity));

      this.renderRecursively(buffer, headBone, partialTicks, x, y, this.renderEntity.getRenderScaleFactor());
      Tessellator.getInstance().draw();
      MATRIX_STACK.pop();
   }

}
