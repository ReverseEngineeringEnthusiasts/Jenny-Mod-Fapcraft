package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.LootTableHandler;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
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
 * <b>Role.</b> The Ellie NPC (implements {@link IEllie}) — a tall girl with a
 * couch/bed sit-down state machine (sitdown/sitdownidle), a hug
 * (hug/hugselected/hugidle) dialogue phase and three scenes: cowgirl,
 * missionary (both on a bed) and the carried "Face fuck" (carry intro/slow/
 * fast/cum).
 * <p>
 * <b>Scene entry.</b> {@link #setDismounted()} (invoked by
 * {@link KoboldStatePacket.Handler#sendState} — see the packet) binds the
 * interaction player, faces her by {@code playerYaw - 180}, and starts
 * {@link Action#CARRY_INTRO} anchored. {@link #doAction(String, UUID)} is the
 * client-side scene chooser: it sets {@link Action#HUGSELECTED} and writes the
 * chosen scene into {@code GIRL_HAND_STATES} via
 * {@code changeDataParameterFromClient("animationFollowUp", ...)}; once the
 * hug dialogue finishes, the server-side {@link #U()} dispatches on that value
 * ({@code "Missionary"} -&gt; {@link Action#MISSIONARY_START}, {@code "cowgirl"}
 * -&gt; {@link Action#COWGIRLSTART}) and positions the player.
 * <p>
 * <b>Bed flow.</b> After the scene, {@code handleSitUpTimer} walks her off the
 * bed to a random sit pose (see {@link #getRandomSitPose()}), anchors her and
 * enters {@link Action#SITDOWN}; {@code handleSitDownTimer} (110 ticks) leads
 * to {@link Action#SITDOWNIDLE} where {@link #handleSitIdle()} waits for a
 * nearby player. The horny potion route ({@link #handleHornyPotion()}) makes
 * her dash to the player and start the hug chain instead.
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} refuses re-entry into
 * loop phases while the cum animation plays and arms the carry timer
 * ({@code ak} = 0) on {@link Action#CARRY_INTRO}. The sit-state timers are
 * obfuscated counters: {@code yFlag} (hugidle, 150), {@code al} (sitdown,
 * 109), {@code ai} (sit-up, 79) and {@code zFlag} (dash, 16) — reset them all
 * in {@link #resetSitState()}. {@link #U()} must only run after the hug
 * dialogue; {@code GIRL_HAND_STATES} carries the scene choice from the
 * client packet.
 */
public class EllieEntity extends AbstractGirlNpcEntity implements IEllie {
   static final float ad = 10.0F;
   static final int ao = 16;
   static final int ap = 79;
   static final int ag = 109;
   static final int as = 150;
   static final int ar = 20;
   static final int ab = 110;
   static final int an = 4;
   int ak = -1;
   boolean aq = false;
   boolean ae = false;
   boolean ac = false;
   int af = -1;
   int yFlag = -1;
   int al = -1;
   int ai = -1;
   boolean ah = false;
   Object[] am;
   int zFlag = -1;
   int aa = 1;
   boolean aj = false;

   public EllieEntity(World world) {
      super(world);
      this.slashSwordRot = -85;
      this.stabSwordRot = -175;
      this.holdBowRot = -85;
      this.swordOffsetStab = new Vec3d(-0.1, 0.05, 0.0);
   }

   @Override
   public void onArriveHome() {
      this.sendChatMessage("Okay, I will be residing here then..");
      this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HUH[0], 6.0F);
   }

   @Override
   public String getDisplayNameText() {
      return "Ellie";
   }

   @Override
   protected ResourceLocation getLootTable() {
      return LootTableHandler.ELLIE_TABLE;
   }

   boolean isBedBlocked() {
      return this.isLocallyRegistered() ? false : this.world.getBlockState(this.getPosition().add(0, 2, 0)).getBlock() != Blocks.AIR;
   }

   public float getEyeHeight() {
      return this.isBedBlocked() ? 1.53F : 1.9F;
   }

   @Override
   public float getScaleFactor() {
      return 0.4F;
   }

   /**
    * SERVER: the dismount/scene-entry hook called by {@link KoboldStatePacket}
    * when the player's carry request is accepted. Binds the interaction
    * player, faces her toward him (player yaw - 180) and starts the
    * {@link Action#CARRY_INTRO} scene anchored; falls back to
    * {@link #resetSitState()} when no player is bound.
    */
   @Override
   public void setDismounted() {
      UUID uuid = this.getInteractionPlayerUUID();
      if (uuid == null) {
         this.resetSitState();
      } else {
         EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
         if (player == null) {
            this.resetSitState();
         } else {
            float yaw = player.rotationYaw - 180.0F;
            this.setYawRotation(yaw);
            this.setCurrentAction(Action.CARRY_INTRO);
            this.setAnchored(true);
         }
      }
   }

   @Override
   public boolean shouldRenderNameTag() {
      return this.getCurrentAction() != Action.CARRY_INTRO;
   }

   /**
    * CLIENT: the interaction-menu gate. With the sex flag ({@code dressUp})
    * offers cowgirl/missionary; otherwise opens the dress-up strip menu when
    * already nude, or the "Face fuck" dialogue when dressed.
    */
   public boolean canJoinPlayer(EntityPlayer player, boolean dressUp) {
      if (dressUp) {
         openInventoryGui(player, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
         return true;
      } else if ((Integer)this.entityDataManager.get(OUTFIT_INDEX) == 0) {
         openInventoryGui(player, this, new String[]{"action.names.dressup"}, true);
         return true;
      } else {
         openInventoryGui(player, this, new String[]{"Face fuck"}, true);
         return true;
      }
   }

   @Override
   public void goHome() {
      super.goHome();
      this.sendChatMessage("stay safe darling~");
      this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_SIGH[1], 6.0F);
   }

   /**
    * CLIENT: scene chooser. Delegates to the NPC defaults (follow/equipment/
    * home), then for the scene actions sets {@link Action#HUGSELECTED} and
    * writes the follow-up choice into {@code GIRL_HAND_STATES} via the
    * {@code animationFollowUp} data-parameter packet; {@code Face fuck}
    * instead triggers the carry sync through {@link #triggerActionSync(boolean, boolean, UUID)}.
    * {@link #U()} reads the stored choice server-side.
    */
   @Override
   public void doAction(String action, UUID uuid) {
      super.doAction(action, uuid);
      this.aq = true;
      switch (action) {
         case "action.names.missionary":
            this.setCurrentAction(Action.HUGSELECTED);
            this.changeDataParameterFromClient("animationFollowUp", "Missionary");
            break;
         case "action.names.cowgirl":
            this.setCurrentAction(Action.HUGSELECTED);
            this.changeDataParameterFromClient("animationFollowUp", "cowgirl");
            break;
         case "action.names.dressup":
         case "action.names.strip":
            this.setCurrentAction(Action.STRIP);
            this.changeDataParameterFromClient("animationFollowUp", "");
            break;
         case "Face fuck":
            this.triggerActionSync(true, true, uuid);
            HandlePlayerMovement.setMovementLock(false);
      }
   }

   @Override
   protected void alignPlayerToGirl(EntityPlayerMP playerMP, boolean force) {
   }

   /**
    * Guards the state machine: refuses re-entry into loop phases while the
    * corresponding cum animation plays, arms the carry timer ({@code ak} = 0)
    * on {@link Action#CARRY_INTRO}, and records the sit-up delay ({@code ai} =
    * 79) when {@link Action#HUGSELECTED} is set SERVER-side.
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (action == Action.HUGSELECTED && !this.world.isRemote) {
         this.ai = 79;
      }

      if (currentAction != Action.MISSIONARY_CUM || action != Action.MISSIONARY_FAST && action != Action.MISSIONARY_SLOW) {
         if (currentAction != Action.COWGIRLCUM || action != Action.COWGIRLSLOW && action != Action.COWGIRLFAST) {
            if (currentAction != Action.CARRY_CUM || action != Action.CARRY_SLOW && action != Action.CARRY_FAST) {
               if (action == Action.CARRY_INTRO) {
                  this.ak = 0;
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   /**
    * CLIENT: per-tick scene UI hooks — reopens the sex menu when the
    * {@code ae} one-shot flag is set (hug dialogue done) and shows the horny
    * meter during the carry loop.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.ae) {
         this.canJoinPlayer(Minecraft.getMinecraft().player, true);
         this.ae = false;
      }

      this.handleSitIdle();
      this.showHornyMeter();
   }

   void showHornyMeter() {
      if (!HornyMeterHud.isHornyMeterVisible()) {
         if (this.getCurrentAction() == Action.CARRY_SLOW) {
            HornyMeterHud.showHornyMeter();
         }
      }
   }

   /**
    * Ticks the carry-intro timer ({@code ak}, 110 ticks): when it expires,
    * repositions the interacting player in front of Ellie so the carry scene
    * can start.
    */
   void handleSitTimer() {
      if (this.ak != -1) {
         if (++this.ak >= 110) {
            this.ak = -1;
            if (this.getCurrentAction() == Action.CARRY_INTRO) {
               UUID uuid = this.getInteractionPlayerUUID();
               if (uuid != null) {
                  EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
                  if (player != null) {
                     float yaw = this.getYawRotation();
                     Vec3d pos = this.getTargetPosition().add(VectorMath.rotateByYaw(new Vec3d(0.0, 2.5625F - player.getEyeHeight(), -0.3125), 180.0F + yaw));
                     player.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  }
               }
            }
         }
      }
   }

   void handleSitIdle() {
      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
         if (player != null) {
            if (!(this.getDistance(player) > 1.5F)) {
               if (player.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
                  BeeScreen.enableInteraction();
               }
            }
         }
      }
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.setFirstSit();
      this.handleHornyPotion();
      this.handleSitUpFinish();
      this.handleSitUpTimer();
      this.handleSitDownTimer();
      this.handleHugTimer();
      this.handleSitTransition();
      this.handleStandTimer();
   }

   void setFirstSit() {
      if (!this.ac) {
         this.ac = true;
         this.noClip = false;
         this.setNoGravity(false);
      }
   }

   /**
    * SERVER: the scene dispatcher — reads the scene choice the client stored
    * in {@code GIRL_HAND_STATES} (the {@code animationFollowUp} packet) and
    * starts {@link Action#MISSIONARY_START} or {@link Action#COWGIRLSTART},
    * stripping her and locking the interacting player into position.
    */
   @Override
   protected void U() {
      String handState = (String)this.entityDataManager.get(GIRL_HAND_STATES);
      if ("Missionary".equals(handState)) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.setCurrentAction(Action.MISSIONARY_START);
         UUID uuid = this.getInteractionPlayerUUID();
         if (uuid == null) {
            return;
         }

         EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
         if (player == null) {
            this.resetCameraAndPhysics();
            return;
         }

         player.setNoGravity(true);
         player.noClip = true;
         Vec3d pos = this.getTargetPosition();
         player.rotationYaw = this.getYawRotation();
         Vec3d offset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.1), player.rotationYaw);
         pos = pos.add(offset);
         player.setPositionAndUpdate(pos.x, pos.y, pos.z);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
      }

      if ("cowgirl".equals(handState)) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.setCurrentAction(Action.COWGIRLSTART);
         UUID uuid = this.getInteractionPlayerUUID();
         if (uuid == null) {
            return;
         }

         EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
         if (player == null) {
            this.resetCameraAndPhysics();
            return;
         }

         player.setNoGravity(true);
         player.noClip = true;
         Vec3d pos = this.getTargetPosition();
         player.rotationYaw = this.getYawRotation() + 180.0F;
         Vec3d offset = VectorMath.rotateByYaw(new Vec3d(0.0, 1.0 - player.eyeHeight, -1.8125), player.rotationYaw);
         pos = pos.add(offset);
         player.setPositionAndUpdate(pos.x, pos.y, pos.z);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
      }
   }

   void handleStandTimer() {
      if (--this.af == 0) {
         this.U();
      }
   }

   void handleSitTransition() {
      if (this.getCurrentAction() == Action.SITDOWNIDLE && this.af < 0) {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
         if (player != null) {
            if (!(this.getDistance(player) > 1.5F)) {
               this.af = 20;
               this.setInteractionPlayerUUID(player.getPersistentID());
            }
         }
      }
   }

   void handleHugTimer() {
      if (--this.yFlag == 0) {
         this.setCurrentAction(Action.HUGIDLE);
      }
   }

   void handleSitDownTimer() {
      if (--this.al == 0) {
         this.setCurrentAction(Action.SITDOWNIDLE);
      }
   }

   /**
    * SERVER: the sit-up state machine. When the sit-up delay ({@code ai}, 79
    * ticks) expires or the {@code ah} one-shot is armed, un-anchors Ellie,
    * walks her to a random bed sit pose ({@link #getRandomSitPose()}), then
    * anchors her and starts {@link Action#SITDOWN} (110 ticks via
    * {@code al}) which flows into {@link Action#SITDOWNIDLE}.
    */
   void handleSitUpTimer() {
      if (--this.ai == 0 || this.ah) {
         this.ah = true;
         this.entityDataManager.set(IS_ANCHORED, false);
         this.setCurrentAction(Action.NULL);
         this.noClip = false;
         this.setNoGravity(false);
         if (this.am == null) {
            this.am = this.getRandomSitPose();
         }

         if (this.am == null) {
            this.sendGirlChatMessage("no bed in sight...");
            this.world.playSound(null, this.getPosition(), SoundHandler.GIRLS_ELLIE_SIGH[0], SoundCategory.NEUTRAL, 6.0F, 1.0F);
            this.resetGirlState();
            this.resetSitState();
         } else {
            EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
            if (player != null) {
               player.setNoGravity(false);
               player.noClip = false;
            }

            Vec3d sitPos = (Vec3d)this.am[0];
            int yaw = (Integer)this.am[1];
            if (sitPos.distanceTo(this.getPositionVector()) > 1.0) {
               this.getNavigator().tryMoveToXYZ(sitPos.x, sitPos.y, sitPos.z, 0.35F);
               this.tickPathVelocity();
            } else {
               this.setTargetPosition(sitPos);
               this.setYawRotation(yaw);
               this.setCurrentAction(Action.SITDOWN);
               this.entityDataManager.set(IS_ANCHORED, true);
               this.al = 109;
               this.noClip = true;
               this.setNoGravity(true);
               this.ah = false;
               this.am = null;
            }
         }
      }
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.yFlag = -1;
   }

   /**
    * SERVER: scans the beds around Ellie (expanding radius) and picks the
    * nearest bed side with free space to sit on. Returns a
    * {@code {position, yaw}} pair or null when no usable bed exists within
    * range.
    */
   Object[] getRandomSitPose() {
      int bestIndex = -1;
      int attempts = 0;
      Vec3d[][] offsets = new Vec3d[][]{
         {new Vec3d(0.5, 0.0, -0.18), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0)},
         {new Vec3d(0.5, 0.0, 1.18), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 0.0, -1.0)},
         {new Vec3d(-0.18, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0)},
         {new Vec3d(1.18, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 0.0)}
      };
      int[] yaws = new int[]{0, 180, -90, 90};

      Vec3d bedVec;
      do {
         BlockPos bedPos = this.findNearestBed(this.getPosition(), ++attempts);
         if (bedPos == null) {
            return null;
         }

         bedVec = new Vec3d(bedPos.getX(), bedPos.getY(), bedPos.getZ());

         for (int i = 0; i < offsets.length; i++) {
            Vec3d offsetVec = bedVec.add(offsets[i][1]);
            Block block = this.world.getBlockState(new BlockPos(offsetVec.x, offsetVec.y, offsetVec.z)).getBlock();
            Vec3d headVec = bedVec.add(offsets[i][2]);
            Block headBlock = this.world.getBlockState(new BlockPos(headVec.x, headVec.y, headVec.z)).getBlock();
            if (block == Blocks.AIR && headBlock == Blocks.BED) {
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
      } while (bestIndex == -1);

      Vec3d sitOffset = bedVec.add(offsets[bestIndex][0]);
      return new Object[]{sitOffset, yaws[bestIndex]};
   }

   /**
    * SERVER: the horny-potion route — when Ellie is dosed she locks onto the
    * nearest player, anchors herself, starts the dash ({@link Action#DASH},
    * {@code zFlag} = 16) and removes her wander/watch AI so nothing
    * interrupts the approach.
    */
   void handleHornyPotion() {
      if (this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null) {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
         if (player != null) {
            this.removeActivePotionEffect(HornyPotion.HORNY_POTION);
            this.setInteractionPlayerUUID(player.getPersistentID());
            float yaw = (float)(Math.atan2(this.posZ - player.posZ, this.posX - player.posX) * (180.0 / Math.PI));
            this.setYawRotation(yaw);
            this.setTargetPosition(this.getPositionVector());
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setCurrentAction(Action.DASH);
            this.zFlag = 16;
            this.setNoGravity(true);
            this.noClip = true;
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
            this.tasks.removeTask(this.wanderGoal);
            this.tasks.removeTask(this.watchClosestGirlGoal);
         }
      }
   }

   /**
    * SERVER: completes the dash ({@code zFlag} countdown, 16 ticks) by
    * anchoring Ellie in front of the interacting player and starting the hug
    * ({@link Action#HUG}, {@code yFlag} = 150).
    */
   void handleSitUpFinish() {
      if (--this.zFlag == 0) {
         UUID uuid = this.getInteractionPlayerUUID();
         if (uuid == null) {
            this.resetSitState();
         } else {
            EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
            if (player == null) {
               this.resetSitState();
            } else {
               player.setNoGravity(true);
               player.noClip = true;
               Vec3d backVec = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, -0.5), player.rotationYaw);
               Vec3d sitPos = backVec.add(player.getPositionVector());
               this.setTargetPosition(sitPos);
               this.setYawRotation(player.rotationYaw);
               this.setCurrentAction(Action.HUG);
               this.yFlag = 150;
            }
         }
      }
   }

   /**
    * SERVER: full sit-state teardown — un-anchors, resets the action to
    * {@link Action#NULL}, unbinds the interaction player and re-enables
    * physics. Resets every sit timer ({@code ah}, {@code yFlag}, {@code zFlag},
    * {@code ai}, {@code am}).
    */
   void resetSitState() {
      this.entityDataManager.set(IS_ANCHORED, false);
      this.setCurrentAction(Action.NULL);
      this.setInteractionPlayerUUID(null);
      this.noClip = false;
      this.setNoGravity(false);
      this.ah = false;
      this.yFlag = -1;
      this.zFlag = -1;
      this.ai = -1;
      this.am = null;
   }

   /**
    * CLIENT: interaction gate — refuses interaction while Ellie is already in
    * a scene (bound to a player or the caller is in one), otherwise opens
    * {@link #canJoinPlayer(EntityPlayer, boolean)}.
    */
   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (getActiveSceneInfo(player) != null) {
         return false;
      }

      if (this.getInteractionPlayerUUID() != null) {
         return false;
      }

      if (this.world.isRemote) {
         this.canJoinPlayer(player, false);
      }

      return true;
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.COWGIRLFAST || action == Action.COWGIRLSLOW) {
         return Action.COWGIRLCUM;
      } else if (action == Action.MISSIONARY_FAST || action == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_CUM;
      } else {
         return action != Action.CARRY_SLOW && action != Action.CARRY_FAST ? null : Action.CARRY_CUM;
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.COWGIRLSLOW) {
         return Action.COWGIRLFAST;
      } else if (action == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_FAST;
      } else {
         return action == Action.CARRY_SLOW ? Action.CARRY_FAST : null;
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
               this.createAnimation("animation.ellie.eyes", true, event);
            } else {
               this.createAnimation("animation.ellie.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.ellie.null", true, event);
            } else {
               double moved = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (moved == 0.0) {
                  this.createAnimation(this.isBedBlocked() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, event);
               } else if (this.isBedBlocked()) {
                  this.createAnimation("animation.ellie.crouchwalk", true, event);
               } else {
                  switch (this.getWalkType()) {
                     case RUN:
                        this.createAnimation("animation.ellie.run", true, event);
                        return PlayState.CONTINUE;
                     case FAST_WALK:
                        this.createAnimation("animation.ellie.fastwalk", true, event);
                        return PlayState.CONTINUE;
                     case WALK:
                        this.createAnimation("animation.ellie.walk", true, event);
                  }
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.ellie.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.ellie.strip", false, event);
                  break;
               case DASH:
                  this.createAnimation("animation.ellie.dash", false, event);
                  break;
               case HUG:
                  this.createAnimation("animation.ellie.hug", false, event);
                  break;
               case HUGIDLE:
                  this.createAnimation("animation.ellie.hugidle", true, event);
                  break;
               case HUGSELECTED:
                  this.createAnimation("animation.ellie.hugselected", false, event);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.ellie.sitdown", false, event);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.ellie.sitdownidle", true, event);
                  break;
               case COWGIRLSTART:
                  this.createAnimation("animation.ellie.cowgirlstart", false, event);
                  break;
               case COWGIRLSLOW:
                  this.createAnimation("animation.ellie.cowgirlslow2", true, event);
                  break;
               case COWGIRLFAST:
                  this.createAnimation("animation.ellie.cowgirlfast", true, event);
                  break;
               case COWGIRLCUM:
                  this.createAnimation("animation.ellie.cowgirlcum", true, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.ellie.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.ellie.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.ellie.ride", true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.ellie.sit", true, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.ellie.throwpearl", false, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.ellie.downed", true, event);
                  break;
               case MISSIONARY_START:
                  this.createAnimation("animation.ellie.missionary_start", false, event);
                  break;
               case MISSIONARY_SLOW:
                  this.createAnimation("animation.ellie.missionary_slow", true, event);
                  break;
               case MISSIONARY_FAST:
                  this.createAnimation("animation.ellie.missionary_fast", true, event);
                  break;
               case MISSIONARY_CUM:
                  this.createAnimation("animation.ellie.missionary_cum", false, event);
                  break;
               case CARRY_INTRO:
                  this.createAnimation("animation.ellie.carry_intro", false, event);
                  break;
               case CARRY_SLOW:
                  this.createAnimation("animation.ellie.carry_slow" + this.aa, true, event);
                  break;
               case CARRY_FAST:
                  this.createAnimation("animation.ellie.carry_fast", true, event);
                  break;
               case CARRY_CUM:
                  this.createAnimation("animation.ellie.carry_cum", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener that advances
    * the hug dialogue, carry, cowgirl and missionary scenes. Key transitions:
    * {@code hugDone} reopens the scene menu, {@code stripDone} exits the
    * strip, {@code cowgirlStartDone}/{@code missionary_startDone} -&gt; slow
    * loops, jump on the fast-done keyframes keeps the fast loop,
    * {@code carry_slowDone} re-rolls the variant suffix ({@code aa}), and
    * {@code missionary_cumDone}/{@code cowgirlcumDone}/{@code carry_cumDone}
    * -&gt; {@code resetCameraAndPhysics()}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "becomeNude":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", this.entityDataManager.get(OUTFIT_INDEX) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               this.setCurrentAction((Action)null);
               this.resetCameraAndPhysics();
               this.U();
               break;
            case "hugMSG2":
               this.sendGirlChatMessage("Hmm...");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HMPH[3], 6.0F);
               break;
            case "hugMSG3":
               this.sendGirlChatMessage("Hey!");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HUH[1], 1.0F);
               break;
            case "hugMSG4":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.mommyhorny", new Object[0]));
               this.playRandomSoundAtVolume(SoundHandler.GIRLS_ELLIE_MOMMYHORNY, 0.5F);
               break;
            case "hugMSG5":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.whattodo", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HUH[1], 6.0F);
               break;
            case "hugDone":
               if (this.isControlledByLocalPlayer()) {
                  this.canJoinPlayer(Minecraft.getMinecraft().player, true);
               }
               break;
            case "hugselectedMSG1":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.iknow", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_HMPH[3], 6.0F);
               break;
            case "hugselectedMSG2":
               this.sendGirlChatMessage(I18n.format("ellie.dialogue.followmedarling", new Object[0]));
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               if (this.isControlledByLocalPlayer()) {
                  HandlePlayerMovement.setMovementLock(true);
               }
               break;
            case "sitdownMSG1":
               this.playRandomSoundAtVolume(SoundHandler.GIRLS_ELLIE_COMETOMOMMY, 0.5F);
               if (this.isLocalPlayerNearby()) {
                  this.sendGirlChatMessage(I18n.format("ellie.dialogue.cometomommy", new Object[0]));
               }
               break;
            case "cowgirlStartMSG0":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[4], 6.0F);
               break;
            case "cowgirlStartMSG1":
               if (this.isLocalPlayerNearby()) {
                  this.sendChatMessage(I18n.format("ellie.dialogue.like", new Object[0]));
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "cowgirlStartMSG2":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cowgirlStartDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.COWGIRLSLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "cowgirlfastMSG1":
               if (this.aj) {
                  this.aj = false;
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               }

               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "cowgirlfastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.COWGIRLSLOW);
               }
               break;
            case "cowgirlfastdomMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.2);
               }
               break;
            case "cowgirlcumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG2":
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_MOAN[5], 3.0F);
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG3":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.75F);
               break;
            case "cowgirlcumMSG4":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.hideHornyMeter();
               }
               break;
            case "cowgirlcumMSG5":
            case "missionary_cumMSG2":
               this.playRandomSoundAtVolume(SoundHandler.GIRLS_ELLIE_GOODBOY, 0.5F);
               if (this.isControlledByLocalPlayer()) {
                  this.sendChatMessage(I18n.format("ellie.dialogue.goodboy", new Object[0]));
               }
               break;
            case "cowgirlcumMSG6":
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "missionary_cumDone":
            case "cowgirlcumDone":
            case "carry_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "attackSound":
               this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_STRONG);
               break;
            case "attackDone":
               this.setCurrentAction(Action.NULL);
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "openSexUi":
               if (this.isLocalPlayerNearby()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "missionary_slowMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.getRNG().nextBoolean() && this.getRNG().nextBoolean()) {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 6.0F);
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "missionary_fastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (!this.getRNG().nextBoolean() && !this.getRNG().nextBoolean()) {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               } else {
                  this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_MOAN), 6.0F);
               }

               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.05);
               }
               break;
            case "missionary_startDone":
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.MISSIONARY_SLOW);
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "missionary_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.MISSIONARY_SLOW);
               }
               break;
            case "bedRustle":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "bedRustle1":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[1]);
               break;
            case "missionary_cumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.GIRLS_ELLIE_AHH), 6.0F);
               break;
            case "carry_introMSG1":
               this.sendChatMessage("I'm hungry..");
               this.playRandomSoundAtVolume(SoundHandler.GIRLS_ELLIE_HMPH, 6.0F);
               break;
            case "carry_introMSG2":
               this.sendChatMessage("heh~");
               this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_GIGGLE[3], 6.0F);
               break;
            case "lipsound":
               this.playRandomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "carry_slowDone":
               int oldState = this.aa;

               do {
                  this.aa = this.getRNG().nextInt(4) + 1;
               } while (this.aa == oldState);

               return;
            case "carry_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.CARRY_SLOW);
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
