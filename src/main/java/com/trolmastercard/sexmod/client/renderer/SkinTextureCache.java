package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.SkinFetcher;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Bounded LRU of tinted player skins shared by every girl renderer (see
 * {@link GirlRenderer#SKIN_TEXTURE_CACHE}).
 * <p>
 * <b>Eviction.</b> Access-ordered map of {@code SKIN_CACHE_CAPACITY} entries;
 * evicted and invalidated entries release their GPU texture through the
 * texture manager, except the shared fallback texture which outlives the cache.
 * <p>
 * <b>Threading.</b> {@link #invalidate(UUID)} / {@link #invalidateAll()} run on
 * the client render thread; calls from another thread are rescheduled onto it.
 * Texture creation and eviction both touch GL state and must only happen there.
 * <p>
 * CLIENT-side render thread only.
 */
@SideOnly(Side.CLIENT)
final class SkinTextureCache {
   static final int CAPACITY = 64;
   /**
    * Bounded LRU of tinted player skins (access order).
    */
   static final LinkedHashMap<UUID, ResourceLocation> TEXTURES = new LinkedHashMap<UUID, ResourceLocation>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<UUID, ResourceLocation> eldest) {
         if (this.size() <= CAPACITY) {
            return false;
         }

         if (!eldest.getValue().equals(fallbackSkinLocation)) {
            Minecraft.getMinecraft().getTextureManager().deleteTexture(eldest.getValue());
         }

         return true;
      }
   };
   /**
    * The bundled steve skin as a dynamic texture, registered once and shared by
    * every fallback lookup.
    */
   private static ResourceLocation fallbackSkinLocation;

   private SkinTextureCache() {
   }

   /**
    * Fetches the player skin for {@code uuid} ({@link SkinFetcher}), repaints
    * the head-top/face pixels with the mod's skin-tone colors, registers it as
    * a dynamic texture and caches it. On any fetch error the shared steve
    * fallback texture is cached for the uuid instead.
    */
   static ResourceLocation tint(UUID uuid, RenderManager renderManager, Color baseSkinColor, Color blushColor) {
      BufferedImage skinImage;
      try {
         skinImage = SkinFetcher.fetchSkin(uuid);
         Graphics graphics = skinImage.getGraphics();
         graphics.setColor(baseSkinColor);
         graphics.fillRect(0, 0, 4, 3);
         graphics.setColor(blushColor);
         graphics.fillRect(4, 0, 3, 3);
      } catch (Exception e) {
         ResourceLocation fallbackTexture = getFallbackSkinTexture();
         TEXTURES.put(uuid, fallbackTexture);
         return fallbackTexture;
      }

      ResourceLocation tintedTexture = renderManager.renderEngine.getDynamicTextureLocation("player" + uuid, new DynamicTexture(skinImage));
      TEXTURES.put(uuid, tintedTexture);
      return tintedTexture;
   }

   private static ResourceLocation getFallbackSkinTexture() {
      if (fallbackSkinLocation == null) {
         BufferedImage fallbackImage;
         try {
            fallbackImage = ImageIO.read(
               Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("sexmod", "textures/player/steve.png")).getInputStream()
            );
         } catch (Exception e) {
            fallbackImage = new BufferedImage(64, 64, 2);
         }

         fallbackSkinLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("sexmod:player_fallback", new DynamicTexture(fallbackImage));
      }

      return fallbackSkinLocation;
   }

   /**
    * Drops {@code uuid}'s cached tinted skin and releases its GPU texture; the
    * next render fetches and re-tints. Runs on the client thread — calls from
    * another thread are rescheduled onto it.
    */
   static void invalidate(UUID uuid) {
      if (uuid == null) {
         return;
      }

      Minecraft minecraft = Minecraft.getMinecraft();
      if (!minecraft.isCallingFromMinecraftThread()) {
         minecraft.addScheduledTask(() -> invalidate(uuid));
         return;
      }

      ResourceLocation staleTexture = TEXTURES.remove(uuid);
      if (staleTexture != null && !staleTexture.equals(fallbackSkinLocation)) {
         minecraft.getTextureManager().deleteTexture(staleTexture);
      }
   }

   /**
    * Drops every cached tinted skin (session change / skin update). Same thread
    * rules as {@link #invalidate(UUID)}.
    */
   static void invalidateAll() {
      Minecraft minecraft = Minecraft.getMinecraft();
      if (!minecraft.isCallingFromMinecraftThread()) {
         minecraft.addScheduledTask(SkinTextureCache::invalidateAll);
         return;
      }

      for (ResourceLocation staleTexture : new ArrayList<>(TEXTURES.values())) {
         if (!staleTexture.equals(fallbackSkinLocation)) {
            minecraft.getTextureManager().deleteTexture(staleTexture);
         }
      }

      TEXTURES.clear();
   }
}
