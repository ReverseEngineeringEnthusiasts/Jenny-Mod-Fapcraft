package com.trolmastercard.sexmod.common.entity.galath;

import com.trolmastercard.sexmod.client.event.PlayerMovementController;
import com.trolmastercard.sexmod.client.gui.hud.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.hud.HornyMeterHud;
import com.trolmastercard.sexmod.client.gui.screen.SceneFadeScreen;
import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
import com.trolmastercard.sexmod.client.render.effects.CumTrailOverlay;
import com.trolmastercard.sexmod.client.render.effects.DynamicTrailRenderer;
import com.trolmastercard.sexmod.common.combat.GalathDamageSource;
import com.trolmastercard.sexmod.common.combat.SuccubusDamageSource;
import com.trolmastercard.sexmod.common.command.CommandFuta;
import com.trolmastercard.sexmod.common.entity.ai.MobPredicates;
import com.trolmastercard.sexmod.common.entity.ai.PathUtils;
import com.trolmastercard.sexmod.common.entity.base.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.common.util.SideCheck;
import com.trolmastercard.sexmod.network.galath.GalathPouncePacket;
import com.trolmastercard.sexmod.network.galath.RequestGalathRidePacket;
import com.trolmastercard.sexmod.network.scene.SetGirlVelocityPacket;
import com.trolmastercard.sexmod.network.scene.TeleportPlayerPacket;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.WorldServer;
import software.bernie.geckolib3.core.event.SoundKeyframeEvent;

