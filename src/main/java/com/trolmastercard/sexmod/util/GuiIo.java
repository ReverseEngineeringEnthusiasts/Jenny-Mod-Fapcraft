package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import java.io.IOException;

/**
 * Compatibility bridge for the 1.12.2 GUI methods whose real signatures throw
 * {@link IOException} ({@code GuiScreen.mouseClicked}, {@code keyTyped},
 * {@code handleMouseInput}, {@code handleKeyboardInput}, {@code actionPerformed}).
 * The local MCP jar used by the Maven build predates those throws, while the
 * Cleanroom userdev has them. Routing the {@code super} calls through here lets
 * both compile paths see the same source and logs a failed GUI action instead
 * of letting the checked exception escape the overrides.
 */
public final class GuiIo {
   private GuiIo() {
   }

   @FunctionalInterface
   public interface Action {
      void run() throws IOException;
   }

   @FunctionalInterface
   public interface Supplier<T> {
      T get() throws IOException;
   }

   public static void run(Action action) {
      try {
         action.run();
      } catch (IOException e) {
         Main.LOGGER.warn("GUI action failed", e);
      }
   }

   public static <T> T get(Supplier<T> supplier, T fallback) {
      try {
         return supplier.get();
      } catch (IOException e) {
         Main.LOGGER.warn("GUI action failed", e);
         return fallback;
      }
   }
}
