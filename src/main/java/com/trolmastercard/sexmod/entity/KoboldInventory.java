package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.util.KoboldManager;
import com.trolmastercard.sexmod.util.KoboldTask;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Inventory handling for {@link KoboldEntity}: stacking rules for the 27-slot
 * {@link net.minecraftforge.items.ItemStackHandler} she carries, overflow to a
 * tribe chest, and the walk-to-chest / open-chest sequence.
 * <p>
 * <b>Pitfalls.</b> {@code canInsertItemStack} mutates the passed stack while
 * simulating ({@code stack.setCount(...)}), exactly like the original: do not
 * "fix" that to a copy without re-checking {@code canAssignTask}, which relies
 * on the simulation not filling the simulated handler. {@code hasInventoryItems}
 * returns {@code false} as soon as one slot is non-empty (inverted name kept
 * from the original).
 */
final class KoboldInventory {
   private KoboldInventory() {
   }

   static boolean canStoreInventory(KoboldEntity entity, UUID tribeId, boolean checkOpen) {
      if (hasInventoryItems(entity)) {
         return false;
      }

      if (isTribeChestOpen(entity, tribeId, checkOpen)) {
         entity.a0 = 0;
         return true;
      }

      if (--entity.a0 < 0 && entity.ax) {
         entity.a0 = 300;
         EntityPlayer master = entity.world.getPlayerEntityByUUID(UUID.fromString((String)entity.getDataManager().get(KoboldEntity.MASTER)));
         EyeAndKoboldColor tribeColor = EyeAndKoboldColor.valueOf((String)entity.getDataManager().get(KoboldEntity.CURRENT_ACTION));
         if (master != null) {
            master.sendStatusMessage(
               new TextComponentString(
                  tribeColor.getTextColor()
                     + entity.getDisplayNameText()
                     + "s "
                     + TextFormatting.WHITE
                     + "inventory is full and there are either no chests to put her items in or said chests are full as well"
               ),
               false
            );
         }

         return false;
      } else {
         return false;
      }
   }

   static boolean isTribeChestOpen(KoboldEntity entity, UUID tribeId, boolean checkOpen) {
      HashSet chests = KoboldManager.getTribeChests(tribeId);
      if (chests == null) {
         return false;
      }

      BlockPos chosenChest = null;

      for (BlockPos chestPos : (java.util.Collection<BlockPos>) (chests) ) {
         TileEntityChest chest = (TileEntityChest)entity.world.getTileEntity(chestPos);
         IItemHandler chestHandler = chest.getSingleChestHandler();
         boolean canStore = false;

         for (int i = 0; i < entity.inventory.getSlots(); i++) {
            ItemStack stack = entity.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               for (int slot = 0; slot < chestHandler.getSlots(); slot++) {
                  ItemStack remaining = chestHandler.insertItem(slot, stack, true);
                  if (remaining.getCount() != stack.getCount()) {
                     canStore = true;
                     break;
                  }
               }

               if (canStore) {
                  break;
               }
            }
         }

         if (canStore) {
            if (chosenChest == null) {
               chosenChest = chestPos;
            } else if (entity.getDistanceSq(chosenChest) > entity.getDistanceSq(chestPos)) {
               chosenChest = chestPos;
            }
         }
      }

      if (chosenChest == null) {
         return false;
      }

      if (!(entity.getDistance(chosenChest.getX(), chosenChest.getY(), chosenChest.getZ()) < 2.0)) {
         if (Math.abs(chosenChest.getY() - entity.getPosition().getY()) > 4) {
            if (!checkOpen) {
               return false;
            }

            entity.syncTribeBlocks(chosenChest);
         } else {
            PathNavigate navigator = entity.getNavigator();
            BlockPos standPos = entity.findStandPos(chosenChest);
            navigator.tryMoveToXYZ(standPos.getX(), standPos.getY(), standPos.getZ(), 0.35F);
            if (navigator.getPath() == null) {
               if (!checkOpen) {
                  return false;
               }

               entity.syncTribeBlocks(chosenChest);
            }
         }

         return true;
      } else {
         TileEntityChest chest = (TileEntityChest)entity.world.getTileEntity(chosenChest);
         IItemHandler chestHandler = chest.getSingleChestHandler();

         for (int i = 0; i < entity.inventory.getSlots(); i++) {
            ItemStack stack = entity.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               for (int slot = 0; slot < chestHandler.getSlots(); slot++) {
                  ItemStack remaining = chestHandler.insertItem(slot, stack, false);
                  if (remaining.getCount() <= 0) {
                     entity.inventory.setStackInSlot(i, ItemStack.EMPTY);
                     break;
                  }

                  entity.inventory.setStackInSlot(i, remaining);
                  stack = remaining;
               }
            }
         }

         entity.world.playSound(null, chosenChest, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         return true;
      }
   }

   static boolean hasInventoryItems(KoboldEntity entity) {
      for (int slot = 0; slot < entity.inventory.getSlots(); slot++) {
         if (!entity.inventory.getStackInSlot(slot).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   static boolean canAssignTask(KoboldEntity entity, KoboldTask task) {
      ArrayList drops = new ArrayList();

      for (BlockPos target : task.getMiningTargets()) {
         try {
            IBlockState state = entity.world.getBlockState(target);
            ItemStack drop = state.getBlock().getItem(entity.world, target, state);
            drops.add(drop);
         } catch (IllegalArgumentException ex) {
         }
      }

      return canStoreItems(entity, drops);
   }

   static boolean canInsertItem(KoboldEntity entity, ItemStack stack) {
      return canInsertItemStack(entity, entity.inventory, stack, true, false);
   }

   static boolean canStoreItems(KoboldEntity entity, List<ItemStack> stacks) {
      ItemStackHandler simulated = new ItemStackHandler(entity.inventory.getSlots());

      for (int slot = 0; slot < simulated.getSlots(); slot++) {
         simulated.setStackInSlot(slot, entity.inventory.getStackInSlot(slot));
      }

      for (ItemStack stack : stacks) {
         if (!canInsertItemStack(entity, simulated, stack, true, false)) {
            return false;
         }
      }

      return true;
   }

   static boolean canExtractItem(KoboldEntity entity, ItemStack stack) {
      return canInsertItemStack(entity, entity.inventory, stack, false, true);
   }

   static boolean canInsertItemStack(KoboldEntity entity, ItemStackHandler handler, ItemStack stack, boolean simulate, boolean extract) {
      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack existing = handler.getStackInSlot(slot);
         if (existing.getItem() == stack.getItem() && existing.getMetadata() == stack.getMetadata()) {
            int maxStack = existing.getMaxStackSize();
            if (maxStack > stack.getCount() + existing.getCount()) {
               if (!simulate) {
                  existing.setCount(existing.getCount() + stack.getCount());
               }

               return true;
            }

            int space = maxStack - existing.getCount();
            existing.setCount(maxStack);
            stack.setCount(stack.getCount() - space);
         }
      }

      for (int slot = 0; slot < handler.getSlots(); slot++) {
         ItemStack slotStack = handler.getStackInSlot(slot);
         if (slotStack.getItem() == Items.AIR) {
            if (!simulate) {
               handler.setStackInSlot(slot, stack);
            }

            return true;
         }
      }

      if (simulate) {
         return false;
      }

      if (!extract) {
         return false;
      }

      EntityItem itemEntity = new EntityItem(entity.world);
      itemEntity.setItem(stack);
      itemEntity.setPosition(entity.posX, entity.posY, entity.posZ);
      entity.world.spawnEntity(itemEntity);
      return false;
   }
}
