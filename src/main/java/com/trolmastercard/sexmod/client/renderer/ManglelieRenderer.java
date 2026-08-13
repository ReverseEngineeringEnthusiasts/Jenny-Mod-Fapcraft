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
   static final UnknownScreen CORRUPTION_COLOR_MAIN = new UnknownScreen(115, 108, 188, 255);
   static final Vector3fSexmodSpecial OFFSET_BODY = new Vector3fSexmodSpecial(0.05F, 0.04F, 0.0F);
   static final Vector3fSexmodSpecial OFFSET_ARM = new Vector3fSexmodSpecial(0.0F, 0.065F, 0.0F);
   static final Vector3fSexmodSpecial OFFSET_LEG = new Vector3fSexmodSpecial(0.0F, 0.03F, 0.03F);
   static final UnknownScreen CORRUPTION_COLOR_DARK = new UnknownScreen(63, 59, 150, 255);
   static final UnknownScreen CORRUPTION_COLOR_LIGHT = new UnknownScreen(79, 74, 188, 255);
   static final float SCALE_A = 0.5F;
   static final float SCALE_W = 0.5F;
   static final int VERTEX_COUNT = 40;
   static final float OFFSET_Y = 0.01F;
   static final float OFFSET_T = 0.03F;
   public static final HashSet<String> BLACKLISTED_BONES = new HashSet<String>() {
      {
         this.add("boobs2");
         this.add("booty2");
         this.add("vagina2");
         this.add("fuckhole2");
      }
   };
   boolean initialized = false;

   public ManglelieRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   public HashSet<String> a() {
      if (!this.initialized) {
         BLACKLISTED_BONES.addAll(BodyParts.CUSTOM_PART_BONES);
         this.initialized = true;
      }

      return BLACKLISTED_BONES;
   }

   public void a(ManglelieEntity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!this.d(var1)) {
         if (!this.a(var1)) {
            if (!isManglelieLooking(var1, 0.5F)) {
               if (!this.c(var1)) {
                  super.a(var1, var2, var4, var6, var8, var9);
                  renderMangleliePov(var1, var9);
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
         return var2.isHuggingManglelie();
      }
   }

   public void doRenderShadowAndFire(Entity var1, double var2, double var4, double var6, float var8, float var9) {
      if (!(var1 instanceof ManglelieEntity)) {
         super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
      } else {
         ManglelieEntity var10 = (ManglelieEntity)var1;
         if (!this.d(var10)) {
            if (!var10.isCorrupting()) {
               super.doRenderShadowAndFire(var1, var2, var4, var6, var8, var9);
            }
         }
      }
   }

   static boolean isManglelieLooking(BaseGirlEntity var0, float var1) {
      if (!(var0 instanceof ManglelieEntity)) {
         return false;
      }

      GalathEntity var2 = ((ManglelieEntity)var0).getGalathPartner(false);
      return var2 == null ? false : var2.bm < var1;
   }

   public static void renderMangleliePov(BaseGirlEntity var0, float var1) {
      EntityPlayerSP var2 = mc.player;
      if (var2 != null) {
         if (!isManglelieLooking(var0, 0.5F)) {
            Tessellator var3 = Tessellator.getInstance();
            BufferBuilder var4 = var3.getBuffer();
            GlStateManager.pushMatrix();
            if (var0.isLocallyRegistered()) {
               GlStateManager.translate(0.0, 0.01, 0.0);
            } else {
               GalathGeometryRender.a(mc, var0, var1);
               renderManglelieRibbon(var0, var1);
            }

            mc.getTextureManager().bindTexture(LINE_TEXTURE);
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

   static void renderManglelieRibbon(BaseGirlEntity var0, float var1) {
      if (var0 instanceof ManglelieEntity) {
         ManglelieEntity var2 = (ManglelieEntity)var0;
         if (var2.isCorrupting()) {
            if (!ManglelieNpcModel.isInThreesome(var2)) {
               GalathEntity var3 = var2.getGalathPartner(false);
               if (var3 != null) {
                  GlStateManager.rotate(-RotationHelper.b(var0.prevRenderYawOffset, var0.renderYawOffset, var1), 0.0F, 1.0F, 0.0F);
               }
            }
         }
      }
   }

   public static boolean isGalathLooking(BaseGirlEntity var0) {
      if (var0 instanceof GalathEntity) {
         var0 = ((GalathEntity)var0).getMangleliePartner(false);
      }

      return var0 == null ? false : !Action.a(var0, Action.THREESOME_SLOW, Action.THREESOME_FAST, Action.THREESOME_CUM);
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2) {
      if (isGalathLooking(var0)) {
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
      UnknownScreen var10 = var2 % 2 == 0 ? CORRUPTION_COLOR_LIGHT : CORRUPTION_COLOR_DARK;
      var1.pos(var4.x, var4.y, var4.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var5.x, var5.y, var5.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var8.x, var8.y, var8.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var7.x, var7.y, var7.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var5.x, var5.y, var5.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var8.x, var8.y, var8.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var9.x, var9.y, var9.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
      var1.pos(var6.x, var6.y, var6.z).color(var10.red, var10.green, var10.blue, var10.alpha).endVertex();
   }

   @Override
   protected void onBoneProcessing(BufferBuilder var1, String var2, GeoBone var3) {
      a(this.renderEntity, var2, var3, false);
      Entity var4 = this.renderEntity.getCorruptEntity();
      if (var4 != null) {
         if ("weapon".equals(var2) && this.renderEntity.isLookingAtGalathEntity(var4, mc.getRenderPartialTicks())) {
            this.renderEquippedItem(var1, var3, true);
         }

         if ("offhand".equals(var2) && !this.renderEntity.isLookingAtGalathEntity(var4, mc.getRenderPartialTicks())) {
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
      float var6 = this.renderEntity.getCorruptProgress(mc.getRenderPartialTicks());
      if (var6 < 1.0F) {
         float var7 = (float)RotationHelper.e(var6);
         this.renderEntity.setItemUseCount((int)(11.0F * (1.0F - var7) + 71980.0F));
         this.renderEntity.setHeldItemOverride(var5);
         this.renderEntity.setActiveHand(EnumHand.MAIN_HAND);
         this.renderEntity.setHandActiveState();
      } else {
         this.renderEntity.setHeldItemOverride(ItemStack.EMPTY);
         this.renderEntity.clearHandActiveState();
      }

      var4.renderItem(this.renderEntity, var5, TransformType.THIRD_PERSON_RIGHT_HAND);
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
      GL11.glDisable(2896);
      GlStateManager.popMatrix();
      GlStateManager.enableBlend();
      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
   }

   public static void a(BaseGirlEntity var0, String var1, GeoBone var2, boolean var3) {
      if (var1.contains("skirt_")) {
         int var4 = parseBoneIndex(var1);
         if (ThreadNames.isBetween(var4, 17.0, 35.0)) {
            if (mc.isGamePaused()) {
               return;
            }

            String var5 = var4 < 26 ? "cheekL" : "cheekR";
            if (var3) {
               var5 = var5 + "2";
            }

            float var6 = TrigMath.toDegrees(var0.getAnimationProcessor().getBone(var5).getRotationX());
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

            float var8 = TrigMath.toDegrees(var0.getAnimationProcessor().getBone(var7).getRotationX());
            if (var8 < 0.0F) {
               return;
            }

            var2.setRotationX(TrigMath.wrapDegrees(var8));
            var2.setPositionY(TrigMath.wrapDegrees(var8 * 0.03F));
         }
      }
   }

   static int parseBoneIndex(String var0) {
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
      if (!ManglelieNpcModel.isInThreesome(var3)) {
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

         Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.renderEntity));

         this.renderRecursively(var2, var11, var4, var5, var6, this.renderEntity.getRenderScaleFactor());
         Tessellator.getInstance().draw();
         MATRIX_STACK.pop();
      }
   }

   static void a(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      Vec3d[][] var4 = GalathGeometryRender.a(var0, var3, "clothBoobLconStart", "clothBoobLconEnd", OFFSET_BODY, OFFSET_ARM);
      Vec3d[][] var5 = GalathGeometryRender.a(var0, var3, "clothBoobRconStart", "clothBoobRconEnd", OFFSET_BODY, OFFSET_ARM);
      Vec3d[][] var6 = GalathGeometryRender.a(var0, var3, "clothBoobMidconStart", "clothBoobMidconEnd", OFFSET_LEG, OFFSET_LEG);
      GalathGeometryRender.a(var1, var4, CORRUPTION_COLOR_MAIN);
      GalathGeometryRender.a(var1, var5, CORRUPTION_COLOR_MAIN);
      GalathGeometryRender.a(var1, var6, CORRUPTION_COLOR_MAIN);
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
      return var0.isCorrupting() && !ManglelieNpcModel.isInThreesome(var0);
   }

   public static Vec3d b(GalathEntity var0, float var1) {
      return EntityLookVectorHelper.a(var0, mc.player, var1).add(var0.getCachedBoneOffset("mangPos"));
   }

   public static Vec3d getEntityLookVector(GalathEntity var0, float var1) {
      return EntityLookVectorHelper.getEntityLookVector(var0, var1).add(var0.getCachedBoneOffset("mangPos"));
   }

}
