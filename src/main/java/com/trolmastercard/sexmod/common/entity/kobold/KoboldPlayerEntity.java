package com.trolmastercard.sexmod.common.entity.kobold;

import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.gui.screen.SceneFadeScreen;
import com.trolmastercard.sexmod.client.network.ModelPreviewWorldClient;
import com.trolmastercard.sexmod.common.util.math.VectorMath;
import com.trolmastercard.sexmod.network.PacketHandler;
import com.trolmastercard.sexmod.network.scene.TeleportPlayerPacket;
import com.trolmastercard.sexmod.registry.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.resource.GeckoLibCache;

import com.trolmastercard.sexmod.common.entity.base.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.common.entity.base.Action;
import com.trolmastercard.sexmod.common.entity.base.EyeAndKoboldColor;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.screen.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.model.vanilla.AllieModel;
import com.trolmastercard.sexmod.client.model.base.IVanillaModel;
import com.trolmastercard.sexmod.client.render.entity.KoboldRenderer;
import com.trolmastercard.sexmod.api.IKobold;
import com.trolmastercard.sexmod.common.util.math.RotationHelper;
import com.trolmastercard.sexmod.client.render.player.PlayerKoboldRenderer;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.util.MatrixStack;

/**
 * <b>Role.</b> Player-form kobold (implements {@link IKobold}) — the
 * transformation with blowjob, anal and mating-press scenes plus full
 * appearance customization (size, eye color, body color) stored in the
 * kobold-specific data keys.
 * <p>
 * <b>State.</b> Inherits {@link AbstractKoboldPlayerEntity} keys (119-121);
 * adds {@code SIZE_SCALAR} (122) = size scalar in {@code [0, 0.25]} (encoded as
 * part-id {@code 0} value / 100 * 0.25). {@code setCustomPartList} maps part
 * indices 0/1/2 to size/body-color/eye-color and packs the rest into the DNA
 * string. {@code aw} = default body color {@link EyeAndKoboldColor#PURPLE}.
 * <p>
 * <b>Scene flow.</b> Owner commands {@code anal}/{@code oral}/{@code mating}
 * teleport the acting player in, broadcast the intro action and strip.
 * Progression runs in the {@code ISoundListener}: {@code blowjobStartDone}
 * -&gt; {@link Action#SUCKBLOWJOB_BLINK}, {@code analStartDone} -&gt;
 * {@link Action#KOBOLD_ANAL_SLOW}, {@code mating_press_startDone} -&gt; soft,
 * jump switches to fast. Cum ends via {@code analCumDone}/
 * {@code blowjobCumDone}/{@code mating_press_cumDone} -&gt;
 * {@code resetCameraAndPhysics()}.
 * <p>
 * <b>Pitfalls.</b> {@code SIZE_SCALAR} scales the model (matrix, camera pivot) and the
 * sound pitch (0.9-1.1) — every consumer must use the same
 * {@code 0.25F - SIZE_SCALAR} formula. {@link #getNextAction(Action)} and
 * {@link #getCumAction(Action)} must cover the blink variant of the blowjob
 * loop. The animation predicate returns {@link PlayState#STOP} inside
 * {@link SexWorldClient} worlds.
 */
