package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.LunaEntity;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiHandler implements IGuiHandler {
   File configFile;
   File dataFile;
   boolean isInitialized = false;

   public GuiHandler() {
   }

   public GuiHandler(boolean var1) {
      this.onGuiOpen();
   }

   @SideOnly(Side.CLIENT)
   void onGuiOpen() {
   }

   public Object getServerGuiElement(int var1, EntityPlayer var2, World var3, int var4, int var5, int var6) {
      if (var1 == 0) {
         try {
            for (BaseGirlEntity var8 : BaseGirlEntity.getGirlEntityList()) {
               if (!var8.world.isRemote
                  && var8.getPosition().getX() == var4
                  && var8.getPosition().getY() == var5
                  && var8.getPosition().getZ() == var6) {
                  if (var8 instanceof LunaEntity) {
                     return new GirlInventoryContainer2((LunaEntity)var8, var2.inventory, UUID.randomUUID());
                  }

                  return new ChestContainer(var8, var2.inventory, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var11) {
         }
      }

      if (var1 == 1) {
         try {
            for (BaseGirlEntity var13 : BaseGirlEntity.getGirlEntityList()) {
               if (!var13.world.isRemote
                  && var13 instanceof IInventory
                  && var13.getPosition().getX() == var4
                  && var13.getPosition().getY() == var5
                  && var13.getPosition().getZ() == var6) {
                  IInventory var9 = (IInventory)var13;
                  return new GirlInventoryContainer(var2.inventory, var9, var2, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var10) {
         }
      }

      return null;
   }

   public Object getClientGuiElement(int var1, EntityPlayer var2, World var3, int var4, int var5, int var6) {
      if (var1 == 0) {
         try {
            for (BaseGirlEntity var8 : BaseGirlEntity.getGirlEntityList()) {
               if (var8.world.isRemote
                  && var8.getPosition().getX() == var4
                  && var8.getPosition().getY() == var5
                  && var8.getPosition().getZ() == var6) {
                  if (var8 instanceof LunaEntity) {
                     return new GirlInventoryContainerGui((LunaEntity)var8, var2.inventory, UUID.randomUUID());
                  }

                  return new GirlInventoryContainerGui2(var8, var2.inventory, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var10) {
         }
      }

      if (var1 == 1) {
         try {
            for (BaseGirlEntity var12 : BaseGirlEntity.getGirlEntityList()) {
               if (var12.world.isRemote
                  && var12 instanceof IInventory
                  && var12.getPosition().getX() == var4
                  && var12.getPosition().getY() == var5
                  && var12.getPosition().getZ() == var6) {
                  return new ChestContainerGui(var2, var12, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var9) {
         }
      }

      return null;
   }

}
