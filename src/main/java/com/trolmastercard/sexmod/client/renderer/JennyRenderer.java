package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.JennyEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Geckolib renderer for the Jenny NPC ({@link JennyEntity}); all rendering
 * logic lives in {@link GirlRenderer}.
 */
public class JennyRenderer extends GirlRenderer<JennyEntity> {
   public JennyRenderer(RenderManager var1, AnimatedGeoModel<?> var2, double var3) {
      super(var1, var2, var3);
   }
}
