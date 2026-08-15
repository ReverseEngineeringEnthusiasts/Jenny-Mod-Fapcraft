package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.EllieEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib renderer for the Ellie NPC ({@link EllieEntity}); all rendering
 * logic lives in {@link GirlRenderer}.
 */
public class EllieRenderer extends GirlRenderer<EllieEntity> {
   public EllieRenderer(RenderManager var1, AnimatedGeoModel<?> var2, double var3) {
      super(var1, var2, var3);
   }
}
