package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.item.GalathCoinItem;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for the Galath coin item (geo/texture/animation locations
 * only; the render animations live in {@code GalathCoinRenderer}).
 */
public class GalathCoinModel extends AnimatedGeoModel<GalathCoinItem> {
   public ResourceLocation getModelLocation(GalathCoinItem var0) { return new ResourceLocation("sexmod", "geo/galath/galath_coin.geo.json"); }

   public ResourceLocation getTextureLocation(GalathCoinItem var0) {
      return new ResourceLocation("sexmod", "textures/items/galath_coin/galath_coin.png");
   }

   public ResourceLocation getAnimationFileLocation(GalathCoinItem var0) {
      return new ResourceLocation("sexmod", "animations/galath/galath_coin.animation.json");
   }
}
