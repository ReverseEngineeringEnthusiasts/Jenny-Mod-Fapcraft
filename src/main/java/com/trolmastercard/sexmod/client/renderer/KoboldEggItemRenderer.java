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

public class KoboldEggItemRenderer extends GeoItemRenderer<KoboldEggItem> {
   ItemStack a = null;

   public KoboldEggItemRenderer() {
      super(new KoboldEggItemModel());
   }

   @Override
   public void render(KoboldEggItem var1, ItemStack var2) {
      this.a(var1, var2);
   }

   public void a(KoboldEggItem var1, ItemStack var2) {
      this.a = var2;
      super.render(var1, var2);
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if ("shell".equals(var7)) {
         var3 = KoboldEggRenderer.b.getRed() / 255.0F;
         var4 = KoboldEggRenderer.b.getGreen() / 255.0F;
         var5 = KoboldEggRenderer.b.getBlue() / 255.0F;
      }

      if ("colorSpots".equals(var7)) {
         Vec3i var8 = this.a_clash797(this.a).getMainColor();
         var3 = var8.getX() / 255.0F;
         var4 = var8.getY() / 255.0F;
         var5 = var8.getZ() / 255.0F;
      }

      super.renderRecursively(var1, var2, var3, var4, var5, var6);
   }

   EyeAndKoboldColor a_clash797(ItemStack var1) {
      return EyeAndKoboldColor.getColorByWoolId(var1.getMetadata());
   }
}