public class KoboldPlayerEntity extends AbstractKoboldPlayerEntity implements IKobold {
   public static final EyeAndKoboldColor DEFAULT_BODY_COLOR = EyeAndKoboldColor.PURPLE;
   public static final DataParameter<Float> SIZE_SCALAR = EntityDataManager.createKey(KoboldPlayerEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(122);
   boolean flySecondFrame = false;
   boolean blowjobRightSide = true;
   boolean switchVariant = false;
   int moanCooldown = 0;

   protected KoboldPlayerEntity(World world) {
      super(world);
   }

   public KoboldPlayerEntity(World world, UUID uuid) {
      super(world, uuid);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeAndKoboldColor color = EyeAndKoboldColor.values()[this.getRNG().nextInt(EyeAndKoboldColor.values().length)];
      this.entityDataManager.register(EYE_COLOR, new BlockPos(color.getMainColor()));
      this.entityDataManager.register(SKIN_COLOR_NAME, DEFAULT_BODY_COLOR.name());
      this.entityDataManager.register(SIZE_SCALAR, 0.0F);
   }

   @Override
   public AxisAlignedBB getPlayerCollisionBox(EntityPlayer player) {
      return new AxisAlignedBB(
         player.posX - 0.3F,
         player.posY,
         player.posZ - 0.3F,
         player.posX + 0.3F,
         player.posY + 0.9F,
         player.posZ + 0.3F
      );
   }

   /**
    * Maps the custom-part id list onto the kobold-specific data keys:
    * index 0 = size ({@code SIZE_SCALAR}), index 1 = body color ({@code SKIN_COLOR_NAME}), index 2 =
    * eye color ({@code EYE_COLOR}); the remainder is appended as the DNA string
    * ({@code MODEL_CODE}). On the CLIENT the render caches are cleared afterwards.
    * <p>
    * Ordering of the three special indices is part of the persisted format —
    * do not reorder.
    */
   @Override
   public void setCustomPartList(List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int i = 0; i < parts.size(); i++) {
         int partId = (Integer)parts.get(i);
         switch (i) {
            case 0:
               this.entityDataManager.set(SIZE_SCALAR, partId / 100.0F * 0.25F);
               break;
            case 1:
               this.entityDataManager.set(SKIN_COLOR_NAME, EyeAndKoboldColor.values()[partId].toString());
               break;
            case 2:
               this.entityDataManager.set(EYE_COLOR, new BlockPos(EyeAndKoboldColor.values()[partId].getMainColor()));
               break;
            default:
               AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
         }
      }

      this.entityDataManager.set(MODEL_CODE, builder.toString());
      if (this.world.isRemote) {
         PlayerKoboldRenderer.clearRenderCache();
      }
   }

