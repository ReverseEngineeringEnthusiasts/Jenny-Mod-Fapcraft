package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.SendChatMessagePacket;
import com.trolmastercard.sexmod.util.WorldUtils;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

/**
 * Chat output for a girl: local player messages, packeted server chat and the
 * "message to tracking players" broadcast.
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps its
 * methods as delegates. Overridable hooks ({@code sendGirlChatMessage},
 * {@code isControlledByLocalPlayer}) are invoked through the girl instance so
 * subclass overrides still dispatch.
 */
final class GirlChat {
   private GirlChat() {
   }

   static void sendMessageToTrackingPlayers(BaseGirlEntity girl, String message) {
      for (EntityPlayer player : WorldUtils.getNearbyPlayers(girl)) {
         player.sendMessage(new TextComponentString(message));
      }
   }

   /** Broadcasts "&lt;name&gt; message" chat to nearby players (both sides). */
   static void sendGirlChatMessage(BaseGirlEntity girl, String message) {
      if (!girl.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(String.format("<%s> %s", girl.getEffectiveDisplayName(), message), girl.dimension, girl.getGirlId()),
               new TargetPoint(girl.dimension, girl.posX, girl.posY, girl.posZ, 40.0)
            );
      } else if (girl.isControlledByLocalPlayer()) {
         PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(String.format("<%s> %s", girl.getEffectiveDisplayName(), message), girl.dimension, girl.getGirlId()));
      }
   }

   /** Sends the given message as the girl; if not local, additionally calls sendGirlChatMessage. */
   static void broadcastChatAround(BaseGirlEntity girl, String message, boolean toAll) {
      if (!toAll) {
         girl.sendGirlChatMessage(message);
      }

      if (!girl.world.isRemote) {
         PacketHandler.networkWrapper
            .sendToAllAround(
               new SendChatMessagePacket(message, girl.dimension, girl.getGirlId()),
               new TargetPoint(girl.dimension, girl.posX, girl.posY, girl.posZ, 40.0)
            );
      } else {
         if (girl.isControlledByLocalPlayer()) {
            PacketHandler.networkWrapper.sendToServer(new SendChatMessagePacket(message, girl.dimension, girl.getGirlId()));
         }
      }
   }

   static void sendChatMessage(BaseGirlEntity girl, String message) {
      if (girl.world.isRemote) {
         Minecraft.getMinecraft().player.sendMessage(new TextComponentString(String.format("<%s> %s", girl.getEffectiveDisplayName(), message)));
      }
   }

   static void sendChatMessageToPlayer(BaseGirlEntity girl, UUID uuid, String message) {
      EntityPlayer player = girl.world.getPlayerEntityByUUID(uuid);
      if (player == null) {
         System.out.println("Player with UUID " + uuid.toString() + " not found");
      } else {
         if (girl.world.isRemote) {
            Minecraft.getMinecraft().player.sendMessage(new TextComponentString("<" + player.getName() + "> " + message));
         }
      }
   }
}
