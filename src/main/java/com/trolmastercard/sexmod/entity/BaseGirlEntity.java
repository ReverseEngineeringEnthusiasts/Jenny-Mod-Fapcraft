package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.GirlInventoryScreen;
import com.trolmastercard.sexmod.client.model.GirlModel;
import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import com.trolmastercard.sexmod.client.renderer.SexSceneRenderer;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.networking.ChangeDataParameterPacket;
import com.trolmastercard.sexmod.networking.KoboldStatePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.ResetControllerPacket;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.LootTableHandler;
import com.trolmastercard.sexmod.util.Point2D;
import com.trolmastercard.sexmod.util.GirlRegistry;
import com.trolmastercard.sexmod.util.SceneDebug;
import com.trolmastercard.sexmod.util.ClientServerCheck;
import com.mojang.realmsclient.util.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.GeoModelProvider;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;
import software.bernie.geckolib3.util.MatrixStack;

/**
 * Base class for every Fapcraft girl — NPCs (Jenny, Bia, Luna, Ellie, Slime,
 * Bee, Goblin, Allie, Kobold, Galath, Manglelie) and the horny-potion
 * player-form variants (subclasses of {@link AbstractPlayerGirlEntity}).
 * <p>
 * <b>Shared state</b> lives in the vanilla {@link EntityDataManager}. The
 * {@link Action} keys below use <b>explicit serializer IDs 99..110</b>
 * (created via {@code getSerializer().createKey(...)}) so the same keys are
 * compatible across the whole girl hierarchy — including
 * {@link AbstractNpcOnlyEntity}'s keys 119..121. <b>Never renumber these
 * IDs</b>: they are baked into the built jar and into worlds saved by it.
 * <p>
 * <b>Key semantics</b> (all synced to the client automatically):
 * <ul>
 *   <li>{@link #GIRL_ID} — persistent girl UUID, minted on first access.</li>
 *   <li>{@link #CUR_ACTION} — the current {@link Action}, advanced by
 *       {@link #setCurrentAction(Action)} (client writes route via
 *       {@code ChangeDataParameterPacket}).</li>
 *   <li>{@link #GIRL_HAND_STATES} — the scene-entry hand-state, written by the
 *       client as {@code "animationFollowUp"} and consumed by each girl's
 *       {@code U()} dispatcher after the dismount lerp completes.</li>
 *   <li>{@link #INTERACTION_PARTNER_UUID} — the player bound to the current
 *       scene ("null" string when unset).</li>
 *   <li>{@link #IS_ANCHORED} / {@link #TARGET_POS} / {@link #YAW_ROTATION} —
 *       anchor lock: while anchored the girl is held at target position/yaw
 *       every tick in {@link #updateAITasks()}.</li>
 *   <li>{@link #MASTER} — the owning player's UUID (follow mode).</li>
 *   <li>{@link #OUTFIT_INDEX} — 0 = nude, 1 = dressed.</li>
 *   <li>{@link #WALK_SPEED}, {@link #CUSTOM_MODEL_KEY}, {@link #CUSTOM_NAME} —
 *       walk state, whitelisted custom-model code, custom display name.</li>
 * </ul>
 * <p>
 * <b>Scene lifecycle</b> (see {@link Action} for the state machine):
 * <ol>
 *   <li>Entry: client {@code doAction} sets {@code GIRL_HAND_STATES} via
 *       {@code ChangeDataParameterPacket} and sends {@code KoboldStatePacket};
 *       the server calls {@code setDismounted()} (per-girl flag) and the girl
 *       lerps ~40 ticks to {@code TARGET_POS} (see
 *       {@code RotationHelper.lerpVec3d} — INT variant!), then anchors and
 *       calls {@code U()} to pick the scene action.</li>
 *   <li>Progression: sound-keyframe transitions in {@code registerControllers}
 *       (primary) + SERVER-side follow-ups in {@link #tickFollowUpTransitions()}
 *       + input (sneak/jump via {@code HandlePlayerMovement}).</li>
 *   <li>End: the cum action's {@code xxx_cumDone} keyframe calls
 *       {@link #resetCameraAndPhysics()} -&gt; {@code resetLocalPlayerClientState()}
 *       -&gt; single-arg {@code ResetGirlPacket} = FULL reset (player physics +
 *       girl release, {@code reinitTasks()}).</li>
 * </ol>
 * <p>
 * <b>Pitfalls:</b>
 * <ul>
 *   <li>On the client, data-manager writes MUST go through
 *       {@link #changeDataParameterFromClient(String, String)} — writing the
 *       data manager directly on the client never reaches the server.</li>
 *   <li>{@link #onUpdate()} must never {@code setDead} the girl — the original
 *       jar has no removal there; a deobf-regression did and made girls
 *       vanish on benign tick exceptions.</li>
 *   <li>{@link #resetGirlState()} (R-Shift) sends the two-arg TRUE packet
 *       (player-only reset); the natural scene end uses the single-arg packet.
 *       See {@code ResetGirlPacket}.</li>
 * </ul>
 */
