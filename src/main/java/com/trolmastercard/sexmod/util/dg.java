package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GoblinRenderer;
import com.trolmastercard.sexmod.entity.AbstractGirlNpcEntity;
import com.trolmastercard.sexmod.entity.AbstractNpcOnlyEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.entity.GoblinPlayerEntity;
import com.trolmastercard.sexmod.entity.fp;







import java.util.HashSet;
import java.util.UUID;
import javax.annotation.Nullable;
import javax.vecmath.Vector4f;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class dg extends d9 {
   GoblinPlayerEntity B = null;
   boolean C = false;
   boolean E = false;
   boolean D = false;

   public dg(RenderManager var1, AnimatedGeoModel var2) {
      super(var1, var2);
   }

   @Override
   protected Vec3i a_clash192(String var1) {
      String[] var2 = AbstractNpcOnlyEntity.a_clash225(this.j);
      if (var2.length < 8) {
         return z;
      } else if (var1.contains("band")) {
         return GoblinRenderer.w;
      } else if (var1.contains("eyeColor") || var1.contains("eyeColor2")) {
         return GoblinRenderer.b_clash401(var2[8]);
      } else if (var1.contains("variant") || var1.contains("boob")) {
         return GoblinRenderer.c_clash402(var2[7]);
      } else if (var1.contains("hair")) {
         return GoblinRenderer.d_clash403(var2[6]);
      } else if (GoblinRenderer.D.contains(var1)) {
         return GoblinRenderer.c_clash402(var2[7]);
      } else {
         return GoblinRenderer.M.contains(var1) ? GoblinRenderer.d_clash403(var2[6]) : z;
      }
   }

   @Override
   protected Vector4f a(String var1, float var2, float var3, float var4) {
      if (var1.startsWith("crown")) {
         ItemStack var5 = (ItemStack)this.j.func_184212_Q().func_187225_a(AbstractGirlNpcEntity.X);
         if (var5.func_190926_b()) {
            return super.a(var1, var2, var3, var4);
         }

         ItemArmor var6 = (ItemArmor)var5.func_77973_b();
         ArmorMaterial var7 = var6.func_82812_d();
         float var8 = 0.0F;
         switch (var7) {
            case GOLD:
               var8 = 1.0F;
               break;
            case CHAIN:
            case IRON:
               var8 = 2.0F;
               break;
            case LEATHER:
               var8 = 4.0F;
               int var9 = var6.func_82814_b(var5);
               float var10 = (var9 >> 16 & 0xFF) / 255.0F;
               float var11 = (var9 >> 8 & 0xFF) / 255.0F;
               float var12 = (var9 & 0xFF) / 255.0F;
               var2 = var10;
               var3 = var11;
               var4 = var12;
         }

         return new Vector4f(var2, var3, var4, 72.0F * var8 / 4096.0F);
      } else {
         return super.a(var1, var2, var3, var4);
      }
   }

   @Override
   protected boolean c_clash196(String var1) {
      return var1.startsWith("crown") ? true : super.c_clash196(var1);
   }

   @Override
   public HashSet<String> a() {
      return new HashSet<String>() {
         {
            this.add("boobs");
            this.add("booty");
            this.add("vagina");
            this.add("fuckhole");
            this.add("preggy");
            this.add("LegL");
            this.add("LegR");
            this.add("cheekR");
            this.add("cheekL");
         }
      };
   }

   @Override
   protected void a(String var1, GeoBone var2) {
      String[] var3 = AbstractNpcOnlyEntity.a_clash225(this.j);
      if (var3.length >= 8) {
         switch (var1) {
            case "earL":
               GoblinRenderer.a(var2, var3[0], var3[1], var3[3]);
               break;
            case "earR":
               GoblinRenderer.a(var2, var3[0], var3[2], var3[4]);
               break;
            case "hair":
               GoblinRenderer.a(var2, var3[5]);
               break;
            case "body":
               var2.setPivotY(-0.15F);
               GoblinRenderer.a(this.j, var2);
               break;
            case "LegR":
               GoblinRenderer.a(this.C, var2, 25.0F, 25.0F);
               break;
            case "boobR":
               GoblinRenderer.a(this.C, var2, 30.0F, 30.0F);
               break;
            case "boobR1":
               GoblinRenderer.a(this.C, var2, 10.0F, 15.0F);
               break;
            case "boobR2":
               GoblinRenderer.a(this.C, var2, 5.0F, 3.0F);
         }

         if (var1.contains("crown")) {
            GoblinRenderer.a(this.j, var2, var3[9]);
         }
      }
   }

   @Override
   public void a(BaseGirlEntity var1, double var2, double var4, double var6, float var8, float var9) {
      this.D = v;
      this.B = (GoblinPlayerEntity)var1;
      this.C = -420.69F == var8 && var1.y_clash492() == fp.SHOULDER_IDLE;
      this.E = -420.69F == var8 && var1.y_clash492() == fp.PICK_UP;
      this.y = var9;
      GoblinRenderer.B = var8;
      fp var10 = var1.y_clash492();
      UUID var11 = this.B.e_clash54();
      if (var11 != null) {
         if (var1.h_clash508()) {
            Vec3d var19 = GoblinRenderer.a(var1.field_70170_p, var1, var11, var2, var4, var6);
            var2 = var19.field_72450_a;
            var4 = var19.field_72448_b;
            var6 = var19.field_72449_c;
         }

         if (var10 == fp.THROWN || var10 == fp.START_THROWING) {
            if (i.field_71474_y.field_74320_O == 0 && var8 == -420.69F && !var1.h_clash508()) {
               return;
            }

            if (!var1.h_clash508()) {
               float var20 = var1.I_clash415();
               var1.field_70760_ar = var20;
               var1.field_70761_aq = var20;
            }
         }

         if (GoblinRenderer.a(var1, var10)) {
            if (i.field_71439_g.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.field_70761_aq = i.field_71439_g.field_70177_z + 180.0F;
               var1.field_70760_ar = i.field_71439_g.field_70177_z + 180.0F;
               Vec3d var21 = i.field_71439_g.func_70040_Z();
               GlStateManager.func_179094_E();
               GlStateManager.func_179137_b(var21.field_72450_a, var21.field_72448_b + i.field_71439_g.func_70047_e(), var21.field_72449_c);
               Vec3d var28 = GoblinEntity.b(new Vec3d(-Math.abs(i.field_71439_g.field_70125_A), 0.0, 0.0), i.field_71439_g.field_70177_z);
               GlStateManager.func_179114_b(i.field_71439_g.field_70125_A, (float)var28.field_72450_a, 0.0F, (float)var28.field_72449_c);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else if (!this.B.m_clash583().equals(i.field_71439_g.getPersistentID())) {
               if (!var1.h_clash508() || i.field_71439_g.getPersistentID().equals(var11)) {
                  if (!i.field_71439_g.getPersistentID().equals(var11)) {
                     EntityPlayer var22 = var1.field_70170_p.func_152378_a(var11);
                     if (var22 != null) {
                        var1.field_70761_aq = var22.field_70177_z;
                        var1.field_70760_ar = var22.field_70177_z;
                     }
                  } else {
                     var1.field_70761_aq = i.field_71439_g.field_70177_z;
                     var1.field_70760_ar = i.field_71439_g.field_70177_z;
                  }
               }

               Vec3d var23 = GoblinRenderer.a(var1, this.B.e_clash54(), var9);
               var2 = var23.field_72450_a;
               var4 = var23.field_72448_b;
               var6 = var23.field_72449_c;
            }
         } else if (this.C) {
            GoblinRenderer.a_clash399(var9);
            Vec3d var24 = new Vec3d(RotationHelper.a_clash25(-0.1F, 0.2F, i.field_71474_y.field_74334_X / 110.0F), 0.0, 0.0);
            var24 = GoblinEntity.b(var24, i.field_71439_g.field_70177_z);
            var2 = var24.field_72450_a;
            var4 = var24.field_72448_b;
            var6 = var24.field_72449_c;
            var1.field_70761_aq = i.field_71439_g.field_70177_z;
            var1.field_70760_ar = i.field_71439_g.field_70126_B;
            if (i.field_71439_g.func_70093_af()) {
               var4 -= 0.075;
            }
         } else if (var10 == fp.SHOULDER_IDLE) {
            if (i.field_71439_g.getPersistentID().equals(var11) && i.field_71474_y.field_74320_O == 0) {
               return;
            }

            EntityPlayer var26 = var1.field_70170_p.func_152378_a(var11);
            if (var26 == null) {
               return;
            }

            Vector4f var29 = GoblinRenderer.a_clash400(var26, var9);
            var2 = var29.x;
            var4 = var29.y;
            var6 = var29.z;
            var1.field_70761_aq = var29.w;
            if (var26.func_70093_af()) {
               var4 -= 0.32;
            }
         } else if (var10 == fp.PICK_UP) {
            EntityPlayer var27 = var1.field_70170_p.func_152378_a(var11);
            if (var27 != null) {
               var1.field_70760_ar = var27.field_70758_at;
               var1.field_70761_aq = var27.field_70759_as;
            }
         }

         super.a(var1, (double)var2, (double)var4, (double)var6, var8, var9);
         if (GoblinRenderer.a(var1, var10) && i.field_71474_y.field_74320_O == 0 && i.field_71439_g.getPersistentID().equals(var11)) {
            GlStateManager.func_179121_F();
         }
      } else {
         if (var1.h_clash508()) {
            Vec3d var12 = GoblinRenderer.a(var1.field_70170_p, var1, var11, var2, var4, var6);
            var2 = var12.field_72450_a;
            var4 = var12.field_72448_b;
            var6 = var12.field_72449_c;
         }

         if (var10 == fp.THROWN || var10 == fp.START_THROWING) {
            if (i.field_71474_y.field_74320_O == 0 && var8 == -420.69F && !var1.h_clash508()) {
               return;
            }

            if (!var1.h_clash508()) {
               float var14 = var1.I_clash415();
               var1.field_70760_ar = var14;
               var1.field_70761_aq = var14;
            }
         }

         if (GoblinRenderer.a(var1, var10)) {
            if (i.field_71439_g.getPersistentID().equals(var11)) {
               if (-420.69F != var8) {
                  return;
               }

               var1.field_70761_aq = i.field_71439_g.field_70177_z + 180.0F;
               var1.field_70760_ar = i.field_71439_g.field_70177_z + 180.0F;
               Vec3d var15 = i.field_71439_g.func_70040_Z();
               GlStateManager.func_179094_E();
               GlStateManager.func_179137_b(var15.field_72450_a, var15.field_72448_b + i.field_71439_g.func_70047_e(), var15.field_72449_c);
               Vec3d var13 = GoblinEntity.b(new Vec3d(-Math.abs(i.field_71439_g.field_70125_A), 0.0, 0.0), i.field_71439_g.field_70177_z);
               GlStateManager.func_179114_b(i.field_71439_g.field_70125_A, (float)var13.field_72450_a, 0.0F, (float)var13.field_72449_c);
               var2 = 0.0;
               var4 = 0.0;
               var6 = 0.0;
            } else if (!this.B.m_clash583().equals(i.field_71439_g.getPersistentID())) {
               if (var1.h_clash508()) {
               }

               var1.field_70761_aq = i.field_71439_g.field_70177_z;
               var1.field_70760_ar = i.field_71439_g.field_70177_z;
               Vec3d var16 = GoblinRenderer.a(var1, this.B.e_clash54(), var9);
               var2 = var16.field_72450_a;
               var4 = var16.field_72448_b;
               var6 = var16.field_72449_c;
            }
         } else if (this.C) {
            GoblinRenderer.a_clash399(var9);
            Vec3d var17 = new Vec3d(RotationHelper.a_clash25(-0.1F, 0.2F, i.field_71474_y.field_74334_X / 110.0F), 0.0, 0.0);
            var17 = GoblinEntity.b(var17, i.field_71439_g.field_70177_z);
            var2 = var17.field_72450_a;
            var4 = var17.field_72448_b;
            var6 = var17.field_72449_c;
            var1.field_70761_aq = i.field_71439_g.field_70177_z;
            var1.field_70760_ar = i.field_71439_g.field_70126_B;
            if (i.field_71439_g.func_70093_af()) {
               var4 -= 0.075;
            }
         } else {
            if (var10 == fp.SHOULDER_IDLE) {
               return;
            }

            if (var10 == fp.PICK_UP) {
            }
         }

         super.a(var1, (double)var2, (double)var4, (double)var6, var8, var9);
         if (GoblinRenderer.a(var1, var10) && i.field_71474_y.field_74320_O == 0 && i.field_71439_g.getPersistentID().equals(var11)) {
            GlStateManager.func_179121_F();
         }
      }
   }

   @Override
   protected void b(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, f7 var4, float var5) {
      a(var1, var2, var3, var4, var5);
   }

   @Nullable
   @Override
   protected f7 e_clash326(BaseGirlEntity var1) {
      if (!this.D) {
         return null;
      }

      if (!(var1 instanceof GoblinPlayerEntity)) {
         return null;
      }

      GoblinPlayerEntity var2 = (GoblinPlayerEntity)var1;
      UUID var3 = var2.m_clash583();
      EntityPlayerSP var4 = i.field_71439_g;
      if (var3 != null && (i.field_71474_y.field_74320_O != 0 || !var4.getPersistentID().equals(var3))) {
         EntityPlayer var5 = var2.k_clash584();
         if (var5 == null) {
            return null;
         }

         ItemStack var6 = (ItemStack)var2.func_184212_Q().func_187225_a(AbstractGirlNpcEntity.T);
         if (var6.func_190926_b()) {
            return null;
         }

         if (!(var6.func_77973_b() instanceof ItemArmor)) {
            return null;
         }

         ItemArmor var7 = (ItemArmor)var6.func_77973_b();
         switch (var7.func_82812_d()) {
            case GOLD:
               return new f7(99.0F, 98.0F, 14.0F);
            case CHAIN:
            case IRON:
               return new f7(85.0F, 85.0F, 85.0F);
            case LEATHER:
               int var8 = var7.func_82814_b(var6);
               float var9 = var8 >> 16 & 0xFF;
               float var10 = var8 >> 8 & 0xFF;
               float var11 = var8 & 0xFF;
               return new f7(var9, var10, var11);
            case DIAMOND:
            default:
               return new f7(23.0F, 100.0F, 93.0F);
         }
      } else {
         return null;
      }
   }

   @Override
   protected void c_clash145() {
      GlStateManager.func_179137_b(0.0, -0.77, -0.05);
      GlStateManager.func_179139_a(0.5, 0.5, 0.5);
   }

   @Override
   protected void a(boolean var1, ItemStack var2) {
      super.a(var1, var2);
      if (var2.func_77973_b().func_77661_b(var2) == EnumAction.BOW) {
         if (var1) {
            GlStateManager.func_179109_b(0.1F, 0.0F, 0.0F);
            GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
         } else {
            GlStateManager.func_179114_b(170.0F, 1.0F, 0.0F, 0.0F);
         }
      } else {
         GlStateManager.func_179114_b(var1 ? 70.0F : 180.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179137_b(0.0, 0.05, -0.03);
      }
   }

   @Override
   protected void a_clash146(boolean var1) {
   }

   @Override
   protected void a(boolean var1, boolean var2) {
      super.a(var1, var2);
      if (var1) {
         if (var2) {
            GlStateManager.func_179137_b(0.0, 0.2, -0.25);
            GlStateManager.func_179114_b(85.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.func_179114_b(38.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179114_b(90.0F, 0.0F, 0.0F, 1.0F);
         } else {
            GlStateManager.func_179114_b(90.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.func_179137_b(0.0, -0.265, -0.04);
         }
      } else if (var2) {
         GlStateManager.func_179114_b(0.0F, 1.0F, 0.0F, 0.0F);
         GlStateManager.func_179114_b(150.0F, 0.0F, 1.0F, 0.0F);
         GlStateManager.func_179114_b(0.0F, 0.0F, 0.0F, 1.0F);
         GlStateManager.func_179137_b(0.0, -0.33, -0.1);
      } else {
         GlStateManager.func_179137_b(-0.02, -0.05, -0.05);
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
