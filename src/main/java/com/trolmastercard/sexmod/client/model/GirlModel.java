package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.model.api.IGirlModelInfo;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.TrailSegment;
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
   public ResourceLocation getModelLocation(BaseGirlEntity var0) {
      return this.getSexWorldTexture(var0);
   }

   public abstract ResourceLocation getTextureLocation(BaseGirlEntity var0);
   public String[] HeadArmor() { return new String[0]; }
   public String[] Attachments() { return new String[0]; }
   public String[] TopArmor() { return new String[0]; }
   public String[] Top() { return new String[0]; }
   public String[] BottomArmor() { return new String[0]; }
   public String[] Bottom() { return new String[0]; }
   public String[] ShoesArmor() { return new String[0]; }
   public String[] Shoes() { return new String[0]; }

   public ResourceLocation getTextureForGirl(BaseGirlEntity var1) {
      return this.getTextureLocation(var1);
   }

   /**
    * Model location by outfit index: the preload world always uses outfit 0;
    * an out-of-range index falls back to outfit 0 (nude) with a console
    * notice.
    */
   public ResourceLocation getSexWorldTexture(BaseGirlEntity var1) {
      if (var1.world instanceof SexWorldClient) {
         return this.modelLocations[0];
      } else if ((Integer)var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) > this.modelLocations.length) {
         System.out.println("Girl doesn't have an outfit Nr." + var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX) + " so im just making her nude lol");
         return this.modelLocations[0];
      } else {
         return this.modelLocations[var1.getDataManager().get(BaseGirlEntity.OUTFIT_INDEX)];
      }
   }

   public ResourceLocation getDefaultTexture(BaseGirlEntity var1) {
      return this.getTextureLocation();
   }

   @Override
   public void setMolangQueries(IAnimatable var1, double var2) {
      if (Minecraft.getMinecraft().world != null) {
         super.setMolangQueries(var1, var2);
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
   public void setLivingAnimations(T var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations((T)var1, var2, var3);
      AnimationProcessor var4 = this.getAnimationProcessor();
      this.animateGirl((T)var1, var4);
      if (!(var1.world instanceof SexWorldClient)) {
         if ((Boolean)var1.getDataManager().get(BaseGirlEntity.IS_ANCHORED)) {
            var1.setPositionAndRotationDirect(
               var1.getTargetPosition().x, var1.getTargetPosition().y, var1.getTargetPosition().z, var1.getYawRotation(), 0.0F, 3, true
            );
         }

         if (var1.actionController != null) {
            var1.actionController.transitionLengthTicks = !(var1.world instanceof SexWorldClient) && var1.getCurrentAction() != null ? var1.getCurrentAction().transitionTick : 5.0;
         }

         this.handleAnimationEvent((T)var1, var4, var3);
         if (var1 instanceof AbstractGirlNpcEntity && !var1.isLocallyRegistered() && var1.getOutfitIndex() != 0) {
            this.applyHeldItems(
               var4,
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.HELMET_SLOT),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.CHEST_SLOT),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.LEGS_SLOT),
               (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.BOOTS_SLOT)
            );
         } else {
            this.renderAllBones(var4);
         }
      }
   }

   public static Vec3d getInterpolatedPosition(BaseGirlEntity var0) {
      return lerpPositions(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector());
   }

   public static Vec3d getBoneOffsetWorld(BaseGirlEntity var0, Vec3d var1) {
      return lerpPositions(var1, var0.getPositionVector());
   }

   /**
    * Direction-dependent pose math used to aim camera/bone offsets: maps the
    * vector between two positions onto a yaw (-180..0), a head-roll and a
    * bob/offset value, with NaN guards. Used by
    * {@link #getInterpolatedPosition}/{@link #getBoneOffsetWorld}.
    */
   public static Vec3d lerpPositions(Vec3d var0, Vec3d var1) {
      Vec3d var2 = var1.subtract(var0);
      Vec3d var3 = new Vec3d(Math.abs(var2.x), Math.abs(var2.y), Math.abs(var2.z));
      double var4 = var3.x / (var3.x + var3.y + var3.z);
      double var6 = var3.y / (var3.x + var3.y + var3.z);
      double var8 = var3.z / (var3.x + var3.y + var3.z);
      Vec3d var10 = new Vec3d(
         (var2.x > 0.0 ? 1 : -1) * var4, (var2.y > 0.0 ? 1 : -1) * var6, (var2.z > 0.0 ? 1 : -1) * var8
      );
      double var11 = var10.y / 2.0 + 0.5;
      float var13 = (float)RotationHelper.lerpDouble(-180.0, 0.0, var11);
      if (Float.isNaN(var13)) {
         var13 = -90.0F;
      }

      float var14 = var11 < 0.5 ? 0.0F : (float)RotationHelper.lerpDouble(0.0, 16.0, -var11);
      if (Float.isNaN(var14)) {
         var14 = 0.0F;
      }

      float var15 = (float)(4.0 - Math.sin((Math.PI / 2) + var11 * 2.0 * Math.PI) * 4.0);
      if (Float.isNaN(var15)) {
         var15 = 8.0F;
      }

      return new Vec3d(TrigMath.wrapDegrees(var13), var14, var15);
   }

   /**
    * Applies the girl's armor visibility from her equipped armor slots:
    * each armor slot toggles the armor bone group on and the nude variant off.
    */
   void applyHeldItems(AnimationProcessor<T> var1, ItemStack var2, ItemStack var3, ItemStack var4, ItemStack var5) {
      this.renderHeadArmor(var1, !var2.isEmpty());
      this.renderTopArmor(var1, var3.getItem() instanceof ItemArmor);
      this.renderBottomArmor(var1, !var4.isEmpty());
      this.renderShoesArmor(var1, !var5.isEmpty());
   }

   protected void renderAllBones(AnimationProcessor<T> var1) {
      this.renderHeadArmor(var1, false);
      this.renderTopArmor(var1, false);
      this.renderBottomArmor(var1, false);
      this.renderShoesArmor(var1, false);
   }

   void renderHeadArmor(AnimationProcessor var1, boolean var2) {
      this.renderBoneGroup(this.HeadArmor(), var2, var1);
      this.renderBoneGroup(this.Attachments(), !var2, var1);
   }

   void renderTopArmor(AnimationProcessor<T> var1, boolean var2) {
      this.renderBoneGroup(this.TopArmor(), var2, var1);
      this.renderBoneGroup(this.Top(), !var2, var1);
   }

   void renderBottomArmor(AnimationProcessor<T> var1, boolean var2) {
      this.renderBoneGroup(this.BottomArmor(), var2, var1);
      this.renderBoneGroup(this.Bottom(), !var2, var1);
   }

   void renderShoesArmor(AnimationProcessor<T> var1, boolean var2) {
      this.renderBoneGroup(this.ShoesArmor(), var2, var1);
      this.renderBoneGroup(this.Shoes(), !var2, var1);
   }

   void renderBoneGroup(String[] var1, boolean var2, AnimationProcessor<T> var3) {
      for (String var7 : var1) {
         this.renderSingleBone(var7, var2, var3);
      }
   }

   void renderSingleBone(String var1, boolean var2, AnimationProcessor<T> var3) {
      if (var3.getBone(var1) != null) {
         var3.getBone(var1).setHidden(!var2);
      }
   }

   /**
    * Whether the girl's skin variant is the vanilla default (Steve) — the
    * Alex-variant arms are hidden otherwise.
    */
   protected boolean canRender(T var1) {
      UUID var2 = var1.getInteractionPlayerUUID();
      if (var2 == null) {
         return true;
      }

      World var3 = var1.world;
      AbstractClientPlayer var4 = (AbstractClientPlayer)var3.getPlayerEntityByUUID(var2);
      return var4 == null ? true : "default".equals(var4.getSkinType());
   }

   /**
    * Arm-skin variant bones: shows the Steve (or Alex) arm set depending on
    * the interaction player's skin type, and hides the whole {@code steve}
    * body bone while the current action has no player.
    */
   void animateGirl(T var1, AnimationProcessor<T> var2) {
      boolean var3 = this.canRender((T)var1);
      if (var3) {
         var2.getBone("rightArmAlex").setHidden(var3);
         var2.getBone("rightLowerArmAlex").setHidden(var3);
         IBone var10 = var2.getBone("rightArmSteve");
         var10.setHidden(false);
         IBone var11 = var2.getBone("rightLowerArmSteve");
         var11.setHidden(false);
         var2.getBone("leftArmAlex").setHidden(var3);
         var2.getBone("leftLowerArmAlex").setHidden(var3);
         IBone var12 = var2.getBone("leftArmSteve");
         var12.setHidden(false);
         IBone var13 = var2.getBone("leftLowerArmSteve");
         var13.setHidden(false);
         IBone var9 = var2.getBone("steve");
         if (var9 != null) {
            var9.setHidden(!var1.getCurrentAction().hasPlayer);
         }
      } else {
         var2.getBone("rightArmAlex").setHidden(var3);
         var2.getBone("rightLowerArmAlex").setHidden(var3);
         IBone var5 = var2.getBone("rightArmSteve");
         var5.setHidden(true);
         IBone var6 = var2.getBone("rightLowerArmSteve");
         var6.setHidden(true);
         var2.getBone("leftArmAlex").setHidden(var3);
         var2.getBone("leftLowerArmAlex").setHidden(var3);
         IBone var7 = var2.getBone("leftArmSteve");
         var7.setHidden(true);
         IBone var8 = var2.getBone("leftLowerArmSteve");
         var8.setHidden(true);
         IBone var4 = var2.getBone("steve");
         if (var4 != null) {
            var4.setHidden(!var1.getCurrentAction().hasPlayer);
         }
      }
   }

   protected boolean shouldRender(T var1) {
      return true;
   }

   /**
    * Head-look bones for the idle/attack/bow actions: neck takes half the
    * head yaw, head takes full yaw + pitch, body straightens. Other actions
    * (scenes) drive the head via the animation controller instead.
    */
   protected void handleAnimationEvent(T var1, AnimationProcessor<T> var2, AnimationEvent var3) {
      if (!(var1.world instanceof SexWorldClient)) {
         if (this.shouldRender(var1)) {
            if (var1.getCurrentAction() == Action.NULL || var1.getCurrentAction() == Action.ATTACK || var1.getCurrentAction() == Action.BOW) {
               EntityModelData var4 = (EntityModelData) var3.getExtraDataOfType(EntityModelData.class).get(0);
               IBone var5 = var2.getBone("neck");
               var5.setRotationY(var4.netHeadYaw * 0.5F * (float) (Math.PI / 180.0));
               IBone var6 = var2.getBone("head");
               var6.setRotationY(var4.netHeadYaw * (float) (Math.PI / 180.0));
               var6.setRotationX(var4.headPitch * (float) (Math.PI / 180.0));
               IBone var7 = var2.getBone("body") == null ? var2.getBone("dd") : var2.getBone("body");
               var7.setRotationY(0.0F);
            }
         }
      }
   }

   /**
    * Maps an armor bone name back to the girl's armor slot (helmet/chest/
    * legs/boots) — used by the renderer's armor tinting.
    */
   public ItemStack getItemStackForBone(BaseGirlEntity var1, String var2) {
      if (Arrays.asList(this.HeadArmor()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.HELMET_SLOT);
      } else if (Arrays.asList(this.TopArmor()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.CHEST_SLOT);
      } else if (Arrays.asList(this.BottomArmor()).contains(var2)) {
         return (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.LEGS_SLOT);
      } else {
         return Arrays.asList(this.ShoesArmor()).contains(var2) ? (ItemStack)var1.entityDataManager.get(AbstractGirlNpcEntity.BOOTS_SLOT) : ItemStack.EMPTY;
      }
   }

}
