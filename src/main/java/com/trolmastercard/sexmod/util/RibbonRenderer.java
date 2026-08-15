package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.gui.UnknownScreen;
import java.util.ArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> CLIENT-side renderer for animated ribbons (scene effects, editor
 * previews). Builds a wavy quad strip whose per-segment rotation is driven by
 * three {@link WaveFunction}s evaluated over time, then draws it as a colored
 * triangle strip.
 * <p>
 * <b>Pitfall.</b> {@code renderRibbonStrip} draws 4 faces per segment pair and
 * assumes the caller began a {@code POSITION_COLOR} buffer — do not change the
 * vertex format or the strip tears.
 */
public class RibbonRenderer {
   public static void renderRibbon(BufferBuilder buffer, Tessellator tessellator, Minecraft minecraft, RibbonRenderer.RibbonConfig config) {
      Vec3d[] basePoints = new Vec3d[]{
         new Vec3d(-config.thickness, -config.advance, 0.0), new Vec3d(-config.thickness, config.advance, 0.0), new Vec3d(config.thickness, config.advance, 0.0), new Vec3d(config.thickness, -config.advance, 0.0)
      };
      Vec3d advanceVec = new Vec3d(0.0, 0.0, -config.length);
      Vec3d widthVec = VectorMath.scale(advanceVec.normalize(), config.width);
      Vec3d[] startQuad = new Vec3d[4];
      System.arraycopy(basePoints, 0, startQuad, 0, 4);
      ArrayList segments = new ArrayList();
      float ticks = minecraft.player.ticksExisted + minecraft.getRenderPartialTicks();

      for (int i = 0; i <= config.segmentCount; i++) {
         Vec3d[] quad = new Vec3d[4];
         float progress = 1.0F - (float)i / config.segmentCount;

         for (int j = 0; j < 4; j++) {
            Vec3d point = basePoints[j];
            quad[j] = new Vec3d(point.x * progress, point.y, point.z).add(widthVec);
         }

         segments.add(quad);
         advanceVec = VectorMath.rotateByEuler(advanceVec, config.xWaveFn.getPoint(i, ticks), config.yWaveFn.getPoint(i, ticks), config.zWaveFn.getPoint(i, ticks));
         widthVec = widthVec.add(advanceVec);
      }

      buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
      renderRibbonStrip(buffer, startQuad, (Vec3d[])segments.get(0), config.color);

      for (int i2 = 0; i2 < config.segmentCount - 1; i2++) {
         Vec3d[] quadA = (Vec3d[])segments.get(i2);
         Vec3d[] quadB = (Vec3d[])segments.get(i2 + 1);
         renderRibbonStrip(buffer, quadA, quadB, config.color);
      }

      tessellator.draw();
   }

   static float getWaveOffset(float x, float frequency, float time, int index, float amplitude) {
      return (float)(Math.sin(x * frequency + time * index) * amplitude);
   }

   static void renderRibbonStrip(BufferBuilder buffer, Vec3d[] quadA, Vec3d[] quadB, UnknownScreen color) {
      buffer.pos(quadA[1].x, quadA[1].y, quadA[1].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[2].x, quadA[2].y, quadA[2].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[2].x, quadB[2].y, quadB[2].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[1].x, quadB[1].y, quadB[1].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[0].x, quadA[0].y, quadA[0].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[1].x, quadA[1].y, quadA[1].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[1].x, quadB[1].y, quadB[1].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[0].x, quadB[0].y, quadB[0].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[2].x, quadA[2].y, quadA[2].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[3].x, quadA[3].y, quadA[3].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[3].x, quadB[3].y, quadB[3].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[2].x, quadB[2].y, quadB[2].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[0].x, quadA[0].y, quadA[0].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadA[3].x, quadA[3].y, quadA[3].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[3].x, quadB[3].y, quadB[3].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
      buffer.pos(quadB[0].x, quadB[0].y, quadB[0].z).color(color.red, color.green, color.blue, color.alpha).endVertex();
   }

   @FunctionalInterface
   public interface WaveFunction {
      float getPoint(int index, float time);
   }

   public static class RibbonConfig {
      public UnknownScreen color;
      public float width;
      public int segmentCount;
      public float length;
      public RibbonRenderer.WaveFunction xWaveFn;
      public RibbonRenderer.WaveFunction yWaveFn;
      public RibbonRenderer.WaveFunction zWaveFn;
      public float thickness;
      public float advance;

      public RibbonConfig(UnknownScreen color, float width, int segmentCount, float length, RibbonRenderer.WaveFunction xWaveFn, RibbonRenderer.WaveFunction yWaveFn, RibbonRenderer.WaveFunction zWaveFn, float thickness, float advance) {
         this.color = color;
         this.width = width;
         this.segmentCount = segmentCount;
         this.length = length;
         this.xWaveFn = xWaveFn;
         this.yWaveFn = yWaveFn;
         this.zWaveFn = zWaveFn;
         this.thickness = thickness;
         this.advance = advance;
      }

      public RibbonRenderer.RibbonConfig copy() {
         return new RibbonRenderer.RibbonConfig(this.color, this.width, this.segmentCount, this.length, this.xWaveFn, this.yWaveFn, this.zWaveFn, this.thickness, this.advance);
      }
   }
}
