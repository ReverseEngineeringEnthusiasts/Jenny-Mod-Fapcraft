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

/**
 * <b>Role.</b> NPC editor wand — the customization tool. Right-clicking a girl
 * opens the {@link ClothingScreen} (model-code editor); attacking a girl or
 * left-clicking copies her model-code + part-ids to the clipboard (as
 * {@code code$parts}); while held, its damage value switches the held model
 * between normal/active ({@code applyEditor}).
 */
public class NpcEditorWandItem extends Item {
   public static final NpcEditorWandItem EDITOR_WAND = new NpcEditorWandItem();

   public NpcEditorWandItem() {
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.maxStackSize = 1;
   }

   public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
      if (world.isRemote) {
         this.applyEditor(entity, stack);
      }

      super.onUpdate(stack, world, entity, slot, selected);
   }

   @SideOnly(Side.CLIENT)
   void applyEditor(Entity entity, ItemStack stack) {
      if (entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)entity;
         if (!stack.equals(player.getHeldItemMainhand()) && !stack.equals(player.getHeldItemOffhand())) {
            stack.setItemDamage(0);
         } else {
            RayTraceResult rayTrace = Minecraft.getMinecraft().objectMouseOver;
            stack.setItemDamage(rayTrace != null && BaseGirlEntity.isValidGirl(rayTrace.entityHit) ? 1 : 0);
         }
      }
   }

   @SubscribeEvent
   public void onEntityInteract(EntityInteract event) {
      Entity target = event.getTarget();
      if (target instanceof BaseGirlEntity) {
         if (BaseGirlEntity.isValidGirl(target)) {
            EntityPlayer player = event.getEntityPlayer();
            if (player != null) {
               ItemStack stack = player.getHeldItemMainhand();
               if (stack.getItem() != EDITOR_WAND) {
                  stack = player.getHeldItemOffhand();
               }

               if (stack.getItem() == EDITOR_WAND) {
                  event.setCanceled(true);
                  if (event.getWorld().isRemote) {
                     if (ServerWhitelistManager.isGlobalRenderingDisabled) {
                        ServerWhitelistManager.isGlobalRenderingDisabled = 0 != ServerWhitelistManager.getModelCount(true);
                        if (ServerWhitelistManager.isGlobalRenderingDisabled) {
                           return;
                        }
                     }

                     ClothingScreen.openClothingScreen(((BaseGirlEntity)target).asGirl());
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onAttackEntity(AttackEntityEvent event) {
      Entity target = event.getTarget();
      if (target != null) {
         if (target instanceof BaseGirlEntity) {
            EntityPlayer player = event.getEntityPlayer();
            if (player != null) {
               ItemStack stack = player.getHeldItemMainhand();
               if (stack.getItem() != EDITOR_WAND) {
                  stack = player.getHeldItemOffhand();
               }

               if (stack.getItem() == EDITOR_WAND) {
                  event.setCanceled(true);
                  if (player.world.isRemote) {
                     BaseGirlEntity girl = (BaseGirlEntity)target;
                     String code = girl.getCustomModelCode();
                     String parts = BaseGirlEntity.encodePartIdList(BaseGirlEntity.getAllPartIdsForGirl(girl.getGirlId()));
                     player.sendMessage(
                        new TextComponentString(String.format("%s's model-code: %s%s$%s", girl.getDisplayNameText(), TextFormatting.YELLOW, code, parts))
                     );
                     player.sendMessage(new TextComponentString(TextFormatting.ITALIC + "copied to clipboard"));
                     ThreadNames.copyToClipboard(String.format("%s$%s", code, parts));
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent
   public void onLeftClickBlock(LeftClickBlock event) {
      if (this.canEdit(event.getEntityPlayer(), event.getWorld())) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onLeftClickEmpty(LeftClickEmpty event) {
      this.canEdit(event.getEntityPlayer(), event.getWorld());
   }

   boolean canEdit(EntityPlayer player, World world) {
      if (player == null) {
         return false;
      }

      ItemStack stack = player.getHeldItemMainhand();
      if (stack.getItem() != EDITOR_WAND) {
         stack = player.getHeldItemOffhand();
      }

      if (stack.getItem() != EDITOR_WAND) {
         return false;
      } else if (!world.isRemote) {
         return true;
      } else {
         AbstractPlayerGirlEntity playerGirl = AbstractPlayerGirlEntity.getPlayerGirlByUUID(player.getPersistentID());
         if (playerGirl == null) {
            player.sendStatusMessage(new TextComponentString("you gotta turn into the girl, you want to copy the model-code off"), true);
            return true;
         } else {
            String code = playerGirl.getCustomModelCode();
            String parts = BaseGirlEntity.encodePartIdList(BaseGirlEntity.getAllPartIdsForGirl(playerGirl.getGirlId()));
            player.sendMessage(
               new TextComponentString(
                  String.format("%s's model-code: %s%s$%s", ThreadNames.capitalizeFirst(NpcType.getNpcType(playerGirl).toString()), TextFormatting.YELLOW, code, parts)
               )
            );
            player.sendMessage(new TextComponentString(TextFormatting.ITALIC + "copied to clipboard"));
            ThreadNames.copyToClipboard(String.format("%s$%s", code, parts));
            return true;
         }
      }
   }

   public static void register() {
      EDITOR_WAND.setRegistryName(new ResourceLocation("sexmod", "npc_editor_wand"));
      EDITOR_WAND.setTranslationKey("npc_editor_wand");
      MinecraftForge.EVENT_BUS.register(NpcEditorWandItem.class);
   }

   @SubscribeEvent
   public static void registerItems(Register<Item> event) {
      event.getRegistry().register(EDITOR_WAND);
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public static void onModelRegistry(ModelRegistryEvent event) {
      ModelLoader.setCustomModelResourceLocation(EDITOR_WAND, 0, new ModelResourceLocation("sexmod:npc_editor_wand"));
      ModelLoader.setCustomModelResourceLocation(EDITOR_WAND, 1, new ModelResourceLocation("sexmod:npc_editor_wand_active"));
   }

}
