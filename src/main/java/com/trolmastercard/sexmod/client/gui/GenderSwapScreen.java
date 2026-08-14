package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.StartStandingSexAnimationPacket;
import java.util.UUID;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class GenderSwapScreen {
   public static GenderSwapScreen instance;
   private GenderSwapScreen.a activeButton;

   public void tick() {
      if (instance.activeButton != null) {
         if (--instance.activeButton.countdown <= 0.0F) {
            Minecraft.getMinecraft()
               .player
               .sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.format("genderswap.sexpromt.timeout", new Object[0])));
            this.clearActiveButton();
         }
      }
   }

   public GenderSwapScreen.a getActiveButton() {
      return instance.activeButton;
   }

   void clearActiveButton() {
      instance.activeButton = null;
   }

   public void onButtonClicked(@Nonnull GenderSwapScreen.a var1) {
      World var2 = Minecraft.getMinecraft().player.world;
      EntityPlayer var3 = var2.getPlayerEntityByUUID(var1.playerUUID);
      EntityPlayer var4 = var2.getPlayerEntityByUUID(var1.girlUUID);
      if (var4 != null && var3 != null) {
         TextComponentString var5 = new TextComponentString(
            TextFormatting.LIGHT_PURPLE
               + (var1.isMale ? var4.getName() : var3.getName())
               + " "
               + TextFormatting.DARK_PURPLE
               + I18n.format("genderswap.sexpromt.playerxaskedfory", new Object[0])
               + " "
               + TextFormatting.LIGHT_PURPLE
               + I18n.format(var1.label, new Object[0])
         );
         TextComponentString var6 = new TextComponentString(TextFormatting.DARK_PURPLE + I18n.format("genderswap.sexpromt.autodeletion", new Object[0]));
         TextComponentString var7 = new TextComponentString(
            TextFormatting.DARK_PURPLE
               + "[ "
               + TextFormatting.LIGHT_PURPLE
               + I18n.format("genderswap.sexpromt.accept", new Object[0])
               + TextFormatting.DARK_PURPLE
               + " | "
               + TextFormatting.LIGHT_PURPLE
               + I18n.format("genderswap.sexpromt.decline", new Object[0])
               + TextFormatting.DARK_PURPLE
               + " ]"
         );
         var3.sendMessage(var5);
         var3.sendMessage(var6);
         var3.sendMessage(var7);
         this.activeButton = var1;
      }
   }

   @SubscribeEvent
   public void onClientChat(ClientChatEvent var1) {
      if (instance.getActiveButton() != null) {
         String var2 = var1.getMessage().toLowerCase();
         if (var2.equals(I18n.format("genderswap.sexpromt.accept", new Object[0]).toLowerCase())) {
            GenderSwapScreen.a var3 = instance.getActiveButton();
            this.sendSwapRequest(var3.label, var3.playerUUID, var3.girlUUID);
            this.clearActiveButton();
            var1.setCanceled(true);
         }

         if (var2.equals(I18n.format("genderswap.sexpromt.decline", new Object[0]).toLowerCase())) {
            Minecraft.getMinecraft()
               .player
               .sendMessage(
                  new TextComponentString(TextFormatting.DARK_PURPLE + I18n.format("genderswap.sexpromt.declineconformation", new Object[0]))
               );
            this.clearActiveButton();
            var1.setCanceled(true);
         }
      }
   }

   void sendSwapRequest(String var1, UUID var2, UUID var3) {
      PacketHandler.networkWrapper.sendToServer(new StartStandingSexAnimationPacket(var2, var3, var1));
   }

   public static class a {
      public String label;
      public UUID girlUUID;
      public UUID playerUUID;
      public float countdown;
      boolean isMale;

      public a(String var1, UUID var2, UUID var3, boolean var4) {
         this.label = var1;
         this.girlUUID = var2;
         this.playerUUID = var3;
         this.countdown = 1200.0F;
         this.isMale = var4;
      }
   }
}