import com.trolmastercard.sexmod.common.entity.base.Action;
import com.trolmastercard.sexmod.common.entity.base.BaseGirlEntity;
import com.trolmastercard.sexmod.common.entity.base.NpcType;
import com.trolmastercard.sexmod.common.entity.galath.GalathEnergyBallPart;
import com.trolmastercard.sexmod.common.entity.girl.ManglelieEntity;
import com.trolmastercard.sexmod.common.entity.galath.DragonChargeEntity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.hud.GalathFlightHud;
import com.trolmastercard.sexmod.client.model.base.GirlAnimationController;
import com.trolmastercard.sexmod.client.render.entity.WildSlimeFaceLayer;
import com.trolmastercard.sexmod.common.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.common.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.api.IGalath;
import com.trolmastercard.sexmod.common.item.GalathCoinItem;
import com.trolmastercard.sexmod.network.PacketHandler;
import com.trolmastercard.sexmod.network.scene.ResetGirlPacket;
import com.trolmastercard.sexmod.network.scene.SetSceneCameraPacket;
import com.trolmastercard.sexmod.network.scene.LockPlayerMovementPacket;
import com.trolmastercard.sexmod.network.galath.DragonBreathParticlesPacket;
import com.trolmastercard.sexmod.network.galath.GalathSummonParticlesPacket;
import com.trolmastercard.sexmod.common.world.data.AllieWorldData;
import com.trolmastercard.sexmod.common.world.data.BeeWorldData;
import com.trolmastercard.sexmod.common.event.ForgeEventHandler;
import com.trolmastercard.sexmod.common.world.data.OwnershipSavedData;
import com.trolmastercard.sexmod.common.world.data.CustomModelCodeData;
import com.trolmastercard.sexmod.common.entity.kobold.KoboldTask;
import com.trolmastercard.sexmod.common.util.math.RotationHelper;
import com.trolmastercard.sexmod.registry.SoundHandler;
import com.trolmastercard.sexmod.common.util.ThreadNames;
import com.trolmastercard.sexmod.common.world.WorldUtils;
import com.trolmastercard.sexmod.common.util.math.VectorMath;
import com.trolmastercard.sexmod.common.util.math.Vector4d;
import com.trolmastercard.sexmod.common.util.math.MutableVector3f;
import com.trolmastercard.sexmod.common.util.math.TrigMath;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockTorch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityMultiPart;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.BossInfo.Color;
import net.minecraft.world.BossInfo.Overlay;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent.CheckSpawn;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Galath — the flying succubus boss girl (implements
 * {@link IEntityMultiPart}, {@link IGalath}). Wild Galaths spawn near wither
 * skeleton/blaze hives, fly via the {@link GalathFlightData} state machine and
 * attack players/mobs with dragon charges ({@link DragonEntity}), sword
 * attacks and a rape pounce. Defeating her (knockout) lets a player "corrupt"
 * her; once corrupted she grants a {@link GalathCoinItem} and becomes the
 * player's bound succubus with cowgirl/anal/threesome/morning-blowjob scenes
 * and the Manglelie partnership.
 * <p>
 * <b>State.</b> Own data keys (do not reorder): {@code TARGET_ENTITY_ID} (111)
 * = target entity id, {@code ATTACK_PROGRESS} (112) = attack/sword progress,
 * {@code LEFT_ENERGY_BALL_ALIVE}/{@code RIGHT_ENERGY_BALL_ALIVE} (113/114) =
 * energy-ball alive flags, {@code MIRROR_SIDE} (115) = mirror side,
 * {@code SWORD_ATTACK_PROGRESS} (116) = sword attack progress,
 * {@code FLIGHT_TARGET_POS} (117) = flight target pos ("x|y|z"),
 * {@code PARALYZED} (118) = paralyzed flag, {@code RAPE_PROGRESS} (119) = rape
 * progress, {@code HIDE_EFFECTS_FLAG} (120) = knock-out state,
 * {@code WildSlimeFaceLayer} (121) = Manglelie partner UUID,
 * {@code SPRINTING} (122) = sprint flag. The two {@link SexEntityPart}
 * hitboxes ({@code energyBallHitboxLeft}, {@code energyBallHitboxRight}) back
 * the energy-ball attack.
 * <p>
 * <b>Flow.</b> Wild: {@code updateAITasks} -&gt; {@link #tickFlightSystems()} drives the
 * flight state machine ({@link GalathEntity#initFlightData(GalathEntity)} picks a random
 * {@link GalathFlightData} action when a target is in range, see
 * {@link GalathEntity#acquireFlightTarget(GalathEntity)});
 * {@link #setFlightVelocity(Vec3d)} (dragon hit)
 * sends her into {@link Action#KNOCK_OUT_FLY} -&gt; KNOCK_OUT_GROUND
 * ({@link GalathEntity#handleKnockout(GalathEntity)}) -&gt;
 * KNOCK_OUT_STAND_UP. The corrupting player
 * then right-clicks ({@link GalathEntity#processGirlInteract(GalathEntity, EntityPlayer, EnumHand)})
 * which starts {@link Action#CORRUPT_INTRO}; {@code handleCorruptCum} (server)
 * advances CORRUPT_CUM -&gt; {@link Action#GIVE_COIN} -&gt;
 * {@link #grantCoinAndBindMaster()} grants the coin and binds the master.
 * Tamed: scenes enter through
 * {@link GalathEntity#processMasterInteract(GalathEntity, EntityPlayer, EnumHand)} +
 * {@link GalathEntity#doAction(GalathEntity, String, UUID)}; {@link #handleRapeState()} and
 * {@link GalathEntity#handleRapeCum(GalathEntity)}/
 * {@link GalathEntity#endTamedCorruptScene(GalathEntity)} end rape/corrupt
 * scenes
 * (player reset via {@link SetPlayerMovementPacket}).
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} blocks transitions out
 * of GALATH_DE_SUMMON, guards all cum loops, persists cum time via
 * {@link GirlSavedData} and fires the GIVE_COIN/HUG_MANG/MORNING_BLOWJOB_CUM
 * teardown hooks. The boss bar ({@code bossBar}) is only visible for wild Galaths
 * ({@link #tickWildAi()}); {@code despawned} girls only update the Manglelie world
 * data. {@link #getAimYaw(GalathEntity, float)} mutates the render yaw — many
 * systems depend on it. The energy-ball hitboxes must be active exactly while
 * {@code summonSkeletonTicks} is in [9, 30] (see {@link #resetEnergyBalls()}).
 */
public class GalathEntity extends BaseGirlEntity implements IEntityMultiPart, IGalath {
   public static final float a2 = 0.6F;
   public static final float b6 = 0.6F;
   public static final int bj = 10;
   public static final int an = 20;
   public static final float aU = 50.0F;
   public static final float ba = 40.0F;
   public static final int bM = 5;
   public static final int KoboldTask = 25;
   public static final float bJ = 30.0F;
   public static final float aA = 3.0F;
   public static final int a3 = 23;
   public static final int MAX_TICKS_45 = 45;
   public static final float ca = 0.3F;
   public static final float a8 = 9.0F;
   public static final float aX = 30.0F;
   public static final int bE = 24;
   public static final int aQ = 32;
   public static final int av = 5;
   public static final int bQ = 36;
   public static final int aR = 40;
   public static final int aB = 54;
   public static final int by = 10;
   public static final float b_ = 0.25F;
   public static final double ax = 3.0;
   public static final double bF = 1.0;
   public static final double bv = 1.5;
   public static final double az = 0.3F;
   public static final double ag = 40.0;
   public static final double au = 5.0;
   public static final double ae = 0.2;
   public static final double aV = 3.0;
   public static final double ar = 0.1F;
   public static final double ai = 6.0;
   public static final double ah = 50.0;
   public static final double bR = 39.0;
   public static final double bV = 58.0;
   public static final double aZ = 2.0;
   public static final double SCALE_1_0 = 1.0;
   public static final float aJ = 0.5F;
   public static final MutableVector3f STAR_COLOR = new MutableVector3f(0.83137256F, 0.6862745F, 0.21568628F);
   public static final Vec3d LEFT_ENERGY_BALL_OFFSET = new Vec3d(-1.049342F, 2.0547214F, -0.050482392F);
   public static final Vec3d RIGHT_ENERGY_BALL_OFFSET = new Vec3d(1.2522261F, 1.4357733F, 0.23570988F);
   public static final int aN = 10;
   public static final float ak = 0.2F;
   public static final int am = 5;
   public static final float SPEED_15 = 15.0F;
   public static final int aM = 48;
   public static final float be = 0.05F;
   public static final float a7 = 0.65F;
   public static final float bh = 0.9F;
   public static final float ANGLE_45 = 45.0F;
   public static final float a0 = 1.0F;
   public static final float ForgeEventHandler = 1.5F;
   public static final float ao = 110.0F;
   public static final int aj = 15;
   public static final float aw = 6.0F;
   public static final float bp = 0.94F;
   public static final int COUNT_13 = 13;
   public static final int bW = 40;
   public static final int bl = 25;
   public static final int aY = 38;
   public static final int DISTANCE_95 = 95;
   static final int bB = 10;
   static final int aI = 30;
   static final int bf = 175;
   static final float as = 2.0F;
   public static final float bo = 0.25F;
   public static final float TIMEOUT_1000 = 1000.0F;
   public static final float bX = 15.0F;
   public static final float b9 = 5.0F;
   public static final int aW = 8000;
   public static final float aK = 0.1F;
   public static final float ac = 5.0F;
   public static final float b5 = -10.0F;
   public static final int bk = 16;
   public static final int br = 7;
   public static final int cb = 4;
   public static final float FACTOR_0_5 = 0.5F;
   public static final float SexModEntities = 0.55F;
   static final Class<?>[] NON_SOLID_FLIGHT_BLOCKS = new Class[]{
      BlockAir.class, BlockCarpet.class, BlockBush.class, BlockButton.class, BlockLadder.class, BlockTorch.class, BlockSign.class, BlockBanner.class
   };
   public static final DataParameter<Integer> TARGET_ENTITY_ID = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   public static final DataParameter<Integer> ATTACK_PROGRESS = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(112);
   public static final DataParameter<Boolean> LEFT_ENERGY_BALL_ALIVE = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(113);
   public static final DataParameter<Boolean> RIGHT_ENERGY_BALL_ALIVE = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(114);
   public static final DataParameter<Boolean> MIRROR_SIDE = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(115);
   public static final DataParameter<Integer> SWORD_ATTACK_PROGRESS = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(116);
   public static final DataParameter<String> FLIGHT_TARGET_POS = EntityDataManager.createKey(GalathEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(117);
   public static final DataParameter<Boolean> PARALYZED = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(118);
   public static final DataParameter<Float> RAPE_PROGRESS = EntityDataManager.createKey(GalathEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<Boolean> HIDE_EFFECTS_FLAG = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> WildSlimeFaceLayer = EntityDataManager.createKey(GalathEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   public static final DataParameter<Boolean> SPRINTING = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(122);
   public static final double b0 = 0.2;
   public static final float bS = 5.0F;
   public static final int a1 = 60;
   BossInfoServer bossBar = new BossInfoServer(new TextComponentString(this.getDisplayNameText()), Color.RED, Overlay.PROGRESS);
   GalathEnergyBallPart energyBallHitboxLeft = new GalathEnergyBallPart(this, "energyBallHitBox", 0.75F, 0.75F);
   GalathEnergyBallPart energyBallHitboxRight = new GalathEnergyBallPart(this, "energyBallHitBox", 0.75F, 0.75F);
   public GalathFlightData activeFlight = null;
   public Vec3d flightTargetPosition = null;
   public Vec3d lastFlightPosition = null;
   public int rapeChargeTicks = 0;
   public Vec3d chargeStartPosition = null;
   public List<EntityWitherSkeleton> summonedSkeletons = new ArrayList<>();
   public float headRotationX = 0.0F;
   public long dashStartTick = -1L;
   public long dashEndTick = -1L;
   public float bodyRotationY = 0.0F;
   public float bodyScaleY = 0.0F;
   boolean deathHandled = false;
   public Vec3d clientTargetPosition = null;
   boolean markedDespawned = false;
   Vec3d previousPosition;
   Vec3d predictedPosition;
   Vec3d velocity;
   float boostSpeed = 0.0F;
   boolean corruptIntroActive = false;
   public int summonSkeletonTicks = 0;
   double headTiltZ = 0.0;
   double headTiltX = 0.0;
   double prevHeadTiltZ = 0.0;
   double prevHeadTiltX = 0.0;
   boolean customModelCodeLoaded = false;
   Path currentPath = null;
   BlockPos wanderTarget = null;
   int wanderTicks = 0;
   Action previousAction = null;
   int rapeCumTicks = 0;
   int standUpTicks = 0;
   int knockoutGroundTicks = 0;
   long lastHurtSoundTime = 0L;
   boolean morningBlowjobStarted = false;
   boolean despawned = false;
   int rapeVariant = 0;
   boolean corruptHard = false;
   public boolean masterbateSittingPending = false;
   public boolean pussyLickingForward = false;
   public boolean bedSoftAnimation = false;
   public boolean bedBackAnimation = false;
   public boolean renderSword = false;
   public boolean spawnSwordParticles = false;
   public boolean drawStars = true;
   public boolean wingsAnimated = false;
   boolean alternateMoan = false;

   public GalathEntity(World world) {
      super(world);
   }

   public GalathEntity(World world, @Nonnull EntityPlayer player, Vec3d pos, boolean spawnStructure) {
      this(world);
      UUID uuid = player.getPersistentID();
      this.entityDataManager.set(MASTER, uuid.toString());
      this.bossBar.setVisible(false);
      this.wanderTarget = new BlockPos(this.getPositionVector());
      String npcName = AllieWorldData.getNpcName(uuid, NpcType.GALATH);
      if (npcName != null) {
         super.setCustomNameOverride(npcName);
      }

      if (!spawnStructure) {
         if (this.getRNG().nextFloat() > 0.1F) {
            this.setCurrentAction(Action.GALATH_SUMMON);
         } else {
            this.setCurrentAction(Action.MASTERBATE);
            this.setYawRotation(180.0F - (float)TrigMath.sinDegrees(Math.atan2(pos.x - player.posX, pos.z - player.posZ)));
            ThreadNames.createDaemonThread(8000, () -> {
               EntityPlayer master = this.getMasterPlayer();
               if (master != null) {
                  if (!master.isDead) {
                     this.setTargetPosition(master.getPositionVector());
                     this.setYawRotation(master.rotationYaw + 180.0F);
                     this.setCurrentAction(Action.RAPE_INTRO);
                     this.setInteractionPlayerUUID(master.getPersistentID());
                     this.setAnchored(true);
                  }
               }
            });
         }
      }
   }

   public GalathEntity(World world, @Nonnull EntityPlayer player, Vec3d pos) {
      this(world, player, pos, false);
   }

   @Override
   public void setCustomModelCode(String modelCode) {
      super.setCustomModelCode(modelCode);
      CustomModelCodeData.setCustomModelCode(this);
   }

   @Override
   public String getDisplayNameText() {
      return "Galath";
   }

   @Override
   public float getScaleFactor() {
      return this.getMangleliePartnerUUID() == null ? 0.5F : 1.35F;
   }

   public float getEyeHeight() {
      return 1.9F;
   }

   public boolean hasMaster() {
      return super.hasMaster();
   }

   public boolean isPushedByWater() {
      return false;
   }

   protected void handleJumpWater() {
      if (this.hasMaster()) {
         super.handleJumpWater();
      }
   }

   protected float getWaterSlowDown() {
      return this.hasMaster() ? super.getWaterSlowDown() : 0.0F;
   }

   public boolean isInWater() {
      return this.hasMaster() ? super.isInWater() : false;
   }

   public boolean handleWaterMovement() {
      return this.hasMaster() ? super.handleWaterMovement() : false;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(TARGET_ENTITY_ID, -1);
      this.entityDataManager.register(ATTACK_PROGRESS, 0);
      this.entityDataManager.register(LEFT_ENERGY_BALL_ALIVE, true);
      this.entityDataManager.register(RIGHT_ENERGY_BALL_ALIVE, true);
      this.entityDataManager.register(MIRROR_SIDE, false);
      this.entityDataManager.register(FLIGHT_TARGET_POS, "null");
      this.entityDataManager.register(SWORD_ATTACK_PROGRESS, -1);
      this.entityDataManager.register(PARALYZED, false);
      this.entityDataManager.register(RAPE_PROGRESS, 0.0F);
      this.entityDataManager.register(HIDE_EFFECTS_FLAG, false);
      this.entityDataManager.register(WildSlimeFaceLayer, "");
      this.entityDataManager.register(SPRINTING, false);
   }

   @Override
   protected void applyEntityAttributes() {
      // Forge registers SWIM_SPEED in EntityLivingBase; the water tick reads
      // it. Without this super call getEntityAttribute(SWIM_SPEED) is null
      // and handleJumpWater NPEs for a tamed Galath in water. The base class
      // already registered the standard attributes, so only FLYING_SPEED is
      // added here; registering the others twice throws "already registered".
      super.applyEntityAttributes();
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(110.0);
      this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.6F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6F);
      this.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0);
   }

   @Override
   protected void initEntityAI() {
      this.watchClosestGirlGoal = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAITempt(this, 0.4, false, new HashSet<>(TEMPTATION_ITEMS)));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.watchClosestGirlGoal);
   }

   public void addTrackingPlayer(EntityPlayerMP playerMP) {
      super.addTrackingPlayer(playerMP);
      this.bossBar.addPlayer(playerMP);
   }

   public void removeTrackingPlayer(EntityPlayerMP playerMP) {
      super.removeTrackingPlayer(playerMP);
      this.bossBar.removePlayer(playerMP);
   }

   @Override
   public Vec3d getTargetPosition() {
      return this.world.isRemote && this.clientTargetPosition != null ? this.clientTargetPosition : super.getTargetPosition();
   }

   @Nullable
   public UUID getMangleliePartnerUUID() {
      String partnerStr = (String)this.entityDataManager.get(WildSlimeFaceLayer);
      if ("".equals(partnerStr)) {
         return null;
      }

      try {
         return UUID.fromString(partnerStr);
      } catch (Exception ex) {
         return null;
      }
   }

   @Nullable
   public ManglelieEntity getMangleliePartner(boolean server) {
      UUID uuid = this.getMangleliePartnerUUID();
      if (uuid == null) {
         return null;
      }

      BaseGirlEntity partner = server ? getServerGirlEntity(uuid) : getClientGirlEntity(uuid);
      return partner instanceof ManglelieEntity ? (ManglelieEntity)partner : null;
   }

   @Nullable
   public static ManglelieEntity getMangleliePartnerOf(BaseGirlEntity girl, boolean server) {
      return !(girl instanceof GalathEntity) ? null : ((GalathEntity)girl).getMangleliePartner(server);
   }

   public void setMangleliePartnerUUID(@Nullable UUID uuid) {
      this.entityDataManager.set(WildSlimeFaceLayer, uuid == null ? "" : uuid.toString());
   }

   public void markDespawnedWithPartner() {
      this.markedDespawned = true;
      ManglelieEntity manglelie = this.getMangleliePartner(true);
      if (manglelie != null) {
         manglelie.markDespawned();
      }
   }

   /**
    * SERVER: ends the rape scene, see
    * {@link GalathEntity#handleRapeState(GalathEntity)}.
    */
   public void handleRapeState() {
      this.handleRapeStateImpl();
   }

   public Vec3d getFlightTargetPos() {
      String[] parts = ((String)this.entityDataManager.get(FLIGHT_TARGET_POS)).split("\\|");
      return new Vec3d(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }

   public void setFlightTargetPos(@Nullable Vec3d pos) {
      this.entityDataManager.set(FLIGHT_TARGET_POS, pos.x + "|" + pos.y + "|" + pos.z);
   }

   public int getSwordAttackProgress() {
      return (Integer)this.entityDataManager.get(SWORD_ATTACK_PROGRESS);
   }

   public void setSwordAttackProgress(int progress) {
      this.entityDataManager.set(SWORD_ATTACK_PROGRESS, progress);
   }

   public boolean isNonBoss() {
      return false;
   }

   @Override
   public boolean isHuggingManglelie() {
      switch (this.getCurrentAction()) {
         case HUG_MANG:
         case MORNING_BLOWJOB_SLOW:
         case MORNING_BLOWJOB_FAST:
         case MORNING_BLOWJOB_CUM:
            return true;
         default:
            return false;
      }
   }

   void updatePredictedPosition() {
      this.velocity = new Vec3d(this.motionX, this.motionY, this.motionZ);
      this.previousPosition = this.getPositionVector();
      this.predictedPosition = this.getPositionVector().add(this.velocity);
      this.velocity = this.velocity.scale(0.9);
   }

   /**
    * BOTH sides: the per-tick split — tamed girls tick the master systems
    * ({@link #updateRidingGravity()} gravity + {@link #tickTamedSystems()}),
    * wild girls tick the flight UI (boss bar, gravity, flight data) and the
    * wild systems
    * ({@link GalathEntity#tickWildSystems(GalathEntity)}). CLIENT
    * additionally ticks the coin-give particles
    * ({@link GalathEntity#tickGiveCoinScene(GalathEntity)}).
    */
   @Override
   public void onUpdate() {
      boolean hasMasterFlag = this.hasMaster();
      if (hasMasterFlag) {
         this.updateRidingGravity();
      } else {
         this.updateFlightUI();
      }

      this.updatePredictedPosition();
      super.onUpdate();
      if (hasMasterFlag) {
         this.tickTamedSystems();
      } else {
         this.tickWildSystems();
      }

      if (this.world.isRemote) {
         this.tickGiveCoinScene();
      }
   }

   @Override
   public boolean canBeInteractedWith() {
      return false;
   }

   /**
    * SERVER: tamed per-tick systems — soft fall damping, riding-player yaw
    * sync, the carry camera pitch, the boost impulse, wing/effect flags, and
    * the rape/corrupt scene enders ({@link GalathEntity#handleRapeCum(GalathEntity)},
    * {@link GalathEntity#endTamedCorruptScene(GalathEntity)}).
    */
   void tickTamedSystems() {
      if (!this.isInWater() && !this.hasNoGravity() && this.motionY < 0.0 && this.getCurrentAction() != Action.MASTERBATE) {
         this.motionY *= 0.4F;
      }

      this.syncRenderYawToRider();
      this.updateFlightRenderAngles();
      this.updateMasterLookPitch();
      this.tickBoostFlight();
      this.updateWingsAnimated();
      this.endTamedCorruptScene();
      this.handleRapeCum();
      if (this.getTargetEntity() == null) {
         this.renderSword = false;
      }
   }

   static boolean isNearHive(BlockPos pos, World world) {
      for (BlockPos hivePos : BeeWorldData.hivePositions) {
         if (Math.sqrt(pos.distanceSq(hivePos)) < 1000.0) {
            return false;
         }
      }

      try {
         for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
            if (!girl.world.isRemote && girl instanceof GalathEntity && !girl.isDead && girl.getDistanceSq(pos) < 1000000.0) {
               return false;
            }
         }
      } catch (ConcurrentModificationException ex) {
         // ignored: entity list mutated during iteration; retried on the next tick
      }

      for (int y = pos.getY(); y < 15.0F + pos.getY(); y++) {
         if (world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getBlock() != Blocks.AIR) {
            return false;
         }
      }

      for (int y = pos.getY(); y > pos.getY() - 5.0F; y--) {
         if (world.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).getBlock() instanceof BlockLiquid) {
            return false;
         }
      }

      return true;
   }

   void updateFlightUI() {
      this.updateGravity();
      this.updateHealthBar();
      this.tickSummonSkeletonProgress();
   }

   @Override
   public boolean isCustomType() {
      return this.getCurrentAction() != Action.CORRUPT_INTRO ? false : this.corruptIntroActive;
   }

   protected void collideWithNearbyEntities() {
   }

   public void addPotionEffect(PotionEffect effect) {
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void resetAnimationControllerTicks() {
      if (this.getCurrentAction() != Action.GALATH_DE_SUMMON) {
         this.actionController.tickOffset = 0.0;
      }
   }

   @Override
   public String getEffectiveDisplayName() {
      EntityPlayer master = this.getMasterPlayer();
      return master == null ? super.getEffectiveDisplayName() : String.format("%s %s[%s]", super.getEffectiveDisplayName(), TextFormatting.DARK_PURPLE, master.getName());
   }

   @Override
   public Vector4d getFlightData() {
      return new Vector4d(this.headTiltZ, this.headTiltX, this.prevHeadTiltZ, this.prevHeadTiltX);
   }

   void updateFlightRenderAngles() {
      this.prevHeadTiltZ = this.headTiltZ;
      this.prevHeadTiltX = this.headTiltX;
      Vec3d delta = this.predictedPosition.subtract(this.previousPosition);
      Vec3d rotated = VectorMath.rotateByYaw(delta, this.renderYawOffset + 180.0F);
      this.headTiltZ = TrigMath.toRadians(ThreadNames.clampDouble(rotated.z * 40.0, -50.0, 50.0));
      this.headTiltX = TrigMath.toRadians(ThreadNames.clampDouble(rotated.x * 40.0, -50.0, 50.0));
   }

   /**
    * SERVER: the paralysis knock — dragon explosions call this; once
    * paralyzed she stops the flight action, is flung away from the source
    * ({@link Action#KNOCK_OUT_FLY}) and broadcasts the "time to corrupt her"
    * message with the effect particles.
    */
   public void setFlightVelocity(Vec3d targetPos) {
      if (!(Boolean)this.entityDataManager.get(PARALYZED)) {
         this.entityDataManager.set(PARALYZED, true);
         if (this.activeFlight != null) {
            this.activeFlight.updateFlight(this);
         }

         this.activeFlight = null;
         Vec3d pos = this.getPositionVector();
         Random random = this.getRNG();
         Vec3d vel = targetPos == null
            ? new Vec3d(random.nextDouble(), random.nextDouble(), random.nextDouble()).normalize()
            : pos.subtract(targetPos).normalize();
         this.setVelocity(vel.x * 1.0, 1.0, vel.z * 1.0);
         this.setCurrentAction(Action.KNOCK_OUT_FLY);
         this.setNoGravity(false);
         this.noClip = false;
         this.getNavigator().clearPath();
         playRandomSound(this, SoundHandler.GIRLS_GALATH_AAA, true);
      }
   }

   void sendTrackingMessage(Entity entity) {
      BaseGirlEntity.sendMessageToTrackingPlayers(this, TextFormatting.YELLOW + "Galath is paralyzed! Now it's time to corrupt her");
      BaseGirlEntity.sendMessageToTrackingPlayers(this, TextFormatting.GRAY + "(Walk to her and right click her)");
      PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(this.getPositionVector(), true), this);
      this.setFlightVelocity(null);
      this.entityDataManager.set(HIDE_EFFECTS_FLAG, true);
   }

   /**
    * BOTH sides, every AI tick: the tamed/wild split — wild girls (no
    * master) tick the flight state machine, boss bar and knock-out recovery;
    * tamed girls follow their owner (or the Manglelie partnership) and run
    * the scene/morning-blowjob helpers.
    */
   @Override
   public void updateAITasks() {
      if (this.despawned) {
         OwnershipSavedData.updateMangleliePartner(this);
      } else {
         this.ensureCustomModelCodeLoaded();
         super.updateAITasks();
         this.watchClosestGirlGoal.isWatching = this.isFlyingIdle();
         if (this.hasMaster()) {
            this.tickTamedAi();
         } else {
            this.tickWildAi();
         }
      }
   }

   void ensureCustomModelCodeLoaded() {
      if (!this.customModelCodeLoaded) {
         this.setCustomModelCode(CustomModelCodeData.getCustomModelCode(this));
         this.customModelCodeLoaded = true;
      }
   }

   boolean isFlyingIdle() {
      return this.getCurrentAction() != Action.NULL ? false : !(Math.abs(this.motionX) + Math.abs(this.motionZ) > 0.01);
   }

   protected void handlePlayerRide(EntityPlayer player) {
      this.handlePlayerRideImpl(player);
   }

   void wanderRandomly() {
      Action action = this.getCurrentAction();
      if (action != Action.RUN) {
         if (action != Action.HUG_MANG) {
            if (!this.isAnchored() && action != Action.MASTERBATE) {
               EntityPlayer player = this.world.getClosestPlayerToEntity(this, 15.0);
               if (this.hasMaster() && player != null && player.getDistance(this) < 2.0F && player.getPersistentID().equals(this.getMasterUUID())) {
                  this.getNavigator().clearPath();
               } else {
                  if (this.wanderTarget == null
                     || this.getDistance(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ()) > this.getFlightRange()
                     || this.wanderTicks > 175) {
                     int xOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
                     int zOffset = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
                     int height = this.world.provider.getDimensionType() == DimensionType.NETHER
                        ? (int)Math.ceil(this.posY)
                        : WorldUtils.getHeightAt(this.world, this.getPosition().getX() + xOffset, this.getPosition().getZ() + zOffset);
                     this.wanderTarget = new BlockPos(this.getPosition().getX() + xOffset, height, this.getPosition().getZ() + zOffset);
                     this.wanderTicks = 0;
                  }

                  if (Math.sqrt(this.wanderTarget.distanceSq(this.getPosition())) > 2.0) {
                     this.getNavigator().tryMoveToXYZ(this.wanderTarget.getX(), this.wanderTarget.getY(), this.wanderTarget.getZ(), 0.35F);
                     this.tickPathVelocity();
                  } else {
                     this.wanderTicks++;
                  }
               }
            } else {
               this.getNavigator().clearPath();
            }
         }
      }
   }

   BlockPos getManglelieOwnerPosition() {
      UUID ownerUuid = OwnershipSavedData.getManglelieOwnerOf(this);
      if (ownerUuid == null) {
         return BlockPos.ORIGIN;
      }

      EntityPlayer owner = this.world.getPlayerEntityByUUID(ownerUuid);
      return owner == null ? BlockPos.ORIGIN : owner.getPosition();
   }

   double getFlightRange() {
      return Math.sqrt(1800.0);
   }

   @Nullable
   public EntityPlayer getRidingPlayer() {
      List passengers = this.getPassengers();
      if (passengers.isEmpty()) {
         return null;
      } else {
         return passengers.get(0) instanceof EntityPlayer ? (EntityPlayer)passengers.get(0) : null;
      }
   }

   @Nullable
   public UUID getRidingPlayerUUID() {
      EntityPlayer rider = this.getRidingPlayer();
      return rider == null ? null : rider.getPersistentID();
   }

   @Override
   public void setCustomNameOverride(String name) {
      super.setCustomNameOverride(name);
      UUID masterUuid = this.getMasterUUID();
      if (masterUuid != null) {
         AllieWorldData.addAllie(masterUuid, NpcType.GALATH, name);
      }
   }

   public void applyVelocityDelta(Vec3d delta) {
      this.motionX = this.motionX + delta.x;
      this.motionZ = this.motionZ + delta.z;
      this.motionY = delta.y / 2.0;
   }

   public void resetInteractionState() {
      this.resetInteractionStateImpl();
   }

   void tickWildAi() {
      this.bossBar.setVisible(true);
      this.tickFlightSystems();
      this.tickIdleWander();
   }

   /**
    * SERVER: the tamed rape/corrupt scenes' shared tick — targets the
    * nearest player or mob in a radius (7 tamed / 20 wild), picks flight
    * actions (see {@link GalathEntity#initFlightData(GalathEntity)}), keeps the energy-ball
    * hitboxes synced and watches the flight target validity.
    */
   void tickFlightSystems() {
      if (!Action.isAnyAction(this, Action.MASTERBATE, Action.HUG_MANG)) {
         if (this.getInteractionPlayerUUID() == null) {
            this.cancelFlightOnLostTarget();
            this.acquireFlightTarget();
            this.tickFlightStateMachine();
            this.checkFlightFinished();
            this.updateChangePositionNoClip();
            this.tickKnockoutFlight();
            this.freezeDuringKnockout();
            this.handleKnockout();
            this.tickKnockoutStandUp();
            this.pruneDeadSkeletons();
            this.spawnSkeletonBreathParticles();
            this.despawnSummonedSkeletons();
            this.despawnFarSummonedSkeletons();
            this.tickWildRapeCum();
         }
      }
   }

   void tickIdleWander() {
      if (this.getTargetEntity() != null) {
         this.wanderTarget = null;
         this.wanderTicks = 0;
      } else if (!(Boolean)this.entityDataManager.get(HIDE_EFFECTS_FLAG)) {
         if (!(Boolean)this.entityDataManager.get(PARALYZED)) {
            this.wanderRandomly();
         }
      }
   }

   /**
    * SERVER: the state-machine gate, see
    * {@link GalathEntity#setCurrentAction(GalathEntity, Action)}.
    */
   @Override
   public void setCurrentAction(Action action) {
      this.setCurrentActionGuarded(action);
   }

   /**
    * Keeps the super call reachable from the entity while the
    * transition gate lives there.
    */
   void setCurrentActionBase(Action action) {
      super.setCurrentAction(action);
   }

   void endMorningBlowjobScene() {
      this.endMorningBlowjobSceneImpl();
   }

   void endHugScene() {
      this.endHugSceneImpl();
   }

   void grantCoinAndBindMaster() {
      this.grantCoinAndBindMasterImpl();
   }

   void setFlying(boolean flying) {
      this.setFlyingImpl(flying);
   }

   @SideOnly(Side.CLIENT)
   @Override
   public float getRenderScaleFactor() {
      Minecraft mc = Minecraft.getMinecraft();
      if (mc.gameSettings.thirdPersonView != 0) {
         return 1.0F;
      }

      switch (this.getCurrentAction()) {
         case CORRUPT_SLOW:
         case CORRUPT_FAST:
         case CORRUPT_CUM:
            break;
         case CORRUPT_INTRO:
            if (this.corruptIntroActive) {
               break;
            }
         case MASTERBATE:
         case RAPE_PREPARE:
         default:
            return 1.0F;
      }

      return 0.5F;
   }

   @Override
   protected boolean supportsCustomModels() {
      return false;
   }

   public boolean canStartPussyLicking() {
      return this.canStartPussyLickingImpl();
   }

   public static void handlePlayerJoin(EntityPlayer player) {
      BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(OwnershipSavedData.getOwnerOf(player));
      if (girl != null) {
         if (girl.equals(player.getRidingEntity())) {
            girl.setInteractionPlayerUUID(player.getPersistentID());
            girl.setCurrentAction(Action.CONTROLLED_FLIGHT);
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public BaseGirlEntity asGirl() {
      ManglelieEntity manglelie = this.getMangleliePartner(false);
      if (manglelie == null) {
         return super.asGirl();
      }

      EntityPlayerSP player = Minecraft.getMinecraft().player;
      if (player.isSneaking()) {
         return manglelie;
      }

      player.sendStatusMessage(new TextComponentString(TextFormatting.GRAY + "[sneak] + [right click] if you want to edit Manglelie instead"), true);
      return super.asGirl();
   }

   @Override
   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      return this.processInteractImpl(player, hand);
   }

   /**
    * Keeps the super call reachable from the entity while the
    * master/girl interaction split lives there.
    */
   boolean processInteractBase(EntityPlayer player, EnumHand hand) {
      return super.processInteract(player, hand);
   }

   /**
    * CLIENT: master-action dispatch, see
    * {@link GalathEntity#doAction(GalathEntity, String, UUID)}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void doAction(String action, UUID uuid) {
      this.doGalathAction(action, uuid);
   }

   @Nullable
   public Entity[] getParts() {
      return new Entity[]{this.energyBallHitboxRight, this.energyBallHitboxLeft};
   }

   public void setTargetEntity(@Nullable EntityLivingBase target) {
      if (target == null) {
         this.entityDataManager.set(TARGET_ENTITY_ID, -1);
      } else {
         this.entityDataManager.set(TARGET_ENTITY_ID, target.getEntityId());
      }
   }

   public int getAttackProgress() {
      return (Integer)this.entityDataManager.get(ATTACK_PROGRESS);
   }

   public void setAttackProgress(int progress) {
      this.entityDataManager.set(ATTACK_PROGRESS, progress);
   }

   public EntityLivingBase getTargetEntity() {
      int targetId = (Integer)this.entityDataManager.get(TARGET_ENTITY_ID);
      return -1 == targetId ? null : (EntityLivingBase)this.world.getEntityByID(targetId);
   }

   /**
    * SERVER: computes the aim yaw toward the flight target (progress-lerped
    * positions) and applies it to the render yaw. Returns null outside the
    * aiming actions (FLY/SUMMON_SKELETON/RAPE_PREPARE) — callers must handle
    * null (see {@link ManglelieEntity#isLookingAtGalathPoint(Vec3d, float)}).
    */
   public static Float getAimYaw(GalathEntity galath, float partialTicks) {
      Action action = galath.getCurrentAction();
      if (action != Action.FLY && action != Action.SUMMON_SKELETON && action != Action.RAPE_PREPARE) {
         return null;
      }

      EntityLivingBase target = galath.getTargetEntity();
      if (target == null) {
         return null;
      }

      Vec3d targetPos = RotationHelper.lerpVec3dDouble(new Vec3d(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ), target.getPositionVector(), partialTicks);
      Vec3d selfPos = RotationHelper.lerpVec3dDouble(new Vec3d(galath.lastTickPosX, galath.lastTickPosY, galath.lastTickPosZ), galath.getPositionVector(), partialTicks);
      Vec3d delta = targetPos.subtract(selfPos);
      float yaw = (float)TrigMath.sinDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
      galath.renderYawOffset = yaw;
      galath.prevRenderYawOffset = yaw;
      return yaw;
   }

   void playHurtSound(float damage) {
      this.playHurtSoundImpl(damage);
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
      return this.attackEntityFromImpl(source, amount);
   }

   /**
    * Keeps the super call reachable from the entity while the damage
    * gate lives there.
    */
   boolean attackEntityFromBase(DamageSource source, float amount) {
      return super.attackEntityFrom(source, amount);
   }

   public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float amount) {
      return this.attackEntityFromPartImpl(part, source, amount);
   }

   @Override
   public void reinitTasks() {
      this.setTargetEntity(null);
      this.clearActiveFlight();
   }

   public World getWorld() {
      return this.world;
   }

   public void setFire(int seconds) {
   }

   public void fall(float distance, float multiplier) {
   }

   @Nullable
   @Override
   protected Action getNextAction(Action action) {
      return null;
   }

   @Override
   protected Action getCumAction(Action action) {
      if (action == Action.CORRUPT_FAST || action == Action.CORRUPT_SLOW) {
         return Action.CORRUPT_CUM;
      }

      if (action == Action.RAPE_ON_GOING) {
         return Action.RAPE_CUM;
      }

      if (Action.isAny(action, Action.MORNING_BLOWJOB_SLOW, Action.MORNING_BLOWJOB_FAST)) {
         this.morningBlowjobStarted = true;
      }

      return null;
   }

   @Override
   public boolean isWingsAnimated() {
      return this.wingsAnimated;
   }

   @Override
   public boolean areWingsAnimated() {
      switch (this.getCurrentAction()) {
         case CORRUPT_SLOW:
         case CORRUPT_FAST:
         case CORRUPT_CUM:
         case COWGIRLCUM:
            return false;
         case MASTERBATE:
         case RAPE_PREPARE:
         case CORRUPT_INTRO:
         default:
            return true;
      }
   }

   /**
    * SERVER: per-tick rape damage, see
    * {@link GalathEntity#handleRapeAction(GalathEntity, boolean)}.
    */
   public void handleRapeAction(boolean applyDamage) {
      this.handleRapeActionImpl(applyDamage);
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

   public void startMasterbateSitting() {
      this.startMasterbateSittingImpl();
   }

   public void startPussyLicking() {
      this.startPussyLickingImpl();
   }

   @Override
   protected boolean handleActionAnimationOverrides(Action action, String animName, boolean started, AnimationEvent event) {
      return this.handleActionAnimationOverridesImpl(action, animName, started, event);
   }

   public float getSwordAttackProgress(float partialTicks) {
      return this.getSwordAttackProgressImpl(partialTicks);
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      return this.animationPredicateImpl(event);
   }

   /**
    * CLIENT: registers the action (via {@link GirlAnimationController}),
    * movement and eyes controllers on the entity and hands the sound listener
    * to {@link GalathEntity#handleSoundKeyframe(GalathEntity,
    * software.bernie.geckolib3.core.event.SoundKeyframeEvent)}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      this.actionController = new GirlAnimationController<>(this, "action", 0.0F, this::animationPredicate);
      this.movementController = new AnimationController<>(this, "movement", 5.0F, this::animationPredicate);
      this.eyesController = new AnimationController<>(this, "eyes", 10.0F, this::animationPredicate);
      this.actionController.registerSoundListener(sound -> this.handleSoundKeyframe(sound));
      data.addAnimationController(this.actionController);
      data.addAnimationController(this.eyesController);
      data.addAnimationController(this.movementController);
   }

   /**
    * Event handlers for the Galath fight/taming lifecycle: replaces wither
    * skeleton/blaze spawns near hives with wild Galaths
    * ({@code canSpawn}), jump = flight boost while riding ({@code onKeyInput}),
    * unmount resets ({@code onMount}), death is intercepted — wild girls
    * enter the knockout + "corrupt her" broadcast, tamed girls de-summon to
    * the coin ({@code onLivingDeath}), player respawn mid-scene resets the
    * girl ({@code onRespawn}), the SUMMON_SKELETON energy balls are rendered
    * as growing dragons ({@code onRenderWorldLast}), and waking up with a
    * tamed Galath spawns the morning-blowjob scene when the bed has free
    * space ({@code onWake}, see {@link #spawnStructure(World, BlockPos, EnumFacing)}).
    */
   public static class GalathLifecycleEvents {
      boolean hasRidingPlayer(GalathEntity galath) {
         return galath.getRidingPlayer() != null;
      }

      @SubscribeEvent(priority = EventPriority.LOWEST)
      public void canSpawn(CheckSpawn event) {
         Result result = event.getResult();
         if (result != Result.DENY) {
            if (!event.isSpawner()) {
               Entity entity = event.getEntity();
               if (entity instanceof EntityWitherSkeleton || entity instanceof EntityBlaze) {
                  BlockPos pos = entity.getPosition();
                  World world = entity.world;
                  if (GalathEntity.isNearHive(pos, world)) {
                     event.setResult(Result.DENY);
                     BeeWorldData.addHivePosition(pos, BeeWorldData.hivePositions);
                     GalathEntity galath = new GalathEntity(world);
                     galath.setPositionAndUpdate(pos.getX(), pos.getY(), pos.getZ());
                     world.spawnEntity(galath);
                  }
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onKeyInput(KeyInputEvent event) {
         Minecraft mc = Minecraft.getMinecraft();
         if (mc.gameSettings.keyBindJump.isKeyDown()) {
            if (GalathFlightHud.canUseCharge()) {
               try {
                  for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
                     if (girl.world.isRemote
                        && girl instanceof GalathEntity
                        && mc.player.getPersistentID().equals(((GalathEntity)girl).getRidingPlayerUUID())) {
                        GalathFlightHud.useCharge();
                        girl.setCurrentAction(Action.BOOST);
                        return;
                     }
                  }
               } catch (ConcurrentModificationException ex) {
                  // ignored: entity list mutated during iteration; retried on the next tick
               }
            }
         }
      }

      @SubscribeEvent
      public void onMount(EntityMountEvent event) {
         if (!event.isMounting()) {
            Entity entity = event.getEntityBeingMounted();
            if (entity instanceof GalathEntity) {
               if (entity.world.isRemote) {
                  GalathFlightHud.startFadeOut();
               } else {
                  ((GalathEntity)entity).resetInteractionState();
               }
            }
         }
      }

      @SubscribeEvent(priority = EventPriority.HIGH)
      public void onLivingDeath(LivingDeathEvent event) {
         Entity entity = event.getEntity();
         if (entity instanceof GalathEntity) {
            if (!event.getSource().equals(DamageSource.OUT_OF_WORLD)) {
               GalathEntity galath = (GalathEntity)entity;
               if (!galath.deathHandled) {
                  if (!entity.world.isRemote) {
                     if (!galath.hasMaster()) {
                        galath.sendTrackingMessage(galath.getCombatTracker().getFighter());
                     } else {
                        GalathCoinItem.deSummonGalath(galath);
                        PacketHandler.networkWrapper.sendToAllTracking(new GalathSummonParticlesPacket(galath.getGirlId(), OwnershipSavedData.getManglelieOwnerOf(galath)), galath);
                        ThreadNames.createDaemonThread(900, () -> OwnershipSavedData.updateMangleliePartner(galath));
                        galath.deathHandled = true;
                     }

                     galath.setHealth(1.0F);
                     event.setCanceled(true);
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void onRespawn(PlayerRespawnEvent event) {
         EntityPlayerMP player = (EntityPlayerMP)event.player;
         BaseGirlEntity girl = BaseGirlEntity.getGirlByUUID(player.getPersistentID(), Boolean.valueOf(true));
         if (girl instanceof GalathEntity) {
            GalathEntity galath = (GalathEntity)girl;
            galath.setTargetEntity(null);
            ResetGirlPacket.Handler.resetGirl(girl);
            PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(true), player);
            girl.setCurrentAction((Action)null);
            if (galath.activeFlight != null) {
               galath.activeFlight.updateFlight(galath);
               galath.activeFlight = null;
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void onRenderWorldLast(RenderWorldLastEvent event) {
         Minecraft mc = Minecraft.getMinecraft();
         RenderManager renderManager = mc.getRenderManager();
         float partialTicks = mc.getRenderPartialTicks();

         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl instanceof GalathEntity && girl.world.isRemote && girl.getCurrentAction() == Action.SUMMON_SKELETON) {
                  double progress = ((GalathEntity)girl).summonSkeletonTicks;
                  if (!(progress < 9.0) && !(progress > 30.0)) {
                     Vec3d basePos = RotationHelper.lerpVec3dDouble(new Vec3d(girl.lastTickPosX, girl.lastTickPosY, girl.lastTickPosZ), girl.getPositionVector(), partialTicks);
                     double scale = (progress - 9.0) / 21.0;
                     if ((Boolean)girl.getDataManager().get(GalathEntity.LEFT_ENERGY_BALL_ALIVE)) {
                        Vec3d offsetR = girl.getCachedBoneOffset("energyBallR");
                        Vec3d posR = basePos.add(offsetR);
                        DragonChargeEntity dragonR = new DragonChargeEntity(girl.world, (GalathEntity)girl);
                        dragonR.SCALE_1_0 = scale;
                        dragonR.setPositionAndUpdate(posR.x, posR.y, posR.z);
                        renderManager.renderEntity(dragonR, 0.0, 0.0, 0.0, 0.0F, partialTicks, true);
                        dragonR.setPosition(0.0, -500.0, 0.0);
                        dragonR.setDead();
                     }

                     if ((Boolean)girl.getDataManager().get(GalathEntity.RIGHT_ENERGY_BALL_ALIVE)) {
                        Vec3d offsetL = girl.getCachedBoneOffset("energyBallL");
                        Vec3d posL = basePos.add(offsetL);
                        DragonChargeEntity dragonL = new DragonChargeEntity(girl.world, (GalathEntity)girl);
                        dragonL.setPositionAndUpdate(posL.x, posL.y, posL.z);
                        dragonL.SCALE_1_0 = scale;
                        renderManager.renderEntity(dragonL, 0.0, 0.0, 0.0, 0.0F, partialTicks, true);
                        dragonL.setPosition(0.0, -500.0, 0.0);
                        dragonL.setDead();
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException ex) {
            // ignored: entity list mutated during iteration; retried on the next tick
         }

         GlStateManager.enableLighting();
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
      }

         /**
       * SERVER: checks the bed-side space requirement for the morning blowjob —
       * the block south/east/north/west of the bed head (and one above) must be
       * free of solid blocks, else the scene cannot spawn.
       */
      boolean spawnStructure(World world, BlockPos pos, EnumFacing facing) {
         if (facing == EnumFacing.NORTH) {
            pos = pos.west();
            if (this.isValidFlightBlock(world, pos)) {
               return false;
            } else if (this.isValidFlightBlock(world, pos.up())) {
               return false;
            } else {
               return this.isValidFlightBlock(world, pos.south()) ? false : !this.isValidFlightBlock(world, pos.south().up());
            }
         } else if (facing == EnumFacing.WEST) {
            pos = pos.south();
            if (this.isValidFlightBlock(world, pos)) {
               return false;
            } else if (this.isValidFlightBlock(world, pos.up())) {
               return false;
            } else {
               return this.isValidFlightBlock(world, pos.east()) ? false : !this.isValidFlightBlock(world, pos.east().up());
            }
         } else if (facing == EnumFacing.SOUTH) {
            pos = pos.east();
            if (this.isValidFlightBlock(world, pos)) {
               return false;
            } else if (this.isValidFlightBlock(world, pos.up())) {
               return false;
            } else {
               return this.isValidFlightBlock(world, pos.north()) ? false : !this.isValidFlightBlock(world, pos.north().up());
            }
         } else if (facing == EnumFacing.EAST) {
            pos = pos.north();
            if (this.isValidFlightBlock(world, pos)) {
               return false;
            } else if (this.isValidFlightBlock(world, pos.up())) {
               return false;
            } else {
               return this.isValidFlightBlock(world, pos.west()) ? false : !this.isValidFlightBlock(world, pos.west().up());
            }
         } else {
            Main.LOGGER.error("Weird bed orientation, when checking for space next to bed, on galaths morning blowjob animation: " + facing.getName());
            return false;
         }
      }

      boolean isValidFlightBlock(World world, BlockPos pos) {
         Block block = world.getBlockState(pos).getBlock();

         for (Class blockClass : GalathEntity.NON_SOLID_FLIGHT_BLOCKS) {
            if (blockClass.isInstance(block)) {
               return false;
            }
         }

         return true;
      }

      @SubscribeEvent
      public void onWake(PlayerWakeUpEvent event) {
         EntityPlayer player = event.getEntityPlayer();
         if (!player.world.isRemote) {
            if (OwnershipSavedData.shouldDespawn(player.getPersistentID(), player.world)) {
               Vec3d pos = player.getPositionVector();
               BlockPos blockPos = new BlockPos(pos);
               EnumFacing facing = (EnumFacing)player.world.getBlockState(blockPos).getValue(BlockHorizontal.FACING);
               if (!this.spawnStructure(player.world, blockPos, facing)) {
                  player.sendMessage(
                     new TextComponentString(
                        String.format(
                           "%sFor Galath and Manglelie to %swake you up with a blowjob%s, you have to provide enough space to the %sright side%s of your bed. This includes the %stop and bottom half%s of the bed.",
                           TextFormatting.GRAY,
                           TextFormatting.DARK_RED,
                           TextFormatting.GRAY,
                           TextFormatting.DARK_RED,
                           TextFormatting.GRAY,
                           TextFormatting.DARK_RED,
                           TextFormatting.GRAY
                        )
                     )
                  );
               } else {
                  float yaw;
                  switch ((EnumFacing)player.world.getBlockState(blockPos).getValue(BlockHorizontal.FACING)) {
                     case NORTH:
                        yaw = 180.0F;
                        break;
                     case EAST:
                        yaw = -90.0F;
                        break;
                     case WEST:
                        yaw = 90.0F;
                        break;
                     default:
                        yaw = 0.0F;
                  }

                  Vec3d spawnPos = new Vec3d(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
                  UUID ownerUuid = OwnershipSavedData.getOwnerOf(player);
                  if (ownerUuid != null) {
                     OwnershipSavedData.updateMangleliePartner((GalathEntity)BaseGirlEntity.getServerGirlEntity(ownerUuid));
                  }

                  GalathEntity galath = new GalathEntity(player.world, player, pos, true);
                  galath.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  player.world.spawnEntity(galath);
                  OwnershipSavedData.grantOwnership(player, galath);
                  galath.canStartPussyLicking();
                  galath.setTargetPosition(spawnPos);
                  galath.setYawRotation(yaw);
                  galath.setAnchored(true);
                  galath.setInteractionPlayerUUID(player.getPersistentID());
                  galath.setCurrentAction(Action.MORNING_BLOWJOB_SLOW);
                  PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(false), (EntityPlayerMP)player);
                  ThreadNames.createDaemonThread(500, () -> {
                     player.setPositionAndUpdate(spawnPos.x, spawnPos.y, spawnPos.z);
                     PacketHandler.networkWrapper.sendTo(new SetSceneCameraPacket(-10.0F, yaw + 180.0F + 5.0F, 0), (EntityPlayerMP)player);
                  });
               }
            }
         }
      }

   }


   // ------------------------------------------------------------------
   // Entity behaviour merged from the per-entity static helper classes.
   // One entity, one place: state, AI, combat, riding and scenes stay local.
   // ------------------------------------------------------------------



   /**
    * SERVER: the state-machine gate — guards the cum loops and
    * GALATH_DE_SUMMON, persists the cum time via {@link GirlSavedData} for
    * the tamed cum scenes, and fires the GIVE_COIN
    * ({@link #grantCoinAndBindMaster(GalathEntity)}), HUG_MANG
    * ({@link #endHugScene(GalathEntity)}) and MORNING_BLOWJOB_CUM
    * ({@link #endMorningBlowjobScene(GalathEntity)}) teardown hooks.
    */
   void setCurrentActionGuarded(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction == Action.GALATH_DE_SUMMON) {
         return;
      }

      if (currentAction == Action.CORRUPT_CUM && (action == Action.CORRUPT_FAST || action == Action.CORRUPT_SLOW)) {
         return;
      }

      if (currentAction == Action.RAPE_CUM && action == Action.RAPE_ON_GOING) {
         return;
      }

      if (currentAction == Action.MORNING_BLOWJOB_CUM && (action == Action.MORNING_BLOWJOB_SLOW || action == Action.MORNING_BLOWJOB_FAST)) {
         return;
      }

      if (!this.world.isRemote && Action.isAny(currentAction, Action.CORRUPT_CUM, Action.RAPE_CUM, Action.MORNING_BLOWJOB_CUM)) {
         OwnershipSavedData.saveCumTime(this.getInteractionPlayerUUID(), this.world.getTotalWorldTime());
      }

      if (action == Action.CORRUPT_SLOW) {
         this.corruptHard = false;
         if (currentAction == Action.CORRUPT_INTRO) {
            setFlyingImpl(false);
         }

         if (this.hasMaster() && currentAction == Action.NULL) {
            setFlyingImpl(true);
         }
      }

      if (currentAction == Action.GIVE_COIN && action == Action.NULL && !this.world.isRemote) {
         grantCoinAndBindMasterImpl();
      }

      if (currentAction == Action.HUG_MANG && action == Action.NULL) {
         endHugSceneImpl();
      }

      if (currentAction == Action.MORNING_BLOWJOB_CUM && action == Action.NULL) {
         endMorningBlowjobSceneImpl();
      }

      this.setCurrentActionBase(action);
   }



   /**
    * SERVER: ends the rape scene — once the RAPE_ON_GOING loop leaves for
    * RAPE_CUM she switches to flight repositioning (CHANGE_POSITION) and
    * releases the player.
    */
   void handleRapeStateImpl() {
      Action action = this.getCurrentAction();
      if (action == Action.RAPE_ON_GOING) {
         this.activeFlight = GalathFlightData.CHANGE_POSITION;
         this.activeFlight.executeStart(this);
         this.setAnchored(false);
         this.setCurrentAction(Action.FLY);
         EntityPlayer player = this.getPlayerEntity();
         this.setInteractionPlayerUUID(null);
         if (player != null) {
            PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(true), (EntityPlayerMP)player);
         }

         BaseGirlEntity.girlPlaySound(this, SoundHandler.GIRLS_GALATH_DIALOG[0]);
      }
   }



   /**
    * SERVER: the morning-blowjob teardown — resets the bound player and the
    * girl after MORNING_BLOWJOB_CUM.
    */
   void endMorningBlowjobSceneImpl() {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      ResetGirlPacket.Handler.resetGirl(this);
   }



   void endHugSceneImpl() {
      this.setAnchored(false);
      ManglelieEntity manglelie = this.getMangleliePartner(true);
      if (manglelie != null) {
         manglelie.setCorrupting(true);
      }
   }



   /**
    * SERVER: the coin grant — swaps the player's main hand for the
    * {@link GalathCoinItem}, unbinds the scene, binds her to the player as
    * master and explains the coin mechanic in chat.
    */
   void grantCoinAndBindMasterImpl() {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         ItemStack stack = player.getHeldItemMainhand();
         player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(GalathCoinItem.GALATH_COIN));
         if (!stack.isEmpty()) {
            player.inventory.addItemStackToInventory(stack);
         }

         PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(true), (EntityPlayerMP)player);
         this.setInteractionPlayerUUID(null);
         this.setTargetEntity(null);
         player.sendMessage(
            new TextComponentString(
               TextFormatting.GRAY
                  + "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin"
            )
         );
         OwnershipSavedData.updateMangleliePartner(this);
         player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
      }
   }



   void setFlyingImpl(boolean flying) {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         Vec3d pos;
         if (flying) {
            pos = new Vec3d(-0.5, 0.5F - player.getEyeHeight(), 0.4F).add(this.getTargetPosition());
         } else {
            pos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
         }

         player.setPositionAndUpdate(pos.x, pos.y, pos.z);
      }
   }



   boolean canStartPussyLickingImpl() {
      if (this.getMangleliePartner(true) != null) {
         return false;
      }

      ManglelieEntity manglelie = new ManglelieEntity(this.world);
      this.setMangleliePartnerUUID(manglelie.getGirlId());
      manglelie.setGalathPartnerUUID(this.getGirlId());
      manglelie.setCorrupting(true);
      manglelie.setCurrentAction(Action.RIDE_MOMMY_HEAD);
      manglelie.setPositionAndUpdate(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(manglelie);
      return true;
   }



   void startMasterbateSittingImpl() {
      if (this.getCurrentAction() != Action.MASTERBATE_SITTING) {
         this.masterbateSittingPending = true;
         this.setCurrentAction(Action.MASTERBATE_SITTING);
      }
   }



   void startPussyLickingImpl() {
      this.pussyLickingForward = true;
      this.setCurrentAction(Action.PUSSY_LICKING);
   }



   void resetInteractionStateImpl() {
      this.setInteractionPlayerUUID(null);
      this.setCurrentAction((Action)null);
   }



   boolean handleActionAnimationOverridesImpl(Action action, String animName, boolean started, AnimationEvent event) {
      if (action == Action.MASTERBATE_SITTING && this.masterbateSittingPending) {
         this.masterbateSittingPending = false;
         this.createAnimation("animation.this.masterbating_sitting", true, event, true);
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_FAST && this.morningBlowjobStarted) {
         this.setCurrentAction(Action.MORNING_BLOWJOB_CUM);
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_FAST && this.bedSoftAnimation) {
         this.createAnimation("animation.shared.bed_fast", true, event, true);
         this.bedSoftAnimation = false;
         return true;
      }

      if (action == Action.MORNING_BLOWJOB_CUM) {
         this.setCurrentAction((Action)null);
         return true;
      }

      if (action == Action.PUSSY_LICKING && this.pussyLickingForward) {
         this.pussyLickingForward = false;
         this.createAnimation("animation.this.pussy_licking", true, event, true);
         return true;
      }

      if (action != Action.MORNING_BLOWJOB_SLOW || !this.morningBlowjobStarted && !PlayerMovementController.isJumping) {
         if (action == Action.MORNING_BLOWJOB_SLOW && this.bedBackAnimation) {
            this.bedBackAnimation = false;
            this.createAnimation("animation.shared.bed_slow", true, event, true);
            return true;
         } else if (action == Action.MORNING_BLOWJOB_FAST && !PlayerMovementController.isJumping) {
            this.setCurrentAction(Action.MORNING_BLOWJOB_SLOW);
            this.bedBackAnimation = true;
            this.createAnimation("animation.shared.bed_back", true, event, true);
            return true;
         } else {
            return false;
         }
      } else {
         this.bedSoftAnimation = true;
         this.setCurrentAction(Action.MORNING_BLOWJOB_FAST);
         this.createAnimation("animation.shared.bed_soft", true, event, true);
         return true;
      }
   }



   float getSwordAttackProgressImpl(float partialTicks) {
      Action action = this.getCurrentAction();
      if (action == Action.PUSSY_LICKING && !this.pussyLickingForward) {
         return 0.0F;
      }

      if (action == Action.MASTERBATE_SITTING && !this.masterbateSittingPending) {
         return 1.0F;
      }

      float scale = Action.getActionTimeScale(this, partialTicks);
      return action == Action.MASTERBATE_SITTING ? scale : 1.0F - scale;
   }



   <E extends IAnimatable> PlayState animationPredicateImpl(AnimationEvent<E> event) {
      if (this.isLocallyRegistered()) {
         this.createAnimation("animation.this.idle", true, event);
         return PlayState.CONTINUE;
      }

      Action action = this.getCurrentAction();
      AnimationController controller = event.getController();
      controller.setAnimationSpeed(1.0);
      if (controller.equals(this.eyesController)) {
         if (action.autoBlink && action != Action.GALATH_DE_SUMMON) {
            this.createAnimation("animation.this.blink", true, event);
            return PlayState.CONTINUE;
         } else {
            return PlayState.STOP;
         }
      } else if (controller.equals(this.movementController)) {
         if (action != Action.NULL) {
            return PlayState.STOP;
         } else if (!this.onGround) {
            this.createAnimation("animation.this.controlled_flight", true, event);
            return PlayState.CONTINUE;
         } else {
            Vec3d movement = this.getPositionVector().subtract(new Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ));
            if (movement.equals(Vec3d.ZERO)) {
               this.createAnimation("animation.this.idle", true, event);
               return PlayState.CONTINUE;
            } else {
               this.rotationYaw = this.rotationYawHead;
               this.createAnimation("animation.this." + (this.entityDataManager.get(GalathEntity.SPRINTING) ? "run" : "walk"), true, event);
               return PlayState.CONTINUE;
            }
         }
      } else {
         if ((action == Action.RAPE_INTRO || action == Action.RAPE_ON_GOING)
               && this.isControlledByLocalPlayer() && !this.hasMaster()) {
            EscapeMinigameHud.ensureVisible(this);
         }

         switch (this.getCurrentAction()) {
            case HUG_MANG:
               this.createAnimation("animation.this.hug_mang", true, event);
               break;
            case MORNING_BLOWJOB_SLOW:
               this.createAnimation(this.bedBackAnimation ? "animation.shared.bed_back" : "animation.shared.bed_slow", true, event);
               break;
            case MORNING_BLOWJOB_FAST:
               if (this.bedSoftAnimation) {
                  this.createAnimation("animation.shared.bed_soft", true, event);
               } else {
                  this.playRandomizedAnimation("animation.shared.bed_fast", 4, 0.75F, event);
               }
               break;
            case MORNING_BLOWJOB_CUM:
               this.createAnimation("animation.shared.bed_cum", true, event);
               break;
            case RAPE_INTRO:
               this.createAnimation("animation.this.rape_intro", true, event);
               break;
            case RAPE_ON_GOING:
               this.createAnimation("animation.this.rape" + this.rapeVariant, true, event);
               break;
            case RAPE_CUM:
               this.createAnimation("animation.this.rape_cum", true, event);
               break;
            case RAPE_CHARGE:
               this.createAnimation("animation.this.rape_charge", true, event);
               break;
            case RAPE_CUM_IDLE:
               this.createAnimation("animation.this.rape_cum_idle", true, event);
               break;
            case CORRUPT_SLOW:
               this.createAnimation("animation.this.corrupt_slow", true, event);
               break;
            case CORRUPT_FAST:
               this.createAnimation("animation.this.corrupt_" + (this.corruptHard ? "hard" : "soft"), true, event);
               break;
            case CORRUPT_CUM:
               this.createAnimation("animation.this.corrupt_cum", true, event);
               break;
            case MASTERBATE:
               this.createAnimation("animation.this.masterbate", true, event);
               break;
            case RAPE_PREPARE:
               this.createAnimation("animation.this.rape_prepare", true, event);
               break;
            case CORRUPT_INTRO:
               this.createAnimation("animation.this.corrupt_intro", true, event);
            case COWGIRLCUM:
            default:
               break;
            case NULL:
               return PlayState.STOP;
            case FLY:
               this.createAnimation("animation.this.idle_flying", true, event);
               break;
            case SUMMON_SKELETON:
               this.createAnimation("animation.this.summon_skeleton" + (this.entityDataManager.get(GalathEntity.MIRROR_SIDE) ? "Mirrored" : ""), true, event);
               break;
            case ATTACK_SWORD:
               this.createAnimation("animation.this.attack", true, event);
               break;
            case KNOCK_OUT_FLY:
               controller.setAnimationSpeed(1.5);
               this.createAnimation("animation.this.knockout_air", true, event);
               break;
            case KNOCK_OUT_GROUND:
               this.createAnimation("animation.this.knocked_out", true, event);
               break;
            case KNOCK_OUT_STAND_UP:
               this.createAnimation("animation.this.knocked_out_stand_up", true, event);
               break;
            case CONTROLLED_FLIGHT:
               this.createAnimation("animation.this.controlled_flight", true, event);
               break;
            case BOOST:
               this.createAnimation("animation.this.boost", true, event);
               break;
            case GALATH_SUMMON:
               this.createAnimation("animation.this.summon", false, event);
               break;
            case GALATH_DE_SUMMON:
               this.createAnimation("animation.this.desummon" + (this.onGround ? "_standing" : ""), true, event);
               break;
            case GIVE_COIN:
               this.createAnimation("animation.this.give_coin", true, event);
               break;
            case RUN:
               controller.setAnimationSpeed(0.7);
               this.createAnimation("animation.this.running", true, event);
               break;
            case PUSSY_LICKING:
               this.createAnimation(this.pussyLickingForward ? "animation.this.pussy_licking_forward" : "animation.this.pussy_licking", true, event);
               break;
            case MASTERBATE_SITTING:
               this.createAnimation(this.masterbateSittingPending ? "animation.this.pussy_licking_back" : "animation.this.masterbating_sitting", true, event);
               break;
            case MASTERBATE_SITTING_CUM:
               this.createAnimation("animation.this.masterbating_sitting_cum", true, event);
         }

         return PlayState.CONTINUE;
      }
   }



   /**
    * CLIENT: the sound-keyframe listener driving the wild fight (charge
    * sounds, sword render toggles, stars) and the tamed scenes (rape
    * switch/UI, corrupt switch, coin cam, morning blowjob, creampie trails).
    * {@code blackScreenTamed} only black-screens wild girls;
    * {@code flapControlled} sends the flight input as
    * {@link UpdateVelocityPacket}.
    */
   @SideOnly(Side.CLIENT)
   void handleSoundKeyframe(SoundKeyframeEvent<?> sound) {
      switch (sound.sound) {
         case "goodTiming":
            this.playSound(SoundHandler.GIRLS_GALATH_DIALOG[4]);
            this.sendChatMessage("Good timing boy~");
            break;
         case "huh":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_HUH);
            break;
         case "giggle":
            Vec3d vec = this.getVectorTowardPlayer();
            this.world
               .playSound(
                  vec.x,
                  vec.y,
                  vec.z,
                  SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_GIGGLE),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "dialog1":
            this.playSound(SoundHandler.GIRLS_GALATH_DIALOG[1]);
            break;
         case "moan":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_MOAN);
            break;
         case "breath":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_BREATHING);
            break;
         case "dialog5":
            this.playSound(SoundHandler.GIRLS_GALATH_DIALOG[5]);
            break;
         case "switchmoan":
            if (this.alternateMoan) {
               this.playRandomSound(SoundHandler.GIRLS_GALATH_BREATHING);
            } else {
               this.playRandomSound(this.getRNG().nextBoolean() ? SoundHandler.GIRLS_GALATH_MOAN : SoundHandler.GIRLS_GALATH_AHH);
            }

            this.alternateMoan = !this.alternateMoan;
            break;
         case "lightcharge":
Vec3d aimVec = this.getVectorTowardPlayer();
            this.world
               .playSound(
                  aimVec.x,
                  aimVec.y,
                  aimVec.z,
                  SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_LIGHTCHARGE),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "strongcharge":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_STRONGCHARGE);
            break;
         case "hmph":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_HMPH);
            break;
         case "cum":
            this.playRandomSoundAtVolume(SoundHandler.MISC_SMALLINSERTS, 2.0F);
            break;
         case "giggle0":
            this.playSound(SoundHandler.GIRLS_GALATH_GIGGLE[0]);
            break;
         case "orgasm":
            this.playRandomSound(SoundHandler.GIRLS_GALATH_ORGASM);
            break;
         case "pound":
            this.playRandomSound(SoundHandler.MISC_POUNDING);
            break;
         case "flap":
Vec3d flapVec = this.getVectorTowardPlayer();
            this.world
               .playSound(
                  flapVec.x,
                  flapVec.y,
                  flapVec.z,
                  SoundHandler.randomSound(SoundHandler.MISC_FLAP),
                  SoundCategory.HOSTILE,
                  1.0F,
                  1.0F,
                  false
               );
            break;
         case "startRenderSword":
            this.renderSword = true;
            this.spawnSwordParticles = true;
            break;
         case "stopFadeInParticles":
            this.spawnSwordParticles = false;
            break;
         case "stopRenderSword":
            this.renderSword = false;
            this.spawnSwordParticles = false;
            break;
         case "dontDrawStars":
            this.drawStars = false;
            break;
         case "setNude":
            this.wingsAnimated = true;
            Vec3d pos = this.getPositionVector();
            Vec3d slipR = this.getCachedBoneOffset("slipR").add(pos);
            Vec3d slipL = this.getCachedBoneOffset("slipL").add(pos);
            Vec3d turnable = this.getCachedBoneOffset("turnable").add(pos);
            this.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipR.x, slipR.y, slipR.z, 0.0, 0.0, 0.0, new int[0]);
            this.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, slipL.x, slipL.y, slipL.z, 0.0, 0.0, 0.0, new int[0]);
            this.world
               .spawnParticle(EnumParticleTypes.DRAGON_BREATH, turnable.x, turnable.y, turnable.z, 0.0, 0.0, 0.0, new int[0]);
            break;
         case "rapeIntroDone":
            if (this.isControlledByLocalPlayer()) {
               this.setCurrentAction(Action.RAPE_ON_GOING);
            }
            break;
         case "rape_switch":
            Random random = this.getRNG();
            int oldState = this.rapeVariant;

            do {
               this.rapeVariant = random.nextInt(3);
            } while (this.rapeVariant == oldState);

            if (!this.hasMaster() && this.isControlledByLocalPlayer()) {
               EntityPlayerSP player = Minecraft.getMinecraft().player;
               if (0.0F >= player.getHealth() - 1.0F) {
                  this.setCurrentAction(Action.RAPE_CUM);
               }
            }
            break;
         case "poundRape":
            this.playRandomSound(SoundHandler.MISC_POUNDING);
            if (this.isControlledByLocalPlayer()) {
               if (this.hasMaster()) {
                  HornyMeterHud.addToHornyMeter(0.03F);
               } else {
                  PacketHandler.networkWrapper.sendToServer(new GalathPouncePacket(true));
               }
            }
            break;
         case "rapeHurt":
            if (!this.hasMaster() && this.isControlledByLocalPlayer()) {
               PacketHandler.networkWrapper.sendToServer(new GalathPouncePacket(false));
            }
            break;
         case "enableRapeUI":
            if (this.isControlledByLocalPlayer()) {
               if (this.hasMaster()) {
                  HornyMeterHud.setHornyMeterVisible(false);
               } else {
                  EscapeMinigameHud.showMinigame(this);
               }
            }
            break;
         case "removeUI":
            if (this.isControlledByLocalPlayer() && !this.hasMaster()) {
               EscapeMinigameHud.failMinigame();
            }
            break;
         case "reloadRenderer":
            if (!this.isControlledByLocalPlayer()) {
               return;
            }

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.gameSettings.thirdPersonView != 0) {
               mc.renderGlobal.loadRenderers();
            }
            break;
         case "corruptSwitch":
            if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
               this.setCurrentAction(Action.CORRUPT_FAST);
            }
            break;
         case "corrupt_hard":
            if (this.isControlledByLocalPlayer() && PlayerMovementController.isJumping) {
               this.corruptHard = true;
               this.resetAnimationControllerOffset();
            }
            break;
         case "corrupt_hard_end":
            this.setCurrentAction(Action.CORRUPT_SLOW);
            this.corruptHard = false;
            break;
         case "addCum":
            HornyMeterHud.addToHornyMeter(0.03);
            break;
         case "clearcum":
            CumTrailOverlay.spawnCummyTrails(this);
            break;
         case "setCamCorrupt":
            if (!this.isControlledByLocalPlayer()) {
               return;
            }

            this.corruptIntroActive = true;
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            float yaw = this.getYawRotation() + 220.0F;
            Vec3d corruptPos = VectorMath.rotateByYaw(new Vec3d(0.5, 0.5F - player.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
            PacketHandler.networkWrapper.sendToServer(new TeleportPlayerPacket(player.getPersistentID().toString(), corruptPos, yaw, 15.0F));
            HornyMeterHud.showHornyMeter();
            break;
         case "enableBoyCam":
            if (this.isControlledByLocalPlayer()) {
               this.corruptIntroActive = false;
            }
            break;
         case "masterbateCumming":
            if (CommandFuta.ENABLED) {
               CumTrailOverlay.registerTrail(new DynamicTrailRenderer(90, girl -> {
                  Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                  Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                  return cockTipPos.subtract(tipDir).normalize();
               }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), this, 0.3F, 0.3F));
            }
            break;
         case "creampie":
            CumTrailOverlay.registerTrail(
               new DynamicTrailRenderer(
                  100,
                  girl -> VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), this.getYawRotation()),
                  girl -> girl.getCachedBoneOffset("creampiePos").add(girl.getTargetPosition()),
                  this,
                  0.6F,
                  0.5F
               )
            );
         case "creampieGalath":
            if (CommandFuta.ENABLED) {
               CumTrailOverlay.registerTrail(new DynamicTrailRenderer(130, girl -> {
                  Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                  Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                  return cockTipPos.subtract(tipDir).normalize();
               }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), this, 0.3F, 0.3F));
            }

            this.playSoundAtVolume(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 3.0F);
            break;
         case "blackScreenTamed":
            if (this.hasMaster()) {
               return;
            }
         case "blackScreen":
            if (this.isControlledByLocalPlayer()) {
               SceneFadeScreen.enableInteraction();
            }
            break;
         case "blackScreenMaster":
            if (Minecraft.getMinecraft().player.getPersistentID().equals(this.getMasterUUID())) {
               SceneFadeScreen.enableInteraction();
               PlayerMovementController.setMovementLock(false);
            }
            break;
         case "flapControlled":
            if (this.isControlledByLocalPlayer()) {
               GalathFlightHud.showHud();
               this.playRandomSound(SoundHandler.MISC_FLAP);
               Minecraft flapMc = Minecraft.getMinecraft();
               EntityPlayerSP flapPlayer = flapMc.player;
               MovementInput input = flapPlayer.movementInput;
               Vec2f moveVec = input.getMoveVector();
               if (moveVec.x != 0.0F || moveVec.y != 0.0F) {
                  Vec3d vel = VectorMath.rotateByYawPitch(
                     new Vec3d(-moveVec.x, 0.0, moveVec.y),
                     RotationHelper.lerp(flapPlayer.prevRotationPitch, flapPlayer.rotationPitch, flapMc.getRenderPartialTicks()),
                     RotationHelper.lerp(flapPlayer.prevRotationYawHead, flapPlayer.rotationYawHead, flapMc.getRenderPartialTicks())
                  );
                  PacketHandler.networkWrapper.sendToServer(new SetGirlVelocityPacket(vel, this.getGirlId()));
               }
            }
            break;
         case "clap":
            this.playRandomSound(SoundHandler.MISC_CLAP);
            break;
         case "energysound":
            this.playSound(SoundHandler.MISC_BEEW[1]);
            break;
         case "energy2":
            this.playSound(SoundHandler.MISC_BEEW[2]);
            break;
         case "tpSound":
            this.playSound(SoundHandler.MISC_WEOWEO[2]);
            break;
         case "lick":
            this.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
            break;
         case "setCoinLook":
            if (this.isControlledByLocalPlayer()) {
               EntityPlayerSP coinPlayer = Minecraft.getMinecraft().player;
               float coinYaw = this.getYawRotation() + 180.0F;
               coinPlayer.rotationYaw = coinYaw;
               coinPlayer.prevRotationYaw = coinYaw;
               coinPlayer.rotationPitch = 0.0F;
               coinPlayer.prevRotationPitch = 0.0F;
            }
            break;
         case "sexui":
            if (this.isControlledByLocalPlayer()) {
               HornyMeterHud.showHornyMeter();
            }
            break;
         case "boostSound":
            Minecraft.getMinecraft().player.playSound(SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_LIGHTCHARGE), 1.0F, 1.0F);
            Minecraft.getMinecraft().player.playSound(SoundHandler.randomSound(SoundHandler.MISC_FLAP), 1.0F, 1.0F);
      }
   }



   boolean attackEntityFromImpl(DamageSource source, float amount) {
      if (source.isFireDamage()) {
         return false;
      }

      if (DamageSource.DROWN.equals(source)) {
         return false;
      }

      if (DamageSource.CACTUS.equals(source)) {
         return false;
      }

      if (DamageSource.FALL.equals(source)) {
         return false;
      }

      if (DamageSource.FLY_INTO_WALL.equals(source)) {
         return false;
      }

      playHurtSoundImpl(amount);
      return this.attackEntityFromBase(source, amount);
   }



   boolean attackEntityFromPartImpl(MultiPartEntityPart part, DamageSource source, float amount) {
      if (this.world.isRemote) {
         return false;
      }

      if (!(source.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (part == this.energyBallHitboxRight) {
         this.getDataManager().set(GalathEntity.RIGHT_ENERGY_BALL_ALIVE, false);
         PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(this.energyBallHitboxRight.getPositionVector(), false), this);
      }

      if (part == this.energyBallHitboxLeft) {
         this.getDataManager().set(GalathEntity.LEFT_ENERGY_BALL_ALIVE, false);
         PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(this.energyBallHitboxLeft.getPositionVector(), false), this);
      }

      return true;
   }



   void playHurtSoundImpl(float damage) {
      if (this.world.isRemote) {
         if (!(this.getHealth() - damage <= 0.0F)) {
            long now = System.currentTimeMillis();
            if (now >= this.lastHurtSoundTime + 1000L) {
               this.playRandomSound(SoundHandler.GIRLS_GALATH_UUH);
               this.lastHurtSoundTime = now;
            }
         }
      }
   }



   /**
    * SERVER: per-tick rape damage — while RAPE_ON_GOING/RAPE_INTRO the bound
    * player takes 1 HP (non-creative, survives at 1), and Galath heals 1.5
    * per pulse when {@code applyDamage} is set.
    */
   void handleRapeActionImpl(boolean applyDamage) {
      Action action = this.getCurrentAction();
      if (action == Action.RAPE_ON_GOING || action == Action.RAPE_INTRO) {
         EntityPlayer player = this.getPlayerEntity();
         if (player != null) {
            if (!(0.0F >= player.getHealth() - 1.0F)) {
               if (!player.capabilities.isCreativeMode) {
                  player.attackEntityFrom(new SuccubusDamageSource(this), 1.0F);
                  if (applyDamage) {
                     this.heal(1.5F);
                  }
               }
            }
         }
      }
   }



   /**
    * SERVER: places the two energy-ball hitbox parts at the summoning hands
    * while the summon action is charging ({@code summonSkeletonTicks} in [9, 30]); both parts
    * are inactive outside that window.
    */
   void resetEnergyBalls() {
      this.energyBallHitboxLeft.isActive = false;
      this.energyBallHitboxRight.isActive = false;
      if (!(this.summonSkeletonTicks < 9.0F)) {
         if (!(this.summonSkeletonTicks > 30.0F)) {
            this.energyBallHitboxLeft.isActive = true;
            this.energyBallHitboxRight.isActive = true;
            boolean reset = (Boolean)this.getDataManager().get(GalathEntity.MIRROR_SIDE);
            Vec3d pos = this.getPositionVector();
            Vec3d tempPos;
            Vec3d tempOffset;
            if (reset) {
               tempPos = pos;
               tempOffset = VectorMath.MirrorXZ(GalathEntity.LEFT_ENERGY_BALL_OFFSET);
            } else {
               tempPos = pos;
               tempOffset = GalathEntity.LEFT_ENERGY_BALL_OFFSET;
            }

            Vec3d leftPos = tempPos.add(VectorMath.rotateByYaw(tempOffset, 180.0F + this.renderYawOffset));
            Vec3d rightPos = this.getPositionVector();
            if (reset) {
               tempPos = rightPos;
               tempOffset = VectorMath.MirrorXZ(GalathEntity.RIGHT_ENERGY_BALL_OFFSET);
            } else {
               tempPos = rightPos;
               tempOffset = GalathEntity.RIGHT_ENERGY_BALL_OFFSET;
            }

            Vec3d mirrorRightPos = tempPos.add(VectorMath.rotateByYaw(tempOffset, 180.0F + this.renderYawOffset));
            this.energyBallHitboxLeft.setLocationAndAngles(leftPos.x, leftPos.y, leftPos.z, this.renderYawOffset, 0.0F);
            this.energyBallHitboxRight.setLocationAndAngles(mirrorRightPos.x, mirrorRightPos.y, mirrorRightPos.z, this.renderYawOffset, 0.0F);
            this.energyBallHitboxLeft.onUpdate();
            this.energyBallHitboxRight.onUpdate();
         }
      }
   }



   void tickSummonSkeletonProgress() {
      if (this.getCurrentAction() != Action.SUMMON_SKELETON) {
         this.summonSkeletonTicks = 0;
      } else {
         if (this.summonSkeletonTicks++ > 45) {
            this.summonSkeletonTicks = 0;
         }
      }
   }



   void despawnFarSummonedSkeletons() {
      EntityLivingBase target = this.getTargetEntity();
      if (target != null) {
         for (EntityWitherSkeleton skeleton : this.summonedSkeletons) {
            if (!skeleton.isDead && !(target.getDistance(skeleton) < 15.0F)) {
               PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(skeleton.getPositionVector(), true), this);
               skeleton.setDead();
               this.world.removeEntity(skeleton);
            }
         }
      }
   }



   void despawnSummonedSkeletons() {
      if ((Boolean)this.getDataManager().get(GalathEntity.PARALYZED)) {
         for (EntityWitherSkeleton skeleton : this.summonedSkeletons) {
            if (!skeleton.isDead) {
               PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(skeleton.getPositionVector(), true), this);
               skeleton.setDead();
               this.world.removeEntity(skeleton);
            }
         }

         this.summonedSkeletons.clear();
      }
   }



   void spawnSkeletonBreathParticles() {
      for (EntityWitherSkeleton skeleton : this.summonedSkeletons) {
         if (!skeleton.isDead && skeleton.ticksExisted % 10 == 0) {
            for (EntityPlayer player : (java.util.Collection<EntityPlayer>) ((WorldServer)this.world).getEntityTracker().getTrackingPlayers(skeleton)) {
               ((EntityPlayerMP)player)
                  .connection
                  .sendPacket(
                     new SPacketParticles(
                        EnumParticleTypes.DRAGON_BREATH,
                        false,
                        (float)skeleton.posX,
                        (float)skeleton.posY,
                        (float)skeleton.posZ,
                        0.2F * ThreadNames.randomSign(),
                        skeleton.getEyeHeight() / 2.0F,
                        0.2F * ThreadNames.randomSign(),
                        0.0F,
                        5,
                        new int[0]
                     )
                  );
            }
         }
      }
   }



   void pruneDeadSkeletons() {
      ArrayList deadSkeletons = new ArrayList();

      for (EntityWitherSkeleton skeleton : this.summonedSkeletons) {
         if (skeleton.isDead) {
            deadSkeletons.add(skeleton);
         }
      }

      for (EntityWitherSkeleton skeleton : (java.util.Collection<EntityWitherSkeleton>) (deadSkeletons) ) {
         this.summonedSkeletons.remove(skeleton);
      }
   }



   void tickKnockoutStandUp() {
      if (this.getCurrentAction() == Action.KNOCK_OUT_STAND_UP) {
         this.standUpTicks++;
         if (this.standUpTicks == 39.0) {
            this.setNoGravity(true);
            this.setVelocity(0.0, 0.6F, 0.0);
            Vec3d pos = this.getPositionVector();
            Vec3d min = pos.subtract(2.0, 2.0, 2.0);
            Vec3d max = pos.add(2.0, 2.0, 2.0);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );

            for (EntityLivingBase entity : this.world.getEntitiesWithinAABB(EntityLivingBase.class, aabb)) {
               if (!(entity instanceof GalathEntity)) {
                  Vec3d entityPos = entity.getPositionVector();
                  Vec3d dir = entityPos.subtract(pos).normalize();
                  entity.motionX = dir.x * 1.0;
                  entity.motionZ = dir.z * 1.0;
                  entity.motionY = 1.0;
                  entity.attackEntityFrom(new GalathDamageSource(this), 0.5F);
                  if (entity instanceof EntityPlayerMP) {
                     EntityPlayerMP playerMP = (EntityPlayerMP)entity;
                     playerMP.connection.sendPacket(new SPacketEntityVelocity(playerMP));
                  }
               }
            }
         }

         if (!(this.standUpTicks < 58.0)) {
            this.setVelocity(Vec3d.ZERO);
            this.getDataManager().set(GalathEntity.PARALYZED, false);
            this.standUpTicks = 0;
         }
      }
   }



   void handleKnockout() {
      if (this.getCurrentAction() == Action.KNOCK_OUT_GROUND) {
         if (!(Boolean)this.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
            if (!(++this.knockoutGroundTicks < 50.0)) {
               this.setCurrentAction(Action.KNOCK_OUT_STAND_UP);
               this.standUpTicks = 0;
               this.knockoutGroundTicks = 0;
            }
         }
      }
   }



   void freezeDuringKnockout() {
      Action action = this.getCurrentAction();
      if (action == Action.KNOCK_OUT_GROUND || action == Action.KNOCK_OUT_STAND_UP) {
         this.motionX = 0.0;
         this.motionZ = 0.0;
         if ((Boolean)this.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
            this.motionY = 0.0;
         }
      }
   }



   void tickKnockoutFlight() {
      if (this.getCurrentAction() == Action.KNOCK_OUT_FLY) {
         BlockPos pos = this.getPosition();
         if (!(this.world.getBlockState(pos).getBlock() instanceof BlockLiquid)) {
            if (this.onGround) {
               this.setCurrentAction(Action.KNOCK_OUT_GROUND);
            }
         } else {
            BlockPos surfacePos = pos;

            while (this.world.getBlockState(surfacePos.up()).getBlock() instanceof BlockLiquid) {
               surfacePos = surfacePos.up();
            }

            for (int x = -1; x < 2; x++) {
               for (int z = -1; z < 2; z++) {
                  this.world.setBlockState(surfacePos.add(x, 0, z), Blocks.OBSIDIAN.getDefaultState());
               }
            }

            surfacePos = surfacePos.up();
            this.setPositionAndUpdate(surfacePos.getX(), surfacePos.getY(), surfacePos.getZ());
            this.setTargetPosition(new Vec3d(surfacePos));
            PacketHandler.networkWrapper.sendToAllTracking(new DragonBreathParticlesPacket(new Vec3d(surfacePos), true), this);

            for (EntityPlayer player : (java.util.Collection<EntityPlayer>) ((WorldServer)this.world).getEntityTracker().getTrackingPlayers(this)) {
               ((EntityPlayerMP)player)
                  .connection
                  .sendPacket(
                     new SPacketSoundEffect(
                        SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, this.posX, this.posY, this.posZ, 1.0F, 1.0F
                     )
                  );
            }

            this.setCurrentAction(Action.KNOCK_OUT_GROUND);
         }
      }
   }



   void updateChangePositionNoClip() {
      if (this.activeFlight == GalathFlightData.CHANGE_POSITION) {
         int progress = this.getAttackProgress();
         this.noClip = progress == 0;
         if (!this.world.isAirBlock(this.getPosition())) {
            this.noClip = true;
         }
      }
   }



   void checkFlightFinished() {
      if (this.activeFlight != null) {
         this.activeFlight.checkFinished(this);
      }
   }



   void tickFlightStateMachine() {
      if (this.getTargetEntity() == null) {
         clearActiveFlight();
      } else if (this.activeFlight == null) {
         initFlightData();
      } else {
         if (this.activeFlight.executeUpdate(this)) {
            initFlightData();
         }
      }
   }



   /**
    * SERVER: selects and starts the next flight action — if a scene player is
    * bound the current action stops; otherwise a random executable
    * {@link GalathFlightData} action is started (with the attack cooldown
    * collapsing into CHANGE_POSITION when it applies).
    */
   void initFlightData() {
      if (!(Boolean)this.getDataManager().get(GalathEntity.PARALYZED)) {
         GalathFlightData flightData = this.activeFlight;
         if (this.getInteractionPlayerUUID() != null) {
            if (flightData != null) {
               flightData.updateFlight(this);
            }

            this.activeFlight = null;
         } else if (flightData != null && flightData.applyAttackCoolDown) {
            flightData.updateFlight(this);
            this.activeFlight = GalathFlightData.CHANGE_POSITION;
            this.activeFlight.executeStart(this);
         } else {
            GalathFlightData[] values = GalathFlightData.values();

            GalathFlightData chosen;
            do {
               chosen = values[this.getRNG().nextInt(values.length)];
            } while (!canInitFlight(chosen));

            this.activeFlight = chosen;
            if (flightData != null) {
               flightData.updateFlight(this);
            }

            this.activeFlight.executeStart(this);
         }
      }
   }



   boolean canInitFlight(GalathFlightData flightData) {
      return flightData.onlyDoThisOnPlayers && !(this.getTargetEntity() instanceof EntityPlayer) ? false : flightData.canExecute(this);
   }



   void clearActiveFlight() {
      this.activeFlight = null;
   }



   /**
    * SERVER: acquires a flight target — the nearest player (wild) or mob
    * (tamed) within the search box, and switches to CHANGE_POSITION to
    * approach it. See {@link #getPlayerInBox(GalathEntity, AxisAlignedBB)} /
    * {@link #getMobInBox(GalathEntity, AxisAlignedBB)}.
    */
   void acquireFlightTarget() {
      if (!hasFlightTarget()) {
         if (this.getInteractionPlayerUUID() == null) {
            boolean hasMaster = this.hasMaster();
            float offset = hasMaster ? 7.0F : 20.0F;
            Vec3d box = new Vec3d(offset, offset, offset);
            Vec3d pos = this.getPositionVector();
            Vec3d min = pos.subtract(box);
            Vec3d max = pos.add(box);
            AxisAlignedBB aabb = new AxisAlignedBB(
               min.x, min.y, min.z, max.x, max.y, max.z
            );
            Object target = hasMaster ? getMobInBox(aabb) : getPlayerInBox(aabb);
            if (target == null) {
               clearFlightTarget();
            } else {
               this.setTargetEntity((EntityLivingBase)target);
               BaseGirlEntity.girlPlaySound(this, SoundHandler.GIRLS_GALATH_DIALOG[1], true);
               if (this.activeFlight != null) {
                  this.activeFlight.updateFlight(this);
               }

               this.activeFlight = GalathFlightData.CHANGE_POSITION;
               this.activeFlight.executeStart(this);
            }
         }
      }
   }



   EntityPlayer getPlayerInBox(AxisAlignedBB aabb) {
      List players = this.world
         .getEntitiesWithinAABB(EntityPlayer.class, aabb, player -> !AbstractPlayerGirlEntity.isOwnerPlayer(player) && !player.isCreative() && !player.isSpectator());
      return players.isEmpty() ? null : (EntityPlayer)players.get(0);
   }



   EntityMob getMobInBox(AxisAlignedBB aabb) {
      List mobs = this.world.getEntitiesWithinAABB(EntityMob.class, aabb);
      if (mobs.isEmpty()) {
         return null;
      }

      ArrayList validMobs = new ArrayList();

      for (EntityMob mob : (java.util.Collection<EntityMob>) (mobs) ) {
         if (com.trolmastercard.sexmod.common.entity.ai.MobPredicates.isValidTarget(mob)) {
            validMobs.add(mob);
         }
      }

      Vec3d eyePos = this.getPositionVector().add(0.0, this.getEyeHeight(), 0.0);

      for (EntityMob mob : (java.util.Collection<EntityMob>) (validMobs) ) {
         if (com.trolmastercard.sexmod.common.entity.ai.MobPredicates.isDaylight(this.world, eyePos, mob)) {
            return mob;
         }
      }

      return null;
   }



   void clearFlightTarget() {
      if (this.getTargetEntity() != null) {
         this.setTargetEntity(null);
         if (this.activeFlight != null) {
            this.activeFlight.updateFlight(this);
         }

         this.activeFlight = null;
         if (!(Boolean)this.getDataManager().get(GalathEntity.PARALYZED)) {
            this.setCurrentAction(Action.NULL);
         }
      }
   }



   boolean hasFlightTarget() {
      EntityLivingBase target = this.getTargetEntity();
      if (target == null) {
         return false;
      } else if (target.isDead) {
         return false;
      } else if (target.dimension != this.dimension) {
         return false;
      } else {
         float dist = this.getDistance(target);
         float maxDist = this.hasMaster() ? 16.0F : 30.0F;
         if (dist > maxDist) {
            return false;
         } else if (!(target instanceof EntityPlayer)) {
            return true;
         } else {
            EntityPlayer player = (EntityPlayer)target;
            if (BaseGirlEntity.getActiveSceneInfo(player.getPersistentID()) != null) {
               return false;
            } else {
               return player.isCreative() ? false : !player.isSpectator();
            }
         }
      }
   }



   void cancelFlightOnLostTarget() {
      if (this.hasMaster()) {
         if (this.getTargetEntity() == null) {
            int attackProgress = (Integer)this.getDataManager().get(GalathEntity.TARGET_ENTITY_ID);
            if (attackProgress != -1) {
               if (this.activeFlight != null) {
                  this.activeFlight.updateFlight(this);
               }

               this.activeFlight = null;
               this.setCurrentAction(Action.NULL);
            }
         }
      }
   }



   boolean processInteractImpl(EntityPlayer player, EnumHand hand) {
      return this.hasMaster() ? processMasterInteract(player, hand) : processGirlInteract(player, hand);
   }



   /**
    * SERVER: tamed interaction — only the master may interact; the action
    * menu depends on held items (no coin) and context: ride while airborne,
    * cowgirl/anal (+ threesome with a Manglelie) on the ground.
    */
   boolean processMasterInteract(EntityPlayer player, EnumHand hand) {
      if (!player.getPersistentID().equals(this.getMasterUUID())) {
         return false;
      }

      if (Action.isAnyAction(this, Action.HUG_MANG, Action.RUN, Action.GALATH_SUMMON, Action.GALATH_DE_SUMMON, Action.MASTERBATE)) {
         return false;
      }

      if (!GalathCoinItem.GALATH_COIN.equals(player.getHeldItem(EnumHand.OFF_HAND).getItem())
         && !GalathCoinItem.GALATH_COIN.equals(player.getHeldItem(EnumHand.MAIN_HAND).getItem())) {
         this.playRandomSound(SoundHandler.GIRLS_GALATH_HUH);
         String[] options;
         if (!player.onGround) {
            options = new String[]{"ride"};
         } else if (this.getMangleliePartner(false) == null) {
            options = new String[]{"cowgirl", "anal", "ride"};
         } else {
            options = new String[]{"cowgirl", "anal", "threesome", "ride"};
         }

         if (this.world.isRemote) {
            BaseGirlEntity.openInventoryGui(player, this.getSelf(), options, false);
         }

         return true;
      } else {
         return false;
      }
   }



   /**
    * CLIENT: master-action dispatch — {@code ride} shows the flight HUD and
    * requests riding; {@code anal}/{@code cowgirl}/{@code threesome} arm the
    * scene after a 1200 ms delay thread (anchor + start the action + bind
    * the player; the threesome also starts Manglelie on
    * {@link Action#THREESOME_SLOW} + {@link Action#PUSSY_LICKING}).
    */
   @SideOnly(Side.CLIENT)
   void doGalathAction(String action, UUID uuid) {
      if ("ride".equals(action)) {
         GalathFlightHud.showHud();
         PacketHandler.networkWrapper.sendToServer(new RequestGalathRidePacket());
      } else if ("anal".equals(action)) {
         SceneFadeScreen.enableInteraction();
         PlayerMovementController.setMovementLock(false);
         ThreadNames.createDaemonThread(1200, () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            this.setTargetPosition(player.getPositionVector());
            this.setYawRotation(0.0F);
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.setAnchored(true);
            this.setCurrentAction(Action.CORRUPT_SLOW);
         });
      } else if ("cowgirl".equals(action)) {
         SceneFadeScreen.enableInteraction();
         PlayerMovementController.setMovementLock(false);
         ThreadNames.createDaemonThread(1200, () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            this.setTargetPosition(player.getPositionVector());
            this.setYawRotation(player.rotationYaw + 180.0F);
            this.setCurrentAction(Action.RAPE_INTRO);
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.setAnchored(true);
         });
      } else {
         if ("threesome".equals(action)) {
            ManglelieEntity manglelie = this.getMangleliePartner(false);
            if (manglelie == null) {
               return;
            }

            SceneFadeScreen.enableInteraction();
            PlayerMovementController.setMovementLock(false);
            ThreadNames.createDaemonThread(1200, () -> {
               Minecraft mc = Minecraft.getMinecraft();
               EntityPlayerSP player = mc.player;
               mc.gameSettings.thirdPersonView = 1;
               manglelie.setTargetPosition(player.getPositionVector());
               this.setTargetPosition(player.getPositionVector());
               manglelie.setYawRotation(player.rotationYaw + 180.0F);
               this.setYawRotation(player.rotationYaw);
               manglelie.setCurrentAction(Action.THREESOME_SLOW);
               this.setCurrentAction(Action.PUSSY_LICKING);
               manglelie.setInteractionPlayerUUID(player.getPersistentID());
               this.setInteractionPlayerUUID(player.getPersistentID());
               manglelie.setAnchored(true);
               this.setAnchored(true);
            });
         }
      }
   }



   /**
    * SERVER: the corruption trigger — a knocked-out (paralyzed) Galath being
    * right-clicked by a non-owner player starts {@link Action#CORRUPT_INTRO}
    * anchored and locks the player in.
    */
   boolean processGirlInteract(EntityPlayer player, EnumHand hand) {
      if (!(Boolean)this.getDataManager().get(GalathEntity.PARALYZED)) {
         return this.processInteractBase(player, hand);
      } else if (this.getCurrentAction() != Action.KNOCK_OUT_GROUND) {
         return this.processInteractBase(player, hand);
      } else if (this.world.isRemote) {
         player.rotationYaw -= -128.0F;
         player.rotationPitch = 19.0F;
         return true;
      } else {
         this.setCurrentAction(Action.CORRUPT_INTRO);
         this.setInteractionPlayerUUID(player.getPersistentID());
         this.setAnchored(true);
         this.setTargetPosition(this.getPositionVector());
         this.setYawRotation(player.rotationYaw);
         PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(false), (EntityPlayerMP)player);
         player.setPositionAndUpdate(this.posX, this.posY, this.posZ);
         return true;
      }
   }



   void write(NBTTagCompound nbt) {
      nbt.setString("sexmod:master", (String)this.getDataManager().get(BaseGirlEntity.MASTER));
      if (this.markedDespawned) {
         nbt.setBoolean("sexmod:despawned", true);
      }
   }



   void read(NBTTagCompound nbt) {
      this.getDataManager().set(BaseGirlEntity.MASTER, nbt.getString("sexmod:master"));
      if (nbt.getBoolean("sexmod:despawned")) {
         this.despawned = true;
      }

      UUID masterUuid = this.getMasterUUID();
      if (masterUuid != null) {
         String npcName = AllieWorldData.getNpcName(masterUuid, NpcType.GALATH);
         if (npcName != null) {
            this.setCustomNameOverride(npcName);
         }
      }
   }



   void updateRidingGravity() {
      this.setNoGravity(this.getRidingPlayer() != null);
   }



   void syncRenderYawToRider() {
      EntityPlayer rider = this.getRidingPlayer();
      if (rider != null) {
         this.prevRenderYawOffset = rider.prevRotationYawHead;
         this.renderYawOffset = rider.rotationYawHead;
      }
   }



   void tickBoostFlight() {
      EntityPlayer player = this.getRidingPlayer();
      Action action = this.getCurrentAction();
      if (player != null) {
         if (action == Action.BOOST) {
            int sideIndex = SideCheck.getInstance() ? 0 : 1;
            if (this.getActionTicks(sideIndex) >= 13) {
               if (this.getActionTicks(sideIndex) == 13) {
                  this.boostSpeed = 6.0F;
               }

               Vec3d look = player.getLook(0.0F).normalize();
               this.motionX = look.x * this.boostSpeed;
               this.motionY = look.y * this.boostSpeed;
               this.motionZ = look.z * this.boostSpeed;
               this.boostSpeed *= 0.94F;
            }
         }
      }
   }



   void updateMasterLookPitch() {
      if (this.world.isRemote) {
         if (this.getRidingPlayer() == null) {
            EntityPlayer master = this.getMasterPlayer();
            if (master != null) {
               handleGalathPlayer(master);
            }
         }
      }
   }



   void handleGalathPlayer(EntityPlayer player) {
      AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
      Vec3d headPos = new Vec3d(player.posX, player.posY + (playerGirl == null ? player.eyeHeight : playerGirl.getEyeHeight()), player.posZ);
      Vec3d eyePos = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double dist = eyePos.distanceTo(headPos);
      double heightDiff = headPos.y - eyePos.y;
      this.rotationPitch = (float)(-(Math.sin(heightDiff / dist) * (180.0 / Math.PI)));
   }



   void handlePlayerRideImpl(EntityPlayer player) {
      int attempts = 0;

      BlockPos teleportPos;
      do {
         teleportPos = player.getPosition().add(Main.RANDOM.nextInt(4), 0, Main.RANDOM.nextInt(4));
      } while (++attempts < 20 && !this.attemptTeleport(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ()));

      if (attempts >= 20) {
         this.setPosition(player.posX, player.posY, player.posZ);
      }

      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
   }



   /**
    * CLIENT: the GIVE_COIN scene — at tick 95 summons the coin for the local
    * player, and during ticks 25-38 spawns the dragon-breath particles
    * streaming between the two hands (progress lerp between the weapon and
    * offhand bones).
    */
   @SideOnly(Side.CLIENT)
   void tickGiveCoinScene() {
      if (this.getCurrentAction() == Action.GIVE_COIN) {
         int coinTick = this.getActionTicks(1);
         if (coinTick == 95) {
            GalathCoinItem.summonForPlayer(Minecraft.getMinecraft().player, this);
         }

         if (coinTick > 25 && coinTick < 38) {
            Vec3d pos = this.getPositionVector();
            Vec3d weaponPos = this.getCachedBoneOffset("weapon").add(pos);
            Vec3d offhandPos = this.getCachedBoneOffset("offhand").add(pos);
            DragonBreathParticle.BREATH_SCALE = 0.5F;

            for (float t = 0.0F; t < 1.0F; t += 0.2F) {
               Vec3d lerped = RotationHelper.lerpVec3dDouble(weaponPos, offhandPos, t);
               Minecraft.getMinecraft().effectRenderer.addEffect(new DragonBreathParticle(this.world, lerped.x, lerped.y, lerped.z));
            }
         }
      }
   }



   /**
    * SERVER: ends the rape scene — 28 ticks into RAPE_CUM she un-anchors,
    * resets to NULL and releases the player (positioned on solid ground with
    * movement restored).
    */
   void handleRapeCum() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == Action.RAPE_CUM) {
            if (this.getActionTicks(0) >= 28) {
               this.setAnchored(false);
               this.setCurrentAction(Action.NULL);
               EntityPlayer player = this.getPlayerEntity();
               this.setInteractionPlayerUUID(null);
               if (player != null) {
                  player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
                  PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(true), (EntityPlayerMP)player);
               }
            }
         }
      }
   }



   void endTamedCorruptScene() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == Action.CORRUPT_CUM) {
            if (this.getActionTicks(0) >= 30) {
               this.setAnchored(false);
               this.setCurrentAction(Action.NULL);
               EntityPlayer player = this.getPlayerEntity();
               this.setInteractionPlayerUUID(null);
               if (player != null) {
                  player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
                  PacketHandler.networkWrapper.sendTo(new LockPlayerMovementPacket(true), (EntityPlayerMP)player);
               }
            }
         }
      }
   }



   void tickWildSystems() {
      GalathEntity.getAimYaw(this, 0.0F);
      this.resetEnergyBalls();
      this.updateFlightRenderAngles();
      spawnSwordAttackParticles();
      clearSwordRenderFlags();
      restoreStarsVisible();
      updateWingsAnimated();
      handleCorruptCum();
      if (this.world.isRemote) {
         tickCorruptIntroCamera();
      }
   }



   /**
    * SERVER: the corrupt-scene ender — 30 ticks into CORRUPT_CUM advances to
    * the coin-give scene ({@link Action#GIVE_COIN}).
    */
   void handleCorruptCum() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == Action.CORRUPT_CUM) {
            if (this.getActionTicks(0) >= 30) {
               this.setCurrentAction(Action.GIVE_COIN);
            }
         }
      }
   }



   void updateWingsAnimated() {
      if ((Boolean)this.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
         this.wingsAnimated = true;
      } else {
         switch (this.getCurrentAction()) {
            case RAPE_INTRO:
            case RAPE_ON_GOING:
            case RAPE_CUM:
            case RAPE_CHARGE:
            case RAPE_CUM_IDLE:
            case CORRUPT_SLOW:
            case CORRUPT_FAST:
            case CORRUPT_CUM:
            case MASTERBATE:
               this.wingsAnimated = true;
            case RAPE_PREPARE:
               return;
            default:
               this.wingsAnimated = false;
         }
      }
   }



   /**
    * CLIENT: re-enables the knock-out star overlay every tick except during
    * {@link Action#KNOCK_OUT_STAND_UP} — the {@code dontDrawStars} animation
    * key clears {@link GalathEntity#drawStars}, this puts it back.
    */
   void restoreStarsVisible() {
      if (this.world.isRemote) {
         if (this.getCurrentAction() != Action.KNOCK_OUT_STAND_UP) {
            this.drawStars = true;
         }
      }
   }



   void updateHealthBar() {
      this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
   }



   void updateGravity() {
      if (!(Boolean)this.getDataManager().get(GalathEntity.PARALYZED)) {
         this.setNoGravity(this.getTargetEntity() != null);
      }
   }



   void clearSwordRenderFlags() {
      if (this.getCurrentAction() != Action.ATTACK_SWORD) {
         this.renderSword = false;
         this.spawnSwordParticles = false;
      }
   }



   /**
    * CLIENT: tracks the energy-ball attack — spawns dragon-breath particles
    * along the weapon bone while the sword is drawn ({@code spawnSwordParticles}).
    */
   void spawnSwordAttackParticles() {
      if (this.world.isRemote) {
         if (this.spawnSwordParticles) {
            Vec3d pos = this.getPositionVector();
            Vec3d startPos = this.getCachedBoneOffset("weaponStart").add(pos);
            Vec3d endPos = this.getCachedBoneOffset("weaponEnd").add(pos);
            float step = 0.1F;
            Random random = this.getRNG();

            for (float t = 0.0F; t < 1.0F; t += step) {
               Vec3d lerped = RotationHelper.lerpVec3dDouble(startPos, endPos, t);

               for (int i = 0; i < 3; i++) {
                  this.world
                     .spawnParticle(
                        EnumParticleTypes.DRAGON_BREATH,
                        lerped.x + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        lerped.y + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        lerped.z + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                        0.0,
                        0.0,
                        0.0,
                        new int[0]
                     );
               }
            }

            for (int i = 0; i < 3; i++) {
               this.world
                  .spawnParticle(
                     EnumParticleTypes.DRAGON_BREATH,
                     endPos.x + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1) * (random.nextBoolean() ? 1 : -1),
                     endPos.y + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                     endPos.z + random.nextDouble() * 0.25 * (random.nextBoolean() ? 1 : -1),
                     0.0,
                     0.0,
                     0.0,
                     new int[0]
                  );
            }
         }
      }
   }



   @SideOnly(Side.CLIENT)
   void tickCorruptIntroCamera() {
      Action action = this.getCurrentAction();
      if (this.previousAction != Action.CORRUPT_INTRO && action == Action.CORRUPT_INTRO) {
         EntityPlayerSP player = Minecraft.getMinecraft().player;
         if (!player.getPersistentID().equals(this.getInteractionPlayerUUID())) {
            this.previousAction = action;
         } else {
            float yaw = this.hasMaster() ? 0.0F : this.getYawRotation() + 180.0F;
            player.rotationYaw = yaw;
            player.prevRotationYaw = yaw;
            player.rotationPitch = 80.0F;
            player.prevRotationPitch = 80.0F;
            this.previousAction = action;
         }
      } else {
         this.previousAction = action;
      }
   }



   void tickWildRapeCum() {
      if (!this.hasMaster()) {
         Action action = this.getCurrentAction();
         if (action != Action.RAPE_CUM) {
            this.rapeCumTicks = 0;
         } else {
            EntityPlayer player = this.getPlayerEntity();
            if (player == null) {
               this.rapeCumTicks = 0;
            } else if (++this.rapeCumTicks == 15) {
               player.attackEntityFrom(new SuccubusDamageSource(this), 2.1474836E9F);
            }
         }
      }
   }



   void tickTamedAi() {
      this.bossBar.setVisible(false);
      if (!OwnershipSavedData.isOwnerNearby(this)) {
         OwnershipSavedData.updateMangleliePartner(this);
      } else if (this.getRidingPlayer() != null) {
         clearFlightData();
      } else {
         handleManglelieOwned();
         if (this.getMangleliePartnerUUID() == null) {
            tickUnpartneredAi();
         } else {
            tickPartneredFollow();
         }
      }
   }



   void handleManglelieOwned() {
      if (OwnershipSavedData.isManglelieOwned(OwnershipSavedData.getManglelieOwnerOf(this))) {
         boolean canLick = this.canStartPussyLicking();
         if (canLick) {
            Main.LOGGER.warn("mommy thinks she got no daughter but she actually does have one. Failsafe called. Hopefully its fixed");
         }
      }
   }



   void tickPartneredFollow() {
      if (!followOwnerOnFoot()) {
         this.getDataManager().set(GalathEntity.SPRINTING, false);
         this.tickFlightSystems();
      }
   }



   boolean followOwnerOnFoot() {
      UUID ownerUuid = OwnershipSavedData.getManglelieOwnerOf(this);
      if (ownerUuid == null) {
         return false;
      }

      EntityPlayer owner = this.world.getPlayerEntityByUUID(ownerUuid);
      if (owner == null) {
         return false;
      }

      BlockPos ownerPos = owner.getPosition();
      if (!isFlightBlocked(ownerPos)) {
         return false;
      }

      if (this.activeFlight != null) {
         this.activeFlight.updateFlight(this);
         this.activeFlight = null;
      }

      float dist = this.getDistance(owner);
      PathNavigate navigator = this.getNavigator();
      if (dist < 4.0F) {
         navigator.clearPath();
         return false;
      }

      if (dist > 16.0F) {
         navigator.clearPath();
         this.handlePlayerRide(owner);
         return true;
      }

      if (PathUtils.getPathEnd(this.currentPath).distanceSq(ownerPos) > 16.0) {
         if (!this.onGround) {
            return true;
         }

         this.currentPath = getPathToPlayer(owner, ownerPos);
         if (this.currentPath == null) {
            this.handlePlayerRide(owner);
         } else {
            navigator.setPath(this.currentPath, 1.0);
         }
      }

      if (this.currentPath != null && !this.currentPath.isFinished()) {
         boolean sprinting = owner.isSprinting() || this.getDistance(owner) > 7.0F;
         double speed = sprinting ? 0.55F : 0.5;
         double extra = Math.floor(dist / 5.0F) * 0.2;
         speed += extra;
         if (this.isInWater()) {
            speed *= 60.0;
         }

         navigator.setSpeed(speed);
         this.getDataManager().set(GalathEntity.SPRINTING, sprinting);
         this.setCurrentAction((Action)null);
         return true;
      } else {
         return false;
      }
   }



   boolean isFlightBlocked(BlockPos pos) {
      if (this.activeFlight == null) {
         return true;
      }

      BlockPos selfPos = this.getPosition();
      int dist = Math.abs(pos.getX() - selfPos.getX()) + Math.abs(pos.getZ() - selfPos.getZ());
      return dist > 16;
   }



   void tickSeekMangleliePartner() {
      if (this.onGround) {
         if (this.getMangleliePartnerUUID() == null) {
            if (this.getCurrentAction() != Action.HUG_MANG) {
               BlockPos center = this.getPosition();
               BlockPos min = center.add(-15.0, -15.0, -15.0);
               BlockPos max = center.add(15.0, 15.0, 15.0);
               AxisAlignedBB aabb = new AxisAlignedBB(min, max);
               List manglelies = this.world.getEntitiesWithinAABB(ManglelieEntity.class, aabb);
               ManglelieEntity chosen = null;

               for (ManglelieEntity manglelie : (java.util.Collection<ManglelieEntity>) (manglelies) ) {
                  if (!manglelie.isDead && manglelie.getGalathPartner(true) == null) {
                     chosen = manglelie;
                     break;
                  }
               }

               if (chosen == null) {
                  if (this.getCurrentAction() == Action.RUN) {
                     this.setCurrentAction((Action)null);
                     this.getNavigator().clearPath();
                  }
               } else {
                  this.pathNavigator = this.getNavigator();
                  if (chosen.getDistance(this) <= 3.65F) {
                     this.pathNavigator.clearPath();
                     this.setCurrentAction(Action.HUG_MANG);
                     this.motionX = 0.0;
                     this.motionY = 0.0;
                     this.motionZ = 0.0;
                     this.setTargetPosition(this.getPositionVector());
                     this.setAnchored(true);
                     this.setMangleliePartnerUUID(chosen.getGirlId());
                     chosen.setGalathPartnerUUID(this.getGirlId());
                     chosen.setCurrentAction(Action.RIDE_MOMMY_HEAD);
                     OwnershipSavedData.markAsManglelieOwned(this.getGirlId());
                  } else {
                     Vec3d selfPos = this.getPositionVector();
                     Vec3d partnerPos = chosen.getPositionVector();
                     Vec3d delta = partnerPos.subtract(selfPos);
                     float yaw = (float)TrigMath.sinDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
                     this.setYawRotation(yaw);
                     this.pathNavigator.clearPath();
                     this.pathNavigator.tryMoveToEntityLiving(chosen, 0.65F);
                     this.setCurrentAction(Action.RUN);
                  }
               }
            }
         }
      }
   }



   void tickUnpartneredAi() {
      tickSeekMangleliePartner();
      this.wanderRandomly();
   }



   void clearFlightData() {
      this.wanderTarget = null;
      this.wanderTicks = 0;
      if (this.activeFlight != null) {
         this.activeFlight.updateFlight(this);
         this.activeFlight = null;
      }
   }



   @Nullable
   Path getPathToPlayer(EntityPlayer player, BlockPos pos) {
      PathNavigate navigator = this.getNavigator();
      return navigator.getPathToEntityLiving(player);
   }

}
