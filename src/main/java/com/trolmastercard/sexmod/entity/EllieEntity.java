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

   public EllieEntity(World var1) {
      super(var1);
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

   @Override
   public void setDismounted() {
      UUID var1 = this.getInteractionPlayerUUID();
      if (var1 == null) {
         this.resetSitState();
      } else {
         EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
         if (var2 == null) {
            this.resetSitState();
         } else {
            float var3 = var2.rotationYaw - 180.0F;
            this.setYawRotation(var3);
            this.setCurrentAction(Action.CARRY_INTRO);
            this.setAnchored(true);
         }
      }
   }

   @Override
   public boolean shouldRenderNameTag() {
      return this.getCurrentAction() != Action.CARRY_INTRO;
   }

   public boolean canJoinPlayer(EntityPlayer var1, boolean var2) {
      if (var2) {
         openInventoryGui(var1, this, new String[]{"action.names.cowgirl", "action.names.missionary"}, false);
         return true;
      } else if ((Integer)this.entityDataManager.get(OUTFIT_INDEX) == 0) {
         openInventoryGui(var1, this, new String[]{"action.names.dressup"}, true);
         return true;
      } else {
         openInventoryGui(var1, this, new String[]{"Face fuck"}, true);
         return true;
      }
   }

   @Override
   public void goHome() {
      super.goHome();
      this.sendChatMessage("stay safe darling~");
      this.playSoundAtVolume(SoundHandler.GIRLS_ELLIE_SIGH[1], 6.0F);
   }

   @Override
   public void doAction(String var1, UUID var2) {
      super.doAction(var1, var2);
      this.aq = true;
      switch (var1) {
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
            this.triggerActionSync(true, true, var2);
            HandlePlayerMovement.setMovementLock(false);
      }
   }

   @Override
   protected void alignPlayerToGirl(EntityPlayerMP var1, boolean var2) {
   }

   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (action == Action.HUGSELECTED && !this.world.isRemote) {
         this.ai = 79;
      }

      if (var2 != Action.MISSIONARY_CUM || action != Action.MISSIONARY_FAST && action != Action.MISSIONARY_SLOW) {
         if (var2 != Action.COWGIRLCUM || action != Action.COWGIRLSLOW && action != Action.COWGIRLFAST) {
            if (var2 != Action.CARRY_CUM || action != Action.CARRY_SLOW && action != Action.CARRY_FAST) {
               if (action == Action.CARRY_INTRO) {
                  this.ak = 0;
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

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

   void handleSitTimer() {
      if (this.ak != -1) {
         if (++this.ak >= 110) {
            this.ak = -1;
            if (this.getCurrentAction() == Action.CARRY_INTRO) {
               UUID var1 = this.getInteractionPlayerUUID();
               if (var1 != null) {
                  EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
                  if (var2 != null) {
                     float var3 = this.getYawRotation();
                     Vec3d var4 = this.getTargetPosition().add(VectorMath.rotateByYaw(new Vec3d(0.0, 2.5625F - var2.getEyeHeight(), -0.3125), 180.0F + var3));
                     var2.setPositionAndUpdate(var4.x, var4.y, var4.z);
                  }
               }
            }
         }
      }
   }

   void handleSitIdle() {
      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 10.0);
         if (var1 != null) {
            if (!(this.getDistance(var1) > 1.5F)) {
               if (var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID())) {
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

   @Override
   protected void U() {
      String var1 = (String)this.entityDataManager.get(GIRL_HAND_STATES);
      if ("Missionary".equals(var1)) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.setCurrentAction(Action.MISSIONARY_START);
         UUID var2 = this.getInteractionPlayerUUID();
         if (var2 == null) {
            return;
         }

         EntityPlayer var3 = this.world.getPlayerEntityByUUID(var2);
         if (var3 == null) {
            this.resetCameraAndPhysics();
            return;
         }

         var3.setNoGravity(true);
         var3.noClip = true;
         Vec3d var4 = this.getTargetPosition();
         var3.rotationYaw = this.getYawRotation();
         Vec3d var5 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.1), var3.rotationYaw);
         var4 = var4.add(var5);
         var3.setPositionAndUpdate(var4.x, var4.y, var4.z);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var3);
      }

      if ("cowgirl".equals(var1)) {
         this.entityDataManager.set(OUTFIT_INDEX, 0);
         this.setCurrentAction(Action.COWGIRLSTART);
         UUID var6 = this.getInteractionPlayerUUID();
         if (var6 == null) {
            return;
         }

         EntityPlayer var7 = this.world.getPlayerEntityByUUID(var6);
         if (var7 == null) {
            this.resetCameraAndPhysics();
            return;
         }

         var7.setNoGravity(true);
         var7.noClip = true;
         Vec3d var9 = this.getTargetPosition();
         var7.rotationYaw = this.getYawRotation() + 180.0F;
         Vec3d var11 = VectorMath.rotateByYaw(new Vec3d(0.0, 1.0 - var7.eyeHeight, -1.8125), var7.rotationYaw);
         var9 = var9.add(var11);
         var7.setPositionAndUpdate(var9.x, var9.y, var9.z);
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var7);
      }
   }

   void handleStandTimer() {
      if (--this.af == 0) {
         this.U();
      }
   }

   void handleSitTransition() {
      if (this.getCurrentAction() == Action.SITDOWNIDLE && this.af < 0) {
         EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 10.0);
         if (var1 != null) {
            if (!(this.getDistance(var1) > 1.5F)) {
               this.af = 20;
               this.setInteractionPlayerUUID(var1.getPersistentID());
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
            EntityPlayer var1 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
            if (var1 != null) {
               var1.setNoGravity(false);
               var1.noClip = false;
            }

            Vec3d var2 = (Vec3d)this.am[0];
            int var3 = (Integer)this.am[1];
            if (var2.distanceTo(this.getPositionVector()) > 1.0) {
               this.getNavigator().tryMoveToXYZ(var2.x, var2.y, var2.z, 0.35F);
               this.tickPathVelocity();
            } else {
               this.setTargetPosition(var2);
               this.setYawRotation(var3);
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

   Object[] getRandomSitPose() {
      int var1 = -1;
      int var2 = 0;
      Vec3d[][] var4 = new Vec3d[][]{
         {new Vec3d(0.5, 0.0, -0.18), new Vec3d(0.0, 0.0, -1.0), new Vec3d(0.0, 0.0, 1.0)},
         {new Vec3d(0.5, 0.0, 1.18), new Vec3d(0.0, 0.0, 1.0), new Vec3d(0.0, 0.0, -1.0)},
         {new Vec3d(-0.18, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0), new Vec3d(1.0, 0.0, 0.0)},
         {new Vec3d(1.18, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0), new Vec3d(-1.0, 0.0, 0.0)}
      };
      int[] var5 = new int[]{0, 180, -90, 90};

      Vec3d var3;
      do {
         BlockPos var6 = this.findNearestBed(this.getPosition(), ++var2);
         if (var6 == null) {
            return null;
         }

         var3 = new Vec3d(var6.getX(), var6.getY(), var6.getZ());

         for (int var7 = 0; var7 < var4.length; var7++) {
            Vec3d var8 = var3.add(var4[var7][1]);
            Block var9 = this.world.getBlockState(new BlockPos(var8.x, var8.y, var8.z)).getBlock();
            Vec3d var10 = var3.add(var4[var7][2]);
            Block var11 = this.world.getBlockState(new BlockPos(var10.x, var10.y, var10.z)).getBlock();
            if (var9 == Blocks.AIR && var11 == Blocks.BED) {
               if (var1 == -1) {
                  var1 = var7;
               } else {
                  double var12 = this.getPosition()
                     .distanceSq(
                        var3.add(var4[var1][0]).x,
                        var3.add(var4[var1][0]).y,
                        var3.add(var4[var1][0]).z
                     );
                  double var14 = this.getPosition()
                     .distanceSq(
                        var3.add(var4[var7][0]).x,
                        var3.add(var4[var7][0]).y,
                        var3.add(var4[var7][0]).z
                     );
                  if (var14 < var12) {
                     var1 = var7;
                  }
               }
            }
         }
      } while (var1 == -1);

      Vec3d var16 = var3.add(var4[var1][0]);
      return new Object[]{var16, var5[var1]};
   }

   void handleHornyPotion() {
      if (this.getActivePotionEffect(HornyPotion.HORNY_POTION) != null) {
         EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 10.0);
         if (var1 != null) {
            this.removeActivePotionEffect(HornyPotion.HORNY_POTION);
            this.setInteractionPlayerUUID(var1.getPersistentID());
            float var2 = (float)(Math.atan2(this.posZ - var1.posZ, this.posX - var1.posX) * (180.0 / Math.PI));
            this.setYawRotation(var2);
            this.setTargetPosition(this.getPositionVector());
            this.entityDataManager.set(IS_ANCHORED, true);
            this.setCurrentAction(Action.DASH);
            this.zFlag = 16;
            this.setNoGravity(true);
            this.noClip = true;
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
            this.tasks.removeTask(this.wanderGoal);
            this.tasks.removeTask(this.watchClosestGirlGoal);
         }
      }
   }

   void handleSitUpFinish() {
      if (--this.zFlag == 0) {
         UUID var1 = this.getInteractionPlayerUUID();
         if (var1 == null) {
            this.resetSitState();
         } else {
            EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
            if (var2 == null) {
               this.resetSitState();
            } else {
               var2.setNoGravity(true);
               var2.noClip = true;
               Vec3d var3 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, -0.5), var2.rotationYaw);
               Vec3d var4 = var3.add(var2.getPositionVector());
               this.setTargetPosition(var4);
               this.setYawRotation(var2.rotationYaw);
               this.setCurrentAction(Action.HUG);
               this.yFlag = 150;
            }
         }
      }
   }

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

   protected boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if (getActiveSceneInfo(var1) != null) {
         return false;
      }

      if (this.getInteractionPlayerUUID() != null) {
         return false;
      }

      if (this.world.isRemote) {
         this.canJoinPlayer(var1, false);
      }

      return true;
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.COWGIRLFAST || var1 == Action.COWGIRLSLOW) {
         return Action.COWGIRLCUM;
      } else if (var1 == Action.MISSIONARY_FAST || var1 == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_CUM;
      } else {
         return var1 != Action.CARRY_SLOW && var1 != Action.CARRY_FAST ? null : Action.CARRY_CUM;
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.COWGIRLSLOW) {
         return Action.COWGIRLFAST;
      } else if (var1 == Action.MISSIONARY_SLOW) {
         return Action.MISSIONARY_FAST;
      } else {
         return var1 == Action.CARRY_SLOW ? Action.CARRY_FAST : null;
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.ellie.eyes", true, var1);
            } else {
               this.createAnimation("animation.ellie.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.ellie.null", true, var1);
            } else {
               double var4 = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (var4 == 0.0) {
                  this.createAnimation(this.isBedBlocked() ? "animation.ellie.crouchidle" : "animation.ellie.idle", true, var1);
               } else if (this.isBedBlocked()) {
                  this.createAnimation("animation.ellie.crouchwalk", true, var1);
               } else {
                  switch (this.getWalkType()) {
                     case RUN:
                        this.createAnimation("animation.ellie.run", true, var1);
                        return PlayState.CONTINUE;
                     case FAST_WALK:
                        this.createAnimation("animation.ellie.fastwalk", true, var1);
                        return PlayState.CONTINUE;
                     case WALK:
                        this.createAnimation("animation.ellie.walk", true, var1);
                  }
               }
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.ellie.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.ellie.strip", false, var1);
                  break;
               case DASH:
                  this.createAnimation("animation.ellie.dash", false, var1);
                  break;
               case HUG:
                  this.createAnimation("animation.ellie.hug", false, var1);
                  break;
               case HUGIDLE:
                  this.createAnimation("animation.ellie.hugidle", true, var1);
                  break;
               case HUGSELECTED:
                  this.createAnimation("animation.ellie.hugselected", false, var1);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.ellie.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.ellie.sitdownidle", true, var1);
                  break;
               case COWGIRLSTART:
                  this.createAnimation("animation.ellie.cowgirlstart", false, var1);
                  break;
               case COWGIRLSLOW:
                  this.createAnimation("animation.ellie.cowgirlslow2", true, var1);
                  break;
               case COWGIRLFAST:
                  this.createAnimation("animation.ellie.cowgirlfast", true, var1);
                  break;
               case COWGIRLCUM:
                  this.createAnimation("animation.ellie.cowgirlcum", true, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.ellie.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.ellie.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.ellie.ride", true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.ellie.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.ellie.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.ellie.downed", true, var1);
                  break;
               case MISSIONARY_START:
                  this.createAnimation("animation.ellie.missionary_start", false, var1);
                  break;
               case MISSIONARY_SLOW:
                  this.createAnimation("animation.ellie.missionary_slow", true, var1);
                  break;
               case MISSIONARY_FAST:
                  this.createAnimation("animation.ellie.missionary_fast", true, var1);
                  break;
               case MISSIONARY_CUM:
                  this.createAnimation("animation.ellie.missionary_cum", false, var1);
                  break;
               case CARRY_INTRO:
                  this.createAnimation("animation.ellie.carry_intro", false, var1);
                  break;
               case CARRY_SLOW:
                  this.createAnimation("animation.ellie.carry_slow" + this.aa, true, var1);
                  break;
               case CARRY_FAST:
                  this.createAnimation("animation.ellie.carry_fast", true, var1);
                  break;
               case CARRY_CUM:
                  this.createAnimation("animation.ellie.carry_cum", true, var1);
            }
      }

      return PlayState.CONTINUE;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener var2 = var1x -> {
         switch (var1x.sound) {
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
               int var4 = this.aa;

               do {
                  this.aa = this.getRNG().nextInt(4) + 1;
               } while (this.aa == var4);

               return;
            case "carry_fastDone":
               if (this.isControlledByLocalPlayer() && !HandlePlayerMovement.isJumping) {
                  this.setCurrentAction(Action.CARRY_SLOW);
               }
         }
      };
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
