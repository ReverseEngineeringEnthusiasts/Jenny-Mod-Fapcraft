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
   public GirlAnimationController(T animatable, String name, float transitionLengthTicks, AnimationController.IAnimationPredicate<T> animationPredicate) {
      super((T)animatable, name, transitionLengthTicks, animationPredicate);
   }

   public GirlAnimationController(T animatable, String name, float transitionLengthTicks, EasingType easingType, AnimationController.IAnimationPredicate<T> animationPredicate) {
      super((T)animatable, name, transitionLengthTicks, easingType, animationPredicate);
   }

   public GirlAnimationController(T animatable, String name, float transitionLengthTicks, Function<Double, Double> easingFunction, AnimationController.IAnimationPredicate<T> animationPredicate) {
      super((T)animatable, name, transitionLengthTicks, easingFunction, animationPredicate);
   }
}
