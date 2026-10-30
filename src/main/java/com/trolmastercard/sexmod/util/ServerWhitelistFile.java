package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * <b>Role.</b> The whitelist file backing {@link ServerWhitelistManager}:
 * {@code sexmod/custom_models/whitelisted_servers.txt}, one server address per
 * line. Reads it into a set, answers membership tests and rewrites it with one
 * more server appended.
 * <p>
 * <b>Pitfalls.</b> Every read recreates the file when it is missing, so even a
 * membership test touches the disk. Lines are taken verbatim — no trimming, no
 * blank-line filter — so a stored line must match the server address exactly.
 */
final class ServerWhitelistFile {
   private ServerWhitelistFile() {
   }

   static boolean contains(String serverName) {
      return read().contains(serverName);
   }

   static HashSet<String> read() {
      File file = new File(ServerWhitelistManager.WHITELIST_FILE);

      try {
         file.createNewFile();
      } catch (Exception exception) {
         Main.LOGGER.error("could not create the whitelisted servers file", exception);
      }

      HashSet servers = new HashSet();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(file));
         try {
            String line;
            while ((line = reader.readLine()) != null) {
               servers.add(line);
            }
         } finally {
            reader.close();
         }

         return servers;
      } catch (IOException ioException) {
         Main.LOGGER.error("could not read the whitelisted servers file", ioException);
         return new HashSet<>();
      }
   }

   static void addServer(String serverName) {
      File file = new File(ServerWhitelistManager.WHITELIST_FILE);
      file.mkdirs();
      HashSet whitelist = new HashSet();
      if (file.exists()) {
         whitelist = read();
      }

      whitelist.add(serverName);
      file.delete();
      file = new File(ServerWhitelistManager.WHITELIST_FILE);

      try {
         FileWriter writer = new FileWriter(file);
         Object primaryExc = null;
         boolean suppressed = false;

         label77: {
            Throwable throwable;
            try {
               suppressed = true;

               for (String server : (java.util.Collection<String>) (whitelist) ) {
                  writer.write(server + "\n");
               }

               suppressed = false;
               break label77;
            } catch (Throwable caughtThrowable) {
               throwable = caughtThrowable;
               suppressed = false;
            } finally {
               if (suppressed) {
                  if (writer != null) {
                     if (primaryExc != null) {
                        try {
                           writer.close();
                        } catch (Throwable suppressedThrowable) {
                           ((Throwable) primaryExc).addSuppressed(suppressedThrowable);
                        }
                     } else {
                        writer.close();
                     }
                  }
               }
            }

            throw new RuntimeException(throwable);
         }

         if (writer != null) {
            writer.close();
         }
      } catch (IOException ioException) {
         Main.LOGGER.error("could not close the whitelisted servers file", ioException);
      }
   }
}
