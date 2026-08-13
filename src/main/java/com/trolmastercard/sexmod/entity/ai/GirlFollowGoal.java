package com.trolmastercard.sexmod.entity.ai;

import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.ck;







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
   Vec3d i = Vec3d.field_186680_a;
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
   public void func_75246_d() {
      super.func_75246_d();
      this.l = this.q.func_70032_d(this.a);
      this.i = this.a.func_174791_d();
      if (this.q.y_clash492() == fp.BOW) {
         this.q.b(fp.NULL);
      }
   }

   boolean a_clash827(EntityLivingBase var1) {
      Vec3d var2 = this.q.func_174791_d();
      return !(var1 instanceof BaseGirlEntity)
         && this.n <= 0
         && var1 != null
         && var1.field_70170_p != null
         && !this.q.equals(var1)
         && var1.func_70089_S()
         && var2.func_72438_d(this.a.func_174791_d()) < 15.0
         && var2.func_72438_d(var1.func_174791_d()) < 20.0
         && !var1.equals(this.a);
   }

   @Override
   protected void a(GirlFollowAiBase.GirlFollowAiBaseState var1) {
      switch (var1) {
         case ATTACK:
            this.q.func_70671_ap().func_75651_a(this.r, 30.0F, 30.0F);
            double var6 = this.q.func_70032_d(this.r);
            this.c.func_75499_g();
            if (var6 < 1.9 && --this.k <= 0) {
               this.d_clash830();
            } else {
               if (this.q.Q.getStackInSlot(1).func_77973_b() instanceof ItemBow && this.q.func_70635_at().func_75522_a(this.r) && ++this.p > 0 && var6 > 6.0) {
                  this.e.func_187227_b(AbstractGirlNpcEntity.M, 2);
                  this.q.b(fp.BOW);
                  if (++this.p >= 32) {
                     this.p = -20;
                     this.e_clash828();
                     this.q.b(fp.NULL);
                  }

                  this.l = this.q.func_70032_d(this.a);
                  this.i = this.a.func_174791_d();
                  return;
               }

               if (var6 < 2.0) {
                  this.e.func_187227_b(AbstractGirlNpcEntity.M, 1);
                  this.c.func_75497_a(this.r, 0.5);
                  this.q.a(BaseGirlEntity.BaseGirlEntityState.WALK);
               } else {
                  this.e.func_187227_b(AbstractGirlNpcEntity.M, 1);
                  this.c.func_75497_a(this.r, 0.7);
                  this.q.a(BaseGirlEntity.BaseGirlEntityState.RUN);
               }
            }
            break;
         case FOLLOW:
            this.e.func_187227_b(AbstractGirlNpcEntity.M, 0);
            double var2 = this.q.func_70032_d(this.a);
            if (this.c.func_111269_d() > var2) {
               this.c.func_75499_g();
               if (!this.q.N) {
                  this.c.func_75497_a(this.a, 0.5);
                  this.a_clash831();
               }
            } else {
               this.c_clash805();
            }

            this.j = 300;
            this.b_clash806();
            break;
         case IDLE:
            this.e.func_187227_b(AbstractGirlNpcEntity.M, 0);
            if (!this.q.N) {
               if (++this.j > 200 + Reference.f.nextInt(100)) {
                  this.j = 0;
                  Vec3d var7 = this.a.func_174791_d();
                  Vec3d var5 = new Vec3d(
                     var7.field_72450_a + 1.0 + Reference.f.nextFloat() * 3.0F, var7.field_72448_b, var7.field_72449_c + 1.0 + Reference.f.nextFloat() * 3.0F
                  );
                  this.c.func_75499_g();
                  this.c.func_75492_a(var5.field_72450_a, var5.field_72448_b, var5.field_72449_c, 0.5);
               }

               this.b_clash806();
            } else if (this.q.func_70032_d(this.a) > 10.0F) {
               this.c_clash805();
            }
            break;
         case RIDE:
            if (this.q.func_184218_aH()) {
               this.q.b(fp.SIT);
            } else {
               this.q.func_189654_d(true);
               this.q.field_70145_X = true;
               Vec3d var4 = this.a.func_174791_d().func_178786_a(this.o.func_70040_Z().field_72450_a * 0.5, 0.0, this.o.func_70040_Z().field_72449_c * 0.5);
               this.q.func_70080_a(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c, 0.0F, 0.0F);
               this.q.field_70159_w = 0.0;
               this.q.field_70181_x = 0.0;
               this.q.field_70179_y = 0.0;
               this.q.b(fp.RIDE);
            }
            break;
         case DOWNED:
            this.c.func_75499_g();
      }
   }

   @Override
   protected GirlFollowAiBase.GirlFollowAiBaseState a_clash807() {
      this.n--;
      if (!this.q.N && this.q.ae_clash498() == null) {
         if (this.a.func_184218_aH()) {
            Entity var1 = this.a.func_184187_bx();
            if (this.q.func_184218_aH() || this.q.func_184220_m(var1) || var1 instanceof EntityHorse && ((EntityHorse)var1).func_110257_ck()) {
               this.o = var1;
               return GirlFollowAiBase.GirlFollowAiBaseState.RIDE;
            }
         } else if (!this.a.func_184218_aH() && this.q.func_184218_aH() || this.f == GirlFollowAiBase.GirlFollowAiBaseState.RIDE && !this.a.func_184218_aH()) {
            this.q.b(fp.NULL);
            this.q.func_184210_p();
            this.q.field_70145_X = false;
            this.q.func_189654_d(false);
         }

         if (this.a_clash827(this.r)) {
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         DamageSource var2 = this.q.func_189748_bU();
         if (var2 != null) {
            EntityLivingBase var8 = (EntityLivingBase)var2.func_76346_g();
            if (this.a_clash827(var8)) {
               this.r = var8;
               return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
            }
         }

         EntityLivingBase var9 = this.a.func_110144_aD();
         if (this.a.field_70173_aa - this.a.func_142013_aG() < 140 && this.a_clash827(var9)) {
            this.r = var9;
            return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
         }

         if (this.f != GirlFollowAiBase.GirlFollowAiBaseState.FOLLOW) {
            var2 = this.a.func_189748_bU();
            if (var2 != null) {
               var9 = (EntityLivingBase)var2.func_76346_g();
               if (this.a_clash827(var9)) {
                  this.r = var9;
                  return GirlFollowAiBase.GirlFollowAiBaseState.ATTACK;
               }
            }

            Vec3d var3 = this.q.func_174791_d();
            AxisAlignedBB var4 = new AxisAlignedBB(
               var3.field_72450_a - 5.0,
               var3.field_72448_b - 2.0,
               var3.field_72449_c - 5.0,
               var3.field_72450_a + 5.0,
               var3.field_72448_b + 2.0,
               var3.field_72449_c + 5.0
            );
            List<EntityMob> var5 = (List<EntityMob>) (List) this.q.field_70170_p.func_72872_a(EntityMob.class, var4);
            var5.sort((var1x, var2x) -> {
               double var3x = var1x.func_70032_d(this.q);
               double var5x = var2x.func_70032_d(this.q);
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

         float var12 = this.q.func_70032_d(this.a);
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
      double var2 = this.r.field_70165_t - this.q.field_70165_t;
      double var4 = this.r.func_174813_aQ().field_72338_b + this.r.field_70131_O / 3.0F - var1.field_70163_u;
      double var6 = this.r.field_70161_v - this.q.field_70161_v;
      double var8 = MathHelper.func_76133_a(var2 * var2 + var6 * var6);
      var1.func_70186_c(var2, var4 + var8 * 0.2F, var6, 1.6F, 2.0F);
      this.q.func_184185_a(SoundEvents.field_187866_fi, 1.0F, 1.0F / (this.q.func_70681_au().nextFloat() * 0.4F + 0.8F));
      this.q.field_70170_p.func_72838_d(var1);
      var1.func_70239_b(4.5);
   }

   protected EntityArrow b_clash829() {
      EntityTippedArrow var1 = new EntityTippedArrow(this.q.field_70170_p, this.q);
      ItemStack var2 = this.q.Q.getStackInSlot(1);
      double var3 = EnchantmentHelper.func_77506_a(Enchantments.field_185309_u, var2);
      int var5 = EnchantmentHelper.func_77506_a(Enchantments.field_185310_v, var2);
      int var6 = EnchantmentHelper.func_77506_a(Enchantments.field_185311_w, var2);
      if (var3 != 0.0) {
         var1.func_70239_b(var1.func_70242_d() + var3 * 0.5 + 0.5);
      }

      if (var5 != 0) {
         var1.func_70240_a(var5);
      }

      if (var6 != 0) {
         var1.func_70015_d(100);
      }

      return var1;
   }

   void d_clash830() {
      this.q.b(fp.ATTACK);
      this.e.func_187227_b(AbstractGirlNpcEntity.M, 1);
      ItemStack var1 = this.q.Q.getStackInSlot(0);
      Multimap var2 = var1.func_111283_C(EntityEquipmentSlot.MAINHAND);
      float var3 = 0.0F;
      float var4 = 0.0F;

      for (AttributeModifier var6 : (java.util.Collection<AttributeModifier>) var2.get(SharedMonsterAttributes.field_111264_e.func_111108_a())) {
         var3 = (float)var6.func_111164_d();
      }

      for (AttributeModifier var15 : (java.util.Collection<AttributeModifier>) var2.get(SharedMonsterAttributes.field_188790_f.func_111108_a())) {
         var4 = (float)var15.func_111164_d();
      }

      var4 = Math.max(var4, 0.5F);
      float var14 = EnchantmentHelper.func_152377_a(var1, this.r.func_70668_bt());
      int var16 = EnchantmentHelper.func_77506_a(Enchantments.field_180313_o, var1);
      int var7 = EnchantmentHelper.func_77506_a(Enchantments.field_77334_n, var1);
      int var8 = EnchantmentHelper.func_77506_a(Enchantments.field_191530_r, var1);
      this.r
         .func_70653_a(
            this.q,
            var16 * 0.5F,
            MathHelper.func_76126_a(this.q.field_70177_z * (float) (Math.PI / 180.0)),
            -MathHelper.func_76134_b(this.q.field_70177_z * (float) (Math.PI / 180.0))
         );
      this.r.func_70015_d(var7 * 4);
      if (var8 != 0) {
         float var9 = 0.5F;
         if (var8 == 2) {
            var9 = 0.67F;
         } else if (var8 == 3) {
            var9 = 0.75F;
         }

         for (EntityLivingBase var11 : this.q.field_70170_p.func_72872_a(EntityLivingBase.class, this.r.func_174813_aQ().func_72314_b(1.0, 0.25, 1.0))) {
            if (var11 != this.q && var11 != this.a && var11 != this.r && !this.q.func_184191_r(var11) && this.q.func_70068_e(var11) < 9.0) {
               var11.func_70653_a(
                  this.q,
                  0.4F,
                  MathHelper.func_76126_a(this.q.field_70177_z * (float) (Math.PI / 180.0)),
                  -MathHelper.func_76134_b(this.q.field_70177_z * (float) (Math.PI / 180.0))
               );
               var11.func_70097_a(DamageSource.func_76358_a(this.q), (var3 + var14) * var9);
            }
         }
      }

      this.r.func_70097_a(DamageSource.func_76358_a(this.q), var3 + var14);
      this.k = Math.round(Math.abs(var4) / 3.373494F * 20.0F);
   }

   @Override
   protected double b_clash806() {
      double var1 = super.b_clash806();
      if (this.q.N) {
         var1 = 0.0;
      }

      this.c.func_75489_a(var1);
      this.q.a(this.q.q_clash489());
      return var1;
   }

   @Override
   public void func_75251_c() {
      super.func_75251_c();
      this.q.func_184212_Q().func_187227_b(AbstractGirlNpcEntity.M, 0);
   }

   void a_clash831() {
      if (!this.q.field_70122_E && !this.q.func_70090_H() && this.q.field_70159_w + this.q.field_70179_y == 0.0 && !(this.q.field_70181_x <= 0.0)) {
         Vec3d var1 = new Vec3d(0.0, 0.0, 0.1F);
         var1 = ck.a_clash306(var1, this.q.field_70177_z);
         this.q.field_70159_w = var1.field_72450_a;
         this.q.field_70179_y = var1.field_72449_c;
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public static class a {
      @SubscribeEvent
      public void a(LivingHurtEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.N) {
               var1.setCanceled(true);
            } else if (var2.func_110143_aJ() - var1.getAmount() < 0.0F && !((String)var2.func_184212_Q().func_187225_a(AbstractGirlNpcEntity.v)).equals("")) {
               var2.N = true;
               var2.b(fp.DOWNED);
               var1.setAmount(var2.func_110143_aJ() - 1.0F);
               var2.func_70661_as().func_75499_g();
            }
         }
      }

      @SubscribeEvent
      public void a(LivingHealEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.N && var2.func_110143_aJ() + var1.getAmount() >= var2.func_110138_aP()) {
               var2.N = false;
               var2.b(fp.NULL);
            }
         }
      }

      @SubscribeEvent
      public void a(LivingDeathEvent var1) {
         if (var1.getEntityLiving() instanceof AbstractGirlNpcEntity) {
            AbstractGirlNpcEntity var2 = (AbstractGirlNpcEntity)var1.getEntityLiving();
            if (var2.field_70170_p.field_72995_K) {
               return;
            }

            for (int var3 = 0; var3 < 6; var3++) {
               Item var4 = var2.Q.getStackInSlot(var3).func_77973_b();
               if (var4 != Items.field_190931_a) {
                  var2.func_145779_a(var4, 1);
               }
            }
         }
      }

      private static RuntimeException a(RuntimeException var0) {
         return var0;
      }
   }
}
