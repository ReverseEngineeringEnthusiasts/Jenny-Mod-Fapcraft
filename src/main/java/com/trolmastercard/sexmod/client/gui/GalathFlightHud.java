package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.ThreadNames;
import com.trolmastercard.sexmod.util.Rectangle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * HUD showing Galath's flight boost charges (up to 3 pips). Rendered while
 * Galath is being ridden/flying; each pip has an animated charge bar that
 * slides up when a charge is spent and blinks while it regenerates.
 * <p>
 * <b>State.</b> {@link #canUseCharge()} gates charge consumption (3s cooldown
 * between uses); spent charges regenerate one per 5s
 * ({@link #updateChargeRegen()}, driven from the overlay render). UI fades in
 * on {@link #showHud()} and out 500ms after {@link #startFadeOut()}.
 * <p>
 * CLIENT-side only; static state, single instance. Timing is wall-clock
 * ({@code System.currentTimeMillis()}), not game ticks.
 */
@SideOnly(Side.CLIENT)
public class GalathFlightHud extends Gui {
   static final ResourceLocation UI_TEXTURE = new ResourceLocation("sexmod", "textures/gui/galath_flight_ui.png");
   static final Rectangle BACKGROUND_BOUNDS = new Rectangle(0, 77, 128, 41);
   static final Rectangle CHARGE_ACTIVE_BOUNDS = new Rectangle(0, 0, 23, 36);
   static final Rectangle CHARGE_BLINK_BOUNDS = new Rectangle(0, 36, 23, 36);
   static final Rectangle ICON_SHADOWS_BOUNDS = new Rectangle(23, 2, 20, 31);
   static final long FADE_DURATION = 500L;
   static final float ANIMATION_SPEED = 150.0F;
   static final float PIP_SCALE_ACTIVE = 0.075F;
   static final float PIP_SCALE_SPENT = -11.25F;
   static final float[] x = new float[]{-14.25F, -15.5F, -16.875F};
   static final float PIP_FADE_DURATION = 500.0F;
   static final float PIP_SCALE_IDLE = -0.15F;
   static final float PIP_OFFSET_REGEN = 37.5F;
   static final float[] t = new float[]{37.5F, 43.0F, 45.0F};
   static final int UI_Y_OFFSET = 70;
   static final int CHARGE_Y_OFFSET = 70;
   static boolean isUIVisible = false;
   static Minecraft mc = Minecraft.getMinecraft();
   static int availableCharges = 3;
   static long lastChargeUsedTime = 0L;
   static long lastRegenTime = 0L;
   static long uiFadeInStartTime = 0L;
   static long uiFadeOutStartTime = 9223372036854775307L;

   public static boolean canUseCharge() {
      return availableCharges <= 0 ? false : System.currentTimeMillis() - lastChargeUsedTime > 3000L;
   }

   public static void useCharge() {
      availableCharges--;
      lastChargeUsedTime = System.currentTimeMillis();
   }

   /**
    * Regenerates one charge every 5 seconds, measured from the later of the
    * last use and the last regeneration. No-op while at max charges.
    */
   void updateChargeRegen() {
      if (availableCharges != 3) {
         long now = System.currentTimeMillis();
         if (now - Math.max(lastChargeUsedTime, lastRegenTime) >= 5000L) {
            availableCharges++;
            lastRegenTime = now;
         }
      }
   }

   /**
    * Draws the flight HUD (bottom-center): background bar, three pip shadows,
    * and the three charge bars. The whole UI fades in/out with a 500ms window
    * and auto-hides once the fade-out completes. Charge bar animations use
    * {@code easeInOutQuad} over 150ms per pip.
    */
   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent event) {
      this.updateChargeRegen();
      if (isUIVisible) {
         ScaledResolution resolution = event.getResolution();
         int screenWidth = resolution.getScaledWidth();
         int screenHeight = resolution.getScaledHeight();
         int centerX = screenWidth / 2;
         long now = System.currentTimeMillis();
         if (now - uiFadeOutStartTime > 500L) {
            hideHud();
         } else {
            mc.getTextureManager().bindTexture(UI_TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableAlpha();
            float alpha;
            if (now < uiFadeInStartTime + 500L) {
               alpha = (float)(now - uiFadeInStartTime) / 500.0F;
            } else if (now < uiFadeOutStartTime + 500L) {
               alpha = 1.0F + (float)(uiFadeOutStartTime - now) / 500.0F;
            } else {
               alpha = 1.0F;
            }

            alpha = ThreadNames.clampFloat(alpha, 0.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
            this.drawChargeText(BACKGROUND_BOUNDS, centerX - BACKGROUND_BOUNDS.width / 2, screenHeight - 70);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, (int)(centerX - 1.5F * CHARGE_ACTIVE_BOUNDS.width + 1.0F), screenHeight - 70 + 3);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, centerX - CHARGE_ACTIVE_BOUNDS.width / 2 + 1, screenHeight - 70 + 3);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, centerX + CHARGE_ACTIVE_BOUNDS.width / 2 + 1, screenHeight - 70 + 3);
            float chargeProgress = (float)RotationHelper.easeInOutQuad(Math.min(1.0F, (float)(now - lastChargeUsedTime) / 150.0F));
            float regenProgress = chargeProgress == 1.0F ? ThreadNames.clampFloat(1.0F - (float)(now - lastRegenTime) / 500.0F, 0.0F, 1.0F) : 0.0F;
            this.drawChargeBar(1, -1.5F * CHARGE_ACTIVE_BOUNDS.width, regenProgress, chargeProgress, centerX, screenHeight, alpha);
            this.drawChargeBar(2, -CHARGE_ACTIVE_BOUNDS.width / 2.0F, regenProgress, chargeProgress, centerX, screenHeight, alpha);
            this.drawChargeBar(3, CHARGE_ACTIVE_BOUNDS.width / 2.0F, regenProgress, chargeProgress, centerX, screenHeight, alpha);
         }
      }
   }

   /**
    * Renders one charge pip: active icon for an available charge, a
    * scale/translate animated "refill" pip while regenerating, and a
    * sine-blinking empty pip otherwise. Positions are computed per pip index
    * so the animation offsets never collide.
    */
   void drawChargeBar(int pipIndex, float xOffset, float regenProgress, float chargeProgress, int centerX, int screenHeight, float alpha) {
      float spentScale;
      if (availableCharges >= pipIndex) {
         spentScale = 0.0F;
      } else if (availableCharges < pipIndex - 1) {
         spentScale = 1.0F;
      } else {
         spentScale = chargeProgress;
      }

      float regenScale;
      if (availableCharges == pipIndex) {
         regenScale = regenProgress;
      } else {
         regenScale = 0.0F;
      }

      float pipScale = 1.0F + spentScale * 0.075F + regenScale * -0.15F;
      GlStateManager.pushMatrix();
      GlStateManager.scale(pipScale, pipScale, pipScale);
      GlStateManager.translate(spentScale * x[pipIndex - 1] + regenScale * t[pipIndex - 1], spentScale * -11.25F + regenScale * 37.5F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, alpha - spentScale - regenScale);
      this.drawChargeText(CHARGE_ACTIVE_BOUNDS, (int)(centerX + xOffset), screenHeight - 70);
      GlStateManager.resetColor();
      GlStateManager.color(1.0F, 1.0F, 1.0F, (float)Math.sin(Math.PI * spentScale) * 0.5F);
      this.drawChargeText(CHARGE_BLINK_BOUNDS, (int)(centerX + xOffset), screenHeight - 70);
      GlStateManager.popMatrix();
      GlStateManager.resetColor();
   }

   public static void showHud() {
      if (!isUIVisible) {
         isUIVisible = true;
         uiFadeInStartTime = System.currentTimeMillis();
         uiFadeOutStartTime = 9223372036854775307L;
      }
   }

   /**
    * Begins the fade-out; the HUD disappears 500ms later (checked in the
    * overlay render).
    */
   public static void startFadeOut() {
      uiFadeOutStartTime = System.currentTimeMillis();
   }

   public static void hideHud() {
      isUIVisible = false;
      uiFadeOutStartTime = 9223372036854775307L;
      uiFadeInStartTime = 0L;
   }

   void drawChargeText(Rectangle bounds, int x, int y) {
      this.drawTexturedModalRect(x, y, bounds.x, bounds.y, bounds.width, bounds.height);
   }

}
