package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.EllieEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib renderer for the Ellie NPC ({@link EllieEntity}); all rendering
 * logic lives in {@link GirlRenderer}.
 */
public class EllieRenderer extends GirlRenderer<EllieEntity> {
   public EllieRenderer(RenderManager renderManager, AnimatedGeoModel<?> model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }
}
