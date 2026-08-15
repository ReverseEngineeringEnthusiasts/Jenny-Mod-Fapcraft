package com.trolmastercard.sexmod;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Downloads a player's Minecraft skin texture from the Mojang session server
 * and caches/returns it as a {@link BufferedImage} for custom NPC/player-girl
 * textures.
 * <p>
 * CLIENT-side only (network + {@link Minecraft} resource access). Fails safe:
 * any exception (network error, malformed JSON, missing texture URL) falls back
 * to the bundled {@code steve.png} texture; if even that cannot be loaded it
 * returns {@code null} — callers must null-check the result.
 * <p>
 * <b>Pitfall:</b> this performs a synchronous HTTP request on the calling
 * thread; do not invoke it from the render thread during frame rendering.
 */
public class SkinFetcher {
   public static final int maxCacheSize = 3;

   /**
    * Fetches the skin for {@code var0} by querying the Mojang session profile
    * endpoint, base64-decoding the skin {@code value}, extracting the texture
    * URL and downloading the PNG.
    *
    * @param var0 the player UUID (hyphens stripped for the API call)
    * @return the skin image, the bundled steve texture on any failure, or
    *         {@code null} if even the fallback is unavailable
    */
   @SideOnly(Side.CLIENT)
   public static BufferedImage fetchSkin(UUID var0) {
      try {
         URL var1 = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + var0.toString().replace("-", ""));
         BufferedReader var2 = new BufferedReader(new InputStreamReader(var1.openStream()));
         String var3 = var2.lines().collect(Collectors.joining());
         int var4 = var3.indexOf("\"value\" : ");
         int var5 = var4 + 11;
         StringBuilder var6 = new StringBuilder();

         for (int var7 = 0; var3.charAt(var5 + var7) != '"'; var7++) {
            var6.append(var3.charAt(var5 + var7));
         }

         String var13 = new String(Base64.getDecoder().decode(var6.toString()));
         int var8 = var13.indexOf("\"url\" : ");
         int var9 = var8 + 9;
         StringBuilder var10 = new StringBuilder();

         for (int var11 = 0; var13.charAt(var9 + var11) != '"'; var11++) {
            var10.append(var13.charAt(var9 + var11));
         }

         URL var14 = new URL(var10.toString());
         return ImageIO.read(var14);
      } catch (Exception var12) {
         try {
            return ImageIO.read(
               Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("sexmod", "textures/player/steve.png")).getInputStream()
            );
         } catch (java.io.IOException var13) {
            return null;
         }
      }
   }

}
