package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.Main;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <b>Role.</b> User-supplied player skin, read from {@code sexmod/player_skin/}
 * (i.e. {@code .minecraft/sexmod/player_skin/}). This is what the old workflow
 * did by replacing {@code assets/sexmod/textures/player/steve.png} inside the
 * jar, without touching the jar:
 * <ul>
 *   <li>{@code steve.png} — wide (4px) arms, the vanilla "default" model</li>
 *   <li>{@code alex.png} — slim (3px) arms, the vanilla "slim" model</li>
 * </ul>
 * The file name picks the model type, so a slim skin also switches the scene
 * bodies to the Alex arm bones ({@code leftArmAlex}/{@code rightArmAlex}) that
 * already exist in every geo model. If both files exist, {@code alex.png} wins.
 * <p>
 * <b>Resolution.</b> The geo models bake their UVs for a 64x64 skin, so any
 * square skin is scaled to 64x64 on load ({@link #normalize(BufferedImage)}).
 * 128x128 skins are supported this way; a non-square sheet (legacy 64x32) is
 * rejected because its layout is not the 64x64 model layout.
 * <p>
 * <b>Scope.</b> The override replaces the local player's skin only. Other
 * players keep their fetched Mojang skins.
 * <p>
 * CLIENT-side only. The image is read once and cached; {@link #reset()} is
 * called from {@code SkinTextureCache.invalidateAll()} (join/leave), which is
 * how a newly added or replaced file is picked up.
 */
@SideOnly(Side.CLIENT)
public final class PlayerSkinOverride {
   /** Model layout the geo textures are baked against. */
   private static final int MODEL_SIZE = 64;
   private static final String WIDE_FILE = "steve.png";
   private static final String SLIM_FILE = "alex.png";

   private static boolean resolved;
   private static File source;
   private static boolean slim;
   private static BufferedImage modelImage;
   private static boolean warned;

   private PlayerSkinOverride() {
   }

   /**
    * Whether the override is the skin for {@code uuid}: the local player's
    * entity id or the session profile id (single player uses the session id
    * when a girl has no interaction player).
    */
   public static boolean appliesTo(UUID uuid) {
      if (uuid == null) {
         return false;
      }

      Minecraft minecraft = Minecraft.getMinecraft();
      if (minecraft.player != null && uuid.equals(minecraft.player.getUniqueID())) {
         return true;
      }

      return uuid.equals(minecraft.getSession().getProfile().getId());
   }

   /** Whether the override uses slim arms; false when there is no override. */
   public static boolean isSlim() {
      resolve();
      return slim;
   }

   /**
    * A fresh 64x64 copy of the override for {@code uuid}, or {@code null} when
    * there is no override for that player. A copy is required because
    * {@code SkinTextureCache} tints the image it is given.
    */
   public static BufferedImage copyFor(UUID uuid) {
      if (!appliesTo(uuid)) {
         return null;
      }

      BufferedImage image = resolve();
      if (image == null) {
         return null;
      }

      BufferedImage copy = new BufferedImage(MODEL_SIZE, MODEL_SIZE, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = copy.createGraphics();
      graphics.drawImage(image, 0, 0, null);
      graphics.dispose();
      return copy;
   }

   /**
    * Fits a skin image to the 64x64 model layout. Returns the image unchanged
    * when it already is 64x64, a bilinear downscale for larger square skins
    * (128x128 and up), and {@code null} for non-square or too-small sheets.
    */
   public static BufferedImage normalize(BufferedImage image) {
      if (image == null) {
         return null;
      }

      int width = image.getWidth();
      int height = image.getHeight();
      if (width == MODEL_SIZE && height == MODEL_SIZE) {
         return image;
      }

      if (width != height || width < MODEL_SIZE) {
         return null;
      }

      BufferedImage scaled = new BufferedImage(MODEL_SIZE, MODEL_SIZE, BufferedImage.TYPE_INT_ARGB);
      Graphics2D graphics = scaled.createGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
      graphics.drawImage(image, 0, 0, MODEL_SIZE, MODEL_SIZE, null);
      graphics.dispose();
      return scaled;
   }

   /** Drops the cached file so the next lookup re-reads the directory. */
   public static void reset() {
      resolved = false;
      source = null;
      slim = false;
      modelImage = null;
      warned = false;
   }

   private static BufferedImage resolve() {
      if (resolved) {
         return modelImage;
      }

      resolved = true;
      File slimFile = find(SLIM_FILE);
      File wideFile = find(WIDE_FILE);
      source = slimFile != null ? slimFile : wideFile;
      if (source == null) {
         return null;
      }

      slim = slimFile != null;
      try {
         modelImage = normalize(ImageIO.read(source));
      } catch (Exception e) {
         modelImage = null;
      }

      if (modelImage == null) {
         slim = false;
      }

      if (modelImage == null && !warned) {
         warned = true;
         Main.LOGGER.warn(
            "Ignoring player skin override {}: use a square PNG of at least 64x64 (128x128 works too)",
            source.getPath()
         );
      }

      return modelImage;
   }

   /** Case-insensitive lookup so a Windows-authored file name also works on Linux. */
   private static File find(String name) {
      File[] files = PlayerSkinOverride.directory().listFiles();
      if (files == null) {
         return null;
      }

      for (File file : files) {
         if (file.isFile() && file.getName().equalsIgnoreCase(name)) {
            return file;
         }
      }

      return null;
   }

   private static File directory() {
      return new File("sexmod", "player_skin");
   }
}
