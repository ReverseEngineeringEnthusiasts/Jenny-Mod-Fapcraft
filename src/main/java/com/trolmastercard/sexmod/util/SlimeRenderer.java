package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import java.util.HashSet;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the Slime NPC.
 */
public class SlimeRenderer extends GirlRenderer {
   public SlimeRenderer(RenderManager renderManager, AnimatedGeoModel geoModel, double scaleFactor) {
      super(renderManager, geoModel, scaleFactor);
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      HashSet blacklisted = super.getBlacklistedBones();
      blacklisted.add("figure");
      return blacklisted;
   }
}
