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
         Field var1 = Class.forName("software.bernie.geckolib3.model.AnimatedGeoModel").getDeclaredField("animationProcessor");
         var1.setAccessible(true);
         var1.set(this, new GirlAnimationProcessor(this));
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }

   /**
    * Loads the geo model, clears the bone cache and registers every top-level
    * bone (the {@link GirlAnimationProcessor} cache is populated here).
    */
   @Override
   public GeoModel getModel(ResourceLocation var1) {
      GeoModel var2 = super.getModel(var1);
      if (var2 == null) {
         throw new GeoModelException(var1, "Could not find model.");
      }

      this.getAnimationProcessor().clearModelRendererList();

      for (GeoBone var4 : var2.topLevelBones) {
         this.registerBone(var4);
      }

      return var2;
   }

   private static GeoModelException wrapException(GeoModelException var0) {
      return var0;
   }
}
