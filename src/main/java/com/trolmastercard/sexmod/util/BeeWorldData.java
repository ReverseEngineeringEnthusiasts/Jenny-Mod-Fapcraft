package com.trolmastercard.sexmod.util;


import java.util.ArrayList;
import java.util.List;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BeeWorldData extends WorldSavedData {
   public static final List<BlockPos> c = new ArrayList<>();
   public static final List<BlockPos> b = new ArrayList<>();
   static final String d = "sexmod:galath_spawn_list";
   static final String a = "sexmod:galath_spawn_list";

   public BeeWorldData() {
      super("sexmod:galath_spawn_list");
   }

   public BeeWorldData(String var1) {
      super("sexmod:galath_spawn_list");
   }

   public static void a(BlockPos var0, List<BlockPos> var1) {
      var1.add(var0);
   }

   @SubscribeEvent
   public void a(Save var1) {
      World var2 = var1.getWorld();
      var2.func_175693_T().func_75745_a("sexmod:galath_spawn_list", this);
      this.func_76185_a();
   }

   @SubscribeEvent
   public void a(Load var1) {
      World var2 = var1.getWorld();
      var2.func_175693_T().func_75742_a(BeeWorldData.class, "sexmod:galath_spawn_list");
   }

   public void func_76184_a(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.func_74775_l("sexmod:galath_spawn_list");
      this.b(var2, "", c);
      this.b(var2, "mang", b);
   }

   public NBTTagCompound func_189551_b(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      this.a(var2, "", c);
      this.a(var2, "mang", b);
      var1.func_74782_a("sexmod:galath_spawn_list", var2);
      return var1;
   }

   void a(NBTTagCompound var1, String var2, List<BlockPos> var3) {
      var1.func_74768_a("sexmod:pos_amount" + var2, var3.size());
      int var4 = 0;

      for (BlockPos var6 : var3) {
         var1.func_74768_a("sexmod:x" + var2 + var4, var6.func_177958_n());
         var1.func_74768_a("sexmod:y" + var2 + var4, var6.func_177956_o());
         var1.func_74768_a("sexmod:z" + var2 + var4, var6.func_177952_p());
         var4++;
      }
   }

   void b(NBTTagCompound var1, String var2, List<BlockPos> var3) {
      var3.clear();
      int var4 = var1.func_74762_e("sexmod:pos_amount" + var2);

      for (int var5 = 0; var5 < var4; var5++) {
         var3.add(
            new BlockPos(var1.func_74762_e("sexmod:x" + var2 + var5), var1.func_74762_e("sexmod:y" + var2 + var5), var1.func_74762_e("sexmod:z" + var2 + var5))
         );
      }
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
