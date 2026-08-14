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
   public static final List<BlockPos> hivePositions = new ArrayList<>();
   public static final List<BlockPos> flowerPositions = new ArrayList<>();
   static final String DATA_KEY = "sexmod:galath_spawn_list";
   static final String SAVE_KEY = "sexmod:galath_spawn_list";

   public BeeWorldData() {
      super("sexmod:galath_spawn_list");
   }

   public BeeWorldData(String var1) {
      super("sexmod:galath_spawn_list");
   }

   public static void addHivePosition(BlockPos var0, List<BlockPos> var1) {
      var1.add(var0);
   }

   @SubscribeEvent
   public void onSave(Save var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().setData("sexmod:galath_spawn_list", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load var1) {
      World var2 = var1.getWorld();
      var2.getMapStorage().getOrLoadData(BeeWorldData.class, "sexmod:galath_spawn_list");
   }

   public void readFromNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.getCompoundTag("sexmod:galath_spawn_list");
      this.readNBT(var2, "", hivePositions);
      this.readNBT(var2, "mang", flowerPositions);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = new NBTTagCompound();
      this.writeNBT(var2, "", hivePositions);
      this.writeNBT(var2, "mang", flowerPositions);
      var1.setTag("sexmod:galath_spawn_list", var2);
      return var1;
   }

   void writeNBT(NBTTagCompound var1, String var2, List<BlockPos> var3) {
      var1.setInteger("sexmod:pos_amount" + var2, var3.size());
      int var4 = 0;

      for (BlockPos var6 : var3) {
         var1.setInteger("sexmod:x" + var2 + var4, var6.getX());
         var1.setInteger("sexmod:y" + var2 + var4, var6.getY());
         var1.setInteger("sexmod:z" + var2 + var4, var6.getZ());
         var4++;
      }
   }

   void readNBT(NBTTagCompound var1, String var2, List<BlockPos> var3) {
      var3.clear();
      int var4 = var1.getInteger("sexmod:pos_amount" + var2);

      for (int var5 = 0; var5 < var4; var5++) {
         var3.add(
            new BlockPos(var1.getInteger("sexmod:x" + var2 + var5), var1.getInteger("sexmod:y" + var2 + var5), var1.getInteger("sexmod:z" + var2 + var5))
         );
      }
   }

}
