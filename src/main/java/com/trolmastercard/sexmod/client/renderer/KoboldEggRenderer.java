package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEggEntity;
import net.minecraft.util.ResourceLocation;
import java.awt.Color;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

/**
 * Entity renderer for the thrown/spawned kobold egg: geckolib egg model with
 * the {@code shell} bone in the static egg color and the {@code colorSpots}
 * bone in the egg's stored color (data-manager {@code EGG_COLOR}), so each egg
 * entity shows its own wool-color variant.
 * <p>
 * CLIENT-side render thread only.
 */
public class KoboldEggRenderer extends GeoEntityRenderer<KoboldEggEntity> {
   public static final Color eggColor = new Color(223, 206, 155);
   KoboldEggEntity eggEntity;

   @Override
   public ResourceLocation getEntityTexture(KoboldEggEntity egg) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/egg.png");
   }
   public KoboldEggRenderer(RenderManager renderManager, AnimatedGeoModel<?> model) {
      super(renderManager, (AnimatedGeoModel<KoboldEggEntity>) (AnimatedGeoModel) model);
   }

   @Override
   public void render(GeoModel model, KoboldEggEntity egg, float r, float g, float b, float a, float ticks) {
      this.eggEntity = egg;
      super.render(model, egg, r, g, b, a, ticks);
   }

   /**
    * Per-bone tint pass: {@code shell} -> static egg color, {@code colorSpots}
    * -> the entity's {@code EGG_COLOR} data-manager value (safe lookup).
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float alpha) {
      String boneName = bone.getName();
      if ("shell".equals(boneName)) {
         r = eggColor.getRed() / 255.0F;
         g = eggColor.getGreen() / 255.0F;
         b = eggColor.getBlue() / 255.0F;
      }

      if ("colorSpots".equals(boneName)) {
         Vec3i eggColor = EyeAndKoboldColor.safeValueOf((String)this.eggEntity.getDataManager().get(KoboldEggEntity.EGG_COLOR)).getMainColor();
         r = eggColor.getX() / 255.0F;
         g = eggColor.getY() / 255.0F;
         b = eggColor.getZ() / 255.0F;
      }

      super.renderRecursively(buffer, bone, r, g, b, alpha);
   }
}
