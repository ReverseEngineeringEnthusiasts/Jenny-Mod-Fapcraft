package com.trolmastercard.sexmod.client;

import com.trolmastercard.sexmod.client.renderer.GirlPlayerRenderer;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;







import javax.vecmath.Vector2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.CameraSetup;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GirlCameraHelper {
   public static final float c = 1.2345679F;
   Vec3d b = null;
   Vec3d d = null;
   AbstractPlayerGirlEntity a = null;
   boolean e = false;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(Pre var1) {
      if (var1.getPartialRenderTick() != 1.2345679F) {
         AbstractPlayerGirlEntity.C_clash585();
         AbstractPlayerGirlEntity var2 = AbstractPlayerGirlEntity.d_clash567(var1.getEntityPlayer().getPersistentID());
         if (var2 != null) {
            var1.setCanceled(true);
            a(var2, var1.getEntityPlayer(), var1.getX(), var1.getY(), var1.getZ(), var1.getPartialRenderTick());
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public static void a(AbstractPlayerGirlEntity var0, EntityPlayer var1, double var2, double var4, double var6, float var8) {
      Minecraft var9 = Minecraft.func_71410_x();
      var1 = var0.c_clash452(var1);
      if (!var1.func_98034_c(var9.field_71439_g) || var0.E_clash458()) {
         RenderManager var10 = var9.func_175598_ae();
         var0.field_70177_z = var1.field_70177_z;
         var0.field_70758_at = var1.field_70758_at;
         var0.field_70759_as = var1.field_70759_as;
         var0.field_70127_C = var1.field_70127_C;
         var0.field_70125_A = var1.field_70125_A;
         var0.field_70126_B = var1.field_70126_B;
         var0.field_70169_q = var1.field_70169_q;
         var0.field_70167_r = var1.field_70167_r;
         var0.field_70166_s = var1.field_70166_s;
         var0.field_70142_S = var1.field_70142_S;
         var0.field_70137_T = var1.field_70137_T;
         var0.field_70136_U = var1.field_70136_U;
         var0.field_70761_aq = var1.field_70761_aq;
         var0.field_70760_ar = var1.field_70760_ar;
         var0.ad = var1.func_70093_af();
         var0.aj = var1.func_70051_ag();
         var0.ak = var1.func_184218_aH();
         var0.af = var1.field_70122_E;
         var0.ah = var1.func_184605_cv() != 0;
         double var11 = var1.field_70142_S - var1.field_70165_t;
         double var13 = var1.field_70161_v - var1.field_70136_U;
         double var15 = (Math.PI / 180.0) * var1.field_70177_z;
         var0.ao = new Vector2f((float)(var11 * Math.cos(var15) + var13 * Math.sin(var15)), (float)(var11 * Math.sin(var15) + var13 * Math.cos(var15)));
         float var17 = var0.z_clash454() ? a(var0, var1) : 0.0F;
         GirlPlayerRenderer.v = true;
         var10.func_188391_a(var0, var2, var4 + var17, var6, 90.0F, var8, false);
      }
   }

   static float a(AbstractPlayerGirlEntity var0, EntityPlayer var1) {
      if ((Boolean)var0.func_184212_Q().func_187225_a(BaseGirlEntity.G)) {
         return 0.0F;
      }

      if ((var1.func_184614_ca().func_77973_b() instanceof ItemBow || var1.func_184592_cb().func_77973_b() instanceof ItemBow) && var0.ah) {
         var0.b(fp.BOW);
      }

      if (var0.y_clash492() == fp.BOW && !var0.ah) {
         var0.b(fp.NULL);
      }

      if (var0.y_clash492() == fp.BOW) {
         var0.field_70177_z = var0.field_70759_as;
         var0.field_70761_aq = var0.field_70759_as;
         var0.field_70760_ar = var0.field_70758_at;
      }

      if (var0.ak) {
         return var1.func_184187_bx() instanceof EntityBoat ? 0.4F : 0.2F;
      } else {
         return 0.0F;
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (var2.field_71439_g != null) {
         if (var1.phase == Phase.END) {
            if (this.b != null) {
               var2.field_71439_g.func_70107_b(this.b.field_72450_a, this.b.field_72448_b, this.b.field_72449_c);
               var2.field_71439_g.field_70142_S = this.d.field_72450_a;
               var2.field_71439_g.field_70137_T = this.d.field_72448_b;
               var2.field_71439_g.field_70136_U = this.d.field_72449_c;
               this.b = null;
               this.d = null;
            }
         } else if (var2.field_71474_y.field_74320_O == 0) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.field_71439_g.getPersistentID());
            if (var3 != null) {
               if (var3.o_clash456()) {
                  this.b = var2.field_71439_g.func_174791_d();
                  this.d = new Vec3d(var2.field_71439_g.field_70142_S, var2.field_71439_g.field_70137_T, var2.field_71439_g.field_70136_U);
                  Vec3d var4 = var3.b_clash547("girlCam");
                  var4 = var3.b(var4, var1.renderTickTime);
                  var4 = var4.func_178787_e(RotationHelper.a(this.d, this.b, var1.renderTickTime));
                  var2.field_71439_g.field_70165_t = var4.field_72450_a;
                  var2.field_71439_g.field_70163_u = var4.field_72448_b - var2.field_71439_g.func_70047_e();
                  var2.field_71439_g.field_70161_v = var4.field_72449_c;
                  var2.field_71439_g.field_70142_S = var4.field_72450_a;
                  var2.field_71439_g.field_70137_T = var4.field_72448_b - var2.field_71439_g.func_70047_e();
                  var2.field_71439_g.field_70136_U = var4.field_72449_c;
                  fp var5 = var3.y_clash492();
                  float var6 = var3.I_clash415();
                  if (!var3.a(var5, var2.field_71439_g)) {
                     if (var5.flipGirlYaw) {
                        var6 += 180.0F;
                     }

                     if (var2.field_71439_g.field_70125_A > var5.maxGirlPitch) {
                        var2.field_71439_g.field_70125_A = var5.maxGirlPitch;
                        var2.field_71439_g.field_70127_C = var5.maxGirlPitch;
                     }

                     if (var2.field_71439_g.field_70125_A < var5.minGirlPitch) {
                        var2.field_71439_g.field_70125_A = var5.minGirlPitch;
                        var2.field_71439_g.field_70127_C = var5.minGirlPitch;
                     }

                     if (var2.field_71439_g.field_70177_z > var6 + 90.0F) {
                        var2.field_71439_g.field_70177_z = var6 + 90.0F;
                        var2.field_71439_g.field_70126_B = var6 + 90.0F;
                     }

                     if (var2.field_71439_g.field_70177_z < var6 - 90.0F) {
                        var2.field_71439_g.field_70177_z = var6 - 90.0F;
                        var2.field_71439_g.field_70126_B = var6 - 90.0F;
                     }
                  }
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(CameraSetup var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (var2.field_71439_g != null) {
         AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.field_71439_g.getPersistentID());
         if (var3 != null) {
            if (var3.F_clash231()) {
               if (var3.Q_clash505()) {
                  var1.setRoll(180.0F);
                  var1.setPitch(-var1.getPitch());
                  var1.setYaw(-var1.getYaw());
               }
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (this.b != null) {
         if (var2.field_71474_y.field_74320_O == 0) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.field_71439_g.getPersistentID());
            if (var3 != null) {
               Vec3d var4 = var2.field_71439_g.func_174791_d();
               Vec3d var5 = RotationHelper.a(this.d, this.b, var1.getPartialTicks());
               Vec3d var6 = var5.func_178788_d(var4);
               a(var3, var2.field_71439_g, var6.field_72450_a, var6.field_72448_b, var6.field_72449_c, var1.getPartialTicks());
               GlStateManager.func_179145_e();
               GlStateManager.func_179126_j();
               GlStateManager.func_179141_d();
            }
         }
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void b(RenderTickEvent var1) {
      Minecraft var2 = Minecraft.func_71410_x();
      if (var2.field_71439_g != null) {
         if (var1.phase != Phase.END) {
            AbstractPlayerGirlEntity var3 = AbstractPlayerGirlEntity.d_clash567(var2.field_71439_g.getPersistentID());
            if (var3 == null) {
               if (this.e) {
                  this.e = false;
                  var2.field_71439_g.eyeHeight = var2.field_71439_g.getDefaultEyeHeight();
               }
            } else if (var3.Q_clash505()) {
               if (this.e) {
                  this.e = false;
                  var2.field_71439_g.eyeHeight = var2.field_71439_g.getDefaultEyeHeight();
               }
            } else {
               if (this.a != var3) {
                  a(var3, var2.field_71439_g, 0.0, 500.0, 0.0, var1.renderTickTime);
                  this.a = var3;
               }

               var2.field_71439_g.eyeHeight = var3.R_clash549();
               this.e = true;
            }
         }
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
