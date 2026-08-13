package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.cj;







import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BasicGirlEntity extends EntityLiving {
   public static final long b = 60000L;
   public static final float g = 3.0F;
   static final float c = 30.0F;
   static final int h = 175;
   static final int i = 10;
   BlockPos f = null;
   int d = 0;
   boolean e = false;
   public int a = -1;

   public BasicGirlEntity(World var1) {
      super(var1);
   }

   protected void func_70619_bc() {
      super.func_70619_bc();
      this.a_clash47();
   }

   void a_clash47() {
      if (this.e) {
         this.func_70661_as().func_75499_g();
      } else {
         EntityPlayer var1 = this.field_70170_p.func_72890_a(this, 15.0);
         if (var1 != null && var1.func_70032_d(this) < 3.0F) {
            this.func_70661_as().func_75499_g();
         } else {
            if (this.f == null || this.func_70011_f(this.f.func_177958_n(), this.f.func_177956_o(), this.f.func_177952_p()) > this.c_clash50() || this.d > 175) {
               int var2 = (this.func_70681_au().nextBoolean() ? 1 : -1) * this.func_70681_au().nextInt(10);
               int var3 = (this.func_70681_au().nextBoolean() ? 1 : -1) * this.func_70681_au().nextInt(10);
               int var4 = this.field_70170_p.field_73011_w.func_186058_p() == DimensionType.NETHER
                  ? (int)Math.ceil(this.field_70163_u)
                  : cj.a(this.field_70170_p, this.func_180425_c().func_177958_n() + var2, this.func_180425_c().func_177952_p() + var3);
               this.f = new BlockPos(this.func_180425_c().func_177958_n() + var2, var4, this.func_180425_c().func_177952_p() + var3);
               this.d = 0;
            }

            if (Math.sqrt(this.f.func_177951_i(this.func_180425_c())) > 2.0) {
               this.func_70661_as().func_75492_a(this.f.func_177958_n(), this.f.func_177956_o(), this.f.func_177952_p(), 0.35F);
               this.d_clash48();
            } else {
               this.d++;
            }
         }
      }
   }

   protected void d_clash48() {
      Path var1 = this.func_70661_as().func_75505_d();
      if (var1 != null) {
         if (!this.field_70122_E && !this.func_70090_H()) {
            int var2 = var1.func_75873_e();
            int var3 = var1.func_75874_d();
            if (var3 != var2 && var3 - 1 != var2) {
               PathPoint var4 = var1.func_75877_a(var2);
               PathPoint var5 = var1.func_75877_a(var2 + 1);
               Vec3d var6 = new Vec3d(var5.field_75839_a - var4.field_75839_a, var5.field_75837_b - var4.field_75837_b, var5.field_75838_c - var4.field_75838_c);
               this.field_70159_w = var6.field_72450_a / 7.0;
               this.field_70179_y = var6.field_72449_c / 7.0;
            }
         }
      }
   }

   public boolean func_70097_a(DamageSource var1, float var2) {
      if (var1 == DamageSource.field_76380_i) {
         this.field_70170_p.func_72900_e(this);
         return true;
      }

      if (!(var1.func_76346_g() instanceof EntityPlayer)) {
         return false;
      }

      if (this.field_70170_p.field_72995_K) {
         this.b_clash49();
      }

      this.e = true;
      ThreadNames.a(6250, () -> this.field_70170_p.func_72900_e(this));
      return false;
   }

   @SideOnly(Side.CLIENT)
   void b_clash49() {
      EntityPlayerSP var1 = Minecraft.func_71410_x().field_71439_g;
      this.a = var1.field_70173_aa;
      var1.func_184185_a(SoundHandler.MISC_WEOWEO[3], 1.0F, 1.0F);
   }

   double c_clash50() {
      return Math.sqrt(1800.0);
   }

   public boolean func_70601_bi() {
      if (this.func_70681_au().nextInt(100) < 1 && this.func_70681_au().nextInt(100) < 10) {
         return true;
      }

      this.field_70170_p.func_72900_e(this);
      return false;
   }

}
