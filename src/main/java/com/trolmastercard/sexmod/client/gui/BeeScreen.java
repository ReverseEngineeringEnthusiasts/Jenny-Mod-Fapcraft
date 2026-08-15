package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Shared transition overlay / black-screen effect used by the girls' scenes
 * (not only bees): when visible it covers the whole screen with an animated
 * scrolling texture for about a second, then hides itself. While animating it
 * also hides the {@link HornyMeterHud}.
 * <p>
 * <b>Usage contract.</b> Scene code calls {@link #runOnMainThread(Runnable)}
 * (which makes the overlay visible and schedules the given action) or
 * {@link #enableInteraction()}; the overlay auto-resets after {@code animTimer}
 * exceeds 69 ticks of animation. {@code isVisible} is a static flag — exactly
 * one transition can play at a time.
 * <p>
 * CLIENT-side only, rendered via the game-overlay event (it never takes input
 * focus; {@code doesGuiPauseGame} returns {@code false}).
 */
@SideOnly(Side.CLIENT)
public class BeeScreen extends GuiScreen {
   public static final int TRADE_TIMEOUT = 1200;
   private static boolean isVisible = false;
   private static double animTimer = 0.0;
   static ResourceLocation TRADE_TEXTURE = new ResourceLocation("sexmod", "textures/gui/transitionscreen.png");
   static ResourceLocation MIRROR_TEXTURE = new ResourceLocation("sexmod", "textures/gui/mirroredtransitionscreen.png");
   static ResourceLocation BLANK_TEXTURE = new ResourceLocation("sexmod", "textures/gui/blackscreen.png");

   public static boolean isBeeScreenVisible() {
      return isVisible;
   }

   public static void enableInteraction() {
      isVisible = true;
   }

   /**
    * Shows the overlay and runs {@code var0} on a daemon thread with a
    * {@value #TRADE_TIMEOUT} ms delay (see {@code ThreadNames.createDaemonThread}),
    * so the screen covers a scene transition while the action executes. Do not
    * call from the render thread expecting immediate execution.
    */
   public static void runOnMainThread(Runnable var0) {
      isVisible = true;
      ThreadNames.createDaemonThread(1200, var0);
   }

   public boolean doesGuiPauseGame() {
      return false;
   }

   /**
    * Renders the animated transition: three tiled strips of the trade texture
    * slide horizontally (speed depends on gui scale) followed by the mirrored
    * strip and a solid black band, all translated by a cosine-lerped offset.
    * After 30 animation ticks the horny meter is hidden; after 69 the overlay
    * resets and un-visibilizes itself. Runs on the CLIENT game-overlay event
    * (ElementType.TEXT).
    */
   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent var1) {
      if (isVisible) {
         if (var1.getType() == ElementType.TEXT) {
            Minecraft var2 = Minecraft.getMinecraft();
            animTimer = animTimer + var2.getTickLength() * 0.75F;
            int var4 = var2.gameSettings.guiScale;
            float var3;
            if (var4 == 1) {
               var3 = (float)RotationHelper.lerpDouble(-1800.0, 1000.0, 0.5 * Math.cos(animTimer / 25.0) + 0.5);
            } else if (var4 == 2) {
               var3 = (float)RotationHelper.lerpDouble(-900.0, 750.0, 0.5 * Math.cos(animTimer / 25.0) + 0.5);
            } else {
               var3 = (float)RotationHelper.lerpDouble(-900.0, 600.0, 0.5 * Math.cos(animTimer / 25.0) + 0.5);
            }

            GlStateManager.pushMatrix();
            if (var4 == 1) {
               GlStateManager.scale(2.0F, 2.0F, 2.0F);
            }

            if (var4 == 2) {
               GlStateManager.scale(1.5, 1.5, 1.5);
            }

            var2.renderEngine.bindTexture(TRADE_TEXTURE);
            this.drawTexturedModalRect(var3, 0.0F, 0, (int)(animTimer * 1.5), 256, 256);
            this.drawTexturedModalRect(var3, 256.0F, 0, (int)(animTimer * 1.5), 256, 256);
            this.drawTexturedModalRect(var3, 512.0F, 0, (int)(animTimer * 1.5), 256, 256);
            var2.renderEngine.bindTexture(MIRROR_TEXTURE);
            this.drawTexturedModalRect(var3 + 600.0F, 0.0F, 0, (int)(animTimer * 1.5), 256, 256);
            this.drawTexturedModalRect(var3 + 600.0F, 256.0F, 0, (int)(animTimer * 1.5), 256, 256);
            this.drawTexturedModalRect(var3 + 600.0F, 512.0F, 0, (int)(animTimer * 1.5), 256, 256);
            var2.renderEngine.bindTexture(BLANK_TEXTURE);
            this.drawTexturedModalRect(var3 + 200.0F, 0.0F, 0, 0, 400, 256);
            this.drawTexturedModalRect(var3 + 200.0F, 256.0F, 0, 0, 400, 256);
            this.drawTexturedModalRect(var3 + 200.0F, 512.0F, 0, 0, 400, 256);
            if (animTimer > 30.0) {
               HornyMeterHud.hideHornyMeter();
            }

            if (animTimer > 69.0) {
               animTimer = 0.0;
               isVisible = false;
            }

            GlStateManager.popMatrix();
         }
      }
   }

}
