package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SpawnEnergyBallParticlesPacket2;
import com.trolmastercard.sexmod.util.Reference;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.gc;







import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DragonEntity extends EntityLiving {
   public static final float d = 0.4F;
   public static final float e = 0.3F;
   static final int b = 200;
   static final int k = 100;
   static final float a = 0.5F;
   static final float l = 0.15F;
   public static final float j = 0.75F;
   public double g = 1.0;
   Vec3d h = Vec3d.field_186680_a;
   boolean c = false;
   boolean i = true;
   GalathEntity f;

   public DragonEntity(World var1) {
      super(var1);
      this.func_70105_a(0.5F, 0.5F);
   }

   public DragonEntity(World var1, GalathEntity var2) {
      super(var1);
      this.func_70105_a(0.5F, 0.5F);
      this.f = var2;
   }

   public DragonEntity(World var1, GalathEntity var2, Vec3d var3) {
      this(var1);
      this.h = var3;
      this.f = var2;
   }

   protected boolean func_70041_e_() {
      return false;
   }

   protected void func_82167_n(Entity var1) {
   }

   public void func_70071_h_() {
      if (!this.field_70128_L) {
         this.field_70145_X = true;
         this.func_189654_d(true);
         this.field_70159_w = this.h.field_72450_a;
         this.field_70181_x = this.h.field_72448_b;
         this.field_70179_y = this.h.field_72449_c;
         super.func_70071_h_();
         if (this.field_70170_p.field_72995_K) {
            this.a_clash114();
         }

         this.c_clash113();
         if (!this.field_70170_p.func_175623_d(this.func_180425_c())) {
            this.b_clash116();
            this.field_70170_p.func_72900_e(this);
         }
      }
   }

   void c_clash113() {
      if (!this.field_70170_p.field_72995_K) {
         if (this.c) {
            Vec3d var1 = this.func_174791_d();
            Vec3d var2 = var1.func_178786_a(0.75, 0.75, 0.75);
            Vec3d var3 = var1.func_72441_c(0.75, 0.75, 0.75);
            AxisAlignedBB var4 = new AxisAlignedBB(
               var2.field_72450_a, var2.field_72448_b, var2.field_72449_c, var3.field_72450_a, var3.field_72448_b, var3.field_72449_c
            );
            List var5 = this.field_70170_p.func_72872_a(GalathEntity.class, var4);
            if (!var5.isEmpty()) {
               this.field_70170_p.func_72876_a(this, this.field_70165_t, this.field_70163_u, this.field_70161_v, 1.0F, true);

               for (GalathEntity var7 : (java.util.Collection<GalathEntity>) (var5) ) {
                  var7.f(this.func_174791_d());
               }

               this.field_70170_p.func_72900_e(this);
            }
         }
      }
   }

   void a_clash114() {
      this.a_clash115(
         RotationHelper.b(this.field_70142_S, this.field_70165_t, 0.5),
         RotationHelper.b(this.field_70137_T, this.field_70163_u, 0.5),
         RotationHelper.b(this.field_70136_U, this.field_70161_v, 0.5)
      );
      this.a_clash115(this.field_70165_t, this.field_70163_u, this.field_70161_v);
   }

   void a_clash115(double var1, double var3, double var5) {
      Random var7 = this.func_70681_au();
      this.field_70170_p
         .func_175688_a(
            EnumParticleTypes.DRAGON_BREATH,
            var1 + var7.nextDouble() * 0.3F,
            var3 + 0.25 + var7.nextDouble() * 0.3F,
            var5 + var7.nextDouble() * 0.3F,
            0.0,
            0.0,
            0.0,
            new int[0]
         );
   }

   void b_clash116() {
      if (!this.field_70170_p.field_72995_K) {
         if (!this.field_70128_L) {
            if (this.i) {
               Vec3d var1 = new Vec3d(this.field_70165_t, this.func_180425_c().func_177956_o() + 1, this.field_70161_v);
               if (!this.b_clash117(var1)) {
                  this.field_70170_p.func_72876_a(this, this.field_70165_t, this.field_70163_u, this.field_70161_v, 2.0F, true);
                  this.i = false;
               } else {
                  EntityWitherSkeleton var2 = new EntityWitherSkeleton(this.field_70170_p);
                  var2.func_184611_a(EnumHand.MAIN_HAND, new ItemStack(Items.field_151052_q));
                  var2.func_70634_a(var1.field_72450_a, var1.field_72448_b, var1.field_72449_c);
                  this.field_70170_p.func_72838_d(var2);
                  PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(var1, true), this);
                  this.f.bI.add(var2);
               }
            }
         }
      }
   }

   boolean b_clash117(Vec3d var1) {
      if (this.f == null) {
         return true;
      }

      EntityLivingBase var2 = this.f.M_clash691();
      return var2 == null ? true : var2.func_70011_f(var1.field_72450_a, var1.field_72448_b, var1.field_72449_c) < 15.0;
   }

   @SideOnly(Side.CLIENT)
   public static void a_clash118(Vec3d var0) {
      WorldClient var1 = Minecraft.func_71410_x().field_71441_e;
      float var2 = gc.wrapDegrees(1.8F);
      Random var3 = Reference.f;

      for (float var4 = 0.0F; var4 < Math.PI * 2; var4 += var2) {
         double var5 = Math.sin(var4);
         double var7 = Math.cos(var4);
         double var9 = var0.field_72450_a + var5 * 0.5;
         double var11 = var5 * 0.15F;
         double var13 = var0.field_72449_c + var7 * 0.5;
         double var15 = var7 * 0.15F;
         double var17 = var0.field_72448_b;
         double var19 = var3.nextDouble() * 0.15F;
         var1.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, var9, var17, var13, var11, var19, var15, new int[0]);
      }
   }

   @SideOnly(Side.CLIENT)
   public static void c_clash119(Vec3d var0) {
      WorldClient var1 = Minecraft.func_71410_x().field_71441_e;
      Random var2 = Reference.f;

      for (int var3 = 0; var3 < 100; var3++) {
         var1.func_175688_a(
            EnumParticleTypes.DRAGON_BREATH,
            var0.field_72450_a,
            var0.field_72448_b,
            var0.field_72449_c,
            var2.nextDouble() * 0.15F,
            var2.nextDouble() * 0.15F,
            var2.nextDouble() * 0.15F,
            new int[0]
         );
      }

      var1.func_184134_a(var0.field_72450_a, var0.field_72448_b, var0.field_72449_c, SoundHandler.MISC_SHATTER[0], SoundCategory.AMBIENT, 0.7F, 1.0F, false);
   }

   public boolean func_70097_a(DamageSource var1, float var2) {
      if (DamageSource.field_76380_i.equals(var1)) {
         this.func_70606_j(0.0F);
         this.i = false;
         this.field_70170_p.func_72900_e(this);
         return true;
      }

      if (!this.field_70170_p.field_72995_K && "arrow".equals(var1.field_76373_n)) {
         this.func_70606_j(0.0F);
         this.i = false;
         PacketHandler.b.sendToAllTracking(new SpawnEnergyBallParticlesPacket2(this.func_174791_d(), false), this);
         Entity var4 = var1.func_76364_f();
         if (var4 != null) {
            this.field_70170_p.func_72900_e(var4);
         }

         this.field_70170_p.func_72900_e(this);
         return true;
      } else {
         Entity var3 = var1.func_76346_g();
         if (!(var3 instanceof EntityPlayer)) {
            return false;
         }

         this.h = var3.func_70040_Z();
         this.c = true;
         return true;
      }
   }

   public void func_70037_a(NBTTagCompound var1) {
      this.field_70170_p.func_72900_e(this);
   }

}
