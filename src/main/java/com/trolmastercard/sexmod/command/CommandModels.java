package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.reskin.ModelDownloadScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.IClientCommand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> {@code /models} — opens the searchable custom-model archive
 * browser from anywhere, including when packs are already installed. The girl
 * wand only shows the no-models prompt on an empty folder; this command is the
 * direct route to add more packs later.
 */
@SideOnly(Side.CLIENT)
public class CommandModels extends CommandBase implements IClientCommand {
   public static final CommandModels MODELS_COMMAND = new CommandModels();

   public boolean allowUsageWithoutPrefix(ICommandSender sender, String args) {
      return false;
   }

   public String getName() {
      return "models";
   }

   public String getUsage(ICommandSender sender) {
      return "/models";
   }

   public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
      return true;
   }

   public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
      final Minecraft mc = Minecraft.getMinecraft();
      mc.addScheduledTask(new Runnable() {
         @Override
         public void run() {
            ModelDownloadScreen.show(mc, null);
         }
      });
   }
}
