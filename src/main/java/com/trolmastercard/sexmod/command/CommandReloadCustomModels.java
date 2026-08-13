package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UnknownPacket;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;







import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandReloadCustomModels extends CommandBase {
   public static final CommandReloadCustomModels RELOAD_CUSTOM_MODELS_COMMAND = new CommandReloadCustomModels();

   public String getName() {
      return "reloadcustommodels";
   }

   public String getUsage(ICommandSender var1) {
      return "/reloadcustommodels";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) {
      ServerWhitelistManager.getModelCount(false);

      for (EntityPlayerMP var5 : var1.getPlayerList().getPlayers()) {
         var1.addScheduledTask(() -> PacketHandler.networkWrapper.sendTo(new UnknownPacket(ServerWhitelistManager.getModelScales()), var5));
      }
   }
}
