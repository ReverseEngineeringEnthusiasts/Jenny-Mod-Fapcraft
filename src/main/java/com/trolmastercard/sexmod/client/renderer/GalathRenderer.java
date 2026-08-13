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
      if (var1.world instanceof SexWorldClient) {
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
         var1.rotationYaw = var2;
         var1.prevRenderYawOffset = var2;
         var1.renderYawOffset = var2;
         var1.prevRotationYawHead = var2;
         var1.rotationYawHead = var2;
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
         var1.renderYawOffset = var1.getYawRotation();
         var1.prevRenderYawOffset = var1.renderYawOffset;
      }
   }

   void d_clash322(GalathEntity var1) {
      if ((Boolean)var1.getDataManager().get(GalathEntity.bP)) {
         Vec3d var2 = new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ);
         Vec3d var3 = var1.getPositionVector().subtract(var2);
         boolean var4 = Math.abs(var3.x) + Math.abs(var3.z) < 0.05F;
         if (var4) {
            var1.renderYawOffset = this.s;
            var1.prevRenderYawOffset = this.s;
         } else {
            float var5 = (float)(gc.b(Math.atan2(var3.z, var3.x)) - 90.0);
            var1.renderYawOffset = var5;
            var1.prevRenderYawOffset = var5;
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

      Vec3d var4 = RotationHelper.a(new Vec3d(var3.prevPosX, var3.prevPosY, var3.prevPosZ), var3.getPositionVector(), var1);
      if (var2 == 24.0F && var0.af == -1L) {
         var0.af = i.world.getTotalWorldTime();
         var0.aH = var0.af + 8L;
      }

      if (ThreadNames.a_clash164(var2, 24.0, 32.0)) {
         Vec3d var9 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), var0.getYawRotation() + 180.0F);
         Vec3d var6 = var0.B_clash642();
         Vec3d var7 = var4.add(0.0, var3.getEyeHeight(), 0.0).add(var9);
         float var8 = ((float)i.world.getTotalWorldTime() + var1 - (float)var0.af) / (float)(var0.aH - var0.af);
         return RotationHelper.a(var6, var7, var8);
      } else if (ThreadNames.a_clash164(var2, 32.0, 54.0)) {
         Vec3d var5 = ck.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), var0.getYawRotation() + 180.0F);
         return var4.add(var5);
      } else {
         return null;
      }
   }

   public static void a_clash324(BaseGirlEntity var0, float var1) {
      EntityPlayerSP var2 = i.player;
      if (var2 != null) {
         Tessellator var3 = Tessellator.getInstance();
         BufferBuilder var4 = var3.getBuffer();
         GlStateManager.pushMatrix();
         af.a(i, var0, var1);
         i.getTextureManager().bindTexture(e);
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         a(var0, var4, var3, RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1));
         b(var0, var4, var3, var1);
         a(var0, var4, var3);
         GlStateManager.popMatrix();
         GlStateManager.enableCull();
         GlStateManager.enableLighting();
      }
   }

   static void b(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0 instanceof GalathEntity) {
         if ((Boolean)var0.getDataManager().get(GalathEntity.bP)) {
            if (!(Boolean)var0.getDataManager().get(GalathEntity.L)) {
               GlStateManager.pushMatrix();
               Vec3d var4 = var0.getCachedBoneOffset("stars");
               GlStateManager.translate(var4.x, var4.y, var4.z);
               float var5 = (float)i.world.getTotalWorldTime() + var3;
               float var6 = (float)(Math.sin(var5 * 0.2) * 5.0);
               float var7 = (float)(Math.cos(var5 * 0.2) * 5.0);
               float var8 = (float)(var5 * 3.0);
               GlStateManager.rotate(var6, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(var8, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(var7, 0.0F, 0.0F, 1.0F);
               float var9 = gc.c_clash745(9.0);
               f7 var10 = GalathEntity.aa;
               i.getTextureManager().bindTexture(e);
               var1.begin(3, DefaultVertexFormats.POSITION_TEX_COLOR);
               GlStateManager.glLineWidth(a(var0, var3, 1.0F, 3.0F));

               for (float var11 = 0.0F; var11 < Math.PI * 2; var11 += var9) {
                  double var12 = Math.sin(var11) * 0.3F;
                  double var14 = Math.cos(var11) * 0.3F;
                  var1.pos(var12, 0.0, var14).tex(0.0, 0.0).color(var10.a, var10.c, var10.b, 1.0F).endVertex();
               }

               var2.draw();
               i.getTextureManager().bindTexture(w);
               var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               var9 = gc.c_clash745(60.0);

               for (float var17 = 0.0F; var17 < Math.PI * 2; var17 += var9) {
                  double var18 = Math.sin(var17) * 0.3F;
                  double var19 = Math.cos(var17) * 0.3F;
                  var1.pos(var18 - 0.1F, 0.1F, var19).tex(0.0, 0.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  var1.pos(var18 + 0.1F, 0.1F, var19).tex(1.0, 0.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  var1.pos(var18 + 0.1F, -0.1F, var19).tex(1.0, 1.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  var1.pos(var18 - 0.1F, -0.1F, var19).tex(0.0, 1.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
               }

               var2.draw();
               GlStateManager.popMatrix();
            }
         }
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0.getCurrentAction() != fp.GIVE_COIN || fp.GIVE_COIN.ticksPlaying[1] <= 100) {
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         Vec3d[][] var4 = af.a(var0, var3, "hairStrandStartR", "hairStrandMidR", "hairStrandEndR", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         Vec3d[][] var5 = af.a(var0, var3, "hairStrandStartL", "hairStrandMidL", "hairStrandEndL", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         af.a(var1, var4, I);
         af.a(var1, var5, I);
         var2.draw();
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2) {
      if (((IGalath)var0).a_clash22()) {
         i.getTextureManager().bindTexture(GalathNpcModel.h);
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
      var0.begin(4, DefaultVertexFormats.POSITION_TEX_COLOR);
      var0.pos(var2[0].x, var2[0].y, var2[0].z)
         .tex(C.c, C.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z)
         .tex(C.c + 0.125F, C.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z)
         .tex(C.c + 0.125F, C.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[11].x, var2[11].y, var2[11].z)
         .tex(C.c, C.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[12].x, var2[12].y, var2[12].z)
         .tex(C.c + 0.125F, C.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[13].x, var2[13].y, var2[13].z)
         .tex(C.c + 0.125F, C.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var1.draw();
      var0.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var0.pos(var2[3].x, var2[3].y, var2[3].z)
         .tex(x.c, x.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[4].x, var2[4].y, var2[4].z)
         .tex(x.c, x.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[5].x, var2[5].y, var2[5].z)
         .tex(x.c + 0.125F, x.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[6].x, var2[6].y, var2[6].z)
         .tex(x.c + 0.125F, x.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[7].x, var2[7].y, var2[7].z)
         .tex(x.c, x.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[8].x, var2[8].y, var2[8].z)
         .tex(x.c, x.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[9].x, var2[9].y, var2[9].z)
         .tex(x.c + 0.125F, x.a)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[10].x, var2[10].y, var2[10].z)
         .tex(x.c + 0.125F, x.a + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var1.draw();
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
      Tessellator.getInstance().draw();
      this.a(var2, var11, var3, var8);
      var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.j));

      this.renderRecursively(var2, var12, var4, var5, var6, this.j.v_clash550());
      Tessellator.getInstance().draw();
      if (var13 != null) {
         var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         Minecraft.getMinecraft().renderEngine.bindTexture(ManglelieNpcModel.j);
         this.renderRecursively(var2, var13, var4, var5, var6, this.j.v_clash550());
         Tessellator.getInstance().draw();
      }

      MATRIX_STACK.pop();
   }

   @Override
   protected void a(BufferBuilder var1, String var2, GeoBone var3) {
      switch (var2) {
         case "hairBack":
            if (!i.isGamePaused()) {
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
            if (!i.isGamePaused()) {
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
                  float var10 = i.getRenderPartialTicks();
                  Vec3d var11 = RotationHelper.a(new Vec3d(this.j.lastTickPosX, this.j.lastTickPosY, this.j.lastTickPosZ), this.j.getPositionVector(), var10);
                  Vec3d var12 = RotationHelper.a(new Vec3d(var22.lastTickPosX, var22.lastTickPosY, var22.lastTickPosZ), this.j.getPositionVector(), var10);
                  Vec3d var24 = var11.subtract(var12);
                  float var14 = (float)ck.rotateByYaw(var24, this.j.renderYawOffset).z;
                  float var10000 = (float)Math.atan2(var24.y, var14);
               }
            }
            break;
         case "weapon":
            if (this.j.ap) {
               GlStateManager.pushMatrix();
               Tessellator.getInstance().draw();
               com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var3);
               GL11.glEnable(2896);
               GlStateManager.scale(1.5, 1.0, 2.0);
               GlStateManager.translate(0.0, 0.0, 0.05);
               GlStateManager.rotate(110.0F, 1.0F, 0.0F, 0.0F);
               Minecraft.getMinecraft().getItemRenderer().renderItem(this.j, new ItemStack(Items.IRON_SWORD), TransformType.THIRD_PERSON_RIGHT_HAND);
               this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
               var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               GL11.glDisable(2896);
               GlStateManager.popMatrix();
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
                  float var15 = this.j.renderYawOffset;
                  Vec3d var13 = var9.getPositionVector().subtract(this.j.getPositionVector());
                  var13 = ck.rotateByYaw(var13, var15);
                  double var16 = -ThreadNames.b(var13.x, -1.0, 1.0);
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
         float var3 = this.j.aD ? 1.0F - Math.min(0.29F, fp.a_clash718(this.j, i.getRenderPartialTicks())) / 0.29F : 1.0F;
         this.a(var1, var2, var3);
         this.bindTexture(ManglelieNpcModel.j);
      }
   }

   void d(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW, fp.MORNING_BLOWJOB_FAST)) {
         if (!i.isGamePaused()) {
            float var2 = i.player.ticksExisted + i.getRenderPartialTicks();
            float var3 = (float)(Math.sin(var2 * 0.1F) * 0.1F) + 0.2F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.j.aD) {
               float var5 = 1.0F - Math.min(0.5F, fp.a_clash718(this.j, i.getRenderPartialTicks())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   void c(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW, fp.MORNING_BLOWJOB_FAST)) {
         if (!i.isGamePaused()) {
            float var2 = i.player.ticksExisted + i.getRenderPartialTicks();
            float var3 = (float)Math.sin(var2 * -0.1F) * 0.1F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.j.aD) {
               float var5 = Math.min(0.5F, fp.a_clash718(this.j, i.getRenderPartialTicks())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   void a_clash325(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
         if (!i.isGamePaused()) {
            float var2 = i.player.ticksExisted + i.getRenderPartialTicks();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.1F));
         }
      }
   }

   void b(GeoBone var1) {
      if (fp.a(this.j, fp.MORNING_BLOWJOB_SLOW)) {
         if (!i.isGamePaused()) {
            float var2 = i.player.ticksExisted + i.getRenderPartialTicks();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.15F));
         }
      }
   }

   void a(BufferBuilder var1, GeoBone var2, float var3) {
      float var4 = fp.d(this.j, i.getRenderPartialTicks());
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
      float var3 = fp.d(this.j, i.getRenderPartialTicks());
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
      float var3 = this.j.b_clash696(i.getRenderPartialTicks());
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
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.a(MATRIX_STACK, var2);
      GlStateManager.disableCull();
      this.bindTexture(e);
      ef.a(var1, Tessellator.getInstance(), i, var3);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   void a(BufferBuilder var1, GeoBone var2, GalathEntity var3, float var4) {
      if (var3.getCurrentAction() == fp.GIVE_COIN) {
         n = var1;
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!this.p.contains(var2.getName())) {
            for (GeoCube var6 : var2.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.q = var2;
               this.a(var1, var6, 1.0F, 1.0F, 1.0F, 1.0F, (double)0.0);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }
         }

         Tessellator.getInstance().draw();
         GeoBone var14 = var2.childBones.get(0);
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         GL11.glDisable(2896);
         float var15 = ThreadNames.b(fp.GIVE_COIN.ticksPlaying[1] + var4, 105.0F, 125.0F);
         float var7 = (var15 - 105.0F) / 20.0F;
         float var8 = RotationHelper.lerp(120.0F, 240.0F, var7);
         f7 var9 = RotationHelper.a(GalathCoinRenderer.f, GalathCoinRenderer.e, var7);
         float var10 = OpenGlHelper.lastBrightnessX;
         float var11 = OpenGlHelper.lastBrightnessY;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var8, var8);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var14);
         MATRIX_STACK.moveToPivot(var14);
         MATRIX_STACK.rotate(var14);
         MATRIX_STACK.scale(var14);
         MATRIX_STACK.moveBackFromPivot(var14);
         if (!this.p.contains(var14.getName())) {
            for (GeoCube var13 : var14.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.q = var14;
               this.a(var1, var13, var9.a, var9.c, var9.b, 1.0F, (double)0.0);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }
         }

         MATRIX_STACK.pop();
         MATRIX_STACK.pop();
         Tessellator.getInstance().draw();
         GL11.glEnable(2896);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var10, var11);
      }
   }

   protected Vec3d a(GalathEntity var1, float var2, Vec3d var3) {
      if (var1.getCurrentAction() == fp.RUN) {
         float var4 = var1.getYawRotation();
         var1.rotationYaw = var4;
         var1.prevRenderYawOffset = var4;
         var1.renderYawOffset = var4;
         var1.prevRotationYawHead = var4;
         var1.rotationYawHead = var4;
      }

      return var3;
   }

}
