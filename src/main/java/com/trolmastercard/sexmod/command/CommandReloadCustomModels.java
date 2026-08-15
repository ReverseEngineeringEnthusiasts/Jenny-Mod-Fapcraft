package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UnknownPacket;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/**
 * <b>Role.</b> {@code /reloadcustommodels} (op level 2) — reloads the server's
 * custom-model registry and pushes the fresh model scales to every connected
 * player via {@link UnknownPacket} so client-side rendering matches the new
 * registry without a rejoin.
 */
public class CommandReloadCustomModels extends CommandBase {
   public static final CommandReloadCustomModels RELOAD_CUSTOM_MODELS_COMMAND = new CommandReloadCustomModels();

   public String getName() {
      return "reloadcustommodels";
   }

   public String getUsage(ICommandSender sender) {
      return "/reloadcustommodels";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      ServerWhitelistManager.getModelCount(false);

      for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
         server.addScheduledTask(() -> PacketHandler.networkWrapper.sendTo(new UnknownPacket(ServerWhitelistManager.getModelScales()), player));
      }
   }
}
