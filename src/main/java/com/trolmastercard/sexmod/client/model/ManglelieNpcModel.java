package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.renderer.ManglelieRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector2f;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;







import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

public class ManglelieNpcModel extends GirlModel<BaseGirlEntity> {
   public static final float h = 7.0F;
   public static final float k = 0.75F;
   static final float l = TrigMath.wrapDegrees(140.0F);
   static final float m = TrigMath.wrapDegrees(35.0F);
   static final float i = 90.0F;
   static final float g = TrigMath.wrapDegrees(45.0F);
   static final float f = TrigMath.wrapDegrees(-45.0F);
   public static final ResourceLocation j = new ResourceLocation("sexmod", "textures/entity/manglelie/manglelie.png");

   @Override
   protected ResourceLocation[] getModelLocations() {
      return new ResourceLocation[]{
         new ResourceLocation("sexmod", "geo/manglelie/manglelie.geo.json"),
         new ResourceLocation("sexmod", "geo/manglelie/manglelie.geo.json"),
         new ResourceLocation("sexmod", "geo/galath/galath_con_mang.geo.json")
      };
   }

   @Override
   public ResourceLocation getTextureLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/manglelie/manglelie.png");
   }

   public static boolean c_clash313(BaseGirlEntity var0) {
      return Action.a(var0, Action.THREESOME_SLOW, Action.THREESOME_FAST, Action.THREESOME_CUM);
   }

   @Override
   public ResourceLocation getAnimationFileLocation(BaseGirlEntity var1) {
      return new ResourceLocation("sexmod", "animations/manglelie/manglelie.animation.json");
   }

   @Override
   public void setLivingAnimations(BaseGirlEntity var1, Integer var2, AnimationEvent var3) {
      super.setLivingAnimations(var1, var2, var3);
      a(var1, this.getAnimationProcessor(), var3.getPartialTick());
      this.b_clash318(var1);
      this.d_clash317(var1);
      this.a_clash315(var1);
      this.e_clash314(var1);
   }

   void e_clash314(BaseGirlEntity var1) {
      if (!this.a.isGamePaused()) {
         if (!c_clash313(var1)) {
            GalathEntity var2 = ManglelieEntity.getGalathPartnerOf(var1, false);
            if (var2 != null) {
               if (Action.a(var2.getCurrentAction(), Action.CORRUPT_CUM, Action.CARRY_FAST, Action.CORRUPT_INTRO, Action.CORRUPT_SLOW)) {
                  AnimationProcessor var3 = this.getAnimationProcessor();
                  IBone var4 = var3.getBone("legR");
                  var4.setRotationY(var4.getRotationY() + f);
                  IBone var5 = var3.getBone("lowerArmR");
                  IBone var6 = var3.getBone("lowerArmL");
                  var5.setRotationX(var5.getRotationX() + f);
                  var6.setRotationX(var6.getRotationX() + f);
               }
            }
         }
      }
   }

   void a_clash315(BaseGirlEntity var1) {
      if (var1 instanceof ManglelieEntity) {
         if (!c_clash313(var1)) {
            ManglelieEntity var2 = (ManglelieEntity)var1;
            GalathEntity var3 = var2.getGalathPartner(false);
            if (var3 != null) {
               IBone var4 = this.getAnimationProcessor().getBone("body");
               var4.setRotationY(var3.bw + (this.a.isGamePaused() ? 0.0F : var4.getRotationY()));
               var4.setScaleX(var3.bm);
               var4.setScaleY(var3.bm);
               var4.setScaleZ(var3.bm);
            }
         }
      }
   }

   Vec3d a_clash316(@Nonnull Entity var1) {
      return EntityLookVectorHelper.getEntityLookVector(var1, this.a.getRenderPartialTicks()).add(0.0, var1.getEyeHeight(), 0.0);
   }

   void d_clash317(BaseGirlEntity var1) {
      if (!ClientProxy.IS_PRELOADING) {
         if (!c_clash313(var1)) {
            if (!this.a.isGamePaused()) {
               ManglelieEntity var2 = (ManglelieEntity)var1;
               if (var2.r_clash411()) {
                  GalathEntity var3 = var2.getGalathPartner(false);
                  if (var3 != null) {
                     AnimationProcessor var4 = this.getAnimationProcessor();
                     IBone var5 = var4.getBone("armL");
                     IBone var6 = var4.getBone("armR");
                     IBone var7 = var4.getBone("lowerArmL");
                     IBone var8 = var4.getBone("lowerArmR");
                     IBone var9 = var4.getBone("elbowR");
                     IBone var10 = var4.getBone("elbowL");
                     Entity var11 = var2.b_clash424();
                     boolean var12 = var11 == null;
                     if (var12) {
                        float var16 = Minecraft.getDebugFPS();
                        if (var16 == 0.0F) {
                           var16 = 1.0F;
                        }

                        if (var2.aj == var12) {
                           var2.V = 0.0F;
                        } else {
                           var2.V += 1.5F / var16;
                        }

                        if (var2.V >= 1.0F) {
                           var2.V = 0.0F;
                           var2.aj = var12;
                        }

                        ManglelieNpcModel.RotationData var15;
                        if (var2.V == 0.0F) {
                           var15 = this.a(var3, var6, var5, var7, var8);
                        } else {
                           var15 = ManglelieNpcModel.RotationData.a(
                              this.a(var3, var6, var5, var7, var8),
                              this.a(var2, var3, var8, var7, var4),
                              (float)(var2.aj ? RotationHelper.c_clash26(var2.V) : 1.0 - RotationHelper.c_clash26(var2.V))
                           );
                        }

                        var6.setRotationX(var15.c.a);
                        var6.setRotationY(var15.c.c);
                        var6.setRotationZ(var15.c.b);
                        var5.setRotationX(var15.g.a);
                        var5.setRotationY(var15.g.c);
                        var5.setRotationZ(var15.g.b);
                        var7.setRotationX(var15.b.a);
                        var7.setRotationY(var15.b.c);
                        var7.setRotationZ(var15.b.b);
                        var8.setRotationX(var15.h.a);
                        var8.setRotationY(var15.h.c);
                        var8.setRotationZ(var15.h.b);
                        var5.setScaleY(var15.a);
                        var6.setScaleY(var15.f);
                        var9.setRotationY(var15.d);
                        var10.setRotationY(var15.e);
                     } else {
                        var2.R = this.a_clash316(var11);
                        float var14 = Minecraft.getDebugFPS();
                        if (var14 == 0.0F) {
                           var14 = 1.0F;
                        }

                        if (var2.aj == var12) {
                           var2.V = 0.0F;
                        } else {
                           var2.V += 1.5F / var14;
                        }

                        if (var2.V >= 1.0F) {
                           var2.V = 0.0F;
                           var2.aj = var12;
                        }

                        ManglelieNpcModel.RotationData var13;
                        if (var2.V == 0.0F) {
                           var13 = this.a(var2, var3, var8, var7, var4);
                        } else {
                           var13 = ManglelieNpcModel.RotationData.a(
                              this.a(var3, var6, var5, var7, var8),
                              this.a(var2, var3, var8, var7, var4),
                              (float)(var2.aj ? RotationHelper.c_clash26(var2.V) : 1.0 - RotationHelper.c_clash26(var2.V))
                           );
                        }

                        var6.setRotationX(var13.c.a);
                        var6.setRotationY(var13.c.c);
                        var6.setRotationZ(var13.c.b);
                        var5.setRotationX(var13.g.a);
                        var5.setRotationY(var13.g.c);
                        var5.setRotationZ(var13.g.b);
                        var7.setRotationX(var13.b.a);
                        var7.setRotationY(var13.b.c);
                        var7.setRotationZ(var13.b.b);
                        var8.setRotationX(var13.h.a);
                        var8.setRotationY(var13.h.c);
                        var8.setRotationZ(var13.h.b);
                        var5.setScaleY(var13.a);
                        var6.setScaleY(var13.f);
                        var9.setRotationY(var13.d);
                        var10.setRotationY(var13.e);
                     }
                  }
               }
            }
         }
      }
   }

   ManglelieNpcModel.RotationData a(@Nonnull ManglelieEntity var1, @Nonnull GalathEntity var2, IBone var3, IBone var4, AnimationProcessor var5) {
      ManglelieNpcModel.RotationData var6 = new ManglelieNpcModel.RotationData();
      var6.b = new Vector3fSexmodSpecial(m, 0.0F, var3.getRotationZ());
      var6.h = new Vector3fSexmodSpecial(l, 0.0F, var4.getRotationZ());
      float var7 = var2.aE + var5.getBone("upperBody").getRotationX();
      float var8 = this.a.getRenderPartialTicks();
      Vec3d var9 = ManglelieRenderer.a_clash376(var2, var8);
      Vec3d var10 = var1.getCachedBoneOffset("armR").add(var9);
      Vec3d var11 = var1.getCachedBoneOffset("armL").add(var9);
      Vector2f var12 = ThreadNames.getLookAngles(var10, var1.R);
      Vector2f var13 = ThreadNames.getLookAngles(var11, var1.R);
      Float var14 = GalathEntity.getAimYaw(var2, var8);
      float var15 = var14 == null ? RotationHelper.b(var2.prevRotationYawHead, var2.rotationYawHead, var8) : var14;
      float var16 = TrigMath.wrapDegrees(var15);
      float var17 = var1.b_clash423(var8);
      float var18 = (float)RotationHelper.e(Math.min(1.0F, var17));
      float var19;
      if (var18 != 1.0F) {
         var19 = 0.0F;
      } else {
         var19 = (var17 * 28.0F - 28.0F) / 32.0F;
         var19 = Math.max(0.0F, var19 - 0.5F) * 2.0F;
      }

      float var20 = (float)RotationHelper.h(var19);
      float var21 = TrigMath.wrapDegrees(RotationHelper.lerp(0.0F, 90.0F, var18));
      boolean var22 = var1.isLookingAtGalathPoint(var1.R, var8);
      if (var22) {
         var6.c = new Vector3fSexmodSpecial(-var7 + var12.a + TrigMath.wrapDegrees(90.0F), var12.c, 0.0F);
         var6.g = new Vector3fSexmodSpecial(
            -var7 + var13.a + TrigMath.wrapDegrees(90.0F),
            (float)(var13.c + TrigMath.wrapDegrees(-20.0F) * Math.cos(var12.c + var16 * 1.0F) + RotationHelper.lerp(var21 / 2.0F, 0.0F, var20)),
            0.0F
         );
         var6.a = 1.0F + Math.abs(Math.abs(var12.c) - Math.abs(var16)) * 0.1909F;
         var6.e = TrigMath.wrapDegrees(90.0F);
         var6.b.b = RotationHelper.lerp(var21, 0.0F, var20);
         if (var19 > 0.5) {
            var6.b.a = m + (float)RotationHelper.b(g, 0.0, RotationHelper.h((var19 - 0.5F) * 2.0F));
         } else if (var19 != 0.0F && var19 < 0.5) {
            var6.b.a = m + (float)RotationHelper.b(0.0, g, RotationHelper.h(var19 * 2.0F));
         }
      } else {
         var6.g = new Vector3fSexmodSpecial(-var7 + var13.a + TrigMath.wrapDegrees(90.0F), var13.c, 0.0F);
         var6.c = new Vector3fSexmodSpecial(
            -var7 + var12.a + TrigMath.wrapDegrees(90.0F),
            (float)(var12.c + TrigMath.wrapDegrees(20.0F) * Math.cos(var13.c + var16 * 1.0F)) - RotationHelper.lerp(var21 / 2.0F, 0.0F, var20),
            0.0F
         );
         var6.f = 1.0F + Math.abs(Math.abs(var13.c) - Math.abs(var16)) * 0.1909F;
         var6.d = TrigMath.wrapDegrees(90.0F);
         var6.h.b = -RotationHelper.lerp(var21, 0.0F, var20);
         if (var19 > 0.5) {
            var6.h.a = l + (float)RotationHelper.b(g, 0.0, RotationHelper.h((var19 - 0.5F) * 2.0F));
         } else if (var19 != 0.0F && var19 < 0.5) {
            var6.h.a = l + (float)RotationHelper.b(0.0, g, RotationHelper.h(var19 * 2.0F));
         }
      }

      var6.c.c += var16;
      var6.g.c += var16;
      return var6;
   }

   ManglelieNpcModel.RotationData a(GalathEntity var1, IBone var2, IBone var3, IBone var4, IBone var5) {
      float var6 = var1.aE;
      ManglelieNpcModel.RotationData var7 = new ManglelieNpcModel.RotationData();
      if (var6 > 0.0F) {
         var7.c = new Vector3fSexmodSpecial(var2.getRotationX() - var6, var2.getRotationY() - var6 * -25.0F / 45.0F, var2.getRotationZ() + var6 * 12.5F / 45.0F);
         var7.g = new Vector3fSexmodSpecial(var3.getRotationX() - var6, var3.getRotationY() + var6 * 15.0F / 45.0F, var3.getRotationZ());
         var7.b = new Vector3fSexmodSpecial(var4.getRotationX(), var4.getRotationY(), var4.getRotationZ());
         var7.h = new Vector3fSexmodSpecial(var5.getRotationX(), var5.getRotationY(), var5.getRotationZ());
         return var7;
      } else {
         var7.h = new Vector3fSexmodSpecial(var5.getRotationX() + 2.0F * var6, var5.getRotationY(), var5.getRotationZ());
         var7.b = new Vector3fSexmodSpecial(var4.getRotationX() + 2.2222223F * var6, var4.getRotationY(), var4.getRotationZ());
         var7.c = new Vector3fSexmodSpecial(var2.getRotationX() - var6, var2.getRotationY(), var2.getRotationZ() + var6 * 5.0F / 45.0F);
         var7.g = new Vector3fSexmodSpecial(var3.getRotationX() - var6, var3.getRotationY(), var3.getRotationZ() - var6 * 5.0F / 45.0F);
         return var7;
      }
   }

   void b_clash318(BaseGirlEntity var1) {
      if (!ClientProxy.IS_PRELOADING) {
         if (!this.a.isGamePaused()) {
            ManglelieEntity var2 = (ManglelieEntity)var1;
            if (ManglelieRenderer.b(var2)) {
               GalathEntity var3 = var2.getGalathPartner(false);
               if (var3 != null) {
                  AnimationProcessor var4 = this.getAnimationProcessor();
                  float var5 = var3.aE;
                  var4.getBone("rotationTool").setRotationX(var5);
                  IBone var6 = var4.getBone("head");
                  IBone var7 = var4.getBone("upperBody");
                  IBone var8 = var4.getBone("boobs");
                  if (var5 > 0.0F) {
                     var7.setRotationX(-1.1111112F * var5);
                     var6.setRotationX(0.1333F * var5);
                     var8.setRotationX(var5 * 22.5F / 45.0F);
                  } else {
                     var7.setRotationX(-1.6666666F * var5);
                     var6.setRotationX(var5 * 0.666F);
                  }

                  float var9 = ThreadNames.a(var2.T, var2.af);
                  float var10 = ThreadNames.a(var2.ai, var2.W);
                  float var11 = Minecraft.getDebugFPS();
                  if (var11 == 0.0F) {
                     var11 = 1.0F;
                  }

                  float var12 = 7.0F * (Math.abs(var9) < 7.0F ? var9 : (var9 > 0.0F ? 7.0F : -7.0F)) * (1.0F / var11);
                  float var13 = 7.0F * (Math.abs(var10) < 7.0F ? var10 : (var10 > 0.0F ? 7.0F : -7.0F)) * (1.0F / var11);
                  float var14 = var2.T + var12;
                  float var15 = var2.ai + var13;
                  var6.setRotationY(var6.getRotationY() + var14);
                  var6.setRotationX(var6.getRotationX() + var15);
                  var2.T = var14;
                  var2.ai = var15;
               }
            }
         }
      }
   }

   public static void a(BaseGirlEntity var0, AnimationProcessor var1, float var2) {
      if (!ClientProxy.IS_PRELOADING) {
         boolean var3 = ManglelieRenderer.a_clash374(var0);
         e(var1, var3);
         f(var1, var3);
         b(var0, var1, var2);
      }
   }

   static void b(BaseGirlEntity var0, AnimationProcessor var1, float var2) {
      if (var0 instanceof ManglelieEntity) {
         for (int var3 = 0; var3 < 3; var3++) {
            IBone var4 = var1.getBone("cockStage" + var3);
            if (var4 != null) {
               var4.setHidden(var3 > ((ManglelieEntity)var0).an);
            }
         }
      }
   }

   static void f(AnimationProcessor var0, boolean var1) {
      var0.getBone("skirt").setHidden(!var1);
   }

   static void e(AnimationProcessor var0, boolean var1) {
      var0.getBone("cheekRBelowSkirt").setHidden(var1);
      var0.getBone("cheekLBelowSkirt").setHidden(var1);
      var0.getBone("sideRNoSkirt").setHidden(var1);
      IBone var2 = var0.getBone("sideRSkirt");
      IBone var10000;
      boolean var10001;
      if (!var1) {
         var10000 = var2;
         var10001 = true;
      } else {
         var10000 = var2;
         var10001 = false;
      }

      var10000.setHidden(var10001);
      var0.getBone("sideLNoSkirt").setHidden(var1);
      IBone var3 = var0.getBone("sideLSkirt");
      if (!var1) {
         var10000 = var3;
         var10001 = true;
      } else {
         var10000 = var3;
         var10001 = false;
      }

      var10000.setHidden(var10001);
   }


   private static class RotationData {
      private Vector3fSexmodSpecial c;
      private Vector3fSexmodSpecial g;
      private Vector3fSexmodSpecial h;
      private Vector3fSexmodSpecial b;
      private float f = 1.0F;
      private float a = 1.0F;
      private float e = 0.0F;
      private float d = 0.0F;

      private RotationData() {
      }

      static ManglelieNpcModel.RotationData a(ManglelieNpcModel.RotationData var0, ManglelieNpcModel.RotationData var1, float var2) {
         ManglelieNpcModel.RotationData var3 = new ManglelieNpcModel.RotationData();
         var3.c = RotationHelper.a(var0.c, var1.c, var2);
         var3.g = RotationHelper.a(var0.g, var1.g, var2);
         var3.h = RotationHelper.a(var0.h, var1.h, var2);
         var3.b = RotationHelper.a(var0.b, var1.b, var2);
         var3.f = RotationHelper.lerp(var0.f, var1.f, var2);
         var3.a = RotationHelper.lerp(var0.a, var1.a, var2);
         var3.e = RotationHelper.lerp(var0.e, var1.e, var2);
         var3.d = RotationHelper.lerp(var0.d, var1.d, var2);
         return var3;
      }
   }
}
