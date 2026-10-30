package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEntity;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

/**
 * <b>Role.</b> The "tribes" world-data layout for {@link KoboldManager}: turns
 * every tribe (color, master, member/saved positions, beds, chests and tasks
 * with their mining targets) into one string-indexed NBT compound and reads it
 * back on load.
 * <p>
 * <b>Pitfalls.</b> Read and write must stay in lockstep — the keys are
 * {@code tribeId<i>}, {@code <tribeId>member<j>pos|id}, {@code <tribeId>bed<j>},
 * {@code <tribeId>chest<j>} and {@code <tribeId><j>taskKind|facing|pos|block<k>}.
 * {@link #readNBTValue} *empties* each key as it reads, so a compound cannot be
 * read twice; an empty id/kind/position string is the end marker of each loop.
 */
final class TribeNbtIO {
   private TribeNbtIO() {
   }

   static String readNBTValue(String key, NBTTagCompound nbt) {
      String value = nbt.getString(key);
      nbt.setString(key, "");
      return value;
   }

   static void read(NBTTagCompound nbt) {
      int i = 0;

      label73:
      while (true) {
         String tribeIdStr = readNBTValue("tribeId" + i, nbt);
         if ("".equals(tribeIdStr)) {
            return;
         }

         UUID tribeUuid = UUID.fromString(tribeIdStr);
         EyeAndKoboldColor color = EyeAndKoboldColor.valueOf(readNBTValue("tribeColor" + i, nbt));
         KoboldManager.setTribeColor(tribeUuid, color);
         String masterStr = readNBTValue("tribeMaster" + i, nbt);
         if (!"".equals(masterStr)) {
            KoboldManager.assignMaster(tribeUuid, UUID.fromString(masterStr));
         }

         int i2 = 0;

         while (true) {
            String memberPosStr = readNBTValue(tribeUuid.toString() + "member" + i2 + "pos", nbt);
            if ("".equals(memberPosStr)) {
               break;
            }

            String memberIdStr = readNBTValue(tribeUuid.toString() + "member" + i2 + "id", nbt);
            if ("".equals(memberIdStr)) {
               break;
            }

            String[] parts = memberPosStr.split("\\|");
            BlockPos pos = new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            UUID memberUuid = UUID.fromString(memberIdStr);
            KoboldManager.setTribeHomeForMember(tribeUuid, memberUuid, pos);
            i2++;
         }

         int i3 = 0;

         while (true) {
            String bedStr = readNBTValue(tribeUuid.toString() + "bed" + i3, nbt);
            if ("".equals(bedStr)) {
               int i4 = 0;

               while (true) {
                  String chestStr = readNBTValue(tribeUuid.toString() + "chest" + i4, nbt);
                  if ("".equals(chestStr)) {
                     int i5 = 0;

                     while (true) {
                        String taskKind = readNBTValue(tribeUuid.toString() + i5 + "taskKind", nbt);
                        if ("".equals(taskKind)) {
                           i++;
                           continue label73;
                        }

                        String facingStr = readNBTValue(tribeUuid.toString() + i5 + "facing", nbt);
                        EnumFacing facing = EnumFacing.NORTH;
                        if (!"".equals(facingStr)) {
                           facing = EnumFacing.byName(facingStr);
                        }

                        String taskPosStr = readNBTValue(tribeUuid.toString() + i5 + "pos", nbt);
                        String[] taskParts = taskPosStr.split("\\|");
                        BlockPos taskPos = new BlockPos(Integer.parseInt(taskParts[0]), Integer.parseInt(taskParts[1]), Integer.parseInt(taskParts[2]));
                        HashSet targetBlocks = new HashSet();
                        int i6 = 0;

                        while (true) {
                           String blockStr = readNBTValue(tribeUuid.toString() + i5 + "block" + i6, nbt);
                           if ("".equals(blockStr)) {
                              KoboldManager.addTask(tribeUuid, new KoboldTask(taskPos, KoboldTask.TaskType.valueOf(taskKind), targetBlocks, facing));
                              i5++;
                              break;
                           }

                           String[] blockParts = blockStr.split("\\|");
                           BlockPos blockPos = new BlockPos(Integer.parseInt(blockParts[0]), Integer.parseInt(blockParts[1]), Integer.parseInt(blockParts[2]));
                           targetBlocks.add(blockPos);
                           i6++;
                        }
                     }
                  }

                  String[] chestParts = chestStr.split("\\|");
                  BlockPos chestPos = new BlockPos(Integer.parseInt(chestParts[0]), Integer.parseInt(chestParts[1]), Integer.parseInt(chestParts[2]));
                  KoboldManager.addTribeChest(tribeUuid, chestPos);
                  i4++;
               }
            }

            String[] bedParts = bedStr.split("\\|");
            BlockPos bedPos2 = new BlockPos(Integer.parseInt(bedParts[0]), Integer.parseInt(bedParts[1]), Integer.parseInt(bedParts[2]));
            KoboldManager.addTribeBed(tribeUuid, bedPos2);
            i3++;
         }
      }
   }

