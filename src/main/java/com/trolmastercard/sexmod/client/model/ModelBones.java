package com.trolmastercard.sexmod.client.model;

import javax.annotation.Nullable;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * <b>Role.</b> Null-safe bone access for the GeckoLib models.
 * <p>
 * <b>Pitfall.</b> {@link AnimationProcessor#getBone(String)} returns {@code null}
 * for a bone the model does not have - a model that was never bound to a
 * renderer, an older geo file without a newer bone, or (the case that crashed
 * the game) {@code GalathNpcModel} passing its own processor into the
 * Manglelie pose pass, which looks up Manglelie-only bones. Every lookup in
 * this package goes through here so a missing bone degrades to a no-op instead
 * of a render-thread crash.
 */
public final class ModelBones {

   private ModelBones() {
   }

   /** @return the bone, or {@code null} when the processor/model does not have it */
   @Nullable
   public static IBone get(@Nullable AnimationProcessor processor, String name) {
      return processor == null ? null : processor.getBone(name);
   }

   /** Hides/unhides {@code name}; does nothing when the bone is absent. */
   public static void setHidden(@Nullable AnimationProcessor processor, String name, boolean hidden) {
      IBone bone = get(processor, name);
      if (bone != null) {
         bone.setHidden(hidden);
      }
   }
}
