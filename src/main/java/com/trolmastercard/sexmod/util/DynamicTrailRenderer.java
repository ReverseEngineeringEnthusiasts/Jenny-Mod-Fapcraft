package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.api.ITargetProvider;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.Vec3d;

/**
 * <b>Role.</b> CLIENT-side ribbon/trail renderer used by girl scene effects
 * (energy tendrils etc.). Maintains a ring of {@link TrailSegment}s that are
 * spawned from the source position provider, drift toward the target provider
 * with jitter, and are drawn as a sorted translucent polyline with per-segment
 * partial-tick interpolation.
 * <p>
 * <b>Pitfalls.</b> {@code renderTrailSegments} insertion-sorts segments by
 * distance (bubble pass) so the strip renders back-to-front — reordering logic
 * must stay. {@link RotationHelper#lerpVec3dDouble} here is correct (render
 * interpolation by partial ticks); do not swap to the INT step variant.
 */
public class DynamicTrailRenderer {
   static final int MAX_TICKS_AGE = 30;
   static final int SEGMENTS_PER_TICK = 6;
   static final int BATCH_SIZE = 6;
   static final float ALPHA_THRESHOLD = 0.15F;
   List<TrailSegment> trailSegments = new ArrayList<>();
   final int maxSegmentsCount;
   final IPositionProvider sourcePositionProvider;
   final ITargetProvider targetPositionProvider;
   public final BaseGirlEntity ownerEntity;
   final float randomnessRadius;
   final float maxDistance;

   public DynamicTrailRenderer(int maxSegmentsCount, IPositionProvider sourcePositionProvider, ITargetProvider targetPositionProvider, BaseGirlEntity ownerEntity, float randomnessRadius, float maxDistance) {
      this.maxSegmentsCount = maxSegmentsCount;
      this.sourcePositionProvider = sourcePositionProvider;
      this.targetPositionProvider = targetPositionProvider;
      this.ownerEntity = ownerEntity;
      this.randomnessRadius = randomnessRadius;
      this.maxDistance = maxDistance;
   }

   public void renderTrail(Minecraft minecraft, Tessellator tessellator, BufferBuilder buffer, float partialTicks) {
      if (this.trailSegments.size() < this.maxSegmentsCount) {
         for (int i = 0; i < 6; i++) {
            Vec3d sourcePos = this.sourcePositionProvider.getPosition(this.ownerEntity);
            this.trailSegments
               .add(
                  new TrailSegment(
                     minecraft.world,
                     this.targetPositionProvider.getTargetPosition(this.ownerEntity),
                     new Vec3d(
                        sourcePos.x + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius,
                        sourcePos.y + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius,
                        sourcePos.z + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius
                     )
                  )
               );
         }
      }

      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      Vec3d targetPos = RotationHelper.lerpVec3dDouble(
         new Vec3d(minecraft.player.lastTickPosX, minecraft.player.lastTickPosY, minecraft.player.lastTickPosZ),
         minecraft.player.getPositionVector(),
         partialTicks
      );
      buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
      this.renderTrailSegments();
      Vec3d lastPos = null;

      for (TrailSegment segment : this.trailSegments) {
         Vec3d segmentPos = RotationHelper.lerpVec3dDouble(segment.offset, segment.velocity, partialTicks);
         if (lastPos == null) {
            lastPos = segmentPos;
         }

         if (lastPos.distanceTo(segmentPos) > this.maxDistance) {
            tessellator.draw();
            buffer.begin(9, DefaultVertexFormats.POSITION_COLOR);
         }

         buffer.pos(segmentPos.x - targetPos.x, segmentPos.y - targetPos.y, segmentPos.z - targetPos.z)
            .color(255, 255, 255, 255)
            .endVertex();
         lastPos = segmentPos;
      }

      tessellator.draw();
      GlStateManager.enableCull();
   }

   public void updateTrails() {
      for (TrailSegment segment : this.trailSegments) {
         segment.onUpdate();
      }
   }

   void renderTrailSegments() {
      if (!this.trailSegments.isEmpty() && this.trailSegments.size() > 1) {
         for (int i = 1; i < this.trailSegments.size(); i++) {
            TrailSegment segment = this.trailSegments.get(i);
            Vec3d velocity = segment.velocity;

            int j;
            for (j = i - 1; j >= 0 && velocity.distanceTo(this.trailSegments.get(j).velocity) < velocity.distanceTo(this.trailSegments.get(j + 1).velocity); j--) {
               this.trailSegments.set(j + 1, this.trailSegments.get(j));
            }

            this.trailSegments.set(j + 1, segment);
         }
      }
   }

}
