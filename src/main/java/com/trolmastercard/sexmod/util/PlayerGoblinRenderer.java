package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.HashSet;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Goblin (horny potion).
 */
public class PlayerGoblinRenderer extends AbstractPlayerKoblinGoboldRenderer {
   GoblinPlayerEntity playerGirl = null;
   boolean isShoulderIdle = false;
   boolean isBeingPickedUp = false;
   boolean isFirstPersonView = false;

   public PlayerGoblinRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
   }

   @Override
   protected Vec3i resolveBoneColor(String boneName) {
      String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
      if (modelCodeParts.length < 8) {
         return tintColor;
      } else if (boneName.contains("band")) {
         return GoblinRenderer.DEFAULT_BONE_COLOR;
      } else if (boneName.contains("eyeColor") || boneName.contains("eyeColor2")) {
         return GoblinRenderer.getEyeColor(modelCodeParts[8]);
      } else if (boneName.contains("variant") || boneName.contains("boob")) {
         return GoblinRenderer.getSkinColor(modelCodeParts[7]);
      } else if (boneName.contains("hair")) {
         return GoblinRenderer.getHairColor(modelCodeParts[6]);
      } else if (GoblinRenderer.NUDE_BONE_NAMES.contains(boneName)) {
         return GoblinRenderer.getSkinColor(modelCodeParts[7]);
      } else {
         return GoblinRenderer.LASH_BONE_NAMES.contains(boneName) ? GoblinRenderer.getHairColor(modelCodeParts[6]) : tintColor;
      }
   }

   @Override
   protected Vector4f calculateBoneArmorColor(String boneName, float red, float green, float blue) {
      if (boneName.startsWith("crown")) {
         ItemStack stack = (ItemStack)this.renderEntity.getDataManager().get(AbstractGirlNpcEntity.HELMET_SLOT);
         if (stack.isEmpty()) {
            return super.calculateBoneArmorColor(boneName, red, green, blue);
         }

         ItemArmor armor = (ItemArmor)stack.getItem();
         ArmorMaterial material = armor.getArmorMaterial();
         float armorValue = 0.0F;
         switch (material) {
            case GOLD:
               armorValue = 1.0F;
               break;
            case CHAIN:
            case IRON:
               armorValue = 2.0F;
               break;
            case LEATHER:
               armorValue = 4.0F;
               int colorInt = armor.getColor(stack);
               float colorRed = (colorInt >> 16 & 0xFF) / 255.0F;
               float colorGreen = (colorInt >> 8 & 0xFF) / 255.0F;
               float colorBlue = (colorInt & 0xFF) / 255.0F;
               red = colorRed;
               green = colorGreen;
               blue = colorBlue;
         }

         return new Vector4f(red, green, blue, 72.0F * armorValue / 4096.0F);
      } else {
         return super.calculateBoneArmorColor(boneName, red, green, blue);
      }
   }

   @Override
   protected boolean isBoneBlacklisted(String boneName) {
      return boneName.startsWith("crown") ? true : super.isBoneBlacklisted(boneName);
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      return new HashSet<String>() {
         {
            this.add("boobs");
            this.add("booty");
            this.add("vagina");
            this.add("fuckhole");
            this.add("preggy");
            this.add("LegL");
            this.add("LegR");
            this.add("cheekR");
            this.add("cheekL");
         }
      };
   }

   @Override
   protected void onBoneRenderStart(String boneName, GeoBone bone) {
      String[] modelCodeParts = AbstractNpcOnlyEntity.getModelCodeParts(this.renderEntity);
      if (modelCodeParts.length >= 8) {
         switch (boneName) {
            case "earL":
               GoblinRenderer.applyBoneParts(bone, modelCodeParts[0], modelCodeParts[1], modelCodeParts[3]);
               break;
            case "earR":
               GoblinRenderer.applyBoneParts(bone, modelCodeParts[0], modelCodeParts[2], modelCodeParts[4]);
               break;
            case "hair":
               GoblinRenderer.applyBonePart(bone, modelCodeParts[5]);
               break;
            case "body":
               bone.setPivotY(-0.15F);
               GoblinRenderer.applyBoneState(this.renderEntity, bone);
               break;
            case "LegR":
               GoblinRenderer.applyBoneRot(this.isShoulderIdle, bone, 25.0F, 25.0F);
               break;
            case "boobR":
               GoblinRenderer.applyBoneRot(this.isShoulderIdle, bone, 30.0F, 30.0F);
               break;
            case "boobR1":
               GoblinRenderer.applyBoneRot(this.isShoulderIdle, bone, 10.0F, 15.0F);
               break;
            case "boobR2":
               GoblinRenderer.applyBoneRot(this.isShoulderIdle, bone, 5.0F, 3.0F);
         }

         if (boneName.contains("crown")) {
            GoblinRenderer.applyBoneColor(this.renderEntity, bone, modelCodeParts[9]);
         }
      }
   }

   @Override
   public void doRenderEntity(BaseGirlEntity entity, double x, double y, double z, float yaw, float partialTicks) {
      this.isFirstPersonView = isFirstPerson;
      this.playerGirl = (GoblinPlayerEntity)entity;
      this.isShoulderIdle = -420.69F == yaw && entity.getCurrentAction() == Action.SHOULDER_IDLE;
      this.isBeingPickedUp = -420.69F == yaw && entity.getCurrentAction() == Action.PICK_UP;
      this.partialTicks = partialTicks;
      GoblinRenderer.currentActionValue = yaw;
      Action action = entity.getCurrentAction();
      UUID ownerUuid = this.playerGirl.getOwnerUUID();
      if (ownerUuid != null) {
         if (entity.isLocallyRegistered()) {
            Vec3d throwPos = GoblinRenderer.getThrowPosition(entity.world, entity, ownerUuid, x, y, z);
            x = throwPos.x;
            y = throwPos.y;
            z = throwPos.z;
         }

         if (action == Action.THROWN || action == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && yaw == -420.69F && !entity.isLocallyRegistered()) {
               return;
            }

            if (!entity.isLocallyRegistered()) {
               float yawOffset = entity.getYawRotation();
               entity.prevRenderYawOffset = yawOffset;
               entity.renderYawOffset = yawOffset;
            }
         }

         if (GoblinRenderer.isThrowAction(entity, action)) {
            if (mc.player.getPersistentID().equals(ownerUuid)) {
               if (-420.69F != yaw) {
                  return;
               }

               entity.renderYawOffset = mc.player.rotationYaw + 180.0F;
               entity.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d lookVec = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(lookVec.x, lookVec.y + mc.player.getEyeHeight(), lookVec.z);
               Vec3d rotateVec = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)rotateVec.x, 0.0F, (float)rotateVec.z);
               x = 0.0;
               y = 0.0;
               z = 0.0;
            } else if (!this.playerGirl.getOwnerUserUUID().equals(mc.player.getPersistentID())) {
               if (!entity.isLocallyRegistered() || mc.player.getPersistentID().equals(ownerUuid)) {
                  if (!mc.player.getPersistentID().equals(ownerUuid)) {
                     EntityPlayer owner = entity.world.getPlayerEntityByUUID(ownerUuid);
                     if (owner != null) {
                        entity.renderYawOffset = owner.rotationYaw;
                        entity.prevRenderYawOffset = owner.rotationYaw;
                     }
                  } else {
                     entity.renderYawOffset = mc.player.rotationYaw;
                     entity.prevRenderYawOffset = mc.player.rotationYaw;
                  }
               }

               Vec3d aim = GoblinRenderer.getThrowAim(entity, this.playerGirl.getOwnerUUID(), partialTicks);
               x = aim.x;
               y = aim.y;
               z = aim.z;
            }
         } else if (this.isShoulderIdle) {
            GoblinRenderer.setFirstPersonCamera(partialTicks);
            Vec3d cameraOffset = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            cameraOffset = GoblinEntity.rotateVectorYaw(cameraOffset, mc.player.rotationYaw);
            x = cameraOffset.x;
            y = cameraOffset.y;
            z = cameraOffset.z;
            entity.renderYawOffset = mc.player.rotationYaw;
            entity.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               y -= 0.075;
            }
         } else if (action == Action.SHOULDER_IDLE) {
            if (mc.player.getPersistentID().equals(ownerUuid) && mc.gameSettings.thirdPersonView == 0) {
               return;
            }

            EntityPlayer owner2 = entity.world.getPlayerEntityByUUID(ownerUuid);
            if (owner2 == null) {
               return;
            }

            Vector4f pov = GoblinRenderer.getFirstPersonView(owner2, partialTicks);
            x = pov.x;
            y = pov.y;
            z = pov.z;
            entity.renderYawOffset = pov.w;
            if (owner2.isSneaking()) {
               y -= 0.32;
            }
         } else if (action == Action.PICK_UP) {
            EntityPlayer owner3 = entity.world.getPlayerEntityByUUID(ownerUuid);
            if (owner3 != null) {
               entity.prevRenderYawOffset = owner3.prevRotationYawHead;
               entity.renderYawOffset = owner3.rotationYawHead;
            }
         }

         super.doRenderEntity(entity, (double)x, (double)y, (double)z, yaw, partialTicks);
         if (GoblinRenderer.isThrowAction(entity, action) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(ownerUuid)) {
            GlStateManager.popMatrix();
         }
      } else {
         if (entity.isLocallyRegistered()) {
            Vec3d throwPos2 = GoblinRenderer.getThrowPosition(entity.world, entity, ownerUuid, x, y, z);
            x = throwPos2.x;
            y = throwPos2.y;
            z = throwPos2.z;
         }

         if (action == Action.THROWN || action == Action.START_THROWING) {
            if (mc.gameSettings.thirdPersonView == 0 && yaw == -420.69F && !entity.isLocallyRegistered()) {
               return;
            }

            if (!entity.isLocallyRegistered()) {
               float yawOffset2 = entity.getYawRotation();
               entity.prevRenderYawOffset = yawOffset2;
               entity.renderYawOffset = yawOffset2;
            }
         }

         if (GoblinRenderer.isThrowAction(entity, action)) {
            if (mc.player.getPersistentID().equals(ownerUuid)) {
               if (-420.69F != yaw) {
                  return;
               }

               entity.renderYawOffset = mc.player.rotationYaw + 180.0F;
               entity.prevRenderYawOffset = mc.player.rotationYaw + 180.0F;
               Vec3d lookVec2 = mc.player.getLookVec();
               GlStateManager.pushMatrix();
               GlStateManager.translate(lookVec2.x, lookVec2.y + mc.player.getEyeHeight(), lookVec2.z);
               Vec3d rotateVec2 = GoblinEntity.rotateVectorYaw(new Vec3d(-Math.abs(mc.player.rotationPitch), 0.0, 0.0), mc.player.rotationYaw);
               GlStateManager.rotate(mc.player.rotationPitch, (float)rotateVec2.x, 0.0F, (float)rotateVec2.z);
               x = 0.0;
               y = 0.0;
               z = 0.0;
            } else if (!this.playerGirl.getOwnerUserUUID().equals(mc.player.getPersistentID())) {
               if (entity.isLocallyRegistered()) {
               }

               entity.renderYawOffset = mc.player.rotationYaw;
               entity.prevRenderYawOffset = mc.player.rotationYaw;
               Vec3d aim2 = GoblinRenderer.getThrowAim(entity, this.playerGirl.getOwnerUUID(), partialTicks);
               x = aim2.x;
               y = aim2.y;
               z = aim2.z;
            }
         } else if (this.isShoulderIdle) {
            GoblinRenderer.setFirstPersonCamera(partialTicks);
            Vec3d cameraOffset2 = new Vec3d(RotationHelper.lerp(-0.1F, 0.2F, mc.gameSettings.fovSetting / 110.0F), 0.0, 0.0);
            cameraOffset2 = GoblinEntity.rotateVectorYaw(cameraOffset2, mc.player.rotationYaw);
            x = cameraOffset2.x;
            y = cameraOffset2.y;
            z = cameraOffset2.z;
            entity.renderYawOffset = mc.player.rotationYaw;
            entity.prevRenderYawOffset = mc.player.prevRotationYaw;
            if (mc.player.isSneaking()) {
               y -= 0.075;
            }
         } else {
            if (action == Action.SHOULDER_IDLE) {
               return;
            }

            if (action == Action.PICK_UP) {
            }
         }

         super.doRenderEntity(entity, (double)x, (double)y, (double)z, yaw, partialTicks);
         if (GoblinRenderer.isThrowAction(entity, action) && mc.gameSettings.thirdPersonView == 0 && mc.player.getPersistentID().equals(ownerUuid)) {
            GlStateManager.popMatrix();
         }
      }
   }

   @Override
   protected void drawOverlayLines(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial offset, float alpha) {
      renderGirlTint(tessellator, buffer, girl, offset, alpha);
   }

   @Nullable
   @Override
   protected Vector3fSexmodSpecial getAdditionalOverlayColor(BaseGirlEntity girl) {
      if (!this.isFirstPersonView) {
         return null;
      }

      if (!(girl instanceof GoblinPlayerEntity)) {
         return null;
      }

      GoblinPlayerEntity playerGirl = (GoblinPlayerEntity)girl;
      UUID ownerUuid = playerGirl.getOwnerUserUUID();
      EntityPlayerSP clientPlayer = mc.player;
      if (ownerUuid != null && (mc.gameSettings.thirdPersonView != 0 || !clientPlayer.getPersistentID().equals(ownerUuid))) {
         EntityPlayer owner = playerGirl.getOwnerPlayer();
         if (owner == null) {
            return null;
         }

         ItemStack stack = (ItemStack)playerGirl.getDataManager().get(AbstractGirlNpcEntity.CHEST_SLOT);
         if (stack.isEmpty()) {
            return null;
         }

         if (!(stack.getItem() instanceof ItemArmor)) {
            return null;
         }

         ItemArmor armor = (ItemArmor)stack.getItem();
         switch (armor.getArmorMaterial()) {
            case GOLD:
               return new Vector3fSexmodSpecial(99.0F, 98.0F, 14.0F);
            case CHAIN:
            case IRON:
               return new Vector3fSexmodSpecial(85.0F, 85.0F, 85.0F);
            case LEATHER:
               int colorInt = armor.getColor(stack);
               float colorRed = colorInt >> 16 & 0xFF;
               float colorGreen = colorInt >> 8 & 0xFF;
               float colorBlue = colorInt & 0xFF;
               return new Vector3fSexmodSpecial(colorRed, colorGreen, colorBlue);
            case DIAMOND:
            default:
               return new Vector3fSexmodSpecial(23.0F, 100.0F, 93.0F);
         }
      } else {
         return null;
      }
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0, -0.77, -0.05);
      GlStateManager.scale(0.5, 0.5, 0.5);
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      super.applyItemPostRotation(isMainHand, stack);
      if (stack.getItem().getItemUseAction(stack) == EnumAction.BOW) {
         if (isMainHand) {
            GlStateManager.translate(0.1F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
         } else {
            GlStateManager.rotate(170.0F, 1.0F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.rotate(isMainHand ? 70.0F : 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.translate(0.0, 0.05, -0.03);
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (isBlocking) {
         if (isMainHand) {
            GlStateManager.translate(0.0, 0.2, -0.25);
            GlStateManager.rotate(85.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(0.0, -0.265, -0.04);
         }
      } else if (isMainHand) {
         GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.rotate(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.rotate(0.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.translate(0.0, -0.33, -0.1);
      } else {
         GlStateManager.translate(-0.02, -0.05, -0.05);
      }
   }

}