   @Override
   public ArrayList<Integer> getBasePartIdList() {
      ArrayList parts = new ArrayList();
      parts.add(Math.round((Float)this.entityDataManager.get(SIZE_SCALAR) * 100.0F / 0.25F));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((String)this.entityDataManager.get(SKIN_COLOR_NAME))));
      parts.add(EyeAndKoboldColor.indexOf(EyeAndKoboldColor.safeValueOf((Vec3i)this.entityDataManager.get(EYE_COLOR))));
      return parts;
   }

   @Override
   protected String buildModelCodeDNA(StringBuilder builder) {
      AbstractNpcOnlyEntity.appendPaddedLetter(builder, 8);
      AbstractNpcOnlyEntity.appendPaddedLetter(builder, 3);
      AbstractNpcOnlyEntity.appendRandomGene(builder);
      AbstractNpcOnlyEntity.appendRandomGene(builder);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 1);
      return builder.toString();
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<Integer>() {
         {
            this.add(101);
            this.add(EyeAndKoboldColor.values().length);
            this.add(EyeAndKoboldColor.values().length);
            this.add(8);
            this.add(3);
            this.add(101);
            this.add(101);
            this.add(3);
            this.add(3);
            this.add(4);
            this.add(2);
         }
      };
   }

   @Override
   protected void clearBoneColors() {
      PlayerKoboldRenderer.clearRenderCache();
      KoboldRenderer.clearBoneColors();
   }

   @Override
   public float getScaleFactor() {
      float shrink = 0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR);
      return 1.4F - shrink;
   }

   /**
    * SERVER: owner commands — {@code anal} starts {@link Action#KOBOLD_ANAL_START},
    * {@code oral} starts {@link Action#STARTBLOWJOB}, {@code mating} starts
    * {@link Action#MATING_PRESS_START}. All broadcast the action, strip and
    * teleport the acting player into the scene.
    */
   @Override
   public void handleOwnerCommand(String command, UUID uuid) {
      if ("anal".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.KOBOLD_ANAL_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.KOBOLD_ANAL_START);
         this.setOutfitIndex(0);
      }

      if ("oral".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.STARTBLOWJOB);
         this.sendActionPacket(this.getOutfitIndex(), Action.STARTBLOWJOB);
         this.setOutfitIndex(0);
      }

      if ("mating".equals(command)) {
         this.teleportPlayerToGirl(uuid);
         this.setCurrentAction(Action.MATING_PRESS_START);
         this.sendActionPacket(this.getOutfitIndex(), Action.MATING_PRESS_START);
         this.setOutfitIndex(0);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(this, player, new String[]{"anal", "oral", "mating"}, null, false));
      return true;
   }

   @Override
   public boolean isBlockedByCeiling() {
      Block block = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !block.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   /**
    * Scales the bone matrix by the kobold size (1 minus the size scalar) so
    * the rendered model matches the {@code SIZE_SCALAR} slider. CLIENT-only rendering
    * helper.
    */
   @Override
   protected MatrixStack applyAdditionalMatrixTransformations(MatrixStack stack) {
      float shrink = 0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR);
      stack.scale(1.0F - shrink, 1.0F - shrink, 1.0F - shrink);
      return stack;
   }

   /**
    * Scales the camera-bone pivot by the kobold size so the first-person
    * camera sits at the right height. CLIENT-only.
    */
   @Override
   protected float transformCameraPivotY(float y) {
      float grow = 1.0F - (0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR));
      return y * grow;
   }

   @Override
   public IVanillaModel getHandModel(int index) {
      return new AllieModel();
   }

   @Override
   public String getHandTexture(int index) {
      return "textures/entity/kobold/hand.png";
   }

   @Override
   public Vec3i getHandColor(int index) {
      try {
         return EyeAndKoboldColor.valueOf((String)this.entityDataManager.get(SKIN_COLOR_NAME)).getMainColor();
      } catch (Exception ex) {
         Main.LOGGER.error("could not read the kobold hand color", ex);
         return super.getHandColor(index);
      }
   }

   @Nullable
   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.SUCKBLOWJOB_BLINK) {
         return Action.THRUSTBLOWJOB;
      } else {
         return action == Action.KOBOLD_ANAL_SLOW ? Action.KOBOLD_ANAL_FAST : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.THRUSTBLOWJOB || action == Action.SUCKBLOWJOB_BLINK) {
         return Action.CUMBLOWJOB;
      } else if (action == Action.KOBOLD_ANAL_SLOW || action == Action.KOBOLD_ANAL_FAST) {
         return Action.KOBOLD_ANAL_CUM;
      } else {
         return action != Action.MATING_PRESS_HARD && action != Action.MATING_PRESS_SOFT ? null : Action.MATING_PRESS_CUM;
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.MATING_PRESS_CUM || action != Action.MATING_PRESS_SOFT && action != Action.MATING_PRESS_HARD) {
         if (currentAction != Action.KOBOLD_ANAL_CUM || action != Action.KOBOLD_ANAL_SLOW && action != Action.KOBOLD_ANAL_FAST) {
            if (currentAction != Action.CUMBLOWJOB || action != Action.SUCKBLOWJOB && action != Action.THRUSTBLOWJOB) {
               super.setCurrentAction(action);
            }
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      return this.predicate(event);
   }

   @Override
   public void playSound(SoundEvent sound) {
      this.playSoundAtVolume(sound, 1.0F);
   }

   void playRandomSounds(SoundEvent[] sounds) {
      this.playRandomSound(sounds, 1.0F);
   }

   void playRandomSound(SoundEvent[] sounds, float volume) {
      this.playSoundAtVolume(sounds[this.getRNG().nextInt(sounds.length)], volume);
   }

   /**
    * Plays a sound pitched by kobold size: smaller kobolds get a slightly
    * higher pitch (0.9-1.1 range from {@link RotationHelper#lerpDouble}).
    */
   public void playSoundAtVolume(SoundEvent sound, float volume) {
      float shrink = 0.25F - (Float)this.entityDataManager.get(SIZE_SCALAR);
      double progress = shrink / 0.25F;
      float pitch = (float)RotationHelper.lerpDouble(0.9F, 1.1F, progress);
      this.playSoundAtPosition(sound, volume, pitch);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      this.registerControllersImpl(data);
   }



   // ------------------------------------------------------------------
   // Entity behaviour lives here; the per-entity static helpers were folded in.
   // ------------------------------------------------------------------



   <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
      if (this.world instanceof ModelPreviewWorldClient) {
         return PlayState.STOP;
      }

      float shrink = 0.25F - (Float)this.getDataManager().get(KoboldEntity.SIZE_SCALAR);
      GeckoLibCache.getInstance().parser.setValue("size", shrink);
      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.this.blink", true, event);
            } else {
               this.createAnimation("animation.this.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.this.null", true, event);
            } else if (this.playerRiding) {
               this.createAnimation("animation.this.sit", true, event);
            } else {
               if (this.movementController.getCurrentAnimation() != null
                  && this.movementController.getCurrentAnimation().animationName.contains("fly")
                  && this.playerOnGround) {
                  this.flySecondFrame = !this.flySecondFrame;
               }

               if (!this.playerOnGround) {
                  this.createAnimation("animation.this.fly" + (this.flySecondFrame ? "2" : ""), true, event);
               } else if (Math.abs(this.playerMotionOffset.x) + Math.abs(this.playerMotionOffset.y) > 0.0F) {
                  if (this.playerSprinting) {
                     this.movementController.setAnimationSpeed(1.2F);
                     this.createAnimation("animation.this.run", true, event);
                  } else if (this.playerMotionOffset.y >= -0.1F) {
                     this.movementController.setAnimationSpeed(2.0);
                     this.createAnimation("animation.this.walk", true, event);
                  } else {
                     this.movementController.setAnimationSpeed(1.75);
                     this.createAnimation("animation.this.backwards_walk", true, event);
                  }
               } else {
                  this.createAnimation("animation.this.idle", true, event);
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.this.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.this.strip", false, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.this.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.this.bowcharge", false, event);
                  break;
               case SIT:
                  this.createAnimation("animation.this.sit", true, event);
                  break;
               case MINE:
                  this.createAnimation("animation.this.fall_tree", true, event);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.this.paymentBackpack", true, event);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.this.blowjobStart", false, event);
                  break;
               case SUCKBLOWJOB_BLINK:
                  String side = this.blowjobRightSide ? "R" : "L";
                  String animSuffix = this.switchVariant ? "Switch" : "";
                  this.createAnimation("animation.this.blowjobSlow" + side + animSuffix, true, event);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.this.blowjobFast", true, event);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.this.blowjobCum", false, event);
                  break;
               case KOBOLD_ANAL_START:
                  this.createAnimation("animation.this.analStart", false, event);
                  break;
               case KOBOLD_ANAL_SLOW:
                  this.createAnimation("animation.this.analSoft", true, event);
                  break;
               case KOBOLD_ANAL_FAST:
                  this.createAnimation("animation.this.analHard", true, event);
                  break;
               case KOBOLD_ANAL_CUM:
                  this.createAnimation("animation.this.analCum", true, event);
                  break;
               case SLEEP:
                  this.createAnimation("animation.this.sleep", true, event);
                  break;
               case MATING_PRESS_START:
                  this.createAnimation("animation.this.mating_press_start", false, event);
                  break;
               case MATING_PRESS_SOFT:
                  this.createAnimation("animation.this.mating_press_soft", true, event);
                  break;
               case MATING_PRESS_HARD:
                  this.createAnimation("animation.this.mating_press_hard", true, event);
                  break;
               case MATING_PRESS_CUM:
                  this.createAnimation("animation.this.mating_press_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }



   @SideOnly(Side.CLIENT)
   void registerControllersImpl(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackDone":
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "paymentMSG1":
               this.sendChatMessageToPlayer(this.getInteractionPlayerUUID(), "I'd like to use ur services owo");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "plob":
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  SceneFadeScreen.enableInteraction();
               }
               break;
            case "paymentDone":
               if (this.isControlledByLocalPlayer()) {
                  this.startActionFromHandState();
               }
               break;
            case "blowjobStartMSG1":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.625 - player.getEyeHeight(), -1.0), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation() + 180.0F, 0.0F
                        )
                     );
               }
               break;
            case "blowjobStartMSG2":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5 - player.getEyeHeight(), -0.6875), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(
                        new TeleportPlayerPacket(
                           this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation() + 180.0F - 40.0F, 0.0F
                        )
                     );
               }
               break;
            case "lipsound":
               if (this.getRNG().nextBoolean()) {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_ALLIE_LIPSOUND, 1.5F);
               } else {
                  this.playRandomSoundAtVolume(SoundHandler.GIRLS_JENNY_LIPSOUND, 1.5F);
               }

               HornyMeterHud.addToHornyMeter(0.02F);
               break;
            case "touch":
               this.playRandomSound(SoundHandler.MISC_TOUCH);
               break;
            case "blowjobStartDone":
               this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               this.switchVariant = false;
               this.blowjobRightSide = true;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "switch":
               this.switchVariant = this.getRNG().nextBoolean();
               this.actionController.clearAnimationCache();
               break;
            case "endSwitch":
               this.switchVariant = false;
               this.blowjobRightSide = !this.blowjobRightSide;
               this.actionController.clearAnimationCache();
               break;
            case "blowjobFastDone":
               if (this.isControlledByLocalPlayer() && !PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.SUCKBLOWJOB_BLINK);
               }
               break;
            case "cumLoud":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "cumQuiet":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "analCumDone":
            case "blowjobCumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "analStartDone":
               this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "analStartCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = VectorMath.rotateByYaw(new Vec3d(0.0, 0.5625 - player.getEyeHeight(), 0.5625), this.getYawRotation() + 180.0F);
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(this.getInteractionPlayerUUID().toString(), this.getTargetPosition().add(pos), this.getYawRotation(), 0.0F));
               }
               break;
            case "pounding":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "analFastRapid":
               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                     this.resetAnimationControllerOffset();
                  } else {
                     this.setCurrentAction(Action.KOBOLD_ANAL_FAST);
                  }
               }
               break;
            case "analDone":
               if (this.getCurrentAction() == Action.KOBOLD_ANAL_FAST) {
                  this.setCurrentAction(Action.KOBOLD_ANAL_SLOW);
               }
               break;
            case "analHard":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "analSoft":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "giggle":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_GIGGLE);
               break;
            case "moan":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               break;
            case "moanMating":
               this.moanCooldown--;
               if (this.moanCooldown <= 0) {
                  this.moanCooldown = 3;
                  this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "analHardMSG1":
               this.moanCooldown--;
               if (this.moanCooldown <= 0) {
                  this.moanCooldown = 4;
                  this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_MOAN);
               }
               break;
            case "orgasm":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_ORGASM);
               break;
            case "breath":
               this.playRandomSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING, 0.5F);
               break;
            case "haa":
               this.playRandomSound(SoundHandler.GIRLS_KOBOLD_HAA, 0.7F);
               break;
            case "interested":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_INTERESTED);
               break;
            case "yep":
               this.playRandomSounds(SoundHandler.GIRLS_KOBOLD_YEP);
               break;
            case "bjmoan":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_KOBOLD_BJMOAN));
               break;
            case "blowjobStartbreath":
               int soundId = this.getRNG().nextInt(3);
               this.playSound(SoundHandler.GIRLS_KOBOLD_LIGHTBREATHING[soundId]);
               break;
            case "matingCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 0.4375 - player.eyeHeight, -0.6875);
                  pos = VectorMath.rotateByYaw(pos, this.getYawRotation() + 180.0F);
                  pos = pos.add(this.getTargetPosition());
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, this.getYawRotation() + 180.0F, 10.0F));
               }
               break;
            case "mating_press_startDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
            case "mating_press_hardDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.MATING_PRESS_SOFT);
               }
               break;
            case "mating_press_softReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.MATING_PRESS_HARD);
               }
               break;
            case "mating_press_hardReady":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }

               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "mating_cum_cam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  Vec3d pos = new Vec3d(0.0, 1.1875 - player.eyeHeight, 0.125);
                  pos = VectorMath.rotateByYaw(pos, this.getYawRotation() + 180.0F);
                  pos = pos.add(this.getTargetPosition());
                  PacketHandler.networkWrapper
                     .sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), pos, this.getYawRotation() + 180.0F, 70.0F));
               }
               break;
            case "cumMsg":
               this.sendChatMessage("I.. hope I am satisfying you sir");
               this.playSound(SoundHandler.GIRLS_KOBOLD_SAD[this.getRNG().nextInt(1)]);
               break;
            case "mating_press_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
               }
         }
      };
      this.movementController.transitionLengthTicks = 3.0;
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
