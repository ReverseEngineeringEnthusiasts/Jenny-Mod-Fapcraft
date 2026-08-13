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

public class AlliesLampModel extends AnimatedGeoModel<AlliesLampItem> {
   ResourceLocation a = null;

   public ResourceLocation getModelLocation(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "geo/allie/lamp.geo.json");
   }



   @Override
   public ResourceLocation getAnimationFileLocation(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "animations/allie/lamp.animation.json");
   }
   public ResourceLocation getTextureLocation(AlliesLampItem var1) {
      if (this.a != null) {
         return this.a;
      }

      try {
         Minecraft var2 = Minecraft.getMinecraft();
         BufferedImage var3 = SkinFetcher.a_clash864(var2.player.getPersistentID());
         Graphics var4 = var3.getGraphics();
         var4.setColor(new Color(185, 254, 255));
         var4.fillRect(0, 0, 2, 2);
         var4.setColor(new Color(255, 255, 255));
         var4.fillRect(2, 0, 1, 2);
         var4.setColor(new Color(0, 0, 0));
         var4.fillRect(3, 0, 1, 2);
         this.a = var2.renderEngine.getDynamicTextureLocation("alliesLamp", new DynamicTexture(var3));
      } catch (Exception var5) {
         var5.printStackTrace();
         this.a = new ResourceLocation("sexmod", "textures/entity/allie/lamp.png");
      }

      return this.a;
   }

   public ResourceLocation a(AlliesLampItem var1) {
      return new ResourceLocation("sexmod", "animations/allie/lamp.animation.json");
   }

}
