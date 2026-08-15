package com.trolmastercard.sexmod.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * <b>Role.</b> One particle segment of a {@link DynamicTrailRenderer} trail:
 * integrates velocity/position with gravity + air resistance
 * ({@link #onUpdate}) and resolves block collisions by sliding along the
 * blocking axis (Bresenham line check), then parks the segment until reset.
 * <p>
 * <b>Pitfalls.</b> {@link #onUpdate} does NOTHING while {@code position} is
 * zero (parked state) — that is the collision stop, not a bug. The 0.95 decay /
 * 0.4905 gravity constants are tuned together; changing one without the other
 * alters trail length and bounce behavior. {@link #bresenhamLine} must include
 * both endpoints for the collision slide to be exact.
 */
public class TrailSegment {
   public static final float GRAVITY = 9.81F;
   public static final float TIME_DELTA = 0.05F;
   public static final float AIR_RESISTANCE = 0.05F;
   public static final float COLLISION_OFFSET = 0.03F;
   World world;
   Vec3d offset;
   Vec3d velocity;
   Vec3d position;

   public TrailSegment(World world, Vec3d velocity, Vec3d position) {
      this.world = world;
      this.velocity = velocity;
      this.offset = velocity;
      this.position = position;
   }

   public void onUpdate() {
      if (Vec3d.ZERO.equals(this.position)) {
         this.offset = this.velocity;
      } else {
         this.position = new Vec3d(this.position.x * 0.95F, (this.position.y - 0.49050003F) * 0.95F, this.position.z * 0.95F);
         this.offset = this.velocity;
         this.velocity = new Vec3d(
            this.velocity.x + this.position.x * 0.05F,
            this.velocity.y + this.position.y * 0.05F,
            this.velocity.z + this.position.z * 0.05F
         );
         BlockPos hitPos = new BlockPos(this.offset);
         BlockPos lastHitPos = null;

         for (BlockPos blockPos : bresenhamLine(new BlockPos(this.offset), new BlockPos(this.velocity))) {
            if (this.world.getBlockState(blockPos).getBlock() != Blocks.AIR) {
               lastHitPos = blockPos;
               break;
            }

            hitPos = blockPos;
         }

         if (lastHitPos != null) {
            int hitX = lastHitPos.getX();
            int lastX = hitPos.getX();
            if (hitX - lastX != 0) {
               double maxX = Math.max(hitX, lastX);
               double slopeXY = (this.offset.y - this.velocity.y) / (this.offset.x - this.velocity.x);
               double interceptY = this.velocity.y - slopeXY * this.velocity.x;
               double y = slopeXY * maxX + interceptY;
               double slopeZY = (this.offset.z - this.velocity.z) / (this.offset.x - this.velocity.x);
               double interceptZ = this.velocity.z - slopeZY * this.velocity.x;
               double z = slopeZY * maxX + interceptZ;
               this.velocity = new Vec3d(maxX + 0.03F * (hitX > lastX ? -1 : 1), y, z);
               this.position = new Vec3d(0.0, 0.0, 0.0);
            } else {
               int hitY = lastHitPos.getY();
               int lastY = hitPos.getY();
               if (hitY - lastY != 0) {
                  double maxY = Math.max(hitY, lastY);
                  double slopeXY2 = (this.offset.x - this.velocity.x) / (this.offset.y - this.velocity.y);
                  double x = this.velocity.x - slopeXY2 * this.velocity.y;
                  double x2 = slopeXY2 * maxY + x;
                  double slopeZY2 = (this.offset.z - this.velocity.z) / (this.offset.y - this.velocity.y);
                  double z2 = this.velocity.z - slopeZY2 * this.velocity.y;
                  double z3 = slopeZY2 * maxY + z2;
                  this.velocity = new Vec3d(x2, maxY + 0.03F * (hitY > lastY ? -1 : 1), z3);
                  this.position = new Vec3d(0.0, 0.0, 0.0);
               } else {
                  int hitZ = lastHitPos.getZ();
                  int lastZ = hitPos.getZ();
                  if (hitZ - lastZ != 0) {
                     double maxZ = Math.max(hitZ, lastZ);
                     double slopeYZ = (this.offset.y - this.velocity.y) / (this.offset.z - this.velocity.z);
                     double y2 = this.velocity.y - slopeYZ * this.velocity.z;
                     double y3 = slopeYZ * maxZ + y2;
                     double slopeXZ = (this.offset.x - this.velocity.x) / (this.offset.z - this.velocity.z);
                     double x3 = this.velocity.x - slopeXZ * this.velocity.z;
                     double x4 = slopeXZ * maxZ + x3;
                     this.velocity = new Vec3d(x4, y3, maxZ + 0.03F * (hitZ > lastZ ? -1 : 1));
                     this.position = new Vec3d(0.0, 0.0, 0.0);
                  }
               }
            }
         }
      }
   }

   static List<BlockPos> bresenhamLine(BlockPos start, BlockPos end) {
      ArrayList points = new ArrayList();
      points.add(start);
      int x0 = start.getX();
      int y0 = start.getY();
      int z0 = start.getZ();
      int x1 = end.getX();
      int y1 = end.getY();
      int z1 = end.getZ();
      int dx = Math.abs(x1 - x0);
      int dy = Math.abs(y1 - y0);
      int dz = Math.abs(z1 - z0);
      int sx = x0 < x1 ? 1 : -1;
      int sy = y0 < y1 ? 1 : -1;
      int sz = z0 < z1 ? 1 : -1;
      int maxStep = Math.max(dx, Math.max(dy, dz));
      int x = x0;
      int y = y0;
      int z = z0;
      int errX = maxStep / 2;
      int errY = maxStep / 2;
      int errZ = maxStep / 2;

      for (int i = 0; i < maxStep; i++) {
         points.add(new BlockPos(x, y, z));
         errX -= dx;
         errY -= dy;
         errZ -= dz;
         if (errX < 0) {
            x += sx;
            errX += maxStep;
         } else if (errY < 0) {
            y += sy;
            errY += maxStep;
         } else if (errZ < 0) {
            z += sz;
            errZ += maxStep;
         }
      }

      points.add(end);
      return points;
   }

}
