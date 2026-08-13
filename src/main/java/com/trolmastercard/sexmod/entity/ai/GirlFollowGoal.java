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

public class GirlFollowGoal extends GirlFollowAiBase {
   AbstractGirlNpcEntity q;
   EntityLivingBase r;
   Entity o;
   double l = Float.MAX_VALUE;
   Vec3d i = Vec3d.ZERO;
   int j = 0;
   int n = 0;
   int k = 0;
   int p = 0;
   int m = 0;

   public GirlFollowGoal(AbstractGirlNpcEntity var1) {
      super(var1);
      this.q = var1;
   }

   @Override
   public void updateTask() {
      super.updateTask();
      this.l = this.q.getDistance(this.a);
      this.i = this.a.getPositionVector();
      if (this.q.getCurrentAction() == Action.BOW) {
         this.q.setCurrentAction(Action.NULL);
      }
   }

   boolean a_clash827(EntityLivingBase var1) {
      Vec3d var2 = this.q.getPositionVector();
      return !(var1 instanceof BaseGirlEntity)
         && this.n <= 0
         && var1 != null
         && var1.world != null
         && !this.q.equals(var1)
         && var1.isEntityAlive()
         && var2.distanceTo(this.a.getPositionVector()) < 15.0
         && var2.distanceTo(var1.getPositionVector()) < 20.0
         && !var1.equals(this.a);
   }

