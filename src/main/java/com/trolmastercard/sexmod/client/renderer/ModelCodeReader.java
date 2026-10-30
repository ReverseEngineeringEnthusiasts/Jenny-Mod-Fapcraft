package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.Main;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Reads a model-code file from disk into the string form the model code
 * evaluator expects (see {@link GirlRenderer#buildModelCode(String)}).
 * <p>
 * <b>Format.</b> Every source line gets a trailing {@code //} so line
 * comments survive concatenation. A read failure is logged through
 * {@link Main#LOGGER} and yields whatever was read so far.
 * <p>
 * CLIENT-side render thread only.
 */
@SideOnly(Side.CLIENT)
final class ModelCodeReader {
   private ModelCodeReader() {
   }

   /**
    * Reads {@code path} line by line, appending {@code "//\n"} after each line.
    *
    * @return the assembled model code, empty if the file could not be read
    */
   static String read(String path) {
      StringBuilder builder = new StringBuilder();

      try {
         BufferedReader reader = new BufferedReader(new FileReader(path));

         String line;
         while ((line = reader.readLine()) != null) {
            builder.append(line).append("//\n");
         }

         reader.close();
      } catch (IOException e) {
         Main.LOGGER.error("could not read the model code file", e);
      }

      return builder.toString();
   }
}
