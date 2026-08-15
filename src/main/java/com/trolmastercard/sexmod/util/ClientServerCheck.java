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
      String threadName = Thread.currentThread().getName().toLowerCase();
      if (threadName.contains("server")) {
         return true;
      }

      if (threadName.contains("client")) {
         return false;
      }

      MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
      if (server == null) {
         return false;
      }

      boolean onServerThread = server.isCallingFromMinecraftThread();
      Main.LOGGER.warn("couldn't clarify if is running on a server or client thread. Came to the solution onServer=" + onServerThread);
      return onServerThread;
   }

}
