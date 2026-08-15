package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * Geckolib model for the goblin: nude/armored geo variants whose per-frame
 * pass drives the throw/pick-up/vanishing/breeding poses — pregnancy bone,
 * first-person body hiding, throw-leg swing from the owner's limb motion,
 * head look-at for awaiting players, and the body offset during breeding
 * scenes in first person.
 * <p>
 * <b>Pose matrix.</b> Body/head bones are hidden or repositioned per action
 * and per perspective (owner in first person vs. onlookers); the goblin
 * renderer relies on these exact hidden states for the shoulder/pick-up
 * views — keep the action switch intact.
 */
public class GoblinNpcModel extends GirlModel<BaseGirlEntity> {
   final float legSwingAngle = 60.0F;
   Minecraft mc = Minecraft.getMinecraft();

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/goblin/goblin.geo.json"), new ResourceLocation("sexmod", "geo/goblin/armored.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/goblin/goblin.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) { return new ResourceLocation("sexmod", "animations/goblin/goblin.animation.json");
   }

   @Override
   protected boolean canRender(BaseGirlEntity girl) {
      if (!(girl instanceof GoblinEntity)) {
         return super.canRender(girl);
      }

      GoblinEntity goblin = (GoblinEntity)girl;
      UUID uuid = goblin.getInteractionPlayerUUID();
      if (uuid == null) {
         uuid = goblin.getOwnerUUID();
      }

      if (uuid == null) {
         return true;
      }

      World world = goblin.world;
      AbstractClientPlayer player = (AbstractClientPlayer)world.getPlayerEntityByUUID(uuid);
      return player == null ? true : "default".equals(player.getSkinType());
   }

   /**
    * Per-frame pose pass (see class javadoc): pregnancy bone, breeding body
    * offset, look/throw/vanish bone states, throw leg swing and walk/idle
    * poses. Two near-identical branches cover owned goblins vs. other
    * entities — keep them in sync.
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      if (!(entity.world instanceof SexWorldClient)) {
         AnimationProcessor processor = this.getAnimationProcessor();
         if (entity instanceof GoblinEntity) {
            IBone preggyBone2 = processor.getBone("preggy");
            preggyBone2.setHidden(!(Boolean)entity.getDataManager().get(GoblinEntity.aV));
            IBone bodyBone2 = processor.getBone("body");
            IBone headBone2 = processor.getBone("head");
            Action action2 = entity.getCurrentAction();
            if ((action2 == Action.BREEDING_SLOW_2 || action2 == Action.BREEDING_FAST_2 || action2 == Action.BREEDING_CUM_2) && this.mc.gameSettings.thirdPersonView == 0) {
               bodyBone2.setPositionY(bodyBone2.getPositionY() + 1.5F);
            }

            IGoblin goblin2 = (IGoblin)entity;
            if (action2 == Action.AWAIT_PICK_UP || action2 == Action.VANISH) {
               this.updateBoneLook(entity, bodyBone2, headBone2);
            }

            if (action2 == Action.SIT) {
               this.updateBoneLook(entity, headBone2);
            }

            if (action2 == Action.START_THROWING) {
               if (this.mc.player.getPersistentID().equals(goblin2.getOwnerUUID())) {
                  this.applyGoblinBone(bodyBone2, processor, entity, goblin2);
               } else {
                  this.applyBoneState(bodyBone2, processor, entity);
               }
            } else {
               bodyBone2.setHidden(false);
            }

            if (!bodyBone2.isHidden() && action2 == Action.START_THROWING || action2 == Action.THROWN) {
               Vec3d interpolatedPos2 = getInterpolatedPosition(entity);
               bodyBone2.setRotationX((float)interpolatedPos2.x);
               bodyBone2.setPositionY((float)interpolatedPos2.y);
               bodyBone2.setPositionZ((float)interpolatedPos2.z);
            }

            if (action2 == Action.START_THROWING || action2 == Action.PICK_UP) {
               this.updateThrowPose(processor, goblin2, entity);
            }
         } else {
            IBone preggyBone = processor.getBone("preggy");
            preggyBone.setHidden(!(Boolean)entity.getDataManager().get(GoblinEntity.aV));
            IBone bodyBone = processor.getBone("body");
            IBone headBone = processor.getBone("head");
            Action action = entity.getCurrentAction();
            if ((action == Action.BREEDING_SLOW_2 || action == Action.BREEDING_FAST_2 || action == Action.BREEDING_CUM_2) && this.mc.gameSettings.thirdPersonView == 0) {
               bodyBone.setPositionY(bodyBone.getPositionY() + 1.5F);
            }

            IGoblin goblin = (IGoblin)entity;
            if (action == Action.VANISH) {
               this.updateBoneLook(entity, bodyBone, headBone);
            }

            if (action == Action.START_THROWING) {
               if (this.mc.player.getPersistentID().equals(goblin.getOwnerUUID())) {
                  this.applyGoblinBone(bodyBone, processor, entity, goblin);
               } else {
                  this.applyBoneState(bodyBone, processor, entity);
               }
            } else {
               bodyBone.setHidden(false);
            }

            if (!bodyBone.isHidden() && action == Action.START_THROWING || action == Action.THROWN) {
               Vec3d interpolatedPos = getInterpolatedPosition(entity);
               bodyBone.setRotationX((float)interpolatedPos.x);
               bodyBone.setPositionY((float)interpolatedPos.y);
               bodyBone.setPositionZ((float)interpolatedPos.z);
            }

            if (action == Action.START_THROWING || action == Action.PICK_UP) {
               this.updateThrowPose(processor, goblin, entity);
            }

            this.updateWalkPose(processor, entity);
            this.updateIdlePose(processor, entity);
         }
      }
   }

   /**
    * Hides the body entirely when the owner's own goblin is being thrown in
    * first person (the POV path renders it instead).
    */
   void updateIdlePose(AnimationProcessor processor, BaseGirlEntity goblin) {
      if (goblin.getCurrentAction() == Action.START_THROWING) {
         if (this.mc.gameSettings.thirdPersonView == 0 && this.mc.player.getPersistentID().equals(((AbstractPlayerGirlEntity)goblin).getOwnerUserUUID())) {
            IBone bodyBone = processor.getBone("body");
            if (bodyBone != null) {
               bodyBone.setHidden(true);
            }
         }
      }
   }

   /**
    * While the goblin is picked up, onlookers see it lowered by 32 px (dangling
    * in the owner's hands); the owner in first person keeps the POV placement.
    */
   void updateWalkPose(AnimationProcessor processor, BaseGirlEntity goblin) {
      if (goblin.getCurrentAction() == Action.PICK_UP) {
         if (this.mc.gameSettings.thirdPersonView != 0 || !this.mc.player.getPersistentID().equals(((IGoblin)goblin).getOwnerUUID())) {
            IBone bodyBone = processor.getBone("body");
            if (bodyBone != null) {
               IBone steveBone = processor.getBone("steve");
               if (steveBone != null) {
                  bodyBone.setPositionY(bodyBone.getPositionY() - 32.0F);
                  steveBone.setPositionY(steveBone.getPositionY() - 32.0F);
               }
            }
         }
      }
   }

   /**
    * Throw/catch leg swing: the goblin's legs swing opposite the owner's
    * interpolated limb swing (60-degree amplitude) while throwing or being
    * picked up.
    */
   void updateThrowPose(AnimationProcessor processor, IGoblin goblin, BaseGirlEntity entity) {
      UUID ownerUuid = goblin.getOwnerUUID();
      if (ownerUuid != null) {
         EntityPlayer owner = entity.world.getPlayerEntityByUUID(ownerUuid);
         if (owner != null) {
            float limbSwingAmount = RotationHelper.lerp(owner.prevLimbSwingAmount, owner.limbSwingAmount, this.mc.getRenderPartialTicks());
            float limbSwing = owner.limbSwing;
            float swingSin = (float)Math.sin(limbSwing);
            IBone leftLegBone = processor.getBone("LeftLeg");
            IBone rightLegBone = processor.getBone("RightLeg");
            float swingAngle = TrigMath.wrapDegrees(60.0F * swingSin * limbSwingAmount);
            leftLegBone.setRotationX(swingAngle);
            rightLegBone.setRotationX(-swingAngle);
         }
      } else {
         entity.getInteractionPlayerUUID();
      }
   }

   /**
    * Head-only look at the nearest player within 15 blocks, gated by the
    * goblin's facing (only looks when the player is in front of it).
    */
   void updateBoneLook(BaseGirlEntity entity, IBone headBone) {
      EntityPlayer player = entity.world.getClosestPlayerToEntity(entity, 15.0);
      if (player != null) {
         Vec3d playerPos = player.getPositionVector();
         Vec3d entityPos = entity.getPositionVector();
         Vec3d delta = playerPos.subtract(entityPos);
         float yaw = entity.rotationYaw;
         boolean inFront = false;
         switch ((int)yaw) {
            case -90:
               inFront = playerPos.x > entityPos.x;
               break;
            case 0:
               inFront = playerPos.z > entityPos.z;
               break;
            case 90:
               inFront = playerPos.x < entityPos.x;
               break;
            case 180:
               inFront = playerPos.z < entityPos.z;
         }

         if (!inFront) {
            headBone.setRotationY(0.0F);
         } else {
            float facingOffset = 0.0F;
            switch ((int)yaw) {
               case 0:
                  facingOffset = -90.0F;
                  break;
               case 90:
                  facingOffset = 180.0F;
                  break;
               case 180:
                  facingOffset = 90.0F;
            }

            float yawAngle = (float)(-(MathHelper.atan2(delta.z, delta.x) * (180.0 / Math.PI) + facingOffset));
            float pitch = ThreadNames.clampFloat((float)(player.getEyeHeight() + playerPos.y - (entity.getEyeHeight() + entityPos.y)), -0.75F, 0.75F);
            headBone.setRotationY(TrigMath.wrapDegrees(yawAngle));
            headBone.setRotationX(pitch);
         }
      }
   }

   /**
    * Body+head look at the nearest player (vanishing pose): body yaw + head
    * pitch, clamped by the height difference.
    */
   void updateBoneLook(BaseGirlEntity entity, IBone bodyBone, IBone headBone) {
      EntityPlayer player = entity.world.getClosestPlayerToEntity(entity, 15.0);
      if (player != null) {
         Vec3d playerPos = player.getPositionVector();
         Vec3d entityPos = entity.getPositionVector();
         Vec3d delta = playerPos.subtract(entityPos);
         float yaw = (float)(-(Math.atan2(delta.z, delta.x) * (180.0 / Math.PI))) + 90.0F;
         float pitch = ThreadNames.clampFloat((float)(player.getEyeHeight() + playerPos.y - (entity.getEyeHeight() + entityPos.y)), -0.75F, 0.75F);
         bodyBone.setRotationY(TrigMath.wrapDegrees(yaw));
         headBone.setRotationX(pitch);
      }
   }

   /**
    * Start-throw body state for non-owners: locally registered goblins hide
    * the body; real ones show it with the steve skin hidden.
    */
   void applyBoneState(IBone bodyBone, AnimationProcessor processor, BaseGirlEntity goblin) {
      if (goblin.isLocallyRegistered()) {
         bodyBone.setHidden(true);
      } else {
         bodyBone.setHidden(false);
         processor.getBone("steve").setHidden(true);
      }
   }

   /**
    * Start-throw body state for the owner: body hidden for locally registered
    * previews, else hidden until the throw progress passes 15 ticks; steve
    * skin always hidden during the throw.
    */
   void applyGoblinBone(IBone bodyBone, AnimationProcessor processor, BaseGirlEntity goblin, IGoblin goblinApi) {
      if (goblin.isLocallyRegistered()) {
         bodyBone.setHidden(true);
      } else {
         bodyBone.setHidden(goblinApi.getThrowProgress() < 15);
      }

      if (!goblin.isLocallyRegistered()) {
         processor.getBone("steve").setHidden(true);
      }
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] TopArmor() {
      return new String[]{"armorBoobL", "armorBoobR"};
   }

   @Override
   public String[] Top() {
      return new String[]{"nippleL", "nippleR"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{"armorCheekR", "armorCheekL", "armorLegL", "armorLegR", "armorShinL", "armorShinR", "armorTorso"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"fuckhole", "vagina", "meatCheekR", "meatCheekL", "meatLegL", "meatLegR", "meatShinL", "meatShinR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorFootL", "armorFootR"};
   }

   @Override
   public String[] Shoes() {
      return new String[]{"meatFootL", "meatFootR"};
   }

}
