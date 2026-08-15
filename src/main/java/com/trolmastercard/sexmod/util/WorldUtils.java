package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.SexWorldClient;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.collect.UnmodifiableIterator;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * <b>Role.</b> World helpers for girl AI, bed placement and tribe logic:
 * angle normalization, light-based look-vector estimation, surface height,
 * structure/bed placement checks, ring particles, double-block (bed/chest)
 * resolution and entity tracking queries.
 * <p>
 * <b>Pitfalls.</b> {@link #getEntityLookVector} deliberately returns straight-up
 * inside {@link SexWorldClient} (the preload world has no light data) — keep
 * that branch. {@link #getStatePos} returns {@code null} for malformed beds and
 * the callers (tribe bed tracking, {@link SendBlocksPacket}) must handle it.
 */
public class WorldUtils {
   public static float normalizeAngleDiff(float angle1, float angle2) {
      angle1 = TrigMath.NormalizeAngle(angle1);
      angle2 = TrigMath.NormalizeAngle(angle2);
      float diff = Math.abs(angle1 - angle2);
      float opposite = 360.0F - diff;
      float minDiff = Math.min(diff, opposite);
      return angle1 > angle2 ? -minDiff : minDiff;
   }

   public static Vec3d getEntityLookVector(EntityLivingBase entity, float partialTicks) {
      World world = entity.world;
      if (world instanceof SexWorldClient) {
         return new Vec3d(0.0, 1.0, 0.0);
      }

      BlockPos pos = new BlockPos(Math.floor(entity.posX), Math.floor(entity.posY), Math.floor(entity.posZ));
      HashMap lightMap = new HashMap();
      int maxLight = 0;

      for (int dx = -1; dx < 2; dx++) {
         for (int dy = -1; dy < 2; dy++) {
            for (int dz = -1; dz < 2; dz++) {
               int light = world.getLight(pos.add(dx, dy, dz), false);
               lightMap.put(new Vec3d(dx, dy, dz), light);
               if (light > maxLight) {
                  maxLight = light;
               }
            }
         }
      }

      Vec3d bestOffset = null;

      for (Entry entry : (java.util.Set<Entry>) lightMap.entrySet()) {
         if ((Integer)entry.getValue() == maxLight) {
            if (bestOffset != null) {
               bestOffset = null;
               break;
            }

            bestOffset = (Vec3d)entry.getKey();
         }
      }

      if (bestOffset == null) {
         bestOffset = new Vec3d(0.2, 0.8, 0.0);
      } else {
         bestOffset = new Vec3d(bestOffset.x, bestOffset.y, -bestOffset.z);
         float yaw = -RotationHelper.lerp(entity.prevRenderYawOffset, entity.renderYawOffset, partialTicks);
         bestOffset = VectorMath.rotateByYaw(bestOffset, yaw);
      }

      return bestOffset.normalize();
   }

   public static int getHeightAt(World world, int x, int z) {
      HashSet transparentBlocks = Sets.newHashSet(
         new Block[]{Blocks.GRASS, Blocks.SAND, Blocks.RED_SANDSTONE, Blocks.WATER, Blocks.STONE, Blocks.COBBLESTONE}
      );
      int y = world.getHeight();
      boolean isTransparent = false;

      while (!isTransparent && y-- >= 0) {
         Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
         isTransparent = transparentBlocks.contains(block);
      }

      return y;
   }

   public static BlockPos getSurfaceBlockPos(World world, BlockPos pos) {
      return new BlockPos(pos.getX(), getHeightAt(world, pos.getX(), pos.getZ()), pos.getZ());
   }

   public static boolean canPlaceStructure(World world, BlockPos pos) {
      return canPlaceBlock(world, pos, null, null, null);
   }

   public static boolean canPlaceBlock(World world, BlockPos pos, Vec3d hitVec, EnumFacing face, EntityPlayer player) {
      IBlockState state = world.getBlockState(pos);
      Block block = state.getBlock();
      if (block.isBed(state, world, pos, null)) {
         return true;
      }

      TileEntity tileEntity = world.getTileEntity(pos);
      if (tileEntity != null) {
         ITextComponent displayName = tileEntity.getDisplayName();
         if (displayName != null && (displayName.toString().contains(" bed") || displayName.toString().contains("bed "))) {
            return true;
         }
      }

      if (face != null && hitVec != null) {
         String blockName = block.getPickBlock(state, new RayTraceResult(hitVec, face), world, pos, player).getDisplayName().toLowerCase();
         return blockName.contains(" bed") || blockName.contains("bed ");
      } else {
         return false;
      }
   }

   public static void spawnParticles(World world, EnumParticleTypes particleType, Vec3d center, int count, double radius, double speed) {
      for (int i = 0; i < count; i++) {
         float progress = (float)i / count;
         double angle = (Math.PI * 2) * progress;
         double sin = Math.sin(angle);
         double cos = Math.cos(angle);
         sin *= radius;
         cos *= radius;
         world.spawnParticle(
            particleType, center.x + sin, center.y, center.z + cos, 0.0, Reference.RANDOM.nextFloat() * speed, 0.0, new int[0]
         );
      }
   }

   public static BlockPos getStatePos(BlockPos pos, IBlockState state) {
      ImmutableMap properties = state.getProperties();
      EnumFacing facing = null;
      EnumPartType partType = null;
      UnmodifiableIterator iterator = properties.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry entry = (Entry)iterator.next();
         if (entry.getKey() instanceof PropertyDirection) {
            facing = (EnumFacing)entry.getValue();
         } else if (entry.getKey() instanceof PropertyEnum) {
            partType = (EnumPartType)entry.getValue();
         }
      }

      if (facing == null) {
         System.out.println("bed is fucked up - it has no facing value");
         return null;
      }

      if (partType == null) {
         System.out.println("bed is fucked up - it has no partType value");
         return null;
      }

      BlockPos pairedPos = null;
      if (partType == EnumPartType.FOOT) {
         if (facing == EnumFacing.NORTH) {
            pairedPos = pos.north();
         }

         if (facing == EnumFacing.EAST) {
            pairedPos = pos.east();
         }

         if (facing == EnumFacing.SOUTH) {
            pairedPos = pos.south();
         }

         if (facing == EnumFacing.WEST) {
            pairedPos = pos.west();
         }
      } else {
         if (facing == EnumFacing.NORTH) {
            pairedPos = pos.south();
         }

         if (facing == EnumFacing.EAST) {
            pairedPos = pos.west();
         }

         if (facing == EnumFacing.SOUTH) {
            pairedPos = pos.north();
         }

         if (facing == EnumFacing.WEST) {
            pairedPos = pos.east();
         }
      }

      if (pairedPos == null) {
         System.out.println("bed is fucked up - it appears to be positioned vertically (wtf?)");
         return null;
      } else {
         return pairedPos;
      }
   }

   public static Set<? extends EntityPlayer> getNearbyPlayers(Entity entity) {
      return entity == null
         ? Collections.emptySet()
         : FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(entity.dimension).getEntityTracker().getTrackingPlayers(entity);
   }

}
