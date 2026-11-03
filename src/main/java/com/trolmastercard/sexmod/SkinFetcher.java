package com.trolmastercard.sexmod;

import com.trolmastercard.sexmod.util.PlayerSkinOverride;
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
 * Before the network call it asks {@link PlayerSkinOverride} for a local
 * override from {@code sexmod/player_skin/}; every image (fetched or local) is
 * fitted to the 64x64 model layout with
 * {@link PlayerSkinOverride#normalize(BufferedImage)}, which is what makes
 * 128x128 skins render correctly.
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

   /**
    * Fetches the skin for {@code uuid}: the local override when one exists for
    * that player, otherwise the Mojang session skin. The returned image is
    * always ready for the 64x64 model UVs.
    *
    * @param uuid the player UUID (hyphens stripped for the API call)
    * @return the skin image, the bundled steve texture on any failure, or
    *         {@code null} if even the fallback is unavailable
    */
   @SideOnly(Side.CLIENT)
   public static BufferedImage fetchSkin(UUID uuid) {
      BufferedImage localSkin = PlayerSkinOverride.copyFor(uuid);
      if (localSkin != null) {
         return localSkin;
      }

      try {
         URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString().replace("-", ""));
         String profileJson;
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            profileJson = reader.lines().collect(Collectors.joining());
         }

         int valueStart = profileJson.indexOf("\"value\" : ");
         int valueIdx = valueStart + 11;
         StringBuilder value = new StringBuilder();

         for (int i = 0; profileJson.charAt(valueIdx + i) != '"'; i++) {
            value.append(profileJson.charAt(valueIdx + i));
         }

         String decoded = new String(Base64.getDecoder().decode(value.toString()));
         int urlStart = decoded.indexOf("\"url\" : ");
         int urlIdx = urlStart + 9;
         StringBuilder urlBuilder = new StringBuilder();

         for (int i2 = 0; decoded.charAt(urlIdx + i2) != '"'; i2++) {
            urlBuilder.append(decoded.charAt(urlIdx + i2));
         }

         URL textureUrl = new URL(urlBuilder.toString());
         return PlayerSkinOverride.normalize(ImageIO.read(textureUrl));
      } catch (Exception e) {
         try (java.io.InputStream stream = Minecraft.getMinecraft()
            .getResourceManager()
            .getResource(new ResourceLocation("sexmod", "textures/player/steve.png"))
            .getInputStream()) {
            return PlayerSkinOverride.normalize(ImageIO.read(stream));
         } catch (java.io.IOException ioe) {
            return null;
         }
      }
   }

}
