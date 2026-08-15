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

   public PlayerAllieRenderer(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
      renderers.add(this);
   }

   @Override
   protected void preRenderCallback() {
      GlStateManager.translate(0.0F, -1.1F, 0.0F);
      GlStateManager.scale(0.7F, 0.7F, 0.7F);
   }

   @Override
   protected void applyItemPostRotation(boolean var1, ItemStack var2) {
      super.applyItemPostRotation(var1, var2);
      switch (var2.getItem().getItemUseAction(var2)) {
         default:
            if (!var1) {
               GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
            }

            GlStateManager.translate(0.0, 0.05, 0.0);
         case BLOCK:
         case BOW:
      }
   }

   @Override
   protected void applyBowRotation(boolean var1) {
      super.applyBowRotation(var1);
      if (var1) {
         GlStateManager.translate(0.15, 0.0, 0.0);
      } else {
         GlStateManager.translate(-0.05, 0.0, 0.0);
      }
   }

   @Override
   protected void applyShieldBlockingTransform(boolean var1, boolean var2) {
      super.applyShieldBlockingTransform(var1, var2);
      if (var1 && !var2) {
         GlStateManager.translate(-0.025, -0.1, -0.1);
         GlStateManager.rotate(10.0F, 1.0F, 0.0F, 0.0F);
      } else if (!var1 && !var2) {
         GlStateManager.translate(-0.05, -0.125, 0.125);
         GlStateManager.rotate(50.0F, 1.0F, 0.0F, 0.0F);
      }
   }

   @Override
   protected void onBoneRenderStart(String var1, GeoBone var2) {
      if (!(Boolean)this.playerGirl.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
         if ("tail".equals(var1)) {
            this.applyBoneRotation(var2, 0.0F, 0.0F, 1.0F);
         }

         if ("body".equals(var1)) {
            this.updateBoneBob(var2);
         }

         if (this.playerGirl.getCurrentAction() != Action.BOW) {
            if ("armL".equals(var1)) {
               this.applyBoneRotation(var2, 0.0F, (float) (-Math.PI / 9), 0.15F);
            }

            if (this.playerGirl.getCurrentAction() != Action.ATTACK) {
               if ("armR".equals(var1)) {
                  this.applyBoneRotation(var2, 0.0F, (float) (Math.PI / 9), 0.15F);
               }
            }
         }
      }
   }

   void applyBoneRotation(GeoBone var1, float var2, float var3, float var4) {
      double var5 = this.currentPosX - this.prevPosX;
      double var7 = this.currentPosZ - this.prevPosZ;
      double var9 = (Math.PI / 180.0) * this.playerGirl.rotationYaw;
      Vec2f var11 = new Vec2f((float)(var5 * Math.cos(var9) + var7 * Math.sin(var9)), (float)(-var5 * Math.sin(var9) + var7 * Math.cos(var9)));
      this.rotG = var11.y * -8.0F;
      this.rotI = var11.x * 8.0F;
      this.rotG = ThreadNames.clampFloat(this.rotG, -1.68F, 1.68F);
      this.rotI = ThreadNames.clampFloat(this.rotI, -1.68F, 1.68F);
      this.rotG = RotationHelper.lerp(this.prevRotX, this.rotG, this.partialTicks);
      this.rotI = RotationHelper.lerp(this.prevRotZ, this.rotI, this.partialTicks);
      var1.setRotationX(var2 + this.rotG * var4);
      var1.setRotationZ(var3 + this.rotI * var4);
   }

   void updateBoneBob(GeoBone var1) {
      double var2 = this.currentPosX - this.prevPosX;
      double var4 = this.currentPosZ - this.prevPosZ;
      this.moveMagnitude = (Math.abs(var2) + Math.abs(var4)) * 5.0;
      this.moveMagnitude = ThreadNames.clampFloat((float)this.moveMagnitude, 0.0F, 1.0F);
      var1.setPositionY((float)RotationHelper.lerpAngle(5.0, 0.0, RotationHelper.lerpDouble(this.smoothedBob, this.moveMagnitude, this.partialTicks)));
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
            EntityPlayer var1 = this.renderEntity.world.getPlayerEntityByUUID(this.playerGirl.getOwnerUserUUID());
            if (var1 != null) {
               this.prevPosX = this.currentPosX;
               this.prevPosZ = this.currentPosZ;
               this.currentPosX = var1.posX;
               this.currentPosZ = var1.posZ;
            }
         }
      }
   }

   public static class a {
      @SubscribeEvent
      public void onClientTick(ClientTickEvent var1) {
         for (PlayerAllieRenderer var3 : PlayerAllieRenderer.renderers) {
            var3.updateCameraRotations();
         }
      }
   }
}
