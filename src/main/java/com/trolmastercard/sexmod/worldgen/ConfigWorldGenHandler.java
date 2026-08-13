package com.trolmastercard.sexmod.worldgen;

import com.trolmastercard.sexmod.entity.GoblinEntity;
import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.cj;
import com.trolmastercard.sexmod.util.e1;







import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ConfigWorldGenHandler extends WorldSavedData implements IWorldGenerator {
   static final String j = "sexmod:generation";
   static final int h = 156;
   static final int a = 62;
   static final int b = 6;
   final double f = 0.004F;
   public static boolean i = true;
   final List<ConfigWorldGenHandler.b> e = new ArrayList<>();
   final List<ConfigWorldGenHandler.a> d = new ArrayList<>();
   private static ConfigWorldGenHandler g = null;
   static boolean c = true;

   public static ConfigWorldGenHandler b_clash469() {
      if (g == null) {
         g = new ConfigWorldGenHandler();
      }

      return g;
   }

   public ConfigWorldGenHandler(String var1) {
      this();
   }

   private ConfigWorldGenHandler() {
      super("sexmod:generation");
      g = this;
      this.e
         .add(
            new ConfigWorldGenHandler.b(
               "ellie",
               new HashSet<>(Arrays.asList(Biomes.REDWOOD_TAIGA, Biomes.COLD_TAIGA, Biomes.TAIGA, Biomes.ROOFED_FOREST)),
               new Vec3i(30, 27, 26),
               9,
               true
            )
         );
      this.e.add(new ConfigWorldGenHandler.b("jenny", new HashSet<>(Arrays.asList(Biomes.PLAINS, Biomes.FOREST)), new Vec3i(9, 4, 9), 1, true));
      this.e
         .add(
            new ConfigWorldGenHandler.b(
               "ellie",
               new HashSet<>(Arrays.asList(Biomes.REDWOOD_TAIGA, Biomes.COLD_TAIGA, Biomes.TAIGA, Biomes.ROOFED_FOREST)),
               new Vec3i(30, 27, 26),
               9,
               true
            )
         );
      this.e.add(new ConfigWorldGenHandler.b("bia", new HashSet<>(Arrays.asList(Biomes.MUTATED_BIRCH_FOREST, Biomes.BIRCH_FOREST)), new Vec3i(11, 9, 15), 2, true));
      this.e.add(new ConfigWorldGenHandler.b("luna", new HashSet<>(Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN)), new Vec3i(3, 7, 10), 0, false));
   }

   public void a_clash470() {
      this.d.clear();
   }

   @SubscribeEvent
   public void a(Save var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().setData("sexmod:generation", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void a(Load var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().getOrLoadData(ConfigWorldGenHandler.class, "sexmod:generation");
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.a_clash470();
      NBTTagCompound var2 = var1.getCompoundTag("sexmod:generation");
      int var3 = 0;

      while (true) {
         String var4 = var2.getString("sexmod:name" + var3);
         String var5 = var2.getString("sexmod:pos" + var3);
         if ("".equals(var4) || "".equals(var5)) {
            return;
         }

         this.d.add(new ConfigWorldGenHandler.a(a_clash471(var5), var4));
         var3++;
      }
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      var1.setTag("sexmod:generation", new NBTTagCompound());
      NBTTagCompound var2 = new NBTTagCompound();
      int var3 = 0;

      for (ConfigWorldGenHandler.a var5 : this.d) {
         var2.setString("sexmod:name" + var3, var5.a);
         var2.setString("sexmod:pos" + var3++, a(var5.b));
      }

      var1.setTag("sexmod:generation", var2);
      return var1;
   }

   static String a(e1 var0) {
      return var0.c + "|" + var0.b;
   }

   static e1 a_clash471(String var0) {
      String[] var1 = var0.split("\\|");
      return new e1(Integer.parseInt(var1[0]), Integer.parseInt(var1[1]));
   }

   public void generate(Random var1, int var2, int var3, World var4, IChunkGenerator var5, IChunkProvider var6) {
      if (i) {
         if (var4.getWorldType() != WorldType.FLAT) {
            this.b(var4, var1, var2, var3);
            this.a(var4, var1, var2, var3);
            this.a(var1, var2, var3, var4);
         }
      }
   }

   void a(Random var1, int var2, int var3, World var4) {
      if (c) {
         c = false;

         for (ConfigWorldGenHandler.b var6 : this.e) {
            this.a(var6, var1, var2, var3, var4);
         }

         c = true;
      }
   }

   void a(ConfigWorldGenHandler.b var1, Random var2, int var3, int var4, World var5) {
      for (ConfigWorldGenHandler.a var7 : this.d) {
         int var8 = var7.a.equals(var1.f) ? 156 : 62;
         if (var7.b.a_clash298(var3, var4) < var8) {
            return;
         }
      }

      int var21 = var1.c.getX();
      int var22 = var1.c.getZ();
      int var23 = var3 * 16 + (16 - var21) / 2;
      int var9 = var4 * 16 + (16 - var22) / 2;
      Biome var10 = var5.provider.getBiomeForCoords(new BlockPos(var23, 80, var9));
      if (var1.e.contains(var10)) {
         int var11 = Integer.MIN_VALUE;
         int var12 = Integer.MAX_VALUE;

         for (int var13 = var23; var13 < var23 + var21; var13++) {
            for (int var14 = var9; var14 < var9 + var22; var14++) {
               int var15 = cj.a(var5, var13, var14);
               if (var1.d && var5.getBlockState(new BlockPos(var13, var15, var14)).getBlock() == Blocks.WATER) {
                  return;
               }

               if (var15 > var11) {
                  var11 = var15;
               }

               if (var15 < var12) {
                  var12 = var15;
               }
            }
         }

         if (var11 - var12 <= var1.a) {
            int var24 = var11;
            this.d.add(new ConfigWorldGenHandler.a(new e1(var3, var4), var1.f));
            var1.b.generate(var5, var2, new BlockPos(var23, var24, var9));
            if (var1.d) {
               boolean var25 = true;

               for (int var26 = var24 - 1; var25; var26--) {
                  var25 = false;
                  Vec3i var16 = new Vec3i(var21 + 2, 0, var22 + 2);
                  var23--;
                  var9--;

                  for (int var17 = var23; var17 < var23 + var16.getX(); var17++) {
                     for (int var18 = var9; var18 < var9 + var16.getZ(); var18++) {
                        BlockPos var19 = new BlockPos(var17, var26, var18);
                        IBlockState var20 = var5.getBlockState(var19);
                        if (var20.getBlock().isPassable(var5, var19)) {
                           var20 = var5.canSeeSky(var19) ? Blocks.GRASS.getDefaultState() : Blocks.DIRT.getDefaultState();
                           var5.setBlockState(var19, var20);
                           var25 = true;
                        }
                     }
                  }
               }
            }
         }
      }
   }

   void b(World var1, Random var2, int var3, int var4) {
      if (!(var2.nextDouble() > 0.004F)) {
         int var5 = var3 * 16 + 8;
         int var6 = var4 * 16 + 8;
         int var7 = cj.a(var1, var5, var6);
         if (!var1.getBlockState(new BlockPos(var5, var7, var6)).getMaterial().isLiquid()) {
            KoboldManager.a(var1, new Vec3d(var5, var7, var6));
         }
      }
   }

   void a(World var1, Random var2, int var3, int var4) {
      int var5 = 16 * var3 + 3;
      int var6 = 16 * var4 + 3;
      int var7 = var2.nextInt(255);
      BlockPos var8 = new BlockPos(var5, var7, var6);
      ArrayList var9 = new ArrayList();

      for (int var10 = 0; var10 <= GoblinEntity.ah.getX(); var10++) {
         for (int var11 = -1; var11 <= GoblinEntity.ah.getY(); var11++) {
            for (int var12 = 0; var12 <= GoblinEntity.ah.getZ(); var12++) {
               BlockPos var13 = var8.add(var10, var11, var12);
               Material var14 = var1.getBlockState(var13).getMaterial();
               boolean var15 = var14.isSolid();
               if (!var15 && (var11 == -1 || var11 == GoblinEntity.ah.getY())) {
                  return;
               }

               if ((var10 == 0 || var10 == GoblinEntity.ah.getX() || var12 == 0 || var12 == GoblinEntity.ah.getZ())
                  && var11 == 0
                  && var1.isAirBlock(var13)
                  && var1.isAirBlock(var13.up())) {
                  var9.add(var13);
               }
            }
         }
      }

      if (var9.size() != 0 && var9.size() <= 4) {
         BlockPos var16 = null;

         for (BlockPos var19 : (java.util.Collection<BlockPos>) (var9) ) {
            BlockPos var22 = var19;
            BlockPos var25 = var8.add(6, 0, 6);
            var22 = var22.subtract(var25);
            if (Math.abs(var22.getX()) != Math.abs(var22.getZ())
               && Math.abs(var22.getX()) != Math.abs(var22.getZ()) - 1
               && Math.abs(var22.getX()) - 1 != Math.abs(var22.getZ())) {
               var16 = var22;
               break;
            }
         }

         if (var16 != null) {
            Vec3i var24 = new Vec3i(0, 0, 0);
            float var26 = 0.0F;
            Rotation var18;
            Vec3d var20;
            if (var16.getZ() == -6) {
               var18 = Rotation.NONE;
               var20 = GoblinEntity.aB;
               var26 = 180.0F;
            } else if (var16.getX() == 5) {
               var18 = Rotation.CLOCKWISE_90;
               var20 = GoblinEntity.ao;
               var24 = new Vec3i(GoblinEntity.ah.getX() - 1, 0, 0);
               var26 = -90.0F;
            } else if (var16.getZ() == 5) {
               var18 = Rotation.CLOCKWISE_180;
               var20 = GoblinEntity.aM;
               var24 = new Vec3i(GoblinEntity.ah.getX() - 1, 0, GoblinEntity.ah.getZ() - 1);
            } else {
               var18 = Rotation.COUNTERCLOCKWISE_90;
               var20 = GoblinEntity.U;
               var24 = new Vec3i(0, 0, GoblinEntity.ah.getZ() - 1);
               var26 = 90.0F;
            }

            new GirlHouseGenerator("goblin").a(var1, var8.add(0, -1, 0).add(var24), var18);
            var20.add(var24.getX(), var24.getY(), var24.getZ());
            var20 = new Vec3d(
               var8.getX() + var20.x + 0.5, var8.getY() + var20.y, var8.getZ() + var20.z + 0.5
            );
            GoblinEntity var27 = new GoblinEntity(var1, true, var26, var20);
            var27.forceSpawn = true;
            var1.spawnEntity(var27);
            var1.getChunk(var3, var4).markDirty();
         }
      }
   }


   static class a {
      e1 b;
      String a;

      public a(e1 var1, String var2) {
         this.b = var1;
         this.a = var2;
      }
   }

   static class b {
      public final String f;
      public final GirlHouseGenerator b;
      public final HashSet<Biome> e;
      public final Vec3i c;
      public final boolean d;
      public final int a;

      public b(String var1, HashSet<Biome> var2, Vec3i var3, int var4, boolean var5) {
         this.f = var1;
         this.e = var2;
         this.c = var3;
         this.d = var5;
         this.a = var4;
         this.b = new GirlHouseGenerator(var1);
      }
   }
}
