package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.util.ServerWhitelistManager;







import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.IClientCommand;

public class CommandWhitelistServer extends CommandBase implements IClientCommand {
   public static final CommandWhitelistServer a = new CommandWhitelistServer();

   public String getName() {
      return "whitelistserver";
   }

   public String getUsage(ICommandSender var1) {
      return "/whitelistserver";
   }

   public boolean allowUsageWithoutPrefix(ICommandSender var1, String var2) {
      return false;
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return true;
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) {
      String var4 = ServerWhitelistManager.g_clash134();
      if (var4 == null) {
         var2.sendMessage(new TextComponentString(TextFormatting.YELLOW + "This is a multiplayer feature only"));
      } else if (ServerWhitelistManager.l(var4)) {
         var2.sendMessage(new TextComponentString(TextFormatting.GREEN + "Server is already whitelisted :)"));
      } else {
         boolean var5 = var3.length > 0 && "confirm".equals(var3[0]);
         if (!var5) {
            var2.sendMessage(
               new TextComponentString(
                  TextFormatting.YELLOW + "By whitelisting this server, you allow the server to send you the custom models that are used on it"
               )
            );
            var2.sendMessage(new TextComponentString(TextFormatting.RED + "ONLY WHITELIST SERVERS, WHOSE SERVER OWNER YOU KNOW AND TRUST"));
            var2.sendMessage(new TextComponentString(TextFormatting.YELLOW + "to confirm your decision type:"));
            var2.sendMessage(new TextComponentString(TextFormatting.GREEN + "/whitelistserver confirm"));
         } else {
            ServerWhitelistManager.h(var4);
            var2.sendMessage(new TextComponentString(TextFormatting.GREEN + "confirmed :)"));
            ServerWhitelistManager.a_clash128();
         }
      }
   }

   private static CommandException a(CommandException var0) {
      return var0;
   }
}
