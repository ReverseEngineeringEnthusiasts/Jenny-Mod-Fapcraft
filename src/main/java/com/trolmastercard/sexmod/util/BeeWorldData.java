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

/**
 * WorldSavedData for bee companions (tamed bee state per world).
 */
public class BeeWorldData extends WorldSavedData {
   public static final List<BlockPos> hivePositions = new ArrayList<>();
   public static final List<BlockPos> flowerPositions = new ArrayList<>();

   public BeeWorldData() {
      super("sexmod:galath_spawn_list");
   }

   public BeeWorldData(String dataId) {
      super("sexmod:galath_spawn_list");
   }

   public static void addHivePosition(BlockPos hivePos, List<BlockPos> positions) {
      positions.add(hivePos);
   }

   @SubscribeEvent
   public void onSave(Save event) {
      World world = event.getWorld();
      world.getMapStorage().setData("sexmod:galath_spawn_list", this);
      this.markDirty();
   }

   @SubscribeEvent
   public void onLoad(Load event) {
      World world = event.getWorld();
      world.getMapStorage().getOrLoadData(BeeWorldData.class, "sexmod:galath_spawn_list");
   }

   public void readFromNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = nbt.getCompoundTag("sexmod:galath_spawn_list");
      this.readNBT(tag, "", hivePositions);
      this.readNBT(tag, "mang", flowerPositions);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      NBTTagCompound tag = new NBTTagCompound();
      this.writeNBT(tag, "", hivePositions);
      this.writeNBT(tag, "mang", flowerPositions);
      nbt.setTag("sexmod:galath_spawn_list", tag);
      return nbt;
   }

   void writeNBT(NBTTagCompound nbt, String key, List<BlockPos> positions) {
      nbt.setInteger("sexmod:pos_amount" + key, positions.size());
      int i = 0;

      for (BlockPos pos : positions) {
         nbt.setInteger("sexmod:x" + key + i, pos.getX());
         nbt.setInteger("sexmod:y" + key + i, pos.getY());
         nbt.setInteger("sexmod:z" + key + i, pos.getZ());
         i++;
      }
   }

   void readNBT(NBTTagCompound nbt, String key, List<BlockPos> positions) {
      positions.clear();
      int count = nbt.getInteger("sexmod:pos_amount" + key);

      for (int i = 0; i < count; i++) {
         positions.add(
            new BlockPos(nbt.getInteger("sexmod:x" + key + i), nbt.getInteger("sexmod:y" + key + i), nbt.getInteger("sexmod:z" + key + i))
         );
      }
   }

}
