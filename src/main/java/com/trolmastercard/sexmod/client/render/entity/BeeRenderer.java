package com.trolmastercard.sexmod.client.render.entity;

import com.trolmastercard.sexmod.client.render.base.GirlRenderer;


import com.trolmastercard.sexmod.common.entity.girl.BeeEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib renderer for the bee girl ({@link BeeEntity}); all rendering logic
 * lives in {@link GirlRenderer}. Registered per entity type in the client
 * proxy with its {@link BeeModel} and shadow radius.
 */
public class BeeRenderer extends GirlRenderer<BeeEntity> {
   public BeeRenderer(RenderManager renderManager, AnimatedGeoModel<?> model, double shadowSize) {
      super(renderManager, model, shadowSize);
   }
}
