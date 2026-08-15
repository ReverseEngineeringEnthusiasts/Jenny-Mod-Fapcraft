package com.trolmastercard.sexmod.util;

import com.trolmastercard.sexmod.client.renderer.GirlRenderer;
import java.util.HashSet;
import net.minecraft.client.renderer.entity.RenderManager;
import software.bernie.geckolib3.model.AnimatedGeoModel;

/**
 * Renderer for the Slime NPC.
 */
public class SlimeRenderer extends GirlRenderer {
   public SlimeRenderer(RenderManager var1, AnimatedGeoModel var2, double var3) {
      super(var1, var2, var3);
   }

   @Override
   public HashSet<String> getBlacklistedBones() {
      HashSet var1 = super.getBlacklistedBones();
      var1.add("figure");
      return var1;
   }
}
