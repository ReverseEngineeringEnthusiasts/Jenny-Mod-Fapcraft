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
   File b;
   File c;
   boolean a = false;

   public GuiHandler() {
   }

   public GuiHandler(boolean var1) {
      this.a_clash441();
   }

   @SideOnly(Side.CLIENT)
   void a_clash441() {
   }

   public Object getServerGuiElement(int var1, EntityPlayer var2, World var3, int var4, int var5, int var6) {
      if (var1 == 0) {
         try {
            for (BaseGirlEntity var8 : BaseGirlEntity.ad_clash509()) {
               if (!var8.field_70170_p.field_72995_K
                  && var8.func_180425_c().func_177958_n() == var4
                  && var8.func_180425_c().func_177956_o() == var5
                  && var8.func_180425_c().func_177952_p() == var6) {
                  if (var8 instanceof LunaEntity) {
                     return new GirlInventoryContainer2((LunaEntity)var8, var2.field_71071_by, UUID.randomUUID());
                  }

                  return new ChestContainer(var8, var2.field_71071_by, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var11) {
         }
      }

      if (var1 == 1) {
         try {
            for (BaseGirlEntity var13 : BaseGirlEntity.ad_clash509()) {
               if (!var13.field_70170_p.field_72995_K
                  && var13 instanceof IInventory
                  && var13.func_180425_c().func_177958_n() == var4
                  && var13.func_180425_c().func_177956_o() == var5
                  && var13.func_180425_c().func_177952_p() == var6) {
                  IInventory var9 = (IInventory)var13;
                  return new GirlInventoryContainer(var2.field_71071_by, var9, var2, UUID.randomUUID());
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
            for (BaseGirlEntity var8 : BaseGirlEntity.ad_clash509()) {
               if (var8.field_70170_p.field_72995_K
                  && var8.func_180425_c().func_177958_n() == var4
                  && var8.func_180425_c().func_177956_o() == var5
                  && var8.func_180425_c().func_177952_p() == var6) {
                  if (var8 instanceof LunaEntity) {
                     return new GirlInventoryContainerGui((LunaEntity)var8, var2.field_71071_by, UUID.randomUUID());
                  }

                  return new GirlInventoryContainerGui2(var8, var2.field_71071_by, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var10) {
         }
      }

      if (var1 == 1) {
         try {
            for (BaseGirlEntity var12 : BaseGirlEntity.ad_clash509()) {
               if (var12.field_70170_p.field_72995_K
                  && var12 instanceof IInventory
                  && var12.func_180425_c().func_177958_n() == var4
                  && var12.func_180425_c().func_177956_o() == var5
                  && var12.func_180425_c().func_177952_p() == var6) {
                  return new ChestContainerGui(var2, var12, UUID.randomUUID());
               }
            }
         } catch (ConcurrentModificationException var9) {
         }
      }

      return null;
   }

   private static ConcurrentModificationException a(ConcurrentModificationException var0) {
      return var0;
   }
}
