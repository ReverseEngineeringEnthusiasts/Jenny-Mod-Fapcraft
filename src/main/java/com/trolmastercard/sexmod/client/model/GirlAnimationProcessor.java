package com.trolmastercard.sexmod.client.model;

import java.util.HashMap;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimatableModel;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;

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
