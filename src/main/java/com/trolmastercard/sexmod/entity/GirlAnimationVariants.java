package com.trolmastercard.sexmod.entity;

import com.mojang.realmsclient.util.Pair;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import java.util.HashMap;
import java.util.Random;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

/**
 * Randomised animation-variant bookkeeping for the client animation
 * controllers (picks a numbered variant of {@code animName} and remembers it in
 * the girl's {@code animationVariantMap}).
 * <p>
 * Extracted verbatim from {@link BaseGirlEntity}; the entity keeps its
 * protected instance methods as delegates. Movement/action checks are called
 * back through the girl so subclass overrides still dispatch.
 */
final class GirlAnimationVariants {
   private GirlAnimationVariants() {
   }

   static void playRandomizedAnimation(BaseGirlEntity girl, String animName, int maxVariants, float chance, AnimationEvent event, boolean force) {
      if (force || !Action.isActionComplete(girl, event.getPartialTick()) || !girl.handleActionAnimationOverrides(girl.getCurrentAction(), animName, HandlePlayerMovement.isJumping, event)) {
         AnimationController controller = event.getController();
         Pair variants = girl.animationVariantMap.get(animName);
         if (variants == null) {
            variants = Pair.of(0, 0);
         }

         int baseVariant = (Integer)variants.first();
         int maxVariant = (Integer)variants.second();
         if (!Action.isActionComplete(girl, event.getPartialTick())) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation(baseVariant == 0 ? animName : animName + baseVariant, ILoopType.EDefaultLoopTypes.LOOP));
            event.getController().transitionLengthTicks = 0.0;
         } else {
            int variant = pickRandomVariant(girl, baseVariant, maxVariant, maxVariants, chance);
            AnimationBuilder builder = new AnimationBuilder();
            AnimationController current = controller;
            AnimationController selectedController;
            AnimationBuilder selectedBuilder;
            String tempAnimName;
            if (variant == 0) {
               selectedController = current;
               selectedBuilder = builder;
               tempAnimName = animName;
            } else {
               selectedController = current;
               selectedBuilder = builder;
               tempAnimName = animName + variant;
            }

            selectedController.setAnimation(selectedBuilder.addAnimation(animName, ILoopType.EDefaultLoopTypes.LOOP));
            controller.transitionLengthTicks = 0.0;
            HashMap variantsMap = girl.animationVariantMap;
            Integer newValue = variant;
            String newName = animName;
            HashMap currentMap = variantsMap;
            HashMap selectedMap;
            String selectedName;
            Integer selectedValue;
            int maxVar;
            if (variant == 0) {
               selectedMap = currentMap;
               selectedName = newName;
               selectedValue = newValue;
               maxVar = maxVariant;
            } else {
               selectedMap = currentMap;
               selectedName = newName;
               selectedValue = newValue;
               maxVar = variant;
            }

            selectedMap.put(selectedName, Pair.of(selectedValue, maxVar));
         }
      }
   }

   static int pickRandomVariant(BaseGirlEntity girl, int baseVariant, int exclude, int max, float chance) {
      if (baseVariant != 0) {
         return 0;
      }

      Random random = girl.getRNG();
      if (random.nextFloat() > chance) {
         return 0;
      }

      int variant;
      do {
         variant = random.nextInt(max);
      } while ((variant == exclude || variant == 0) && max > 2);

      return variant;
   }
}
