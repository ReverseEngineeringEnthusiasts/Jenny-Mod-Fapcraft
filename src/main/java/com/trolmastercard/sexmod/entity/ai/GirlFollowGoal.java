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

   public GirlFollowGoal(AbstractGirlNpcEntity var1) {
      super(var1);
      this.girl = var1;
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

   boolean isTargetVisible(EntityLivingBase var1) {
      Vec3d var2 = this.girl.getPositionVector();
      return !(var1 instanceof BaseGirlEntity)
         && this.attackCooldown <= 0
         && var1 != null
         && var1.world != null
         && !this.girl.equals(var1)
         && var1.isEntityAlive()
         && var2.distanceTo(this.master.getPositionVector()) < 15.0
         && var2.distanceTo(var1.getPositionVector()) < 20.0
         && !var1.equals(this.master);
   }

   /**
    * ATTACK/FOLLOW/IDLE/RIDE/DOWNED execution: melee lunge + swing, bow
    * charge + shot (32-tick cycle), follow/navigate, wander, ride
    * positioning on the master's mount, or stop.
    */
   @Override
   protected void setState(GirlFollowAiBase.GirlFollowAiBaseState var1) {
      switch (var1) {
         case ATTACK:
            this.girl.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);
            double var6 = this.girl.getDistance(this.target);
            this.navigator.clearPath();
            if (var6 < 1.9 && --this.attackTimer <= 0) {
               this.startAttack();
            } else {
               if (this.girl.inventory.getStackInSlot(1).getItem() instanceof ItemBow && this.girl.getEntitySenses().canSee(this.target) && ++this.bowTimer > 0 && var6 > 6.0) {
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

               if (var6 < 2.0) {
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
            double var2 = this.girl.getDistance(this.master);
            if (this.navigator.getPathSearchRange() > var2) {
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
                  Vec3d var7 = this.master.getPositionVector();
                  Vec3d var5 = new Vec3d(
                     var7.x + 1.0 + Reference.RANDOM.nextFloat() * 3.0F, var7.y, var7.z + 1.0 + Reference.RANDOM.nextFloat() * 3.0F
                  );
                  this.navigator.clearPath();
                  this.navigator.tryMoveToXYZ(var5.x, var5.y, var5.z, 0.5);
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
               Vec3d var4 = this.master.getPositionVector().subtract(this.mountEntity.getLookVec().x * 0.5, 0.0, this.mountEntity.getLookVec().z * 0.5);
               this.girl.setPositionAndRotation(var4.x, var4.y, var4.z, 0.0F, 0.0F);
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
            Entity var1 = this.master.getRidingEntity();
            if (this.girl.isRiding() || this.girl.startRiding(var1) || var1 instanceof EntityHorse && ((EntityHorse)var1).isHorseSaddled()) {
               this.mountEntity = var1;
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

         DamageSource var2 = this.girl.getLastDamageSource();
         if (var2 != null) {
            EntityLivingBase var8 = (EntityLivingBase)var2.getTrueSource();
            if (this.isTargetVisible(var8)) {
               this.target = var8;
               return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
            }
         }

         EntityLivingBase var9 = this.master.getLastAttackedEntity();
         if (this.master.ticksExisted - this.master.getLastAttackedEntityTime() < 140 && this.isTargetVisible(var9)) {
            this.target = var9;
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         if (this.state != GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            var2 = this.master.getLastDamageSource();
            if (var2 != null) {
               var9 = (EntityLivingBase)var2.getTrueSource();
               if (this.isTargetVisible(var9)) {
                  this.target = var9;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }

            Vec3d var3 = this.girl.getPositionVector();
            AxisAlignedBB var4 = new AxisAlignedBB(
               var3.x - 5.0,
               var3.y - 2.0,
               var3.z - 5.0,
               var3.x + 5.0,
               var3.y + 2.0,
               var3.z + 5.0
            );
            List<EntityMob> var5 = (List<EntityMob>) (List) this.girl.world.getEntitiesWithinAABB(EntityMob.class, var4);
            var5.sort((var1x, var2x) -> {
               double var3x = var1x.getDistance(this.girl);
               double var5x = var2x.getDistance(this.girl);
               if (var3x == var5x) {
                  return 0;
               } else {
                  return var3x < var5x ? -1 : 1;
               }
            });

            for (EntityMob var7 : (java.util.Collection<EntityMob>) (var5) ) {
               if (this.isTargetVisible(var7) && !(var7 instanceof EntityCreeper)) {
                  this.target = var7;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }
         }

         float var12 = this.girl.getDistance(this.master);
         boolean var13 = var12 > 5.0F;
         if (!var13 && this.state == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            if (++this.checkTimer > 60) {
               var13 = false;
               this.checkTimer = 0;
            } else {
               var13 = true;
            }
         }

         if (var13 && this.state == GirlFollowAiBase.GirlFollowAiBaseState.ATTACK) {
            this.attackCooldown = 60;
         }

         return var13 ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      } else {
         return GirlFollowAiBase.GirlFollowAiBaseState.DOWNED;
      }
   }

   /**
    * SERVER: fires the arrow at the current target with drop compensation,
    * skeleton-style shooting sound and 4.5 base damage.
    */
   public void shootArrow() {
      EntityArrow var1 = this.createArrow();
      double var2 = this.target.posX - this.girl.posX;
      double var4 = this.target.getEntityBoundingBox().minY + this.target.height / 3.0F - var1.posY;
      double var6 = this.target.posZ - this.girl.posZ;
      double var8 = MathHelper.sqrt(var2 * var2 + var6 * var6);
      var1.shoot(var2, var4 + var8 * 0.2F, var6, 1.6F, 2.0F);
      this.girl.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.girl.getRNG().nextFloat() * 0.4F + 0.8F));
      this.girl.world.spawnEntity(var1);
      var1.setDamage(4.5);
   }

   protected EntityArrow createArrow() {
      EntityTippedArrow var1 = new EntityTippedArrow(this.girl.world, this.girl);
      ItemStack var2 = this.girl.inventory.getStackInSlot(1);
      double var3 = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER, var2);
      int var5 = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH, var2);
      int var6 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME, var2);
      if (var3 != 0.0) {
         var1.setDamage(var1.getDamage() + var3 * 0.5 + 0.5);
      }

      if (var5 != 0) {
         var1.setKnockbackStrength(var5);
      }

      if (var6 != 0) {
         var1.setFire(100);
      }

      return var1;
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
      ItemStack var1 = this.girl.inventory.getStackInSlot(0);
      Multimap var2 = var1.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
      float var3 = 0.0F;
      float var4 = 0.0F;

      for (AttributeModifier var6 : (java.util.Collection<AttributeModifier>) var2.get(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
         var3 = (float)var6.getAmount();
      }

      for (AttributeModifier var15 : (java.util.Collection<AttributeModifier>) var2.get(SharedMonsterAttributes.ATTACK_SPEED.getName())) {
         var4 = (float)var15.getAmount();
      }

      var4 = Math.max(var4, 0.5F);
      float var14 = EnchantmentHelper.getModifierForCreature(var1, this.target.getCreatureAttribute());
      int var16 = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, var1);
      int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, var1);
      int var8 = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, var1);
      this.target
         .knockBack(
            this.girl,
            var16 * 0.5F,
            MathHelper.sin(this.girl.rotationYaw * (float) (Math.PI / 180.0)),
            -MathHelper.cos(this.girl.rotationYaw * (float) (Math.PI / 180.0))
         );
      this.target.setFire(var7 * 4);
      if (var8 != 0) {
         float var9 = 0.5F;
         if (var8 == 2) {
            var9 = 0.67F;
         } else if (var8 == 3) {
            var9 = 0.75F;
         }

         for (EntityLivingBase var11 : this.girl.world.getEntitiesWithinAABB(EntityLivingBase.class, this.target.getEntityBoundingBox().grow(1.0, 0.25, 1.0))) {
            if (var11 != this.girl && var11 != this.master && var11 != this.target && !this.girl.isOnSameTeam(var11) && this.girl.getDistanceSq(var11) < 9.0) {
               var11.knockBack(
                  this.girl,
                  0.4F,
                  MathHelper.sin(this.girl.rotationYaw * (float) (Math.PI / 180.0)),
                  -MathHelper.cos(this.girl.rotationYaw * (float) (Math.PI / 180.0))
               );
               var11.attackEntityFrom(DamageSource.causeMobDamage(this.girl), (var3 + var14) * var9);
            }
         }
      }

      this.target.attackEntityFrom(DamageSource.causeMobDamage(this.girl), var3 + var14);
      this.attackTimer = Math.round(Math.abs(var4) / 3.373494F * 20.0F);
   }

   @Override
   protected double getFollowDistance() {
      double var1 = super.getFollowDistance();
      if (this.girl.downed) {
         var1 = 0.0;
      }

      this.navigator.setSpeed(var1);
      this.girl.setWalkSpeed(this.girl.getWalkType());
      return var1;
   }

   @Override
   public void resetTask() {
      super.resetTask();
      this.girl.getDataManager().set(AbstractGirlNpcEntity.ATTACK_MODE, 0);
   }

   void handleKnockback() {
      if (!this.girl.onGround && !this.girl.isInWater() && this.girl.motionX + this.girl.motionZ == 0.0 && !(this.girl.motionY <= 0.0)) {
         Vec3d var1 = new Vec3d(0.0, 0.0, 0.1F);
         var1 = VectorMath.rotateByYaw(var1, this.girl.rotationYaw);
         this.girl.motionX = var1.x;
         this.girl.motionZ = var1.z;
      }
   }

   /**
    * Combat event handlers: lethal damage downs a bound girl instead of
    * killing her ({@code downed} + {@link Action#DOWNED}), full heal revives
    * her, and death drops her inventory (SERVER).
    */
   public static class a {
      @SubscribeEvent
      public void onLivingHurt(LivingHurtEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.downed) {
               var1.setCanceled(true);
            } else if (var2.getHealth() - var1.getAmount() < 0.0F && !((String)var2.getDataManager().get(AbstractGirlNpcEntity.MASTER)).equals("")) {
               var2.downed = true;
               var2.setCurrentAction(Action.DOWNED);
               var1.setAmount(var2.getHealth() - 1.0F);
               var2.getNavigator().clearPath();
            }
         }
      }

      @SubscribeEvent
      public void onLivingHeal(LivingHealEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.downed && var2.getHealth() + var1.getAmount() >= var2.getMaxHealth()) {
               var2.downed = false;
               var2.setCurrentAction(Action.NULL);
            }
         }
      }

      @SubscribeEvent
      public void onLivingDeath(LivingDeathEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.world.isRemote) {
               return;
            }

            for (int var3 = 0; var3 < 6; var3++) {
               Item var4 = var2.inventory.getStackInSlot(var3).getItem();
               if (var4 != Items.AIR) {
                  var2.dropItem(var4, 1);
               }
            }
         }
      }

   }
}