   @Override
   protected void a(GirlFollowAiBase.GirlFollowAiBaseState var1) {
      switch (var1) {
         case ATTACK:
            this.q.getLookHelper().setLookPositionWithEntity(this.r, 30.0F, 30.0F);
            double var6 = this.q.getDistance(this.r);
            this.c.clearPath();
            if (var6 < 1.9 && --this.k <= 0) {
               this.d_clash830();
            } else {
               if (this.q.Q.getStackInSlot(1).getItem() instanceof ItemBow && this.q.getEntitySenses().canSee(this.r) && ++this.p > 0 && var6 > 6.0) {
                  this.e.set(AbstractGirlNpcEntity.M, 2);
                  this.q.setCurrentAction(Action.BOW);
                  if (++this.p >= 32) {
                     this.p = -20;
                     this.e_clash828();
                     this.q.setCurrentAction(Action.NULL);
                  }

                  this.l = this.q.getDistance(this.a);
                  this.i = this.a.getPositionVector();
                  return;
               }

               if (var6 < 2.0) {
                  this.e.set(AbstractGirlNpcEntity.M, 1);
                  this.c.tryMoveToEntityLiving(this.r, 0.5);
                  this.q.setWalkSpeed(BaseGirlEntity.BaseGirlEntityState.WALK);
               } else {
                  this.e.set(AbstractGirlNpcEntity.M, 1);
                  this.c.tryMoveToEntityLiving(this.r, 0.7);
                  this.q.setWalkSpeed(BaseGirlEntity.BaseGirlEntityState.RUN);
               }
            }
            break;
         case FOLLOW:
            this.e.set(AbstractGirlNpcEntity.M, 0);
            double var2 = this.q.getDistance(this.a);
            if (this.c.getPathSearchRange() > var2) {
               this.c.clearPath();
               if (!this.q.N) {
                  this.c.tryMoveToEntityLiving(this.a, 0.5);
                  this.a_clash831();
               }
            } else {
               this.c_clash805();
            }

            this.j = 300;
            this.b_clash806();
            break;
         case IDLE:
            this.e.set(AbstractGirlNpcEntity.M, 0);
            if (!this.q.N) {
               if (++this.j > 200 + Reference.f.nextInt(100)) {
                  this.j = 0;
                  Vec3d var7 = this.a.getPositionVector();
                  Vec3d var5 = new Vec3d(
                     var7.x + 1.0 + Reference.f.nextFloat() * 3.0F, var7.y, var7.z + 1.0 + Reference.f.nextFloat() * 3.0F
                  );
                  this.c.clearPath();
                  this.c.tryMoveToXYZ(var5.x, var5.y, var5.z, 0.5);
               }

               this.b_clash806();
            } else if (this.q.getDistance(this.a) > 10.0F) {
               this.c_clash805();
            }
            break;
         case RIDE:
            if (this.q.isRiding()) {
               this.q.setCurrentAction(Action.SIT);
            } else {
               this.q.setNoGravity(true);
               this.q.noClip = true;
               Vec3d var4 = this.a.getPositionVector().subtract(this.o.getLookVec().x * 0.5, 0.0, this.o.getLookVec().z * 0.5);
               this.q.setPositionAndRotation(var4.x, var4.y, var4.z, 0.0F, 0.0F);
               this.q.motionX = 0.0;
               this.q.motionY = 0.0;
               this.q.motionZ = 0.0;
               this.q.setCurrentAction(Action.RIDE);
            }
            break;
         case DOWNED:
            this.c.clearPath();
      }
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState a_clash807() {
      this.n--;
      if (!this.q.N && this.q.getInteractionPlayerUUID() == null) {
         if (this.a.isRiding()) {
            Entity var1 = this.a.getRidingEntity();
            if (this.q.isRiding() || this.q.startRiding(var1) || var1 instanceof EntityHorse && ((EntityHorse)var1).isHorseSaddled()) {
               this.o = var1;
               return GirlFollowAiBase.GirlFollowAiBaseState.RIDE;
            }
         } else if (!this.a.isRiding() && this.q.isRiding() || this.f == GirlFollowAiBase.GirlFollowAiBaseState.RIDE && !this.a.isRiding()) {
            this.q.setCurrentAction(Action.NULL);
            this.q.dismountRidingEntity();
            this.q.noClip = false;
            this.q.setNoGravity(false);
         }

         if (this.a_clash827(this.r)) {
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         DamageSource var2 = this.q.getLastDamageSource();
         if (var2 != null) {
            EntityLivingBase var8 = (EntityLivingBase)var2.getTrueSource();
            if (this.a_clash827(var8)) {
               this.r = var8;
               return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
            }
         }

         EntityLivingBase var9 = this.a.getLastAttackedEntity();
         if (this.a.ticksExisted - this.a.getLastAttackedEntityTime() < 140 && this.a_clash827(var9)) {
            this.r = var9;
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         if (this.f != GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            var2 = this.a.getLastDamageSource();
            if (var2 != null) {
               var9 = (EntityLivingBase)var2.getTrueSource();
               if (this.a_clash827(var9)) {
                  this.r = var9;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }

            Vec3d var3 = this.q.getPositionVector();
            AxisAlignedBB var4 = new AxisAlignedBB(
               var3.x - 5.0,
               var3.y - 2.0,
               var3.z - 5.0,
               var3.x + 5.0,
               var3.y + 2.0,
               var3.z + 5.0
            );
            List<EntityMob> var5 = (List<EntityMob>) (List) this.q.world.getEntitiesWithinAABB(EntityMob.class, var4);
            var5.sort((var1x, var2x) -> {
               double var3x = var1x.getDistance(this.q);
               double var5x = var2x.getDistance(this.q);
               if (var3x == var5x) {
                  return 0;
               } else {
                  return var3x < var5x ? -1 : 1;
               }
            });

            for (EntityMob var7 : (java.util.Collection<EntityMob>) (var5) ) {
               if (this.a_clash827(var7) && !(var7 instanceof EntityCreeper)) {
                  this.r = var7;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }
         }

         float var12 = this.q.getDistance(this.a);
         boolean var13 = var12 > 5.0F;
         if (!var13 && this.f == GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            if (++this.m > 60) {
               var13 = false;
               this.m = 0;
            } else {
               var13 = true;
            }
         }

         if (var13 && this.f == GirlFollowAiBase.GirlFollowAiBaseState.ATTACK) {
            this.n = 60;
         }

         return var13 ? GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW : GirlFollowAiBase.GirlFollowAiBaseState.IDLE;
      } else {
         return GirlFollowAiBase.GirlFollowAiBaseState.DOWNED;
      }
   }

   public void e_clash828() {
      EntityArrow var1 = this.b_clash829();
      double var2 = this.r.posX - this.q.posX;
      double var4 = this.r.getEntityBoundingBox().minY + this.r.height / 3.0F - var1.posY;
      double var6 = this.r.posZ - this.q.posZ;
      double var8 = MathHelper.sqrt(var2 * var2 + var6 * var6);
      var1.shoot(var2, var4 + var8 * 0.2F, var6, 1.6F, 2.0F);
      this.q.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.q.getRNG().nextFloat() * 0.4F + 0.8F));
      this.q.world.spawnEntity(var1);
      var1.setDamage(4.5);
   }

   protected EntityArrow b_clash829() {
      EntityTippedArrow var1 = new EntityTippedArrow(this.q.world, this.q);
      ItemStack var2 = this.q.Q.getStackInSlot(1);
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

   void d_clash830() {
      this.q.setCurrentAction(Action.ATTACK);
      this.e.set(AbstractGirlNpcEntity.M, 1);
      ItemStack var1 = this.q.Q.getStackInSlot(0);
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
      float var14 = EnchantmentHelper.getModifierForCreature(var1, this.r.getCreatureAttribute());
      int var16 = EnchantmentHelper.getEnchantmentLevel(Enchantments.KNOCKBACK, var1);
      int var7 = EnchantmentHelper.getEnchantmentLevel(Enchantments.FIRE_ASPECT, var1);
      int var8 = EnchantmentHelper.getEnchantmentLevel(Enchantments.SWEEPING, var1);
      this.r
         .knockBack(
            this.q,
            var16 * 0.5F,
            MathHelper.sin(this.q.rotationYaw * (float) (Math.PI / 180.0)),
            -MathHelper.cos(this.q.rotationYaw * (float) (Math.PI / 180.0))
         );
      this.r.setFire(var7 * 4);
      if (var8 != 0) {
         float var9 = 0.5F;
         if (var8 == 2) {
            var9 = 0.67F;
         } else if (var8 == 3) {
            var9 = 0.75F;
         }

         for (EntityLivingBase var11 : this.q.world.getEntitiesWithinAABB(EntityLivingBase.class, this.r.getEntityBoundingBox().grow(1.0, 0.25, 1.0))) {
            if (var11 != this.q && var11 != this.a && var11 != this.r && !this.q.isOnSameTeam(var11) && this.q.getDistanceSq(var11) < 9.0) {
               var11.knockBack(
                  this.q,
                  0.4F,
                  MathHelper.sin(this.q.rotationYaw * (float) (Math.PI / 180.0)),
                  -MathHelper.cos(this.q.rotationYaw * (float) (Math.PI / 180.0))
               );
               var11.attackEntityFrom(DamageSource.causeMobDamage(this.q), (var3 + var14) * var9);
            }
         }
      }

      this.r.attackEntityFrom(DamageSource.causeMobDamage(this.q), var3 + var14);
      this.k = Math.round(Math.abs(var4) / 3.373494F * 20.0F);
   }

   @Override
   protected double b_clash806() {
      double var1 = super.b_clash806();
      if (this.q.N) {
         var1 = 0.0;
      }

      this.c.setSpeed(var1);
      this.q.setWalkSpeed(this.q.getWalkType());
      return var1;
   }

   @Override
   public void resetTask() {
      super.resetTask();
      this.q.getDataManager().set(AbstractGirlNpcEntity.M, 0);
   }

   void a_clash831() {
      if (!this.q.onGround && !this.q.isInWater() && this.q.motionX + this.q.motionZ == 0.0 && !(this.q.motionY <= 0.0)) {
         Vec3d var1 = new Vec3d(0.0, 0.0, 0.1F);
         var1 = VectorMath.rotateByYaw(var1, this.q.rotationYaw);
         this.q.motionX = var1.x;
         this.q.motionZ = var1.z;
      }
   }


   public static class a {
      @SubscribeEvent
      public void a(LivingHurtEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.N) {
               var1.setCanceled(true);
            } else if (var2.getHealth() - var1.getAmount() < 0.0F && !((String)var2.getDataManager().get(AbstractGirlNpcEntity.MASTER)).equals("")) {
               var2.N = true;
               var2.setCurrentAction(Action.DOWNED);
               var1.setAmount(var2.getHealth() - 1.0F);
               var2.getNavigator().clearPath();
            }
         }
      }

      @SubscribeEvent
      public void a(LivingHealEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.N && var2.getHealth() + var1.getAmount() >= var2.getMaxHealth()) {
               var2.N = false;
               var2.setCurrentAction(Action.NULL);
            }
         }
      }

      @SubscribeEvent
      public void a(LivingDeathEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.world.isRemote) {
               return;
            }

            for (int var3 = 0; var3 < 6; var3++) {
               Item var4 = var2.Q.getStackInSlot(var3).getItem();
               if (var4 != Items.AIR) {
                  var2.dropItem(var4, 1);
               }
            }
         }
      }

   }
}
