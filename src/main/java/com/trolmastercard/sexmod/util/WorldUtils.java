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

public class WorldUtils {
   public static float normalizeAngleDiff(float var0, float var1) {
      var0 = TrigMath.NormalizeAngle(var0);
      var1 = TrigMath.NormalizeAngle(var1);
      float var2 = Math.abs(var0 - var1);
      float var3 = 360.0F - var2;
      float var4 = Math.min(var2, var3);
      return var0 > var1 ? -var4 : var4;
   }

   public static Vec3d getEntityLookVector(EntityLivingBase var0, float var1) {
      World var2 = var0.world;
      if (var2 instanceof SexWorldClient) {
         return new Vec3d(0.0, 1.0, 0.0);
      }

      BlockPos var3 = new BlockPos(Math.floor(var0.posX), Math.floor(var0.posY), Math.floor(var0.posZ));
      HashMap var4 = new HashMap();
      int var5 = 0;

      for (int var6 = -1; var6 < 2; var6++) {
         for (int var7 = -1; var7 < 2; var7++) {
            for (int var8 = -1; var8 < 2; var8++) {
               int var9 = var2.getLight(var3.add(var6, var7, var8), false);
               var4.put(new Vec3d(var6, var7, var8), var9);
               if (var9 > var5) {
                  var5 = var9;
               }
            }
         }
      }

      Vec3d var10 = null;

      for (Entry var15 : (java.util.Set<Entry>) var4.entrySet()) {
         if ((Integer)var15.getValue() == var5) {
            if (var10 != null) {
               var10 = null;
               break;
            }

            var10 = (Vec3d)var15.getKey();
         }
      }

      if (var10 == null) {
         var10 = new Vec3d(0.2, 0.8, 0.0);
      } else {
         var10 = new Vec3d(var10.x, var10.y, -var10.z);
         float var14 = -RotationHelper.lerp(var0.prevRenderYawOffset, var0.renderYawOffset, var1);
         var10 = VectorMath.rotateByYaw(var10, var14);
      }

      return var10.normalize();
   }

   public static int a(World var0, int var1, int var2) {
      HashSet var3 = Sets.newHashSet(
         new Block[]{Blocks.GRASS, Blocks.SAND, Blocks.RED_SANDSTONE, Blocks.WATER, Blocks.STONE, Blocks.COBBLESTONE}
      );
      int var4 = var0.getHeight();
      boolean var5 = false;

      while (!var5 && var4-- >= 0) {
         Block var6 = var0.getBlockState(new BlockPos(var1, var4, var2)).getBlock();
         var5 = var3.contains(var6);
      }

      return var4;
   }

   public static BlockPos getSurfaceBlockPos(World var0, BlockPos var1) {
      return new BlockPos(var1.getX(), a(var0, var1.getX(), var1.getZ()), var1.getZ());
   }

   public static boolean b(World var0, BlockPos var1) {
      return a(var0, var1, null, null, null);
   }

   public static boolean a(World var0, BlockPos var1, Vec3d var2, EnumFacing var3, EntityPlayer var4) {
      IBlockState var5 = var0.getBlockState(var1);
      Block var6 = var5.getBlock();
      if (var6.isBed(var5, var0, var1, null)) {
         return true;
      }

      TileEntity var7 = var0.getTileEntity(var1);
      if (var7 != null) {
         ITextComponent var8 = var7.getDisplayName();
         if (var8 != null && (var8.toString().contains(" bed") || var8.toString().contains("bed "))) {
            return true;
         }
      }

      if (var3 != null && var2 != null) {
         String var9 = var6.getPickBlock(var5, new RayTraceResult(var2, var3), var0, var1, var4).getDisplayName().toLowerCase();
         return var9.contains(" bed") || var9.contains("bed ");
      } else {
         return false;
      }
   }

   public static void a(World var0, EnumParticleTypes var1, Vec3d var2, int var3, double var4, double var6) {
      for (int var8 = 0; var8 < var3; var8++) {
         float var9 = (float)var8 / var3;
         double var10 = (Math.PI * 2) * var9;
         double var12 = Math.sin(var10);
         double var14 = Math.cos(var10);
         var12 *= var4;
         var14 *= var4;
         var0.spawnParticle(
            var1, var2.x + var12, var2.y, var2.z + var14, 0.0, Reference.RANDOM.nextFloat() * var6, 0.0, new int[0]
         );
      }
   }

   public static BlockPos a(BlockPos var0, IBlockState var1) {
      ImmutableMap var2 = var1.getProperties();
      EnumFacing var3 = null;
      EnumPartType var4 = null;
      UnmodifiableIterator var5 = var2.entrySet().iterator();

      while (var5.hasNext()) {
         Entry var6 = (Entry)var5.next();
         if (var6.getKey() instanceof PropertyDirection) {
            var3 = (EnumFacing)var6.getValue();
         } else if (var6.getKey() instanceof PropertyEnum) {
            var4 = (EnumPartType)var6.getValue();
         }
      }

      if (var3 == null) {
         System.out.println("bed is fucked up - it has no facing value");
         return null;
      }

      if (var4 == null) {
         System.out.println("bed is fucked up - it has no partType value");
         return null;
      }

      BlockPos var7 = null;
      if (var4 == EnumPartType.FOOT) {
         if (var3 == EnumFacing.NORTH) {
            var7 = var0.north();
         }

         if (var3 == EnumFacing.EAST) {
            var7 = var0.east();
         }

         if (var3 == EnumFacing.SOUTH) {
            var7 = var0.south();
         }

         if (var3 == EnumFacing.WEST) {
            var7 = var0.west();
         }
      } else {
         if (var3 == EnumFacing.NORTH) {
            var7 = var0.south();
         }

         if (var3 == EnumFacing.EAST) {
            var7 = var0.west();
         }

         if (var3 == EnumFacing.SOUTH) {
            var7 = var0.north();
         }

         if (var3 == EnumFacing.WEST) {
            var7 = var0.east();
         }
      }

      if (var7 == null) {
         System.out.println("bed is fucked up - it appears to be positioned vertically (wtf?)");
         return null;
      } else {
         return var7;
      }
   }

   public static Set<? extends EntityPlayer> getNearbyPlayers(Entity var0) {
      return var0 == null
         ? Collections.emptySet()
         : FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(var0.dimension).getEntityTracker().getTrackingPlayers(var0);
   }

}