   static NBTTagCompound write(NBTTagCompound nbt, Map<UUID, KoboldManager.Tribe> tribes) {
      int i = 0;

      for (Entry entry : tribes.entrySet()) {
         KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
         UUID tribeUuid = (UUID)entry.getKey();
         UUID masterUuid = tribe.getMasterPlayerUUID();
         nbt.setString("tribeId" + i, tribeUuid.toString());
         nbt.setString("tribeColor" + i, tribe.tribeColor.toString());
         if (masterUuid != null) {
            nbt.setString("tribeMaster" + i, masterUuid.toString());
         }

         int i2 = 0;
         HashSet seenMemberIds = new HashSet();

         for (KoboldEntity member : tribe.members) {
            if (!member.isDead) {
               BlockPos memberPos = member.getPosition();
               UUID memberUuid = member.getGirlId();
               nbt.setString(
                  tribeUuid.toString() + "member" + i2 + "pos", memberPos.getX() + "|" + memberPos.getY() + "|" + memberPos.getZ()
               );
               nbt.setString(tribeUuid.toString() + "member" + i2 + "id", memberUuid.toString());
               seenMemberIds.add(memberUuid);
               i2++;
            }
         }

         for (Entry entry2 : tribe.savedPositions.entrySet()) {
            UUID memberUuid2 = (UUID)entry2.getKey();
            BlockPos savedPos = (BlockPos)entry2.getValue();
            if (!seenMemberIds.contains(memberUuid2)) {
               nbt.setString(
                  tribeUuid.toString() + "member" + i2 + "pos", savedPos.getX() + "|" + savedPos.getY() + "|" + savedPos.getZ()
               );
               nbt.setString(tribeUuid.toString() + "member" + i2 + "id", memberUuid2.toString());
               seenMemberIds.add(memberUuid2);
               i2++;
            }
         }

         int i3 = 0;

         // jar-verified: the deobf wrote chests under "bed" keys and beds under
         // "chest" keys (crossed); the original writes the addTribeBed set as
         // "bed" and the addTribeChest set as "chest"
         for (BlockPos bedPos : tribe.tribeBeds) {
            nbt.setString(tribeUuid.toString() + "bed" + i3, bedPos.getX() + "|" + bedPos.getY() + "|" + bedPos.getZ());
            i3++;
         }

         int i4 = 0;

         for (BlockPos chestPos : tribe.tribeChests) {
            nbt.setString(tribeUuid.toString() + "chest" + i4, chestPos.getX() + "|" + chestPos.getY() + "|" + chestPos.getZ());
            i4++;
         }

         int i5 = 0;

         for (KoboldTask task : tribe.tasks) {
            nbt.setString(tribeUuid.toString() + i5 + "taskKind", task.taskType.toString());
            nbt.setString(tribeUuid.toString() + i5 + "pos", task.targetPos.getX() + "|" + task.targetPos.getY() + "|" + task.targetPos.getZ());
            nbt.setString(tribeUuid.toString() + i5 + "facing", task.facing.getName());
            int i6 = 0;

            for (BlockPos targetPos : task.miningTargets) {
               nbt.setString(
                  tribeUuid.toString() + i5 + "block" + i6, targetPos.getX() + "|" + targetPos.getY() + "|" + targetPos.getZ()
               );
               i6++;
            }

            i5++;
         }

         i++;
      }

      return nbt;
   }
}
