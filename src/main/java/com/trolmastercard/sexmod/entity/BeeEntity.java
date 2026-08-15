package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.BeeDialogueScreen;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.entity.ai.GirlGotoGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendCompanionHomePacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.potion.HornyPotion;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWaterFlying;
import net.minecraft.entity.ai.EntityFlyHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateFlying;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
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
 * <b>Role.</b> The Bee NPC — a flying girl with a citizen sex scene
 * (start/slow/fast/cum) and a taming/chest mechanic: once tamed
 * ({@code HORNY_FLAG}, id 112) she carries a 27-slot chest inventory and opens
 * the bee dialogue/chest GUIs; wild bees get horny over time
 * ({@code hornyTimer}, 4800 ticks) and approach the nearest player to start
 * the scene by themselves.
 * <p>
 * <b>State.</b> {@code HORNY_FLAG} (112) doubles as tamed/chest flag AND
 * pregnancy-heart flag (see {@link #doParticleStuff()}); {@code eggState} is
 * a particle-cycle counter; {@code hornyTimer} counts up while unbounded
 * (0 = freshly interested). Flying is vanilla {@link EntityFlyHelper} +
 * {@link PathNavigateFlying}; fall damage is disabled.
 * <p>
 * <b>Scene flow.</b> {@link #handleBeeIdle()} (server, AI tick) drives the
 * wild approach: at 4800 ticks she locks the nearest non-owner player within
 * 1.5 blocks into the scene (anchor + {@link Action#CITIZEN_START}).
 * Progression and end run in the sound listener
 * ({@code sex_startDone}/{@code sex_fastDone} -&gt;
 * {@link Action#CITIZEN_SLOW}, jump keeps fast, {@code sex_cumDone} -&gt;
 * {@code resetCameraAndPhysics()}).
 * <p>
 * <b>Pitfalls.</b> {@code processInteract} has a dead condition
 * ({@code HORNY_FLAG && !HORNY_FLAG}`) — jar-faithful, the chest-give branch
 * never fires; do not "fix" it into an active chest-consume path without
 * testing the taming flow. NBT write/read uses the {@code isTamed} and
 * {@code hasChest} keys, both mapped onto {@code HORNY_FLAG}.
 */
public class BeeEntity extends BeeEntityBase {
   public float hornyTimer = 3200.0F;
   int eggState = 0;
   static final float timerO = 4800.0F;
   static final float timerQ = 10.0F;
   public static final DataParameter<Boolean> HORNY_FLAG = EntityDataManager.createKey(BeeEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(112);

   public BeeEntity(World world) {
      super(world);
      this.moveHelper = new EntityFlyHelper(this);
      this.setSize(0.3F, 1.5F);
   }

   @Override
   public String getDisplayNameText() {
      return "Bee";
   }

   @Override
   public float getScaleFactor() {
      return -0.1F;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(HORNY_FLAG, false);
   }

   protected PathNavigate createNavigator(World world) {
      PathNavigateFlying navigator = new PathNavigateFlying(this, world);
      navigator.setCanOpenDoors(false);
      navigator.setCanFloat(true);
      navigator.setCanEnterDoors(true);
      this.pathNavigator = navigator;
      return navigator;
   }

   @Override
   protected void applyEntityAttributes() {
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(16.0);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0);
      this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.2F);
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new GirlGotoGoal(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.25));
      this.tasks.addTask(1, new EntityAISwimming(this));
      this.tasks.addTask(2, this.watchClosestGirlGoal);
      this.tasks.addTask(3, new EntityAIWanderAvoidWaterFlying(this, 1.0));
   }

   /**
    * SERVER, every AI tick: removes a stale horny potion, ticks the wild
    * approach ({@link #handleBeeIdle()}), latches the cum-flag for the
    * particle cycle, spawns particles and applies the flight ceiling.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      if (this.isPotionActive(HornyPotion.HORNY_POTION) && this.hornyTimer < 4800.0F && this.getInteractionPlayerUUID() == null) {
         this.removePotionEffect(HornyPotion.HORNY_POTION);
         this.hornyTimer = 6.9420184E7F;
      }

      this.handleBeeIdle();
      if (this.getCurrentAction().equals(Action.CITIZEN_CUM)) {
         this.eggState = Math.max(1, this.eggState);
      }

      this.doParticleStuff();
      this.rayTraceFlower();
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.CITIZEN_CUM || action != Action.CITIZEN_FAST && action != Action.COWGIRLSLOW) {
         super.setCurrentAction(action);
      }
   }

   /**
    * SERVER: the wild-bee horny timer — counts up while unbounded and
    * masterless; at 4800 ticks she approaches the nearest free player and,
    * within 1.5 blocks, locks them into the citizen scene (anchor + yaw +
    * {@link Action#CITIZEN_START}).
    */
   void handleBeeIdle() {
      if (this.getInteractionPlayerUUID() == null) {
         if (!this.hasMaster()) {
            this.hornyTimer++;
            if (!(this.hornyTimer < 4800.0F)) {
               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 10.0);
               if (player != null) {
                  if (getActiveSceneInfo(player) == null) {
                     if (!AbstractPlayerGirlEntity.isOwnerPlayer(player)) {
                        if (player.getDistance(this) < 1.5F) {
                           this.hornyTimer = 0.0F;
                           this.setInteractionPlayerUUID(player.getPersistentID());
                           this.entityDataManager.set(IS_ANCHORED, true);
                           this.setTargetPosition(this.getFrontOffsetVector());
                           this.setYawRotation(player.rotationYaw - 180.0F);
                           this.pathNavigator.clearPath();
                           PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
                           this.setCurrentAction(Action.CITIZEN_START);
                           Vec3d vec = this.getVectorTowardPlayer(0.2);
                           player.setPositionAndUpdate(vec.x, vec.y, vec.z);
                        } else {
                           this.pathNavigator.clearPath();
                           this.pathNavigator.tryMoveToEntityLiving(player, 1.0);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: ray-traces straight down and cancels upward motion once more
    * than 3 blocks above ground — the bee's flight ceiling.
    */
   void rayTraceFlower() {
      RayTraceResult rayTrace = this.world.rayTraceBlocks(this.getPositionVector(), new Vec3d(this.posX, 0.0, this.posZ));
      if (rayTrace != null) {
         BlockPos pos = rayTrace.getBlockPos();
         double height = this.posY - pos.getY();
         if (height > 3.0 && this.motionY > 0.0) {
            this.motionY = 0.0;
         }
      }
   }

   /**
    * SERVER: the post-cum particle cycle ({@code eggState}) — heart/anger
    * particles around the bee, then a coin-flip (at tick 200) that sets
    * {@code HORNY_FLAG} and drives the "accepted/rejected" particle outcome.
    */
   void doParticleStuff() {
      if (this.eggState != 0) {
         this.eggState++;
         if ((Boolean)this.entityDataManager.get(HORNY_FLAG)) {
            if (this.eggState < 40) {
               for (EntityPlayer player : this.world.playerEntities) {
                  if (player.getDistance(this) < 15.0F) {
                     ((EntityPlayerMP)player)
                        .connection
                        .sendPacket(
                           new SPacketParticles(
                              EnumParticleTypes.HEART,
                              true,
                              (float)this.posX,
                              (float)this.posY + 0.3F,
                              (float)this.posZ,
                              0.2F,
                              0.3F,
                              0.2F,
                              0.25F,
                              1,
                              new int[0]
                           )
                        );
                  }
               }
            } else {
               this.eggState = 0;
            }
         } else if (this.eggState < 200) {
            for (EntityPlayer player : this.world.playerEntities) {
               if (player.getDistance(this) < 15.0F) {
                  ((EntityPlayerMP)player)
                     .connection
                     .sendPacket(
                        new SPacketParticles(
                           EnumParticleTypes.SPELL,
                           true,
                           (float)this.posX,
                           (float)this.posY + 0.3F,
                           (float)this.posZ,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           1,
                           new int[0]
                        )
                     );
               }
            }
         } else if (this.eggState == 200) {
            this.entityDataManager.set(HORNY_FLAG, this.getRNG().nextBoolean());
         } else if (this.eggState < 250) {
            for (EntityPlayer player : this.world.playerEntities) {
               if (player.getDistance(this) < 15.0F) {
                  ((EntityPlayerMP)player)
                     .connection
                     .sendPacket(
                        new SPacketParticles(
                           this.entityDataManager.get(HORNY_FLAG) ? EnumParticleTypes.HEART : EnumParticleTypes.VILLAGER_ANGRY,
                           true,
                           (float)this.posX,
                           (float)this.posY + 0.3F,
                           (float)this.posZ,
                           0.2F,
                           0.3F,
                           0.2F,
                           0.25F,
                           3,
                           new int[0]
                        )
                     );
               }
            }
         } else {
            this.eggState = 0;
         }

         for (EntityPlayer player : this.world.playerEntities) {
            if (player.getDistance(this) < 15.0F) {
               ((EntityPlayerMP)player)
                  .connection
                  .sendPacket(
                     new SPacketParticles(
                        EnumParticleTypes.SPELL,
                        true,
                        (float)this.posX,
                        (float)this.posY + 0.3F,
                        (float)this.posZ,
                        0.2F,
                        0.3F,
                        0.2F,
                        0.25F,
                        10,
                        new int[0]
                     )
                  );
            }
         }
      }
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.hornyTimer < 4800.0F && !this.onGround && this.motionY < 0.0) {
         this.motionY *= 0.4;
      }
   }

   public void fall(float distance, float multiplier) {
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      if ((Boolean)this.entityDataManager.get(HORNY_FLAG)
         && !(Boolean)this.entityDataManager.get(HORNY_FLAG)
         && player.getHeldItem(hand).getItem() == Item.getItemFromBlock(Blocks.CHEST)) {
         this.entityDataManager.set(HORNY_FLAG, true);
         player.getHeldItem(hand).shrink(1);
         return super.processInteract(player, hand);
      }

      if (this.world.isRemote && (Boolean)this.entityDataManager.get(HORNY_FLAG)) {
         this.openBeeDialogue(player);
      }

      return super.processInteract(player, hand);
   }

   @SideOnly(Side.CLIENT)
   void openBeeDialogue(EntityPlayer player) {
      Minecraft.getMinecraft().displayGuiScreen(new BeeDialogueScreen(this, player));
   }

   @Override
   public boolean openInteractionMenu(EntityPlayer player) {
      return false;
   }

   @Override
   public void doAction(String action, UUID uuid) {
   }

   @Override
   protected Action getNextAction(Action action) {
      return action == Action.CITIZEN_SLOW ? Action.CITIZEN_FAST : null;
   }

   @Override
   protected Action getCumAction(Action action) {
      return action != Action.CITIZEN_FAST && action != Action.CITIZEN_SLOW ? null : Action.CITIZEN_CUM;
   }

   @Override
   protected void U() {
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      nbt.setBoolean("isTamed", (Boolean)this.entityDataManager.get(HORNY_FLAG));
      nbt.setBoolean("hasChest", (Boolean)this.entityDataManager.get(HORNY_FLAG));
      nbt.setTag("inventory", this.inventory.serializeNBT());
   }

   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      if (nbt.hasKey("isTamed")) {
         this.entityDataManager.set(HORNY_FLAG, nbt.getBoolean("isTamed"));
      }

      this.entityDataManager.set(HORNY_FLAG, nbt.getBoolean("hasChest"));
      this.inventory.deserializeNBT(nbt.getCompoundTag("inventory"));
   }

   @SideOnly(Side.CLIENT)
   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.world instanceof SexWorldClient) {
         return PlayState.STOP;
      }

      switch (event.getController().getName()) {
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.bee.null", true, event);
            } else {
               this.createAnimation("animation.bee." + (this.entityDataManager.get(HORNY_FLAG) ? "idle_has_chest" : "idle"), true, event);
            }
            break;
         case "action":
            switch (this.getCurrentAction()) {
               case CITIZEN_START:
                  this.createAnimation("animation.bee.sex_start", false, event);
                  break;
               case CITIZEN_SLOW:
                  this.createAnimation("animation.bee.sex_slow", true, event);
                  break;
               case CITIZEN_FAST:
                  this.createAnimation("animation.bee.sex_fast", true, event);
                  break;
               case CITIZEN_CUM:
                  this.createAnimation("animation.bee.sex_cum", false, event);
                  break;
               case THROW_PEARL:
                  this.createAnimation("animation.bee.throw_pearl", true, event);
            }
      }

      return PlayState.CONTINUE;
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * citizen scene; {@code sex_startDone}/{@code sex_fastDone} -&gt;
    * {@link Action#CITIZEN_SLOW} (jump keeps fast), {@code sex_cumDone} -&gt;
    * {@code resetCameraAndPhysics()}.
    */
   @Override
   public void registerControllers(AnimationData data) {
      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      AnimationController.ISoundListener soundListener = sound -> {
         switch (sound.sound) {
            case "pearl":
               if (this.isLocalPlayerNearby() && this.getCurrentAction() == Action.THROW_PEARL) {
                  PacketHandler.networkWrapper.sendToServer(new SendCompanionHomePacket(this.getGirlId()));
               }
               break;
            case "resetCumPercentage":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
               }
               break;
            case "sex_fastMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "sex_startMSG1":
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "sex_fastDone":
               if (!this.isControlledByLocalPlayer() || HandlePlayerMovement.isJumping) {
                  return;
               }
            case "sex_startDone":
               this.setCurrentAction(Action.CITIZEN_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "sex_cumMSG1":
               this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_CUMINFLATION), 2.0F);
               this.playSound(SoundHandler.randomSound(SoundHandler.MISC_POUNDING));
               break;
            case "blackscreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
               break;
            case "sex_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.resetHornyMeter();
                  this.resetCameraAndPhysics();
               }
               break;
            case "sex_fastReady":
               if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                  this.resetAnimationControllerOffset();
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }

}
