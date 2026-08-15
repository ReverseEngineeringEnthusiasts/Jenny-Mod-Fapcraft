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
      (progress, time) -> (float)(Math.sin(time * 0.3 + -0.2 * progress) * 15.0),
      (progress, time) -> (float)(Math.sin(time * -0.15 + -0.2 * progress) * 3.0),
      (progress, time) -> 0.0F,
      0.03F,
      0.005F
   );
   static final RibbonRenderer.RibbonConfig RIBBON_CONFIG_B = new RibbonRenderer.RibbonConfig(
      RIBBON_COLOR_A,
      0.0F,
      12,
      0.0F,
      (progress, time) -> (float)(Math.sin(time * 0.3 + -0.2 * progress) * 15.0),
      (progress, time) -> (float)(Math.sin(time * -0.15 + -0.2 * progress) * 3.0),
      (progress, time) -> 0.0F,
      0.03F,
      0.005F
   );
   boolean initialized = false;
   float animationProgress = 0.0F;

   public GalathRenderer(RenderManager renderManager, AnimatedGeoModel model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }

   /**
    * Wing color while Galath is corrupted ({@code bb} flag): {@code null} in
    * the preload world or when corrupted (use default), else black
    * ({@link #ZERO_OFFSET}).
    */
   @Nullable
   protected Vector3fSexmodSpecial getWingColor(GalathEntity galath) {
      if (galath.world instanceof SexWorldClient) {
         return null;
      } else {
         return galath.bb ? null : ZERO_OFFSET;
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
   protected void drawOverlayLines(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial color, float lineWidth) {
      renderGirlTint(tessellator, buffer, girl, color, lineWidth);
   }

   protected void renderMasterbateEffect(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.MASTERBATE) {
         float yaw = galath.getYawRotation();
         galath.rotationYaw = yaw;
         galath.prevRenderYawOffset = yaw;
         galath.renderYawOffset = yaw;
         galath.prevRotationYawHead = yaw;
         galath.rotationYawHead = yaw;
      }
   }

   /**
    * Main render: resolves the dash target position (see
    * {@link #getDashPosition}) and flight/rape-charge yaw effects, then runs
    * the normal girl pipeline followed by the dash POV (wing meshes + Galath
    * geometry) and, while hugging Manglelie, her POV render.
    */
   public void doRenderGalath(GalathEntity galath, double x, double y, double z, float entityYaw, float partialTicks) {
      Vec3d dashPos = getDashPosition(galath, partialTicks);
      if (dashPos != null) {
         galath.setTargetPositionDirect(dashPos);
      }

      galath.aG = dashPos;
      GalathEntity.getAimYaw(galath, partialTicks);
      this.renderFlightEffect(galath);
      this.renderRapeCharge(galath);
      super.doRenderEntity(galath, x, y, z, entityYaw, partialTicks);
      renderDashPov(galath, partialTicks);
      if (galath.isHuggingManglelie()) {
         ManglelieRenderer.renderMangleliePov(galath, partialTicks);
      }
   }

   void renderRapeCharge(GalathEntity galath) {
      if (galath.getCurrentAction() == Action.RAPE_CHARGE) {
         galath.renderYawOffset = galath.getYawRotation();
         galath.prevRenderYawOffset = galath.renderYawOffset;
      }
   }

   /**
    * Flight yaw effect: when the flight flag is set and Galath is moving, the
    * yaw follows her movement direction (atan2 of the delta); when hovering
    * it holds the last direction, so she doesn't spin while bobbing.
    */
   void renderFlightEffect(GalathEntity galath) {
      if ((Boolean)galath.getDataManager().get(GalathEntity.bP)) {
         Vec3d lastPos = new Vec3d(galath.lastTickPosX, galath.lastTickPosY, galath.lastTickPosZ);
         Vec3d motion = galath.getPositionVector().subtract(lastPos);
         boolean isHovering = Math.abs(motion.x) + Math.abs(motion.z) < 0.05F;
         if (isHovering) {
            galath.renderYawOffset = this.animationProgress;
            galath.prevRenderYawOffset = this.animationProgress;
         } else {
            float yaw = (float)(TrigMath.sinDegrees(Math.atan2(motion.z, motion.x)) - 90.0);
            galath.renderYawOffset = yaw;
            galath.prevRenderYawOffset = yaw;
            this.animationProgress = yaw;
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
   public static Vec3d getDashPosition(GalathEntity galath, float partialTicks) {
      float dashProgress = galath.az();
      if (dashProgress == -1.0F) {
         galath.af = -1L;
         galath.aH = -1L;
         return null;
      }

      EntityLivingBase target = galath.getTargetEntity();
      if (target == null) {
         return null;
      }

      Vec3d targetPos = RotationHelper.lerpVec3dDouble(new Vec3d(target.prevPosX, target.prevPosY, target.prevPosZ), target.getPositionVector(), partialTicks);
      if (dashProgress == 24.0F && galath.af == -1L) {
         galath.af = mc.world.getTotalWorldTime();
         galath.aH = galath.af + 8L;
      }

      if (ThreadNames.isBetween(dashProgress, 24.0, 32.0)) {
         Vec3d approachOffset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 3.0), galath.getYawRotation() + 180.0F);
         Vec3d startPos = galath.B_clash642();
         Vec3d endPos = targetPos.add(0.0, target.getEyeHeight(), 0.0).add(approachOffset);
         float dashProgressT = ((float)mc.world.getTotalWorldTime() + partialTicks - (float)galath.af) / (float)(galath.aH - galath.af);
         return RotationHelper.lerpVec3dDouble(startPos, endPos, dashProgressT);
      } else if (ThreadNames.isBetween(dashProgress, 32.0, 54.0)) {
         Vec3d holdOffset = VectorMath.rotateByYaw(new Vec3d(0.0, 0.0, 1.5), galath.getYawRotation() + 180.0F);
         return targetPos.add(holdOffset);
      } else {
         return null;
      }
   }

   /**
    * First-person dash POV: draws the Galath geometry, wing effect, wing
    * geometry and wing mesh at the local player (culling/lighting disabled
    * around the pass). This is what the player sees during the dash scene.
    */
   public static void renderDashPov(BaseGirlEntity girl, float partialTicks) {
      EntityPlayerSP player = mc.player;
      if (player != null) {
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         GlStateManager.pushMatrix();
         GalathGeometryRender.renderGalathGeometry(mc, girl, partialTicks);
         mc.getTextureManager().bindTexture(LINE_TEXTURE);
         GlStateManager.disableCull();
         GlStateManager.disableLighting();
         renderWingEffect(girl, buffer, tessellator, RotationHelper.lerp(girl.prevRenderYawOffset, girl.renderYawOffset, partialTicks));
         renderWingGeometry(girl, buffer, tessellator, partialTicks);
         renderWingMesh(girl, buffer, tessellator);
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
   static void renderWingGeometry(BaseGirlEntity girl, BufferBuilder buffer, Tessellator tessellator, float partialTicks) {
      if (girl instanceof GalathEntity) {
         if ((Boolean)girl.getDataManager().get(GalathEntity.bP)) {
            if (!(Boolean)girl.getDataManager().get(GalathEntity.HIDE_EFFECTS_FLAG)) {
               GlStateManager.pushMatrix();
               Vec3d starsOffset = girl.getCachedBoneOffset("stars");
               GlStateManager.translate(starsOffset.x, starsOffset.y, starsOffset.z);
               float time = (float)mc.world.getTotalWorldTime() + partialTicks;
               float rotX = (float)(Math.sin(time * 0.2) * 5.0);
               float rotZ = (float)(Math.cos(time * 0.2) * 5.0);
               float rotY = (float)(time * 3.0);
               GlStateManager.rotate(rotX, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate(rotY, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate(rotZ, 0.0F, 0.0F, 1.0F);
               float step = TrigMath.toRadians(9.0);
               Vector3fSexmodSpecial starColor = GalathEntity.aa;
               mc.getTextureManager().bindTexture(LINE_TEXTURE);
               buffer.begin(3, DefaultVertexFormats.POSITION_TEX_COLOR);
               GlStateManager.glLineWidth(getRenderOffset(girl, partialTicks, 1.0F, 3.0F));

               for (float angle = 0.0F; angle < Math.PI * 2; angle += step) {
                  double sinAngle = Math.sin(angle) * 0.3F;
                  double cosAngle = Math.cos(angle) * 0.3F;
                  buffer.pos(sinAngle, 0.0, cosAngle).tex(0.0, 0.0).color(starColor.x, starColor.y, starColor.z, 1.0F).endVertex();
               }

               tessellator.draw();
               mc.getTextureManager().bindTexture(STAR_TEXTURE);
               buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
               step = TrigMath.toRadians(60.0);

               for (float angle2 = 0.0F; angle2 < Math.PI * 2; angle2 += step) {
                  double sinAngle2 = Math.sin(angle2) * 0.3F;
                  double cosAngle2 = Math.cos(angle2) * 0.3F;
                  buffer.pos(sinAngle2 - 0.1F, 0.1F, cosAngle2).tex(0.0, 0.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  buffer.pos(sinAngle2 + 0.1F, 0.1F, cosAngle2).tex(1.0, 0.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  buffer.pos(sinAngle2 + 0.1F, -0.1F, cosAngle2).tex(1.0, 1.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
                  buffer.pos(sinAngle2 - 0.1F, -0.1F, cosAngle2).tex(0.0, 1.0).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
               }

               tessellator.draw();
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
   static void renderWingEffect(BaseGirlEntity girl, BufferBuilder buffer, Tessellator tessellator, float partialTicks) {
      if (girl.getCurrentAction() != Action.GIVE_COIN || Action.GIVE_COIN.ticksPlaying[1] <= 100) {
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
         Vec3d[][] rightMesh = GalathGeometryRender.buildBodyBoneMesh(girl, partialTicks, "hairStrandStartR", "hairStrandMidR", "hairStrandEndR", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         Vec3d[][] leftMesh = GalathGeometryRender.buildBodyBoneMesh(girl, partialTicks, "hairStrandStartL", "hairStrandMidL", "hairStrandEndL", 0.0296875F, 0.06484375F, 0.026124999F, 0.0570625F, "head");
         GalathGeometryRender.renderMesh(buffer, rightMesh, RIBBON_COLOR_B);
         GalathGeometryRender.renderMesh(buffer, leftMesh, RIBBON_COLOR_B);
         tessellator.draw();
      }
   }

   /**
    * Renders the animated wing mesh: two 14-point line strips built from the
    * cached {@code wingRV<0..13>}/{@code wingLV<0..13>} bone offsets, drawn
    * with the Galath texture. Only when wings are animated.
    */
   static void renderWingMesh(BaseGirlEntity girl, BufferBuilder buffer, Tessellator tessellator) {
      if (((IGalath)girl).areWingsAnimated()) {
         mc.getTextureManager().bindTexture(GalathNpcModel.GALATH_TEXTURE);
         Vec3d[] rightWing = new Vec3d[14];
         Vec3d[] leftWing = new Vec3d[14];

         for (int i = 0; i < 14; i++) {
            rightWing[i] = girl.getCachedBoneOffset("wingRV" + i);
            leftWing[i] = girl.getCachedBoneOffset("wingLV" + i);
         }

         renderLineStrip(buffer, tessellator, rightWing);
         renderLineStrip(buffer, tessellator, leftWing);
      }
   }

   /**
    * Emits the two wing strip triangles (quads 0-1-2-11-12-13 and 3..10) with
    * the ribbon texture UVs.
    */
   static void renderLineStrip(BufferBuilder buffer, Tessellator tessellator, Vec3d[] points) {
      buffer.begin(4, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(points[0].x, points[0].y, points[0].z)
         .tex(TEXTURE_UV_A.x, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[1].x, points[1].y, points[1].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[2].x, points[2].y, points[2].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[11].x, points[11].y, points[11].z)
         .tex(TEXTURE_UV_A.x, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[12].x, points[12].y, points[12].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[13].x, points[13].y, points[13].z)
         .tex(TEXTURE_UV_A.x + 0.125F, TEXTURE_UV_A.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      tessellator.draw();
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
      buffer.pos(points[3].x, points[3].y, points[3].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[4].x, points[4].y, points[4].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[5].x, points[5].y, points[5].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[6].x, points[6].y, points[6].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[7].x, points[7].y, points[7].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[8].x, points[8].y, points[8].z)
         .tex(TEXTURE_UV_B.x, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[9].x, points[9].y, points[9].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y)
         .color(255, 255, 255, 255)
         .endVertex();
      buffer.pos(points[10].x, points[10].y, points[10].z)
         .tex(TEXTURE_UV_B.x + 0.125F, TEXTURE_UV_B.y + 0.125F)
         .color(255, 255, 255, 255)
         .endVertex();
      tessellator.draw();
   }

   /**
    * Galath's model pass: renders body, then the coin bone effect (GIVE_COIN),
    * then the steve (skin) bone and the body2 bone with Manglelie's texture —
    * each in its own buffer flush.
    */
   protected void renderModelBuffer(GeoModel model, BufferBuilder buffer, GalathEntity galath, float r, float g, float b, float a, float scale) {
      GeoBone topBone = model.topLevelBones.get(0);
      GeoBone bodyBone = null;
      GeoBone coinBone = null;
      GeoBone steveBone = null;
      GeoBone body2Bone = null;

      for (GeoBone bone : topBone.childBones) {
         switch (bone.getName()) {
            case "steve":
               steveBone = bone;
               break;
            case "body":
               bodyBone = bone;
               break;
            case "coin":
               coinBone = bone;
               break;
            case "body2":
               body2Bone = bone;
         }
      }

      MATRIX_STACK.push();
      MATRIX_STACK.translate(topBone);
      MATRIX_STACK.moveToPivot(topBone);
      MATRIX_STACK.rotate(topBone);
      MATRIX_STACK.scale(topBone);
      MATRIX_STACK.moveBackFromPivot(topBone);
      this.renderRecursively(buffer, bodyBone, r, g, b, a);
      Tessellator.getInstance().draw();
      this.renderBoneEffect(buffer, coinBone, galath, scale);
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);

      Minecraft.getMinecraft().renderEngine.bindTexture(this.getEntityTexture(this.renderEntity));

      this.renderRecursively(buffer, steveBone, r, g, b, this.renderEntity.getRenderScaleFactor());
      Tessellator.getInstance().draw();
      if (body2Bone != null) {
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         Minecraft.getMinecraft().renderEngine.bindTexture(ManglelieNpcModel.MANGLELIE_TEXTURE);
         this.renderRecursively(buffer, body2Bone, r, g, b, this.renderEntity.getRenderScaleFactor());
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
   protected void onBoneProcessing(BufferBuilder buffer, String boneName, GeoBone bone) {
      switch (boneName) {
         case "hairBack":
            if (!mc.isGamePaused()) {
               IBone headBone2 = this.renderEntity.getAnimationProcessor().getBone("head");
               float headPitchDegrees2 = TrigMath.toDegrees(headBone2.getRotationX());
               if (headPitchDegrees2 < 0.0F) {
                  bone.setRotationX(TrigMath.wrapDegrees(-headPitchDegrees2));
               } else {
                  float tiltProgress = Math.min(1.0F, headPitchDegrees2 / 45.0F);
                  bone.setRotationX(TrigMath.wrapDegrees(-headPitchDegrees2));
                  bone.setPositionY(bone.getPositionY() + tiltProgress * 1.5F);
               }
            }
            break;
         case "hairDownSideL":
         case "hairDownSideR":
            if (!mc.isGamePaused()) {
               IBone headBone = this.renderEntity.getAnimationProcessor().getBone("head");
               float headPitchDegrees = TrigMath.toDegrees(headBone.getRotationX());
               if (headPitchDegrees < 0.0F) {
                  bone.setRotationX(TrigMath.wrapDegrees(-headPitchDegrees / 2.0F));
               } else {
                  float tiltProgress2 = Math.min(1.0F, headPitchDegrees / 45.0F);
                  bone.setRotationX(TrigMath.wrapDegrees(-headPitchDegrees));
                  bone.setPositionY(bone.getPositionY() + tiltProgress2);
               }
            }
            break;
         case "head":
            this.handleBlowjobBone(bone);
            Action action = this.renderEntity.getCurrentAction();
            if (action == Action.FLY || action == Action.ATTACK_SWORD) {
               EntityLivingBase target2 = this.renderEntity.getTargetEntity();
               if (target2 != null) {
                  float partialTicks = mc.getRenderPartialTicks();
                  Vec3d girlPos = RotationHelper.lerpVec3dDouble(new Vec3d(this.renderEntity.lastTickPosX, this.renderEntity.lastTickPosY, this.renderEntity.lastTickPosZ), this.renderEntity.getPositionVector(), partialTicks);
                  Vec3d targetPos = RotationHelper.lerpVec3dDouble(new Vec3d(target2.lastTickPosX, target2.lastTickPosY, target2.lastTickPosZ), this.renderEntity.getPositionVector(), partialTicks);
                  Vec3d delta2 = girlPos.subtract(targetPos);
                  float forwardZ = (float)VectorMath.rotateByYaw(delta2, this.renderEntity.renderYawOffset).z;
                  float unusedAimPitch = (float)Math.atan2(delta2.y, forwardZ);
               }
            }
            break;
         case "weapon":
            if (this.renderEntity.ap) {
               GlStateManager.pushMatrix();
               Tessellator.getInstance().draw();
               com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(MATRIX_STACK, bone);
               GL11.glEnable(2896);
               GlStateManager.scale(1.5, 1.0, 2.0);
               GlStateManager.translate(0.0, 0.0, 0.05);
               GlStateManager.rotate(110.0F, 1.0F, 0.0F, 0.0F);
               Minecraft.getMinecraft().getItemRenderer().renderItem(this.renderEntity, new ItemStack(Items.IRON_SWORD), TransformType.THIRD_PERSON_RIGHT_HAND);
               this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
               buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
               GL11.glDisable(2896);
               GlStateManager.popMatrix();
            }
            break;
         case "tongue":
            this.renderPussyLickingBone(buffer, bone);
            break;
         case "mangTongue":
            this.renderMorningBlowjobBone(buffer, bone);
            break;
         case "head3":
            this.handleMorningPose(bone);
            break;
         case "irisL":
         case "irisR":
            this.handleMorningBlowjob(bone);
            break;
         case "irsisFaceR2":
         case "irsisFaceR3":
            this.handleBlowjobState(bone);
            break;
         case "armL":
         case "armR":
            if (this.renderEntity.getCurrentAction() == Action.RAPE_CHARGE) {
               EntityLivingBase target = this.renderEntity.getTargetEntity();
               if (target != null) {
                  float yaw = this.renderEntity.renderYawOffset;
                  Vec3d delta = target.getPositionVector().subtract(this.renderEntity.getPositionVector());
                  delta = VectorMath.rotateByYaw(delta, yaw);
                  double clampedX = -ThreadNames.clampDouble(delta.x, -1.0, 1.0);
                  bone.setRotationZ(bone.getRotationZ() + TrigMath.toRadians(45.0 * clampedX));
               }
            }
      }

      if (this.renderEntity.isHuggingManglelie()) {
         ManglelieRenderer.applyBoneTransform(this.renderEntity, boneName, bone, true);
      }
   }

   /**
    * Tongue-bone effect: pussy-licking/masturbating actions draw the sword
    * ribbon; the morning-blowjob draws its own ribbon (with fade-out while
    * {@code aD} is active).
    */
   void renderPussyLickingBone(BufferBuilder buffer, GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.PUSSY_LICKING, Action.MASTERBATE_SITTING)) {
         this.renderSwordBone(buffer, bone);
      } else if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         this.renderBoneBlowjob(buffer, bone);
      }
   }

   void renderMorningBlowjobBone(BufferBuilder buffer, GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW) || this.renderEntity.aD) {
         float fade = this.renderEntity.aD ? 1.0F - Math.min(0.29F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.29F : 1.0F;
         this.renderBoneAction(buffer, bone, fade);
         this.bindTexture(ManglelieNpcModel.MANGLELIE_TEXTURE);
      }
   }

   /**
    * Sway for the morning-blowjob head pose: slow sine on yaw/pitch, scaled
    * down while the intro ({@code aD}) fades in.
    */
   void handleMorningPose(GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW, Action.MORNING_BLOWJOB_FAST)) {
         if (!mc.isGamePaused()) {
            float time = mc.player.ticksExisted + mc.getRenderPartialTicks();
            float yawSway = (float)(Math.sin(time * 0.1F) * 0.1F) + 0.2F;
            float pitchSway = (float)Math.sin(time * 0.1F) * 0.1F;
            if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
               bone.setRotationY(bone.getRotationY() + yawSway);
               bone.setRotationZ(bone.getRotationZ() + pitchSway);
            } else if (this.renderEntity.aD) {
               float fade = 1.0F - Math.min(0.5F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.5F;
               bone.setRotationY(bone.getRotationY() + yawSway * fade);
               bone.setRotationZ(bone.getRotationZ() + pitchSway * fade);
            }
         }
      }
   }

   /**
    * Head sway for the morning-blowjob (counter-sine to the head3 pose).
    */
   void handleBlowjobBone(GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW, Action.MORNING_BLOWJOB_FAST)) {
         if (!mc.isGamePaused()) {
            float time = mc.player.ticksExisted + mc.getRenderPartialTicks();
            float yawSway = (float)Math.sin(time * -0.1F) * 0.1F;
            float pitchSway = (float)Math.sin(time * 0.1F) * 0.1F;
            if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
               bone.setRotationY(bone.getRotationY() + yawSway);
               bone.setRotationZ(bone.getRotationZ() + pitchSway);
            } else if (this.renderEntity.aD) {
               float fade = Math.min(0.5F, Action.getActionTickSeconds(this.renderEntity, mc.getRenderPartialTicks())) / 0.5F;
               bone.setRotationY(bone.getRotationY() + yawSway * fade);
               bone.setRotationZ(bone.getRotationZ() + pitchSway * fade);
            }
         }
      }
   }

   void handleMorningBlowjob(GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         if (!mc.isGamePaused()) {
            float time = mc.player.ticksExisted + mc.getRenderPartialTicks();
            bone.setPositionX((float)(bone.getPositionX() + Math.sin(time * 0.1F) * -0.1F));
         }
      }
   }

   void handleBlowjobState(GeoBone bone) {
      if (Action.isAnyAction(this.renderEntity, Action.MORNING_BLOWJOB_SLOW)) {
         if (!mc.isGamePaused()) {
            float time = mc.player.ticksExisted + mc.getRenderPartialTicks();
            bone.setPositionX((float)(bone.getPositionX() + Math.sin(time * 0.1F) * -0.15F));
         }
      }
   }

   /**
    * Ribbon animation driven by the action's time scale: length/width pulse
    * with a phase-offset cosine (used for the sword/licking effects).
    */
   void renderBoneAction(BufferBuilder buffer, GeoBone bone, float fadeScale) {
      float timeScale = Action.getActionTimeScale(this.renderEntity, mc.getRenderPartialTicks());
      float pulseWidth = fadeScale * (float)(0.02F * (-0.4F * Math.cos((Math.PI * 2) * timeScale + 1.05) + 0.6F));
      RibbonRenderer.RibbonConfig config = new RibbonRenderer.RibbonConfig(
         RIBBON_COLOR_A,
         0.0F,
         12,
         pulseWidth,
         (progress, time) -> fadeScale * (float)(Math.cos((Math.PI * 2) * timeScale + 0.35F + -0.2F * progress) * -10.0),
         (progress, time) -> 0.0F,
         (progress, time) -> fadeScale * (float)(Math.cos((Math.PI * 2) * timeScale + 1.25 + -0.1F * progress) * -5.0),
         0.03F,
         0.005F
      );
      this.renderBoneRibbon(buffer, bone, config);
   }

   void renderBoneBlowjob(BufferBuilder buffer, GeoBone bone) {
      float timeScale = Action.getActionTimeScale(this.renderEntity, mc.getRenderPartialTicks());
      RibbonRenderer.RibbonConfig config = new RibbonRenderer.RibbonConfig(
         RIBBON_COLOR_A,
         0.0F,
         12,
         0.02F,
         (progress, time) -> (float)(Math.cos((Math.PI * 2) * timeScale + -0.2F * progress) * 15.0),
         (progress, time) -> (float)(Math.cos((Math.PI * 2) * timeScale + -0.2F * progress) * 5.0),
         (progress, time) -> 0.0F,
         0.03F,
         0.005F
      );
      this.renderBoneRibbon(buffer, bone, config);
   }

   /**
    * Sword ribbon: full config at progress 0, shrunk to nothing at progress 1,
    * interpolated in between — matches the sword attack swing.
    */
   void renderSwordBone(BufferBuilder buffer, GeoBone bone) {
      float progress = this.renderEntity.getSwordAttackProgress(mc.getRenderPartialTicks());
      if (progress == 0.0F) {
         this.renderBoneRibbon(buffer, bone, RIBBON_CONFIG_A);
      } else if (progress == 1.0F) {
         this.renderBoneRibbon(buffer, bone, RIBBON_CONFIG_B);
      } else {
         RibbonRenderer.RibbonConfig config = RIBBON_CONFIG_A.copy();
         config.length = RotationHelper.lerp(RIBBON_CONFIG_A.length, 0.0F, progress);
         config.width = RotationHelper.lerp(RIBBON_CONFIG_A.width, 0.0F, progress);
         this.renderBoneRibbon(buffer, bone, config);
      }
   }

   /**
    * Renders a ribbon at the bone: flush pending vertices, apply the bone
    * transform, disable culling, draw the ribbon mesh, restore texture/buffer.
    */
   void renderBoneRibbon(BufferBuilder buffer, GeoBone bone, RibbonRenderer.RibbonConfig config) {
      GlStateManager.pushMatrix();
      Tessellator.getInstance().draw();
      com.trolmastercard.sexmod.MatrixHelper.applyBoneTransform(MATRIX_STACK, bone);
      GlStateManager.disableCull();
      this.bindTexture(LINE_TEXTURE);
      RibbonRenderer.renderRibbon(buffer, Tessellator.getInstance(), mc, config);
      this.bindTexture(Objects.requireNonNull(this.getEntityTexture(this.renderEntity)));
      buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
      GlStateManager.enableCull();
      GlStateManager.popMatrix();
   }

   /**
    * GIVE_COIN effect on the coin bone: renders the coin cubes, then its child
    * with a full-bright lightmap that lerps 120->240 over the action's
    * 105..125-tick window while the coin color lerps dark->bright, then
    * restores the lightmap.
    */
   void renderBoneEffect(BufferBuilder buffer, GeoBone coinBone, GalathEntity galath, float partialTicks) {
      if (galath.getCurrentAction() == Action.GIVE_COIN) {
         tempBuffer = buffer;
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(coinBone);
         MATRIX_STACK.moveToPivot(coinBone);
         MATRIX_STACK.rotate(coinBone);
         MATRIX_STACK.scale(coinBone);
         MATRIX_STACK.moveBackFromPivot(coinBone);
         if (!this.activeCustomPartBones.contains(coinBone.getName())) {
            for (GeoCube cube : coinBone.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = coinBone;
               this.renderCubeGeometry(buffer, cube, 1.0F, 1.0F, 1.0F, 1.0F, (double)0.0);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }
         }

         Tessellator.getInstance().draw();
         GeoBone childBone = coinBone.childBones.get(0);
         buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL);
         GL11.glDisable(2896);
         float coinTicks = ThreadNames.clampFloat(Action.GIVE_COIN.ticksPlaying[1] + partialTicks, 105.0F, 125.0F);
         float progress = (coinTicks - 105.0F) / 20.0F;
         float lightmap = RotationHelper.lerp(120.0F, 240.0F, progress);
         Vector3fSexmodSpecial coinColor = RotationHelper.lerpVector3f(GalathCoinRenderer.COIN_COLOR_DARK, GalathCoinRenderer.COIN_COLOR, progress);
         float lastBrightnessX = OpenGlHelper.lastBrightnessX;
         float lastBrightnessY = OpenGlHelper.lastBrightnessY;
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lightmap, lightmap);
         MATRIX_STACK.push();
         MATRIX_STACK.translate(childBone);
         MATRIX_STACK.moveToPivot(childBone);
         MATRIX_STACK.rotate(childBone);
         MATRIX_STACK.scale(childBone);
         MATRIX_STACK.moveBackFromPivot(childBone);
         if (!this.activeCustomPartBones.contains(childBone.getName())) {
            for (GeoCube cube2 : childBone.childCubes) {
               MATRIX_STACK.push();
               GlStateManager.pushMatrix();
               this.currentRenderingBone = childBone;
               this.renderCubeGeometry(buffer, cube2, coinColor.x, coinColor.y, coinColor.z, 1.0F, (double)0.0);
               GlStateManager.popMatrix();
               MATRIX_STACK.pop();
            }
         }

         MATRIX_STACK.pop();
         MATRIX_STACK.pop();
         Tessellator.getInstance().draw();
         GL11.glEnable(2896);
         OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, lastBrightnessX, lastBrightnessY);
      }
   }

   /**
    * Pins all yaw fields to the movement yaw while running, so Galath faces
    * her travel direction.
    */
   protected Vec3d getBoneWorldPosGalath(GalathEntity galath, float partialTicks, Vec3d pos) {
      if (galath.getCurrentAction() == Action.RUN) {
         float yaw = galath.getYawRotation();
         galath.rotationYaw = yaw;
         galath.prevRenderYawOffset = yaw;
         galath.renderYawOffset = yaw;
         galath.prevRotationYawHead = yaw;
         galath.rotationYawHead = yaw;
      }

      return pos;
   }

}
