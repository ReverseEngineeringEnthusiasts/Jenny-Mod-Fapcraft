package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.api.IGalathFinish;
import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.client.gui.BeeScreen;
import com.trolmastercard.sexmod.client.gui.HornyMeterHud;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.client.renderer.ManglelieRenderer;
import com.trolmastercard.sexmod.entity.ai.AvoidPlayerGoal;
import com.trolmastercard.sexmod.util.BeeWorldData;
import com.trolmastercard.sexmod.util.GirlSavedData;
import com.trolmastercard.sexmod.util.GirlWorldData;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.DebugMode;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.GirlCombatProtection;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.GoblinFirstPersonRenderer;
import com.trolmastercard.sexmod.util.TrailSegment;
import com.trolmastercard.sexmod.util.WorldUtils;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.DynamicTrailRenderer;
import com.trolmastercard.sexmod.util.TrigMath;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent.Arrow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

/**
 * <b>Role.</b> Manglelie ("Mang") — the small imp girl bound to a Galath as
 * her "daughter". Wild Mangles wander near hives; when a Galath finds one
 * (see {@link GalathEntity#at()}) she adopts it and it rides her head
 * ({@link Action#RIDE_MOMMY_HEAD}). The corrupting mechanic: while the tamed
 * Galath is in the corrupt state, Mang picks a nearby mob, holds it in a
 * magical beam (arrow shot from the Galath at 28 ticks), and after ~60 ticks
 * the mob converts into a threesome scene (slow/fast/cum, shared animations
 * with the Galath).
 * <p>
 * <b>State.</b> Own data keys (do not reorder): {@code ad} (111) = Galath
 * partner UUID, {@code ap} (112) = corrupting flag, {@code ab} (113) = corrupt
 * target entity id (-1 none), {@code al} (114) = corrupt start world time,
 * {@code ar} (115) = scared-flag (set by {@link AvoidPlayerGoal}).
 * {@code aa} = despawned flag, {@code aq} = wild flag.
 * <p>
 * <b>Flow.</b> {@link #handleCorruptInit()} seeks an unbound Galath within 15
 * blocks and runs to her; {@link #handlePartnerLook()} binds the pair
 * (Galath gets {@code setMangleliePartnerUUID}); the corrupt cycle is
 * {@link #handleCorruptStart()} (pick a valid target mob) -&gt;
 * {@link #handleCorruptTimer()} (28 ticks, then the arrow shot) -&gt;
 * {@link #handleCorruptTick()} (60 ticks total, then release) with the
 * threesome actions handled by {@link #handleThreesomeState()} and the
 * animation overrides in {@link #handleActionAnimationOverrides(Action, String, boolean, AnimationEvent)}.
 * <p>
 * <b>Pitfalls.</b> {@link #setCurrentAction(Action)} persists the cum time on
 * {@link Action#THREESOME_CUM} (server) and guards the threesome loops.
 * {@link #updateAITasks()} despawns her when the {@code aa} flag is set or
 * her mommy disowns her. Damage is forwarded to the Galath
 * ({@link #attackEntityFrom(DamageSource, float)}). The eyes controller only
 * animates while a corrupt target exists. {@link #getYawRotation()} flips
 * 180 degrees during the threesome render.
 */
