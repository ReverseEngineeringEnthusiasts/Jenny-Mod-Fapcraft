package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.VectorMath;
import com.google.common.collect.Multimap;
import java.util.List;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * <b>Role.</b> The combat follow goal for the NPC girls
 * ({@link AbstractGirlNpcEntity}) — the girl's full companion behaviour:
 * follows the master, rides the master's mount, attacks hostile mobs and
 * anything that attacked the girl or her master (melee with her weapon, or a
 * charged bow shot from the bow slot), wanders near the master when idle and
 * enters the downed state at 0 HP.
 * <p>
 * <b>Flow.</b> {@link #getCurrentState()} evaluates the world each tick
 * (ride -&gt; target -&gt; attacker -&gt; master's target -&gt; nearby mobs -
 * &gt; follow/idle); {@link #setState(...)} executes the chosen state.
 * Combat data is mirrored into {@code ATTACK_MODE} (0 idle, 1 melee, 2 bow)
 * and the weapon damage is computed from the held item's attribute modifiers
 * ({@link #startAttack()}).
 * <p>
 * <b>Pitfalls.</b> The inner class {@code a} owns the downed mechanic: damage
 * that would kill a bound girl instead downs her (action DOWNED, 1 HP
 * remaining), healing to full revives her, and death drops the inventory.
 * {@code updateTask} force-clears the BOW action each tick.
 */
public class GirlFollowGoal extends GirlFollowAiBase {
   AbstractGirlNpcEntity girl;
   EntityLivingBase target;
   Entity mountEntity;
   double lastDistance = Float.MAX_VALUE;
   Vec3d masterPos = Vec3d.ZERO;
   int wanderTimer = 0;
   int attackCooldown = 0;
   int attackTimer = 0;
   int bowTimer = 0;
   int checkTimer = 0;

   public GirlFollowGoal(AbstractGirlNpcEntity girl) {
      super(girl);
      this.girl = girl;
   }

   @Override
   public void updateTask() {
      super.updateTask();
      this.lastDistance = this.girl.getDistance(this.master);
      this.masterPos = this.master.getPositionVector();
      if (this.girl.getCurrentAction() == Action.BOW) {
         this.girl.setCurrentAction(Action.NULL);
      }
   }

   boolean isTargetVisible(EntityLivingBase target) {
      Vec3d girlPos = this.girl.getPositionVector();
      return !(target instanceof BaseGirlEntity)
         && this.attackCooldown <= 0
         && target != null
         && target.world != null
         && !this.girl.equals(target)
         && target.isEntityAlive()
         && girlPos.distanceTo(this.master.getPositionVector()) < 15.0
         && girlPos.distanceTo(target.getPositionVector()) < 20.0
         && !target.equals(this.master);
   }

   /**
    * ATTACK/FOLLOW/IDLE/RIDE/DOWNED execution: melee lunge + swing, bow
    * charge + shot (32-tick cycle), follow/navigate, wander, ride
    * positioning on the master's mount, or stop.
    */
   @Override
   protected void setState(GirlFollowAiBase.GirlFollowAiBaseState state) {
      switch (state) {
         case ATTACK:
            this.girl.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            double dist = this.girl.getDistance(this.target);
            this.navigator.clearPath();
            if (dist < 1.9 && --this.attackTimer <= 0) {
               this.startAttack();
            } else {
               if (this.girl.inventory.getStackInSlot(1).getItem() instanceof ItemBow && this.girl.getEntitySenses().canSee(this.target) && ++this.bowTimer > 0 && dist > 6.0) {
                  this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 2);
                  this.girl.setCurrentAction(Action.BOW);
                  if (++this.bowTimer >= 32) {
                     this.bowTimer = -20;
                     this.shootArrow();
                     this.girl.setCurrentAction(Action.NULL);
                  }

                  this.lastDistance = this.girl.getDistance(this.master);
                  this.masterPos = this.master.getPositionVector();
                  return;
               }

               if (dist < 2.0) {
                  this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 1);
                  this.navigator.tryMoveToEntityLiving(this.target, 0.5);
                  this.girl.setWalkSpeed(BaseGirlEntity.BaseGirlEntityState.WALK);
               } else {
                  this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 1);
                  this.navigator.tryMoveToEntityLiving(this.target, 0.7);
                  this.girl.setWalkSpeed(BaseGirlEntity.BaseGirlEntityState.RUN);
               }
            }
            break;
         case FOLLOW:
            this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 0);
            double followDist = this.girl.getDistance(this.master);
            if (this.navigator.getPathSearchRange() > followDist) {
               this.navigator.clearPath();
               if (!this.girl.downed) {
                  this.navigator.tryMoveToEntityLiving(this.master, 0.5);
                  this.handleKnockback();
               }
            } else {
               this.updateNavigation();
            }

            this.wanderTimer = 300;
            this.getFollowDistance();
            break;
         case IDLE:
            this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 0);
            if (!this.girl.downed) {
               if (++this.wanderTimer > 200 + Reference.RANDOM.nextInt(100)) {
                  this.wanderTimer = 0;
                  Vec3d masterPos = this.master.getPositionVector();
                  Vec3d targetPos = new Vec3d(
                     masterPos.x + 1.0 + Reference.RANDOM.nextFloat() * 3.0F, masterPos.y, masterPos.z + 1.0 + Reference.RANDOM.nextFloat() * 3.0F
                  );
                  this.navigator.clearPath();
                  this.navigator.tryMoveToXYZ(targetPos.x, targetPos.y, targetPos.z, 0.5);
               }

               this.getFollowDistance();
            } else if (this.girl.getDistance(this.master) > 10.0F) {
               this.updateNavigation();
            }
            break;
         case RIDE:
            if (this.girl.isRiding()) {
               this.girl.setCurrentAction(Action.SIT);
            } else {
               this.girl.setNoGravity(true);
               this.girl.noClip = true;
               Vec3d ridePos = this.master.getPositionVector().subtract(this.mountEntity.getLookVec().x * 0.5, 0.0, this.mountEntity.getLookVec().z * 0.5);
               this.girl.setPositionAndRotation(ridePos.x, ridePos.y, ridePos.z, 0.0F, 0.0F);
               this.girl.motionX = 0.0;
               this.girl.motionY = 0.0;
               this.girl.motionZ = 0.0;
               this.girl.setCurrentAction(Action.RIDE);
            }
            break;
         case DOWNED:
            this.navigator.clearPath();
      }
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState getCurrentState() {
      this.attackCooldown--;
      if (!this.girl.downed && this.girl.getInteractionPlayerUUID() == null) {
         if (this.master.isRiding()) {
            Entity mount = this.master.getRidingEntity();
            if (this.girl.isRiding() || this.girl.startRiding(mount) || mount instanceof EntityHorse && ((EntityHorse)mount).isHorseSaddled()) {
               this.mountEntity = mount;
               return GirlFollowAiBase.GirlFollowAiBaseState.RIDE;
            }
         } else if (!this.master.isRiding() && this.girl.isRiding() || this.state == GirlFollowAiBase.GirlFollowAiBaseState.RIDE && !this.master.isRiding()) {
            this.girl.setCurrentAction(Action.NULL);
            this.girl.dismountRidingEntity();
            this.girl.noClip = false;
            this.girl.setNoGravity(false);
         }

         if (this.isTargetVisible(this.target)) {
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         DamageSource source = this.girl.getLastDamageSource();
         if (source != null) {
            EntityLivingBase attacker = (EntityLivingBase)source.getTrueSource();
            if (this.isTargetVisible(attacker)) {
               this.target = attacker;
               return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
            }
         }

         EntityLivingBase attacked = this.master.getLastAttackedEntity();
         if (this.master.ticksExisted - this.master.getLastAttackedEntityTime() < 140 && this.isTargetVisible(attacked)) {
            this.target = attacked;
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         if (this.state != GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            source = this.master.getLastDamageSource();
            if (source != null) {
               attacked = (EntityLivingBase)source.getTrueSource();
               if (this.isTargetVisible(attacked)) {
                  this.target = attacked;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }

            Vec3d pos = this.girl.getPositionVector();
            AxisAlignedBB aabb = new AxisAlignedBB(
               pos.x - 5.0,
               pos.y - 2.0,
               pos.z - 5.0,
               pos.x + 5.0,
               pos.y + 2.0,
               pos.z + 5.0
            );
            List<EntityMob> mobs = (List<EntityMob>) (List) this.girl.world.getEntitiesWithinAABB(EntityMob.class, aabb);
            mobs.sort((mobA, mobB) -> {
               double distA = mobB.getDistance(this.girl);
               double distB = mobB.getDistance(this.girl);
               if (distA == distB) {
                  return 0;
               } else {
                  return distA < distB ? -1 : 1;
               }
            });

            for (EntityMob mob : (java.util.Collection<EntityMob>) (mobs) ) {
               if (this.isTargetVisible(mob) && !(mob instanceof EntityCreeper)) {
                  this.target = mob;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }
         }

         float followDist = this.girl.getDistance(this.master);
         boolean tooFar = followDist > 5.0F;
         if (!tooFar && this.state == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            if (++this.checkTimer > 60) {
               tooFar = false;
               this.checkTimer = 0;
            } else {
               tooFar = true;
            }
         }

         if (tooFar && this.state == GirlFollowAiBase.GirlFollowAiBaseState.ATTACK) {
            this.attackCooldown = 60;
         }

         return tooFar ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      } else {
         return GirlFollowAiBase.GirlFollowAiBaseState.DOWNED;
      }
   }

   /**
    * SERVER: fires the arrow at the current target with drop compensation,
    * skeleton-style shooting sound and 4.5 base damage.
    */
   public void shootArrow() {
      EntityArrow arrow = this.createArrow();
      double dx = this.target.posX - this.girl.posX;
      double dy = this.target.getEntityBoundingBox().minY + this.target.height / 3.0F - arrow.posY;
      double dz = this.target.posZ - this.girl.posZ;
      double followDist = MathHelper.sqrt(dx * dx + dz * dz);
      arrow.shoot(dx, dy + followDist * 0.2F, dz, 1.6F, 2.0F);
      this.girl.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.girl.getRNG().nextFloat() * 0.4F + 0.8F));
      this.girl.world.spawnEntity(arrow);
      arrow.setDamage(4.5);
   }

   protected EntityArrow createArrow() {
      EntityTippedArrow arrow = new EntityTippedArrow(this.girl.world, this.girl);
      ItemStack stack = this.girl.inventory.getStackInSlot(1);
      double power = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, stack);
      int punch = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, stack);
      int flame = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, stack);
      if (power != 0.0) {
         arrow.setDamage(arrow.getDamage() + power * 0.5 + 0.5);
      }

      if (punch != 0) {
         arrow.setKnockbackStrength(punch);
      }

      if (flame != 0) {
         arrow.setFire(100);
      }

      return arrow;
   }

   /**
    * SERVER: the melee swing — computes damage from the held weapon's
    * attack-damage/speed modifiers plus enchantments (sharpness, knockback,
    * fire aspect, sweeping), applies knockback and hits the target (and
    * sweep-adjacent enemies).
    */
   void startAttack() {
      this.girl.setCurrentAction(Action.ATTACK);
      this.dataManager.set(AbstractGirlNpcEntity.ATTACK_MODE, 1);
      ItemStack weapon = this.girl.inventory.getStackInSlot(0);
      Multimap modifiers = weapon.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
      float damage = 0.0F;
      float speed = 0.0F;

      for (AttributeModifier modifier : (java.util.Collection<AttributeModifier>) modifiers.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
         damage = (float)modifier.getAmount();
      }

      for (AttributeModifier modifier : (java.util.Collection<AttributeModifier>) modifiers.get(SharedMonsterAttributes.ATTACK_SPEED.getName())) {
         speed = (float)modifier.getAmount();
      }

      speed = Math.max(speed, 0.5F);
      float enchantDamage = EnchantmentHelper.getModifierForCreature(weapon, this.target.getCreatureAttribute());
      int knockback = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, weapon);
      int fireAspect = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, weapon);
      int sweeping = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, weapon);
      this.target
         .knockBack(
            this.girl,
            knockback * 0.5F,
            MathHelper.sin(this.girl.rotationYaw * (float) (Math.PI / 180.0)),
            -MathHelper.cos(this.girl.rotationYaw * (float) (Math.PI / 180.0))
         );
      this.target.setFire(fireAspect * 4);
      if (sweeping != 0) {
         float sweepMod = 0.5F;
         if (sweeping == 2) {
            sweepMod = 0.67F;
         } else if (sweeping == 3) {
            sweepMod = 0.75F;
         }

         for (EntityLivingBase entity : this.girl.world.getEntitiesWithinAABB(EntityLivingBase.class, this.target.getEntityBoundingBox().grow(1.0, 0.25, 1.0))) {
            if (entity != this.girl && entity != this.master && entity != this.target && !this.girl.isOnSameTeam(entity) && this.girl.getDistanceSq(entity) < 9.0) {
               entity.knockBack(
                  this.girl,
                  0.4F,
                  MathHelper.sin(this.girl.rotationYaw * (float) (Math.PI / 180.0)),
                  -MathHelper.cos(this.girl.rotationYaw * (float) (Math.PI / 180.0))
               );
               entity.attackEntityFrom(DamageSource.causeMobDamage(this.girl), (damage + enchantDamage) * sweepMod);
            }
         }
      }

      this.target.attackEntityFrom(DamageSource.causeMobDamage(this.girl), damage + enchantDamage);
      this.attackTimer = Math.round(Math.abs(speed) / 3.373494F * 20.0F);
   }

   @Override
   protected double getFollowDistance() {
      double followDistance = super.getFollowDistance();
      if (this.girl.downed) {
         followDistance = 0.0;
      }

      this.navigator.setSpeed(followDistance);
      this.girl.setWalkSpeed(this.girl.getWalkType());
      return followDistance;
   }

   @Override
   public void resetTask() {
      super.resetTask();
      this.girl.getDataManager().set(AbstractGirlNpcEntity.ATTACK_MODE, 0);
   }

   void handleKnockback() {
      if (!this.girl.onGround && !this.girl.isInWater() && this.girl.motionX + this.girl.motionZ == 0.0 && !(this.girl.motionY <= 0.0)) {
         Vec3d vec = new Vec3d(0.0, 0.0, 0.1F);
         vec = VectorMath.rotateByYaw(vec, this.girl.rotationYaw);
         this.girl.motionX = vec.x;
         this.girl.motionZ = vec.z;
      }
   }

   /**
    * Combat event handlers: lethal damage downs a bound girl instead of
    * killing her ({@code downed} + {@link Action#DOWNED}), full heal revives
    * her, and death drops her inventory (SERVER).
    */
   public static class a {
      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent event) {
         if (event.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)event.getEntityLiving();
            if (girl.downed) {
               event.setCanceled(true);
            } else if (girl.getHealth() - event.getAmount() < 0.0F && !((String)girl.getDataManager().get(AbstractGirlNpcEntity.MASTER)).equals("")) {
               girl.downed = true;
               girl.setCurrentAction(Action.DOWNED);
               event.setAmount(girl.getHealth() - 1.0F);
               girl.getNavigator().clearPath();
            }
         }
      }

      @SubscribeEvent
      public void onLivingHeal(LivingHealEvent event) {
         if (event.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)event.getEntityLiving();
            if (girl.downed && girl.getHealth() + event.getAmount() >= girl.getMaxHealth()) {
               girl.downed = false;
               girl.setCurrentAction(Action.NULL);
            }
         }
      }

      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent event) {
         if (event.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity girl = (AbstractGirlNpcEntity)event.getEntityLiving();
            if (girl.world.isRemote) {
               return;
            }

            for (int i = 0; i < 6; i++) {
               Item item = girl.inventory.getStackInSlot(i).getItem();
               if (item != Items.AIR) {
                  girl.dropItem(item, 1);
               }
            }
         }
      }

   }
}
