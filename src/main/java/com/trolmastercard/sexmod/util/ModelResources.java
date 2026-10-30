package com.trolmastercard.sexmod.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.resource.GeckoLibCache;

/**
 * <b>Role.</b> The GeckoLib resources behind {@link ServerWhitelistManager}'s
 * custom models: turning a {@code .png} into a dynamic texture, a
 * {@code .geo.json} into a {@link RawGeoModel}, and dropping both again when the
 * model cache is cleared on disconnect.
 * <p>
 * <b>Pitfalls.</b> Client-only — every method here touches the render engine or
 * the GeckoLib cache. The textures are registered under the *model name*, so two
 * servers exposing the same model name overwrite each other's texture until the
 * cache is cleared.
 */
final class ModelResources {
   private ModelResources() {
   }

   @SideOnly(Side.CLIENT)
   static ResourceLocation loadTexture(String name, File file) throws IOException {
      BufferedImage image = ImageIO.read(file);
      return Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(name, new DynamicTexture(image));
   }

   @SideOnly(Side.CLIENT)
   static RawGeoModel loadGeoModel(File file) throws IOException {
      StringBuilder content = new StringBuilder();
      BufferedReader reader = new BufferedReader(new FileReader(file));
      try {
         String line;
         while ((line = reader.readLine()) != null) {
            content.append(line);
         }
      } finally {
         reader.close();
      }

      String json = content.toString();
      return Converter.fromJsonString(json);
   }

   @SideOnly(Side.CLIENT)
   static void releaseAll(Map<String, ServerWhitelistManager.ModelData> modelDataMap) {
      for (Map.Entry entry : modelDataMap.entrySet()) {
         ServerWhitelistManager.ModelData modelData = (ServerWhitelistManager.ModelData)entry.getValue();
         if (modelData != null) {
            ResourceLocation fallbackTex = modelData.getFallbackTexture();
            ResourceLocation textureLoc = modelData.getTextureLocation();
            if (fallbackTex != null) {
               GeckoLibCache.getInstance().getGeoModels().remove(fallbackTex);
            }

            if (textureLoc != null) {
               Minecraft.getMinecraft().renderEngine.deleteTexture(textureLoc);
            }
         }
      }
   }
}
