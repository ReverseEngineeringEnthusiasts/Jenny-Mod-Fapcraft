package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.SkinColor;
import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IGoblin;
import com.trolmastercard.sexmod.util.Point2D;
import com.trolmastercard.sexmod.util.EyeColor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.vecmath.Vector2f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> The Goblin NPC (implements {@link IGoblin}) — the tamed
 * shoulder-rider/throwable goblin and the queen system. A goblin can be picked
 * up (PICK_UP -&gt; SHOULDER_IDLE on the owner's shoulder), thrown
 * (START_THROWING -&gt; THROWN -&gt; STAND_UP), or be a queen (SIT on a throne,
 * guards, breeding scenes, stolen gold). Scenes: paizuri, nelson, breeding.
 * <p>
 * <b>State.</b> Own data keys (do not reorder): {@code OWNER_UUID} (122) =
 * carrying player, {@code QUEEN_ID} (123) = queen goblin's girl id, {@code STOLEN_ITEM}
 * (124) = stolen item stack, {@code TAMED} (125) = tamed flag, {@code PREGNANT} (126) =
 * queen pregnant flag. Appearance comes from the {@link AbstractNpcOnlyEntity}
 * DNA (119/120/121) with the trailing segment (index 9) holding the model
 * part index. {@code isQueen} = queen flag, {@code thronePos}/{@code throneYaw}
 * = throne pos/rotation, {@code guards} = guard list, {@code throwProgress} =
 * throw progress (-1 idle), {@code heldPlayerDistance} = held-player countdown
 * (45).
 * <p>
 * <b>Flow.</b> Pickup: {@link #processInteract(EntityPlayer, EnumHand)} sets
 * the owner + PICK_UP; {@link #handlePickUpState(BaseGirlEntity)} glues her to
 * the owner and, after 45 ticks, reaches SHOULDER_IDLE. Throw:
 * {@link #updateThrowProgress()} launches her at tick 15 (velocity from the
 * owner's aim via {@link #getGoblinThrowPos(BaseGirlEntity)} +
 * {@link #getGoblinThrowHeight(BaseGirlEntity)}) and THROWN at tick 39.
 * Queen: {@link #tickGoldSteal()} sends a RUN goblin to steal gold from a
 * nearby player, {@link #tryStartBreeding()} starts the breeding intro when a
 * player stands in the throne zone, {@link #handleJumpThrow()} throws the
 * guards onto the player and {@link #handleThrowCooldown()} releases two
 * AWAIT_PICK_UP guards after breeding.
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} guards the cum loops,
 * fires {@link #handlePlayerInteract()}/{@link #handlePlayerLook()}/
 * {@link #returnStolenItem()} on the server for PAIZURI_START/NELSON_INTRO/
 * START_THROWING, marks {@code PREGNANT} on BREEDING_CUM_0/NELSON_CUM and calls
 * {@link #endPaizuriScene()} (reset + despawn) when PAIZURI_CUM leaves for
 * NULL.
 * {@code getModelPartIndex()} reads DNA index 7. Dead goblins drop their
 * stolen item on the server.
 * <p>
 * <b>Decomposition.</b> The behaviour lives in package-private helpers beside
 * this class: {@link GoblinAnimations} (pose predicate + controller sounds),
 * {@link GoblinThrow} (carry/throw kinematics and the {@link BaseGirlEntity}
 * overloads), {@link GoblinQueenAi} (gold steal, breeding, guards,
 * cooldowns), {@link GoblinInteractions} (GUI actions, pickup, scene
 * entry/teardown), {@link GoblinAppearance} (DNA packing),
 * {@link GoblinPersistence} (NBT) and {@link GoblinEventHandler} (Forge
 * events). What stays here are the overrides plus thin delegators that keep
 * the public surface unchanged.
 */
public class GoblinEntity extends AbstractNpcOnlyEntity implements IGoblin {
   // Throne offsets, relative to thronePos and keyed by the throne yaw (see the
   // switch blocks in GoblinQueenAi): THRONE_* is the queen's spot beside the
   // throne, THROW_* is where the jump-throw lands her.
   public static final SkinColor DEFAULT_SKIN = SkinColor.DARK_GREEN;
   public static final Vec3i THRONE_SIZE = new Vec3i(11, 6, 11);
   public static final Vec3d THRONE_OFFSET_YAW_180 = new Vec3d(5.0, 1.0, 9.0);
   public static final Vec3d THROW_OFFSET_YAW_0 = new Vec3d(3.0, -1.0, 6.0);
   public static final Vec3d THRONE_OFFSET_YAW_NEG_90 = new Vec3d(1.0, 1.0, 5.0);
   public static final Vec3d THROW_OFFSET_YAW_90 = new Vec3d(-6.0, -1.0, 3.0);
   public static final Vec3d THRONE_OFFSET_YAW_0 = new Vec3d(5.0, 1.0, 1.0);
   public static final Vec3d THROW_OFFSET_YAW_180 = new Vec3d(-3.0, -1.0, -6.0);
   public static final Vec3d THRONE_OFFSET_YAW_90 = new Vec3d(9.0, 1.0, 5.0);
   public static final Vec3d BREEDING_PLAYER_OFFSET = new Vec3d(0.0, -1.0, -4.0);
   public static final Vec3d GUARD_LEFT_OFFSET = new Vec3d(1.0, -1.0, -3.0);
   public static final Vec3d GUARD_RIGHT_OFFSET = new Vec3d(-1.0, -1.0, -3.0);
   public static final Vec3d THROW_OFFSET_YAW_NEG_90 = new Vec3d(6.0, -1.0, -3.0);
   public static final int aj = 39;
   public static final int ae = 15;
   public static final int aE = 8400;
   static final int aH = 45;
   static final int ad = 32000;
   static final int aw = 26;
   static final int THROW_TICKS_205 = 205;
   static final int aL = 100;
   static final int aA = 1200;
   static final int ak = 30;
   static final int aW = 37;
   static final float aU = 2.0F;
   static final int aI = 5;
   static final int THROW_TICKS_100 = 100;
   static final int aq = 20;
   static final float aG = 0.825F;
   static final Vector2f ENTITY_SIZE = new Vector2f(0.5F, 0.99F);
   static final HashSet<Item> GOLD_ITEMS = new HashSet<>(
      Arrays.asList(
         Items.GOLDEN_HOE,
         Items.GOLDEN_HORSE_ARMOR,
         Items.GOLD_INGOT,
         Items.GOLDEN_APPLE,
         Items.GOLDEN_AXE,
         Items.GOLDEN_SHOVEL,
         Items.GOLDEN_PICKAXE,
         Items.GOLDEN_SWORD,
         Items.GOLDEN_CARROT,
         Items.GOLDEN_HELMET,
         Items.GOLDEN_BOOTS,
         Items.GOLDEN_CHESTPLATE,
         Items.GOLDEN_LEGGINGS,
         Items.GOLD_INGOT,
         Items.GOLD_NUGGET,
         Item.getItemFromBlock(Blocks.GOLD_BLOCK),
         Item.getItemFromBlock(Blocks.GOLD_ORE)
      )
   );
   public static final DataParameter<String> OWNER_UUID = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(122);
   public static final DataParameter<String> QUEEN_ID = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(123);
   public static final DataParameter<ItemStack> STOLEN_ITEM = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.ITEM_STACK)
      .getSerializer()
      .createKey(124);
   public static final DataParameter<Boolean> TAMED = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(125);
   public static final DataParameter<Boolean> PREGNANT = EntityDataManager.createKey(GoblinEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(126);
   public boolean isQueen = false;
   public float throneYaw = 0.0F;
   public long impregnationTick = -1L;
   public Vec3d thronePos = Vec3d.ZERO;
   List<UUID> guardUUIDs = new ArrayList<>();
   int robTicks = 31520;
   int heldPlayerDistance = -1;
   public int throwProgress = -1;
   boolean gravityInitDone = false;
   BlockPos guardPost = null;
   int guardPostTicks = 0;
   int standUpTicks = 0;
   int throwTickCount = 0;
   int holdCooldown = -1;
   int jumpThrowTicks = 0;
   long pregnantNoticeTick = 0L;
   List<GoblinEntity> guards = new ArrayList<>();
   int paizuriStartTicks = -1;
   int nelsonStartTicks = -1;
   Action previousAction = null;
   public float fadeScale = 1.0F;
   int throwCooldown = -1;
   boolean breedingSideLeft = true;
   boolean nelsonSlowBasePose = true;
   boolean nelsonAltPose = false;
   String paizuriPoseSuffix = "";
   boolean breedingFastReadyPose = false;

   public GoblinEntity(World world) {
      super(world);
      this.setSize(ENTITY_SIZE.x, ENTITY_SIZE.y);
   }

   public GoblinEntity(World world, @Nonnull String girlId, int modelPartIndex) {
      this(world);
      this.entityDataManager.set(QUEEN_ID, girlId);
      this.entityDataManager.set(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder(), modelPartIndex));
   }

   public GoblinEntity(World world, boolean isThrown, float yaw, Vec3d pos) {
      this(world);
      if (isThrown) {
         this.entityDataManager.set(APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder()));
         this.throneYaw = yaw;
         this.thronePos = pos;
         this.isQueen = true;
         this.setTargetPosition(pos);
         this.setYawRotation(yaw);
         this.setCurrentAction(Action.SIT);
         this.setAnchored(true);
         this.setPosition(pos.x, pos.y, pos.z);
      }
   }

   @Override
   public void reinitTasks() {
      super.reinitTasks();
      this.setOwnerUUID(null);
      this.noClip = false;
      this.setNoGravity(false);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      EyeColor color = EyeColor.values()[this.getRNG().nextInt(EyeColor.values().length)];
      this.entityDataManager.register(ACTION_TARGET_POS, new BlockPos(color.getColor()));
      this.entityDataManager.register(CURRENT_ACTION, DEFAULT_SKIN.name());
      this.entityDataManager.register(OWNER_UUID, "");
      this.entityDataManager.register(QUEEN_ID, "");
      this.entityDataManager.register(STOLEN_ITEM, ItemStack.EMPTY);
      this.entityDataManager.register(TAMED, false);
      this.entityDataManager.register(PREGNANT, false);
   }

   @Override
   protected void clearBoneColors() {
      GoblinRenderer.clearBoneColors();
   }

   public void setDead() {
      super.setDead();
      this.setOwnerUUID(null);
      if (!this.world.isRemote) {
         ItemStack heldItem = (ItemStack)this.entityDataManager.get(STOLEN_ITEM);
         if (heldItem != ItemStack.EMPTY) {
            EntityItem item = new EntityItem(this.world, this.posX, this.posY, this.posZ, heldItem);
            this.world.spawnEntity(item);
         }
      }
   }

   @Override
   public void doAction(String action, UUID uuid) {
      GoblinInteractions.doAction(this, action, uuid);
   }

   public void setThrowTarget(UUID uuid) {
      GoblinInteractions.setThrowTarget(this, uuid);
   }

   public void setPickupTarget(UUID uuid) {
      GoblinInteractions.setPickupTarget(this, uuid);
   }

   @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
   public static boolean openGoblinPadForPlayer(net.minecraft.client.Minecraft mc, UUID playerUuid) {
      return GoblinInteractions.openGoblinPadForPlayer(mc, playerUuid);
   }

   @Override
   public String getDisplayNameText() {
      return "Goblin";
   }

   public float getEyeHeight() {
      return 0.75F;
   }

   @Override
   public float getScaleFactor() {
      return 0.1F;
   }

   @Override
   public void setOwnerUUID(UUID uuid) {
      if (uuid == null) {
         this.entityDataManager.set(OWNER_UUID, "");
      } else {
         this.entityDataManager.set(OWNER_UUID, uuid.toString());
      }
   }

   @Nullable
   @Override
   public UUID getOwnerUUID() {
      String ownerStr = (String)this.entityDataManager.get(OWNER_UUID);
      if ("".equals(ownerStr)) {
         return null;
      }

      try {
         return UUID.fromString((String)this.entityDataManager.get(OWNER_UUID));
      } catch (Exception ex) {
         Main.LOGGER.error("could not parse the owner UUID", ex);
         return null;
      }
   }

   @Override
   public int getHeldPlayerDistance() {
      return this.heldPlayerDistance;
   }

   @Override
   public void setHeldPlayerDistance(int distance) {
      this.heldPlayerDistance = distance;
   }

   protected String buildModelCodeDNA(StringBuilder builder) {
      return GoblinAppearance.buildModelCodeDNA(builder);
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return GoblinAppearance.getCustomPartIdList();
   }

   @Override
   public List<Integer> getCustomPartExtraIdList() {
      return GoblinAppearance.getCustomPartExtraIdList();
   }

   @Override
   public Point2D getModelPartByIndex(int index) {
      return GoblinAppearance.getModelPartByIndex(index);
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      GoblinAppearance.setCustomPartList(this, parts);
   }

   void updateModelCodeDNA() {
      GoblinAppearance.updateModelCodeDNA(this);
   }

   protected String buildModelCodeDNA(StringBuilder builder, int partIndex) {
      return GoblinAppearance.buildModelCodeDNA(builder, partIndex);
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      GoblinPersistence.write(this, nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      GoblinPersistence.read(this, nbt);
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      return GoblinInteractions.processInteract(this, player, hand);
   }

   public static boolean hasGoblinWithUUID(UUID uuid) {
      return GoblinInteractions.hasGoblinWithUUID(uuid);
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 2.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
   }

   /**
    * BOTH sides, every AI tick: dispatches every goblin subsystem —
    * gravity/despawn guard, pickup glue, throw progression, queen AI
    * (steal/breeding), held-state guarding, cooldowns and particle cycles.
    * Ordering is significant; see the individual handlers.
    */
   @Override
   public void updateAITasks() {
      super.updateAITasks();
      this.handleGravity();
      handlePickUpState(this);
      this.handleThrowState();
      this.tickGoldSteal();
      this.fleeFromNearestPlayer();
      this.tryStartBreeding();
      this.handleJumpThrow();
      this.handleHoldCooldown();
      this.handleThrowCooldown();
      this.handleHeldParticles();
      this.handleHeldThrow();
      this.handleThrownLand();
      this.handleStandUp();
      this.handleHeldState();
   }

   public boolean canBeCollidedWith() {
      Action action = this.getCurrentAction();
      if (action == Action.THROWN) {
         return false;
      } else if (action == Action.RUN) {
         return super.canBeCollidedWith();
      } else if (action == Action.AWAIT_PICK_UP) {
         return super.canBeCollidedWith();
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return action != Action.NULL ? false : super.canBeCollidedWith();
      }
   }

   void handleGoblinOwner(EntityPlayer player) {
      GoblinThrow.handleGoblinOwner(this, player);
   }

   void handleHeldState() {
      GoblinThrow.handleHeldState(this);
   }

   double getThrowRange() {
      return Math.sqrt(800.0);
   }

   void handleStandUp() {
      if (this.getCurrentAction() == Action.STAND_UP) {
         if (++this.standUpTicks >= 37) {
            this.standUpTicks = 0;
            this.setCurrentAction(Action.NULL);
         }
      }
   }

   @Override
   public void setThrowTickCount(int tickCount) {
      this.throwTickCount = tickCount;
   }

   @Override
   public int getThrowTickCount() {
      return this.throwTickCount;
   }

   void handleThrownLand() {
      if (this.getCurrentAction() == Action.THROWN) {
         if (this.onGround) {
            int throwTick = this.getThrowTickCount() + 1;
            this.setThrowTickCount(throwTick);
            if (throwTick >= 30) {
               this.setThrowTickCount(0);
               this.setCurrentAction(Action.STAND_UP);
            }
         }
      }
   }

   void handleHeldThrow() {
      GoblinQueenAi.handleHeldThrow(this);
   }

   void handleHeldParticles() {
      GoblinQueenAi.handleHeldParticles(this);
   }

   void handleThrowCooldown() {
      GoblinQueenAi.handleThrowCooldown(this);
   }

   void handleHoldCooldown() {
      GoblinQueenAi.handleHoldCooldown(this);
   }

   public static Vec3d rotateVectorYaw(Vec3d vec, float yaw) {
      return GoblinThrow.rotateVectorYaw(vec, yaw);
   }

   public static Vec3d rotateVectorPitchYaw(Vec3d vec, float pitch, float yaw) {
      return GoblinThrow.rotateVectorPitchYaw(vec, pitch, yaw);
   }

   void handleJumpThrow() {
      GoblinQueenAi.handleJumpThrow(this);
   }

   void tryStartBreeding() {
      GoblinQueenAi.tryStartBreeding(this);
   }

   List<GoblinEntity> ensureGuardsSpawned() {
      return GoblinQueenAi.ensureGuardsSpawned(this);
   }

   void handleGravity() {
      if (!this.gravityInitDone) {
         this.noClip = false;
         this.setNoGravity(false);
         if (!this.isQueen && !(Boolean)this.entityDataManager.get(TAMED) && !((String)this.entityDataManager.get(QUEEN_ID)).equals("") && this.getCurrentAction() == Action.NULL) {
            this.world.removeEntity(this);
         }

         this.gravityInitDone = true;
      }
   }

   /**
    * SERVER: throw flight — advances the throw progress; at tick 15 the
    * goblin is launched from the owner's eye with the owner's aim
    * (pitch/yaw), at tick 39 she lands (THROWN) and unbinds owner +
    * interaction player.
    */
   void updateThrowProgress() {
      GoblinThrow.updateThrowProgress(this);
   }

   public static Vec3d getGoblinThrowPos(BaseGirlEntity girl) {
      return GoblinThrow.getGoblinThrowPos(girl);
   }

   public static float getGoblinThrowDistance(BaseGirlEntity girl) {
      return GoblinThrow.getGoblinThrowDistance(girl);
   }

   public static float getGoblinThrowHeight(BaseGirlEntity girl) {
      return GoblinThrow.getGoblinThrowHeight(girl);
   }

   void fleeFromNearestPlayer() {
      GoblinQueenAi.fleeFromNearestPlayer(this);
   }

   protected void jump() {
      if (this.getCurrentAction() != Action.RUN || this.hasValidPath()) {
         super.jump();
      }
   }

   boolean hasValidPath() {
      PathNavigate navigator = this.getNavigator();
      Path path = navigator.getPath();
      if (path == null) {
         return true;
      } else {
         int currentIndex = path.getCurrentPathIndex();
         int length = path.getCurrentPathLength();
         if (length != currentIndex && length - 1 != currentIndex) {
            PathPoint currentPoint = path.getPathPointFromIndex(currentIndex);
            PathPoint nextPoint = path.getPathPointFromIndex(currentIndex + 1);
            return nextPoint.y - currentPoint.y == 1;
         } else {
            return true;
         }
      }
   }

   void tickGoldSteal() {
      GoblinQueenAi.tickGoldSteal(this);
   }

   int getModelPartIndex() {
      return Integer.parseInt(getModelCodeParts(this)[7]);
   }

   Integer findThrowTarget(EntityPlayer player) {
      return GoblinQueenAi.findThrowTarget(this, player);
   }

   void handleThrowState() {
      GoblinQueenAi.handleThrowState(this);
   }

   @Override
   public void onUpdate() {
      this.updateModelCodeDNA();
      handleGoblinThrowAction(this);
      this.updateThrowProgress();
      if (this.getOwnerUUID() != null) {
         this.inPortal = false;
      }

      super.onUpdate();
      this.handleShoulderIdle();
      this.tickThrownDespawnFade();
      this.tickVanishFade();
      if (this.world.isRemote) {
         this.tickPaizuriStartTimer();
         this.tickNelsonStartTimer();
         if (this.getOwnerUUID() != null) {
            this.noClip = true;
         }
      }
   }

   @Override
   public Action getPreviousAction() {
      return this.previousAction;
   }

   @Override
   public void setPreviousAction(Action action) {
      this.previousAction = action;
   }

   @Override
   public void setThrowProgress(int progress) {
      this.throwProgress = progress;
   }

   @Override
   public int getThrowProgress() {
      return this.throwProgress;
   }

   public static void handleGoblinThrowAction(BaseGirlEntity girl) {
      GoblinThrow.handleGoblinThrowAction(girl);
   }

   public void setFire(int seconds) {
      if (this.getOwnerUUID() == null) {
         super.setFire(seconds);
      }
   }

   void tickVanishFade() {
      if (this.getCurrentAction() == Action.VANISH) {
         this.fadeScale -= 0.05F;
         if (!(this.fadeScale > 0.0F)) {
            this.world.removeEntity(this);
         }
      }
   }

   void tickThrownDespawnFade() {
      if (!(Boolean)this.entityDataManager.get(TAMED)) {
         if (this.getCurrentAction() == Action.THROWN) {
            if (this.onGround || this.isInWater()) {
               this.fadeScale = (float)(this.fadeScale - 0.05);
               if (!(this.fadeScale > 0.0F)) {
                  if (!this.world.isRemote) {
                     this.setCurrentAction(Action.NULL);
                     this.setInteractionPlayerUUID(null);
                     this.setOwnerUUID(null);
                     this.world.removeEntity(this);
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   void tickPaizuriStartTimer() {
      GoblinInteractions.tickPaizuriStartTimer(this);
   }

   @SideOnly(Side.CLIENT)
   void tickNelsonStartTimer() {
      GoblinInteractions.tickNelsonStartTimer(this);
   }

   /**
    * SERVER: the goblin state machine core — guards the cum actions
    * (paizuri/nelson/breeding), and on the server fires the per-action setup
    * hooks (START_THROWING returns the stolen item, PAIZURI_START/
    * NELSON_INTRO position the player, BREEDING_CUM_0 marks pregnant and
    * arms the release cooldown, NELSON_CUM toggles the pregnancy flag, and
    * leaving PAIZURI_CUM runs {@link #endPaizuriScene()}).
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.PAIZURI_CUM || action != Action.PAIZURI_SLOW && action != Action.PAIZURI_FAST) {
         if (currentAction != Action.NELSON_CUM || action != Action.NELSON_SLOW && action != Action.NELSON_FAST) {
            if (currentAction != Action.BREEDING_CUM_0 || action != Action.BREEDING_SLOW_0 && action != Action.BREEDING_FAST_0) {
               if (action == Action.START_THROWING && !this.world.isRemote) {
                  this.setInteractionPlayerUUID(this.getOwnerUUID());
                  this.returnStolenItem();
               }

               if (action == Action.PAIZURI_START && !this.world.isRemote) {
                  this.handlePlayerInteract();
               }

               if (action == Action.NELSON_INTRO && !this.world.isRemote) {
                  this.handlePlayerLook();
               }

               if (this.getCurrentAction() == Action.PAIZURI_CUM && action == Action.NULL && !this.world.isRemote) {
                  this.endPaizuriScene();
               }

               if (action == Action.BREEDING_CUM_0) {
                  this.entityDataManager.set(PREGNANT, true);
                  this.impregnationTick = this.world.getTotalWorldTime();
                  this.pregnantNoticeTick = this.world.getTotalWorldTime();
               }

               if (action == Action.BREEDING_CUM_0) {
                  this.throwCooldown = 0;
               }

               if (action == Action.NELSON_CUM) {
                  this.entityDataManager.set(PREGNANT, true);
               }

               if (currentAction == Action.NELSON_CUM && action != Action.NELSON_CUM) {
                  this.entityDataManager.set(PREGNANT, false);
               }

               super.setCurrentAction(action);
            }
         }
      }
   }

   void endPaizuriScene() {
      GoblinInteractions.endPaizuriScene(this);
   }

   void handlePlayerLook() {
      GoblinInteractions.handlePlayerLook(this);
   }

   void handlePlayerInteract() {
      GoblinInteractions.handlePlayerInteract(this);
   }

   void returnStolenItem() {
      GoblinInteractions.returnStolenItem(this);
   }

   /**
    * SERVER: pickup glue — while PICK_UP with a bound owner, sticks the goblin
    * to the owner's position, counts down {@code heldPlayerDistance} (45) and
    * reaches SHOULDER_IDLE (noClip) at 0. Falls back to NULL when the owner
    * vanishes or exceeds 10 blocks.
    */
   public static void handlePickUpState(BaseGirlEntity girl) {
      GoblinThrow.handlePickUpState(girl);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public boolean shouldRenderNameTag() {
      if (this.getCurrentAction() != Action.NULL) {
         return false;
      } else if (this.getOwnerUUID() != null) {
         return false;
      } else {
         return !this.entityDataManager.get(TAMED) && !Minecraft.getMinecraft().player.canEntityBeSeen(this) ? false : this.getOwnerUUID() == null;
      }
   }

   void handleShoulderIdle() {
      GoblinThrow.handleShoulderIdle(this);
   }

   @Override
   protected Action getNextAction(Action action) {
      switch (action) {
         case PAIZURI_IDLE:
         case PAIZURI_SLOW:
            return Action.PAIZURI_FAST;
         case BREEDING_SLOW_0:
            return Action.BREEDING_FAST_0;
         case BREEDING_SLOW_2:
            return Action.BREEDING_FAST_2;
         case NELSON_SLOW:
            return Action.NELSON_FAST;
         default:
            return null;
      }
   }

   @Override
   protected Action getCumAction(Action action) {
      switch (action) {
         case PAIZURI_SLOW:
         case PAIZURI_FAST:
         case PAIZURI_FAST_CONTINUES:
            return Action.PAIZURI_CUM;
         case BREEDING_SLOW_0:
         case BREEDING_FAST_0:
            for (GoblinEntity goblin : this.guards) {
               goblin.getCumAction(action);
            }

            return Action.BREEDING_CUM_0;
         case BREEDING_SLOW_2:
         case BREEDING_FAST_2:
            return Action.BREEDING_CUM_2;
         case NELSON_SLOW:
         case NELSON_FAST:
            return Action.NELSON_CUM;
         case BREEDING_1:
            return Action.BREEDING_CUM_1;
         default:
            return null;
      }
   }

   public boolean isBlockedByCeiling() {
      Block block = this.world.getBlockState(this.getPosition().add(0, 1, 0)).getBlock();
      return !block.isPassable(this.world, this.getPosition().add(0, 1, 0));
   }

   public void fall(float distance, float multiplier) {
      Action action = this.getCurrentAction();
      if (action != Action.THROWN && action != Action.START_THROWING) {
         super.fall(distance, multiplier);
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      return GoblinAnimations.predicate(this, event);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      GoblinAnimations.registerControllers(this, data);
   }
}
