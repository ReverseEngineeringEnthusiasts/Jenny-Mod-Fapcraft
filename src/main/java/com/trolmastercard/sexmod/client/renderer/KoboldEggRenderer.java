package com.trolmastercard.sexmod.client.renderer;

import com.trolmastercard.sexmod.entity.EyeAndKoboldColor;
import com.trolmastercard.sexmod.entity.KoboldEggEntity;







import net.minecraft.util.ResourceLocation;

import java.awt.Color;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.math.Vec3i;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class KoboldEggRenderer extends GeoEntityRenderer<KoboldEggEntity> {
   public static final Color eggColor = new Color(223, 206, 155);
   KoboldEggEntity eggEntity;



   @Override
   public ResourceLocation getEntityTexture(KoboldEggEntity var1) {
      return new ResourceLocation("sexmod", "textures/entity/kobold/egg.png");
   }
   public KoboldEggRenderer(RenderManager var1, AnimatedGeoModel<?> var2) {
      super(var1, (AnimatedGeoModel<KoboldEggEntity>) (AnimatedGeoModel) var2);
   }

   @Override
   public void render(GeoModel var1, KoboldEggEntity var2, float var3, float var4, float var5, float var6, float var7) {
      this.eggEntity = var2;
      super.render(var1, var2, var3, var4, var5, var6, var7);
   }

   @Override
   public void renderRecursively(BufferBuilder var1, GeoBone var2, float var3, float var4, float var5, float var6) {
      String var7 = var2.getName();
      if ("shell".equals(var7)) {
         var3 = eggColor.getRed() / 255.0F;
         var4 = eggColor.getGreen() / 255.0F;
         var5 = eggColor.getBlue() / 255.0F;
      }

      if ("colorSpots".equals(var7)) {
         Vec3i var8 = EyeAndKoboldColor.safeValueOf((String)this.eggEntity.getDataManager().get(KoboldEggEntity.EGG_COLOR)).getMainColor();
         var3 = var8.getX() / 255.0F;
         var4 = var8.getY() / 255.0F;
         var5 = var8.getZ() / 255.0F;
      }

      super.renderRecursively(var1, var2, var3, var4, var5, var6);
   }
}
