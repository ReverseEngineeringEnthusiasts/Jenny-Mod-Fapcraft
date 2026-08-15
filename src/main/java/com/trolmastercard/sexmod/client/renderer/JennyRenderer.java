package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.JennyEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib renderer for the Jenny NPC ({@link JennyEntity}); all rendering
 * logic lives in {@link GirlRenderer}.
 */
public class JennyRenderer extends GirlRenderer<JennyEntity> {
   public JennyRenderer(RenderManager renderManager, AnimatedGeoModel<?> model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }
}
