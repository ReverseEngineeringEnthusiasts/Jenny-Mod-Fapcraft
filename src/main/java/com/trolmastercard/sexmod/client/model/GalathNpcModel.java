package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector4d;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

public class GalathNpcModel extends GirlModel<BaseGirlEntity> {
   public static ResourceLocation GALATH_TEXTURE = new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   float lastPussyLickingWave = 0.0F;
   long swordDashStartTime = -1L;
   long swordDashEndTime = -1L;

   public GalathNpcModel() {
      this.modelLocations = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath_con_mang.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/galath/galath.animation.json");
   }

   @Override
   protected boolean shouldRender(BaseGirlEntity var1) {
      if (!(var1 instanceof GalathEntity)) {
         return true;
      }

      GalathEntity var2 = (GalathEntity)var1;
      return var2.hasMaster() ? true : var2.getTargetEntity() == null;
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      this.updateIdlePose(var1);
      super.setLivingAnimations(var1, var2, var3);
      this.handleActionPose(var1);
      this.updateHurtPose(var1);
      this.handleFlightPose(var1);
      this.handleWingState(var1);
      this.updateSwordBones(var1);
      this.hideWings(var1);
      this.updatePlayerPose(var1);
      this.hideFutaBone();
      this.updateModelState(var1);
      this.updatePussyPose(var1);
      this.handleDashAnimation(var1);
      if (var1 instanceof GalathEntity) {
         GalathEntity var4 = (GalathEntity)var1;
         var4.aE = this.getAnimationProcessor().getBone("head").getRotationX();
         if (var4.isHuggingManglelie()) {
            ManglelieNpcModel.animateModel(var4, this.getAnimationProcessor(), var3.getPartialTick());
         }
      }
   }

   void updatePussyPose(BaseGirlEntity var1) {
      if (Action.isAnyAction(var1, Action.PUSSY_LICKING)) {
         if (var1 instanceof GalathEntity) {
            if (!this.mc.isGamePaused()) {
               AnimationProcessor var2 = this.getAnimationProcessor();
               IBone var3 = var2.getBone("head");
               float var4 = this.mc.getRenderPartialTicks() + this.mc.player.ticksExisted;
               Vector3fSexmodSpecial var5 = this.getSwordPos((GalathEntity)var1, var4);
               var3.setRotationX(var3.getRotationX() + var5.x);
               var3.setRotationY(var3.getRotationY() + var5.y);
               var3.setRotationZ(var3.getRotationZ() + var5.z);
               if (var1.getCurrentAction() == Action.PUSSY_LICKING && !((GalathEntity)var1).a5) {
                  float var6 = (float)(Math.sin(var4 * 0.3F) * 10.0);
                  if (var6 > 0.0F && this.lastPussyLickingWave < 0.0F || var6 < 0.0F && this.lastPussyLickingWave > 0.0F) {
                     var1.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
                  }

                  this.lastPussyLickingWave = var6;
               }
            }
         }
      }
   }

   Vector3fSexmodSpecial getSwordPos(GalathEntity var1, float var2) {
      return RotationHelper.lerpVector3f(this.getSwordSwingOffset(var2), Vector3fSexmodSpecial.ZERO, var1.getSwordAttackProgress(this.mc.getRenderPartialTicks()));
   }

   Vector3fSexmodSpecial getSwordSwingOffset(float var1) {
      return new Vector3fSexmodSpecial(
         (float)Math.sin(var1 * 0.3F) * TrigMath.wrapDegrees(10.0F),
         (float)Math.sin(var1 * 0.15F) * TrigMath.wrapDegrees(7.0F),
         (float)Math.sin(var1 * -0.15) * TrigMath.wrapDegrees(7.0F)
      );
   }

