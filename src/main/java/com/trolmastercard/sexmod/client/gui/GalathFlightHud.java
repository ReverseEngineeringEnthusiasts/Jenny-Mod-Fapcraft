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
         long var1 = System.currentTimeMillis();
         if (var1 - Math.max(lastChargeUsedTime, lastRegenTime) >= 5000L) {
            availableCharges++;
            lastRegenTime = var1;
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
   public void onRenderGameOverlay(RenderGameOverlayEvent var1) {
      this.updateChargeRegen();
      if (isUIVisible) {
         ScaledResolution var2 = var1.getResolution();
         int var3 = var2.getScaledWidth();
         int var4 = var2.getScaledHeight();
         int var5 = var3 / 2;
         long var6 = System.currentTimeMillis();
         if (var6 - uiFadeOutStartTime > 500L) {
            hideHud();
         } else {
            mc.getTextureManager().bindTexture(UI_TEXTURE);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.enableAlpha();
            float var8;
            if (var6 < uiFadeInStartTime + 500L) {
               var8 = (float)(var6 - uiFadeInStartTime) / 500.0F;
            } else if (var6 < uiFadeOutStartTime + 500L) {
               var8 = 1.0F + (float)(uiFadeOutStartTime - var6) / 500.0F;
            } else {
               var8 = 1.0F;
            }

            var8 = ThreadNames.clampFloat(var8, 0.0F, 1.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, var8);
            this.drawChargeText(BACKGROUND_BOUNDS, var5 - BACKGROUND_BOUNDS.width / 2, var4 - 70);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, (int)(var5 - 1.5F * CHARGE_ACTIVE_BOUNDS.width + 1.0F), var4 - 70 + 3);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, var5 - CHARGE_ACTIVE_BOUNDS.width / 2 + 1, var4 - 70 + 3);
            this.drawChargeText(ICON_SHADOWS_BOUNDS, var5 + CHARGE_ACTIVE_BOUNDS.width / 2 + 1, var4 - 70 + 3);
            float var9 = (float)RotationHelper.easeInOutQuad(Math.min(1.0F, (float)(var6 - lastChargeUsedTime) / 150.0F));
            float var10 = var9 == 1.0F ? ThreadNames.clampFloat(1.0F - (float)(var6 - lastRegenTime) / 500.0F, 0.0F, 1.0F) : 0.0F;
            this.drawChargeBar(1, -1.5F * CHARGE_ACTIVE_BOUNDS.width, var10, var9, var5, var4, var8);
            this.drawChargeBar(2, -CHARGE_ACTIVE_BOUNDS.width / 2.0F, var10, var9, var5, var4, var8);
            this.drawChargeBar(3, CHARGE_ACTIVE_BOUNDS.width / 2.0F, var10, var9, var5, var4, var8);
         }
      }
   }

   /**
    * Renders one charge pip: active icon for an available charge, a
    * scale/translate animated "refill" pip while regenerating, and a
    * sine-blinking empty pip otherwise. Positions are computed per pip index
    * so the animation offsets never collide.
    */
   void drawChargeBar(int var1, float var2, float var3, float var4, int var5, int var6, float var7) {
      float var8;
      if (availableCharges >= var1) {
         var8 = 0.0F;
      } else if (availableCharges < var1 - 1) {
         var8 = 1.0F;
      } else {
         var8 = var4;
      }

      float var9;
      if (availableCharges == var1) {
         var9 = var3;
      } else {
         var9 = 0.0F;
      }

      float var10 = 1.0F + var8 * 0.075F + var9 * -0.15F;
      GlStateManager.pushMatrix();
      GlStateManager.scale(var10, var10, var10);
      GlStateManager.translate(var8 * x[var1 - 1] + var9 * t[var1 - 1], var8 * -11.25F + var9 * 37.5F, 0.0F);
      GlStateManager.color(1.0F, 1.0F, 1.0F, var7 - var8 - var9);
      this.drawChargeText(CHARGE_ACTIVE_BOUNDS, (int)(var5 + var2), var6 - 70);
      GlStateManager.resetColor();
      GlStateManager.color(1.0F, 1.0F, 1.0F, (float)Math.sin(Math.PI * var8) * 0.5F);
      this.drawChargeText(CHARGE_BLINK_BOUNDS, (int)(var5 + var2), var6 - 70);
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

   void drawChargeText(Rectangle var1, int var2, int var3) {
      this.drawTexturedModalRect(var2, var3, var1.x, var1.y, var1.width, var1.height);
   }

}
