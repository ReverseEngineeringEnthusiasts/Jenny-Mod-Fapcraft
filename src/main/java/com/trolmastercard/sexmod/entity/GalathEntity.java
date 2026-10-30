package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.GirlAnimationController;
import com.trolmastercard.sexmod.client.renderer.WildSlimeFaceLayer;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.entity.ai.DoorInteractAiGoal;
import com.trolmastercard.sexmod.entity.ai.WatchClosestGirlGoal;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.item.GalathCoinItem;
import com.trolmastercard.sexmod.networking.GalathRapePouncePacket;
import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.RequestRidingPacket;
import com.trolmastercard.sexmod.networking.ResetGirlPacket;
import com.trolmastercard.sexmod.networking.SetPlayerCamPacket;
import com.trolmastercard.sexmod.networking.SetPlayerMovementPacket;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket;
import com.trolmastercard.sexmod.networking.TeleportPlayerPacket;
import com.trolmastercard.sexmod.networking.UpdateVelocityPacket;
import com.trolmastercard.sexmod.util.AllieWorldData;
import com.trolmastercard.sexmod.util.BeeWorldData;
import com.trolmastercard.sexmod.util.ForgeEventHandler;
import com.trolmastercard.sexmod.util.GalathDamageSource;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.GirlWorldData;
import com.trolmastercard.sexmod.util.KoboldTask;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.SuccubusDamageSource;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import com.trolmastercard.sexmod.util.DynamicTrailRenderer;
import com.trolmastercard.sexmod.util.Vector4d;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.ClientServerCheck;
import com.trolmastercard.sexmod.util.TrigMath;
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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.pathfinding.Path;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
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
 * <b>State.</b> Own data keys (do not reorder): {@code bq} (111) = target
 * entity id, {@code aP} (112) = attack/sword progress, {@code bN}/{@code b7}
 * (113/114) = left/right energy-ball alive flags, {@code ay} (115) = mirror
 * side, {@code bH} (116) = sword attack progress, {@code b8} (117) = flight
 * target pos ("x|y|z"), {@code bP} (118) = paralyzed flag,
 * {@code bO} (119) = rape progress, {@code HIDE_EFFECTS_FLAG} (120) =
 * knock-out state, {@code WildSlimeFaceLayer} (121) = Manglelie partner UUID,
 * {@code bT} (122) = sprint flag. The two {@link SexEntityPart} hitboxes
 * ({@code b2}, {@code energyBallHitboxRight}) back the energy-ball attack.
 * <p>
 * <b>Flow.</b> Wild: {@code updateAITasks} -&gt; {@link #ao()} drives the
 * flight state machine ({@link GalathFlightState#initFlightData(GalathEntity)} picks a random
 * {@link GalathFlightData} action when a target is in range, see
 * {@link GalathFlightState#acquireFlightTarget(GalathEntity)});
 * {@link #setFlightVelocity(Vec3d)} (dragon hit)
 * sends her into {@link Action#KNOCK_OUT_FLY} -&gt; KNOCK_OUT_GROUND
 * ({@link GalathFlightState#handleKnockout(GalathEntity)}) -&gt;
 * KNOCK_OUT_STAND_UP. The corrupting player
 * then right-clicks ({@link #processGirlInteract(EntityPlayer, EnumHand)})
 * which starts {@link Action#CORRUPT_INTRO}; {@code handleCorruptCum} (server)
 * advances CORRUPT_CUM -&gt; {@link Action#GIVE_COIN} -&gt;
 * {@link #ap()} grants the coin and binds the master.
 * Tamed: scenes enter through {@link #processMasterInteract} +
 * {@link #doAction(String, UUID)}; {@link #handleRapeState()} and
 * {@link GalathSceneTicks#handleRapeCum(GalathEntity)}/
 * {@link GalathSceneTicks#endTamedCorruptScene(GalathEntity)} end rape/corrupt
 * scenes
 * (player reset via {@link SetPlayerMovementPacket}).
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} blocks transitions out
 * of GALATH_DE_SUMMON, guards all cum loops, persists cum time via
 * {@link GirlSavedData} and fires the GIVE_COIN/HUG_MANG/MORNING_BLOWJOB_CUM
 * teardown hooks. The boss bar ({@code aO}) is only visible for wild Galaths
 * ({@link #an()}); {@code despawned} girls only update the Manglelie world
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
   public static final Vector3fSexmodSpecial aa = new Vector3fSexmodSpecial(0.83137256F, 0.6862745F, 0.21568628F);
   public static final Vec3d bz = new Vec3d(-1.049342F, 2.0547214F, -0.050482392F);
   public static final Vec3d bC = new Vec3d(1.2522261F, 1.4357733F, 0.23570988F);
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
   static final Class<?>[] aS = new Class[]{
      BlockAir.class, BlockCarpet.class, BlockBush.class, BlockButton.class, BlockLadder.class, BlockTorch.class, BlockSign.class, BlockBanner.class
   };
   public static final DataParameter<Integer> bq = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(111);
   public static final DataParameter<Integer> aP = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(112);
   public static final DataParameter<Boolean> bN = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(113);
   public static final DataParameter<Boolean> b7 = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(114);
   public static final DataParameter<Boolean> ay = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(115);
   public static final DataParameter<Integer> bH = EntityDataManager.createKey(GalathEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(116);
   public static final DataParameter<String> b8 = EntityDataManager.createKey(GalathEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(117);
   public static final DataParameter<Boolean> bP = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(118);
   public static final DataParameter<Float> bO = EntityDataManager.createKey(GalathEntity.class, DataSerializers.FLOAT)
      .getSerializer()
      .createKey(119);
   public static final DataParameter<Boolean> HIDE_EFFECTS_FLAG = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(120);
   public static final DataParameter<String> WildSlimeFaceLayer = EntityDataManager.createKey(GalathEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(121);
   public static final DataParameter<Boolean> bT = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(122);
   public static final double b0 = 0.2;
   public static final float bS = 5.0F;
   public static final int a1 = 60;
   BossInfoServer aO = new BossInfoServer(new TextComponentString(this.getDisplayNameText()), Color.RED, Overlay.PROGRESS);
   SexEntityPart b2 = new SexEntityPart(this, "energyBallHitBox", 0.75F, 0.75F);
   SexEntityPart energyBallHitboxRight = new SexEntityPart(this, "energyBallHitBox", 0.75F, 0.75F);
   public GalathFlightData activeFlight = null;
   public Vec3d flightTargetPosition = null;
   public Vec3d bL = null;
   public int aF = 0;
   public Vec3d bd = null;
   public List<EntityWitherSkeleton> summonedSkeletons = new ArrayList<>();
   public float aE = 0.0F;
   public long dashStartTick = -1L;
   public long dashEndTick = -1L;
   public float bw = 0.0F;
   public float bm = 0.0F;
   boolean deathHandled = false;
   public Vec3d clientTargetPosition = null;
   boolean markedDespawned = false;
   Vec3d previousPosition;
   Vec3d predictedPosition;
   Vec3d velocity;
   float boostSpeed = 0.0F;
   boolean corruptIntroActive = false;
   public int summonSkeletonTicks = 0;
   double a9 = 0.0;
   double bg = 0.0;
   double b4 = 0.0;
   double a_ = 0.0;
   boolean customModelCodeLoaded = false;
   Path currentPath = null;
   BlockPos wanderTarget = null;
   int wanderTicks = 0;
   Action ab = null;
   int rapeCumTicks = 0;
   int standUpTicks = 0;
   int knockoutGroundTicks = 0;
   long bc = 0L;
   boolean morningBlowjobStarted = false;
   boolean despawned = false;
   int rapeVariant = 0;
   boolean corruptHard = false;
   public boolean masterbateSittingPending = false;
   public boolean a5 = false;
   public boolean bedSoftAnimation = false;
   public boolean bedBackAnimation = false;
   public boolean renderSword = false;
   public boolean spawnSwordParticles = false;
   public boolean aL = true;
   public boolean wingsAnimated = false;
   boolean alternateMoan = false;

   public GalathEntity(World world) {
      super(world);
   }

   public GalathEntity(World world, @Nonnull EntityPlayer player, Vec3d pos, boolean spawnStructure) {
      this(world);
      UUID uuid = player.getPersistentID();
      this.entityDataManager.set(MASTER, uuid.toString());
      this.aO.setVisible(false);
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
      GirlWorldData.setCustomModelCode(this);
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
      this.entityDataManager.register(bq, -1);
      this.entityDataManager.register(aP, 0);
      this.entityDataManager.register(bN, true);
      this.entityDataManager.register(b7, true);
      this.entityDataManager.register(ay, false);
      this.entityDataManager.register(b8, "null");
      this.entityDataManager.register(bH, -1);
      this.entityDataManager.register(bP, false);
      this.entityDataManager.register(bO, 0.0F);
      this.entityDataManager.register(HIDE_EFFECTS_FLAG, false);
      this.entityDataManager.register(WildSlimeFaceLayer, "");
      this.entityDataManager.register(bT, false);
   }

   @Override
   protected void applyEntityAttributes() {
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MAX_HEALTH);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(50.0);
      this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(110.0);
      this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.6F);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.6F);
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
      this.aO.addPlayer(playerMP);
   }

   public void removeTrackingPlayer(EntityPlayerMP playerMP) {
      super.removeTrackingPlayer(playerMP);
      this.aO.removePlayer(playerMP);
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

   public void aC() {
      this.markedDespawned = true;
      ManglelieEntity manglelie = this.getMangleliePartner(true);
      if (manglelie != null) {
         manglelie.markDespawned();
      }
   }

   /**
    * SERVER: ends the rape scene — once the RAPE_ON_GOING loop leaves for
    * RAPE_CUM she switches to flight repositioning (CHANGE_POSITION) and
    * releases the player.
    */
   public void handleRapeState() {
      Action action = this.getCurrentAction();
      if (action == Action.RAPE_ON_GOING) {
         this.activeFlight = GalathFlightData.CHANGE_POSITION;
         this.activeFlight.executeStart(this);
         this.setAnchored(false);
         this.setCurrentAction(Action.FLY);
         EntityPlayer player = this.getPlayerEntity();
         this.setInteractionPlayerUUID(null);
         if (player != null) {
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
         }

         BaseGirlEntity.girlPlaySound(this, SoundHandler.GIRLS_GALATH_DIALOG[0]);
      }
   }

   public Vec3d getFlightTargetPos() {
      String[] parts = ((String)this.entityDataManager.get(b8)).split("\\|");
      return new Vec3d(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]));
   }

   public void setFlightTargetPos(@Nullable Vec3d pos) {
      this.entityDataManager.set(b8, pos.x + "|" + pos.y + "|" + pos.z);
   }

   public int getSwordAttackProgress() {
      return (Integer)this.entityDataManager.get(bH);
   }

   public void setSwordAttackProgress(int progress) {
      this.entityDataManager.set(bH, progress);
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
    * ({@link #updateRidingGravity()} gravity + {@link #au()}), wild girls tick the
    * flight UI (boss bar, gravity, flight data) and the wild systems
    * ({@link GalathSceneTicks#tickWildSystems(GalathEntity)}). CLIENT
    * additionally ticks the coin-give particles
    * ({@link GalathSceneTicks#tickGiveCoinScene(GalathEntity)}).
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
         this.au();
      } else {
         GalathSceneTicks.tickWildSystems(this);
      }

      if (this.world.isRemote) {
         GalathSceneTicks.tickGiveCoinScene(this);
      }
   }

   @Override
   public boolean canBeInteractedWith() {
      return false;
   }

   void updateRidingGravity() {
      this.setNoGravity(this.getRidingPlayer() != null);
   }

   /**
    * SERVER: tamed per-tick systems — soft fall damping, riding-player yaw
    * sync, the carry camera pitch, the boost impulse, wing/effect flags, and
    * the rape/corrupt scene enders ({@link GalathSceneTicks#handleRapeCum(GalathEntity)},
    * {@link GalathSceneTicks#endTamedCorruptScene(GalathEntity)}).
    */
   void au() {
      if (!this.isInWater() && !this.hasNoGravity() && this.motionY < 0.0 && this.getCurrentAction() != Action.MASTERBATE) {
         this.motionY *= 0.4F;
      }

      this.aB();
      this.aj();
      this.aq();
      this.aw();
      GalathSceneTicks.updateWingsAnimated(this);
      GalathSceneTicks.endTamedCorruptScene(this);
      GalathSceneTicks.handleRapeCum(this);
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

   void aw() {
      EntityPlayer player = this.getRidingPlayer();
      Action action = this.getCurrentAction();
      if (player != null) {
         if (action == Action.BOOST) {
            int sideIndex = ClientServerCheck.getInstance() ? 0 : 1;
            if (action.ticksPlaying[sideIndex] >= 13) {
               if (action.ticksPlaying[sideIndex] == 13) {
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

   void updateFlightUI() {
      GalathSceneTicks.updateGravity(this);
      GalathSceneTicks.updateHealthBar(this);
      GalathEnergyBalls.tickSummonSkeletonProgress(this);
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
      return new Vector4d(this.a9, this.bg, this.b4, this.a_);
   }

   void aj() {
      this.b4 = this.a9;
      this.a_ = this.bg;
      Vec3d delta = this.predictedPosition.subtract(this.previousPosition);
      Vec3d rotated = VectorMath.rotateByYaw(delta, this.renderYawOffset + 180.0F);
      this.a9 = TrigMath.toRadians(ThreadNames.clampDouble(rotated.z * 40.0, -50.0, 50.0));
      this.bg = TrigMath.toRadians(ThreadNames.clampDouble(rotated.x * 40.0, -50.0, 50.0));
   }

   /**
    * SERVER: the paralysis knock — dragon explosions call this; once
    * paralyzed she stops the flight action, is flung away from the source
    * ({@link Action#KNOCK_OUT_FLY}) and broadcasts the "time to corrupt her"
    * message with the effect particles.
    */
   public void setFlightVelocity(Vec3d targetPos) {
      if (!(Boolean)this.entityDataManager.get(bP)) {
         this.entityDataManager.set(bP, true);
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
      PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.getPositionVector(), true), this);
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
         GirlSavedData.updateMangleliePartner(this);
      } else {
         this.ensureCustomModelCodeLoaded();
         super.updateAITasks();
         this.watchClosestGirlGoal.isWatching = this.isFlyingIdle();
         if (this.hasMaster()) {
            GalathTamedAi.tickTamedAi(this);
         } else {
            this.an();
         }
      }
   }

   void ensureCustomModelCodeLoaded() {
      if (!this.customModelCodeLoaded) {
         this.setCustomModelCode(GirlWorldData.getCustomModelCode(this));
         this.customModelCodeLoaded = true;
      }
   }

   boolean isFlyingIdle() {
      return this.getCurrentAction() != Action.NULL ? false : !(Math.abs(this.motionX) + Math.abs(this.motionZ) > 0.01);
   }

   void aq() {
      if (this.world.isRemote) {
         if (this.getRidingPlayer() == null) {
            EntityPlayer master = this.getMasterPlayer();
            if (master != null) {
               this.handleGalathPlayer(master);
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

   protected void handlePlayerRide(EntityPlayer player) {
      int attempts = 0;

      BlockPos teleportPos;
      do {
         teleportPos = player.getPosition().add(Reference.RANDOM.nextInt(4), 0, Reference.RANDOM.nextInt(4));
      } while (++attempts < 20 && !this.attemptTeleport(teleportPos.getX(), teleportPos.getY(), teleportPos.getZ()));

      if (attempts >= 20) {
         this.setPosition(player.posX, player.posY, player.posZ);
      }

      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
   }

   void ay() {
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

   BlockPos av() {
      UUID ownerUuid = GirlSavedData.getManglelieOwnerOf(this);
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
   public UUID ax() {
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
      this.setInteractionPlayerUUID(null);
      this.setCurrentAction((Action)null);
   }

   void aB() {
      EntityPlayer rider = this.getRidingPlayer();
      if (rider != null) {
         this.prevRenderYawOffset = rider.prevRotationYawHead;
         this.renderYawOffset = rider.rotationYawHead;
      }
   }

   void an() {
      this.aO.setVisible(true);
      this.ao();
      this.as();
   }

   /**
    * SERVER: the tamed rape/corrupt scenes' shared tick — targets the
    * nearest player or mob in a radius (7 tamed / 20 wild), picks flight
    * actions (see {@link GalathFlightState#initFlightData(GalathEntity)}), keeps the energy-ball
    * hitboxes synced and watches the flight target validity.
    */
   void ao() {
      if (!Action.isAnyAction(this, Action.MASTERBATE, Action.HUG_MANG)) {
         if (this.getInteractionPlayerUUID() == null) {
            GalathFlightState.cancelFlightOnLostTarget(this);
            GalathFlightState.acquireFlightTarget(this);
            GalathFlightState.tickFlightStateMachine(this);
            GalathFlightState.checkFlightFinished(this);
            GalathFlightState.updateChangePositionNoClip(this);
            GalathFlightState.tickKnockoutFlight(this);
            GalathFlightState.freezeDuringKnockout(this);
            GalathFlightState.handleKnockout(this);
            GalathFlightState.tickKnockoutStandUp(this);
            GalathEnergyBalls.aG(this);
            GalathEnergyBalls.aA(this);
            GalathEnergyBalls.aD(this);
            GalathEnergyBalls.despawnFarSummonedSkeletons(this);
            GalathSceneTicks.tickWildRapeCum(this);
         }
      }
   }

   void as() {
      if (this.getTargetEntity() != null) {
         this.wanderTarget = null;
         this.wanderTicks = 0;
      } else if (!(Boolean)this.entityDataManager.get(HIDE_EFFECTS_FLAG)) {
         if (!(Boolean)this.entityDataManager.get(bP)) {
            this.ay();
         }
      }
   }

   /**
    * SERVER: the state-machine gate — guards the cum loops and
    * GALATH_DE_SUMMON, persists the cum time via {@link GirlSavedData} for
    * the tamed cum scenes, and fires the GIVE_COIN ({@link #ap()}),
    * HUG_MANG ({@link #al()}) and MORNING_BLOWJOB_CUM ({@link #aE()})
    * teardown hooks.
    */
   @Override
   public void setCurrentAction(Action action) {
      Action currentAction = this.getCurrentAction();
      if (currentAction != Action.GALATH_DE_SUMMON) {
         if (currentAction != Action.CORRUPT_CUM || action != Action.CORRUPT_FAST && action != Action.CORRUPT_SLOW) {
            if (currentAction != Action.RAPE_CUM || action != Action.RAPE_ON_GOING) {
               if (currentAction != Action.MORNING_BLOWJOB_CUM || action != Action.MORNING_BLOWJOB_SLOW && action != Action.MORNING_BLOWJOB_FAST) {
                  if (!this.world.isRemote && Action.isAny(currentAction, Action.CORRUPT_CUM, Action.RAPE_CUM, Action.MORNING_BLOWJOB_CUM)) {
                     GirlSavedData.saveCumTime(this.getInteractionPlayerUUID(), this.world.getTotalWorldTime());
                  }

                  if (action == Action.CORRUPT_SLOW) {
                     this.corruptHard = false;
                     if (currentAction == Action.CORRUPT_INTRO) {
                        this.setFlying(false);
                     }

                     if (this.hasMaster() && currentAction == Action.NULL) {
                        this.setFlying(true);
                     }
                  }

                  if (currentAction == Action.GIVE_COIN && action == Action.NULL && !this.world.isRemote) {
                     this.ap();
                  }

                  if (currentAction == Action.HUG_MANG && action == Action.NULL) {
                     this.al();
                  }

                  if (currentAction == Action.MORNING_BLOWJOB_CUM && action == Action.NULL) {
                     this.aE();
                  }

                  super.setCurrentAction(action);
               }
            }
         }
      }
   }

   /**
    * SERVER: the morning-blowjob teardown — resets the bound player and the
    * girl after MORNING_BLOWJOB_CUM.
    */
   void aE() {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         ResetGirlPacket.Handler.resetGirls((EntityPlayerMP)player);
      }

      ResetGirlPacket.Handler.resetGirl(this);
   }

   void al() {
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
   void ap() {
      EntityPlayer player = this.getPlayerEntity();
      if (player != null) {
         ItemStack stack = player.getHeldItemMainhand();
         player.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(GalathCoinItem.GALATH_COIN));
         if (!stack.isEmpty()) {
            player.inventory.addItemStackToInventory(stack);
         }

         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)player);
         this.setInteractionPlayerUUID(null);
         this.setTargetEntity(null);
         player.sendMessage(
            new TextComponentString(
               TextFormatting.GRAY
                  + "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin"
            )
         );
         GirlSavedData.updateMangleliePartner(this);
         player.setPositionAndUpdate(player.posX, Math.ceil(player.posY) + 1.0, player.posZ);
      }
   }

   void setFlying(boolean flying) {
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

   public static void handlePlayerJoin(EntityPlayer player) {
      BaseGirlEntity girl = BaseGirlEntity.getServerGirlEntity(GirlSavedData.getOwnerOf(player));
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

   protected boolean processInteract(EntityPlayer player, EnumHand hand) {
      return this.hasMaster() ? this.processMasterInteract(player, hand) : this.processGirlInteract(player, hand);
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
            openInventoryGui(player, this.getSelf(), options, false);
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
   @Override
   public void doAction(String action, UUID uuid) {
      if ("ride".equals(action)) {
         GalathFlightHud.showHud();
         PacketHandler.networkWrapper.sendToServer(new RequestRidingPacket());
      } else if ("anal".equals(action)) {
         BeeScreen.enableInteraction();
         HandlePlayerMovement.setMovementLock(false);
         ThreadNames.createDaemonThread(1200, () -> {
            EntityPlayerSP player = Minecraft.getMinecraft().player;
            this.setTargetPosition(player.getPositionVector());
            this.setYawRotation(0.0F);
            this.setInteractionPlayerUUID(player.getPersistentID());
            this.setAnchored(true);
            this.setCurrentAction(Action.CORRUPT_SLOW);
         });
      } else if ("cowgirl".equals(action)) {
         BeeScreen.enableInteraction();
         HandlePlayerMovement.setMovementLock(false);
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

            BeeScreen.enableInteraction();
            HandlePlayerMovement.setMovementLock(false);
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
      if (!(Boolean)this.entityDataManager.get(bP)) {
         return super.processInteract(player, hand);
      } else if (this.getCurrentAction() != Action.KNOCK_OUT_GROUND) {
         return super.processInteract(player, hand);
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
         PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
         player.setPositionAndUpdate(this.posX, this.posY, this.posZ);
         return true;
      }
   }

   @Nullable
   public Entity[] getParts() {
      return new Entity[]{this.energyBallHitboxRight, this.b2};
   }

   public void setTargetEntity(@Nullable EntityLivingBase target) {
      if (target == null) {
         this.entityDataManager.set(bq, -1);
      } else {
         this.entityDataManager.set(bq, target.getEntityId());
      }
   }

   public int getAttackProgress() {
      return (Integer)this.entityDataManager.get(aP);
   }

   public void setAttackProgress(int progress) {
      this.entityDataManager.set(aP, progress);
   }

   public EntityLivingBase getTargetEntity() {
      int targetId = (Integer)this.entityDataManager.get(bq);
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
      if (this.world.isRemote) {
         if (!(this.getHealth() - damage <= 0.0F)) {
            long now = System.currentTimeMillis();
            if (now >= this.bc + 1000L) {
               this.playRandomSound(SoundHandler.GIRLS_GALATH_UUH);
               this.bc = now;
            }
         }
      }
   }

   public boolean attackEntityFrom(DamageSource source, float amount) {
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

      this.playHurtSound(amount);
      return super.attackEntityFrom(source, amount);
   }

   public boolean attackEntityFromPart(MultiPartEntityPart part, DamageSource source, float amount) {
      if (this.world.isRemote) {
         return false;
      }

      if (!(source.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (part == this.energyBallHitboxRight) {
         this.entityDataManager.set(b7, false);
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.energyBallHitboxRight.getPositionVector(), false), this);
      }

      if (part == this.b2) {
         this.entityDataManager.set(bN, false);
         PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.b2.getPositionVector(), false), this);
      }

      return true;
   }

   @Override
   public void reinitTasks() {
      this.setTargetEntity(null);
      GalathFlightState.aH(this);
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
    * SERVER: per-tick rape damage — while RAPE_ON_GOING/RAPE_INTRO the bound
    * player takes 1 HP (non-creative, survives at 1), and Galath heals 1.5
    * per pulse when {@code applyDamage} is set.
    */
   public void handleRapeAction(boolean applyDamage) {
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

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      GalathNbtIO.write(this, nbt);
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound nbt) {
      super.readEntityFromNBT(nbt);
      GalathNbtIO.read(this, nbt);
   }

   public void ak() {
      if (this.getCurrentAction() != Action.MASTERBATE_SITTING) {
         this.masterbateSittingPending = true;
         this.setCurrentAction(Action.MASTERBATE_SITTING);
      }
   }

   public void startPussyLicking() {
      this.a5 = true;
      this.setCurrentAction(Action.PUSSY_LICKING);
   }

   @Override
   protected boolean handleActionAnimationOverrides(Action action, String animName, boolean started, AnimationEvent event) {
      if (action == Action.MASTERBATE_SITTING && this.masterbateSittingPending) {
         this.masterbateSittingPending = false;
         this.createAnimation("animation.galath.masterbating_sitting", true, event, true);
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

      if (action == Action.PUSSY_LICKING && this.a5) {
         this.a5 = false;
         this.createAnimation("animation.galath.pussy_licking", true, event, true);
         return true;
      }

      if (action != Action.MORNING_BLOWJOB_SLOW || !this.morningBlowjobStarted && !HandlePlayerMovement.isJumping) {
         if (action == Action.MORNING_BLOWJOB_SLOW && this.bedBackAnimation) {
            this.bedBackAnimation = false;
            this.createAnimation("animation.shared.bed_slow", true, event, true);
            return true;
         } else if (action == Action.MORNING_BLOWJOB_FAST && !HandlePlayerMovement.isJumping) {
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

   public float getSwordAttackProgress(float partialTicks) {
      Action action = this.getCurrentAction();
      if (action == Action.PUSSY_LICKING && !this.a5) {
         return 0.0F;
      }

      if (action == Action.MASTERBATE_SITTING && !this.masterbateSittingPending) {
         return 1.0F;
      }

      float scale = Action.getActionTimeScale(this, partialTicks);
      return action == Action.MASTERBATE_SITTING ? scale : 1.0F - scale;
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      if (this.isLocallyRegistered()) {
         this.createAnimation("animation.galath.idle", true, event);
         return PlayState.CONTINUE;
      }

      Action action = this.getCurrentAction();
      AnimationController controller = event.getController();
      controller.setAnimationSpeed(1.0);
      if (controller.equals(this.eyesController)) {
         if (action.autoBlink && action != Action.GALATH_DE_SUMMON) {
            this.createAnimation("animation.galath.blink", true, event);
            return PlayState.CONTINUE;
         } else {
            return PlayState.STOP;
         }
      } else if (controller.equals(this.movementController)) {
         if (action != Action.NULL) {
            return PlayState.STOP;
         } else if (!this.onGround) {
            this.createAnimation("animation.galath.controlled_flight", true, event);
            return PlayState.CONTINUE;
         } else {
            Vec3d movement = this.getPositionVector().subtract(new Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ));
            if (movement.equals(Vec3d.ZERO)) {
               this.createAnimation("animation.galath.idle", true, event);
               return PlayState.CONTINUE;
            } else {
               this.rotationYaw = this.rotationYawHead;
               this.createAnimation("animation.galath." + (this.entityDataManager.get(bT) ? "run" : "walk"), true, event);
               return PlayState.CONTINUE;
            }
         }
      } else {
         switch (this.getCurrentAction()) {
            case HUG_MANG:
               this.createAnimation("animation.galath.hug_mang", true, event);
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
               this.createAnimation("animation.galath.rape_intro", true, event);
               break;
            case RAPE_ON_GOING:
               this.createAnimation("animation.galath.rape" + this.rapeVariant, true, event);
               break;
            case RAPE_CUM:
               this.createAnimation("animation.galath.rape_cum", true, event);
               break;
            case RAPE_CHARGE:
               this.createAnimation("animation.galath.rape_charge", true, event);
               break;
            case RAPE_CUM_IDLE:
               this.createAnimation("animation.galath.rape_cum_idle", true, event);
               break;
            case CORRUPT_SLOW:
               this.createAnimation("animation.galath.corrupt_slow", true, event);
               break;
            case CORRUPT_FAST:
               this.createAnimation("animation.galath.corrupt_" + (this.corruptHard ? "hard" : "soft"), true, event);
               break;
            case CORRUPT_CUM:
               this.createAnimation("animation.galath.corrupt_cum", true, event);
               break;
            case MASTERBATE:
               this.createAnimation("animation.galath.masterbate", true, event);
               break;
            case RAPE_PREPARE:
               this.createAnimation("animation.galath.rape_prepare", true, event);
               break;
            case CORRUPT_INTRO:
               this.createAnimation("animation.galath.corrupt_intro", true, event);
            case COWGIRLCUM:
            default:
               break;
            case NULL:
               return PlayState.STOP;
            case FLY:
               this.createAnimation("animation.galath.idle_flying", true, event);
               break;
            case SUMMON_SKELETON:
               this.createAnimation("animation.galath.summon_skeleton" + (this.entityDataManager.get(ay) ? "Mirrored" : ""), true, event);
               break;
            case ATTACK_SWORD:
               this.createAnimation("animation.galath.attack", true, event);
               break;
            case KNOCK_OUT_FLY:
               controller.setAnimationSpeed(1.5);
               this.createAnimation("animation.galath.knockout_air", true, event);
               break;
            case KNOCK_OUT_GROUND:
               this.createAnimation("animation.galath.knocked_out", true, event);
               break;
            case KNOCK_OUT_STAND_UP:
               this.createAnimation("animation.galath.knocked_out_stand_up", true, event);
               break;
            case CONTROLLED_FLIGHT:
               this.createAnimation("animation.galath.controlled_flight", true, event);
               break;
            case BOOST:
               this.createAnimation("animation.galath.boost", true, event);
               break;
            case GALATH_SUMMON:
               this.createAnimation("animation.galath.summon", false, event);
               break;
            case GALATH_DE_SUMMON:
               this.createAnimation("animation.galath.desummon" + (this.onGround ? "_standing" : ""), true, event);
               break;
            case GIVE_COIN:
               this.createAnimation("animation.galath.give_coin", true, event);
               break;
            case RUN:
               controller.setAnimationSpeed(0.7);
               this.createAnimation("animation.galath.running", true, event);
               break;
            case PUSSY_LICKING:
               this.createAnimation(this.a5 ? "animation.galath.pussy_licking_forward" : "animation.galath.pussy_licking", true, event);
               break;
            case MASTERBATE_SITTING:
               this.createAnimation(this.masterbateSittingPending ? "animation.galath.pussy_licking_back" : "animation.galath.masterbating_sitting", true, event);
               break;
            case MASTERBATE_SITTING_CUM:
               this.createAnimation("animation.galath.masterbating_sitting_cum", true, event);
         }

         return PlayState.CONTINUE;
      }
   }

   /**
    * CLIENT: registers the action (via {@link GirlAnimationController}),
    * movement and eyes controllers plus the sound listener that drives the
    * wild fight (charge sounds, sword render toggles, stars) and the tamed
    * scenes (rape switch/UI, corrupt switch, coin cam, morning blowjob,
    * creampie trails). {@code blackScreenTamed} only black-screens wild
    * girls; {@code flapControlled} sends the flight input as
    * {@link UpdateVelocityPacket}.
    */
   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData data) {
      this.actionController = new GirlAnimationController<>(this, "action", 0.0F, this::animationPredicate);
      this.movementController = new AnimationController<>(this, "movement", 5.0F, this::animationPredicate);
      this.eyesController = new AnimationController<>(this, "eyes", 10.0F, this::animationPredicate);
      this.actionController
         .registerSoundListener(
            sound -> {
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
                     this.aL = false;
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
                           PacketHandler.networkWrapper.sendToServer(new GalathRapePouncePacket(true));
                        }
                     }
                     break;
                  case "rapeHurt":
                     if (!this.hasMaster() && this.isControlledByLocalPlayer()) {
                        PacketHandler.networkWrapper.sendToServer(new GalathRapePouncePacket(false));
                     }
                     break;
                  case "enableRapeUI":
                     if (this.isControlledByLocalPlayer()) {
                        if (this.hasMaster()) {
                           HornyMeterHud.setHornyMeterVisible(false);
                        } else {
                           EscapeMinigameHud.showMinigame();
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
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
                        this.setCurrentAction(Action.CORRUPT_FAST);
                     }
                     break;
                  case "corrupt_hard":
                     if (this.isControlledByLocalPlayer() && HandlePlayerMovement.isJumping) {
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
                     CummyEntity.spawnCummyTrails(this);
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
                        CummyEntity.registerTrail(new DynamicTrailRenderer(90, girl -> {
                           Vec3d cockTipPos = girl.getBoneWorldPosition("futaCockTip");
                           Vec3d tipDir = girl.getBoneWorldPosition("futaCockTipDirHelp");
                           return cockTipPos.subtract(tipDir).normalize();
                        }, girl -> girl.getCachedBoneOffset("futaCockTip").add(girl.getTargetPosition()), this, 0.3F, 0.3F));
                     }
                     break;
                  case "creampie":
                     CummyEntity.registerTrail(
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
                        CummyEntity.registerTrail(new DynamicTrailRenderer(130, girl -> {
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
                        BeeScreen.enableInteraction();
                     }
                     break;
                  case "blackScreenMaster":
                     if (Minecraft.getMinecraft().player.getPersistentID().equals(this.getMasterUUID())) {
                        BeeScreen.enableInteraction();
                        HandlePlayerMovement.setMovementLock(false);
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
                           PacketHandler.networkWrapper.sendToServer(new UpdateVelocityPacket(vel, this.getGirlId()));
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
         );
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
   public static class a {
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
                        && mc.player.getPersistentID().equals(((GalathEntity)girl).ax())) {
                        GalathFlightHud.useCharge();
                        girl.setCurrentAction(Action.BOOST);
                        return;
                     }
                  }
               } catch (ConcurrentModificationException ex) {
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
                        PacketHandler.networkWrapper.sendToAllTracking(new SpawnEnergyBallParticlesPacket(galath.getGirlId(), GirlSavedData.getManglelieOwnerOf(galath)), galath);
                        ThreadNames.createDaemonThread(900, () -> GirlSavedData.updateMangleliePartner(galath));
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
            PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(true), player);
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
                     if ((Boolean)girl.getDataManager().get(GalathEntity.bN)) {
                        Vec3d offsetR = girl.getCachedBoneOffset("energyBallR");
                        Vec3d posR = basePos.add(offsetR);
                        DragonEntity dragonR = new DragonEntity(girl.world, (GalathEntity)girl);
                        dragonR.SCALE_1_0 = scale;
                        dragonR.setPositionAndUpdate(posR.x, posR.y, posR.z);
                        renderManager.renderEntity(dragonR, 0.0, 0.0, 0.0, 0.0F, partialTicks, true);
                        dragonR.setPosition(0.0, -500.0, 0.0);
                        dragonR.setDead();
                     }

                     if ((Boolean)girl.getDataManager().get(GalathEntity.b7)) {
                        Vec3d offsetL = girl.getCachedBoneOffset("energyBallL");
                        Vec3d posL = basePos.add(offsetL);
                        DragonEntity dragonL = new DragonEntity(girl.world, (GalathEntity)girl);
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

         for (Class blockClass : GalathEntity.aS) {
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
            if (GirlSavedData.shouldDespawn(player.getPersistentID(), player.world)) {
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
                  UUID ownerUuid = GirlSavedData.getOwnerOf(player);
                  if (ownerUuid != null) {
                     GirlSavedData.updateMangleliePartner((GalathEntity)BaseGirlEntity.getServerGirlEntity(ownerUuid));
                  }

                  GalathEntity galath = new GalathEntity(player.world, player, pos, true);
                  galath.setPositionAndUpdate(pos.x, pos.y, pos.z);
                  player.world.spawnEntity(galath);
                  GirlSavedData.grantOwnership(player, galath);
                  galath.canStartPussyLicking();
                  galath.setTargetPosition(spawnPos);
                  galath.setYawRotation(yaw);
                  galath.setAnchored(true);
                  galath.setInteractionPlayerUUID(player.getPersistentID());
                  galath.setCurrentAction(Action.MORNING_BLOWJOB_SLOW);
                  PacketHandler.networkWrapper.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)player);
                  ThreadNames.createDaemonThread(500, () -> {
                     player.setPositionAndUpdate(spawnPos.x, spawnPos.y, spawnPos.z);
                     PacketHandler.networkWrapper.sendTo(new SetPlayerCamPacket(-10.0F, yaw + 180.0F + 5.0F, 0), (EntityPlayerMP)player);
                  });
               }
            }
         }
      }

   }
}
