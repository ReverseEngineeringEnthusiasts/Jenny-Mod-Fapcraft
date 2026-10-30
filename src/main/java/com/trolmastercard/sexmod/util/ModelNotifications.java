package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import com.trolmastercard.sexmod.proxy.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;

/**
 * <b>Role.</b> Where custom-model diagnostics go: on a client they become
 * coloured chat lines for the local player, on a server they go to the logger.
 * Also renders the "your Fapcraft is outdated" chat notice.
 * <p>
 * <b>Pitfalls.</b> On the client a present player means the message is *not*
 * logged at all, so a DEBUG reload reads as chat spam and leaves no log trail.
 * The update notice must stay one {@link TextComponentString} — splitting it
 * loses the {@code §}-colour prefix per line.
 */
final class ModelNotifications {
   private ModelNotifications() {
   }

   static void logError(Level level, String message) {
      if (Main.proxy instanceof ClientProxy) {
         logInfo(level, message);
      } else {
         Main.LOGGER.log(level, message);
      }
   }

   @SideOnly(Side.CLIENT)
   static void logInfo(Level level, String message) {
      EntityPlayerSP player = Minecraft.getMinecraft().player;
      if (player == null) {
         Main.LOGGER.log(level, message);
      } else {
         TextFormatting color;
         if (Level.DEBUG.equals(level)) {
            color = TextFormatting.DARK_GREEN;
         } else if (Level.ERROR.equals(level)) {
            color = TextFormatting.RED;
         } else {
            color = TextFormatting.WHITE;
         }

         player.sendMessage(new TextComponentString(color.toString() + message));
      }
   }

   @SideOnly(Side.CLIENT)
   static String buildOutdatedNotice(int localBuild, String latest, String change) {
      StringBuilder msg = new StringBuilder();
      msg.append("§eYour Fapcraft is outdated! (installed build ").append(localBuild).append(", latest ").append(latest).append(")");
      msg.append("§7\nDownload: §bhttps://github.com/ReverseEngineeringEnthusiasts/Jenny-Mod-Fapcraft/releases");
      if (change != null && !change.isEmpty()) {
         msg.append("\n§7What's new in ").append(latest).append(":\n").append(change);
      }

      return msg.toString();
   }
}
