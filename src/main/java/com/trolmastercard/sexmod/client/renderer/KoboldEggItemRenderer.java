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
   public void render(KoboldEggItem var1, ItemStack var2) {
      this.renderEggItem(var1, var2);
   }

   public void renderEggItem(KoboldEggItem var1, ItemStack var2) {
      this.eggStack = var2;
      super.render(var1, var2);
   }

   /**
    * Per-bone tint pass: {@code shell} -> static {@link KoboldEggRenderer#eggColor},
    * {@code colorSpots} -> the wool-id color of the held stack (metadata).
    */
   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if ("shell".equals(var7)) {
         var3 = KoboldEggRenderer.eggColor.getRed() / 255.0F;
         var4 = KoboldEggRenderer.eggColor.getGreen() / 255.0F;
         var5 = KoboldEggRenderer.eggColor.getBlue() / 255.0F;
      }

      if ("colorSpots".equals(var7)) {
         Vec3i var8 = this.getEggColor(this.eggStack).getMainColor();
         var3 = var8.getX() / 255.0F;
         var4 = var8.getY() / 255.0F;
         var5 = var8.getZ() / 255.0F;
      }

      super.renderRecursively(var1, var2, var3, var4, var5, var6);
   }

   EyeAndKoboldColor getEggColor(ItemStack var1) {
      return EyeAndKoboldColor.getColorByWoolId(var1.getMetadata());
   }
}
