package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.model.api.IGirlModelInfo;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

/**
 * Base geckolib model for the girl entities. Handles: outfit-index texture
 * selection, per-frame animation events (head look, anchored position pinning,
 * action-transition timing), the steve/Alex arm-skin variants, outfit armor
 * bone visibility, and the static camera-bone placement list.
 * <p>
 * <b>Camera placements.</b> {@link #CAMERA_PLACEMENTS} ({@code boyCam},
 * {@code girlCam}) names the bones whose world positions the renderer
 * publishes each frame — the scene camera attaches to these; keep the list in
 * sync with the geo models.
 * <p>
 * <b>Anchored pinning.</b> {@link #setLivingAnimations} repositions an
 * anchored girl directly to her target position (client-side render smoothing)
 * and drives the animation controller's transition length from the current
 * {@code Action}. Skipped in the {@link SexWorldClient} preload world.
 * <p>
 * <b>Outfit bones.</b> {@link #applyHeldItems} toggles armor vs. nude bone
 * groups from the girl's armor slots ({@link IGirlModelInfo} bone lists);
 * {@link #getItemStackForBone} maps armor bones back to their slots (used for
 * armor tinting).
 * <p>
 * CLIENT-side only.
 */
public abstract class GirlModel<T extends BaseGirlEntity> extends GirlModelBase<T> implements IGirlModelInfo {
   public static final List<String> BRA_STRING_BONES = Arrays.asList(
      "braStringMidStartR",
      "braStringMidMid1R",
      "braStringMidMid2R",
      "braStringMidMid3R",
      "braStringMidEndR",
      "braStringBackR",
      "braStringRightEndR",
      "braStringRightStartR",
      "braStringRightL",
      "braStringMidMid1L",
      "braStringMidMid2L",
      "braStringMidMid3L",
      "braStringMidEndL",
      "braStringBackL",
      "braStringLeftEndL",
      "braStringLeftStartL",
      "braStringMidStartL",
      "braStringRightR"
   );
   public static final List<String> CAMERA_PLACEMENTS = Arrays.asList("boyCam", "girlCam");
   public static boolean enableModelCache = true;
   protected ResourceLocation[] modelLocations = this.getModelLocations();
   protected Minecraft mc = Minecraft.getMinecraft();

   protected GirlModel() {
   }

   protected abstract ResourceLocation[] getModelLocations();

   public ResourceLocation getTextureLocation() { return this.getTextureLocation((BaseGirlEntity) null); }

   @Override
   public ResourceLocation getModelLocation(BaseGirlEntity entity) {
      return this.getSexWorldTexture(entity);
   }

   public abstract ResourceLocation getTextureLocation(BaseGirlEntity entity);
   public String[] HeadArmor() { return new String[0]; }
   public String[] Attachments() { return new String[0]; }
   public String[] TopArmor() { return new String[0]; }
   public String[] Top() { return new String[0]; }
   public String[] BottomArmor() { return new String[0]; }
   public String[] Bottom() { return new String[0]; }
   public String[] ShoesArmor() { return new String[0]; }
   public String[] Shoes() { return new String[0]; }

   public ResourceLocation getTextureForGirl(BaseGirlEntity entity) {
      return this.getTextureLocation(entity);
   }

