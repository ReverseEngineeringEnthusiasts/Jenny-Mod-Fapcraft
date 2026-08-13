package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.client.model.GalathNpcModel;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.client.renderer.api.IGirlRenderer;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.api.IGalath;
import com.trolmastercard.sexmod.entity.fp;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.af;
import com.trolmastercard.sexmod.util.bm;
import com.trolmastercard.sexmod.util.ck;
import com.trolmastercard.sexmod.util.ef;
import com.trolmastercard.sexmod.util.f7;
import com.trolmastercard.sexmod.util.gc;







import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GalathRenderer extends GirlRenderer<GalathEntity> implements IGirlRenderer {
   public static final int D = 14;
   public static final HashSet<String> E = new HashSet<String>() {
      {
         this.add("static");
         this.add("turnable");
         this.add("slip");
         this.add("boobs");
         this.add("booty");
         this.add("vagina");
         this.add("fuckhole");
         this.add("futaBallLR");
         this.add("futaBallLL");
         this.add("coin");
         this.add("pentagram");
      }
   };
   public static final f7 y = new f7(0.0F, 0.0F, 0.0F);
   static final UnknownScreen H = new UnknownScreen(152, 45, 62, 255);
   static final UnknownScreen I = new UnknownScreen(84, 66, 88, 255);
   static final bm C = new bm(0.25F, 0.125F);
   static final bm x = new bm(0.375F, 0.125F);
   static final float F = 0.125F;
   static final ResourceLocation w = new ResourceLocation("sexmod", "textures/star.png");
   static final int v = 105;
   static final int A = 125;
   static final float B = 0.0296875F;
   static final float J = 0.06484375F;
   static final float z = 0.026124999F;
   static final float u = 0.0570625F;
   static final ef.b G = new ef.b(
      H,
      0.1F,
      12,
      0.035F,
      (var0, var1) -> (float)(Math.sin(var1 * 0.3 + -0.2 * var0) * 15.0),
      (var0, var1) -> (float)(Math.sin(var1 * -0.15 + -0.2 * var0) * 3.0),
      (var0, var1) -> 0.0F,
      0.03F,
      0.005F
   );
   static final ef.b t = new ef.b(
      H,
      0.0F,
      12,
      0.0F,
      (var0, var1) -> (float)(Math.sin(var1 * 0.3 + -0.2 * var0) * 15.0),
      (var0, var1) -> (float)(Math.sin(var1 * -0.15 + -0.2 * var0) * 3.0),
      (var0, var1) -> 0.0F,
      0.03F,
      0.005F
   );
   boolean r = false;
   float s = 0.0F;

   public GalathRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Nullable
   protected f7 a_clash319(GalathEntity var1) {
      if (var1.field_70170_p instanceof SexWorldClient) {
         return null;
      } else {
         return var1.bb ? null : y;
      }
   }

   @Override
   public HashSet<String> a() {
      if (!this.r) {
         E.addAll(BodyParts.a);
         E.addAll(ManglelieRenderer.B);
         this.r = true;
      }

      return E;
   }

   @Override
   protected void b(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, f7 var4, float var5) {
      a(var1, var2, var3, var4, var5);
   }

   protected void b_clash320(GalathEntity var1) {
      if (var1.getCurrentAction() == fp.MASTERBATE) {
         float var2 = var1.getYawRotation();
         var1.field_70177_z = var2;
         var1.field_70760_ar = var2;
         var1.field_70761_aq = var2;
         var1.field_70758_at = var2;
         var1.field_70759_as = var2;
      }
   }

   public void a(GalathEntity var1, double var2, double var4, double var6, float var8, float var9) {
      Vec3d var10 = a_clash323(var1, var9);
      if (var10 != null) {
         var1.setTargetPositionDirect(var10);
      }

      var1.aG = var10;
      GalathEntity.a_clash692(var1, var9);
      this.d_clash322(var1);
      this.c_clash321(var1);
      super.a(var1, var2, var4, var6, var8, var9);
      a_clash324(var1, var9);
      if (var1.b_clash23()) {
         ManglelieRenderer.a_clash372(var1, var9);
      }
   }

   void c_clash321(GalathEntity var1) {
      if (var1.getCurrentAction() == fp.RAPE_CHARGE) {
         var1.field_70761_aq = var1.getYawRotation();
         var1.field_70760_ar = var1.field_70761_aq;
      }
   }

   void d_clash322(GalathEntity var1) {
      if ((Boolean)var1.func_184212_Q().func_187225_a(GalathEntity.bP)) {
         Vec3d var2 = new Vec3d(var1.field_70142_S, var1.field_70137_T, var1.field_70136_U);
         Vec3d var3 = var1.func_174791_d().func_178788_d(var2);
         boolean var4 = Math.abs(var3.field_72450_a) + Math.abs(var3.field_72449_c) < 0.05F;
         if (var4) {
            var1.field_70761_aq = this.s;
            var1.field_70760_ar = this.s;
         } else {
            float var5 = (float)(gc.b(Math.atan2(var3.field_72449_c, var3.field_72450_a)) - 90.0);
            var1.field_70761_aq = var5;
            var1.field_70760_ar = var5;
            this.s = var5;
         }
      }
   }

   @Nullable
   public static Vec3d a_clash323(GalathEntity var0, float var1) {
      float var2 = var0.az();
      if (var2 == -1.0F) {
         var0.af = -1L;
         var0.aH = -1L;
         return null;
      }

      EntityLivingBase var3 = var0.M_clash691();
      if (var3 == null) {
         return null;
      }

      Vec3d var4 = RotationHelper.a(new Vec3d(var3.field_70169_q, var3.field_70167_r, var3.field_70166_s), var3.func_174791_d(), var1);
      if (var2 == 24.0F && var0.af == -1L) {
         var0.af = i.field_71441_e.func_82737_E();
         var0.aH = var0.af + 8L;
      }

      if (ThreadNames.a_clash164(var2, 24.0, 32.0)) {
         Vec3d var9 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), var0.getYawRotation() + 180.0F);
         Vec3d var6 = var0.B_clash642();
         Vec3d var7 = var4.func_72441_c(0.0, var3.func_70047_e(), 0.0).func_178787_e(var9);
         float var8 = ((float)i.field_71441_e.func_82737_E() + var1 - (float)var0.af) / (float)(var0.aH - var0.af);
         return RotationHelper.a(var6, var7, var8);
      } else if (ThreadNames.a_clash164(var2, 32.0, 54.0)) {
         Vec3d var5 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), var0.getYawRotation() + 180.0F);
         return var4.func_178787_e(var5);
      } else {
         return null;
      }
   }

   public static void a_clash324(BaseGirlEntity var0, float var1) {
      EntityPlayerSP var2 = i.field_71439_g;
      if (var2 != null) {
         Tessellator var3 = Tessellator.func_178181_a();
         BufferBuilder var4 = var3.func_178180_c();
         GlStateManager.func_179094_E();
         af.a(i, var0, var1);
         i.func_110434_K().func_110577_a(e);
         GlStateManager.func_179129_p();
         GlStateManager.func_179140_f();
         a(var0, var4, var3, RotationHelper.lerp(var0.field_70760_ar, var0.field_70761_aq, var1));
         b(var0, var4, var3, var1);
         a(var0, var4, var3);
         GlStateManager.func_179121_F();
         GlStateManager.func_179089_o();
         GlStateManager.func_179145_e();
      }
   }

   static void b(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0 instanceof GalathEntity) {
         if ((Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.bP)) {
            if (!(Boolean)var0.func_184212_Q().func_187225_a(GalathEntity.L)) {
               GlStateManager.func_179094_E();
               Vec3d var4 = var0.getCachedBoneOffset("stars");
               GlStateManager.func_179137_b(var4.field_72450_a, var4.field_72448_b, var4.field_72449_c);
               float var5 = (float)i.field_71441_e.func_82737_E() + var3;
               float var6 = (float)(Math.sin(var5 * 0.2) * 5.0);
               float var7 = (float)(Math.cos(var5 * 0.2) * 5.0);
               float var8 = (float)(var5 * 3.0);
               GlStateManager.func_179114_b(var6, 1.0F, 0.0F, 0.0F);
               GlStateManager.func_179114_b(var8, 0.0F, 1.0F, 0.0F);
               GlStateManager.func_179114_b(var7, 0.0F, 0.0F, 1.0F);
               float var9 = gc.c_clash745(9.0);
               f7 var10 = GalathEntity.aa;
               i.func_110434_K().func_110577_a(e);
               var1.func_181668_a(3, DefaultVertexFormats.field_181709_i);
               GlStateManager.func_187441_d(a(var0, var3, 1.0F, 3.0F));

               for (float var11 = 0.0F; var11 < Math.PI * 2; var11 += var9) {
                  double var12 = Math.sin(var11) * 0.3F;
                  double var14 = Math.cos(var11) * 0.3F;
                  var1.func_181662_b(var12, 0.0, var14).func_187315_a(0.0, 0.0).func_181666_a(var10.a, var10.c, var10.b, 1.0F).func_181675_d();
               }

               var2.func_78381_a();
               i.func_110434_K().func_110577_a(w);
               var1.func_181668_a(7, DefaultVertexFormats.field_181709_i);
               var9 = gc.c_clash745(60.0);

               for (float var17 = 0.0F; var17 < Math.PI * 2; var17 += var9) {
                  double var18 = Math.sin(var17) * 0.3F;
                  double var19 = Math.cos(var17) * 0.3F;
                  var1.func_181662_b(var18 - 0.1F, 0.1F, var19).func_187315_a(0.0, 0.0).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).func_181675_d();
                  var1.func_181662_b(var18 + 0.1F, 0.1F, var19).func_187315_a(1.0, 0.0).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).func_181675_d();
                  var1.func_181662_b(var18 + 0.1F, -0.1F, var19).func_187315_a(1.0, 1.0).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).func_181675_d();
                  var1.func_181662_b(var18 - 0.1F, -0.1F, var19).func_187315_a(0.0, 1.0).func_181666_a(1.0F, 1.0F, 1.0F, 1.0F).func_181675_d();
               }

               var2.func_78381_a();
               GlStateManager.func_179121_F();
            }
         }
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0.getCurrentAction() != fp.GIVE_COIN || fp.GIVE_COIN.ticksPlaying[1] <= 100) {
         var1.func_181668_a(7, DefaultVertexFormats.field_181709_i);
         Vec3d[][] var4 = af.a(var0, var3, "hairStrandStartR", "hairStrandMidR", "hairStrandEndR", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         Vec3d[][] var5 = af.a(var0, var3, "hairStrandStartL", "hairStrandMidL", "hairStrandEndL", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         af.a(var1, var4, I);
         af.a(var1, var5, I);
         var2.func_78381_a();
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2) {
      if (((IGalath)var0).a_clash22()) {
         i.func_110434_K().func_110577_a(GalathNpcModel.h);
         Vec3d[] var3 = new Vec3d[14];
         Vec3d[] var4 = new Vec3d[14];

         for (int var5 = 0; var5 < 14; var5++) {
            var3[var5] = var0.getCachedBoneOffset("wingRV" + var5);
            var4[var5] = var0.getCachedBoneOffset("wingLV" + var5);
         }

         a(var1, var2, var3);
         a(var1, var2, var4);
      }
   }

   static void a(BufferBuilder var0, Tessellator var1, Vec3d[] var2) {
      var0.func_181668_a(4, DefaultVertexFormats.field_181709_i);
      var0.func_181662_b(var2[0].field_72450_a, var2[0].field_72448_b, var2[0].field_72449_c)
         .func_187315_a(C.c, C.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[1].field_72450_a, var2[1].field_72448_b, var2[1].field_72449_c)
         .func_187315_a(C.c + 0.125F, C.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[2].field_72450_a, var2[2].field_72448_b, var2[2].field_72449_c)
         .func_187315_a(C.c + 0.125F, C.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[11].field_72450_a, var2[11].field_72448_b, var2[11].field_72449_c)
         .func_187315_a(C.c, C.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[12].field_72450_a, var2[12].field_72448_b, var2[12].field_72449_c)
         .func_187315_a(C.c + 0.125F, C.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[13].field_72450_a, var2[13].field_72448_b, var2[13].field_72449_c)
         .func_187315_a(C.c + 0.125F, C.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var1.func_78381_a();
      var0.func_181668_a(7, DefaultVertexFormats.field_181709_i);
      var0.func_181662_b(var2[3].field_72450_a, var2[3].field_72448_b, var2[3].field_72449_c)
         .func_187315_a(x.c, x.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[4].field_72450_a, var2[4].field_72448_b, var2[4].field_72449_c)
         .func_187315_a(x.c, x.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[5].field_72450_a, var2[5].field_72448_b, var2[5].field_72449_c)
         .func_187315_a(x.c + 0.125F, x.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[6].field_72450_a, var2[6].field_72448_b, var2[6].field_72449_c)
         .func_187315_a(x.c + 0.125F, x.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[7].field_72450_a, var2[7].field_72448_b, var2[7].field_72449_c)
         .func_187315_a(x.c, x.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[8].field_72450_a, var2[8].field_72448_b, var2[8].field_72449_c)
         .func_187315_a(x.c, x.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[9].field_72450_a, var2[9].field_72448_b, var2[9].field_72449_c)
         .func_187315_a(x.c + 0.125F, x.a)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var0.func_181662_b(var2[10].field_72450_a, var2[10].field_72448_b, var2[10].field_72449_c)
         .func_187315_a(x.c + 0.125F, x.a + 0.125F)
         .func_181669_b(255, 255, 255, 255)
         .func_181675_d();
      var1.func_78381_a();
   }

   protected void a(GeoModel var1, BufferBuilder var2, GalathEntity var3, float var4, float var5, float var6, float var7, float var8) {
      GeoBone var9 = var1.topLevelBones.get(0);
      GeoBone var10 = null;
      GeoBone var11 = null;
      GeoBone var12 = null;
      GeoBone var13 = null;

      for (GeoBone var15 : var9.childBones) {
         switch (var15.getName()) {
            case "steve":
               var12 = var15;
               break;
            case "body":
               var10 = var15;
               break;
            case "coin":
               var11 = var15;
               break;
            case "body2":
               var13 = var15;
         }
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(var9);
      MATRIX_STACK.moveToPivot(var9);
      MATRIX_STACK.rotate(var9);
      MATRIX_STACK.scale(var9);
      MATRIX_STACK.moveBackFromPivot(var9);
      this.renderRecursively(var2, var10, var4, var5, var6, var7);
      Tessellator.func_178181_a().func_78381_a();
      this.a(var2, var11, var3, var8);
      var2.func_181668_a(7, DefaultVertexFormats.field_181712_l);

      Minecraft.func_71410_x().field_71446_o.func_110577_a(this.func_110775_a(this.j));

      this.renderRecursively(var2, var12, var4, var5, var6, this.j.v_clash550());
      Tessellator.func_178181_a().func_78381_a();
      if (var13 != null) {
         var2.func_181668_a(7, DefaultVertexFormats.field_181712_l);
         Minecraft.func_71410_x().field_71446_o.func_110577_a(ManglelieNpcModel.j);
         this.renderRecursively(var2, var13, var4, var5, var6, this.j.v_clash550());
         Tessellator.func_178181_a().func_78381_a();
      }

      MATRIX_STACK.pop();
   }

   @Override
   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
      switch (var2) {
         case "hairBack":
            if (!i.func_147113_T()) {
               IBone var18 = this.j.getAnimationProcessor().getBone("head");
               float var19 = gc.d_clash746(var18.getRotationX());
               if (var19 < 0.0F) {
                  var3.setRotationX(gc.wrapDegrees(-var19));
               } else {
                  float var21 = Math.min(1.0F, var19 / 45.0F);
                  var3.setRotationX(gc.wrapDegrees(-var19));
                  var3.setPositionY(var3.getPositionY() + var21 * 1.5F);
               }
            }
            break;
         case "hairDownSideL":
         case "hairDownSideR":
            if (!i.func_147113_T()) {
               IBone var6 = this.j.getAnimationProcessor().getBone("head");
               float var7 = gc.d_clash746(var6.getRotationX());
               if (var7 < 0.0F) {
                  var3.setRotationX(gc.wrapDegrees(-var7 / 2.0F));
               } else {
                  float var20 = Math.min(1.0F, var7 / 45.0F);
                  var3.setRotationX(gc.wrapDegrees(-var7));
                  var3.setPositionY(var3.getPositionY() + var20);
               }
            }
            break;
         case "head":
            this.c(var3);
            fp var8 = this.j.getCurrentAction();
            if (var8 == fp.FLY || var8 == fp.ATTACK_SWORD) {
               EntityLivingBase var22 = this.j.M_clash691();
               if (var22 != null) {
                  float var10 = i.func_184121_ak();
                  Vec3d var11 = RotationHelper.a(new Vec3d(this.j.field_70142_S, this.j.field_70137_T, this.j.field_70136_U), this.j.func_174791_d(), var10);
                  Vec3d var12 = RotationHelper.a(new Vec3d(var22.field_70142_S, var22.field_70137_T, var22.field_70136_U), this.j.func_174791_d(), var10);
                  Vec3d var24 = var11.func_178788_d(var12);
                  float var14 = (float)ck.rotateByYaw(var24, this.j.field_70761_aq).field_72449_c;
                  float var10000 = (float)Math.atan2(var24.field_72448_b, var14);
               }
            }
            break;
         case "weapon":
            if (this.j.ap) {
               GlStateManager.func_179094_E();
               Tessellator.func_178181_a().func_78381_a();
               com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var3);
               GL11.glEnable(2896);
               GlStateManager.func_179139_a(1.5, 1.0, 2.0);
               GlStateManager.func_179137_b(0.0, 0.0, 0.05);
               GlStateManager.func_179114_b(110.0F, 1.0F, 0.0F, 0.0F);
               Minecraft.func_71410_x().func_175597_ag().func_178099_a(this.j, new ItemStack(Items.field_151040_l), TransformType.THIRD_PERSON_RIGHT_HAND);
               this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
               var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
               GL11.glDisable(2896);
               GlStateManager.func_179121_F();
            }
            break;
         case "tongue":
            this.e(var1, var3);
            break;
         case "mangTongue":
            this.c(var1, var3);
            break;
         case "head3":
            this.d(var3);
            break;
         case "irisL":
         case "irisR":
            this.a_clash325(var3);
            break;
         case "irsisFaceR2":
         case "irsisFaceR3":
            this.b(var3);
            break;
         case "armL":
         case "armR":
            if (this.j.getCurrentAction() == fp.RAPE_CHARGE) {
               EntityLivingBase var9 = this.j.M_clash691();
               if (var9 != null) {
                  float var15 = this.j.field_70761_aq;
                  Vec3d var13 = var9.func_174791_d().func_178788_d(this.j.func_174791_d());
                  var13 = ck.rotateByYaw(var13, var15);
                  double var16 = -ThreadNames.b(var13.field_72450_a, -1.0, 1.0);
                  var3.setRotationZ(var3.getRotationZ() + gc.c_clash745(45.0 * var16));
               }
            }
      }

      if (this.j.b_clash23()) {
         ManglelieRenderer.a(this.j, var2, var3, true);
      }
   }

   void e(BufferBuilder var1, GeoBone var2) {
      if (fp.a(this.j, fp.PUSSY_LICKING, fp.MASTERBATE_SITTING)) {
         this.f(var1, var2);
      } else if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
         this.d(var1, var2);
      }
   }

   void c(BufferBuilder var1, GeoBone var2) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW) || this.j.aD) {
         float var3 = this.j.aD ? 1.0F - Math.min(0.29F, fp.a_clash718(this.j, i.func_184121_ak())) / 0.29F : 1.0F;
         this.a(var1, var2, var3);
         this.func_110776_a(ManglelieNpcModel.j);
      }
   }

   void d(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW, fp.MORNING_BLOWJOB_FAST)) {
         if (!i.func_147113_T()) {
            float var2 = i.field_71439_g.field_70173_aa + i.func_184121_ak();
            float var3 = (float)(Math.sin(var2 * 0.1F) * 0.1F) + 0.2F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.j.aD) {
               float var5 = 1.0F - Math.min(0.5F, fp.a_clash718(this.j, i.func_184121_ak())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   void c(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW, fp.MORNING_BLOWJOB_FAST)) {
         if (!i.func_147113_T()) {
            float var2 = i.field_71439_g.field_70173_aa + i.func_184121_ak();
            float var3 = (float)Math.sin(var2 * -0.1F) * 0.1F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.j.aD) {
               float var5 = Math.min(0.5F, fp.a_clash718(this.j, i.func_184121_ak())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   void a_clash325(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
         if (!i.func_147113_T()) {
            float var2 = i.field_71439_g.field_70173_aa + i.func_184121_ak();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.1F));
         }
      }
   }

   void b(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
         if (!i.func_147113_T()) {
            float var2 = i.field_71439_g.field_70173_aa + i.func_184121_ak();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.15F));
         }
      }
   }

   void a(BufferBuilder var1, GeoBone var2, float var3) {
      float var4 = fp.d(this.j, i.func_184121_ak());
      float var5 = var3 * (float)(0.02F * (-0.4F * Math.cos((Math.PI * 2) * var4 + 1.05) + 0.6F));
      ef.b var6 = new ef.b(
         H,
         0.0F,
         12,
         var5,
         (var2x, var3x) -> var3 * (float)(Math.cos((Math.PI * 2) * var4 + 0.35F + -0.2F * var2x) * -10.0),
         (var0, var1x) -> 0.0F,
         (var2x, var3x) -> var3 * (float)(Math.cos((Math.PI * 2) * var4 + 1.25 + -0.1F * var2x) * -5.0),
         0.03F,
         0.005F
      );
      this.a(var1, var2, var6);
   }

   void d(BufferBuilder var1, GeoBone var2) {
      float var3 = fp.d(this.j, i.func_184121_ak());
      ef.b var4 = new ef.b(
         H,
         0.0F,
         12,
         0.02F,
         (var1x, var2x) -> (float)(Math.cos((Math.PI * 2) * var3 + -0.2F * var1x) * 15.0),
         (var1x, var2x) -> (float)(Math.cos((Math.PI * 2) * var3 + -0.2F * var1x) * 5.0),
         (var0, var1x) -> 0.0F,
         0.03F,
         0.005F
      );
      this.a(var1, var2, var4);
   }

   void f(BufferBuilder var1, GeoBone var2) {
      float var3 = this.j.b_clash696(i.func_184121_ak());
      if (var3 == 0.0F) {
         this.a(var1, var2, G);
      } else if (var3 == 1.0F) {
         this.a(var1, var2, t);
      } else {
         ef.b var4 = G.a_clash906();
         var4.g = RotationHelper.lerp(G.g, 0.0F, var3);
         var4.e = RotationHelper.lerp(G.e, 0.0F, var3);
         this.a(var1, var2, var4);
      }
   }

   void a(BufferBuilder var1, GeoBone var2, ef.b var3) {
      GlStateManager.func_179094_E();
      Tessellator.func_178181_a().func_78381_a();
      com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var2);
      GlStateManager.func_179129_p();
      this.func_110776_a(e);
      ef.a(var1, Tessellator.func_178181_a(), i, var3);
      this.func_110776_a(Objects.requireNonNull(this.getEntityTexture(this.j)));
      var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
      GlStateManager.func_179089_o();
      GlStateManager.func_179121_F();
   }

   void a(BufferBuilder var1, GeoBone var2, GalathEntity var3, float var4) {
      if (var3.getCurrentAction() == fp.GIVE_COIN) {
         n = var1;
         var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!this.p.contains(var2.getName())) {
            for (GeoCube var6 : var2.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.func_179094_E();
               this.q = var2;
               this.a(var1, var6, 1.0F, 1.0F, 1.0F, 1.0F, (double)0.0);
               GlStateManager.func_179121_F();
               MATRIX_STACK.pop();
            }
         }

         Tessellator.func_178181_a().func_78381_a();
         GeoBone var14 = var2.childBones.get(0);
         var1.func_181668_a(7, DefaultVertexFormats.field_181712_l);
         GL11.glDisable(2896);
         float var15 = ThreadNames.b(fp.GIVE_COIN.ticksPlaying[1] + var4, 105.0F, 125.0F);
         float var7 = (var15 - 105.0F) / 20.0F;
         float var8 = RotationHelper.lerp(120.0F, 240.0F, var7);
         f7 var9 = RotationHelper.a(GalathCoinRenderer.f, GalathCoinRenderer.e, var7);
         float var10 = OpenGlHelper.lastBrightnessX;
         float var11 = OpenGlHelper.lastBrightnessY;
         OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, var8, var8);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var14);
         MATRIX_STACK.moveToPivot(var14);
         MATRIX_STACK.rotate(var14);
         MATRIX_STACK.scale(var14);
         MATRIX_STACK.moveBackFromPivot(var14);
         if (!this.p.contains(var14.getName())) {
            for (GeoCube var13 : var14.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.func_179094_E();
               this.q = var14;
               this.a(var1, var13, var9.a, var9.c, var9.b, 1.0F, (double)0.0);
               GlStateManager.func_179121_F();
               MATRIX_STACK.pop();
            }
         }

         MATRIX_STACK.pop();
         MATRIX_STACK.pop();
         Tessellator.func_178181_a().func_78381_a();
         GL11.glEnable(2896);
         OpenGlHelper.func_77475_a(OpenGlHelper.field_77476_b, var10, var11);
      }
   }

   protected Vec3d a(GalathEntity var1, float var2, Vec3d var3) {
      if (var1.getCurrentAction() == fp.RUN) {
         float var4 = var1.getYawRotation();
         var1.field_70177_z = var4;
         var1.field_70760_ar = var4;
         var1.field_70761_aq = var4;
         var1.field_70758_at = var4;
         var1.field_70759_as = var4;
      }

      return var3;
   }

}
