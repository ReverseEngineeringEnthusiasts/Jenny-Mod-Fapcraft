package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

/**
 * <b>Role.</b> {@code /whitelistserver [confirm]} — client-side opt-in for a
 * server to push custom models: adds the current server IP to
 * {@code sexmod/custom_models/whitelisted_servers.txt} and requests the model
 * download. The two-step confirm is a security gate (only whitelist servers you
 * trust).
 */
public class CommandWhitelistServer extends CommandBase implements IClientCommand {
   public static final CommandWhitelistServer WHITELIST_SERVER_COMMAND = new CommandWhitelistServer();

   public String getName() {
      return "whitelistserver";
   }

   public String getUsage(ICommandSender sender) {
      return "/whitelistserver";
   }

   public boolean allowUsageWithoutPrefix(ICommandSender sender, String args) {
      return false;
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return true;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      String key = ServerWhitelistManager.getCustomModelsKey();
      if (key == null) {
         sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "This is a multiplayer feature only"));
      } else if (ServerWhitelistManager.isModelWhitelisted(key)) {
         sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "Server is already whitelisted :)"));
      } else {
         boolean confirmed = args.length > 0 && "confirm".equals(args[0]);
         if (!confirmed) {
            sender.sendMessage(
               new TextComponentString(
                  TextFormatting.YELLOW + "By whitelisting this server, you allow the server to send you the custom models that are used on it"
               )
            );
            sender.sendMessage(new TextComponentString(TextFormatting.RED + "ONLY WHITELIST SERVERS, WHOSE SERVER OWNER YOU KNOW AND TRUST"));
            sender.sendMessage(new TextComponentString(TextFormatting.YELLOW + "to confirm your decision type:"));
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "/whitelistserver confirm"));
         } else {
            ServerWhitelistManager.initWhitelistFile(key);
            sender.sendMessage(new TextComponentString(TextFormatting.GREEN + "confirmed :)"));
            ServerWhitelistManager.reloadCustomModels();
         }
      }
   }

   private static CommandException wrapException(CommandException exception) {
      return exception;
   }
}