   /**
    * Model location by outfit index: the preload world always uses outfit 0;
    * an out-of-range index falls back to outfit 0 (nude) with a console
    * notice.
    */
   public ResourceLocation getSexWorldTexture(BaseGirlEntity entity) {
      if (entity.world instanceof SexWorldClient) {
         return this.modelLocations[0];
      } else if ((Integer)entity.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) >= this.modelLocations.length) {
         // original jar had '>' here — OOB at index == length; '>=' guards the last index
         System.out.println("Girl doesn't have an outfit Nr." + entity.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) + " so im just making her nude lol");
         return this.modelLocations[0];
      } else {
         return this.modelLocations[entity.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX)];
      }
   }

   public ResourceLocation getDefaultTexture(BaseGirlEntity entity) {
      return this.getTextureLocation();
   }

   @Override
   public void setMolangQueries(IAnimatable animatable, double queryTick) {
      if (Minecraft.getMinecraft().world != null) {
         super.setMolangQueries(animatable, queryTick);
      }
   }

   /**
    * Per-frame animation entry (CLIENT-side): runs the standard geckolib
    * molang/animation pass, then the arm-skin variant + outfit bone handling,
    * pins anchored girls to their target position, syncs the controller's
    * transition length to the current action, and dispatches the
    * {@link #handleAnimationEvent} head-look. Skipped entirely in the
    * {@link SexWorldClient} preload world.
    */
   @Override
   public void setLivingAnimations(T entity, Integer uniqueID, AnimationEvent event) {
      super.setLivingAnimations((T)entity, uniqueID, event);
      AnimationProcessor processor = this.getAnimationProcessor();
      this.animateGirl((T)entity, processor);
      if (!(entity.world instanceof SexWorldClient)) {
         if ((Boolean)entity.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
            entity.setPositionAndRotationDirect(
               entity.getTargetPosition().x, entity.getTargetPosition().y, entity.getTargetPosition().z, entity.getYawRotation(), 0.0F, 3, true
            );
         }

         if (entity.actionController != null) {
            entity.actionController.transitionLengthTicks = !(entity.world instanceof SexWorldClient) && entity.getCurrentAction() != null ? entity.getCurrentAction().transitionTick : 5.0;
         }

         this.handleAnimationEvent((T)entity, processor, event);
         if (entity instanceof AbstractGirlNpcEntity && !entity.isLocallyRegistered() && entity.getOutfitIndex() != 0) {
            this.applyHeldItems(
               processor,
               (ItemStack)entity.entityDataManager.get(AbstractGirlNpcEntity.HELMET_SLOT),
               (ItemStack)entity.entityDataManager.get(AbstractGirlNpcEntity.CHEST_SLOT),
               (ItemStack)entity.entityDataManager.get(AbstractGirlNpcEntity.LEGS_SLOT),
               (ItemStack)entity.entityDataManager.get(AbstractGirlNpcEntity.BOOTS_SLOT)
            );
         } else {
            this.renderAllBones(processor);
         }
      }
   }

   public static Vec3d getInterpolatedPosition(BaseGirlEntity girl) {
      return lerpPositions(new Vec3d(girl.lastTickPosX, girl.lastTickPosY, girl.lastTickPosZ), girl.getPositionVector());
   }

   public static Vec3d getBoneOffsetWorld(BaseGirlEntity girl, Vec3d offset) {
      return lerpPositions(offset, girl.getPositionVector());
   }

   /**
    * Direction-dependent pose math used to aim camera/bone offsets: maps the
    * vector between two positions onto a yaw (-180..0), a head-roll and a
    * bob/offset value, with NaN guards. Used by
    * {@link #getInterpolatedPosition}/{@link #getBoneOffsetWorld}.
    */
   public static Vec3d lerpPositions(Vec3d from, Vec3d to) {
      Vec3d delta = to.subtract(from);
      Vec3d absDelta = new Vec3d(Math.abs(delta.x), Math.abs(delta.y), Math.abs(delta.z));
      double xRatio = absDelta.x / (absDelta.x + absDelta.y + absDelta.z);
      double yRatio = absDelta.y / (absDelta.x + absDelta.y + absDelta.z);
      double zRatio = absDelta.z / (absDelta.x + absDelta.y + absDelta.z);
      Vec3d directionVec = new Vec3d(
         (delta.x > 0.0 ? 1 : -1) * xRatio, (delta.y > 0.0 ? 1 : -1) * yRatio, (delta.z > 0.0 ? 1 : -1) * zRatio
      );
      double verticalRatio = directionVec.y / 2.0 + 0.5;
      float yaw = (float)RotationHelper.lerpDouble(-180.0, 0.0, verticalRatio);
      if (Float.isNaN(yaw)) {
         yaw = -90.0F;
      }

      float roll = verticalRatio < 0.5 ? 0.0F : (float)RotationHelper.lerpDouble(0.0, 16.0, -verticalRatio);
      if (Float.isNaN(roll)) {
         roll = 0.0F;
      }

      float bob = (float)(4.0 - Math.sin((Math.PI / 2) + verticalRatio * 2.0 * Math.PI) * 4.0);
      if (Float.isNaN(bob)) {
         bob = 8.0F;
      }

      return new Vec3d(TrigMath.wrapDegrees(yaw), roll, bob);
   }

   /**
    * Applies the girl's armor visibility from her equipped armor slots:
    * each armor slot toggles the armor bone group on and the nude variant off.
    */
   void applyHeldItems(AnimationProcessor<T> processor, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
      this.renderHeadArmor(processor, !helmet.isEmpty());
      this.renderTopArmor(processor, chest.getItem() instanceof ItemArmor);
      this.renderBottomArmor(processor, !legs.isEmpty());
      this.renderShoesArmor(processor, !boots.isEmpty());
   }

   protected void renderAllBones(AnimationProcessor<T> processor) {
      this.renderHeadArmor(processor, false);
      this.renderTopArmor(processor, false);
      this.renderBottomArmor(processor, false);
      this.renderShoesArmor(processor, false);
   }

   void renderHeadArmor(AnimationProcessor processor, boolean armorVisible) {
      this.renderBoneGroup(this.HeadArmor(), armorVisible, processor);
      this.renderBoneGroup(this.Attachments(), !armorVisible, processor);
   }

   void renderTopArmor(AnimationProcessor<T> processor, boolean armorVisible) {
      this.renderBoneGroup(this.TopArmor(), armorVisible, processor);
      this.renderBoneGroup(this.Top(), !armorVisible, processor);
   }

   void renderBottomArmor(AnimationProcessor<T> processor, boolean armorVisible) {
      this.renderBoneGroup(this.BottomArmor(), armorVisible, processor);
      this.renderBoneGroup(this.Bottom(), !armorVisible, processor);
   }

   void renderShoesArmor(AnimationProcessor<T> processor, boolean armorVisible) {
      this.renderBoneGroup(this.ShoesArmor(), armorVisible, processor);
      this.renderBoneGroup(this.Shoes(), !armorVisible, processor);
   }

   void renderBoneGroup(String[] boneNames, boolean visible, AnimationProcessor<T> processor) {
      for (String boneName : boneNames) {
         this.renderSingleBone(boneName, visible, processor);
      }
   }

   void renderSingleBone(String boneName, boolean visible, AnimationProcessor<T> processor) {
      if (processor.getBone(boneName) != null) {
         processor.getBone(boneName).setHidden(!visible);
      }
   }

   /**
    * Whether the girl's skin variant is the vanilla default (Steve) — the
    * Alex-variant arms are hidden otherwise.
    */
   protected boolean canRender(T girl) {
      UUID uuid = girl.getInteractionPlayerUUID();
      if (uuid == null) {
         return true;
      }

      World world = girl.world;
      AbstractClientPlayer player = (AbstractClientPlayer)world.getPlayerEntityByUUID(uuid);
      return player == null ? true : "default".equals(player.getSkinType());
   }

   /**
    * Arm-skin variant bones: shows the Steve (or Alex) arm set depending on
    * the interaction player's skin type, and hides the whole {@code steve}
    * body bone while the current action has no player.
    */
   void animateGirl(T girl, AnimationProcessor<T> processor) {
      boolean isDefaultSkin = this.canRender((T)girl);
      if (isDefaultSkin) {
         processor.getBone("rightArmAlex").setHidden(isDefaultSkin);
         processor.getBone("rightLowerArmAlex").setHidden(isDefaultSkin);
         IBone rightArmSteveBone2 = processor.getBone("rightArmSteve");
         rightArmSteveBone2.setHidden(false);
         IBone rightLowerArmSteveBone2 = processor.getBone("rightLowerArmSteve");
         rightLowerArmSteveBone2.setHidden(false);
         processor.getBone("leftArmAlex").setHidden(isDefaultSkin);
         processor.getBone("leftLowerArmAlex").setHidden(isDefaultSkin);
         IBone leftArmSteveBone2 = processor.getBone("leftArmSteve");
         leftArmSteveBone2.setHidden(false);
         IBone leftLowerArmSteveBone2 = processor.getBone("leftLowerArmSteve");
         leftLowerArmSteveBone2.setHidden(false);
         IBone steveBone2 = processor.getBone("steve");
         if (steveBone2 != null) {
            steveBone2.setHidden(!girl.getCurrentAction().hasPlayer);
         }
      } else {
         processor.getBone("rightArmAlex").setHidden(isDefaultSkin);
         processor.getBone("rightLowerArmAlex").setHidden(isDefaultSkin);
         IBone rightArmSteveBone = processor.getBone("rightArmSteve");
         rightArmSteveBone.setHidden(true);
         IBone rightLowerArmSteveBone = processor.getBone("rightLowerArmSteve");
         rightLowerArmSteveBone.setHidden(true);
         processor.getBone("leftArmAlex").setHidden(isDefaultSkin);
         processor.getBone("leftLowerArmAlex").setHidden(isDefaultSkin);
         IBone leftArmSteveBone = processor.getBone("leftArmSteve");
         leftArmSteveBone.setHidden(true);
         IBone leftLowerArmSteveBone = processor.getBone("leftLowerArmSteve");
         leftLowerArmSteveBone.setHidden(true);
         IBone steveBone = processor.getBone("steve");
         if (steveBone != null) {
            steveBone.setHidden(!girl.getCurrentAction().hasPlayer);
         }
      }
   }

   protected boolean shouldRender(T girl) {
      return true;
   }

   /**
    * Head-look bones for the idle/attack/bow actions: neck takes half the
    * head yaw, head takes full yaw + pitch, body straightens. Other actions
    * (scenes) drive the head via the animation controller instead.
    */
   protected void handleAnimationEvent(T girl, AnimationProcessor<T> processor, AnimationEvent event) {
      if (!(girl.world instanceof SexWorldClient)) {
         if (this.shouldRender(girl)) {
            if (girl.getCurrentAction() == Action.NULL || girl.getCurrentAction() == Action.ATTACK || girl.getCurrentAction() == Action.BOW) {
               EntityModelData modelData = (EntityModelData) event.getExtraDataOfType(EntityModelData.class).get(0);
               IBone neckBone = processor.getBone("neck");
               neckBone.setRotationY(modelData.netHeadYaw * 0.5F * (float) (Math.PI / 180.0));
               IBone headBone = processor.getBone("head");
               headBone.setRotationY(modelData.netHeadYaw * (float) (Math.PI / 180.0));
               headBone.setRotationX(modelData.headPitch * (float) (Math.PI / 180.0));
               IBone bodyBone = processor.getBone("body") == null ? processor.getBone("dd") : processor.getBone("body");
               bodyBone.setRotationY(0.0F);
            }
         }
      }
   }

   /**
    * Maps an armor bone name back to the girl's armor slot (helmet/chest/
    * legs/boots) — used by the renderer's armor tinting.
    */
   public ItemStack getItemStackForBone(BaseGirlEntity girl, String boneName) {
      if (Arrays.asList(this.HeadArmor()).contains(boneName)) {
         return (ItemStack)girl.entityDataManager.get(AbstractGirlNpcEntity.HELMET_SLOT);
      } else if (Arrays.asList(this.TopArmor()).contains(boneName)) {
         return (ItemStack)girl.entityDataManager.get(AbstractGirlNpcEntity.CHEST_SLOT);
      } else if (Arrays.asList(this.BottomArmor()).contains(boneName)) {
         return (ItemStack)girl.entityDataManager.get(AbstractGirlNpcEntity.LEGS_SLOT);
      } else {
         return Arrays.asList(this.ShoesArmor()).contains(boneName) ? (ItemStack)girl.entityDataManager.get(AbstractGirlNpcEntity.BOOTS_SLOT) : ItemStack.EMPTY;
      }
   }

}
