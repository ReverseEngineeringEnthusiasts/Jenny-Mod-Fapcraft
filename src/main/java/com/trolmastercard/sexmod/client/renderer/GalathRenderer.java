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
import com.trolmastercard.sexmod.entity.Action;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.GalathGeometryRender;
import com.trolmastercard.sexmod.util.Vector2f;
import com.trolmastercard.sexmod.util.VectorMath;
import com.trolmastercard.sexmod.util.RibbonRenderer;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import com.trolmastercard.sexmod.util.TrigMath;
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

/**
 * Renderer for Galath (the demon/dragon girl). Beyond the standard girl
 * pipeline it renders: a flight dash trajectory
 * ({@link #getDashPosition} + first-person dash POV), animated wing meshes and
 * star rings, hair ribbons, the coin on the coin bone during GIVE_COIN, sword
 * / pussy-licking / morning-blowjob ribbon effects, and Manglelie interaction
 * poses.
 * <p>
 * <b>Dash.</b> {@link #doRenderGalath} resolves the dash target position
 * (lerped to the target entity, 24..32 ticks of approach then a hold at 1.5
 * blocks) and pins it as the render target before the normal render; the POV
 * wing meshes are drawn in a second pass. This is what makes the rape/dash
 * scene look like Galath flying at the player.
 * <p>
 * <b>Custom passes.</b> {@link #renderModelBuffer} renders body, coin
 * (GIVE_COIN effect), steve (player skin) and body2 (Manglelie texture) bones
 * in separate buffer flushes; wing meshes are built from cached
 * {@code wingRV...}/{@code wingLV...} bone offsets with the mod's line texture.
 * <p>
 * <b>Pose bones.</b> {@link #onBoneProcessing} adds hair-follow, blowjob
 * oscillation, rape-charge arm aim and sword/tongue ribbons (see the ribbon
 * helpers). Position interpolation uses {@link RotationHelper#lerpVec3dDouble}
 * (PROGRESS lerp — correct here).
 * <p>
 * CLIENT-side render thread only.
 */
