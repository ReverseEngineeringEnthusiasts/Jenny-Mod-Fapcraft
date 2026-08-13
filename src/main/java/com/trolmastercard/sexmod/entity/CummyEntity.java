package com.trolmastercard.sexmod.entity;

import com.trolmastercard.sexmod.api.IPositionProvider;
import com.trolmastercard.sexmod.api.ITargetProvider;
import com.trolmastercard.sexmod.util.DynamicTrailRenderer;







import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CummyEntity {
   static final ResourceLocation cummyTexture = new ResourceLocation("sexmod", "textures/cummy.png");
   static Minecraft mc = Minecraft.getMinecraft();
   static List<DynamicTrailRenderer> trailRenderers = new ArrayList<>();

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(RenderWorldLastEvent var1) {
      mc.renderEngine.bindTexture(cummyTexture);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Tessellator var2 = Tessellator.getInstance();
      BufferBuilder var3 = var2.getBuffer();
      float var4 = var1.getPartialTicks();
      GlStateManager.disableLighting();
      GlStateManager.enableAlpha();
      if (mc.player != null) {
         for (DynamicTrailRenderer var6 : trailRenderers) {
            var6.a(mc, var2, var3, var4);
         }

         GlStateManager.enableDepth();
         GlStateManager.enableLighting();
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void a(ClientTickEvent var1) {
      if (var1.phase != Phase.END) {
         for (DynamicTrailRenderer var3 : trailRenderers) {
            var3.updateTrails();
         }
      }
   }

   public static void a(DynamicTrailRenderer var0) {
      trailRenderers.add(var0);
   }

   public static void a(int var0, IPositionProvider var1, ITargetProvider var2, BaseGirlEntity var3, float var4, float var5) {
      trailRenderers.add(new DynamicTrailRenderer(var0, var1, var2, var3, var4, var5));
   }

   public static void spawnCummyTrails(@Nonnull BaseGirlEntity var0) {
      ArrayList var1 = new ArrayList();

      for (DynamicTrailRenderer var3 : trailRenderers) {
         if (var3.ownerEntity.getGirlId().equals(var0.getGirlId())) {
            var1.add(var3);
         }
      }

      trailRenderers.removeAll(var1);
   }

}
