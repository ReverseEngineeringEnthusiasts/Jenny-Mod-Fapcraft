package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * <b>Role.</b> Best-effort CLIENT/SERVER thread detector, used by
 * {@link ThreadNames#createDaemonThread} to name daemon threads. Decides by
 * thread name first ("server"/"client" substring); falls back to
 * {@code isCallingFromMinecraftThread()} with a warning when the name is
 * ambiguous.
 *
 * @return {@code true} if the calling thread is a server thread
 */
public class ClientServerCheck {
   public static boolean getInstance() {
      String var0 = Thread.currentThread().getName().toLowerCase();
      if (var0.contains("server")) {
         return true;
      }

      if (var0.contains("client")) {
         return false;
      }

      MinecraftServer var1 = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (var1 == null) {
         return false;
      }

      boolean var2 = var1.isCallingFromMinecraftThread();
      Main.LOGGER.warn("couldn't clarify if is running on a server or client thread. Came to the solution onServer=" + var2);
      return var2;
   }

}
