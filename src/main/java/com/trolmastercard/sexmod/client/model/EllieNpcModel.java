package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.ThreadNames;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * Geckolib model for the Ellie NPC: nude/dressed outfit geo files, plus a
 * custom sitting pose — while Ellie sits idle, her head tracks the nearest
 * player within 15 blocks (yaw clamped per facing, pitch by height
 * difference). Outfit bone lists define the armor/nude variants.
 */
public class EllieNpcModel extends GirlModel<BaseGirlEntity> {
   HashMap<Integer, float[]> headYawOffsets = new HashMap<Integer, float[]>() {
      {
         this.put(0, new float[]{0.0F, -1.2F, 1.2F});
         this.put(-90, new float[]{2.0F, -71.56F, -68.0F});
         this.put(90, new float[]{-2.0F, 68.0F, 70.5F});
      }
   };

   public EllieNpcModel() {
      this.modelLocations = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{new ResourceLocation("sexmod", "geo/ellie/nude.geo.json"), new ResourceLocation("sexmod", "geo/ellie/dressed.geo.json")};
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "textures/entity/ellie/ellie.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity entity) {
      return new ResourceLocation("sexmod", "animations/ellie/ellie.animation.json");
   }

   /**
    * Sitting head-tracking: only for the plain NPC (not player-girls) in the
    * SITDOWNIDLE action; the head yaw aims at the nearest player with
    * per-facing clamps (180-degree back-facing has its own arc), pitch from
    * the player's height difference.
    */
   @Override
   public void setLivingAnimations(BaseGirlEntity entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations(entity, uniqueID, event);
      if (!(entity.world instanceof SexWorldClient)) {
         if (!(entity instanceof AbstractPlayerGirlEntity)) {
            if (entity.getCurrentAction() == Action.SITDOWNIDLE) {
               EntityPlayer nearestPlayer = entity.world.getClosestPlayerToEntity(entity, 15.0);
               if (nearestPlayer != null) {
                  IBone headBone = this.getAnimationProcessor().getBone("head");
                  Vec3d toPlayer = entity.getPositionVector().subtract(nearestPlayer.getPositionVector());
                  int facingYaw = Math.round(entity.getYawRotation());
                  float headYaw;
                  if (facingYaw == 180) {
                     headYaw = (float)Math.atan2(toPlayer.x, toPlayer.z) * 1.2F;
                     if (headYaw > 0.0F) {
                        headYaw = Math.max(1.5F, Math.min(3.14F, headYaw));
                     } else {
                        headYaw = Math.max(-3.14F, Math.min(-1.5F, headYaw));
                     }

                     if (headYaw != 1.5F && headYaw != 3.14F && headYaw != -3.14F && headYaw != -1.5F) {
                        headYaw += 3.0F;
                     } else {
                        headYaw = 0.0F;
                     }
                  } else {
                     float minClamp = this.headYawOffsets.get(facingYaw)[1];
                     float maxClamp = this.headYawOffsets.get(facingYaw)[2];
                     headYaw = ((float)(Math.atan2(toPlayer.x, toPlayer.z) + this.headYawOffsets.get(facingYaw)[0]) + entity.getYawRotation()) * 0.8F;
                     headYaw = (float)ThreadNames.clampDouble(headYaw, minClamp, maxClamp);
                     if (headYaw == minClamp || headYaw == maxClamp) {
                        headYaw = 0.0F;
                     }
                  }

                  float headPitch = headYaw == 0.0F ? 0.0F : ThreadNames.clampFloat((float)((nearestPlayer.posY - entity.posY) * 0.5), -0.75F, 0.75F);
                  headBone.setRotationY(headYaw);
                  headBone.setRotationX(headPitch);
               }
            }
         }
      }
   }

   @Override
   public String[] HeadArmor() {
      return new String[]{"armorHelmet"};
   }

   @Override
   public String[] Attachments() {
      return new String[]{"headband"};
   }

   @Override
   public String[] TopArmor() {
      return new String[]{"armorShoulderR", "armorShoulderL", "armorChest", "armorBoobs"};
   }

   @Override
   public String[] Top() {
      return new String[]{"boobsFlesh", "upperBodyL", "upperBodyR"};
   }

   @Override
   public String[] BottomArmor() {
      return new String[]{"armorBootyR", "armorBootyL", "armorPantsLowL", "armorPantsLowR", "armorPantsLowR", "armorPantsUpR", "armorPantsUpL", "armorHip"};
   }

   @Override
   public String[] Bottom() {
      return new String[]{"fleshL", "fleshR", "vagina", "hotpants", "slip", "curvesL", "curvesR", "kneeL", "kneeR"};
   }

   @Override
   public String[] ShoesArmor() {
      return new String[]{"armorShoesL", "armorShoesR"};
   }

}
