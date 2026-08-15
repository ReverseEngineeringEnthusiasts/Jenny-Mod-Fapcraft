package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IEllie;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SendGirlToSexPacket;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.LootTableHandler;
import com.trolmastercard.sexmod.util.IBeddableSexGirl;
import java.util.UUID;
import javax.vecmath.Vector4d;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
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
 * Bia NPC — the catgirl with talk, headpat, strip, and bed scenes
 * (anal + prone doggy).
 * <p>
 * <b>Scene entry</b> (shared with Jenny/Luna/Kobold): client {@code doAction}
 * sets {@code animationFollowUp} (GIRL_HAND_STATES) via
 * {@code ChangeDataParameterPacket} and sends {@code KoboldStatePacket}; the
 * server calls {@code setDismounted()} ({@link #yFlag}), then
 * {@link #updateAITasks()} lerps her to {@code TARGET_POS} for ~40 ticks
 * ({@code ag} counter), anchors her and calls {@link #U()} which dispatches
 * on GIRL_HAND_STATES: talk -> TALK_HORNY dialogue, then the anal/doggy menu;
 * strip -> STRIP animation.
 * <p>
 * <b>Bed scenes</b> (anal/doggy): {@link #U()} sends
 * {@code SendGirlToSexPacket} -&gt; {@link #goToSexBed()} walks her to the
 * nearest bed ({@link #af}/{@code zFlag} walk state), anchors her, and
 * {@link #handleAnalState()} runs the scene pickup: when the player stands
 * within 1 block, a <b>jar-verified 22-tick countdown</b> ({@link #ac})
 * runs before the scene action (ANAL_START / PRONE_DOGGY_INTRO) begins.
 * <p>
 * <b>Pitfall:</b> {@link #handleAnalState()} MUST assign {@code ac = 22} on
 * first contact — the original static field was {@code j = 22} on
 * {@code BaseGirlEntity}. A deobf regression set {@code ac = -1} instead,
 * which permanently stalled every Bia bed scene at ANAL_WAIT/SITDOWNIDLE.
 * The player-girl twin {@code BiaPlayerEntity.handleBiaAnalState} kept the
 * correct {@code ar = 22} — use it as the reference.
 */
public class BiaEntity extends AbstractGirlNpcEntity implements IEllie, IBeddableSexGirl {
   static final int ae = 3;
   /** Set by {@code setDismounted()}: true while the scene-entry lerp runs (40 ticks). */
   public boolean yFlag = false;

   /** Dismount-lerp tick counter (0..40). */
   int ag = 0;

   /** Bed-walk state: true while walking to the bed (re-path at 60/120). */
   boolean af = false;

   /** Bed-walk tick counter. */
   int zFlag = 0;

   /** One-shot no-gravity cleanup guard. */
   boolean ab = true;

   /**
    * Scene-pickup countdown for {@code handleAnalState()}: -1 = waiting for
    * first player contact, 22 = counting down to the scene action, 0 = start.
    * <b>Must be set to 22 on first contact</b> (jar-verified; the original
    * static field was {@code j = 22}).
    */
   int ac = -1;

   /** Set while a bed scene transition is in flight (suppresses the camera reset). */
   boolean aa = false;
   final int[] ai = new int[]{0, 180, -90, 90};
   final Vec3d[][] ad = new Vec3d[][]{
      {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
      {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
      {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
      {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
   };
   int ah = 1;

   public BiaEntity(World world) {
      super(world);
      this.setSize(0.49F, 1.65F);
      this.slashSwordRot = 140;
      this.stabSwordRot = 50;
      this.holdBowRot = 140;
      this.swordOffsetStab = new Vec3d(0.0, -0.029999997854232782, -0.2);
   }

   @Override
   public String getDisplayNameText() {
      return "Bia";
   }

   @Override
   public float getScaleFactor() {
      return -0.2F;
   }

   @Override
   public void onArriveHome() {
      this.sendChatMessage("I am living here now nya~");
      this.playRandomSound(SoundHandler.GIRLS_BIA_BREATH);
   }

   @Override
   public void setDismounted() {
      this.yFlag = true;
   }

   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction == Action.ANAL_CUM || currentAction == Action.PRONE_DOGGY_CUM) {
         this.entityDataManager.set(GIRL_HAND_STATES, "");
      }

      if (currentAction != Action.ANAL_CUM || action != Action.ANAL_FAST && action != Action.ANAL_SLOW) {
         if (currentAction != Action.PRONE_DOGGY_CUM || action != Action.PRONE_DOGGY_HARD && action != Action.PRONE_DOGGY_SOFT) {
            super.setCurrentAction(action);
         }
      }
   }

   @Override
   protected ResourceLocation getLootTable() {
      return LootTableHandler.BIA_TABLE;
   }

   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.ab) {
         this.setNoGravity(false);
         this.noClip = false;
         this.ab = false;
      }

      if (this.yFlag) {
         this.ag++;
         if (!this.getPositionVector().equals(this.getTargetPosition()) && this.ag <= 40) {
            this.rotationYaw = this.getYawRotation();

            try {
               TARGET_POS.equals(null);
            } catch (NullPointerException ex) {
               this.setTargetPosition(this.getFrontOffsetVector());
            }

            this.setNoGravity(false);
            Vec3d pos = RotationHelper.lerpVec3d(this.getPositionVector(), this.getTargetPosition(), 40 - this.ag);
            this.setPosition(pos.x, pos.y, pos.z);
         } else {
            this.yFlag = false;
            this.ag = 0;
            this.setYawRotation(this.world.getMinecraftServer().getPlayerList().getPlayerByUUID(this.getInteractionPlayerUUID()).rotationYaw + 180.0F);
            this.entityDataManager.set(IS_ANCHORED, true);
            this.getNavigator().clearPath();
            this.U();
         }
      }

      if (this.af) {
         if (!(this.getPositionVector().distanceTo(this.getTargetPosition()) < 0.6) && this.zFlag <= 200) {
            this.zFlag++;
            if (this.zFlag == 60 || this.zFlag == 120) {
               this.getNavigator().clearPath();
               this.getNavigator().tryMoveToXYZ(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, 0.35);
            }
         } else {
            this.af = false;
            this.entityDataManager.set(IS_ANCHORED, true);
            this.zFlag = 0;
            this.noClip = true;
            this.setNoGravity(true);
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
            if ("anal".equals(this.entityDataManager.get(GIRL_HAND_STATES))) {
               this.setCurrentAction(Action.ANAL_PREPARE);
               this.setOutfitIndex(0);
            } else {
               this.setCurrentAction(Action.SITDOWN);
            }
         }
      }
   }

   public boolean processInteract(EntityPlayer player, EnumHand hand) {
      if (super.processInteract(player, hand)) {
         return true;
      }

      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         return true;
      }

      ItemStack stack = player.getHeldItem(hand);
      boolean isNameTag = stack.getItem() == Items.NAME_TAG;
      if (isNameTag) {
         stack.interactWithEntity(player, this, hand);
         return true;
      }

      if (this.world.isRemote && !this.openInteractionMenu(player)) {
         this.sendChatMessage(I18n.format("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      if (this.getInteractionPlayerUUID() == null
         && (!this.hasMaster() || ((String)this.entityDataManager.get(MASTER)).equals(Minecraft.getMinecraft().player.getPersistentID().toString()))) {
         String[] options = new String[]{
            this.entityDataManager.get(OUTFIT_INDEX) == 1 ? "action.names.strip" : "action.names.dressup", "action.names.talk", "action.names.headpat"
         };
         openInventoryGui(player, this, options, true);
         return true;
      } else {
         return false;
      }
   }

   void openBiaInventory(EntityPlayer player) {
      openInventoryGui(player, this, new String[]{"action.names.anal", "doggy"}, false);
   }

   @Override
   public void ac() {
      if (this.isAnchored() && !this.aa) {
         this.resetCameraAndPhysics();
      }

      this.aa = false;
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote && this.isControlledByLocalPlayer() && this.getCurrentAction() == Action.PRONE_DOGGY_INTRO && !BeeScreen.isBeeScreenVisible()) {
         HornyMeterHud.showHornyMeter();
      }

      this.handleAnalState();
   }

   @Override
   protected void resetLocalPlayerClientState() {
      super.resetLocalPlayerClientState();
      this.ac = -1;
   }

   void handleAnalState() {
      Action action = this.getCurrentAction();
      if (action == Action.ANAL_WAIT || action == Action.SITDOWNIDLE) {
         EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
         if (player != null) {
            if (!(player.getDistance(this) > 1.0F)) {
               if (this.ac == -1) {
                  SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.handleAnalState action=%s ac==-1 (world remote=%s)", action, this.world.isRemote);
                  if (this.world.isRemote) {
                     BeeScreen.enableInteraction();
                     HandlePlayerMovement.setMovementLock(false);
                  } else {
                     this.setInteractionPlayerUUID(player.getPersistentID());
                  }

                  // jar-faithful countdown: BaseGirlEntity's static j == 22 ticks
                  this.ac = 22;
                  SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.handleAnalState: ac set to 22");
               } else if (--this.ac <= 0) {
                  this.ac = -1;
                  SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.handleAnalState: countdown done, starting scene action=%s (remote=%s)", action, this.world.isRemote);
                  player.noClip = true;
                  player.setNoGravity(true);
                  if (action == Action.ANAL_WAIT) {
                     if (!this.world.isRemote) {
                        this.setCurrentAction(Action.ANAL_START);
                        Vec3d pos = this.getTargetPosition().add(VectorMath.rotateByYaw(-0.3, -1.0, -0.5, this.getYawRotation()));
                        player.setPositionAndUpdate(pos.x, pos.y, pos.z);
                     } else if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.showHornyMeter();
                     }
                  } else {
                     float yaw = this.getYawRotation();
                     player.rotationYaw = yaw;
                     player.rotationPitch = 60.0F;
                     if (!this.world.isRemote) {
                        this.setOutfitIndex(0);
                        this.setCurrentAction(Action.PRONE_DOGGY_INTRO);
                        Vec3d targetPos = this.getTargetPosition();
                        Vec3d followPos = targetPos.add(VectorMath.rotateByYaw(0.0, 0.0, 1.0, yaw));
                        this.setTargetPosition(followPos);
                        Vec3d playerPos = targetPos.add(VectorMath.rotateByYaw(0.0, 1.1875 - player.getEyeHeight(), 0.5, yaw));
                        player.setPositionAndUpdate(playerPos.x, playerPos.y, playerPos.z);
                        this.setAnchored(true);
                     }
                  }
               } else if (this.ac % 5 == 0) {
                  SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.handleAnalState: counting down ac=%d", this.ac);
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void resetAnimationControllerTicks() {
      super.resetAnimationControllerTicks();
      if (this.getCurrentAction() == Action.PRONE_DOGGY_HARD) {
         int oldState = this.ah;

         do {
            this.ah = this.getRNG().nextInt(3) + 1;
         } while (oldState == this.ah);
      }
   }

   @Override
   public void reinitTasks() {
      this.wanderGoal = new EntityAIWanderAvoidWater(this, 0.35);
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(5, this.watchClosestGirlGoal);
      this.tasks.addTask(5, this.wanderGoal);
   }

   @Override
   public void doAction(String action, UUID uuid) {
      SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.doAction %s player=%s (remote=%s)", action, uuid, this.world.isRemote);
      super.doAction(action, uuid);
      switch (action) {
         case "action.names.talk":
            this.setInteractionPlayerUUID(Minecraft.getMinecraft().player.getPersistentID());
            this.changeDataParameterFromClient("playerSheHasSexWith", Minecraft.getMinecraft().player.getPersistentID().toString());
            this.changeDataParameterFromClient("animationFollowUp", "talkHorny");
            this.triggerAnalAction(uuid);
            break;
         case "action.names.headpat":
            this.setInteractionPlayerUUID(Minecraft.getMinecraft().player.getPersistentID());
            this.changeDataParameterFromClient("playerSheHasSexWith", Minecraft.getMinecraft().player.getPersistentID().toString());
            this.changeDataParameterFromClient("animationFollowUp", "Headpat");
            this.triggerAnalAction(uuid);
            break;
         case "action.names.anal":
            this.changeDataParameterFromClient("animationFollowUp", "anal");
            this.setCurrentAction(Action.TALK_RESPONSE);
            this.aa = true;
            break;
         case "doggy":
            this.changeDataParameterFromClient("animationFollowUp", "doggy");
            this.setCurrentAction(Action.TALK_RESPONSE);
            this.aa = true;
            break;
         case "action.names.dressup":
         case "action.names.strip":
            this.setCurrentAction(Action.STRIP);
      }
   }

   public void onDeath(DamageSource source) {
      super.onDeath(source);
      if (!this.world.isRemote) {
         EntityItem item = new EntityItem(
            this.world,
            this.posX,
            this.posY,
            this.posZ,
            new ItemStack(Blocks.WOOL, this.getRNG().nextInt(4), 12)
         );
         this.world.spawnEntity(item);
      }
   }

   void triggerAnalAction(UUID uuid) {
      this.triggerActionSync(true, true, uuid);
      HandlePlayerMovement.setMovementLock(false);
   }

   Vector4d getBedVector() {
      BlockPos bedPos = null;
      int attempts = 0;

      while (!this.isValidBed(bedPos)) {
         bedPos = this.findNearestBed(this.getPosition(), attempts);
         if (++attempts == 50) {
            break;
         }
      }

      if (bedPos != null && attempts != 50) {
         this.tasks.removeTask(this.wanderGoal);
         this.tasks.removeTask(this.watchClosestGirlGoal);
         Vec3d bedVec = new Vec3d(bedPos.getX(), bedPos.getY(), bedPos.getZ());
         int bestIndex = -1;

         for (int i = 0; i < this.ad.length; i++) {
            Vec3d offsetPos = bedVec.add(this.ad[i][1]);
            Vec3d offsetNeg = bedVec.subtract(this.ad[i][1]);
            Block block = this.world.getBlockState(new BlockPos(offsetPos.x, offsetPos.y, offsetPos.z)).getBlock();
            if (block == Blocks.AIR && WorldUtils.canPlaceStructure(this.world, new BlockPos(offsetNeg))) {
               if (bestIndex == -1) {
                  bestIndex = i;
               } else {
                  double bestDist = this.getPosition()
                     .distanceSq(
                        bedVec.add(this.ad[bestIndex][0]).x,
                        bedVec.add(this.ad[bestIndex][0]).y,
                        bedVec.add(this.ad[bestIndex][0]).z
                     );
                  double dist = this.getPosition()
                     .distanceSq(
                        bedVec.add(this.ad[i][0]).x,
                        bedVec.add(this.ad[i][0]).y,
                        bedVec.add(this.ad[i][0]).z
                     );
                  if (dist < bestDist) {
                     bestIndex = i;
                  }
               }
            }
         }

         if (bestIndex == -1) {
            this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
            this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
            return null;
         } else {
            Vec3d bedOffset = bedVec.add(this.ad[bestIndex][0]);
            return new Vector4d(bedOffset.x, bedOffset.y, bedOffset.z, this.ai[bestIndex]);
         }
      } else {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }
   }

   boolean isValidBed(BlockPos pos) {
      if (pos == null) {
         return false;
      } else if (WorldUtils.canPlaceStructure(this.world, pos.north()) && this.world.isAirBlock(pos.south())) {
         return true;
      } else if (WorldUtils.canPlaceStructure(this.world, pos.east()) && this.world.isAirBlock(pos.west())) {
         return true;
      } else {
         return WorldUtils.canPlaceStructure(this.world, pos.south()) && this.world.isAirBlock(pos.north())
            ? true
            : WorldUtils.canPlaceStructure(this.world, pos.west()) && this.world.isAirBlock(pos.east());
      }
   }

   Vector4d findNearestBedVector() {
      BlockPos bedPos = this.getNearestBed(this.getPosition());
      if (bedPos == null) {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }

      this.tasks.removeTask(this.wanderGoal);
      this.tasks.removeTask(this.watchClosestGirlGoal);
      Vec3d bedVec = new Vec3d(bedPos.getX(), bedPos.getY(), bedPos.getZ());
      int bestIndex = -1;

      for (int i = 0; i < this.ad.length; i++) {
         Vec3d offsetPos = bedVec.add(this.ad[i][1]);
         if (this.world.getBlockState(new BlockPos(offsetPos.x, offsetPos.y, offsetPos.z)).getBlock()
            == Blocks.AIR) {
            if (bestIndex == -1) {
               bestIndex = i;
            } else {
               double bestDist = this.getPosition()
                  .distanceSq(
                     bedVec.add(this.ad[bestIndex][0]).x,
                     bedVec.add(this.ad[bestIndex][0]).y,
                     bedVec.add(this.ad[bestIndex][0]).z
                  );
               double dist = this.getPosition()
                  .distanceSq(
                     bedVec.add(this.ad[i][0]).x,
                     bedVec.add(this.ad[i][0]).y,
                     bedVec.add(this.ad[i][0]).z
                  );
               if (dist < bestDist) {
                  bestIndex = i;
               }
            }
         }
      }

      if (bestIndex == -1) {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.bedobscured", new Object[0]));
         return null;
      } else {
         Vec3d bedOffset = bedVec.add(this.ad[bestIndex][0]);
         return new Vector4d(bedOffset.x, bedOffset.y, bedOffset.z, this.ai[bestIndex]);
      }
   }

   @Override
   public void goToSexBed() {
      String stateStr = (String)this.entityDataManager.get(GIRL_HAND_STATES);
      Vector4d bedVec4 = stateStr.equals("anal") ? this.findNearestBedVector() : this.getBedVector();
      if (bedVec4 != null) {
         Vec3d pos = new Vec3d(bedVec4.getX(), bedVec4.getY(), bedVec4.getZ());
         this.setYawRotation((float)bedVec4.getW());
         this.setTargetPosition(pos);
         this.cameraYaw = this.getYawRotation();
         this.getNavigator().clearPath();
         this.getNavigator().tryMoveToXYZ(pos.x, pos.y, pos.z, 0.35);
         this.af = true;
         this.zFlag = 0;
      }
   }

   @Override
   protected Action getNextAction(Action action) {
      if (action == Action.ANAL_SLOW) {
         return Action.ANAL_FAST;
      } else {
         return action == Action.PRONE_DOGGY_INTRO ? Action.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.ANAL_SLOW || action == Action.ANAL_FAST) {
         return Action.ANAL_CUM;
      } else {
         return action != Action.PRONE_DOGGY_SOFT && action != Action.PRONE_DOGGY_HARD ? null : Action.PRONE_DOGGY_CUM;
      }
   }

   @Override
   protected void U() {
      SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.U() handState=%s action=%s remote=%s", this.entityDataManager.get(GIRL_HAND_STATES), this.getCurrentAction(), this.world.isRemote);
      switch ((String)this.entityDataManager.get(GIRL_HAND_STATES)) {
         case "talkHorny":
            this.setCurrentAction(Action.TALK_HORNY);
            break;
         case "Headpat":
            this.setCurrentAction(Action.HEAD_PAT);
            break;
         case "doggy":
         case "anal":
            this.resetCameraAndPhysics();
            PacketHandler.networkWrapper.sendToServer(new SendGirlToSexPacket(this.getGirlId()));
            return;
      }

      if (this.world.isRemote) {
         this.changeDataParameterFromClient("animationFollowUp", "");
      } else {
         this.entityDataManager.set(GIRL_HAND_STATES, "");
      }
   }

   @Override
   public float getLeftArmAngle() {
      return 35.0F;
   }

   @Override
   public float getRightArmAngle() {
      return 140.0F;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.bia.fhappy", true, event);
            } else {
               this.createAnimation("animation.bia.null", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bia.null", true, event);
            } else if (this.isRiding()) {
               this.createAnimation("animation.bia.sit", true, event);
            } else if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               switch (this.getWalkType()) {
                  case RUN:
                     this.createAnimation("animation.bia.run", true, event);
                     break;
                  case FAST_WALK:
                     this.createAnimation("animation.bia.fastwalk", true, event);
                     break;
                  case WALK:
                     this.createAnimation("animation.bia.walk", true, event);
               }

               this.rotationYaw = this.rotationYawHead;
            } else {
               this.createAnimation("animation.bia.idle", true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bia.null", true, event);
                  break;
               case STRIP:
                  this.createAnimation("animation.bia.strip", false, event);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bia.attack" + this.nextAttack, false, event);
                  break;
               case BOW:
                  this.createAnimation("animation.bia.bowcharge", false, event);
                  break;
               case RIDE:
                  this.createAnimation("animation.bia.ride", true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.bia.sit", true, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bia.throwpearl", false, event);
                  break;
               case DOWNED:
                  this.createAnimation("animation.bia.downed", true, event);
                  break;
               case TALK_HORNY:
                  this.createAnimation("animation.bia.talk_horny2", true, event);
                  break;
               case TALK_IDLE:
                  this.createAnimation("animation.bia.talk_idle2", true, event);
                  break;
               case TALK_RESPONSE:
                  this.createAnimation("animation.bia.talk_response", true, event);
                  break;
               case ANAL_PREPARE:
                  this.createAnimation("animation.bia.anal_prepare", false, event);
                  break;
               case ANAL_WAIT:
                  this.createAnimation("animation.bia.anal_wait", false, event);
                  break;
               case ANAL_START:
                  this.createAnimation("animation.bia.anal_start", true, event);
                  break;
               case ANAL_SLOW:
                  this.createAnimation("animation.bia.anal_slow", true, event);
                  break;
               case ANAL_FAST:
                  this.createAnimation("animation.bia.anal_fast", true, event);
                  break;
               case ANAL_CUM:
                  this.createAnimation("animation.bia.anal_cum", false, event);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.bia.headpat", false, event);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.bia.sitdown", false, event);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.bia.sitdownidle", true, event);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.createAnimation("animation.bia.prone_doggy_intro", true, event);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.createAnimation("animation.bia.prone_doggy_insert", true, event);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.createAnimation("animation.bia.prone_doggy_soft", true, event);
                  break;
               case PRONE_DOGGY_HARD:
                  this.createAnimation("animation.bia.prone_doggy_hard" + this.ah, true, event);
                  break;
               case PRONE_DOGGY_CUM:
                  this.createAnimation("animation.bia.prone_doggy_cum", true, event);
                  break;
               case WAVE_IDLE:
                  this.createAnimation("animation.bia.wave_idle", true, event);
                  break;
               case WAVE:
                  this.createAnimation("animation.bia.wave", true, event);
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
            case "attackDone":
               this.setCurrentAction(Action.NULL);
               if (++this.nextAttack == 3) {
                  this.nextAttack = 0;
               }
               break;
            case "becomeNude":
               if (this.isLocalPlayerNearby()) {
                  this.changeDataParameterFromClient("currentModel", this.entityDataManager.get(OUTFIT_INDEX) == 1 ? "0" : "1");
               }
               break;
            case "stripDone":
               this.resetCameraAndPhysics();
               this.U();
               break;
            case "stripMSG1":
               this.sendChatMessage(I18n.format("bia.dialogue.hihi", new Object[0]));
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_GIGGLE));
               break;
            case "sexUiOn":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "pearl":
               PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               break;
            case "talk_hornyMSG1":
               this.sendChatMessage(I18n.format("bia.dialogue.heya", new Object[0]));
               this.playRandomSound(SoundHandler.GIRLS_BIA_HEY);
               break;
            case "talk_hornyMSG2":
               this.sendChatMessage(I18n.format("bia.dialogue.horny", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[2]);
               break;
            case "talk_hornyMSG3":
               this.sendChatMessage(I18n.format("bia.dialogue.so", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "talk_hornyMSG4":
               this.sendChatMessage(I18n.format("bia.dialogue.fun", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "talk_hornyDone":
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.sound talk_hornyDone (remote=%s, controlled=%s)", this.world.isRemote, this.isControlledByLocalPlayer());
               this.setCurrentAction(Action.TALK_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  this.openBiaInventory(Minecraft.getMinecraft().player);
               }
               break;
            case "talk_responseMSG1":
               this.sendChatMessage(I18n.format("bia.dialogue.huh", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_HUH[2]);
               break;
            case "talk_responseMSG2":
               this.sendChatMessage(I18n.format("bia.dialogue.iuhm", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[1]);
               break;
            case "talk_responseMSG3":
               this.sendChatMessage(I18n.format("bia.dialogue.yes", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[0]);
               break;
            case "talk_responseDone":
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.sound talk_responseDone (remote=%s, controlled=%s, handState=%s)", this.world.isRemote, this.isControlledByLocalPlayer(), this.entityDataManager.get(GIRL_HAND_STATES));
               if (this.isControlledByLocalPlayer()) {
                  this.resetGirlState();
               }

               this.U();
               break;
            case "anal_prepareMSG1":
               this.playSound(SoundHandler.MISC_PLOB[0]);
               break;
            case "anal_prepareMSG2":
               this.playSound(SoundHandler.MISC_BEDRUSTLE[0]);
               break;
            case "anal_prepareDone":
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.sound anal_prepareDone (remote=%s)", this.world.isRemote);
               this.setCurrentAction(Action.ANAL_WAIT);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "anal_startMSG1":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[3]);
               this.playSound(SoundHandler.MISC_POUNDING[34]);
               break;
            case "anal_fastMSG1":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
            case "anal_slowMSG1":
            case "anal_startMSG2":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }

               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_POUNDING), 0.5F);
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "anal_fastDone":
               if (!this.isControlledByLocalPlayer() || HandlePlayerMovement.isJumping) {
                  return;
               }
            case "anal_startDone":
               this.setCurrentAction(Action.ANAL_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "anal_cumMSG2":
               this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_BIA_AHH));
               break;
            case "blackScreen":
            case "anal_cumBlackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "doggy_cumDone":
            case "anal_cumDone":
               SceneDebug.log(SceneDebug.SCENE_ENTRY, "Bia.sound %s (remote=%s, controlled=%s)", sound.sound, this.world.isRemote, this.isControlledByLocalPlayer());
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "headpatMSG1":
               this.sendChatMessage(I18n.format("bia.dialogue.headpats", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_BREATH[0]);
               break;
            case "headpatMSG2":
               this.sendChatMessage(I18n.format("bia.dialogue.hmm", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_MMM[0]);
               break;
            case "headpatMSG3":
               this.sendChatMessage(I18n.format("bia.dialogue.huh2", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_HUH[0]);
               break;
            case "headpatMSG4":
               this.sendChatMessage(I18n.format("bia.dialogue.thankyou", new Object[0]));
               this.playSound(SoundHandler.GIRLS_BIA_GIGGLE[1]);
               break;
            case "headpatDone":
               this.resetCameraAndPhysics();
               break;
            case "sitdownMSG1":
               this.sendChatMessage("come here big boy~");
               this.playRandomSound(SoundHandler.GIRLS_BIA_BREATH);
               break;
            case "sitdownDone":
               this.setCurrentAction(Action.SITDOWNIDLE);
               break;
            case "slide":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_SLIDE));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.005);
               }
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               break;
            case "doggyMoan":
               this.playRandomSound(this.getRNG().nextBoolean() ? SoundHandler.GIRLS_BIA_AHH : SoundHandler.GIRLS_BIA_MMM);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04);
               }
               break;
            case "doggySwitch":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.PRONE_DOGGY_HARD);
               }
               break;
            case "doggyReset":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               break;
            case "orgasm1":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[6]);
               break;
            case "orgasm2":
               this.playSound(SoundHandler.GIRLS_BIA_MMM[7]);
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
