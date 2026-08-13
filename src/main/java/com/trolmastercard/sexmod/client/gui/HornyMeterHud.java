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

   public static void setHornyMeterVisible(boolean var0) {
      if (!isVisible) {
         resetHornyMeter();
         isVisible = true;
         displayState = var0;
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
   public void onRenderGameOverlay(RenderGameOverlayEvent var1) {
      if (isVisible && var1.getType() == ElementType.TEXT) {
         Minecraft var2 = Minecraft.getMinecraft();
         if (slideInProgress < 1.0F) {
            slideInProgress = slideInProgress + var2.getTickLength() / 25.0F;
         } else {
            slideInProgress = 1.0F;
         }

         GL11.glPushMatrix();
         var2.renderEngine.bindTexture(BUTTON_TEXTURE);
         GL11.glScalef(0.35F, 0.35F, 0.35F);
         if (meterValue >= 1.0) {
            if (HandlePlayerMovement.isInAction) {
               isExpanded = true;
            }

            int var3 = isExpanded ? 54 : 0;
            this.drawTexturedModalRect(240, 160, 0, 108 + var3, 256, 52);
         }

         if (displayState && !isExpanded) {
            int var7 = HandlePlayerMovement.isJumping ? 54 : 0;
            this.drawTexturedModalRect((int)RotationHelper.lerp(-200.0F, 98.0F, slideInProgress), 405, 0, var7, 158, 54);
         }

         GL11.glScalef(2.857143F, 2.857143F, 2.857143F);
         var2.renderEngine.bindTexture(METER_TEXTURE);
         GL11.glScalef(0.75F, 0.75F, 0.75F);
         this.drawTexturedModalRect(10, (int)RotationHelper.lerp(-200.0F, 10.0F, slideInProgress), 0, 0, 146, 175);
         smoothedMeter = RotationHelper.b(smoothedMeter, meterValue, var2.getTickLength());
         int var8 = (int)RotationHelper.b(0.0, 160.0, smoothedMeter);
         int var4 = (int)RotationHelper.b(167.0, 8.0, smoothedMeter);
         double var5 = RotationHelper.b(178.0, 18.0, smoothedMeter);
         if (!isExpanded) {
            this.drawTexturedModalRect(67, (int)RotationHelper.b(-45.0, var5, slideInProgress), 159, var4, 32, var8);
            this.drawTexturedModalRect(
               120,
               (int)RotationHelper.b(-58.0, RotationHelper.b(178.0, 149.0, 1.0 - smoothedMeter), slideInProgress),
               212,
               (int)RotationHelper.b(169.0, 141.0, 1.0 - smoothedMeter),
               28,
               (int)RotationHelper.b(1.0, 29.0, 1.0 - smoothedMeter)
            );
            this.drawTexturedModalRect(
               18,
               (int)RotationHelper.b(-58.0, RotationHelper.b(178.0, 149.0, 1.0 - smoothedMeter), slideInProgress),
               212,
               (int)RotationHelper.b(169.0, 141.0, 1.0 - smoothedMeter),
               28,
               (int)RotationHelper.b(1.0, 29.0, 1.0 - smoothedMeter)
            );
         } else {
            slideOutProgress = slideOutProgress + var2.getTickLength() / 15.0F;
            this.drawTexturedModalRect(67, (int)RotationHelper.lerp(18.0F, -300.0F, slideOutProgress), 159, 8, 32, 160);
         }

         GL11.glPopMatrix();
      }
   }

   public static void addToHornyMeter(double var0) {
      meterValue += var0;
      meterValue = meterValue > 1.0 ? 1.0 : meterValue;
   }

   public static void resetHornyMeter() {
      meterValue = 0.0;
      isExpanded = false;
   }

}
