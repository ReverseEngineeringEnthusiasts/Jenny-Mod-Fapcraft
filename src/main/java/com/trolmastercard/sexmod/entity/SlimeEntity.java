package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.LootTableHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> The Slime NPC — a jump-happy slime girl with a pregnancy
 * lifecycle: it hops around (procedural jump animation, no AI tasks), gets
 * "impregnated" by the horny potion, grows, then births a
 * {@link WildSlimeEntity} that matures back into a new Slime. Scenes: blowjob
 * and doggy (same action set as Jenny).
 * <p>
 * <b>State.</b> Own data keys: {@code TICKS_UNTIL_BIRTH} (113) = pregnancy
 * progress (0..2400; 1 = undress, 2 = pregnant start, 4+ = scene-ready),
 * {@code TARGET_YAW} (112) = hop direction, {@code HORNY_LEVEL} (111) =
 * countdown to birthing a wild slime (-1 = not pregnant). {@code slimeState}
 * drives the idle jump animation cycle (IDLE/JUMP_START/JUMP_AIR/JUMP_END).
 * <p>
 * <b>Scene flow.</b> {@link #checkInteractionTrigger()} (server, AI tick)
 * anchors the slime when pregnant: if progress &gt;= 4 she starts the doggy
 * bed intro by herself, otherwise the nearest player within 1 block gets
 * locked in and she starts doggy or blowjob. Cum ends via the
 * {@code bjcDone}/{@code doggyCumDone} sounds -&gt;
 * {@code resetCameraAndPhysics()} + {@code pregnant = 2400}
 * ({@code changeDataParameterFromClient}) which starts the growth.
 * <p>
 * <b>Pitfalls.</b> {@code writeEntityToNBT}/{@code readEntityFromNBT}
 * deliberately cross-write {@code hornyLevel} and {@code ticksUntilBirth}
 * (swapped keys — jar behavior, keep as-is). {@link #setCurrentAction(Action)}
 * guards the cum loops. {@code handleJumpState} only runs while the action is
 * {@link Action#NULL}; the slime jumps by itself every ~50 ticks, adding
 * pregnancy progress at tick 50 (server) and snapping yaw to the target
 * angle. {@code fall} is intentionally a no-op.
 */
public class SlimeEntity extends BaseGirlEntity {
   static final double PREG_SCALE_0_7 = 0.7F;
   static final float PREG_SCALE_0_9 = 0.9F;
   static final double PREG_MAX_100 = 100.0;
   static final float PREG_GROWTH_0_1 = 0.1F;
   static final int PREGNANT_TICKS = 2400;
   SlimeEntity.SlimeEntityState slimeState = SlimeEntity.SlimeEntityState.IDLE;
   public static DataParameter<Integer> HORNY_LEVEL = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(113);
   public static DataParameter<Float> TARGET_YAW = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.FLOAT).getSerializer().createKey(112);
   public static DataParameter<Integer> TICKS_UNTIL_BIRTH = EntityDataManager.createKey(SlimeEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   int jumpTicks = 0;
   boolean wasOnGround = true;
   boolean isJumping = false;
   int blinkTicks = 0;

   public SlimeEntity(World world) {
      super(world);
   }

   @Override
   public String getDisplayNameText() {
      return "Slime";
   }

   @Override
   public float getScaleFactor() {
      return 1.6F;
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.CUMBLOWJOB || action != Action.THRUSTBLOWJOB && action != Action.SUCKBLOWJOB) {
         if (this.getCurrentAction() != Action.DOGGYCUM || action != Action.DOGGYFAST && action != Action.DOGGYSLOW) {
            super.setCurrentAction(action);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean shouldRenderNameTag() {
      return false;
   }

   @Override
   protected void initEntityAI() {
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(TICKS_UNTIL_BIRTH, 0);
      this.getDataManager().register(TARGET_YAW, 0.0F);
      this.getDataManager().register(HORNY_LEVEL, -1);
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.SUCKBLOWJOB || action == Action.THRUSTBLOWJOB) {
         return Action.CUMBLOWJOB;
      } else {
         return action != Action.DOGGYSLOW && action != Action.DOGGYFAST ? null : Action.DOGGYCUM;
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.SUCKBLOWJOB) {
         return Action.THRUSTBLOWJOB;
      } else {
         return action == Action.DOGGYSLOW ? Action.DOGGYFAST : null;
      }
   }

   protected float getJumpUpwardsMotion() {
      return 0.9F;
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setInteger("hornyLevel", (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH));
      nbt.setInteger("ticksUntilBirth", (Integer)this.entityDataManager.get(HORNY_LEVEL));
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.entityDataManager.set(TICKS_UNTIL_BIRTH, nbt.getInteger("hornyLevel"));
      this.entityDataManager.set(HORNY_LEVEL, nbt.getInteger("ticksUntilBirth"));
      if ((Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH) != 0) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
      }

      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected ResourceLocation getLootTable() {
      return LootTableHandler.SLIME_TABLE;
   }

   @Override
   public void reinitTasks() {
      this.entityDataManager.set(TICKS_UNTIL_BIRTH, 0);
      this.entityDataManager.set(OUTFIT_INDEX, 1);
   }

   /**
    * SERVER, every AI tick: the horny-potion trigger — when dosed and idle
    * she removes the potion, starts the undress (if dressed) and arms the
    * pregnancy progress at 2.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.checkInteractionTrigger();
      this.handleHornyJump();
      if (this.isPotionActive(HornyPotion.HORNY_POTION) && this.slimeState == SlimeEntity.SlimeEntityState.IDLE && (Integer)this.entityDataManager.get(HORNY_LEVEL) == -1) {
         this.entityDataManager.set(TICKS_UNTIL_BIRTH, 2);
         if ((Integer)this.entityDataManager.get(OUTFIT_INDEX) == 1) {
            this.setCurrentAction(Action.UNDRESS);
         }

         this.removePotionEffect(HornyPotion.HORNY_POTION);
      }
   }

   /**
    * BOTH sides: pregnancy particles (hearts every 10 ticks once pregnant),
    * the jump-state machine while idle, and CLIENT-side interaction
    * positioning + horny-level particles.
    */
   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.getCurrentAction() == Action.NULL) {
         this.handleJumpState();
      }

      if ((Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH) >= 2 && this.ticksExisted % 10 == 0) {
         spawnParticlesAround(EnumParticleTypes.HEART, this);
      }

      if (this.world.isRemote) {
         this.handleHornyLevel();
         this.handlePlayerInteraction();
      }
   }

   @SideOnly(Side.CLIENT)
   void handlePlayerInteraction() {
      if (this.getInteractionPlayerUUID() != null) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if (this.getInteractionPlayerUUID().equals(player.getPersistentID())) {
            Vec3d pos = this.getPositionVector();
            Vec3d offset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
            pos = pos.add(offset);
            player.setPosition(pos.x, pos.y, pos.z);
            player.setVelocity(0.0, 0.0, 0.0);
         }
      }
   }

   void handleHornyLevel() {
      int hornyLevel = (Integer)this.entityDataManager.get(HORNY_LEVEL);
      if (hornyLevel != -1) {
         spawnParticlesAround(EnumParticleTypes.SPELL_WITCH, this);
         if (hornyLevel == 0) {
            this.playSound(SoundHandler.MISC_PLOB[0]);
         }
      }
   }

   /**
    * SERVER: counts the horny level down each AI tick; when it goes below 0 a
    * {@link WildSlimeEntity} is spawned in place and the pregnancy flag
    * clears.
    */
   void handleHornyJump() {
      int hornyLevel = (Integer)this.entityDataManager.get(HORNY_LEVEL);
      if (hornyLevel != -1) {
         this.entityDataManager.set(HORNY_LEVEL, hornyLevel - 1);
         if (--hornyLevel < 0) {
            WildSlimeEntity slime = new WildSlimeEntity(this.world);
            slime.setPosition(this.posX, this.posY, this.posZ);
            this.world.spawnEntity(slime);
            this.entityDataManager.set(HORNY_LEVEL, -1);
         }
      }
   }

   /**
    * SERVER: the pregnancy scene trigger — once progress reaches 4 and she is
    * on the ground with no action, she self-starts the doggy bed intro
    * (anchored); below 4, the nearest player within 1 block gets locked into
    * a doggy or blowjob scene.
    */
   void checkInteractionTrigger() {
      int ticksUntilBirth = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
      if (ticksUntilBirth >= 2) {
         if (ticksUntilBirth >= 4 && this.onGround && this.getCurrentAction() == Action.NULL) {
            this.setTargetPosition(this.getPositionVector());
            this.setYawRotation(this.rotationYaw);
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setNoGravity(true);
            this.noClip = true;
            this.setCurrentAction(Action.STARTDOGGY);
         } else {
            EntityPlayer player = this.world.getClosestPlayerToEntity(this, 1.0);
            if (player != null && player.onGround && getActiveSceneInfo(player) == null) {
               this.setTargetPosition(this.getPositionVector());
               this.setYawRotation(this.rotationYaw);
               this.entityDataManager.set(IS_ANCHORED, true);
               this.setNoGravity(true);
               this.noClip = true;
               player.setNoGravity(true);
               player.noClip = true;
               PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
               this.setInteractionPlayerUUID(player.getPersistentID());
               player.rotationYaw = this.getYawRotation();
               Vec3d offset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.65F), this.getYawRotation());
               player.setPosition(this.posX + offset.x, this.posY, this.posZ + offset.z);
               if (this.getCurrentAction() == Action.WAITDOGGY) {
                  this.setCurrentAction(Action.DOGGYSTART);
               } else {
                  this.setCurrentAction(Action.SUCKBLOWJOB);
               }
            }
         }
      }
   }

   /**
    * The procedural jump machine. CLIENT: cycles the jump animation states on
    * takeoff/landing and locks yaw to {@code TARGET_YAW}. SERVER: every ~50
    * ticks performs the hop (stop, face target, launch) and increments
    * {@code TICKS_UNTIL_BIRTH} at tick 50, triggering the undress at
    * progress 1. {@code TARGET_YAW} aims at the nearest player once pregnant.
    */
   void handleJumpState() {
      if (this.world.isRemote) {
         if (this.jumpTicks == 90.0) {
            this.slimeState = SlimeEntity.SlimeEntityState.JUMP_START;
         }

         if (!this.wasOnGround && this.onGround) {
            this.slimeState = SlimeEntity.SlimeEntityState.JUMP_END;
            this.jumpTicks = 0;
         }

         float targetYaw = (Float)this.entityDataManager.get(TARGET_YAW);
         this.rotationYaw = targetYaw;
         this.rotationYawHead = targetYaw;
         this.renderYawOffset = targetYaw;
      } else {
         if (this.jumpTicks == 85.0) {
            this.entityDataManager.set(TARGET_YAW, this.getBirthProgress());
         }

         if (this.jumpTicks == 100.0) {
            this.stopMovement();
         }

         if (!this.wasOnGround && this.onGround) {
            this.isJumping = (Integer)this.entityDataManager.get(HORNY_LEVEL) == -1 && this.getRNG().nextFloat() < 0.1F;
         }

         if (this.isJumping && this.jumpTicks == 50) {
            int ticks = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
            int newTicks = ticks + 1;
            this.entityDataManager.set(TICKS_UNTIL_BIRTH, newTicks);
            if (newTicks == 1) {
               this.setCurrentAction(Action.UNDRESS);
            }
         }
      }

      if (this.onGround) {
         this.jumpTicks++;
      }

      this.wasOnGround = this.onGround;
   }

   /**
    * SERVER: ends one hop — zeroes motion, jumps, rotates to the target yaw
    * and launches toward it at 0.7 blocks/tick. Resets the hop counter.
    */
   void stopMovement() {
      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
      this.jump();
      float targetYaw = (Float)this.entityDataManager.get(TARGET_YAW);
      this.rotationYaw = targetYaw;
      this.prevRotationYaw = targetYaw;
      Vec3d vec = new Vec3d(0.0, 0.0, 0.7F);
      vec = VectorMath.rotateByYaw(vec, targetYaw);
      this.motionX = vec.x;
      this.motionZ = vec.z;
      this.jumpTicks = 0;
   }

   /**
    * SERVER: computes the hop target yaw — random when not pregnant, otherwise
    * aimed at the nearest player (who is not already in a scene).
    */
   float getBirthProgress() {
      int ticks = (Integer)this.entityDataManager.get(TICKS_UNTIL_BIRTH);
      if ((Integer)this.entityDataManager.get(HORNY_LEVEL) != -1) {
         return this.getRandomAngle();
      } else if (ticks < 2) {
         return this.getRandomAngle();
      } else {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 30.0);
         if (player == null) {
            return this.getRandomAngle();
         } else {
            return getActiveSceneInfo(player) != null
               ? this.getRandomAngle()
               : (float)Math.atan2(this.posZ - player.posZ, this.posX - player.posX) * (float) (180.0 / Math.PI) + 90.0F;
         }
      }
   }

   float getRandomAngle() {
      return Reference.RANDOM.nextFloat() * 360.0F;
   }

   public void fall(float distance, float multiplier) {
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.slime.fhappy", true, event);
            } else {
               this.createAnimation("animation.slime.null", true, event);
            }
            break;
         case "action":
            if (this.getCurrentAction() == Action.NULL) {
               this.createAnimation(this.slimeState.animationId, true, event);
            } else {
               switch (this.getCurrentAction()) {
                  case UNDRESS:
                     this.createAnimation("animation.slime.undress", false, event);
                     break;
                  case DRESS:
                     this.createAnimation("animation.slime.dress", false, event);
                     break;
                  case STRIP:
                     this.createAnimation("animation.slime.strip", false, event);
                     break;
                  case STARTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobintro", false, event);
                     break;
                  case SUCKBLOWJOB:
                     this.createAnimation("animation.slime.blowjobsuck", true, event);
                     break;
                  case THRUSTBLOWJOB:
                     this.createAnimation("animation.slime.blowjobthrust", true, event);
                     break;
                  case CUMBLOWJOB:
                     this.createAnimation("animation.slime.blowjobcum", false, event);
                     break;
                  case STARTDOGGY:
                     this.createAnimation("animation.slime.doggygoonbed", false, event);
                     break;
                  case WAITDOGGY:
                     this.createAnimation("animation.slime.doggywait", true, event);
                     break;
                  case DOGGYSTART:
                     this.createAnimation("animation.slime.doggystart", false, event);
                     break;
                  case DOGGYSLOW:
                     this.createAnimation("animation.slime.doggyslow", true, event);
                     break;
                  case DOGGYFAST:
                     this.createAnimation("animation.slime.doggyfast", true, event);
                     break;
                  case DOGGYCUM:
                     this.createAnimation("animation.slime.doggycum", false, event);
               }
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * pregnancy undress/dress, the jump cycle and the blowjob/doggy scenes.
    * Scene end: {@code bjcDone}/{@code doggyCumDone} -&gt;
    * {@code resetCameraAndPhysics()} and pregnancy start
    * ({@code pregnant = 2400}); {@code undress} -&gt; nude + NULL action.
    */
   @Override
   public void registerControllers(AnimationData data) {
      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "undress":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", "0");
                  this.setCurrentAction(Action.NULL);
               }
               break;
            case "dress":
               if (this.isLocalPlayerNearby()) {
                  this.entityDataManager.set(OUTFIT_INDEX, 1);
                  this.setCurrentAction((Action)null);
                  this.resetCameraAndPhysics();
               }
               break;
            case "becomeNude":
               this.entityDataManager.set(OUTFIT_INDEX, 0);
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer() && !HornyMeterHud.isVisible) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.4, -0.8, -0.2, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.RANDOM.nextInt(5) == 0) {
                  this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_JUMP, 0.5F);
               }

               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               this.playSound(SoundEvents.ENTITY_SLIME_DEATH);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "bjiDone":
               this.setCurrentAction(Action.SUCKBLOWJOB);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "bjtDone":
               this.setCurrentAction(Action.SUCKBLOWJOB);
               break;
            case "bjtReady":
            case "doggyfastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "bjcMSG1":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "bjcMSG2":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "doggyslowMSG2":
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               break;
            case "bjcBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "bjcDone":
            case "doggyCumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
                  this.changeDataParameterFromClient("pregnant", String.valueOf(2400));
               }
               break;
            case "doggyGoOnBedMSG1":
               this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedDone":
               this.setCurrentAction(Action.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.playSound(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.playSound(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.playSoundAtVolume(SoundEvents.ENTITY_SLIME_SQUISH, 0.25F);
               break;
            case "doggystartMSG4":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 1.5F);
               break;
            case "doggystartMSG5":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               break;
            case "doggystartDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int choice = Reference.RANDOM.nextInt(4);
               if (choice == 0) {
                  choice = Reference.RANDOM.nextInt(2);
                  if (choice == 0) {
                     this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "doggyfastMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }

               this.blinkTicks++;
               if (this.blinkTicks % 2 == 0) {
                  int choice2 = Reference.RANDOM.nextInt(2);
                  if (choice2 == 0) {
                     this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
                  } else {
                     this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
                  }
               } else {
                  this.playSound(SoundEvents.BLOCK_SLIME_HIT);
               }
               break;
            case "doggyfastDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 4.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.playSound(SoundEvents.ENTITY_SLIME_DEATH);
               break;
            case "jumpStart":
               this.playSound(SoundEvents.ENTITY_SLIME_JUMP);
               break;
            case "jumpStartDone":
               this.slimeState = SlimeEntity.SlimeEntityState.JUMP_AIR;
               break;
            case "jumpEndSound":
               this.playSound(SoundEvents.ENTITY_SLIME_SQUISH);
               break;
            case "jumpEndDone":
               this.slimeState = SlimeEntity.SlimeEntityState.IDLE;
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.eyesController);
   }

   enum SlimeEntityState {
      IDLE("animation.slime.idle"),
      JUMP_START("animation.slime.jumpstart"),
      JUMP_AIR("animation.slime.jumpair"),
      JUMP_END("animation.slime.jumpend");

      String animationId;

      public String getAnimationId() {
         return this.animationId;
      }

      SlimeEntityState(String animationId) {
         this.animationId = animationId;
      }
   }
}
