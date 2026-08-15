package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.Main;
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

   public ManglelieEntity(World world) {
      super(world);
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

   public void setCorrupting(boolean corrupting) {
      this.entityDataManager.set(ap, corrupting);
   }

   public boolean isCorrupting() {
      return (Boolean)this.entityDataManager.get(ap);
   }

   @Nullable
   public UUID getCorruptPlayerUUID() {
      String corruptPlayerStr = (String)this.entityDataManager.get(ad);
      if ("".equals(corruptPlayerStr)) {
         return null;
      }

      try {
         return UUID.fromString(corruptPlayerStr);
      } catch (Exception ex) {
         ex.printStackTrace();
         return null;
      }
   }

   @Override
   public boolean shouldRenderNameTag() {
      return !this.isCorrupting();
   }

   @Nullable
   public GalathEntity getGalathPartner(boolean server) {
      UUID uuid = this.getCorruptPlayerUUID();
      if (uuid == null) {
         return null;
      }

      BaseGirlEntity girl = server ? BaseGirlEntity.getServerGirlEntity(uuid) : BaseGirlEntity.getClientGirlEntity(uuid);
      return !(girl instanceof GalathEntity) ? null : (GalathEntity)girl;
   }

   public void setGalathPartnerUUID(UUID uuid) {
      if (uuid == null) {
         this.entityDataManager.set(ad, "");
      } else {
         this.entityDataManager.set(ad, uuid.toString());
      }
   }

   @Override
   public Float getYawRotation() {
      float yaw = super.getYawRotation();
      if (ManglelieNpcModel.isInThreesome(this)) {
         yaw += 180.0F;
      }

      return yaw;
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
      GalathEntity galath = this.getGalathPartner(true);
      if (galath != null) {
         if (galath.aF() != null) {
            if (!this.getGirlId().equals(galath.aF())) {
               System.out.println("removed non-wild mang cuz her mommy disowned her and got another mang");
               this.world.removeEntity(this);
            }
         }
      }
   }

   public static GalathEntity getGalathPartnerOf(BaseGirlEntity girl, boolean server) {
      return !(girl instanceof ManglelieEntity) ? null : ((ManglelieEntity)girl).getGalathPartner(server);
   }

   public long getCorruptStartTime() {
      String timeStr = (String)this.entityDataManager.get(al);
      if ("".equals(timeStr)) {
         return -1L;
      }

      try {
         return Long.parseLong(timeStr);
      } catch (Exception ex) {
         return -1L;
      }
   }

   public void setCorruptStartTime(long time) {
      this.entityDataManager.set(al, Long.toString(time));
      this.corrupting = false;
   }

   /**
    * SERVER: the corrupt timer — 28 ticks after the corrupt start she fires
    * an arrow from the Galath's position (3.5 above) at the corrupt target
    * and marks the shot as done.
    */
   void handleCorruptTimer() {
      long corruptStart = this.getCorruptStartTime();
      if (corruptStart != -1L) {
         long worldTime = this.world.getTotalWorldTime();
         if (!((float)worldTime < 28.0F + (float)corruptStart)) {
            if (!this.corrupting) {
               Entity corruptEntity = this.getCorruptEntity();
               if (corruptEntity != null) {
                  GalathEntity galath = this.getGalathPartner(true);
                  if (galath != null) {
                     EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
                     Vec3d startPos = galath.getPositionVector().add(0.0, 3.5, 0.0);
                     arrow.setPositionAndUpdate(startPos.x, startPos.y, startPos.z);
                     Vec3d targetPos = corruptEntity.getPositionVector();
                     Vec3d dir = targetPos.subtract(startPos).normalize();
                     arrow.motionX = dir.x * 4.0;
                     arrow.motionY = dir.y * 4.0;
                     arrow.motionZ = dir.z * 4.0;
                     BaseGirlEntity.girlPlaySound(galath, SoundEvents.ENTITY_ARROW_SHOOT, true);
                     this.world.spawnEntity(arrow);
                     this.corrupting = true;
                  }
               }
            }
         }
      }
   }

   public void addPotionEffect(PotionEffect effect) {
   }

   void updateCorruptGravity() {
      boolean corrupting = this.getCorruptPlayerUUID() != null;
      this.setNoGravity(corrupting);
      this.noClip = corrupting;
   }

   public boolean canBeCollidedWith() {
      return this.getCorruptPlayerUUID() == null;
   }

   @SideOnly(Side.CLIENT)
   @Override
   public Vec3d renderCustomModelTransform(Minecraft mc, SexSceneEntity scene, EntityLivingBase entity, float partialTicks) {
      if (this.isLocallyRegistered()) {
         return super.renderCustomModelTransform(mc, scene, entity, partialTicks);
      }

      if (!this.isCorrupting()) {
         return super.renderCustomModelTransform(mc, scene, entity, partialTicks);
      }

      GalathEntity galath = this.getGalathPartner(false);
      if (galath == null) {
         return super.renderCustomModelTransform(mc, scene, entity, partialTicks);
      }

      ManglelieRenderer.renderGalathInteract(galath, partialTicks, scene);
      return ManglelieRenderer.getLookVector(galath, partialTicks);
   }

   public float getCorruptProgress(float partialTicks) {
      long corruptStart = this.getCorruptStartTime();
      if (corruptStart == -1L) {
         return 0.0F;
      }

      long worldTime = this.world.getTotalWorldTime();
      float elapsed = (float)(worldTime - corruptStart);
      return (elapsed + partialTicks) / 28.0F;
   }

   @Nullable
   public Entity getCorruptEntity() {
      int corruptId = (Integer)this.entityDataManager.get(ab);
      return corruptId == -1 ? null : this.world.getEntityByID(corruptId);
   }

   void setCorruptEntity(int entityId) {
      this.entityDataManager.set(ab, entityId);
      this.setCorruptStartTime(entityId == -1 ? -1L : this.world.getTotalWorldTime());
   }

   void handleCorruptEntity() {
      Entity corruptEntity = this.getCorruptEntity();
      if (corruptEntity != null) {
         GalathEntity galath = this.getGalathPartner(true);
         if (galath == null) {
            this.setCorruptEntity(-1);
         } else if (!this.isCorrupting()) {
            this.setCorruptEntity(-1);
         } else {
            if (isGalathBlocked(corruptEntity, galath)) {
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
   public static boolean isGalathBlocked(Entity entity, GalathEntity galath) {
      if (entity.isDead) {
         return true;
      }

      if (entity.dimension != galath.dimension) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.isValidTarget(entity)) {
         return true;
      }

      if (!com.trolmastercard.sexmod.MobPredicates.isDaylight(galath.world, galath.getTargetPosition().add(0.0, galath.getEyeHeight(), 0.0), entity)) {
         return true;
      }

      Vec3d offset = entity.getPositionVector().subtract(galath.getPositionVector());
      if (offset.x * offset.x + offset.z * offset.z > 225.0) {
         return true;
      }

      Float aimYaw = GalathEntity.getAimYaw(galath, 0.0F);
      float yaw = aimYaw == null ? galath.rotationYawHead : aimYaw;
      Vec3d rotated = VectorMath.rotateByYaw(offset, yaw);
      return rotated.z < 0.0;
   }

   /**
    * SERVER: the corrupt target selection — while corrupting with no bound
    * entity, scans for a valid mob (per {@link #isGalathBlocked(Entity, GalathEntity)})
    * within 15 blocks of the Galath and binds its entity id.
    */
   void handleCorruptStart() {
      if (this.getCorruptEntity() == null) {
         if (this.isCorrupting()) {
            GalathEntity galath = this.getGalathPartner(true);
            if (galath != null) {
               if (galath.getInteractionPlayerUUID() == null) {
                  if (galath.getCurrentAction() != Action.MASTERBATE) {
                     BlockPos center = galath.getPosition();
                     BlockPos range = new BlockPos(15.0, 15.0, 15.0);

                     for (EntityMob mob : this.world
                        .getEntitiesWithinAABB(EntityMob.class, new AxisAlignedBB(center.add(range), center.subtract(range)))) {
                        if (!isGalathBlocked(mob, galath)) {
                           this.setCorruptEntity(mob.getEntityId());
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
      Entity corruptEntity = this.getCorruptEntity();
      if (corruptEntity != null) {
         GalathEntity galath = this.getGalathPartner(true);
         if (galath != null) {
            long corruptStart = this.getCorruptStartTime();
            if (corruptStart != -1L) {
               long worldTime = this.world.getTotalWorldTime();
               long elapsed = worldTime - this.getCorruptStartTime();
               if (!((float)elapsed < 60.0F)) {
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
         BaseGirlEntity partner = BaseGirlEntity.getServerGirlEntity(this.galathPartnerUUID);
         if (partner instanceof GalathEntity) {
            GalathEntity galath = (GalathEntity)partner;
            this.setGalathPartnerUUID(this.galathPartnerUUID);
            galath.setMangleliePartnerUUID(this.getGirlId());
            this.setCorrupting(true);
            this.setCurrentAction(Action.RIDE_MOMMY_HEAD);
            this.galathPartnerUUID = null;
            if (galath.getCurrentAction() == Action.HUG_MANG) {
               galath.setAnchored(false);
               galath.setCurrentAction((Action)null);
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
         GalathEntity galath = this.getGalathPartner(true);
         if (galath != null) {
            if (!galath.isDead && this.getGirlId().equals(galath.aF())) {
               this.setYawRotation(0.0F);
               this.setTargetPosition(galath.getPositionVector());
               this.setAnchored(true);
            } else {
               Main.LOGGER.warn("A dead mommy has been saved onto a mang. Deleting her and creating a new one");
               this.world.removeEntity(this);
            }
         }
      }
   }

   @Override
   public void setYawRotation(float yaw) {
      super.setYawRotation(yaw);
   }

   @Override
   public Vec3d transformRenderOffset(Vec3d vec, float partialTicks) {
      if (!this.isCorrupting()) {
         return vec;
      }

      if (ManglelieNpcModel.isInThreesome(this)) {
         return vec;
      }

      GalathEntity galath = this.getGalathPartner(false);
      return galath == null ? vec : ManglelieRenderer.getLookVector(galath, partialTicks);
   }

   /**
    * SERVER: seeks an unbound Galath within 15 blocks and runs to her (RUN +
    * pathing); stands still when none is found.
    */
   void handleCorruptInit() {
      if (!this.isCorrupting()) {
         if (this.getCorruptPlayerUUID() == null) {
            BlockPos center = this.getPosition();
            BlockPos min = center.add(-15.0, -15.0, -15.0);
            BlockPos max = center.add(15.0, 15.0, 15.0);
            AxisAlignedBB aabb = new AxisAlignedBB(min, max);
            List galaths = this.world.getEntitiesWithinAABB(GalathEntity.class, aabb);
            GalathEntity chosen = null;

            for (GalathEntity galath : (java.util.Collection<GalathEntity>) (galaths) ) {
               if (!galath.isDead && galath.getMangleliePartner(true) == null && galath.onGround) {
                  chosen = galath;
                  break;
               }
            }

            if (chosen == null) {
               if (this.getCurrentAction() == Action.RUN) {
                  this.setCurrentAction((Action)null);
                  this.getNavigator().clearPath();
               }
            } else if (this.getCurrentAction() != Action.RIDE_MOMMY_HEAD) {
               this.setCurrentAction(Action.RUN);
               Vec3d selfPos = this.getPositionVector();
               Vec3d partnerPos = chosen.getPositionVector();
               Vec3d delta = partnerPos.subtract(selfPos);
               float yaw = (float)TrigMath.sinDegrees(Math.atan2(delta.z, delta.x)) - 90.0F;
               this.setYawRotation(yaw);
               this.pathNavigator = this.getNavigator();
               this.pathNavigator.clearPath();
               this.pathNavigator.tryMoveToEntityLiving(chosen, 0.65F);
            }
         }
      }
   }

   public boolean isLookingAtGalathEntity(Entity entity, float partialTicks) {
      GalathEntity galath = this.getGalathPartner(partialTicks == 1.0F);
      if (galath == null) {
         return false;
      }

      Vec3d lookVec = com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(this, partialTicks);
      return this.isThrowBlocked(com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(entity, partialTicks).subtract(lookVec), galath, partialTicks);
   }

   public boolean isLookingAtGalathPoint(Vec3d point, float partialTicks) {
      GalathEntity galath = this.getGalathPartner(partialTicks == 1.0F);
      if (galath == null) {
         return false;
      }

      Vec3d lookVec = com.trolmastercard.sexmod.util.EntityLookVectorHelper.getEntityLookVector(this, partialTicks);
      return this.isThrowBlocked(point.subtract(lookVec), galath, partialTicks);
   }

   boolean isThrowBlocked(Vec3d delta, GalathEntity galath, float partialTicks) {
      Vec3d rotated = VectorMath.rotateByYaw(delta, RotationHelper.lerpFloat(galath.prevRotationYawHead, galath.rotationYawHead, partialTicks));
      return rotated.x > 0.35;
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
            GalathEntity galath = this.getGalathPartner(false);
            if (galath != null) {
               Entity corruptTarget = this.getCorruptTarget();
               if (corruptTarget == null) {
                  this.af = 0.0F;
                  this.rotationLerp = 0.0F;
               } else {
                  Vec3d targetPos = corruptTarget.getPositionVector().add(0.0, corruptTarget.getEyeHeight(), 0.0);
                  Vec3d mouthPos = galath.getPositionVector().add(galath.getCachedBoneOffset("mangPos")).add(this.getCachedBoneOffset("head"));
                  Vec3d delta = mouthPos.subtract(targetPos);
                  float yaw = (float)(TrigMath.sinDegrees(Math.atan2(delta.z, delta.x)) + 90.0);
                  Float aimYaw = GalathEntity.getAimYaw(galath, 0.0F);
                  yaw -= galath.rotationYawHead;
                  if (aimYaw != null) {
                     yaw -= aimYaw;
                  }

                  this.af = Math.abs(WorldUtils.normalizeAngleDiff(0.0F, yaw)) < 80.0F ? -TrigMath.wrapDegrees(yaw) : 0.0F;
                  this.rotationLerp = this.af == 0.0F ? 0.0F : (float)ThreadNames.clampDouble(-delta.y / 2.0, -0.75, 0.75);
               }
            }
         }
      }
   }

   /**
    * SERVER: damage is forwarded to her Galath mommy (unless void damage);
    * Mang herself never takes hits.
    */
   public boolean attackEntityFrom(DamageSource source, float amount) {
      if (source == DamageSource.OUT_OF_WORLD) {
         return super.attackEntityFrom(source, amount);
      }

      GalathEntity galath = this.getGalathPartner(true);
      if (galath == null) {
         return super.attackEntityFrom(source, amount);
      }

      galath.attackEntityFrom(source, amount);
      return false;
   }

   @Nullable
   Entity getCorruptTarget() {
      Object corruptEntity = this.getCorruptEntity();
      if (corruptEntity != null) {
         return (Entity)corruptEntity;
      }

      for (EntityPlayer player : this.world.playerEntities) {
         float dist = player.getDistance(this);
         if (!(dist > 6.0F) && (corruptEntity == null || ((Entity)corruptEntity).getDistance(this) > dist)) {
            corruptEntity = player;
         }
      }

      return (Entity)corruptEntity;
   }

   @Override
   public void writeEntityToNBT(NBTTagCompound nbt) {
      super.writeEntityToNBT(nbt);
      UUID uuid = this.getCorruptPlayerUUID();
      nbt.setString("sexmod:mommy", uuid == null ? "" : uuid.toString());
      nbt.setBoolean("sexmod:iswild", this.aq);
      if (this.despawned) {
         nbt.setBoolean("sexmod:despawned", true);
      }
   }

   public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      String mommyStr = nbt.getString("sexmod:mommy");
      if (!"".equals(mommyStr)) {
         this.galathPartnerUUID = UUID.fromString(mommyStr);
      }

      if (nbt.getBoolean("sexmod:despawned")) {
         this.aa = true;
      }

      this.aq = nbt.getBoolean("sexmod:iswild");
   }

   @Override
   protected boolean supportsCustomModels() {
      return false;
   }

   @Override
   public void setCustomModelCode(String modelCode) {
      super.setCustomModelCode(modelCode);
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
   protected Action getNextAction(Action action) {
      return null;
   }

   @Override
   protected Action getCumAction(Action action) {
      if (Action.isAny(action, Action.THREESOME_FAST, Action.THREESOME_SLOW)) {
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

      BlockPos pos = this.getPosition();
      ArrayList positions = new ArrayList();
      positions.addAll(BeeWorldData.hivePositions);
      positions.addAll(BeeWorldData.flowerPositions);

      for (BlockPos hivePos : (java.util.Collection<BlockPos>) (positions) ) {
         if (Math.sqrt(pos.distanceSq(hivePos)) < 700.0) {
            return false;
         }
      }

      BeeWorldData.addHivePosition(pos, BeeWorldData.flowerPositions);
      return true;
   }

   /**
    * CLIENT: the threesome action overrides — THREESOME_CUM ends the scene
    * (reset both girls, clear cum trails), the slow/fast transitions swap
    * between the soft/hard/back shared animations and drive the Galath's
    * masterbate/pussy-licking mirror actions.
    */
   @Override
   protected boolean handleActionAnimationOverrides(Action action, String animationName, boolean started, AnimationEvent event) {
      if (action == Action.THREESOME_CUM) {
         this.threesomeSlowStarted = false;
         this.threesomeFastStarted = false;
         this.threesomeCumDone = false;
         this.an = 2;
         this.resetCameraAndPhysics();
         GalathEntity galath = this.getGalathPartner(false);
         if (galath != null) {
            galath.resetCameraAndPhysics();
            CummyEntity.spawnCummyTrails(galath);
         }

         CummyEntity.spawnCummyTrails(this);
         return true;
      } else if (this.threesomeSlowStarted && action == Action.THREESOME_FAST) {
         this.setCurrentAction(Action.THREESOME_CUM);
         this.createAnimation("animation.shared.double_holding_cum", true, event, true);
         GalathEntity galath = this.getGalathPartner(false);
         if (galath != null) {
            galath.setCurrentAction(Action.MASTERBATE_SITTING_CUM);
         }

         return true;
      } else if ((this.threesomeSlowStarted || started) && action == Action.THREESOME_SLOW) {
         this.threesomeFastStarted = false;
         this.setCurrentAction(Action.THREESOME_FAST);
         this.createAnimation("animation.shared.double_holding_soft", true, event, true);
         GalathEntity galath = this.getGalathPartner(false);
         if (galath != null) {
            galath.ak();
         }

         return true;
      } else {
         if (this.threesomeSlowStarted) {
            return false;
         }

         if (started && !this.threesomeFastStarted && action == Action.THREESOME_FAST) {
            this.threesomeFastStarted = true;
            this.createAnimation("animation.shared.double_holding_hard", true, event, true);
            return true;
         }

         if (!started && action == Action.THREESOME_FAST) {
            this.threesomeCumDone = true;
            this.setCurrentAction(Action.THREESOME_SLOW);
            this.createAnimation("animation.shared.double_holding_back", true, event, true);
            GalathEntity galath = this.getGalathPartner(false);
            if (galath != null) {
               galath.startPussyLicking();
            }

            return true;
         } else if (this.threesomeCumDone && action == Action.THREESOME_SLOW) {
            this.threesomeCumDone = false;
            this.createAnimation("animation.shared.double_holding_slow", true, event, true);
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   protected <E extends IAnimatable> PlayState animationPredicate(AnimationEvent<E> event) {
      AnimationController controller = event.getController();
      if (this.eyesController == controller) {
         if (this.getCorruptEntity() == null) {
            return PlayState.STOP;
         }

         this.createAnimation("animation.manglelie.angry_face", true, event);
         return PlayState.CONTINUE;
      } else if (this.movementController == controller) {
         if (this.getCurrentAction() == Action.NULL && !this.isCorrupting()) {
            if (Math.abs(this.prevPosX - this.posX) + Math.abs(this.prevPosZ - this.posZ) > 0.0) {
               if ((Boolean)this.entityDataManager.get(ar)) {
                  this.createAnimation("animation.manglelie.scared_run", true, event);
               } else {
                  this.createAnimation("animation.manglelie.walk", true, event);
               }

               this.rotationYaw = this.rotationYawHead;
               return PlayState.CONTINUE;
            } else {
               this.createAnimation("animation.manglelie.idle", true, event);
               return PlayState.CONTINUE;
            }
         } else {
            return PlayState.STOP;
         }
      } else {
         switch (this.getCurrentAction()) {
            case RUN:
               this.createAnimation("animation.manglelie.running", true, event);
               break;
            case RIDE_MOMMY_HEAD:
               this.createAnimation("animation.manglelie.sit_on_galath", true, event);
               break;
            case THREESOME_SLOW:
               if (this.threesomeCumDone) {
                  this.createAnimation("animation.shared.double_holding_back", true, event);
               } else {
                  this.playRandomizedAnimation("animation.shared.double_holding_slow", 4, 0.33F, event);
               }
               break;
            case THREESOME_FAST:
               if (this.threesomeFastStarted) {
                  this.playRandomizedAnimation("animation.shared.double_holding_hard", 3, 0.33F, event);
               } else {
                  this.createAnimation("animation.shared.double_holding_soft", true, event);
               }
               break;
            case THREESOME_CUM:
               this.createAnimation("animation.shared.double_holding_cum", true, event);
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
   public void registerControllers(AnimationData data) {
      data.addAnimationController(this.movementController);
      data.addAnimationController(this.eyesController);
      this.actionController.registerSoundListener(sound -> {
         switch (sound.sound) {
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
               CummyEntity.registerTrail(new DynamicTrailRenderer(10, girl -> {
                  Vec3d emitterPos = girl.getBoneWorldPosition("semenEmitter");
                  Vec3d semenDir = girl.getBoneWorldPosition("semenDir");
                  return emitterPos.subtract(semenDir).normalize();
               }, girl -> girl.getCachedBoneOffset("semenEmitter").add(girl.getTargetPosition()), this, 0.3F, 0.3F));
               break;
            case "blackScreen":
               if (this.isControlledByLocalPlayer()) {
                  BeeScreen.enableInteraction();
               }
         }
      });
      data.addAnimationController(this.actionController);
   }

   /**
    * Forge handler: Mang's arrows must never damage other girls — the
    * projectile impact on a {@link BaseGirlEntity} is cancelled.
    */
   public static class b {
      @SubscribeEvent
      public void handleArrowHit(Arrow event) {
         RayTraceResult rayTrace = event.getRayTraceResult();
         EntityArrow arrow = event.getArrow();
         if (arrow.shootingEntity instanceof ManglelieEntity) {
            if (rayTrace.entityHit instanceof BaseGirlEntity) {
               event.setCanceled(true);
            }
         }
      }

   }
}
