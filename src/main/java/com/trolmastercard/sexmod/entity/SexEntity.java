package com.trolmastercard.sexmod.entity;


import com.google.common.base.Optional;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SexEntity extends Entity {
   public static final int m = 15;
   private static final DataParameter<Integer> g = EntityDataManager.func_187226_a(SexEntity.class, DataSerializers.field_187192_b)
      .func_187156_b()
      .func_187161_a(111);
   private static final DataParameter<Optional<UUID>> f = EntityDataManager.func_187226_a(SexEntity.class, DataSerializers.field_187203_m)
      .func_187156_b()
      .func_187161_a(110);
   private boolean k;
   private int l;
   private int h;
   public int d;
   private int c;
   private int j;
   private float e;
   public Entity i;
   private SexEntity.SexEntityState n = SexEntity.SexEntityState.FLYING;
   private int a;
   private int o;
   public static LunaEntity b = null;

   public SexEntity(World var1, LunaEntity var2, double var3) {
      super(var1);
      this.a(var2);
      this.a_clash779(var3);
   }

   public SexEntity(World var1) {
      super(var1);
   }

   private void a(LunaEntity var1) {
      this.func_70105_a(0.25F, 0.25F);
      this.field_70158_ak = true;
      var1.av = this;
   }

   protected void func_70088_a() {
      this.func_184212_Q().func_187214_a(g, 0);
      this.func_184212_Q().func_187214_a(f, Optional.of(b.f_clash491()));
   }

   public AxisAlignedBB func_184177_bl() {
      return this.func_174813_aQ().func_186662_g(10.0);
   }

   LunaEntity b_clash775() {
      Optional var1 = (Optional)this.field_70180_af.func_187225_a(f);
      if (!var1.isPresent()) {
         return null;
      } else {
         BaseGirlEntity var2 = BaseGirlEntity.a_clash523((UUID)var1.get());
         if (var2 == null) {
            return null;
         } else {
            return !(var2 instanceof LunaEntity) ? null : (LunaEntity)var2;
         }
      }
   }

   public LunaEntity g_clash776() {
      Optional var1 = (Optional)this.field_70180_af.func_187225_a(f);
      if (!var1.isPresent()) {
         return null;
      }

      BaseGirlEntity var2 = BaseGirlEntity.b_clash522((UUID)var1.get());
      return !(var2 instanceof LunaEntity) ? null : (LunaEntity)var2;
   }

   public void b_clash777(int var1) {
      this.o = var1;
   }

   public void a_clash778(int var1) {
      this.a = var1;
   }

   public void func_70030_z() {
      super.func_70030_z();
      if (!this.field_70170_p.field_72995_K) {
         if ((this.i != null || this.field_70122_E) && this.d == 0) {
            this.b_clash775().o_clash390();
         }
      }
   }

   public void a_clash779(double var1) {
      LunaEntity var3 = this.b_clash775();
      if (var3 != null) {
         BlockPos var4 = var3.ai;
         float var5 = (float)Math.sqrt(var3.func_174791_d().func_186679_c(var4.func_177958_n(), var4.func_177956_o(), var4.func_177952_p()));
         float var6 = -22.5F + 45.0F * (var5 / 7.0F);
         float var7 = var3.I_clash415();
         float var8 = MathHelper.func_76134_b(-var7 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float var9 = MathHelper.func_76126_a(-var7 * (float) (Math.PI / 180.0) - (float) Math.PI);
         float var10 = -MathHelper.func_76134_b(-var6 * (float) (Math.PI / 180.0));
         float var11 = MathHelper.func_76126_a(-var6 * (float) (Math.PI / 180.0));
         double var12 = var3.field_70169_q + (var3.field_70165_t - var3.field_70169_q) - var9 * 0.3;
         double var14 = var3.field_70167_r + (var3.field_70163_u - var3.field_70167_r) + var3.func_70047_e();
         double var16 = var3.field_70166_s + (var3.field_70161_v - var3.field_70166_s) - var8 * 0.3;
         this.func_70012_b(var12, var14, var16, var7, var6);
         this.field_70159_w = var1 * -var9;
         this.field_70181_x = var1 * MathHelper.func_76131_a(-(var11 / var10), -5.0F, 5.0F);
         this.field_70179_y = var1 * -var8;
         float var18 = MathHelper.func_76133_a(
            this.field_70159_w * this.field_70159_w + this.field_70181_x * this.field_70181_x + this.field_70179_y * this.field_70179_y
         );
         this.field_70159_w = this.field_70159_w * (0.6 / var18 + 0.5 + this.field_70146_Z.nextGaussian() * 0.0045);
         this.field_70181_x = this.field_70181_x * (0.6 / var18 + 0.5 + this.field_70146_Z.nextGaussian() * 0.0045);
         this.field_70179_y = this.field_70179_y * (0.6 / var18 + 0.5 + this.field_70146_Z.nextGaussian() * 0.0045);
         float var19 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
         this.field_70177_z = (float)(MathHelper.func_181159_b(this.field_70159_w, this.field_70179_y) * (180.0 / Math.PI));
         this.field_70125_A = (float)(MathHelper.func_181159_b(this.field_70181_x, var19) * (180.0 / Math.PI));
         this.field_70126_B = this.field_70177_z;
         this.field_70127_C = this.field_70125_A;
      }
   }

   public void func_184206_a(DataParameter<?> var1) {
      if (g.equals(var1)) {
         int var2 = (Integer)this.func_184212_Q().func_187225_a(g);
         this.i = var2 > 0 ? this.field_70170_p.func_73045_a(var2 - 1) : null;
      }

      super.func_184206_a(var1);
   }

   @SideOnly(Side.CLIENT)
   public boolean func_70112_a(double var1) {
      return var1 < 4096.0;
   }

   @SideOnly(Side.CLIENT)
   public void func_180426_a(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
   }

   public void func_70071_h_() {
      super.func_70071_h_();
      if (this.b_clash775() == null) {
         this.func_70106_y();
      } else if (this.field_70170_p.field_72995_K || !this.f_clash780()) {
         if (this.k) {
            this.l++;
            if (this.l >= 1200) {
               this.func_70106_y();
               return;
            }
         }

         float var1 = 0.0F;
         BlockPos var2 = new BlockPos(this);
         IBlockState var3 = this.field_70170_p.func_180495_p(var2);
         if (var3.func_185904_a() == Material.field_151586_h) {
            var1 = BlockLiquid.func_190973_f(var3, this.field_70170_p, var2);
         }

         if (this.n == SexEntity.SexEntityState.FLYING) {
            if (this.i != null) {
               this.field_70159_w = 0.0;
               this.field_70181_x = 0.0;
               this.field_70179_y = 0.0;
               this.n = SexEntity.SexEntityState.HOOKED_IN_ENTITY;
               return;
            }

            if (var1 > 0.0F) {
               this.field_70159_w *= 0.3;
               this.field_70181_x *= 0.2;
               this.field_70179_y *= 0.3;
               this.n = SexEntity.SexEntityState.BOBBING;
               return;
            }

            if (!this.field_70170_p.field_72995_K) {
               this.e_clash782();
            }

            if (!this.k && !this.field_70122_E && !this.field_70123_F) {
               this.h++;
            } else {
               this.h = 0;
               this.field_70159_w = 0.0;
               this.field_70181_x = 0.0;
               this.field_70179_y = 0.0;
            }
         } else {
            if (this.n == SexEntity.SexEntityState.HOOKED_IN_ENTITY) {
               if (this.i != null) {
                  if (this.i.field_70128_L) {
                     this.i = null;
                     this.n = SexEntity.SexEntityState.FLYING;
                  } else {
                     this.field_70165_t = this.i.field_70165_t;
                     double var6 = this.i.field_70131_O;
                     this.field_70163_u = this.i.func_174813_aQ().field_72338_b + var6 * 0.8;
                     this.field_70161_v = this.i.field_70161_v;
                     this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
                  }
               }

               return;
            }

            if (this.n == SexEntity.SexEntityState.BOBBING) {
               this.field_70159_w *= 0.9;
               this.field_70179_y *= 0.9;
               double var4 = this.field_70163_u + this.field_70181_x - var2.func_177956_o() - var1;
               if (Math.abs(var4) < 0.01) {
                  var4 += Math.signum(var4) * 0.1;
               }

               this.field_70181_x = this.field_70181_x - var4 * this.field_70146_Z.nextFloat() * 0.2;
               if (!this.field_70170_p.field_72995_K && var1 > 0.0F) {
                  this.a_clash784(var2);
               }
            }
         }

         if (var3.func_185904_a() != Material.field_151586_h) {
            this.field_70181_x -= 0.03;
         }

         this.func_70091_d(MoverType.SELF, this.field_70159_w, this.field_70181_x, this.field_70179_y);
         this.h_clash781();
         this.field_70159_w *= 0.92;
         this.field_70181_x *= 0.92;
         this.field_70179_y *= 0.92;
         this.func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      }
   }

   private boolean f_clash780() {
      return false;
   }

   private void h_clash781() {
      float var1 = MathHelper.func_76133_a(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
      this.field_70177_z = (float)(MathHelper.func_181159_b(this.field_70159_w, this.field_70179_y) * (180.0 / Math.PI));
      this.field_70125_A = (float)(MathHelper.func_181159_b(this.field_70181_x, var1) * (180.0 / Math.PI));

      while (this.field_70125_A - this.field_70127_C < -180.0F) {
         this.field_70127_C -= 360.0F;
      }

      while (this.field_70125_A - this.field_70127_C >= 180.0F) {
         this.field_70127_C += 360.0F;
      }

      while (this.field_70177_z - this.field_70126_B < -180.0F) {
         this.field_70126_B -= 360.0F;
      }

      while (this.field_70177_z - this.field_70126_B >= 180.0F) {
         this.field_70126_B += 360.0F;
      }

      this.field_70125_A = this.field_70127_C + (this.field_70125_A - this.field_70127_C) * 0.2F;
      this.field_70177_z = this.field_70126_B + (this.field_70177_z - this.field_70126_B) * 0.2F;
   }

   private void e_clash782() {
      Vec3d var1 = new Vec3d(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      Vec3d var2 = new Vec3d(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
      RayTraceResult var3 = this.field_70170_p.func_147447_a(var1, var2, false, true, false);
      var1 = new Vec3d(this.field_70165_t, this.field_70163_u, this.field_70161_v);
      var2 = new Vec3d(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
      if (var3 != null) {
         var2 = new Vec3d(var3.field_72307_f.field_72450_a, var3.field_72307_f.field_72448_b, var3.field_72307_f.field_72449_c);
      }

      Entity var4 = null;
      List var5 = this.field_70170_p
         .func_72839_b(this, this.func_174813_aQ().func_72321_a(this.field_70159_w, this.field_70181_x, this.field_70179_y).func_186662_g(1.0));
      double var6 = 0.0;

      for (Entity var9 : (java.util.Collection<Entity>) (var5) ) {
         if (this.a_clash785(var9) && (var9 != this.b_clash775() || this.h >= 5)) {
            AxisAlignedBB var10 = var9.func_174813_aQ().func_186662_g(0.3F);
            RayTraceResult var11 = var10.func_72327_a(var1, var2);
            if (var11 != null) {
               double var12 = var1.func_72436_e(var11.field_72307_f);
               if (var12 < var6 || var6 == 0.0) {
                  var4 = var9;
                  var6 = var12;
               }
            }
         }
      }

      if (var4 != null) {
         var3 = new RayTraceResult(var4);
      }

      if (var3 != null && var3.field_72313_a != Type.MISS) {
         if (var3.field_72313_a == Type.ENTITY) {
            this.i = var3.field_72308_g;
            this.a_clash783();
         } else {
            this.k = true;
         }
      }
   }

   private void a_clash783() {
      this.func_184212_Q().func_187227_b(g, this.i.func_145782_y() + 1);
   }

   private void a_clash784(BlockPos var1) {
      WorldServer var2 = (WorldServer)this.field_70170_p;
      int var3 = 1;
      BlockPos var4 = var1.func_177984_a();
      if (this.field_70146_Z.nextFloat() < 0.25F && this.field_70170_p.func_175727_C(var4)) {
         var3++;
      }

      if (this.field_70146_Z.nextFloat() < 0.5F && !this.field_70170_p.func_175678_i(var4)) {
         var3--;
      }

      if (this.d > 0) {
         this.d--;
         if (this.d <= 0) {
            this.c = 0;
            this.j = 0;
         } else {
            this.field_70181_x = this.field_70181_x - 0.2 * this.field_70146_Z.nextFloat() * this.field_70146_Z.nextFloat();
         }
      } else if (this.j > 0) {
         this.j -= var3;
         if (this.j > 0) {
            this.e = (float)(this.e + this.field_70146_Z.nextGaussian() * 4.0);
            float var5 = this.e * (float) (Math.PI / 180.0);
            float var6 = MathHelper.func_76126_a(var5);
            float var7 = MathHelper.func_76134_b(var5);
            double var8 = this.field_70165_t + var6 * this.j * 0.1F;
            double var10 = MathHelper.func_76128_c(this.func_174813_aQ().field_72338_b) + 1.0F;
            double var12 = this.field_70161_v + var7 * this.j * 0.1F;
            IBlockState var14 = var2.func_180495_p(new BlockPos(var8, var10 - 1.0, var12));
            if (var14.func_185904_a() == Material.field_151586_h) {
               if (this.field_70146_Z.nextFloat() < 0.15F) {
                  var2.func_175739_a(EnumParticleTypes.WATER_BUBBLE, var8, var10 - 0.1F, var12, 1, var6, 0.1, var7, 0.0, new int[0]);
               }

               float var15 = var6 * 0.04F;
               float var16 = var7 * 0.04F;
               var2.func_175739_a(EnumParticleTypes.WATER_WAKE, var8, var10, var12, 0, var16, 0.01, -var15, 1.0, new int[0]);
               var2.func_175739_a(EnumParticleTypes.WATER_WAKE, var8, var10, var12, 0, -var16, 0.01, var15, 1.0, new int[0]);
            }
         } else {
            this.field_70181_x = -0.4F * MathHelper.func_151240_a(this.field_70146_Z, 0.6F, 1.0F);
            this.func_184185_a(SoundEvents.field_187609_F, 0.25F, 1.0F + (this.field_70146_Z.nextFloat() - this.field_70146_Z.nextFloat()) * 0.4F);
            double var17 = this.func_174813_aQ().field_72338_b + 0.5;
            var2.func_175739_a(
               EnumParticleTypes.WATER_BUBBLE,
               this.field_70165_t,
               var17,
               this.field_70161_v,
               (int)(1.0F + this.field_70130_N * 20.0F),
               this.field_70130_N,
               0.0,
               this.field_70130_N,
               0.2F,
               new int[0]
            );
            var2.func_175739_a(
               EnumParticleTypes.WATER_WAKE,
               this.field_70165_t,
               var17,
               this.field_70161_v,
               (int)(1.0F + this.field_70130_N * 20.0F),
               this.field_70130_N,
               0.0,
               this.field_70130_N,
               0.2F,
               new int[0]
            );
            this.d = MathHelper.func_76136_a(this.field_70146_Z, 20, 40);
         }
      } else if (this.c > 0) {
         this.c -= var3;
         float var18 = 0.15F;
         if (this.c < 20) {
            var18 = (float)(0.15F + (20 - this.c) * 0.05);
         } else if (this.c < 40) {
            var18 = (float)(0.15F + (40 - this.c) * 0.02);
         } else if (this.c < 60) {
            var18 = (float)(0.15F + (60 - this.c) * 0.01);
         }

         if (this.field_70146_Z.nextFloat() < var18) {
            float var19 = MathHelper.func_151240_a(this.field_70146_Z, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
            float var20 = MathHelper.func_151240_a(this.field_70146_Z, 25.0F, 60.0F);
            double var21 = this.field_70165_t + MathHelper.func_76126_a(var19) * var20 * 0.1F;
            double var22 = MathHelper.func_76128_c(this.func_174813_aQ().field_72338_b) + 1.0F;
            double var23 = this.field_70161_v + MathHelper.func_76134_b(var19) * var20 * 0.1F;
            IBlockState var24 = var2.func_180495_p(new BlockPos((int)var21, (int)var22 - 1, (int)var23));
            if (var24.func_185904_a() == Material.field_151586_h) {
               var2.func_175739_a(EnumParticleTypes.WATER_SPLASH, var21, var22, var23, 2 + this.field_70146_Z.nextInt(2), 0.1F, 0.0, 0.1F, 0.0, new int[0]);
            }
         }

         if (this.c <= 0) {
            this.e = MathHelper.func_151240_a(this.field_70146_Z, 0.0F, 360.0F);
            this.j = MathHelper.func_76136_a(this.field_70146_Z, 20, 80);
         }
      } else {
         this.c = MathHelper.func_76136_a(this.field_70146_Z, 100, 600);
         this.c = this.c - this.o * 20 * 5;
      }
   }

   protected boolean a_clash785(Entity var1) {
      return var1.func_70067_L() || var1 instanceof EntityItem;
   }

   public void func_70014_b(NBTTagCompound var1) {
   }

   public void func_70037_a(NBTTagCompound var1) {
   }

   public int c_clash786() {
      if (!this.field_70170_p.field_72995_K && this.b_clash775() != null) {
         byte var1 = 0;
         if (this.i != null) {
            this.d_clash787();
            this.field_70170_p.func_72960_a(this, (byte)31);
            var1 = (byte)(this.i instanceof EntityItem ? 3 : 5);
         } else if (this.d > 0) {
            Builder var3 = new Builder((WorldServer)this.field_70170_p);

            for (ItemStack var6 : this.field_70170_p
               .func_184146_ak()
               .func_186521_a(LootTableList.field_186387_al)
               .func_186462_a(this.field_70146_Z, var3.func_186471_a())) {
               LunaEntity var7 = this.b_clash775();
               var7.b_clash383(var6);
            }

            this.d = 9999;
            var1 = 1;
         }

         if (this.k) {
            var1 = 2;
         }

         return var1;
      } else {
         return 0;
      }
   }

   protected void d_clash787() {
      LunaEntity var1 = this.b_clash775();
      if (var1 != null) {
         double var2 = var1.field_70165_t - this.field_70165_t;
         double var4 = var1.field_70163_u - this.field_70163_u;
         double var6 = var1.field_70161_v - this.field_70161_v;
         this.i.field_70159_w += var2 * 0.1;
         this.i.field_70181_x += var4 * 0.1;
         this.i.field_70179_y += var6 * 0.1;
      }
   }

   protected boolean func_70041_e_() {
      return false;
   }

   public void func_70020_e(NBTTagCompound var1) {
   }

   public NBTTagCompound func_189511_e(NBTTagCompound var1) {
      return null;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   enum SexEntityState {
      FLYING,
      HOOKED_IN_ENTITY,
      BOBBING;
   }
}
