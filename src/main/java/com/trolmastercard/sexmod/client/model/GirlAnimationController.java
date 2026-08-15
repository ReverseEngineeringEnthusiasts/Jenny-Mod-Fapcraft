package com.trolmastercard.sexmod.client.model;

import java.util.function.Function;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.easing.EasingType;

/**
 * Geckolib {@link AnimationController} subclass used by all girl models. The
 * animation predicates registered through these controllers are where scene
 * transitions happen (see {@code registerControllers} in the girl models and
 * the {@code animationPredicate} switch on {@code Action}).
 * <p>
 * CLIENT-side only; the constructors mirror geckolib's own signatures (no
 * custom logic) — keep them in sync if geckolib's API changes.
 */
public class GirlAnimationController<T extends IAnimatable> extends AnimationController<T> {
   public GirlAnimationController(T var1, String var2, float var3, AnimationController.IAnimationPredicate<T> var4) {
      super((T)var1, var2, var3, var4);
   }

   public GirlAnimationController(T var1, String var2, float var3, EasingType var4, AnimationController.IAnimationPredicate<T> var5) {
      super((T)var1, var2, var3, var4, var5);
   }

   public GirlAnimationController(T var1, String var2, float var3, Function<Double, Double> var4, AnimationController.IAnimationPredicate<T> var5) {
      super((T)var1, var2, var3, var4, var5);
   }
}
