package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import com.trolmastercard.sexmod.entity.fp;







import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class KoboldTask {
   public static final int d = 30;
   BlockPos a;
   KoboldTask.TaskType c;
   HashSet<BlockPos> b;
   List<KoboldEntity> f = new ArrayList<>();
   EnumFacing e = EnumFacing.NORTH;

   public KoboldTask(BlockPos var1, KoboldTask.TaskType var2, HashSet<BlockPos> var3) {
      this.a = var1;
      this.c = var2;
      this.b = var3;
   }

   public KoboldTask(BlockPos var1, KoboldTask.TaskType var2, HashSet<BlockPos> var3, EnumFacing var4) {
      this.a = var1;
      this.c = var2;
      this.b = var3;
      this.e = var4;
   }

   public EnumFacing f_clash200() {
      return this.e;
   }

   public BlockPos b_clash201() {
      return this.a;
   }

   public KoboldTask.TaskType d_clash202() {
      return this.c;
   }

   public HashSet<BlockPos> g_clash203() {
      return this.b;
   }

   public void b_clash204(BlockPos var1) {
      this.b.add(var1);
   }

   public void a_clash205(HashSet<BlockPos> var1) {
      this.b.addAll(var1);
   }

   public void a_clash206(BlockPos var1) {
      this.b.remove(var1);
   }

   public void b(HashSet<BlockPos> var1) {
      if (!var1.isEmpty()) {
         this.b.removeAll(var1);
      }
   }

   public boolean c_clash207(BlockPos var1) {
      return this.b.contains(var1);
   }

   public boolean a_clash208(KoboldEntity var1) {
      if (this.c.a <= this.f.size()) {
         return false;
      }

      this.f.add(var1);
      return true;
   }

   public List<KoboldEntity> c_clash209() {
      return this.f;
   }

   public void a_clash210() {
      for (KoboldEntity var2 : this.f) {
         if (var2.ae_clash498() == null) {
            var2.func_189654_d(false);
            var2.field_70145_X = false;
            var2.b(fp.NULL);
            var2.func_184212_Q().func_187227_b(BaseGirlEntity.G, false);
         }
      }

      this.f.clear();
   }

   public void c(KoboldEntity var1) {
      this.f.remove(var1);
   }

   public boolean e_clash211() {
      return this.c.a <= this.f.size();
   }

   public boolean b_clash212(KoboldEntity var1) {
      return this.f.contains(var1);
   }

   public static HashSet<BlockPos> a(World var0, BlockPos var1, UUID var2) {
      BlockPos var3 = var1;

      while (!c(var0, var3)) {
         var3 = var1.func_177977_b();
      }

      BlockPos var4 = var1;

      while (!b(var0, var4)) {
         var4 = var4.func_177984_a();
      }

      HashSet var5 = new HashSet();
      int var6 = var4.func_177956_o() - var3.func_177956_o();

      for (int var7 = 0; var7 <= var6; var7++) {
         var5.add(var3.func_177982_a(0, var7, 0));
      }

      HashSet var15 = a_clash213(var0, var3);
      HashSet var8 = new HashSet();

      for (BlockPos var10 : (java.util.Collection<BlockPos>) (var15) ) {
         if (var10.func_177958_n() == var3.func_177958_n() && var10.func_177952_p() == var3.func_177952_p()) {
            var8.add(var10);
         }
      }

      for (BlockPos var18 : (java.util.Collection<BlockPos>) (var8) ) {
         var15.remove(var18);
      }

      var5.addAll(var15);
      HashSet var17 = new HashSet();

      for (BlockPos var11 : (java.util.Collection<BlockPos>) (var5) ) {
         for (KoboldTask var13 : KoboldManager.p_clash79(var2)) {
            HashSet var14 = var13.g_clash203();
            if (var14.contains(var11)) {
               var17.add(var11);
               break;
            }
         }
      }

      var5.removeAll(var17);
      KoboldTask var20 = new KoboldTask(var3, KoboldTask.TaskType.FALL_TREE, var5);
      KoboldManager.b(var2, var20);
      return var5;
   }

   static boolean b(World var0, BlockPos var1) {
      Block var2 = var0.func_180495_p(var1.func_177984_a()).func_177230_c();
      return !(var2 instanceof BlockLog);
   }

   static boolean c(World var0, BlockPos var1) {
      IBlockState var2 = var0.func_180495_p(var1.func_177977_b());
      return !(var2 instanceof BlockLog) && var2.func_185904_a() != Material.field_151579_a;
   }

   static HashSet<BlockPos> a_clash213(World var0, BlockPos var1) {
      return a(var0, var1, new HashSet<>());
   }

   static HashSet<BlockPos> a(World var0, BlockPos var1, HashSet<BlockPos> var2) {
      if (var2.contains(var1)) {
         return new HashSet<>();
      }

      var2.add(var1);
      if (var0.func_180495_p(var1.func_177982_a(1, 0, 0)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 0, 0), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 0, 0)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 0, 0), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(0, 0, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(0, 0, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(0, 0, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(0, 0, -1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(1, 0, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 0, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 0, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 0, -1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 0, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 0, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(1, 0, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 0, -1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(0, 1, 0)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(0, 1, 0), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(1, 1, 0)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 1, 0), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 1, 0)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 1, 0), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(0, 1, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(0, 1, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(0, 1, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(0, 1, -1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(1, 1, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 1, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 1, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 1, -1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(-1, 1, 1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(-1, 1, 1), var2));
      }

      if (var0.func_180495_p(var1.func_177982_a(1, 1, -1)).func_177230_c() instanceof BlockLog) {
         var2.addAll(a(var0, var1.func_177982_a(1, 1, -1), var2));
      }

      return var2;
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }

   public enum TaskType {
      FALL_TREE(1),
      MINE(3);

      int a;

      TaskType(int var3) {
         this.a = var3;
      }

      int a_clash887() {
         return this.a;
      }
   }
}
