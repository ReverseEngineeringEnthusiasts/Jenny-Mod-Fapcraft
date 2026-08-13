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

   public DynamicTrailRenderer(int var1, IPositionProvider var2, ITargetProvider var3, BaseGirlEntity var4, float var5, float var6) {
      this.maxSegmentsCount = var1;
      this.sourcePositionProvider = var2;
      this.targetPositionProvider = var3;
      this.ownerEntity = var4;
      this.randomnessRadius = var5;
      this.maxDistance = var6;
   }

   public void a(Minecraft var1, Tessellator var2, BufferBuilder var3, float var4) {
      if (this.trailSegments.size() < this.maxSegmentsCount) {
         for (int var5 = 0; var5 < 6; var5++) {
            Vec3d var6 = this.sourcePositionProvider.getPosition(this.ownerEntity);
            this.trailSegments
               .add(
                  new TrailSegment(
                     var1.world,
                     this.targetPositionProvider.getTargetPosition(this.ownerEntity),
                     new Vec3d(
                        var6.x + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius,
                        var6.y + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius,
                        var6.z + (Reference.RANDOM.nextFloat() * 2.0F - 1.0F) * this.randomnessRadius
                     )
                  )
               );
         }
      }

      GlStateManager.disableCull();
      GlStateManager.disableAlpha();
      Vec3d var10 = RotationHelper.a(
         new Vec3d(var1.player.lastTickPosX, var1.player.lastTickPosY, var1.player.lastTickPosZ),
         var1.player.getPositionVector(),
         var4
      );
      var3.begin(9, DefaultVertexFormats.POSITION_COLOR);
      this.b_clash450();
      Vec3d var11 = null;

      for (TrailSegment var8 : this.trailSegments) {
         Vec3d var9 = RotationHelper.a(var8.offset, var8.velocity, var4);
         if (var11 == null) {
            var11 = var9;
         }

         if (var11.distanceTo(var9) > this.maxDistance) {
            var2.draw();
            var3.begin(9, DefaultVertexFormats.POSITION_COLOR);
         }

         var3.pos(var9.x - var10.x, var9.y - var10.y, var9.z - var10.z)
            .color(255, 255, 255, 255)
            .endVertex();
         var11 = var9;
      }

      var2.draw();
      GlStateManager.enableCull();
   }

   public void a_clash449() {
      for (TrailSegment var2 : this.trailSegments) {
         var2.onUpdate();
      }
   }

   void b_clash450() {
      if (!this.trailSegments.isEmpty() && this.trailSegments.size() > 1) {
         for (int var1 = 1; var1 < this.trailSegments.size(); var1++) {
            TrailSegment var2 = this.trailSegments.get(var1);
            Vec3d var3 = var2.velocity;

            int var4;
            for (var4 = var1 - 1; var4 >= 0 && var3.distanceTo(this.trailSegments.get(var4).velocity) < var3.distanceTo(this.trailSegments.get(var4 + 1).velocity); var4--) {
               this.trailSegments.set(var4 + 1, this.trailSegments.get(var4));
            }

            this.trailSegments.set(var4 + 1, var2);
         }
      }
   }

}
