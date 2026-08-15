package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AlliePlayerEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the player-form Allie (horny potion).
 */
public class PlayerAllieRenderer extends GirlPlayerRenderer {
   static final float BOB_SCALE_8 = 8.0F;
   static final float BOB_SCALE_1_68 = 1.68F;
   static final float BOB_SCALE_5 = 5.0F;
   static Collection<PlayerAllieRenderer> renderers = new ArrayList<>();
   double currentPosX = 0.0;
   double currentPosZ = 0.0;
   double prevPosX = 0.0;
   double prevPosZ = 0.0;
   float prevRotX = 0.0F;
   float prevRotZ = 0.0F;
   float rotG;
   float rotI;
   double smoothedBob = 0.0;
   double moveMagnitude = 0.0;

   public PlayerAllieRenderer(RenderManager renderManager, AnimatedGeoModel geoModel) {
      super(renderManager, geoModel);
      renderers.add(this);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.1F, 0.0F);
      GlStateManager.scale(0.7F, 0.7F, 0.7F);
   }

   @Override
   protected void applyItemPostRotation(boolean isMainHand, ItemStack stack) {
      super.applyItemPostRotation(isMainHand, stack);
      switch (stack.getItem().getItemUseAction(stack)) {
         default:
            if (!isMainHand) {
               GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.translate(0.0, 0.05, 0.0);
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void applyBowRotation(boolean isMainHand) {
      super.applyBowRotation(isMainHand);
      if (isMainHand) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      } else {
         GlStateManager.translate(-0.05, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean isBlocking, boolean isMainHand) {
      super.applyShieldBlockingTransform(isBlocking, isMainHand);
      if (isBlocking && !isMainHand) {
         GlStateManager.translate(-0.025, -0.1, -0.1);
         GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
      } else if (!isBlocking && !isMainHand) {
         GlStateManager.translate(-0.05, -0.125, 0.125);
         GlStateManager.rotate(50.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void onBoneRenderStart(String boneName, GeoBone bone) {
      if (!(Boolean)this.playerGirl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
         if ("tail".equals(boneName)) {
            this.applyBoneRotation(bone, 0.0F, 0.0F, 1.0F);
         }

         if ("body".equals(boneName)) {
            this.updateBoneBob(bone);
         }

         if (this.playerGirl.getCurrentAction() != Action.BOW) {
            if ("armL".equals(boneName)) {
               this.applyBoneRotation(bone, 0.0F, (float) (-Math.PI / 9), 0.15F);
            }

            if (this.playerGirl.getCurrentAction() != Action.ATTACK) {
               if ("armR".equals(boneName)) {
                  this.applyBoneRotation(bone, 0.0F, (float) (Math.PI / 9), 0.15F);
               }
            }
         }
      }
   }

   void applyBoneRotation(GeoBone bone, float rotX, float rotZ, float intensity) {
      double dx = this.currentPosX - this.prevPosX;
      double dz = this.currentPosZ - this.prevPosZ;
      double yawRad = (Math.PI / 180.0) * this.playerGirl.rotationYaw;
      Vec2f movement = new Vec2f((float)(dx * Math.cos(yawRad) + dz * Math.sin(yawRad)), (float)(-dx * Math.sin(yawRad) + dz * Math.cos(yawRad)));
      this.rotG = movement.y * -8.0F;
      this.rotI = movement.x * 8.0F;
      this.rotG = ThreadNames.clampFloat(this.rotG, -1.68F, 1.68F);
      this.rotI = ThreadNames.clampFloat(this.rotI, -1.68F, 1.68F);
      this.rotG = RotationHelper.lerp(this.prevRotX, this.rotG, this.partialTicks);
      this.rotI = RotationHelper.lerp(this.prevRotZ, this.rotI, this.partialTicks);
      bone.setRotationX(rotX + this.rotG * intensity);
      bone.setRotationZ(rotZ + this.rotI * intensity);
   }

   void updateBoneBob(GeoBone bone) {
      double dx = this.currentPosX - this.prevPosX;
      double dz = this.currentPosZ - this.prevPosZ;
      this.moveMagnitude = (Math.abs(dx) + Math.abs(dz)) * 5.0;
      this.moveMagnitude = ThreadNames.clampFloat((float)this.moveMagnitude, 0.0F, 1.0F);
      bone.setPositionY((float)RotationHelper.lerpAngle(5.0, 0.0, RotationHelper.lerpDouble(this.smoothedBob, this.moveMagnitude, this.partialTicks)));
      if (this.playerGirl instanceof AlliePlayerEntity) {
         ((AlliePlayerEntity)this.playerGirl).aq = (float)RotationHelper.lerpAngle(0.3F, 0.0, RotationHelper.lerpDouble(this.smoothedBob, this.moveMagnitude, this.partialTicks));
      }
   }

   void updateCameraRotations() {
      if (this.playerGirl != null) {
         this.prevRotX = this.rotG;
         this.prevRotZ = this.rotI;
         this.smoothedBob = this.moveMagnitude;
         if (this.playerGirl.getOwnerUserUUID() != null) {
            EntityPlayer owner = this.renderEntity.world.getPlayerEntityByUUID(this.playerGirl.getOwnerUserUUID());
            if (owner != null) {
               this.prevPosX = this.currentPosX;
               this.prevPosZ = this.currentPosZ;
               this.currentPosX = owner.posX;
               this.currentPosZ = owner.posZ;
            }
         }
      }
   }

   public static class a {
      @SubscribeEvent
      public void onClientTick(ClientTickEvent event) {
         for (PlayerAllieRenderer renderer : PlayerAllieRenderer.renderers) {
            renderer.updateCameraRotations();
         }
      }
   }
}
