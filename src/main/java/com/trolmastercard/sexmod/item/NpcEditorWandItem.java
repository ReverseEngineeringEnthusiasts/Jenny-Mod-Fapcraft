package com.trolmastercard.sexmod.item;

import com.trolmastercard.sexmod.client.gui.ClothingScreen;
import com.trolmastercard.sexmod.entity.AbstractPlayerGirlEntity;
import com.trolmastercard.sexmod.entity.BaseGirlEntity;
import com.trolmastercard.sexmod.entity.NpcType;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import com.trolmastercard.sexmod.util.ThreadNames;







import net.minecraft.util.ResourceLocation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.EntityInteract;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class NpcEditorWandItem extends Item {
   public static final NpcEditorWandItem a = new NpcEditorWandItem();

   public NpcEditorWandItem() {
      this.func_77637_a(CreativeTabs.field_78040_i);
      this.field_77777_bU = 1;
   }

   public void func_77663_a(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      if (var2.field_72995_K) {
         this.a(var3, var1);
      }

      super.func_77663_a(var1, var2, var3, var4, var5);
   }

   @SideOnly(Side.CLIENT)
   void a(Entity var1, ItemStack var2) {
      if (var1 instanceof EntityPlayer) {
         EntityPlayer var3 = (EntityPlayer)var1;
         if (!var2.equals(var3.func_184614_ca()) && !var2.equals(var3.func_184592_cb())) {
            var2.func_77964_b(0);
         } else {
            RayTraceResult var4 = Minecraft.func_71410_x().field_71476_x;
            var2.func_77964_b(var4 != null && BaseGirlEntity.a_clash542(var4.field_72308_g) ? 1 : 0);
         }
      }
   }

   @SubscribeEvent
   public void a(EntityInteract var1) {
      Entity var2 = var1.getTarget();
      if (var2 instanceof BaseGirlEntity) {
         if (BaseGirlEntity.a_clash542(var2)) {
            EntityPlayer var3 = var1.getEntityPlayer();
            if (var3 != null) {
               ItemStack var4 = var3.func_184614_ca();
               if (var4.func_77973_b() != a) {
                  var4 = var3.func_184592_cb();
               }

               if (var4.func_77973_b() == a) {
                  var1.setCanceled(true);
                  if (var1.getWorld().field_72995_K) {
                     if (ServerWhitelistManager.d) {
                        ServerWhitelistManager.d = 0 != ServerWhitelistManager.b_clash126(true);
                        if (ServerWhitelistManager.d) {
                           return;
                        }
                     }

                     ClothingScreen.a_clash825(((BaseGirlEntity)var2).E_clash543());
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void a(AttackEntityEvent var1) {
      Entity var2 = var1.getTarget();
      if (var2 != null) {
         if (var2 instanceof BaseGirlEntity) {
            EntityPlayer var3 = var1.getEntityPlayer();
            if (var3 != null) {
               ItemStack var4 = var3.func_184614_ca();
               if (var4.func_77973_b() != a) {
                  var4 = var3.func_184592_cb();
               }

               if (var4.func_77973_b() == a) {
                  var1.setCanceled(true);
                  if (var3.field_70170_p.field_72995_K) {
                     BaseGirlEntity var5 = (BaseGirlEntity)var2;
                     String var6 = var5.getCustomModelCode();
                     String var7 = BaseGirlEntity.c(BaseGirlEntity.h_clash555(var5.getGirlId()));
                     var3.func_145747_a(
                        new TextComponentString(String.format("%s's model-code: %s%s$%s", var5.getDisplayNameText(), TextFormatting.YELLOW, var6, var7))
                     );
                     var3.func_145747_a(new TextComponentString(TextFormatting.ITALIC + "copied to clipboard"));
                     ThreadNames.a_clash162(String.format("%s$%s", var6, var7));
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void a(LeftClickBlock var1) {
      if (this.a(var1.getEntityPlayer(), var1.getWorld())) {
         var1.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void a(LeftClickEmpty var1) {
      this.a(var1.getEntityPlayer(), var1.getWorld());
   }

   boolean a(EntityPlayer var1, World var2) {
      if (var1 == null) {
         return false;
      }

      ItemStack var3 = var1.func_184614_ca();
      if (var3.func_77973_b() != a) {
         var3 = var1.func_184592_cb();
      }

      if (var3.func_77973_b() != a) {
         return false;
      } else if (!var2.field_72995_K) {
         return true;
      } else {
         AbstractPlayerGirlEntity var4 = AbstractPlayerGirlEntity.getPlayerGirlByUUID(var1.getPersistentID());
         if (var4 == null) {
            var1.func_146105_b(new TextComponentString("you gotta turn into the girl, you want to copy the model-code off"), true);
            return true;
         } else {
            String var5 = var4.getCustomModelCode();
            String var6 = BaseGirlEntity.c(BaseGirlEntity.h_clash555(var4.getGirlId()));
            var1.func_145747_a(
               new TextComponentString(
                  String.format("%s's model-code: %s%s$%s", ThreadNames.b_clash163(NpcType.getNpcType(var4).toString()), TextFormatting.YELLOW, var5, var6)
               )
            );
            var1.func_145747_a(new TextComponentString(TextFormatting.ITALIC + "copied to clipboard"));
            ThreadNames.a_clash162(String.format("%s$%s", var5, var6));
            return true;
         }
      }
   }

   public static void register() {
      a.setRegistryName(new ResourceLocation("sexmod", "npc_editor_wand"));
      a.func_77655_b("npc_editor_wand");
      MinecraftForge.EVENT_BUS.register(NpcEditorWandItem.class);
   }

   @SubscribeEvent
   public static void a(Register<Item> var0) {
      var0.getRegistry().register(a);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void a(ModelRegistryEvent var0) {
      ModelLoader.setCustomModelResourceLocation(a, 0, new ModelResourceLocation("sexmod:npc_editor_wand"));
      ModelLoader.setCustomModelResourceLocation(a, 1, new ModelResourceLocation("sexmod:npc_editor_wand_active"));
   }

}
