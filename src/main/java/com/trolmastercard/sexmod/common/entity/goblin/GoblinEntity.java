package com.trolmastercard.sexmod.common.entity.goblin;

import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.gui.screen.GalathScreen;
import com.trolmastercard.sexmod.client.gui.screen.SceneFadeScreen;
import com.trolmastercard.sexmod.common.entity.goblin.HairColor;
import com.trolmastercard.sexmod.common.world.PreviewWorld;
import com.trolmastercard.sexmod.network.scene.ResetGirlPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.registry.SoundHandler;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map.Entry;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.controller.AnimationController;

import com.trolmastercard.sexmod.common.entity.base.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.common.entity.base.Action;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.common.entity.goblin.SkinColor;
import com.trolmastercard.sexmod.client.render.entity.GoblinRenderer;
import com.trolmastercard.sexmod.common.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.common.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.api.IGoblin;
import com.trolmastercard.sexmod.common.util.math.Point2D;
import com.trolmastercard.sexmod.common.entity.goblin.EyeColor;
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
 * <b>Decomposition.</b> The per-entity glue (animations, appearance,
 * interactions, persistence) lives in this class. Two named subsystems stay
 * separate: {@link GoblinQueenAi} (gold steal, breeding, guards, cooldowns)
 * and {@link GoblinThrow} (carry/throw kinematics shared with the player
 * form). {@link GoblinEventHandler} stays a Forge event handler.
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
      this.doActionImpl(action, uuid);
   }

   public void setThrowTarget(UUID uuid) {
      this.setThrowTargetImpl(uuid);
   }

   public void setPickupTarget(UUID uuid) {
      this.setPickupTargetImpl(uuid);
   }

   @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
   public static boolean openGoblinPadForPlayer(net.minecraft.client.Minecraft mc, UUID playerUuid) {
      return openGoblinPadForPlayerImpl(mc, playerUuid);
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
      return buildModelCodeDNAImpl(builder);
   }

   @Override
   public ArrayList<Integer> getCustomPartIdList() {
      return getCustomPartIdListImpl();
   }

   @Override
   public List<Integer> getCustomPartExtraIdList() {
      return getCustomPartExtraIdListImpl();
   }

   @Override
   public Point2D getModelPartByIndex(int index) {
      return getModelPartByIndexImpl(index);
   }

   @Override
   public void setCustomPartList(List<Integer> parts) {
      this.setCustomPartListImpl(parts);
   }

   void updateModelCodeDNA() {
      this.updateModelCodeDNAImpl();
   }

   protected String buildModelCodeDNA(StringBuilder builder, int partIndex) {
      return buildModelCodeDNAImpl(builder, partIndex);
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      this.write(nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.read(nbt);
   }

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      return this.processInteractImpl(player, hand);
   }

   public static boolean hasGoblinWithUUID(UUID uuid) {
      return hasGoblinWithUUIDImpl(uuid);
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
      this.tickPaizuriStartTimerImpl();
   }

   @SideOnly(Side.CLIENT)
   void tickNelsonStartTimer() {
      this.tickNelsonStartTimerImpl();
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
      this.endPaizuriSceneImpl();
   }

   void handlePlayerLook() {
      this.handlePlayerLookImpl();
   }

   void handlePlayerInteract() {
      this.handlePlayerInteractImpl();
   }

   void returnStolenItem() {
      this.returnStolenItemImpl();
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
      return this.predicate(event);
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
      if (this.world instanceof PreviewWorld) {
         return PlayState.STOP;
      }

      if (this.actionController == null) {
         this.initAnimationControllers();
      }

      switch (event.getController().getName()) {
         case "eyes":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.this.null", true, event);
            } else {
               this.createAnimation("animation.this.blink", true, event);
            }
            break;
         case "movement":
            if (this.getCurrentAction() != Action.NULL) {
               this.createAnimation("animation.this.null", true, event);
            } else {
               double moved = Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ);
               if (!(Boolean)this.entityDataManager.get(BaseGirlEntity.IS_ANCHORED) && moved > 0.0) {
                  if (this.onGround && Math.abs(Math.abs(this.prevPosY) - Math.abs(this.posY)) < 0.1F) {
                     if (moved > 0.2F) {
                        this.createAnimation("animation.this.walk", true, event);
                     } else {
                        this.createAnimation("animation.this.walk", true, event);
                     }

                     this.rotationYaw = this.rotationYawHead;
                  } else {
                     this.createAnimation("animation.this.fly", true, event);
                  }
               } else {
                  this.createAnimation("animation.this.idle", true, event);
               }
            }
            break;
         case "action":
            Minecraft mc = Minecraft.getMinecraft();
            String camMode = mc.player.getPersistentID().equals(this.getOwnerUUID()) && mc.gameSettings.thirdPersonView == 0 ? "1" : "3";
            switch (this.getCurrentAction()) {
               case PAIZURI_IDLE:
                  this.createAnimation("animation.this.paizuri_idle", true, event);
                  break;
               case PAIZURI_SLOW:
                  this.createAnimation("animation.this.paizuri_slow" + this.paizuriPoseSuffix, true, event);
                  break;
               case BREEDING_SLOW_0:
                  this.createAnimation("animation.this.breeding_slow_1" + (this.breedingSideLeft ? "l" : "r"), true, event);
                  break;
               case BREEDING_SLOW_2:
                  this.createAnimation("animation.this.breeding_slow_3", true, event);
                  break;
               case NELSON_SLOW:
                  this.createAnimation("animation.this.nelson_slow" + (this.nelsonSlowBasePose ? "" : "2"), true, event);
                  break;
               case PAIZURI_FAST:
                  this.createAnimation("animation.this.paizuri_fast", true, event);
                  break;
               case PAIZURI_FAST_CONTINUES:
                  this.createAnimation("animation.this.paizuri_fast_countinues", true, event);
                  break;
               case BREEDING_1:
                  this.createAnimation("animation.this.breeding_2", true, event);
                  break;
               case BREEDING_FAST_2:
                  this.createAnimation("animation.this.breeding_fast_3", true, event);
                  break;
               case NELSON_FAST:
                  this.createAnimation("animation.this.nelson_fast" + (this.nelsonAltPose ? "c" : "s"), true, event);
                  break;
               case BREEDING_FAST_0:
                  this.createAnimation("animation.this.breeding_fast_1" + (this.breedingFastReadyPose ? "c" : "s"), true, event);
                  break;
               case NULL:
                  this.createAnimation("animation.this.null", true, event);
                  break;
               case SHOULDER_IDLE:
                  this.createAnimation("animation.this.shoulder_idle", true, event);
                  break;
               case PICK_UP:
                  this.createAnimation(String.format("animation.this.pick_up_%sperson", camMode), true, event);
                  break;
               case SIT:
                  this.createAnimation("animation.this.sit", true, event);
                  break;
               case RUN:
                  if (this.onGround) {
                     this.createAnimation("animation.this.running", true, event);
                  } else {
                     this.createAnimation("animation.this.fly", true, event);
                  }
                  break;
               case CATCH:
                  this.createAnimation(String.format("animation.this.catch_%sperson", camMode), true, event);
                  break;
               case CATCH_BJ:
                  this.createAnimation(String.format("animation.this.catch_%spersonBj", camMode), true, event);
                  break;
               case CATCH_BJ_IDLE:
                  this.createAnimation(String.format("animation.this.catch_%spersonBj_idle", camMode), true, event);
                  break;
               case START_THROWING:
                  this.createAnimation(String.format("animation.this.throw_%sperson", camMode), true, event);
                  break;
               case THROWN:
                  this.createAnimation("animation.this.thrown", true, event);
                  break;
               case PAIZURI_START:
                  this.createAnimation("animation.this.paizuri_start", true, event);
                  break;
               case PAIZURI_CUM:
                  this.createAnimation("animation.this.paizuri_cum", true, event);
                  break;
               case JUMP_0:
                  this.createAnimation("animation.this.jump_1", true, event);
                  break;
               case JUMP_1:
                  this.createAnimation("animation.this.jump_2", true, event);
                  break;
               case JUMP_2:
                  this.createAnimation("animation.this.jump_3", true, event);
                  break;
               case BREEDING_INTRO_0:
                  this.createAnimation("animation.this.breeding_intro_1", true, event);
                  break;
               case BREEDING_INTRO_1:
                  this.createAnimation("animation.this.breeding_intro_2", true, event);
                  break;
               case BREEDING_INTRO_2:
                  this.createAnimation("animation.this.breeding_intro_3", true, event);
                  break;
               case BREEDING_CUM_0:
                  this.createAnimation("animation.this.breeding_cum_1", true, event);
                  break;
               case BREEDING_CUM_1:
                  this.createAnimation("animation.this.breeding_cum_2", true, event);
                  break;
               case BREEDING_CUM_2:
                  this.createAnimation("animation.this.breeding_cum_3", true, event);
                  break;
               case VANISH:
               case AWAIT_PICK_UP:
                  this.createAnimation("animation.this.await_pick_up", true, event);
                  break;
               case STAND_UP:
                  this.createAnimation("animation.this.stand_up", false, event);
                  break;
               case NELSON_INTRO:
                  this.createAnimation("animation.this.nelson_intro", true, event);
                  break;
               case NELSON_CUM:
                  this.createAnimation("animation.this.nelson_cum", true, event);
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
            case "catchEh":
               this.sendChatMessage("ehh..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchAkward":
               this.sendChatMessage("awkward..");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchWell":
               this.sendChatMessage("well...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchRather":
               this.sendChatMessage("would you rather have this stupid... thing?");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchMe":
               this.sendChatMessage("...or use me?~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "catchDone":
               if ("bj".equals(this.entityDataManager.get(BaseGirlEntity.GIRL_HAND_STATES))) {
                  this.setCurrentAction(Action.CATCH_BJ);
               }
               break;
            case "catchBjDone":
               this.setCurrentAction(Action.CATCH_BJ_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  BaseGirlEntity.openInventoryGui(player, this, new String[]{"use her", "take ur stuff back"}, null, false);
               }
               break;
            case "paizuriChoice":
               this.sendChatMessage("good choice!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriBoth":
               this.sendChatMessage("...for both of us!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizruiUse":
               this.sendChatMessage("now use me like a fuck toy!~");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "paizuriSwitch":
               if (!this.getRNG().nextBoolean()) {
                  this.paizuriPoseSuffix = "".equals(this.paizuriPoseSuffix) ? "2" : "";
               }
               break;
            case "touch":
               this.playRandomSoundAtVolume(SoundHandler.MISC_TOUCH, 3.0F);
               break;
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.04F);
               }
               break;
            case "paizuri_startDone":
               this.setCurrentAction(Action.PAIZURI_IDLE);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "paizuriFastDone":
               this.setCurrentAction(Action.PAIZURI_SLOW);
               break;
            case "paizuriFastReady":
               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.PAIZURI_FAST_CONTINUES);
               }
               break;
            case "paizuriFastContinuesReady":
               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.resetAnimationControllerOffset();
               }
               break;
            case "smallPound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_POUNDING, 0.25F);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02F);
               }
               break;
            case "paizruiCam":
               if (this.isControlledByLocalPlayer()) {
                  EntityPlayerSP player = Minecraft.getMinecraft().player;
                  player.rotationPitch = 70.0F;
                  player.prevRotationPitch = 70.0F;
               }
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  SceneFadeScreen.enableInteraction();
               }
               break;
            case "paizuriCumDone":
               this.setCurrentAction(Action.NULL);
               break;
            case "cumSound":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "jumpCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = this.getYawRotation() + 170.0F;
                  mc.player.rotationPitch = -20.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 2;
               }
               break;
            case "breedingHmm":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.player.rotationYaw = this.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }

               this.sendChatMessage("hmm...");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingFound":
               this.sendChatMessage("guess we found a worthy breeding partner!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingEnough":
               this.sendChatMessage("Eh.. go pin him down, before he runs off!");
               this.playRandomSound(SoundHandler.MISC_PLOB);
               break;
            case "breedingCam2":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 2;
                  mc.player.rotationYaw = this.getYawRotation() - 120.0F;
                  mc.player.rotationPitch = -30.0F;
               }
            case "breedingIntroDone":
               this.setCurrentAction(Action.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "breeding_slow1Done":
               if (this.getRNG().nextBoolean()) {
                  this.breedingSideLeft = !this.breedingSideLeft;
               }

               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.BREEDING_FAST_0);
                  this.breedingFastReadyPose = false;
               }
               break;
            case "breeding_fast1Done":
               this.setCurrentAction(Action.BREEDING_SLOW_0);
               if (this.isControlledByLocalPlayer()) {
                  this.breedingFastReadyPose = false;
               }
               break;
            case "breeding_fast1Ready":
               if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
                  this.breedingFastReadyPose = true;
                  this.resetAnimationControllerOffset();
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "cum":
               this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 3.0F);
               break;
            case "breeding_intro_3Done":
               this.setCurrentAction(Action.BREEDING_SLOW_2);
               break;
            case "breeding_3_wiggle":
               if (this.getRNG().nextBoolean()) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "breeding_fast_3Done":
               if (this.isControlledByLocalPlayer() && !PlayerMovementController.isJumping) {
                  this.setCurrentAction(Action.BREEDING_SLOW_2);
               }
               break;
            case "breeding_intro_2Done":
               this.setCurrentAction(Action.BREEDING_1);
               break;
            case "breeding_cumCam":
               if (this.isControlledByLocalPlayer()) {
                  Minecraft mc = Minecraft.getMinecraft();
                  mc.gameSettings.thirdPersonView = 0;
                  mc.player.rotationYaw = this.getYawRotation() + 180.0F;
                  mc.player.rotationPitch = -15.0F;
                  mc.player.rotationYawHead = mc.player.rotationYaw;
                  mc.gameSettings.thirdPersonView = 0;
               }
               break;
            case "neslon_introDone":
               this.setCurrentAction(Action.NELSON_SLOW);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "nelson_slowDone":
               if (this.getRNG().nextBoolean()) {
                  this.nelsonSlowBasePose = !this.nelsonSlowBasePose;
               }
               break;
            case "neslon_fastSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.nelsonAltPose = true;
                  return;
               }

               if (PlayerMovementController.isJumping) {
                  this.nelsonAltPose = true;
               }
               break;
            case "neslon_fastBackSwitch":
               if (!this.isControlledByLocalPlayer()) {
                  this.actionController.tickOffset = 0.0;
               } else if (PlayerMovementController.isJumping) {
                  this.actionController.tickOffset = 0.0;
               }
               break;
            case "nelsonFastDone":
               this.nelsonAltPose = false;
               if (this.isControlledByLocalPlayer()) {
                  this.setCurrentAction(Action.NELSON_SLOW);
               }
               break;
            case "nelson_cumDone":
               if (this.isControlledByLocalPlayer()) {
                  this.resetCameraAndPhysics();
                  this.setCurrentAction(Action.NULL);
               }
         }
      };
      this.actionController.registerSoundListener(soundListener);
      this.movementController.transitionLengthTicks = 10.0;
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
   }



   static String buildModelCodeDNAImpl(StringBuilder builder) {
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 8);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 8);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, SkinColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 0);
      return builder.toString();
   }



   static ArrayList<Integer> getCustomPartIdListImpl() {
      return new ArrayList<Integer>() {
         {
            this.add(4);
            this.add(3);
            this.add(3);
            this.add(16);
            this.add(16);
            this.add(6);
            this.add(HairColor.values().length);
            this.add(SkinColor.values().length);
            this.add(EyeColor.values().length);
         }
      };
   }



   static List<Integer> getCustomPartExtraIdListImpl() {
      return Collections.singletonList(2);
   }



   static Point2D getModelPartByIndexImpl(int index) {
      switch (index) {
         case 0:
            return new Point2D(40, 130);
         case 1:
            return new Point2D(60, 130);
         case 2:
            return new Point2D(80, 130);
         case 3:
            return new Point2D(100, 130);
         case 4:
            return new Point2D(120, 130);
         case 5:
            return new Point2D(140, 130);
         case 6:
            return new Point2D(160, 130);
         case 7:
            return new Point2D(180, 130);
         case 8:
            return new Point2D(200, 0);
         case 9:
            return new Point2D(200, 130);
         default:
            return Point2D.ZERO;
      }
   }



   void setCustomPartListImpl(List<Integer> parts) {
      StringBuilder builder = new StringBuilder();

      for (int partId : parts) {
         AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
      }

      AbstractNpcOnlyEntity.appendPaddedNumber(builder, Integer.parseInt(AbstractNpcOnlyEntity.getModelCodeParts(this)[9]));
      this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
      if (Main.proxy instanceof ClientProxy) {
         GoblinRenderer.clearBoneColors();
      }
   }



   void updateModelCodeDNAImpl() {
      if (this.customPartsData != null) {
         StringBuilder builder = new StringBuilder();

         for (Entry entry : this.customPartsData) {
            int partId = (Integer)((Entry)entry.getValue()).getValue();
            AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
         }

         AbstractNpcOnlyEntity.appendPaddedNumber(builder, Integer.parseInt(AbstractNpcOnlyEntity.getModelCodeParts(this)[9]));
         this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
         GoblinRenderer.clearBoneColors();
      }
   }



   static String buildModelCodeDNAImpl(StringBuilder builder, int partIndex) {
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 3);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 2);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 7);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 5);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, HairColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, partIndex);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, EyeColor.values().length - 1);
      AbstractNpcOnlyEntity.appendPaddedNumber(builder, 0);
      return builder.toString();
   }



   /**
    * CLIENT: action dispatch from the this's GUI — {@code take ur stuff
    * back} starts the throw, {@code use her} binds the acting player as the
    * throw/pickup target.
    */
   void doActionImpl(String action, UUID uuid) {
      if ("take ur stuff back".equals(action)) {
         this.setCurrentAction(Action.START_THROWING);
      }

      if ("use her".equals(action)) {
         this.setThrowTarget(uuid);
      }
   }



   /**
    * CLIENT: arms the "use her" timer ({@code paizuriStartTicks} = 0) — after 15
    * ticks the paizuri scene starts (see {@link #tickPaizuriStartTimer()}).
    */
   void setThrowTargetImpl(UUID uuid) {
      this.paizuriStartTicks = 0;
      SceneFadeScreen.enableInteraction();
      PlayerMovementController.setMovementLock(false);
      this.setInteractionPlayerUUID(uuid);
   }



   /**
    * CLIENT: arms the nelson "pickup" timer ({@code nelsonStartTicks} = 0) — after
    * 15 ticks the nelson scene starts (see {@link #tickNelsonStartTimer()}).
    */
   void setPickupTargetImpl(UUID uuid) {
      this.nelsonStartTicks = 0;
      SceneFadeScreen.enableInteraction();
      PlayerMovementController.setMovementLock(false);
      this.setInteractionPlayerUUID(uuid);
   }



   /**
    * CLIENT: opens the goblin throw/pickup gesture pad ({@code GalathScreen})
    * when the local player owns a goblin that is riding their shoulder
    * (SHOULDER_IDLE). Returns true if a pad was opened. Used by both the G
    * keybind and the RShift (Leave-sex-scene) dual keybind.
    */
   @net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)
   static boolean openGoblinPadForPlayerImpl(net.minecraft.client.Minecraft mc, UUID playerUuid) {
      if (mc == null || playerUuid == null) return false;
      if (mc.currentScreen instanceof com.trolmastercard.sexmod.client.gui.screen.GalathScreen) return false;
      BaseGirlEntity padGirl = null;
      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl.world.isRemote && girl instanceof IGoblin) {
               IGoblin goblin = (IGoblin)girl;
               if (playerUuid.equals(goblin.getOwnerUUID())) {
                  padGirl = girl;
                  break;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         return false;
      }
      if (padGirl != null && padGirl.getCurrentAction() == Action.SHOULDER_IDLE) {
         mc.displayGuiScreen(new com.trolmastercard.sexmod.client.gui.screen.GalathScreen(padGirl));
         return true;
      }
      return false;
   }



   boolean processInteractImpl(EntityPlayer player, EnumHand hand) {
      if (this.world.isRemote) {
         return true;
      }

      if (this.isQueen) {
         return true;
      }

      if (this.getCurrentAction() == Action.RUN) {
         if (this.getDistance(player) > 3.5) {
            player.sendStatusMessage(new TextComponentString("get a bit closer..."), true);
         } else {
            this.setTargetPosition(player.getPositionVector());
            this.setYawRotation(player.rotationYaw);
            this.setCurrentAction(Action.CATCH);
            this.entityDataManager.set(BaseGirlEntity.GIRL_HAND_STATES, "bj");
            this.setOwnerUUID(player.getPersistentID());
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.getNavigator().clearPath();
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }

         return true;
      } else {
         if (hasGoblinWithUUID(player.getPersistentID())) {
            player.sendStatusMessage(new TextComponentString("you are already carrying a Goblin"), true);
         } else {
            this.setOwnerUUID(player.getPersistentID());
            this.setCurrentAction(Action.PICK_UP);
            this.heldPlayerDistance = 45;
            this.setAnchored(false);
            this.entityDataManager.set(GoblinEntity.TAMED, true);
            this.getNavigator().clearPath();
         }

         return true;
      }
   }



   static boolean hasGoblinWithUUIDImpl(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (girl instanceof IGoblin && !girl.world.isRemote && !girl.isDead) {
               UUID candidateUuid = ((IGoblin)girl).getOwnerUUID();
               // WILD/unowned goblins have a null owner; the QUERY uuid is the
               // receiver so a null candidate just fails the match (no NPE).
               // (Original bytecode: var0.equals(var3) — query first. The rename
               // swapped the operands, NPEing on every unowned goblin.)
               if (uuid.equals(candidateUuid)) {
                  return true;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      return false;
   }



   /**
    * SERVER: paizuri scene teardown — resets the bound player, unbinds,
    * un-anchors, restores physics, drops the stolen item and (untamed) sends
    * her home and despawns her.
    */
   void endPaizuriSceneImpl() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      this.setInteractionPlayerUUID(null);
      this.setAnchored(false);
      this.noClip = false;
      this.setNoGravity(false);
      this.entityDataManager.set(GoblinEntity.STOLEN_ITEM, ItemStack.EMPTY);
      if (!(Boolean)this.entityDataManager.get(GoblinEntity.TAMED)) {
         this.setPositionAndUpdate(this.homePos.x, this.homePos.y, this.homePos.z);
         this.world.removeEntity(this);
      }
   }



   void handlePlayerLookImpl() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(player.getPositionVector());
         this.setYawRotation(player.rotationYaw);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         this.setInteractionPlayerUUID(player.getPersistentID());
      }
   }



   void handlePlayerInteractImpl() {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      if (player != null) {
         this.setOwnerUUID(null);
         this.setTargetPosition(player.getPositionVector());
         this.setYawRotation(player.rotationYaw + 180.0F);
         this.setAnchored(true);
         this.noClip = true;
         this.setNoGravity(true);
         player.setNoGravity(true);
         player.noClip = true;
         this.setInteractionPlayerUUID(player.getPersistentID());
         player.setPositionAndUpdate(player.posX, player.posY - 0.5, player.posZ);
         player.rotationPitch = 70.0F;
         player.prevRotationPitch = 70.0F;
      }
   }



   /**
    * SERVER: returns the stolen item stack to the interaction player's
    * inventory and clears the stolen-item key.
    */
   void returnStolenItemImpl() {
      ItemStack heldItem = (ItemStack)this.entityDataManager.get(GoblinEntity.STOLEN_ITEM);
      if (heldItem != ItemStack.EMPTY) {
         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (player != null) {
            player.inventory.addItemStackToInventory(heldItem.copy());
            this.entityDataManager.set(GoblinEntity.STOLEN_ITEM, ItemStack.EMPTY);
         }
      }
   }



   void tickPaizuriStartTimerImpl() {
      if (this.paizuriStartTicks != -1) {
         if (++this.paizuriStartTicks == 15) {
            this.paizuriStartTicks = -1;
            this.setCurrentAction(Action.PAIZURI_START);
            Minecraft.getMinecraft().player.closeScreen();
         }
      }
   }



   void tickNelsonStartTimerImpl() {
      if (this.nelsonStartTicks != -1) {
         if (++this.nelsonStartTicks == 15) {
            this.nelsonStartTicks = -1;
            this.setCurrentAction(Action.NELSON_INTRO);
            Minecraft mc = Minecraft.getMinecraft();
            mc.player.closeScreen();
            mc.gameSettings.thirdPersonView = 2;
         }
      }
   }



   void write(NBTTagCompound nbt) {
      nbt.setString("bodyColor", (String)this.entityDataManager.get(AbstractNpcOnlyEntity.CURRENT_ACTION));
      nbt.setInteger("eyeColorX", ((BlockPos)this.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getX());
      nbt.setInteger("eyeColorY", ((BlockPos)this.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getY());
      nbt.setInteger("eyeColorZ", ((BlockPos)this.entityDataManager.get(AbstractNpcOnlyEntity.ACTION_TARGET_POS)).getZ());
      nbt.setString("model", (String)this.entityDataManager.get(AbstractNpcOnlyEntity.APPEARANCE_DNA));
      nbt.setString("girlID", (String)this.entityDataManager.get(AbstractNpcOnlyEntity.GIRL_ID));
      nbt.setString("queen", (String)this.entityDataManager.get(GoblinEntity.QUEEN_ID));
      nbt.setBoolean("isQueen", this.isQueen);
      nbt.setBoolean("isTamed", (Boolean)this.entityDataManager.get(GoblinEntity.TAMED));
      nbt.setInteger("robTicks", this.robTicks);
      if (this.isQueen) {
         nbt.setBoolean("preggo", (Boolean)this.entityDataManager.get(GoblinEntity.PREGNANT));
         nbt.setFloat("throneRot", this.throneYaw);
         nbt.setDouble("thronePosX", this.thronePos.x);
         nbt.setDouble("thronePosY", this.thronePos.y);
         nbt.setDouble("thronePosZ", this.thronePos.z);
         nbt.setLong("impregnationTick", this.impregnationTick);

         for (int i = 0; i < this.guardUUIDs.size(); i++) {
            nbt.setString("guard" + i, this.guardUUIDs.get(i).toString());
         }
      }
   }



   void read(NBTTagCompound nbt) {
      this.isQueen = nbt.getBoolean("isQueen");
      this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, nbt.getString("model"));
      this.entityDataManager.set(AbstractNpcOnlyEntity.CURRENT_ACTION, nbt.getString("bodyColor"));
      String[] parts = AbstractNpcOnlyEntity.getModelCodeParts(this);
      if (Integer.parseInt(parts[3]) > 7 || Integer.parseInt(parts[4]) > 7) {
         this.entityDataManager.set(
            AbstractNpcOnlyEntity.APPEARANCE_DNA, this.buildModelCodeDNA(new StringBuilder(), this.getModelPartIndex())
         );
         Main.LOGGER.log(Level.INFO, "updated an old Goblin");
      }

      this.entityDataManager.set(
         AbstractNpcOnlyEntity.ACTION_TARGET_POS,
         new BlockPos(nbt.getInteger("eyeColorX"), nbt.getInteger("eyeColorY"), nbt.getInteger("eyeColorZ"))
      );
      this.entityDataManager.set(AbstractNpcOnlyEntity.GIRL_ID, nbt.getString("girlID"));
      this.entityDataManager.set(GoblinEntity.QUEEN_ID, nbt.getString("queen"));
      this.entityDataManager.set(GoblinEntity.TAMED, nbt.getBoolean("isTamed"));
      this.robTicks = nbt.getInteger("robTicks");
      if (this.isQueen) {
         this.throneYaw = nbt.getFloat("throneRot");
         this.thronePos = new Vec3d(nbt.getDouble("thronePosX"), nbt.getDouble("thronePosY"), nbt.getDouble("thronePosZ"));

         for (int i = 0; !"".equals(nbt.getString("guard" + i)); i++) {
            this.guardUUIDs.add(UUID.fromString(nbt.getString("guard" + i)));
         }

         this.entityDataManager.set(GoblinEntity.PREGNANT, nbt.getBoolean("preggo"));
         this.impregnationTick = nbt.getLong("impregnationTick");
      }
   }

}
