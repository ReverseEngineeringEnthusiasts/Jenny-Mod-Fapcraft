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
import org.lwjgl.opengl.GL11;

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
      if (trailRenderers.isEmpty() || mc.player == null) {
         return;
      }

      // snapshot every flag this pass touches so it can be put back exactly as found
      boolean lighting = GL11.glIsEnabled(GL11.GL_LIGHTING);
      boolean alpha = GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
      boolean depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      try {
         mc.renderEngine.bindTexture(cummyTexture);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder buffer = tessellator.getBuffer();
         float partialTicks = event.getPartialTicks();
         GlStateManager.disableLighting();
         GlStateManager.enableAlpha();

         for (DynamicTrailRenderer renderer : trailRenderers) {
            renderer.renderTrail(mc, tessellator, buffer, partialTicks);
         }
      } finally {
         restore(depth, GlStateManager::enableDepth, GlStateManager::disableDepth);
         restore(alpha, GlStateManager::enableAlpha, GlStateManager::disableAlpha);
         restore(lighting, GlStateManager::enableLighting, GlStateManager::disableLighting);
      }
   }

   private static void restore(boolean enabled, Runnable enable, Runnable disable) {
      if (enabled) {
         enable.run();
      } else {
         disable.run();
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
