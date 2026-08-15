package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Forge {@link IGuiHandler} routing girl GUI ids to their screens/containers.
 * <p>
 * <b>GUI ids.</b> 0 = girl equipment (server:
 * {@link GirlInventoryContainer2} for Luna / {@link ChestContainer} otherwise;
 * client: {@link GirlInventoryContainerGui} / {@link GirlInventoryContainerGui2})
 * and 1 = girl 27-slot chest (server: {@link GirlInventoryContainer}; client:
 * {@link ChestContainerGui}). The entity is located by matching its block
 * position against the packet's x/y/z; UUIDs are random per open — the GUI
 * closes by matching the container instance in the static container lists.
 * <p>
 * Server and client sides must return container/screen pairs with the same
 * slot layout or vanilla sync desyncs. Both methods swallow
 * {@link ConcurrentModificationException} while scanning the shared girl list.
 */
public class GuiHandler implements IGuiHandler {

   public GuiHandler() {
   }

   public GuiHandler(boolean unused) {
      this.onGuiOpen();
   }

   @SideOnly(Side.CLIENT)
   void onGuiOpen() {
   }

   /**
    * SERVER-side: builds the server container for the given GUI id by scanning
    * the girl entity list for the girl at the packet position (server-side
    * entities only).
    *
    * @return the container, or {@code null} if no girl matches
    */
   public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
      if (id == 0) {
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (!girl.world.isRemote
                  && girl.getPosition().getX() == x
                  && girl.getPosition().getY() == y
                  && girl.getPosition().getZ() == z) {
                  if (girl instanceof LunaEntity) {
                     return new GirlInventoryContainer2((LunaEntity)girl, player.inventory, UUID.randomUUID());
                  }

                  return new ChestContainer(girl, player.inventory, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException cme) {
         }
      }

      if (id == 1) {
         try {
            for (BaseGirlEntity girl2 : BaseGirlEntity.getGirlEntityList()) {
               if (!girl2.world.isRemote
                  && girl2 instanceof IInventory
                  && girl2.getPosition().getX() == x
                  && girl2.getPosition().getY() == y
                  && girl2.getPosition().getZ() == z) {
                  IInventory girlInventory = (IInventory)girl2;
                  return new GirlInventoryContainer(player.inventory, girlInventory, player, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException cme2) {
         }
      }

      return null;
   }

   /**
    * CLIENT-side: builds the matching GUI screen for the given id by scanning
    * the client-side girl list for the girl at the packet position.
    *
    * @return the screen, or {@code null} if no girl matches
    */
   public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
      if (id == 0) {
         try {
            for (BaseGirlEntity girl : BaseGirlEntity.getGirlEntityList()) {
               if (girl.world.isRemote
                  && girl.getPosition().getX() == x
                  && girl.getPosition().getY() == y
                  && girl.getPosition().getZ() == z) {
                  if (girl instanceof LunaEntity) {
                     return new GirlInventoryContainerGui((LunaEntity)girl, player.inventory, UUID.randomUUID());
                  }

                  return new GirlInventoryContainerGui2(girl, player.inventory, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException cme) {
         }
      }

      if (id == 1) {
         try {
            for (BaseGirlEntity girl2 : BaseGirlEntity.getGirlEntityList()) {
               if (girl2.world.isRemote
                  && girl2 instanceof IInventory
                  && girl2.getPosition().getX() == x
                  && girl2.getPosition().getY() == y
                  && girl2.getPosition().getZ() == z) {
                  return new ChestContainerGui(player, girl2, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException cme2) {
         }
      }

      return null;
   }

}
