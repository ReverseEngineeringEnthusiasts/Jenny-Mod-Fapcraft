package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.SkinFetcher;
import com.trolmastercard.sexmod.item.AlliesLampItem;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the Allies Lamp item: the lamp geo with a skin-based
 * dynamic texture — the local player's fetched skin repainted with the lamp's
 * cyan/white/black face pixels. Falls back to the plain lamp texture on skin
 * fetch failure. Texture cached after first load.
 */
public class AlliesLampModel extends AnimatedGeoModel<AlliesLampItem> {
   ResourceLocation lampTexture = null;

   public ResourceLocation getModelLocation(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "geo/allie/lamp.geo.json");
   }

   @Override
   public ResourceLocation getAnimationFileLocation(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "animations/allie/lamp.animation.json");
   }
   /**
    * Lazily builds and caches the skin-blended lamp texture (see class
    * javadoc). Synchronous {@link SkinFetcher} network call on first use.
    */
   public ResourceLocation getTextureLocation(AlliesLampItem var1) {
      if (this.lampTexture != null) {
         return this.lampTexture;
      }

      try {
         Minecraft var2 = Minecraft.getMinecraft();
         BufferedImage var3 = SkinFetcher.fetchSkin(var2.player.getPersistentID());
         Graphics var4 = var3.getGraphics();
         var4.setColor(new Color(185, 254, 255));
         var4.fillRect(0, 0, 2, 2);
         var4.setColor(new Color(255, 255, 255));
         var4.fillRect(2, 0, 1, 2);
         var4.setColor(new Color(0, 0, 0));
         var4.fillRect(3, 0, 1, 2);
         this.lampTexture = var2.renderEngine.getDynamicTextureLocation("alliesLamp", new DynamicTexture(var3));
      } catch (Exception var5) {
         var5.printStackTrace();
         this.lampTexture = new ResourceLocation("sexmod", "textures/entity/allie/lamp.png");
      }

      return this.lampTexture;
   }

   public ResourceLocation getLampTexture(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "animations/allie/lamp.animation.json");
   }

}
