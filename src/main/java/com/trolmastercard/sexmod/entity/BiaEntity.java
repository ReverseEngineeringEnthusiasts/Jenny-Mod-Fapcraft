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

public class BiaEntity extends AbstractGirlNpcEntity implements IEllie, IBeddableSexGirl {
   static final int ae = 3;
   public boolean yFlag = false;
   int ag = 0;
   boolean af = false;
   int zFlag = 0;
   boolean ab = true;
   int ac = -1;
   boolean aa = false;
   final int[] ai = new int[]{0, 180, -90, 90};
   final Vec3d[][] ad = new Vec3d[][]{
      {new Vec3d(0.5, 0.0, -0.5), new Vec3d(0.0, 0.0, -1.0)},
      {new Vec3d(0.5, 0.0, 1.5), new Vec3d(0.0, 0.0, 1.0)},
      {new Vec3d(-0.5, 0.0, 0.5), new Vec3d(-1.0, 0.0, 0.0)},
      {new Vec3d(1.5, 0.0, 0.5), new Vec3d(1.0, 0.0, 0.0)}
   };
   int ah = 1;

   public BiaEntity(World var1) {
      super(var1);
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
   public float i_clash226() {
      return -0.2F;
   }

   @Override
   public void c_clash237() {
      this.sendChatMessage("I am living here now nya~");
      this.playRandomSound(SoundHandler.GIRLS_BIA_BREATH);
   }

   @Override
   public void b_clash158() {
      this.yFlag = true;
   }

   @Override
   public void setCurrentAction(Action action) {
      Action var2 = this.getCurrentAction();
      if (var2 == Action.ANAL_CUM || var2 == Action.PRONE_DOGGY_CUM) {
         this.entityDataManager.set(GIRL_HAND_STATES, "");
      }

      if (var2 != Action.ANAL_CUM || action != Action.ANAL_FAST && action != Action.ANAL_SLOW) {
         if (var2 != Action.PRONE_DOGGY_CUM || action != Action.PRONE_DOGGY_HARD && action != Action.PRONE_DOGGY_SOFT) {
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
            } catch (NullPointerException var2) {
               this.setTargetPosition(this.getFrontOffsetVector());
            }

            this.setNoGravity(false);
            Vec3d var1 = RotationHelper.a(this.getPositionVector(), this.getTargetPosition(), 40 - this.ag);
            this.setPosition(var1.x, var1.y, var1.z);
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

   public boolean processInteract(EntityPlayer var1, EnumHand var2) {
      if (super.processInteract(var1, var2)) {
         return true;
      }

      if (this.getCurrentAction() == Action.SITDOWNIDLE) {
         return true;
      }

      ItemStack var3 = var1.getHeldItem(var2);
      boolean var4 = var3.getItem() == Items.NAME_TAG;
      if (var4) {
         var3.interactWithEntity(var1, this, var2);
         return true;
      }

      if (this.world.isRemote && !this.openInteractionMenu(var1)) {
         this.sendChatMessage(I18n.format("bia.dialogue.busy", new Object[0]));
      }

      return true;
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer var1) {
      if (this.getInteractionPlayerUUID() == null
         && (!this.hasMaster() || ((String)this.entityDataManager.get(MASTER)).equals(Minecraft.getMinecraft().player.getPersistentID().toString()))) {
         String[] var2 = new String[]{
            this.entityDataManager.get(OUTFIT_INDEX) == 1 ? "action.names.strip" : "action.names.dressup", "action.names.talk", "action.names.headpat"
         };
         openInventoryGui(var1, this, var2, true);
         return true;
      } else {
         return false;
      }
   }

   void b_clash286(EntityPlayer var1) {
      openInventoryGui(var1, this, new String[]{"action.names.anal", "doggy"}, false);
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
      if (this.world.isRemote && this.isControlledByLocalPlayer() && this.getCurrentAction() == Action.PRONE_DOGGY_INTRO && !BeeScreen.a_clash731()) {
         HornyMeterHud.showHornyMeter();
      }

      this.d_clash287();
   }

   @Override
   protected void resetLocalPlayerClientState() {
      super.resetLocalPlayerClientState();
      this.ac = -1;
   }

   void d_clash287() {
      Action var1 = this.getCurrentAction();
      if (var1 == Action.ANAL_WAIT || var1 == Action.SITDOWNIDLE) {
         EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 10.0);
         if (var2 != null) {
            if (!(var2.getDistance(this) > 1.0F)) {
               if (this.ac == -1) {
                  if (this.world.isRemote) {
                     BeeScreen.enableInteraction();
                     HandlePlayerMovement.setMovementLock(false);
                  } else {
                     this.setInteractionPlayerUUID(var2.getPersistentID());
                  }

                  this.ac = -1;
               } else if (--this.ac <= 0) {
                  this.ac = -1;
                  var2.noClip = true;
                  var2.setNoGravity(true);
                  if (var1 == Action.ANAL_WAIT) {
                     if (!this.world.isRemote) {
                        this.setCurrentAction(Action.ANAL_START);
                        Vec3d var7 = this.getTargetPosition().add(VectorMath.a(-0.3, -1.0, -0.5, this.getYawRotation()));
                        var2.setPositionAndUpdate(var7.x, var7.y, var7.z);
                     } else if (this.isControlledByLocalPlayer()) {
                        HornyMeterHud.showHornyMeter();
                     }
                  } else {
                     float var3 = this.getYawRotation();
                     var2.rotationYaw = var3;
                     var2.rotationPitch = 60.0F;
                     if (!this.world.isRemote) {
                        this.setOutfitIndex(0);
                        this.setCurrentAction(Action.PRONE_DOGGY_INTRO);
                        Vec3d var4 = this.getTargetPosition();
                        Vec3d var5 = var4.add(VectorMath.a(0.0, 0.0, 1.0, var3));
                        this.setTargetPosition(var5);
                        Vec3d var6 = var4.add(VectorMath.a(0.0, 1.1875 - var2.getEyeHeight(), 0.5, var3));
                        var2.setPositionAndUpdate(var6.x, var6.y, var6.z);
                        this.setAnchored(true);
                     }
                  }
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
         int var1 = this.ah;

         do {
            this.ah = this.getRNG().nextInt(3) + 1;
         } while (var1 == this.ah);
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
   public void doAction(String var1, UUID var2) {
      super.doAction(var1, var2);
      switch (var1) {
         case "action.names.talk":
            this.setInteractionPlayerUUID(Minecraft.getMinecraft().player.getPersistentID());
            this.changeDataParameterFromClient("playerSheHasSexWith", Minecraft.getMinecraft().player.getPersistentID().toString());
            this.changeDataParameterFromClient("animationFollowUp", "talkHorny");
            this.a_clash288(var2);
            break;
         case "action.names.headpat":
            this.setInteractionPlayerUUID(Minecraft.getMinecraft().player.getPersistentID());
            this.changeDataParameterFromClient("playerSheHasSexWith", Minecraft.getMinecraft().player.getPersistentID().toString());
            this.changeDataParameterFromClient("animationFollowUp", "Headpat");
            this.a_clash288(var2);
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

   public void onDeath(DamageSource var1) {
      super.onDeath(var1);
      if (!this.world.isRemote) {
         EntityItem var2 = new EntityItem(
            this.world,
            this.posX,
            this.posY,
            this.posZ,
            new ItemStack(Blocks.WOOL, this.getRNG().nextInt(4), 12)
         );
         this.world.spawnEntity(var2);
      }
   }

   void a_clash288(UUID var1) {
      this.triggerActionSync(true, true, var1);
      HandlePlayerMovement.setMovementLock(false);
   }

   Vector4d a_clash289() {
      BlockPos var1 = null;
      int var2 = 0;

      while (!this.a_clash290(var1)) {
         var1 = this.findNearestBed(this.getPosition(), var2);
         if (++var2 == 50) {
            break;
         }
      }

      if (var1 != null && var2 != 50) {
         this.tasks.removeTask(this.wanderGoal);
         this.tasks.removeTask(this.watchClosestGirlGoal);
         Vec3d var3 = new Vec3d(var1.getX(), var1.getY(), var1.getZ());
         int var4 = -1;

         for (int var5 = 0; var5 < this.ad.length; var5++) {
            Vec3d var6 = var3.add(this.ad[var5][1]);
            Vec3d var7 = var3.subtract(this.ad[var5][1]);
            Block var8 = this.world.getBlockState(new BlockPos(var6.x, var6.y, var6.z)).getBlock();
            if (var8 == Blocks.AIR && WorldUtils.b(this.world, new BlockPos(var7))) {
               if (var4 == -1) {
                  var4 = var5;
               } else {
                  double var9 = this.getPosition()
                     .distanceSq(
                        var3.add(this.ad[var4][0]).x,
                        var3.add(this.ad[var4][0]).y,
                        var3.add(this.ad[var4][0]).z
                     );
                  double var11 = this.getPosition()
                     .distanceSq(
                        var3.add(this.ad[var5][0]).x,
                        var3.add(this.ad[var5][0]).y,
                        var3.add(this.ad[var5][0]).z
                     );
                  if (var11 < var9) {
                     var4 = var5;
                  }
               }
            }
         }

         if (var4 == -1) {
            this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
            this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
            return null;
         } else {
            Vec3d var13 = var3.add(this.ad[var4][0]);
            return new Vector4d(var13.x, var13.y, var13.z, this.ai[var4]);
         }
      } else {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }
   }

   boolean a_clash290(BlockPos var1) {
      if (var1 == null) {
         return false;
      } else if (WorldUtils.b(this.world, var1.north()) && this.world.isAirBlock(var1.south())) {
         return true;
      } else if (WorldUtils.b(this.world, var1.east()) && this.world.isAirBlock(var1.west())) {
         return true;
      } else {
         return WorldUtils.b(this.world, var1.south()) && this.world.isAirBlock(var1.north())
            ? true
            : WorldUtils.b(this.world, var1.west()) && this.world.isAirBlock(var1.east());
      }
   }

   Vector4d b_clash291() {
      BlockPos var1 = this.getNearestBed(this.getPosition());
      if (var1 == null) {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.nobedinsight", new Object[0]));
         return null;
      }

      this.tasks.removeTask(this.wanderGoal);
      this.tasks.removeTask(this.watchClosestGirlGoal);
      Vec3d var2 = new Vec3d(var1.getX(), var1.getY(), var1.getZ());
      int var3 = -1;

      for (int var4 = 0; var4 < this.ad.length; var4++) {
         Vec3d var5 = var2.add(this.ad[var4][1]);
         if (this.world.getBlockState(new BlockPos(var5.x, var5.y, var5.z)).getBlock()
            == Blocks.AIR) {
            if (var3 == -1) {
               var3 = var4;
            } else {
               double var6 = this.getPosition()
                  .distanceSq(
                     var2.add(this.ad[var3][0]).x,
                     var2.add(this.ad[var3][0]).y,
                     var2.add(this.ad[var3][0]).z
                  );
               double var8 = this.getPosition()
                  .distanceSq(
                     var2.add(this.ad[var4][0]).x,
                     var2.add(this.ad[var4][0]).y,
                     var2.add(this.ad[var4][0]).z
                  );
               if (var8 < var6) {
                  var3 = var4;
               }
            }
         }
      }

      if (var3 == -1) {
         this.playSound(SoundHandler.GIRLS_BIA_BREATH[2]);
         this.sendChatMessage(I18n.format("jenny.dialogue.bedobscured", new Object[0]));
         return null;
      } else {
         Vec3d var10 = var2.add(this.ad[var3][0]);
         return new Vector4d(var10.x, var10.y, var10.z, this.ai[var3]);
      }
   }

   @Override
   public void goToSexBed() {
      String var1 = (String)this.entityDataManager.get(GIRL_HAND_STATES);
      Vector4d var2 = var1.equals("anal") ? this.b_clash291() : this.a_clash289();
      if (var2 != null) {
         Vec3d var3 = new Vec3d(var2.getX(), var2.getY(), var2.getZ());
         this.setYawRotation((float)var2.getW());
         this.setTargetPosition(var3);
         this.cameraYaw = this.getYawRotation();
         this.getNavigator().clearPath();
         this.getNavigator().tryMoveToXYZ(var3.x, var3.y, var3.z, 0.35);
         this.af = true;
         this.zFlag = 0;
      }
   }

   @Override
   protected Action getNextAction(Action var1) {
      if (var1 == Action.ANAL_SLOW) {
         return Action.ANAL_FAST;
      } else {
         return var1 == Action.PRONE_DOGGY_INTRO ? Action.PRONE_DOGGY_INSERT : null;
      }
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (var1 == Action.ANAL_SLOW || var1 == Action.ANAL_FAST) {
         return Action.ANAL_CUM;
      } else {
         return var1 != Action.PRONE_DOGGY_SOFT && var1 != Action.PRONE_DOGGY_HARD ? null : Action.PRONE_DOGGY_CUM;
      }
   }

   @Override
   protected void U() {
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
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      if (this.world instanceof SexWorldClient) {
         return null;
      }

      switch (var1.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() == Action.NULL && this.getCurrentAction().autoBlink) {
               this.createAnimation("animation.bia.fhappy", true, var1);
            } else {
               this.createAnimation("animation.bia.null", true, var1);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bia.null", true, var1);
            } else if (this.isRiding()) {
               this.createAnimation("animation.bia.sit", true, var1);
            } else if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               switch (this.getWalkType()) {
                  case RUN:
                     this.createAnimation("animation.bia.run", true, var1);
                     break;
                  case FAST_WALK:
                     this.createAnimation("animation.bia.fastwalk", true, var1);
                     break;
                  case WALK:
                     this.createAnimation("animation.bia.walk", true, var1);
               }

               this.rotationYaw = this.rotationYawHead;
            } else {
               this.createAnimation("animation.bia.idle", true, var1);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case NULL:
                  this.createAnimation("animation.bia.null", true, var1);
                  break;
               case STRIP:
                  this.createAnimation("animation.bia.strip", false, var1);
                  break;
               case ATTACK:
                  this.createAnimation("animation.bia.attack" + this.nextAttack, false, var1);
                  break;
               case BOW:
                  this.createAnimation("animation.bia.bowcharge", false, var1);
                  break;
               case RIDE:
                  this.createAnimation("animation.bia.ride", true, var1);
                  break;
               case SIT:
                  this.createAnimation("animation.bia.sit", true, var1);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bia.throwpearl", false, var1);
                  break;
               case DOWNED:
                  this.createAnimation("animation.bia.downed", true, var1);
                  break;
               case TALK_HORNY:
                  this.createAnimation("animation.bia.talk_horny2", true, var1);
                  break;
               case TALK_IDLE:
                  this.createAnimation("animation.bia.talk_idle2", true, var1);
                  break;
               case TALK_RESPONSE:
                  this.createAnimation("animation.bia.talk_response", true, var1);
                  break;
               case ANAL_PREPARE:
                  this.createAnimation("animation.bia.anal_prepare", false, var1);
                  break;
               case ANAL_WAIT:
                  this.createAnimation("animation.bia.anal_wait", false, var1);
                  break;
               case ANAL_START:
                  this.createAnimation("animation.bia.anal_start", true, var1);
                  break;
               case ANAL_SLOW:
                  this.createAnimation("animation.bia.anal_slow", true, var1);
                  break;
               case ANAL_FAST:
                  this.createAnimation("animation.bia.anal_fast", true, var1);
                  break;
               case ANAL_CUM:
                  this.createAnimation("animation.bia.anal_cum", false, var1);
                  break;
               case HEAD_PAT:
                  this.createAnimation("animation.bia.headpat", false, var1);
                  break;
               case SITDOWN:
                  this.createAnimation("animation.bia.sitdown", false, var1);
                  break;
               case SITDOWNIDLE:
                  this.createAnimation("animation.bia.sitdownidle", true, var1);
                  break;
               case PRONE_DOGGY_INTRO:
                  this.createAnimation("animation.bia.prone_doggy_intro", true, var1);
                  break;
               case PRONE_DOGGY_INSERT:
                  this.createAnimation("animation.bia.prone_doggy_insert", true, var1);
                  break;
               case PRONE_DOGGY_SOFT:
                  this.createAnimation("animation.bia.prone_doggy_soft", true, var1);
                  break;
               case PRONE_DOGGY_HARD:
                  this.createAnimation("animation.bia.prone_doggy_hard" + this.ah, true, var1);
                  break;
               case PRONE_DOGGY_CUM:
                  this.createAnimation("animation.bia.prone_doggy_cum", true, var1);
                  break;
               case WAVE_IDLE:
                  this.createAnimation("animation.bia.wave_idle", true, var1);
                  break;
               case WAVE:
                  this.createAnimation("animation.bia.wave", true, var1);
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
               this.setCurrentAction(Action.TALK_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  this.b_clash286(Minecraft.getMinecraft().player);
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
      this.actionController.registerSoundListener(var2);
      var1.addAnimationController(this.actionController);
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
   }

}
