package com.trolmastercard.sexmod.client.model;

import java.lang.reflect.Field;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.geo.exception.GeoModelException;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Base class for geckolib girl models. Replaces geckolib's default animation
 * processor with the caching {@link GirlAnimationProcessor} (via reflection on
 * the private {@code animationProcessor} field — keep the field name in sync
 * with the geckolib version) and re-registers all top-level bones after each
 * model load so the name-based bone cache stays correct.
 * <p>
 * CLIENT-side only. {@link #getModel} throws {@link GeoModelException} when
 * the geo file is missing — model files must exist.
 */
public abstract class GirlModelBase<T extends IAnimatable> extends AnimatedGeoModel<T> {
   protected GirlModelBase() {
      try {
         Field animationProcessorField = Class.forName("software.bernie.geckolib3.model.AnimatedGeoModel").getDeclaredField("animationProcessor");
         animationProcessorField.setAccessible(true);
         animationProcessorField.set(this, new GirlAnimationProcessor(this));
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /**
    * Loads the geo model, clears the bone cache and registers every top-level
    * bone (the {@link GirlAnimationProcessor} cache is populated here).
    */
   @Override
   public GeoModel getModel(ResourceLocation location) {
      GeoModel model = super.getModel(location);
      if (model == null) {
         throw new GeoModelException(location, "Could not find model.");
      }

      this.getAnimationProcessor().clearModelRendererList();

      for (GeoBone bone : model.topLevelBones) {
         this.registerBone(bone);
      }

      return model;
   }

   private static GeoModelException wrapException(GeoModelException exception) {
      return exception;
   }
}
