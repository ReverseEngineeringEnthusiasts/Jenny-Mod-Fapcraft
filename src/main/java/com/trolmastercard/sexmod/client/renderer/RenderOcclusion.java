package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.util.VectorMath;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Visibility gate for girl rendering and the voxel ray-trace it stands on.
 * <p>
 * <b>Occlusion gate.</b> {@link #canRender(BaseGirlEntity, EntityPlayer, int)}
 * hides the girl in first person when a solid block occludes every corner of
 * her bounding box; translucent / non-solid blocks count as visible.
 * Player-girls always render.
 * <p>
 * <b>Ray trace.</b> {@link #rayTraceBlocks(Vec3d, Vec3d, World)} mirrors vanilla
 * voxel-DDA semantics with NaN guards and only accepts blocks on the
 * {@link BlockRenderLayer#SOLID} layer, so glass and foliage do not occlude.
 * <p>
 * CLIENT-side render thread only (reads world state, draws nothing).
 */
@SideOnly(Side.CLIENT)
final class RenderOcclusion {
   private RenderOcclusion() {
   }

   /**
    * First-person occlusion gate: the girl is only rendered when at least one
    * corner of her bounding box has an unobstructed (or translucent/non-solid)
    * ray from the player's eye. Player-girls always render. In third person
    * the camera position is moved behind a solid block when needed.
    *
    * @param thirdPersonView the local player's {@code gameSettings.thirdPersonView}
    * @return {@code true} if the girl may be drawn
    */
   static boolean canRender(BaseGirlEntity girl, EntityPlayer player, int thirdPersonView) {
      if (girl instanceof AbstractPlayerGirlEntity) {
         return true;
      }

      World world = girl.world;
      Vec3d girlPos = girl.getPositionVector();
      float halfWidth = girl.width * 1.5F;
      float height = girl.height * 1.5F;
      Vec3d eyePos = player.getPositionVector().add(0.0, player.getEyeHeight(), 0.0);
      if (thirdPersonView != 0) {
         return true;
      }

      if (thirdPersonView > 0) {
         float yaw = player.rotationYaw;
         float pitch = player.rotationPitch;
         if (thirdPersonView == 2) {
            pitch += 180.0F;
         }

         float distance = 4.0F;
         Vec3d cameraPos = eyePos.add(
            MathHelper.sin(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0)) * distance,
            MathHelper.sin(pitch * (float) (Math.PI / 180.0)) * distance,
            -MathHelper.cos(yaw * (float) (Math.PI / 180.0)) * MathHelper.cos(pitch * (float) (Math.PI / 180.0)) * distance
         );
         BlockPos cameraBlock = new BlockPos(cameraPos);
         boolean isAir = world.isAirBlock(cameraBlock);
         if (!isAir) {
            eyePos = cameraPos;
         } else if (world.isAirBlock(cameraBlock.add(0, 1, 0))) {
            eyePos = new Vec3d(cameraPos.x, cameraBlock.getY() + 1, cameraPos.z);
         }
      }

      Vec3d[] corners = new Vec3d[]{
         girlPos.add(-halfWidth / 2.0F, 0.0, -halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, 0.0, halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, 0.0, -halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, 0.0, halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, height, -halfWidth / 2.0F),
         girlPos.add(-halfWidth / 2.0F, height, halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, height, -halfWidth / 2.0F),
         girlPos.add(halfWidth / 2.0F, height, halfWidth / 2.0F)
      };

      for (Vec3d corner : corners) {
         RayTraceResult hit = rayTraceBlocks(eyePos, corner, world);
         if (hit == null) {
            return true;
         }

         IBlockState blockState = world.getBlockState(hit.getBlockPos());
         if (blockState.isTranslucent()) {
            return true;
         }

         if (blockState.getBlock().getRenderLayer() != BlockRenderLayer.SOLID) {
            return true;
         }
      }

      return false;
   }

   /**
    * Distance to the first solid block along the view ray, capped at 4 blocks.
    */
   static float rayTraceBoneDistance(World world, Vec3d eyePos, float yaw, float pitch) {
      RayTraceResult hit = rayTraceBlocks(eyePos, eyePos.add(VectorMath.rotateByYawPitch(new Vec3d(0.0, 0.0, -4.0), yaw, pitch)), world);
      if (hit == null) {
         return 4.0F;
      }

      Vec3d hitVec = hit.hitVec;
      return hitVec == null ? 4.0F : (float)eyePos.distanceTo(hitVec);
   }

   /**
    * Block ray-trace (voxel DDA, mirrors vanilla semantics) with NaN guards —
    * used for the occlusion check. Returns the first solid block hit or
    * {@code null} (NaN inputs or no hit within 200 steps).
    */
   static RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, World world) {
      if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
         return null;
      }

      if (!Double.isNaN(end.x) && !Double.isNaN(end.y) && !Double.isNaN(end.z)) {
         int endX = MathHelper.floor(end.x);
         int endY = MathHelper.floor(end.y);
         int endZ = MathHelper.floor(end.z);
         int currentX = MathHelper.floor(start.x);
         int currentY = MathHelper.floor(start.y);
         int currentZ = MathHelper.floor(start.z);
         BlockPos currentPos = new BlockPos(currentX, currentY, currentZ);
         IBlockState blockState = world.getBlockState(currentPos);
         if (blockState.getCollisionBoundingBox(world, currentPos) != Block.NULL_AABB && blockState.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
            return blockState.collisionRayTrace(world, currentPos, start, end);
         }

         int steps = 200;

         while (steps-- >= 0) {
            if (Double.isNaN(start.x) || Double.isNaN(start.y) || Double.isNaN(start.z)) {
               return null;
            }

            if (currentX == endX && currentY == endY && currentZ == endZ) {
               return null;
            }

            boolean stepX = true;
            boolean stepY = true;
            boolean stepZ = true;
            double nextX = 999.0;
            double nextY = 999.0;
            double nextZ = 999.0;
            if (endX > currentX) {
               nextX = currentX + 1.0;
            } else if (endX < currentX) {
               nextX = currentX + 0.0;
            } else {
               stepX = false;
            }

            if (endY > currentY) {
               nextY = currentY + 1.0;
            } else if (endY < currentY) {
               nextY = currentY + 0.0;
            } else {
               stepY = false;
            }

            if (endZ > currentZ) {
               nextZ = currentZ + 1.0;
            } else if (endZ < currentZ) {
               nextZ = currentZ + 0.0;
            } else {
               stepZ = false;
            }

            double tMaxX = 999.0;
            double tMaxY = 999.0;
            double tMaxZ = 999.0;
            double deltaX = end.x - start.x;
            double deltaY = end.y - start.y;
            double deltaZ = end.z - start.z;
            if (stepX) {
               tMaxX = (nextX - start.x) / deltaX;
            }

            if (stepY) {
               tMaxY = (nextY - start.y) / deltaY;
            }

            if (stepZ) {
               tMaxZ = (nextZ - start.z) / deltaZ;
            }

            if (tMaxX == -0.0) {
               tMaxX = -1.0E-4;
            }

            if (tMaxY == -0.0) {
               tMaxY = -1.0E-4;
            }

            if (tMaxZ == -0.0) {
               tMaxZ = -1.0E-4;
            }

            EnumFacing facing;
            if (tMaxX < tMaxY && tMaxX < tMaxZ) {
               facing = endX > currentX ? EnumFacing.WEST : EnumFacing.EAST;
               start = new Vec3d(nextX, start.y + deltaY * tMaxX, start.z + deltaZ * tMaxX);
            } else if (tMaxY < tMaxZ) {
               facing = endY > currentY ? EnumFacing.DOWN : EnumFacing.UP;
               start = new Vec3d(start.x + deltaX * tMaxY, nextY, start.z + deltaZ * tMaxY);
            } else {
               facing = endZ > currentZ ? EnumFacing.NORTH : EnumFacing.SOUTH;
               start = new Vec3d(start.x + deltaX * tMaxZ, start.y + deltaY * tMaxZ, nextZ);
            }

            currentX = MathHelper.floor(start.x) - (facing == EnumFacing.EAST ? 1 : 0);
            currentY = MathHelper.floor(start.y) - (facing == EnumFacing.UP ? 1 : 0);
            currentZ = MathHelper.floor(start.z) - (facing == EnumFacing.SOUTH ? 1 : 0);
            currentPos = new BlockPos(currentX, currentY, currentZ);
            IBlockState hitState = world.getBlockState(currentPos);
            if ((hitState.getMaterial() == Material.PORTAL || hitState.getCollisionBoundingBox(world, currentPos) != Block.NULL_AABB)
               && hitState.getBlock().getRenderLayer() == BlockRenderLayer.SOLID) {
               return hitState.collisionRayTrace(world, currentPos, start, end);
            }
         }

         return null;
      } else {
         return null;
      }
   }
}
