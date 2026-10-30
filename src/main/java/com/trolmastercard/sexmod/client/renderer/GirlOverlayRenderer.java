package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.Vector3fSexmodSpecial;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Colored line overlays drawn on top of a girl (corruption / buff tints, see
 * {@link GirlRenderer#renderGirlColor}).
 * <p>
 * <b>Geometry.</b> Lines are built from the girl's cached bone offsets
 * ({@link BaseGirlEntity#getCachedBoneOffset}), so they follow the animated
 * skeleton. {@link #renderGirlTint} draws the {@code braString*} chain;
 * {@link #renderGirlColor} translates the whole chain to the girl's render
 * position first and picks the {@code braString} tint. The caller owns the
 * color/texture state around them.
 * <p>
 * CLIENT-side render thread only: emits vertices into the shared tessellator
 * buffer, sets line width and pushes/pops one matrix.
 */
@SideOnly(Side.CLIENT)
final class GirlOverlayRenderer {
   private GirlOverlayRenderer() {
   }

   /**
    * Draws the colored line overlay (e.g. corrupted girl tint) anchored to the
    * girl (target position when anchored, else lerped) relative to the local
    * player, with line width from {@link #getRenderOffset}.
    */
   static void renderGirlColor(GirlRenderer<?> renderer, BaseGirlEntity girl, float partialTicks, Vector3fSexmodSpecial overlayColor) {
      EntityPlayerSP localPlayer = GirlRenderer.mc.player;
      overlayColor = new Vector3fSexmodSpecial(overlayColor.x / 255.0F, overlayColor.y / 255.0F, overlayColor.z / 255.0F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      GlStateManager.pushMatrix();
      try {
         GlStateManager.translate(0.0, 0.01, 0.0);
         Entity renderEntity = renderer.getRenderEntity(girl);
         Vec3d girlPos = girl.isAnchored()
            ? girl.getTargetPosition()
            : RotationHelper.lerpVec3dDouble(new Vec3d(renderEntity.lastTickPosX, renderEntity.lastTickPosY, renderEntity.lastTickPosZ), renderEntity.getPositionVector(), partialTicks);
         Vec3d localPlayerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
         Vec3d offset = girlPos.subtract(localPlayerPos);
         GlStateManager.translate(offset.x, offset.y, offset.z);
         GirlRenderer.mc.getTextureManager().bindTexture(GirlRenderer.LINE_TEXTURE);
         float lineWidth = getRenderOffset(girl, partialTicks, 1.0F, 5.0F);
         renderer.drawOverlayLines(tessellator, buffer, girl, overlayColor, lineWidth);
      } finally {
         GlStateManager.popMatrix();
      }
   }

   /**
    * Line width scaled by the girl's distance from the camera (5 blocks = max
    * width, closer = thinner), clamped 0..1 progress.
    */
   static float getRenderOffset(BaseGirlEntity girl, float partialTicks, float minWidth, float maxWidth) {
      EntityPlayerSP localPlayer = GirlRenderer.mc.player;
      Entity renderEntity = ((GirlRenderer)GirlRenderer.mc.getRenderManager().getEntityRenderObject(girl)).getRenderEntity(girl);
      Vec3d girlPos = girl.isAnchored()
         ? girl.getTargetPosition()
         : RotationHelper.lerpVec3dDouble(new Vec3d(renderEntity.lastTickPosX, renderEntity.lastTickPosY, renderEntity.lastTickPosZ), renderEntity.getPositionVector(), partialTicks);
      Vec3d playerPos = RotationHelper.lerpVec3dDouble(new Vec3d(localPlayer.lastTickPosX, localPlayer.lastTickPosY, localPlayer.lastTickPosZ), localPlayer.getPositionVector(), partialTicks);
      Vec3d cameraPos = ActiveRenderInfo.getCameraPosition().add(playerPos);
      float distance = (float)cameraPos.distanceTo(girlPos);
      float clampedDistance = Math.abs(distance) / 5.0F;
      return RotationHelper.lerp(maxWidth, minWidth, ThreadNames.clampFloat(clampedDistance, 0.0F, 1.0F));
   }

   /**
    * Draws a single colored line segment between two cached bone offsets.
    */
   static void renderNameLabel(BufferBuilder buffer, Tessellator tessellator, BaseGirlEntity girl, String startBone, String endBone, float r, float g, float b, float width) {
      buffer.begin(1, DefaultVertexFormats.POSITION_TEX_COLOR);
      GlStateManager.glLineWidth(width);
      Vec3d startPos = girl.getCachedBoneOffset(startBone);
      Vec3d endPos = girl.getCachedBoneOffset(endBone);
      buffer.pos(startPos.x, startPos.y, startPos.z)
         .tex(0.0, 0.0)
         .color(r, g, b, 1.0F)
         .endVertex();
      buffer.pos(endPos.x, endPos.y, endPos.z)
         .tex(0.0, 0.0)
         .color(r, g, b, 1.0F)
         .endVertex();
      tessellator.draw();
   }

   /**
    * Draws the bra-string tint chain (all {@code braString*} segments) in the
    * given color — used by the corruption/buff overlay renderers.
    */
   static void renderGirlTint(Tessellator tessellator, BufferBuilder buffer, BaseGirlEntity girl, Vector3fSexmodSpecial color, float width) {
      renderNameLabel(buffer, tessellator, girl, "braStringMidStartR", "braStringMidMid1R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid1R", "braStringMidMid2R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid2R", "braStringMidMid3R", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid3R", "braStringMidEndR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidEndR", "braStringBackR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringBackR", "braStringRightEndR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringRightEndR", "braStringRightStartR", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringRightR", "braStringRightL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidStartL", "braStringMidMid1L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid1L", "braStringMidMid2L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid2L", "braStringMidMid3L", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidMid3L", "braStringMidEndL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringMidEndL", "braStringBackL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringBackL", "braStringLeftEndL", color.x, color.y, color.z, width);
      renderNameLabel(buffer, tessellator, girl, "braStringLeftEndL", "braStringLeftStartL", color.x, color.y, color.z, width);
   }
}
