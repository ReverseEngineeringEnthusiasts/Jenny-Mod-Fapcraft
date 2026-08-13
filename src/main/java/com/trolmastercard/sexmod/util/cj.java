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

public class cj {
   public static float a_clash300(float var0, float var1) {
      var0 = gc.b_clash741(var0);
      var1 = gc.b_clash741(var1);
      float var2 = Math.abs(var0 - var1);
      float var3 = 360.0F - var2;
      float var4 = Math.min(var2, var3);
      return var0 > var1 ? -var4 : var4;
   }

   public static Vec3d a_clash301(EntityLivingBase var0, float var1) {
      World var2 = var0.field_70170_p;
      if (var2 instanceof SexWorldClient) {
         return new Vec3d(0.0, 1.0, 0.0);
      }

      BlockPos var3 = new BlockPos(Math.floor(var0.field_70165_t), Math.floor(var0.field_70163_u), Math.floor(var0.field_70161_v));
      HashMap var4 = new HashMap();
      int var5 = 0;

      for (int var6 = -1; var6 < 2; var6++) {
         for (int var7 = -1; var7 < 2; var7++) {
            for (int var8 = -1; var8 < 2; var8++) {
               int var9 = var2.func_175721_c(var3.func_177982_a(var6, var7, var8), false);
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
         var10 = new Vec3d(var10.field_72450_a, var10.field_72448_b, -var10.field_72449_c);
         float var14 = -RotationHelper.lerp(var0.field_70760_ar, var0.field_70761_aq, var1);
         var10 = ck.rotateByYaw(var10, var14);
      }

      return var10.func_72432_b();
   }

   public static int a(World var0, int var1, int var2) {
      HashSet var3 = Sets.newHashSet(
         new Block[]{Blocks.field_150349_c, Blocks.field_150354_m, Blocks.field_180395_cM, Blocks.field_150355_j, Blocks.field_150348_b, Blocks.field_150347_e}
      );
      int var4 = var0.func_72800_K();
      boolean var5 = false;

      while (!var5 && var4-- >= 0) {
         Block var6 = var0.func_180495_p(new BlockPos(var1, var4, var2)).func_177230_c();
         var5 = var3.contains(var6);
      }

      return var4;
   }

   public static BlockPos a_clash302(World var0, BlockPos var1) {
      return new BlockPos(var1.func_177958_n(), a(var0, var1.func_177958_n(), var1.func_177952_p()), var1.func_177952_p());
   }

   public static boolean b(World var0, BlockPos var1) {
      return a(var0, var1, null, null, null);
   }

   public static boolean a(World var0, BlockPos var1, Vec3d var2, EnumFacing var3, EntityPlayer var4) {
      IBlockState var5 = var0.func_180495_p(var1);
      Block var6 = var5.func_177230_c();
      if (var6.isBed(var5, var0, var1, null)) {
         return true;
      }

      TileEntity var7 = var0.func_175625_s(var1);
      if (var7 != null) {
         ITextComponent var8 = var7.func_145748_c_();
         if (var8 != null && (var8.toString().contains(" bed") || var8.toString().contains("bed "))) {
            return true;
         }
      }

      if (var3 != null && var2 != null) {
         String var9 = var6.getPickBlock(var5, new RayTraceResult(var2, var3), var0, var1, var4).func_82833_r().toLowerCase();
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
         var0.func_175688_a(
            var1, var2.field_72450_a + var12, var2.field_72448_b, var2.field_72449_c + var14, 0.0, Reference.f.nextFloat() * var6, 0.0, new int[0]
         );
      }
   }

   public static BlockPos a(BlockPos var0, IBlockState var1) {
      ImmutableMap var2 = var1.func_177228_b();
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
            var7 = var0.func_177978_c();
         }

         if (var3 == EnumFacing.EAST) {
            var7 = var0.func_177974_f();
         }

         if (var3 == EnumFacing.SOUTH) {
            var7 = var0.func_177968_d();
         }

         if (var3 == EnumFacing.WEST) {
            var7 = var0.func_177976_e();
         }
      } else {
         if (var3 == EnumFacing.NORTH) {
            var7 = var0.func_177968_d();
         }

         if (var3 == EnumFacing.EAST) {
            var7 = var0.func_177976_e();
         }

         if (var3 == EnumFacing.SOUTH) {
            var7 = var0.func_177978_c();
         }

         if (var3 == EnumFacing.WEST) {
            var7 = var0.func_177974_f();
         }
      }

      if (var7 == null) {
         System.out.println("bed is fucked up - it appears to be positioned vertically (wtf?)");
         return null;
      } else {
         return var7;
      }
   }

   public static Set<? extends EntityPlayer> a_clash303(Entity var0) {
      return var0 == null
         ? Collections.emptySet()
         : FMLCommonHandler.instance().getMinecraftServerInstance().func_71218_a(var0.field_71093_bK).func_73039_n().getTrackingPlayers(var0);
   }

}
