package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.networking.SetPlayerForGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.IBeddableSexGirl;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * Jenny NPC — the blond sword/bow girl with blowjob, boobjob, doggy and
 * paizuri scenes.
 * <p>
 * <b>Scene entry</b> (shared with Bia/Luna/Kobold): client
 * {@code doAction} sets {@code animationFollowUp} via
 * {@code ChangeDataParameterPacket} (GIRL_HAND_STATES) and sends
 * {@code KoboldStatePacket}; the server calls {@code setDismounted()}
 * ({@link #ab}), then {@link #updateAITasks()} lerps her to
 * {@code TARGET_POS} for ~40 ticks ({@code ac} counter), anchors her
 * ({@code IS_ANCHORED}) and calls {@link #U()} which dispatches on
 * GIRL_HAND_STATES.
 * <p>
 * <b>Payment gate:</b> without the horny potion effect
 * ({@link #yFlag}, server-side in {@link #onUpdate()}), the first scene
 * request goes through {@link Action#PAYMENT} — the girl demands emeralds
 * before starting. With the potion active, the scene starts directly.
 * <p>
 * <b>Scenes</b>: blowjob/boobjob play in place; doggy walks her to the
 * nearest bed ({@link IBeddableSexGirl#goToSexBed()}, {@link #zFlag} walk
 * state) then anchors and starts DOGGYSTART.
 * <p>
 * <b>Pitfall:</b> the dismount lerp at line ~157 MUST keep
 * {@code RotationHelper.lerpVec3d(pos, target, 40 - ac)} — the INT (step)
 * variant. The double variant flings the girl 40x and she vanishes (see
 * {@code RotationHelper} class doc). Also keep the {@link #af} snap-in logic
 * (binds the interaction player when the player is within half a block) —
 * it is what starts the standing doggy scene.
 */
public class JennyEntity extends AbstractGirlNpcEntity implements IEllie, IBeddableSexGirl {
   /** Set by {@code setDismounted()}: true while the scene-entry lerp runs (40 ticks). */
   public boolean zFlag = false;

   /** Set by {@code setDismounted()}: true while the scene-entry lerp runs. */
   public boolean ab = false;

   /** Snap-in state: true while waiting for the player to walk within 0.5 blocks (doggy). */
   public boolean af = false;

   /** SERVER-maintained flag: true while the player has the horny potion effect active. */
   public static final DataParameter<Boolean> yFlag = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(118);

   /** Dismount-lerp tick counter (0..40). */
   int ac = 0;

   /** Bed-walk tick counter (re-path at 60/120). */
   int ad = 0;

   boolean aa = false;

   int ag = 0;

   boolean ae = false;

   public JennyEntity(World world) {
      super(world);
      this.setSize(0.49F, 1.95F);
      this.slashSwordRot = 140;
      this.stabSwordRot = 50;
      this.holdBowRot = 140;
      this.swordOffsetStab = new Vec3d(0.0, -0.029999997854232782, -0.2);
   }

   public static JennyEntity createJenny(World world) {
      JennyEntity jenny = new JennyEntity(world);
      jenny.isSpecialState = true;
      return jenny;
   }

   @Override
   public String getDisplayNameText() {
      return "Jenny";
   }

   @Override
   public float getScaleFactor() {
      return -0.2F;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(yFlag, false);
   }

   @Override
   public void onArriveHome() {
      this.sendChatMessage("Alright, this is my new Home~");
      this.playSound(SoundHandler.GIRLS_JENNY_HAPPYOH[1]);
   }

   public float getEyeHeight() {
      return 1.64F;
   }

   protected SoundEvent getDeathSound() {
      return SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_SIGH);
   }

   protected SoundEvent getHurtSound(DamageSource source) {
      return null;
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
      if (this.af && player != null && player.getPositionVector().distanceTo(this.getPositionVector()) < 0.5) {
         this.af = false;
         this.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, this.world.getClosestPlayerToEntity(this, 15.0).getPersistentID().toString());
         EntityPlayerMP playerMP = this.getServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID());
         this.entityDataManager.set(BaseGirlEntity.INTERACTION_PARTNER_UUID, playerMP.getPersistentID().toString());
         playerMP.setPositionAndUpdate(this.getPositionVector().x, this.getPositionVector().y, this.getPositionVector().z);
         this.alignPlayerToGirl(playerMP, false);
         playerMP.moveRelative(0.0F, 0.0F, 0.0F, 0.0F);
         this.positionPlayerRelative(0.0, 0.0, 0.4, 0.0F, 60.0F);
         this.cameraOriginPos = null;
         this.setCurrentAction(Action.DOGGYSTART);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), playerMP);
      }

      if (this.zFlag) {
         if (!(this.getPositionVector().distanceTo(this.getTargetPosition()) < 0.6) && this.ad <= 200) {
            this.ad++;
            if (this.ad == 60 || this.ad == 120) {
               this.getNavigator().clearPath();
               this.getNavigator().tryMoveToXYZ(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, 0.35);
            }
         } else {
            this.zFlag = false;
            this.entityDataManager.set(BaseGirlEntity.IS_ANCHORED, true);
            this.ad = 0;
            this.noClip = true;
            this.setNoGravity(true);
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
            this.setCurrentAction(Action.STARTDOGGY);
         }
      }

      if (this.ab) {
         this.ac++;
         if (!this.getPositionVector().equals(BaseGirlEntity.TARGET_POS) && this.ac <= 40) {
            this.rotationYaw = this.getYawRotation();
            this.setTargetPosition(this.getFrontOffsetVector());
            this.setNoGravity(false);
            Vec3d pos = RotationHelper.lerpVec3d(this.getPositionVector(), this.getTargetPosition(), 40 - this.ac);
            this.setPosition(pos.x, pos.y, pos.z);
         } else {
            this.ab = false;
            this.ac = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(BaseGirlEntity.IS_ANCHORED, true);
            this.getNavigator().clearPath();
            if ((Boolean)this.entityDataManager.get(yFlag)) {
               this.U();
               return;
            }

            this.setCurrentAction(Action.PAYMENT);
         }
      }
   }

   public boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (super.processInteract(player, hand)) {
         return true;
      }

      if (this.world.isRemote && !this.openInteractionMenu(player)) {
         this.sendChatMessage(I18n.format("jenny.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (!this.world.isRemote) {
         this.entityDataManager.set(yFlag, this.isPotionActive(HornyPotion.HORNY_POTION));
      }
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      if (this.getInteractionPlayerUUID() == null
         && (!this.hasMaster() || ((String)this.entityDataManager.get(BaseGirlEntity.MASTER)).equals(Minecraft.getMinecraft().player.getPersistentID().toString()))
         )
       {
         String[] options = new String[]{
            "action.names.blowjob",
            "action.names.boobjob",
            "action.names.doggy",
            this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) == 1 ? "action.names.strip" : "action.names.dressup"
         };
         if ((Boolean)this.entityDataManager.get(yFlag)) {
            BaseGirlEntity.openInventoryGui(player, this, options, true);
            return true;
         } else {
            BaseGirlEntity.openInventoryGui(
               player,
               this,
               options,
               new ItemStack[]{
                  new ItemStack(Items.EMERALD, 3),
                  new ItemStack(Items.ENDER_PEARL, 2),
                  new ItemStack(Items.DIAMOND, 2),
                  this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) == 1 ? new ItemStack(Items.GOLD_INGOT, 1) : new ItemStack(Items.AIR, 0)
               },
               true
            );
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public void doAction(String action, UUID uuid) {
      super.doAction(action, uuid);
      if ("action.names.blowjob".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "blowjob");
         this.setOwner(true, uuid);
      } else if ("action.names.boobjob".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "boobjob");
         this.setOwner(true, uuid);
      } else if ("action.names.doggy".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "doggy");
         this.setOwner(true, uuid);
      } else if ("action.names.strip".equals(action)) {
         this.changeDataParameterFromClient("animationFollowUp", "strip");
         this.setOwner(true, uuid);
      } else if ("action.names.dressup".equals(action)) {
         this.setCurrentAction(Action.STRIP);
      }
   }

   protected void setOwner(boolean flag, UUID uuid) {
      super.triggerActionSync(flag, true, uuid);
      HandlePlayerMovement.setMovementLock(false);
   }

   @Override
   public void goToSexBed() {
      BlockPos bedPos = this.getNearestBed(this.getPosition());
      if (bedPos == null) {
         this.playSound(SoundHandler.GIRLS_JENNY_HMPH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
      } else {
         this.tasks.removeTask(this.wanderGoal);
         this.tasks.removeTask(this.watchClosestGirlGoal);
         Vec3d bedVec = new Vec3d(bedPos.getX(), bedPos.getY(), bedPos.getZ());
         int[] yaws = new int[]{0, 180, -90, 90};
         Vec3d[][] offsets = new Vec3d[][]{
            {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
            {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
            {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
            {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
         };
         int bestIndex = -1;

         for (int i = 0; i < offsets.length; i++) {
            Vec3d offsetVec = bedVec.add(offsets[i][1]);
            if (this.world.getBlockState(new BlockPos(offsetVec.x, offsetVec.y, offsetVec.z)).getBlock()
               == Blocks.AIR) {
               if (bestIndex == -1) {
                  bestIndex = i;
               } else {
                  double bestDist = this.getPosition()
                     .distanceSq(
                        bedVec.add(offsets[bestIndex][0]).x,
                        bedVec.add(offsets[bestIndex][0]).y,
                        bedVec.add(offsets[bestIndex][0]).z
                     );
                  double dist = this.getPosition()
                     .distanceSq(
                        bedVec.add(offsets[i][0]).x,
                        bedVec.add(offsets[i][0]).y,
                        bedVec.add(offsets[i][0]).z
                     );
                  if (dist < bestDist) {
                     bestIndex = i;
                  }
               }
            }
         }

         if (bestIndex == -1) {
            this.playSound(SoundHandler.GIRLS_JENNY_HMPH[2]);
            this.sendChatMessage(I18n.format("jenny.dialogue.bedobscured", new Object[0]));
            return;
         }

         Vec3d bedOffset = bedVec.add(offsets[bestIndex][0]);
         this.setAnchored(false);
         this.setYawRotation(yaws[bestIndex]);
         this.setTargetPosition(new Vec3d(bedOffset.x, bedOffset.y, bedOffset.z));
         this.cameraYaw = this.getYawRotation();
         this.getNavigator().clearPath();
         this.getNavigator().tryMoveToXYZ(bedOffset.x, bedOffset.y, bedOffset.z, 0.35);
         this.zFlag = true;
         this.ad = 0;
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.DOGGYCUM || action != Action.DOGGYSLOW && action != Action.DOGGYFAST) {
         if (currentAction != Action.CUMBLOWJOB || action != Action.THRUSTBLOWJOB && action != Action.SUCKBLOWJOB) {
            if (currentAction != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
               super.setCurrentAction(action);
               if (currentAction == Action.STARTBLOWJOB || currentAction == Action.PAIZURI_START) {
                  UUID uuid = this.getInteractionPlayerUUID();
                  if (uuid != null) {
                     EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                     if (player != null) {
                        Vec3d offset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.2), this.getYawRotation() + 180.0F);
                        player.setPositionAndUpdate(player.posX + offset.x, player.posY, player.posZ + offset.z);
                     }
                  }
               }
            }
         }
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.SUCKBLOWJOB || action == Action.THRUSTBLOWJOB) {
         this.positionPlayerRelative(0.0, 0.0, 0.0, 0.0F, 70.0F);
         return Action.CUMBLOWJOB;
      } else if (action == Action.DOGGYSLOW || action == Action.DOGGYFAST) {
         return Action.DOGGYCUM;
      } else {
         return action != Action.PAIZURI_FAST && action != Action.PAIZURI_SLOW ? null : Action.PAIZURI_CUM;
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      switch (action) {
         case SUCKBLOWJOB:
            return Action.THRUSTBLOWJOB;
         case DOGGYSLOW:
            return Action.DOGGYFAST;
         case PAIZURI_SLOW:
            if (this.ae) {
               this.ae = false;
               this.positionPlayerRelative(0.0, 0.0, 0.2F, 0.0F, 70.0F);
            }

            return Action.PAIZURI_FAST;
         default:
            return null;
      }
   }

   @Override
   public void setDismounted() {
      this.ab = true;
   }

   @Override
   public void reinitTasks() {
      this.wanderGoal = new EntityAIWanderAvoidWater(this, 0.35);
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(5, this.watchClosestGirlGoal);
      this.tasks.addTask(5, this.wanderGoal);
   }

   @Override
   protected void U() {
      switch ((String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)) {
         case "strip":
            this.resetGirlState();
            this.setCurrentAction(Action.STRIP);
            break;
         case "blowjob":
            this.setCurrentAction(Action.STARTBLOWJOB);
            break;
         case "boobjob":
            if ((Integer)this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) != 0) {
               this.setCurrentAction(Action.STRIP);
               return;
            }

            this.setCurrentAction(Action.PAIZURI_START);
            break;
         case "doggy":
            if ((Integer)this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) != 0) {
               this.setCurrentAction(Action.STRIP);
               this.resetGirlState();
               return;
            }

            this.resetCameraAndPhysics();
            if (this.world.isRemote) {
               PacketHandler.networkWrapper.sendToServer(new SendGirlToSexPacket(this.getGirlId()));
            } else {
               this.resetGirlState();
               this.goToSexBed();
            }
      }

      if (this.world.isRemote) {
         this.changeDataParameterFromClient("animationFollowUp", "");
      } else {
         this.entityDataManager.set(BaseGirlEntity.GIRL_HAND_STATES, "");
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.jenny.fhappy", true, event);
            } else {
               this.createAnimation("animation.jenny.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL && this.getCurrentAction() != null) {
               this.createAnimation("animation.jenny.null", true, event);
            } else if (this.isRiding()) {
               this.createAnimation("animation.jenny.sit", true, event);
            } else if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               switch (this.getWalkType()) {
                  case RUN:
                     this.createAnimation("animation.jenny.run", true, event);
                     break;
                  case FAST_WALK:
                     this.createAnimation("animation.jenny.fastwalk", true, event);
                     break;
                  case WALK:
                     this.createAnimation("animation.jenny.walk", true, event);
               }

               this.rotationYaw = this.rotationYawHead;
            } else {
               this.createAnimation("animation.jenny.idle", true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case SUCKBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobsuck", true, event);
                  break;
               case DOGGYSLOW:
                  this.createAnimation("animation.jenny.doggyslow", true, event);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.jenny.paizuri_slow", true, event);
                  break;
               case NULL:
                  this.createAnimation("animation.jenny.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.jenny.strip", false, event);
                  break;
               case PAYMENT:
                  this.createAnimation("animation.jenny.payment", false, event);
                  break;
               case STARTBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobintro", false, event);
                  break;
               case THRUSTBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobthrust", true, event);
                  break;
               case CUMBLOWJOB:
                  this.createAnimation("animation.jenny.blowjobcum", false, event);
                  break;
               case STARTDOGGY:
                  this.createAnimation("animation.jenny.doggygoonbed", false, event);
                  break;
               case WAITDOGGY:
                  this.createAnimation("animation.jenny.doggywait", true, event);
                  break;
               case DOGGYSTART:
                  this.createAnimation("animation.jenny.doggystart", false, event);
                  break;
               case DOGGYFAST:
                  this.createAnimation("animation.jenny.doggyfast_" + (this.aa ? "hard" : "soft"), true, event);
                  break;
               case DOGGYCUM:
                  this.createAnimation("animation.jenny.doggycum", false, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.jenny.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.jenny.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.jenny.ride", true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.jenny.sit", true, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.jenny.throwpearl", false, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.jenny.downed", true, event);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.jenny.paizuri_start", false, event);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.jenny.paizuri_fast", true, event);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.jenny.paizuri_cum", false, event);
                  break;
               case WAVE:
                  this.createAnimation("animation.jenny.wave", true, event);
                  break;
               case WAVE_IDLE:
                  this.createAnimation("animation.jenny.wave_idle", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "attackSound":
               this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG);
               break;
            case "attackDone":
               this.setCurrentAction(Action.NULL);
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "becomeNude":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", this.entityDataManager.get(BaseGirlEntity.OUTFIT_INDEX) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               if (!((String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)).equals("boobjob")) {
                  this.resetCameraAndPhysics();
               }

               this.U();
               break;
            case "stripMSG1":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.hihi", new Object[0]));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "paymentMSG1":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.huh", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_HUH[1]);
               break;
            case "paymentMSG2":
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.5F);
               String prefix = "<" + Minecraft.getMinecraft().player.getName() + "> ";
               switch ((String)this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES)) {
                  case "strip":
                     this.broadcastChatAround(prefix + I18n.format("jenny.dialogue.showBobsandveganapls", new Object[0]), true);
                     return;
                  case "blowjob":
                     this.broadcastChatAround(prefix + I18n.format("jenny.dialogue.giveblowjob", new Object[0]), true);
                     return;
                  case "doggy":
                     this.broadcastChatAround(prefix + I18n.format("jenny.dialogue.givesex", new Object[0]), true);
                     return;
                  case "boobjob":
                     this.broadcastChatAround(prefix + I18n.format("jenny.dialogue.givebooba", new Object[0]), true);
                     return;
                  default:
                     this.broadcastChatAround(prefix + "sex pls", true);
                     return;
               }
            case "paymentMSG3":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.hehe", new Object[0]));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paymentMSG4":
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.25F);
               break;
            case "paymentDone":
               this.U();
               break;
            case "bjiMSG1":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext1", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_MMM[8]);
               this.cameraYaw = this.rotationYaw + 180.0F;
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG2":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext2", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG3":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext3", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[0]);
               break;
            case "bjiMSG4":
               this.playSound(SoundHandler.MISC_BELLJINGLE[0]);
               break;
            case "bjiMSG5":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext4", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_JENNY_HMPH[1], 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "bjiMSG6":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext5", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[8]);
               break;
            case "bjiMSG7":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext6", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[4]);
               break;
            case "bjiMSG8":
               this.broadcastChatAround(
                  "<" + Minecraft.getMinecraft().player.getName() + "> " + I18n.format("jenny.dialogue.blowjobtext7", new Object[0]), true
               );
               this.playSoundAtVolume(SoundHandler.MISC_PLOB[0], 0.5F);
               break;
            case "bjiMSG9":
               this.sendGirlChatMessage(I18n.format("jenny.dialogue.blowjobtext8", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[2]);
               break;
            case "bjiMSG10":
               if (this.isControlledByLocalPlayer()) {
                  this.positionPlayerRelative(-0.65, -0.8, -0.25, 60.0F, -3.0F);
               }
               break;
            case "bjiMSG11":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }

               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjiMSG12":
               if (Reference.RANDOM.nextInt(5) == 0) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_BJMOAN));
               }

               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "bjtMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIPSOUND));
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
            case "doggyfastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
                  this.aa = true;
               }
               break;
            case "bjtReady":
            case "paizuriReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "bjcMSG1":
               this.playSound(SoundHandler.GIRLS_JENNY_BJMOAN[1]);
               break;
            case "bjcMSG2":
               this.playSound(SoundHandler.GIRLS_JENNY_BJMOAN[7]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "bjcMSG3":
               this.playSound(SoundHandler.GIRLS_JENNY_AFTERSESSIONMOAN[1]);
               break;
            case "bjcMSG4":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[0]);
               break;
            case "bjcMSG5":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[1]);
               break;
            case "bjcMSG6":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[2]);
               break;
            case "bjcMSG7":
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[3]);
               break;
            case "bjcBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "bjcDone":
            case "paizuri_cumDone":
            case "doggyCumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "doggyGoOnBedMSG1":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               this.cameraYaw = this.rotationYaw;
               break;
            case "doggyGoOnBedMSG2":
               this.sendChatMessage(I18n.format("jenny.dialogue.doggytext1", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING[9]);
               break;
            case "doggyGoOnBedMSG3":
               this.sendChatMessage(I18n.format("jenny.dialogue.doggytext2", new Object[0]));
               this.playSound(SoundHandler.GIRLS_JENNY_GIGGLE[0]);
               break;
            case "doggyGoOnBedMSG4":
               this.playSoundAtVolume(SoundHandler.MISC_SLAP[0], 0.75F);
               break;
            case "doggyGoOnBedDone":
               PacketHandler.networkWrapper.sendToServer(new SetPlayerForGirlPacket(this.getGirlId(), Minecraft.getMinecraft().player.getPersistentID()));
               this.setCurrentAction(Action.WAITDOGGY);
               break;
            case "doggystartMSG1":
               this.playSound(SoundHandler.MISC_TOUCH[0]);
               break;
            case "doggystartMSG2":
               this.playSound(SoundHandler.MISC_TOUCH[1]);
               break;
            case "doggystartMSG3":
               this.playSoundAtVolume(SoundHandler.MISC_BEDRUSTLE[1], 0.5F);
               break;
            case "doggystartMSG4":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS));
               this.playSound(SoundHandler.GIRLS_JENNY_MMM[1]);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "doggystartMSG5":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggystartDone":
               this.setCurrentAction(Action.DOGGYSLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doggyslowMSG1":
               this.aa = false;
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.33F);
               int choice = Reference.RANDOM.nextInt(4);
               if (choice == 0) {
                  choice = Reference.RANDOM.nextInt(2);
                  if (choice == 0) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
                  } else {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  }
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.00666);
               }
               break;
            case "doggyslowMSG2":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_LIGHTBREATHING), 0.5F);
               break;
            case "doggyfastMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.ag++;
               if (this.ag % 2 == 0) {
                  int choice2 = Reference.RANDOM.nextInt(2);
                  if (choice2 == 0) {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
                  } else {
                     this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING));
                  }
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }
               break;
            case "doggyfastDone":
               this.aa = false;
               this.setCurrentAction(Action.DOGGYSLOW);
               break;
            case "doggycumMSG1":
               this.playSoundAtVolume(SoundHandler.MISC_CUMINFLATION[0], 2.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 2.0F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MOAN));
               break;
            case "doggycumMSG2":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[4]);
               break;
            case "doggycumMSG3":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[5]);
               break;
            case "doggycumMSG4":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[6]);
               break;
            case "doggycumMSG5":
               this.playSound(SoundHandler.GIRLS_JENNY_HEAVYBREATHING[7]);
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "boobjob_camera":
               UUID uuid = Minecraft.getMinecraft().player.getPersistentID();
               if (uuid.equals(this.world.getClosestPlayerToEntity(this.getSelf(), 2.0).getPersistentID())) {
                  this.cameraYaw = this.world.getPlayerEntityByUUID(uuid).rotationYaw;
                  this.setInteractionPlayerUUID(uuid);
                  if (!this.ae) {
                     this.ae = true;
                     this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
                  }
               }
               break;
            case "paizuri_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.PAIZURI_SLOW);
                  HornyMeterHud.resetHornyMeter();
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.getRNG().nextBoolean()) {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_MMM));
               } else {
                  this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_JENNY_AHH));
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "paizuriSlowMSG1":
            case "paizuriStartMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "paizuri_fastDone":
               this.setCurrentAction(Action.PAIZURI_SLOW);
               if (this.isControlledByLocalPlayer() && !this.ae) {
                  this.ae = true;
                  this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
               }
               break;
            case "paizuri_startStep":
               IBlockState state = this.world.getBlockState(this.getPosition().subtract(new Vec3i(0, 1, 0)));
               this.playSound(state.getBlock().getSoundType(state, this.world, this.getPosition(), this).getStepSound());
               break;
            case "paizuri_cumStart":
               if (this.isControlledByLocalPlayer() && !this.ae) {
                  this.positionPlayerRelative(-0.7, -0.6, 0.2, 60.0F, -3.0F);
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
