package com.trolmastercard.sexmod.client.gui;

import com.trolmastercard.sexmod.util.RotationHelper;
import com.trolmastercard.sexmod.util.HandlePlayerMovement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

/**
 * CLIENT cum meter HUD ("the balls"). Visible during scenes; shows a slide-in
 * meter that fills as the player performs actions
 * ({@link #addToHornyMeter(double)}), and expands when full
 * ({@code meterValue >= 1.0}) to indicate the ending input is available —
 * {@code HandlePlayerMovement} maps jump + full meter to
 * {@code triggerCumAction}.
 * <p>
 * Key API: {@link #showHornyMeter()} (scene start), {@link #resetHornyMeter()}
 * (scene end / between actions), {@link #addToHornyMeter(double)} (per action
 * sound keyframe, e.g. 0.02 slow / 0.04 fast), {@link #hideHornyMeter()}.
 * The meter state is static/global — one meter per client, not per girl.
 * <p>
 * <b>Pitfall:</b> {@link #resetHornyMeter()} must also reset
 * {@link #isExpanded} or the meter stays in its expanded "ending ready" state
 * after the scene ends.
 */
@SideOnly(Side.CLIENT)
public class HornyMeterHud extends Gui {
   static ResourceLocation BUTTON_TEXTURE = new ResourceLocation("sexmod", "textures/gui/buttons.png");
   static ResourceLocation METER_TEXTURE = new ResourceLocation("sexmod", "textures/gui/hornymeter.png");
   public static boolean isVisible = false;
   public static double meterValue = 0.0;
   static double smoothedMeter = meterValue;
   static float slideInProgress = 0.0F;
   static float slideOutProgress = 0.0F;
   static boolean isExpanded = false;
   static boolean displayState = true;

   public static void showHornyMeter() {
      if (!isVisible) {
         resetHornyMeter();
         isVisible = true;
         displayState = true;
      }
   }

   public static void setHornyMeterVisible(boolean visible) {
      if (!isVisible) {
         resetHornyMeter();
         isVisible = true;
         displayState = visible;
      }
   }

   public static void hideHornyMeter() {
      resetHornyMeter();
      isVisible = false;
      displayState = true;
   }

   public static boolean isHornyMeterVisible() {
      return isVisible;
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent event) {
      if (isVisible && event.getType() == ElementType.TEXT) {
         Minecraft mc = Minecraft.getMinecraft();
         if (slideInProgress < 1.0F) {
            slideInProgress = slideInProgress + mc.getTickLength() / 25.0F;
         } else {
            slideInProgress = 1.0F;
         }

         GL11.glPushMatrix();
         mc.renderEngine.bindTexture(BUTTON_TEXTURE);
         GL11.glScalef(0.35F, 0.35F, 0.35F);
         if (meterValue >= 1.0) {
            if (HandlePlayerMovement.isInAction) {
               isExpanded = true;
            }

            int yOffset = isExpanded ? 54 : 0;
            this.drawTexturedModalRect(240, 160, 0, 108 + yOffset, 256, 52);
         }

         if (displayState && !isExpanded) {
            int jumpOffset = HandlePlayerMovement.isJumping ? 54 : 0;
            this.drawTexturedModalRect((int)RotationHelper.lerp(-200.0F, 98.0F, slideInProgress), 405, 0, jumpOffset, 158, 54);
         }

         GL11.glScalef(2.857143F, 2.857143F, 2.857143F);
         mc.renderEngine.bindTexture(METER_TEXTURE);
         GL11.glScalef(0.75F, 0.75F, 0.75F);
         this.drawTexturedModalRect(10, (int)RotationHelper.lerp(-200.0F, 10.0F, slideInProgress), 0, 0, 146, 175);
         smoothedMeter = RotationHelper.lerpDouble(smoothedMeter, meterValue, mc.getTickLength());
         int meterHeight = (int)RotationHelper.lerpDouble(0.0, 160.0, smoothedMeter);
         int meterY = (int)RotationHelper.lerpDouble(167.0, 8.0, smoothedMeter);
         double lerpedY = RotationHelper.lerpDouble(178.0, 18.0, smoothedMeter);
         if (!isExpanded) {
            this.drawTexturedModalRect(67, (int)RotationHelper.lerpDouble(-45.0, lerpedY, slideInProgress), 159, meterY, 32, meterHeight);
            this.drawTexturedModalRect(
               120,
               (int)RotationHelper.lerpDouble(-58.0, RotationHelper.lerpDouble(178.0, 149.0, 1.0 - smoothedMeter), slideInProgress),
               212,
               (int)RotationHelper.lerpDouble(169.0, 141.0, 1.0 - smoothedMeter),
               28,
               (int)RotationHelper.lerpDouble(1.0, 29.0, 1.0 - smoothedMeter)
            );
            this.drawTexturedModalRect(
               18,
               (int)RotationHelper.lerpDouble(-58.0, RotationHelper.lerpDouble(178.0, 149.0, 1.0 - smoothedMeter), slideInProgress),
               212,
               (int)RotationHelper.lerpDouble(169.0, 141.0, 1.0 - smoothedMeter),
               28,
               (int)RotationHelper.lerpDouble(1.0, 29.0, 1.0 - smoothedMeter)
            );
         } else {
            slideOutProgress = slideOutProgress + mc.getTickLength() / 15.0F;
            this.drawTexturedModalRect(67, (int)RotationHelper.lerp(18.0F, -300.0F, slideOutProgress), 159, 8, 32, 160);
         }

         GL11.glPopMatrix();
      }
   }

   public static void addToHornyMeter(double amount) {
      meterValue += amount;
      meterValue = meterValue > 1.0 ? 1.0 : meterValue;
   }

   public static void resetHornyMeter() {
      meterValue = 0.0;
      isExpanded = false;
      // also reset the animation state so a re-shown meter slides in again
      slideInProgress = 0.0F;
      slideOutProgress = 0.0F;
      smoothedMeter = 0.0;
   }

}