   void updateModelState(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         GalathEntity var2 = (GalathEntity)var1;
         AnimationProcessor var3 = this.getAnimationProcessor();
         IBone var4 = var3.getBone("body");
         var2.bw = var4.getRotationY();
         var2.bm = var4.getScaleY();
      }
   }

   void handleDashAnimation(BaseGirlEntity var1) {
      if (var1.actionController.getAnimationState() == AnimationState.Transitioning) {
         AnimationProcessor var2 = this.getAnimationProcessor();
         Action var3 = var1.getCurrentAction();
         if (var3 == Action.HUG_MANG) {
            IBone var4 = var2.getBone("body2");
            if (var4 == null) {
               return;
            }

            var4.setPositionX(0.0F);
            var4.setPositionY(-0.53F);
            var4.setPositionZ(-40.05F);
         }
      }
   }

   void updateIdlePose(BaseGirlEntity var1) {
      if (!ClientProxy.IS_PRELOADING) {
         if (var1.getCurrentAction() == Action.MASTERBATE) {
            Object var2 = var1.getMasterPlayer();
            if (var2 == null) {
               var2 = this.mc.player;
            }

            MolangParser var3 = GeckoLibCache.getInstance().parser;
            Vec3d var4 = EntityLookVectorHelper.getLookVectorTo(var1, (EntityPlayer)var2, this.mc.getRenderPartialTicks()).add(var1.getCachedBoneOffset("head"));
            float var5 = (float)TrigMath.sinDegrees(Math.atan2(var4.z, var4.x)) - var1.getYawRotation();
            float var6 = (float)TrigMath.sinDegrees(
               Math.atan2(var4.y, Math.sqrt(var4.x * var4.x + var4.z * var4.z))
            );
            double var7 = Math.abs(var4.x) + Math.abs(var4.y) + Math.abs(var4.z);
            double var9 = var7 * 7.0 + -20.0;
            double var11 = var7 * 5.0 + -20.0;
            var3.setValue("pitch", var9 + var6 - 80.0);
            var3.setValue("armpitch", var11 + var6 + -110.0);
            var3.setValue("armyaw", var5 + 80.0F);
            var3.setValue("yaw", var5 + 90.0F);
         }
      }
   }

   void hideFutaBone() {
      if (!ClientProxy.IS_PRELOADING) {
         this.getAnimationProcessor().getBone("futaCock").setHidden(!CommandFuta.ENABLED);
         this.getAnimationProcessor().getBone("futaBallLL").setHidden(!CommandFuta.ENABLED);
         this.getAnimationProcessor().getBone("futaBallLR").setHidden(!CommandFuta.ENABLED);
      }
   }

   void updatePlayerPose(BaseGirlEntity var1) {
      if (var1 instanceof AbstractPlayerGirlEntity) {
         this.getAnimationProcessor().getBone("coin").setHidden(true);
      }
   }

   void hideWings(BaseGirlEntity var1) {
      this.getAnimationProcessor().getBone("wings").setHidden(!((IGalath)var1).areWingsAnimated());
   }

   void updateSwordBones(BaseGirlEntity var1) {
      AnimationProcessor var2 = this.getAnimationProcessor();
      IBone var3 = var2.getBone("nippleR");
      IBone var4 = var2.getBone("nippleL");
      IBone var5 = var2.getBone("braBoobL");
      IBone var6 = var2.getBone("braBoobR");
      IBone var7 = var2.getBone("slip");
      boolean var8 = ((IGalath)var1).isWingsAnimated();
      if (var8) {
         Action.isAnyAction(var1, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING, Action.MASTERBATE_SITTING_CUM);
         if (var3 != null) {
            if (var5 != null) {
               IBone var13 = var3;
               var13.setHidden(false);
               IBone var14 = var4;
               var14.setHidden(false);
               var5.setHidden(var8);
               var6.setHidden(var8);
               IBone var15 = var7;
               var15.setHidden(true);
            }
         }
      } else {
         boolean var9 = Action.isAnyAction(var1, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING, Action.MASTERBATE_SITTING_CUM);
         if (var3 != null) {
            if (var5 != null) {
               IBone var10 = var3;
               var10.setHidden(true);
               IBone var11 = var4;
               var11.setHidden(true);
               var5.setHidden(var8);
               var6.setHidden(var8);
               IBone var12 = var7;
               var12.setHidden(var9);
            }
         }
      }
   }

   void handleWingState(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         if ((Boolean)var1.getDataManager().get(GalathEntity.bP)) {
            if (var1.getCurrentAction() == Action.KNOCK_OUT_FLY) {
               IBone var2 = this.getAnimationProcessor().getBone("body");
               Vec3d var3 = new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ);
               Vec3d var4 = var1.getPositionVector().subtract(var3);
               boolean var5 = Math.abs(var4.x) + Math.abs(var4.z) < 0.01F;
               if (var5) {
                  var2.setRotationX(TrigMath.wrapDegrees(-90.0F));
                  var2.setPositionY(0.0F);
                  var2.setPositionZ(0.0F);
               } else {
                  Vec3d var6 = getInterpolatedPosition(var1);
                  var2.setRotationX(-((float)var6.x));
                  var2.setPositionY((float)var6.y);
                  var2.setPositionZ((float)var6.z);
               }
            }
         }
      }
   }

   void updateHurtPose(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         if (var1.getCurrentAction() == Action.RAPE_CHARGE) {
            Vec3d var2 = getInterpolatedPosition(var1);
            IBone var3 = this.getAnimationProcessor().getBone("body");
            IBone var4 = this.getAnimationProcessor().getBone("rotationTool");
            var4.setRotationX((float)var2.x);
            var3.setPositionY((float)var2.y);
            var3.setPositionZ((float)var2.z);
            float var5 = (Float)var1.getDataManager().get(GalathEntity.bO);
            var3.setRotationY(TrigMath.wrapDegrees(var5 * 180.0F));
         }
      }
   }

   void handleFlightPose(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         GalathEntity var2 = (GalathEntity)var1;
         if (var2.getCurrentAction() != Action.ATTACK_SWORD) {
            this.swordDashStartTime = -1L;
            this.swordDashEndTime = -1L;
         } else {
            int var3 = var2.az();
            if (var3 == 24 && this.swordDashStartTime == -1L) {
               this.swordDashStartTime = this.mc.world.getTotalWorldTime();
               this.swordDashEndTime = this.swordDashStartTime + 8L;
            }

            if (ThreadNames.isBetween(var3, 24.0, 32.0)) {
               IBone var4 = this.getAnimationProcessor().getBone("body");
               Vec3d var5 = GirlModel.getBoneOffsetWorld(var2, var2.B_clash642());
               float var6 = ((float)Minecraft.getMinecraft().world.getTotalWorldTime() + this.mc.getRenderPartialTicks() - (float)this.swordDashStartTime) / (float)(this.swordDashEndTime - this.swordDashStartTime);
               var5 = RotationHelper.lerpVec3dDouble(var5, Vec3d.ZERO, var6);
               var4.setRotationX((float)var5.x);
               var4.setPositionY((float)var5.y);
               var4.setPositionZ((float)var5.z);
            }
         }
      }
   }

   void handleActionPose(BaseGirlEntity var1) {
      float var2 = 0.0F;
      switch (var1.getCurrentAction()) {
         case BOOST:
            if (Action.BOOST.ticksPlaying[1] > 13 && Action.BOOST.ticksPlaying[1] < 40) {
               var2 = 45.0F;
            }
         case FLY:
         case CONTROLLED_FLIGHT:
            float var3 = Minecraft.getMinecraft().getRenderPartialTicks();
            IBone var4 = this.getAnimationProcessor().getBone("rotationTool");
            Vector4d var5 = ((IGalath)var1).getFlightData();
            var4.setRotationX((float)RotationHelper.lerpDouble(var5.z + var2, var5.x + var2, var3));
            var4.setRotationZ((float)RotationHelper.lerpDouble(var5.w, var5.y, var3));
            return;
      }
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

}
