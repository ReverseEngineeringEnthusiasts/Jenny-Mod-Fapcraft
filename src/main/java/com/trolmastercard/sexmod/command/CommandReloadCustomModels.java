package com.trolmastercard.sexmod.command;

import com.trolmastercard.sexmod.networking.PacketHandler;
import com.trolmastercard.sexmod.networking.UnknownPacket;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;







import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandReloadCustomModels extends CommandBase {
   public static final CommandReloadCustomModels a = new CommandReloadCustomModels();

   public String func_71517_b() {
      return "reloadcustommodels";
   }

   public String func_71518_a(ICommandSender var1) {
      return "/reloadcustommodels";
   }

   public int func_82362_a() {
      return 2;
   }

   public void func_184881_a(MinecraftServer var1, ICommandSender var2, String[] var3) {
      ServerWhitelistManager.b_clash126(false);

      for (EntityPlayerMP var5 : var1.func_184103_al().func_181057_v()) {
         var1.func_152344_a(() -> PacketHandler.b.sendTo(new UnknownPacket(ServerWhitelistManager.e_clash144()), var5));
      }
   }
}
