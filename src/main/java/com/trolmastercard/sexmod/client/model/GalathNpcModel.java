package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.command.CommandFuta;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.SoundHandler;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.ak;
import com.trolmastercard.sexmod.util.f2;
import com.trolmastercard.sexmod.util.f7;
import com.trolmastercard.sexmod.util.gc;







import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.shadowed.eliotlash.molang.MolangParser;

public class GalathNpcModel extends GirlModel<BaseGirlEntity> {
   public static ResourceLocation h = new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   float g = 0.0F;
   long f = -1L;
   long i = -1L;

   public GalathNpcModel() {
      this.c = this.getModelLocations();
   }

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath_con_mang.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/galath/galath.png");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/galath/galath.animation.json");
   }

   @Override
   protected boolean e_clash170(BaseGirlEntity var1) {
      if (!(var1 instanceof GalathEntity)) {
         return true;
      }

      GalathEntity var2 = (GalathEntity)var1;
      return var2.k_clash637() ? true : var2.M_clash691() == null;
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      this.k(var1);
      super.setLivingAnimations(var1, var2, var3);
      this.a_clash180(var1);
      this.h(var1);
      this.f_clash179(var1);
      this.b_clash178(var1);
      this.e_clash177(var1);
      this.g_clash176(var1);
      this.j(var1);
      this.a_clash175();
      this.c_clash173(var1);
      this.i(var1);
      this.d_clash174(var1);
      if (var1 instanceof GalathEntity) {
         GalathEntity var4 = (GalathEntity)var1;
         var4.aE = this.getAnimationProcessor().getBone("head").getRotationX();
         if (var4.b_clash23()) {
            ManglelieNpcModel.a(var4, this.getAnimationProcessor(), var3.getPartialTick());
         }
      }
   }

   void i(BaseGirlEntity var1) {
      if (fp.a(var1, fp.PUSSY_LICKING)) {
         if (var1 instanceof GalathEntity) {
            if (!this.a.isGamePaused()) {
               AnimationProcessor var2 = this.getAnimationProcessor();
               IBone var3 = var2.getBone("head");
               float var4 = this.a.getRenderPartialTicks() + this.a.player.ticksExisted;
               f7 var5 = this.a_clash171((GalathEntity)var1, var4);
               var3.setRotationX(var3.getRotationX() + var5.a);
               var3.setRotationY(var3.getRotationY() + var5.c);
               var3.setRotationZ(var3.getRotationZ() + var5.b);
               if (var1.getCurrentAction() == fp.PUSSY_LICKING && !((GalathEntity)var1).a5) {
                  float var6 = (float)(Math.sin(var4 * 0.3F) * 10.0);
                  if (var6 > 0.0F && this.g < 0.0F || var6 < 0.0F && this.g > 0.0F) {
                     var1.a(SoundHandler.randomSound(SoundHandler.GIRLS_ALLIE_LIPSOUND));
                  }

                  this.g = var6;
               }
            }
         }
      }
   }

   f7 a_clash171(GalathEntity var1, float var2) {
      return RotationHelper.a(this.a_clash172(var2), f7.d, var1.b_clash696(this.a.getRenderPartialTicks()));
   }

   f7 a_clash172(float var1) {
      return new f7(
         (float)Math.sin(var1 * 0.3F) * gc.wrapDegrees(10.0F),
         (float)Math.sin(var1 * 0.15F) * gc.wrapDegrees(7.0F),
         (float)Math.sin(var1 * -0.15) * gc.wrapDegrees(7.0F)
      );
   }

   void c_clash173(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         GalathEntity var2 = (GalathEntity)var1;
         AnimationProcessor var3 = this.getAnimationProcessor();
         IBone var4 = var3.getBone("body");
         var2.bw = var4.getRotationY();
         var2.bm = var4.getScaleY();
      }
   }

   void d_clash174(BaseGirlEntity var1) {
      if (var1.C.getAnimationState() == AnimationState.Transitioning) {
         AnimationProcessor var2 = this.getAnimationProcessor();
         fp var3 = var1.getCurrentAction();
         if (var3 == fp.HUG_MANG) {
            IBone var4 = var2.getBone("body2");
            if (var4 == null) {
               return;
            }

            var4.setPositionX(0.0F);
            var4.setPositionY(-0.53F);
            var4.setPositionZ(-40.05F);
         }
      }
   }

   void k(BaseGirlEntity var1) {
      if (!ClientProxy.IS_PRELOADING) {
         if (var1.getCurrentAction() == fp.MASTERBATE) {
            Object var2 = var1.z_clash528();
            if (var2 == null) {
               var2 = this.a.player;
            }

            MolangParser var3 = GeckoLibCache.getInstance().parser;
            Vec3d var4 = ak.b(var1, (EntityPlayer)var2, this.a.getRenderPartialTicks()).add(var1.getCachedBoneOffset("head"));
            float var5 = (float)gc.b(Math.atan2(var4.z, var4.x)) - var1.getYawRotation();
            float var6 = (float)gc.b(
               Math.atan2(var4.y, Math.sqrt(var4.x * var4.x + var4.z * var4.z))
            );
            double var7 = Math.abs(var4.x) + Math.abs(var4.y) + Math.abs(var4.z);
            double var9 = var7 * 7.0 + -20.0;
            double var11 = var7 * 5.0 + -20.0;
            var3.setValue("pitch", var9 + var6 - 80.0);
            var3.setValue("armpitch", var11 + var6 + -110.0);
            var3.setValue("armyaw", var5 + 80.0F);
            var3.setValue("yaw", var5 + 90.0F);
         }
      }
   }

   void a_clash175() {
      if (!ClientProxy.IS_PRELOADING) {
         this.getAnimationProcessor().getBone("futaCock").setHidden(!CommandFuta.e);
         this.getAnimationProcessor().getBone("futaBallLL").setHidden(!CommandFuta.e);
         this.getAnimationProcessor().getBone("futaBallLR").setHidden(!CommandFuta.e);
      }
   }

   void j(BaseGirlEntity var1) {
      if (var1 instanceof AbstractPlayerGirlEntity) {
         this.getAnimationProcessor().getBone("coin").setHidden(true);
      }
   }

   void g_clash176(BaseGirlEntity var1) {
      this.getAnimationProcessor().getBone("wings").setHidden(!((IGalath)var1).a_clash22());
   }

   void e_clash177(BaseGirlEntity var1) {
      AnimationProcessor var2 = this.getAnimationProcessor();
      IBone var3 = var2.getBone("nippleR");
      IBone var4 = var2.getBone("nippleL");
      IBone var5 = var2.getBone("braBoobL");
      IBone var6 = var2.getBone("braBoobR");
      IBone var7 = var2.getBone("slip");
      boolean var8 = ((IGalath)var1).c_clash21();
      if (var8) {
         fp.a(var1, fp.PUSSY_LICKING, fp.MASTERBATE_SITTING, fp.MASTERBATE_SITTING_CUM);
         if (var3 != null) {
            if (var5 != null) {
               IBone var13 = var3;
               var13.setHidden(false);
               IBone var14 = var4;
               var14.setHidden(false);
               var5.setHidden(var8);
               var6.setHidden(var8);
               IBone var15 = var7;
               var15.setHidden(true);
            }
         }
      } else {
         boolean var9 = fp.a(var1, fp.PUSSY_LICKING, fp.MASTERBATE_SITTING, fp.MASTERBATE_SITTING_CUM);
         if (var3 != null) {
            if (var5 != null) {
               IBone var10 = var3;
               var10.setHidden(true);
               IBone var11 = var4;
               var11.setHidden(true);
               var5.setHidden(var8);
               var6.setHidden(var8);
               IBone var12 = var7;
               var12.setHidden(var9);
            }
         }
      }
   }

   void b_clash178(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         if ((Boolean)var1.getDataManager().get(GalathEntity.bP)) {
            if (var1.getCurrentAction() == fp.KNOCK_OUT_FLY) {
               IBone var2 = this.getAnimationProcessor().getBone("body");
               Vec3d var3 = new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ);
               Vec3d var4 = var1.getPositionVector().subtract(var3);
               boolean var5 = Math.abs(var4.x) + Math.abs(var4.z) < 0.01F;
               if (var5) {
                  var2.setRotationX(gc.wrapDegrees(-90.0F));
                  var2.setPositionY(0.0F);
                  var2.setPositionZ(0.0F);
               } else {
                  Vec3d var6 = d_clash346(var1);
                  var2.setRotationX(-((float)var6.x));
                  var2.setPositionY((float)var6.y);
                  var2.setPositionZ((float)var6.z);
               }
            }
         }
      }
   }

   void h(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         if (var1.getCurrentAction() == fp.RAPE_CHARGE) {
            Vec3d var2 = d_clash346(var1);
            IBone var3 = this.getAnimationProcessor().getBone("body");
            IBone var4 = this.getAnimationProcessor().getBone("rotationTool");
            var4.setRotationX((float)var2.x);
            var3.setPositionY((float)var2.y);
            var3.setPositionZ((float)var2.z);
            float var5 = (Float)var1.getDataManager().get(GalathEntity.bO);
            var3.setRotationY(gc.wrapDegrees(var5 * 180.0F));
         }
      }
   }

   void f_clash179(BaseGirlEntity var1) {
      if (var1 instanceof GalathEntity) {
         GalathEntity var2 = (GalathEntity)var1;
         if (var2.getCurrentAction() != fp.ATTACK_SWORD) {
            this.f = -1L;
            this.i = -1L;
         } else {
            int var3 = var2.az();
            if (var3 == 24 && this.f == -1L) {
               this.f = this.a.world.getTotalWorldTime();
               this.i = this.f + 8L;
            }

            if (ThreadNames.a_clash164(var3, 24.0, 32.0)) {
               IBone var4 = this.getAnimationProcessor().getBone("body");
               Vec3d var5 = a(var2, var2.B_clash642());
               float var6 = ((float)Minecraft.getMinecraft().world.getTotalWorldTime() + this.a.getRenderPartialTicks() - (float)this.f) / (float)(this.i - this.f);
               var5 = RotationHelper.a(var5, Vec3d.ZERO, var6);
               var4.setRotationX((float)var5.x);
               var4.setPositionY((float)var5.y);
               var4.setPositionZ((float)var5.z);
            }
         }
      }
   }

   void a_clash180(BaseGirlEntity var1) {
      float var2 = 0.0F;
      switch (var1.getCurrentAction()) {
         case BOOST:
            if (fp.BOOST.ticksPlaying[1] > 13 && fp.BOOST.ticksPlaying[1] < 40) {
               var2 = 45.0F;
            }
         case FLY:
         case CONTROLLED_FLIGHT:
            float var3 = Minecraft.getMinecraft().getRenderPartialTicks();
            IBone var4 = this.getAnimationProcessor().getBone("rotationTool");
            f2 var5 = ((IGalath)var1).d_clash20();
            var4.setRotationX((float)RotationHelper.b(var5.c + var2, var5.d + var2, var3));
            var4.setRotationZ((float)RotationHelper.b(var5.b, var5.a, var3));
            return;
      }
   }

   @Override
   public String[] c() {
      return new String[]{"armorHelmet"};
   }

}
