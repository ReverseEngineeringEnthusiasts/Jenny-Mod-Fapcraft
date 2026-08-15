package com.trolmastercard.sexmod.client.model;

import com.trolmastercard.sexmod.entity.SexSceneEntity;
import com.trolmastercard.sexmod.util.ServerWhitelistManager;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib model for {@link SexSceneEntity} (custom model parts). The model/
 * texture resolve per-part from the server whitelist
 * ({@link ServerWhitelistManager}); item-model parts use the built-in
 * {@code cross} geo/texture instead. Animations are not used
 * ({@code setLivingAnimations} is empty) — the pose comes from the bone
 * attachment logic in {@code SexSceneRenderer}.
 * <p>
 * CLIENT-side render thread only.
 */
public class SexSceneModel extends AnimatedGeoModel<SexSceneEntity> {
   public ResourceLocation getModelLocation(SexSceneEntity var0) {
      return var0.isItemModel ? new ResourceLocation("sexmod", "geo/cross.geo.json") : ServerWhitelistManager.getModelResource(var0.getModelCode());
   }

   public ResourceLocation getTextureLocation(SexSceneEntity var0) {
      return var0.isItemModel ? new ResourceLocation("sexmod", "textures/cross.png") : ServerWhitelistManager.getModelTexture(var0.getModelCode());
   }

   public ResourceLocation getAnimationFileLocation(SexSceneEntity var0) {
      return new ResourceLocation("sexmod", "animations/slime/slime.animation.json");
   }

   @Override
   public void setLivingAnimations(SexSceneEntity var1, Integer var2, @Nullable AnimationEvent var3) {
   }

}