public class ManglelieEntity extends BaseGirlEntity {
   public static final String ac = "sexmod:mommy";
   public static final float am = 60.0F;
   public static final float ag = 4.0F;
   public static final float SPEED_3_5 = 3.5F;
   public static final float ah = 28.0F;
   public static final float ae = 15.0F;
   public static final float ANGLE_15 = 15.0F;
   public static final float SCALE_0_65 = 0.65F;
   public static final float ao = 3.65F;
   public static final float RANGE_6 = 6.0F;
   public static final float ak = 80.0F;
   public static final float DISTANCE_700 = 700.0F;
   public static final DataParameter<String> ad = EntityDataManager.createKey(ManglelieEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(111);
   public static final DataParameter<Boolean> ap = EntityDataManager.createKey(ManglelieEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(112);
   public static final DataParameter<Integer> ab = EntityDataManager.createKey(ManglelieEntity.class, DataSerializers.VARINT)
      .getSerializer()
      .createKey(113);
   public static final DataParameter<String> al = EntityDataManager.createKey(ManglelieEntity.class, DataSerializers.STRING)
      .getSerializer()
      .createKey(114);
   public static final DataParameter<Boolean> ar = EntityDataManager.createKey(ManglelieEntity.class, DataSerializers.BOOLEAN)
      .getSerializer()
      .createKey(115);
   private UUID galathPartnerUUID = null;
   public boolean aj = true;
   public Vec3d ZERO_VECTOR = Vec3d.ZERO;
   public float VELOCITY_0 = 0.0F;
   boolean aq = true;
   boolean despawned = false;
   boolean corrupting = false;
   public float af = 0.0F;
   public float rotationLerp = 0.0F;
   public float TICK_0 = 0.0F;
   public float ai = 0.0F;
   boolean aa = false;
   boolean modelCodeLoaded = false;
   boolean threesomeSlowStarted = false;
   boolean threesomeFastStarted = false;
   boolean threesomeCumDone = false;
   public int an = 2;

   public ManglelieEntity(World var1) {
      super(var1);
   }

   @Override
   protected void entityInit() {
      super.entityInit();
      this.entityDataManager.register(ad, "");
      this.entityDataManager.register(ap, false);
      this.entityDataManager.register(ab, -1);
      this.entityDataManager.register(al, "");
      this.entityDataManager.register(ar, false);
   }

   @Override
   public String getDisplayNameText() {
      return "Manglelie";
   }

   @Override
   protected void initEntityAI() {
      super.initEntityAI();
      this.tasks.addTask(1, new AvoidPlayerGoal(this, 20.0F, 1.0, 1.2));
   }

   @Override
   public float getScaleFactor() {
      return 0.0F;
   }

   public void setCorrupting(boolean var1) {
      this.entityDataManager.set(ap, var1);
   }

   public boolean isCorrupting() {
      return (Boolean)this.entityDataManager.get(ap);
   }

   @Nullable
   public UUID getCorruptPlayerUUID() {
      String var1 = (String)this.entityDataManager.get(ad);
      if ("".equals(var1)) {
         return null;
      }

      try {
         return UUID.fromString(var1);
      } catch (Exception var3) {
         var3.printStackTrace();
         return null;
      }
   }

   @Override
   public boolean shouldRenderNameTag() {
      return !this.isCorrupting();
   }

   @Nullable
   public GalathEntity getGalathPartner(boolean var1) {
      UUID var2 = this.getCorruptPlayerUUID();
      if (var2 == null) {
         return null;
      }

      BaseGirlEntity var3 = var1 ? BaseGirlEntity.getServerGirlEntity(var2) : BaseGirlEntity.getClientGirlEntity(var2);
      return !(var3 instanceof GalathEntity) ? null : (GalathEntity)var3;
   }

   public void setGalathPartnerUUID(UUID var1) {
      if (var1 == null) {
         this.entityDataManager.set(ad, "");
      } else {
         this.entityDataManager.set(ad, var1.toString());
      }
   }

   @Override
   public Float getYawRotation() {
      float var1 = super.getYawRotation();
      if (ManglelieNpcModel.isInThreesome(this)) {
         var1 += 180.0F;
      }

      return var1;
   }

   public void markDespawned() {
      this.despawned = true;
   }

   /**
    * BOTH sides, every AI tick: dispatches the whole Mang state machine —
    * model-code load, threesome positioning, partner binding, the corrupt
    * seek/start/tick/timer/finish cycle and gravity control; removes herself
    * when the despawn flag is set.
    */
   @Override
   public void updateAITasks() {
      if (this.aa) {
         this.world.removeEntity(this);
      } else {
         this.loadModelCode();
         this.handleThreesomeState();
         super.updateAITasks();
         this.handlePartnerLook();
         this.handleCorruptInit();
         this.handleCorruptEntity();
         this.handleCorruptTick();
         this.handleCorruptStart();
         this.updateCorruptGravity();
         this.handleCorruptTimer();
         this.handleGalathPartner();
         this.handleCorruptFinish();
      }
   }

   void handleCorruptFinish() {
      if (this.getCorruptPlayerUUID() != null) {
         this.aq = false;
      }

      if (!this.aq) {
         if (this.getGalathPartner(true) == null) {
            System.out.println("removed non-wild mang for lack of mommy");
            this.world.removeEntity(this);
         }
      }
   }

   void handleGalathPartner() {
      GalathEntity var1 = this.getGalathPartner(true);
      if (var1 != null) {
         if (var1.aF() != null) {
            if (!this.getGirlId().equals(var1.aF())) {
               System.out.println("removed non-wild mang cuz her mommy disowned her and got another mang");
               this.world.removeEntity(this);
            }
         }
      }
   }

   public static GalathEntity getGalathPartnerOf(BaseGirlEntity var0, boolean var1) {
      return !(var0 instanceof ManglelieEntity) ? null : ((ManglelieEntity)var0).getGalathPartner(var1);
   }

   public long getCorruptStartTime() {
      String var1 = (String)this.entityDataManager.get(al);
      if ("".equals(var1)) {
         return -1L;
      }

      try {
         return Long.parseLong(var1);
      } catch (Exception var2) {
         return -1L;
      }
   }

   public void setCorruptStartTime(long var1) {
      this.entityDataManager.set(al, Long.toString(var1));
      this.corrupting = false;
   }

   /**
    * SERVER: the corrupt timer — 28 ticks after the corrupt start she fires
    * an arrow from the Galath's position (3.5 above) at the corrupt target
    * and marks the shot as done.
    */
   void handleCorruptTimer() {
      long var1 = this.getCorruptStartTime();
      if (var1 != -1L) {
         long var3 = this.world.getTotalWorldTime();
         if (!((float)var3 < 28.0F + (float)var1)) {
            if (!this.corrupting) {
               Entity var5 = this.getCorruptEntity();
               if (var5 != null) {
                  GalathEntity var6 = this.getGalathPartner(true);
                  if (var6 != null) {
                     EntityTippedArrow var7 = new EntityTippedArrow(this.world, this);
                     Vec3d var8 = var6.getPositionVector().add(0.0, 3.5, 0.0);
                     var7.setPositionAndUpdate(var8.x, var8.y, var8.z);
                     Vec3d var9 = var5.getPositionVector();
                     Vec3d var10 = var9.subtract(var8).normalize();
                     var7.motionX = var10.x * 4.0;
                     var7.motionY = var10.y * 4.0;
                     var7.motionZ = var10.z * 4.0;
                     BaseGirlEntity.girlPlaySound(var6, SoundEvents.ENTITY_ARROW_SHOOT, true);
                     this.world.spawnEntity(var7);
                     this.corrupting = true;
                  }
               }
            }
         }
      }
   }

   public void addPotionEffect(PotionEffect var1) {
   }

   void updateCorruptGravity() {
      boolean var1 = this.getCorruptPlayerUUID() != null;
      this.setNoGravity(var1);
      this.noClip = var1;
   }

   public boolean canBeCollidedWith() {
      return this.getCorruptPlayerUUID() == null;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public Vec3d renderCustomModelTransform(Minecraft var1, SexSceneEntity var2, EntityLivingBase var3, float var4) {
      if (this.isLocallyRegistered()) {
         return super.renderCustomModelTransform(var1, var2, var3, var4);
      }

      if (!this.isCorrupting()) {
         return super.renderCustomModelTransform(var1, var2, var3, var4);
      }

      GalathEntity var5 = this.getGalathPartner(false);
      if (var5 == null) {
         return super.renderCustomModelTransform(var1, var2, var3, var4);
      }

      ManglelieRenderer.renderGalathInteract(var5, var4, var2);
      return ManglelieRenderer.getLookVector(var5, var4);
   }

   public float getCorruptProgress(float var1) {
      long var2 = this.getCorruptStartTime();
      if (var2 == -1L) {
         return 0.0F;
      }

      long var4 = this.world.getTotalWorldTime();
      float var6 = (float)(var4 - var2);
      return (var6 + var1) / 28.0F;
   }

   @Nullable
   public Entity getCorruptEntity() {
      int var1 = (Integer)this.entityDataManager.get(ab);
      return var1 == -1 ? null : this.world.getEntityByID(var1);
   }

   void setCorruptEntity(int var1) {
      this.entityDataManager.set(ab, var1);
      this.setCorruptStartTime(var1 == -1 ? -1L : this.world.getTotalWorldTime());
   }

   void handleCorruptEntity() {
      Entity var1 = this.getCorruptEntity();
      if (var1 != null) {
         GalathEntity var2 = this.getGalathPartner(true);
         if (var2 == null) {
            this.setCorruptEntity(-1);
         } else if (!this.isCorrupting()) {
            this.setCorruptEntity(-1);
         } else {
            if (isGalathBlocked(var1, var2)) {
               this.setCorruptEntity(-1);
            }
         }
      }
   }

   /**
    * SERVER: valid-corrupt-target test — the target must be alive, in the
    * Galath's dimension, daylight-visible, within 15 blocks horizontally and
    * in front of the Galath's aim.
    */
   public static boolean isGalathBlocked(Entity var0, GalathEntity var1) {
      if (var0.isDead) {
         return true;
      }

      if (var0.dimension != var1.dimension) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.isValidTarget(var0)) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.isDaylight(var1.world, var1.getTargetPosition().add(0.0, var1.getEyeHeight(), 0.0), var0)) {
         return true;
      }

      Vec3d var2 = var0.getPositionVector().subtract(var1.getPositionVector());
      if (var2.x * var2.x + var2.z * var2.z > 225.0) {
         return true;
      }

      Float var3 = GalathEntity.getAimYaw(var1, 0.0F);
      float var4 = var3 == null ? var1.rotationYawHead : var3;
      Vec3d var5 = VectorMath.rotateByYaw(var2, var4);
      return var5.z < 0.0;
   }

   /**
    * SERVER: the corrupt target selection — while corrupting with no bound
    * entity, scans for a valid mob (per {@link #isGalathBlocked(Entity, GalathEntity)})
    * within 15 blocks of the Galath and binds its entity id.
    */
   void handleCorruptStart() {
      if (this.getCorruptEntity() == null) {
         if (this.isCorrupting()) {
            GalathEntity var1 = this.getGalathPartner(true);
            if (var1 != null) {
               if (var1.getInteractionPlayerUUID() == null) {
                  if (var1.getCurrentAction() != Action.MASTERBATE) {
                     BlockPos var2 = var1.getPosition();
                     BlockPos var3 = new BlockPos(15.0, 15.0, 15.0);

                     for (EntityMob var6 : this.world
                        .getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(var2.add(var3), var2.subtract(var3)))) {
                        if (!isGalathBlocked(var6, var1)) {
                           this.setCorruptEntity(var6.getEntityId());
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   /**
    * SERVER: releases the corrupt target 60 ticks after the start and
    * unbinds it.
    */
   void handleCorruptTick() {
      Entity var1 = this.getCorruptEntity();
      if (var1 != null) {
         GalathEntity var2 = this.getGalathPartner(true);
         if (var2 != null) {
            long var3 = this.getCorruptStartTime();
            if (var3 != -1L) {
               long var5 = this.world.getTotalWorldTime();
               long var7 = var5 - this.getCorruptStartTime();
               if (!((float)var7 < 60.0F)) {
                  this.corrupting = false;
                  this.setCorruptEntity(-1);
               }
            }
         }
      }
   }

   /**
    * SERVER: completes the adoption — binds the Galath partner UUID both
    * ways, marks her corrupting and rides the mommy's head; un-anchors the
    * Galath if she was mid-hug.
    */
   void handlePartnerLook() {
      if (this.galathPartnerUUID != null) {
         BaseGirlEntity var1 = BaseGirlEntity.getServerGirlEntity(this.galathPartnerUUID);
         if (var1 instanceof GalathEntity) {
            GalathEntity var2 = (GalathEntity)var1;
            this.setGalathPartnerUUID(this.galathPartnerUUID);
            var2.setMangleliePartnerUUID(this.getGirlId());
            this.setCorrupting(true);
            this.setCurrentAction(Action.RIDE_MOMMY_HEAD);
            this.galathPartnerUUID = null;
            if (var2.getCurrentAction() == Action.HUG_MANG) {
               var2.setAnchored(false);
               var2.setCurrentAction((Action)null);
            }
         }
      }
   }

   @Override
   public void setCurrentAction(Action action) {
      if (this.getCurrentAction() != Action.THREESOME_CUM || !Action.isAny(action, Action.THREESOME_FAST, Action.THREESOME_SLOW)) {
         if (!this.world.isRemote && action == Action.THREESOME_CUM) {
            GirlSavedData.saveCumTime(this.getInteractionPlayerUUID(), this.world.getTotalWorldTime());
         }

         super.setCurrentAction(action);
      }
   }

   /**
    * SERVER: the threesome position lock — while corrupting with no threesome
    * action running, keeps her anchored on the Galath's head.
    */
   void handleThreesomeState() {
      if (this.isCorrupting() && !Action.isAnyAction(this, Action.THREESOME_SLOW, Action.THREESOME_CUM, Action.THREESOME_FAST)) {
         GalathEntity var1 = this.getGalathPartner(true);
         if (var1 != null) {
            if (!var1.isDead && this.getGirlId().equals(var1.aF())) {
               this.setYawRotation(0.0F);
               this.setTargetPosition(var1.getPositionVector());
               this.setAnchored(true);
            } else {
               Main.LOGGER.warn("A dead mommy has been saved onto a mang. Deleting her and creating a new one");
               this.world.removeEntity(this);
            }
         }
      }
   }

   @Override
   public void setYawRotation(float var1) {
      super.setYawRotation(var1);
   }

   @Override
   public Vec3d transformRenderOffset(Vec3d var1, float var2) {
      if (!this.isCorrupting()) {
         return var1;
      }

      if (ManglelieNpcModel.isInThreesome(this)) {
         return var1;
      }

      GalathEntity var3 = this.getGalathPartner(false);
      return var3 == null ? var1 : ManglelieRenderer.getLookVector(var3, var2);
   }

   /**
    * SERVER: seeks an unbound Galath within 15 blocks and runs to her (RUN +
    * pathing); stands still when none is found.
    */
   void handleCorruptInit() {
      if (!this.isCorrupting()) {
         if (this.getCorruptPlayerUUID() == null) {
            BlockPos var1 = this.getPosition();
            BlockPos var2 = var1.add(-15.0, -15.0, -15.0);
            BlockPos var3 = var1.add(15.0, 15.0, 15.0);
            AxisAlignedBB var4 = new AxisAlignedBB(var2, var3);
            List var5 = this.world.getEntitiesWithinAABB(GalathEntity.class, var4);
            GalathEntity var6 = null;

            for (GalathEntity var8 : (java.util.Collection<GalathEntity>) (var5) ) {
               if (!var8.isDead && var8.getMangleliePartner(true) == null && var8.onGround) {
                  var6 = var8;
                  break;
               }
            }

            if (var6 == null) {
               if (this.getCurrentAction() == Action.RUN) {
                  this.setCurrentAction((Action)null);
                  this.getNavigator().clearPath();
               }
            } else if (this.getCurrentAction() != Action.RIDE_MOMMY_HEAD) {
               this.setCurrentAction(Action.RUN);
               Vec3d var11 = this.getPositionVector();
               Vec3d var12 = var6.getPositionVector();
               Vec3d var9 = var12.subtract(var11);
               float var10 = (float)TrigMath.sinDegrees(Math.atan2(var9.z, var9.x)) - 90.0F;
               this.setYawRotation(var10);
               this.pathNavigator = this.getNavigator();
               this.pathNavigator.clearPath();
               this.pathNavigator.tryMoveToEntityLiving(var6, 0.65F);
            }
         }
      }
   }

   public boolean isLookingAtGalathEntity(Entity var1, float var2) {
      GalathEntity var3 = this.getGalathPartner(var2 == 1.0F);
      if (var3 == null) {
         return false;
      }

      Vec3d var4 = com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(this, var2);
      return this.isThrowBlocked(com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(var1, var2).subtract(var4), var3, var2);
   }

   public boolean isLookingAtGalathPoint(Vec3d var1, float var2) {
      GalathEntity var3 = this.getGalathPartner(var2 == 1.0F);
      if (var3 == null) {
         return false;
      }

      Vec3d var4 = com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(this, var2);
      return this.isThrowBlocked(var1.subtract(var4), var3, var2);
   }

   boolean isThrowBlocked(Vec3d var1, GalathEntity var2, float var3) {
      Vec3d var4 = VectorMath.rotateByYaw(var1, RotationHelper.lerpFloat(var2.prevRotationYawHead, var2.rotationYawHead, var3));
      return var4.x > 0.35;
   }

   @Override
   public void onUpdate() {
      super.onUpdate();
      if (this.world.isRemote) {
         this.handleParticleTick();
      }
   }

   @SideOnly(Side.CLIENT)
   void handleParticleTick() {
      if (Minecraft.getMinecraft().player.ticksExisted % 7.0F == 0.0F) {
         if (ManglelieRenderer.isCorrupting(this)) {
            GalathEntity var1 = this.getGalathPartner(false);
            if (var1 != null) {
               Entity var2 = this.getCorruptTarget();
               if (var2 == null) {
                  this.af = 0.0F;
                  this.rotationLerp = 0.0F;
               } else {
                  Vec3d var3 = var2.getPositionVector().add(0.0, var2.getEyeHeight(), 0.0);
                  Vec3d var4 = var1.getPositionVector().add(var1.getCachedBoneOffset("mangPos")).add(this.getCachedBoneOffset("head"));
                  Vec3d var5 = var4.subtract(var3);
                  float var6 = (float)(TrigMath.sinDegrees(Math.atan2(var5.z, var5.x)) + 90.0);
                  Float var7 = GalathEntity.getAimYaw(var1, 0.0F);
                  var6 -= var1.rotationYawHead;
                  if (var7 != null) {
                     var6 -= var7;
                  }

                  this.af = Math.abs(WorldUtils.normalizeAngleDiff(0.0F, var6)) < 80.0F ? -TrigMath.wrapDegrees(var6) : 0.0F;
                  this.rotationLerp = this.af == 0.0F ? 0.0F : (float)ThreadNames.clampDouble(-var5.y / 2.0, -0.75, 0.75);
               }
            }
         }
      }
   }

   /**
    * SERVER: damage is forwarded to her Galath mommy (unless void damage);
    * Mang herself never takes hits.
    */
   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (var1 == DamageSource.OUT_OF_WORLD) {
         return super.attackEntityFrom(var1, var2);
      }

      GalathEntity var3 = this.getGalathPartner(true);
      if (var3 == null) {
         return super.attackEntityFrom(var1, var2);
      }

      var3.attackEntityFrom(var1, var2);
      return false;
   }

   @Nullable
   Entity getCorruptTarget() {
      Object var1 = this.getCorruptEntity();
      if (var1 != null) {
         return (Entity)var1;
      }

      for (EntityPlayer var3 : this.world.playerEntities) {
         float var4 = var3.getDistance(this);
         if (!(var4 > 6.0F) && (var1 == null || ((Entity)var1).getDistance(this) > var4)) {
            var1 = var3;
         }
      }

      return (Entity)var1;
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      UUID var2 = this.getCorruptPlayerUUID();
      var1.setString("sexmod:mommy", var2 == null ? "" : var2.toString());
      var1.setBoolean("sexmod:iswild", this.aq);
      if (this.despawned) {
         var1.setBoolean("sexmod:despawned", true);
      }
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      String var2 = var1.getString("sexmod:mommy");
      if (!"".equals(var2)) {
         this.galathPartnerUUID = UUID.fromString(var2);
      }

      if (var1.getBoolean("sexmod:despawned")) {
         this.aa = true;
      }

      this.aq = var1.getBoolean("sexmod:iswild");
   }

   @Override
   protected boolean supportsCustomModels() {
      return false;
   }

   @Override
   public void setCustomModelCode(String var1) {
      super.setCustomModelCode(var1);
      GirlWorldData.setCustomModelCode(this);
   }

   void loadModelCode() {
      if (!this.modelCodeLoaded) {
         this.setCustomModelCode(GirlWorldData.getCustomModelCode(this));
         this.modelCodeLoaded = true;
      }
   }

   @Nullable
   @Override
   protected Action getNextAction(Action var1) {
      return null;
   }

   @Override
   protected Action getCumAction(Action var1) {
      if (Action.isAny(var1, Action.THREESOME_FAST, Action.THREESOME_SLOW)) {
         this.threesomeSlowStarted = true;
      }

      return null;
   }

   @Override
   public void reinitTasks() {
      if (this.isCorrupting()) {
         this.setCurrentAction(Action.RIDE_MOMMY_HEAD);
         this.setYawRotation(0.0F);
         this.entityDataManager.setDirty(YAW_ROTATION);
      }
   }

   public boolean getCanSpawnHere() {
      if (!super.getCanSpawnHere()) {
         return false;
      }

      BlockPos var1 = this.getPosition();
      ArrayList var2 = new ArrayList();
      var2.addAll(BeeWorldData.hivePositions);
      var2.addAll(BeeWorldData.flowerPositions);

      for (BlockPos var4 : (java.util.Collection<BlockPos>) (var2) ) {
         if (Math.sqrt(var1.distanceSq(var4)) < 700.0) {
            return false;
         }
      }

      BeeWorldData.addHivePosition(var1, BeeWorldData.flowerPositions);
      return true;
   }

   /**
    * CLIENT: the threesome action overrides — THREESOME_CUM ends the scene
    * (reset both girls, clear cum trails), the slow/fast transitions swap
    * between the soft/hard/back shared animations and drive the Galath's
    * masterbate/pussy-licking mirror actions.
    */
   @Override
   protected boolean handleActionAnimationOverrides(Action var1, String var2, boolean var3, AnimationEvent var4) {
      if (var1 == Action.THREESOME_CUM) {
         this.threesomeSlowStarted = false;
         this.threesomeFastStarted = false;
         this.threesomeCumDone = false;
         this.an = 2;
         this.resetCameraAndPhysics();
         GalathEntity var8 = this.getGalathPartner(false);
         if (var8 != null) {
            var8.resetCameraAndPhysics();
            CummyEntity.spawnCummyTrails(var8);
         }

         CummyEntity.spawnCummyTrails(this);
         return true;
      } else if (this.threesomeSlowStarted && var1 == Action.THREESOME_FAST) {
         this.setCurrentAction(Action.THREESOME_CUM);
         this.createAnimation("animation.shared.double_holding_cum", true, var4, true);
         GalathEntity var7 = this.getGalathPartner(false);
         if (var7 != null) {
            var7.setCurrentAction(Action.MASTERBATE_SITTING_CUM);
         }

         return true;
      } else if ((this.threesomeSlowStarted || var3) && var1 == Action.THREESOME_SLOW) {
         this.threesomeFastStarted = false;
         this.setCurrentAction(Action.THREESOME_FAST);
         this.createAnimation("animation.shared.double_holding_soft", true, var4, true);
         GalathEntity var6 = this.getGalathPartner(false);
         if (var6 != null) {
            var6.ak();
         }

         return true;
      } else {
         if (this.threesomeSlowStarted) {
            return false;
         }

         if (var3 && !this.threesomeFastStarted && var1 == Action.THREESOME_FAST) {
            this.threesomeFastStarted = true;
            this.createAnimation("animation.shared.double_holding_hard", true, var4, true);
            return true;
         }

         if (!var3 && var1 == Action.THREESOME_FAST) {
            this.threesomeCumDone = true;
            this.setCurrentAction(Action.THREESOME_SLOW);
            this.createAnimation("animation.shared.double_holding_back", true, var4, true);
            GalathEntity var5 = this.getGalathPartner(false);
            if (var5 != null) {
               var5.startPussyLicking();
            }

            return true;
         } else if (this.threesomeCumDone && var1 == Action.THREESOME_SLOW) {
            this.threesomeCumDone = false;
            this.createAnimation("animation.shared.double_holding_slow", true, var4, true);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> var1) {
      AnimationController var2 = var1.getController();
      if (this.eyesController == var2) {
         if (this.getCorruptEntity() == null) {
            return PlayState.STOP;
         }

         this.createAnimation("animation.manglelie.angry_face", true, var1);
         return PlayState.CONTINUE;
      } else if (this.movementController == var2) {
         if (this.getCurrentAction() == Action.NULL && !this.isCorrupting()) {
            if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               if ((Boolean)this.entityDataManager.get(ar)) {
                  this.createAnimation("animation.manglelie.scared_run", true, var1);
               } else {
                  this.createAnimation("animation.manglelie.walk", true, var1);
               }

               this.rotationYaw = this.rotationYawHead;
               return PlayState.CONTINUE;
            } else {
               this.createAnimation("animation.manglelie.idle", true, var1);
               return PlayState.CONTINUE;
            }
         } else {
            return PlayState.STOP;
         }
      } else {
         switch (this.getCurrentAction()) {
            case RUN:
               this.createAnimation("animation.manglelie.running", true, var1);
               break;
            case RIDE_MOMMY_HEAD:
               this.createAnimation("animation.manglelie.sit_on_galath", true, var1);
               break;
            case THREESOME_SLOW:
               if (this.threesomeCumDone) {
                  this.createAnimation("animation.shared.double_holding_back", true, var1);
               } else {
                  this.playRandomizedAnimation("animation.shared.double_holding_slow", 4, 0.33F, var1);
               }
               break;
            case THREESOME_FAST:
               if (this.threesomeFastStarted) {
                  this.playRandomizedAnimation("animation.shared.double_holding_hard", 3, 0.33F, var1);
               } else {
                  this.createAnimation("animation.shared.double_holding_soft", true, var1);
               }
               break;
            case THREESOME_CUM:
               this.createAnimation("animation.shared.double_holding_cum", true, var1);
               break;
            default:
               return PlayState.STOP;
         }

         return PlayState.CONTINUE;
      }
   }

   /**
    * CLIENT: registers the controllers plus the sound listener driving the
    * threesome (pound sounds, cum trails from the {@code semenEmitter} bone,
    * black screen) — {@code cs0/1/2} select the pose variant ({@code an}),
    * {@code sexui} shows the horny meter.
    */
   @Override
   public void registerControllers(AnimationData var1) {
      var1.addAnimationController(this.movementController);
      var1.addAnimationController(this.eyesController);
      this.actionController.registerSoundListener(var1x -> {
         switch (var1x.sound) {
            case "pound":
               this.playRandomSound(SoundHandler.MISC_POUNDING);
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.addToHornyMeter(0.02);
               }
               break;
            case "cs0":
               this.an = 0;
               break;
            case "cs1":
               this.an = 1;
               break;
            case "cs2":
               this.an = 2;
               break;
            case "sexui":
               if (this.isControlledByLocalPlayer()) {
                  HornyMeterHud.showHornyMeter();
               }
               break;
            case "doubleSemen0":
               this.playRandomSoundAtVolume(SoundHandler.MISC_INSERTS, 6.0F);
               this.playRandomSound(SoundHandler.MISC_POUNDING);
            case "doubleSemen":
               CummyEntity.registerTrail(new DynamicTrailRenderer(10, var0 -> {
                  Vec3d var1xx = var0.getBoneWorldPosition("semenEmitter");
                  Vec3d var2 = var0.getBoneWorldPosition("semenDir");
                  return var1xx.subtract(var2).normalize();
               }, var0 -> var0.getCachedBoneOffset("semenEmitter").add(var0.getTargetPosition()), this, 0.3F, 0.3F));
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
         }
      });
      var1.addAnimationController(this.actionController);
   }

   /**
    * Forge handler: Mang's arrows must never damage other girls — the
    * projectile impact on a {@link BaseGirlEntity} is cancelled.
    */
   public static class b {
      @SubscribeEvent
      public void handleArrowHit(Arrow var1) {
         RayTraceResult var2 = var1.getRayTraceResult();
         EntityArrow var3 = var1.getArrow();
         if (var3.shootingEntity instanceof ManglelieEntity) {
            if (var2.entityHit instanceof BaseGirlEntity) {
               var1.setCanceled(true);
            }
         }
      }

   }
}
