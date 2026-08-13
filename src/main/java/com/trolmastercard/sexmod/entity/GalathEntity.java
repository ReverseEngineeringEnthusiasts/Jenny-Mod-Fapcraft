package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.ao;
import com.trolmastercard.sexmod.api.ar;
import com.trolmastercard.sexmod.api.b2;
import com.trolmastercard.sexmod.api.b8;
import com.trolmastercard.sexmod.api.ba;
import com.trolmastercard.sexmod.api.by;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.EscapeMinigameHud;
import com.trolmastercard.sexmod.client.gui.GalathFlightHud;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.GirlAnimationController;
import com.trolmastercard.sexmod.client.particle.DragonBreathParticle;
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
import com.trolmastercard.sexmod.util.ad;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.ah;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.am;
import com.trolmastercard.sexmod.util.an;
import com.trolmastercard.sexmod.util.bl;
import com.trolmastercard.sexmod.util.bm;
import com.trolmastercard.sexmod.util.bq;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.d3;
import com.trolmastercard.sexmod.util.ep;
import com.trolmastercard.sexmod.util.f2;
import com.trolmastercard.sexmod.util.f7;
import com.trolmastercard.sexmod.util.fl;
import com.trolmastercard.sexmod.util.g0;
import com.trolmastercard.sexmod.util.gc;







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
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
   public static final int X = 45;
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
   public static final double Q = 1.0;
   public static final float aJ = 0.5F;
   public static final f7 aa = new f7(0.83137256F, 0.6862745F, 0.21568628F);
   public static final Vec3d bz = new Vec3d(-1.049342F, 2.0547214F, -0.050482392F);
   public static final Vec3d bC = new Vec3d(1.2522261F, 1.4357733F, 0.23570988F);
   public static final int aN = 10;
   public static final float ak = 0.2F;
   public static final int am = 5;
   public static final float T = 15.0F;
   public static final int aM = 48;
   public static final float be = 0.05F;
   public static final float a7 = 0.65F;
   public static final float bh = 0.9F;
   public static final float K = 45.0F;
   public static final float a0 = 1.0F;
   public static final float ForgeEventHandler = 1.5F;
   public static final float ao = 110.0F;
   public static final int aj = 15;
   public static final float aw = 6.0F;
   public static final float bp = 0.94F;
   public static final int R = 13;
   public static final int bW = 40;
   public static final int bl = 25;
   public static final int aY = 38;
   public static final int N = 95;
   static final int bB = 10;
   static final int aI = 30;
   static final int bf = 175;
   static final float as = 2.0F;
   public static final float bo = 0.25F;
   public static final float Y = 1000.0F;
   public static final float bX = 15.0F;
   public static final float b9 = 5.0F;
   public static final int aW = 8000;
   public static final float aK = 0.1F;
   public static final float ac = 5.0F;
   public static final float b5 = -10.0F;
   public static final int bk = 16;
   public static final int br = 7;
   public static final int cb = 4;
   public static final float M = 0.5F;
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
   public static final DataParameter<Boolean> L = EntityDataManager.createKey(GalathEntity.class, DataSerializers.BOOLEAN)
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
   SexEntityPart V = new SexEntityPart(this, "energyBallHitBox", 0.75F, 0.75F);
   public GalathFlightData bZ = null;
   public Vec3d O = null;
   public Vec3d bL = null;
   public int aF = 0;
   public Vec3d bd = null;
   public List<EntityWitherSkeleton> bI = new ArrayList<>();
   public float aE = 0.0F;
   public long af = -1L;
   public long aH = -1L;
   public float bw = 0.0F;
   public float bm = 0.0F;
   boolean bU = false;
   public Vec3d aG = null;
   boolean bA = false;
   Vec3d bD;
   Vec3d W;
   Vec3d Z;
   float al = 0.0F;
   boolean U = false;
   public int ad = 0;
   double a9 = 0.0;
   double bg = 0.0;
   double b4 = 0.0;
   double a_ = 0.0;
   boolean bK = false;
   Path aq = null;
   BlockPos bG = null;
   int aC = 0;
   fp ab = null;
   int at = 0;
   int bY = 0;
   int b3 = 0;
   long bc = 0L;
   boolean S = false;
   boolean P = false;
   int b1 = 0;
   boolean aT = false;
   public boolean bx = false;
   public boolean a5 = false;
   public boolean aD = false;
   public boolean bt = false;
   public boolean ap = false;
   public boolean bu = false;
   public boolean aL = true;
   public boolean bb = false;
   boolean a6 = false;

   public GalathEntity(World var1) {
      super(var1);
   }

   public GalathEntity(World var1, @Nonnull EntityPlayer var2, Vec3d var3, boolean var4) {
      this(var1);
      UUID var5 = var2.getPersistentID();
      this.m.set(v, var5.toString());
      this.aO.setVisible(false);
      this.bG = new BlockPos(this.getPositionVector());
      String var6 = AllieWorldData.a(var5, NpcType.GALATH);
      if (var6 != null) {
         super.g_clash538(var6);
      }

      if (!var4) {
         if (this.getRNG().nextFloat() > 0.1F) {
            this.b(fp.GALATH_SUMMON);
         } else {
            this.b(fp.MASTERBATE);
            this.setYawRotation(180.0F - (float)gc.b(Math.atan2(var3.x - var2.posX, var3.z - var2.posZ)));
            ThreadNames.a(8000, () -> {
               EntityPlayer var1x = this.z_clash528();
               if (var1x != null) {
                  if (!var1x.isDead) {
                     this.setTargetPosition(var1x.getPositionVector());
                     this.setYawRotation(var1x.rotationYaw + 180.0F);
                     this.b(fp.RAPE_INTRO);
                     this.setInteractionPlayerUUID(var1x.getPersistentID());
                     this.setAnchored(true);
                  }
               }
            });
         }
      }
   }

   public GalathEntity(World var1, @Nonnull EntityPlayer var2, Vec3d var3) {
      this(var1, var2, var3, false);
   }

   @Override
   public void setCustomModelCode(String var1) {
      super.setCustomModelCode(var1);
      GirlWorldData.a_clash153(this);
   }

   @Override
   public String getDisplayNameText() {
      return "Galath";
   }

   @Override
   public float i_clash226() {
      return this.aF() == null ? 0.5F : 1.35F;
   }

   public float getEyeHeight() {
      return 1.9F;
   }

   public boolean k_clash637() {
      return this.J_clash526();
   }

   public boolean isPushedByWater() {
      return false;
   }

   protected void handleJumpWater() {
      if (this.k_clash637()) {
         super.handleJumpWater();
      }
   }

   protected float getWaterSlowDown() {
      return this.k_clash637() ? super.getWaterSlowDown() : 0.0F;
   }

   public boolean isInWater() {
      return this.k_clash637() ? super.isInWater() : false;
   }

   public boolean handleWaterMovement() {
      return this.k_clash637() ? super.handleWaterMovement() : false;
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.m.register(bq, -1);
      this.m.register(aP, 0);
      this.m.register(bN, true);
      this.m.register(b7, true);
      this.m.register(ay, false);
      this.m.register(b8, "null");
      this.m.register(bH, -1);
      this.m.register(bP, false);
      this.m.register(bO, 0.0F);
      this.m.register(L, false);
      this.m.register(WildSlimeFaceLayer, "");
      this.m.register(bT, false);
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
      this.o = new WatchClosestGirlGoal(this, EntityPlayer.class, 3.0F, 1.0F);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(2, new EntityAITempt(this, 0.4, false, new HashSet<>(I)));
      this.tasks.addTask(3, new DoorInteractAiGoal(this));
      this.tasks.addTask(5, this.o);
   }

   public void addTrackingPlayer(EntityPlayerMP var1) {
      super.addTrackingPlayer(var1);
      this.aO.addPlayer(var1);
   }

   public void removeTrackingPlayer(EntityPlayerMP var1) {
      super.removeTrackingPlayer(var1);
      this.aO.removePlayer(var1);
   }

   @Override
   public Vec3d getTargetPosition() {
      return this.world.isRemote && this.aG != null ? this.aG : super.getTargetPosition();
   }

   @Nullable
   public UUID aF() {
      String var1 = (String)this.m.get(WildSlimeFaceLayer);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString(var1);
      } catch (Exception var2) {
         return null;
      }
   }

   @Nullable
   public ManglelieEntity a_clash638(boolean var1) {
      UUID var2 = this.aF();
      if (var2 == null) {
         return null;
      }

      BaseGirlEntity var3 = var1 ? getServerGirlEntity(var2) : getClientGirlEntity(var2);
      return var3 instanceof ManglelieEntity ? (ManglelieEntity)var3 : null;
   }

   @Nullable
   public static ManglelieEntity a_clash639(BaseGirlEntity var0, boolean var1) {
      return !(var0 instanceof GalathEntity) ? null : ((GalathEntity)var0).a_clash638(var1);
   }

   public void a_clash640(@Nullable UUID var1) {
      this.m.set(WildSlimeFaceLayer, var1 == null ? "" : var1.toString());
   }

   public void aC() {
      this.bA = true;
      ManglelieEntity var1 = this.a_clash638(true);
      if (var1 != null) {
         var1.q_clash416();
      }
   }

   public void w_clash641() {
      fp var1 = this.getCurrentAction();
      if (var1 == fp.RAPE_ON_GOING) {
         this.bZ = GalathFlightData.CHANGE_POSITION;
         this.bZ.b_clash706(this);
         this.setAnchored(false);
         this.b(fp.FLY);
         EntityPlayer var2 = this.S_clash495();
         this.setInteractionPlayerUUID(null);
         if (var2 != null) {
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var2);
         }

         BaseGirlEntity.a(this, SoundHandler.GIRLS_GALATH_DIALOG[0]);
      }
   }

   public Vec3d B_clash642() {
      String[] var1 = ((String)this.m.get(b8)).split("\\|");
      return new Vec3d(Double.parseDouble(var1[0]), Double.parseDouble(var1[1]), Double.parseDouble(var1[2]));
   }

   public void e(@Nullable Vec3d var1) {
      this.m.set(b8, var1.x + "|" + var1.y + "|" + var1.z);
   }

   public int az() {
      return (Integer)this.m.get(bH);
   }

   public void a_clash643(int var1) {
      this.m.set(bH, var1);
   }

   public boolean isNonBoss() {
      return false;
   }

   @Override
   public boolean b_clash23() {
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

   void aa_clash644() {
      this.Z = new Vec3d(this.motionX, this.motionY, this.motionZ);
      this.bD = this.getPositionVector();
      this.W = this.getPositionVector().add(this.Z);
      this.Z = this.Z.scale(0.9);
   }

   @Override
   public void onUpdate() {
      boolean var1 = this.k_clash637();
      if (var1) {
         this.E_clash646();
      } else {
         this.c_clash649();
      }

      this.aa_clash644();
      super.onUpdate();
      if (var1) {
         this.au();
      } else {
         this.R_clash650();
      }

      if (this.world.isRemote) {
         this.X_clash645();
      }
   }

   @Override
   public boolean B_clash511() {
      return false;
   }

   @SideOnly(Side.CLIENT)
   void X_clash645() {
      if (this.getCurrentAction() == fp.GIVE_COIN) {
         int var1 = fp.GIVE_COIN.ticksPlaying[1];
         if (var1 == 95) {
            GalathCoinItem.a(Minecraft.getMinecraft().player, this);
         }

         if (var1 > 25 && var1 < 38) {
            Vec3d var2 = this.getPositionVector();
            Vec3d var3 = this.getCachedBoneOffset("weapon").add(var2);
            Vec3d var4 = this.getCachedBoneOffset("offhand").add(var2);
            DragonBreathParticle.b = 0.5F;

            for (float var5 = 0.0F; var5 < 1.0F; var5 += 0.2F) {
               Vec3d var6 = RotationHelper.a(var3, var4, var5);
               Minecraft.getMinecraft().effectRenderer.addEffect(new DragonBreathParticle(this.world, var6.x, var6.y, var6.z));
            }
         }
      }
   }

   void E_clash646() {
      this.setNoGravity(this.ab_clash671() != null);
   }

   void au() {
      if (!this.isInWater() && !this.hasNoGravity() && this.motionY < 0.0 && this.getCurrentAction() != fp.MASTERBATE) {
         this.motionY *= 0.4F;
      }

      this.aB();
      this.aj();
      this.aq();
      this.aw();
      this.C_clash652();
      this.Y_clash648();
      this.o_clash647();
      if (this.M_clash691() == null) {
         this.ap = false;
      }
   }

   void o_clash647() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == fp.RAPE_CUM) {
            if (fp.RAPE_CUM.ticksPlaying[0] >= 28) {
               this.setAnchored(false);
               this.b(fp.NULL);
               EntityPlayer var1 = this.S_clash495();
               this.setInteractionPlayerUUID(null);
               if (var1 != null) {
                  var1.setPositionAndUpdate(var1.posX, Math.ceil(var1.posY) + 1.0, var1.posZ);
                  PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var1);
               }
            }
         }
      }
   }

   void Y_clash648() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == fp.CORRUPT_CUM) {
            if (fp.CORRUPT_CUM.ticksPlaying[0] >= 30) {
               this.setAnchored(false);
               this.b(fp.NULL);
               EntityPlayer var1 = this.S_clash495();
               this.setInteractionPlayerUUID(null);
               if (var1 != null) {
                  var1.setPositionAndUpdate(var1.posX, Math.ceil(var1.posY) + 1.0, var1.posZ);
                  PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var1);
               }
            }
         }
      }
   }

   static boolean a(BlockPos var0, World var1) {
      for (BlockPos var3 : BeeWorldData.c) {
         if (Math.sqrt(var0.distanceSq(var3)) < 1000.0) {
            return false;
         }
      }

      try {
         for (BaseGirlEntity var8 : BaseGirlEntity.getGirlEntityList()) {
            if (!var8.world.isRemote && var8 instanceof GalathEntity && !var8.isDead && var8.getDistanceSq(var0) < 1000000.0) {
               return false;
            }
         }
      } catch (ConcurrentModificationException var4) {
      }

      for (int var6 = var0.getY(); var6 < 15.0F + var0.getY(); var6++) {
         if (var1.getBlockState(new BlockPos(var0.getX(), var6, var0.getZ())).getBlock() != Blocks.AIR) {
            return false;
         }
      }

      for (int var7 = var0.getY(); var7 > var0.getY() - 5.0F; var7--) {
         if (var1.getBlockState(new BlockPos(var0.getX(), var7, var0.getZ())).getBlock() instanceof BlockLiquid) {
            return false;
         }
      }

      return true;
   }

   void aw() {
      EntityPlayer var1 = this.ab_clash671();
      fp var2 = this.getCurrentAction();
      if (var1 != null) {
         if (var2 == fp.BOOST) {
            int var3 = g0.a_clash472() ? 0 : 1;
            if (var2.ticksPlaying[var3] >= 13) {
               if (var2.ticksPlaying[var3] == 13) {
                  this.al = 6.0F;
               }

               Vec3d var4 = var1.getLook(0.0F).normalize();
               this.motionX = var4.x * this.al;
               this.motionY = var4.y * this.al;
               this.motionZ = var4.z * this.al;
               this.al *= 0.94F;
            }
         }
      }
   }

   void c_clash649() {
      this.n_clash655();
      this.j_clash654();
      this.ah_clash659();
   }

   void R_clash650() {
      a_clash692(this, 0.0F);
      this.h_clash658();
      this.aj();
      this.af_clash657();
      this.L_clash656();
      this.F_clash653();
      this.C_clash652();
      this.u_clash651();
      if (this.world.isRemote) {
         this.H_clash674();
      }
   }

   void u_clash651() {
      if (!this.world.isRemote) {
         if (this.getCurrentAction() == fp.CORRUPT_CUM) {
            if (fp.CORRUPT_CUM.ticksPlaying[0] >= 30) {
               this.b(fp.GIVE_COIN);
            }
         }
      }
   }

   void C_clash652() {
      if ((Boolean)this.m.get(L)) {
         this.bb = true;
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
               this.bb = true;
            case RAPE_PREPARE:
               return;
            default:
               this.bb = false;
         }
      }
   }

   @Override
   public boolean m_clash494() {
      return this.getCurrentAction() != fp.CORRUPT_INTRO ? false : this.U;
   }

   void F_clash653() {
      if (this.world.isRemote) {
         if (this.getCurrentAction() != fp.KNOCK_OUT_STAND_UP) {
            this.aL = true;
         }
      }
   }

   void j_clash654() {
      this.aO.setPercent(this.getHealth() / this.getMaxHealth());
   }

   void n_clash655() {
      if (!(Boolean)this.m.get(bP)) {
         this.setNoGravity(this.M_clash691() != null);
      }
   }

   void L_clash656() {
      if (this.getCurrentAction() != fp.ATTACK_SWORD) {
         this.ap = false;
         this.bu = false;
      }
   }

   protected void collideWithNearbyEntities() {
   }

   public void addPotionEffect(PotionEffect var1) {
   }

   void af_clash657() {
      if (this.world.isRemote) {
         if (this.bu) {
            Vec3d var1 = this.getPositionVector();
            Vec3d var2 = this.getCachedBoneOffset("weaponStart").add(var1);
            Vec3d var3 = this.getCachedBoneOffset("weaponEnd").add(var1);
            float var4 = 0.1F;
            Random var5 = this.getRNG();

            for (float var6 = 0.0F; var6 < 1.0F; var6 += var4) {
               Vec3d var7 = RotationHelper.a(var2, var3, var6);

               for (int var8 = 0; var8 < 3; var8++) {
                  this.world
                     .spawnParticle(
                        EnumParticleTypes.DRAGON_BREATH,
                        var7.x + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1),
                        var7.y + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1),
                        var7.z + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1),
                        0.0,
                        0.0,
                        0.0,
                        new int[0]
                     );
               }
            }

            for (int var9 = 0; var9 < 3; var9++) {
               this.world
                  .spawnParticle(
                     EnumParticleTypes.DRAGON_BREATH,
                     var3.x + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1) * (var5.nextBoolean() ? 1 : -1),
                     var3.y + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1),
                     var3.z + var5.nextDouble() * 0.25 * (var5.nextBoolean() ? 1 : -1),
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
   @Override
   public void ag() {
      if (this.getCurrentAction() != fp.GALATH_DE_SUMMON) {
         this.C.tickOffset = 0.0;
      }
   }

   @Override
   public String ab_clash540() {
      EntityPlayer var1 = this.z_clash528();
      return var1 == null ? super.ab_clash540() : String.format("%s %s[%s]", super.ab_clash540(), TextFormatting.DARK_PURPLE, var1.getName());
   }

   void h_clash658() {
      this.b2.a = false;
      this.V.a = false;
      if (!(this.ad < 9.0F)) {
         if (!(this.ad > 30.0F)) {
            this.b2.a = true;
            this.V.a = true;
            boolean var1 = (Boolean)this.m.get(ay);
            Vec3d var4 = this.getPositionVector();
            Vec3d var10000;
            Vec3d var10001;
            if (var1) {
               var10000 = var4;
               var10001 = ck.c_clash308(bz);
            } else {
               var10000 = var4;
               var10001 = bz;
            }

            Vec3d var2 = var10000.add(ck.rotateByYaw(var10001, 180.0F + this.renderYawOffset));
            Vec3d var5 = this.getPositionVector();
            if (var1) {
               var10000 = var5;
               var10001 = ck.c_clash308(bC);
            } else {
               var10000 = var5;
               var10001 = bC;
            }

            Vec3d var3 = var10000.add(ck.rotateByYaw(var10001, 180.0F + this.renderYawOffset));
            this.b2.setLocationAndAngles(var2.x, var2.y, var2.z, this.renderYawOffset, 0.0F);
            this.V.setLocationAndAngles(var3.x, var3.y, var3.z, this.renderYawOffset, 0.0F);
            this.b2.onUpdate();
            this.V.onUpdate();
         }
      }
   }

   void ah_clash659() {
      if (this.getCurrentAction() != fp.SUMMON_SKELETON) {
         this.ad = 0;
      } else {
         if (this.ad++ > 45) {
            this.ad = 0;
         }
      }
   }

   @Override
   public f2 d_clash20() {
      return new f2(this.a9, this.bg, this.b4, this.a_);
   }

   void aj() {
      this.b4 = this.a9;
      this.a_ = this.bg;
      Vec3d var1 = this.W.subtract(this.bD);
      Vec3d var2 = ck.rotateByYaw(var1, this.renderYawOffset + 180.0F);
      this.a9 = gc.c_clash745(ThreadNames.b(var2.z * 40.0, -50.0, 50.0));
      this.bg = gc.c_clash745(ThreadNames.b(var2.x * 40.0, -50.0, 50.0));
   }

   public void f(Vec3d var1) {
      if (!(Boolean)this.m.get(bP)) {
         this.m.set(bP, true);
         if (this.bZ != null) {
            this.bZ.e(this);
         }

         this.bZ = null;
         Vec3d var2 = this.getPositionVector();
         Random var3 = this.getRNG();
         Vec3d var4 = var1 == null
            ? new Vec3d(var3.nextDouble(), var3.nextDouble(), var3.nextDouble()).normalize()
            : var2.subtract(var1).normalize();
         this.setVelocity(var4.x * 1.0, 1.0, var4.z * 1.0);
         this.b(fp.KNOCK_OUT_FLY);
         this.setNoGravity(false);
         this.noClip = false;
         this.getNavigator().clearPath();
         a(this, SoundHandler.GIRLS_GALATH_AAA, true);
      }
   }

   void a_clash660(Entity var1) {
      BaseGirlEntity.a_clash496(this, TextFormatting.YELLOW + "Galath is paralyzed! Now it's time to corrupt her");
      BaseGirlEntity.a_clash496(this, TextFormatting.GRAY + "(Walk to her and right click her)");
      PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.getPositionVector(), true), this);
      this.f(null);
      this.m.set(L, true);
   }

   @Override
   public void updateAITasks() {
      if (this.P) {
         GirlSavedData.a_clash848(this);
      } else {
         this.P_clash661();
         super.updateAITasks();
         this.o.a = this.x_clash662();
         if (this.k_clash637()) {
            this.ae_clash664();
         } else {
            this.an();
         }
      }
   }

   void P_clash661() {
      if (!this.bK) {
         this.setCustomModelCode(GirlWorldData.c_clash151(this));
         this.bK = true;
      }
   }

   boolean x_clash662() {
      return this.getCurrentAction() != fp.NULL ? false : !(Math.abs(this.motionX) + Math.abs(this.motionZ) > 0.01);
   }

   void aq() {
      if (this.world.isRemote) {
         if (this.ab_clash671() == null) {
            EntityPlayer var1 = this.z_clash528();
            if (var1 != null) {
               this.d_clash663(var1);
            }
         }
      }
   }

   void d_clash663(EntityPlayer var1) {
      AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getPersistentID());
      Vec3d var3 = new Vec3d(var1.posX, var1.posY + (var2 == null ? var1.eyeHeight : var2.getEyeHeight()), var1.posZ);
      Vec3d var4 = new Vec3d(this.posX, this.posY + this.getEyeHeight(), this.posZ);
      double var5 = var4.distanceTo(var3);
      double var7 = var3.y - var4.y;
      this.rotationPitch = (float)(-(Math.sin(var7 / var5) * (180.0 / Math.PI)));
   }

   void ae_clash664() {
      this.aO.setVisible(false);
      if (!GirlSavedData.c_clash847(this)) {
         GirlSavedData.a_clash848(this);
      } else if (this.ab_clash671() != null) {
         this.y_clash669();
      } else {
         this.m_clash665();
         if (this.aF() == null) {
            this.aJ();
         } else {
            this.am();
         }
      }
   }

   void m_clash665() {
      if (GirlSavedData.b_clash846(GirlSavedData.b_clash851(this))) {
         boolean var1 = this.v_clash675();
         if (var1) {
            Main.LOGGER.warn("mommy thinks she got no daughter but she actually does have one. Failsafe called. Hopefully its fixed");
         }
      }
   }

   void am() {
      if (!this.ai_clash666()) {
         this.m.set(bT, false);
         this.ao();
      }
   }

   boolean ai_clash666() {
      UUID var1 = GirlSavedData.b_clash851(this);
      if (var1 == null) {
         return false;
      }

      EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
      if (var2 == null) {
         return false;
      }

      BlockPos var3 = var2.getPosition();
      if (!this.a_clash667(var3)) {
         return false;
      }

      if (this.bZ != null) {
         this.bZ.e(this);
         this.bZ = null;
      }

      float var4 = this.getDistance(var2);
      PathNavigate var5 = this.getNavigator();
      if (var4 < 4.0F) {
         var5.clearPath();
         return false;
      }

      if (var4 > 16.0F) {
         var5.clearPath();
         this.b_clash668(var2);
         return true;
      }

      if (fl.a(this.aq).distanceSq(var3) > 16.0) {
         if (!this.onGround) {
            return true;
         }

         this.aq = this.a(var2, var3);
         if (this.aq == null) {
            this.b_clash668(var2);
         } else {
            var5.setPath(this.aq, 1.0);
         }
      }

      if (this.aq != null && !this.aq.isFinished()) {
         boolean var6 = var2.isSprinting() || this.getDistance(var2) > 7.0F;
         double var7 = var6 ? 0.55F : 0.5;
         double var9 = Math.floor(var4 / 5.0F) * 0.2;
         var7 += var9;
         if (this.isInWater()) {
            var7 *= 60.0;
         }

         var5.setSpeed(var7);
         this.m.set(bT, var6);
         this.b((fp)null);
         return true;
      } else {
         return false;
      }
   }

   boolean a_clash667(BlockPos var1) {
      if (this.bZ == null) {
         return true;
      }

      BlockPos var2 = this.getPosition();
      int var3 = Math.abs(var1.getX() - var2.getX()) + Math.abs(var1.getX() - var2.getX());
      return var3 > 16;
   }

   protected void b_clash668(EntityPlayer var1) {
      int var3 = 0;

      BlockPos var2;
      do {
         var2 = var1.getPosition().add(Reference.f.nextInt(4), 0, Reference.f.nextInt(4));
      } while (++var3 < 20 && !this.attemptTeleport(var2.getX(), var2.getY(), var2.getZ()));

      if (var3 >= 20) {
         this.setPosition(var1.posX, var1.posY, var1.posZ);
      }

      this.motionX = 0.0;
      this.motionY = 0.0;
      this.motionZ = 0.0;
   }

   @Nullable
   Path a(EntityPlayer var1, BlockPos var2) {
      PathNavigate var3 = this.getNavigator();
      return var3.getPathToEntityLiving(var1);
   }

   void aJ() {
      this.at();
      this.ay();
   }

   void y_clash669() {
      this.bG = null;
      this.aC = 0;
      if (this.bZ != null) {
         this.bZ.e(this);
         this.bZ = null;
      }
   }

   void at() {
      if (this.onGround) {
         if (this.aF() == null) {
            if (this.getCurrentAction() != fp.HUG_MANG) {
               if (!GirlSavedData.b_clash846(GirlSavedData.f_clash850(this.getGirlId()))) {
                  BlockPos var1 = this.getPosition();
                  BlockPos var2 = var1.add(-15.0, -15.0, -15.0);
                  BlockPos var3 = var1.add(15.0, 15.0, 15.0);
                  AxisAlignedBB var4 = new AxisAlignedBB(var2, var3);
                  List var5 = this.world.getEntitiesWithinAABB(ManglelieEntity.class, var4);
                  ManglelieEntity var6 = null;

                  for (ManglelieEntity var8 : (java.util.Collection<ManglelieEntity>) (var5) ) {
                     if (!var8.isDead && var8.a_clash413(true) == null) {
                        var6 = var8;
                        break;
                     }
                  }

                  if (var6 == null) {
                     if (this.getCurrentAction() == fp.RUN) {
                        this.b((fp)null);
                        this.getNavigator().clearPath();
                     }
                  } else {
                     this.f = this.getNavigator();
                     if (var6.getDistance(this) <= 3.65F) {
                        this.f.clearPath();
                        this.b(fp.HUG_MANG);
                        this.motionX = 0.0;
                        this.motionY = 0.0;
                        this.motionZ = 0.0;
                        this.setTargetPosition(this.getPositionVector());
                        this.setAnchored(true);
                        this.a_clash640(var6.getGirlId());
                        var6.a_clash414(this.getGirlId());
                        var6.b(fp.RIDE_MOMMY_HEAD);
                        GirlSavedData.e_clash845(this.getGirlId());
                     } else {
                        Vec3d var11 = this.getPositionVector();
                        Vec3d var12 = var6.getPositionVector();
                        Vec3d var9 = var12.subtract(var11);
                        float var10 = (float)gc.b(Math.atan2(var9.z, var9.x)) - 90.0F;
                        this.setYawRotation(var10);
                        this.f.clearPath();
                        this.f.tryMoveToEntityLiving(var6, 0.65F);
                        this.b(fp.RUN);
                     }
                  }
               }
            }
         }
      }
   }

   void ay() {
      fp var1 = this.getCurrentAction();
      if (var1 != fp.RUN) {
         if (var1 != fp.HUG_MANG) {
            if (!this.isAnchored() && var1 != fp.MASTERBATE) {
               EntityPlayer var2 = this.world.getClosestPlayerToEntity(this, 15.0);
               if (this.J_clash526() && var2 != null && var2.getDistance(this) < 2.0F && var2.getPersistentID().equals(this.O_clash527())) {
                  this.getNavigator().clearPath();
               } else {
                  if (this.bG == null
                     || this.getDistance(this.bG.getX(), this.bG.getY(), this.bG.getZ()) > this.i_clash670()
                     || this.aC > 175) {
                     int var3 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
                     int var4 = (this.getRNG().nextBoolean() ? 1 : -1) * this.getRNG().nextInt(10);
                     int var5 = this.world.provider.getDimensionType() == DimensionType.NETHER
                        ? (int)Math.ceil(this.posY)
                        : cj.a(this.world, this.getPosition().getX() + var3, this.getPosition().getZ() + var4);
                     this.bG = new BlockPos(this.getPosition().getX() + var3, var5, this.getPosition().getZ() + var4);
                     this.aC = 0;
                  }

                  if (Math.sqrt(this.bG.distanceSq(this.getPosition())) > 2.0) {
                     this.getNavigator().tryMoveToXYZ(this.bG.getX(), this.bG.getY(), this.bG.getZ(), 0.35F);
                     this.tickPathVelocity();
                  } else {
                     this.aC++;
                  }
               }
            } else {
               this.getNavigator().clearPath();
            }
         }
      }
   }

   BlockPos av() {
      UUID var1 = GirlSavedData.b_clash851(this);
      if (var1 == null) {
         return BlockPos.ORIGIN;
      }

      EntityPlayer var2 = this.world.getPlayerEntityByUUID(var1);
      return var2 == null ? BlockPos.ORIGIN : var2.getPosition();
   }

   double i_clash670() {
      return Math.sqrt(1800.0);
   }

   @Nullable
   public EntityPlayer ab_clash671() {
      List var1 = this.getPassengers();
      if (var1.isEmpty()) {
         return null;
      } else {
         return var1.get(0) instanceof EntityPlayer ? (EntityPlayer)var1.get(0) : null;
      }
   }

   @Nullable
   public UUID ax() {
      EntityPlayer var1 = this.ab_clash671();
      return var1 == null ? null : var1.getPersistentID();
   }

   @Override
   public void g_clash538(String var1) {
      super.g_clash538(var1);
      UUID var2 = this.O_clash527();
      if (var2 != null) {
         AllieWorldData.a(var2, NpcType.GALATH, var1);
      }
   }

   public void d(Vec3d var1) {
      this.motionX = this.motionX + var1.x;
      this.motionZ = this.motionZ + var1.z;
      this.motionY = var1.y / 2.0;
   }

   public void t_clash672() {
      this.setInteractionPlayerUUID(null);
      this.b((fp)null);
   }

   void aB() {
      EntityPlayer var1 = this.ab_clash671();
      if (var1 != null) {
         this.prevRenderYawOffset = var1.prevRotationYawHead;
         this.renderYawOffset = var1.rotationYawHead;
      }
   }

   void an() {
      this.aO.setVisible(true);
      this.ao();
      this.as();
   }

   void ao() {
      if (!fp.a(this, fp.MASTERBATE, fp.HUG_MANG)) {
         if (this.getInteractionPlayerUUID() == null) {
            this.Q_clash673();
            this.I_clash687();
            this.D_clash685();
            this.q_clash684();
            this.J_clash683();
            this.T_clash682();
            this.S_clash681();
            this.b_clash680();
            this.ad_clash679();
            this.aG();
            this.aA();
            this.aD();
            this.O_clash677();
            this.Z_clash676();
         }
      }
   }

   void Q_clash673() {
      if (this.k_clash637()) {
         if (this.M_clash691() == null) {
            int var1 = (Integer)this.m.get(bq);
            if (var1 != -1) {
               if (this.bZ != null) {
                  this.bZ.e(this);
               }

               this.bZ = null;
               this.b(fp.NULL);
            }
         }
      }
   }

   void as() {
      if (this.M_clash691() != null) {
         this.bG = null;
         this.aC = 0;
      } else if (!(Boolean)this.m.get(L)) {
         if (!(Boolean)this.m.get(bP)) {
            this.ay();
         }
      }
   }

   @Override
   public void b(fp var1) {
      fp var2 = this.getCurrentAction();
      if (var2 != fp.GALATH_DE_SUMMON) {
         if (var2 != fp.CORRUPT_CUM || var1 != fp.CORRUPT_FAST && var1 != fp.CORRUPT_SLOW) {
            if (var2 != fp.RAPE_CUM || var1 != fp.RAPE_ON_GOING) {
               if (var2 != fp.MORNING_BLOWJOB_CUM || var1 != fp.MORNING_BLOWJOB_SLOW && var1 != fp.MORNING_BLOWJOB_FAST) {
                  if (!this.world.isRemote && fp.a(var2, fp.CORRUPT_CUM, fp.RAPE_CUM, fp.MORNING_BLOWJOB_CUM)) {
                     GirlSavedData.a(this.getInteractionPlayerUUID(), this.world.getTotalWorldTime());
                  }

                  if (var1 == fp.CORRUPT_SLOW) {
                     this.aT = false;
                     if (var2 == fp.CORRUPT_INTRO) {
                        this.d(false);
                     }

                     if (this.k_clash637() && var2 == fp.NULL) {
                        this.d(true);
                     }
                  }

                  if (var2 == fp.GIVE_COIN && var1 == fp.NULL && !this.world.isRemote) {
                     this.ap();
                  }

                  if (var2 == fp.HUG_MANG && var1 == fp.NULL) {
                     this.al();
                  }

                  if (var2 == fp.MORNING_BLOWJOB_CUM && var1 == fp.NULL) {
                     this.aE();
                  }

                  super.b(var1);
               }
            }
         }
      }
   }

   void aE() {
      EntityPlayer var1 = this.S_clash495();
      if (var1 != null) {
         ResetGirlPacket.Handler.a((EntityPlayerMP)var1);
      }

      ResetGirlPacket.Handler.a_clash10(this);
   }

   void al() {
      this.setAnchored(false);
      ManglelieEntity var1 = this.a_clash638(true);
      if (var1 != null) {
         var1.c_clash410(true);
      }
   }

   void ap() {
      EntityPlayer var1 = this.S_clash495();
      if (var1 != null) {
         ItemStack var2 = var1.getHeldItemMainhand();
         var1.setHeldItem(EnumHand.MAIN_HAND, new ItemStack(GalathCoinItem.r));
         if (!var2.isEmpty()) {
            var1.inventory.addItemStackToInventory(var2);
         }

         PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), (EntityPlayerMP)var1);
         this.setInteractionPlayerUUID(null);
         this.a_clash689(null);
         var1.sendMessage(
            new TextComponentString(
               TextFormatting.GRAY
                  + "Defeating a succubus makes her accept the victor as her master, granting him a coin to which her soul is bound. Using the coin summons her, offering services on demand. If her master uses the coin on her or goes too far, she returns to the coin"
            )
         );
         GirlSavedData.a_clash848(this);
         var1.setPositionAndUpdate(var1.posX, Math.ceil(var1.posY) + 1.0, var1.posZ);
      }
   }

   @SideOnly(Side.CLIENT)
   void H_clash674() {
      fp var1 = this.getCurrentAction();
      if (this.ab != fp.CORRUPT_INTRO && var1 == fp.CORRUPT_INTRO) {
         EntityPlayerSP var2 = Minecraft.getMinecraft().player;
         if (!var2.getPersistentID().equals(this.getInteractionPlayerUUID())) {
            this.ab = var1;
         } else {
            float var3 = this.k_clash637() ? 0.0F : this.getYawRotation() + 180.0F;
            var2.rotationYaw = var3;
            var2.prevRotationYaw = var3;
            var2.rotationPitch = 80.0F;
            var2.prevRotationPitch = 80.0F;
            this.ab = var1;
         }
      } else {
         this.ab = var1;
      }
   }

   void d(boolean var1) {
      EntityPlayer var2 = this.S_clash495();
      if (var2 != null) {
         Vec3d var3;
         if (var1) {
            var3 = new Vec3d(-0.5, 0.5F - var2.getEyeHeight(), 0.4F).add(this.getTargetPosition());
         } else {
            var3 = ck.rotateByYaw(new Vec3d(0.5, 0.5F - var2.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
         }

         var2.setPositionAndUpdate(var3.x, var3.y, var3.z);
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public float v_clash550() {
      Minecraft var1 = Minecraft.getMinecraft();
      if (var1.gameSettings.thirdPersonView != 0) {
         return 1.0F;
      }

      switch (this.getCurrentAction()) {
         case CORRUPT_SLOW:
         case CORRUPT_FAST:
         case CORRUPT_CUM:
            break;
         case CORRUPT_INTRO:
            if (this.U) {
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
   protected boolean X_clash438() {
      return false;
   }

   public boolean v_clash675() {
      if (this.a_clash638(true) != null) {
         return false;
      }

      ManglelieEntity var1 = new ManglelieEntity(this.world);
      this.a_clash640(var1.getGirlId());
      var1.a_clash414(this.getGirlId());
      var1.c_clash410(true);
      var1.b(fp.RIDE_MOMMY_HEAD);
      var1.setPositionAndUpdate(this.posX, this.posY, this.posZ);
      this.world.spawnEntity(var1);
      return true;
   }

   void Z_clash676() {
      if (!this.k_clash637()) {
         fp var1 = this.getCurrentAction();
         if (var1 != fp.RAPE_CUM) {
            this.at = 0;
         } else {
            EntityPlayer var2 = this.S_clash495();
            if (var2 == null) {
               this.at = 0;
            } else if (++this.at == 15) {
               var2.attackEntityFrom(new SuccubusDamageSource(this), 2.1474836E9F);
            }
         }
      }
   }

   void O_clash677() {
      EntityLivingBase var1 = this.M_clash691();
      if (var1 != null) {
         for (EntityWitherSkeleton var3 : this.bI) {
            if (!var3.isDead && !(var1.getDistance(var3) < 15.0F)) {
               PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(var3.getPositionVector(), true), this);
               var3.setDead();
               this.world.removeEntity(var3);
            }
         }
      }
   }

   void aD() {
      if ((Boolean)this.m.get(bP)) {
         for (EntityWitherSkeleton var2 : this.bI) {
            if (!var2.isDead) {
               PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(var2.getPositionVector(), true), this);
               var2.setDead();
               this.world.removeEntity(var2);
            }
         }

         this.bI.clear();
      }
   }

   public static void c_clash678(EntityPlayer var0) {
      BaseGirlEntity var1 = BaseGirlEntity.getServerGirlEntity(GirlSavedData.b_clash853(var0));
      if (var1 != null) {
         if (var1.equals(var0.getRidingEntity())) {
            var1.setInteractionPlayerUUID(var0.getPersistentID());
            var1.b(fp.CONTROLLED_FLIGHT);
         }
      }
   }

   void aA() {
      for (EntityWitherSkeleton var2 : this.bI) {
         if (!var2.isDead && var2.ticksExisted % 10 == 0) {
            for (EntityPlayer var5 : (java.util.Collection<EntityPlayer>) ((WorldServer)this.world).getEntityTracker().getTrackingPlayers(var2)) {
               ((EntityPlayerMP)var5)
                  .connection
                  .sendPacket(
                     new SPacketParticles(
                        EnumParticleTypes.DRAGON_BREATH,
                        false,
                        (float)var2.posX,
                        (float)var2.posY,
                        (float)var2.posZ,
                        0.2F * ThreadNames.a_clash166(),
                        var2.getEyeHeight() / 2.0F,
                        0.2F * ThreadNames.a_clash166(),
                        0.0F,
                        5,
                        new int[0]
                     )
                  );
            }
         }
      }
   }

   void aG() {
      ArrayList var1 = new ArrayList();

      for (EntityWitherSkeleton var3 : this.bI) {
         if (var3.isDead) {
            var1.add(var3);
         }
      }

      for (EntityWitherSkeleton var5 : (java.util.Collection<EntityWitherSkeleton>) (var1) ) {
         this.bI.remove(var5);
      }
   }

   void ad_clash679() {
      if (this.getCurrentAction() == fp.KNOCK_OUT_STAND_UP) {
         this.bY++;
         if (this.bY == 39.0) {
            this.setNoGravity(true);
            this.setVelocity(0.0, 0.6F, 0.0);
            Vec3d var1 = this.getPositionVector();
            Vec3d var2 = var1.subtract(2.0, 2.0, 2.0);
            Vec3d var3 = var1.add(2.0, 2.0, 2.0);
            AxisAlignedBB var4 = new AxisAlignedBB(
               var2.x, var2.y, var2.z, var3.x, var3.y, var3.z
            );

            for (EntityLivingBase var7 : this.world.getEntitiesWithinAABB(EntityLivingBase.class, var4)) {
               if (!(var7 instanceof GalathEntity)) {
                  Vec3d var8 = var7.getPositionVector();
                  Vec3d var9 = var8.subtract(var1).normalize();
                  var7.motionX = var9.x * 1.0;
                  var7.motionZ = var9.z * 1.0;
                  var7.motionY = 1.0;
                  var7.attackEntityFrom(new GalathDamageSource(this), 0.5F);
                  if (var7 instanceof EntityPlayerMP) {
                     EntityPlayerMP var10 = (EntityPlayerMP)var7;
                     var10.connection.sendPacket(new SPacketEntityVelocity(var10));
                  }
               }
            }
         }

         if (!(this.bY < 58.0)) {
            this.setVelocity(Vec3d.ZERO);
            this.m.set(bP, false);
            this.bY = 0;
         }
      }
   }

   void b_clash680() {
      if (this.getCurrentAction() == fp.KNOCK_OUT_GROUND) {
         if (!(Boolean)this.m.get(L)) {
            if (!(++this.b3 < 50.0)) {
               this.b(fp.KNOCK_OUT_STAND_UP);
               this.bY = 0;
               this.b3 = 0;
            }
         }
      }
   }

   void S_clash681() {
      fp var1 = this.getCurrentAction();
      if (var1 == fp.KNOCK_OUT_GROUND || var1 == fp.KNOCK_OUT_STAND_UP) {
         this.motionX = 0.0;
         this.motionZ = 0.0;
         if ((Boolean)this.m.get(L)) {
            this.motionY = 0.0;
         }
      }
   }

   void T_clash682() {
      if (this.getCurrentAction() == fp.KNOCK_OUT_FLY) {
         BlockPos var1 = this.getPosition();
         if (!(this.world.getBlockState(var1).getBlock() instanceof BlockLiquid)) {
            if (this.onGround) {
               this.b(fp.KNOCK_OUT_GROUND);
            }
         } else {
            BlockPos var2 = var1;

            while (this.world.getBlockState(var2.up()).getBlock() instanceof BlockLiquid) {
               var2 = var2.up();
            }

            for (int var3 = -1; var3 < 2; var3++) {
               for (int var4 = -1; var4 < 2; var4++) {
                  this.world.setBlockState(var2.add(var3, 0, var4), Blocks.OBSIDIAN.getDefaultState());
               }
            }

            var2 = var2.up();
            this.setPositionAndUpdate(var2.getX(), var2.getY(), var2.getZ());
            this.setTargetPosition(new Vec3d(var2));
            PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(new Vec3d(var2), true), this);

            for (EntityPlayer var7 : (java.util.Collection<EntityPlayer>) ((WorldServer)this.world).getEntityTracker().getTrackingPlayers(this)) {
               ((EntityPlayerMP)var7)
                  .connection
                  .sendPacket(
                     new SPacketSoundEffect(
                        SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.AMBIENT, this.posX, this.posY, this.posZ, 1.0F, 1.0F
                     )
                  );
            }

            this.b(fp.KNOCK_OUT_GROUND);
         }
      }
   }

   void J_clash683() {
      if (this.bZ == GalathFlightData.CHANGE_POSITION) {
         int var1 = this.ar();
         this.noClip = var1 == 0;
         if (!this.world.isAirBlock(this.getPosition())) {
            this.noClip = true;
         }
      }
   }

   void q_clash684() {
      if (this.bZ != null) {
         this.bZ.a_clash708(this);
      }
   }

   void D_clash685() {
      if (this.M_clash691() == null) {
         this.aH();
      } else if (this.bZ == null) {
         this.z_clash686();
      } else {
         if (this.bZ.c_clash707(this)) {
            this.z_clash686();
         }
      }
   }

   void z_clash686() {
      if (!(Boolean)this.m.get(bP)) {
         GalathFlightData var1 = this.bZ;
         if (this.getInteractionPlayerUUID() != null) {
            if (var1 != null) {
               var1.e(this);
            }

            this.bZ = null;
         } else if (var1 != null && var1.applyAttackCoolDown) {
            var1.e(this);
            this.bZ = GalathFlightData.CHANGE_POSITION;
            this.bZ.b_clash706(this);
         } else {
            GalathFlightData[] var2 = GalathFlightData.values();

            GalathFlightData var3;
            do {
               var3 = var2[this.getRNG().nextInt(var2.length)];
            } while (!this.a(var3));

            this.bZ = var3;
            if (var1 != null) {
               var1.e(this);
            }

            this.bZ.b_clash706(this);
         }
      }
   }

   boolean a(GalathFlightData var1) {
      return var1.onlyDoThisOnPlayers && !(this.M_clash691() instanceof EntityPlayer) ? false : var1.d_clash709(this);
   }

   void aH() {
      this.bZ = null;
   }

   void I_clash687() {
      if (!this.f_clash688()) {
         if (this.getInteractionPlayerUUID() == null) {
            boolean var1 = this.k_clash637();
            float var2 = var1 ? 7.0F : 20.0F;
            Vec3d var3 = new Vec3d(var2, var2, var2);
            Vec3d var4 = this.getPositionVector();
            Vec3d var5 = var4.subtract(var3);
            Vec3d var6 = var4.add(var3);
            AxisAlignedBB var7 = new AxisAlignedBB(
               var5.x, var5.y, var5.z, var6.x, var6.y, var6.z
            );
            Object var8 = var1 ? this.a(var7) : this.b(var7);
            if (var8 == null) {
               this.aI();
            } else {
               this.a_clash689((EntityLivingBase)var8);
               BaseGirlEntity.a(this, SoundHandler.GIRLS_GALATH_DIALOG[1], true);
               if (this.bZ != null) {
                  this.bZ.e(this);
               }

               this.bZ = GalathFlightData.CHANGE_POSITION;
               this.bZ.b_clash706(this);
            }
         }
      }
   }

   EntityPlayer b(AxisAlignedBB var1) {
      List var2 = this.world
         .getEntitiesWithinAABB(EntityPlayer.class, var1, var0 -> !AbstractPlayerGirlEntity.e(var0) && !var0.isCreative() && !var0.isSpectator());
      return var2.isEmpty() ? null : (EntityPlayer)var2.get(0);
   }

   EntityMob a(AxisAlignedBB var1) {
      List var2 = this.world.getEntitiesWithinAABB(EntityMob.class, var1);
      if (var2.isEmpty()) {
         return null;
      }

      ArrayList var3 = new ArrayList();

      for (EntityMob var5 : (java.util.Collection<EntityMob>) (var2) ) {
         if (com.trolmastercard.sexmod.MobPredicates.a_clash801(var5)) {
            var3.add(var5);
         }
      }

      Vec3d var7 = this.getPositionVector().add(0.0, this.getEyeHeight(), 0.0);

      for (EntityMob var6 : (java.util.Collection<EntityMob>) (var3) ) {
         if (com.trolmastercard.sexmod.MobPredicates.a(this.world, var7, var6)) {
            return var6;
         }
      }

      return null;
   }

   void aI() {
      if (this.M_clash691() != null) {
         this.a_clash689(null);
         if (this.bZ != null) {
            this.bZ.e(this);
         }

         this.bZ = null;
         if (!(Boolean)this.m.get(bP)) {
            this.b(fp.NULL);
         }
      }
   }

   boolean f_clash688() {
      EntityLivingBase var1 = this.M_clash691();
      if (var1 == null) {
         return false;
      } else if (var1.isDead) {
         return false;
      } else if (var1.dimension != this.dimension) {
         return false;
      } else {
         float var2 = this.getDistance(var1);
         float var3 = this.k_clash637() ? 16.0F : 30.0F;
         if (var2 > var3) {
            return false;
         } else if (!(var1 instanceof EntityPlayer)) {
            return true;
         } else {
            EntityPlayer var4 = (EntityPlayer)var1;
            if (BaseGirlEntity.c_clash531(var4.getPersistentID()) != null) {
               return false;
            } else {
               return var4.isCreative() ? false : !var4.isSpectator();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public BaseGirlEntity E_clash543() {
      ManglelieEntity var1 = this.a_clash638(false);
      if (var1 == null) {
         return super.E_clash543();
      }

      EntityPlayerSP var2 = Minecraft.getMinecraft().player;
      if (var2.isSneaking()) {
         return var1;
      }

      var2.sendStatusMessage(new TextComponentString(TextFormatting.GRAY + "[sneak] + [right click] if you want to edit Manglelie instead"), true);
      return super.E_clash543();
   }

   protected boolean processInteract(EntityPlayer var1, EnumHand var2) {
      return this.k_clash637() ? this.a(var1, var2) : this.b(var1, var2);
   }

   boolean a(EntityPlayer var1, EnumHand var2) {
      if (!var1.getPersistentID().equals(this.O_clash527())) {
         return false;
      }

      if (fp.a(this, fp.HUG_MANG, fp.RUN, fp.GALATH_SUMMON, fp.GALATH_DE_SUMMON, fp.MASTERBATE)) {
         return false;
      }

      if (!GalathCoinItem.r.equals(var1.getHeldItem(EnumHand.OFF_HAND).getItem())
         && !GalathCoinItem.r.equals(var1.getHeldItem(EnumHand.MAIN_HAND).getItem())) {
         this.a(SoundHandler.GIRLS_GALATH_HUH);
         String[] var3;
         if (!var1.onGround) {
            var3 = new String[]{"ride"};
         } else if (this.a_clash638(false) == null) {
            var3 = new String[]{"cowgirl", "anal", "ride"};
         } else {
            var3 = new String[]{"cowgirl", "anal", "threesome", "ride"};
         }

         if (this.world.isRemote) {
            a(var1, this.af_clash520(), var3, false);
         }

         return true;
      } else {
         return false;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void a(String var1, UUID var2) {
      if ("ride".equals(var1)) {
         GalathFlightHud.f_clash791();
         PacketHandler.b.sendToServer(new RequestRidingPacket());
      } else if ("anal".equals(var1)) {
         BeeScreen.enableInteraction();
         d3.setMovementLock(false);
         ThreadNames.a(1200, () -> {
            EntityPlayerSP var1x = Minecraft.getMinecraft().player;
            this.setTargetPosition(var1x.getPositionVector());
            this.setYawRotation(0.0F);
            this.setInteractionPlayerUUID(var1x.getPersistentID());
            this.setAnchored(true);
            this.b(fp.CORRUPT_SLOW);
         });
      } else if ("cowgirl".equals(var1)) {
         BeeScreen.enableInteraction();
         d3.setMovementLock(false);
         ThreadNames.a(1200, () -> {
            EntityPlayerSP var1x = Minecraft.getMinecraft().player;
            this.setTargetPosition(var1x.getPositionVector());
            this.setYawRotation(var1x.rotationYaw + 180.0F);
            this.b(fp.RAPE_INTRO);
            this.setInteractionPlayerUUID(var1x.getPersistentID());
            this.setAnchored(true);
         });
      } else {
         if ("threesome".equals(var1)) {
            ManglelieEntity var3 = this.a_clash638(false);
            if (var3 == null) {
               return;
            }

            BeeScreen.enableInteraction();
            d3.setMovementLock(false);
            ThreadNames.a(1200, () -> {
               Minecraft var2x = Minecraft.getMinecraft();
               EntityPlayerSP var3x = var2x.player;
               var2x.gameSettings.thirdPersonView = 1;
               var3.setTargetPosition(var3x.getPositionVector());
               this.setTargetPosition(var3x.getPositionVector());
               var3.setYawRotation(var3x.rotationYaw + 180.0F);
               this.setYawRotation(var3x.rotationYaw);
               var3.b(fp.THREESOME_SLOW);
               this.b(fp.PUSSY_LICKING);
               var3.setInteractionPlayerUUID(var3x.getPersistentID());
               this.setInteractionPlayerUUID(var3x.getPersistentID());
               var3.setAnchored(true);
               this.setAnchored(true);
            });
         }
      }
   }

   boolean b(EntityPlayer var1, EnumHand var2) {
      if (!(Boolean)this.m.get(bP)) {
         return super.processInteract(var1, var2);
      } else if (this.getCurrentAction() != fp.KNOCK_OUT_GROUND) {
         return super.processInteract(var1, var2);
      } else if (this.world.isRemote) {
         var1.rotationYaw -= -128.0F;
         var1.rotationPitch = 19.0F;
         return true;
      } else {
         this.b(fp.CORRUPT_INTRO);
         this.setInteractionPlayerUUID(var1.getPersistentID());
         this.setAnchored(true);
         this.setTargetPosition(this.getPositionVector());
         this.setYawRotation(var1.rotationYaw);
         PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var1);
         var1.setPositionAndUpdate(this.posX, this.posY, this.posZ);
         return true;
      }
   }

   @Nullable
   public Entity[] getParts() {
      return new Entity[]{this.V, this.b2};
   }

   public void a_clash689(@Nullable EntityLivingBase var1) {
      if (var1 == null) {
         this.m.set(bq, -1);
      } else {
         this.m.set(bq, var1.getEntityId());
      }
   }

   public int ar() {
      return (Integer)this.m.get(aP);
   }

   public void b_clash690(int var1) {
      this.m.set(aP, var1);
   }

   public EntityLivingBase M_clash691() {
      int var1 = (Integer)this.m.get(bq);
      return -1 == var1 ? null : (EntityLivingBase)this.world.getEntityByID(var1);
   }

   public static Float a_clash692(GalathEntity var0, float var1) {
      fp var2 = var0.getCurrentAction();
      if (var2 != fp.FLY && var2 != fp.SUMMON_SKELETON && var2 != fp.RAPE_PREPARE) {
         return null;
      }

      EntityLivingBase var3 = var0.M_clash691();
      if (var3 == null) {
         return null;
      }

      Vec3d var4 = RotationHelper.a(new Vec3d(var3.lastTickPosX, var3.lastTickPosY, var3.lastTickPosZ), var3.getPositionVector(), var1);
      Vec3d var5 = RotationHelper.a(new Vec3d(var0.lastTickPosX, var0.lastTickPosY, var0.lastTickPosZ), var0.getPositionVector(), var1);
      Vec3d var6 = var4.subtract(var5);
      float var7 = (float)gc.b(Math.atan2(var6.z, var6.x)) - 90.0F;
      var0.renderYawOffset = var7;
      var0.prevRenderYawOffset = var7;
      return var7;
   }

   void c_clash693(float var1) {
      if (this.world.isRemote) {
         if (!(this.getHealth() - var1 <= 0.0F)) {
            long var2 = System.currentTimeMillis();
            if (var2 >= this.bc + 1000L) {
               this.a(SoundHandler.GIRLS_GALATH_UUH);
               this.bc = var2;
            }
         }
      }
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (var1.isFireDamage()) {
         return false;
      }

      if (DamageSource.DROWN.equals(var1)) {
         return false;
      }

      if (DamageSource.CACTUS.equals(var1)) {
         return false;
      }

      if (DamageSource.FALL.equals(var1)) {
         return false;
      }

      if (DamageSource.FLY_INTO_WALL.equals(var1)) {
         return false;
      }

      this.c_clash693(var2);
      return super.attackEntityFrom(var1, var2);
   }

   public boolean attackEntityFromPart(MultiPartEntityPart var1, DamageSource var2, float var3) {
      if (this.world.isRemote) {
         return false;
      }

      if (!(var2.getTrueSource() instanceof EntityPlayer)) {
         return false;
      }

      if (var1 == this.V) {
         this.m.set(b7, false);
         PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.V.getPositionVector(), false), this);
      }

      if (var1 == this.b2) {
         this.m.set(bN, false);
         PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.b2.getPositionVector(), false), this);
      }

      return true;
   }

   @Override
   public void reinitTasks() {
      this.a_clash689(null);
      this.aH();
   }

   public World getWorld() {
      return this.world;
   }

   public void setFire(int var1) {
   }

   public void fall(float var1, float var2) {
   }

   @Nullable
   @Override
   protected fp getNextAction(fp var1) {
      return null;
   }

   @Override
   protected fp getCumAction(fp var1) {
      if (var1 == fp.CORRUPT_FAST || var1 == fp.CORRUPT_SLOW) {
         return fp.CORRUPT_CUM;
      }

      if (var1 == fp.RAPE_ON_GOING) {
         return fp.RAPE_CUM;
      }

      if (fp.a(var1, fp.MORNING_BLOWJOB_SLOW, fp.MORNING_BLOWJOB_FAST)) {
         this.S = true;
      }

      return null;
   }

   @Override
   public boolean c_clash21() {
      return this.bb;
   }

   @Override
   public boolean a_clash22() {
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

   public void c_clash694(boolean var1) {
      fp var2 = this.getCurrentAction();
      if (var2 == fp.RAPE_ON_GOING || var2 == fp.RAPE_INTRO) {
         EntityPlayer var3 = this.S_clash495();
         if (var3 != null) {
            if (!(0.0F >= var3.getHealth() - 1.0F)) {
               if (!var3.capabilities.isCreativeMode) {
                  var3.attackEntityFrom(new SuccubusDamageSource(this), 1.0F);
                  if (var1) {
                     this.heal(1.5F);
                  }
               }
            }
         }
      }
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setString("sexmod:master", (String)this.m.get(v));
      if (this.bA) {
         var1.setBoolean("sexmod:despawned", true);
      }
   }

   @Override
   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.m.set(v, var1.getString("sexmod:master"));
      if (var1.getBoolean("sexmod:despawned")) {
         this.P = true;
      }

      UUID var2 = this.O_clash527();
      if (var2 != null) {
         String var3 = AllieWorldData.a(var2, NpcType.GALATH);
         if (var3 != null) {
            this.g_clash538(var3);
         }
      }
   }

   public void ak() {
      if (this.getCurrentAction() != fp.MASTERBATE_SITTING) {
         this.bx = true;
         this.b(fp.MASTERBATE_SITTING);
      }
   }

   public void a_clash695() {
      this.a5 = true;
      this.b(fp.PUSSY_LICKING);
   }

   @Override
   protected boolean a(fp var1, String var2, boolean var3, AnimationEvent var4) {
      if (var1 == fp.MASTERBATE_SITTING && this.bx) {
         this.bx = false;
         this.a("animation.galath.masterbating_sitting", true, var4, true);
         return true;
      }

      if (var1 == fp.MORNING_BLOWJOB_FAST && this.S) {
         this.b(fp.MORNING_BLOWJOB_CUM);
         return true;
      }

      if (var1 == fp.MORNING_BLOWJOB_FAST && this.aD) {
         this.a("animation.shared.bed_fast", true, var4, true);
         this.aD = false;
         return true;
      }

      if (var1 == fp.MORNING_BLOWJOB_CUM) {
         this.b((fp)null);
         return true;
      }

      if (var1 == fp.PUSSY_LICKING && this.a5) {
         this.a5 = false;
         this.a("animation.galath.pussy_licking", true, var4, true);
         return true;
      }

      if (var1 != fp.MORNING_BLOWJOB_SLOW || !this.S && !d3.d) {
         if (var1 == fp.MORNING_BLOWJOB_SLOW && this.bt) {
            this.bt = false;
            this.a("animation.shared.bed_slow", true, var4, true);
            return true;
         } else if (var1 == fp.MORNING_BLOWJOB_FAST && !d3.d) {
            this.b(fp.MORNING_BLOWJOB_SLOW);
            this.bt = true;
            this.a("animation.shared.bed_back", true, var4, true);
            return true;
         } else {
            return false;
         }
      } else {
         this.aD = true;
         this.b(fp.MORNING_BLOWJOB_FAST);
         this.a("animation.shared.bed_soft", true, var4, true);
         return true;
      }
   }

   public float b_clash696(float var1) {
      fp var2 = this.getCurrentAction();
      if (var2 == fp.PUSSY_LICKING && !this.a5) {
         return 0.0F;
      }

      if (var2 == fp.MASTERBATE_SITTING && !this.bx) {
         return 1.0F;
      }

      float var3 = fp.d(this, var1);
      return var2 == fp.MASTERBATE_SITTING ? var3 : 1.0F - var3;
   }

   @Override
   protected <E extends IAnimatable> PlayState a(AnimationEvent<E> var1) {
      if (this.isLocallyRegistered()) {
         this.a("animation.galath.idle", true, var1);
         return PlayState.CONTINUE;
      }

      fp var2 = this.getCurrentAction();
      AnimationController var3 = var1.getController();
      var3.setAnimationSpeed(1.0);
      if (var3.equals(this.s)) {
         if (var2.autoBlink && var2 != fp.GALATH_DE_SUMMON) {
            this.a("animation.galath.blink", true, var1);
            return PlayState.CONTINUE;
         } else {
            return PlayState.STOP;
         }
      } else if (var3.equals(this.E)) {
         if (var2 != fp.NULL) {
            return PlayState.STOP;
         } else if (!this.onGround) {
            this.a("animation.galath.controlled_flight", true, var1);
            return PlayState.CONTINUE;
         } else {
            Vec3d var4 = this.getPositionVector().subtract(new Vec3d(this.lastTickPosX, this.lastTickPosY, this.lastTickPosZ));
            if (var4.equals(Vec3d.ZERO)) {
               this.a("animation.galath.idle", true, var1);
               return PlayState.CONTINUE;
            } else {
               this.rotationYaw = this.rotationYawHead;
               this.a("animation.galath." + (this.m.get(bT) ? "run" : "walk"), true, var1);
               return PlayState.CONTINUE;
            }
         }
      } else {
         switch (this.getCurrentAction()) {
            case HUG_MANG:
               this.a("animation.galath.hug_mang", true, var1);
               break;
            case MORNING_BLOWJOB_SLOW:
               this.a(this.bt ? "animation.shared.bed_back" : "animation.shared.bed_slow", true, var1);
               break;
            case MORNING_BLOWJOB_FAST:
               if (this.aD) {
                  this.a("animation.shared.bed_soft", true, var1);
               } else {
                  this.a("animation.shared.bed_fast", 4, 0.75F, var1);
               }
               break;
            case MORNING_BLOWJOB_CUM:
               this.a("animation.shared.bed_cum", true, var1);
               break;
            case RAPE_INTRO:
               this.a("animation.galath.rape_intro", true, var1);
               break;
            case RAPE_ON_GOING:
               this.a("animation.galath.rape" + this.b1, true, var1);
               break;
            case RAPE_CUM:
               this.a("animation.galath.rape_cum", true, var1);
               break;
            case RAPE_CHARGE:
               this.a("animation.galath.rape_charge", true, var1);
               break;
            case RAPE_CUM_IDLE:
               this.a("animation.galath.rape_cum_idle", true, var1);
               break;
            case CORRUPT_SLOW:
               this.a("animation.galath.corrupt_slow", true, var1);
               break;
            case CORRUPT_FAST:
               this.a("animation.galath.corrupt_" + (this.aT ? "hard" : "soft"), true, var1);
               break;
            case CORRUPT_CUM:
               this.a("animation.galath.corrupt_cum", true, var1);
               break;
            case MASTERBATE:
               this.a("animation.galath.masterbate", true, var1);
               break;
            case RAPE_PREPARE:
               this.a("animation.galath.rape_prepare", true, var1);
               break;
            case CORRUPT_INTRO:
               this.a("animation.galath.corrupt_intro", true, var1);
            case COWGIRLCUM:
            default:
               break;
            case NULL:
               return PlayState.STOP;
            case FLY:
               this.a("animation.galath.idle_flying", true, var1);
               break;
            case SUMMON_SKELETON:
               this.a("animation.galath.summon_skeleton" + (this.m.get(ay) ? "Mirrored" : ""), true, var1);
               break;
            case ATTACK_SWORD:
               this.a("animation.galath.attack", true, var1);
               break;
            case KNOCK_OUT_FLY:
               var3.setAnimationSpeed(1.5);
               this.a("animation.galath.knockout_air", true, var1);
               break;
            case KNOCK_OUT_GROUND:
               this.a("animation.galath.knocked_out", true, var1);
               break;
            case KNOCK_OUT_STAND_UP:
               this.a("animation.galath.knocked_out_stand_up", true, var1);
               break;
            case CONTROLLED_FLIGHT:
               this.a("animation.galath.controlled_flight", true, var1);
               break;
            case BOOST:
               this.a("animation.galath.boost", true, var1);
               break;
            case GALATH_SUMMON:
               this.a("animation.galath.summon", false, var1);
               break;
            case GALATH_DE_SUMMON:
               this.a("animation.galath.desummon" + (this.onGround ? "_standing" : ""), true, var1);
               break;
            case GIVE_COIN:
               this.a("animation.galath.give_coin", true, var1);
               break;
            case RUN:
               var3.setAnimationSpeed(0.7);
               this.a("animation.galath.running", true, var1);
               break;
            case PUSSY_LICKING:
               this.a(this.a5 ? "animation.galath.pussy_licking_forward" : "animation.galath.pussy_licking", true, var1);
               break;
            case MASTERBATE_SITTING:
               this.a(this.bx ? "animation.galath.pussy_licking_back" : "animation.galath.masterbating_sitting", true, var1);
               break;
            case MASTERBATE_SITTING_CUM:
               this.a("animation.galath.masterbating_sitting_cum", true, var1);
         }

         return PlayState.CONTINUE;
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerControllers(AnimationData var1) {
      this.C = new GirlAnimationController<>(this, "action", 0.0F, this::a);
      this.E = new AnimationController<>(this, "movement", 5.0F, this::a);
      this.s = new AnimationController<>(this, "eyes", 10.0F, this::a);
      this.C
         .registerSoundListener(
            var1x -> {
               switch (var1x.sound) {
                  case "goodTiming":
                     this.a(SoundHandler.GIRLS_GALATH_DIALOG[4]);
                     this.sendChatMessage("Good timing boy~");
                     break;
                  case "huh":
                     this.a(SoundHandler.GIRLS_GALATH_HUH);
                     break;
                  case "giggle":
                     Vec3d var20 = this.A_clash497();
                     this.world
                        .playSound(
                           var20.x,
                           var20.y,
                           var20.z,
                           SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_GIGGLE),
                           SoundCategory.HOSTILE,
                           1.0F,
                           1.0F,
                           false
                        );
                     break;
                  case "dialog1":
                     this.a(SoundHandler.GIRLS_GALATH_DIALOG[1]);
                     break;
                  case "moan":
                     this.a(SoundHandler.GIRLS_GALATH_MOAN);
                     break;
                  case "breath":
                     this.a(SoundHandler.GIRLS_GALATH_BREATHING);
                     break;
                  case "dialog5":
                     this.a(SoundHandler.GIRLS_GALATH_DIALOG[5]);
                     break;
                  case "switchmoan":
                     if (this.a6) {
                        this.a(SoundHandler.GIRLS_GALATH_BREATHING);
                     } else {
                        this.a(this.getRNG().nextBoolean() ? SoundHandler.GIRLS_GALATH_MOAN : SoundHandler.GIRLS_GALATH_AHH);
                     }

                     this.a6 = !this.a6;
                     break;
                  case "lightcharge":
                     Vec3d var19 = this.A_clash497();
                     this.world
                        .playSound(
                           var19.x,
                           var19.y,
                           var19.z,
                           SoundHandler.randomSound(SoundHandler.GIRLS_GALATH_LIGHTCHARGE),
                           SoundCategory.HOSTILE,
                           1.0F,
                           1.0F,
                           false
                        );
                     break;
                  case "strongcharge":
                     this.a(SoundHandler.GIRLS_GALATH_STRONGCHARGE);
                     break;
                  case "hmph":
                     this.a(SoundHandler.GIRLS_GALATH_HMPH);
                     break;
                  case "cum":
                     this.a(SoundHandler.MISC_SMALLINSERTS, 2.0F);
                     break;
                  case "giggle0":
                     this.a(SoundHandler.GIRLS_GALATH_GIGGLE[0]);
                     break;
                  case "orgasm":
                     this.a(SoundHandler.GIRLS_GALATH_ORGASM);
                     break;
                  case "pound":
                     this.a(SoundHandler.MISC_POUNDING);
                     break;
                  case "flap":
                     Vec3d var4 = this.A_clash497();
                     this.world
                        .playSound(
                           var4.x,
                           var4.y,
                           var4.z,
                           SoundHandler.randomSound(SoundHandler.MISC_FLAP),
                           SoundCategory.HOSTILE,
                           1.0F,
                           1.0F,
                           false
                        );
                     break;
                  case "startRenderSword":
                     this.ap = true;
                     this.bu = true;
                     break;
                  case "stopFadeInParticles":
                     this.bu = false;
                     break;
                  case "stopRenderSword":
                     this.ap = false;
                     this.bu = false;
                     break;
                  case "dontDrawStars":
                     this.aL = false;
                     break;
                  case "setNude":
                     this.bb = true;
                     Vec3d var5 = this.getPositionVector();
                     Vec3d var6 = this.getCachedBoneOffset("slipR").add(var5);
                     Vec3d var7 = this.getCachedBoneOffset("slipL").add(var5);
                     Vec3d var8 = this.getCachedBoneOffset("turnable").add(var5);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var6.x, var6.y, var6.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var7.x, var7.y, var7.z, 0.0, 0.0, 0.0, new int[0]);
                     this.world
                        .spawnParticle(EnumParticleTypes.DRAGON_BREATH, var8.x, var8.y, var8.z, 0.0, 0.0, 0.0, new int[0]);
                     break;
                  case "rapeIntroDone":
                     if (this.isControlledByLocalPlayer()) {
                        this.b(fp.RAPE_ON_GOING);
                     }
                     break;
                  case "rape_switch":
                     Random var9 = this.getRNG();
                     int var10 = this.b1;

                     do {
                        this.b1 = var9.nextInt(3);
                     } while (this.b1 == var10);

                     if (!this.k_clash637() && this.isControlledByLocalPlayer()) {
                        EntityPlayerSP var22 = Minecraft.getMinecraft().player;
                        if (0.0F >= var22.getHealth() - 1.0F) {
                           this.b(fp.RAPE_CUM);
                        }
                     }
                     break;
                  case "poundRape":
                     this.a(SoundHandler.MISC_POUNDING);
                     if (this.isControlledByLocalPlayer()) {
                        if (this.k_clash637()) {
                           HornyMeterHud.addToHornyMeter(0.03F);
                        } else {
                           PacketHandler.b.sendToServer(new GalathRapePouncePacket(true));
                        }
                     }
                     break;
                  case "rapeHurt":
                     if (!this.k_clash637() && this.isControlledByLocalPlayer()) {
                        PacketHandler.b.sendToServer(new GalathRapePouncePacket(false));
                     }
                     break;
                  case "enableRapeUI":
                     if (this.isControlledByLocalPlayer()) {
                        if (this.k_clash637()) {
                           HornyMeterHud.a_clash359(false);
                        } else {
                           EscapeMinigameHud.a_clash738();
                        }
                     }
                     break;
                  case "removeUI":
                     if (this.isControlledByLocalPlayer() && !this.k_clash637()) {
                        EscapeMinigameHud.d_clash739();
                     }
                     break;
                  case "reloadRenderer":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     Minecraft var23 = Minecraft.getMinecraft();
                     if (var23.gameSettings.thirdPersonView != 0) {
                        var23.renderGlobal.loadRenderers();
                     }
                     break;
                  case "corruptSwitch":
                     if (this.isControlledByLocalPlayer() && d3.d) {
                        this.b(fp.CORRUPT_FAST);
                     }
                     break;
                  case "corrupt_hard":
                     if (this.isControlledByLocalPlayer() && d3.d) {
                        this.aT = true;
                        this.N();
                     }
                     break;
                  case "corrupt_hard_end":
                     this.b(fp.CORRUPT_SLOW);
                     this.aT = false;
                     break;
                  case "addCum":
                     HornyMeterHud.addToHornyMeter(0.03);
                     break;
                  case "clearcum":
                     CummyEntity.a_clash747(this);
                     break;
                  case "setCamCorrupt":
                     if (!this.isControlledByLocalPlayer()) {
                        return;
                     }

                     this.U = true;
                     EntityPlayerSP var21 = Minecraft.getMinecraft().player;
                     float var24 = this.getYawRotation() + 220.0F;
                     Vec3d var14 = ck.rotateByYaw(new Vec3d(0.5, 0.5F - var21.getEyeHeight(), 0.4F), this.getYawRotation()).add(this.getTargetPosition());
                     PacketHandler.b.sendToServer(new TeleportPlayerPacket(var21.getPersistentID().toString(), var14, var24, 15.0F));
                     HornyMeterHud.showHornyMeter();
                     break;
                  case "enableBoyCam":
                     if (this.isControlledByLocalPlayer()) {
                        this.U = false;
                     }
                     break;
                  case "masterbateCumming":
                     if (CommandFuta.e) {
                        CummyEntity.a(new ep(90, var0 -> {
                           Vec3d var1xx = var0.d_clash548("futaCockTip");
                           Vec3d var2 = var0.d_clash548("futaCockTipDirHelp");
                           return var1xx.subtract(var2).normalize();
                        }, var0 -> var0.getCachedBoneOffset("futaCockTip").add(var0.getTargetPosition()), this, 0.3F, 0.3F));
                     }
                     break;
                  case "creampie":
                     CummyEntity.a(
                        new ep(
                           100,
                           var1xx -> ck.rotateByYaw(new Vec3d(0.0, 0.0, 0.6F), this.getYawRotation()),
                           var0 -> var0.getCachedBoneOffset("creampiePos").add(var0.getTargetPosition()),
                           this,
                           0.6F,
                           0.5F
                        )
                     );
                  case "creampieGalath":
                     if (CommandFuta.e) {
                        CummyEntity.a(new ep(130, var0 -> {
                           Vec3d var1xx = var0.d_clash548("futaCockTip");
                           Vec3d var2 = var0.d_clash548("futaCockTipDirHelp");
                           return var1xx.subtract(var2).normalize();
                        }, var0 -> var0.getCachedBoneOffset("futaCockTip").add(var0.getTargetPosition()), this, 0.3F, 0.3F));
                     }

                     this.a(SoundHandler.randomSound(SoundHandler.MISC_SMALLINSERTS), 3.0F);
                     break;
                  case "blackScreenTamed":
                     if (this.k_clash637()) {
                        return;
                     }
                  case "blackScreen":
                     if (this.isControlledByLocalPlayer()) {
                        BeeScreen.enableInteraction();
                     }
                     break;
                  case "blackScreenMaster":
                     if (Minecraft.getMinecraft().player.getPersistentID().equals(this.O_clash527())) {
                        BeeScreen.enableInteraction();
                        d3.setMovementLock(false);
                     }
                     break;
                  case "flapControlled":
                     if (this.isControlledByLocalPlayer()) {
                        GalathFlightHud.f_clash791();
                        this.a(SoundHandler.MISC_FLAP);
                        Minecraft var12 = Minecraft.getMinecraft();
                        EntityPlayerSP var15 = var12.player;
                        MovementInput var16 = var15.movementInput;
                        Vec2f var17 = var16.getMoveVector();
                        if (var17.x != 0.0F || var17.y != 0.0F) {
                           Vec3d var18 = ck.a(
                              new Vec3d(-var17.x, 0.0, var17.y),
                              RotationHelper.lerp(var15.prevRotationPitch, var15.rotationPitch, var12.getRenderPartialTicks()),
                              RotationHelper.lerp(var15.prevRotationYawHead, var15.rotationYawHead, var12.getRenderPartialTicks())
                           );
                           PacketHandler.b.sendToServer(new UpdateVelocityPacket(var18, this.getGirlId()));
                        }
                     }
                     break;
                  case "clap":
                     this.a(SoundHandler.MISC_CLAP);
                     break;
                  case "energysound":
                     this.a(SoundHandler.MISC_BEEW[1]);
                     break;
                  case "energy2":
                     this.a(SoundHandler.MISC_BEEW[2]);
                     break;
                  case "tpSound":
                     this.a(SoundHandler.MISC_WEOWEO[2]);
                     break;
                  case "lick":
                     this.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
                     break;
                  case "setCoinLook":
                     if (this.isControlledByLocalPlayer()) {
                        EntityPlayerSP var11 = Minecraft.getMinecraft().player;
                        float var13 = this.getYawRotation() + 180.0F;
                        var11.rotationYaw = var13;
                        var11.prevRotationYaw = var13;
                        var11.rotationPitch = 0.0F;
                        var11.prevRotationPitch = 0.0F;
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
      var1.addAnimationController(this.C);
      var1.addAnimationController(this.s);
      var1.addAnimationController(this.E);
   }

   public static class a {
      boolean a_clash7(GalathEntity var1) {
         return var1.ab_clash671() != null;
      }

      @SubscribeEvent(priority = EventPriority.LOWEST)
      public void a(CheckSpawn var1) {
         Result var2 = var1.getResult();
         if (var2 != Result.DENY) {
            if (!var1.isSpawner()) {
               Entity var3 = var1.getEntity();
               if (var3 instanceof EntityWitherSkeleton || var3 instanceof EntityBlaze) {
                  BlockPos var4 = var3.getPosition();
                  World var5 = var3.world;
                  if (GalathEntity.a(var4, var5)) {
                     var1.setResult(Result.DENY);
                     BeeWorldData.a(var4, BeeWorldData.c);
                     GalathEntity var6 = new GalathEntity(var5);
                     var6.setPositionAndUpdate(var4.getX(), var4.getY(), var4.getZ());
                     var5.spawnEntity(var6);
                  }
               }
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(KeyInputEvent var1) {
         Minecraft var2 = Minecraft.getMinecraft();
         if (var2.gameSettings.keyBindJump.isKeyDown()) {
            if (GalathFlightHud.d_clash788()) {
               try {
                  for (BaseGirlEntity var4 : BaseGirlEntity.getGirlEntityList()) {
                     if (var4.world.isRemote
                        && var4 instanceof GalathEntity
                        && var2.player.getPersistentID().equals(((GalathEntity)var4).ax())) {
                        GalathFlightHud.a_clash789();
                        var4.b(fp.BOOST);
                        return;
                     }
                  }
               } catch (ConcurrentModificationException var5) {
               }
            }
         }
      }

      @SubscribeEvent
      public void a(EntityMountEvent var1) {
         if (!var1.isMounting()) {
            Entity var2 = var1.getEntityBeingMounted();
            if (var2 instanceof GalathEntity) {
               if (var2.world.isRemote) {
                  GalathFlightHud.c_clash792();
               } else {
                  ((GalathEntity)var2).t_clash672();
               }
            }
         }
      }

      @SubscribeEvent(priority = EventPriority.HIGH)
      public void a(LivingDeathEvent var1) {
         Entity var2 = var1.getEntity();
         if (var2 instanceof GalathEntity) {
            if (!var1.getSource().equals(DamageSource.OUT_OF_WORLD)) {
               GalathEntity var3 = (GalathEntity)var2;
               if (!var3.bU) {
                  if (!var2.world.isRemote) {
                     if (!var3.k_clash637()) {
                        var3.a_clash660(var3.getCombatTracker().getFighter());
                     } else {
                        GalathCoinItem.a_clash185(var3);
                        PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket(var3.getGirlId(), GirlSavedData.b_clash851(var3)), var3);
                        ThreadNames.a(900, () -> GirlSavedData.a_clash848(var3));
                        var3.bU = true;
                     }

                     var3.setHealth(1.0F);
                     var1.setCanceled(true);
                  }
               }
            }
         }
      }

      @SubscribeEvent
      public void a(PlayerRespawnEvent var1) {
         EntityPlayerMP var2 = (EntityPlayerMP)var1.player;
         BaseGirlEntity var3 = BaseGirlEntity.a(var2.getPersistentID(), Boolean.valueOf(true));
         if (var3 instanceof GalathEntity) {
            GalathEntity var4 = (GalathEntity)var3;
            var4.a_clash689(null);
            ResetGirlPacket.Handler.a_clash10(var3);
            PacketHandler.b.sendTo(new SetPlayerMovementPacket(true), var2);
            var3.b((fp)null);
            if (var4.bZ != null) {
               var4.bZ.e(var4);
               var4.bZ = null;
            }
         }
      }

      @SideOnly(Side.CLIENT)
      @SubscribeEvent
      public void a(RenderWorldLastEvent var1) {
         Minecraft var2 = Minecraft.getMinecraft();
         RenderManager var3 = var2.getRenderManager();
         float var4 = var2.getRenderPartialTicks();

         try {
            for (BaseGirlEntity var6 : BaseGirlEntity.getGirlEntityList()) {
               if (var6 instanceof GalathEntity && var6.world.isRemote && var6.getCurrentAction() == fp.SUMMON_SKELETON) {
                  double var7 = ((GalathEntity)var6).ad;
                  if (!(var7 < 9.0) && !(var7 > 30.0)) {
                     Vec3d var9 = RotationHelper.a(new Vec3d(var6.lastTickPosX, var6.lastTickPosY, var6.lastTickPosZ), var6.getPositionVector(), var4);
                     double var10 = (var7 - 9.0) / 21.0;
                     if ((Boolean)var6.getDataManager().get(GalathEntity.bN)) {
                        Vec3d var12 = var6.getCachedBoneOffset("energyBallR");
                        Vec3d var13 = var9.add(var12);
                        DragonEntity var14 = new DragonEntity(var6.world, (GalathEntity)var6);
                        var14.g = var10;
                        var14.setPositionAndUpdate(var13.x, var13.y, var13.z);
                        var3.renderEntity(var14, 0.0, 0.0, 0.0, 0.0F, var4, true);
                        var14.setPosition(0.0, -500.0, 0.0);
                        var14.setDead();
                     }

                     if ((Boolean)var6.getDataManager().get(GalathEntity.b7)) {
                        Vec3d var16 = var6.getCachedBoneOffset("energyBallL");
                        Vec3d var17 = var9.add(var16);
                        DragonEntity var18 = new DragonEntity(var6.world, (GalathEntity)var6);
                        var18.setPositionAndUpdate(var17.x, var17.y, var17.z);
                        var18.g = var10;
                        var3.renderEntity(var18, 0.0, 0.0, 0.0, 0.0F, var4, true);
                        var18.setPosition(0.0, -500.0, 0.0);
                        var18.setDead();
                     }
                  }
               }
            }
         } catch (ConcurrentModificationException var15) {
         }

         GlStateManager.enableLighting();
         GlStateManager.enableDepth();
         GlStateManager.enableAlpha();
      }

      boolean a(World var1, BlockPos var2, EnumFacing var3) {
         if (var3 == EnumFacing.NORTH) {
            var2 = var2.west();
            if (this.a_clash8(var1, var2)) {
               return false;
            } else if (this.a_clash8(var1, var2.up())) {
               return false;
            } else {
               return this.a_clash8(var1, var2.south()) ? false : !this.a_clash8(var1, var2.south().up());
            }
         } else if (var3 == EnumFacing.WEST) {
            var2 = var2.south();
            if (this.a_clash8(var1, var2)) {
               return false;
            } else if (this.a_clash8(var1, var2.up())) {
               return false;
            } else {
               return this.a_clash8(var1, var2.east()) ? false : !this.a_clash8(var1, var2.east().up());
            }
         } else if (var3 == EnumFacing.SOUTH) {
            var2 = var2.east();
            if (this.a_clash8(var1, var2)) {
               return false;
            } else if (this.a_clash8(var1, var2.up())) {
               return false;
            } else {
               return this.a_clash8(var1, var2.north()) ? false : !this.a_clash8(var1, var2.north().up());
            }
         } else if (var3 == EnumFacing.EAST) {
            var2 = var2.north();
            if (this.a_clash8(var1, var2)) {
               return false;
            } else if (this.a_clash8(var1, var2.up())) {
               return false;
            } else {
               return this.a_clash8(var1, var2.west()) ? false : !this.a_clash8(var1, var2.west().up());
            }
         } else {
            Main.LOGGER.error("Weird bed orientation, when checking for space next to bed, on galaths morning blowjob animation: " + var3.getName());
            return false;
         }
      }

      boolean a_clash8(World var1, BlockPos var2) {
         Block var3 = var1.getBlockState(var2).getBlock();

         for (Class var7 : GalathEntity.aS) {
            if (var7.isInstance(var3)) {
               return false;
            }
         }

         return true;
      }

      @SubscribeEvent
      public void a(PlayerWakeUpEvent var1) {
         EntityPlayer var2 = var1.getEntityPlayer();
         if (!var2.world.isRemote) {
            if (GirlSavedData.a_clash856(var2.getPersistentID(), var2.world)) {
               Vec3d var4 = var2.getPositionVector();
               BlockPos var5 = new BlockPos(var4);
               EnumFacing var6 = (EnumFacing)var2.world.getBlockState(var5).getValue(BlockHorizontal.FACING);
               if (!this.a(var2.world, var5, var6)) {
                  var2.sendMessage(
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
                  float var3;
                  switch ((EnumFacing)var2.world.getBlockState(var5).getValue(BlockHorizontal.FACING)) {
                     case NORTH:
                        var3 = 180.0F;
                        break;
                     case EAST:
                        var3 = -90.0F;
                        break;
                     case WEST:
                        var3 = 90.0F;
                        break;
                     default:
                        var3 = 0.0F;
                  }

                  Vec3d var7 = new Vec3d(var5.getX() + 0.5, var5.getY(), var5.getZ() + 0.5);
                  UUID var8 = GirlSavedData.b_clash853(var2);
                  if (var8 != null) {
                     GirlSavedData.a_clash848((GalathEntity)BaseGirlEntity.getServerGirlEntity(var8));
                  }

                  GalathEntity var9 = new GalathEntity(var2.world, var2, var4, true);
                  var9.setPositionAndUpdate(var4.x, var4.y, var4.z);
                  var2.world.spawnEntity(var9);
                  GirlSavedData.a(var2, var9);
                  var9.v_clash675();
                  var9.setTargetPosition(var7);
                  var9.setYawRotation(var3);
                  var9.setAnchored(true);
                  var9.setInteractionPlayerUUID(var2.getPersistentID());
                  var9.b(fp.MORNING_BLOWJOB_SLOW);
                  PacketHandler.b.sendTo(new SetPlayerMovementPacket(false), (EntityPlayerMP)var2);
                  ThreadNames.a(500, () -> {
                     var2.setPositionAndUpdate(var7.x, var7.y, var7.z);
                     PacketHandler.b.sendTo(new SetPlayerCamPacket(-10.0F, var3 + 180.0F + 5.0F, 0), (EntityPlayerMP)var2);
                  });
               }
            }
         }
      }

   }
}
