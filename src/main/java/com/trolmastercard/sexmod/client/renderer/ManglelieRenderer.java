package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import com.trolmastercard.sexmod.client.model.ManglelieNpcModel;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.BodyParts;
import com.trolmastercard.sexmod.entity.GalathEntity;
import com.trolmastercard.sexmod.entity.ManglelieEntity;
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.EntityLookVectorHelper;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;







import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class ManglelieRenderer extends GirlRenderer<ManglelieEntity> {
   static final UnknownScreen C = new UnknownScreen(115, 108, 188, 255);
   static final Vector3fSexmodSpecial D = new Vector3fSexmodSpecial(0.05F, 0.04F, 0.0F);
   static final Vector3fSexmodSpecial v = new Vector3fSexmodSpecial(0.0F, 0.065F, 0.0F);
   static final Vector3fSexmodSpecial z = new Vector3fSexmodSpecial(0.0F, 0.03F, 0.03F);
   static final UnknownScreen r = new UnknownScreen(63, 59, 150, 255);
   static final UnknownScreen x = new UnknownScreen(79, 74, 188, 255);
   static final float A = 0.5F;
   static final float w = 0.5F;
   static final int s = 40;
   static final float y = 0.01F;
   static final float t = 0.03F;
   public static final HashSet<String> B = new HashSet<String>() {
      {
         this.add("boobs2");
         this.add("booty2");
         this.add("vagina2");
         this.add("fuckhole2");
      }
   };
   boolean u = false;

   public ManglelieRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   public HashSet<String> a() {
      if (!this.u) {
         B.addAll(BodyParts.a);
         this.u = true;
      }

      return B;
   }

   public void a(ManglelieEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!this.d(var1)) {
         if (!this.a(var1)) {
            if (!c_clash371(var1, 0.5F)) {
               if (!this.c(var1)) {
                  super.a(var1, var2, var4, var6, var8, var9);
                  a_clash372(var1, var9);
               }
            }
         }
      }
   }

   boolean c(ManglelieEntity var1) {
      GalathEntity var2 = var1.getGalathPartner(false);
      if (var2 == null) {
         return false;
      }

      switch (var2.getCurrentAction()) {
         case CONTROLLED_FLIGHT:
         case BOOST:
            return true;
         default:
            return false;
      }
   }

   boolean a(ManglelieEntity var1) {
      return var1.getCurrentAction() != Action.RIDE_MOMMY_HEAD ? false : var1.getGalathPartner(false) == null;
   }

   boolean d(ManglelieEntity var1) {
      GalathEntity var2 = var1.getGalathPartner(false);
      if (var2 == null) {
         return false;
      } else if (var2.isDead) {
         var1.setGalathPartnerUUID(null);
         return false;
      } else {
         return var2.b_clash23();
      }
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!(var1 instanceof ManglelieEntity)) {
         super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
      } else {
         ManglelieEntity var10 = (ManglelieEntity)var1;
         if (!this.d(var10)) {
            if (!var10.r_clash411()) {
               super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
            }
         }
      }
   }

   static boolean c_clash371(BaseGirlEntity var0, float var1) {
      if (!(var0 instanceof ManglelieEntity)) {
         return false;
      }

      GalathEntity var2 = ((ManglelieEntity)var0).getGalathPartner(false);
      return var2 == null ? false : var2.bm < var1;
   }

   public static void a_clash372(BaseGirlEntity var0, float var1) {
      EntityPlayerSP var2 = i.player;
      if (var2 != null) {
         if (!c_clash371(var0, 0.5F)) {
            Tessellator var3 = Tessellator.getInstance();
            BufferBuilder var4 = var3.getBuffer();
            GlStateManager.pushMatrix();
            if (var0.isLocallyRegistered()) {
               GlStateManager.translate(0.0, 0.01, 0.0);
            } else {
               GalathGeometryRender.a(i, var0, var1);
               b_clash373(var0, var1);
            }

            i.getTextureManager().bindTexture(e);
            GlStateManager.disableCull();
            GlStateManager.disableLighting();
            a(var0, var4, var3, getInterpolatedYaw(var0, var1));
            a(var0, var4, var3);
            GlStateManager.popMatrix();
            GlStateManager.enableCull();
            GlStateManager.enableLighting();
         }
      }
   }

   static void b_clash373(BaseGirlEntity var0, float var1) {
      if (var0 instanceof ManglelieEntity) {
         ManglelieEntity var2 = (ManglelieEntity)var0;
         if (var2.r_clash411()) {
            if (!ManglelieNpcModel.c_clash313(var2)) {
               GalathEntity var3 = var2.getGalathPartner(false);
               if (var3 != null) {
                  GlStateManager.rotate(-RotationHelper.b(var0.prevRenderYawOffset, var0.renderYawOffset, var1), 0.0F, 1.0F, 0.0F);
               }
            }
         }
      }
   }

   public static boolean a_clash374(BaseGirlEntity var0) {
      if (var0 instanceof GalathEntity) {
         var0 = ((GalathEntity)var0).getMangleliePartner(false);
      }

      return var0 == null ? false : !Action.a(var0, Action.THREESOME_SLOW, Action.THREESOME_FAST, Action.THREESOME_CUM);
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2) {
      if (a_clash374(var0)) {
         var1.begin(7, DefaultVertexFormats.POSITION_COLOR);

         for (int var3 = 0; var3 < 39; var3++) {
            a(var0, var1, var3, var3 + 1);
         }

         a(var0, var1, 39, 0);
         var2.draw();
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, int var2, int var3) {
      Vec3d var4 = var0.getCachedBoneOffset("skirt_" + var2 + "_0");
      Vec3d var5 = var0.getCachedBoneOffset("skirt_" + var2 + "_1");
      Vec3d var6 = var0.getCachedBoneOffset("skirt_" + var2 + "_2");
      Vec3d var7 = var0.getCachedBoneOffset("skirt_" + var3 + "_0");
      Vec3d var8 = var0.getCachedBoneOffset("skirt_" + var3 + "_1");
      Vec3d var9 = var0.getCachedBoneOffset("skirt_" + var3 + "_2");
      UnknownScreen var10 = var2 % 2 == 0 ? x : r;
      var1.pos(var4.x, var4.y, var4.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var5.x, var5.y, var5.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var8.x, var8.y, var8.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var7.x, var7.y, var7.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var5.x, var5.y, var5.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var8.x, var8.y, var8.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var9.x, var9.y, var9.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
      var1.pos(var6.x, var6.y, var6.z).color(var10.a, var10.d, var10.c, var10.b).endVertex();
   }

   @Override
   protected void onBoneProcessing(BufferBuilder var1, String var2, GeoBone var3) {
      a(this.j, var2, var3, false);
      Entity var4 = this.j.b_clash424();
      if (var4 != null) {
         if ("weapon".equals(var2) && this.j.isLookingAtGalathEntity(var4, i.getRenderPartialTicks())) {
            this.renderEquippedItem(var1, var3, true);
         }

         if ("offhand".equals(var2) && !this.j.isLookingAtGalathEntity(var4, i.getRenderPartialTicks())) {
            this.renderEquippedItem(var1, var3, false);
         }
      }
   }

   public void renderEquippedItem(BufferBuilder var1, GeoBone var2, boolean var3) {
      ItemRenderer var4 = Minecraft.getMinecraft().getItemRenderer();
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.a(IGeoRenderer.MATRIX_STACK, var2);
      GL11.glEnable(2896);
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      if (var3) {
         GlStateManager.translate(-0.01, 0.0, 0.0);
         GlStateManager.rotate(120.0F, 1.0F, 0.0F, 0.0F);
      } else {
         GlStateManager.translate(0.15, 0.0, -0.05);
         GlStateManager.rotate(-140.0F, 1.0F, 0.0F, 0.0F);
      }

      GlStateManager.scale(0.7, 0.7, 0.7);
      ItemStack var5 = new ItemStack(Items.BOW);
      float var6 = this.j.b_clash423(i.getRenderPartialTicks());
      if (var6 < 1.0F) {
         float var7 = (float)RotationHelper.e(var6);
         this.j.setItemUseCount((int)(11.0F * (1.0F - var7) + 71980.0F));
         this.j.setHeldItemOverride(var5);
         this.j.setActiveHand(EnumHand.MAIN_HAND);
         this.j.setHandActiveState();
      } else {
         this.j.setHeldItemOverride(ItemStack.EMPTY);
         this.j.clearHandActiveState();
      }

      var4.renderItem(this.j, var5, TransformType.THIRD_PERSON_RIGHT_HAND);
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.j)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   public static void a(BaseGirlEntity var0, String var1, GeoBone var2, boolean var3) {
      if (var1.contains("skirt_")) {
         int var4 = a_clash375(var1);
         if (ThreadNames.isBetween(var4, 17.0, 35.0)) {
            if (i.isGamePaused()) {
               return;
            }

            String var5 = var4 < 26 ? "cheekL" : "cheekR";
            if (var3) {
               var5 = var5 + "2";
            }

            float var6 = TrigMath.d_clash746(var0.getAnimationProcessor().getBone(var5).getRotationX());
            if (var6 < 0.0F) {
               return;
            }

            var2.setPositionY(var2.getPositionY() + var6 * 0.01F);
         }

         if (ThreadNames.isBetween(var4, 1.0, 11.0)) {
            if (!var1.endsWith("1")) {
               return;
            }

            String var7 = var4 < 6 ? "legR" : "legL";
            if (var3) {
               var7 = var7 + "2";
            }

            float var8 = TrigMath.d_clash746(var0.getAnimationProcessor().getBone(var7).getRotationX());
            if (var8 < 0.0F) {
               return;
            }

            var2.setRotationX(TrigMath.wrapDegrees(var8));
            var2.setPositionY(TrigMath.wrapDegrees(var8 * 0.03F));
         }
      }
   }

   static int a_clash375(String var0) {
      int var1 = var0.indexOf(95);
      int var2 = var0.indexOf(95, var1 + 1);
      if (var1 != -1 && var2 != -1) {
         String var3 = var0.substring(var1 + 1, var2);

         try {
            return Integer.parseInt(var3);
         } catch (NumberFormatException var4) {
            return -1;
         }
      } else {
         return -1;
      }
   }

   protected void a(GeoModel var1, BufferBuilder var2, ManglelieEntity var3, float var4, float var5, float var6, float var7, float var8) {
      if (!ManglelieNpcModel.c_clash313(var3)) {
         super.a(var1, var2, var3, var4, var5, var6, var7, var8);
      } else {
         GeoBone var9 = var1.topLevelBones.get(0);
         GeoBone var10 = null;
         GeoBone var11 = null;

         for (GeoBone var13 : var9.childBones) {
            switch (var13.getName()) {
               case "steve":
                  var11 = var13;
                  break;
               case "body2":
                  var10 = var13;
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
         var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.j));

         this.renderRecursively(var2, var11, var4, var5, var6, this.j.getRenderScaleFactor());
         Tessellator.getInstance().draw();
         MATRIX_STACK.pop();
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      Vec3d[][] var4 = GalathGeometryRender.a(var0, var3, "clothBoobLconStart", "clothBoobLconEnd", D, v);
      Vec3d[][] var5 = GalathGeometryRender.a(var0, var3, "clothBoobRconStart", "clothBoobRconEnd", D, v);
      Vec3d[][] var6 = GalathGeometryRender.a(var0, var3, "clothBoobMidconStart", "clothBoobMidconEnd", z, z);
      GalathGeometryRender.a(var1, var4, C);
      GalathGeometryRender.a(var1, var5, C);
      GalathGeometryRender.a(var1, var6, C);
      var2.draw();
   }

   @Override
   public boolean a(HashSet var1, GeoBone var2) {
      while (var2.parent != null) {
         String var3 = var2.getName();
         if (var3.contains("clothBoob")) {
            return true;
         }

         if (var1.contains(var3)) {
            return false;
         }

         if (var3.startsWith("armor")) {
            return false;
         }

         var2 = var2.parent;
      }

      return true;
   }

   protected Vec3d a(ManglelieEntity var1, float var2, Vec3d var3) {
      if (var1.getCurrentAction() == Action.RUN) {
         float var5 = var1.getYawRotation();
         var1.rotationYaw = var5;
         var1.prevRenderYawOffset = var5;
         var1.renderYawOffset = var5;
         var1.prevRotationYawHead = var5;
         var1.rotationYawHead = var5;
         return var3;
      }

      if (b(var1)) {
         GalathEntity var4 = var1.getGalathPartner(false);
         if (var4 != null) {
            a(var4, var2, var1);
            return b(var4, var2);
         }
      }

      return var3;
   }

   public static void a(GalathEntity var0, float var1, EntityLivingBase var2) {
      if (var0.isAnchored()) {
         float var7 = var0.getYawRotation();
         float var8 = var0.getYawRotation();
         Float var9 = GalathEntity.getAimYaw(var0, var1);
         if (var9 != null) {
            var7 = var9;
            var8 = var9;
         }

         var2.rotationYaw = var7;
         var2.prevRenderYawOffset = var8;
         var2.renderYawOffset = var7;
         var2.prevRotationYawHead = var8;
         var2.rotationYawHead = var7;
      } else {
         float var4 = var0.rotationYawHead;
         float var5 = var0.prevRotationYawHead;
         Float var6 = GalathEntity.getAimYaw(var0, var1);
         if (var6 != null) {
            var4 = var6;
            var5 = var6;
         }

         var2.rotationYaw = var4;
         var2.prevRenderYawOffset = var5;
         var2.renderYawOffset = var4;
         var2.prevRotationYawHead = var5;
         var2.rotationYawHead = var4;
      }
   }

   public static boolean b(ManglelieEntity var0) {
      return var0.r_clash411() && !ManglelieNpcModel.c_clash313(var0);
   }

   public static Vec3d b(GalathEntity var0, float var1) {
      return EntityLookVectorHelper.a(var0, i.player, var1).add(var0.getCachedBoneOffset("mangPos"));
   }

   public static Vec3d a_clash376(GalathEntity var0, float var1) {
      return EntityLookVectorHelper.getEntityLookVector(var0, var1).add(var0.getCachedBoneOffset("mangPos"));
   }

}