public abstract class BaseGirlEntity extends EntityCreature implements IAnimatable {
   protected static final long TICK_RATE = 20L;
   private final AnimationFactory animationFactory = new AnimationFactory(this);
   public EntityAIWanderAvoidWater wanderGoal;
   public WatchClosestGirlGoal watchClosestGirlGoal;
   public static HashSet<BaseGirlEntity> GLOBAL_GIRL_CACHE = new HashSet<>();
   public Vec3d cameraOriginPos;
   protected float cameraYaw;
   public EntityDataManager entityDataManager;
   public PathNavigate pathNavigator;
   public Vec3d homePos = Vec3d.ZERO;
   public EntityEnderPearl activeEnderPearl;
   public float scaleFactor = 1.0F;
   public boolean isSpecialState = false;
   private boolean isLocallyRegistered = false;
   HashMap<String, Vec3d> boneOffsetCache = new HashMap<>();
   public static final DataParameter<String> MASTER = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(110);
   public static final DataParameter<Boolean> IS_ANCHORED = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(109);
   public static final DataParameter<String> TARGET_POS = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(108);
   public static final DataParameter<Float> YAW_ROTATION = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(107);
   public static final DataParameter<String> GIRL_ID = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(106);
   public static final DataParameter<Integer> OUTFIT_INDEX = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(105);
   public static final DataParameter<String> CUR_ACTION = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(104);
   public static final DataParameter<String> GIRL_HAND_STATES = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(103);
   public static final DataParameter<String> INTERACTION_PARTNER_UUID = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(102);
   public static final DataParameter<String> WALK_SPEED = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(101);
   public static final DataParameter<String> CUSTOM_MODEL_KEY = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(100);
   public static final DataParameter<String> CUSTOM_NAME = EntityDataManager.createKey(BaseGirlEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(99);
   protected static final List<Item> TEMPTATION_ITEMS = Arrays.asList(Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.ENDER_PEARL);
   public AnimationController actionController;
   public AnimationController movementController;
   public AnimationController eyesController;
   HashMap<String, Pair<Integer, Integer>> animationVariantMap = new HashMap<>();
   AnimationProcessor<?> cachedAnimationProcessor = null;
   public List<String> boneTrackingList = new ArrayList<>();
   protected List<Entry<BoneType, Entry<List<String>, Integer>>> customPartsData = null;

   /** Sets the walk-mode state (WALK/FAST_WALK/RUN) stored in the data manager. */
   public void setWalkSpeed(BaseGirlEntity.BaseGirlEntityState state) {
      this.entityDataManager.set(WALK_SPEED, state.toString());
   }

   /** @return the current walk-mode state */
   public BaseGirlEntity.BaseGirlEntityState getWalkType() {
      return BaseGirlEntity.BaseGirlEntityState.valueOf((String)this.entityDataManager.get(WALK_SPEED));
   }

   @SideOnly(Side.CLIENT)
   protected void changeDataParameterFromClient(String key, String value) {
      PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.getGirlId(), key, value));
   }

   /** @return this girl's persistent UUID, minted on first access if unset */
   public UUID getGirlId() {
      try {
         return UUID.fromString((String)this.entityDataManager.get(GIRL_ID));
      } catch (Exception ex) {
         UUID id = UUID.randomUUID();
         this.entityDataManager.set(GIRL_ID, id.toString());
         return id;
      }
   }

   /** @return the current scene action (state stored in the data manager) */
   public Action getCurrentAction() {
      return Action.valueOf((String)this.entityDataManager.get(CUR_ACTION));
   }

   /**
    * Sets the girl's current scene action (CLIENT: routed through a
    * ChangeDataParameterPacket; SERVER: applied directly, resetting the
    * action's tick counter). ATTACK is only allowed from {@link Action#NULL}.
    * @param action the target state; null is treated as {@link Action#NULL}
    */
   public void setCurrentAction(Action action) {
      Action previousAction = this.getCurrentAction();
      SceneDebug.log(SceneDebug.ACTIONS, "setCurrentAction %s -> %s (%s, remote=%s, anchored=%s)", this.getDisplayNameText(), previousAction, action, this.world.isRemote, this.isAnchored());
      if (previousAction != action) {
         if (action != Action.ATTACK || previousAction == Action.NULL) {
            action = action == null ? Action.NULL : action;
            if (this.world.isRemote) {
               this.changeDataParameterFromClient("currentAction", action.toString());
            } else {
               previousAction.ticksPlaying = new int[]{0, 0};
               this.entityDataManager.set(CUR_ACTION, action.toString());
            }
         }
      }
   }

   /** @return the outfit/model index (0 = nude, 1 = dressed) */
   public int getOutfitIndex() {
      return (Integer)this.entityDataManager.get(OUTFIT_INDEX);
   }

   /** Sets the outfit index; on the client this is routed through a packet. */
   public void setOutfitIndex(int index) {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("currentModel", "0");
      } else {
         this.entityDataManager.set(OUTFIT_INDEX, index);
      }
   }

   public boolean isCustomType() {
      return false;
   }

   /** @return the player this girl is currently having sex with, or null */
   @Nullable
   public EntityPlayer getPlayerEntity() {
      UUID uuid = this.getInteractionPlayerUUID();
      return uuid == null ? null : this.world.getPlayerEntityByUUID(uuid);
   }

   public static void sendMessageToTrackingPlayers(BaseGirlEntity girl, String message) {
      for (EntityPlayer player : WorldUtils.getNearbyPlayers(girl)) {
         player.sendMessage(new TextComponentString(message));
      }
   }

   public static void girlPlaySound(BaseGirlEntity girl, SoundEvent sound, boolean atPlayer) {
      Vec3d pos = girl.getPositionVector();

      for (EntityPlayer player : WorldUtils.getNearbyPlayers(girl)) {
         Vec3d soundPos;
         if (!atPlayer) {
            soundPos = pos;
         } else {
            Vec3d playerPos = player.getPositionVector();
            Vec3d dir = pos.subtract(playerPos).normalize();
            soundPos = playerPos.add(dir);
         }

         ((EntityPlayerMP)player)
            .connection
            .sendPacket(new SPacketSoundEffect(sound, SoundCategory.AMBIENT, soundPos.x, soundPos.y, soundPos.z, 1.0F, 1.0F));
      }
   }

   public static void girlPlaySound(BaseGirlEntity girl, SoundEvent sound) {
      girlPlaySound(girl, sound, false);
   }

   public static void playRandomSound(BaseGirlEntity girl, SoundEvent[] sounds) {
      girlPlaySound(girl, SoundHandler.randomSound(sounds));
   }

   public static void playRandomSound(BaseGirlEntity girl, SoundEvent[] sounds, boolean atPlayer) {
      girlPlaySound(girl, SoundHandler.randomSound(sounds), atPlayer);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getVectorTowardPlayer() {
      Vec3d playerPos = Minecraft.getMinecraft().player.getPositionVector();
      Vec3d girlPos = this.getPositionVector();
      Vec3d dir = girlPos.subtract(playerPos).normalize();
      return playerPos.add(dir);
   }

   /** @return UUID of the player bound to this girl's scene, or null */
   @Nullable
   public UUID getInteractionPlayerUUID() {
      String uuidStr = (String)this.entityDataManager.get(INTERACTION_PARTNER_UUID);
      return uuidStr.equals("null") ? null : UUID.fromString(uuidStr);
   }

   public void setInteractionPlayerUUID(UUID uuid) {
      if (this.world.isRemote) {
         if (uuid == null) {
            this.changeDataParameterFromClient("playerSheHasSexWith", null);
         } else {
            this.changeDataParameterFromClient("playerSheHasSexWith", uuid.toString());
         }
      } else {
         if (uuid == null) {
            this.entityDataManager.set(INTERACTION_PARTNER_UUID, "null");
         } else {
            this.entityDataManager.set(INTERACTION_PARTNER_UUID, uuid.toString());
         }
      }
   }

   public void setInteractionPlayer(@Nonnull EntityPlayer player) {
      this.setInteractionPlayerUUID(player.getPersistentID());
   }

   /** @return the anchored target position ("0|0|0" if unset), used by the renderer/state machine */
   public Vec3d getTargetPosition() {
      String[] parts = ((String)this.entityDataManager.get(TARGET_POS)).split("\\|");
      return new Vec3d(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }

   /** Sets the target position; on the client routed through a packet. */
   public void setTargetPosition(Vec3d pos) {
      if (this.world.isRemote) {
         String formatted = pos.x + "f" + pos.y + "f" + pos.z + "f";
         this.changeDataParameterFromClient("targetPos", formatted);
      } else {
         this.entityDataManager.set(TARGET_POS, pos.x + "|" + pos.y + "|" + pos.z);
      }
   }

   public void setTargetPositionDirect(Vec3d pos) {
      this.entityDataManager.set(TARGET_POS, pos.x + "|" + pos.y + "|" + pos.z);
   }

   /** @return the anchored yaw rotation of the girl */
   public Float getYawRotation() {
      return (Float)this.entityDataManager.get(YAW_ROTATION);
   }

   public void setYawRotation(float yaw) {
      this.entityDataManager.set(YAW_ROTATION, yaw);
   }

   /** Anchors the girl to {@link #getTargetPosition()}; on the client routed through a packet. */
   public void setAnchored(boolean anchored) {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("shouldbeattargetpos", String.valueOf(anchored));
      } else {
         this.entityDataManager.set(IS_ANCHORED, anchored);
      }
   }

   /** @return true while the girl is locked to her target position/yaw */
   public boolean isAnchored() {
      return (Boolean)this.entityDataManager.get(IS_ANCHORED);
   }

   protected boolean canDespawn() {
      return false;
   }

   protected BaseGirlEntity(World world) {
      super(world);
      if (world.isRemote) {
         this.initAnimationControllers();
      }

      if (!world.isRemote || !(world instanceof SexWorldClient)) {
         PathNavigate navigator = this.getNavigator();
         if (navigator instanceof PathNavigateGround) {
            ((PathNavigateGround)navigator).setBreakDoors(true);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void initAnimationControllers() {
      this.actionController = new AnimationController<>(this, "action", 0.0F, this::animationPredicate);
      this.movementController = new AnimationController<>(this, "movement", 5.0F, this::animationPredicate);
      this.eyesController = new AnimationController<>(this, "eyes", 10.0F, this::animationPredicate);
   }

   protected void entityInit() {
      super.entityInit();
      this.pathNavigator = this.getNavigator();
      this.entityDataManager = this.getDataManager();
      this.entityDataManager.register(GIRL_ID, UUID.randomUUID().toString());
      this.entityDataManager.register(OUTFIT_INDEX, 1);
      this.entityDataManager.register(CUR_ACTION, Action.NULL.toString());
      this.entityDataManager.register(GIRL_HAND_STATES, "");
      this.entityDataManager.register(INTERACTION_PARTNER_UUID, "null");
      this.entityDataManager.register(IS_ANCHORED, false);
      this.entityDataManager.register(YAW_ROTATION, 0.0F);
      this.entityDataManager.register(TARGET_POS, "0|0|0");
      this.entityDataManager.register(MASTER, "");
      this.entityDataManager.register(WALK_SPEED, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      this.entityDataManager.register(CUSTOM_MODEL_KEY, "");
      this.entityDataManager.register(CUSTOM_NAME, "");
   }

   public void setLocallyRegistered(boolean registered) {
      this.isLocallyRegistered = registered;
      if (registered) {
         GirlRegistry.registerGirl(this);
      } else {
         GirlRegistry.unregisterGirl(this);
      }
   }

   public boolean isLocallyRegistered() {
      return this.isLocallyRegistered;
   }

   public static List<BaseGirlEntity> getGirlEntityList() {
      if (!ClientServerCheck.getInstance()) {
         return getClientGirls();
      }

      WorldServer[] worlds = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;
      if (worlds.length == 0) {
         return new ArrayList<>();
      }

      ArrayList girls = new ArrayList();

      for (WorldServer world : worlds) {
         girls.addAll(world.getEntities(BaseGirlEntity.class, entity -> true));
      }

      return girls;
   }

   @SideOnly(Side.CLIENT)
   private static List<BaseGirlEntity> getClientGirls() {
      WorldClient world = Minecraft.getMinecraft().world;
      return world == null ? new ArrayList<>() : world.getEntities(BaseGirlEntity.class, entity -> true);
   }

   public boolean canBeInteractedWith() {
      return true;
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(20.0);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(30.0);
   }

   protected void initEntityAI() {
      this.wanderGoal = new EntityAIWanderAvoidWater(this, 0.35);
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAITempt(this, 0.4, false, new HashSet<>(TEMPTATION_ITEMS)));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
      this.tasks.addTask(5, this.wanderGoal);
   }

   public void writeEntityToNBT(NBTTagCompound nbt) {
      nbt.setDouble("homeX", this.homePos.x);
      nbt.setDouble("homeY", this.homePos.y);
      nbt.setDouble("homeZ", this.homePos.z);
      nbt.setString("girlID", (String)this.entityDataManager.get(GIRL_ID));
      String customName = this.getCustomName();
      if (!"".equals(customName)) {
         nbt.setString("sexmod:customname", customName);
      }

      if (this.supportsCustomModels()) {
         nbt.setString("sexmod:customModel", this.getCustomModelCode());
      }

      super.writeEntityToNBT(nbt);
   }

   protected boolean supportsCustomModels() {
      return isValidGirl(this);
   }

   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      this.homePos = new Vec3d(nbt.getDouble("homeX"), nbt.getDouble("homeY"), nbt.getDouble("homeZ"));
      String customName = nbt.getString("sexmod:customname");
      if (!"".equals(customName)) {
         this.setCustomNameOverride(customName);
      }

      String girlId = nbt.getString("girlID");
      if (!"".equals(girlId)) {
         UUID uuid = UUID.fromString(girlId);
         boolean duped = false;

         for (BaseGirlEntity girl : girlList(uuid)) {
            if (!girl.world.isRemote && girl != this && !girl.isDead && girl.isAddedToWorld()) {
               duped = true;
               break;
            }
         }

         if (duped) {
            Main.LOGGER.log(Level.WARN, String.format("got a duped %s with id '%s'. Deleted her", this.getDisplayNameText(), uuid));
            this.world.removeEntity(this);
         } else {
            this.entityDataManager.set(GIRL_ID, uuid.toString());
            if (this.supportsCustomModels()) {
               this.setCustomModelCode(nbt.getString("sexmod:customModel"));
            }
         }
      }
   }

   public boolean shouldRenderModel() {
      return true;
   }

   /** Overrides the vanilla velocity setter (used to lock/clear girl motion). */
   public void setVelocity(double x, double y, double z) {
      this.motionX = x;
      this.motionY = y;
      this.motionZ = z;
   }

   /** Sets the entity velocity from a vector. */
   public void setVelocity(Vec3d velocity) {
      this.motionX = velocity.x;
      this.motionY = velocity.y;
      this.motionZ = velocity.z;
   }

   public Vec3d getLastTickPosVector() {
      return new Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ);
   }

   /** Holds the girl at her anchor position when anchored, then re-applies custom parts. */
   public void updateAITasks() {
      if (this.getInteractionPlayerUUID() != null || this.isAnchored()) {
         SceneDebug.log(SceneDebug.AI_TICK, "AI %s remote=%s dead=%s tick=%d anchored=%s action=%s target=%s interact=%s", this.getDisplayNameText(), this.world.isRemote, this.isDead, this.ticksExisted, this.entityDataManager.get(IS_ANCHORED), this.getCurrentAction(), this.getTargetPosition(), this.getInteractionPlayerUUID());
      } else if (this.ticksExisted % 40 == 0) {
         SceneDebug.log(SceneDebug.AI_TICK, "updateAITasks %s remote=%s anchored=%s action=%s target=%s interact=%s", this.getDisplayNameText(), this.world.isRemote, this.entityDataManager.get(IS_ANCHORED), this.getCurrentAction(), this.getTargetPosition(), this.getInteractionPlayerUUID());
      }

      if ((Boolean)this.entityDataManager.get(IS_ANCHORED)) {
         this.setRotationYawHead(this.getYawRotation());
         this.setPositionAndRotation(this.getTargetPosition().x, this.getTargetPosition().y, this.getTargetPosition().z, this.getYawRotation(), 0.0F);
         this.setRotation(this.getYawRotation(), this.rotationPitch);
      }

      if (this.homePos.equals(Vec3d.ZERO)) {
         this.homePos = new Vec3d(this.getPosition());
      }

      this.updateCustomModelParts();
   }

   /** Called every tick on both sides; advances scene follow-up transitions. */
   public void onUpdate() {
      // Scene-state heartbeat: bounded logging while a scene is active so the
      // tick continuity of the girl can be verified in the built jar.
      if (this.getInteractionPlayerUUID() != null || this.isAnchored()) {
         if (this.ticksExisted % 5 == 0) {
            SceneDebug.log(SceneDebug.HEARTBEAT, "HEARTBEAT %s remote=%s dead=%s tick=%d action=%s anchored=%s interact=%s", this.getDisplayNameText(), this.world.isRemote, this.isDead, this.ticksExisted, this.getCurrentAction(), this.isAnchored(), this.getInteractionPlayerUUID());
         }
      }
      // Defensive: older buggy builds could leave a girl with a corrupted ride
      // (stale reference to a removed entity, or a ride whose passenger chain is
      // inconsistent). Vanilla's passenger handling NPEs on such state during the
      // tick; clear it before super so old worlds load instead of crashing in
      // EntityLivingBase. This is a DISMOUNT-ONLY guard: it never removes the
      // girl. The original jar has no setDead anywhere in onUpdate — the
      // previous "harden onUpdate" attempt (be258ed) deleted girls on ANY tick
      // exception, which is what made Jenny/Bia disappear and stay gone on
      // reload. The current narrowed variant still deletes girls whenever an NPE
      // happens while the girl is riding (which is the normal state during
      // follow-mode riding and some scenes). Jar-faithful behavior = tick
      // through, never setDead.
      if (!this.world.isRemote) {
         try {
            net.minecraft.entity.Entity ride = this.getRidingEntity();
            if (ride != null && (ride.isDead || !ride.isEntityAlive())) {
               this.dismountRidingEntity();
            }
         } catch (Throwable t) {
            // corrupt chain — force-dismount via the field-clearing path
            this.dismountRidingEntity();
         }
      }
      super.onUpdate();
      this.tickFollowUpTransitions();
   }

   /**
    * Removes server-whitelisted custom parts from the girl's model code and
    * persists the filtered set (SERVER side; respects ServerWhitelistManager).
    */
   protected void updateCustomModelParts() {
      if (ServerWhitelistManager.isLoaded) {
         HashSet parts = this.getCustomPartsSet();
         NpcType type = NpcType.getNpcType(this);
         HashSet allowed = new HashSet();
         String group = ServerWhitelistManager.getCurrentGroup();

         for (String part : (java.util.Collection<String>) (parts) ) {
            if (!"".equals(ServerWhitelistManager.getPartName(part, group))) {
               allowed.add(part);
            } else {
               HashSet allowedTypes = ServerWhitelistManager.getAllowedNpcTypes(part);
               if (allowedTypes == null) {
                  allowed.add(part);
               } else if (!allowedTypes.isEmpty() && !allowedTypes.contains(type)) {
                  allowed.add(part);
               }
            }
         }

         if (!allowed.isEmpty()) {
            parts.removeAll(allowed);
            this.setCustomModelCode(encodeCustomParts(parts));
         }
      }
   }

   /**
    * Advances the scene: when the current action's tick counter reaches its
    * length, transitions to the action's follow-up (SERVER side only).
    * Jar-faithful: has NO hasPlayer reset branch (see DOCUMENTATION 2p).
    */
   protected void tickFollowUpTransitions() {
      Action action = this.getCurrentAction();
      if (++action.ticksPlaying[this.world.isRemote ? 1 : 0] >= action.length) {
         if (action.followUp != null && !this.world.isRemote) {
            SceneDebug.log(SceneDebug.ACTIONS, "tickFollowUp %s: followUp %s -> %s", this.getDisplayNameText(), action, action.followUp);
            this.setCurrentAction(action.followUp);
         }
      }
   }

   protected void tickPathVelocity() {
      Path path = this.getNavigator().getPath();
      if (path != null) {
         if (!this.onGround && !this.isInWater()) {
            int currentIndex = path.getCurrentPathIndex();
            int length = path.getCurrentPathLength();
            if (length != currentIndex && length - 1 != currentIndex) {
               PathPoint currentPoint = path.getPathPointFromIndex(currentIndex);
               PathPoint nextPoint = path.getPathPointFromIndex(currentIndex + 1);
               Vec3d delta = new Vec3d(nextPoint.x - currentPoint.x, nextPoint.y - currentPoint.y, nextPoint.z - currentPoint.z);
               this.motionX = delta.x / 7.0;
               this.motionZ = delta.z / 7.0;
            }
         }
      }
   }

   public void reinitTasks() {
   }

   @SideOnly(Side.CLIENT)
   public boolean openInteractionMenu(EntityPlayer player) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer player, BaseGirlEntity girl) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(girl, player));
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer player, BaseGirlEntity girl, String[] options, ItemStack[] rewards, boolean dressUp) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(girl, player, options, rewards, dressUp));
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer player, BaseGirlEntity girl, String[] options, boolean dressUp) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(girl, player, options, null, dressUp));
   }

   public void setHeldItemOverride(ItemStack stack) {
      this.activeItemStack = stack;
   }

   public void setItemUseCount(int count) {
      this.activeItemStackUseCount = count;
   }

   public Vec3d getPreviousPosition() {
      return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
   }

   protected static Vec3d getPreviousPosition(BaseGirlEntity girl) {
      return new Vec3d(girl.prevPosX, girl.prevPosY, girl.prevPosZ);
   }

   public BaseGirlEntity getSelf() {
      return this;
   }

   public void setDead() {
      SceneDebug.log(SceneDebug.SET_DEAD, "setDead %s remote=%s tick=%d action=%s anchored=%s interact=%s riding=%s pos=%s", this.getDisplayNameText(), this.world.isRemote, this.ticksExisted, this.getCurrentAction(), this.isAnchored(), this.getInteractionPlayerUUID(), this.getRidingEntity(), this.getPositionVector());
      super.setDead();
   }

   /** Clears the master binding and walk state back to WALK (client routed through a packet). */
   public void goHome() {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("master", "");
         this.changeDataParameterFromClient("walk speed", BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      } else {
         this.entityDataManager.set(MASTER, "");
         this.entityDataManager.set(WALK_SPEED, BaseGirlEntity.BaseGirlEntityState.WALK.toString());
      }
   }

   protected void alignPlayerToGirl(EntityPlayerMP player, boolean teleport) {
      player.motionX = 0.0;
      player.motionY = 0.0;
      player.motionZ = 0.0;
      if (teleport) {
         Vec3d offset = this.getVectorTowardPlayer(0.35);
         player.setPositionAndUpdate(offset.x, offset.y, offset.z);
      }
   }

   public void snapPlayerToPosition(UUID playerUUID) {
      EntityPlayer player = this.world.getPlayerEntityByUUID(playerUUID);
      player.motionX = 0.0;
      player.motionY = 0.0;
      player.motionZ = 0.0;
      Vec3d offset = this.getVectorTowardPlayer(0.35);
      player.setPositionAndUpdate(offset.x, offset.y, offset.z);
      this.setYawRotation(player.rotationYawHead + 180.0F);
   }

   protected void triggerActionSync(boolean flag1, boolean flag2, UUID playerUUID) {
      if (this.world.isRemote) {
         PacketHandler.networkWrapper.sendToServer(new KoboldStatePacket(this.getGirlId(), playerUUID, flag1, flag2));
      } else {
         KoboldStatePacket.Handler.sendState(this.getGirlId(), playerUUID, flag1, flag2);
      }
   }

   public static BaseGirlEntity getClientGirlEntity(UUID girlId) {
      if (girlId == null) {
         return null;
      }

      for (BaseGirlEntity girl : girlList(girlId)) {
         if (girl.world.isRemote) {
            return girl;
         }
      }

      return null;
   }

   public static BaseGirlEntity getServerGirlEntity(UUID girlId) {
      if (girlId == null) {
         return null;
      }

      for (BaseGirlEntity girl : girlList(girlId)) {
         if (!girl.world.isRemote) {
            return girl;
         }
      }

      return null;
   }

   public static ArrayList<BaseGirlEntity> girlList(UUID girlId) {
      ArrayList girls = new ArrayList();

      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (girl != null && girl.getGirlId().equals(girlId)) {
               girls.add(girl);
            }
         }
      } catch (ConcurrentModificationException ex) {
         System.out.println("had a ConcurrentModificationException while cycling through the girl list... hopefully nothin borke owo");
         ex.printStackTrace();
      }

      return girls;
   }

   protected BlockPos getNearestBed(BlockPos pos) {
      return this.findNearestBed(pos, 1);
   }

   public BlockPos findNearestBed(BlockPos origin, int radius) {
      return this.findNearestStructureBlock(origin, radius, Blocks.BED, 22, 3, null);
   }

   public void setHandActiveState() {
      this.entityDataManager.set(HAND_STATES, Byte.valueOf("1"));
   }

   public void clearHandActiveState() {
      this.entityDataManager.set(HAND_STATES, Byte.valueOf("0"));
   }

   public BlockPos findNearestStructureBlock(BlockPos origin, int requiredCount, Block targetBlock, int maxRadius, int heightRange, @Nullable HashSet<Biome> allowedBiomes) {
      int step = 1;
      byte dir = -1;
      BlockPos pos = origin;
      int found = 0;

      while (step < maxRadius) {
         for (int i = 0; i < 2; i++) {
            dir *= -1;

            for (int j = 0; j < step; j++) {
               pos = pos.add(0, 0, dir);

               for (int h = -heightRange; h < heightRange + 1; h++) {
                  if (this.world.getBlockState(pos.add(0, h, dir)).getBlock() == targetBlock) {
                     found++;
                     if (found >= requiredCount && (allowedBiomes == null || allowedBiomes.contains(this.world.getBiome(pos.add(dir, h, 0))))) {
                        return pos.add(0, h, dir);
                     }
                  }
               }
            }

            for (int k = 0; k < step; k++) {
               pos = pos.add(dir, 0, 0);

               for (int h2 = -heightRange; h2 < heightRange + 1; h2++) {
                  if (this.world.getBlockState(pos.add(dir, h2, 0)).getBlock() == targetBlock) {
                     found++;
                     if (found >= requiredCount && (allowedBiomes == null || allowedBiomes.contains(this.world.getBiome(pos.add(dir, h2, 0))))) {
                        return pos.add(dir, h2, 0);
                     }
                  }
               }
            }

            step++;
         }
      }

      return null;
   }

   protected List<BlockPos> findBlocksInRadius(BlockPos center, Class blockClass, int maxRadius, int height, @Nullable HashSet<Biome> biomes) {
      int radius = 1;
      byte step = -1;
      BlockPos base = center;
      ArrayList found = new ArrayList();

      while (radius < maxRadius) {
         for (int pass = 0; pass < 2; pass++) {
            step *= -1;

            for (int i = 0; i < radius; i++) {
               base = base.add(0, 0, step);

               for (int yOffset = -height; yOffset < height + 1; yOffset++) {
                  if (blockClass.isInstance(this.world.getBlockState(base.add(0, yOffset, step)).getBlock())
                     && (biomes == null || biomes.contains(this.world.getBiome(base.add(step, yOffset, 0))))) {
                     found.add(base.add(0, yOffset, step));
                  }
               }
            }

            for (int j = 0; j < radius; j++) {
               base = base.add(step, 0, 0);

               for (int zOffset = -height; zOffset < height + 1; zOffset++) {
                  if (blockClass.isInstance(this.world.getBlockState(base.add(step, zOffset, 0)).getBlock())
                     && (biomes == null || biomes.contains(this.world.getBiome(base.add(step, zOffset, 0))))) {
                     found.add(base.add(step, zOffset, 0));
                  }
               }
            }

            radius++;
         }
      }

      return found;
   }

   /** @return true if this girl is bound to a master player UUID */
   public boolean hasMaster() {
      return !((String)this.entityDataManager.get(MASTER)).equals("");
   }

   /** @return the master player's UUID, or null if unbounded */
   @Nullable
   public UUID getMasterUUID() {
      String masterUuid = (String)this.entityDataManager.get(MASTER);
      if ("".equals(masterUuid)) {
         return null;
      }

      try {
         return UUID.fromString(masterUuid);
      } catch (IllegalArgumentException ex) {
         return null;
      }
   }

   /** @return the master player entity, or null */
   @Nullable
   public EntityPlayer getMasterPlayer() {
      UUID masterUuid = this.getMasterUUID();
      return masterUuid == null ? null : this.world.getPlayerEntityByUUID(masterUuid);
   }

   protected ResourceLocation getLootTable() {
      return LootTableHandler.JENNY_TABLE;
   }

   @SideOnly(Side.CLIENT)
   public void doAction(String action, UUID uuid) {
   }

   @SideOnly(Side.CLIENT)
   protected abstract <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event);

   @SideOnly(Side.CLIENT)
   protected boolean handleActionAnimationOverrides(Action action, String animName, boolean started, AnimationEvent event) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected void createAnimation(String animName, boolean loop, AnimationEvent event, boolean force) {
      if (force || !Action.isActionComplete(this, event.getPartialTick()) || !this.handleActionAnimationOverrides(this.getCurrentAction(), animName, HandlePlayerMovement.isJumping, event)) {
         ILoopType.EDefaultLoopTypes loopType = loop ? ILoopType.EDefaultLoopTypes.LOOP : ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
         event.getController().setAnimation(new AnimationBuilder().addAnimation(animName, loopType));
         event.getController().transitionLengthTicks = 0.0;
      }
   }

   @SideOnly(Side.CLIENT)
   protected void createAnimation(String animName, boolean loop, AnimationEvent event) {
      this.createAnimation(animName, loop, event, false);
   }

   @SideOnly(Side.CLIENT)
   protected void playRandomizedAnimation(String animName, int maxVariants, float chance, AnimationEvent event, boolean force) {
      if (force || !Action.isActionComplete(this, event.getPartialTick()) || !this.handleActionAnimationOverrides(this.getCurrentAction(), animName, HandlePlayerMovement.isJumping, event)) {
         AnimationController controller = event.getController();
         Pair variants = this.animationVariantMap.get(animName);
         if (variants == null) {
            variants = Pair.of(0, 0);
         }

         int baseVariant = (Integer)variants.first();
         int maxVariant = (Integer)variants.second();
         if (!Action.isActionComplete(this, event.getPartialTick())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(baseVariant == 0 ? animName : animName + baseVariant, ILoopType.EDefaultLoopTypes.LOOP));
            event.getController().transitionLengthTicks = 0.0;
         } else {
            int variant = this.pickRandomVariant(baseVariant, maxVariant, maxVariants, chance);
            AnimationBuilder builder = new AnimationBuilder();
            AnimationController current = controller;
            AnimationController selectedController;
            AnimationBuilder selectedBuilder;
            String tempAnimName;
            if (variant == 0) {
               selectedController = current;
               selectedBuilder = builder;
               tempAnimName = animName;
            } else {
               selectedController = current;
               selectedBuilder = builder;
               tempAnimName = animName + variant;
            }

            selectedController.setAnimation(selectedBuilder.addAnimation(animName, ILoopType.EDefaultLoopTypes.LOOP));
            controller.transitionLengthTicks = 0.0;
            HashMap variantsMap = this.animationVariantMap;
            Integer newValue = variant;
            String newName = animName;
            HashMap currentMap = variantsMap;
            HashMap selectedMap;
            String selectedName;
            Integer selectedValue;
            int maxVar;
            if (variant == 0) {
               selectedMap = currentMap;
               selectedName = newName;
               selectedValue = newValue;
               maxVar = maxVariant;
            } else {
               selectedMap = currentMap;
               selectedName = newName;
               selectedValue = newValue;
               maxVar = variant;
            }

            selectedMap.put(selectedName, Pair.of(selectedValue, maxVar));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void playRandomizedAnimation(String animName, int maxVariants, float chance, AnimationEvent event) {
      this.playRandomizedAnimation(animName, maxVariants, chance, event, false);
   }

   int pickRandomVariant(int baseVariant, int exclude, int max, float chance) {
      if (baseVariant != 0) {
         return 0;
      }

      Random random = this.getRNG();
      if (random.nextFloat() > chance) {
         return 0;
      }

      int variant;
      do {
         variant = random.nextInt(max);
      } while ((variant == exclude || variant == 0) && max > 2);

      return variant;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public abstract void registerControllers(AnimationData data);

   /**
    * Leaves the current scene: sends ResetGirlPacket(uuid, true) on the client
    * (full reset) or resets the bound player on the server. R-Shift keybind hook.
    */
   protected void resetGirlState() {
      if (this.world.isRemote && this.isControlledByLocalPlayer()) {
         this.cameraOriginPos = null;
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId(), true));
      } else if (!this.world.isRemote) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID()));
      }
   }

   public static BaseGirlEntity getCompanionInteractingWithPlayer(EntityPlayer player) {
      return player == null ? null : getGirlByUUID(player.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public Vec3d renderCustomModelTransform(Minecraft mc, SexSceneEntity scene, EntityLivingBase entity, float partialTicks) {
      return SexSceneRenderer.getSceneEntityPosition(mc, scene, entity, this, partialTicks);
   }

   public static BaseGirlEntity getGirlByUUID(@Nonnull UUID uuid) {
      return getGirlByUUID(uuid, (Boolean)null);
   }

   public static BaseGirlEntity getGirlByUUID(@Nonnull UUID uuid, Boolean serverOnly) {
      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.isDead && uuid.equals(girl.getInteractionPlayerUUID())) {
               if (serverOnly == null) {
                  return girl;
               }

               boolean isRemote = girl.world.isRemote;
               if (isRemote && !serverOnly) {
                  return girl;
               }

               if (!isRemote && serverOnly) {
                  return girl;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
      }

      return null;
   }

   @Nullable
   public static BaseGirlEntity getActiveSceneInfo(@Nonnull UUID uuid) {
      boolean noServer = FMLCommonHandler.instance().getMinecraftServerInstance() == null;

      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.isDead) {
               boolean isRemote = girl.world.isRemote;
               if (isRemote == noServer && uuid.equals(girl.getInteractionPlayerUUID())) {
                  return girl;
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
      }

      return null;
   }

   public static BaseGirlEntity getActiveSceneInfo(@Nonnull EntityPlayer player) {
      return getActiveSceneInfo(player.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public void ac() {
   }

   public void resetCameraAndPhysics() {
      SceneDebug.log(SceneDebug.RESET, "resetCameraAndPhysics %s (remote=%s, action=%s, anchored=%s)", this.getDisplayNameText(), this.world.isRemote, this.getCurrentAction(), this.isAnchored());
      this.cameraOriginPos = null;
      this.setNoGravity(false);
      this.setCurrentAction((Action)null);
      if (this.world.isRemote) {
         this.resetLocalPlayerClientState();
      }
   }

   @SideOnly(Side.CLIENT)
   /** CLIENT: unlocks player movement, un-hides the player and tells the server to reset the girl. */
   protected void resetLocalPlayerClientState() {
      SceneDebug.log(SceneDebug.RESET, "resetLocalPlayerClientState %s (controlled=%s)", this.getDisplayNameText(), this.isControlledByLocalPlayer());
      if (this.isControlledByLocalPlayer()) {
         HandlePlayerMovement.setMovementLock(true);
         Minecraft.getMinecraft().player.setInvisible(false);
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId()));
      }
   }

   @SideOnly(Side.CLIENT)
   public static void triggerFastSexAction(UUID uuid) {
      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            UUID interactionUuid = girl.getInteractionPlayerUUID();
            if (interactionUuid != null && interactionUuid.equals(uuid)) {
               Action nextAction = girl.getNextAction(girl.getCurrentAction());
               if (nextAction == null) {
                  return;
               }

               girl.setCurrentAction(nextAction);
               return;
            }
         }
      } catch (ConcurrentModificationException ex) {
      }
   }

   @SideOnly(Side.CLIENT)
   public static void triggerCumAction(UUID uuid) {
      try {
         for (BaseGirlEntity girl : getGirlEntityList()) {
            if (!girl.isDead && girl.world.isRemote) {
               UUID interactionUuid = girl.getInteractionPlayerUUID();
               if (interactionUuid != null && interactionUuid.equals(uuid)) {
                  Action cumAction = girl.getCumAction(girl.getCurrentAction());
                  if (cumAction != null) {
                     girl.setCurrentAction(cumAction);
                  }
               }
            }
         }
      } catch (ConcurrentModificationException ex) {
      }
   }

   /** Resets the animation controller tick offset and notifies the server (ResetControllerPacket). */
   public void resetAnimationControllerOffset() {
      this.resetAnimationControllerTicks();
      PacketHandler.networkWrapper.sendToServer(new ResetControllerPacket(this.getGirlId()));
   }

   @SideOnly(Side.CLIENT)
   /** CLIENT: resets the action controller's tick offset. */
   public void resetAnimationControllerTicks() {
      this.actionController.tickOffset = 0.0;
   }

   @SideOnly(Side.CLIENT)
   @Nullable
   protected abstract Action getNextAction(Action action);

   @SideOnly(Side.CLIENT)
   protected abstract Action getCumAction(Action action);

   public TargetPoint getTargetNetworkPoint() {
      return new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 50.0);
   }

   protected void positionPlayerRelative(double dx, double dy, double dz, float yaw, float pitch) {
      if (this.getInteractionPlayerUUID() == null) {
         System.out.println("couldnt move camera because the player isn't set");
      } else {
         EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (this.cameraOriginPos == null) {
            this.cameraOriginPos = player.getPositionVector();
         }

         Vec3d newPos = this.cameraOriginPos;
         newPos = newPos.add(-Math.sin((this.cameraYaw + 90.0F) * (Math.PI / 180.0)) * dx, 0.0, Math.cos((this.cameraYaw + 90.0F) * (Math.PI / 180.0)) * dx);
         newPos = newPos.add(0.0, dy, 0.0);
         newPos = newPos.add(-Math.sin(this.cameraYaw * (Math.PI / 180.0)) * dz, 0.0, Math.cos(this.cameraYaw * (Math.PI / 180.0)) * dz);
         if (this.world.isRemote) {
            PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), newPos, this.cameraYaw + yaw, pitch));
         } else {
            player.setPositionAndRotation(newPos.x, newPos.y, newPos.z, this.cameraYaw + yaw, pitch);
            player.setPositionAndUpdate(newPos.x, newPos.y, newPos.z);
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean isControlledByLocalPlayer() {
      if (!this.world.isRemote) {
         return false;
      }

      EntityPlayerSP localPlayer = Minecraft.getMinecraft().player;
      return localPlayer.getPersistentID().equals(this.getInteractionPlayerUUID()) || localPlayer.getUniqueID().equals(this.getInteractionPlayerUUID());
   }

   protected void U() {
   }

   /** Stores a custom display name for the girl in the data manager. */
   public void setCustomNameOverride(String name) {
      this.entityDataManager.set(CUSTOM_NAME, name);
   }

   /** @return the custom display name, or "" if unset */
   public String getCustomName() {
      return (String)this.entityDataManager.get(CUSTOM_NAME);
   }

   public abstract String getDisplayNameText();

   /** @return the custom name if set, otherwise the girl's default display name */
   public String getEffectiveDisplayName() {
      String customName = (String)this.entityDataManager.get(CUSTOM_NAME);
      return !"".equals(customName) ? customName : this.getDisplayNameText();
   }

   public abstract float getScaleFactor();

   @SideOnly(Side.CLIENT)
   public boolean shouldRenderNameTag() {
      return true;
   }

   /** Broadcasts "&lt;name&gt; message" chat to nearby players (both sides). */
   public void sendGirlChatMessage(String message) {
      if (!this.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(String.format("<%s> %s", this.getEffectiveDisplayName(), message), this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else if (this.isControlledByLocalPlayer()) {
         PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(String.format("<%s> %s", this.getEffectiveDisplayName(), message), this.dimension, this.getGirlId()));
      }
   }

   /** Sends the given message as the girl; if not local, additionally calls sendGirlChatMessage. */
   protected void broadcastChatAround(String message, boolean toAll) {
      if (!toAll) {
         this.sendGirlChatMessage(message);
      }

      if (!this.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(message, this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else {
         if (this.isControlledByLocalPlayer()) {
            PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(message, this.dimension, this.getGirlId()));
         }
      }
   }

   protected void sendChatMessage(String message) {
      if (this.world.isRemote) {
         Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("<%s> %s", this.getEffectiveDisplayName(), message)));
      }
   }

   protected void sendChatMessageToPlayer(UUID uuid, String message) {
      EntityPlayer player = this.world.getPlayerEntityByUUID(uuid);
      if (player == null) {
         System.out.println("Player with UUID " + uuid.toString() + " not found");
      } else {
         if (this.world.isRemote) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("<" + player.getName() + "> " + message));
         }
      }
   }

   /** Plays a sound at the girl's position (world sound event). */
   public void playSoundAtPosition(SoundEvent sound, float volume, float pitch) {
      this.world
         .playSound(
            this.getPosition().getX(),
            this.getPosition().getY(),
            this.getPosition().getZ(),
            sound,
            SoundCategory.NEUTRAL,
            volume,
            pitch,
            false
         );
   }

   /** Plays a sound at the girl's position at full volume. */
   public void playSound(SoundEvent sound) {
      this.playSoundAtPosition(sound, 1.0F, 1.0F);
   }

   public void playRandomSound(SoundEvent[] sounds, int... indices) {
      if (indices.length == 0) {
         this.playSound(sounds[this.getRNG().nextInt(sounds.length)]);
      } else {
         this.playSoundAtPosition(sounds[indices[this.getRNG().nextInt(indices.length)]], 1.0F, 1.0F);
      }
   }

   public void playRandomSoundAtVolume(SoundEvent[] sounds, float volume) {
      this.playSoundAtPosition(sounds[this.getRNG().nextInt(sounds.length)], volume, 1.0F);
   }

   public void playSoundAtVolume(SoundEvent sound, float volume) {
      this.playSoundAtPosition(sound, volume, 1.0F);
   }

   public static boolean isValidGirl(Entity entity) {
      if (entity == null) {
         return false;
      } else {
         return !(entity instanceof BaseGirlEntity) ? false : !(entity instanceof AbstractPlayerGirlEntity);
      }
   }

   @SideOnly(Side.CLIENT)
   public BaseGirlEntity asGirl() {
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean isLocalPlayerNearby() {
      EntityPlayer player = this.world.getClosestPlayerToEntity(this, 50.0);
      return player == null ? false : player.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID());
   }

   public Vec3d getFrontOffsetVector() {
      return this.getVectorTowardPlayer(1.0);
   }

   /** @return a point {@code distance} blocks in front of the interacting player's yaw */
   public Vec3d getVectorTowardPlayer(double distance) {
      EntityPlayer player = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
      float yaw = player.rotationYaw;
      return player.getPositionVector().add(-Math.sin(yaw * (Math.PI / 180.0)) * distance, 0.0, Math.cos(yaw * (Math.PI / 180.0)) * distance);
   }

   public Vec3d transformRenderOffset(Vec3d vec, float partialTicks) {
      return vec;
   }

   public static void spawnParticlesAround(EnumParticleTypes particle, BaseGirlEntity girl) {
      double vx = Reference.RANDOM.nextGaussian() * 0.02;
      double vy = Reference.RANDOM.nextGaussian() * 0.02;
      double vz = Reference.RANDOM.nextGaussian() * 0.02;
      girl.world
         .spawnParticle(
            particle,
            girl.posX + Reference.RANDOM.nextFloat() * girl.width * 2.0F - girl.width,
            girl.posY + 0.5 + Reference.RANDOM.nextFloat() * girl.height,
            girl.posZ + Reference.RANDOM.nextFloat() * girl.width * 2.0F - girl.width,
            vx,
            vy,
            vz,
            new int[0]
         );
   }

   public static void spawnParticlesAround(EnumParticleTypes particle, BaseGirlEntity girl, int count) {
      for (int i = 0; i < count; i++) {
         spawnParticlesAround(particle, girl);
      }
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }

   public boolean canBePushed() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected SoundEvent getAmbientSound() {
      if (this.getRNG().nextInt(10000) == 0) {
         if (this.world.isRemote && Minecraft.getMinecraft().player.getPositionVector().distanceTo(this.getPositionVector()) < 10.0) {
            this.sendChatMessage("whopa");
         }

         return SoundHandler.randomSound(SoundHandler.MISC_FART);
      } else {
         return null;
      }
   }

   public float getLeftArmAngle() {
      return 0.0F;
   }

   public float getRightArmAngle() {
      return 0.0F;
   }

   @SideOnly(Side.CLIENT)
   /**
    * CLIENT: computes the world-transform MatrixStack for a named model bone
    * (walks the bone parent chain, applies the girl's yaw when anchored).
    * @return empty stack if the bone is unknown
    */
   public MatrixStack getBoneMatrixStack(String boneName, boolean usePivots) {
      if (this.cachedAnimationProcessor == null) {
         this.cachedAnimationProcessor = this.getAnimationProcessor();
      }

      IBone bone = this.cachedAnimationProcessor.getBone(boneName);
      if (bone == null) {
         if (!GirlModel.CAMERA_PLACEMENTS.contains(boneName)) {
            Main.LOGGER.log(Level.WARN, String.format("The bone '%s' does not exist on %s. Bone model matrix couldn't be calculated", boneName, this.getDisplayNameText()));
            this.boneTrackingList.remove(boneName);
         }

         return new MatrixStack();
      } else {
         GeoBone currentBone = (GeoBone)bone;
         ArrayList chain = new ArrayList();
         GeoBone bone6 = currentBone;

         while (bone6.parent != null) {
            GeoBone parent = bone6.parent;
            chain.add(parent);
            bone6 = parent;
         }

         Collections.reverse(chain);
         MatrixStack matrixStack = new MatrixStack();
         if (this.isAnchored()) {
            matrixStack.rotateY((float)(-Math.toRadians(this.getYawRotation().floatValue())));
         } else if (usePivots) {
            matrixStack.rotateY(
               (float)(-Math.toRadians(RotationHelper.lerp(this.prevRenderYawOffset, this.renderYawOffset, Minecraft.getMinecraft().getRenderPartialTicks())))
            );
         }

         for (GeoBone chainBone : (java.util.Collection<GeoBone>) (chain) ) {
            matrixStack.translate(chainBone);
            matrixStack.moveToPivot(chainBone);
            matrixStack.rotate(chainBone);
            matrixStack.scale(chainBone);
            matrixStack.moveBackFromPivot(chainBone);
         }

         matrixStack.translate(currentBone);
         matrixStack.moveToPivot(currentBone);
         matrixStack.rotate(currentBone);
         matrixStack.scale(currentBone);
         return this.applyAdditionalMatrixTransformations(matrixStack);
      }
   }

   protected MatrixStack applyAdditionalMatrixTransformations(MatrixStack stack) {
      return stack;
   }

   @SideOnly(Side.CLIENT)
   /** @return the cached world offset of a bone, or ZERO (tracking the bone) */
   public Vec3d getCachedBoneOffset(String boneName) {
      Vec3d offset = this.boneOffsetCache.get(boneName);
      if (offset != null) {
         return offset;
      }

      if (!this.boneTrackingList.contains(boneName)) {
         this.boneTrackingList.add(boneName);
      }

      return Vec3d.ZERO;
   }

   @SideOnly(Side.CLIENT)
   /** @return the girl's position plus the cached offset of the named bone */
   public Vec3d getBoneWorldPosition(String boneName) {
      return this.getCachedBoneOffset(boneName).add(this.getPositionVector());
   }

   /** Caches the world position of a bone for later rendering/positioning. */
   public void setBoneWorldPosition(String boneName, Vec3d pos) {
      this.boneOffsetCache.put(boneName, pos);
   }

   @SideOnly(Side.CLIENT)
   /** CLIENT: height of the girlCam bone in world units (for camera placement). */
   public float getCameraBoneHeight() {
      AnimationProcessor processor = this.getAnimationProcessor();
      IBone bone = processor.getBone("girlCam");
      if (bone == null) {
         return 0.0F;
      }

      float pivotY = bone.getPivotY();
      pivotY = this.transformCameraPivotY(pivotY);
      return pivotY / 16.0F;
   }

   @SideOnly(Side.CLIENT)
   public float getRenderScaleFactor() {
      return 1.0F;
   }

   protected float transformCameraPivotY(float y) {
      return y;
   }

   /** @return the AnimatedGeoModel used by this girl's renderer, or null */
   public AnimatedGeoModel<? extends BaseGirlEntity> getGeoModel() {
      Minecraft mc = Minecraft.getMinecraft();
      Render render = mc.getRenderManager().getEntityRenderObject(this);
      if (render == null) {
         return null;
      } else if (!(render instanceof GirlRenderer)) {
         return null;
      } else {
         GeoEntityRenderer renderer = (GeoEntityRenderer)render;
         GeoModelProvider provider = renderer.getGeoModelProvider();
         if (provider == null) {
            return null;
         } else {
            return !(provider instanceof AnimatedGeoModel) ? null : (AnimatedGeoModel)provider;
         }
      }
   }

   public AnimationProcessor<?> getAnimationProcessor() {
      return this.getGeoModel().getAnimationProcessor();
   }

   public boolean isPartEnabled(int index) {
      ArrayList parts = this.getCustomPartIdList();
      return parts.size() - 1 < index ? false : (Integer)parts.get(index) == 101;
   }

   public Point2D getModelPartByIndex(int index) {
      return Point2D.ZERO;
   }

   public void setCustomPartList(List<Integer> parts) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         StringBuilder builder = new StringBuilder();

         for (int partId : parts) {
            AbstractNpcOnlyEntity.appendPaddedNumber(builder, partId);
         }

         this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, builder.toString());
      }
   }

   public String getCustomPartListCode() {
      return !(this instanceof AbstractNpcOnlyEntity) && !(this instanceof AbstractKoboldPlayerEntity)
         ? ""
         : (String)this.entityDataManager.get(AbstractNpcOnlyEntity.APPEARANCE_DNA);
   }

   public static String encodePartIdList(List<Integer> parts) {
      StringBuilder sb = new StringBuilder();

      for (int part : parts) {
         sb.append(part);
         sb.append("-");
      }

      return sb.toString();
   }

   public static List<Integer> decodePartIdList(String code) {
      ArrayList ids = new ArrayList();
      String[] tokens = code.split("-");

      for (String token : tokens) {
         ids.add(Integer.parseInt(token));
      }

      return ids;
   }

   public static List<Integer> getAllPartIdsForGirl(UUID uuid) {
      BaseGirlEntity girl = Main.proxy instanceof ClientProxy ? getClientGirlEntity(uuid) : getServerGirlEntity(uuid);
      ArrayList parts = new ArrayList<>(girl.getBasePartIdList());
      if (girl instanceof AbstractNpcOnlyEntity || girl instanceof AbstractKoboldPlayerEntity) {
         parts.addAll(decodePartIdList((String)girl.getDataManager().get(AbstractNpcOnlyEntity.APPEARANCE_DNA)));
      }

      return parts;
   }

   public ArrayList<Integer> getBasePartIdList() {
      return new ArrayList<>();
   }

   public List<Entry<BoneType, Entry<List<String>, Integer>>> buildCustomPartsData(UUID uuid) {
      if (this.customPartsData != null) {
         return this.customPartsData;
      }

      ArrayList parts = this.getCustomPartIdList();
      if (parts.isEmpty()) {
         this.customPartsData = new ArrayList<>();
         return this.customPartsData;
      }

      ArrayList data = new ArrayList();
      List allParts = getAllPartIdsForGirl(uuid);

      for (int i = 0; i < parts.size(); i++) {
         data.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC, new SimpleEntry<>(this.getPartNames((Integer)parts.get(i)), allParts.get(i))));
      }

      this.customPartsData = data;
      return data;
   }

   public void setCustomPartsData(List<Entry<BoneType, Entry<List<String>, Integer>>> data) {
      this.customPartsData = data;
   }

   public void setCustomPartValue(int index, int value) {
      if (this.customPartsData != null) {
         if (this.customPartsData.size() - 1 >= index) {
            Entry entry = this.customPartsData.get(index);
            ((Entry)entry.getValue()).setValue(value);
            this.customPartsData.set(index, entry);
         }
      }
   }

   public void setCustomPartListCode(String code) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, code);
      }
   }

   private List<String> getPartNames(int count) {
      ArrayList names = new ArrayList();

      for (int i = 0; i < count; i++) {
         names.add("");
      }

      return names;
   }

   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<>();
   }

   public List<Integer> getCustomPartExtraIdList() {
      return new ArrayList<>();
   }

   public void setCustomModelCode(String modelCode) {
      this.entityDataManager.set(CUSTOM_MODEL_KEY, modelCode);
   }

   public String getCustomModelCode() {
      return (String)this.entityDataManager.get(CUSTOM_MODEL_KEY);
   }

   public static String encodeCustomParts(HashSet<String> parts) {
      if (parts == null) {
         return "";
      }

      if (parts.isEmpty()) {
         return "";
      }

      StringBuilder builder = new StringBuilder();

      for (String part : parts) {
         builder.append(part);
         builder.append("#");
      }

      return builder.toString();
   }

   public HashSet<String> getCustomPartsSet() {
      String code = this.getCustomModelCode();
      String[] parts = code.split("#");
      HashSet set = new HashSet();

      for (String part : parts) {
         if (!"".equals(part) && !"cross".equals(part)) {
            set.add(part);
         }
      }

      return set;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasCustomParts() {
      return true;
   }

   public enum BaseGirlEntityState {
      WALK,
      FAST_WALK,
      RUN;
   }
}