public class GalathRenderer extends GirlRenderer<GalathEntity> implements IGirlRenderer {
   public static final int WING_VERTICES_COUNT = 14;
   public static final HashSet<String> BLACKLISTED_BONES = new HashSet<String>() {
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
   public static final Vector3fSexmodSpecial ZERO_OFFSET = new Vector3fSexmodSpecial(0.0F, 0.0F, 0.0F);
   static final UnknownScreen RIBBON_COLOR_A = new UnknownScreen(152, 45, 62, 255);
   static final UnknownScreen RIBBON_COLOR_B = new UnknownScreen(84, 66, 88, 255);
   static final Vector2f TEXTURE_UV_A = new Vector2f(0.25F, 0.125F);
   static final Vector2f TEXTURE_UV_B = new Vector2f(0.375F, 0.125F);
   static final float TEXTURE_UV_HEIGHT = 0.125F;
   static final ResourceLocation STAR_TEXTURE = new ResourceLocation("sexmod", "textures/star.png");
   static final int STAR_UV_X = 105;
   static final int STAR_UV_Y = 125;
   static final float RIBBON_SCALE_A = 0.0296875F;
   static final float RIBBON_SCALE_B = 0.06484375F;
   static final float RIBBON_SCALE_C = 0.026124999F;
   static final float RIBBON_SCALE_D = 0.0570625F;
   static final RibbonRenderer.RibbonConfig RIBBON_CONFIG_A = new RibbonRenderer.RibbonConfig(
      RIBBON_COLOR_A,
      0.1F,
      12,
      0.035F,
      (var0, var1) -> (float)(Math.sin(var1 * 0.3 + -0.2 * var0) * 15.0),
      (var0, var1) -> (float)(Math.sin(var1 * -0.15 + -0.2 * var0) * 3.0),
      (var0, var1) -> 0.0F,
      0.03F,
      0.005F
   );
   static final RibbonRenderer.RibbonConfig RIBBON_CONFIG_B = new RibbonRenderer.RibbonConfig(
      RIBBON_COLOR_A,
      0.0F,
      12,
      0.0F,
      (var0, var1) -> (float)(Math.sin(var1 * 0.3 + -0.2 * var0) * 15.0),
      (var0, var1) -> (float)(Math.sin(var1 * -0.15 + -0.2 * var0) * 3.0),
      (var0, var1) -> 0.0F,
      0.03F,
      0.005F
   );
   boolean initialized = false;
   float animationProgress = 0.0F;

   public GalathRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   /**
    * Wing color while Galath is corrupted ({@code bb} flag): {@code null} in
    * the preload world or when corrupted (use default), else black
    * ({@link #ZERO_OFFSET}).
    */
   @Nullable
   protected Vector3fSexmodSpecial getWingColor(GalathEntity var1) {
      if (var1.world instanceof SexWorldClient) {
         return null;
      } else {
         return var1.bb ? null : ZERO_OFFSET;
      }
   }

   /**
    * Merges the mod's custom-part bones + Manglelie's blacklist into the
    * static blacklist once (single-init guard), so no bone is drawn twice.
    */
   @Override
   public HashSet<String> getBlacklistedBones() {
      if (!this.initialized) {
         BLACKLISTED_BONES.addAll(BodyParts.CUSTOM_PART_BONES);
         BLACKLISTED_BONES.addAll(ManglelieRenderer.BLACKLISTED_BONES);
         this.initialized = true;
      }

      return BLACKLISTED_BONES;
   }

   @Override
   protected void drawOverlayLines(Tessellator var1, BufferBuilder var2, BaseGirlEntity var3, Vector3fSexmodSpecial var4, float var5) {
      renderGirlTint(var1, var2, var3, var4, var5);
   }

   protected void renderMasterbateEffect(GalathEntity var1) {
      if (var1.getCurrentAction() == Action.MASTERBATE) {
         float var2 = var1.getYawRotation();
         var1.rotationYaw = var2;
         var1.prevRenderYawOffset = var2;
         var1.renderYawOffset = var2;
         var1.prevRotationYawHead = var2;
         var1.rotationYawHead = var2;
      }
   }

   /**
    * Main render: resolves the dash target position (see
    * {@link #getDashPosition}) and flight/rape-charge yaw effects, then runs
    * the normal girl pipeline followed by the dash POV (wing meshes + Galath
    * geometry) and, while hugging Manglelie, her POV render.
    */
   public void doRenderGalath(GalathEntity var1, double var2, double var4, double var6, float var8, float var9) {
      Vec3d var10 = getDashPosition(var1, var9);
      if (var10 != null) {
         var1.setTargetPositionDirect(var10);
      }

      var1.aG = var10;
      GalathEntity.getAimYaw(var1, var9);
      this.renderFlightEffect(var1);
      this.renderRapeCharge(var1);
      super.doRenderEntity(var1, var2, var4, var6, var8, var9);
      renderDashPov(var1, var9);
      if (var1.isHuggingManglelie()) {
         ManglelieRenderer.renderMangleliePov(var1, var9);
      }
   }

   void renderRapeCharge(GalathEntity var1) {
      if (var1.getCurrentAction() == Action.RAPE_CHARGE) {
         var1.renderYawOffset = var1.getYawRotation();
         var1.prevRenderYawOffset = var1.renderYawOffset;
      }
   }

   /**
    * Flight yaw effect: when the flight flag is set and Galath is moving, the
    * yaw follows her movement direction (atan2 of the delta); when hovering
    * it holds the last direction, so she doesn't spin while bobbing.
    */
   void renderFlightEffect(GalathEntity var1) {
      if ((Boolean)var1.getDataManager().get(GalathEntity.bP)) {
         Vec3d var2 = new Vec3d(var1.lastTickPosX, var1.lastTickPosY, var1.lastTickPosZ);
         Vec3d var3 = var1.getPositionVector().subtract(var2);
         boolean var4 = Math.abs(var3.x) + Math.abs(var3.z) < 0.05F;
         if (var4) {
            var1.renderYawOffset = this.animationProgress;
            var1.prevRenderYawOffset = this.animationProgress;
         } else {
            float var5 = (float)(TrigMath.sinDegrees(Math.atan2(var3.z, var3.x)) - 90.0);
            var1.renderYawOffset = var5;
            var1.prevRenderYawOffset = var5;
            this.animationProgress = var5;
         }
      }
   }

   /**
    * Dash trajectory: for dash progress 24..32 lerps from the current position
    * to a point 3 blocks behind the target's eyes (8-tick window), for 32..54
    * holds 1.5 blocks behind the target; otherwise {@code null}. Also arms
    * the dash timing fields. {@code az()} == -1 resets and disables the dash.
    */
   @Nullable
   public static Vec3d getDashPosition(GalathEntity var0, float var1) {
      float var2 = var0.az();
      if (var2 == -1.0F) {
         var0.af = -1L;
         var0.aH = -1L;
         return null;
      }

      EntityLivingBase var3 = var0.getTargetEntity();
      if (var3 == null) {
         return null;
      }

      Vec3d var4 = RotationHelper.lerpVec3dDouble(new Vec3d(var3.prevPosX, var3.prevPosY, var3.prevPosZ), var3.getPositionVector(), var1);
      if (var2 == 24.0F && var0.af == -1L) {
         var0.af = mc.world.getTotalWorldTime();
         var0.aH = var0.af + 8L;
      }

      if (ThreadNames.isBetween(var2, 24.0, 32.0)) {
         Vec3d var9 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), var0.getYawRotation() + 180.0F);
         Vec3d var6 = var0.B_clash642();
         Vec3d var7 = var4.add(0.0, var3.getEyeHeight(), 0.0).add(var9);
         float var8 = ((float)mc.world.getTotalWorldTime() + var1 - (float)var0.af) / (float)(var0.aH - var0.af);
         return RotationHelper.lerpVec3dDouble(var6, var7, var8);
      } else if (ThreadNames.isBetween(var2, 32.0, 54.0)) {
         Vec3d var5 = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), var0.getYawRotation() + 180.0F);
         return var4.add(var5);
      } else {
         return null;
      }
   }

   /**
    * First-person dash POV: draws the Galath geometry, wing effect, wing
    * geometry and wing mesh at the local player (culling/lighting disabled
    * around the pass). This is what the player sees during the dash scene.
    */
   public static void renderDashPov(BaseGirlEntity var0, float var1) {
      EntityPlayerSP var2 = mc.player;
      if (var2 != null) {
         Tessellator var3 = Tessellator.getInstance();
         BufferBuilder var4 = var3.getBuffer();
         GlStateManager.pushMatrix();
         GalathGeometryRender.renderGalathGeometry(mc, var0, var1);
         mc.getTextureManager().bindTexture(LINE_TEXTURE);
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         renderWingEffect(var0, var4, var3, RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1));
         renderWingGeometry(var0, var4, var3, var1);
         renderWingMesh(var0, var4, var3);
         GlStateManager.popMatrix();
         GlStateManager.enableCull();
         GlStateManager.enableLighting();
      }
   }

   /**
    * Renders the wing star ring (line circle + star quads at the {@code stars}
    * bone) rotating with world time — the visual effect of the wing
    * animation. Skipped when the effects flag is hidden.
    */
   static void renderWingGeometry(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0 instanceof GalathEntity) {
         if ((Boolean)var0.getDataManager().get(GalathEntity.bP)) {
            if (!(Boolean)var0.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
               GlStateManager.pushMatrix();
               Vec3d var4 = var0.getCachedBoneOffset("stars");
               GlStateManager.translate(var4.x, var4.y, var4.z);
               float var5 = (float)mc.world.getTotalWorldTime() + var3;
               float var6 = (float)(Math.sin(var5 * 0.2) * 5.0);
               float var7 = (float)(Math.cos(var5 * 0.2) * 5.0);
               float var8 = (float)(var5 * 3.0);
               GlStateManager.rotate(var6, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(var8, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(var7, 0.0F, 0.0F, 1.0F);
               float var9 = TrigMath.toRadians(9.0);
               Vector3fSexmodSpecial var10 = GalathEntity.aa;
               mc.getTextureManager().bindTexture(LINE_TEXTURE);
               var1.begin(3, DefaultVertexFormats.POSITION_TEX_COLOR);
               GlStateManager.glLineWidth(getRenderOffset(var0, var3, 1.0F, 3.0F));

               for (float var11 = 0.0F; var11 < Math.PI * 2; var11 += var9) {
                  double var12 = Math.sin(var11) * 0.3F;
                  double var14 = Math.cos(var11) * 0.3F;
                  var1.pos(var12, 0.0, var14).tex(0.0, 0.0).color(var10.x, var10.y, var10.z, 1.0F).endVertex();
               }

               var2.draw();
               mc.getTextureManager().bindTexture(STAR_TEXTURE);
               var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               var9 = TrigMath.toRadians(60.0);

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

   /**
    * Renders the hair-strand ribbon mesh (two 3-point ribbons along the head's
    * hair bones) — the trailing hair effect while flying. Skipped during the
    * coin-giving action past its first 100 ticks.
    */
   static void renderWingEffect(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2, float var3) {
      if (var0.getCurrentAction() != Action.GIVE_COIN || Action.GIVE_COIN.ticksPlaying[1] <= 100) {
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         Vec3d[][] var4 = GalathGeometryRender.buildBodyBoneMesh(var0, var3, "hairStrandStartR", "hairStrandMidR", "hairStrandEndR", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         Vec3d[][] var5 = GalathGeometryRender.buildBodyBoneMesh(var0, var3, "hairStrandStartL", "hairStrandMidL", "hairStrandEndL", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         GalathGeometryRender.renderMesh(var1, var4, RIBBON_COLOR_B);
         GalathGeometryRender.renderMesh(var1, var5, RIBBON_COLOR_B);
         var2.draw();
      }
   }

   /**
    * Renders the animated wing mesh: two 14-point line strips built from the
    * cached {@code wingRV<0..13>}/{@code wingLV<0..13>} bone offsets, drawn
    * with the Galath texture. Only when wings are animated.
    */
   static void renderWingMesh(BaseGirlEntity var0, BufferBuilder var1, Tessellator var2) {
      if (((IGalath)var0).areWingsAnimated()) {
         mc.getTextureManager().bindTexture(GalathNpcModel.GALATH_TEXTURE);
         Vec3d[] var3 = new Vec3d[14];
         Vec3d[] var4 = new Vec3d[14];

         for (int var5 = 0; var5 < 14; var5++) {
            var3[var5] = var0.getCachedBoneOffset("wingRV" + var5);
            var4[var5] = var0.getCachedBoneOffset("wingLV" + var5);
         }

         renderLineStrip(var1, var2, var3);
         renderLineStrip(var1, var2, var4);
      }
   }

   /**
    * Emits the two wing strip triangles (quads 0-1-2-11-12-13 and 3..10) with
    * the ribbon texture UVs.
    */
   static void renderLineStrip(BufferBuilder var0, Tessellator var1, Vec3d[] var2) {
      var0.begin(4, DefaultVertexFormats.POSITION_TEX_COLOR);
      var0.pos(var2[0].x, var2[0].y, var2[0].z)
         .tex(TEXTURE_UV_A.x, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[1].x, var2[1].y, var2[1].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[2].x, var2[2].y, var2[2].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[11].x, var2[11].y, var2[11].z)
         .tex(TEXTURE_UV_A.x, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[12].x, var2[12].y, var2[12].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[13].x, var2[13].y, var2[13].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var1.draw();
      var0.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      var0.pos(var2[3].x, var2[3].y, var2[3].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[4].x, var2[4].y, var2[4].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[5].x, var2[5].y, var2[5].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[6].x, var2[6].y, var2[6].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[7].x, var2[7].y, var2[7].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[8].x, var2[8].y, var2[8].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[9].x, var2[9].y, var2[9].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      var0.pos(var2[10].x, var2[10].y, var2[10].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      var1.draw();
   }

   /**
    * Galath's model pass: renders body, then the coin bone effect (GIVE_COIN),
    * then the steve (skin) bone and the body2 bone with Manglelie's texture —
    * each in its own buffer flush.
    */
   protected void renderModelBuffer(GeoModel var1, BufferBuilder var2, GalathEntity var3, float var4, float var5, float var6, float var7, float var8) {
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
      this.renderBoneEffect(var2, var11, var3, var8);
      var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.renderEntity));

      this.renderRecursively(var2, var12, var4, var5, var6, this.renderEntity.getRenderScaleFactor());
      Tessellator.getInstance().draw();
      if (var13 != null) {
         var2.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         Minecraft.getMinecraft().renderEngine.bindTexture(ManglelieNpcModel.MANGLELIE_TEXTURE);
         this.renderRecursively(var2, var13, var4, var5, var6, this.renderEntity.getRenderScaleFactor());
         Tessellator.getInstance().draw();
      }

      MATRIX_STACK.pop();
   }

   /**
    * Pose-bone processing: hair follows the head pitch (hairBack,
    * hairDownSideL/R), blowjob/morning-pose oscillations on head/head3/iris
    * bones, the sword held during sword actions ({@code weapon} bone when
    * {@code ap}), tongue/mangTongue ribbon effects, rape-charge arm aim at the
    * target, and — while hugging Manglelie — her skirt-follow transform.
    */
   @Override
   protected void onBoneProcessing(BufferBuilder var1, String var2, GeoBone var3) {
      switch (var2) {
         case "hairBack":
            if (!mc.isGamePaused()) {
               IBone var18 = this.renderEntity.getAnimationProcessor().getBone("head");
               float var19 = TrigMath.toDegrees(var18.getRotationX());
               if (var19 < 0.0F) {
                  var3.setRotationX(TrigMath.wrapDegrees(-var19));
               } else {
                  float var21 = Math.min(1.0F, var19 / 45.0F);
                  var3.setRotationX(TrigMath.wrapDegrees(-var19));
                  var3.setPositionY(var3.getPositionY() + var21 * 1.5F);
               }
            }
            break;
         case "hairDownSideL":
         case "hairDownSideR":
            if (!mc.isGamePaused()) {
               IBone var6 = this.renderEntity.getAnimationProcessor().getBone("head");
               float var7 = TrigMath.toDegrees(var6.getRotationX());
               if (var7 < 0.0F) {
                  var3.setRotationX(TrigMath.wrapDegrees(-var7 / 2.0F));
               } else {
                  float var20 = Math.min(1.0F, var7 / 45.0F);
                  var3.setRotationX(TrigMath.wrapDegrees(-var7));
                  var3.setPositionY(var3.getPositionY() + var20);
               }
            }
            break;
         case "head":
            this.handleBlowjobBone(var3);
            Action var8 = this.renderEntity.getCurrentAction();
            if (var8 == Action.FLY || var8 == Action.ATTACK_SWORD) {
               EntityLivingBase var22 = this.renderEntity.getTargetEntity();
               if (var22 != null) {
                  float var10 = mc.getRenderPartialTicks();
                  Vec3d var11 = RotationHelper.lerpVec3dDouble(new Vec3d(this.renderEntity.lastTickPosX, this.renderEntity.lastTickPosY, this.renderEntity.lastTickPosZ), this.renderEntity.getPositionVector(), var10);
                  Vec3d var12 = RotationHelper.lerpVec3dDouble(new Vec3d(var22.lastTickPosX, var22.lastTickPosY, var22.lastTickPosZ), this.renderEntity.getPositionVector(), var10);
                  Vec3d var24 = var11.subtract(var12);
                  float var14 = (float)VectorMath.rotateByYaw(var24, this.renderEntity.renderYawOffset).z;
                  float var10000 = (float)Math.atan2(var24.y, var14);
               }
            }
            break;
         case "weapon":
            if (this.renderEntity.ap) {
               GlStateManager.pushMatrix();
               Tessellator.getInstance().draw();
               com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(MATRIX_STACK, var3);
               GL11.glEnable(2896);
               GlStateManager.scale(1.5, 1.0, 2.0);
               GlStateManager.translate(0.0, 0.0, 0.05);
               GlStateManager.rotate(110.0F, 1.0F, 0.0F, 0.0F);
               Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, new ItemStack(Items.IRON_SWORD), TransformType.THIRD_PERSON_RIGHT_HAND);
               this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
               var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               GL11.glDisable(2896);
               GlStateManager.popMatrix();
            }
            break;
         case "tongue":
            this.renderPussyLickingBone(var1, var3);
            break;
         case "mangTongue":
            this.renderMorningBlowjobBone(var1, var3);
            break;
         case "head3":
            this.handleMorningPose(var3);
            break;
         case "irisL":
         case "irisR":
            this.handleMorningBlowjob(var3);
            break;
         case "irsisFaceR2":
         case "irsisFaceR3":
            this.handleBlowjobState(var3);
            break;
         case "armL":
         case "armR":
            if (this.renderEntity.getCurrentAction() == Action.RAPE_CHARGE) {
               EntityLivingBase var9 = this.renderEntity.getTargetEntity();
               if (var9 != null) {
                  float var15 = this.renderEntity.renderYawOffset;
                  Vec3d var13 = var9.getPositionVector().subtract(this.renderEntity.getPositionVector());
                  var13 = VectorMath.rotateByYaw(var13, var15);
                  double var16 = -ThreadNames.clampDouble(var13.x, -1.0, 1.0);
                  var3.setRotationZ(var3.getRotationZ() + TrigMath.toRadians(45.0 * var16));
               }
            }
      }

      if (this.renderEntity.isHuggingManglelie()) {
         ManglelieRenderer.applyBoneTransform(this.renderEntity, var2, var3, true);
      }
   }

   /**
    * Tongue-bone effect: pussy-licking/masturbating actions draw the sword
    * ribbon; the morning-blowjob draws its own ribbon (with fade-out while
    * {@code aD} is active).
    */
   void renderPussyLickingBone(BufferBuilder var1, GeoBone var2) {
      if (Action.isAnyAction(this.renderEntity, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING)) {
         this.renderSwordBone(var1, var2);
      } else if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         this.renderBoneBlowjob(var1, var2);
      }
   }

   void renderMorningBlowjobBone(BufferBuilder var1, GeoBone var2) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW) || this.renderEntity.aD) {
         float var3 = this.renderEntity.aD ? 1.0F - Math.min(0.29F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.29F : 1.0F;
         this.renderBoneAction(var1, var2, var3);
         this.bindTexture(ManglelieNpcModel.MANGLELIE_TEXTURE);
      }
   }

   /**
    * Sway for the morning-blowjob head pose: slow sine on yaw/pitch, scaled
    * down while the intro ({@code aD}) fades in.
    */
   void handleMorningPose(GeoBone var1) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW, Action.MORNING_BLOWJOB_FAST)) {
         if (!mc.isGamePaused()) {
            float var2 = mc.player.ticksExisted + mc.getRenderPartialTicks();
            float var3 = (float)(Math.sin(var2 * 0.1F) * 0.1F) + 0.2F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.renderEntity.aD) {
               float var5 = 1.0F - Math.min(0.5F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   /**
    * Head sway for the morning-blowjob (counter-sine to the head3 pose).
    */
   void handleBlowjobBone(GeoBone var1) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW, Action.MORNING_BLOWJOB_FAST)) {
         if (!mc.isGamePaused()) {
            float var2 = mc.player.ticksExisted + mc.getRenderPartialTicks();
            float var3 = (float)Math.sin(var2 * -0.1F) * 0.1F;
            float var4 = (float)Math.sin(var2 * 0.1F) * 0.1F;
            if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
               var1.setRotationY(var1.getRotationY() + var3);
               var1.setRotationZ(var1.getRotationZ() + var4);
            } else if (this.renderEntity.aD) {
               float var5 = Math.min(0.5F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.5F;
               var1.setRotationY(var1.getRotationY() + var3 * var5);
               var1.setRotationZ(var1.getRotationZ() + var4 * var5);
            }
         }
      }
   }

   void handleMorningBlowjob(GeoBone var1) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         if (!mc.isGamePaused()) {
            float var2 = mc.player.ticksExisted + mc.getRenderPartialTicks();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.1F));
         }
      }
   }

   void handleBlowjobState(GeoBone var1) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         if (!mc.isGamePaused()) {
            float var2 = mc.player.ticksExisted + mc.getRenderPartialTicks();
            var1.setPositionX((float)(var1.getPositionX() + Math.sin(var2 * 0.1F) * -0.15F));
         }
      }
   }

   /**
    * Ribbon animation driven by the action's time scale: length/width pulse
    * with a phase-offset cosine (used for the sword/licking effects).
    */
   void renderBoneAction(BufferBuilder var1, GeoBone var2, float var3) {
      float var4 = Action.getActionTimeScale(this.renderEntity, mc.getRenderPartialTicks());
      float var5 = var3 * (float)(0.02F * (-0.4F * Math.cos((Math.PI * 2) * var4 + 1.05) + 0.6F));
      RibbonRenderer.RibbonConfig var6 = new RibbonRenderer.RibbonConfig(
         RIBBON_COLOR_A,
         0.0F,
         12,
         var5,
         (var2x, var3x) -> var3 * (float)(Math.cos((Math.PI * 2) * var4 + 0.35F + -0.2F * var2x) * -10.0),
         (var0, var1x) -> 0.0F,
         (var2x, var3x) -> var3 * (float)(Math.cos((Math.PI * 2) * var4 + 1.25 + -0.1F * var2x) * -5.0),
         0.03F,
         0.005F
      );
      this.renderBoneRibbon(var1, var2, var6);
   }

   void renderBoneBlowjob(BufferBuilder var1, GeoBone var2) {
      float var3 = Action.getActionTimeScale(this.renderEntity, mc.getRenderPartialTicks());
      RibbonRenderer.RibbonConfig var4 = new RibbonRenderer.RibbonConfig(
         RIBBON_COLOR_A,
         0.0F,
         12,
         0.02F,
         (var1x, var2x) -> (float)(Math.cos((Math.PI * 2) * var3 + -0.2F * var1x) * 15.0),
         (var1x, var2x) -> (float)(Math.cos((Math.PI * 2) * var3 + -0.2F * var1x) * 5.0),
         (var0, var1x) -> 0.0F,
         0.03F,
         0.005F
      );
      this.renderBoneRibbon(var1, var2, var4);
   }

   /**
    * Sword ribbon: full config at progress 0, shrunk to nothing at progress 1,
    * interpolated in between — matches the sword attack swing.
    */
   void renderSwordBone(BufferBuilder var1, GeoBone var2) {
      float var3 = this.renderEntity.getSwordAttackProgress(mc.getRenderPartialTicks());
      if (var3 == 0.0F) {
         this.renderBoneRibbon(var1, var2, RIBBON_CONFIG_A);
      } else if (var3 == 1.0F) {
         this.renderBoneRibbon(var1, var2, RIBBON_CONFIG_B);
      } else {
         RibbonRenderer.RibbonConfig var4 = RIBBON_CONFIG_A.copy();
         var4.length = RotationHelper.lerp(RIBBON_CONFIG_A.length, 0.0F, var3);
         var4.width = RotationHelper.lerp(RIBBON_CONFIG_A.width, 0.0F, var3);
         this.renderBoneRibbon(var1, var2, var4);
      }
   }

   /**
    * Renders a ribbon at the bone: flush pending vertices, apply the bone
    * transform, disable culling, draw the ribbon mesh, restore texture/buffer.
    */
   void renderBoneRibbon(BufferBuilder var1, GeoBone var2, RibbonRenderer.RibbonConfig var3) {
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(MATRIX_STACK, var2);
      GlStateManager.disableCull();
      this.bindTexture(LINE_TEXTURE);
      RibbonRenderer.renderRibbon(var1, Tessellator.getInstance(), mc, var3);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
      var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   /**
    * GIVE_COIN effect on the coin bone: renders the coin cubes, then its child
    * with a full-bright lightmap that lerps 120->240 over the action's
    * 105..125-tick window while the coin color lerps dark->bright, then
    * restores the lightmap.
    */
   void renderBoneEffect(BufferBuilder var1, GeoBone var2, GalathEntity var3, float var4) {
      if (var3.getCurrentAction() == Action.GIVE_COIN) {
         tempBuffer = var1;
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var2);
         MATRIX_STACK.moveToPivot(var2);
         MATRIX_STACK.rotate(var2);
         MATRIX_STACK.scale(var2);
         MATRIX_STACK.moveBackFromPivot(var2);
         if (!this.activeCustomPartBones.contains(var2.getName())) {
            for (GeoCube var6 : var2.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = var2;
               this.renderCubeGeometry(var1, var6, 1.0F, 1.0F, 1.0F, 1.0F, (double)0.0);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }
         }

         Tessellator.getInstance().draw();
         GeoBone var14 = var2.childBones.get(0);
         var1.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         GL11.glDisable(2896);
         float var15 = ThreadNames.clampFloat(Action.GIVE_COIN.ticksPlaying[1] + var4, 105.0F, 125.0F);
         float var7 = (var15 - 105.0F) / 20.0F;
         float var8 = RotationHelper.lerp(120.0F, 240.0F, var7);
         Vector3fSexmodSpecial var9 = RotationHelper.lerpVector3f(GalathCoinRenderer.COIN_COLOR_DARK, GalathCoinRenderer.COIN_COLOR, var7);
         float var10 = OpenGlHelper.lastBrightnessX;
         float var11 = OpenGlHelper.lastBrightnessY;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var8, var8);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(var14);
         MATRIX_STACK.moveToPivot(var14);
         MATRIX_STACK.rotate(var14);
         MATRIX_STACK.scale(var14);
         MATRIX_STACK.moveBackFromPivot(var14);
         if (!this.activeCustomPartBones.contains(var14.getName())) {
            for (GeoCube var13 : var14.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = var14;
               this.renderCubeGeometry(var1, var13, var9.x, var9.y, var9.z, 1.0F, (double)0.0);
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

   /**
    * Pins all yaw fields to the movement yaw while running, so Galath faces
    * her travel direction.
    */
   protected Vec3d getBoneWorldPosGalath(GalathEntity var1, float var2, Vec3d var3) {
      if (var1.getCurrentAction() == Action.RUN) {
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
