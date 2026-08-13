package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;







import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class SexSceneModel extends AnimatedGeoModel<SexSceneEntity> {
   public ResourceLocation getModelLocation(SexSceneEntity var0) {
      return var0.f ? new ResourceLocation("sexmod", "geo/cross.geo.json") : ServerWhitelistManager.k(var0.a_clash343());
   }

   public ResourceLocation getTextureLocation(SexSceneEntity var0) {
      return var0.f ? new ResourceLocation("sexmod", "textures/cross.png") : ServerWhitelistManager.c_clash137(var0.a_clash343());
   }

   public ResourceLocation getAnimationFileLocation(SexSceneEntity var0) {
      return new ResourceLocation("sexmod", "animations/slime/slime.animation.json");
   }

   @Override
   public void setLivingAnimations(SexSceneEntity var1, Integer var2, @Nullable AnimationEvent var3) {
   }

   private static RuntimeException a(RuntimeException var0) {
      return var0;
   }
}
