package com.trolmastercard.sexmod.client.model;

import java.util.HashMap;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

/**
 * Geckolib {@link AnimationProcessor} that caches registered bones in a map
 * for O(1) lookup by name. The girl renderers/pose code access bones by name
 * every frame (e.g. hair-follow and skirt-follow logic), so this cache is
 * what makes that cheap.
 * <p>
 * CLIENT-side only. The cache must be cleared together with the renderer list
 * (done in {@link #clearModelRendererList}) or stale bone references survive
 * model reloads.
 */
public class GirlAnimationProcessor<T extends IAnimatable> extends AnimationProcessor<T> {
   HashMap<String, IBone> boneCache = new HashMap<>();

   public GirlAnimationProcessor(IAnimatableModel var1) {
      super(var1);
   }

   @Override
   public IBone getBone(String var1) {
      return this.boneCache.get(var1);
   }

   @Override
   public void registerModelRenderer(IBone var1) {
      super.registerModelRenderer(var1);
      this.boneCache.put(var1.getName(), var1);
   }

   @Override
   public void clearModelRendererList() {
      super.clearModelRendererList();
      this.boneCache.clear();
   }
}
