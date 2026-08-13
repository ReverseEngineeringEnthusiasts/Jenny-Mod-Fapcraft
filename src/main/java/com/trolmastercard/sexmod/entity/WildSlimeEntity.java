package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.Reference;







import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityMoveHelper;
import net.minecraft.entity.ai.EntityMoveHelper.Action;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class WildSlimeEntity extends EntityLiving {
   public static List<WildSlimeEntity> g = new ArrayList<>();
   private static final DataParameter<Integer> d = EntityDataManager.func_187226_a(WildSlimeEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(111);
   private static final DataParameter<Integer> c = EntityDataManager.func_187226_a(WildSlimeEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(110);
   public float a;
   public float e;
   public float h;
   private boolean f;

   public WildSlimeEntity(World var1) {
      super(var1);
      this.field_70765_h = new WildSlimeEntity.b(this);
   }

   protected void func_184651_r() {
      this.field_70714_bg.func_75776_a(1, new WildSlimeEntity.d(this));
      this.field_70714_bg.func_75776_a(5, new WildSlimeEntity.c(this));
   }

   protected void func_70088_a() {
      super.func_70088_a();
      this.field_70180_af.func_187214_a(c, 1);
      this.field_70180_af.func_187214_a(d, 0);
   }

   public void func_180430_e(float var1, float var2) {
   }

   protected boolean func_70692_ba() {
      return false;
   }

   protected void a(int var1, boolean var2) {
      this.field_70180_af.func_187227_b(c, var1);
      this.func_70105_a(0.51000005F * var1, 0.51000005F * var1);
      this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      this.func_110148_a(SharedMonsterAttributes.field_111267_a).func_111128_a(var1 * var1);
      this.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111128_a(0.2F + 0.1F * var1);
      if (var2) {
         this.func_70606_j(this.func_110138_aP());
      }

      this.field_70728_aV = var1;
   }

   public int getSquishFactor() {
      return (Integer)this.field_70180_af.func_187225_a(c);
   }

   public static void a(DataFixer var0) {
      EntityLiving.func_189752_a(var0, WildSlimeEntity.class);
   }

   public void func_70014_b(NBTTagCompound var1) {
      super.func_70014_b(var1);
      var1.func_74768_a("Size", this.getSquishFactor() - 1);
      var1.func_74757_a("wasOnGround", this.f);
      var1.func_74768_a("ageInTicks", (Integer)this.field_70180_af.func_187225_a(d));
   }

   public void func_70037_a(NBTTagCompound var1) {
      super.func_70037_a(var1);
      int var2 = var1.func_74762_e("Size");
      if (var2 < 0) {
         var2 = 0;
      }

      this.a(var2 + 1, false);
      this.f = var1.func_74767_n("wasOnGround");
      this.field_70180_af.func_187227_b(d, var1.func_74762_e("ageInTicks"));
   }

   public boolean j_clash93() {
      return this.getSquishFactor() <= 1;
   }

   protected EnumParticleTypes g_clash94() {
      return EnumParticleTypes.SLIME;
   }

   public static ArrayList<WildSlimeEntity> a_clash95(Vec3d var0) {
      ArrayList var1 = a_clash96(var0, 0.1);
      if (var1.isEmpty()) {
         var1 = a_clash96(var0, 0.5);
      }

      return var1;
   }

   private static ArrayList<WildSlimeEntity> a_clash96(Vec3d var0, double var1) {
      ArrayList var3 = new ArrayList();

      try {
         for (WildSlimeEntity var5 : g) {
            if (var5 != null) {
               double var6 = Math.abs(var5.field_70169_q - var0.field_72450_a)
                  + Math.abs(var5.field_70167_r - var0.field_72448_b)
                  + Math.abs(var5.field_70166_s - var0.field_72449_c);
               if (var5.field_70170_p != null && var6 < var1) {
                  var3.add(var5);
               }
            }
         }
      } catch (Exception var8) {
         System.out.println("couldnt find slimes at distance " + var1);
      }

      return var3;
   }

   public Vec3d e_clash97() {
      return new Vec3d(this.field_70169_q, this.field_70167_r, this.field_70166_s);
   }

   void a(EnumParticleTypes var1) {
      double var2 = Reference.f.nextGaussian() * 0.02;
      double var4 = Reference.f.nextGaussian() * 0.02;
      double var6 = Reference.f.nextGaussian() * 0.02;
      this.field_70170_p
         .func_175688_a(
            var1,
            this.field_70165_t + Reference.f.nextFloat() * this.field_70130_N * 2.0F - this.field_70130_N,
            this.field_70163_u + 0.15 + Reference.f.nextFloat() * this.field_70131_O,
            this.field_70161_v + Reference.f.nextFloat() * this.field_70130_N * 2.0F - this.field_70130_N,
            var2,
            var4,
            var6,
            new int[0]
         );
   }

   public void func_70071_h_() {
      this.field_70180_af.func_187227_b(d, (Integer)this.field_70180_af.func_187225_a(d) + 1);
      if (this.field_70170_p.field_72995_K) {
         if (((Integer)this.field_70180_af.func_187225_a(d)).intValue() > 7980.0) {
            this.a(EnumParticleTypes.CLOUD);
         } else if (((Integer)this.field_70180_af.func_187225_a(d)).intValue() > 5880.0 && this.field_70173_aa % 10 == 0) {
            this.a(EnumParticleTypes.VILLAGER_HAPPY);
         }
      } else if ((Integer)this.field_70180_af.func_187225_a(d) > 8400) {
         SlimeEntity var1 = new SlimeEntity(this.field_70170_p);
         var1.func_70080_a(this.field_70165_t, this.field_70163_u, this.field_70161_v, this.field_70177_z, this.field_70125_A);
         this.field_70170_p.func_72838_d(var1);
         var1.a(SoundEvents.field_187604_bf);
         this.field_70170_p.func_72900_e(this);
      }

      this.e = this.e + (this.a - this.e) * 0.5F;
      this.h = this.e;
      super.func_70071_h_();
      if (this.field_70122_E && !this.f) {
         int var13 = this.getSquishFactor();
         if (this.k_clash104()) {
            var13 = 0;
         }

         for (int var2 = 0; var2 < var13 * 8; var2++) {
            float var3 = this.field_70146_Z.nextFloat() * (float) (Math.PI * 2);
            float var4 = this.field_70146_Z.nextFloat() * 0.5F + 0.5F;
            float var5 = MathHelper.func_76126_a(var3) * var13 * 0.5F * var4;
            float var6 = MathHelper.func_76134_b(var3) * var13 * 0.5F * var4;
            World var7 = this.field_70170_p;
            EnumParticleTypes var8 = this.g_clash94();
            double var9 = this.field_70165_t + var5;
            double var11 = this.field_70161_v + var6;
            var7.func_175688_a(var8, var9, this.func_174813_aQ().field_72338_b, var11, 0.0, 0.0, 0.0, new int[0]);
         }

         this.func_184185_a(this.f_clash101(), this.func_70599_aP(), ((this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.2F + 1.0F) / 0.8F);
         this.a = -0.5F;
      } else if (!this.field_70122_E && this.f) {
         this.a = 1.0F;
      }

      this.f = this.field_70122_E;
      this.b_clash98();
   }

   protected void b_clash98() {
      this.a *= 0.6F;
   }

   protected int a_clash99() {
      return this.field_70146_Z.nextInt(100) + 50;
   }

   protected WildSlimeEntity d_clash100() {
      return new WildSlimeEntity(this.field_70170_p);
   }

   public void func_184206_a(DataParameter<?> var1) {
      if (c.equals(var1)) {
         int var2 = this.getSquishFactor();
         this.func_70105_a(0.51000005F * var2, 0.51000005F * var2);
         this.field_70177_z = this.field_70759_as;
         this.field_70761_aq = this.field_70759_as;
         if (this.func_70090_H() && this.field_70146_Z.nextInt(20) == 0) {
            this.func_71061_d_();
         }
      }

      super.func_184206_a(var1);
   }

   public void func_70106_y() {
      int var1 = this.getSquishFactor();
      if (!this.field_70170_p.field_72995_K && var1 > 1 && this.func_110143_aJ() <= 0.0F) {
         int var2 = 2 + this.field_70146_Z.nextInt(3);

         for (int var3 = 0; var3 < var2; var3++) {
            float var4 = (var3 % 2 - 0.5F) * var1 / 4.0F;
            float var5 = (var3 / 2 - 0.5F) * var1 / 4.0F;
            WildSlimeEntity var6 = this.d_clash100();
            if (this.func_145818_k_()) {
               var6.func_96094_a(this.func_95999_t());
            }

            if (this.func_104002_bU()) {
               var6.func_110163_bv();
            }

            var6.a(var1 / 2, true);
            var6.func_70012_b(this.field_70165_t + var4, this.field_70163_u + 0.5, this.field_70161_v + var5, this.field_70146_Z.nextFloat() * 360.0F, 0.0F);
            this.field_70170_p.func_72838_d(var6);
         }
      }

      super.func_70106_y();
   }

   public float func_70047_e() {
      return 0.625F * this.field_70131_O;
   }

   protected SoundEvent func_184601_bQ(DamageSource var1) {
      return this.j_clash93() ? SoundEvents.field_187898_fy : SoundEvents.field_187880_fp;
   }

   protected SoundEvent func_184615_bR() {
      return this.j_clash93() ? SoundEvents.field_187896_fx : SoundEvents.field_187874_fm;
   }

   protected SoundEvent f_clash101() {
      return this.j_clash93() ? SoundEvents.field_187900_fz : SoundEvents.field_187886_fs;
   }

   protected Item func_146068_u() {
      return this.getSquishFactor() == 1 ? Items.field_151123_aH : null;
   }

   @Nullable
   protected ResourceLocation func_184647_J() {
      return this.getSquishFactor() == 1 ? LootTableList.field_186378_ac : LootTableList.field_186419_a;
   }

   protected float func_70599_aP() {
      return 0.4F * this.getSquishFactor();
   }

   public int func_70646_bf() {
      return 0;
   }

   protected boolean i_clash102() {
      return this.getSquishFactor() > 0;
   }

   protected void func_70664_aZ() {
      this.field_70181_x = 0.42F;
      this.field_70160_al = true;
   }

   @Nullable
   public IEntityLivingData func_180482_a(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      this.a(1, true);
      return super.func_180482_a(var1, var2);
   }

   protected SoundEvent c_clash103() {
      return this.j_clash93() ? SoundEvents.field_189110_fE : SoundEvents.field_187882_fq;
   }

   protected boolean k_clash104() {
      return false;
   }


   static class a extends EntityAIBase {
      private final WildSlimeEntity b;
      private float a;
      private int c;

      public a(WildSlimeEntity var1) {
         this.b = var1;
         this.func_75248_a(2);
      }

      public boolean func_75250_a() {
         return this.b.func_70638_az() == null
            && (this.b.field_70122_E || this.b.func_70090_H() || this.b.func_180799_ab() || this.b.func_70644_a(MobEffects.field_188424_y));
      }

      public void func_75246_d() {
         if (--this.c <= 0) {
            this.c = 40 + this.b.func_70681_au().nextInt(60);
            this.a = this.b.func_70681_au().nextInt(360);
         }

         ((WildSlimeEntity.b)this.b.func_70605_aq()).a_clash0(this.a, false);
      }

   }

   static class b extends EntityMoveHelper {
      private float b;
      private int c;
      private final WildSlimeEntity d;
      private boolean a;

      public b(WildSlimeEntity var1) {
         super(var1);
         this.d = var1;
         this.b = 180.0F * var1.field_70177_z / (float) Math.PI;
      }

      public void a_clash0(float var1, boolean var2) {
         this.b = var1;
         this.a = var2;
      }

      public void a_clash1(double var1) {
         this.field_75645_e = var1;
         this.field_188491_h = Action.MOVE_TO;
      }

      public void func_75641_c() {
         this.field_75648_a.field_70177_z = this.func_75639_a(this.field_75648_a.field_70177_z, this.b, 90.0F);
         this.field_75648_a.field_70759_as = this.field_75648_a.field_70177_z;
         this.field_75648_a.field_70761_aq = this.field_75648_a.field_70177_z;
         if (this.field_188491_h != Action.MOVE_TO) {
            this.field_75648_a.func_191989_p(0.0F);
         } else {
            this.field_188491_h = Action.WAIT;
            if (this.field_75648_a.field_70122_E) {
               this.field_75648_a
                  .func_70659_e((float)(this.field_75645_e * this.field_75648_a.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111126_e()));
               if (this.c-- <= 0) {
                  this.c = this.d.a_clash99();
                  if (this.a) {
                     this.c /= 3;
                  }

                  float var1 = Reference.f.nextInt(360);
                  ((WildSlimeEntity.b)this.d.func_70605_aq()).a_clash0(var1, false);
                  this.d.func_70683_ar().func_75660_a();
                  if (this.d.i_clash102()) {
                     this.d
                        .func_184185_a(
                           this.d.c_clash103(),
                           this.d.func_70599_aP(),
                           ((this.d.func_70681_au().nextFloat() - this.d.func_70681_au().nextFloat()) * 0.2F + 1.0F) * 0.8F
                        );
                  }
               } else {
                  this.d.field_70702_br = 0.0F;
                  this.d.field_191988_bg = 0.0F;
                  this.field_75648_a.func_70659_e(0.0F);
               }
            } else {
               this.field_75648_a
                  .func_70659_e((float)(this.field_75645_e * this.field_75648_a.func_110148_a(SharedMonsterAttributes.field_111263_d).func_111126_e()));
            }
         }
      }

   }

   static class c extends EntityAIBase {
      private final WildSlimeEntity a;

      public c(WildSlimeEntity var1) {
         this.a = var1;
         this.func_75248_a(5);
      }

      public boolean func_75250_a() {
         return true;
      }

      public void func_75246_d() {
         ((WildSlimeEntity.b)this.a.func_70605_aq()).a_clash1(1.0);
      }
   }

   static class d extends EntityAIBase {
      private final WildSlimeEntity a;

      public d(WildSlimeEntity var1) {
         this.a = var1;
         this.func_75248_a(5);
         ((PathNavigateGround)var1.func_70661_as()).func_179693_d(true);
      }

      public boolean func_75250_a() {
         return this.a.func_70090_H() || this.a.func_180799_ab();
      }

      public void func_75246_d() {
         if (this.a.func_70681_au().nextFloat() < 0.8F) {
            this.a.func_70683_ar().func_75660_a();
         }

         ((WildSlimeEntity.b)this.a.func_70605_aq()).a_clash1(1.2);
      }

   }
}
