package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendBlocksPacket;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * <b>Role.</b> Keeps the tribes' bed/chest sets in sync with the world for
 * {@link KoboldManager}: placing a chest next to a tribe bed claims the new half
 * for that tribe, breaking a tracked chest or bed removes it, and the master
 * gets the block markers echoed back in both cases.
 * <p>
 * <b>Pitfalls.</b> Server side only — the callers check {@code world.isRemote},
 * as does this class. The chest branch tracks the bed set and the bed branch
 * tracks the chest set: the two sets are crossed on purpose (jar-verified
 * against the deobf), so "fixing" the names would move the markers to the wrong
 * blocks. Break events read the block from the world, not from the event.
 */
final class TribeBlockTracking {
   private TribeBlockTracking() {
   }

   static void place(World world, BlockPos pos, IBlockState state, Map<UUID, KoboldManager.Tribe> tribes) {
      if (!world.isRemote) {
         if (state.getBlock() instanceof BlockChest) {
            BlockChest.Type chestType = ((BlockChest)world.getBlockState(pos).getBlock()).chestType;
            BlockPos pairedPos = KoboldBlockLookup.findPairedChest(world, pos, chestType);
            if (pairedPos != null) {
               for (Entry entry : tribes.entrySet()) {
                  KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
                  if (tribe.tribeBeds.contains(pairedPos)) {
                     tribe.tribeBeds.add(pos);
                     UUID masterUuid = KoboldManager.findTribeIdWith((UUID)entry.getKey());
                     if (masterUuid != null) {
                        EntityPlayerMP master = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid);
                        if (master != null) {
                           PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(pos, true), master);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   static void breakBlock(World world, BlockPos pos, Map<UUID, KoboldManager.Tribe> tribes) {
      if (!world.isRemote) {
         IBlockState state = world.getBlockState(pos);
         Block block = state.getBlock();
         if (block instanceof BlockChest) {
            for (Entry entry : tribes.entrySet()) {
               KoboldManager.Tribe tribe = (KoboldManager.Tribe)entry.getValue();
               if (tribe.tribeBeds.contains(pos)) {
                  tribe.tribeBeds.remove(pos);
                  UUID masterUuid = KoboldManager.findTribeIdWith((UUID)entry.getKey());
                  if (masterUuid != null) {
                     EntityPlayerMP master = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid);
                     if (master != null) {
                        PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(pos, false), master);
                     }
                  }
               }
            }
         }

         if (block instanceof BlockBed) {
            for (Entry entry2 : tribes.entrySet()) {
               KoboldManager.Tribe tribe2 = (KoboldManager.Tribe)entry2.getValue();
               if (tribe2.tribeChests.contains(pos)) {
                  BlockPos pairedPos = WorldUtils.getStatePos(pos, state);
                  tribe2.tribeChests.remove(pos);
                  tribe2.tribeChests.remove(pairedPos);
                  UUID masterUuid2 = KoboldManager.findTribeIdWith((UUID)entry2.getKey());
                  if (masterUuid2 != null) {
                     EntityPlayerMP master2 = (EntityPlayerMP)world.getPlayerEntityByUUID(masterUuid2);
                     if (master2 != null) {
                        HashSet blocks = new HashSet();
                        blocks.add(pos);
                        blocks.add(pairedPos);
                        PacketHandler.networkWrapper.sendTo(new SendBlocksPacket(blocks, false), master2);
                     }
                  }
               }
            }
         }
      }
   }
}
