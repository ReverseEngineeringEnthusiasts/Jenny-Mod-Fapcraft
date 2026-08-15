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

/**
 * Client-side consent prompt for the gender-swap (standing sex) action: when a
 * player requests a swap action on another player, that player receives a chat
 * prompt with accept/decline lines and answers by typing the corresponding
 * word into chat.
 * <p>
 * <b>Flow.</b> {@link #onButtonClicked(SwapButton)} installs the pending
 * {@link SwapButton} and prints the purple prompt (with a 1200-tick countdown
 * shown by {@link #tick()}). {@link #onClientChat(ClientChatEvent)} intercepts
 * the reply: "accept" sends {@link StartStandingSexAnimationPacket} to the
 * server, "decline" just confirms; both cancel the chat event and clear the
 * pending button.
 * <p>
 * CLIENT-side only, singleton ({@link #instance}); only one prompt can be
 * pending at a time.
 */
public class GenderSwapScreen {
   public static GenderSwapScreen instance;
   private GenderSwapScreen.SwapButton activeButton;

   /**
    * Decrements the pending prompt's countdown and clears it with a timeout
    * message when it reaches zero. Call once per CLIENT tick.
    */
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

   public GenderSwapScreen.SwapButton getActiveButton() {
      return instance.activeButton;
   }

   void clearActiveButton() {
      instance.activeButton = null;
   }

   /**
    * Installs {@code button} as the pending swap button and prints the prompt
    * (who asked whom for which action, accept/decline line, auto-deletion
    * notice) to the asked player. No-op unless both involved players are
    * present in the world.
    */
   public void onButtonClicked(@Nonnull GenderSwapScreen.SwapButton button) {
      World world = Minecraft.getMinecraft().player.world;
      EntityPlayer player = world.getPlayerEntityByUUID(button.playerUUID);
      EntityPlayer girl = world.getPlayerEntityByUUID(button.girlUUID);
      if (girl != null && player != null) {
         TextComponentString requestLine = new TextComponentString(
            TextFormatting.LIGHT_PURPLE
               + (button.isMale ? girl.getName() : player.getName())
               + " "
               + TextFormatting.DARK_PURPLE
               + I18n.format("genderswap.sexpromt.playerxaskedfory", new Object[0])
               + " "
               + TextFormatting.LIGHT_PURPLE
               + I18n.format(button.label, new Object[0])
         );
         TextComponentString deleteLine = new TextComponentString(TextFormatting.DARK_PURPLE + I18n.format("genderswap.sexpromt.autodeletion", new Object[0]));
         TextComponentString choiceLine = new TextComponentString(
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
         player.sendMessage(requestLine);
         player.sendMessage(deleteLine);
         player.sendMessage(choiceLine);
         this.activeButton = button;
      }
   }

   /**
    * Chat interception for the prompt reply: "accept" sends the swap request
    * packet, "decline" confirms in chat; both clear the pending button and
    * swallow the message. Matching is case-insensitive against the localized
    * accept/decline strings.
    */
   @SubscribeEvent
   public void onClientChat(ClientChatEvent event) {
      if (instance.getActiveButton() != null) {
         String message = event.getMessage().toLowerCase();
         if (message.equals(I18n.format("genderswap.sexpromt.accept", new Object[0]).toLowerCase())) {
            GenderSwapScreen.SwapButton button = instance.getActiveButton();
            this.sendSwapRequest(button.label, button.playerUUID, button.girlUUID);
            this.clearActiveButton();
            event.setCanceled(true);
         }

         if (message.equals(I18n.format("genderswap.sexpromt.decline", new Object[0]).toLowerCase())) {
            Minecraft.getMinecraft()
               .player
               .sendMessage(
                  new TextComponentString(TextFormatting.DARK_PURPLE + I18n.format("genderswap.sexpromt.declineconformation", new Object[0]))
               );
            this.clearActiveButton();
            event.setCanceled(true);
         }
      }
   }

   /**
    * Sends the standing-sex animation request to the server.
    *
    * @param label the localized action label, e.g. {@code genderswap.sexpromt.missionary}
    * @param playerUuid the asked player's UUID (the girl side)
    * @param girlUuid the requesting player's UUID (the boy side)
    */
   void sendSwapRequest(String label, UUID playerUuid, UUID girlUuid) {
      PacketHandler.networkWrapper.sendToServer(new StartStandingSexAnimationPacket(playerUuid, girlUuid, label));
   }

   /**
    * A pending consent prompt: the requested action label, the two involved
    * UUIDs, whether the requester is male, and the remaining validity time
    * (starts at 1200 ticks, drained by {@link GenderSwapScreen#tick()}).
    */
   public static class SwapButton {
      public String label;
      public UUID girlUUID;
      public UUID playerUUID;
      public float countdown;
      boolean isMale;

      public SwapButton(String label, UUID girlUuid, UUID playerUuid, boolean isMale) {
         this.label = label;
         this.girlUUID = girlUuid;
         this.playerUUID = playerUuid;
         this.countdown = 1200.0F;
         this.isMale = isMale;
      }
   }
}
