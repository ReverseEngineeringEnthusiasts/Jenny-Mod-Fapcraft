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

/**
 * <b>Role.</b> CLIENT-only helper that renders the "cummy" cum-trail
 * overlays: holds a list of {@link DynamicTrailRenderer}s, updates them every
 * client tick and renders the bound texture in the world render pass. Girls
 * register trails from their animation sound listeners (e.g. the creampie
 * and masterbate keyframes in {@link GalathEntity#registerControllers} and
 * the threesome in {@link ManglelieEntity}); {@link #spawnCummyTrails(BaseGirlEntity)}
 * clears all trails of a girl (scene end).
 * <p>
 * <b>Pitfalls.</b> The render list is static and never cleared except via
 * {@code spawnCummyTrails} — trails leak if a girl despawns without a cum
 * reset path.
 */
@SideOnly(Side.CLIENT)
public class CummyEntity {
   static final ResourceLocation cummyTexture = new ResourceLocation("sexmod", "textures/cummy.png");
   static Minecraft mc = Minecraft.getMinecraft();
   static List<DynamicTrailRenderer> trailRenderers = new ArrayList<>();

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderWorldLast(RenderWorldLastEvent event) {
      mc.renderEngine.bindTexture(cummyTexture);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder buffer = tessellator.getBuffer();
      float partialTicks = event.getPartialTicks();
      GlStateManager.disableLighting();
      GlStateManager.enableAlpha();
      if (mc.player != null) {
         for (DynamicTrailRenderer renderer : trailRenderers) {
            renderer.renderTrail(mc, tessellator, buffer, partialTicks);
         }

         GlStateManager.enableDepth();
         GlStateManager.enableLighting();
      }
   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onClientTick(ClientTickEvent event) {
      if (event.phase != Phase.END) {
         for (DynamicTrailRenderer renderer : trailRenderers) {
            renderer.updateTrails();
         }
      }
   }

   public static void registerTrail(DynamicTrailRenderer renderer) {
      trailRenderers.add(renderer);
   }

   public static void createTrail(int maxSegmentsCount, IPositionProvider positionProvider, ITargetProvider targetProvider, BaseGirlEntity girl, float randomnessRadius, float maxDistance) {
      trailRenderers.add(new DynamicTrailRenderer(maxSegmentsCount, positionProvider, targetProvider, girl, randomnessRadius, maxDistance));
   }

   /**
    * Removes every cum trail owned by the given girl (called on scene end so
    * trails do not persist into the next scene).
    */
   public static void spawnCummyTrails(@Nonnull BaseGirlEntity girl) {
      ArrayList toRemove = new ArrayList();

      for (DynamicTrailRenderer renderer : trailRenderers) {
         if (renderer.ownerEntity.getGirlId().equals(girl.getGirlId())) {
            toRemove.add(renderer);
         }
      }

      trailRenderers.removeAll(toRemove);
   }

}
