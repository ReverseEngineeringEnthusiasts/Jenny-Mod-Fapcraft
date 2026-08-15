package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.client.model.KoboldEggItemModel;
import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.item.KoboldEggItem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;

/**
 * Item renderer for the kobold egg: renders the geckolib egg model, tinting
 * the {@code shell} bone with the static egg base color and the
 * {@code colorSpots} bone with the egg's variant color derived from the item
 * metadata (wool id).
 * <p>
 * CLIENT-side render thread only. The tint overrides the incoming color
 * parameters for those two bone names — keep the name matching intact.
 */
public class KoboldEggItemRenderer extends GeoItemRenderer<KoboldEggItem> {
   ItemStack eggStack = null;

   public KoboldEggItemRenderer() {
      super(new KoboldEggItemModel());
   }

   @Override
   public void render(KoboldEggItem item, ItemStack stack) {
      this.renderEggItem(item, stack);
   }

   public void renderEggItem(KoboldEggItem item, ItemStack stack) {
      this.eggStack = stack;
      super.render(item, stack);
   }

   /**
    * Per-bone tint pass: {@code shell} -> static {@link KoboldEggRenderer#eggColor},
    * {@code colorSpots} -> the wool-id color of the held stack (metadata).
    */
   @Override
   public void renderRecursively(BufferBuilder buffer, GeoBone bone, float r, float g, float b, float alpha) {
      String boneName = bone.getName();
      if ("shell".equals(boneName)) {
         r = KoboldEggRenderer.eggColor.getRed() / 255.0F;
         g = KoboldEggRenderer.eggColor.getGreen() / 255.0F;
         b = KoboldEggRenderer.eggColor.getBlue() / 255.0F;
      }

      if ("colorSpots".equals(boneName)) {
         Vec3i eggColor = this.getEggColor(this.eggStack).getMainColor();
         r = eggColor.getX() / 255.0F;
         g = eggColor.getY() / 255.0F;
         b = eggColor.getZ() / 255.0F;
      }

      super.renderRecursively(buffer, bone, r, g, b, alpha);
   }

   EyeAndKoboldColor getEggColor(ItemStack stack) {
      return EyeAndKoboldColor.getColorByWoolId(stack.getMetadata());
   }
}
