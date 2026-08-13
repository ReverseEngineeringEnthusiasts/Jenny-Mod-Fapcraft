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
 * Base class for every Fapcraft girl (NPC and player-form variants).
 *
 * Holds the shared girl state via the vanilla {@link EntityDataManager}:
 * girl UUID, current {@link fp} scene action, outfit index, interaction
 * partner UUID, anchor/target position, master UUID, custom name and custom
 * model code. Owns the geckolib animation controllers (action / movement /
 * eyes) and the scene-lifecycle machinery: {@link #setCurrentAction(fp)}
 * transitions, per-tick {@link #tickFollowUpTransitions()} follow-up states,
 * {@link #resetGirlState()} / {@link ResetGirlPacket} scene exit, and the
 * client/server helpers that position the camera, play sounds, and open the
 * interaction GUI.
 *
 * Scene lifecycle: approach -> PAYMENT gate (Luna/Jenny demand items unless
 * the player is flying) -> intro -> slow/fast -> cum -> reset. See
 * DOCUMENTATION.md for the verified behavior notes.
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
   public void setWalkSpeed(BaseGirlEntity.BaseGirlEntityState var1) {
      this.entityDataManager.set(WALK_SPEED, var1.toString());
   }

   /** @return the current walk-mode state */
   public BaseGirlEntity.BaseGirlEntityState getWalkType() {
      return BaseGirlEntity.BaseGirlEntityState.valueOf((String)this.entityDataManager.get(WALK_SPEED));
   }

   @SideOnly(Side.CLIENT)
   protected void changeDataParameterFromClient(String var1, String var2) {
      PacketHandler.networkWrapper.sendToServer(new ChangeDataParameterPacket(this.getGirlId(), var1, var2));
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

   /** @return the current scene action ({@link fp} state stored in the data manager) */
   public Action getCurrentAction() {
      return Action.valueOf((String)this.entityDataManager.get(CUR_ACTION));
   }

   /**
    * Sets the girl's current scene action (CLIENT: routed through a
    * ChangeDataParameterPacket; SERVER: applied directly, resetting the
    * action's tick counter). ATTACK is only allowed from {@link fp#NULL}.
    * @param action the target state; null is treated as {@link fp#NULL}
    */
   public void setCurrentAction(Action action) {
      Action previousAction = this.getCurrentAction();
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
   public void setOutfitIndex(int var1) {
      if (this.world.isRemote) {
         this.changeDataParameterFromClient("currentModel", "0");
      } else {
         this.entityDataManager.set(OUTFIT_INDEX, var1);
      }
   }

   public boolean isCustomType() {
      return false;
   }

   /** @return the player this girl is currently having sex with, or null */
   @Nullable
   public EntityPlayer getPlayerEntity() {
      UUID var1 = this.getInteractionPlayerUUID();
      return var1 == null ? null : this.world.getPlayerEntityByUUID(var1);
   }

   public static void sendMessageToTrackingPlayers(BaseGirlEntity var0, String var1) {
      for (EntityPlayer var3 : WorldUtils.getNearbyPlayers(var0)) {
         var3.sendMessage(new TextComponentString(var1));
      }
   }

   public static void girlPlaySound(BaseGirlEntity var0, SoundEvent var1, boolean var2) {
      Vec3d var3 = var0.getPositionVector();

      for (EntityPlayer var5 : WorldUtils.getNearbyPlayers(var0)) {
         Vec3d var6;
         if (!var2) {
            var6 = var3;
         } else {
            Vec3d var7 = var5.getPositionVector();
            Vec3d var8 = var3.subtract(var7).normalize();
            var6 = var7.add(var8);
         }

         ((EntityPlayerMP)var5)
            .connection
            .sendPacket(new SPacketSoundEffect(var1, SoundCategory.AMBIENT, var6.x, var6.y, var6.z, 1.0F, 1.0F));
      }
   }

   public static void girlPlaySound(BaseGirlEntity var0, SoundEvent var1) {
      girlPlaySound(var0, var1, false);
   }

   public static void playRandomSound(BaseGirlEntity var0, SoundEvent[] var1) {
      girlPlaySound(var0, SoundHandler.randomSound(var1));
   }

   public static void playRandomSound(BaseGirlEntity var0, SoundEvent[] var1, boolean var2) {
      girlPlaySound(var0, SoundHandler.randomSound(var1), var2);
   }

   @SideOnly(Side.CLIENT)
   public Vec3d getVectorTowardPlayer() {
      Vec3d var1 = Minecraft.getMinecraft().player.getPositionVector();
      Vec3d var2 = this.getPositionVector();
      Vec3d var3 = var2.subtract(var1).normalize();
      return var1.add(var3);
   }

   /** @return UUID of the player bound to this girl's scene, or null */
   @Nullable
   public UUID getInteractionPlayerUUID() {
      String var1 = (String)this.entityDataManager.get(INTERACTION_PARTNER_UUID);
      return var1.equals("null") ? null : UUID.fromString(var1);
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

   public void setInteractionPlayer(@Nonnull EntityPlayer var1) {
      this.setInteractionPlayerUUID(var1.getPersistentID());
   }

   /** @return the anchored target position ("0|0|0" if unset), used by the renderer/state machine */
   public Vec3d getTargetPosition() {
      String[] var1 = ((String)this.entityDataManager.get(TARGET_POS)).split("\\|");
      return new Vec3d(Double.parseDouble(var1[0]), Double.parseDouble(var1[1]), Double.parseDouble(var1[2]));
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

   protected BaseGirlEntity(World var1) {
      super(var1);
      if (var1.isRemote) {
         this.initAnimationControllers();
      }

      if (!var1.isRemote || !(var1 instanceof SexWorldClient)) {
         PathNavigate var2 = this.getNavigator();
         if (var2 instanceof PathNavigateGround) {
            ((PathNavigateGround)var2).setBreakDoors(true);
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

   public void setLocallyRegistered(boolean var1) {
      this.isLocallyRegistered = var1;
      if (var1) {
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
         girls.addAll(world.getEntities(BaseGirlEntity.class, var0x -> true));
      }

      return girls;
   }

   @SideOnly(Side.CLIENT)
   private static List<BaseGirlEntity> getClientGirls() {
      WorldClient var0 = Minecraft.getMinecraft().world;
      return var0 == null ? new ArrayList<>() : var0.getEntities(BaseGirlEntity.class, var0x -> true);
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

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setDouble("homeX", this.homePos.x);
      var1.setDouble("homeY", this.homePos.y);
      var1.setDouble("homeZ", this.homePos.z);
      var1.setString("girlID", (String)this.entityDataManager.get(GIRL_ID));
      String var2 = this.getCustomName();
      if (!"".equals(var2)) {
         var1.setString("sexmod:customname", var2);
      }

      if (this.supportsCustomModels()) {
         var1.setString("sexmod:customModel", this.getCustomModelCode());
      }

      super.writeEntityToNBT(var1);
   }

   protected boolean supportsCustomModels() {
      return isValidGirl(this);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.homePos = new Vec3d(var1.getDouble("homeX"), var1.getDouble("homeY"), var1.getDouble("homeZ"));
      String var2 = var1.getString("sexmod:customname");
      if (!"".equals(var2)) {
         this.setCustomNameOverride(var2);
      }

      String var3 = var1.getString("girlID");
      if (!"".equals(var3)) {
         UUID var4 = UUID.fromString(var3);
         boolean var5 = false;

         for (BaseGirlEntity var7 : girlList(var4)) {
            if (!var7.world.isRemote && var7 != this && !var7.isDead && var7.isAddedToWorld()) {
               var5 = true;
               break;
            }
         }

         if (var5) {
            Main.LOGGER.log(Level.WARN, String.format("got a duped %s with id '%s'. Deleted her", this.getDisplayNameText(), var4));
            this.world.removeEntity(this);
         } else {
            this.entityDataManager.set(GIRL_ID, var4.toString());
            if (this.supportsCustomModels()) {
               this.setCustomModelCode(var1.getString("sexmod:customModel"));
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
      super.onUpdate();
      this.tickFollowUpTransitions();
   }

   /**
    * Removes server-whitelisted custom parts from the girl's model code and
    * persists the filtered set (SERVER side; respects ServerWhitelistManager).
    */
   protected void updateCustomModelParts() {
      if (ServerWhitelistManager.isLoaded) {
         HashSet var1 = this.getCustomPartsSet();
         NpcType var2 = NpcType.getNpcType(this);
         HashSet var3 = new HashSet();
         String var4 = ServerWhitelistManager.getCurrentGroup();

         for (String var6 : (java.util.Collection<String>) (var1) ) {
            if (!"".equals(ServerWhitelistManager.getPartName(var6, var4))) {
               var3.add(var6);
            } else {
               HashSet var7 = ServerWhitelistManager.getAllowedNpcTypes(var6);
               if (var7 == null) {
                  var3.add(var6);
               } else if (!var7.isEmpty() && !var7.contains(var2)) {
                  var3.add(var6);
               }
            }
         }

         if (!var3.isEmpty()) {
            var1.removeAll(var3);
            this.setCustomModelCode(encodeCustomParts(var1));
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
            this.setCurrentAction(action.followUp);
         }
      }
   }

   protected void tickPathVelocity() {
      Path var1 = this.getNavigator().getPath();
      if (var1 != null) {
         if (!this.onGround && !this.isInWater()) {
            int var2 = var1.getCurrentPathIndex();
            int var3 = var1.getCurrentPathLength();
            if (var3 != var2 && var3 - 1 != var2) {
               PathPoint var4 = var1.getPathPointFromIndex(var2);
               PathPoint var5 = var1.getPathPointFromIndex(var2 + 1);
               Vec3d var6 = new Vec3d(var5.x - var4.x, var5.y - var4.y, var5.z - var4.z);
               this.motionX = var6.x / 7.0;
               this.motionZ = var6.z / 7.0;
            }
         }
      }
   }

   public void reinitTasks() {
   }

   @SideOnly(Side.CLIENT)
   public boolean openInteractionMenu(EntityPlayer var1) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer var0, BaseGirlEntity var1) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0));
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer var0, BaseGirlEntity var1, String[] var2, ItemStack[] var3, boolean var4) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0, var2, var3, var4));
   }

   @SideOnly(Side.CLIENT)
   protected static void openInventoryGui(EntityPlayer var0, BaseGirlEntity var1, String[] var2, boolean var3) {
      Minecraft.getMinecraft().displayGuiScreen(new GirlInventoryScreen(var1, var0, var2, null, var3));
   }

   public void setHeldItemOverride(ItemStack var1) {
      this.activeItemStack = var1;
   }

   public void setItemUseCount(int var1) {
      this.activeItemStackUseCount = var1;
   }

   public Vec3d getPreviousPosition() {
      return new Vec3d(this.prevPosX, this.prevPosY, this.prevPosZ);
   }

   protected static Vec3d getPreviousPosition(BaseGirlEntity var0) {
      return new Vec3d(var0.prevPosX, var0.prevPosY, var0.prevPosZ);
   }

   public BaseGirlEntity getSelf() {
      return this;
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
         KoboldStatePacket.Handler.a(this.getGirlId(), playerUUID, flag1, flag2);
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

   protected BlockPos getNearestBed(BlockPos var1) {
      return this.findNearestBed(var1, 1);
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

   protected List<BlockPos> findBlocksInRadius(BlockPos var1, Class var2, int var3, int var4, @Nullable HashSet<Biome> var5) {
      int var6 = 1;
      byte var7 = -1;
      BlockPos var8 = var1;
      ArrayList var9 = new ArrayList();

      while (var6 < var3) {
         for (int var10 = 0; var10 < 2; var10++) {
            var7 *= -1;

            for (int var11 = 0; var11 < var6; var11++) {
               var8 = var8.add(0, 0, var7);

               for (int var12 = -var4; var12 < var4 + 1; var12++) {
                  if (var2.isInstance(this.world.getBlockState(var8.add(0, var12, var7)).getBlock())
                     && (var5 == null || var5.contains(this.world.getBiome(var8.add(var7, var12, 0))))) {
                     var9.add(var8.add(0, var12, var7));
                  }
               }
            }

            for (int var13 = 0; var13 < var6; var13++) {
               var8 = var8.add(var7, 0, 0);

               for (int var14 = -var4; var14 < var4 + 1; var14++) {
                  if (var2.isInstance(this.world.getBlockState(var8.add(var7, var14, 0)).getBlock())
                     && (var5 == null || var5.contains(this.world.getBiome(var8.add(var7, var14, 0))))) {
                     var9.add(var8.add(var7, var14, 0));
                  }
               }
            }

            var6++;
         }
      }

      return var9;
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
      } catch (IllegalArgumentException var2) {
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
   public void doAction(String var1, UUID var2) {
   }

   @SideOnly(Side.CLIENT)
   protected abstract <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1);

   @SideOnly(Side.CLIENT)
   protected boolean handleActionAnimationOverrides(Action var1, String var2, boolean var3, AnimationEvent var4) {
      return false;
   }

   @SideOnly(Side.CLIENT)
   protected void createAnimation(String var1, boolean var2, AnimationEvent var3, boolean var4) {
      if (var4 || !Action.isActionComplete(this, var3.getPartialTick()) || !this.handleActionAnimationOverrides(this.getCurrentAction(), var1, HandlePlayerMovement.isJumping, var3)) {
         ILoopType.EDefaultLoopTypes var5 = var2 ? ILoopType.EDefaultLoopTypes.LOOP : ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME;
         var3.getController().setAnimation(new AnimationBuilder().addAnimation(var1, var5));
         var3.getController().transitionLengthTicks = 0.0;
      }
   }

   @SideOnly(Side.CLIENT)
   protected void createAnimation(String var1, boolean var2, AnimationEvent var3) {
      this.createAnimation(var1, var2, var3, false);
   }

   @SideOnly(Side.CLIENT)
   protected void playRandomizedAnimation(String var1, int var2, float var3, AnimationEvent var4, boolean var5) {
      if (var5 || !Action.isActionComplete(this, var4.getPartialTick()) || !this.handleActionAnimationOverrides(this.getCurrentAction(), var1, HandlePlayerMovement.isJumping, var4)) {
         AnimationController var6 = var4.getController();
         Pair var7 = this.animationVariantMap.get(var1);
         if (var7 == null) {
            var7 = Pair.of(0, 0);
         }

         int var8 = (Integer)var7.first();
         int var9 = (Integer)var7.second();
         if (!Action.isActionComplete(this, var4.getPartialTick())) {
            var4.getController().setAnimation(new AnimationBuilder().addAnimation(var8 == 0 ? var1 : var1 + var8, ILoopType.EDefaultLoopTypes.LOOP));
            var4.getController().transitionLengthTicks = 0.0;
         } else {
            int var10 = this.pickRandomVariant(var8, var9, var2, var3);
            AnimationBuilder var12 = new AnimationBuilder();
            AnimationController var11 = var6;
            AnimationController var10000;
            AnimationBuilder var10001;
            String var10002;
            if (var10 == 0) {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1;
            } else {
               var10000 = var11;
               var10001 = var12;
               var10002 = var1 + var10;
            }

            var10000.setAnimation(var10001.addAnimation(var10002, ILoopType.EDefaultLoopTypes.LOOP));
            var6.transitionLengthTicks = 0.0;
            HashMap var16 = this.animationVariantMap;
            Integer var15 = var10;
            String var14 = var1;
            HashMap var13 = var16;
            HashMap var17;
            String var18;
            Integer var19;
            int var10003;
            if (var10 == 0) {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var9;
            } else {
               var17 = var13;
               var18 = var14;
               var19 = var15;
               var10003 = var10;
            }

            var17.put(var18, Pair.of(var19, var10003));
         }
      }
   }

   @SideOnly(Side.CLIENT)
   protected void playRandomizedAnimation(String var1, int var2, float var3, AnimationEvent var4) {
      this.playRandomizedAnimation(var1, var2, var3, var4, false);
   }

   int pickRandomVariant(int var1, int var2, int var3, float var4) {
      if (var1 != 0) {
         return 0;
      }

      Random var5 = this.getRNG();
      if (var5.nextFloat() > var4) {
         return 0;
      }

      int var6;
      do {
         var6 = var5.nextInt(var3);
      } while ((var6 == var2 || var6 == 0) && var3 > 2);

      return var6;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public abstract void registerControllers(AnimationData var1);

   /**
    * Leaves the current scene: sends ResetGirlPacket(uuid, true) on the client
    * (full reset) or resets the bound player on the server. R-Shift keybind hook.
    */
   protected void resetGirlState() {
      if (this.world.isRemote && this.isControlledByLocalPlayer()) {
         this.cameraOriginPos = null;
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId(), true));
      } else if (!this.world.isRemote) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID()));
      }
   }

   public static BaseGirlEntity getCompanionInteractingWithPlayer(EntityPlayer var0) {
      return var0 == null ? null : getGirlByUUID(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public Vec3d renderCustomModelTransform(Minecraft var1, SexSceneEntity var2, EntityLivingBase var3, float var4) {
      return SexSceneRenderer.a(var1, var2, var3, this, var4);
   }

   public static BaseGirlEntity getGirlByUUID(@Nonnull UUID var0) {
      return getGirlByUUID(var0, (Boolean)null);
   }

   public static BaseGirlEntity getGirlByUUID(@Nonnull UUID var0, Boolean var1) {
      try {
         for (BaseGirlEntity var3 : getGirlEntityList()) {
            if (!var3.isDead && var0.equals(var3.getInteractionPlayerUUID())) {
               if (var1 == null) {
                  return var3;
               }

               boolean var4 = var3.world.isRemote;
               if (var4 && !var1) {
                  return var3;
               }

               if (!var4 && var1) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   @Nullable
   public static BaseGirlEntity getActiveSceneInfo(@Nonnull UUID var0) {
      boolean var1 = FMLCommonHandler.instance().getMinecraftServerInstance() == null;

      try {
         for (BaseGirlEntity var3 : getGirlEntityList()) {
            if (!var3.isDead) {
               boolean var4 = var3.world.isRemote;
               if (var4 == var1 && var0.equals(var3.getInteractionPlayerUUID())) {
                  return var3;
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
      }

      return null;
   }

   public static BaseGirlEntity getActiveSceneInfo(@Nonnull EntityPlayer var0) {
      return getActiveSceneInfo(var0.getPersistentID());
   }

   @SideOnly(Side.CLIENT)
   public void ac() {
   }

   public void resetCameraAndPhysics() {
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
      if (this.isControlledByLocalPlayer()) {
         HandlePlayerMovement.setMovementLock(true);
         Minecraft.getMinecraft().player.setInvisible(false);
         PacketHandler.networkWrapper.sendToServer(new ResetGirlPacket(this.getGirlId()));
      }
   }

   @SideOnly(Side.CLIENT)
   public static void triggerFastSexAction(UUID var0) {
      try {
         for (BaseGirlEntity var2 : getGirlEntityList()) {
            UUID var3 = var2.getInteractionPlayerUUID();
            if (var3 != null && var3.equals(var0)) {
               Action var4 = var2.getNextAction(var2.getCurrentAction());
               if (var4 == null) {
                  return;
               }

               var2.setCurrentAction(var4);
               return;
            }
         }
      } catch (ConcurrentModificationException var5) {
      }
   }

   @SideOnly(Side.CLIENT)
   public static void triggerCumAction(UUID var0) {
      try {
         for (BaseGirlEntity var2 : getGirlEntityList()) {
            if (!var2.isDead && var2.world.isRemote) {
               UUID var3 = var2.getInteractionPlayerUUID();
               if (var3 != null && var3.equals(var0)) {
                  Action var4 = var2.getCumAction(var2.getCurrentAction());
                  if (var4 != null) {
                     var2.setCurrentAction(var4);
                  }
               }
            }
         }
      } catch (ConcurrentModificationException var5) {
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
   protected abstract Action getNextAction(Action var1);

   @SideOnly(Side.CLIENT)
   protected abstract Action getCumAction(Action var1);

   public TargetPoint getTargetNetworkPoint() {
      return new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 50.0);
   }

   protected void positionPlayerRelative(double var1, double var3, double var5, float var7, float var8) {
      if (this.getInteractionPlayerUUID() == null) {
         System.out.println("couldnt move camera because the player isn't set");
      } else {
         EntityPlayer var9 = this.world.getPlayerEntityByUUID(this.getInteractionPlayerUUID());
         if (this.cameraOriginPos == null) {
            this.cameraOriginPos = var9.getPositionVector();
         }

         Vec3d var10 = this.cameraOriginPos;
         var10 = var10.add(-Math.sin((this.cameraYaw + 90.0F) * (Math.PI / 180.0)) * var1, 0.0, Math.cos((this.cameraYaw + 90.0F) * (Math.PI / 180.0)) * var1);
         var10 = var10.add(0.0, var3, 0.0);
         var10 = var10.add(-Math.sin(this.cameraYaw * (Math.PI / 180.0)) * var5, 0.0, Math.cos(this.cameraYaw * (Math.PI / 180.0)) * var5);
         if (this.world.isRemote) {
            PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(var9.getPersistentID().toString(), var10, this.cameraYaw + var7, var8));
         } else {
            var9.setPositionAndRotation(var10.x, var10.y, var10.z, this.cameraYaw + var7, var8);
            var9.setPositionAndUpdate(var10.x, var10.y, var10.z);
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
   public void sendGirlChatMessage(String var1) {
      if (!this.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(String.format("<%s> %s", this.getEffectiveDisplayName(), var1), this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else if (this.isControlledByLocalPlayer()) {
         PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(String.format("<%s> %s", this.getEffectiveDisplayName(), var1), this.dimension, this.getGirlId()));
      }
   }

   /** Sends the given message as the girl; if not local, additionally calls sendGirlChatMessage. */
   protected void broadcastChatAround(String var1, boolean var2) {
      if (!var2) {
         this.sendGirlChatMessage(var1);
      }

      if (!this.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(var1, this.dimension, this.getGirlId()),
               new TargetPoint(this.dimension, this.posX, this.posY, this.posZ, 40.0)
            );
      } else {
         if (this.isControlledByLocalPlayer()) {
            PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(var1, this.dimension, this.getGirlId()));
         }
      }
   }

   protected void sendChatMessage(String var1) {
      if (this.world.isRemote) {
         Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("<%s> %s", this.getEffectiveDisplayName(), var1)));
      }
   }

   protected void sendChatMessageToPlayer(UUID var1, String var2) {
      EntityPlayer var3 = this.world.getPlayerEntityByUUID(var1);
      if (var3 == null) {
         System.out.println("Player with UUID " + var1.toString() + " not found");
      } else {
         if (this.world.isRemote) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("<" + var3.getName() + "> " + var2));
         }
      }
   }

   /** Plays a sound at the girl's position (world sound event). */
   public void playSoundAtPosition(SoundEvent var1, float var2, float var3) {
      this.world
         .playSound(
            this.getPosition().getX(),
            this.getPosition().getY(),
            this.getPosition().getZ(),
            var1,
            SoundCategory.NEUTRAL,
            var2,
            var3,
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

   public void playRandomSoundAtVolume(SoundEvent[] var1, float var2) {
      this.playSoundAtPosition(var1[this.getRNG().nextInt(var1.length)], var2, 1.0F);
   }

   public void playSoundAtVolume(SoundEvent sound, float volume) {
      this.playSoundAtPosition(sound, volume, 1.0F);
   }

   public static boolean isValidGirl(Entity var0) {
      if (var0 == null) {
         return false;
      } else {
         return !(var0 instanceof BaseGirlEntity) ? false : !(var0 instanceof AbstractPlayerGirlEntity);
      }
   }

   @SideOnly(Side.CLIENT)
   public BaseGirlEntity asGirl() {
      return this;
   }

   @SideOnly(Side.CLIENT)
   public boolean isLocalPlayerNearby() {
      EntityPlayer var1 = this.world.getClosestPlayerToEntity(this, 50.0);
      return var1 == null ? false : var1.getPersistentID().equals(Minecraft.getMinecraft().player.getPersistentID());
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

   public Vec3d transformRenderOffset(Vec3d var1, float var2) {
      return var1;
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
   public MatrixStack getBoneMatrixStack(String var1, boolean var2) {
      if (this.cachedAnimationProcessor == null) {
         this.cachedAnimationProcessor = this.getAnimationProcessor();
      }

      IBone var3 = this.cachedAnimationProcessor.getBone(var1);
      if (var3 == null) {
         if (!GirlModel.CAMERA_PLACEMENTS.contains(var1)) {
            Main.LOGGER.log(Level.WARN, String.format("The bone '%s' does not exist on %s. Bone model matrix couldn't be calculated", var1, this.getDisplayNameText()));
            this.boneTrackingList.remove(var1);
         }

         return new MatrixStack();
      } else {
         GeoBone var4 = (GeoBone)var3;
         ArrayList var5 = new ArrayList();
         GeoBone var6 = var4;

         while (var6.parent != null) {
            GeoBone var7 = var6.parent;
            var5.add(var7);
            var6 = var7;
         }

         Collections.reverse(var5);
         MatrixStack var9 = new MatrixStack();
         if (this.isAnchored()) {
            var9.rotateY((float)(-Math.toRadians(this.getYawRotation().floatValue())));
         } else if (var2) {
            var9.rotateY(
               (float)(-Math.toRadians(RotationHelper.lerp(this.prevRenderYawOffset, this.renderYawOffset, Minecraft.getMinecraft().getRenderPartialTicks())))
            );
         }

         for (GeoBone var8 : (java.util.Collection<GeoBone>) (var5) ) {
            var9.translate(var8);
            var9.moveToPivot(var8);
            var9.rotate(var8);
            var9.scale(var8);
            var9.moveBackFromPivot(var8);
         }

         var9.translate(var4);
         var9.moveToPivot(var4);
         var9.rotate(var4);
         var9.scale(var4);
         return this.applyAdditionalMatrixTransformations(var9);
      }
   }

   protected MatrixStack applyAdditionalMatrixTransformations(MatrixStack var1) {
      return var1;
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
      AnimationProcessor var1 = this.getAnimationProcessor();
      IBone var2 = var1.getBone("girlCam");
      if (var2 == null) {
         return 0.0F;
      }

      float var3 = var2.getPivotY();
      var3 = this.transformCameraPivotY(var3);
      return var3 / 16.0F;
   }

   @SideOnly(Side.CLIENT)
   public float getRenderScaleFactor() {
      return 1.0F;
   }

   protected float transformCameraPivotY(float var1) {
      return var1;
   }

   /** @return the AnimatedGeoModel used by this girl's renderer, or null */
   public AnimatedGeoModel<? extends BaseGirlEntity> getGeoModel() {
      Minecraft var1 = Minecraft.getMinecraft();
      Render var2 = var1.getRenderManager().getEntityRenderObject(this);
      if (var2 == null) {
         return null;
      } else if (!(var2 instanceof GirlRenderer)) {
         return null;
      } else {
         GeoEntityRenderer var3 = (GeoEntityRenderer)var2;
         GeoModelProvider var4 = var3.getGeoModelProvider();
         if (var4 == null) {
            return null;
         } else {
            return !(var4 instanceof AnimatedGeoModel) ? null : (AnimatedGeoModel)var4;
         }
      }
   }

   public AnimationProcessor<?> getAnimationProcessor() {
      return this.getGeoModel().getAnimationProcessor();
   }

   public boolean h(int var1) {
      ArrayList var2 = this.getCustomPartIdList();
      return var2.size() - 1 < var1 ? false : (Integer)var2.get(var1) == 101;
   }

   public Point2D g(int var1) {
      return Point2D.ZERO;
   }

   public void setCustomPartList(List<Integer> var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         StringBuilder var2 = new StringBuilder();

         for (int var4 : var1) {
            AbstractNpcOnlyEntity.c(var2, var4);
         }

         this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, var2.toString());
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

   public static List<Integer> getAllPartIdsForGirl(UUID var0) {
      BaseGirlEntity var1 = null instanceof ClientProxy ? getClientGirlEntity(var0) : getServerGirlEntity(var0);
      ArrayList var2 = new ArrayList<>(var1.getBasePartIdList());
      if (var1 instanceof AbstractNpcOnlyEntity || var1 instanceof AbstractKoboldPlayerEntity) {
         var2.addAll(decodePartIdList((String)var1.getDataManager().get(AbstractNpcOnlyEntity.APPEARANCE_DNA)));
      }

      return var2;
   }

   public ArrayList<Integer> getBasePartIdList() {
      return new ArrayList<>();
   }

   public List<Entry<BoneType, Entry<List<String>, Integer>>> buildCustomPartsData(UUID var1) {
      if (this.customPartsData != null) {
         return this.customPartsData;
      }

      ArrayList var2 = this.getCustomPartIdList();
      if (var2.isEmpty()) {
         this.customPartsData = new ArrayList<>();
         return this.customPartsData;
      }

      ArrayList var3 = new ArrayList();
      List var4 = getAllPartIdsForGirl(var1);

      for (int var5 = 0; var5 < var2.size(); var5++) {
         var3.add(new SimpleEntry<>(BoneType.GIRL_SPECIFIC, new SimpleEntry<>(this.e((Integer)var2.get(var5)), var4.get(var5))));
      }

      this.customPartsData = var3;
      return var3;
   }

   public void setCustomPartsData(List<Entry<BoneType, Entry<List<String>, Integer>>> var1) {
      this.customPartsData = var1;
   }

   public void setCustomPartValue(int var1, int var2) {
      if (this.customPartsData != null) {
         if (this.customPartsData.size() - 1 >= var1) {
            Entry var3 = this.customPartsData.get(var1);
            ((Entry)var3.getValue()).setValue(var2);
            this.customPartsData.set(var1, var3);
         }
      }
   }

   public void setCustomPartListCode(String var1) {
      if (this instanceof AbstractNpcOnlyEntity || this instanceof AbstractKoboldPlayerEntity) {
         this.entityDataManager.set(AbstractNpcOnlyEntity.APPEARANCE_DNA, var1);
      }
   }

   private List<String> e(int var1) {
      ArrayList var2 = new ArrayList();

      for (int var3 = 0; var3 < var1; var3++) {
         var2.add("");
      }

      return var2;
   }

   public ArrayList<Integer> getCustomPartIdList() {
      return new ArrayList<>();
   }

   public List<Integer> getCustomPartExtraIdList() {
      return new ArrayList<>();
   }

   public void setCustomModelCode(String var1) {
      this.entityDataManager.set(CUSTOM_MODEL_KEY, var1);
   }

   public String getCustomModelCode() {
      return (String)this.entityDataManager.get(CUSTOM_MODEL_KEY);
   }

   public static String encodeCustomParts(HashSet<String> var0) {
      if (var0 == null) {
         return "";
      }

      if (var0.isEmpty()) {
         return "";
      }

      StringBuilder var1 = new StringBuilder();

      for (String var3 : var0) {
         var1.append(var3);
         var1.append("#");
      }

      return var1.toString();
   }

   public HashSet<String> getCustomPartsSet() {
      String var1 = this.getCustomModelCode();
      String[] var2 = var1.split("#");
      HashSet var3 = new HashSet();

      for (String var7 : var2) {
         if (!"".equals(var7) && !"cross".equals(var7)) {
            var3.add(var7);
         }
      }

      return var3;
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
