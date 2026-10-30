package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.reskin.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> The once-per-session update check behind
 * {@link ServerWhitelistManager#checkForUpdatesOnce()}: asks
 * {@link UpdateChecker} for the latest GitHub release on a background thread
 * and posts one chat notice when this build is behind.
 * <p>
 * <b>Pitfalls.</b> The run flag is set before the thread starts, so a failed or
 * offline check is never retried this session. Everything on the thread is
 * wrapped in a catch-all — an update check must never crash the game — and the
 * chat message is only sent from the client thread via
 * {@code addScheduledTask}.
 */
final class UpdateNotice {
   /** Whether the update check already ran this session (never pester per join). */
   private static volatile boolean updateCheckRanThisSession = false;

   private UpdateNotice() {
   }

   @SideOnly(Side.CLIENT)
   static void checkOnce() {
      if (updateCheckRanThisSession) return;
      updateCheckRanThisSession = true;
      Thread t = new Thread(() -> {
         try {
            String latest = UpdateChecker.fetchLatestTag();
            if (latest == null) return; // offline / no release — silent
            final int localBuild = ServerWhitelistManager.localBuild();
            boolean outdated = UpdateChecker.isOutdatedBuild(localBuild, latest);
            if (outdated) {
               if (!UpdateChecker.alreadyNotified("build" + localBuild, latest)) {
                  UpdateChecker.markNotified("build" + localBuild, latest);
                  String body = UpdateChecker.fetchLatestBody();
                  final String change = UpdateChecker.summarizeBody(body, 5);
                  final String notice = ModelNotifications.buildOutdatedNotice(localBuild, latest, change);
                  Minecraft.getMinecraft().addScheduledTask(() -> {
                     if (Minecraft.getMinecraft().player != null) {
                        Minecraft.getMinecraft().player.sendMessage(new TextComponentString(notice));
                     }
                  });
               }
            }
         } catch (Throwable ex) {
            // silent — update check must never crash the game
         }
      }, "sexmod-update-check");
      t.setDaemon(true);
      t.start();
   }
}
